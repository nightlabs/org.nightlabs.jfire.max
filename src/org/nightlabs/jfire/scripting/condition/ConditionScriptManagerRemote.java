package org.nightlabs.jfire.scripting.condition;

import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.scripting.condition.id.ConditionContextProviderID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

@Remote
public interface ConditionScriptManagerRemote {

	ScriptConditioner getScriptConditioner(ScriptRegistryItemID scriptRegistryItemID, Map<String, Object> parameterValues, int valueLimit);

	Map<ScriptRegistryItemID, ScriptConditioner> getScriptConditioner(Map<ScriptRegistryItemID, Map<String, Object>> scriptID2Paramters, int valueLimit);

	Set<ScriptRegistryItemID> getConditionContextScriptIDs(ConditionContextProviderID providerID);

}