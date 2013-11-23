package org.nightlabs.jfire.accounting;

import javax.ejb.Local;

import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;

@Local
public interface AccountingManagerLocal {
	PaymentResult _payBegin(PaymentData paymentData);
	PaymentResult _payDoWork(PaymentID paymentID, PaymentResult payDoWorkClientResult, boolean forceRollback);
	PaymentResult _payEnd(PaymentID paymentID, PaymentResult payEndClientResult, boolean forceRollback);
}
