package org.nightlabs.jfire.dunning;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;

@Remote
public interface DunningManagerRemote 
{
	DunningConfig storeDunningConfig(DunningConfig dunningConfig, boolean get, String[] fetchGroups, int maxFetchDepth);
	
	List<DunningConfig> getDunningConfigs(Collection<DunningConfigID> dunningConfigIDs, String[] fetchGroups, int maxFetchDepth);
	Set<DunningConfigID> getDunningConfigIDs();
	
	List<DunningProcess> getDunningProcesses(Collection<DunningProcessID> dunningProcessIDs, String[] fetchGroups, int maxFetchDepth);
	Set<DunningProcessID> getDunningProcessIDs();
	
	void initialise() throws Exception;
}