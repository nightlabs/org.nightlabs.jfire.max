/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.layout;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.reporting.Birt;

/**
 * Holds the format and the content of a report rendered by BIRT.
 * 
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
