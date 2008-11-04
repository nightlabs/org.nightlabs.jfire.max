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
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueID;
	
	/**
	 * @jdo.field persistence-modifier="persistent" @!dependent="true"
	 */
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
	private List<State> states;
	

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long jbpmProcessInstanceId = -1;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean processEnded = false;

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	protected IssueLocal() { }

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
	private transient List<State> _states = null;

	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}
	
	public void setJbpmProcessInstanceId(long jbpmProcessInstanceId) {
		this.jbpmProcessInstanceId = jbpmProcessInstanceId;
	}
	
	public void setState(State state) {
		this.state = state;
		this.states.add(state);
	}

	public State getState() {
		return state;
	}
	
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Issue is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	@Override
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
	public int hashCode()
	{
		return 
			Util.hashCode(this.organisationID) ^
			Util.hashCode(this.issueID);
		
	}

	@Override
	public boolean isProcessEnded()
	{
		return processEnded;
	}
	@Override
	public void setProcessEnded()
	{
		processEnded = true;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	public static String getPrimaryKey(String organisationID, long issueID)
	{
		return organisationID + '/' + Long.toString(issueID);
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	protected Set<State> clearStatesBeforeDelete() {
		Set<State> res = new HashSet<State>(this.states);
		res.add(this.state);
		this.state = null;
		this.states.clear();
		return res;
	}
}
