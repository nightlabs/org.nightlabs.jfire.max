package org.nightlabs.jfire.reporting.oda.jdojs;

import org.eclipse.birt.report.model.elements.interfaces.IOdaDataSetModel;
import org.eclipse.datatools.connectivity.oda.IQuery;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface IJDOJSProxy extends IQuery {

//	public static final String PROPERTY_PREPARE_SCRIPT = "prepareScript";
	public static final String PROPERTY_PREPARE_SCRIPT = IOdaDataSetModel.QUERY_TEXT_PROP;
	public static final String PROPERTY_FETCH_SCRIPT = "fetchScript";
//	public static final String PROPERTY_FETCH_SCRIPT = IOdaDataSetModel.QUERY_TEXT_PROP;
	
	
	public String getPrepareScript();
	public String getFetchScript();

}
