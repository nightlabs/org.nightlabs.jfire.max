/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.scripting;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.util.NLLocale;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptExecutorGeneralSalesStatistic extends AbstractJFSScriptExecutorDelegate {

	/**
	 * 
	 */
	public ScriptExecutorGeneralSalesStatistic() {
		super();
	}

	private JFSResultSetMetaData metaData;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			metaData.addColumn("productName", DataType.STRING);
			metaData.addColumn("articleCount", DataType.BIGDECIMAL);
			metaData.addColumn("sumAmount", DataType.BIGDECIMAL);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		JFSResultSet resultSet = new JFSResultSet((JFSResultSetMetaData)getResultSetMetaData());
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		Query q = pm.newQuery(
				"SELECT "+
				 "  this.product.productType, "+
				 "  COUNT(this.articleID), "+
				 "  SUM(this.price.amount) "+
				"FROM "+
				"  "+Article.class.getName()+" " +
				"GROUP BY "+
				"  this.product.productType"
			);
		Collection<Object[]> products = (Collection<Object[]>)q.execute();
		int count = 0;
		for (Iterator<Object[]> iter = products.iterator(); iter.hasNext();) {
			Object[] row = iter.next();
			Object[] nRow = new Object[3];
			nRow[0] = ((ProductType)row[0]).getName().getText(NLLocale.getDefault().getLanguage());
			nRow[1] = row[1];
			nRow[2] = row[2];
			resultSet.addRow(nRow);
			count++;
		}
		resultSet.init();
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doPrepare()
	 */
	public void doPrepare() throws ScriptException {
	}
}
