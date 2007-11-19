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
 */
public class IssueType
implements Serializable{
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
	 *		dependent-value="true"
	 *		mapped-by="issueType"
	 */
	private Set<IssueSeverityType> severityTypes;
	
	/**
	 * Instances of {@link IssuePriority}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssuePriority"
	 *		dependent-value="true"
	 *		mapped-by="issueType"
	 */
	private Set<IssuePriority> priorities;
	
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
		
		this.severityTypes = new HashSet<IssueSeverityType>();
		this.priorities = new HashSet<IssuePriority>();
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

	public Set<IssueSeverityType> getSeverityTypes() {
		return severityTypes;
	}

	public Set<IssuePriority> getPriorities() {
		return priorities;
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
		if (!(obj instanceof IssuePriority)) return false;
		IssueType o = (IssueType) obj;
		return Util.equals(o.issueTypeID, this.issueTypeID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(issueTypeID);
	}
}
