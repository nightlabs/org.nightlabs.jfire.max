/**
 * 
 */
package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * An extended class of {@link I18nText} that represents the {@link IssueType}'s name. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueTypeNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueTypeID"
 * 
 * @jdo.fetch-group name="IssueType.name" fetch-groups="default" fields="issueType, names"
 */ 
public class IssueTypeName 
extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue type's name belongs. Within one organisation,
	 * all the issue type's names have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
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
	protected IssueTypeName() {}
	
	/**
	 * 
	 * @param issueType
	 */
	public IssueTypeName(IssueType issueType) {
		this.issueType = issueType;
		organisationID = issueType.getOrganisationID();
		issueTypeID = issueType.getIssueTypeID();
	}

	/**
	 * 
	 * @return
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getIssueTypeID() {
		return issueTypeID;
	}
	
	/**
	 * 
	 * @return
	 */
	public IssueType getIssueType() {
		return issueType;
	}

	@Override
	/**
	 * 
	 */
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Override
	/**
	 * 
	 */
	protected String getFallBackValue(String languageID)
	{
		return organisationID + '/' + issueTypeID;
	}
}