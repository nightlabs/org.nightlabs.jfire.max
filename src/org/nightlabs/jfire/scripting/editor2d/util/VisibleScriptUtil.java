/**
 * 
 */
package org.nightlabs.jfire.scripting.editor2d.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.scripting.IScript;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.condition.Script;
import org.nightlabs.jfire.scripting.dao.ScriptRegistryDAO;
import org.nightlabs.jfire.scripting.editor2d.ScriptRootDrawComponent;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class VisibleScriptUtil {

	protected VisibleScriptUtil() {}
	
	public static void assignVisibleScriptResults(ScriptRootDrawComponent scriptRootDrawComponent,
			Map<ScriptRegistryItemID, Object> scriptResultMap)
	{
		Map<Long, Script> id2VisibleScript = scriptRootDrawComponent.getVisibleScripts();
		for (Map.Entry<Long, Script> entry : id2VisibleScript.entrySet()) {
			Script script = entry.getValue();
			for (Map.Entry<String, ScriptRegistryItemID> entry2 : script.getVariableName2ScriptID().entrySet()) {
				Object result = scriptResultMap.get(entry2.getValue());
				script.getVariableName2Value().put(entry2.getKey(), result);
			}
		}
		ScriptRegistry scriptRegistry = ScriptRegistryDAO.sharedInstance().getScriptRegistry(
				new String[] {ScriptRegistry.FETCH_GROUP_THIS_SCRIPT_REGISTRY},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
		Map<Long, Boolean> id2Result = getConditonScriptResults(id2VisibleScript, scriptRegistry);
		scriptRootDrawComponent.assignVisibleScriptResults(id2Result);
	}
	
	protected static Map<Long, Boolean> getConditonScriptResults(Map<Long, Script> id2ConditionScript,
			ScriptRegistry scriptRegistry)
	{
		int size = id2ConditionScript.values().size();
		List<IScript> scripts = new ArrayList<IScript>(size);
		List<Map<String, Object>> parameters = new ArrayList<Map<String, Object>>(size);
		for (Map.Entry<Long, Script> entry : id2ConditionScript.entrySet()) {
			Script visibleScript = entry.getValue();
			scripts.add(visibleScript);
			parameters.add(visibleScript.getVariableName2Value());
		}
		try {
			List<Object> results = scriptRegistry.executeScripts(scripts, parameters);
			Map<Long, Boolean> id2Result = new HashMap<Long, Boolean>();
			for (Map.Entry<Long, Script> entry : id2ConditionScript.entrySet()) {
				Script visibleScript = entry.getValue();
				long id = entry.getKey();
				Boolean result = (Boolean) results.get(scripts.indexOf(visibleScript));
				id2Result.put(id, result);
			}
			return id2Result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	

}
