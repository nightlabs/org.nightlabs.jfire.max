package org.nightlabs.jfire.accounting.pay;

import java.util.Date;

import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.l10n.Currency;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public interface PayableObject
{
	String getOrganisationID();
	String getPayableObjectID();
	
	LegalEntity getCustomer();
	LegalEntity getVendor();
	
	Currency getCurrency();
	
	/**
	 * @return
	 */
	long getAmountToPay();
	
	Date getFinalizeDT();
	
	boolean isOutstanding();
}
