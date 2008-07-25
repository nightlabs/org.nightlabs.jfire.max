package org.nightlabs.jfire.trade.jbpm;

import org.apache.log4j.Logger;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;

/**
 * This action handler leaves the node using the same transition it was entered with.
 * It is registered for {@link JbpmConstantsOffer.Both#NODE_NAME_SENT}.
 */
public class ActionHandlerSendOffer
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerSendOffer.class.getName()));
		action.setName(ActionHandlerSendOffer.class.getName());

//		Event event = new Event("node-enter");
//		event.addAction(action);

		Node node = jbpmProcessDefinition.getNode(JbpmConstantsOffer.Both.NODE_NAME_SENT);
		if (node == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsOffer.Both.NODE_NAME_SENT +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		node.setAction(action);
	}

	@Override
	@Implement
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		Offer offer = (Offer) getStatable();

		// send the offer to the customer, if the customer is an organisation
		if (offer.getOrder().getCustomer() instanceof OrganisationLegalEntity) {
			// CrossTrade stuff is handled already outside - we don't need to send it here!
		}

		String transitionName = ActionHandlerNodeEnter.getLastNodeEnterTransitionName();
//		Logger.getLogger(ActionHandlerSendOffer.class).info("****************************************");
//		Logger.getLogger(ActionHandlerSendOffer.class).info("****************************************");
//		Logger.getLogger(ActionHandlerSendOffer.class).info("****************************************");
//		Logger.getLogger(ActionHandlerSendOffer.class).info("****************************************");
//		Logger.getLogger(ActionHandlerSendOffer.class).info("****************************************");
//		Logger.getLogger(ActionHandlerSendOffer.class).info("****************************************");
		Logger.getLogger(ActionHandlerSendOffer.class).debug("**************************** leaving via: " + transitionName);
		executionContext.leaveNode(transitionName);
	}
}
