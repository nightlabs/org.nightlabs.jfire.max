/**
 *
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.util.NLLocale;
import org.nightlabs.util.Util;

/**
 * Used as parameter for rendering methods.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class RenderReportRequest implements Serializable {
	private static final long serialVersionUID = 1L;


	private ReportRegistryItemID reportRegistryItemID;
	private OutputFormat outputFormat;
	private Locale locale;
	private Map<String,Object> parameters;

	/**
	 * This constructor must only be used in conjunction with ALL the setters (except for the locale) - otherwise the resulting object cannot be used.
	 */
	public RenderReportRequest() {
	}

	/**
	 * Creates a new {@link RenderReportRequest} with the given parameters and the default locale (of the current java runtime).
	 *
	 * @param reportRegistryItemID the id of the report which shall be rendered - must NOT be <code>null</code>.
	 * @param parameters the parameters - can be <code>null</code> (<code>null</code> will be converted to an empty <code>Map</code>)
	 * @param outputFormat the desired output-format - must NOT be <code>null</code>.
	 */
	public RenderReportRequest(
			ReportRegistryItemID reportRegistryItemID,
			Map<String, Object> parameters
		)
	{
		this.setReportRegistryItemID(reportRegistryItemID);
		this.setParameters(parameters);
		this.setOutputFormat(OutputFormat.pdf);
		this.locale = NLLocale.getDefault();
	}
	
	/**
	 * Creates a new {@link RenderReportRequest} with the given parameters and the default locale (of the current java runtime).
	 *
	 * @param reportRegistryItemID the id of the report which shall be rendered - must NOT be <code>null</code>.
	 * @param parameters the parameters - can be <code>null</code> (<code>null</code> will be converted to an empty <code>Map</code>)
	 * @param outputFormat the desired output-format - must NOT be <code>null</code>.
	 */
	public RenderReportRequest(
			ReportRegistryItemID reportRegistryItemID,
			Map<String, Object> parameters,
			OutputFormat outputFormat
		)
	{
		this.setReportRegistryItemID(reportRegistryItemID);
		this.setParameters(parameters);
		this.setOutputFormat(outputFormat);
		this.locale = NLLocale.getDefault();
	}

	/**
	 * Creates a new {@link RenderReportRequest} with the given parameters.
	 *
	 * @param reportRegistryItemID the id of the report which shall be rendered - must NOT be <code>null</code>.
	 * @param parameters the parameters - can be <code>null</code> (<code>null</code> will be converted to an empty <code>Map</code>)
	 * @param outputFormat the desired output-format - must NOT be <code>null</code>.
	 * @param locale the Locale for the report should be rendered with.
	 */
	public RenderReportRequest(
			ReportRegistryItemID reportRegistryItemID,
			Map<String, Object> parameters,
			OutputFormat outputFormat,
			Locale locale
		)
	{
		this.setReportRegistryItemID(reportRegistryItemID);
		this.setParameters(parameters);
		this.setOutputFormat(outputFormat);
		this.locale = locale;
	}

	/**
	 * @return the outputFormat - should never be <code>null</code>
	 */
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * @param outputFormat the desired output-format - must NOT be <code>null</code>.
	 */
	public void setOutputFormat(OutputFormat outputFormat) {
		if (outputFormat == null)
			throw new IllegalArgumentException("outputFormat must not be null!");

		this.outputFormat = outputFormat;
	}

	/**
	 * @return the parameters - this is NEVER <code>null</code> - a new instance of {@link HashMap} is automatically assigned, if necessary.
	 */
	public Map<String, Object> getParameters()
	{
		if (parameters == null)
			parameters = new HashMap<String, Object>(0);

		return parameters;
	}

	/**
	 * @param parameters the parameters - can be <code>null</code>
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the reportRegistryItemID - should never be <code>null</code>
	 */
	public ReportRegistryItemID getReportRegistryItemID() {
		return reportRegistryItemID;
	}

	/**
	 * @param reportRegistryItemID the id of the report which shall be rendered - must NOT be <code>null</code>.
	 */
	public void setReportRegistryItemID(ReportRegistryItemID reportRegistryItemID)
	{
		if (reportRegistryItemID == null)
			throw new IllegalArgumentException("reportRegistryItemID must not be null!");

		this.reportRegistryItemID = reportRegistryItemID;
	}

	/**
	 * @return The locale that should be used to render the report - this can be <code>null</code>.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale The locale that should be used to render the report
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result
				+ ((outputFormat == null) ? 0 : outputFormat.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime
				* result
				+ ((reportRegistryItemID == null) ? 0 : reportRegistryItemID
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RenderReportRequest) {
			RenderReportRequest other = (RenderReportRequest)obj;
			boolean regItemEqual = false;
			if (other.getReportRegistryItemID() != null && 
					other.getReportRegistryItemID().equals(getReportRegistryItemID()))
				regItemEqual = true;
			boolean paramsEqual =
				(other.getParameters() != null && getParameters() != null) ||
				(other.getParameters() == null && getParameters() == null) ;
			
			if (other.getParameters() != null) {
				if (getParameters() != null) {
					for (Entry<String, Object> oEntry : other.getParameters().entrySet()) {
						Object thisEntry = getParameters().get(oEntry.getKey());
						if (thisEntry == null)
							paramsEqual = false;
						else
							paramsEqual = thisEntry.equals(oEntry.getValue());
						if (!paramsEqual)
							break;
					}
				}
			}
			
			boolean localesEqual = Util.equals(this.getLocale(), other.getLocale());
			
			return regItemEqual && paramsEqual && localesEqual && this.outputFormat == other.outputFormat;
		}
		else
			return false;		
	}

	public RenderReportRequest clone() {
		RenderReportRequest newRequest = new RenderReportRequest();
		newRequest.setReportRegistryItemID(this.getReportRegistryItemID());
		if (this.getOutputFormat() != null)
			newRequest.setOutputFormat(this.getOutputFormat());
		if (this.getLocale() != null)
			newRequest.setLocale(this.getLocale());
		if (this.getParameters() != null) 
			newRequest.setParameters(new HashMap<String, Object>(this.getParameters()));
		return newRequest;
		
	}
}
