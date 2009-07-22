package org.nightlabs.jfire.accounting.jbpm;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;

public class ActionHandlerInvoiceProcessEnd
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		// if it was a failure/abortion, we clear the backreference of the articles to their current invoice.
		if (!JbpmConstantsInvoice.Both.NODE_NAME_PAID.equals(executionContext.getToken().getNode().getName())) {
			Invoice invoice = (Invoice) getStatable();
			for (Article article : invoice.getArticles()) {
				article.setInvoice(null);
			}

			if (invoice.getInvoiceLocal().isBooked()) {
				PersistenceManager pm = getPersistenceManager();
				User user = User.getUser(pm);
				Accounting.getAccounting(pm).bookUncollectableInvoice(user, invoice);
			}
		}
	}
}
