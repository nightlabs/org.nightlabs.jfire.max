package org.nightlabs.jfire.jbpm.graph.def;

import java.util.List;

public interface Statable
{
	StatableLocal getStatableLocal();
	void setState(State state);
	State getState();
	/**
	 * @return all {@link State} instances that are created for a public {@link StateDefinition} (see {@link StateDefinition#isPublicState()}) - no matter
	 *		whether they are created by this organisation or by the business partner.
	 */
	List<State> getStates();

	static final String FETCH_GROUP_STATE = "Statable.state";
	static final String FETCH_GROUP_STATES = "Statable.states";
}
