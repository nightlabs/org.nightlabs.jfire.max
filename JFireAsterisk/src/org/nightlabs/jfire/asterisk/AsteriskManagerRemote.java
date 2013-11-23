package org.nightlabs.jfire.asterisk;

import javax.ejb.Remote;

import org.nightlabs.jfire.timer.id.TaskID;

@Remote
public interface AsteriskManagerRemote
{
	void initialise() throws Exception;
	void cleanupAsteriskCallFiles(TaskID taskID) throws Exception;

// I removed this for a reason. AsteriskServer is a subclass of PhoneSystem and PhoneSystems can already be stored.
//	AsteriskServer storeAsteriskServer(
//			AsteriskServer asteriskServer, boolean get, String[] fetchGroups,
//			int maxFetchDepth);
}