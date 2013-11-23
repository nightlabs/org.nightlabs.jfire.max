   package org.nightlabs.jfire.issue.bug;

import javax.ejb.Remote;

@Remote
public interface IssueBugTrackingManagerRemote 
{
	void initialise() throws Exception;
}