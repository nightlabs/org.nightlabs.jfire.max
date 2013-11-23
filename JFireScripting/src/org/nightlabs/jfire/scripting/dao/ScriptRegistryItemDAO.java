package org.nightlabs.jfire.scripting.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.scripting.ScriptManagerRemote;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.ScriptRegistryItem;
import org.nightlabs.jfire.scripting.id.ScriptParameterSetID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.progress.ProgressMonitor;

public class ScriptRegistryItemDAO extends BaseJDOObjectDAO<ScriptRegistryItemID,ScriptRegistryItem> {

	private static ScriptRegistryItemDAO sharedInstance;
	
	public static ScriptRegistryItemDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ScriptRegistryItemDAO.class) {
				if (sharedInstance == null) {
					sharedInstance = new ScriptRegistryItemDAO();
				}
			}
		}
		return sharedInstance;
	}
	
	@Override
	protected Collection<? extends ScriptRegistryItem> retrieveJDOObjects(
			Set<ScriptRegistryItemID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		// TODO Auto-generated method stub
				ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
		              List<ScriptRegistryItemID> ids=new ArrayList<ScriptRegistryItemID>(objectIDs);
					List<ScriptRegistryItem> l=    sm.getScriptRegistryItems(ids, fetchGroups, maxFetchDepth);
		return l;
	}

	public ScriptRegistryItem getScriptRegistryItem(ScriptRegistryItemID id,String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			return sm.getScriptRegistryItem(id, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Collection<ScriptParameterSet> getScriptParameterSet(Collection<ScriptParameterSetID> ids, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{		
		try{
		      ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
		      return sm.getScriptParameterSets(ids, fetchGroups, maxFetchDepth);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	
	public Collection<ScriptRegistryItemID> getTopLevelScriptRegistryItemIDs()
	{		
		try {
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			return sm.getTopLevelScriptRegistryItemIDs();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public Collection<ScriptRegistryItemID> getScriptRegistryItemIDsForParent (
			ScriptRegistryItemID scriptRegistryItemID)
	{		
		try {
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			return sm.getScriptRegistryItemIDsForParent(scriptRegistryItemID);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public List<ScriptRegistryItem> getScriptRegistryItems(List<ScriptRegistryItemID> scriptRegistryItemIDs, String[] fetchGroups, ProgressMonitor monitor)
	{
		try {
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			return sm.getScriptRegistryItems(scriptRegistryItemIDs, fetchGroups,  NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
	public ScriptRegistryItem storeRegistryItem (
			ScriptRegistryItem reportRegistryItem,
			boolean get,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
		){
		try {
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			return sm.storeRegistryItem(reportRegistryItem, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Collection<String> getlanguages(){
		
		try{
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			return sm.getLanguages();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public Collection<ScriptParameterSetID> getScriptParameterSetID(String organisationID){
		
		try{
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			return sm.getAllScriptParameterSetIDs(organisationID);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
