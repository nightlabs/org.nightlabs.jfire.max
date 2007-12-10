package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssuePriorityID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssuePriority"
 *
 * @jdo.create-objectid-class field-order="organisationID, issuePriorityID"
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.fetch-group name="IssuePriority.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="IssuePriority.this" fetch-groups="default" fields="name"
 */
public class IssuePriority
implements Serializable{
	
	public static final String FETCH_GROUP_THIS = "IssuePriority.this";
	public static final String FETCH_GROUP_NAME = "IssuePriority.name";
	
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issuePriorityID;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issuePriority"
	 */
	private IssuePriorityName name;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssuePriority()
	{
	}

	public IssuePriority(String organisationID, String issuePriorityID){
		if (issuePriorityID == null)
			throw new IllegalArgumentException("issuePriorityID must not be null!");

		this.organisationID = organisationID;
		this.issuePriorityID = issuePriorityID;
		this.name = new IssuePriorityName(this);
	}
	
	public String getOrganisationID() {
		return organisationID;
	}

	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	public void setIssuePriorityID(String issuePriorityID) {
		this.issuePriorityID = issuePriorityID;
	}
	
	/**
	 * @return Returns the issuePriorityID
	 */
	public String getIssuePriorityID()
	{
		return issuePriorityID;
	}

	public IssuePriorityName getIssuePriorityText()
	{
		return name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssuePriority)) return false;
		IssuePriority o = (IssuePriority) obj;
		return Util.equals(o.issuePriorityID, this.issuePriorityID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(issuePriorityID);
	}
}