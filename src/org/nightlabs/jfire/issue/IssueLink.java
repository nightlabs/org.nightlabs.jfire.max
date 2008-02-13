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
 * @jdo.fetch-group name="IssueLink.this" fetch-groups="default" fields="relation"
 * 
 */ 
public class IssueLink 
implements Serializable{
	
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
	 * @jdo.column length="100"
	 */
	private String issueLinkID;
	
	/**
	 * @jdo.column length="100"
	 */
	private String referencedObjectID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String relation;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssueLink()
	{
	}

	public IssueLink(Issue issue, String issueLinkID, String referencedObjectID, String relation){
		if (issue == null)
			throw new IllegalArgumentException("issue must not be null!");
		if (issueLinkID == null)
			throw new IllegalArgumentException("issueLinkID must not be null!");

		this.organisationID = issue.getOrganisationID();
		this.issueID = issue.getIssueID();
		this.issueLinkID = issueLinkID;
		
		this.referencedObjectID = referencedObjectID;
		this.relation = relation;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public long getIssueID() {
		return issueID;
	}
	
	public String getIssueLinkID() {
		return issueLinkID;
	}
	
	
	public String getReferencedObjectID() {
		return referencedObjectID;
	}
	
	public String getRelation() {
		return relation;
	}
}
