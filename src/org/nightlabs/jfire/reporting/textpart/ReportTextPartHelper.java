/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

import java.util.Locale;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.textpart.ReportTextPart.Type;

/**
 * Static helper class that helps to build evaluation strings for {@link ReportTextPart}s inside a report layout.
 * It should be used like the following example inside a dynamic text part in a report layout.
 * <pre>
 * importClass(Packages.org.nightlabs.jfire.reporting.textpart.ReportTextPartHelper);
 * eval("" + ReportTextPartHelper.getEvalString(JFireReportingHelper.getDataSetParamObject(myVar), "reportTextPartID"));
 * </pre>
 * The empty String before the call to {@link #getEvalString(ObjectID, String)} is unfortunately neccessary,
 * otherwise the eval() method for some reason will only echo the String it receives.
 * <p>
 * Of course the import could also be done somewhere else in the reports context but it has to be 
 * made before {@link ReportTextPartHelper} is accessed.
 * </p>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ReportTextPartHelper {

	private static final Logger logger = Logger.getLogger(ReportTextPartHelper.class);
	
	protected ReportTextPartHelper() {}
	
	/**
	 * Returns a String that can be passed to the javascript eval() method and that will
	 * evaluate to the content in the {@link ReportTextPart} found either for the given
	 * linkedObjectID or for the currently rendered report layout. 
	 * 
	 * @param linkedObjectID The ObjectID the {@link ReportTextPartConfiguration} to search should be linked to.
	 * @param reportTextPartID The id of the {@link ReportTextPart} within the configuration.
	 * @return A String that can be passed to the javascript eval() method inside a report layout.
	 */
	public static String getEvalString(ObjectID linkedObjectID, String reportTextPartID) {
		PersistenceManager pm = JFireReportingHelper.getPersistenceManager();
		ReportRegistryItemID reportRegistryItemID = (ReportRegistryItemID) JDOHelper.getObjectId(JFireReportingHelper.getReportLayout());
		if (logger.isDebugEnabled()) {
			logger.debug(ReportTextPartHelper.class.getSimpleName() + " started with the following parameters: " + 
					"(linkedObjectID = " + linkedObjectID + ", " + 
					"reportTextPartID = " + reportTextPartID + ")");
		}
		ReportTextPartConfiguration configuration = ReportTextPartConfiguration.getReportTextPartConfiguration(
				pm, reportRegistryItemID, linkedObjectID,
				false, null, -1);
		if (
				configuration != null &&
				configuration.getReportTextParts().size() != 1 &&
				(reportTextPartID == null || "".equals(reportRegistryItemID)) 
			) {
			throw new IllegalArgumentException(
					"The parameter reportTextPartID is not set and the found configuration, which is " +
					(configuration != null ? ("linked to " + configuration.getLinkedObjectID()) : "null") +
					" does not have exactly one " + 
					ReportTextPart.class.getSimpleName() + " set.");
		}
		
		if (configuration == null)
			throw new IllegalArgumentException(
					"Could not find a " + ReportTextPartConfiguration.class.getSimpleName() + " for the following parameters: " +
					"(linkedObjectID = " + linkedObjectID + ", " + 
					"reportTextPartID = " + reportTextPartID + ").");
		
		if (logger.isDebugEnabled()) {
			logger.debug(ReportTextPartHelper.class.getSimpleName() + " found " + ReportTextPartConfiguration.class.getSimpleName() + " linked to " + configuration.getLinkedObjectID());
		}

		org.nightlabs.jfire.reporting.textpart.ReportTextPart reportTextPart = null;
		if (reportTextPartID == null || "".equals(reportTextPartID)) {
			reportTextPart = configuration.getReportTextParts().get(0);
		} else {
			reportTextPart = configuration.getReportTextPart(reportTextPartID);
		}
		
		if (reportTextPart == null) {
			logger.warn(ReportTextPart.class.getSimpleName() + " with reportTextPartID " + reportTextPartID + " could not be found. The script will not fail, it will return an empty String.");
			return "";
		} else {
			return getEvalString(reportTextPart, JFireReportingHelper.getLocale());
		}
	}
	
	/**
	 * Returns a String that can be passed to the javascript eval() method and that will
	 * evaluate to the content of the given {@link ReportTextPart}.
	 * 
	 * @param locale The locale to localize the {@link ReportTextPart}s content to.
	 * @return A String that can be passed to the javascript eval() method inside a report layout.
	 */
	public static String getEvalString(ReportTextPart reportTextPart, Locale locale) {
		String contentStr = reportTextPart.getContent().getText(locale);
		if (reportTextPart.getType() == Type.JSHTML) {
			ReportTextPartJSHTMLParser parser = new ReportTextPartJSHTMLParser(contentStr, "context", "print");
			parser.parse();
			String result = "var context = new Packages." + ReportTextPartContext.class.getName() + "();\n" +
				parser.getParsedText() + "\n" + 
				"context.getContextResult()";
			if (logger.isDebugEnabled())
				logger.debug("getEvalString() found JSHTML type content and returning: " + result);
			return result;
		}
		return contentStr;
	}
}
