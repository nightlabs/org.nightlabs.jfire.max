package org.nightlabs.jfire.asterisk;

import javax.ejb.Remote;

import org.nightlabs.jfire.timer.id.TaskID;

@Remote
public interface AsteriskManagerRemote
{
	void initialise() throws Exception;
	void cleanupAsteriskCallFiles(TaskID taskID) throws Exception;
	
	AsteriskServer storeAsteriskServer(
			AsteriskServer asteriskServer, boolean get, String[] fetchGroups,
			int maxFetchDepth);
}