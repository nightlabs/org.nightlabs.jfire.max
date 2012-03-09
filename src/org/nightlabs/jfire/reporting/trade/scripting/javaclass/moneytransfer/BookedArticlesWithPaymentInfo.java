/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.scripting.javaclass.moneytransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentConst;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.pay.PayMoneyTransfer;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.SQLResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultUtil;
import org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptUtil;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.TimePeriod;

/**
 * BIRT datasource javaclass script that lists booked articles and tries to resolve 
 * the payment information per article (proportional to the other articles in the invoice).
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 */
public class BookedArticlesWithPaymentInfo extends AbstractJFSScriptExecutorDelegate {

	private static final Logger logger = Logger.getLogger(BookedArticlesWithPaymentInfo.class);
	
	private static class MOPArticleEntry {
		long amount;
		double quantity;
	}
	
	public BookedArticlesWithPaymentInfo() {
		super();
	}

	private JFSResultSetMetaData metaData;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			metaData.addColumn("articleJDOID", DataType.STRING);
			metaData.addColumn("productTypeJDOID", DataType.STRING);
			metaData.addColumn("productTypeName", DataType.STRING);
			metaData.addColumn("reversing", DataType.BOOLEAN);
			metaData.addColumn("currencyJDOID", DataType.STRING);
			metaData.addColumn("currencyName", DataType.STRING);
			metaData.addColumn("modeOfPaymentJDOID", DataType.STRING);
			metaData.addColumn("modeOfPaymentName", DataType.STRING);
			metaData.addColumn("amount", DataType.BIGDECIMAL);
			metaData.addColumn("amountDouble", DataType.DOUBLE);
			metaData.addColumn("quantity", DataType.DOUBLE);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		TimePeriod timePeriod = getObjectParameterValue("timePeriod", TimePeriod.class);
		Collection<UserID> bookUserIDs = getObjectParameterValue("bookUserIDs", Collection.class);
		Collection<AnchorID> productTypeOwnerIDs = getObjectParameterValue("productTypeOwnerIDs", Collection.class);
		Collection<ProductTypeID> productTypeIDs = getObjectParameterValue("productTypeIDs", Collection.class);
		
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();

		// create the result buffer
		getResultSetMetaData();
		TableBuffer buffer = null;
		try {
			buffer = JFSResultUtil.createTableBuffer(metaData);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
		
		
		StringBuffer jdoql = new StringBuffer();
		jdoql.append("SELECT ");
		
		jdoql.append(" this, this.productType, this.reversing, this.price.currency, this.price.amount ");
		
		jdoql.append("FROM " + Article.class.getName() + " ");
		jdoql.append("WHERE ");
		
		// Invoice has to be booked
		jdoql.append("this.invoice.invoiceLocal.bookDT != null ");
		
		
		Map<String, Object> jdoParams = new HashMap<String, Object>();

		// filter by time period
		ReportingScriptUtil.addTimePeriodCondition(jdoql, "this.invoice.invoiceLocal.bookDT", "timePeriod", timePeriod, jdoParams);

		// Filter by users
		if (bookUserIDs != null && bookUserIDs.size() > 0) {
			jdoql.append("&& ( ");
			int i = 0;
			for (Iterator<UserID> it = bookUserIDs.iterator(); it.hasNext();) {
				UserID bookUserID = it.next();
				// TODO: WORKAROUND: JPOX Bug
				//					jdoql.append("JDOHelper.getObjectId(this.invoice.invoiceLocal.bookUser) == :bookUserID" + i);
				//					jdoParams.put("bookUserID" + i, bookUserID);
				jdoql.append("(this.invoice.invoiceLocal.bookUser == :bookUser" + i +")");
				jdoParams.put("bookUser" + i, pm.getObjectById(bookUserID));
				if (it.hasNext())
					jdoql.append("|| ");
				i++;
			}
			jdoql.append(") ");
		}

		if (productTypeIDs != null && productTypeIDs.size() > 0) {
			jdoql.append("&& (");
			int i = 0;
			for (Iterator<ProductTypeID> it = productTypeIDs.iterator(); it.hasNext();) {
				ProductTypeID productTypeID = it.next();
				jdoql.append("(JDOHelper.getObjectId(this.productType) == :productTypeID" + i + ")");
				jdoParams.put("productTypeID" + i, productTypeID);
			}
			jdoql.append(")");
		}
		
		if (productTypeOwnerIDs != null && productTypeOwnerIDs.size() > 0) {
			jdoql.append("&& (");
			int i = 0;
			for (Iterator<AnchorID> it = productTypeOwnerIDs.iterator(); it.hasNext();) {
				AnchorID anchorID = it.next();
				jdoql.append("(JDOHelper.getObjectId(this.productType.owner) == :ownerID" + i + ")");
				jdoParams.put("ownerID" + i, anchorID);
			}
			jdoql.append(") ");
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Creating query");
			logger.debug(jdoql.toString());
		}
		Query q = pm.newQuery(jdoql.toString());
		Collection<Object[]> queryResult = (Collection<Object[]>)q.executeWithMap(jdoParams);

		for (Object[] queryRow : queryResult) {
			Article article = (Article) queryRow[0];
			Invoice invoice = article.getInvoice(); // invoice can't be null as we searched for booked articles
			
			// This map collects the amount paid by the mode of payment flavour
			Map<ModeOfPaymentFlavourID, MOPArticleEntry> amountsByMOP = new HashMap<ModeOfPaymentFlavourID, MOPArticleEntry>(1);
			// this cumulates the amounts registered in amountsByMOP 
			long articlePaidAmount = 0;
//			// articleFactor is the factor by which the payments will be 
//			double articleFactor = (double) ((double)article.getPrice().getAmount() / (double)invoice.getPrice().getAmount()); 

			// get all Pay-InvoiceMoneyTransferes made for the current invoice
			Collection<PayableObjectMoneyTransfer> invoiceMoneyTransfers = 
				PayableObjectMoneyTransfer.getPayableObjectMoneyTransfers(
						getPersistenceManager(), 
						invoice, 
						PayableObjectMoneyTransfer.BookType.pay
				);
			
			for (PayableObjectMoneyTransfer invoiceMoneyTransfer : invoiceMoneyTransfers) {
				// The container of an InvoiceMoneyTransfer with bookType 'pay' should be a PayMoneyTransfer
				PayMoneyTransfer payMoneyTransfer = (PayMoneyTransfer) invoiceMoneyTransfer.getContainer();
				if (!payMoneyTransfer.getPayment().isSuccessfulAndComplete()) {
					// we only take successful payments into account
					continue;
				}
				
				// Get the MOP-flavour of the payment that lead to the transfer
				ModeOfPaymentFlavourID mopf = (ModeOfPaymentFlavourID) JDOHelper.getObjectId(payMoneyTransfer.getPayment().getModeOfPaymentFlavour());

				long invoiceAmount = invoice.getPrice().getAmount();
				long transferAmount = invoiceMoneyTransfer.getAmount();
				if (article.isReversing()) {
					transferAmount *= -1;
				}
				// The transferFactor is the fraction of the article paid with the current InvoiceMoneyTransfer 
				double transferFactor = 0d;
				if (invoiceAmount != 0) {
					transferFactor = (double)transferAmount / (double)invoiceAmount;
				} else {
					continue; // invoices with 0-amount are treated as non-payment
				}
				
				long paymentArticleAmount = Math.round(article.getPrice().getAmount() * transferFactor);

				// Cumulate the amounts for this article
				articlePaidAmount += paymentArticleAmount;
				
				// insert the amount in the map of amounts per MOP-flavour
				MOPArticleEntry articleEntry = amountsByMOP.get(mopf);
				if (articleEntry == null) {
					articleEntry = new MOPArticleEntry();
					amountsByMOP.put(mopf, articleEntry);
				}
				articleEntry.amount += paymentArticleAmount;
				
				// calculate the article quantity
				// it is the fraction of the article currently paid
				// unless the article price is zero, then the fraction will always be 1
				double articleFactor = 1d;
				if (article.getPrice().getAmount() != 0) {
					articleFactor = (double)paymentArticleAmount / (double)article.getPrice().getAmount();
				}
				if (article.isReversing()) {
					articleEntry.quantity += -articleFactor;
				} else {
					articleEntry.quantity += articleFactor;
				}
			}
			
			// If the article/invoice was not paid completely, we add an entry for 'non-payment'
			if (articlePaidAmount != article.getPrice().getAmount() || (articlePaidAmount == 0 && article.getPrice().getAmount() == 0)) {
				MOPArticleEntry articleEntry = amountsByMOP.get(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_NON_PAYMENT);
				if (articleEntry == null) {
					articleEntry = new MOPArticleEntry();
					amountsByMOP.put(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_NON_PAYMENT, articleEntry);
				}
				long diffAmount = (article.getPrice().getAmount() - articlePaidAmount);
				articleEntry.amount += diffAmount;
				long articleAmount = article.getPrice().getAmount();
				double diffFactor = 0d;
				if (articleAmount != 0)
					diffFactor = (double)diffAmount / (double)articleAmount;
				else 
					diffFactor = 1d;
				if (article.isReversing()) {
					articleEntry.quantity += -Math.abs(diffFactor);
				} else {
					articleEntry.quantity += Math.abs(diffFactor);
				}
			}
			
			// Now iterate the cumulated amount entries and create a result row for each one
			// so there might be multiple result rows for one article -> the quantity is a double value (parts summing up to 1)
			for (Map.Entry<ModeOfPaymentFlavourID, MOPArticleEntry> amountEntry : amountsByMOP.entrySet()) {
				List<Object> resultRow = new ArrayList<Object>(11);
				resultRow.add(JDOHelper.getObjectId(article).toString()); // 0 = this
				resultRow.add(JDOHelper.getObjectId(queryRow[1]).toString()); // 1 = productTpye
				resultRow.add(((ProductType) queryRow[1]).getName().getText(JFireReportingHelper.getLocale()));
				Boolean reversing = (Boolean) queryRow[2]; // 2 = reversing 
				resultRow.add(reversing); 
				Currency curr = (Currency) queryRow[3]; // 3 = currency
				resultRow.add(JDOHelper.getObjectId(curr).toString());
				resultRow.add(curr.getCurrencySymbol());
				
				ModeOfPaymentFlavour mopf = (ModeOfPaymentFlavour) getPersistenceManager().getObjectById(amountEntry.getKey());
				resultRow.add(amountEntry.getKey().toString());
				resultRow.add(mopf.getName().getText(JFireReportingHelper.getLocale()));
				
				Long amount = new Long(amountEntry.getValue().amount);
				resultRow.add(amount);
				resultRow.add(curr.toDouble(amount));
				resultRow.add(amountEntry.getValue().quantity);
				try {
					buffer.addRecord(new Record(resultRow));
				} catch (Exception e) {
					throw new ScriptException(e);
				}
			}
		}
		
		SQLResultSet resultSet = new SQLResultSet(buffer);
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doPrepare()
	 */
	public void doPrepare() throws ScriptException {
	}

}
