/**
 *
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.util.NLLocale;

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
}
