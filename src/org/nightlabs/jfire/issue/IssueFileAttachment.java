package org.nightlabs.jfire.issue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.id.IssueFileAttachmentID;
import org.nightlabs.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IssueFileAttachment} class represents an attached file on an {@link Issue}.
 *
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=IssueFileAttachmentID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueFileAttachment")
@FetchGroups({
	@FetchGroup(
		name=IssueFileAttachment.FETCH_GROUP_DATA,
		members=@Persistent(name="data")),
	@FetchGroup(
		name="IssueFileAttachment.issue",
		members=@Persistent(name="issue"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueFileAttachment
implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(IssueFileAttachment.class);

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_FILEATTACHMENT = "IssueFileAttachment.this";

	public static final String FETCH_GROUP_DATA = "IssueFileAttachment.data";

	/**
	 * This is the organisationID to which the issue file attachment belongs. Within one organisation,
	 * all the issue file attachments have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long issueFileAttachmentID;

	@Persistent(serialized="true", persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] data;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date fileTimestamp;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String fileName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private long fileSize;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Issue issue;


	/**
	 * @deprecated Only for JDO!!!!!!!!!
	 */
	@Deprecated
	protected IssueFileAttachment() {
	}

	/**
	 * Constructs a new IssueFileAttachment.
	 *
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueFileAttachment</code>
	 * @param issueFileAttachmentID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueFileAttachment.class</code> to create an id.
	 * @param issue the issue that this issue attachment is made in
	 */
	public IssueFileAttachment(String organisationID, long issueFileAttachmentID, Issue issue){
		this.organisationID = organisationID;
		this.issueFileAttachmentID = issueFileAttachmentID;
		this.issue = issue;
	}

	/**
	 * @return long
	 */
	public long getIssueFileAttachmentID() {
		return issueFileAttachmentID;
	}

	/**
	 * Returns the issue that this attachment is created for.
	 * @return the issue that this attachment is created for
	 */
	public Issue getIssue() {
		return issue;
	}

	/**
	 * Loads an input stream into byte array.
	 *
	 * @param in the input stream of the file that will be loaded
	 * @param length the byte length of the file
	 * @param timeStamp the date of the loaded time
	 * @param name the name of the file
	 * @throws IOException
	 */
	public void loadStream(InputStream in, long length, Date timeStamp, String name)
	throws IOException
	{
		logger.debug("Loading stream as Issue File Attachment");
		boolean error = true;
		try {
			DataBuffer db = new DataBuffer((long) (length * 0.6));
			OutputStream out = new DeflaterOutputStream(db.createOutputStream());
			try {
				IOUtil.transferStreamData(in, out);
			} finally {
				out.close();
			}
			data = db.createByteArray();

			fileTimestamp = timeStamp;
			fileName = name;
			fileSize = length;	// <-- Since 05.06.2009

			error = false;
		} finally {
			if (error) { // make sure that in case of an error all the file members are null.
				fileName = null;
				fileTimestamp = null;
				fileSize = -1;	// <-- Since 05.06.2009
				data = null;
			}
		}
	}

	/**
	 * Returns the file name.
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Returns the created time of the file.
	 * @return the created time of the file.
	 */
	public Date getFileTimestamp() {
		return fileTimestamp;
	}

	/**
	 * @return the size of the data file in this {@link IssueFileAttachment}.
	 */
	public long getFileSize() { return fileSize; }

	/**
	 *
	 * @param in
	 * @param name
	 * @throws IOException
	 */
	public void loadStream(InputStream in, String name)
	throws IOException
	{
		loadStream(in, 10 * 1024, new Date(), name);
	}

	/**
	 *
	 * @param f
	 * @throws IOException
	 */
	public void loadFile(File f)
	throws IOException
	{
		logger.debug("Loading file "+f+" as Issue File Attachment");
		FileInputStream in = new FileInputStream(f);
		try {
			loadStream(in, f.length(), new Date(f.lastModified()), f.getName());
		} finally {
			in.close();
		}
	}

	/**
	 * Creates a new {@link InputStream} for the file attachment
	 * that is wrapped by an {@link InflaterInputStream}.
	 * This means you can read the file attachment unzipped from the returend stream.
	 */
	public InputStream createFileAttachmentInputStream() {
		return new InflaterInputStream(new ByteArrayInputStream(data));
	}

	/**
	 *
	 * @param file
	 * @throws IOException
	 */
	public void saveFile(File file) throws IOException
	{
		FileOutputStream out = new FileOutputStream(file);
		try {
			InputStream in = createFileAttachmentInputStream();
			try {
				IOUtil.transferStreamData(in, out);
			} finally {
				in.close();
			}
		} finally {
			out.close();
		}
		file.setLastModified(fileTimestamp.getTime());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ (int) (issueFileAttachmentID ^ (issueFileAttachmentID >>> 32));
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IssueFileAttachment other = (IssueFileAttachment) obj;
		if (issueFileAttachmentID != other.issueFileAttachmentID)
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.String#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + issueFileAttachmentID + ']';
	}


}
