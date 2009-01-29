package org.nightlabs.jfire.trade.jbpm;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.trade.Offer;

/**
 * This action handler finalizes the Offer its process is run for.
 */
public class ActionHandlerAcceptOfferImplicitelyVendor
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerAcceptOfferImplicitelyVendor.class.getName()));
		action.setName(ActionHandlerAcceptOfferImplicitelyVendor.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsOffer.Both.NODE_NAME_ACCEPTED_IMPLICITELY);
		if (finalized == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsOffer.Both.NODE_NAME_ACCEPTED_IMPLICITELY +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		finalized.addEvent(event);
	}

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Offer offer = (Offer) getStatable();

		if (!offer.isFinalized()) {
			ActionHandlerFinalizeOffer.finalizeOffer(pm, offer);
		}
	}
}
