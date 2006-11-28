package org.nightlabs.jfire.trade.state;

import java.util.List;

public interface Statable
{
	StatableLocal getStatableLocal();
	void setState(State state);
	State getState();
	List<State> getStates();
}
