package org.nightlabs.jfire.scripting.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.scripting.IScriptParameter;
import org.nightlabs.jfire.scripting.ScriptManagerRemote;
import org.nightlabs.jfire.scripting.ScriptParameter;
import org.nightlabs.jfire.scripting.id.ScriptParameterID;
import org.nightlabs.progress.ProgressMonitor;

public class ScriptParameterDAO extends
		BaseJDOObjectDAO<ScriptParameterID, ScriptParameter> {

	
	private static ScriptParameterDAO sharedInstance;
	
	public static ScriptParameterDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new ScriptParameterDAO();
		return sharedInstance;
	}
	
	
	@Override
	protected Collection<? extends ScriptParameter> retrieveJDOObjects(
			Set<ScriptParameterID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		
		ScriptManagerRemote sm=getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
        List<ScriptParameterID> ids=new ArrayList<ScriptParameterID>(objectIDs);
                                   List<ScriptParameter> l= sm.getScriptParameters(ids, fetchGroups, maxFetchDepth);
	
             return l;
	}
	
	public IScriptParameter storeParameter(
			IScriptParameter scriptParameter,
			boolean get,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
			
	      ){
		    try{
		    	ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
		    	return sm.storeParameter(scriptParameter, get, fetchGroups, maxFetchDepth);
		    	
		    }catch (Exception e){
		    	throw new RuntimeException(e);
		    }
     	
	}
	
	
	

}
