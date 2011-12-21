package org.nightlabs.jfire.trade.dashboard;

import java.io.Serializable;

import org.nightlabs.jfire.query.store.id.QueryStoreID;

public class DashboardGadgetInvoiceConfig implements Serializable {

	private static final long serialVersionUID = 20111221L;
	
	private int amountOfInvoices;
	
	private QueryStoreID invoiceQueryItemId;
	
	public DashboardGadgetInvoiceConfig() {
	}
	
	public DashboardGadgetInvoiceConfig(int amountOfInvoices, QueryStoreID invoiceQueryItemId) {
		this.amountOfInvoices = amountOfInvoices;
		this.setInvoiceQueryItemId(invoiceQueryItemId);
	}

	public void setAmountOfInvoices(int amountOfInvoices) {
		this.amountOfInvoices = amountOfInvoices;
	}
	
	public int getAmountOfInvoices() {
		return amountOfInvoices;
	}

	public QueryStoreID getInvoiceQueryItemId() {
		return invoiceQueryItemId;
	}

	public void setInvoiceQueryItemId(QueryStoreID invoiceQueryItemId) {
		this.invoiceQueryItemId = invoiceQueryItemId;
	}
}
