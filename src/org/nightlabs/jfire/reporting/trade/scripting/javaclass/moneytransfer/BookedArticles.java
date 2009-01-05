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
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.accounting.Currency;
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
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class BookedArticles extends AbstractJFSScriptExecutorDelegate {

	private static final Logger logger = Logger.getLogger(BookedArticles.class);
	public BookedArticles() {
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
			metaData.addColumn("amount", DataType.BIGDECIMAL);
			metaData.addColumn("amountDouble", DataType.DOUBLE);
			metaData.addColumn("quantity", DataType.BIGDECIMAL);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		TimePeriod timePeriod = getObjectParameterValue("timePeriod", TimePeriod.class);
		Boolean groupResults = getObjectParameterValue("groupResults", Boolean.class);
		if (groupResults == null)
			groupResults = true;
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
		
		if (groupResults) {
			jdoql.append(" \"\", this.productType, this.reversing, this.price.currency, SUM(this.price.amount), COUNT(this.articleID) ");
		} else {
			jdoql.append(" this, this.productType, this.reversing, this.price.currency, this.price.amount ");
		}
		
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
		
		if (groupResults) {
			jdoql.append("GROUP BY ");
			jdoql.append("this.productType, this.reversing, this.price.currency");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Creating query");
			logger.debug(jdoql.toString());
		}
		Query q = pm.newQuery(jdoql.toString());
		Collection<Object[]> queryResult = (Collection<Object[]>)q.executeWithMap(jdoParams);

		for (Object[] queryRow : queryResult) {
			List<Object> resultRow = new ArrayList<Object>(8);
			if (!groupResults)
				resultRow.add(JDOHelper.getObjectId(queryRow[0])); // 0 = this
			else
				resultRow.add("");
			resultRow.add(JDOHelper.getObjectId(queryRow[1]).toString()); // 1 = productTpye
			resultRow.add(((ProductType) queryRow[1]).getName().getText(JFireReportingHelper.getLocale()));
			Boolean reversing = (Boolean) queryRow[2]; // 2 = reversing 
			resultRow.add(reversing); 
			Currency curr = (Currency) queryRow[3]; // 3 = currency
			resultRow.add(JDOHelper.getObjectId(curr).toString());
			resultRow.add(curr.getCurrencySymbol());
			Long amount = (Long) queryRow[4]; // 4 = amount
			resultRow.add(amount);
			resultRow.add(curr.toDouble(amount));
			if (groupResults) {
				Long count = (Long) queryRow[5]; // 5 = count
				if (reversing)
					resultRow.add(new Long(-count));
				else
					resultRow.add(count);
			} else {
				if (reversing)
					resultRow.add(-1);
				else
					resultRow.add(1);
			}
			try {
				buffer.addRecord(new Record(resultRow));
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
	public void doPrepare() throws ScriptException {
	}

}
