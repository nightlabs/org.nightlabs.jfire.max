package org.nightlabs.jfire.issue;

import java.io.Serializable;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueLinkID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueLink"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, issueID, issueLinkID"
 * 
 * @jdo.fetch-group name="IssueLink.this" fetch-groups="default" fields="issueLinkType"
 * 
 */ 
public class IssueLink 
implements Serializable
{
	public static final String FETCH_GROUP_THIS = "IssueLink.this";
	
	private static final long serialVersionUID = 1L;
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
	private long issueLinkID;
	
	/**
	 * @jdo.column length="100"
	 */
	private String linkObjectID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueLinkType issueLinkType;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssueLink() {}

	public IssueLink(Issue issue, long issueLinkID, String linkObjectID, IssueLinkType issueLinkType) {
		if (issue == null)
			throw new IllegalArgumentException("issue must not be null!");

		this.organisationID = issue.getOrganisationID();
		this.issueID = issue.getIssueID();
		this.issueLinkID = issueLinkID;
		
		this.linkObjectID = linkObjectID;
		this.issueLinkType = issueLinkType;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public long getIssueID() {
		return issueID;
	}
	
	public long getIssueLinkID() {
		return issueLinkID;
	}
	
	public String getLinkObjectID() {
		return linkObjectID;
	}
	
	public IssueLinkType getIssueLinkType() {
		return issueLinkType;
	}
}
