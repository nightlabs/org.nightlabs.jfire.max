package org.nightlabs.jfire.prop.html;

import java.util.ArrayList;
import java.util.List;

import org.nightlabs.htmlcontent.IFCKEditorContent;
import org.nightlabs.htmlcontent.IFCKEditorContentFile;
import org.nightlabs.htmlcontent.IFCKEditorContentFileFactory;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;

/**
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *                          detachable="true"
 *                          table="JFireBase_Prop_HTMLDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="html, files"
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class HTMLDataField extends DataField implements IFCKEditorContent
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String html;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		dependent-element="true"
	 *		null-value="exception"
	 */
	private List<IFCKEditorContentFile> files;

	/**
	 * Create a new HTMLDataField instance.
	 */
	protected HTMLDataField()
	{
		super();
	}

	/**
	 * Create a new HTMLDataField instance.
	 */
	public HTMLDataField(DataBlock block, StructField<? extends DataField> field)
	{
		super(block, field);
		files = new ArrayList<IFCKEditorContentFile>();
	}

	/**
	 * Create a new HTMLDataField instance.
	 */
	public HTMLDataField(String organisationID, long propertySetID, DataField cloneField)
	{
		super(organisationID, propertySetID, cloneField);
		files = new ArrayList<IFCKEditorContentFile>();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet)
	{
		HTMLDataField htmlDataField = new HTMLDataField(
				propertySet.getOrganisationID(),
				propertySet.getPropertySetID(),
				this);
		htmlDataField.setHtml(getHtml());
		htmlDataField.setFiles(new ArrayList<IFCKEditorContentFile>(getFiles()));
		return htmlDataField;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return
			(getHtml() == null || getHtml().isEmpty()) &&
			(getFiles() == null || getFiles().isEmpty());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContent#addFile(org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile)
	 */
	@Override
	public void addFile(IFCKEditorContentFile file)
	{
		files.add(file);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContent#getFile(long)
	 */
	@Override
	public IFCKEditorContentFile getFile(long fileId)
	{
		for (IFCKEditorContentFile file : getFiles())
	        if(file.getFileId() == fileId)
	        	return file;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContent#getFileFactory()
	 */
	@Override
	public IFCKEditorContentFileFactory getFileFactory()
	{
		return new IFCKEditorContentFileFactory() {
			@Override
            public IFCKEditorContentFile createContentFile()
            {
				return new HTMLContentFile();
            }
		};
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContent#getFiles()
	 */
	@Override
	public List<IFCKEditorContentFile> getFiles()
	{
		return files;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContent#getHtml()
	 */
	@Override
	public String getHtml()
	{
		return html;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContent#setFiles(java.util.List)
	 */
	@Override
	public void setFiles(List<IFCKEditorContentFile> files)
	{
		this.files = files;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContent#setHtml(java.lang.String)
	 */
	@Override
	public void setHtml(String html)
	{
		this.html = html;
	}
}
