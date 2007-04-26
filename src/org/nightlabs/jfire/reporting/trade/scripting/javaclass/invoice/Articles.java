/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.scripting.javaclass.invoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.oda.SQLResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultUtil;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate;
import org.nightlabs.jfire.reporting.trade.scripting.ReportingTradeScriptingUtil;
import org.nightlabs.jfire.scripting.AbstractScriptExecutorJavaClassDelegate;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.trade.Article;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class Articles
extends AbstractScriptExecutorJavaClassDelegate
implements ScriptExecutorJavaClassReportingDelegate {

	/**
	 * 
	 */
	public Articles() {
		super();
	}

	private JFSResultSetMetaData metaData;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			ReportingTradeScriptingUtil.addDefaultArticleFieldsToMetaData(metaData);
			ReportingTradeScriptingUtil.addPriceFragmentListToMetaData(getPersistenceManager(), metaData);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		Map<String, Object> param = getScriptExecutorJavaClass().getParameterValues();
		// WORKAROUND: This should really be passed to the query by the oda type.
//		InvoiceID invoiceID = (InvoiceID) param.get("invoiceID");
		InvoiceID invoiceID = (InvoiceID) JFireReportingHelper.getParameter("invoiceID");
		if (invoiceID == null)
			throw new IllegalArgumentException("The parameter invoiceID was not set.");
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		Query q = pm.newQuery(org.nightlabs.jfire.accounting.Invoice.class);
		q.setUnique(true);
		q.setFilter("this.organisationID == \""+invoiceID.organisationID+"\" &&");
		q.setFilter("this.invoiceIDPrefix == \""+invoiceID.invoiceIDPrefix+"\" && ");
		q.setFilter("this.invoiceID == "+invoiceID.invoiceID);

		Invoice invoice = (Invoice) q.execute();

		getResultSetMetaData();
		TableBuffer buffer = null;
		try {
			buffer = JFSResultUtil.createTableBuffer(metaData);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
		SortedMap<Long, Article> sortedArticles = new TreeMap<Long, Article>();
		for (Article article : invoice.getArticles()) {
			sortedArticles.put(article.getArticleID(), article);			
		}
		List<Object> row = new ArrayList<Object>();
		for (Article article : sortedArticles.values()) {
			row.clear();
			ReportingTradeScriptingUtil.addDefaultArticleFieldsToResultSet(article, row);
			ReportingTradeScriptingUtil.addPriceFragmentsToResultSet(getPersistenceManager(), article.getPrice(), row);
			try {
				buffer.addRecord(new Record(new ArrayList(row)));
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
