package org.nightlabs.jfire.scripting.condition.dao;

import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.scripting.condition.ConditionScriptManager;
import org.nightlabs.jfire.scripting.condition.ConditionScriptManagerUtil;
import org.nightlabs.jfire.scripting.condition.ScriptConditioner;
import org.nightlabs.jfire.scripting.condition.id.ConditionContextProviderID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ScriptConditionerDAO 
{
	private static ScriptConditionerDAO scriptConditionerDAO;
	public static ScriptConditionerDAO sharedInstance() 
	{
		if (scriptConditionerDAO == null)
			scriptConditionerDAO = new ScriptConditionerDAO();
		return scriptConditionerDAO;
	}
	
	protected ScriptConditionerDAO() {
		
	}

	public Map<ScriptRegistryItemID, ScriptConditioner> getScriptConditioners(Map<ScriptRegistryItemID, Map<String, Object>> scriptID2Paramters, int valueLimit,  
			ProgressMonitor monitor) 
	{
		try {
			monitor.beginTask("Getting ScriptContitioners", // TODO: How to localize this, ResouceBundles for the server/ per user ?!? 
					1);						
			ConditionScriptManager csm = ConditionScriptManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Map<ScriptRegistryItemID, ScriptConditioner> scriptID2ScriptConditioner = csm.getScriptConditioner(scriptID2Paramters, valueLimit);
			monitor.worked(1);
			return scriptID2ScriptConditioner;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}			
	}

	public Set<ScriptRegistryItemID> getConditionContextScriptIDs(ConditionContextProviderID conditionContextProviderID, ProgressMonitor monitor)
	{
		try {
			monitor.beginTask("Getting ScriptConditioners", 
					1);			
			ConditionScriptManager csm = ConditionScriptManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Set<ScriptRegistryItemID> scriptIDs = csm.getConditionContextScriptIDs(conditionContextProviderID);
			monitor.worked(1);
			return scriptIDs;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}			
	}

}
