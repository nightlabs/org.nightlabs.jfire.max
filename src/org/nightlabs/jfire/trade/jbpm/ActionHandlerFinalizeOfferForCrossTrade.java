package org.nightlabs.jfire.trade.jbpm;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.jfire.trade.Offer;

public class ActionHandlerFinalizeOfferForCrossTrade
extends ActionHandlerFinalizeOffer
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerFinalizeOfferForCrossTrade.class.getName()));
		action.setName(ActionHandlerFinalizeOfferForCrossTrade.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsOffer.Vendor.NODE_NAME_FINALIZED_FOR_CROSS_TRADE);
		if (finalized == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsOffer.Vendor.NODE_NAME_FINALIZED_FOR_CROSS_TRADE +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		finalized.addEvent(event);
	}

	@Override
	protected void doExecute(ExecutionContext executionContext)
			throws Exception
	{
		// we do not call super.doExecute(...), because we don't want to leave via the same transition since we are another node.
		PersistenceManager pm = getPersistenceManager();
		Offer offer = (Offer) getStatable();
		finalizeOffer(pm, offer);
	}
}
