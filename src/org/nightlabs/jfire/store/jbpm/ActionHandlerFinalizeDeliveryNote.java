package org.nightlabs.jfire.store.jbpm;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.id.DeliveryNoteID;

public class ActionHandlerFinalizeDeliveryNote
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerFinalizeDeliveryNote.class.getName()));
		action.setName(ActionHandlerFinalizeDeliveryNote.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsDeliveryNote.Vendor.NODE_NAME_FINALIZED);
		if (finalized == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsDeliveryNote.Vendor.NODE_NAME_FINALIZED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		finalized.addEvent(event);
	}

	@Implement
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		DeliveryNote deliveryNote = (DeliveryNote) getStatable();
//		doExecute(pm, deliveryNote);
//	}
//
//	protected static void doExecute(PersistenceManager pm, DeliveryNote deliveryNote)
//	throws Exception
//	{
//		if (deliveryNote.isFinalized())
//			return;

		Accounting accounting = Accounting.getAccounting(pm);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		if (!deliveryNote.getVendor().getPrimaryKey().equals(accounting.getMandator().getPrimaryKey()))
			throw new IllegalArgumentException("Can not finalize an deliveryNote where mandator is not vendor of this deliveryNote!");

		// deliveryNote.setFinalized(...) does nothing, if it is already finalized.
		deliveryNote.setFinalized(user);

		// book asynchronously
//		AsyncInvoke.exec(new BookDeliveryNoteInvocation((DeliveryNoteID) JDOHelper.getObjectId(deliveryNote)), true);

		DeliveryNoteID deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);
		if (!State.hasState(pm, deliveryNoteID, JbpmConstantsDeliveryNote.Both.NODE_NAME_BOOKED)) // in case a manual booking has occured (though this should be more-or-less impossible in the short time)
			executionContext.leaveNode(JbpmConstantsDeliveryNote.Both.TRANSITION_NAME_BOOK);

//	JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
//	try {
//		pm.getExtent(DeliveryNoteLocal.class);
//		DeliveryNoteLocal deliveryNoteLocal = (DeliveryNoteLocal) pm.getObjectById(DeliveryNoteLocalID.create(deliveryNoteID));
//		ProcessInstance processInstance = jbpmContext.getProcessInstance(deliveryNoteLocal.getJbpmProcessInstanceId());
//		processInstance.signal(JbpmConstantsDeliveryNote.Both.TRANSITION_NAME_BOOK);
//	} finally {
//		jbpmContext.close();
//	}
	}
}
