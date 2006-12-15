package org.nightlabs.jfire.trade.jbpm;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Trader;

public class ActionHandlerFinalizeOffer
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerFinalizeOffer.class.getName()));
		action.setName(ActionHandlerFinalizeOffer.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsOffer.Vendor.STATE_DEFINITION_JBPM_NODE_NAME_FINALIZED);
		if (finalized == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsOffer.Vendor.STATE_DEFINITION_JBPM_NODE_NAME_FINALIZED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		finalized.addEvent(event);
	}

	@Implement
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		Offer offer = (Offer) getStatable();
		trader.finalizeOffer(user, offer);
	}
}
