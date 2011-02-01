package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.nightlabs.jfire.accounting.pay.PayableObjectLocal;
import org.nightlabs.jfire.dunning.id.DunningLetterLocalID;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
	objectIdClass=DunningLetterLocalID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_DunningLetterLocal")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
		name="DunningLetter.dunningLetterLocal",
		members=@Persistent(name="dunningLetter")),
	@FetchGroup(
		name=DunningLetterLocal.FETCH_GROUP_DUNNING_LETTER,
		members=@Persistent(name="dunningLetter")),
	@FetchGroup(
		name="StatableLocal.state",
		members=@Persistent(name="state")),
	@FetchGroup(
		name="StatableLocal.states",
		members=@Persistent(name="states"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningLetterLocal
implements Serializable, PayableObjectLocal
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_DUNNING_LETTER = "DunningLetterLocal.dunningLetter";

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private String dunningLetterID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetter dunningLetter;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private State state;

	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DunningLetterLocal_states",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<State> states;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean processEnded = false;

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
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningLetterLocal() { }

	public DunningLetterLocal(DunningLetter dunningLetter)
	{
		this.organisationID = dunningLetter.getOrganisationID();
		this.dunningLetterID = dunningLetter.getDunningLetterID();
		
		this.dunningLetter = dunningLetter;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getDunningLetterID()
	{
		return dunningLetterID;
	}

	/**
	 * @return the same as {@link #getStatable()}
	 */
	public DunningLetter getDunningLetter()
	{
		return dunningLetter;
	}
	
	public Statable getStatable()
	{
		return dunningLetter;
	}

	public void setState(State currentState)
	{
		if (currentState == null)
			throw new IllegalArgumentException("state must not be null!");

		this.state = currentState;
		this.states.add(currentState);
	}

	public State getState()
	{
		return state;
	}

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient List<State> _states = null;

	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long jbpmProcessInstanceId = -1;

	public long getJbpmProcessInstanceId()
	{
		return jbpmProcessInstanceId;
	}

	public void setJbpmProcessInstanceId(long jbpmProcessInstanceId)
	{
		this.jbpmProcessInstanceId = jbpmProcessInstanceId;
	}
}