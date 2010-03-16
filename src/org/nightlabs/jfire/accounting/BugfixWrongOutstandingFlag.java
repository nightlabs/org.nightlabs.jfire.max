package org.nightlabs.jfire.accounting;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.moduleregistry.UpdateHistoryItem;
import org.nightlabs.jdo.moduleregistry.UpdateNeededHandle;
import org.nightlabs.jfire.trade.JFireTradeEAR;

class BugfixWrongOutstandingFlag
{
	private static final Logger logger = Logger.getLogger(BugfixWrongOutstandingFlag.class);

	public static void fix(PersistenceManager pm) {
		UpdateNeededHandle handle = UpdateHistoryItem.updateNeeded(pm, JFireTradeEAR.MODULE_NAME, Invoice.class.getName() + "#uncollectableStillOutstanding");
		if (handle != null) {
			UpdateHistoryItem.updateDone(handle);
			logger.warn("initialise: Beginning to fix wrong 'outstanding' flags of uncollectable invoices.");

			Query q = pm.newQuery(InvoiceLocal.class);
			q.setFilter("this.bookUncollectableDT != null && this.outstanding");

			@SuppressWarnings("unchecked")
			Collection<InvoiceLocal> invoiceLocals = (Collection<InvoiceLocal>) q.execute();
			for (InvoiceLocal invoiceLocal : invoiceLocals) {
				if (!invoiceLocal.isBookedUncollectable())
					throw new IllegalStateException("Found invoice which should not match query filter (it is not booked uncollectable)!!! " + invoiceLocal);

				if (!invoiceLocal.isOutstanding())
					throw new IllegalStateException("Found invoice which should not match query filter (it is not outstanding)!!! " + invoiceLocal);

				invoiceLocal.setOutstanding(false);
				logger.warn("initialise: Fixed wrong 'outstanding' flag: Set to 'false', because invoice is already booked as uncollectable: " + invoiceLocal.getOrganisationID() + '/' + invoiceLocal.getInvoiceIDPrefix() + '/' + invoiceLocal.getInvoiceID());
			}

			logger.warn("initialise: Fixed wrong 'outstanding' flags of " + invoiceLocals.size() + " uncollectable invoices.");
			q.closeAll();
		}
	}

}
