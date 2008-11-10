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

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.util.IOUtil;

/**
 * The {@link IssueFileAttachment} class represents an attached file on an {@link Issue}. 
 * <p>
 * 
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssueFileAttachmentID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssueFileAttachment"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueID, issueFileAttachmentID"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IssueFileAttachment.data" fields="data"
 * @jdo.fetch-group name="IssueFileAttachment.issue" fields="issue"
 */
public class IssueFileAttachment 
implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(IssueFileAttachment.class);

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_FILEATTACHMENT = "IssueFileAttachment.this";
	
	public static final String FETCH_GROUP_DATA = "IssueFileAttachment.data";

	/**
	 * This is the organisationID to which the issue file attachment belongs. Within one organisation,
	 * all the issue file attachments have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@SuppressWarnings("unused")
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@SuppressWarnings("unused")
	private long issueID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@SuppressWarnings("unused")
	private long issueFileAttachmentID;

	/**
	 * @jdo.field persistence-modifier="persistent" serialized="true"
	 * @jdo.column sql-type="BLOB"
	 */
	private byte[] data;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date fileTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String fileName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	
	/**
	 * @deprecated Only for JDO!!!!!!!!!
	 */
	protected IssueFileAttachment() {
	}

	/**
	 * Constructs a new IssueFileAttachment.
	 * @param issue the issue that this issue attachment is made in 
	 * @param issueFileAttachmentID a unique id for this issue attachment
	 */
	public IssueFileAttachment(Issue issue, long issueFileAttachmentID){
		this.organisationID = issue.getOrganisationID();
		this.issueID = issue.getIssueID();
		this.issueFileAttachmentID = issueFileAttachmentID;
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

			error = false;
		} finally {
			if (error) { // make sure that in case of an error all the file members are null.
				fileName = null;
				fileTimestamp = null;
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
}
