/**
 * 
 */
package org.nightlabs.ipanema.reporting;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class Birt {


	public static enum OutputFormat {
		html, pdf
	}
	
	/**
	 * @deprecated do not instantiate this class 
	 */
	protected Birt() {}

	public static OutputFormat parseOutputFormat(String s) {
		if ("html".equals(s))
			return OutputFormat.html;
		else if ("pdf".equals(s))
			return OutputFormat.pdf;
		
		throw new IllegalArgumentException(s+" is not a valid Birt OutputFormat");
	}
	
}
