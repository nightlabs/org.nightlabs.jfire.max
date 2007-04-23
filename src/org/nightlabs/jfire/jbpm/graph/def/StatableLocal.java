package org.nightlabs.jfire.jbpm.graph.def;

import java.util.List;

public interface StatableLocal
{
	Statable getStatable();
	void setState(State state);
	State getState();

	/**
	 * @return all local {@link State}s (created by this organisation) no matter whether they're public or private.
	 */
	List<State> getStates();

	long getJbpmProcessInstanceId();
	void setJbpmProcessInstanceId(long jbpmProcessInstanceId);

	static final String FETCH_GROUP_STATE = "StatableLocal.state";
	static final String FETCH_GROUP_STATES = "StatableLocal.states";
}
