package org.nightlabs.jfire.accounting.jbpm;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.InvoiceLocal;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.id.InvoiceLocalID;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.State;

public class SendInvoiceInvocation
extends Invocation
{
	private static final long serialVersionUID = 1L;

	private InvoiceID invoiceID;

	public SendInvoiceInvocation(InvoiceID invoiceID)
	{
		this.invoiceID = invoiceID;
	}

	@Implement
	public Serializable invoke()
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (State.hasState(pm, invoiceID, JbpmConstantsInvoice.Both.NODE_NAME_SENT))
				return null;

			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
			try {
				pm.getExtent(InvoiceLocal.class);
				InvoiceLocal invoiceLocal = (InvoiceLocal) pm.getObjectById(InvoiceLocalID.create(invoiceID));
				ProcessInstance processInstance = jbpmContext.getProcessInstance(invoiceLocal.getJbpmProcessInstanceId());
				processInstance.signal(JbpmConstantsInvoice.Vendor.TRANSITION_NAME_SEND);
			} finally {
				jbpmContext.close();
			}
		} finally {
			pm.close();
		}
		return null;
	}

}
