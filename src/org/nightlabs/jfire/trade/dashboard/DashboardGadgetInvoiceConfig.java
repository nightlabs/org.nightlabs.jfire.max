package org.nightlabs.jfire.trade.dashboard;

import java.io.Serializable;

import org.nightlabs.jfire.query.store.id.QueryStoreID;

public class DashboardGadgetInvoiceConfig implements Serializable {

	private static final long serialVersionUID = 20111220L;
	
	private int amountOfInvoices;
	
	private QueryStoreID invoiceQueryItemId;
	
	public DashboardGadgetInvoiceConfig() {
	}
	
	public DashboardGadgetInvoiceConfig(int amountOfInvoices, QueryStoreID invoiceQueryItemId) {
		this.amountOfInvoices = amountOfInvoices;
		this.setInvoiceQueryItemId(invoiceQueryItemId);
	}
		
	
	public int getAmountLastCustomers() {
		return amountOfInvoices;
	}

	public void setAmountLastCustomers(int amountOfInvoices) {
		this.amountOfInvoices = amountOfInvoices;
	}

	public QueryStoreID getInvoiceQueryItemId() {
		return invoiceQueryItemId;
	}

	public void setInvoiceQueryItemId(QueryStoreID invoiceQueryItemId) {
		this.invoiceQueryItemId = invoiceQueryItemId;
	}
}
