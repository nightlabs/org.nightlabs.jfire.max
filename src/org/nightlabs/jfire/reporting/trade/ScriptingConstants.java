/**
 * 
 */
package org.nightlabs.jfire.reporting.trade;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptingConstants {

	public static final String SCRIPT_REGISTRY_ITEM_TYPE_ROOT = "JFireReportingTrade-Type";
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_GENERAL = "JFireReportingTrade-General";
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_ORDERDATA = "JFireReportingTrade-OrderData";
//	public static final String SCRIPT_REGISTRY_ITEM_TYPE_REPORTING_SCRIPT = "JFireReporting-Script";
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_INVOICEDATA = "JFireReporting-Category-InvoiceData";

	/**
	 * Used as fallback value for script types where none was specified in the content.xml (of the root element).
	 */
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_TRADE_SCRIPT = "JFireReportingTrade-Script";

	public static final String SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ROOT = "Root";
//	public static final String SCRIPT_REGISTRY_ITEM_ID_CATEGORY_GENERAL = "JFireReporting-Category-General-Root";
//	public static final String SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ORDERDATA = "JFireReporting-Category-OrderData-Root";
//	public static final String SCRIPT_REGISTRY_ITEM_ID_CATEGORY_INVOICEDATA = "JFireReporting-Category-OrderData-Root";
}
