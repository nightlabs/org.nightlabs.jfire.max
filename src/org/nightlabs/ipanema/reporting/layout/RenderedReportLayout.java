/**
 * 
 */
package org.nightlabs.ipanema.reporting.layout;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.ipanema.reporting.Birt;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class RenderedReportLayout implements Serializable {

	/**
	 * 
	 */
	public RenderedReportLayout() {
		super();
	}
	
	private Date timestamp;
	
	private Birt.OutputFormat outputFormat;
	
	private byte[] data;
	

	/**
	 * @return Returns the outputFormat.
	 */
	public Birt.OutputFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * @param outputFormat The outputFormat to set.
	 */
	public void setOutputFormat(Birt.OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	/**
	 * @return Returns the timestamp.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * 
	 * @return Returns the data of this RenderedReportLayout
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Set the data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
}
