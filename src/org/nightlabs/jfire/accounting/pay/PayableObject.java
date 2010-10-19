package org.nightlabs.jfire.accounting.pay;

import org.nightlabs.jfire.trade.LegalEntity;


/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public interface PayableObject
{
	LegalEntity getCustomer();
	LegalEntity getVendor();
}
