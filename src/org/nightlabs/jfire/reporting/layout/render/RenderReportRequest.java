/**
 * 
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.Serializable;
import java.util.Map;

import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

/**
 * Used as parameter for rendering methods.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class RenderReportRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private ReportRegistryItemID reportRegistryItemID;
	private OutputFormat outputFormat;
	private Map<String,Object> parameters;
	
	/**
	 * 
	 */
	public RenderReportRequest() {
	}

	/**
	 * 
	 */
	public RenderReportRequest(
			ReportRegistryItemID reportRegistryItemID,
			Map<String, Object> parameters,
			OutputFormat outputFormat
		) 
	{
		this.reportRegistryItemID = reportRegistryItemID;
		this.parameters = parameters;
		this.outputFormat = outputFormat;
	}
	
	/**
	 * @return the outputFormat
	 */
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * @param outputFormat the outputFormat to set
	 */
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the reportRegistryItemID
	 */
	public ReportRegistryItemID getReportRegistryItemID() {
		return reportRegistryItemID;
	}

	/**
	 * @param reportRegistryItemID the reportRegistryItemID to set
	 */
	public void setReportRegistryItemID(ReportRegistryItemID reportRegistryItemID) {
		this.reportRegistryItemID = reportRegistryItemID;
	}
	
	

}
