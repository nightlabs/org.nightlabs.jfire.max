/**
 * 
 */
package org.nightlabs.jfire.scripting.editor2d.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.DrawComponentContainer;
import org.nightlabs.editor2d.Layer;
import org.nightlabs.editor2d.PageDrawComponent;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.scripting.IScript;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.condition.Script;
import org.nightlabs.jfire.scripting.condition.VisibleScope;
import org.nightlabs.jfire.scripting.dao.ScriptRegistryDAO;
import org.nightlabs.jfire.scripting.editor2d.ScriptRootDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.ScriptingConstants;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class VisibleScriptUtil {

	protected VisibleScriptUtil() {}
	
//	public static void assignVisibleScriptResults(ScriptRootDrawComponent scriptRootDrawComponent,
//			Map<ScriptRegistryItemID, Object> scriptResultMap)
//	{
//		Map<Long, Script> id2VisibleScript = scriptRootDrawComponent.getVisibleScripts();
//		ScriptRegistry scriptRegistry = ScriptRegistryDAO.sharedInstance().getScriptRegistry(
//				new String[] {ScriptRegistry.FETCH_GROUP_THIS_SCRIPT_REGISTRY},
//				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
//		assignVisibleScriptResults(scriptRootDrawComponent, id2VisibleScript, 
//				scriptResultMap, scriptRegistry);
//	}

	public static void assignVisibleScriptResults(ScriptRootDrawComponent scriptRootDrawComponent,
			Map<ScriptRegistryItemID, Object> scriptResultMap)
	{
		ScriptRegistry scriptRegistry = ScriptRegistryDAO.sharedInstance().getScriptRegistry(
				new String[] {ScriptRegistry.FETCH_GROUP_THIS_SCRIPT_REGISTRY},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());

		Set<Long> alreadyFetchIDs = new HashSet<Long>();
		// first check page visible scripts 
		Map<Long, Script> id2VisibleScript = getVisibleScriptsInRightOrder(
				scriptRootDrawComponent, PageDrawComponent.class);		
		assignVisibleScriptResults(scriptRootDrawComponent, id2VisibleScript, scriptResultMap, scriptRegistry);
		alreadyFetchIDs.addAll(id2VisibleScript.keySet());

		// then all layer visible scripts for each page
		for (DrawComponent dc : scriptRootDrawComponent.getDrawComponents()) {
			id2VisibleScript = getVisibleScriptsInRightOrder(dc, Layer.class);		
			assignVisibleScriptResults(scriptRootDrawComponent, id2VisibleScript, scriptResultMap, scriptRegistry);
			alreadyFetchIDs.addAll(id2VisibleScript.keySet());			
		}
		
		// as last the rest, but not those we already assigned
		id2VisibleScript = scriptRootDrawComponent.getVisibleScripts();
		for (Long id : alreadyFetchIDs) {
			id2VisibleScript.remove(id);
		}
		assignVisibleScriptResults(scriptRootDrawComponent, id2VisibleScript, scriptResultMap, scriptRegistry);
		
	}
	
	protected static void assignVisibleScriptResults(ScriptRootDrawComponent scriptRootDrawComponent,
			Map<Long, Script> id2VisibleScript, Map<ScriptRegistryItemID, Object> scriptResultMap,
			ScriptRegistry scriptRegistry)
	{
			for (Map.Entry<Long, Script> entry : id2VisibleScript.entrySet()) {
				Script script = entry.getValue();
				for (Map.Entry<String, ScriptRegistryItemID> entry2 : script.getVariableName2ScriptID().entrySet()) {
					Object result = scriptResultMap.get(entry2.getValue());
					script.getVariableName2Value().put(entry2.getKey(), result);
				}
			}
			Map<Long, Boolean> id2Result = getConditonScriptResults(id2VisibleScript, scriptRegistry);
			scriptRootDrawComponent.assignVisibleScriptResults(id2Result);
	}
	
	protected static Map<Long, Boolean> getConditonScriptResults(Map<Long, Script> id2ConditionScript,
			ScriptRegistry scriptRegistry)
	{
		int size = id2ConditionScript.values().size();
		List<IScript> scripts = new ArrayList<IScript>(size);
		List<Map<String, Object>> parameters = new ArrayList<Map<String, Object>>(size);
		VisibleScope visibleScope = new VisibleScope();
		for (Map.Entry<Long, Script> entry : id2ConditionScript.entrySet()) {
			Script visibleScript = entry.getValue();
			scripts.add(visibleScript);
			Map<String, Object> variableName2Value = visibleScript.getVariableName2Value();
			variableName2Value.put(VisibleScope.VARIABLE_NAME, visibleScope);
			parameters.add(variableName2Value);
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

	protected static LinkedHashMap<Long, Script> getVisibleScriptsInRightOrder(
			DrawComponent drawComponent, Class<? extends DrawComponent> dcClass) 
	{
		LinkedHashMap<Long, Script> dcID2VisibleScript = new LinkedHashMap<Long, Script>();
		if (drawComponent instanceof DrawComponentContainer) {
			List<DrawComponent> children = ((DrawComponentContainer)drawComponent).getDrawComponents();
			for (DrawComponent dc : children) {
				if (dcClass.isAssignableFrom(dc.getClass())) {
					Script visibleScript = (Script) dc.getProperties().get(ScriptingConstants.PROP_VISIBLE_SCRIPT);
					if (visibleScript != null) {
						dcID2VisibleScript.put(dc.getId(), visibleScript);
					}				
				}
			}			
		}
		return dcID2VisibleScript;
	}
	
}
