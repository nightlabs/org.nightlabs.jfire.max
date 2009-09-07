package org.nightlabs.jfire.issue;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.jbpm.JbpmConstantsIssue;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.util.Util;

/**
 * The {@link IssueType} class defines the valid set of {@link IssuePriority}s and {@link IssueSeverityType}s
 * and {@link IssueResolution}s for an {@link Issue} of a given type.
 * <p>
 * Additionally, an {@link IssueType} holds the ProcessDefinition for the workflow of the state transitions
 * of an Issue.
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
	objectIdClass=IssueTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueType")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueType.FETCH_GROUP_NAME,
		members=@Persistent(name="name")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueType.FETCH_GROUP_ISSUE_PRIORITIES,
		members=@Persistent(name="issuePriorities")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueType.FETCH_GROUP_ISSUE_SEVERITY_TYPES,
		members=@Persistent(name="issueSeverityTypes")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueType.FETCH_GROUP_ISSUE_RESOLUTIONS,
		members=@Persistent(name="issueResolutions")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueType.FETCH_GROUP_PROCESS_DEFINITION,
		members=@Persistent(name="processDefinition")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueType.FETCH_GROUP_THIS_ISSUE_TYPE,
		members={@Persistent(name="name"), @Persistent(name="issuePriorities"), @Persistent(name="issueSeverityTypes"), @Persistent(name="issueResolutions"), @Persistent(name="processDefinition")}
	),
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members=@Persistent(name="name")
	)
})
@Queries({
	@javax.jdo.annotations.Query(
		name="getAllIssueTypeIDs",
		value="SELECT JDOHelper.getObjectId(this)"
	),
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueType
implements Serializable, Comparable<IssueType>
{
	private static final long serialVersionUID = 1L;
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_ISSUE_TYPE = "IssueType.this";
	public static final String FETCH_GROUP_NAME = "IssueType.name";
	public static final String FETCH_GROUP_ISSUE_PRIORITIES = "IssueType.issuePriorities";
	public static final String FETCH_GROUP_ISSUE_SEVERITY_TYPES = "IssueType.issueSeverityTypes";
	public static final String FETCH_GROUP_ISSUE_RESOLUTIONS = "IssueType.issueResolutions";
	public static final String FETCH_GROUP_PROCESS_DEFINITION = "IssueType.processDefinition";

	/**
	 * @deprecated Use {@link #getIssueTypeIDs(PersistenceManager)} instead!
	 */
	@Deprecated
	public static final String QUERY_ALL_ISSUETYPE_IDS = "getAllIssueTypeIDs";

	public static Collection<IssueTypeID> getIssueTypeIDs(PersistenceManager pm)
	{
		Query q = pm.newNamedQuery(IssueType.class, "getAllIssueTypeIDs");
		@SuppressWarnings("unchecked")
		Collection<IssueTypeID> result = (Collection<IssueTypeID>) q.execute();
		return result;
	}

	public static final String DEFAULT_ISSUE_TYPE_ID = "Default";

	/**
	 * This is the organisationID to which the issue type belongs. Within one organisation,
	 * all the issue types have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String issueTypeID;

	@Persistent(
		dependent="true",
		mappedBy="issueType",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	private IssueTypeName name;

	/**
	 * Instances of {@link IssueSeverityType}.
	 */
	@Join
	@Persistent(
		table="JFireIssueTracking_IssueType_issueSeverityTypes",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<IssueSeverityType> issueSeverityTypes;

	/**
	 * Instances of {@link IssuePriority}.
	 */
	@Join
	@Persistent(
		table="JFireIssueTracking_IssueType_issuePriorities",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<IssuePriority> issuePriorities;

	/**
	 * Instances of {@link IssuePriority}.
	 */
	@Join
	@Persistent(
		table="JFireIssueTracking_IssueType_issueResolutions",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<IssueResolution> issueResolutions;

	/**
	 * Instances of {@link ProcessDefinition}.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProcessDefinition processDefinition;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean isDefault;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected IssueType() { }

	/**
	 * Constructs a new issue type.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueType</code>.
	 * @param issueTypeID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueType.class</code> to create an id.
	 */
	public IssueType(String organisationID, String issueTypeID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(issueTypeID, "issueTypeID");
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
//		ActionHandlerNodeEnter.register(jbpmProcessDefinition); // has been replaced by AOP (see JFireJbpmAOP)

		// Question: This will fail if called more than once, need to add version ?
		// Answer: No this will not fail, because jBPM saves a new process definition with the same name under a different
		// ID. When looking for a process definition, it always returns the latest one with the name. This way, it can
		// finish started processes with the old version, while new processes use the new version of the process definition.
		// Marco.
		jbpmProcessDefinition.setName(getOrganisationID() + ":IssueType-" + getIssueTypeID());
		this.processDefinition = ProcessDefinition.storeProcessDefinition(getPersistenceManager(), null, jbpmProcessDefinition, jbpmProcessDefinitionURL);
		JbpmConstantsIssue.initStandardProcessDefinition(this.processDefinition);
	}

	public ProcessInstance createProcessInstanceForIssue(Issue issue) {
		JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
		try {
			ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate(getProcessDefinition().getJbpmProcessDefinitionName());
			issue.getStatableLocal().setJbpmProcessInstanceId(processInstance.getId());
			processInstance.getContextInstance().setVariable(AbstractActionHandler.VARIABLE_NAME_STATABLE_ID, JDOHelper.getObjectId(issue).toString());
			ProcessDefinition.createStartState(
					getPersistenceManager(),
					SecurityReflector.getUserDescriptor().getUser(getPersistenceManager()),
					issue,
					processInstance.getProcessDefinition()
			);
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
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(issueTypeID);
	}

	@Override
	public int compareTo(IssueType o) {
		return this.name.getText().compareTo(o.getName().getText());
	}
}
