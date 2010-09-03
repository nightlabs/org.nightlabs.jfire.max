/**
 *
 */
package org.nightlabs.jfire.reporting.trade.scripting.javaclass.invoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.SQLResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultUtil;
import org.nightlabs.jfire.scripting.ScriptException;

/**
 * BIRT datasource javaclass script that lists the properties of a collection of {@link Invoices}s.
 * It takes the following parameters:
 * <ul>
 *   <li>invoiceIDs: Collection&lt;InvoiceID&gt;</li>
 * </ul>
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class InvoiceData extends AbstractJFSScriptExecutorDelegate {

	public InvoiceData() {
		super();
	}

	private JFSResultSetMetaData metaData;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			metaData.addColumn("organisationID", DataType.STRING);
			metaData.addColumn("invoiceIDPrefix", DataType.STRING);
			metaData.addColumn("invoiceID", DataType.BIGDECIMAL);
			metaData.addColumn("invoiceIDAsString", DataType.STRING);
			metaData.addColumn("createDT", DataType.DATETIME);
			metaData.addColumn("createUserJDOID", DataType.STRING);
			metaData.addColumn("vendorJDOID", DataType.STRING);
			metaData.addColumn("customerJDOID", DataType.STRING);
			metaData.addColumn("currencyJDOID", DataType.STRING);
			metaData.addColumn("price", DataType.BIGDECIMAL);
			metaData.addColumn("stateJDOID", DataType.STRING);
			metaData.addColumn("articleCount", DataType.INTEGER);
			metaData.addColumn("valid", DataType.BOOLEAN);
			metaData.addColumn("finalizeUserJDOID", DataType.STRING);
			metaData.addColumn("finalizeDT", DataType.DATETIME);
			metaData.addColumn("amountPaid", DataType.BIGDECIMAL);
			metaData.addColumn("amountToPay", DataType.BIGDECIMAL);
			metaData.addColumn("outstanding", DataType.BOOLEAN);
			metaData.addColumn("booked", DataType.BOOLEAN);
			metaData.addColumn("bookUserJDOID", DataType.STRING);
			metaData.addColumn("bookUserDT", DataType.STRING);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		Collection<InvoiceID> invoiceIDs = getObjectParameterValue("invoiceIDs", Collection.class);
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();

		getResultSetMetaData();
		TableBuffer buffer = null;
		try {
			buffer = JFSResultUtil.createTableBuffer(metaData);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
		if (invoiceIDs == null)
			return new SQLResultSet(buffer);
		for (InvoiceID invoiceID : invoiceIDs) {
			Invoice invoice = (Invoice) pm.getObjectById(invoiceID);
			List<Object> row = new ArrayList<Object>(21);
			row.add(invoice.getOrganisationID());
			row.add(invoice.getInvoiceIDPrefix());
			row.add(invoice.getInvoiceID());
			row.add(invoice.getInvoiceIDAsString());
			row.add(invoice.getCreateDT());
			row.add(JDOHelper.getObjectId(invoice.getCreateUser()).toString());
			row.add(JDOHelper.getObjectId(invoice.getVendor()).toString());
			row.add(JDOHelper.getObjectId(invoice.getCustomer()).toString());
			row.add(JDOHelper.getObjectId(invoice.getCurrency()).toString());
			row.add(invoice.getPrice().getAmount());
			row.add(JDOHelper.getObjectId(invoice.getInvoiceLocal().getState()).toString());
			row.add(invoice.getArticleCount());
			row.add(invoice.isValid());
			row.add(JDOHelper.getObjectId(invoice.getFinalizeUser()).toString());
			row.add(invoice.getFinalizeDT());
			row.add(invoice.getInvoiceLocal().getAmountPaid());
			row.add(invoice.getInvoiceLocal().getAmountToPay());
			row.add(invoice.getInvoiceLocal().isOutstanding());
			row.add(invoice.getInvoiceLocal().isBooked());
			row.add(JDOHelper.getObjectId(invoice.getInvoiceLocal().getBookUser()).toString());
			row.add(invoice.getInvoiceLocal().getBookDT());
			try {
				buffer.addRecord(new Record(row));
			} catch (Exception e) {
				throw new ScriptException(e);
			}
		}
		SQLResultSet resultSet = new SQLResultSet(buffer);
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doPrepare()
	 */
	@Override
	public void doPrepare() throws ScriptException {
	}

}
