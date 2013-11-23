package org.nightlabs.jfire.trade.dashboard;

import java.util.List;

import javax.ejb.Remote;

@Remote
public interface DashboardManagerRemote {
	void initialise() throws Exception;
	List<LastCustomerTransaction> searchLastCustomerTransactions(DashboardGadgetLastCustomersConfig lastCustomersConfig);
}
