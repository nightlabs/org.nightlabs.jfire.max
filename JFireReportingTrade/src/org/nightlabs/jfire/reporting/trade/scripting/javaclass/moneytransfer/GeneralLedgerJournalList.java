/**
 *
 */
package org.nightlabs.jfire.reporting.trade.scripting.javaclass.moneytransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.SQLResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultUtil;
import org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptUtil;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.util.TimePeriod;
import org.nightlabs.util.Util;

/**
 * <ul>
 *   <li>timePeriod(optional): A {@link TimePeriod} to filter the transfers</li>
 * </ul>
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class GeneralLedgerJournalList extends AbstractJFSScriptExecutorDelegate {

	/** Logger for this class */
	private static final Logger logger = Logger.getLogger(GeneralLedgerJournalList.class);
	
	private static final String COL_ENTRY_TYPE = "journalEntryType";
	private static final String COL_ROW_NUMBER = "rowNumber";
	private static final String COL_AMOUNT = "amount";
	private static final String COL_CONTAINER = "containerJDOID";
	private static final String COL_REF_ANCHOR = "refAnchorJDOID";
	
	public static final String ENTRY_TYPE_SUMMARY = "Summary";
	public static final String ENTRY_TYPE_CREDIT = "Credit";
	public static final String ENTRY_TYPE_DEBIT = "Debit";
	
	public GeneralLedgerJournalList() {
		super();
	}

	private JFSResultSetMetaData metaData;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			metaData.addColumn(COL_ENTRY_TYPE, DataType.STRING); // Credit / Debit / Summary
			// rowNumber is used for downward compatibility, as the row.___number variable was introduced in Birt 2.5
			metaData.addColumn(COL_ROW_NUMBER, DataType.INTEGER);
			metaData.addColumn("timeStamp", DataType.DATETIME);
			metaData.addColumn(COL_CONTAINER, DataType.STRING);
			metaData.addColumn(COL_REF_ANCHOR, DataType.STRING);
			metaData.addColumn("refAnchorIsAccount", DataType.STRING);
			metaData.addColumn("refAnchorDescription", DataType.STRING);
			metaData.addColumn("currencyJDOID", DataType.STRING);
			metaData.addColumn("currencySymbol", DataType.STRING);
			metaData.addColumn(COL_AMOUNT, DataType.BIGDECIMAL);
			metaData.addColumn("transferDescription", DataType.STRING);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		TimePeriod timePeriod = getObjectParameterValue("timePeriod", TimePeriod.class);
		
		Boolean addSummaryRows = getObjectParameterValue("addSummaryRows", Boolean.class);
		if (addSummaryRows == null)
			addSummaryRows = true;
		Boolean consolidate = getObjectParameterValue("consolidate", Boolean.class);
		if (consolidate == null)
			consolidate = true;
		Boolean excludeLegalEntities = getObjectParameterValue("excludeLegalEntities", Boolean.class);
		if (excludeLegalEntities == null)
			excludeLegalEntities = true;
		
		if (logger.isDebugEnabled()) {
			logger.debug("Starting " + getClass().getSimpleName() + " with the following parameters:");
			logger.debug("  timePeriod: " + timePeriod);
			logger.debug("  addSummaryRows: " + addSummaryRows);
			logger.debug("  consolidate: " + consolidate);
			logger.debug("  excludeLegalEntities: " + excludeLegalEntities);
		}
		
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
				"  "+MoneyTransfer.class.getName()+" " +
		"WHERE (1 == 1) ");

		Map<String, Object> jdoParams = new HashMap<String, Object>();

		// filter by time period
		ReportingScriptUtil.addTimePeriodCondition(jdoql, "this.timestamp", "timePeriod", timePeriod, jdoParams);
		
		jdoql.append("ORDER BY this.transferID ASC, this.container.transferID ASC");

		if (logger.isDebugEnabled()) {
			logger.debug("Executing JDOQL: " + jdoql.toString());
		}
		Query q = pm.newQuery(jdoql.toString());
		Collection<MoneyTransfer> queryResult = (Collection<MoneyTransfer>)q.executeWithMap(jdoParams);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Have " + queryResult.size() + " rows in query result.");
		}
		Transfer lastContainer = null;
		for (Iterator<MoneyTransfer> it = queryResult.iterator(); it.hasNext();) {
			MoneyTransfer moneyTransfer = it.next();

			// The container is never null, if the moneyTransfer has no container, the transfer
			// itself will be set as container -> consolidate relies on that fact
			Transfer container = moneyTransfer.getContainer() != null ? moneyTransfer.getContainer() : moneyTransfer;
			boolean containerChange = !Util.equals(lastContainer, container);
			lastContainer = container;
			String containerJDOID = JDOHelper.getObjectId(container).toString();
			
			if (addSummaryRows && containerChange) {
				// If we should add summary rows and we just switched to a new container, we add the
				// summary row
				List<Object> row = createResultRow();
				row.add(ENTRY_TYPE_SUMMARY);
				row.add(null);
				row.add(null);
				row.add(containerJDOID);
				row.add(false);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(moneyTransfer.getDescription());
				addRecord(buffer, row);
			}
			
			if (consolidate && containerChange) {
				// if we should consolidate and just switched containers, do it
				consolidate(buffer);
			}
			
			// For each transfer we add two records:
			// One with entry-type credit and refAccount the to-account (where money goes to)
			// One with entry-type debit and refAccount the from-account (where money is taken away)
			
			boolean toIsAccount = moneyTransfer.getTo() instanceof Account; 
			boolean addCredit = toIsAccount || (!excludeLegalEntities);			
			if (addCredit) {
				List<Object> row = createResultRow();
				row.add(ENTRY_TYPE_CREDIT);
				row.add(null);
				row.add(moneyTransfer.getTimestamp());
				if (!addSummaryRows) row.add(containerJDOID); else row.add(null);
				row.add(JDOHelper.getObjectId(moneyTransfer.getTo()).toString());
				row.add(toIsAccount);
				row.add(moneyTransfer.getTo().getDescription());
				row.add(JDOHelper.getObjectId(moneyTransfer.getCurrency()).toString());
				row.add(moneyTransfer.getCurrency().getCurrencySymbol());
				row.add(moneyTransfer.getAmount());
				if (!addSummaryRows) row.add(moneyTransfer.getDescription()); else row.add(null);
				// add the record
				addRecord(buffer, row);
			}
			
			boolean fromIsAccount = moneyTransfer.getFrom() instanceof Account; 
			boolean addDebit = fromIsAccount || (!excludeLegalEntities);
			if (addDebit) {
				List<Object> row = createResultRow();
				row.add(ENTRY_TYPE_DEBIT);
				row.add(null);
				row.add(moneyTransfer.getTimestamp());
				if (!addSummaryRows) row.add(containerJDOID); else row.add(null);
				row.add(JDOHelper.getObjectId(moneyTransfer.getFrom()).toString());
				row.add(moneyTransfer.getFrom() instanceof Account);
				row.add(moneyTransfer.getFrom().getDescription());
				row.add(JDOHelper.getObjectId(moneyTransfer.getCurrency()).toString());
				row.add(moneyTransfer.getCurrency().getCurrencySymbol());
				row.add(moneyTransfer.getAmount());
				if (!addSummaryRows) row.add(moneyTransfer.getDescription()); else row.add(null);
				// add the record
				addRecord(buffer, row);
			}
		}

		setRowNumbers(buffer);
		
		return createResultSet(buffer);
	}
	
	private SQLResultSet createResultSet(TableBuffer buffer) throws ScriptException {
		try {
			SQLResultSet resultSet = new SQLResultSet(buffer);
			resultSet.init();
			return resultSet;
		} catch (Exception e) {
			throw new ScriptException("Could not create SQLResultSet (final result-set)", e);
		}
	}

	private void addRecord(TableBuffer buffer, List<Object> row) throws ScriptException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Adding record to result-set: " + row);
			}
			buffer.addRecord(new Record(row));
		} catch (Exception e) {
			throw new ScriptException("Failed to add a row to the result-set buffer", e);
		}
	}
	
	private ArrayList<Object> createResultRow() throws ScriptException {
		try {
			return new ArrayList<Object>(metaData.getColumnCount());
		} catch (JFireOdaException e) {
			throw new ScriptException("Failed creating result row", e);
		}
	}
	
	private void consolidate(TableBuffer buffer) throws ScriptException {
		if (logger.isDebugEnabled()) {
			logger.debug("consolidate: Starting transfer-consolidation");
		}
		int colEntryType = metaData.getColumnIndex(COL_ENTRY_TYPE);
		int colRefAnchor = metaData.getColumnIndex(COL_REF_ANCHOR);
		int colAmount = metaData.getColumnIndex(COL_AMOUNT);
		int colContainer = metaData.getColumnIndex(COL_CONTAINER);
		Record summaryRecord = null;
		boolean first = true;
		try {
			if (buffer.last()) {
				
				// if the last record was as summary record, we remember that record for later
				// re-adding but delete it now
				String entryType = buffer.getString(1);
				if (ENTRY_TYPE_SUMMARY.equals(entryType)) {
					if (logger.isDebugEnabled()) {
						logger.debug("consolidate: Found summary record at last position, remembering and deleting it");
					}
					summaryRecord = buffer.getRecord();
					buffer.deleteRow();
//					buffer.previous();
				}
				
				if (logger.isDebugEnabled()) {
					logger.debug("consolidate: Starting consolidation loop");
				}
				// Now we iterate and group amounts by entryType and account until we reach another
				// container, or a summary row, while we iterate we remove the Records we find and
				// re-add the consolidated ones afterwards.
				Map<String, Record> typeAndRefAnchor2Record = new HashMap<String, Record>();
				List<String> typeAndRefOrder = new LinkedList<String>();
				String lastContainer = null;
				while (buffer.previous()) {
					Record record = buffer.getRecord();
					if (logger.isDebugEnabled()) {
						logger.debug("consolidate: consolidate loop processing record: " + getRecordDebugString(record));
					}
					Object recordEntryType = record.getObject(colEntryType);
					if (ENTRY_TYPE_SUMMARY.equals(recordEntryType)) {
						if (logger.isDebugEnabled()) {
							logger.debug("consolidate: Reached summary row, consolidate loop finished");
						}
						// if we reached a summary row we stop
						break;
					}
					String currContainer = (String) record.getObject(colContainer);
					if (!first && !Util.equals(currContainer, lastContainer)) {
						if (logger.isDebugEnabled()) {
							logger.debug("consolidate: Reached other container row, consolidate loop finished.");
							logger.debug("  lastContainer: " + lastContainer);
							logger.debug("  currContainer: " + currContainer);
						}
						// if we reached another container we stop
						break;
					}
					lastContainer = currContainer;
					first = false;
					String typeAndRefKey = recordEntryType + "#" + record.getObject(colRefAnchor);
					Record mappedRecord = typeAndRefAnchor2Record.get(typeAndRefKey);
					if (mappedRecord == null) {
						if (logger.isDebugEnabled()) {
							logger.debug("consolidate: No mapped record found for " + typeAndRefKey);
						}
						mappedRecord = record;
						typeAndRefAnchor2Record.put(typeAndRefKey, mappedRecord);
						typeAndRefOrder.add(0, typeAndRefKey);
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("consolidate: Found mapped record found for " + typeAndRefKey);
							logger.debug("  record.amount:            " + record.getObject(colAmount));
							logger.debug("  mapped.amount:            " + mappedRecord.getObject(colAmount));
							logger.debug("  setting mapped.amount to: " + ((Long) mappedRecord.getObject(colAmount) + (Long) record.getObject(colAmount)));
						}
						mappedRecord.setObject(colAmount, (Long) mappedRecord.getObject(colAmount) + (Long) record.getObject(colAmount));
					}
					buffer.deleteRow();
				}
				
				if (logger.isDebugEnabled()) {
					logger.debug("consolidate: Re-adding consolidated rows");
				}
				// Now we re-add the changed/consolidated records to the buffer.
				for (String typeAndRefKey : typeAndRefOrder) {
					Record record = typeAndRefAnchor2Record.get(typeAndRefKey);
					if (record == null) {
						throw new IllegalStateException("Failed consolidating data, could not find registered consolidated record " + typeAndRefKey);
					}
					buffer.addRecord(record);
				}
				// If we removed a summary row above, we re-add it
				if (summaryRecord != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("consolidate: Re-adding remembered summary row");
					}
					buffer.addRecord(summaryRecord);
				}
			}
		} catch (Exception e) {
			throw new ScriptException("Failed consolidating data", e);
		}
	}
	
	private void setRowNumbers(TableBuffer buffer) throws ScriptException {
		int colNum = metaData.getColumnIndex(COL_ROW_NUMBER);
		try {
			if (buffer.first()) {
				do {
					buffer.getRecord().setObject(colNum, buffer.getRow());
				} while (buffer.next());
			}
		} catch (Exception e) {
			throw new ScriptException("Failed setting rowNumbers", e);
		}
	}
	
	private String getRecordDebugString(Record record) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < record.getFieldCount(); i++) {
			sb.append(record.getObject(i + 1));
			if (i < record.getFieldCount() - 1) {
				sb.append(", ");
			}
		}
		return sb.append("]").toString();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doPrepare()
	 */
	@Override
	public void doPrepare() throws ScriptException {
	}

}
