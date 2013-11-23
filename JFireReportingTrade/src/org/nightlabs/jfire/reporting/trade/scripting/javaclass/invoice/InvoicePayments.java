package org.nightlabs.jfire.reporting.trade.scripting.javaclass.invoice;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.pay.PayMoneyTransfer;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.scripting.ScriptException;

/**
 * BIRT datasource javaclass script that lists the {@link Payment}s done for an {@link Invoice} and
 * prints the outstanding amount.
 * It takes the following parameters:
 * <ul>
 *   <li>invoiceID: {@link InvoiceID}</li>
 * </ul>
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class InvoicePayments extends AbstractJFSScriptExecutorDelegate {

//	public static final String PARAMETER_ID_INVOICE

	/** */
	private JFSResultSetMetaData resultSetMetaData;
	/** */
	private int columnCount;

	@Override
	public IResultSetMetaData getResultSetMetaData() throws ScriptException {
		if (resultSetMetaData == null) {
			resultSetMetaData = new JFSResultSetMetaData();
//			resultSetMetaData.addColumn();
//			resultSetMetaData.addColumn();
			// For JDOID we use the ObjectID.toString() method. This is because we cannot transfer Objects to the Birt layout, only simple datatypes
			// so if you want to address an object pass the String-representation of this object
			// from the Payment
			resultSetMetaData.addColumn("paymentJDOID", DataType.STRING);
			// from the InvoiceMoneyTransfer
			resultSetMetaData.addColumn("invoiceMoneyTransferJDOID", DataType.STRING);
			// from the Payment
			resultSetMetaData.addColumn("transferAmount", DataType.DOUBLE);
			// from the Payment
			resultSetMetaData.addColumn("transferCurrencyJDOID", DataType.STRING);
			// from the Payment
			resultSetMetaData.addColumn("transferCurrencySymbol", DataType.STRING);
			// from the Payment
			resultSetMetaData.addColumn("paymentBeginDT", DataType.DATETIME);
			resultSetMetaData.addColumn("paymentEndDT", DataType.DATETIME);
			// from the Payment
			resultSetMetaData.addColumn("modeOfPaymentFlavourJDOID", DataType.STRING);
			// This should be localized with the Report locale use JFireReportingHelper.getLocale()
			resultSetMetaData.addColumn("modeOfPaymentFlavourName", DataType.STRING);

			try {
				columnCount = resultSetMetaData.getColumnCount();
			} catch (final JFireOdaException exception) {
				throw new RuntimeException(exception);
			}
		}

		return resultSetMetaData;
	}

	@Override
	public Object doExecute() throws ScriptException {

		InvoiceID invoiceID = getObjectParameterValue("invoiceID", InvoiceID.class);

		JFSResultSet resultSet = new JFSResultSet((JFSResultSetMetaData)getResultSetMetaData());
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		Query q = pm.newQuery(
				"SELECT \n"+
				"    this, \n"+
				"    containerTransfer.payment \n"+
				"FROM " + PayableObjectMoneyTransfer.class.getName() +" \n" +
				"WHERE \n"+
				"  this.bookType == \"" + PayableObjectMoneyTransfer.BookType.pay + "\" && \n"+
				"  JDOHelper.getObjectId(this.invoice) == :invoiceID && \n"+
				"  this.container == containerTransfer \n"+
				"VARIABLES \n"+
				"  " + PayMoneyTransfer.class.getName() + " containerTransfer"
			);
		Collection<Object[]> products = (Collection<Object[]>)q.execute(invoiceID);

		for (Iterator<Object[]> iter = products.iterator(); iter.hasNext();) {
			Object[] row = iter.next();
			// row[0] is of Type InvoiceMoneyTransfer
			// row[1] is of Type Payment

			PayableObjectMoneyTransfer invoiceMoneyTransfer = (PayableObjectMoneyTransfer) row[0];
			Payment payment = (Payment) row[1];

			Object[] nRow = new Object[columnCount];
			nRow[0] = JDOHelper.getObjectId(payment).toString();
			nRow[1] = JDOHelper.getObjectId(invoiceMoneyTransfer).toString();
			nRow[2] = payment.getCurrency().toDouble(payment.getAmount());
			nRow[3] = JDOHelper.getObjectId(payment.getCurrency()).toString();
			nRow[4] = payment.getCurrency().getCurrencySymbol();
			nRow[5] = payment.getBeginDT();
			nRow[6] = payment.getEndDT();
			nRow[7] = payment.getModeOfPaymentFlavourID().toString();
			nRow[8] = payment.getModeOfPaymentFlavour().getName().getText(JFireReportingHelper.getLocale());
			resultSet.addRow(nRow);
		}
		resultSet.init();
		return resultSet;
	}
}
