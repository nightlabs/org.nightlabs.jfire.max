package org.nightlabs.jfire.trade.dashboard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.ejb.Stateless;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.transfer.id.AnchorID;

@Stateless
public class DashboardManagerBean extends BaseSessionBeanImpl implements DashboardManagerRemote {

	private static final String MODULE_ID = "JFireTradeDashboardEAR";

	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(DashboardManagerBean.class);
	
	public DashboardManagerBean() {
	}
	
	@Override
	public void initialise() throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, MODULE_ID);
			if (moduleMetaData == null) {
				moduleMetaData = ModuleMetaData.initModuleMetadata(pm, MODULE_ID, DashboardManagerBean.class);
				intialiseNewOrganisation(pm);
			}
		} finally {
			pm.close();
		}
	}
	
	private void intialiseNewOrganisation(PersistenceManager pm) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating DashboardLayoutConfigModuleInitialiser");
		}
		TradeDashboardGadgetsConfigModuleInitialiser dashboardLayoutConfigModuleInitialiser = new TradeDashboardGadgetsConfigModuleInitialiser();
		pm.makePersistent(dashboardLayoutConfigModuleInitialiser);
	}

	@Override
	public List<LastCustomerTransaction> searchLastCustomerTransactions(DashboardGadgetLastCustomersConfig lastCustomersConfig) {
		PersistenceManager pm = createPersistenceManager();
		try {
			List<LastCustomerTransaction> result = new LinkedList<LastCustomerTransaction>();
			
			TreeMap<Date, LastCustomerTransaction> orderedTransactions = new TreeMap<Date, LastCustomerTransaction>(new Comparator<Date>() {
				@Override
				public int compare(Date o1, Date o2) {
					return -(o1.compareTo(o2));
				}
			});
			int maxCustomers = lastCustomersConfig != null ? lastCustomersConfig.getAmountLastCustomers() : DashboardGadgetLastCustomersConfig.maxCustomersInDashboard;
			
			putAll(queryLastCustomersForArticleContainer(pm, maxCustomers, Order.class, "created"), orderedTransactions);
			putAll(queryLastCustomersForArticleContainer(pm, maxCustomers, Offer.class, "created"), orderedTransactions);
			putAll(queryLastCustomersForArticleContainer(pm, maxCustomers, Offer.class, "finalized"), orderedTransactions);
			putAll(queryLastCustomersForArticleContainer(pm, maxCustomers, Invoice.class, "created"), orderedTransactions);
			putAll(queryLastCustomersForArticleContainer(pm, maxCustomers, Invoice.class, "finalized"), orderedTransactions);
			putAll(queryLastCustomersForArticleContainer(pm, maxCustomers, DeliveryNote.class, "created"), orderedTransactions);
			putAll(queryLastCustomersForArticleContainer(pm, maxCustomers, DeliveryNote.class, "finalized"), orderedTransactions);

			Set<AnchorID> distinctCustomers = new HashSet<AnchorID>();
			
			for (LastCustomerTransaction transaction : orderedTransactions.values()) {
				if (!distinctCustomers.contains(transaction.getCustomerID())) {
					result.add(transaction);
					distinctCustomers.add(transaction.getCustomerID());
				}
				if (distinctCustomers.size() == maxCustomers) {
					break;
				}
			}
			
			return result;
		} finally {
			pm.close();
		}
	}
	
	private void putAll(List<LastCustomerTransaction> transactions, Map<Date, LastCustomerTransaction> map) {
		for (LastCustomerTransaction trans : transactions) {
			map.put(trans.getTransactionDate(), trans);
		}
	}

	private List<LastCustomerTransaction> queryLastCustomersForArticleContainer(
			PersistenceManager pm,
			int maxCustomers, Class<?> articleContainerClass, String transactionType) {

		String field = "created".equals(transactionType) ? "this.createDT" : "this.finalizeDT";
		String userField = "created".equals(transactionType) ? "this.createUser" : "this.finalizeUser";
		String customerField = articleContainerClass != Offer.class ? "customer" : "this.order.customer";
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT ");
		sb.append(customerField).append(", ");
		sb.append("max(").append(field).append(") ");
		sb.append("FROM ");
		sb.append(articleContainerClass.getName()).append(" ");
		sb.append("WHERE ").append(field).append(" != null");
		sb.append(" && JDOHelper.getObjectId(").append(userField).append(") == :userId");
		Query query = pm.newQuery(sb.toString());
		
		query.setRange(0, maxCustomers);
		query.setOrdering("max(" + field+ ") DESC");
		query.setGrouping(customerField);
		
		Collection<Object[]> queryResult = (Collection<Object[]>) query.execute(SecurityReflector.getUserDescriptor().getUserObjectID());
		
		
		List<LastCustomerTransaction> queryTransactions = new LinkedList<LastCustomerTransaction>();
		for (Object[] resultRow : queryResult) {
			queryTransactions.add(
					new LastCustomerTransaction(
							(AnchorID) JDOHelper.getObjectId(resultRow[0]), 
							articleContainerClass.getSimpleName()+"."+transactionType, 
							(Date) resultRow[1]
					)
			);
//			queryTransactions.add(new LastCustomerTransaction((AnchorID) JDOHelper.getObjectId(resultRow[0]), (String) resultRow[1], (Date) resultRow[2]));
		}
		return queryTransactions;
	}
	
}