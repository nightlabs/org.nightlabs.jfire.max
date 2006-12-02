package org.nightlabs.jfire.jbpm.graph.def;

import java.util.List;

public interface StatableLocal
{
	Statable getStatable();
	void setState(State state);
	State getState();
	List<State> getStates();

	long getJbpmProcessInstanceId();
	void setJbpmProcessInstanceId(long jbpmProcessInstanceId);

	static final String FETCH_GROUP_STATE = "StatableLocal.state";
	static final String FETCH_GROUP_STATES = "StatableLocal.states";
}
