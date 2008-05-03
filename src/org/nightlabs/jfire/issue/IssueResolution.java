package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssueResolutionID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssueResolution"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueResolutionID"
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.fetch-group name="IssueResolution.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="IssueResolution.this" fetch-groups="default" fields="name"
 */
public class IssueResolution
implements Serializable{
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_ISSUE_RESOLUTION = "IssueResolution.this";
	public static final String FETCH_GROUP_NAME = "IssueResolution.name";
	
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
	private String issueResolutionID;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueResolution"
	 */
	private IssueResolutionName name;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssueResolution()
	{
	}

	public IssueResolution(String organisationID, String issueResolutionID){
		if (organisationID == null)
			throw new IllegalArgumentException("organisationID must not be null!");
		if (issueResolutionID == null)
			throw new IllegalArgumentException("issueResolutinoID must not be null!");

		this.organisationID = organisationID;
		this.issueResolutionID = issueResolutionID;
		this.name = new IssueResolutionName(this);
	}
	
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the issueResolutionID
	 */
	public String getIssueResolutionID()
	{
		return issueResolutionID;
	}

	public IssueResolutionName getName()
	{
		return name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueResolution)) return false;
		IssueResolution o = (IssueResolution) obj;
		return Util.equals(o.issueResolutionID, this.issueResolutionID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(issueResolutionID);
	}
}