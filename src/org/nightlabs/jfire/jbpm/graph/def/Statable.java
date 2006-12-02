package org.nightlabs.jfire.jbpm.graph.def;

import java.util.List;

public interface Statable
{
	StatableLocal getStatableLocal();
	void setState(State state);
	State getState();
	List<State> getStates();

	static final String FETCH_GROUP_STATE = "Statable.state";
	static final String FETCH_GROUP_STATES = "Statable.states";
}
