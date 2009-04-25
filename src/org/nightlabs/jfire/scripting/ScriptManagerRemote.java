package org.nightlabs.jfire.scripting;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.scripting.id.ScriptParameterSetID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

@Remote
public interface ScriptManagerRemote {

	String ping(String message);

	/**
	 * This method is called by the datastore initialisation mechanism.
	 */
	void initialise();

	ScriptRegistryItem getScriptRegistryItem(ScriptRegistryItemID scriptRegistryItemID, String[] fetchGroups, int maxFetchDepth);

	List<ScriptRegistryItem> getScriptRegistryItems(List<ScriptRegistryItemID> scriptRegistryItemIDs, String[] fetchGroups, int maxFetchDepth);

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	Collection getTopLevelScriptRegistryItems(String organisationID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * @param organisationID The organisationID the carriers should be searched for. If null top level carriers for all organisations are returned.
	 */
	Collection<ScriptRegistryItemCarrier> getTopLevelScriptRegistryItemCarriers(String organisationID);

	ScriptRegistryItem storeRegistryItem(ScriptRegistryItem reportRegistryItem, boolean get, String[] fetchGroups, int maxFetchDepth);

	Collection<ScriptParameterSet> getScriptParameterSets(String organisationID, String[] fetchGroups, int maxFetchDepth);

	Set<ScriptParameterSetID> getAllScriptParameterSetIDs(String organisationID);

	ScriptParameterSet getScriptParameterSet(ScriptParameterSetID scriptParameterSetID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the {@link ScriptParameterSet}s associated with the
	 * {@link ScriptRegistryItem}s referenced by the given {@link ScriptRegistryItemID}s.
	 */
	Collection<ScriptParameterSet> getScriptParameterSetsForScriptRegistryItemIDs(Collection<ScriptRegistryItemID> scriptParameterSetID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the {@link ScriptParameterSet}s for the given {@link ScriptParameterSetID}s.
	 */
	Collection<ScriptParameterSet> getScriptParameterSets(Collection<ScriptParameterSetID> scriptParameterSetIDs, String[] fetchGroups, int maxFetchDepth);

	ScriptParameterSet createParameterSet(I18nText name, String[] fetchGroups, int maxFetchDepth);

	ScriptParameterSet storeParameterSet(ScriptParameterSet scriptParameterSet, boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 *
	 * @param organisationID The organisationID the carriers should be searched for.
	 * If null top level carriers for all organisations are returned.
	 * @param scriptRegistryItemType the scriptRegistryItemType to search for
	 */
	Collection<ScriptRegistryItemCarrier> getTopLevelScriptRegistryItemCarriers(String organisationID, String scriptRegistryItemType);

	/**
	 * returns the detached {@link ScriptRegistry}
	 */
	ScriptRegistry getScriptRegistry(String[] fetchGroups, int maxFetchDepth);

	/**
	 * returns the detached {@link ScriptRegistry}
	 */
	ScriptRegistry getScriptRegistry();
}