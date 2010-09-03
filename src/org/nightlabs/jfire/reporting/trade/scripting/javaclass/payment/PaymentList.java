/**
 *
 */
package org.nightlabs.jfire.reporting.trade.scripting.javaclass.payment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.accounting.pay.Payment;
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
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.TimePeriod;

/**
 * BIRT datasource javaclass script that lists {@link Payment}s.
 * It takes the following parameters:
 * <ul>
 *
 * </ul>
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class PaymentList extends AbstractJFSScriptExecutorDelegate {

	public PaymentList() {
		super();
	}

	private JFSResultSetMetaData metaData;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			metaData.addColumn("organisationID", DataType.STRING);
			metaData.addColumn("paymentID", DataType.BIGDECIMAL);
			metaData.addColumn("paymentDirection", DataType.STRING);
			metaData.addColumn("reasonForPayment", DataType.STRING);
			metaData.addColumn("modeOfPaymentFlavourJDOID", DataType.STRING);
			metaData.addColumn("currencyJDOID", DataType.STRING);
			metaData.addColumn("partnerJDOID", DataType.STRING);
			metaData.addColumn("partnerAccountJDOID", DataType.STRING);
			metaData.addColumn("amount", DataType.BIGDECIMAL);
			metaData.addColumn("userJDOID", DataType.STRING);
			metaData.addColumn("beginDT", DataType.DATETIME);
			metaData.addColumn("endDT", DataType.DATETIME);
			metaData.addColumn("postponed", DataType.BOOLEAN);
			metaData.addColumn("pending", DataType.BOOLEAN);
			metaData.addColumn("failed", DataType.BOOLEAN);
			metaData.addColumn("forceRollback", DataType.BOOLEAN);
			metaData.addColumn("invoiceIDs", DataType.STRING);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		// Map<String, Object> param = getScriptExecutorJavaClass().getParameterValues();
		Collection<UserID> users = getObjectParameterValue("userIDs", Collection.class);
//		Collection<UserID> userGroups = getObjectParameterValue("userGroupIDs", Collection.class);
		Collection<AnchorID> partnerIDs = getObjectParameterValue("partnerIDs", Collection.class);;
		Collection<ModeOfPaymentFlavourID> modeOfPaymentFlavourIDs = getObjectParameterValue("modeOfPaymentFlavourIDs", Collection.class);;
		TimePeriod beginTimePeriod = getObjectParameterValue("beginTimePeriod", TimePeriod.class);
		TimePeriod endTimePeriod = getObjectParameterValue("endTimePeriod", TimePeriod.class);

		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();

		// translate the given user-groups into users
//		if (userGroups != null) {
//			for (UserID userGroupID : userGroups) {
//				UserGroup userGroup = null;
//				try {
//					userGroup = (UserGroup) pm.getObjectById(userGroupID);
//				} catch (JDOObjectNotFoundException e) {
//					throw new ScriptException("Parameter userGroupIDs references non-existing UserGroup!", e);
//				}
//				for (UserLocal userLocal : userGroup.getAuthorizedObject().getMembers()) {
//					if (users == null) {
//						users = new HashSet<UserID>();
//					}
//					users.add(UserID.create(userLocal.getOrganisationID(), userLocal.getUserID()));
//				}
//			}
//		}

		StringBuffer jdoql = new StringBuffer();
		 jdoql.append("SELECT "+
		 "  this "+
		"FROM " +
		"  "+Payment.class.getName()+" " +
		"WHERE (1 == 1) ");
		Map<String, Object> jdoParams = new HashMap<String, Object>();

		// Filter by user
		if (users != null) {
			jdoql.append("&& ( ");
			int i = 0;
			for (Iterator<UserID> iterator = users.iterator(); iterator.hasNext();) {
				UserID userID = iterator.next();
				// TODO: WORKAROUND: JPOX Bug
//				jdoql.append("(JDOHelper.getObjectId(this.user) == :userID" + i + ") ");
				jdoql.append("(this.user.organisationID == \"" + userID.organisationID + "\" && this.user.userID == \"" + userID.userID + "\")");
				jdoParams.put("userID" + i, userID);
				if (iterator.hasNext())
					jdoql.append("|| ");
				i++;
			}
			jdoql.append(") ");
		}

		// filter by partner
		if (partnerIDs != null) {
			jdoql.append("&& ( ");
			int i = 0;
			for (Iterator<AnchorID> iterator = partnerIDs.iterator(); iterator.hasNext();) {
				AnchorID partnerID = iterator.next();
				// TODO: WORKAROUND: JPOX Bug
//				jdoql.append("(JDOHelper.getObjectId(this.partner) == :partnerID" + i + ") ");
//				jdoParams.put("partnerID"+i, partnerID);
				jdoql.append("(this.partner == :partner" + i + ") ");
				jdoParams.put("partner"+i, pm.getObjectById(partnerID));
				if (iterator.hasNext())
					jdoql.append("|| ");
				i++;
			}
			jdoql.append(") ");
		}

		// filter by modeOfPayment
		if (modeOfPaymentFlavourIDs != null && modeOfPaymentFlavourIDs.size() > 0) {
			jdoql.append("&& ( ");
			int i = 0;
			for (Iterator<ModeOfPaymentFlavourID> iterator = modeOfPaymentFlavourIDs.iterator(); iterator.hasNext();) {
				ModeOfPaymentFlavourID modeOfPaymentFlavourID = iterator.next();
				// TODO: WORKAROUND: JPOX Bug
//				jdoql.append("(JDOHelper.getObjectId(this.modeOfPaymentFlavour) == :modeOfPaymentFlavourID" + i + ") ");
//				jdoParams.put(":modeOfPaymentFlavourID" + i, modeOfPaymentFlavourID);
				jdoql.append("(this.modeOfPaymentFlavour == :modeOfPaymentFlavour" + i + ") ");
				jdoParams.put("modeOfPaymentFlavour" + i, pm.getObjectById(modeOfPaymentFlavourID));
				if (iterator.hasNext())
					jdoql.append("|| ");
				i++;
			}
			jdoql.append(") ");
		}

		// filter by begin time period
		ReportingScriptUtil.addTimePeriodCondition(jdoql, "this.beginDT", "beginDT", beginTimePeriod, jdoParams);
		// filter by begin time period
		ReportingScriptUtil.addTimePeriodCondition(jdoql, "this.endDT", "endDT", endTimePeriod, jdoParams);

		Query q = pm.newQuery(jdoql.toString());
		Collection<Payment> queryResult = (Collection<Payment>)q.executeWithMap(jdoParams);
		getResultSetMetaData();
		TableBuffer buffer = null;
		try {
			buffer = JFSResultUtil.createTableBuffer(metaData);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
		for (Iterator<Payment> iter = queryResult.iterator(); iter.hasNext();) {
			List<Object> row = new ArrayList<Object>(17);
			Payment payment = iter.next();
			row.add(payment.getOrganisationID());
			row.add(payment.getPaymentID());
			row.add(payment.getPaymentDirection());
			row.add(payment.getReasonForPayment());
			row.add(payment.getModeOfPaymentFlavourID().toString());
			row.add(payment.getCurrencyID().toString());
			row.add(payment.getPartnerID().toString());
			row.add(payment.getPartnerAccountID() != null ? payment.getPartnerAccountID().toString() : null);
			row.add(payment.getAmount());
			row.add(JDOHelper.getObjectId(payment.getUser()).toString());
			row.add(payment.getBeginDT());
			row.add(payment.getEndDT());
			row.add(payment.isPostponed());
			row.add(payment.isPending());
			row.add(payment.isFailed());
			row.add(payment.isForceRollback());
			row.add(JFireReportingHelper.createDataSetParam(payment.getInvoiceIDs()));
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
	@Override
	public void doPrepare() throws ScriptException {
	}

}
