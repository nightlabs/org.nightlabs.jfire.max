package org.nightlabs.jfire.prop.html;

import java.util.Date;

import org.nightlabs.htmlcontent.IFCKEditorContentFile;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * @jdo.persistence-capable identity-type="application"
 *                          detachable="true"
 *                          table="JFireBase_Prop_HTMLDataFieldFile"
 *                          objectid-class="org.nightlabs.jfire.prop.html.id.HTMLContentFileID"
 *
 * @jdo.create-objectid-class field-order="organisationID, fileId"
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class HTMLContentFile implements IFCKEditorContentFile
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long fileId;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date changeDT;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String contentType;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte[] data;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String description;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String name;
	
	/**
	 * Create a new HTMLContentFile instance.
	 * For JDO only!
	 */
	protected HTMLContentFile()
	{
	}
	
	/**
	 * Create a new HTMLContentFile instance.
	 * @param dataField The parent data field
	 */
	public HTMLContentFile(HTMLDataField dataField)
	{
		this.organisationID = dataField.getOrganisationID();
		this.fileId = IDGenerator.nextID(HTMLContentFile.class);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#getChangeDT()
	 */
	@Override
	public Date getChangeDT()
	{
		return changeDT;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#getContentType()
	 */
	@Override
	public String getContentType()
	{
		return contentType;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#getData()
	 */
	@Override
	public byte[] getData()
	{
		return data;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return description;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#getFileId()
	 */
	@Override
	public long getFileId()
	{
		return fileId;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#isImageFile()
	 */
	@Override
	public boolean isImageFile()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#setData(byte[])
	 */
	@Override
	public void setData(byte[] data)
	{
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description)
	{
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.IFCKEditorContentFile#setName(java.lang.String)
	 */
	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Get the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
}
