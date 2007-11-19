/**
 * 
 */
package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueTypeNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.fetch-group name="IssueTypeName.name" fetch-groups="default" fields="issueType, names"
 */ 
public class IssueTypeName 
extends I18nText{

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String issueTypeID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueType issueType;
	
	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		value-type="java.lang.String"
	 *		table="JFireIssueTracking_IssueTypeName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();
	
	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueTypeName() {
	}
	
	public IssueTypeName(IssueType issueType){
		this.issueType = issueType;
		issueTypeID = issueType.getIssueTypeID();
	}
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return issueTypeID == null ? languageID : issueTypeID;
	}
}
