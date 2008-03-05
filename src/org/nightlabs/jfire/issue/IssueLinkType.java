package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueLinkTypeID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueLinkType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, issueLinkTypeID"
 * 
 * @jdo.fetch-group name="IssueLinkType.this" fetch-groups="default" fields="linkableObjectClassNames, issueLinkTypeName"
 * 
 */ 
public class IssueLinkType 
implements Serializable 
{
	public static final String FETCH_GROUP_THIS = "IssueLinkType.this";
	
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private String issueLinkTypeID;	
	
	/**
	 * String of the referenced object class names.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		dependent-value="true"
	 *		table="JFireIssueTracking_IssueLinkType_linkableObjectClassNames"
	 *
	 * @jdo.join
	 */
	
	private Set<String> linkableObjectClassNames;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueLinkType"
	 */
	private IssueLinkTypeName issueLinkTypeName;
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssueLinkType() {}

	public IssueLinkType(String organisationID, String issueLinkTypeID) {
		this.organisationID = organisationID;
		this.issueLinkTypeID = issueLinkTypeID;
		
		this.linkableObjectClassNames = new HashSet<String>();
		this.issueLinkTypeName = new IssueLinkTypeName(this);
	}
	
	public Set<String> getLinkableObjectClassNames() {
		return linkableObjectClassNames;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getIssueLinkTypeID() {
		return issueLinkTypeID;
	}
	
	public IssueLinkTypeName getIssueLinkTypeName() {
		return issueLinkTypeName;
	}
	
	protected void afterCreateIssueLink(IssueLink newIssueLink) { }
	
	protected void beforeDeleteIssueLink(IssueLink issueLinkToBeDeleted) { }
	protected void afterDeleteIssueLink(IssueLink issueLinkDeleted) { }
}