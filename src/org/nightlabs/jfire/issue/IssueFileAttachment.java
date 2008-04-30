package org.nightlabs.jfire.issue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.util.IOUtil;

/**
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
 * @jdo.inheritance strategy = "new-table"
 *
 * TODO get rid of the "this" fetch-group and think about what you really need! In the attachment (i.e. an instance of this class), you
 * will have to download the descriptive information (filename, timestamp, etc.) very often (every time you open an issue in your UI), but you
 * need to load the "data" field only very very seldom. And the "data" field might contain a huge amount of data (imagine someone attaching
 * a 500 MB log file to an issue!).
 * In the future, please really think about what data you really need and manage your fetch-groups accordingly (minimize traffic and conserve bandwidth!).
 *
 * @jdo.fetch-group name="IssueFileAttachment.date" fields="date"
 * @jdo.fetch-group name="IssueFileAttachment.issue" fields="issue"
 */
public class IssueFileAttachment 
implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(IssueFileAttachment.class);

	public static final String FETCH_GROUP_THIS_FILEATTACHMENT = "IssueFileAttachment.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueFileAttachmentID;

	/**
	 * @!jdo.field persistence-modifier="persistent" collection-type="array" serialized-element="true"
	 * @jdo.field persistence-modifier="persistent" serialized="true"
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
	 * @jdo.field persistence-modifier="none"
	 */
	private transient List<File> _files = new ArrayList<File>();

	public void setFile(File file) {
		this._files.add(file);
	}
	
	/**
	 * @deprecated Only for JDO!!!!!!!!!
	 */
	protected IssueFileAttachment() {
	}

	public IssueFileAttachment(Issue issue, long issueFileAttachmentID){
		this.organisationID = issue.getOrganisationID();
		this.issueID = issue.getIssueID();
		this.issueFileAttachmentID = issueFileAttachmentID;
	}

	public Issue getIssue() {
		return issue;
	}

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

	public String getFileName() {
		return fileName;
	}

	public Date getFileTimestamp() {
		return fileTimestamp;
	}

	public void loadStream(InputStream in, String name) 
	throws IOException 
	{
		loadStream(in, 10 * 1024, new Date(), name);
	}

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
