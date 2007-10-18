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
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class IssuePriority
implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issuePriorityID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private IssuePriorityText text;

	protected IssuePriority()
	{
	}

	public IssuePriority(String issuePriorityID){
		if (issuePriorityID == null)
			throw new IllegalArgumentException("issuePriorityID must not be null!");

		this.issuePriorityID = issuePriorityID;
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

	public IssuePriorityText getIssuePriorityText()
	{
		return text;
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