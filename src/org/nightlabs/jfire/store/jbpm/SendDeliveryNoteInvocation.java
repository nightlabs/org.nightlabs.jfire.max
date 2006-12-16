package org.nightlabs.jfire.store.jbpm;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.store.DeliveryNoteLocal;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.DeliveryNoteLocalID;

public class SendDeliveryNoteInvocation
extends Invocation
{
	private static final long serialVersionUID = 1L;

	private DeliveryNoteID deliveryNoteID;

	public SendDeliveryNoteInvocation(DeliveryNoteID deliveryNoteID)
	{
		this.deliveryNoteID = deliveryNoteID;
	}

	@Implement
	public Serializable invoke()
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (State.hasState(pm, deliveryNoteID, JbpmConstantsDeliveryNote.Both.NODE_NAME_SENT))
				return null;

			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
			try {
				pm.getExtent(DeliveryNoteLocal.class);
				DeliveryNoteLocal deliveryNoteLocal = (DeliveryNoteLocal) pm.getObjectById(DeliveryNoteLocalID.create(deliveryNoteID));
				ProcessInstance processInstance = jbpmContext.getProcessInstance(deliveryNoteLocal.getJbpmProcessInstanceId());
				processInstance.signal(JbpmConstantsDeliveryNote.Vendor.TRANSITION_NAME_SEND);
			} finally {
				jbpmContext.close();
			}
		} finally {
			pm.close();
		}
		return null;
	}

}
