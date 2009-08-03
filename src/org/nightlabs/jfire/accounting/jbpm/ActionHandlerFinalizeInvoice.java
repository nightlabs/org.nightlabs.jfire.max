package org.nightlabs.jfire.accounting.jbpm;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;

public class ActionHandlerFinalizeInvoice
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

//	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
//	{
//		Action action = new Action(new Delegation(ActionHandlerFinalizeInvoice.class.getName()));
//		action.setName(ActionHandlerFinalizeInvoice.class.getName());
//
//		Event event = new Event("node-enter");
//		event.addAction(action);
//
//		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsInvoice.Vendor.NODE_NAME_FINALIZED);
//		if (finalized == null)
//			throw new IllegalArgumentException("The node \""+ JbpmConstantsInvoice.Vendor.NODE_NAME_FINALIZED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");
//
//		finalized.addEvent(event);
//	}

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Invoice invoice = (Invoice) getStatable();

		if (invoice.isFinalized())
			return;

		Accounting accounting = Accounting.getAccounting(pm);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		if (
				(!invoice.getVendor().equals(accounting.getMandator()))
				&&
				(!invoice.getCustomer().equals(accounting.getMandator()))
		)
			throw new IllegalArgumentException("Cannot finalize an invoice where the mandator is neither vendor nor customer of the invoice!");

		// invoice.setFinalized(...) does nothing, if it is already finalized.
		invoice.setFinalized(user);

		// book synchronously
		InvoiceID invoiceID = (InvoiceID) JDOHelper.getObjectId(invoice);
		if (!State.hasState(pm, invoiceID, JbpmConstantsInvoice.Both.NODE_NAME_BOOKED))
			executionContext.leaveNode(JbpmConstantsInvoice.Vendor.TRANSITION_NAME_BOOK);
	}
}
