package org.nightlabs.jfire.store.jbpm;

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
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Store;

public class ActionHandlerBookDeliveryNoteImplicitely
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerBookDeliveryNoteImplicitely.class.getName()));
		action.setName(ActionHandlerBookDeliveryNoteImplicitely.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node booked = jbpmProcessDefinition.getNode(JbpmConstantsDeliveryNote.Vendor.NODE_NAME_BOOKED_IMPLICITELY);
		if (booked == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsDeliveryNote.Vendor.NODE_NAME_BOOKED_IMPLICITELY +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		booked.addEvent(event);
	}

	@Implement
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		DeliveryNote deliveryNote = (DeliveryNote) getStatable();
		Store.getStore(pm).validateDeliveryNote(deliveryNote);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		deliveryNote.setFinalized(user);
//		ActionHandlerFinalizeDeliveryNote.doExecute(pm, deliveryNote);
	}

}
