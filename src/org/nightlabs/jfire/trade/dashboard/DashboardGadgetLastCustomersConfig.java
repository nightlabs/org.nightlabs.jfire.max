package org.nightlabs.jfire.trade.dashboard;

import java.io.Serializable;

public class DashboardGadgetLastCustomersConfig implements Serializable {

	private static final long serialVersionUID = 20111220L;
	
	private int amountLastCustomers;
	
	public DashboardGadgetLastCustomersConfig() {
	}
	
	public DashboardGadgetLastCustomersConfig(int amountLastCustomers) {
		this.amountLastCustomers = amountLastCustomers;
	}
		
	public int getAmountLastCustomers() {
		return amountLastCustomers;
	}

	public void setAmountLastCustomers(int amountLastCustomers) {
		this.amountLastCustomers = amountLastCustomers;
	}
}
