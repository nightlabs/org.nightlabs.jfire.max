package org.nightlabs.jfire.voucher.scripting;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class VoucherScriptingConstants
{
//	public static final String SCRIPT_REGISTRY_ITEM_TYPE_VOUCHER = "voucher";
	public static final String PARAMETER_ID_PERSISTENCE_MANAGER = "persistenceManager";
//	public static final String PARAMETER_ID_VOUCHER_ID = "voucherID";
	public static final String PARAMETER_ID_VOUCHER_KEY_ID = "voucherKeyID";
	
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_TRADE_ROOT = "JFireTrade-Type";
	public static final String SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ROOT = "JFireTrade";
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_TRADE_VOUCHER = "JFireTrade-Type-Voucher";
	public static final String SCRIPT_REGISTRY_ITEM_ID_CATEGORY_VOUCHER = "Voucher";
	
	public static final String VOUCHER_SCRIPTING_ZONE = "VoucherScriptingZone";
	
	public static class OID
	{
		public static final ScriptRegistryItemID SCRIPT_REGISTRY_ITEM_ID_SCRIPT_VOUCHER_KEY =
			ScriptRegistryItemID.create(
					Organisation.DEV_ORGANISATION_ID,
					SCRIPT_REGISTRY_ITEM_TYPE_TRADE_VOUCHER,
					"VoucherKey");
		
		public static final ScriptRegistryItemID SCRIPT_REGISTRY_ITEM_ID_SCRIPT_VOUCHER_NAME =
			ScriptRegistryItemID.create(
					Organisation.DEV_ORGANISATION_ID,
					SCRIPT_REGISTRY_ITEM_TYPE_TRADE_VOUCHER,
					"VoucherName");
		
		public static final ScriptRegistryItemID SCRIPT_REGISTRY_ITEM_ID_SCRIPT_VOUCHER_PRICE =
			ScriptRegistryItemID.create(
					Organisation.DEV_ORGANISATION_ID,
					SCRIPT_REGISTRY_ITEM_TYPE_TRADE_VOUCHER,
					"VoucherPrice");
	}
}
