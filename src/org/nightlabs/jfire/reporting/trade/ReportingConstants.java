/**
 * 
 */
package org.nightlabs.jfire.reporting.trade;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportingConstants {
	
	public static final String REPORT_REGISTRY_ITEM_TYPE_ROOT = "JFireReportingTrade-Reporting-Root";
	public static final String REPORT_REGISTRY_ITEM_TYPE_GENERAL = "JFireReportingTrade-Reporting-General";
	public static final String REPORT_REGISTRY_ITEM_TYPE_ORDER = "JFireReportingTrade-Reporting-Order";
	public static final String REPORT_REGISTRY_ITEM_TYPE_OFFER = "JFireReportingTrade-Reporting-Offer";
	public static final String REPORT_REGISTRY_ITEM_TYPE_INVOICE = "JFireReportingTrade-Reporting-Invoice";
	public static final String REPORT_REGISTRY_ITEM_TYPE_DELIVERY_NOTE = "JFireReportingTrade-Reporting-Delivery";

	/**
	 * Used as fallback value for script types where none was specified in the content.xml (of the root element).
	 */
	public static final String REPORT_REGISTRY_ITEM_TYPE_TRADE = "JFireReportingTrade-Reporting";

	public static final String REPORT_REGISTRY_ITEM_ID_CATEGORY_ROOT = "JFireReportingTrade-Reporting-Root";

}
