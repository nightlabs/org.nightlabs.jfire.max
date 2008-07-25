package org.nightlabs.jfire.trade.jbpm;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.OfferID;

/**
 * This action handler is registered for the node {@link JbpmConstantsOffer.Vendor#NODE_NAME_FINALIZED}
 * and finalizes the Offer it is run for, it uses {@link Trader#onFinalizeOffer(User, Offer)} for doing so. 
 * It also automatically performs the send-transition if it can find one.
 */
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

		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsOffer.Vendor.NODE_NAME_FINALIZED);
		if (finalized == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsOffer.Vendor.NODE_NAME_FINALIZED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		finalized.addEvent(event);
	}

	protected static void finalizeOffer(PersistenceManager pm, Offer offer) 
	throws Exception
	{
		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		Trader.getTrader(pm).onFinalizeOffer(user, offer);
	}
	
	
	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Offer offer = (Offer) getStatable();

		finalizeOffer(pm, offer);
		
		OfferID offerID = (OfferID) JDOHelper.getObjectId(offer);
		if (!State.hasState(pm, offerID, JbpmConstantsOffer.Both.NODE_NAME_SENT))
			executionContext.leaveNode(JbpmConstantsOffer.Vendor.TRANSITION_NAME_SEND);
	}
}
