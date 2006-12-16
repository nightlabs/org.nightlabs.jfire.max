package org.nightlabs.jfire.trade.jbpm;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OfferLocalID;

public class SendOfferInvocation
extends Invocation
{
	private static final long serialVersionUID = 1L;

	private OfferID offerID;

	public SendOfferInvocation(OfferID offerID)
	{
		this.offerID = offerID;
	}

	@Implement
	public Serializable invoke()
			throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (State.hasState(pm, offerID, JbpmConstantsOffer.Both.NODE_NAME_SENT))
				return null;

			OfferLocal offerLocal = (OfferLocal) pm.getObjectById(OfferLocalID.create(offerID));

			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
			try {
				ProcessInstance processInstance = jbpmContext.getProcessInstance(offerLocal.getJbpmProcessInstanceId());
				processInstance.signal(JbpmConstantsOffer.Vendor.TRANSITION_NAME_SEND);
			} finally {
				jbpmContext.close();
			}
		} finally {
			pm.close();
		}
		return null;
	}

}
