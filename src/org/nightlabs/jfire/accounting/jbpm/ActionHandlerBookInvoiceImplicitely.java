package org.nightlabs.jfire.accounting.jbpm;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;

public class ActionHandlerBookInvoiceImplicitely
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerBookInvoiceImplicitely.class.getName()));
		action.setName(ActionHandlerBookInvoiceImplicitely.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node booked = jbpmProcessDefinition.getNode(JbpmConstantsInvoice.Vendor.NODE_NAME_BOOKED_IMPLICITELY);
		if (booked == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsInvoice.Vendor.NODE_NAME_BOOKED_IMPLICITELY +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		booked.addEvent(event);
	}

	@Implement
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Invoice invoice = (Invoice) getStatable();
		Accounting accounting = Accounting.getAccounting(pm);
		accounting.validateInvoice(invoice);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		invoice.setFinalized(user);
//		ActionHandlerFinalizeInvoice.doExecute(pm, invoice);
	}

}
