package org.nightlabs.jfire.asterisk;

import javax.ejb.Remote;

import org.nightlabs.jfire.timer.id.TaskID;

@Remote
public interface AsteriskManagerRemote
{
	void initialise() throws Exception;
	void cleanupCallFiles(TaskID taskID) throws Exception;
}