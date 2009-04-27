package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Join;
import org.nightlabs.jfire.issue.id.IssueLocalID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * This class is created for wrapping the issue with its state. It's intended to be used within an organisation.
 * <p>
 * It's based on the design pattern called <a href="https://www.jfire.org/modules/phpwiki/index.php/Design%20Pattern%20XyzLocal">Design Pattern XyzLocal</a>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueLocalID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.StatableLocal"
 * 
 * @jdo.create-objectid-class 
 * 		field-order="organisationID, issueID"
 * 		include-body="id/IssueLocalID.body.inc"
 *
 * @jdo.fetch-group name="IssueLocal.state" fields="state"
 * @jdo.fetch-group name="IssueLocal.states" fields="states"
 *
 * @jdo.fetch-group name="IssueLocal.this" fetch-groups="default" fields="issue, state, states"
 *
 * @jdo.fetch-group name="Issue.issueLocal" fields="issue"
 *
 * @jdo.fetch-group name="StatableLocal.state" fields="state"
 * @jdo.fetch-group name="StatableLocal.states" fields="states"
 */
@PersistenceCapable(
	objectIdClass=IssueLocalID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueLocal")
@FetchGroups({
	@FetchGroup(
		name="IssueLocal.state",
		members=@Persistent(name="state")),
	@FetchGroup(
		name="IssueLocal.states",
		members=@Persistent(name="states")),
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueLocal.FETCH_GROUP_THIS_ISSUE_LOCAL,
		members={@Persistent(name="issue"), @Persistent(name="state"), @Persistent(name="states")}),
	@FetchGroup(
		name="Issue.issueLocal",
		members=@Persistent(name="issue")),
	@FetchGroup(
		name="StatableLocal.state",
		members=@Persistent(name="state")),
	@FetchGroup(
		name="StatableLocal.states",
		members=@Persistent(name="states"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueLocal 
implements Serializable, StatableLocal
{
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IssueLocal.class);

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_ISSUE_LOCAL = "IssueLocal.this";
	
	/**
	 * This is the organisationID to which the issue local belongs. Within one organisation,
	 * all the issue locals have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long issueID;
	
	/**
	 * @jdo.field persistence-modifier="persistent" @!dependent="true"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private State state;
	
	/**
	 * This is the history of <b>ALL</b> {@link State}s with the newest last and the oldest first.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		table="JFireIssueTracking_IssueLocal_states"
	 *		@!dependent-value="true"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireIssueTracking_IssueLocal_states",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<State> states;
	

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long jbpmProcessInstanceId = -1;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean processEnded = false;

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	protected IssueLocal() { }

	/**
	 * 
	 * @param issue
	 */
	public IssueLocal(Issue issue)
	{
		this.issue = issue;
		this.organisationID = issue.getOrganisationID();
		this.issueID = issue.getIssueID();
	}
	
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the issueID.
	 */
	public long getIssueID() {
		return issueID;
	}

	public long getJbpmProcessInstanceId() {
		return jbpmProcessInstanceId;
	}

	public Statable getStatable() {
		return issue;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient List<State> _states = null;

	/**
	 * 
	 */
	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}
	
	/**
	 * 
	 */
	public void setJbpmProcessInstanceId(long jbpmProcessInstanceId) {
		this.jbpmProcessInstanceId = jbpmProcessInstanceId;
	}
	
	/**
	 * 
	 */
	public void setState(State state) {
		this.state = state;
		this.states.add(state);
	}

	/**
	 * 
	 */
	public State getState() {
		return state;
	}
	
	/*
	 * (non-Javadoc)
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Issue is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	@Override
	/*
	 * (non-Javadoc)
	 */
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof IssueLocal))
			return false;

		IssueLocal o = (IssueLocal) obj;

		return 
			Util.equals(this.organisationID, o.organisationID) &&
			Util.equals(this.issueID, o.issueID);
	}

	@Override
	/*
	 * (non-Javadoc)
	 */
	public int hashCode()
	{
		return 
			Util.hashCode(this.organisationID) ^
			Util.hashCode(this.issueID);
		
	}

	@Override
	/**
	 * 
	 */
	public boolean isProcessEnded()
	{
		return processEnded;
	}
	
	@Override
	/**
	 * 
	 */
	public void setProcessEnded()
	{
		processEnded = true;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * 
	 * @param organisationID
	 * @param issueID
	 * @return
	 */
	public static String getPrimaryKey(String organisationID, long issueID)
	{
		return organisationID + '/' + Long.toString(issueID);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	
	/*
	 * (non-Javadoc)
	 */
	protected Set<State> clearStatesBeforeDelete() {
		Set<State> res = new HashSet<State>(this.states);
		res.add(this.state);
		this.state = null;
		this.states.clear();
		return res;
	}
}
