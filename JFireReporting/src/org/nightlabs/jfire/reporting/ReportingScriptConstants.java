package org.nightlabs.jfire.reporting;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * Class with constants for the Reporting datasource scripts.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ReportingScriptConstants {

	/**
	 * The item type for the {@link PropertySet} script.
	 */
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_PROPERTY_SET = "JFireReporting-Scripting-PropertySet";
	/**
	 * The item id for the {@link PropertySet} script.
	 */
	private static final String _SCRIPT_REGISTRY_ITEM_ID_PROPERTY_SET = "PropertySet";

	/**
	 * The {@link ScriptRegistryItemID} for the {@link PropertySet} script.
	 */
	public static final ScriptRegistryItemID SCRIPT_REGISTRY_ITEM_ID_PROPERTY_SET = ScriptRegistryItemID.create(
				Organisation.DEV_ORGANISATION_ID,
				SCRIPT_REGISTRY_ITEM_TYPE_PROPERTY_SET,
				_SCRIPT_REGISTRY_ITEM_ID_PROPERTY_SET);


	protected ReportingScriptConstants() {}
}
