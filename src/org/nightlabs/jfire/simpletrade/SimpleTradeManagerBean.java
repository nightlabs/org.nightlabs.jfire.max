/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.simpletrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.TariffMapper;
import org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfigUtil;
import org.nightlabs.jfire.accounting.gridpriceconfig.IResultPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculatorFactory;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCell;
import org.nightlabs.jfire.accounting.gridpriceconfig.StablePriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.TariffPricePair;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.AffectedProductType;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.accounting.tariffuserset.TariffUserSet;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.JFireException;
import org.nightlabs.jfire.entityuserset.AuthorizedObjectRef;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote;
import org.nightlabs.jfire.jdo.notification.persistent.SubscriptionUtil;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.simpletrade.notification.SimpleProductTypeNotificationFilter;
import org.nightlabs.jfire.simpletrade.notification.SimpleProductTypeNotificationReceiver;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.simpletrade.store.SimpleProductTypeActionHandler;
import org.nightlabs.jfire.simpletrade.store.prop.SimpleProductTypeStruct;
import org.nightlabs.jfire.simpletrade.store.recurring.SimpleProductTypeRecurringTradeActionHandler;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.NotAvailableException;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.RoleConstants;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.CrossTradeDeliveryCoordinator;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryConst;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.notification.ProductTypePermissionFlagSetNotificationReceiver;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.CustomerGroupMapper;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.trade.recurring.RecurringOrder;
import org.nightlabs.jfire.trade.recurring.RecurringTrader;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * @ejb.bean name="jfire/ejb/JFireSimpleTrade/SimpleTradeManager"
 *					 jndi-name="jfire/ejb/JFireSimpleTrade/SimpleTradeManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class SimpleTradeManagerBean
extends BaseSessionBeanImpl
implements SimpleTradeManagerRemote
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(SimpleTradeManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.simpletrade.SimpleTradeManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise()
	throws Exception
	{
		PersistenceManager pm = this.createPersistenceManager();
		try {
			String organisationID = getOrganisationID();

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireSimpleTradeEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of JFireSimpleTrade started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = pm.makePersistent(
					ModuleMetaData.createModuleMetaDataFromManifest(JFireSimpleTradeEAR.MODULE_NAME, JFireSimpleTradeEAR.class)
			);

			SimpleProductTypeStruct.getSimpleProductTypeStructLocal(pm);

			SimpleProductTypeActionHandler simpleProductTypeActionHandler = new SimpleProductTypeActionHandler(
					Organisation.DEV_ORGANISATION_ID, SimpleProductTypeActionHandler.class.getName(), SimpleProductType.class);
			simpleProductTypeActionHandler = pm.makePersistent(simpleProductTypeActionHandler);

			// Register the RecurringTradeProductTypeActionHandler for SimpleProductTypes
			SimpleProductTypeRecurringTradeActionHandler srtptah = new SimpleProductTypeRecurringTradeActionHandler(
					Organisation.DEV_ORGANISATION_ID, SimpleProductTypeRecurringTradeActionHandler.class.getName(), SimpleProductType.class);
			srtptah = pm.makePersistent(srtptah);

			Store store = Store.getStore(pm);
//			Accounting accounting = Accounting.getAccounting(pm);

			// create a default DeliveryConfiguration with all default ModeOfDelivery s
			DeliveryConfiguration deliveryConfiguration = new DeliveryConfiguration(organisationID, "JFireSimpleTrade.default");
			deliveryConfiguration.getName().setText(Locale.ENGLISH.getLanguage(), "Default Delivery Configuration for JFireSimpleTrade");
			deliveryConfiguration.getName().setText(Locale.GERMAN.getLanguage(), "Standard-Liefer-Konfiguration für JFireSimpleTrade");
			deliveryConfiguration.setCrossTradeDeliveryCoordinator(CrossTradeDeliveryCoordinator.getDefaultCrossTradeDeliveryCoordinator(pm));
			pm.getExtent(ModeOfDelivery.class);

			try {
				ModeOfDelivery modeOfDelivery;

				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);
				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

//				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MAILING_VIRTUAL);
//				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MAILING_PHYSICAL);
				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_DELIVER_TO_DELIVERY_QUEUE);
				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

				deliveryConfiguration = pm.makePersistent(deliveryConfiguration);
			} catch (JDOObjectNotFoundException x) {
				logger.warn("Could not populate default DeliveryConfiguration for JFireSimpleTrade with ModeOfDelivery s!", x);
			}

			User user = User.getUser(pm, getPrincipal());
			SimpleProductType rootSimpleProductType = new SimpleProductType(
					organisationID, SimpleProductType.class.getName(),
					null,
					ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
			rootSimpleProductType.setOwner(store.getMandator());
			rootSimpleProductType.getName().setText(Locale.ENGLISH.getLanguage(), LocalOrganisation.getLocalOrganisation(pm).getOrganisation().getPerson().getDisplayName());
			rootSimpleProductType.setDeliveryConfiguration(deliveryConfiguration);
			store.addProductType(user, rootSimpleProductType); // , SimpleProductTypeActionHandler.getDefaultHome(pm, rootSimpleProductType));
			store.setProductTypeStatus_published(user, rootSimpleProductType);

			// give the root product type a property set

			logger.info("Initialization of JFireSimpleTrade complete!");
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Set<ProductTypeID> getChildSimpleProductTypeIDs(ProductTypeID parentSimpleProductTypeID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Collection<SimpleProductType> productTypes = SimpleProductType.getChildProductTypes(pm, parentSimpleProductTypeID);

			productTypes = Authority.filterIndirectlySecuredObjects(
					pm,
					productTypes,
					getPrincipal(),
					RoleConstants.seeProductType,
					ResolveSecuringAuthorityStrategy.allow
			);

			return NLJDOHelper.getObjectIDSet(productTypes);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @ejb.interface-method
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public SimpleProductType getSimpleProductType(
//			ProductTypeID simpleProductTypeID, String[] fetchGroups,
//			int maxFetchDepth) {
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			List<SimpleProductType> spts = getSimpleProductTypes(CollectionUtil.array2HashSet(new ProductTypeID[] {simpleProductTypeID}), fetchGroups, maxFetchDepth);
//			if (spts.size() > 0)
//				return spts.get(0);
//			return null;
//		} finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * @ejb.interface-method
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public List<SimpleProductType> getSimpleProductTypes(
//			Collection<ProductTypeID> simpleProductTypeIDs, String[] fetchGroups,
//			int maxFetchDepth) {
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			return NLJDOHelper.getDetachedObjectList(pm, simpleProductTypeIDs,
//					SimpleProductType.class, fetchGroups, maxFetchDepth);
//		} finally {
//			pm.close();
//		}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.simpletrade.SimpleTradeManagerRemote#storeProductType(org.nightlabs.jfire.simpletrade.store.SimpleProductType, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	@Override
	public SimpleProductType storeProductType(SimpleProductType productType, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws PriceCalculationException
	{
		if (productType == null)
			throw new IllegalArgumentException("productType must not be null!");

		// In case this method is called multiple times (e.g. retry on dead lock), it's essential that we get a fresh copy every time.
		// TODO maybe put this into an interceptor!. Marco.
		// It is a DataNucleus Bug that the object is manipulated during pm.makePersistent(...), because its COPY should be changed - not the original.
		// Therefore, we will NOT put this into an interceptor, but instead create a test case and raise a DN JIRA issue.
		// Additionally, the contract will be that all EJB methods which modify the arguments must copy them themselves just like we
		// temporarily do below. Marco.
		productType = Util.cloneSerializable(productType);

		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups == null)
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			else
				pm.getFetchPlan().setGroups(fetchGroups);

			// Check if this is a managed product type
			ProductTypeLocal.assertProductTypeNotManaged(pm, (ProductTypeID) JDOHelper.getObjectId(productType));

			boolean priceCalculationNeeded = false;
			if (NLJDOHelper.exists(pm, productType)) {
				// if the nestedProductTypes changed, we need to recalculate prices
				// test first, whether they were detached
				Map<String, NestedProductTypeLocal> newNestedProductTypes = new HashMap<String, NestedProductTypeLocal>();
				try {
					for (NestedProductTypeLocal npt : productType.getProductTypeLocal().getNestedProductTypeLocals()) {
						newNestedProductTypes.put(npt.getInnerProductTypePrimaryKey(), npt);
						npt.getQuantity();
					}
				} catch (JDODetachedFieldAccessException x) {
					newNestedProductTypes = null;
				}

				if (newNestedProductTypes != null) {
					SimpleProductType original = (SimpleProductType) pm.getObjectById(JDOHelper.getObjectId(productType));

					priceCalculationNeeded = !ProductTypeLocal.compareNestedProductTypeLocals(original.getProductTypeLocal().getNestedProductTypeLocals(), newNestedProductTypes);
				}

				productType = pm.makePersistent(productType);
			}
			else {
				productType = (SimpleProductType) Store.getStore(pm).addProductType(
						User.getUser(pm, getPrincipal()),
						productType);

				// make sure the prices are correct
				priceCalculationNeeded = true;
			}

			// TODO JPOX WORKAROUND: In cross-organisation trade and some other situations, we get JDODetachedFieldAccessExceptions, even though the object should be persistent - trying a workaround
			if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) {
				ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
				pm.flush();
				pm.evictAll();
				productType = (SimpleProductType) pm.getObjectById(productTypeID);
			}

//			03:09:47,950 ERROR [LogInterceptor] RuntimeException in method: public abstract org.nightlabs.jfire.simpletrade.store.SimpleProductType org.nightlabs.jfire.simpletrade.SimpleTradeManager.storeProductType(org.nightlabs.jfire.simpletrade.store.SimpleProductType,boolean,java.lang.String[],int) throws org.nightlabs.ModuleException,java.rmi.RemoteException:
//			javax.jdo.JDODetachedFieldAccessException: You have just attempted to access field "extendedProductType" yet this field was not detached when you detached the object. Either dont access this field, or detach the field when detaching the object.
//			at org.nightlabs.jfire.store.ProductType.jdoGetextendedProductType(ProductType.java)
//			at org.nightlabs.jfire.store.ProductType.getExtendedProductType(ProductType.java:680)
//			at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator._resolvableProductTypes_registerWithAnchestors(PriceCalculator.java:300)
//			at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator.preparePriceCalculation_createResolvableProductTypesMap(PriceCalculator.java:282)
//			at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator.preparePriceCalculation(PriceCalculator.java:159)
//			at org.nightlabs.jfire.simpletrade.SimpleTradeManagerBean.storeProductType(SimpleTradeManagerBean.java:611)
//			at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//			at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//			at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			at java.lang.reflect.Method.invoke(Method.java:585)
//			at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//			at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//			at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//			at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//			at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//			at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//			at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//			at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//			at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//			at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//			at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:138)
//			at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//			at org.jboss.ejb.Container.invoke(Container.java:960)
//			at sun.reflect.GeneratedMethodAccessor109.invoke(Unknown Source)
//			at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			at java.lang.reflect.Method.invoke(Method.java:585)
//			at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//			at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//			at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//			at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//			at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//			at org.jboss.invocation.unified.server.UnifiedInvoker.invoke(UnifiedInvoker.java:231)
//			at sun.reflect.GeneratedMethodAccessor133.invoke(Unknown Source)
//			at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			at java.lang.reflect.Method.invoke(Method.java:585)
//			at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//			at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//			at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//			at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//			at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//			at javax.management.MBeanServerInvocationHandler.invoke(MBeanServerInvocationHandler.java:201)
//			at $Proxy16.invoke(Unknown Source)
//			at org.jboss.remoting.ServerInvoker.invoke(ServerInvoker.java:734)
//			at org.jboss.remoting.transport.socket.ServerThread.processInvocation(ServerThread.java:560)
//			at org.jboss.remoting.transport.socket.ServerThread.dorun(ServerThread.java:383)
//			at org.jboss.remoting.transport.socket.ServerThread.run(ServerThread.java:165)


			if (priceCalculationNeeded) {
				logger.info("storeProductType: price-calculation is necessary! Will recalculate the prices of " + JDOHelper.getObjectId(productType));
				if (productType.getPackagePriceConfig() != null && productType.getInnerPriceConfig() != null) {
					((IResultPriceConfig)productType.getPackagePriceConfig()).adoptParameters(
							productType.getInnerPriceConfig());
				}

				// find out which productTypes package this one and recalculate their prices as well - recursively! siblings are automatically included in the package-recalculation
				HashSet<ProductTypeID> processedProductTypeIDs = new HashSet<ProductTypeID>();
				ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
				for (AffectedProductType apt : PriceConfigUtil.getAffectedProductTypes(pm, productType)) {
					if (!processedProductTypeIDs.add(apt.getProductTypeID()))
						continue;

					ProductType pt;
					if (apt.getProductTypeID().equals(productTypeID))
						pt = productType;
					else
						pt = (ProductType) pm.getObjectById(apt.getProductTypeID());

					if (ProductType.PACKAGE_NATURE_OUTER == pt.getPackageNature() && pt.getPackagePriceConfig() != null) {
						logger.info("storeProductType: price-calculation starting for: " + JDOHelper.getObjectId(pt));

						PriceCalculator priceCalculator = new PriceCalculator(pt, new CustomerGroupMapper(pm), new TariffMapper(pm));
						priceCalculator.preparePriceCalculation();
						priceCalculator.calculatePrices();

						logger.info("storeProductType: price-calculation complete for: " + JDOHelper.getObjectId(pt));
					}
				}
			}
			else
				logger.info("storeProductType: price-calculation is NOT necessary! Stored ProductType without recalculation: " + JDOHelper.getObjectId(productType));

			if (productType.isConfirmed()) {
				Authority.resolveSecuringAuthority(
						pm,
						productType.getProductTypeLocal(),
						ResolveSecuringAuthorityStrategy.organisation
				).assertContainsRoleRef(
						getPrincipal(),
						RoleConstants.editConfirmedProductType
				);
			}
			else {
				Authority.resolveSecuringAuthority(
						pm,
						productType.getProductTypeLocal(),
						ResolveSecuringAuthorityStrategy.allow // already checked by the JavaEE server
				).assertContainsRoleRef(
						getPrincipal(),
						RoleConstants.editUnconfirmedProductType
				);
			}


			// take care about the inheritance
			productType.applyInheritance();
			// imho, the recalculation of the prices for the inherited ProductTypes is already implemented in JFireTrade. Marco.

			if (!get)
				return null;

			return pm.detachCopy(productType);
		} finally {
			pm.close();
		}
	}

	/**
	 * @return Returns the {@link PropertySet}s for the given simpleProductTypeIDs trimmed so that they only contain the given structFieldIDs.
	 * @see PropertySet#detachPropertySetWithTrimmedFieldList(PersistenceManager, PropertySet, Set, String[], int)
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Map<ProductTypeID, PropertySet> getSimpleProductTypesPropertySets(
			Set<ProductTypeID> simpleProductTypeIDs,
			Set<StructFieldID> structFieldIDs,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		Map<ProductTypeID, PropertySet> result = new HashMap<ProductTypeID, PropertySet>(simpleProductTypeIDs.size());
		PersistenceManager pm = createPersistenceManager();
		try {
			for (ProductTypeID productTypeID : simpleProductTypeIDs) {
				SimpleProductType productType = (SimpleProductType) pm.getObjectById(productTypeID);

				boolean seeAllowed = Authority.resolveSecuringAuthority(
						pm, productType.getProductTypeLocal(), ResolveSecuringAuthorityStrategy.allow
				).containsRoleRef(getPrincipal(), RoleConstants.seeProductType);

				if (!seeAllowed)
					continue;

				PropertySet detached = PropertySet.detachPropertySetWithTrimmedFieldList(
						pm,
						productType.getPropertySet(), structFieldIDs,
						fetchGroups, maxFetchDepth
				);
				result.put(productTypeID, detached);
			}
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	@Override
	public Set<PriceConfigID> getFormulaPriceConfigIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(pm.getExtent(FormulaPriceConfig.class, false));
			q.setResult("JDOHelper.getObjectId(this)");

			@SuppressWarnings("unchecked")
			Collection<? extends PriceConfigID> c = (Collection<? extends PriceConfigID>) q.execute();

			return new HashSet<PriceConfigID>(c);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	@Override
	public List<FormulaPriceConfig> getFormulaPriceConfigs(Collection<PriceConfigID> formulaPriceConfigIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, formulaPriceConfigIDs, FormulaPriceConfig.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}



	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException in case there are not enough <tt>Product</tt>s available and the <tt>Product</tt>s cannot be created (because of a limit).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	@Override
	public Collection<? extends Article> createArticles(
			SegmentID segmentID,
			OfferID offerID,
			Collection<ProductTypeID> productTypeIDs,
			TariffID tariffID,
			String[] fetchGroups, int maxFetchDepth
	)
	throws NotAvailableException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			RecurringTrader recurringTrader = RecurringTrader.getRecurringTrader(pm);
			Segment segment = (Segment) pm.getObjectById(segmentID);

			User user = User.getUser(pm, getPrincipal());
			Tariff tariff = (Tariff) pm.getObjectById(tariffID);
			Order order = segment.getOrder();


			// find an Offer within the Order which is not finalized - or create one
			Offer offer;
			if (offerID == null) {
				Collection<Offer> offers = Offer.getNonFinalizedNonEndedOffers(pm, order);
				if (!offers.isEmpty()) {
					offer = offers.iterator().next();
				}
				else {

					if (order instanceof RecurringOrder)
						offer = recurringTrader.createRecurringOffer(user, (RecurringOrder) order, null); // TODO offerIDPrefix ???
					else
						offer = trader.createOffer(user, order, null); // TODO offerIDPrefix ???
				}
			}
			else {
				pm.getExtent(Offer.class);
				offer = (Offer) pm.getObjectById(offerID);
			}

			Collection<ProductType> productTypes = new LinkedList<ProductType>();
			for (ProductTypeID productTypeID : productTypeIDs) {
				ProductType productType = (ProductType) pm.getObjectById(productTypeID);

				Authority.resolveSecuringAuthority(
						pm,
						productType.getProductTypeLocal(),
						ResolveSecuringAuthorityStrategy.organisation // must be "organisation", because the role "sellProductType" is not checked on EJB method level!
				).assertContainsRoleRef(
						getPrincipal(),
						org.nightlabs.jfire.trade.RoleConstants.sellProductType
				);

				productTypes.add(productType);
			}

			Collection<? extends Article> articles = trader.createArticles(
					user, offer, segment, productTypes, new ArticleCreator(tariff));

			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) {
				List<ArticleID> articleIDs = NLJDOHelper.getObjectIDList(articles);
				for (int tryCounter = 0; tryCounter < 10; ++tryCounter) {
					pm.flush();
					pm.evictAll();
					articles = NLJDOHelper.getObjectList(pm, articleIDs, Article.class);
					try {
						Collection<? extends Article> detachedArticles = null;
						detachedArticles = pm.detachCopyAll(articles);
						return detachedArticles;
					} catch (Exception x) {
						logger.warn("Detaching Articles failed! Trying it again. tryCounter="+tryCounter, x);
					}
				}
			}

			return pm.detachCopyAll(articles);
		} finally {
			pm.close();
		}
	}


	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException in case there are not enough <tt>Product</tt>s available and the <tt>Product</tt>s cannot be created (because of a limit).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	@Override
	public Collection<? extends Article> createArticles(
			SegmentID segmentID,
			OfferID offerID,
			ProductTypeID productTypeID,
			int quantity,
			TariffID tariffID,
			boolean allocate,
			boolean allocateSynchronously,
			String[] fetchGroups, int maxFetchDepth
	)
	throws NotAvailableException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			Store store = Store.getStore(pm);
			Segment segment = (Segment) pm.getObjectById(segmentID);
			Order order = segment.getOrder();

			User user = User.getUser(pm, getPrincipal());

			pm.getExtent(SimpleProductType.class);
			ProductType pt = (ProductType) pm.getObjectById(productTypeID);
			if (!(pt instanceof SimpleProductType))
				throw new IllegalArgumentException("productTypeID \""+productTypeID+"\" specifies a ProductType of type \""+pt.getClass().getName()+"\", but must be \""+SimpleProductType.class.getName()+"\"!");

			SimpleProductType productType = (SimpleProductType)pt;

			Authority.resolveSecuringAuthority(
					pm,
					productType.getProductTypeLocal(),
					ResolveSecuringAuthorityStrategy.organisation // must be "organisation", because the role "sellProductType" is not checked on EJB method level!
			).assertContainsRoleRef(
					getPrincipal(),
					org.nightlabs.jfire.trade.RoleConstants.sellProductType
			);

			Tariff tariff = (Tariff) pm.getObjectById(tariffID);

			// find an Offer within the Order which is not finalized - or create one
			Offer offer;
			if (offerID == null) {
				Collection<Offer> offers = Offer.getNonFinalizedNonEndedOffers(pm, order);
				if (!offers.isEmpty()) {
					offer = offers.iterator().next();
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
			NestedProductTypeLocal pseudoNestedPT = null;
			if (quantity != 1)
				pseudoNestedPT = new NestedProductTypeLocal(null, productType.getProductTypeLocal(), quantity);

			Collection<? extends Product> products = store.findProducts(user, productType, pseudoNestedPT, null);

			Collection<? extends Article> articles = trader.createArticles(
					user, offer, segment,
					products,
					new ArticleCreator(tariff),
					allocate, allocateSynchronously);
//			Collection articles = new ArrayList();
//			for (Iterator it = products.iterator(); it.hasNext(); ) {
//			SimpleProduct product = (SimpleProduct) it.next();
//			Article article = trader.createArticle(
//			user, offer, segment, product,
//			new ArticleCreator(tariff),
//			true, false);
////		auto-release must be controlled via the offer (the whole offer has an expiry time
////		new Date(System.currentTimeMillis() + 3600 * 1000 * 10)); // TODO the autoReleaseTimeout must come from the config
//			articles.add(article);
//			}

			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			// TODO remove this JPOX WORKAROUND! getting sometimes:
//			Caused by: javax.jdo.JDOUserException: Cannot read fields from a deleted object
//			FailedObject:jdo/org.nightlabs.jfire.accounting.id.PriceFragmentID?organisationID=chezfrancois.jfire.org&priceConfigID=9&priceID=232&priceFragmentTypePK=dev.jfire.org%2F_Total_
//			at org.jpox.state.jdo.PersistentNewDeleted.transitionReadField(PersistentNewDeleted.java:105)
//			at org.jpox.state.StateManagerImpl.transitionReadField(StateManagerImpl.java:3394)
//			at org.jpox.state.StateManagerImpl.isLoaded(StateManagerImpl.java:1982)
//			at org.jpox.store.rdbms.scostore.FKMapStore.put(FKMapStore.java:764)
//			at org.jpox.store.rdbms.scostore.AbstractMapStore.putAll(AbstractMapStore.java:320)
//			at org.jpox.store.mapping.MapMapping.postUpdate(MapMapping.java:175)
//			at org.jpox.store.rdbms.request.UpdateRequest.execute(UpdateRequest.java:318)
//			at org.jpox.store.rdbms.table.ClassTable.update(ClassTable.java:2573)
//			at org.jpox.store.rdbms.table.ClassTable.update(ClassTable.java:2568)
//			at org.jpox.store.StoreManager.update(StoreManager.java:967)
//			at org.jpox.state.StateManagerImpl.flush(StateManagerImpl.java:4928)
//			at org.jpox.AbstractPersistenceManager.flush(AbstractPersistenceManager.java:3233)
//			at org.jpox.store.rdbms.RDBMSManagedTransaction.getConnection(RDBMSManagedTransaction.java:172)
//			at org.jpox.store.rdbms.AbstractRDBMSTransaction.getConnection(AbstractRDBMSTransaction.java:97)
//			at org.jpox.resource.JdoTransactionHandle.getConnection(JdoTransactionHandle.java:246)
//			at org.jpox.store.rdbms.RDBMSManager.getConnection(RDBMSManager.java:426)
//			at org.jpox.store.rdbms.scostore.AbstractSetStore.iterator(AbstractSetStore.java:123)
//			at org.jpox.store.rdbms.scostore.FKMapStore.clear(FKMapStore.java:991)
//			at org.jpox.sco.HashMap.clear(HashMap.java:717)
//			at org.jpox.sco.HashMap.setValueFrom(HashMap.java:232)
//			at org.jpox.sco.SCOUtils.newSCOInstance(SCOUtils.java:100)
//			at org.jpox.state.StateManagerImpl.newSCOInstance(StateManagerImpl.java:3339)
//			at org.jpox.state.StateManagerImpl.replaceSCOField(StateManagerImpl.java:3356)
//			at org.jpox.state.DetachFieldManager.internalFetchObjectField(DetachFieldManager.java:88)
//			at org.jpox.state.AbstractFetchFieldManager.fetchObjectField(AbstractFetchFieldManager.java:108)
//			at org.jpox.state.StateManagerImpl.replacingObjectField(StateManagerImpl.java:2951)
//			at org.nightlabs.jfire.accounting.Price.jdoReplaceField(Price.java)
//			at org.nightlabs.jfire.trade.ArticlePrice.jdoReplaceField(ArticlePrice.java)
//			at org.nightlabs.jfire.accounting.Price.jdoReplaceFields(Price.java)
//			at org.jpox.state.StateManagerImpl.replaceFields(StateManagerImpl.java:3170)
//			at org.jpox.state.StateManagerImpl.replaceFields(StateManagerImpl.java:3188)
//			at org.jpox.state.StateManagerImpl.detachCopy(StateManagerImpl.java:4193)
//			at org.jpox.AbstractPersistenceManager.internalDetachCopy(AbstractPersistenceManager.java:1944)
//			at org.jpox.AbstractPersistenceManager.detachCopyInternal(AbstractPersistenceManager.java:1974)
//			at org.jpox.resource.PersistenceManagerImpl.detachCopyInternal(PersistenceManagerImpl.java:961)
//			at org.jpox.state.DetachFieldManager.internalFetchObjectField(DetachFieldManager.java:144)
//			at org.jpox.state.AbstractFetchFieldManager.fetchObjectField(AbstractFetchFieldManager.java:108)
//			at org.jpox.state.StateManagerImpl.replacingObjectField(StateManagerImpl.java:2951)
//			at org.nightlabs.jfire.trade.Article.jdoReplaceField(Article.java)
//			at org.nightlabs.jfire.trade.Article.jdoReplaceFields(Article.java)
//			at org.jpox.state.StateManagerImpl.replaceFields(StateManagerImpl.java:3170)
//			at org.jpox.state.StateManagerImpl.replaceFields(StateManagerImpl.java:3188)
//			at org.jpox.state.StateManagerImpl.detachCopy(StateManagerImpl.java:4193)
//			at org.jpox.AbstractPersistenceManager.internalDetachCopy(AbstractPersistenceManager.java:1944)
//			at org.jpox.AbstractPersistenceManager.detachCopyInternal(AbstractPersistenceManager.java:1974)
//			at org.jpox.AbstractPersistenceManager.detachCopyAll(AbstractPersistenceManager.java:2043)
//			at org.jpox.resource.PersistenceManagerImpl.detachCopyAll(PersistenceManagerImpl.java:1000)
//			at org.nightlabs.jfire.simpletrade.SimpleTradeManagerBean.createArticles(SimpleTradeManagerBean.java:745)

			if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) {
				List<ArticleID> articleIDs = NLJDOHelper.getObjectIDList(articles);
				for (int tryCounter = 0; tryCounter < 10; ++tryCounter) {
					pm.flush();
					pm.evictAll();
					articles = NLJDOHelper.getObjectList(pm, articleIDs, Article.class);
					try {
						Collection<? extends Article> detachedArticles = null;
						detachedArticles = pm.detachCopyAll(articles);
						return detachedArticles;
					} catch (Exception x) {
						logger.warn("Detaching Articles failed! Trying it again. tryCounter="+tryCounter, x);
					}
				}
			}

			return pm.detachCopyAll(articles);
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<ProductTypeID> getPublishedSimpleProductTypeIDs()
	{
		if (!userIsOrganisation()) // noone else needs this method - at least at the moment.
			throw new IllegalStateException("For security reasons, this method can only be called by partner organisations! You are not an organisation!");

		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(SimpleProductType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			q.setFilter("this.published && this.organisationID == :localOrganisationID");
			Collection<ProductTypeID> res = CollectionUtil.castCollection((Collection<?>)q.execute(getOrganisationID()));
			return new HashSet<ProductTypeID>(res);
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<SimpleProductType> getSimpleProductTypesForReseller(Collection<ProductTypeID> productTypeIDs)
	{
		if (!userIsOrganisation()) // noone else needs this method - at least at the moment.
			throw new IllegalStateException("For security reasons, this method can only be called by partner organisations! You are not an organisation!");

	// TODO We need to think about how we proceed about non-visible ProductTypes. We MUST update objects
	// that have been published once (because the partners already know them). Maybe we don't filter "hard" for them, but only
	// transmit this visibility as a "soft" information and hide it in the UI (and probably not even in the trade ADMIN UI, but
	// only in normal sales UI, because in the admin UI, we probably have to see the nested product types, even when they're
	// not "visible" anymore. This definitely needs further thoughts. Marco.

		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(SimpleProductType.class);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT,
					ProductType.FETCH_GROUP_NAME,
					SimpleProductType.FETCH_GROUP_PROPERTY_SET,
					PropertySet.FETCH_GROUP_FULL_DATA, // TODO this applies to SimpleProductType.propertySet - maybe we should somehow control what parts of it shall be public?!
					FetchGroupsPriceConfig.FETCH_GROUP_EDIT,
					DeliveryConfiguration.FETCH_GROUP_THIS_DELIVERY_CONFIGURATION,
					OrganisationLegalEntity.FETCH_GROUP_ORGANISATION,
					LegalEntity.FETCH_GROUP_PERSON,
					PropertySet.FETCH_GROUP_FULL_DATA // TODO we should somehow filter this so only public data is exported
			});
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS);
			pm.evictAll(true, ProductType.class); // if we don't throw them out of the cache, they might have already fields resolved which I don't want to be loaded and detached.

			List<SimpleProductType> res = new ArrayList<SimpleProductType>(productTypeIDs.size());
			for (ProductTypeID productTypeID : productTypeIDs) {
				if (!getOrganisationID().equals(productTypeID.organisationID))
					throw new IllegalArgumentException("Cannot get foreign SimpleProductTypes! Argument is invalid: " + productTypeID);

				SimpleProductType simpleProductType = (SimpleProductType) pm.getObjectById(productTypeID);

				// we need to strip off the nested product types (they're out of business ;-)
				// and we need to replace the price config - actually it should be sufficient to simply omit the inner price config
				// as the package price config contains only stable prices

				// we simply touch every field we need (and don't have in the fetch-groups yet) - the others should not be loaded and thus not detached then.
//				simpleProductType.getName().getTexts(); // loaded via fetch-group
//				simpleProductType.getName().getProductType();
				simpleProductType.getPackagePriceConfig();
				simpleProductType.getOwner();
				simpleProductType.getVendor();
				simpleProductType.getExtendedProductType();
				simpleProductType.getDeliveryConfiguration();

				// and detach
				simpleProductType = pm.detachCopy(simpleProductType);

				// TODO load CustomerGroups of the other customer-organisation
				// and remove all prices from the package price config that are for
				// different customer groups (not available to the client)
				if (simpleProductType.getPackagePriceConfig() == null) {
					// nothing to do
				}
				else if (simpleProductType.getPackagePriceConfig() instanceof GridPriceConfig) {
					Set<CustomerGroupID> unavailableCustomerGroupIDs = new HashSet<CustomerGroupID>();
					GridPriceConfig gridPriceConfig = (GridPriceConfig) simpleProductType.getPackagePriceConfig();

					for (CustomerGroupID customerGroupID : unavailableCustomerGroupIDs)
						gridPriceConfig.removeCustomerGroup(customerGroupID.organisationID, customerGroupID.customerGroupID);
				}
				else
					throw new IllegalStateException("SimpleProductType.packagePriceConfig unsupported! " + productTypeID);

				res.add(simpleProductType);
			}
			return res;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.simpletrade.SimpleTradeManagerRemote#importSimpleProductTypesForReselling(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	@Override
	public void importSimpleProductTypesForReselling(String emitterOrganisationID)
	throws JFireException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			try {
				Hashtable<?, ?> initialContextProperties = getInitialContextProperties(emitterOrganisationID);

				PersistentNotificationEJBRemote persistentNotificationEJB = JFireEjb3Factory.getRemoteBean(PersistentNotificationEJBRemote.class, initialContextProperties);

				SimpleProductTypeNotificationFilter simpleProductTypeNotificationFilter = new SimpleProductTypeNotificationFilter(
						emitterOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, getOrganisationID(),
						SimpleProductTypeNotificationFilter.class.getName()
				);
				SimpleProductTypeNotificationReceiver simpleProductTypeNotificationReceiver = new SimpleProductTypeNotificationReceiver(simpleProductTypeNotificationFilter);
				simpleProductTypeNotificationReceiver = pm.makePersistent(simpleProductTypeNotificationReceiver);
				persistentNotificationEJB.storeNotificationFilter(simpleProductTypeNotificationFilter, false, null, 1);


//				ProductTypePermissionFlagSetNotificationFilter productTypePermissionFlagSetNotificationFilter = new ProductTypePermissionFlagSetNotificationFilter(
//						emitterOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, getOrganisationID(),
//						ProductTypePermissionFlagSetNotificationFilter.class.getName()
//				);
//				ProductTypePermissionFlagSetNotificationReceiver productTypePermissionFlagSetNotificationReceiver = new ProductTypePermissionFlagSetNotificationReceiver(productTypePermissionFlagSetNotificationFilter);
//				productTypePermissionFlagSetNotificationReceiver = pm.makePersistent(productTypePermissionFlagSetNotificationReceiver);
//				persistentNotificationEJB.storeNotificationFilter(productTypePermissionFlagSetNotificationFilter, false, null, 1);
				ProductTypePermissionFlagSetNotificationReceiver.register(pm, emitterOrganisationID);

//				ArrayList<ProductTypeID> productTypeIDs = new ArrayList<ProductTypeID>(1);
//				productTypeIDs.add(productTypeID);

				SimpleTradeManagerRemote simpleTradeManager = JFireEjb3Factory.getRemoteBean(SimpleTradeManagerRemote.class, initialContextProperties);

				Set<ProductTypeID> productTypeIDs = simpleTradeManager.getPublishedSimpleProductTypeIDs();
				// never used
				//Collection<SimpleProductType> productTypes = simpleTradeManager.getSimpleProductTypesForReseller(productTypeIDs);

				simpleProductTypeNotificationReceiver.replicateSimpleProductTypes(emitterOrganisationID, productTypeIDs, new HashSet<ProductTypeID>(0));

//				if (productTypes.size() != 1)
//				throw new IllegalStateException("productTypes.size() != 1");

//				// currently we only support subscribing root-producttypes
//				for (SimpleProductType productType : productTypes) {
//				if (productType.getExtendedProductType() != null)
//				throw new UnsupportedOperationException("The given SimpleProductType is not a root node (not yet supported!): " + productTypeID);
//				}

//				productTypes = pm.makePersistentAll(productTypes);
//				return productTypes.iterator().next();
			} catch (Exception x) {
				logger.error("Import of SimpleProductType failed!", x);
				throw new JFireException(x);
			}
		} finally {
			pm.close();
		}
	}

//	/**
//	* @ejb.interface-method
//	* @ejb.permission role-name="_Guest_"
//	* @ejb.transaction type="Required"
//	*/
//	public SimpleProductType backend_subscribe(ProductTypeID productTypeID, String[] fetchGroups, int maxFetchDepth)
//	{
//	PersistenceManager pm = getPersistenceManager();
//	try {

//	} finally {
//	pm.close();
//	}
//	}

	/**
	 * @return a <tt>Collection</tt> of {@link TariffPricePair}
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.sellProductType, org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 */
	@RolesAllowed({"org.nightlabs.jfire.trade.sellProductType", "org.nightlabs.jfire.accounting.queryPriceConfigurations"})
	@Override
	public Collection<TariffPricePair> getTariffPricePairs(
			ProductTypeID productTypeID, CustomerGroupID customerGroupID, CurrencyID currencyID,
			String[] tariffFetchGroups, String[] priceFetchGroups
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
//			StablePriceConfig priceConfig = (StablePriceConfig) pm.getObjectById(priceConfigID);

			ProductType productType = (ProductType) pm.getObjectById(productTypeID);
			StablePriceConfig priceConfig = (StablePriceConfig) productType.getPackagePriceConfig();
			if (priceConfig == null)
				return Collections.emptyList();

			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT, ProductType.FETCH_GROUP_TARIFF_USER_SET
			});
			ProductType detachedProductType = pm.detachCopy(productType);

			String organisationID = getOrganisationID();

			TariffUserSet tariffUserSet = productType.getTariffUserSet();
			AuthorizedObjectRef<Tariff> authorizedObjectRef = null;
			if (tariffUserSet != null) {
				authorizedObjectRef = tariffUserSet.getAuthorizedObjectRef(UserLocalID.create(organisationID, getUserID(), organisationID));
				if (authorizedObjectRef == null)
					authorizedObjectRef = tariffUserSet.getAuthorizedObjectRef(UserLocalID.create(organisationID, User.USER_ID_OTHER, organisationID));
			}

			Collection<PriceCell> priceCells = priceConfig.getPriceCells(
					CustomerGroup.getPrimaryKey(customerGroupID.organisationID, customerGroupID.customerGroupID),
					currencyID.currencyID);

			Collection<TariffPricePair> res = new ArrayList<TariffPricePair>();

			iteratePriceCells: for (Iterator<PriceCell> it = priceCells.iterator(); it.hasNext(); ) {
				PriceCell priceCell = it.next();
				String tariffPK = priceCell.getPriceCoordinate().getTariffPK();
				TariffID tariffID = TariffID.create(tariffPK);

				if (tariffFetchGroups != null)
					pm.getFetchPlan().setGroups(tariffFetchGroups); // set it already before pm.getObjectById(...) to allow for JDO's optimization

				Tariff tariff = (Tariff) pm.getObjectById(tariffID);

				if (tariffUserSet != null) {
					if (authorizedObjectRef == null)
						continue iteratePriceCells;

					if (authorizedObjectRef.getEntityRef(tariff) == null)
						continue iteratePriceCells;
				}

				if (tariffFetchGroups != null)
					pm.getFetchPlan().setGroups(tariffFetchGroups); // set it again, in case the fetch-plan was modified in the mean-time

				tariff = pm.detachCopy(tariff);

				if (priceFetchGroups != null)
					pm.getFetchPlan().setGroups(priceFetchGroups);

				Price price = pm.detachCopy(priceCell.getPrice());

				res.add(new TariffPricePair(detachedProductType, tariff, price));
			}

			return res;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.simpletrade.SimpleTradeManagerRemote#storePriceConfigs(java.util.Collection, boolean, org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editPriceConfiguration")
	@Override
	public Collection<GridPriceConfig> storePriceConfigs(Collection<GridPriceConfig> priceConfigs, boolean get, AssignInnerPriceConfigCommand assignInnerPriceConfigCommand)
	throws PriceCalculationException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			final CustomerGroupMapper cgm = new CustomerGroupMapper(pm);
			final TariffMapper tm = new TariffMapper(pm);

			PriceCalculatorFactory pcf = new PriceCalculatorFactory() {
				public PriceCalculator createPriceCalculator(ProductType productType)
				{
					return new PriceCalculator(productType, cgm, tm);
				}
			};

			return GridPriceConfigUtil.storePriceConfigs(pm, priceConfigs, pcf, get, assignInnerPriceConfigCommand);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.simpletrade.SimpleTradeManagerRemote#getCandidateOrganisationIDsForCrossTrade()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	@Override
	public Collection<OrganisationID> getCandidateOrganisationIDsForCrossTrade()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Set<OrganisationID> res = new HashSet<OrganisationID>();

			Query q = pm.newQuery(Organisation.class);
			q.setResult("JDOHelper.getObjectId(this)");

			@SuppressWarnings("unchecked")
			Collection<OrganisationID> organisationIDs = (Collection<OrganisationID>) q.execute();

			for (OrganisationID organisationID : organisationIDs) {
				if (getOrganisationID().equals(organisationID.organisationID))
					continue;

				try {
					pm.getObjectById(ProductTypeID.create(organisationID.organisationID, SimpleProductType.class.getName()));
				} catch (JDOObjectNotFoundException x) {
					res.add(organisationID);
				}
			}

			return res;
		} finally {
			pm.close();
		}
	}

}
