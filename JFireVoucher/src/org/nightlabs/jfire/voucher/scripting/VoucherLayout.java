package org.nightlabs.jfire.voucher.scripting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.trade.editor2d.ILayout;
import org.nightlabs.jfire.voucher.scripting.id.VoucherLayoutID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */@PersistenceCapable(
	objectIdClass=VoucherLayoutID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherLayout")
@FetchGroups(
	@FetchGroup(
		name=VoucherLayout.FETCH_GROUP_FILE,
		members={@Persistent(name="fileName"), @Persistent(name="fileTimestamp"), @Persistent(name="fileData")})
)
@Queries(
	@javax.jdo.annotations.Query(
		name="getVoucherLayoutIdsByFileName",
		value="SELECT JDOHelper.getObjectId(this) WHERE fileName == :fileName")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class VoucherLayout
implements Serializable, ILayout
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(VoucherLayout.class);

	public static final String FETCH_GROUP_FILE = "VoucherLayout.file";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private long voucherLayoutID;

	/**
	 * The original file name without path.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private String fileName = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private Date fileTimestamp = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")

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

	/**
	 * Returns a set of the IDs of all {@link VoucherLayout}s that share the given fileName.
	 *
	 * @param pm The persistence manager to use to execute the query.
	 * @param fileName The fileName of the {@link VoucherLayout}s to retrieve
	 * @return a set of the IDs of all {@link VoucherLayout}s that share the given fileName.
	 */
	public static Set<VoucherLayoutID> getVoucherLayoutIdsByFilename(PersistenceManager pm, String fileName) {
		Query query = pm.newNamedQuery(VoucherLayout.class, "getVoucherLayoutIdsByFileName");
		Collection<VoucherLayoutID> queryResult = (Collection<VoucherLayoutID>) query.execute(fileName);
		return new HashSet<VoucherLayoutID>(queryResult);
	}

	/**
	 * Fills the value fields of this instance with the values in the given source object.
	 *
	 * @param source The source instance to copy the values from.
	 */
	public void copyValuesFrom(VoucherLayout source) {
		this.fileData = Arrays.copyOf(source.fileData, source.fileData.length);
		this.fileName = source.fileName;
		this.fileTimestamp = (Date) source.fileTimestamp.clone();
	}
}
