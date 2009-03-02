package org.nightlabs.jfire.voucher.scripting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.trade.ILayout;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
  * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.voucher.scripting.id.VoucherLayoutID"
 *		detachable="true"
 *		table="JFireVoucher_VoucherLayout"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, voucherLayoutID"
 *
 * @jdo.fetch-group name="VoucherLayout.file" fields="fileName, fileTimestamp, fileData"
 *
 * @jdo.query
 * 		name="getVoucherLayoutIdsByFileName"
 * 		query="SELECT JDOHelper.getObjectId(this) WHERE fileName == :fileName"
 */
public class VoucherLayout
implements Serializable, ILayout
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(VoucherLayout.class);

	public static final String FETCH_GROUP_FILE = "VoucherLayout.file";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long voucherLayoutID;

	/**
	 * The original file name without path.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String fileName = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date fileTimestamp = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */
	private byte[] fileData = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherLayout() { }

	public VoucherLayout(String organisationID, long voucherLayoutID)
	{
		this.organisationID = organisationID;
		this.voucherLayoutID = voucherLayoutID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getVoucherLayoutID()
	{
		return voucherLayoutID;
	}

	public String getFileName()
	{
		return fileName;
	}

	public Date getFileTimestamp()
	{
		return fileTimestamp;
	}

	public byte[] getFileData()
	{
		return fileData;
	}

	public void loadFile(File f)
	throws IOException
	{
		logger.info("loadFile(\""+f.getAbsolutePath()+"\"): loading " + f.length() + " bytes into RAM.");

		boolean error = true;
		try {
			DataBuffer db = new DataBuffer(f.length(), f);
			fileData = db.createByteArray();

			fileTimestamp = new Date(f.lastModified());
			fileName = f.getName();

			error = false;
		} finally {
			if (error) { // make sure that in case of an error all the file members are null.
				fileName = null;
				fileTimestamp = null;
				fileData = null;
			}
		}
	}

	/**
	 * @param f If <tt>f</tt> exists and is a directory, the <tt>fileName</tt> will be appended to it.
	 *		If <tt>f</tt> does not exist, it is assumed to be a file which will be created. If it exists
	 *		and is a file, the file will be overwritten! If any directory within the path to the given file
	 *		is missing, it will automatically be created.
	 */
	public void saveFile(File f)
	throws IOException
	{
		if (fileData == null)
			throw new IllegalStateException("This instance of VoucherLayout does not have any data!");

		if (fileName == null)
			throw new NullPointerException("fileName is null! How the hell can this happen when fileData is assigned?!");

		long timestamp = fileTimestamp.getTime();

		File orgF = f;
		if (f.isDirectory()) {
//			f.mkdirs(); // Commented this out, because f.isDirectory() only returns true, if it already exists. Marco.
			f = new File(f, fileName);
		}
		else
			f.getParentFile().mkdirs();

		logger.info("saveFile(\""+orgF.getAbsolutePath()+"\"): creating file \"" + f.getAbsolutePath() + "\" and writing " + fileData.length + " bytes into it.");

		FileOutputStream fout = new FileOutputStream(f);
		try {
			fout.write(fileData);
		} finally {
			fout.close();
		}
		f.setLastModified(timestamp);
	}
}
