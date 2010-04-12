package org.nightlabs.jfire.prop.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Value;

import org.nightlabs.htmlcontent.IFCKEditorContent;
import org.nightlabs.htmlcontent.IFCKEditorContentFile;
import org.nightlabs.htmlcontent.IFCKEditorContentFileFactory;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.util.NLLocale;

/**
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *                          detachable="true"
 *                          table="JFireHTMLProp_HTMLDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="texts, files"
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireHTMLProp_HTMLDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members={@Persistent(name="texts"), @Persistent(name="files")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class HTMLDataField extends DataField
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20090116L;

	/**
	 * key: String languageID<br/>
	 * value: String html text
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireHTMLProp_HTMLDataField_texts"
	 *
	 * @jdo.join
	 * @jdo.value-column sql-type="CLOB"
	 */
	@Join
	@Persistent(
		table="JFireHTMLProp_HTMLDataField_texts",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	@Value(
			columns={@Column(sqlType="CLOB")}
	)
	private Map<String, String> texts;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="list"
	 * 		dependent-element="true"
	 *		null-value="exception"
	 *		element-type="org.nightlabs.jfire.prop.html.HTMLContentFile"
	 *		table="JFireHTMLProp_HTMLDataField_files"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		dependentElement="true",
		nullValue=NullValue.EXCEPTION,
		table="JFireHTMLProp_HTMLDataField_files",
		persistenceModifier=PersistenceModifier.PERSISTENT,
		types=HTMLContentFile.class)
	private List<IFCKEditorContentFile> files;

	/**
	 * Create a new HTMLDataField instance.
	 * @deprecated For JDO only
	 */
	@Deprecated
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
		texts = new HashMap<String, String>();
		files = new ArrayList<IFCKEditorContentFile>();
	}

	/**
	 * Create a new HTMLDataField instance.
	 */
	public HTMLDataField(String organisationID, long propertySetID, int dataBlockID, DataField cloneField)
	{
		super(organisationID, propertySetID, dataBlockID, cloneField);
		files = new ArrayList<IFCKEditorContentFile>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet, int)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet, int dataBlockID)
	{
		HTMLDataField htmlDataField = new HTMLDataField(
				propertySet.getOrganisationID(),
				propertySet.getPropertySetID(),
				dataBlockID,
				this);
		htmlDataField.setTexts(new HashMap<String, String>(getTexts()));
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
			(getTexts() == null || getTexts().isEmpty()) &&
			(getFiles() == null || getFiles().isEmpty());
	}

	public void addFile(IFCKEditorContentFile file)
	{
		files.add(file);
	}

	public IFCKEditorContentFile getFile(long fileId)
	{
		for (IFCKEditorContentFile file : getFiles())
	        if(file.getFileId() == fileId)
	        	return file;
		return null;
	}

	public IFCKEditorContentFileFactory getFileFactory()
	{
		return new IFCKEditorContentFileFactory() {
			@Override
            public IFCKEditorContentFile createContentFile()
            {
				return new HTMLContentFile(HTMLDataField.this);
            }
		};
	}

	public List<IFCKEditorContentFile> getFiles()
	{
		return files;
	}

	public void setFiles(List<IFCKEditorContentFile> files)
	{
		this.files = files;
	}

	public Map<String, String> getTexts()
	{
		return texts;
	}

	public void setTexts(Map<String, String> texts)
	{
		this.texts = texts;
	}

	public String getText(String languageId)
	{
		return texts.get(languageId);
	}

	public void setText(String languageId, String html)
	{
		texts.put(languageId, html);
	}

	public String getDefaultLanguage()
	{
		if(texts.isEmpty())
			return null;
		String language = NLLocale.getDefault().getLanguage();
		if(texts.containsKey(language))
			return language;
		language = Locale.getDefault().getLanguage();
		if(texts.containsKey(language))
			return language;
		language = "en";
		if(texts.containsKey(language))
			return language;
		return texts.keySet().iterator().next();
	}

	/**
	 * Inner class to provide a {@link IFCKEditorContent} implementation
	 * that takes care of i18n facilities of {@link HTMLDataField}.
	 */
	private static class HTMLDataFieldFCKEditorContent implements IFCKEditorContent
	{
		private HTMLDataField htmlDataField;
		private String languageId;

		HTMLDataFieldFCKEditorContent(HTMLDataField htmlDataField, String languageId)
		{
			this.htmlDataField = htmlDataField;
			this.languageId = languageId;
		}

		@Override
		public void addFile(IFCKEditorContentFile file)
		{
			htmlDataField.addFile(file);
		}

		@Override
		public IFCKEditorContentFile getFile(long fileId)
		{
			return htmlDataField.getFile(fileId);
		}

		@Override
		public IFCKEditorContentFileFactory getFileFactory()
		{
			return htmlDataField.getFileFactory();
		}

		@Override
		public List<IFCKEditorContentFile> getFiles()
		{
			return htmlDataField.getFiles();
		}

		@Override
		public String getHtml()
		{
			return htmlDataField.getText(languageId);
		}

		@Override
		public void setFiles(List<IFCKEditorContentFile> files)
		{
			htmlDataField.setFiles(files);
		}

		@Override
		public void setHtml(String html)
		{
			htmlDataField.setText(languageId, html);
		}
	}

	private static class HTMLDataFieldContent {

		private Map<String, String> texts;
		private List<IFCKEditorContentFile> files;

		HTMLDataFieldContent(final Map<String, String> texts, final List<IFCKEditorContentFile> files) {
			this.texts = texts;
			this.files = files;
		}

		public Map<String, String> getTexts() {
			return texts;
		}

		public List<IFCKEditorContentFile> getFiles() {
			return files;
		}
	}

	public IFCKEditorContent getContent(String languageId)
	{
		return new HTMLDataFieldFCKEditorContent(this, languageId);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns a new instance of {@link HTMLDataFieldContent} wrapping properties of this data field.
	 */
	@Override
	public Object getData() {
//		return getContent(NLLocale.getDefault().getLanguage());
		return new HTMLDataFieldContent(getTexts(), getFiles());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets the data of this data field according to the data given. In the case an instance of {@link HTMLDataFieldContent} is
	 * given data are set according to the values wrapped by this instance.
	 */
	@Override
	public void setData(Object data) {
		if (data instanceof HTMLDataFieldContent) {
			HTMLDataFieldContent content = (HTMLDataFieldContent) data;
			setTexts(new HashMap<String, String>(content.getTexts()));
			setFiles(new ArrayList<IFCKEditorContentFile>(content.getFiles()));

		}
	}

	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return false;
	}
}
