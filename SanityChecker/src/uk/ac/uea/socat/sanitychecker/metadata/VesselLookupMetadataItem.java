package uk.ac.uea.socat.sanitychecker.metadata;

import java.text.ParseException;

import org.apache.log4j.Logger;

import uk.ac.uea.socat.sanitychecker.config.MetadataConfigItem;
import uk.ac.uea.socat.sanitychecker.data.SocatDataRecord;
import uk.ac.uea.socat.sanitychecker.data.datetime.DateTimeException;
import uk.ac.uea.socat.sanitychecker.data.datetime.DateTimeHandler;

/**
 * Implementation of the {@code MetadataItem} class
 * to handle the Vessel Name entry
 */
public class VesselLookupMetadataItem extends MetadataItem {

	/**
	 * Constructs a metadata item object.
	 * @param config The configuration for the metadata item
	 * @param line The line number of the file on which this item occurs. If the item has come from the command line, pass {@code -1}.
 	 * @param messages the set of output message
	 * @param logger The system logger
	 * @throws ParseException If the supplied in value could not be parsed into the correct data type
	 */
	public VesselLookupMetadataItem(MetadataConfigItem config, int line, Logger logger) throws ParseException, DateTimeException {
		super(config, line, logger);
		itCanGenerate = true;
		itCanGenerateFromOneRecord = true;
	}

	/**
	 * If a vessel name was supplied in the metadata,
	 * we'll use that. Otherwise we'll look it up using the
	 * NODC code.
	 */
	@Override
	public void generateValue(DateTimeHandler dateTimeHandler) throws MetadataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRecordForValue(SocatDataRecord record) throws MetadataException {
		// TODO Auto-generated method stub
		
	}

}
