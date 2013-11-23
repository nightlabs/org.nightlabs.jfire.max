package org.nightlabs.jfire.store.jbpm;

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
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.id.DeliveryNoteID;

public class ActionHandlerBookDeliveryNote
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerBookDeliveryNote.class.getName()));
		action.setName(ActionHandlerBookDeliveryNote.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node booked = jbpmProcessDefinition.getNode(JbpmConstantsDeliveryNote.Both.NODE_NAME_BOOKED);
		if (booked == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsDeliveryNote.Both.NODE_NAME_BOOKED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		booked.addEvent(event);
	}

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		DeliveryNote deliveryNote = (DeliveryNote) getStatable();
		store.onBookDeliveryNote(user, deliveryNote);

//		ArrayList products = new ArrayList(deliveryNote.getArticles().size());
//		for (Article article : deliveryNote.getArticles()) {
//			products.add(article.getProduct());
//		}
//		store.consolidateProductReferences(products); // TODO is the consolidate here at the right position?

		// send asynchronously
//		AsyncInvoke.exec(new SendDeliveryNoteInvocation((DeliveryNoteID) JDOHelper.getObjectId(deliveryNote)), true);
		DeliveryNoteID deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);
		if (!State.hasState(pm, deliveryNoteID, JbpmConstantsDeliveryNote.Both.NODE_NAME_SENT))
			executionContext.leaveNode(JbpmConstantsDeliveryNote.Vendor.TRANSITION_NAME_SEND);
	}

}
