package org.nightlabs.jfire.dynamictrade;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.PackagePriceConfig;
import org.nightlabs.jfire.dynamictrade.prop.DynamicProductTypeStruct;
import org.nightlabs.jfire.dynamictrade.recurring.DynamicProductTypeRecurringTradeActionHandler;
import org.nightlabs.jfire.dynamictrade.store.DynamicProduct;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductTypeActionHandler;
import org.nightlabs.jfire.dynamictrade.template.DynamicProductTemplate;
import org.nightlabs.jfire.dynamictrade.template.id.DynamicProductTemplateID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.RoleConstants;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.Unit;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryConst;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.Offer;
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
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class DynamicTradeManagerBean
extends BaseSessionBeanImpl
implements DynamicTradeManagerRemote
{
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.DynamicTradeManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			String organisationID = getOrganisationID();

			DynamicProductTypeStruct.getSimpleProductTypeStructLocal(pm);
			
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireDynamicTradeEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			moduleMetaData = pm.makePersistent(
					ModuleMetaData.createModuleMetaDataFromManifest(JFireDynamicTradeEAR.MODULE_NAME, JFireDynamicTradeEAR.class)
			);

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
					Organisation.DEV_ORGANISATION_ID, DynamicProductTypeActionHandler.class.getName(), DynamicProductType.class);
			dynamicProductTypeActionHandler = pm.makePersistent(dynamicProductTypeActionHandler);

			// Register the RecurringTradeProductTypeActionHandler for SimpleProductTypes
			DynamicProductTypeRecurringTradeActionHandler dptrtah = new DynamicProductTypeRecurringTradeActionHandler(
					Organisation.DEV_ORGANISATION_ID, DynamicProductTypeRecurringTradeActionHandler.class.getName(), DynamicProductType.class);
			dptrtah = pm.makePersistent(dptrtah);

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

			createTestData(pm);
		} finally {
			pm.close();
		}
	}

	private static void createTestData(PersistenceManager pm) throws Exception
	{
		DynamicProductTemplate cat_a = pm.makePersistent(new DynamicProductTemplate(null, true));
		cat_a.getName().setText(Locale.ENGLISH.getLanguage(), "Cat A");

		DynamicProductTemplate cat_a_a = pm.makePersistent(new DynamicProductTemplate(cat_a, true));
		cat_a_a.getName().setText(Locale.ENGLISH.getLanguage(), "Cat A/A");

		DynamicProductTemplate cat_a_a_a = pm.makePersistent(new DynamicProductTemplate(cat_a_a, false));
		cat_a_a_a.getName().setText(Locale.ENGLISH.getLanguage(), "Template A/A/A");

		DynamicProductTemplate cat_a_a_b = pm.makePersistent(new DynamicProductTemplate(cat_a_a, false));
		cat_a_a_b.getName().setText(Locale.ENGLISH.getLanguage(), "Template A/A/B");

		DynamicProductTemplate cat_a_b = pm.makePersistent(new DynamicProductTemplate(cat_a, true));
		cat_a_b.getName().setText(Locale.ENGLISH.getLanguage(), "Cat A/B");

		DynamicProductTemplate cat_a_b_a = pm.makePersistent(new DynamicProductTemplate(cat_a_b, false));
		cat_a_b_a.getName().setText(Locale.ENGLISH.getLanguage(), "Template A/B/A");

		DynamicProductTemplate cat_b = pm.makePersistent(new DynamicProductTemplate(null, true));
		cat_b.getName().setText(Locale.ENGLISH.getLanguage(), "Cat B");
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Set<ProductTypeID> getChildDynamicProductTypeIDs(ProductTypeID parentDynamicProductTypeID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Collection<DynamicProductType> productTypes = DynamicProductType.getChildProductTypes(pm, parentDynamicProductTypeID);

			productTypes = Authority.filterIndirectlySecuredObjects(
					pm,
					productTypes,
					getPrincipal(),
					RoleConstants.seeProductType,
					ResolveSecuringAuthorityStrategy.allow);

			return NLJDOHelper.getObjectIDSet(productTypes);
		} finally {
			pm.close();
		}
	}

//	/**
//	* @ejb.interface-method
//	* @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	* @ejb.permission role-name="_Guest_"
//	*/
//	public Set<ProductTypeID> getDynamicProductTypeIDs(Byte inheritanceNature, Boolean saleable) {
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	Query q = pm.newQuery(DynamicProductType.class);
//	q.setResult("JDOHelper.getObjectId(this)");
//	if (inheritanceNature != null || saleable != null) {
//	StringBuffer filter = new StringBuffer();

//	if (inheritanceNature != null)
//	filter.append("inheritanceNature == :inheritanceNature");

//	if (saleable != null) {
//	if (filter.length() != 0)
//	filter.append(" && ");

//	filter.append("saleable == :saleable");
//	}

//	q.setFilter(filter.toString());
//	}

//	HashMap<String, Object> params = new HashMap<String, Object>(2);
//	params.put("inheritanceNature", inheritanceNature);
//	params.put("saleable", saleable);

//	return new HashSet<ProductTypeID>((Collection<? extends ProductTypeID>) q.executeWithMap(params));
//	} finally {
//	pm.close();
//	}
//	}

//	/**
//	* @ejb.interface-method
//	* @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	* @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
//	*/
//	public List<DynamicProductType> getDynamicProductTypes(Collection<ProductTypeID> dynamicProductTypeIDs, String[] fetchGroups, int maxFetchDepth) {
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	return NLJDOHelper.getDetachedObjectList(pm, dynamicProductTypeIDs, DynamicProductType.class, fetchGroups, maxFetchDepth);
//	} finally {
//	pm.close();
//	}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.DynamicTradeManagerRemote#storeDynamicProductType(org.nightlabs.jfire.dynamictrade.store.DynamicProductType, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	@Override
	public DynamicProductType storeDynamicProductType(DynamicProductType dynamicProductType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		if (dynamicProductType == null)
			throw new IllegalArgumentException("dynamicProductType must not be null!");

		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups == null)
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			else
				pm.getFetchPlan().setGroups(fetchGroups);

//			try {
//			DynamicProductLocalAccountantDelegate delegate = (DynamicProductLocalAccountantDelegate) dynamicProductType
//			.getLocalAccountantDelegate();
//			if (delegate != null) {
//			OrganisationLegalEntity organisationLegalEntity = null;

//			for (Account account : delegate.getAccounts().values()) {
//			try {
//			if (account.getOwner() == null) {
//			if (organisationLegalEntity == null)
//			organisationLegalEntity = OrganisationLegalEntity
//			.getOrganisationLegalEntity(pm, getOrganisationID(),
//			OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION,
//			true);

//			account.setOwner(organisationLegalEntity);
//			}
//			} catch (JDODetachedFieldAccessException x) {
//			// ignore
//			}
//			}
//			}
//			} catch (JDODetachedFieldAccessException x) {
//			// ignore
//			}

			// Check if this is a managed product type
			ProductTypeLocal.assertProductTypeNotManaged(pm, (ProductTypeID) JDOHelper.getObjectId(dynamicProductType));

			// we don't need any price calculation as we have dynamic prices only - no cached values

			if (NLJDOHelper.exists(pm, dynamicProductType)) {
				dynamicProductType = pm.makePersistent(dynamicProductType);
			} else {
				dynamicProductType = (DynamicProductType) Store.getStore(pm).addProductType(
						User.getUser(pm, getPrincipal()),
						dynamicProductType);

				// TODO DataNucleus WORKAROUND
//				1141698 ERROR ({no user}) [LogInterceptor] RuntimeException in method: public abstract org.nightlabs.jfire.dynamictrade.store.DynamicProductType org.nightlabs.jfire.dynamictrade.DynamicTradeManager.storeDynamicProductType(org.nightlabs.jfire.dynamictrade.store.DynamicProductType,boolean,java.lang.String[],int) throws java.rmi.RemoteException:
//				java.lang.IllegalStateException: There is no PersistenceManager assigned to this object (it is currently not persistent): org.nightlabs.jfire.dynamictrade.store.DynamicProductType@11feafe[chezfrancois.jfire.org,service]
//				at org.nightlabs.jdo.inheritance.JDOInheritanceManager.provideFields(JDOInheritanceManager.java:32)
//				at org.nightlabs.jdo.inheritance.JDOInheritanceManager.inheritAllFields(JDOInheritanceManager.java:22)
//				at org.nightlabs.jfire.store.ProductType.applyInheritance(ProductType.java:1107)
//				at org.nightlabs.jfire.dynamictrade.DynamicTradeManagerBean.storeDynamicProductType(DynamicTradeManagerBean.java:295)
//				at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//				at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//				at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//				at java.lang.reflect.Method.invoke(Method.java:597)
//				at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//				at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//				at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//				at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//				at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//				at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//				at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//				at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//				at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//				at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//				at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:138)
//				at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//				at org.jboss.ejb.Container.invoke(Container.java:960)
//				at sun.reflect.GeneratedMethodAccessor126.invoke(Unknown Source)
//				at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//				at java.lang.reflect.Method.invoke(Method.java:597)
//				at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//				at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//				at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//				at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//				at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//				at org.jboss.invocation.unified.server.UnifiedInvoker.invoke(UnifiedInvoker.java:231)
//				at sun.reflect.GeneratedMethodAccessor220.invoke(Unknown Source)
//				at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//				at java.lang.reflect.Method.invoke(Method.java:597)
//				at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//				at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//				at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//				at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//				at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//				at javax.management.MBeanServerInvocationHandler.invoke(MBeanServerInvocationHandler.java:288)
//				at $Proxy16.invoke(Unknown Source)
//				at org.jboss.remoting.ServerInvoker.invoke(ServerInvoker.java:769)
//				at org.jboss.remoting.transport.socket.ServerThread.processInvocation(ServerThread.java:573)
//				at org.jboss.remoting.transport.socket.ServerThread.dorun(ServerThread.java:387)
//				at org.jboss.remoting.transport.socket.ServerThread.run(ServerThread.java:166)

				if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) {
					ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(dynamicProductType);
					if (productTypeID == null)
						throw new IllegalStateException("JDOHelper.getObjectId(dynamicProductType) returned null!");

					pm.flush();
					pm.evictAll();
					dynamicProductType = (DynamicProductType) pm.getObjectById(productTypeID);
				}
			}

			if (dynamicProductType.isConfirmed()) {
				Authority.resolveSecuringAuthority(
						pm,
						dynamicProductType.getProductTypeLocal(),
						ResolveSecuringAuthorityStrategy.organisation
				).assertContainsRoleRef(
						getPrincipal(),
						RoleConstants.editConfirmedProductType
				);
			}
			else {
				Authority.resolveSecuringAuthority(
						pm,
						dynamicProductType.getProductTypeLocal(),
						ResolveSecuringAuthorityStrategy.allow // already checked by the JavaEE server
				).assertContainsRoleRef(
						getPrincipal(),
						RoleConstants.editUnconfirmedProductType
				);
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.DynamicTradeManagerRemote#storeDynamicTradePriceConfigs(java.util.Collection, boolean, org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editPriceConfiguration")
	@Override
	public Collection<DynamicTradePriceConfig> storeDynamicTradePriceConfigs(Collection<DynamicTradePriceConfig> priceConfigs, boolean get, AssignInnerPriceConfigCommand assignInnerPriceConfigCommand)
	throws PriceCalculationException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				pm.getFetchPlan().setGroups(new String[] {
						FetchPlan.DEFAULT,
						FetchGroupsPriceConfig.FETCH_GROUP_EDIT});
			}

			for (DynamicTradePriceConfig dynamicTradePriceConfig : priceConfigs) {
				PriceConfigID priceConfigID = (PriceConfigID) JDOHelper.getObjectId(dynamicTradePriceConfig);
				if (priceConfigID != null)
					PriceConfig.assertPriceConfigNotManaged(pm, priceConfigID);
			}

			// Because we do not need to calculate any prices (all prices are dynamic), we
			// do not need to use GridPriceConfigUtil.storePriceConfigs(...), but simply
			// call pm.makePersistentAll(...).

			priceConfigs = pm.makePersistentAll(priceConfigs);

			if (assignInnerPriceConfigCommand != null) {
				ProductType pt = (ProductType) pm.getObjectById(assignInnerPriceConfigCommand.getProductTypeID());
				IInnerPriceConfig pc = assignInnerPriceConfigCommand.getInnerPriceConfigID() == null ? null : (IInnerPriceConfig) pm.getObjectById(assignInnerPriceConfigCommand.getInnerPriceConfigID());
				boolean applyInheritance = false;
				if (pt.getFieldMetaData(ProductType.FieldName.innerPriceConfig).isValueInherited() != assignInnerPriceConfigCommand.isInnerPriceConfigInherited()) {
					pt.getFieldMetaData(ProductType.FieldName.innerPriceConfig).setValueInherited(assignInnerPriceConfigCommand.isInnerPriceConfigInherited());
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
	 * Get the object-ids of all {@link DynamicTradePriceConfig}s.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	@SuppressWarnings("unchecked")
	@Override
	public Set<PriceConfigID> getDynamicTradePriceConfigIDs()
	{
		PersistenceManager pm = createPersistenceManager();
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
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	@Override
	public List<DynamicTradePriceConfig> getDynamicTradePriceConfigs(Collection<PriceConfigID> dynamicTradePriceConfigIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, dynamicTradePriceConfigIDs, DynamicTradePriceConfig.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * creates a new Dynamic Recurring Article
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	@Override
	public	Article createRecurringArticle(SegmentID segmentID,
			OfferID offerID,
			ProductTypeID productTypeID,
			long quantity,
			UnitID unitID,
			TariffID tariffID,
			I18nText productName,
			Price singlePrice,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException
			{

		PersistenceManager pm = createPersistenceManager();
		try {
			DynamicTrader dynamicTrader = DynamicTrader.getDynamicTrader(pm);
			Article article= dynamicTrader.createRecurringArticle(segmentID, offerID, productTypeID, quantity, unitID, tariffID, productName,singlePrice);

			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(article);

		} finally {
			pm.close();
		}
			}



	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	@Override
	public Article createArticle(
			SegmentID segmentID,
			OfferID offerID,
			ProductTypeID productTypeID,
			long quantity,
			UnitID unitID,
			TariffID tariffID,
			I18nText productName,
			Price singlePrice,
			boolean allocate,
			boolean allocateSynchronously,
			String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			DynamicTrader dynamicTrader = DynamicTrader.getDynamicTrader(pm);
			Article article= dynamicTrader.createArticle(segmentID, offerID, productTypeID, quantity, unitID, tariffID, productName, singlePrice, allocate, allocateSynchronously);

			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(article);


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
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	@Override
	public Article modifyArticle(
			ArticleID articleID,
			Long quantity,
			UnitID unitID,
			TariffID tariffID,
			I18nText productName,
			Price singlePrice,
			boolean get,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Article article = (Article) pm.getObjectById(articleID);
			Offer offer = article.getOffer();
			if (offer.isFinalized())
				throw new IllegalStateException("Offer is already finalized! Cannot modify!");

			Authority.resolveSecuringAuthority(
					pm,
					article.getProductType().getProductTypeLocal(),
					ResolveSecuringAuthorityStrategy.organisation // must be "organisation", because the role "sellProductType" is not checked on EJB method level!
			).assertContainsRoleRef(
					getPrincipal(),
					org.nightlabs.jfire.trade.RoleConstants.sellProductType
			);



			DynamicProduct product = (DynamicProduct) article.getProduct();
			DynamicProductInfo ProductInfo;

			// check if the Product is null and that happens in the case of Recurring Articl e
			if (product != null)
				ProductInfo = product;
			else
				ProductInfo = (DynamicProductInfo) article;

			boolean recalculatePrice = false;

			if (quantity != null) {
				ProductInfo.setQuantity(quantity.longValue());
				recalculatePrice = true;
			}

			if (unitID != null) {
				Unit unit = (Unit) pm.getObjectById(unitID);
				ProductInfo.setUnit(unit);
			}

			if (tariffID != null) {
				Tariff tariff = (Tariff) pm.getObjectById(tariffID);
				article.setTariff(tariff);
			}

			if (productName != null)
				ProductInfo.getName().copyFrom(productName);

			if (singlePrice != null) {
				ProductInfo.getSinglePrice().setAmount(0);
				ProductInfo.getSinglePrice().clearFragments();

				if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
					pm.flush();

				ProductInfo.getSinglePrice().sumPrice(singlePrice);
				recalculatePrice = true;

				if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
					pm.flush();
			}

			if (recalculatePrice) {
				int tryCounter = 0;
				ArticlePrice price = article.getProductType().getPackagePriceConfig().createArticlePrice(article);
				while (++tryCounter < 20) { // TODO remove this workaround!
					try {
						price = pm.makePersistent(price);

						if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
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


	@RolesAllowed("_Guest_")
	@Override
	public DynamicProductTemplate storeDynamicProductTemplate(
			DynamicProductTemplate dynamicProductTemplate,
			boolean get,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, dynamicProductTemplate, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<DynamicProductTemplateID> getChildDynamicProductTemplateIDs(DynamicProductTemplateID parentCategoryID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			DynamicProductTemplate parentCategory = parentCategoryID == null ? null : (DynamicProductTemplate) pm.getObjectById(parentCategoryID);
			return NLJDOHelper.getObjectIDList(DynamicProductTemplate.getChildDynamicProductTemplates(pm, parentCategory));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<DynamicProductTemplate> getDynamicProductTemplates(Collection<DynamicProductTemplateID> dynamicProductTemplateIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, dynamicProductTemplateIDs, DynamicProductTemplate.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
