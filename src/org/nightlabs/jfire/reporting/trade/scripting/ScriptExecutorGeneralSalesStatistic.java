/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.scripting;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptExecutorGeneralSalesStatistic implements
		ScriptExecutorJavaClassReportingDelegate {

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
		System.out.println("**********************************************");
		System.out.println("**********************************************");
		System.out.println("   doExecute");
		System.out.println("**********************************************");
		System.out.println("**********************************************");
		JFSResultSet resultSet = new JFSResultSet((JFSResultSetMetaData)getResultSetMetaData());
		Map<String, Object> param = getScriptExecutorJavaClass().getParameterValues();
		PersistenceManager pm = (PersistenceManager)param.get("persistenceManager");
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
		Collection products = (Collection)q.execute();
		int count = 0;
		for (Iterator iter = products.iterator(); iter.hasNext();) {
			Object[] row = (Object[]) iter.next();
			Object[] nRow = new Object[3];
			nRow[0] = ((ProductType)row[0]).getName().getText(Locale.getDefault().getLanguage());
			nRow[1] = row[1];
			nRow[2] = row[2];
			resultSet.addRow(nRow);
			count++;
		}
		System.out.println("**********************************************");
		System.out.println("**********************************************");
		System.out.println("   doExecute returning resultSet "+resultSet);
		System.out.println("                      with #rows "+count);
		System.out.println("**********************************************");
		System.out.println("**********************************************");
		resultSet.init();
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doPrepare()
	 */
	public void doPrepare() throws ScriptException {
		// TODO Auto-generated method stub

	}
	
	private ScriptExecutorJavaClass scriptExecutorJavaClass;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#getScriptExecutorJavaClass()
	 */
	public ScriptExecutorJavaClass getScriptExecutorJavaClass() {
		return scriptExecutorJavaClass;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#setScriptExecutorJavaClass(org.nightlabs.jfire.scripting.ScriptExecutorJavaClass)
	 */
	public void setScriptExecutorJavaClass(
			ScriptExecutorJavaClass scriptExecutorJavaClass) {
		this.scriptExecutorJavaClass = scriptExecutorJavaClass;
	}

}
