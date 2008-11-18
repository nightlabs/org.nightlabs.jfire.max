/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

/**
 * Context that is deployed into the jshtml scripts that are exectued for {@link ReportTextPart}s.
 * A new instance of this class is created in each jshtml eval call.
 * It provides the possibility to append text to the scripts result. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ReportTextPartContext {

	private final StringBuilder sb;
	
	/**
	 * Create a new {@link ReportTextPartContext}. 
	 */
	public ReportTextPartContext() {
		sb = new StringBuilder();
	}

	/**
	 * Appends the String representation of 
	 * the given object to the result of the
	 * script this context was created for.
	 * Note that this method will have no 
	 * effect (nothing will be appended) 
	 * when <code>null</code> is passed. 
	 * 
	 * @param obj The object to add to the result.
	 */
	public void print(Object obj) {
		if (obj != null)
			sb.append(obj);
	}
	
	/**
	 * Appends the String representation of
	 * the given object to the result of the 
	 * script this context was created for (see {@link #print(Object)}).
	 * Additionally it will add a line-break (line-break visible in the report output)
	 * to the string.
	 * 
	 * @param obj The object to add to the result.
	 */
	public void println(Object obj) {
		print(obj);
		print("<br/>");
	}
	
	/**
	 * @return The cummulated content of the result of this context.
	 */
	public String getContextResult() {
		return sb.toString();
	}
}
