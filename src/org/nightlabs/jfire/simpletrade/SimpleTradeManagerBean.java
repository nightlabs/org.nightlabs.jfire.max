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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
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
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireException;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJB;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBUtil;
import org.nightlabs.jfire.jdo.notification.persistent.SubscriptionUtil;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.simpletrade.notification.SimpleProductTypeNotificationFilter;
import org.nightlabs.jfire.simpletrade.notification.SimpleProductTypeNotificationReceiver;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.simpletrade.store.SimpleProductTypeActionHandler;
import org.nightlabs.jfire.simpletrade.store.SimpleProductTypeSearchFilter;
import org.nightlabs.jfire.simpletrade.store.prop.SimpleProductTypeStruct;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.CrossTradeDeliveryCoordinator;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryConst;
import org.nightlabs.jfire.store.id.ProductTypeID;
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
import org.nightlabs.util.CollectionUtil;


/**
 * @ejb.bean name="jfire/ejb/JFireSimpleTrade/SimpleTradeManager"
 *					 jndi-name="jfire/ejb/JFireSimpleTrade/SimpleTradeManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class SimpleTradeManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(SimpleTradeManagerBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
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
	 * It creates the root simple product for the organisation itself.
	 * Simple products of other organisations must be imported.
	 * 
	 * @throws ModuleException
	 * @throws CannotPublishProductTypeException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise()
	throws ModuleException, CannotPublishProductTypeException
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {
			String organisationID = getOrganisationID();

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireSimpleTrade");
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of JFireSimpleTrade started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					"JFireSimpleTrade", "0.9.3-0-beta", "0.9.3-0-beta");
			pm.makePersistent(moduleMetaData);
			
			SimpleProductTypeStruct.getSimpleProductTypeStruct(organisationID, pm);

			SimpleProductTypeActionHandler simpleProductTypeActionHandler = new SimpleProductTypeActionHandler(
					Organisation.DEV_ORGANISATION_ID, SimpleProductTypeActionHandler.class.getName(), SimpleProductType.class);
			simpleProductTypeActionHandler = pm.makePersistent(simpleProductTypeActionHandler);

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
	
				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MAILING_VIRTUAL);
				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MAILING_PHYSICAL);
				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);
				
				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_DELIVER_TO_DELIVERY_QUEUE);
				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

				pm.makePersistent(deliveryConfiguration);
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
			

//			// TEST add test products
//			// TODO remove this test stuff
//			{
//				String langID = Locale.ENGLISH.getLanguage();
//
////				pm.getExtent(CustomerGroup.class);
////				CustomerGroup customerGroup = (CustomerGroup) pm.getObjectById(CustomerGroupID.create(organisationID, "default"));
//
//				pm.getExtent(Currency.class);
//				Currency euro = (Currency) pm.getObjectById(CurrencyID.create("EUR"));
//
//				pm.getExtent(Tariff.class);
//				Tariff tariff;
//				try {
//					tariff = (Tariff) pm.getObjectById(TariffID.create(organisationID, 0));
//				} catch (JDOObjectNotFoundException x) {
//					tariff = new Tariff(organisationID);
//					tariff.getName().setText(langID, "Normal Price");
//					pm.makePersistent(tariff);
//				}
//
//				// create the category "car"
//				SimpleProductType car = new SimpleProductType(
//						organisationID, "car", rootSimpleProductType, null,
//						ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
//				car.getName().setText(langID, "Car");
////				car.setDeliveryConfiguration(deliveryConfiguration);
//				store.addProductType(user, car, SimpleProductType.getDefaultHome(pm, car));
//				store.setProductTypeStatus_published(user, car);
//
//				// create the price config "Car - Middle Class"
//				PriceFragmentType totalPriceFragmentType = PriceFragmentType.getTotalPriceFragmentType(pm);
//				PriceFragmentType vatNet = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(getRootOrganisationID(), "vat-de-19-net"));
//				PriceFragmentType vatVal = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(getRootOrganisationID(), "vat-de-19-val"));
//
//				Accounting accounting = Accounting.getAccounting(pm);
//				Trader trader = Trader.getTrader(pm);
//				StablePriceConfig stablePriceConfig = new StablePriceConfig(organisationID, accounting.createPriceConfigID());
//				FormulaPriceConfig formulaPriceConfig = new FormulaPriceConfig(organisationID, accounting.createPriceConfigID());
//				formulaPriceConfig.getName().setText(langID, "Car - Middle Class");
//
//				CustomerGroup customerGroupDefault = trader.getDefaultCustomerGroupForKnownCustomer();
//				CustomerGroup customerGroupAnonymous = LegalEntity.getAnonymousCustomer(pm).getDefaultCustomerGroup();
//				formulaPriceConfig.addCustomerGroup(customerGroupDefault);
//				formulaPriceConfig.addCustomerGroup(customerGroupAnonymous);
//				formulaPriceConfig.addCurrency(euro);
//				formulaPriceConfig.addTariff(tariff);
////				formulaPriceConfig.addProductType(rootSimpleProductType);
//				formulaPriceConfig.addPriceFragmentType(totalPriceFragmentType);
//				formulaPriceConfig.addPriceFragmentType(vatNet);
//				formulaPriceConfig.addPriceFragmentType(vatVal);
//				stablePriceConfig.adoptParameters(formulaPriceConfig);
//
//				FormulaCell fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
//				fallbackFormulaCell.setFormula(totalPriceFragmentType,
//						"cell.resolvePriceCellsAmount(\n" +
//						"	new AbsolutePriceCoordinate(\n" +
//						"		\""+organisationID+"/"+CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT+"\",\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null\n" +
//						"	)\n" +
//						");");
//				fallbackFormulaCell.setFormula(vatNet, "cell.resolvePriceCellsAmount(\n" +
//						"	new AbsolutePriceCoordinate(\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		\""+Organisation.DEV_ORGANISATION_ID+"/_Total_\"\n" +
//						"	)\n" +
//						") / 1.16;");
//				fallbackFormulaCell.setFormula(vatVal, "cell.resolvePriceCellsAmount(\n" +
//						"	new AbsolutePriceCoordinate(\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		\""+Organisation.DEV_ORGANISATION_ID+"/_Total_\"\n" +
//						"	)\n" +
//						")\n" +
//
////						"/ 1.16 * 0.16");
//
//						"\n" +
//						"-\n" +
//						"\n" +
//						"cell.resolvePriceCellsAmount(\n" +
//						"	new AbsolutePriceCoordinate(\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		\""+getRootOrganisationID()+"/vat-de-19-net\"\n" +
//						"	)\n" +
//						");");
//
//				FormulaCell cell = formulaPriceConfig.createFormulaCell(customerGroupDefault, tariff, euro);
//				cell.setFormula(totalPriceFragmentType, "5000");
//
//				// create the car "BMW 320i" and assign the "Car - Middle Class" price config
//				SimpleProductType bmw320i = new SimpleProductType(
//						organisationID, "bmw320i", car, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
//				bmw320i.getName().setText(Locale.ENGLISH.getLanguage(), "BMW 320i");
//				bmw320i.setPackagePriceConfig(stablePriceConfig);
//				bmw320i.setInnerPriceConfig(formulaPriceConfig);
//				bmw320i.setDeliveryConfiguration(deliveryConfiguration);
//				store.addProductType(user, bmw320i, SimpleProductType.getDefaultHome(pm, bmw320i));
//
//				store.setProductTypeStatus_published(user, bmw320i);
//				store.setProductTypeStatus_confirmed(user, bmw320i);
//				store.setProductTypeStatus_saleable(user, bmw320i, true);
//
//				// create the category "Car Part"
//				SimpleProductType carPart = new SimpleProductType(
//						organisationID, "carPart", rootSimpleProductType, null, ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_INNER);
//				carPart.getName().setText(Locale.ENGLISH.getLanguage(), "Car Part");
//				carPart.setDeliveryConfiguration(deliveryConfiguration);
//				store.addProductType(user, carPart, SimpleProductType.getDefaultHome(pm, carPart));
//
//				// create the part "Wheel"
//				SimpleProductType wheel = new SimpleProductType(
//						organisationID, "wheel", carPart, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_INNER);
//				wheel.getName().setText(Locale.ENGLISH.getLanguage(), "Wheel");
//				wheel.setDeliveryConfiguration(deliveryConfiguration);
//				store.addProductType(user, wheel, SimpleProductType.getDefaultHome(pm, wheel));
//
//				// create the priceConfig "Car Part - Wheel" and assign it to "Wheel"
//				formulaPriceConfig = new FormulaPriceConfig(organisationID, accounting.createPriceConfigID());
//				formulaPriceConfig.addProductType(car);
//				formulaPriceConfig.addPriceFragmentType(vatVal);
//				formulaPriceConfig.addPriceFragmentType(vatNet);
//				formulaPriceConfig.getName().setText(langID, "Car Part - Wheel");
//				fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
//				fallbackFormulaCell.setFormula(
//						Organisation.DEV_ORGANISATION_ID,
//						PriceFragmentType.TOTAL_PRICEFRAGMENTTYPEID,
//						"cell.resolvePriceCellsAmount(\n" +
//						"	new AbsolutePriceCoordinate(\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		\""+car.getPrimaryKey()+"\",\n" +
//						"		null\n" +
//						"	)\n" +
//						") * 0.1;");
//
//				fallbackFormulaCell.setFormula(vatNet, "cell.resolvePriceCellsAmount(\n" +
//						"	new AbsolutePriceCoordinate(\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		\""+Organisation.DEV_ORGANISATION_ID+"/_Total_\"\n" +
//						"	)\n"+
//						") / 1.16;");
//				fallbackFormulaCell.setFormula(vatVal, "cell.resolvePriceCellsAmount(\n" +
//						"	new AbsolutePriceCoordinate(\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		\""+Organisation.DEV_ORGANISATION_ID+"/_Total_\"\n" +
//						"	)\n"+
//						")\n" +
//
////						"/ 1.16 * 0.16;");
//
//						"\n" +
//						"-\n" +
//						"\n" +
//						"cell.resolvePriceCellsAmount(\n" +
//						"	new AbsolutePriceCoordinate(\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		null,\n" +
//						"		\""+getRootOrganisationID()+"/vat-de-19-net\"\n" +
//						"	)\n"+
//						");");
//
//				wheel.setInnerPriceConfig(formulaPriceConfig);
//
//				// package 4 wheels inside the bmw320i
//				NestedProductTypeLocal wheelInsideBMW = bmw320i.createNestedProductType(wheel);
//				wheelInsideBMW.setQuantity(4);
//
//				// calculate prices
//				PriceCalculator priceCalculator = new PriceCalculator(bmw320i);
//				priceCalculator.preparePriceCalculation(accounting);
//				priceCalculator.calculatePrices();
//			}
//			// TEST END

			logger.info("Initialization of JFireSimpleTrade complete!");
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @return Returns a <tt>Collection</tt> of <tt>SimpleProductType</tt>.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="SimpleTradeManager-user"
//	 * @ejb.transaction type="Required"
//	 */
//	public Collection<SimpleProductType> test(Map<String, SimpleProductType> m)
//		throws ModuleException
//	{
//		return new ArrayList<SimpleProductType>(m.values());
//	}
//
//	/**
//	 * @return Returns a <tt>Collection</tt> of <tt>SimpleProductType</tt>.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="SimpleTradeManager-user"
//	 * @ejb.transaction type="Required"
//	 */
//	public Collection getChildProductTypes(ProductTypeID parentProductTypeID, String[] fetchGroups, int maxFetchDepth)
//		throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Collection res = SimpleProductType.getChildProductTypes(pm, parentProductTypeID);
//
//			FetchPlan fetchPlan = pm.getFetchPlan();
//			fetchPlan.setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups != null)
//				fetchPlan.setGroups(fetchGroups);
//
//			return pm.detachCopyAll(res);
//		} finally {
//			pm.close();
//		}
//	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<ProductTypeID> getChildSimpleProductTypeIDs(
			ProductTypeID parentSimpleProductTypeID) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(SimpleProductType.getChildProductTypes(pm,
					parentSimpleProductTypeID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public SimpleProductType getSimpleProductType(
			ProductTypeID simpleProductTypeID, String[] fetchGroups,
			int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			List<SimpleProductType> spts = getSimpleProductTypes(CollectionUtil.array2HashSet(new ProductTypeID[] {simpleProductTypeID}), fetchGroups, maxFetchDepth);
			if (spts.size() > 0)
				return spts.get(0);
			return null;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<SimpleProductType> getSimpleProductTypes(
			Collection<ProductTypeID> simpleProductTypeIDs, String[] fetchGroups,
			int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, simpleProductTypeIDs,
					SimpleProductType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @return Returns a newly detached instance of <tt>SimpleProductType</tt> if <tt>get</tt> is true - otherwise <tt>null</tt>.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="SimpleTradeManager.Admin"
	 * @ejb.transaction type="Required"
	 */
	public SimpleProductType storeProductType(SimpleProductType productType, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		if (productType == null)
			throw new IllegalArgumentException("productType must not be null!");

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups == null)
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			else
				pm.getFetchPlan().setGroups(fetchGroups);

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
			{
				ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
				pm.flush();
				pm.evictAll();
				productType = (SimpleProductType) pm.getObjectById(productTypeID);
			}

//			03:09:47,950 ERROR [LogInterceptor] RuntimeException in method: public abstract org.nightlabs.jfire.simpletrade.store.SimpleProductType org.nightlabs.jfire.simpletrade.SimpleTradeManager.storeProductType(org.nightlabs.jfire.simpletrade.store.SimpleProductType,boolean,java.lang.String[],int) throws org.nightlabs.ModuleException,java.rmi.RemoteException:
//				javax.jdo.JDODetachedFieldAccessException: You have just attempted to access field "extendedProductType" yet this field was not detached when you detached the object. Either dont access this field, or detach the field when detaching the object.
//				        at org.nightlabs.jfire.store.ProductType.jdoGetextendedProductType(ProductType.java)
//				        at org.nightlabs.jfire.store.ProductType.getExtendedProductType(ProductType.java:680)
//				        at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator._resolvableProductTypes_registerWithAnchestors(PriceCalculator.java:300)
//				        at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator.preparePriceCalculation_createResolvableProductTypesMap(PriceCalculator.java:282)
//				        at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator.preparePriceCalculation(PriceCalculator.java:159)
//				        at org.nightlabs.jfire.simpletrade.SimpleTradeManagerBean.storeProductType(SimpleTradeManagerBean.java:611)
//				        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//				        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//				        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//				        at java.lang.reflect.Method.invoke(Method.java:585)
//				        at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//				        at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//				        at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//				        at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//				        at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//				        at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//				        at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//				        at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//				        at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//				        at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//				        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:138)
//				        at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//				        at org.jboss.ejb.Container.invoke(Container.java:960)
//				        at sun.reflect.GeneratedMethodAccessor109.invoke(Unknown Source)
//				        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//				        at java.lang.reflect.Method.invoke(Method.java:585)
//				        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//				        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//				        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//				        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//				        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//				        at org.jboss.invocation.unified.server.UnifiedInvoker.invoke(UnifiedInvoker.java:231)
//				        at sun.reflect.GeneratedMethodAccessor133.invoke(Unknown Source)
//				        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//				        at java.lang.reflect.Method.invoke(Method.java:585)
//				        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//				        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//				        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//				        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//				        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//				        at javax.management.MBeanServerInvocationHandler.invoke(MBeanServerInvocationHandler.java:201)
//				        at $Proxy16.invoke(Unknown Source)
//				        at org.jboss.remoting.ServerInvoker.invoke(ServerInvoker.java:734)
//				        at org.jboss.remoting.transport.socket.ServerThread.processInvocation(ServerThread.java:560)
//				        at org.jboss.remoting.transport.socket.ServerThread.dorun(ServerThread.java:383)
//				        at org.jboss.remoting.transport.socket.ServerThread.run(ServerThread.java:165)


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
	 * @return Returns the {@link Property}s for the given simpleProductTypeIDs trimmed so that they only contain the given structFieldIDs.
	 * @see Property#detachPropertyWithTrimmedFieldList(PersistenceManager, Property, Set, String[], int)
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="SimpleTradeManager.Admin"
	 * @ejb.transaction type="Required"
	 */
	public Map<ProductTypeID, PropertySet> getSimpleProductTypesPropertySets(
			Set<ProductTypeID> simpleProductTypeIDs,
			Set<StructFieldID> structFieldIDs,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		Map<ProductTypeID, PropertySet> result = new HashMap<ProductTypeID, PropertySet>(simpleProductTypeIDs.size());
		PersistenceManager pm = getPersistenceManager();
		try {
			for (ProductTypeID productTypeID : simpleProductTypeIDs) {
				SimpleProductType productType = (SimpleProductType) pm.getObjectById(productTypeID);
				PropertySet detached = PropertySet.detachPropertyWithTrimmedFieldList(
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
	
//	/**
//	 * @return Returns a newly detached instance of <tt>SimpleProductType</tt> if <tt>get</tt> is true - otherwise <tt>null</tt>.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="SimpleTradeManager.Admin"
//	 * @ejb.transaction type="Required"
//	 */
//	public SimpleProductType updateProductType(SimpleProductType productType, boolean get, String[] fetchGroups, int maxFetchDepth)
//		throws ModuleException
//	{
//		if (productType == null)
//			throw new NullPointerException("productType");
//
//		PersistenceManager pm = getPersistenceManager();
//		try {
////			// WORKAROUND
////			pm.getExtent(SimpleProductType.class);
////			if (productType.getExtendedProductType() != null) {
////				Object oid = JDOHelper.getObjectId(productType.getExtendedProductType());
////				productType.setExtendedProductType((ProductType)pm.getObjectById(oid));
////			}
////
////			IInnerPriceConfig packagePriceConfig = productType.getInnerPriceConfig();
////			if (packagePriceConfig != null) {
////				pm.getExtent(packagePriceConfig.getClass());
////				Object oid = JDOHelper.getObjectId(packagePriceConfig);
////				if (oid != null) {
////					try {
////						packagePriceConfig = (IInnerPriceConfig) pm.getObjectById(oid);
////						productType.setInnerPriceConfig(packagePriceConfig);
////					} catch (JDOObjectNotFoundException x) {
////						// Object is not in datastore, so try to store it as it is.
////					}
////				}
////			}
////
////			IInnerPriceConfig innerPriceConfig = productType.getInnerPriceConfig();
////			if (innerPriceConfig != null) {
////				pm.getExtent(innerPriceConfig.getClass());
////				Object oid = JDOHelper.getObjectId(innerPriceConfig);
////				if (oid != null) {
////					try {
////						innerPriceConfig = (IInnerPriceConfig) pm.getObjectById(oid);
////						productType.setInnerPriceConfig(innerPriceConfig);
////					} catch (JDOObjectNotFoundException x) {
////						// Object is not in datastore, so try to store it as it is.
////					}
////				}
////			}
////			// END WORK AROUND
////			Object productTypeID = JDOHelper.getObjectId(productType);
////			if (productTypeID != null)
////				cache_addChangedObjectID(productTypeID);
//			if (!Store.getStore(pm).containsProductType(productType))
//				throw new IllegalStateException("The productType \""+productType.getPrimaryKey()+"\" is not yet known! Use addProductType(...) instead!");
//
//			return (SimpleProductType) NLJDOHelper.storeJDO(pm, productType, get, fetchGroups);
////			return (SimpleProductType)NLJDOHelper.storeJDO(pm, product);
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Set<PriceConfigID> getFormulaPriceConfigIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(pm.getExtent(FormulaPriceConfig.class, false));
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<PriceConfigID>((Collection<? extends PriceConfigID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@SuppressWarnings("unchecked")
	public List<FormulaPriceConfig> getFormulaPriceConfigs(Collection<PriceConfigID> formulaPriceConfigIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
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
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection<? extends Article> createArticles(
			SegmentID segmentID,
			OfferID offerID,
			ProductTypeID productTypeID,
			int quantity,
			TariffID tariffID,
			boolean allocate,
			boolean allocateSynchronously,
			String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
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

			Tariff tariff = (Tariff) pm.getObjectById(tariffID);

			// find an Offer within the Order which is not finalized - or create one
			Offer offer;
			if (offerID == null) {
				Collection offers = Offer.getNonFinalizedNonEndedOffers(pm, order);
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
//				SimpleProduct product = (SimpleProduct) it.next();
//				Article article = trader.createArticle(
//						user, offer, segment, product,
//						new ArticleCreator(tariff),
//						true, false);
//// auto-release must be controlled via the offer (the whole offer has an expiry time
////						new Date(System.currentTimeMillis() + 3600 * 1000 * 10)); // TODO the autoReleaseTimeout must come from the config
//				articles.add(article);
//			}

			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			// TODO remove this JPOX WORKAROUND! getting sometimes:
//			Caused by: javax.jdo.JDOUserException: Cannot read fields from a deleted object
//			FailedObject:jdo/org.nightlabs.jfire.accounting.id.PriceFragmentID?organisationID=chezfrancois.jfire.org&priceConfigID=9&priceID=232&priceFragmentTypePK=dev.jfire.org%2F_Total_
//			        at org.jpox.state.jdo.PersistentNewDeleted.transitionReadField(PersistentNewDeleted.java:105)
//			        at org.jpox.state.StateManagerImpl.transitionReadField(StateManagerImpl.java:3394)
//			        at org.jpox.state.StateManagerImpl.isLoaded(StateManagerImpl.java:1982)
//			        at org.jpox.store.rdbms.scostore.FKMapStore.put(FKMapStore.java:764)
//			        at org.jpox.store.rdbms.scostore.AbstractMapStore.putAll(AbstractMapStore.java:320)
//			        at org.jpox.store.mapping.MapMapping.postUpdate(MapMapping.java:175)
//			        at org.jpox.store.rdbms.request.UpdateRequest.execute(UpdateRequest.java:318)
//			        at org.jpox.store.rdbms.table.ClassTable.update(ClassTable.java:2573)
//			        at org.jpox.store.rdbms.table.ClassTable.update(ClassTable.java:2568)
//			        at org.jpox.store.StoreManager.update(StoreManager.java:967)
//			        at org.jpox.state.StateManagerImpl.flush(StateManagerImpl.java:4928)
//			        at org.jpox.AbstractPersistenceManager.flush(AbstractPersistenceManager.java:3233)
//			        at org.jpox.store.rdbms.RDBMSManagedTransaction.getConnection(RDBMSManagedTransaction.java:172)
//			        at org.jpox.store.rdbms.AbstractRDBMSTransaction.getConnection(AbstractRDBMSTransaction.java:97)
//			        at org.jpox.resource.JdoTransactionHandle.getConnection(JdoTransactionHandle.java:246)
//			        at org.jpox.store.rdbms.RDBMSManager.getConnection(RDBMSManager.java:426)
//			        at org.jpox.store.rdbms.scostore.AbstractSetStore.iterator(AbstractSetStore.java:123)
//			        at org.jpox.store.rdbms.scostore.FKMapStore.clear(FKMapStore.java:991)
//			        at org.jpox.sco.HashMap.clear(HashMap.java:717)
//			        at org.jpox.sco.HashMap.setValueFrom(HashMap.java:232)
//			        at org.jpox.sco.SCOUtils.newSCOInstance(SCOUtils.java:100)
//			        at org.jpox.state.StateManagerImpl.newSCOInstance(StateManagerImpl.java:3339)
//			        at org.jpox.state.StateManagerImpl.replaceSCOField(StateManagerImpl.java:3356)
//			        at org.jpox.state.DetachFieldManager.internalFetchObjectField(DetachFieldManager.java:88)
//			        at org.jpox.state.AbstractFetchFieldManager.fetchObjectField(AbstractFetchFieldManager.java:108)
//			        at org.jpox.state.StateManagerImpl.replacingObjectField(StateManagerImpl.java:2951)
//			        at org.nightlabs.jfire.accounting.Price.jdoReplaceField(Price.java)
//			        at org.nightlabs.jfire.trade.ArticlePrice.jdoReplaceField(ArticlePrice.java)
//			        at org.nightlabs.jfire.accounting.Price.jdoReplaceFields(Price.java)
//			        at org.jpox.state.StateManagerImpl.replaceFields(StateManagerImpl.java:3170)
//			        at org.jpox.state.StateManagerImpl.replaceFields(StateManagerImpl.java:3188)
//			        at org.jpox.state.StateManagerImpl.detachCopy(StateManagerImpl.java:4193)
//			        at org.jpox.AbstractPersistenceManager.internalDetachCopy(AbstractPersistenceManager.java:1944)
//			        at org.jpox.AbstractPersistenceManager.detachCopyInternal(AbstractPersistenceManager.java:1974)
//			        at org.jpox.resource.PersistenceManagerImpl.detachCopyInternal(PersistenceManagerImpl.java:961)
//			        at org.jpox.state.DetachFieldManager.internalFetchObjectField(DetachFieldManager.java:144)
//			        at org.jpox.state.AbstractFetchFieldManager.fetchObjectField(AbstractFetchFieldManager.java:108)
//			        at org.jpox.state.StateManagerImpl.replacingObjectField(StateManagerImpl.java:2951)
//			        at org.nightlabs.jfire.trade.Article.jdoReplaceField(Article.java)
//			        at org.nightlabs.jfire.trade.Article.jdoReplaceFields(Article.java)
//			        at org.jpox.state.StateManagerImpl.replaceFields(StateManagerImpl.java:3170)
//			        at org.jpox.state.StateManagerImpl.replaceFields(StateManagerImpl.java:3188)
//			        at org.jpox.state.StateManagerImpl.detachCopy(StateManagerImpl.java:4193)
//			        at org.jpox.AbstractPersistenceManager.internalDetachCopy(AbstractPersistenceManager.java:1944)
//			        at org.jpox.AbstractPersistenceManager.detachCopyInternal(AbstractPersistenceManager.java:1974)
//			        at org.jpox.AbstractPersistenceManager.detachCopyAll(AbstractPersistenceManager.java:2043)
//			        at org.jpox.resource.PersistenceManagerImpl.detachCopyAll(PersistenceManagerImpl.java:1000)
//			        at org.nightlabs.jfire.simpletrade.SimpleTradeManagerBean.createArticles(SimpleTradeManagerBean.java:745)

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

			return pm.detachCopyAll(articles);
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<ProductTypeID> getPublishedSimpleProductTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(SimpleProductType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			q.setFilter("this.published");
			Collection<ProductTypeID> res = CollectionUtil.castCollection((Collection<?>)q.execute());
			return new HashSet<ProductTypeID>(res);
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<SimpleProductType> getSimpleProductTypesForReseller(Collection<ProductTypeID> productTypeIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(SimpleProductType.class);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT,
					FetchGroupsPriceConfig.FETCH_GROUP_EDIT,
					DeliveryConfiguration.FETCH_GROUP_THIS_DELIVERY_CONFIGURATION,
					OrganisationLegalEntity.FETCH_GROUP_ORGANISATION,
					LegalEntity.FETCH_GROUP_PERSON,
					PropertySet.FETCH_GROUP_FULL_DATA // TODO we should somehow filter this so only public data is exported
					});
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS);

			List<SimpleProductType> res = new ArrayList<SimpleProductType>(productTypeIDs.size());
			for (ProductTypeID productTypeID : productTypeIDs) {
				if (!getOrganisationID().equals(productTypeID.organisationID))
					throw new IllegalArgumentException("Cannot get foreign SimpleProductTypes! Argument is invalid: " + productTypeID);

				SimpleProductType simpleProductType = (SimpleProductType) pm.getObjectById(productTypeID);

				// we need to strip off the nested product types (they're out of business ;-)
				// and we need to replace the price config - actually it should be sufficient to simply omit the inner price config
				// as the package price config contains only stable prices

				// we simply touch every field we need - the others should not be loaded and thus not detached then.
				simpleProductType.getName().getTexts();
				simpleProductType.getPackagePriceConfig();
				simpleProductType.getOwner();
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
					for (CustomerGroup customerGroup : gridPriceConfig.getCustomerGroups()) {
						
					}
	
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public void importSimpleProductTypesForReselling(String emitterOrganisationID)
	throws JFireException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				Hashtable initialContextProperties = getInitialContextProperties(emitterOrganisationID);

				PersistentNotificationEJB persistentNotificationEJB = PersistentNotificationEJBUtil.getHome(initialContextProperties).create();
				SimpleProductTypeNotificationFilter notificationFilter = new SimpleProductTypeNotificationFilter(
						emitterOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, getOrganisationID(),
						SimpleProductTypeNotificationFilter.class.getName());
				SimpleProductTypeNotificationReceiver notificationReceiver = new SimpleProductTypeNotificationReceiver(notificationFilter);
				notificationReceiver = pm.makePersistent(notificationReceiver);
				persistentNotificationEJB.storeNotificationFilter(notificationFilter, false, null, 1);

//				ArrayList<ProductTypeID> productTypeIDs = new ArrayList<ProductTypeID>(1);
//				productTypeIDs.add(productTypeID);

				SimpleTradeManager simpleTradeManager = SimpleTradeManagerUtil.getHome(initialContextProperties).create();

				Set<ProductTypeID> productTypeIDs = simpleTradeManager.getPublishedSimpleProductTypeIDs();
				Collection<SimpleProductType> productTypes = simpleTradeManager.getSimpleProductTypesForReseller(productTypeIDs);

				notificationReceiver.replicateSimpleProductTypes(emitterOrganisationID, productTypeIDs, new HashSet<ProductTypeID>(0));

//				if (productTypes.size() != 1)
//					throw new IllegalStateException("productTypes.size() != 1");
//
//				// currently we only support subscribing root-producttypes
//				for (SimpleProductType productType : productTypes) {
//					if (productType.getExtendedProductType() != null)
//						throw new UnsupportedOperationException("The given SimpleProductType is not a root node (not yet supported!): " + productTypeID);
//				}
//
//				productTypes = pm.makePersistentAll(productTypes);
//				return productTypes.iterator().next();
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			logger.error("Import of SimpleProductType failed!", x);
			throw new JFireException(x);
		}
	}

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public SimpleProductType backend_subscribe(ProductTypeID productTypeID, String[] fetchGroups, int maxFetchDepth)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//
//		} finally {
//			pm.close();
//		}
//	}

	private static Pattern tariffPKSplitPattern = null;

	/**
	 * @return a <tt>Collection</tt> of {@link TariffPricePair}
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection<TariffPricePair> getTariffPricePairs(
			PriceConfigID priceConfigID, CustomerGroupID customerGroupID, CurrencyID currencyID,
			String[] tariffFetchGroups, String[] priceFetchGroups)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (tariffPKSplitPattern == null)
				tariffPKSplitPattern = Pattern.compile("/");

			// TODO use setResult and put all this logic into the JDO query!
			StablePriceConfig priceConfig = (StablePriceConfig) pm.getObjectById(priceConfigID);
			Collection priceCells = priceConfig.getPriceCells(
					CustomerGroup.getPrimaryKey(customerGroupID.organisationID, customerGroupID.customerGroupID),
					currencyID.currencyID);

			Collection<TariffPricePair> res = new ArrayList<TariffPricePair>();

			for (Iterator it = priceCells.iterator(); it.hasNext(); ) {
				PriceCell priceCell = (PriceCell) it.next();
				String tariffPK = priceCell.getPriceCoordinate().getTariffPK();
				TariffID tariffID = TariffID.create(tariffPK);
//				String[] tariffPKParts = tariffPKSplitPattern.split(tariffPK);
//				if (tariffPKParts.length != 2)
//					throw new IllegalStateException("How the hell can it happen that the tariffPK does not consist out of two parts?");
//
//				String tariffOrganisationID = tariffPKParts[0];
//				long tariffID = Long.parseLong(tariffPKParts[1], 16);

				if (tariffFetchGroups != null)
					pm.getFetchPlan().setGroups(tariffFetchGroups);

				Tariff tariff = (Tariff) pm.getObjectById(tariffID); // TariffID.create(tariffOrganisationID, tariffID));
				tariff = pm.detachCopy(tariff);

				if (priceFetchGroups != null)
					pm.getFetchPlan().setGroups(priceFetchGroups);

				Price price = pm.detachCopy(priceCell.getPrice());

				res.add(new TariffPricePair(tariff, price));
			}

			return res;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection<GridPriceConfig> storePriceConfigs(Collection<GridPriceConfig> priceConfigs, boolean get, AssignInnerPriceConfigCommand assignInnerPriceConfigCommand)
	throws PriceCalculationException
	{
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * This method returns {@link OrganisationID}s for all {@link Organisation}s that are known to
	 * the current organisation, but excluding:
	 * <ul>
	 * <li>the current organisation</li>
	 * <li>all organisations for which already a subscribed root-simple-producttype exists</li>
	 * </ul>
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<OrganisationID> getCandidateOrganisationIDsForCrossTrade()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Set<OrganisationID> res = new HashSet<OrganisationID>();

			Query q = pm.newQuery(Organisation.class);
			q.setResult("JDOHelper.getObjectId(this)");
			for (OrganisationID organisationID : (Collection<OrganisationID>)q.execute()) {
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


	/**
	 * Searches with the given searchFilter for {@link SimpleProductType}s.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * 
	 * FIXME: Use {@link JDOQuery} instead of search filter and get only ids from the datastore
	 */
	public Collection<ProductTypeID> searchProductTypes(SimpleProductTypeSearchFilter searchFilter)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().clearGroups();
			Collection<ProductType> productTypes = (Collection<ProductType>) searchFilter.executeQuery(pm);
			Collection<ProductTypeID> ids = new ArrayList<ProductTypeID>(productTypes.size());
			for (ProductType	productType : productTypes)
				ids.add(productType.getObjectId());
			return ids;
		} finally {
			pm.close();
		}
	}
}
