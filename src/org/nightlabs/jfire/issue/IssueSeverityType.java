package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssueSeverityTypeID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssueSeverityType"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class IssueSeverityType
implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueSeverityTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private IssueSeverityTypeText text;

	protected IssueSeverityType()
	{
	}

	public IssueSeverityType(String issueSeverityTypeID){
		if (issueSeverityTypeID == null)
			throw new IllegalArgumentException("issueSeverityTypeID must not be null!");

		this.issueSeverityTypeID = issueSeverityTypeID;
		this.text = new IssueSeverityTypeText(this);
	}
	
	public void setIssueSeverityTypeID(String issueSeverityTypeID) {
		this.issueSeverityTypeID = issueSeverityTypeID;
	}
	
	/**
	 * @return Returns the issueSeverityTypeID.
	 */
	public String getIssueSeverityTypeID()
	{
		return issueSeverityTypeID;
	}

	public IssueSeverityTypeText getIssueSeverityTypeText()
	{
		return text;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueSeverityType)) return false;
		IssueSeverityType o = (IssueSeverityType) obj;
		return Util.equals(o.issueSeverityTypeID, this.issueSeverityTypeID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(issueSeverityTypeID);
	}
}