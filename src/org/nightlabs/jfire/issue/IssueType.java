/**
 * 
 */
package org.nightlabs.jfire.issue;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.jfire.issue.jbpm.JbpmConstants;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.util.Util;

/**
 * {@link IssueType} defines the valid set of {@link IssuePriority}s and {@link IssueSeverityType}s 
 * and {@link IssueResolution}s for an {@link Issue} of a given type.
 * <p>
 * Additionally an {@link IssueType} holds the ProcessDefinition for the workflow of the state transitions
 * of an Issue.
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
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
 * @jdo.fetch-group name="IssueType.issueResolutions" fetch-groups="default" fields="issueResolutions"
 * @jdo.fetch-group name="IssueType.processDefinition" fetch-groups="default" fields="processDefinition"
 * @jdo.fetch-group name="IssueType.this" fetch-groups="default" fields="name, issuePriorities, issueSeverityTypes, issueResolutions, processDefinition"
 */
public class IssueType
implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_THIS = "IssueType.this";
	public static final String FETCH_GROUP_NAME = "IssueType.name";
	public static final String FETCH_GROUP_ISSUE_PRIORITIES = "IssueType.issuePriorities";
	public static final String FETCH_GROUP_ISSUE_SEVERITY_TYPES = "IssueType.issueSeverityTypes";
	public static final String FETCH_GROUP_ISSUE_RESOLUTIONS = "IssueType.issueResolutions";
	
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
	 *		table="JFireIssueTracking_IssueType_issueSeverityTypes"
	 *
	 * @jdo.join
	 */
	private List<IssueSeverityType> issueSeverityTypes;
	
	/**
	 * Instances of {@link IssuePriority}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssuePriority"
	 *		table="JFireIssueTracking_IssueType_issuePriorities"
	 *
	 * @jdo.join
	 */
	private List<IssuePriority> issuePriorities;
	
	/**
	 * Instances of {@link IssuePriority}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="IssueResolution"
	 *		table="JFireIssueTracking_IssueType_issueResolutions"
	 *
	 * @jdo.join
	 */
	private List<IssueResolution> issueResolutions;
	
	/**
	 * Instances of {@link ProcessDefinition}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 */
	private ProcessDefinition processDefinition;
	
	/**
	 * @deprecated Only for JDO!!!! 
	 */
	protected IssueType() {
	}
	
	public IssueType(String organisationID, String issueTypeID) {
		this.organisationID = organisationID;
		this.issueTypeID = issueTypeID;
		
		this.issueSeverityTypes = new ArrayList<IssueSeverityType>();
		this.issuePriorities = new ArrayList<IssuePriority>();
		this.issueResolutions = new ArrayList<IssueResolution>();
		this.processDefinition = null;
		
		name = new IssueTypeName(this);
	}

	/**
	 * @return The organisationID of this {@link IssueType}.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return The issueTypeID of this {@link IssueType}.
	 */
	public String getIssueTypeID() {
		return issueTypeID;
	}
	
	/**
	 * @return The name of this {@link IssueType}.
	 */
	public IssueTypeName getName() {
		return name;
	}

	/**
	 * @return The list of valid {@link IssueSeverityType}s a user can choose from
	 * when editing an {@link Issue} of this {@link IssueType}.
	 */
	public List<IssueSeverityType> getIssueSeverityTypes() {
		return issueSeverityTypes;
	}
	/**
	 * @return The list of valid {@link IssuePriority}s a user can choose from
	 * when editing an {@link Issue} of this {@link IssueType}.
	 */
	public List<IssuePriority> getIssuePriorities() {
		return issuePriorities;
	}
	
	/**
	 * @return The list of valid {@link IssueResolution}s a user can choose from
	 * when editing an {@link Issue} of this {@link IssueType}.
	 */
	public List<IssueResolution> getIssueResolutions() {
		return issueResolutions;
	}
	
	/**
	 * 
	 * @return The {@link ProcessDefinition} assigned to this IssueType.
	 */
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}

	/**
	 * Read a {@link ProcessDefinition} from the given URL and store it as new Version
	 * of the {@link ProcessDefinition} of this IssueType.
	 * <p>
	 * New Issues created with this IssueType will have and Process instance according 
	 * to the newly read definition.
	 * </p>
	 * @param jbpmProcessDefinitionURL An URL pointing to a folder containing the definitions 'processdefinition.xml' file.
	 * @throws IOException If reading the URL fails.
	 */
	public void readProcessDefinition(URL jbpmProcessDefinitionURL) throws IOException {
		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);
		ActionHandlerNodeEnter.register(jbpmProcessDefinition);
		// TODO: This will fail if called more than once, need to add version ?
		jbpmProcessDefinition.setName(getOrganisationID() + ":IssueType-" + getIssueTypeID());
		this.processDefinition = ProcessDefinition.storeProcessDefinition(getPersistenceManager(), null, jbpmProcessDefinition, jbpmProcessDefinitionURL);
		JbpmConstants.initStandardProcessDefinition(this.processDefinition);
	}
	
	public ProcessInstance createProcessInstanceForIssue(Issue issue) {
		JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
		try {
			ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate(getProcessDefinition().getJbpmProcessDefinitionName());
			issue.getStatableLocal().setJbpmProcessInstanceId(processInstance.getId());
			processInstance.getContextInstance().setVariable(AbstractActionHandler.VARIABLE_NAME_STATABLE_ID, JDOHelper.getObjectId(issue).toString());
			ActionHandlerNodeEnter.createStartState(
					getPersistenceManager(), SecurityReflector.getUserDescriptor().getUser(getPersistenceManager()), issue, processInstance.getProcessDefinition());
			return processInstance;
		} finally {
			jbpmContext.close();
		}
		
	}
	
	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object. 
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager issueTypePM = JDOHelper.getPersistenceManager(this);
		if (issueTypePM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return issueTypePM;
	}	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueType)) return false;
		IssueType o = (IssueType) obj;
		return
			Util.equals(this.organisationID, o.organisationID) && 
			Util.equals(this.issueTypeID, o.issueTypeID);
	}

	@Override
	public int hashCode()
	{
		return 
			Util.hashCode(organisationID) ^
			Util.hashCode(issueTypeID);
	}
}
