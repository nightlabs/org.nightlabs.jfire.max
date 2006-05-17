/**
 * 
 */
package org.nightlabs.jfire.reporting.scripting;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.ResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;
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
		JFSResultSet resultSet = new JFSResultSet((JFSResultSetMetaData)getResultSetMetaData());
		Map<String, Object> param = getScriptExecutorJavaClass().getParameterValues();
		PersistenceManager pm = (PersistenceManager)param.get("persistenceManager");
		Query q = pm.newQuery(
				"SELECT "+
				"  this.getProduct().getProductType().getName().getText(\"en\"), " +
				"  COUNT(this), " +
				"  SUM(this.getPrice().getAmount()) " +
				"FROM " +
				"  "+Article.class.getName()+" "+				
				"GROUP BY " +
				"  this.getProduct()"
			);
		Collection products = (Collection)q.execute();
		for (Iterator iter = products.iterator(); iter.hasNext();) {
			Object[] row = (Object[]) iter.next();
			resultSet.addRow(row);
		}
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
