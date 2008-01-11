/**
 * 
 */
package org.nightlabs.jfire.reporting.trade;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportingTradeConstants {
	
	public static final String REPORT_REGISTRY_ITEM_TYPE_ROOT = "JFireReportingTrade-Reporting-Root";
	public static final String REPORT_REGISTRY_ITEM_TYPE_GENERAL = "JFireReportingTrade-Reporting-General";
	public static final String REPORT_REGISTRY_ITEM_TYPE_ORDER = "JFireReportingTrade-Reporting-Order";
	public static final String REPORT_REGISTRY_ITEM_TYPE_OFFER = "JFireReportingTrade-Reporting-Offer";
	public static final String REPORT_REGISTRY_ITEM_TYPE_INVOICE = "JFireReportingTrade-Reporting-Invoice";
	public static final String REPORT_REGISTRY_ITEM_TYPE_DELIVERY_NOTE = "JFireReportingTrade-Reporting-DeliveryNote";

	/**
	 * Used as fallback value for script types where none was specified in the content.xml (of the root element).
	 */
	public static final String REPORT_REGISTRY_ITEM_TYPE_TRADE = "JFireReportingTrade-Reporting";

	public static final String REPORT_REGISTRY_ITEM_ID_CATEGORY_ROOT = "JFireReportingTrade-Reporting-Root";
	public static final String REPORT_REGISTRY_ITEM_ID_CATEGORY_GENERAL = "General-Root";
	public static final String REPORT_REGISTRY_ITEM_ID_CATEGORY_INVOICE = "Invoice-Root";
	public static final String REPORT_REGISTRY_ITEM_ID_CATEGORY_ORDER = "Order-Root";
	public static final String REPORT_REGISTRY_ITEM_ID_CATEGORY_OFFER = "Offer-Root";
	public static final String REPORT_REGISTRY_ITEM_ID_CATEGORY_DELIVERY_NOTE = "DeliveryNote-Root";
	
	public static final String REPORT_REGISTRY_ITEM_ID_DEFAULT_INVOICE_LAYOUT = "Default-InvoiceLayout";
	
	
	public static final String VALUE_PROVIDER_CATEGORY_ID_TRADE_REPORTS = "JFireReportingTrade-ValueProviderCategory-TradeReports";

	public static final String VALUE_PROVIDER_CATEGORY_ID_LEGAL_ENTITY = "JFireReportingTrade-ValueProviderCategory-LegalEntity";
	public static final ValueProviderID VALUE_PROVIDER_ID_LEGAL_ENTITY_SEARCH = ValueProviderID.create( 
			Organisation.DEV_ORGANISATION_ID, VALUE_PROVIDER_CATEGORY_ID_LEGAL_ENTITY, "Search");
	public static final ValueProviderID VALUE_PROVIDER_ID_LEGAL_ENTITY_SEARCH_MULTIPLE = ValueProviderID.create( 
			Organisation.DEV_ORGANISATION_ID, VALUE_PROVIDER_CATEGORY_ID_LEGAL_ENTITY, "SearchMultiple");

	public static final String VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS = "JFireReportingTrade-ValueProviderCategory-TradeDocuments";
	public static final String VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE = "JFireReportingTrade-ValueProviderCategory-TradeDocuments-Invoice";
	public static final String VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_ORDER = "JFireReportingTrade-ValueProviderCategory-TradeDocuments-Order";	
	public static final String VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_OFFER = "JFireReportingTrade-ValueProviderCategory-TradeDocuments-Offer";
	public static final String VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_DELIVERY_NOTE = "JFireReportingTrade-ValueProviderCategory-TradeDocuments-DeliveryNote";	
	
	public static final ValueProviderID VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_CUSTOMER = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE, "ByCustomer");
	public static final ValueProviderID VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_CUSTOMER_AND_PERIOD = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE, "ByCustomerAndPeriod");
	public static final ValueProviderID VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_ARTICLE_TYPE = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE, "ByArticleType");

	public static final ValueProviderID VALUE_PROVIDER_ID_TRADE_DOCUMENTS_ORDER_BY_CUSTOMER = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_ORDER, "ByOrder");
	public static final ValueProviderID VALUE_PROVIDER_ID_TRADE_DOCUMENTS_OFFER_BY_CUSTOMER = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_OFFER, "ByOffer");
	public static final ValueProviderID VALUE_PROVIDER_ID_TRADE_DOCUMENTS_DELIVERY_NOTE_BY_CUSTOMER = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_DELIVERY_NOTE, "ByDeliveryNote");
	
}
