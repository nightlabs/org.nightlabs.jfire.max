/**
 * 
 */
package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.accounting.Invoice;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class InvoiceQuickSearchQuery 
extends AbstractArticleContainerQuickSearchQuery 
{
	@Override
	public Class getArticleContainerClass() {
		return Invoice.class;
	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "invoiceID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "invoiceIDPrefix";
	}
}
