/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.scripting.delivery;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.SQLResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultUtil;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;
import org.nightlabs.jfire.trade.Article;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class DeliveryList implements
		ScriptExecutorJavaClassReportingDelegate {

	/**
	 * 
	 */
	public DeliveryList() {
		super();
	}

	private JFSResultSetMetaData metaData;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			metaData.addColumn("article", DataType.JAVA_OBJECT);
			metaData.addColumn("createUser", DataType.JAVA_OBJECT);
			metaData.addColumn("createDT", DataType.DATE);
			metaData.addColumn("productType", DataType.JAVA_OBJECT);
			metaData.addColumn("vendor", DataType.JAVA_OBJECT);
			metaData.addColumn("customer", DataType.JAVA_OBJECT);
			metaData.addColumn("order", DataType.JAVA_OBJECT);
			metaData.addColumn("offer", DataType.JAVA_OBJECT);
			metaData.addColumn("deliveryNote", DataType.JAVA_OBJECT);
			metaData.addColumn("invoice", DataType.JAVA_OBJECT);
			metaData.addColumn("delivered", DataType.BOOLEAN);
			metaData.addColumn("outstanding", DataType.BOOLEAN);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		Map<String, Object> param = getScriptExecutorJavaClass().getParameterValues();
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		String jdoql = 				
		 "SELECT "+
		 "  this, "+
		 "  this.createUser, "+
		 "  this.createDT, "+
		 "  this.productType, "+
		 "  this.order.vendor, "+
		 "  this.order.customer, "+ 
		 "  this.order, "+
		 "  this.offer, "+
		 "  this.deliveryNote, "+
		 "  this.invoice, "+
		 "  this.articleLocal.delivered, "+
		 "  this.invoice.invoiceLocal.outstanding "+
		"FROM "+
		"  "+Article.class.getName()+" "
		;

		Query q = pm.newQuery(jdoql);
		Collection queryResult = (Collection)q.execute();
		getResultSetMetaData();
		TableBuffer buffer = null;
		try {
			buffer = JFSResultUtil.createTableBuffer(metaData);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
		for (Iterator iter = queryResult.iterator(); iter.hasNext();) {
			try {
				buffer.addRecord(new Record((Object[]) iter.next()));
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
