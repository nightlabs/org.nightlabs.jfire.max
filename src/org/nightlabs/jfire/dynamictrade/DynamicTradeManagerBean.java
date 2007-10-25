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

import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.PackagePriceConfig;
import org.nightlabs.jfire.dynamictrade.store.DynamicProduct;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductTypeActionHandler;
import org.nightlabs.jfire.dynamictrade.store.Unit;
import org.nightlabs.jfire.dynamictrade.store.id.UnitID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryConst;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.util.Util;

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
	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
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
	 * @throws CannotPublishProductTypeException 
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise() throws CannotPublishProductTypeException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			String organisationID = getOrganisationID();

			// initialise meta-data
			pm.getExtent(DynamicProductType.class);

			ProductTypeID rootID = ProductTypeID.create(organisationID, DynamicProductType.class.getName());
			try {
				pm.getObjectById(rootID);
				return; // already existing
			} catch (JDOObjectNotFoundException x) {
				// ignore and create it below
			}

			// create the ProductTypeActionHandler for DynamicProductTypes
			DynamicProductTypeActionHandler dynamicProductTypeActionHandler = new DynamicProductTypeActionHandler(
					Organisation.DEVIL_ORGANISATION_ID, DynamicProductTypeActionHandler.class.getName(), DynamicProductType.class);
			dynamicProductTypeActionHandler = pm.makePersistent(dynamicProductTypeActionHandler);

			// create a default DeliveryConfiguration with one ModeOfDelivery
			DeliveryConfiguration deliveryConfiguration = new DeliveryConfiguration(organisationID, "JFireDynamicTrade.default");
			deliveryConfiguration.getName().setText(Locale.ENGLISH.getLanguage(), "Default Delivery Configuration for JFireDynamicTrade");
			deliveryConfiguration.getName().setText(Locale.GERMAN.getLanguage(), "Standard-Liefer-Konfiguration für JFireDynamicTrade");
			pm.getExtent(ModeOfDelivery.class);

			ModeOfDelivery modeOfDelivery;
			modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);
			deliveryConfiguration.addModeOfDelivery(modeOfDelivery);
			
			modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_DELIVER_TO_DELIVERY_QUEUE);
			deliveryConfiguration.addModeOfDelivery(modeOfDelivery);
			
			deliveryConfiguration = pm.makePersistent(deliveryConfiguration);


			// create the root-ProductType
			Store store = Store.getStore(pm);
			User user = User.getUser(pm, getPrincipal());
			DynamicProductType root = new DynamicProductType(
					rootID.organisationID, rootID.productTypeID,
					null,
					ProductType.INHERITANCE_NATURE_BRANCH,
					ProductType.PACKAGE_NATURE_OUTER);
			root.setOwner(store.getMandator());
			root.getName().setText(Locale.ENGLISH.getLanguage(), LocalOrganisation.getLocalOrganisation(pm).getOrganisation().getPerson().getDisplayName());
			root = (DynamicProductType) store.addProductType(user, root);
			root.setPackagePriceConfig(PackagePriceConfig.getPackagePriceConfig(pm));
			root.setDeliveryConfiguration(deliveryConfiguration);
			store.setProductTypeStatus_published(user, root);

			Unit unit = new Unit(organisationID, IDGenerator.nextID(Unit.class));
			unit.getSymbol().setText(Locale.ENGLISH.getLanguage(), "h");
			unit.getName().setText(Locale.ENGLISH.getLanguage(), "hour");
			unit.getName().setText(Locale.GERMAN.getLanguage(), "Stunde");
			pm.makePersistent(unit);

			unit = new Unit(organisationID, IDGenerator.nextID(Unit.class));
			unit.getSymbol().setText(Locale.ENGLISH.getLanguage(), "pcs.");
			unit.getName().setText(Locale.ENGLISH.getLanguage(), "pieces");
			unit.getSymbol().setText(Locale.GERMAN.getLanguage(), "Stk.");
			unit.getName().setText(Locale.GERMAN.getLanguage(), "Stück");
			pm.makePersistent(unit);

			unit = new Unit(organisationID, IDGenerator.nextID(Unit.class));
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
				dynamicProductType = pm.makePersistent(dynamicProductType);
			} else {
				dynamicProductType = (DynamicProductType) Store.getStore(pm).addProductType(User.getUser(pm, getPrincipal()),
						dynamicProductType);
//						DynamicProductTypeActionHandler.getDefaultHome(pm, dynamicProductType));
			}

			// take care about the inheritance
			dynamicProductType.applyInheritance();

			if (!get)
				return null;

			return pm.detachCopy(dynamicProductType);
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
	public Collection<DynamicTradePriceConfig> storeDynamicTradePriceConfigs(Collection<DynamicTradePriceConfig> priceConfigs, boolean get, AssignInnerPriceConfigCommand assignInnerPriceConfigCommand)
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

			if (assignInnerPriceConfigCommand != null) {
				ProductType pt = (ProductType) pm.getObjectById(assignInnerPriceConfigCommand.getProductTypeID());
				IInnerPriceConfig pc = assignInnerPriceConfigCommand.getInnerPriceConfigID() == null ? null : (IInnerPriceConfig) pm.getObjectById(assignInnerPriceConfigCommand.getInnerPriceConfigID());
				boolean applyInheritance = false;
				if (pt.getFieldMetaData("innerPriceConfig").isValueInherited() != assignInnerPriceConfigCommand.isInnerPriceConfigInherited()) {
					pt.getFieldMetaData("innerPriceConfig").setValueInherited(assignInnerPriceConfigCommand.isInnerPriceConfigInherited());
					applyInheritance = true;
				}
				if (!Util.equals(pc, pt.getInnerPriceConfig())) {
					pt.setInnerPriceConfig(pc);
					applyInheritance = true;
				}
				if (applyInheritance)
					pt.applyInheritance();
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Article createArticle(
			SegmentID segmentID,
			OfferID offerID,
			ProductTypeID productTypeID,
			double quantity,
			UnitID unitID,
			TariffID tariffID,
			I18nText productName,
			Price singlePrice,
			boolean allocate,
			boolean allocateSynchronously,
			String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		if (segmentID == null)     throw new IllegalArgumentException("segmentID must not be null!");
		// offerID can be null
		if (productTypeID == null) throw new IllegalArgumentException("productTypeID must not be null!");
		// quantity can be everything
		if (unitID == null) throw new IllegalArgumentException("unitID must not be null!");
		if (tariffID == null)      throw new IllegalArgumentException("tariffID must not be null!");
		if (productName == null)   throw new IllegalArgumentException("productName must not be null!");
		if (singlePrice == null)   throw new IllegalArgumentException("singlePrice must not be null!");

		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			Store store = Store.getStore(pm);
			Segment segment = (Segment) pm.getObjectById(segmentID);
			Order order = segment.getOrder();

			User user = User.getUser(pm, getPrincipal());

			pm.getExtent(Unit.class);
			Unit unit = (Unit) pm.getObjectById(unitID);

			pm.getExtent(DynamicProductType.class);
			ProductType pt = (ProductType) pm.getObjectById(productTypeID);
			if (!(pt instanceof DynamicProductType))
				throw new IllegalArgumentException("productTypeID \""+productTypeID+"\" specifies a ProductType of type \""+pt.getClass().getName()+"\", but must be \""+DynamicProductType.class.getName()+"\"!");

			DynamicProductType productType = (DynamicProductType)pt;

			Tariff tariff = (Tariff) pm.getObjectById(tariffID);

			// find an Offer within the Order which is not finalized - or create one
			Offer offer;
			if (offerID == null) {
				Collection offers = Offer.getNonFinalizedOffers(pm, order);
				if (!offers.isEmpty()) {
					offer = (Offer) offers.iterator().next();
				}
				else {
					offer = trader.createOffer(user, order, null); // TODO offerIDPrefix ???
				}
			}
			else {
				pm.getExtent(Offer.class);
				offer = (Offer) pm.getObjectById(offerID);
			}

			// find / create Products
			Collection products = store.findProducts(user, productType, null, null); // we create exactly one => no NestedProductType needed
			if (products.size() != 1)
				throw new IllegalStateException("store.findProducts(...) created " + products.size() + " instead of exactly 1 product!");

			DynamicProduct product = (DynamicProduct) products.iterator().next();
			product.setSinglePrice(singlePrice);
			product.getName().copyFrom(productName);
			product.setQuantity(quantity);
			product.setUnit(unit);

			Collection articles = trader.createArticles(
					user, offer, segment,
					products,
					new ArticleCreator(tariff),
					allocate, allocateSynchronously, true);

			if (articles.size() != 1)
				throw new IllegalStateException("trader.createArticles(...) created " + articles.size() + " instead of exactly 1 article!");

			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Article) pm.detachCopy(articles.iterator().next());
		} finally {
			pm.close();
		}
	}

	/**
	 * @param articleID Specifies the {@link Article} that should be changed. Must not be <code>null</code>.
	 * @param quantity If <code>null</code>, no change will happen to this property - otherwise it will be updated (causes recalculation of the offer's price).
	 * @param unitID If <code>null</code>, no change will happen to this property - otherwise it will be updated.
	 * @param productName If <code>null</code>, no change will happen to this property - otherwise it will be updated.
	 * @param singlePrice If <code>null</code>, no change will happen to this property - otherwise it will be updated (causes recalculation of the offer's price).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Article modifyArticle(
			ArticleID articleID,
			Double quantity,
			UnitID unitID,
			TariffID tariffID,
			I18nText productName,
			Price singlePrice,
			boolean get,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Article article = (Article) pm.getObjectById(articleID);
			Offer offer = article.getOffer();
			if (offer.isFinalized())
				throw new IllegalStateException("Offer is already finalized! Cannot modify!");

			DynamicProduct product = (DynamicProduct) article.getProduct();

			boolean recalculatePrice = false;

			if (quantity != null) {
				product.setQuantity(quantity.doubleValue());
				recalculatePrice = true;
			}

			if (unitID != null) {
				Unit unit = (Unit) pm.getObjectById(unitID);
				product.setUnit(unit);
			}

			if (tariffID != null) {
				Tariff tariff = (Tariff) pm.getObjectById(tariffID);
				article.setTariff(tariff);
			}

			if (productName != null)
				product.getName().copyFrom(productName);

			if (singlePrice != null) {
				product.getSinglePrice().setAmount(0);
				product.getSinglePrice().clearFragments();
				pm.flush();
				product.getSinglePrice().sumPrice(singlePrice);
				recalculatePrice = true;
				pm.flush();
			}

			if (recalculatePrice) {
				int tryCounter = 0;
				ArticlePrice price = article.getProductType().getPackagePriceConfig().createArticlePrice(article);
				while (++tryCounter < 20) { // TODO remove this workaround!
					try {
						price = pm.makePersistent(price);
						pm.flush();
						break;
					} catch (Exception x) {
						// ignore
					}
				}
				article.setPrice(price);
				Trader.getTrader(pm).validateOffer(offer, true);
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(article);
		} finally {
			pm.close();
		}
	}
}
