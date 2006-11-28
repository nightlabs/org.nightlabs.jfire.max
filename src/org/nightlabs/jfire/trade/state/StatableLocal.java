package org.nightlabs.jfire.trade.state;

import java.util.List;

public interface StatableLocal
{
	Statable getStatable();
	void setState(State state);
	State getState();
	List<State> getStates();
}
