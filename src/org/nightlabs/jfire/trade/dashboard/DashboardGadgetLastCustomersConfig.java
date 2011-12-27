package org.nightlabs.jfire.trade.dashboard;

import java.io.Serializable;

public class DashboardGadgetLastCustomersConfig implements Serializable {

	private static final long serialVersionUID = 20111220L;
	
	public static final int initialAmountOfCustomersInDashboard = 10;	// initially at most 10
	
	public static final int maxAmountOfCustomersInDashboard = 50;
	
	private int amountLastCustomers;
	
	public DashboardGadgetLastCustomersConfig() { }
	
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
