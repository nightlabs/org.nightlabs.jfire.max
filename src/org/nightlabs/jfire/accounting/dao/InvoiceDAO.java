package org.nightlabs.jfire.accounting.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.query.InvoiceQuery;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;

public class InvoiceDAO
		extends BaseJDOObjectDAO<InvoiceID, Invoice>
{
	private static InvoiceDAO sharedInstance = null;

	public static InvoiceDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (InvoiceDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new InvoiceDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<Invoice> retrieveJDOObjects(Set<InvoiceID> invoiceIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		AccountingManager am = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return am.getInvoices(invoiceIDs, fetchGroups, maxFetchDepth);
	}

	public Invoice getInvoice(InvoiceID invoiceID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, invoiceID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Invoice> getInvoices(Set<InvoiceID> invoiceIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, invoiceIDs, fetchGroups, maxFetchDepth, monitor);
	}


	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public List<Invoice> getInvoices(
			AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			AccountingManager am = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			List<InvoiceID> invoiceIDList = am.getInvoiceIDs(vendorID, customerID, rangeBeginIdx, rangeEndIdx);
			Set<InvoiceID> invoiceIDs = new HashSet<InvoiceID>(invoiceIDList);

			Map<InvoiceID, Invoice> invoiceMap = new HashMap<InvoiceID, Invoice>(invoiceIDs.size());
			for(Invoice invoice : getJDOObjects(null, invoiceIDs, fetchGroups, maxFetchDepth, monitor))
				invoiceMap.put((InvoiceID) JDOHelper.getObjectId(invoice), invoice);

			List<Invoice> res = new ArrayList<Invoice>(invoiceIDList.size());
			for (InvoiceID invoiceID : invoiceIDList) {
				Invoice invoice = invoiceMap.get(invoiceID);
				if (invoice != null)
					res.add(invoice);
			}

			return res;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<Invoice> getInvoices(QueryCollection<? extends InvoiceQuery> queries,
		String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			AccountingManager accountingManager = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Set<InvoiceID> invoiceIDs = accountingManager.getInvoiceIDs(queries);
			return InvoiceDAO.sharedInstance().getInvoices(
				invoiceIDs,
				fetchGroups,
				maxFetchDepth,
				monitor
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
