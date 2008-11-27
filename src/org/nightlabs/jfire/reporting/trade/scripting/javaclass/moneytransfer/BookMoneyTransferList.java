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

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.accounting.book.BookMoneyTransfer;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.SQLResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultUtil;
import org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptUtil;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.util.TimePeriod;

/**
 * BIRT datasource javaclass script that lists {@link BookMoneyTransfer}s.
 * It takes the following parameters:
 * <ul>
 *   <li>timePeriod(optional): A {@link TimePeriod} to filter the transfers</li>
 *   <li>showOnlyOutstanding(optional): A {@link Boolean} indicating whether only transfers for
 *   outstanding invoices should be listed.</li>
 *   <li>initiatorIDs(optional): A {@link Collection} of {@link UserID}s. Filter the transfers by the user that initiated them.</li>
 * </ul>
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class BookMoneyTransferList extends AbstractJFSScriptExecutorDelegate {

	public BookMoneyTransferList() {
		super();
	}

	private JFSResultSetMetaData metaData;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			metaData.addColumn("anchorJDOID", DataType.STRING);
			metaData.addColumn("organisationID", DataType.STRING);
			metaData.addColumn("transferTypeID", DataType.STRING);
			metaData.addColumn("transferID", DataType.BIGDECIMAL);
			metaData.addColumn("containerJDOID", DataType.STRING);
			metaData.addColumn("fromAnchorJDOID", DataType.STRING);
			metaData.addColumn("toAnchorJDOID", DataType.STRING);
			metaData.addColumn("fromBalanceBeforeTransfer", DataType.BIGDECIMAL);
			metaData.addColumn("toBalanceBeforeTransfer", DataType.BIGDECIMAL);
			metaData.addColumn("initiatorJDOID", DataType.STRING);
			metaData.addColumn("timeStamp", DataType.TIMESTAMP);
			metaData.addColumn("currencyJDOID", DataType.STRING);
			metaData.addColumn("amount", DataType.BIGDECIMAL);
			metaData.addColumn("transferDescription", DataType.STRING);
			metaData.addColumn("invoiceJDOID", DataType.STRING);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		TimePeriod timePeriod = getObjectParameterValue("timePeriod", TimePeriod.class);
		Boolean showOnlyOutstanding = getObjectParameterValue("showOnlyOutstanding", Boolean.class);
		if (showOnlyOutstanding == null)
			showOnlyOutstanding = false;
		Collection<UserID> initiatorIDs = getObjectParameterValue("initiatorIDs", Collection.class);
		
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
		jdoql.append("SELECT "+
				"  this "+
				"FROM " +
				"  "+BookMoneyTransfer.class.getName()+" " +
		"WHERE (1 == 1) ");
		
		if (showOnlyOutstanding) {
			jdoql.append("&& this.invoice.invoiceLocal.outstanding");
		}
		
		
		Map<String, Object> jdoParams = new HashMap<String, Object>();

		// filter by time period
		ReportingScriptUtil.addTimePeriodCondition(jdoql, "this.timestamp", "timePeriod", timePeriod, jdoParams);

		// TODO: WORKAROUND: JPOX Bug
		//			jdoql.append("(JDOHelper.getObjectId(this.from) == :accountID || JDOHelper.getObjectId(this.to) == :accountID)");
		//			jdoParams.put("accountID", accountID);

		// Filter by initiators
		if (initiatorIDs != null) {
			jdoql.append("&& ( ");
			int i = 0;
			for (Iterator<UserID> it = initiatorIDs.iterator(); it.hasNext();) {
				UserID initiatorID = it.next();
				// TODO: WORKAROUND: JPOX Bug
				//					jdoql.append("JDOHelper.getObjectId(this.initiator) == :initiatorID" + i);
				//					jdoParams.put("initiatorID" + i, initiatorID);
				jdoql.append("(this.initiator == :initiator" + i +")");
				jdoParams.put("initiator" + i, pm.getObjectById(initiatorID));
				if (it.hasNext())
					jdoql.append("|| ");
				i++;
			}
			jdoql.append(")");
		}



		Query q = pm.newQuery(jdoql.toString());
		Collection<BookMoneyTransfer> queryResult = (Collection<BookMoneyTransfer>)q.executeWithMap(jdoParams);

		for (Iterator<BookMoneyTransfer> it = queryResult.iterator(); it.hasNext();) {
			List<Object> row = new ArrayList<Object>(18);
			BookMoneyTransfer moneyTransfer = it.next();
			row.add(JDOHelper.getObjectId(moneyTransfer).toString());
			row.add(moneyTransfer.getOrganisationID());
			row.add(moneyTransfer.getTransferTypeID());
			row.add(moneyTransfer.getTransferID());
			row.add(ReportingScriptUtil.getObjectJDOID(moneyTransfer.getContainer()));
			row.add(ReportingScriptUtil.getObjectJDOID(moneyTransfer.getFrom()));
			row.add(ReportingScriptUtil.getObjectJDOID(moneyTransfer.getTo()));
			row.add(moneyTransfer.getFromBalanceBeforeTransfer());
			row.add(moneyTransfer.getToBalanceBeforeTransfer());
			row.add(ReportingScriptUtil.getObjectJDOID(moneyTransfer.getInitiator()));
			row.add(moneyTransfer.getTimestamp());
			row.add(ReportingScriptUtil.getObjectJDOID(moneyTransfer.getCurrency()));
			row.add(moneyTransfer.getAmount());
			row.add(moneyTransfer.getDescription());
			row.add(JDOHelper.getObjectId(moneyTransfer.getInvoice()).toString());
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
