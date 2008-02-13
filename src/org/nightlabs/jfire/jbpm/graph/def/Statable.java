package org.nightlabs.jfire.jbpm.graph.def;

import java.util.List;

/**
 * The {@link Statable} interface is used for Objects whose
 * 'state' is managed by a Jbpm process.
 * <p>
 * Several objects in JFireJbpm operate on this interface.
 * For example {@link ActionHandlerNodeEnter} which will
 * create a {@link State} whenever a node is entered and
 * set it to the appropriate {@link #setState(State)}.
 * </p>
 * 
 * @author Marco Schulze
 */
public interface Statable
{
	/**
	 * @return The Local representation of this Statable where all states are stored.
	 * 	The {@link Statable} itself usually stores only public states.
	 */
	StatableLocal getStatableLocal();
	/**
	 * Set the current {@link State} of this {@link Statable} instance.
	 * <p>
	 * Implementations should also add the given state to the list of states returned in {@link #getStates()}.
	 * </p>
	 * @param state The state to set.
	 */
	void setState(State state);
	/**
	 * @return The current {@link State} of this {@link Statable} instance.
	 */
	State getState();
	/**
	 * @return all {@link State} instances that are created for a public {@link StateDefinition} (see {@link StateDefinition#isPublicState()}) - no matter
	 *		whether they are created by this organisation or by the business partner.
	 */
	List<State> getStates();

	/**
	 * Virtual fetch-group used by implementations that should include the current state.
	 */
	static final String FETCH_GROUP_STATE = "Statable.state";
	/**
	 * Virtual fetch-group used by implementations that should include the current state.
	 */
	static final String FETCH_GROUP_STATES = "Statable.states";
}
