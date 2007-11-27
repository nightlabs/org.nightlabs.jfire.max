/**
 * 
 */
package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssueTypeID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssueType"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueTypeID"
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.fetch-group name="IssueType.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="IssueType.issuePriorities" fetch-groups="default" fields="issuePriorities"
 * @jdo.fetch-group name="IssueType.issueSeverityTypes" fetch-groups="default" fields="issueSeverityTypes"
 * @jdo.fetch-group name="IssueType.stateDefinition" fetch-groups="default" fields="stateDefinition"
 * @jdo.fetch-group name="IssueType.this" fetch-groups="default" fields="name, issuePriorities, issueSeverityTypes, stateDefinition"
 */
public class IssueType
implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_THIS = "IssueType.this";
	public static final String FETCH_GROUP_NAME = "IssueType.name";
	public static final String FETCH_GROUP_PRIORITIES = "IssueType.issuePriorities";
	public static final String FETCH_GROUP_SEVERITY_TYPES = "IssueType.issueSeverityTypes";
	
	/**
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
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueType"
	 */
	private IssueTypeName name;
	
	/**
	 * Instances of {@link IssueSeverityType}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueSeverityType"
	 *		table="JFireIssueTracking_IssueType_severityTypes"
	 *
	 * @jdo.join
	 */
	private Set<IssueSeverityType> issueSeverityTypes;
	
	/**
	 * Instances of {@link IssuePriority}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssuePriority"
	 *		table="JFireIssueTracking_IssueType_priorities"
	 *
	 * @jdo.join
	 */
	private Set<IssuePriority> issuePriorities;
	
	/**
	 * Instances of {@link StateDefinition}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 */
	private StateDefinition stateDefinition;
	
	/**
	 * @deprecated Only for JDO!!!! 
	 */
	protected IssueType() {
	}
	
	public IssueType(String organisationID, String issueTypeID) {
		this.organisationID = organisationID;
		this.issueTypeID = issueTypeID;
		
		this.issueSeverityTypes = new HashSet<IssueSeverityType>();
		this.issuePriorities = new HashSet<IssuePriority>();
		this.stateDefinition = null;
		
		name = new IssueTypeName(this);
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getIssueTypeID() {
		return issueTypeID;
	}

	public IssueTypeName getName() {
		return name;
	}

	public void setName(IssueTypeName name) {
		this.name = name;
	}

	public Set<IssueSeverityType> getIssueSeverityTypes() {
		return issueSeverityTypes;
	}

	public Set<IssuePriority> getIssuePriorities() {
		return issuePriorities;
	}

	public StateDefinition getStateDefinition() {
		return stateDefinition;
	}

	public void setStateDefinition(StateDefinition stateDefinition) {
		this.stateDefinition = stateDefinition;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueType)) return false;
		IssueType o = (IssueType) obj;
		return Util.equals(o.issueTypeID, this.issueTypeID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(issueTypeID);
	}
}
