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
 * It should be used in like the following example inside a dynamic text part in a report layout.
 * <pre>
 * eval(ReportTextPartHelper.getEvalString(JFireReportingHelper.getDataSetParamObject(myVar), "reportTextPartID"));
 * </pre>
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
	 * @param linkedObjectID
	 * @param reportTextPartID
	 * @return
	 */
	public static String getEvalString(ObjectID linkedObjectID, String reportTextPartID) {
		PersistenceManager pm = JFireReportingHelper.getPersistenceManager();
		ReportRegistryItemID reportRegistryItemID = (ReportRegistryItemID) JDOHelper.getObjectId(JFireReportingHelper.getReportLayout());
		if (logger.isDebugEnabled()) {
			logger.debug(ReportTextPartHelper.class.getSimpleName() + " started with the following parameters: " + 
					"(linkedObjectID = " + linkedObjectID + ", " + 
					"reportTextPartID = " + reportTextPartID + ")");
		}
		ReportTextPartConfiguration configuration = ReportTextPartConfiguration.getReportTextPartConfiguration(pm, linkedObjectID, reportRegistryItemID);
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
	 * 
	 * @param locale
	 * @return
	 */
	public static String getEvalString(ReportTextPart reportTextPart, Locale locale) {
		if (!reportTextPart.getContent().containsLanguageID(locale.getLanguage()))
			return "";
		String contentStr = reportTextPart.getContent().getText(locale.getLanguage());
		if (reportTextPart.getType() == Type.HTML) {
			contentStr = "\"" + escapeEvalString(contentStr, 1) + "\"";
			// TODO: Later we can split for special images (or similar elements)
			// and have the surrounding html wrapped in an inner eval(escapeEvalString(, 2)) + codeForVariable + eval(escapeEvalString(, 2)
		} else if (reportTextPart.getType() == Type.JAVASCRIPT) {
			contentStr = escapeEvalString(contentStr, 1);
		}
		return contentStr;
	}
	
	private static String escapeEvalString(String strToEval, int level) {
		String escaped = strToEval;
		for (int i = 0; i < level; i++) {
			escaped = escaped.replaceAll("\\\\", "\\\\");
			escaped = escaped.replaceAll("\"", "\\\"");
		}
		return escaped;
	}
	
}
