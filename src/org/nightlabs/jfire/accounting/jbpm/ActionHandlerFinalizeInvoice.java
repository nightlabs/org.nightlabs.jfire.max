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

public class ActionHandlerFinalizeInvoice
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerFinalizeInvoice.class.getName()));
		action.setName(ActionHandlerFinalizeInvoice.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsInvoice.Vendor.STATE_DEFINITION_JBPM_NODE_NAME_FINALIZED);
		if (finalized == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsInvoice.Vendor.STATE_DEFINITION_JBPM_NODE_NAME_FINALIZED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		finalized.addEvent(event);
	}

	@Implement
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Accounting accounting = Accounting.getAccounting(pm);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		Invoice invoice = (Invoice) getStatable();
		accounting.finalizeInvoice(user, invoice);
	}
}
