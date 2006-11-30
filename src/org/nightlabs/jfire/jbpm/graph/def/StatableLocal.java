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
}
