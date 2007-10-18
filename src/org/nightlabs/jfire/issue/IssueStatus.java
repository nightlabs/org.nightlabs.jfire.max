package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairatk at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssueStatusID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssueStatus"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class IssueStatus 
implements Serializable{
	
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueStatusID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private String text;

	protected IssueStatus()
	{
	}

	public IssueStatus(String issueStatusID, String text){
		if (issueStatusID == null)
			throw new IllegalArgumentException("issueStatusID must not be null!");

		if (text == null)
			throw new IllegalArgumentException("text must not be null!");

		this.issueStatusID = issueStatusID;
		this.text = text;
	}
	
	public void setIssueStatusID(String issueStatusID) {
		this.issueStatusID = issueStatusID;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * @return Returns the issueStatusID.
	 */
	public String getIssueStatusID()
	{
		return issueStatusID;
	}

	public String getText()
	{
		return text;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueStatus)) return false;
		IssueStatus o = (IssueStatus) obj;
		return Util.equals(o.issueStatusID, this.issueStatusID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(issueStatusID);
	}
}
