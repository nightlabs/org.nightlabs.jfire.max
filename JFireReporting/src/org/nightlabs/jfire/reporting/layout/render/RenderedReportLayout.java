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

package org.nightlabs.jfire.reporting.layout.render;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

/**
 * Holds the format and the content of a report rendered by BIRT.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class RenderedReportLayout implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static String getDefaultReportFileName()
	{
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		sb.append("renderedLayout");
		sb.append("-");
		sb.append(cal.get(Calendar.YEAR));
		sb.append("-");
		sb.append(cal.get(Calendar.MONTH));
		sb.append("-");
		sb.append(cal.get(Calendar.DAY_OF_MONTH));
		sb.append("-");
		sb.append(cal.get(Calendar.HOUR_OF_DAY));
		sb.append(cal.get(Calendar.MINUTE));
		sb.append(cal.get(Calendar.SECOND));
		return sb.toString();

//		return "renderedLayout";
	}

	/**
	 * The header holds the descriptive information
	 * for a {@link RenderedReportLayout}
	 */
	public class Header implements Serializable {
		private static final long serialVersionUID = 2L;

		private ReportRegistryItemID reportRegistryItemID;
		private Birt.OutputFormat outputFormat;
		private Date timestamp;
		private boolean zipped;
		private boolean multipleFiles;
		private String entryFileName;
		private Collection<Throwable> renderingErrors;

		public Header(ReportRegistryItemID reportRegistryItemID, OutputFormat outputFormat, Date timestamp) {
			this.reportRegistryItemID = reportRegistryItemID;
			this.timestamp = timestamp;
			this.outputFormat = outputFormat;
			this.zipped = false;
			this.multipleFiles = false;
			this.entryFileName = getDefaultReportFileName() + "." + outputFormat.toString();
		}

		/**
		 * @return the outputFormat
		 */
		public Birt.OutputFormat getOutputFormat() {
			return outputFormat;
		}

		/**
		 * @param outputFormat the outputFormat to set
		 */
		public void setOutputFormat(Birt.OutputFormat outputFormat) {
			this.outputFormat = outputFormat;
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

		/**
		 * @return the timestamp
		 */
		public Date getTimestamp() {
			return timestamp;
		}

		/**
		 * @param timestamp the timestamp to set
		 */
		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}

		/**
		 * Returns the file name of the entry of the renderedLayout.
		 * Defaults to {@link RenderedReportLayout#getDefaultReportFileName()}.
		 * @return the entryFileName
		 */
		public String getEntryFileName() {
			return entryFileName;
		}

		/**
		 * @param entryFileName the entryFileName to set
		 */
		public void setEntryFileName(String entryFileName) {
			this.entryFileName = entryFileName;
		}

		/**
		 * Returns whether the data of the {@link RenderedReportLayout}
		 * is zipped (compressed).
		 *
		 * @return the zipped
		 */
		public boolean isZipped() {
			return zipped;
		}

		/**
		 * Set whether the data of the {@link RenderedReportLayout} is/should be zipped.
		 * <p>
		 * Note that while {@link #isMultipleFiles()} is <code>true</code> this method will not
		 * touch the value of zipped.
		 * </p>
		 * 
		 * @param zipped the zipped to set
		 */
		public void setZipped(boolean zipped) {
			if (!isMultipleFiles()) {
				this.zipped = zipped;
			}
		}

		/**
		 * Returns the collection of BIRT errors that occurred
		 * during the rendering of the referenced layout.
		 *
		 * @return The collection of BIRT errors that occurred during rendering.
		 */
		public Collection<Throwable> getRenderingErrors() {
			return renderingErrors;
		}

		/**
		 * Set the rendering errors.
		 * @param renderingErrors The rendering errors to set.
		 */
		public void setRenderingErrors(Collection<Throwable> renderingErrors) {
			this.renderingErrors = renderingErrors;
		}

		/**
		 * Returns whether the rendering of the associated layout
		 * produced BIRT errors. If this returns <code>true</code>
		 * the errors can be accessed via {@link #getRenderingErrors()}.
		 *
		 * @return Returns whether the rendering of the associated layout
		 * 		produced BIRT errors
		 */
		public boolean hasRenderingErrors() {
			return renderingErrors != null && renderingErrors.size() > 0;
		}

		/**
		 * Returns whether the {@link RenderedReportLayout} consists of multiple files, if
		 * this is <code>true</code> {@link #isZipped()} has to be <code>true</code> as well, in
		 * order for a {@link RenderedReportLayout} to be transfered correctly.
		 * 
		 * @return Whether the {@link RenderedReportLayout} consists of multiple files.
		 */
		public boolean isMultipleFiles() {
			return multipleFiles;
		}

		/**
		 * Define whether this {@link RenderedReportLayout} consists of multiple files (like html
		 * file with images).
		 * <p>
		 * Note, that a value of <code>true</code> forces {@link #isZipped()} to be
		 * <code>true</code> as well. Setting a value of <code>false</code> here leaves
		 * {@link #isZipped()} untouched.
		 * </p>
		 * 
		 * @param multipleFiles Whether this {@link RenderedReportLayout} consists of multiple
		 *            files.
		 */
		public void setMultipleFiles(boolean multipleFiles) {
			this.multipleFiles = multipleFiles;
			if (multipleFiles) {
				zipped = true;
			}
		}
	}

	private Header header;

	private Serializable data;

	/**
	 *
	 */
	public RenderedReportLayout(ReportRegistryItemID itemID, OutputFormat format, Date timestamp) {
		super();
		header = new Header(itemID, format, timestamp);
	}


	/**
	 * @return the header
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(Header header) {
		this.header = header;
	}

	/**
	 *
	 * @return Returns the data of this RenderedReportLayout
	 */
	public Serializable getData() {
		return data;
	}

	/**
	 * Set the data
	 */
	public void setData(Serializable data) {
		this.data = data;
	}
}
