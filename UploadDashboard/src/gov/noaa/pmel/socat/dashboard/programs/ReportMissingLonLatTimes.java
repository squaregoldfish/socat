/**
 * 
 */
package gov.noaa.pmel.socat.dashboard.programs;

import gov.noaa.pmel.socat.dashboard.handlers.DsgNcFileHandler;
import gov.noaa.pmel.socat.dashboard.nc.Constants;
import gov.noaa.pmel.socat.dashboard.nc.CruiseDsgNcFile;
import gov.noaa.pmel.socat.dashboard.server.DashboardDataStore;
import gov.noaa.pmel.socat.dashboard.server.RowNumSet;
import gov.noaa.pmel.socat.dashboard.shared.DashboardUtils;
import gov.noaa.pmel.socat.dashboard.shared.SocatCruiseData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.TreeSet;

/**
 * Reports missing longitude, latitude, or time values in DSG files, 
 * which should not have any missing longitude, latitude, or time values.
 * 
 * @author Karl Smith
 */
public class ReportMissingLonLatTimes {

	// Names of the variables in the DSG files
	private static final String LONGITUDE_NCVAR_NAME = Constants.SHORT_NAMES.get(Constants.longitude_VARNAME);
	private static final String LATITUDE_NCVAR_NAME = Constants.SHORT_NAMES.get(Constants.latitude_VARNAME);
	private static final String TIME_NCVAR_NAME = Constants.SHORT_NAMES.get(Constants.time_VARNAME);
	private static final double REL_TOLER = 1.0E-9;
	private static final double ABS_TOLER = 1.0E-5;

	/**
	 * @param args
	 * 		ExpocodesFile - report missing longitude, latitude, or 
	 * 						time values in these cruises
	 */
	public static void main(String[] args) {
		if ( args.length != 1 ) {
			System.err.println();
			System.err.println("Arguments:  ExpocodesFile");
			System.err.println();
			System.err.println("Reports any missing longitude, latitude, or time values in DSG files ");
			System.err.println("(which should not have any missing longitude, latitude, or time values) "); 
			System.err.println("for the indicated cruises.  The default dashboard configuration is used "); 
			System.err.println("for this process. ");
			System.err.println();
			System.exit(1);
		}

		String expocodesFilename = args[0];

		boolean success = true;

		// Get the default dashboard configuration
		DashboardDataStore dataStore = null;		
		try {
			dataStore = DashboardDataStore.get();
		} catch (Exception ex) {
			System.err.println("Problems reading the default dashboard configuration file: " + ex.getMessage());
			ex.printStackTrace();
			System.exit(1);
		}
		try {
			// Get the expocode of the cruises to update
			TreeSet<String> allExpocodes = new TreeSet<String>();
			try {
				BufferedReader expoReader = new BufferedReader(new FileReader(expocodesFilename));
				try {
					String dataline = expoReader.readLine();
					while ( dataline != null ) {
						dataline = dataline.trim();
						if ( ! ( dataline.isEmpty() || dataline.startsWith("#") ) )
							allExpocodes.add(dataline);
						dataline = expoReader.readLine();
					}
				} finally {
					expoReader.close();
				}
			} catch (Exception ex) {
				System.err.println("Error getting expocodes from " + 
						expocodesFilename + ": " + ex.getMessage());
				ex.printStackTrace();
				System.exit(1);
			}

			DsgNcFileHandler dsgHandler = dataStore.getDsgNcFileHandler();
			RowNumSet missingLons = new RowNumSet();
			RowNumSet missingLats = new RowNumSet();
			RowNumSet missingTimes = new RowNumSet();
			for ( String expocode : allExpocodes ) {
				try {
					CruiseDsgNcFile dsgFile = dsgHandler.getDsgNcFile(expocode);
					double[] longitudes = dsgFile.readDoubleVarDataValues(LONGITUDE_NCVAR_NAME);
					double[] latitudes = dsgFile.readDoubleVarDataValues(LATITUDE_NCVAR_NAME);
					double[] times = dsgFile.readDoubleVarDataValues(TIME_NCVAR_NAME);
					int numObs = longitudes.length;
					if ( latitudes.length != numObs )
						throw new IllegalArgumentException("number of latitudes (" + 
								Integer.toString(latitudes.length) + 
								") does not match the number of longitudes (" + 
								Integer.toString(numObs) + ")");
					if ( times.length != numObs )
						throw new IllegalArgumentException("number of times (" + 
								Integer.toString(times.length) + 
								") does not match the number of longitudes (" + 
								Integer.toString(numObs) + ")");
					missingLons.clear();
					missingLats.clear();
					missingTimes.clear();
					for (int k = 0; k < numObs; k++) {
						if ( DashboardUtils.closeTo(longitudes[k], SocatCruiseData.FP_MISSING_VALUE, REL_TOLER, ABS_TOLER) )
							missingLons.add(k+1);
						if ( DashboardUtils.closeTo(latitudes[k], SocatCruiseData.FP_MISSING_VALUE, REL_TOLER, ABS_TOLER) )
							missingLats.add(k+1);
						if ( DashboardUtils.closeTo(times[k], SocatCruiseData.FP_MISSING_VALUE, REL_TOLER, ABS_TOLER) )
							missingTimes.add(k+1);
					}
					if ( ! missingLons.isEmpty() )
						System.out.println(expocode + " missing longitudes for rows " + missingLons.toString());
					if ( ! missingLats.isEmpty() )
						System.out.println(expocode + " missing latitudes for rows " + missingLats.toString());
					if ( ! missingTimes.isEmpty() )
						System.out.println(expocode + " missing times for rows " + missingTimes.toString());
				} catch (Exception ex) {
					System.err.println("Problems working with " + expocode);
					ex.printStackTrace();
					success = false;
					continue;
				}				
			}
		} finally {
			dataStore.shutdown();
		}
		if ( ! success )
			System.exit(1);
		System.exit(0);
	}

}