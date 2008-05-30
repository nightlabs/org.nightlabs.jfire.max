package org.nightlabs.jfire.prop.html;

import java.util.List;

import org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContent;
import org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile;
import org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFileFactory;
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
 * TODO: files...:
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="html,files"
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
	 * @jdo.field persistence-modifier="persistent"
	 * TODO: jdo list
	 */
	private List<IFCKEditorContentFile> files;

	/**
	 * Create a new HTMLDataField instance.
	 */
	public HTMLDataField()
	{
		super();
	}

	/**
	 * Create a new HTMLDataField instance.
	 */
	public HTMLDataField(DataBlock block, StructField<? extends DataField> field)
	{
		super(block, field);
	}

	/**
	 * Create a new HTMLDataField instance.
	 */
	public HTMLDataField(String organisationID, long propertySetID, DataField cloneField)
	{
		super(organisationID, propertySetID, cloneField);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addFile(IFCKEditorContentFile file)
	{
		files.add(file);
	}

	@Override
	public IFCKEditorContentFile getFile(long fileId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFCKEditorContentFileFactory getFileFactory()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IFCKEditorContentFile> getFiles()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHtml()
	{
		return html;
	}

	@Override
	public void setFiles(List<IFCKEditorContentFile> files)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void setHtml(String html)
	{
		this.html = html;
	}
}
