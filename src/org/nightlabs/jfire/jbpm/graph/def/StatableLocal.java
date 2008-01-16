package org.nightlabs.jfire.jbpm.graph.def;

import java.util.List;

import org.jbpm.graph.exe.ProcessInstance;

/**
 * A {@link StatableLocal} should be assigned to a {@link Statable}
 * it also holds the current status and a list of passed status but not only
 * the public ones. Additionally this interface can store a reference to
 * the Jbpm {@link ProcessInstance} that manages this {@link StatableLocal}
 * and its {@link Statable}.
 *  
 * @author Marco Schulze
 */
public interface StatableLocal
{
	/**
	 * @return The {@link Statable} this {@link StatableLocal} is linked to.
	 */
	Statable getStatable();
	/**
	 * Set the current {@link State} of this {@link StatableLocal}.
	 * <p>
	 * Implementations should also add the given state to the list of states returned in {@link #getStates()}. 
	 * </p>
	 * @param state The {@link State} to set.
	 */
	void setState(State state);
	/**
	 * @return The current {@link State} of this {@link StatableLocal}.
	 */
	State getState();

	/**
	 * @return all local {@link State}s (created by this organisation) no matter whether they're public or private.
	 */
	List<State> getStates();

	/**
	 * @return The id of {@link ProcessInstance} associated to this {@link StatableLocal}. 
	 */
	long getJbpmProcessInstanceId();
	/**
	 * Set the id of {@link ProcessInstance} associated to this {@link StatableLocal}.
	 * @param jbpmProcessInstanceId The id to set.
	 */
	void setJbpmProcessInstanceId(long jbpmProcessInstanceId);

	/**
	 * Virtual fetch-group used by implementations that should include the current state. 
	 */
	static final String FETCH_GROUP_STATE = "StatableLocal.state";
	/**
	 * Virtual fetch-group used by implementations that should include the list of states. 
	 */
	static final String FETCH_GROUP_STATES = "StatableLocal.states";

	/**
	 * Indicate whether a process has reached an end state or is still running.
	 * <p>
	 * This method returns <code>true</code> for new object instances; until {@link #setProcessEnded()} is called.
	 * </p>
	 *
	 * @return <code>true</code> if the process has reached and end state, <code>false</code> if it is still alive.
	 * @see #setProcessEnded()
	 */
	boolean isProcessEnded();

	/**
	 * Called by {@link ActionHandlerNodeEnter} when it reached an end-state. After this method has been called,
	 * {@link #isProcessEnded()} must return <code>true</code>.
	 * <p>
	 * You must never call this method directly.
	 * </p>
	 */
	void setProcessEnded();
}
