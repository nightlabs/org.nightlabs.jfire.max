package org.nightlabs.jfire.accounting.jbpm;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;

public class ActionHandlerBookInvoice
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerBookInvoice.class.getName()));
		action.setName(ActionHandlerBookInvoice.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node booked = jbpmProcessDefinition.getNode(JbpmConstantsInvoice.Both.NODE_NAME_BOOKED);
		if (booked == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsInvoice.Both.NODE_NAME_BOOKED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		booked.addEvent(event);
	}

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Accounting accounting = Accounting.getAccounting(pm);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		Invoice invoice = (Invoice) getStatable();
		accounting.onBookInvoice(user, invoice);

		// send asynchronously
		InvoiceID invoiceID = (InvoiceID) JDOHelper.getObjectId(invoice);
//		AsyncInvoke.exec(new SendInvoiceInvocation(invoiceID, true);
		if (!State.hasState(pm, invoiceID, JbpmConstantsInvoice.Both.NODE_NAME_SENT))
			executionContext.leaveNode(JbpmConstantsInvoice.Vendor.TRANSITION_NAME_SEND);
	}

}
