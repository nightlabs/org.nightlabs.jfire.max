package org.nightlabs.jfire.prop.file;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.IContentDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField.ContentTypeUtil;
import org.nightlabs.util.IOUtil;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFirePropFile_FileDataField")
	@FetchGroups(
		@FetchGroup(
			fetchGroups={"default"},
			name="FetchGroupsProp.fullData",
			members={@Persistent(name="content"), @Persistent(name="fileTimestamp"), @Persistent(name="fileName")})
	)
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class FileDataField
extends DataField
implements IContentDataField
{
	private static final long serialVersionUID = 1L;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] content;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date fileTimestamp;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String fileName;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String contentType;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String contentEncoding;

	protected FileDataField() { }

	public FileDataField(DataBlock dataBlock, StructField<? extends DataField> structField) {
		super(dataBlock, structField);
	}

	public FileDataField(String organisationID, long propertySetID, int dataBlockID, DataField cloneField) {
		super(organisationID, propertySetID, dataBlockID, cloneField);
	}

	@Override
	public DataField cloneDataField(PropertySet propertySet, int dataBlockID) {
		FileDataField newField = new FileDataField(propertySet.getOrganisationID(), propertySet.getPropertySetID(), dataBlockID, this);
		newField.fileName = this.fileName;
		newField.fileTimestamp = this.fileTimestamp;

		if (this.content != null) {
			newField.content = new byte[this.content.length];
			for (int i = 0; i < this.content.length; i++) {
				newField.content[i] = this.content[i];
			}
			//			newField.content = this.content.clone();
		}
		return newField;
	}

	@Override
	public boolean isEmpty() {
		return content == null;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * Set the content-type of this {@link ImageDataField}.
	 * @param contentType The content-type to set.
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Set the content encoding of this {@link ImageDataField}
	 * @param contentEncoding The encoding to set.
	 */
	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	@Override
	public String getContentEncoding() {
		return contentEncoding;
	}

	@Override
	public byte[] getContent() {
		return content;
	}

	@Override
	public byte[] getPlainContent() {
		if (isEmpty())
			return null;
		if (IContentDataField.CONTENT_ENCODING_PLAIN.equals(getContentEncoding())) {
			return getContent();
		} else if (IContentDataField.CONTENT_ENCODING_DEFLATE.equals(getContentEncoding())) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(getContent().length);
			InflaterOutputStream inflater = new InflaterOutputStream(out);
			try {
				inflater.write(getContent());
			} catch (IOException e) {
				throw new RuntimeException("Could not decode image data", e); //$NON-NLS-1$
			}
			return out.toByteArray();
		}
		throw new IllegalStateException("This ImageDataField was encoded with an unknown encoding type " + getContentEncoding() + ". Can't decode the content"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Set the content. Note, that the content might be encoded, see {@link #setContentEncoding()}.
	 * @param content The content to set
	 */
	public void setContent(byte[] content)
	{
		this.content = content;
	}

	/**
	 * Clear this image data field.
	 */
	public void clear()
	{
		this.content = null;
		this.fileTimestamp = null;
		this.fileName = null;
		this.contentEncoding = null;
		this.contentType = null;
	}

	public void saveToStream(OutputStream out) throws IOException {
		OutputStream stream = out;
		if (IContentDataField.CONTENT_ENCODING_DEFLATE.equals(getContentEncoding())) {
			stream = new InflaterOutputStream(out);
		}
		BufferedOutputStream buf = new BufferedOutputStream(stream);
		buf.write(getContent());
		buf.flush();
		// I don't close any streams here as they are all delegating
		// to the parameter stream and won't allocate resources themselves, I hope ;-)
		// @Tobias: I think the comment above was written by you and I think it is totally correct not to close these streams,
		// because a view in the source code of FilterOutputStream (the super-class of BufferedOutputStream) revealed
		// that it closes its underlying stream. It seems to be a general contract: InflaterOutputStream, DeflaterOutputStream
		// and OutputStreamWriter all behave the same way.
		// Since we didn't open the argument 'out', we must not close it. Marco.
	}

	public void saveToFile(File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			saveToStream(out);
		} finally {
			out.close();
		}
	}

	public File saveToDir(File dir) throws IOException {
		String fName = getFileName();
		if (fName == null) {
			fName =
				"no_name_" + //$NON-NLS-1$
				getOrganisationID() + "_" + ObjectIDUtil.longObjectIDFieldToString(getPropertySetID()) + "_" + //$NON-NLS-1$ //$NON-NLS-2$
				getStructBlockOrganisationID() + "_" + getStructBlockID() + "_" + //$NON-NLS-1$ //$NON-NLS-2$
				getStructFieldOrganisationID() + "_" + getStructFieldID() + "." + ContentTypeUtil.getFileExtension(getContentType()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		File saveFile = new File(dir, fName);
		saveToFile(saveFile);
		return saveFile;
	}

	/**
	 * Load the binary data from the given stream and encodes the data using the deflate algorithm.
	 *
	 * @param in The {@link InputStream} to read from.
	 * @param length The length of the data to read. This is just a hint, it is not treated as the exact length of the data to read.
	 * @param fileTimestamp The time-stamp to set for the data read.
	 * @param fileName The (file)name to set for the data read.
	 * @param contentType The content-type to set for the data read.
	 * @throws IOException If reading the stream fails.
	 */
	public void loadStream(InputStream in, long length, Date fileTimestamp, String fileName, String contentType)
	throws IOException
	{
		boolean error = true;
		try {
			DataBuffer db = new DataBuffer((long) (length * 0.6));
			OutputStream out = new DeflaterOutputStream(db.createOutputStream());
			try {
				IOUtil.transferStreamData(in, out);
			} finally {
				out.close();
			}
			content = db.createByteArray();

			this.fileTimestamp = fileTimestamp;
			this.fileName = fileName;
			this.contentEncoding = IContentDataField.CONTENT_ENCODING_DEFLATE;
			this.contentType = contentType;

			error = false;
		} finally {
			if (error) { // make sure that in case of an error all the file members are null.
				clear();
			}
		}
	}

	/**
	 * Load the binary data of the given stream and store it encoded using the deflate algorithm.
	 *
	 * @param in The {@link InputStream} to read from.
	 * @param name The (file)name to set for the data read.
	 * @param contentType The content-type to set for the data read.
	 * @throws IOException If reading the stream fails.
	 */
	public void loadStream(InputStream in, String name, String contentType)
	throws IOException
	{
		loadStream(in, 10 * 1024, new Date(), name, contentType);
	}

	/**
	 * Load the contents of the given file and store them encoded using the deflate algorithm.
	 *
	 * @param f The file to load the data from.
	 * @param contentType The content-type to set for the data read.
	 * @throws IOException If reading the file fails.
	 */
	public void loadFile(File f, String contentType)
	throws IOException
	{
		FileInputStream in = new FileInputStream(f);
		try {
			loadStream(in, f.length(), new Date(f.lastModified()), f.getName(), contentType);
		} finally {
			in.close();
		}
	}

	/**
	 * Get the (file)name of the data.
	 * Note, that this might not be set to a filename with the correct
	 * extension according to the content-type.
	 *
	 * @return The (file)name of the data.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Set the fileName.
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	/**
	 * Get the time-stamp of the data.
	 * This might represent the time the data was read or
	 * the time-stamp of the file the data was read from.
	 * @return The time-stamp of the data.
	 */
	public Date getFileTimestamp() {
		return fileTimestamp;
	}

	/**
	 * Set the fileTimestamp.
	 * @param fileTimestamp the fileTimestamp to set
	 */
	public void setFileTimestamp(Date fileTimestamp)
	{
		this.fileTimestamp = fileTimestamp;
	}

	private static class FileDataFieldContent {

		private byte[] content;
		private String contentEncoding;
		private String contentType;
		private String fileName;
		private Date fileTimestamp;

		FileDataFieldContent(final byte[] content, final String contentEncoding,
				final String contentType, final String fileName, final Date fileTimestamp) {
			this.content = content;
			this.contentEncoding = contentEncoding;
			this.contentType = contentType;
			this.fileName = fileName;
			this.fileTimestamp = fileTimestamp;
		}

		public byte[] getContent() {
			return content;
		}

		public String getContentEncoding() {
			return contentEncoding;
		}

		public String getContentType() {
			return contentType;
		}

		public String getFileName() {
			return fileName;
		}

		public Date getFileTimestamp() {
			return fileTimestamp;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns a new instance of {@link FileDataFieldContent} wrapping properties of this data field.
	 */
	@Override
	public Object getData() {
		return new FileDataFieldContent(getContent(), getContentEncoding(), getContentType(), getFileName(), getFileTimestamp());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets the data of this data field according to the data given. In the case an instance of {@link FileDataFieldContent} is
	 * given data are set according to the values wrapped by this instance.
	 */
	@Override
	public void setData(Object data) {
		if (data instanceof FileDataFieldContent) {
			FileDataFieldContent dataFieldContent = (FileDataFieldContent) data;
			content = new byte[dataFieldContent.getContent().length];
			for (int i = 0; i < content.length; i++) {
				content[i] = dataFieldContent.getContent()[i];
			}
			contentEncoding = dataFieldContent.getContentEncoding();
			contentType = dataFieldContent.getContentType();
			fileName = dataFieldContent.getFileName();
			fileTimestamp = dataFieldContent.getFileTimestamp();
		}
	}

	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return false;
	}

	@Override
	public String getDescription() {
		return null; // Why isn't this multilingual?!
	}
}
