/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import java.util.Date;
import java.util.Map;

import org.nightlabs.util.TimePeriod;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportingScriptUtil {

	/**
	 * Protected as this class is for static use. 
	 */
	protected ReportingScriptUtil() {
	}
	
	/**
	 * Adds a {@link TimePeriod} condition to a JDOQL query in the form of
	 * <pre>
	 *   && (variable >= :timePeriodVarNameFrom && variable <= :timePeriodVarNameTo) 
	 * </pre>
	 * 
	 * @param jdoql The query to add the condition to.
	 * @param variable The variable name of the {@link Date} field in the query.
	 * @param timePeriodVarName The parameter name to use for the {@link TimePeriod}.
	 * @param timePeriod The {@link TimePeriod} to use.
	 * @param queryParameters The {@link Map} of parameters for the query. The {@link Date} values will be added here.
	 */
	public static void addTimePeriodCondition(
			StringBuffer jdoql,
			String variable, String timePeriodVarName, 
			TimePeriod timePeriod, Map<String, Object> queryParameters
		) 
	{
		if (timePeriod == null || !timePeriod.isConfining())
			return;
		jdoql.append("&& (");
		if (timePeriod.isFromSet())
			jdoql.append(variable+" >= :"+timePeriodVarName+"From");
		if (timePeriod.isToSet()) {
			if (timePeriod.isFromSet())
				jdoql.append(" && ");
			jdoql.append(variable+" <= :"+timePeriodVarName+"To");
		}
		if (timePeriod.isFromSet())
			queryParameters.put(timePeriodVarName+"From", timePeriod.getFrom());
		if (timePeriod.isToSet())
			queryParameters.put(timePeriodVarName+"To", timePeriod.getTo());
		jdoql.append(") ");
	}

}
