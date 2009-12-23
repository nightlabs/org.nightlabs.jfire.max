/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.scripting.javaclass.delivery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.SQLResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultUtil;
import org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptUtil;
import org.nightlabs.jfire.reporting.trade.scripting.ReportingTradeScriptingUtil;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.TimePeriod;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class DeliveryList extends AbstractJFSScriptExecutorDelegate {

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
			metaData.addColumn("articleID", DataType.STRING);
			metaData.addColumn("createUserID", DataType.STRING);
			metaData.addColumn("createDT", DataType.DATETIME);
			metaData.addColumn("productTypeID", DataType.STRING);
			metaData.addColumn("vendorID", DataType.STRING);
			metaData.addColumn("customerID", DataType.STRING);
			metaData.addColumn("orderID", DataType.STRING);
			metaData.addColumn("offerID", DataType.STRING);
			metaData.addColumn("deliveryNoteID", DataType.STRING);
			metaData.addColumn("invoiceID", DataType.STRING);
			metaData.addColumn("delivered", DataType.BOOLEAN);
			metaData.addColumn("outstanding", DataType.BOOLEAN);
			ReportingTradeScriptingUtil.addPriceFragmentListToMetaData(getPersistenceManager(), metaData);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		UserID createUserID = getObjectParameterValue("createUserID", UserID.class);
		TimePeriod createTimePeriod = getObjectParameterValue("createTimePeriod", TimePeriod.class);
		TimePeriod deliveryTimePeriod = getObjectParameterValue("deliveryTimePeriod", TimePeriod.class);
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		StringBuffer jdoql = new StringBuffer();
		 jdoql.append("SELECT "+
		 "  this, "+
		 "  JDOHelper.getObjectId(this.createUser), "+
		 "  this.createDT, "+
		 "  JDOHelper.getObjectId(this.productType), "+
		 "  JDOHelper.getObjectId(this.order.vendor), "+
		 "  JDOHelper.getObjectId(this.order.customer), "+
		 "  JDOHelper.getObjectId(this.order), "+
		 "  JDOHelper.getObjectId(this.offer), "+
		 "  JDOHelper.getObjectId(this.deliveryNote), "+
		 "  JDOHelper.getObjectId(this.invoice), "+
		 "  this.articleLocal.delivered, "+
		 "  this.invoice.invoiceLocal.outstanding "+
		"FROM "+
		"  "+Article.class.getName()+" "+
		"WHERE (1 == 1) "
		);
		Map<String, Object> jdoParams = new HashMap<String, Object>();
		
		if (createUserID != null) {
			jdoql.append("&& JDOHelper.getObjectId(this.createUser) == :createUserID ");
			jdoParams.put("createUserID", createUserID);
		}
		
		ReportingScriptUtil.addTimePeriodCondition(jdoql, "this.createDT", "createDT", createTimePeriod, jdoParams);
		
		ReportingScriptUtil.addTimePeriodCondition(jdoql, "this.articleLocal.delivery.beginDT", "deliveryDT", deliveryTimePeriod, jdoParams);
		
		Query q = pm.newQuery(jdoql);
		Collection<?> queryResult = (Collection<?>)q.executeWithMap(jdoParams);
		getResultSetMetaData();
		TableBuffer buffer = null;
		try {
			buffer = JFSResultUtil.createTableBuffer(metaData);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
		List<Object> row = new ArrayList<Object>(12);
		for (Iterator<?> iter = queryResult.iterator(); iter.hasNext();) {
			row.clear();
			CollectionUtil.addAllToCollection((Object[]) iter.next(), row);
			Article article = (Article) row.get(0);
			ReportingTradeScriptingUtil.addPriceFragmentsToResultSet(getPersistenceManager(), article.getPrice(), row);
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
	public void doPrepare() throws ScriptException {
	}

}
