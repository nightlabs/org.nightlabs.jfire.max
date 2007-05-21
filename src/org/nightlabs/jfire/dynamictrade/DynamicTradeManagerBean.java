package org.nightlabs.jfire.dynamictrade;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductTypeActionHandler;
import org.nightlabs.jfire.dynamictrade.store.Unit;
import org.nightlabs.jfire.dynamictrade.store.id.UnitID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.id.ProductTypeID;

/**
 * @ejb.bean name="jfire/ejb/JFireDynamicTrade/DynamicTradeManager"	
 *					 jndi-name="jfire/ejb/JFireDynamicTrade/DynamicTradeManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 */
public abstract class DynamicTradeManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"	
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It creates the root DynamicProductType for the organisation itself.
	 * DynamicProductTypes of other organisations cannot be imported or
	 * traded as reseller.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ProductTypeID rootID = ProductTypeID.create(getOrganisationID(), DynamicProductType.class.getName());
			try {
				pm.getObjectById(rootID);
				return; // already existing
			} catch (JDOObjectNotFoundException x) {
				Store store = Store.getStore(pm);
				User user = User.getUser(pm, getPrincipal());
				DynamicProductType root = new DynamicProductType(
						rootID.organisationID, rootID.productTypeID,
						null,
						store.getMandator(),
						ProductType.INHERITANCE_NATURE_BRANCH,
						ProductType.PACKAGE_NATURE_OUTER);
				root.getName().setText(Locale.ENGLISH.getLanguage(), LocalOrganisation.getLocalOrganisation(pm).getOrganisation().getPerson().getDisplayName());
				store.addProductType(user, root, DynamicProductTypeActionHandler.getDefaultHome(pm, root));
				store.setProductTypeStatus_published(user, root);
			}

			Unit unit = new Unit(IDGenerator.getOrganisationID(), IDGenerator.nextID(Unit.class));
			unit.getSymbol().setText(Locale.ENGLISH.getLanguage(), "h");
			unit.getName().setText(Locale.ENGLISH.getLanguage(), "hour");
			unit.getName().setText(Locale.GERMAN.getLanguage(), "Stunde");
			pm.makePersistent(unit);

			unit = new Unit(IDGenerator.getOrganisationID(), IDGenerator.nextID(Unit.class));
			unit.getSymbol().setText(Locale.ENGLISH.getLanguage(), "pcs.");
			unit.getName().setText(Locale.ENGLISH.getLanguage(), "pieces");
			unit.getSymbol().setText(Locale.GERMAN.getLanguage(), "Stk.");
			unit.getName().setText(Locale.GERMAN.getLanguage(), "Stück");
			pm.makePersistent(unit);

			unit = new Unit(IDGenerator.getOrganisationID(), IDGenerator.nextID(Unit.class));
			unit.getSymbol().setText(Locale.ENGLISH.getLanguage(), "()");
			unit.getName().setText(Locale.ENGLISH.getLanguage(), "(spot-rate)");
			unit.getSymbol().setText(Locale.GERMAN.getLanguage(), "()");
			unit.getName().setText(Locale.GERMAN.getLanguage(), "(pauschal)");
			pm.makePersistent(unit);

		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<ProductTypeID> getChildDynamicProductTypeIDs(
			ProductTypeID parentDynamicProductTypeID) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(DynamicProductType.getChildProductTypes(pm,
					parentDynamicProductTypeID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<ProductTypeID> getDynamicProductTypeIDs(Byte inheritanceNature, Boolean saleable) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(DynamicProductType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			if (inheritanceNature != null || saleable != null) {
				StringBuffer filter = new StringBuffer();

				if (inheritanceNature != null)
					filter.append("inheritanceNature == :inheritanceNature");

				if (saleable != null) {
					if (filter.length() != 0)
						filter.append(" && ");

					filter.append("saleable == :saleable");
				}

				q.setFilter(filter.toString());
			}

			HashMap<String, Object> params = new HashMap<String, Object>(2);
			params.put("inheritanceNature", inheritanceNature);
			params.put("saleable", saleable);

			return new HashSet<ProductTypeID>((Collection<? extends ProductTypeID>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<DynamicProductType> getDynamicProductTypes(
			Collection<ProductTypeID> dynamicProductTypeIDs, String[] fetchGroups,
			int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dynamicProductTypeIDs,
					DynamicProductType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public DynamicProductType storeDynamicProductType(DynamicProductType dynamicProductType, boolean get,
			String[] fetchGroups, int maxFetchDepth) {
		if (dynamicProductType == null)
			throw new IllegalArgumentException("dynamicProductType must not be null!");

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups == null)
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			else
				pm.getFetchPlan().setGroups(fetchGroups);

//			try {
//				DynamicProductLocalAccountantDelegate delegate = (DynamicProductLocalAccountantDelegate) dynamicProductType
//						.getLocalAccountantDelegate();
//				if (delegate != null) {
//					OrganisationLegalEntity organisationLegalEntity = null;
//
//					for (Account account : delegate.getAccounts().values()) {
//						try {
//							if (account.getOwner() == null) {
//								if (organisationLegalEntity == null)
//									organisationLegalEntity = OrganisationLegalEntity
//											.getOrganisationLegalEntity(pm, getOrganisationID(),
//													OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION,
//													true);
//
//								account.setOwner(organisationLegalEntity);
//							}
//						} catch (JDODetachedFieldAccessException x) {
//							// ignore
//						}
//					}
//				}
//			} catch (JDODetachedFieldAccessException x) {
//				// ignore
//			}

			// we don't need any price calculation as we have dynamic prices only - no cached values

			if (NLJDOHelper.exists(pm, dynamicProductType)) {
				dynamicProductType = (DynamicProductType) pm.makePersistent(dynamicProductType);
			} else {
				dynamicProductType = (DynamicProductType) Store.getStore(pm).addProductType(User.getUser(pm, getPrincipal()),
						dynamicProductType,
						DynamicProductTypeActionHandler.getDefaultHome(pm, dynamicProductType));
			}

			// take care about the inheritance
			dynamicProductType.applyInheritance();

			if (!get)
				return null;

			return (DynamicProductType) pm.detachCopy(dynamicProductType);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<DynamicTradePriceConfig> storeDynamicTradePriceConfigs(Collection<DynamicTradePriceConfig> priceConfigs, boolean get, ProductTypeID productTypeID, PriceConfigID innerPriceConfigID)
	throws PriceCalculationException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				pm.getFetchPlan().setGroups(new String[] {
						FetchPlan.DEFAULT, 
						FetchGroupsPriceConfig.FETCH_GROUP_EDIT});
			}

			// Because we do not need to calculate any prices (all prices are dynamic), we
			// do not need to use GridPriceConfigUtil.storePriceConfigs(...), but simply
			// call pm.makePersistentAll(...).

			priceConfigs = pm.makePersistentAll(priceConfigs);

			if (productTypeID != null && innerPriceConfigID != null) {
				ProductType pt = (ProductType) pm.getObjectById(productTypeID);
				IInnerPriceConfig pc = (IInnerPriceConfig) pm.getObjectById(innerPriceConfigID);
				pt.setInnerPriceConfig(pc);
			}

			if (get)
				return pm.detachCopyAll(priceConfigs);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Set<PriceConfigID> getDynamicTradePriceConfigIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(DynamicTradePriceConfig.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<PriceConfigID>((Collection<? extends PriceConfigID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	@SuppressWarnings("unchecked")
	public List<DynamicTradePriceConfig> getDynamicTradePriceConfigs(Collection<PriceConfigID> dynamicTradePriceConfigIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dynamicTradePriceConfigIDs, DynamicTradePriceConfig.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Set<UnitID> getUnitIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Unit.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<UnitID>((Collection<? extends UnitID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	@SuppressWarnings("unchecked")
	public List<Unit> getUnits(Collection<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, unitIDs, Unit.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
