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

package org.nightlabs.jfire.chezfrancois;

import java.rmi.RemoteException;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.tariffpriceconfig.FormulaCell;
import org.nightlabs.jfire.accounting.tariffpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.tariffpriceconfig.PriceCalculator;
import org.nightlabs.jfire.accounting.tariffpriceconfig.StablePriceConfig;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Trader;


/**
 * @ejb.bean name="jfire/ejb/JFireChezFrancois/ChezFrancoisDatastoreInitializer"	
 *					 jndi-name="jfire/ejb/JFireChezFrancois/ChezFrancoisDatastoreInitializer"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class ChezFrancoisDatastoreInitializerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(ChezFrancoisDatastoreInitializerBean.class);

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
	 * This method is called by the datastore initialization mechanism.
	 * It populates the datastore with the demo data.
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public void initialize() 
	throws ModuleException 
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {
			String organisationID = getOrganisationID();
			
			if (!ChezFrancoisServerInitializer.ORGANISATION_ID_WINE_STORE.equals(organisationID))
				return;

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireChezFrancois");
			if (moduleMetaData != null)
				return;

			LOGGER.info("Initialization of JFireChezFrancois started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					"JFireChezFrancois", "1.0.0-0-beta", "1.0.0-0-beta");
			pm.makePersistent(moduleMetaData);

			Store store = Store.getStore(pm);
//			Accounting accounting = Accounting.getAccounting(pm);

			User user = User.getUser(pm, getPrincipal());
			pm.getExtent(SimpleProductType.class);
			SimpleProductType rootSimpleProductType = (SimpleProductType) pm.getObjectById(
					ProductTypeID.create(organisationID, SimpleProductType.class.getName()));

			DeliveryConfiguration deliveryConfiguration = rootSimpleProductType.getDeliveryConfiguration();


			String langID = Locale.ENGLISH.getLanguage();

//				pm.getExtent(CustomerGroup.class);
//				CustomerGroup customerGroup = (CustomerGroup) pm.getObjectById(CustomerGroupID.create(organisationID, "default"));

			pm.getExtent(Currency.class);
			Currency euro = (Currency) pm.getObjectById(CurrencyID.create("EUR"));

			pm.getExtent(Tariff.class);
			Tariff tariff;
			try {
				tariff = (Tariff) pm.getObjectById(TariffID.create(organisationID, 0));
			} catch (JDOObjectNotFoundException x) {
				tariff = new Tariff(organisationID);
				tariff.getName().setText(langID, "Normal Price");
				pm.makePersistent(tariff);
			}

			// create the category "car"
			SimpleProductType car = new SimpleProductType(
					organisationID, "car", rootSimpleProductType, null, 
					ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
			car.getName().setText(langID, "Car");
//				car.setDeliveryConfiguration(deliveryConfiguration);
			store.addProductType(user, car, SimpleProductType.getDefaultHome(pm, car));
			store.setProductTypeStatus_published(user, car);

			// create the price config "Car - Middle Class"
			PriceFragmentType totalPriceFragmentType = PriceFragmentType.getTotalPriceFragmentType(pm);
			PriceFragmentType vatNet = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(getRootOrganisationID(), "vat-de-16-net"));
			PriceFragmentType vatVal = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(getRootOrganisationID(), "vat-de-16-val"));

			Accounting accounting = Accounting.getAccounting(pm);
			Trader trader = Trader.getTrader(pm);
			StablePriceConfig stablePriceConfig = new StablePriceConfig(organisationID, accounting.createPriceConfigID());
			FormulaPriceConfig formulaPriceConfig = new FormulaPriceConfig(organisationID, accounting.createPriceConfigID());
			formulaPriceConfig.getName().setText(langID, "Car - Middle Class");
			
			CustomerGroup customerGroupDefault = trader.getDefaultCustomerGroupForKnownCustomer();
			CustomerGroup customerGroupAnonymous = LegalEntity.getAnonymousCustomer(pm).getDefaultCustomerGroup();
			formulaPriceConfig.addCustomerGroup(customerGroupDefault);
			formulaPriceConfig.addCustomerGroup(customerGroupAnonymous);
			formulaPriceConfig.addCurrency(euro);
			formulaPriceConfig.addTariff(tariff);
//				formulaPriceConfig.addProductType(rootSimpleProductType);
			formulaPriceConfig.addPriceFragmentType(totalPriceFragmentType);
			formulaPriceConfig.addPriceFragmentType(vatNet);
			formulaPriceConfig.addPriceFragmentType(vatVal);
			stablePriceConfig.adoptParameters(formulaPriceConfig);

			FormulaCell fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
			fallbackFormulaCell.setFormula(totalPriceFragmentType,
					"cell.resolvePriceCellsAmount(\n" +
					"	new AbsolutePriceCoordinate(\n" +
					"		\""+organisationID+"/"+CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT+"\",\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		null\n" +
					"	)\n" +
					");");
			fallbackFormulaCell.setFormula(vatNet, "cell.resolvePriceCellsAmount(\n" +
					"	new AbsolutePriceCoordinate(\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
					"	)\n" +
					") / 1.16;");
			fallbackFormulaCell.setFormula(vatVal, "cell.resolvePriceCellsAmount(\n" +
					"	new AbsolutePriceCoordinate(\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
					"	)\n" +
					")\n" +

//						"/ 1.16 * 0.16");

					"\n" +
					"-\n" +
					"\n" +
					"cell.resolvePriceCellsAmount(\n" +
					"	new AbsolutePriceCoordinate(\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		\""+getRootOrganisationID()+"/vat-de-16-net\"\n" +
					"	)\n" +
					");");

			FormulaCell cell = formulaPriceConfig.createFormulaCell(customerGroupDefault, tariff, euro);
			cell.setFormula(totalPriceFragmentType, "5000");

			// create the car "BMW 320i" and assign the "Car - Middle Class" price config
			SimpleProductType bmw320i = new SimpleProductType(
					organisationID, "bmw320i", car, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
			bmw320i.getName().setText(Locale.ENGLISH.getLanguage(), "BMW 320i");
			bmw320i.setPackagePriceConfig(stablePriceConfig);
			bmw320i.setInnerPriceConfig(formulaPriceConfig);
			bmw320i.setDeliveryConfiguration(deliveryConfiguration);
			store.addProductType(user, bmw320i, SimpleProductType.getDefaultHome(pm, bmw320i));

			store.setProductTypeStatus_published(user, bmw320i);
			store.setProductTypeStatus_confirmed(user, bmw320i);
			store.setProductTypeStatus_saleable(user, bmw320i, true);

			// create the category "Car Part"
			SimpleProductType carPart = new SimpleProductType(
					organisationID, "carPart", rootSimpleProductType, null, ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_INNER);
			carPart.getName().setText(Locale.ENGLISH.getLanguage(), "Car Part");
			carPart.setDeliveryConfiguration(deliveryConfiguration);
			store.addProductType(user, carPart, SimpleProductType.getDefaultHome(pm, carPart));

			// create the part "Wheel"
			SimpleProductType wheel = new SimpleProductType(
					organisationID, "wheel", carPart, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_INNER);
			wheel.getName().setText(Locale.ENGLISH.getLanguage(), "Wheel");
			wheel.setDeliveryConfiguration(deliveryConfiguration);
			store.addProductType(user, wheel, SimpleProductType.getDefaultHome(pm, wheel));

			// create the priceConfig "Car Part - Wheel" and assign it to "Wheel"
			formulaPriceConfig = new FormulaPriceConfig(organisationID, accounting.createPriceConfigID());
			formulaPriceConfig.addProductType(car);
			formulaPriceConfig.addPriceFragmentType(vatVal);
			formulaPriceConfig.addPriceFragmentType(vatNet);
			formulaPriceConfig.getName().setText(langID, "Car Part - Wheel");
			fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
			fallbackFormulaCell.setFormula(
					Organisation.DEVIL_ORGANISATION_ID,
					PriceFragmentType.TOTAL_PRICEFRAGMENTTYPEID,
					"cell.resolvePriceCellsAmount(\n" +
					"	new AbsolutePriceCoordinate(\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		\""+car.getPrimaryKey()+"\",\n" +
					"		null\n" +
					"	)\n" +
					") * 0.1;");

			fallbackFormulaCell.setFormula(vatNet, "cell.resolvePriceCellsAmount(\n" +
					"	new AbsolutePriceCoordinate(\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
					"	)\n"+
					") / 1.16;");
			fallbackFormulaCell.setFormula(vatVal, "cell.resolvePriceCellsAmount(\n" +
					"	new AbsolutePriceCoordinate(\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
					"	)\n"+
					")\n" +
					
//						"/ 1.16 * 0.16;");
					
					"\n" +
					"-\n" +
					"\n" +
					"cell.resolvePriceCellsAmount(\n" +
					"	new AbsolutePriceCoordinate(\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		null,\n" +
					"		\""+getRootOrganisationID()+"/vat-de-16-net\"\n" +
					"	)\n"+
					");");

			wheel.setInnerPriceConfig(formulaPriceConfig);

			// package 4 wheels inside the bmw320i
			NestedProductType wheelInsideBMW = bmw320i.createNestedProductType(wheel);
			wheelInsideBMW.setQuantity(4);

			// calculate prices
			PriceCalculator priceCalculator = new PriceCalculator(bmw320i);
			priceCalculator.preparePriceCalculation(accounting);
			priceCalculator.calculatePrices();

			LOGGER.info("Initialization of JFireChezFrancois complete!");
		} finally {
			pm.close();
		}
	}

}
