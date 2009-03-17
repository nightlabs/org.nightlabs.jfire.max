package org.nightlabs.jfire.chezfrancois;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;

/**
 * Creates the data that constitute the CashBox.
 * That is, a CashBox is (for now) allowed to have several CashBoxTrays.
 * 
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class DataCreatorCashBoxTray extends DataCreator {

	/**
	 * @param pm
	 * @param user
	 */
	public DataCreatorCashBoxTray(PersistenceManager pm, User user) {
		super(pm, user);
		// TODO Auto-generated constructor stub
	}

}
