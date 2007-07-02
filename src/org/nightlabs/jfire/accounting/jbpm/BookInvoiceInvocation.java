package org.nightlabs.jfire.accounting.jbpm;


/**
 * This invocation triggers the transition specified by {@link JbpmConstantsInvoice.Vendor#TRANSITION_NAME_BOOK}
 * on the vendor side. It is enqueued by {@link ActionHandlerFinalizeInvoice}.
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 * @deprecated We can't do this asynchronously :-(
 */
public class BookInvoiceInvocation
//extends Invocation
{
	private static final long serialVersionUID = 1L;
//
//	private InvoiceID invoiceID;
//
//	public BookInvoiceInvocation(InvoiceID invoiceID)
//	{
//		this.invoiceID = invoiceID;
//	}
//
//	@Implement
//	public Serializable invoke()
//	throws Exception
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			if (State.hasState(pm, invoiceID, JbpmConstantsInvoice.Both.NODE_NAME_BOOKED)) // in case a manual booking has occured (though this should be more-or-less impossible in the short time)
//				return null;
//
//			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
//			try {
//				pm.getExtent(InvoiceLocal.class);
//				InvoiceLocal invoiceLocal = (InvoiceLocal) pm.getObjectById(InvoiceLocalID.create(invoiceID));
//				ProcessInstance processInstance = jbpmContext.getProcessInstance(invoiceLocal.getJbpmProcessInstanceId());
//				processInstance.signal(JbpmConstantsInvoice.Vendor.TRANSITION_NAME_BOOK);
//			} finally {
//				jbpmContext.close();
//			}
//		} finally {
//			pm.close();
//		}
//		return null;
//	}

}
