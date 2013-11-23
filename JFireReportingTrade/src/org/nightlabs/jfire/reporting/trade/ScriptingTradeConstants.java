/**
 * 
 */
package org.nightlabs.jfire.reporting.trade;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptingTradeConstants {

	public static final String SCRIPT_REGISTRY_ITEM_TYPE_ROOT = "JFireReportingTrade-Scripting-Root";
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_GENERAL = "JFireReportingTrade-Scripting-General";
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_ARTICLECONTAINER_DATA = "JFireReportingTrade-Scripting-ArticleContainerData";
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_ORDER_DATA = "JFireReportingTrade-Scripting-OrderData";
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_INVOICE_DATA = "JFireReportingTrade-Scripting-InvoiceData";
	

	/**
	 * Used as fallback value for script types where none was specified in the content.xml (of the root element).
	 */
	public static final String SCRIPT_REGISTRY_ITEM_TYPE_SCRIPT_TRADE = "JFireReportingTrade-Scripting";

	public static final String SCRIPT_REGISTRY_ITEM_ID_CATEGORY_TRADE = "JFireReportingTrade-Scripting-Root";
	
}
