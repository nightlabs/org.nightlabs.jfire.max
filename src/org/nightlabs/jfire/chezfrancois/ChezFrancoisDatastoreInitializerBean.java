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
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.fragmentbased.PFMappingAccountantDelegate;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.tariffpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.UserGroupRef;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;


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
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ChezFrancoisDatastoreInitializerBean.class);

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
		try {
			PersistenceManager pm = this.getPersistenceManager();
			try {
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
				pm.getFetchPlan().setMaxFetchDepth(1);
	
				String organisationID = getOrganisationID();
	
				if (!ChezFrancoisServerInitializer.ORGANISATION_ID_WINE_STORE.equals(organisationID))
					return;
	
				ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireChezFrancois");
				if (moduleMetaData != null)
					return;
	
				logger.info("Initialization of JFireChezFrancois started...");
	
				// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
				moduleMetaData = new ModuleMetaData(
						"JFireChezFrancois", "1.0.0-0-beta", "1.0.0-0-beta");
				pm.makePersistent(moduleMetaData);


				// registering demo timer task
				TaskID taskID = TaskID.create(Organisation.DEVIL_ORGANISATION_ID, Task.TASK_TYPE_ID_SYSTEM, "ChezFrancois-DemoTimerTask");
				try {
					Task task = (Task) pm.getObjectById(taskID);
					task.getActiveExecID();
				} catch (JDOObjectNotFoundException x) {
					Task task = new Task(
							taskID.organisationID, taskID.taskTypeID, taskID.taskID,
							User.getUser(pm, getOrganisationID(), User.USERID_SYSTEM),
							ChezFrancoisDatastoreInitializerHome.JNDI_NAME,
							"demoTimerTask");

					task.getName().setText(Locale.ENGLISH.getLanguage(), "Chez Francois Demo Timer Task");
					task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This task demonstrates how to use the JFire Timer.");

					task.getTimePatternSet().createTimePattern(
							"*", // year
							"*", // month
							"*", // day
							"mon-fri", // dayOfWeek
							"*", //  hour
							"*/2"); // minute

					task.getTimePatternSet().createTimePattern(
							"*", // year
							"*", // month
							"*", // day
							"sat,sun", // dayOfWeek
							"*", //  hour
							"1-59/2"); // minute

					task.setEnabled(true);
					pm.makePersistent(task);
				}
				// end registration of demo timer task


				String languageID = "en";
	
				// create Tariffs: normal price, gold card
				pm.getExtent(Tariff.class);
				Tariff tariffNormalPrice;
				try {
					tariffNormalPrice = (Tariff) pm.getObjectById(TariffID.create(organisationID, 0));
				} catch (JDOObjectNotFoundException x) {
					tariffNormalPrice = new Tariff(organisationID);
					tariffNormalPrice.getName().setText(languageID, "Normal Price");
					pm.makePersistent(tariffNormalPrice);
				}
	
				Tariff tariffGoldCard;
				try {
					tariffGoldCard = (Tariff) pm.getObjectById(TariffID.create(organisationID, 1));
				} catch (JDOObjectNotFoundException x) {
					tariffGoldCard = new Tariff(organisationID);
					tariffGoldCard.getName().setText(languageID, "Gold Card");
					pm.makePersistent(tariffGoldCard);
				}
	
				DataCreator dataCreator = new DataCreator(User.getUser(pm, getPrincipal()));
	
				dataCreator.getRootSimpleProductType().getName().setText(languageID, "Chez Francois Wine Store");
	
				// create ProductTypes: wine (bottle)
				SimpleProductType wine = dataCreator.createCategory(null, "wine", 
						"Wine", "Wein", "Vin");
				SimpleProductType bottle = dataCreator.createCategory(wine, "bottle", 
						"Bottle", "Flasche", "Bouteille");
				SimpleProductType bottleRed = dataCreator.createCategory(bottle, "bottle-red", 
						"Red", "Rot", "Rouge");
				SimpleProductType bottleWhite = dataCreator.createCategory(bottle, "bottle-white", 
						"White", "Weiß", "Blanc");
				SimpleProductType bottleMerlot = dataCreator.createCategory(bottleRed, "bottle-merlot", 
						"Merlot");
				SimpleProductType bottleCabernetSauvignon = dataCreator.createCategory(bottleRed, "bottle-cabernet-sauvignon", 
						"Cabernet Sauvignon");
				SimpleProductType bottlePinotNoir = dataCreator.createCategory(bottleRed, "bottle-pinot-noir", 
						"Pinot Noir");
	
				SimpleProductType bottleMerlotAustralia = dataCreator.createCategory(bottleMerlot, "bottle-merlot-australia", 
						"Australia", "Australien", "Australie");
				SimpleProductType bottleMerlotFrance = dataCreator.createCategory(bottleMerlot, "bottle-merlot-france", 
						"France", "Frankreich", "France");
				SimpleProductType bottleMerlotCalifornia = dataCreator.createCategory(bottleMerlot, "bottle-merlot-california", 
						"California", "Kalifornien", "Californie");
	
				SimpleProductType bottleCabernetSauvignonFrance = dataCreator.createCategory(bottleCabernetSauvignon, "bottle-cabernet-sauvignon-france", 
						"France", "Frankreich", "France");
				SimpleProductType bottleCabernetSauvignonSouthAfrika = dataCreator.createCategory(bottleCabernetSauvignon, "bottle-cabernet-sauvignon-south-africa", 
						"South Africa", "Südafrika", "Afrique du Sud");
	
				IInnerPriceConfig priceConfigCheapWines = dataCreator.createFixPriceConfig(new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {399, 350}, 
						"Cheap Wines", "Billiger Wein", "Vin pas cher");
				IInnerPriceConfig priceConfigMiddleWines = dataCreator.createFixPriceConfig(new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {500, 420}, 
						"Middle Wines", "Mittlerer Wein", "Vin moyen");
				IInnerPriceConfig priceConfigExpensiveWines = dataCreator.createFixPriceConfig(new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {999, 830}, 
						"Expensive Wines", "Teurer Wein", "Vin cher");
	
				SimpleProductType bottleMerlotAustralia2001 = dataCreator.createLeaf(bottleMerlotAustralia, "bottle-merlot-australia-2001", priceConfigExpensiveWines, 
						"Merlot 2001 (Australia)", "Merlot 2001 (Australien)", "Merlot 2001 (Australie)");
				SimpleProductType bottleMerlotAustralia2004 = dataCreator.createLeaf(bottleMerlotAustralia, "bottle-merlot-australia-2004", priceConfigCheapWines, 
						"Merlot 2004 (Australia)", "Merlot 2004 (Australien)", "Merlot 2004 (Australie)");
				SimpleProductType bottleMerlotFrance2001 = dataCreator.createLeaf(bottleMerlotFrance, "bottle-merlot-france-2001", priceConfigExpensiveWines, 
						"Merlot 2001 (France)", "Merlot 2001 (Frankreich)", "Merlot 2001 (France)");
				SimpleProductType bottleMerlotCalifornia2003 = dataCreator.createLeaf(bottleMerlotCalifornia, "bottle-merlot-california-2003", priceConfigMiddleWines, 
						"Merlot 2003 (California)", "Merlot 2003 (Kalifornien)", "Merlot 2003 (Californie)");
	
				SimpleProductType bottleCabernetSauvignonFrance2002 = dataCreator.createLeaf(bottleCabernetSauvignonFrance, "bottle-cabernet-sauvignon-france-2002", priceConfigMiddleWines, 
						"Cabernet Sauvignon 2002 (France)", "Cabernet Sauvignon 2002 (Frankreich)", "Cabernet Sauvignon 2002 (France)");
				SimpleProductType bottleCabernetSauvignonSouthAfrika2003 = dataCreator.createLeaf(bottleCabernetSauvignonSouthAfrika, "bottle-cabernet-sauvignon-south-africa-2003", priceConfigCheapWines, 
						"Cabernet Sauvignon 2003 (South Africa)", "Cabernet Sauvignon 2003 (Südafrika)", "Cabernet Sauvignon 2003 (Afrique du Sud)");
	
				// create ProductTypes: wine (box)
				SimpleProductType box = dataCreator.createCategory(wine, "box", 
						"Box", "Karton", "Caisse");
				SimpleProductType boxRed = dataCreator.createCategory(box, "box-red", 
						"Red", "Rot", "Rouge");
				SimpleProductType boxMerlot = dataCreator.createCategory(boxRed, "box-merlot", 
						"Merlot");
	
				SimpleProductType boxWhite = dataCreator.createCategory(box, "box-white", "White");
	
				IInnerPriceConfig priceConfigBox6Bottles90Percent = dataCreator.createFormulaPriceConfig(
						new Tariff[] {tariffNormalPrice, tariffGoldCard}, new String[] {
								"cell.resolvePriceCellsAmount(\n" +
								"	new AbsolutePriceCoordinate(\n" +
								"		null,\n" +
								"		null,\n" +
								"		null,\n" +
								"		\""+bottle.getPrimaryKey()+"\",\n" +
								"		null\n" +
								"	)\n" +
								") * -0.1;",
								"cell.resolvePriceCellsAmount(\n" +
								"	new AbsolutePriceCoordinate(\n" +
								"		null,\n" +
								"		null,\n" +
								"		null,\n" +
								"		\""+bottle.getPrimaryKey()+"\",\n" +
								"		null\n" +
								"	)\n" +
								") * -0.1;"},
								"Box (6 bottles, 90%)", "Karton (6 Flaschen, 90%)", "Caisse (6 bouteilles, 90%)"
								);
				((FormulaPriceConfig)priceConfigBox6Bottles90Percent).addProductType(bottle);
	
				SimpleProductType boxMerlotAustralia = dataCreator.createCategory(boxMerlot, "box-merlot-australia", 
						"Australia", "Australien", "Australie");
				SimpleProductType boxMerlotFrance = dataCreator.createCategory(boxMerlot, "box-merlot-france", 
						"France", "Frankreich", "France");
				SimpleProductType boxMerlotCalifornia = dataCreator.createCategory(boxMerlot, "box-merlot-california", 
						"California", "Kalifornien", "Californie");
	
				SimpleProductType boxMerlotAustralia2001 = dataCreator.createLeaf(boxMerlotAustralia, "box-merlot-australia-2001", priceConfigBox6Bottles90Percent, 
						"Box (6): Merlot 2001 (Australia)", "Karton (6): Merlot 2001 (Australien)", "Caisse (6): Merlot 2001 (Australie)");
				SimpleProductType boxMerlotAustralia2004 = dataCreator.createLeaf(boxMerlotAustralia, "box-merlot-australia-2004", priceConfigBox6Bottles90Percent, 
						"Box (6): Merlot 2004 (Australia)", "Karton (6): Merlot 2004 (Australien)", "Caisse (6): Merlot 2004 (Australie)");
				SimpleProductType boxMerlotFrance2001 = dataCreator.createLeaf(boxMerlotFrance, "box-merlot-france-2001", priceConfigBox6Bottles90Percent, 
						"Box (6): Merlot 2001 (France)", "Karton (6): Merlot 2001 (Frankreich)", "Caisse (6): Merlot 2001 (France)");
				SimpleProductType boxMerlotCalifornia2003 = dataCreator.createLeaf(boxMerlotCalifornia, "box-merlot-california-2003", priceConfigBox6Bottles90Percent, 
						"Box (6): Merlot 2003 (California)", "Karton (6): Merlot 2003 (Kalifornien)", "Caisse (6): Merlot 2003 (Californie)");
				boxMerlotAustralia2001.createNestedProductType(bottleMerlotAustralia2001).setQuantity(6);
				boxMerlotAustralia2004.createNestedProductType(bottleMerlotAustralia2004).setQuantity(6);
				boxMerlotFrance2001.createNestedProductType(bottleMerlotFrance2001).setQuantity(6);
				boxMerlotCalifornia2003.createNestedProductType(bottleMerlotCalifornia2003).setQuantity(6);

				boxMerlotAustralia2001.getFieldMetaData("nestedProductTypes").setValueInherited(false);
				boxMerlotAustralia2004.getFieldMetaData("nestedProductTypes").setValueInherited(false);
				boxMerlotFrance2001.getFieldMetaData("nestedProductTypes").setValueInherited(false);
				boxMerlotCalifornia2003.getFieldMetaData("nestedProductTypes").setValueInherited(false);

				// create ProductTypes: accessories
				IInnerPriceConfig priceConfigChocolate = dataCreator.createFixPriceConfig(
						new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {200, 150}, 
						"Chocolate", "Schokolade", "Chocolat");
				IInnerPriceConfig priceConfigCorkScrew = dataCreator.createFixPriceConfig(
						new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {600, 450}, 
						"Corkscrew", "Korkenzieher", "Tire-bouchon");
	
				SimpleProductType accessories = dataCreator.createCategory(null, "accessories", 
						"Accessories", "Zubehör", "Accessoires");
				SimpleProductType chocolate = dataCreator.createCategory(accessories, "chocolate", 
						"Chocolate", "Schokolade", "Chocolat");
				SimpleProductType sarotti = dataCreator.createLeaf(chocolate, "sarotti", priceConfigChocolate, 
						"Sarotti");
				SimpleProductType corkscrew = dataCreator.createCategory(accessories, "corkscrew", 
						"Corkscrew", "Korkenzieher", "Tire-bouchon");
				SimpleProductType corkscrewStainlessSteel = dataCreator.createLeaf(corkscrew, "corkscrew-stainless-steel", priceConfigCorkScrew, 
						"Corkscrew (stainless steel)", "Korkenzieher (Edelstahl)", "Tire-bouchon (inox)");

				dataCreator.calculatePrices();

				// create Accounts: Red Wine (bottle), White Wine (bottle), Red Wine (box), White Wine (box)
				Account accountBottleRedVatNet = dataCreator.createLocalAccount("bottle-red-vat-net.eur", "Bottle Red Net (EUR)");
				Account accountBottleRedVatVal = dataCreator.createLocalAccount("bottle-red-vat-val.eur", "Bottle Red VAT (EUR)");
				Account accountBottleWhiteVatNet = dataCreator.createLocalAccount("bottle-white-vat-net.eur", "Bottle White Net (EUR)");
				Account accountBottleWhiteVatVal = dataCreator.createLocalAccount("bottle-white-vat-val.eur", "Bottle White VAT (EUR)");

				Account accountBoxRedVatNet = dataCreator.createLocalAccount("box-red-vat-net.eur", "Box Red Net (EUR)");
				Account accountBoxRedVatVal = dataCreator.createLocalAccount("box-red-vat-val.eur", "Box Red VAT (EUR)");
				Account accountBoxWhiteVatNet = dataCreator.createLocalAccount("box-white-vat-net.eur", "Box White Net (EUR)");
				Account accountBoxWhiteVatVal = dataCreator.createLocalAccount("box-white-vat-val.eur", "Box White VAT (EUR)");

				// create Accounts: Accessories
				Account accessoriesVatNet = dataCreator.createLocalAccount("accessories-vat-net.eur", "Accessories Net (EUR)");
				Account accessoriesVatVal = dataCreator.createLocalAccount("accessories-vat-val.eur", "Accessories VAT (EUR)");

				// configure moneyflow
				LocalAccountantDelegate wineAccountantDelegate = new PFMappingAccountantDelegate(organisationID, "wineAccountantDelegate");
				wine.setLocalAccountantDelegate(wineAccountantDelegate);
	
				wineAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(bottleRed, dataCreator.getPriceFragmentTypeTotal(), accountBottleRedVatNet));
				wineAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(bottleRed, dataCreator.getPriceFragmentTypeVatVal(), accountBottleRedVatVal));
	
				wineAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(bottleWhite, dataCreator.getPriceFragmentTypeVatNet(), accountBottleWhiteVatNet));
				wineAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(bottleWhite, dataCreator.getPriceFragmentTypeVatVal(), accountBottleWhiteVatVal));
	
				wineAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(boxRed, dataCreator.getPriceFragmentTypeTotal(), accountBoxRedVatNet));
				wineAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(boxRed, dataCreator.getPriceFragmentTypeVatVal(), accountBoxRedVatVal));
	
				wineAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(boxWhite, dataCreator.getPriceFragmentTypeVatNet(), accountBoxWhiteVatNet));
				wineAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(boxWhite, dataCreator.getPriceFragmentTypeVatVal(), accountBoxWhiteVatVal));
	
				LocalAccountantDelegate accessoriesAccountantDelegate = new PFMappingAccountantDelegate(organisationID, "accessoriesAccountantDelegate");
				accessories.setLocalAccountantDelegate(accessoriesAccountantDelegate);
	
				accessoriesAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(accessories, dataCreator.getPriceFragmentTypeVatNet(), accessoriesVatNet));
				accessoriesAccountantDelegate.addMoneyFlowMapping(
						dataCreator.createPFMoneyFlowMapping(accessories, dataCreator.getPriceFragmentTypeVatVal(), accessoriesVatVal));
	
	
				// apply inheritance (e.g. because of LocalAccountantDelegates)
				wine.getFieldMetaData("localAccountantDelegate").setValueInherited(false);
				wine.applyInheritance();
				accessories.getFieldMetaData("localAccountantDelegate").setValueInherited(false);
				accessories.applyInheritance();


				UserGroup userGroup = new UserGroup(organisationID, UserGroup.USERID_PREFIX_TYPE_USERGROUP + "SalesAgents");
				userGroup.setName("Sales Agents");
				userGroup.setDescription("This group is blablabla.");
				new UserLocal(userGroup);
				userGroup = (UserGroup) pm.makePersistent(userGroup);

				userGroup = new UserGroup(organisationID, UserGroup.USERID_PREFIX_TYPE_USERGROUP + "SalesManagers");
				userGroup.setName("Sales Managers");
				userGroup.setDescription("This group is blablabla.");
				new UserLocal(userGroup);
				userGroup = (UserGroup) pm.makePersistent(userGroup);

				userGroup = new UserGroup(organisationID, UserGroup.USERID_PREFIX_TYPE_USERGROUP + "Statistics");
				userGroup.setName("Statistics");
				userGroup.setDescription("This group consists out of the statistics guys.");
				new UserLocal(userGroup);
				userGroup = (UserGroup) pm.makePersistent(userGroup);

				userGroup = new UserGroup(organisationID, UserGroup.USERID_PREFIX_TYPE_USERGROUP + "TheOthers");
				userGroup.setName("The Others");
				userGroup.setDescription("This group is trallali trallala.");
				new UserLocal(userGroup);
				userGroup = (UserGroup) pm.makePersistent(userGroup);

				// create some more users
				User user00 = dataCreator.createUser("user00", "test", "Chez Francois", "Miller", "Adam", "adam.miller@chezfrancois.co.th");
				LegalEntity legalEntity00 = dataCreator.createLegalEntity(user00.getPerson());
				User user01 = dataCreator.createUser("user01", "test", "Chez Francois", "Miller", "Eva", "eva.miller@chezfrancois.co.th");
				LegalEntity legalEntity01 = dataCreator.createLegalEntity(user01.getPerson());
				User user02 = dataCreator.createUser("marco", "test", "NightLabs GmbH", "Schulze", "Marco", "m a r c o.at.nightlabs dot de");
				LegalEntity legalEntity02 = dataCreator.createLegalEntity(user02.getPerson());
				User user03 = dataCreator.createUser("alex", "test", "NightLabs GmbH", "Bieber", "Alex", "a l e x.at.nightlabs dot de");
				LegalEntity legalEntity03 = dataCreator.createLegalEntity(user03.getPerson());

				userGroup = new UserGroup(organisationID, UserGroup.USERID_PREFIX_TYPE_USERGROUP + "Administrators");
				userGroup.setName("Administrators");
				userGroup.setDescription("This group has all access rights within its organisation.");
				new UserLocal(userGroup);
				userGroup = (UserGroup) pm.makePersistent(userGroup);
				userGroup.addUser(user00);
				userGroup.addUser(user01);
				userGroup.addUser(user02);
				userGroup.addUser(user03);

				Authority authority = (Authority) pm.getObjectById(AuthorityID.create(organisationID, Authority.AUTHORITY_ID_ORGANISATION));

				UserGroupRef userGroupRef = (UserGroupRef) authority.createUserRef(userGroup);
				authority.createUserRef(user00);
				authority.createUserRef(user01);

				for (Iterator it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
					RoleGroup roleGroup = (RoleGroup) it.next();
					RoleGroupRef roleGroupRef = authority.getRoleGroupRef(roleGroup.getRoleGroupID());

					if (roleGroupRef == null)
						continue;

					userGroupRef.addRoleGroupRef(roleGroupRef);
				}

				dataCreator.createOrderForEndcustomer(LegalEntity.getAnonymousCustomer(pm));
				dataCreator.createOrderForEndcustomer(LegalEntity.getAnonymousCustomer(pm));
				dataCreator.createOrderForEndcustomer(legalEntity00);
				dataCreator.createOrderForEndcustomer(LegalEntity.getAnonymousCustomer(pm));
				dataCreator.createOrderForEndcustomer(legalEntity00);

				logger.info("Initialization of JFireChezFrancois complete!");


				logger.info("Initializing JDO for Article.class...");
				pm.getExtent(Article.class);
				logger.info("Initializing JDO for Article.class complete!");
	
			} finally {
				pm.close();
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (ModuleException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * This is a demo task doing nothing.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public void demoTimerTask(TaskID taskID) 
	{
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");

		String jndiName = "java:/jbpm/JbpmConfiguration";
		try {
			InitialContext initialContext = new InitialContext();
			Object o = initialContext.lookup(jndiName);
			logger.info("fetched object from jndi with jndiName \""+jndiName+"\"");
			logger.info("  class=" + (o == null ? null : o.getClass().getName()));
			logger.info("  instance=" + o);
		} catch (Throwable t) {
			logger.error("Lookup of jbpm with jndiName \""+jndiName+"\" failed!", t);
		}

		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");

		jndiName = "java:/jbpm/JbpmConfigurationMarco";
		try {
			InitialContext initialContext = new InitialContext();
			Object o = initialContext.lookup(jndiName);
			logger.info("fetched object from jndi with jndiName \""+jndiName+"\"");
			logger.info("  class=" + (o == null ? null : o.getClass().getName()));
			logger.info("  instance=" + o);
		} catch (Throwable t) {
			logger.error("Lookup of jbpm with jndiName \""+jndiName+"\" failed!", t);
		}

		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");

		logger.info("demoTimerTask: entered for taskID " + taskID);
		logger.info("demoTimerTask: sleeping 10 sec");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ignore) { }
		logger.info("demoTimerTask: about to exit for taskID " + taskID);
	}

//	/**
//	 * This method is called by the datastore initialization mechanism.
//	 * It populates the datastore with the demo data.
//	 * 
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type = "Required"
//	 */
//	public void initializeOLD() 
//	throws ModuleException 
//	{
//		PersistenceManager pm = this.getPersistenceManager();
//		try {
//			String organisationID = getOrganisationID();
//			
//			if (!ChezFrancoisServerInitializer.ORGANISATION_ID_WINE_STORE.equals(organisationID))
//				return;
//
//			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireChezFrancois");
//			if (moduleMetaData != null)
//				return;
//
//			LOGGER.info("Initialization of JFireChezFrancois started...");
//
//			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
//			moduleMetaData = new ModuleMetaData(
//					"JFireChezFrancois", "1.0.0-0-beta", "1.0.0-0-beta");
//			pm.makePersistent(moduleMetaData);
//
//			Store store = Store.getStore(pm);
////			Accounting accounting = Accounting.getAccounting(pm);
//
//			User user = User.getUser(pm, getPrincipal());
//			pm.getExtent(SimpleProductType.class);
//			SimpleProductType rootSimpleProductType = (SimpleProductType) pm.getObjectById(
//					ProductTypeID.create(organisationID, SimpleProductType.class.getName()));
//
//			DeliveryConfiguration deliveryConfiguration = rootSimpleProductType.getDeliveryConfiguration();
//
//
//			String langID = Locale.ENGLISH.getLanguage();
//
////				pm.getExtent(CustomerGroup.class);
////				CustomerGroup customerGroup = (CustomerGroup) pm.getObjectById(CustomerGroupID.create(organisationID, "default"));
//
//			pm.getExtent(Currency.class);
//			Currency euro = (Currency) pm.getObjectById(CurrencyID.create("EUR"));
//
//			pm.getExtent(Tariff.class);
//			Tariff tariff;
//			try {
//				tariff = (Tariff) pm.getObjectById(TariffID.create(organisationID, 0));
//			} catch (JDOObjectNotFoundException x) {
//				tariff = new Tariff(organisationID);
//				tariff.getName().setText(langID, "Normal Price");
//				pm.makePersistent(tariff);
//			}
//
//			// create the category "car"
//			SimpleProductType car = new SimpleProductType(
//					organisationID, "car", rootSimpleProductType, null, 
//					ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
//			car.getName().setText(langID, "Car");
////				car.setDeliveryConfiguration(deliveryConfiguration);
//			store.addProductType(user, car, SimpleProductType.getDefaultHome(pm, car));
//			store.setProductTypeStatus_published(user, car);
//
//			// create the price config "Car - Middle Class"
//			PriceFragmentType totalPriceFragmentType = PriceFragmentType.getTotalPriceFragmentType(pm);
//			PriceFragmentType vatNet = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(getRootOrganisationID(), "vat-de-16-net"));
//			PriceFragmentType vatVal = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(getRootOrganisationID(), "vat-de-16-val"));
//
//			Accounting accounting = Accounting.getAccounting(pm);
//			Trader trader = Trader.getTrader(pm);
//			StablePriceConfig stablePriceConfig = new StablePriceConfig(organisationID, accounting.createPriceConfigID());
//			FormulaPriceConfig formulaPriceConfig = new FormulaPriceConfig(organisationID, accounting.createPriceConfigID());
//			formulaPriceConfig.getName().setText(langID, "Car - Middle Class");
//			
//			CustomerGroup customerGroupDefault = trader.getDefaultCustomerGroupForKnownCustomer();
//			CustomerGroup customerGroupAnonymous = LegalEntity.getAnonymousCustomer(pm).getDefaultCustomerGroup();
//			formulaPriceConfig.addCustomerGroup(customerGroupDefault);
//			formulaPriceConfig.addCustomerGroup(customerGroupAnonymous);
//			formulaPriceConfig.addCurrency(euro);
//			formulaPriceConfig.addTariff(tariff);
////				formulaPriceConfig.addProductType(rootSimpleProductType);
//			formulaPriceConfig.addPriceFragmentType(totalPriceFragmentType);
//			formulaPriceConfig.addPriceFragmentType(vatNet);
//			formulaPriceConfig.addPriceFragmentType(vatVal);
//			stablePriceConfig.adoptParameters(formulaPriceConfig);
//
//			FormulaCell fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
//			fallbackFormulaCell.setFormula(totalPriceFragmentType,
//					"cell.resolvePriceCellsAmount(\n" +
//					"	new AbsolutePriceCoordinate(\n" +
//					"		\""+organisationID+"/"+CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT+"\",\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null\n" +
//					"	)\n" +
//					");");
//			fallbackFormulaCell.setFormula(vatNet, "cell.resolvePriceCellsAmount(\n" +
//					"	new AbsolutePriceCoordinate(\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
//					"	)\n" +
//					") / 1.16;");
//			fallbackFormulaCell.setFormula(vatVal, "cell.resolvePriceCellsAmount(\n" +
//					"	new AbsolutePriceCoordinate(\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
//					"	)\n" +
//					")\n" +
//
////						"/ 1.16 * 0.16");
//
//					"\n" +
//					"-\n" +
//					"\n" +
//					"cell.resolvePriceCellsAmount(\n" +
//					"	new AbsolutePriceCoordinate(\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		\""+getRootOrganisationID()+"/vat-de-16-net\"\n" +
//					"	)\n" +
//					");");
//
//			FormulaCell cell = formulaPriceConfig.createFormulaCell(customerGroupDefault, tariff, euro);
//			cell.setFormula(totalPriceFragmentType, "5000");
//
//			// create the car "BMW 320i" and assign the "Car - Middle Class" price config
//			SimpleProductType bmw320i = new SimpleProductType(
//					organisationID, "bmw320i", car, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
//			bmw320i.getName().setText(Locale.ENGLISH.getLanguage(), "BMW 320i");
//			bmw320i.setPackagePriceConfig(stablePriceConfig);
//			bmw320i.setInnerPriceConfig(formulaPriceConfig);
//			bmw320i.setDeliveryConfiguration(deliveryConfiguration);
//			store.addProductType(user, bmw320i, SimpleProductType.getDefaultHome(pm, bmw320i));
//
//			store.setProductTypeStatus_published(user, bmw320i);
//			store.setProductTypeStatus_confirmed(user, bmw320i);
//			store.setProductTypeStatus_saleable(user, bmw320i, true);
//
//			// create the category "Car Part"
//			SimpleProductType carPart = new SimpleProductType(
//					organisationID, "carPart", rootSimpleProductType, null, ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_INNER);
//			carPart.getName().setText(Locale.ENGLISH.getLanguage(), "Car Part");
//			carPart.setDeliveryConfiguration(deliveryConfiguration);
//			store.addProductType(user, carPart, SimpleProductType.getDefaultHome(pm, carPart));
//
//			// create the part "Wheel"
//			SimpleProductType wheel = new SimpleProductType(
//					organisationID, "wheel", carPart, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_INNER);
//			wheel.getName().setText(Locale.ENGLISH.getLanguage(), "Wheel");
//			wheel.setDeliveryConfiguration(deliveryConfiguration);
//			store.addProductType(user, wheel, SimpleProductType.getDefaultHome(pm, wheel));
//
//			// create the priceConfig "Car Part - Wheel" and assign it to "Wheel"
//			formulaPriceConfig = new FormulaPriceConfig(organisationID, accounting.createPriceConfigID());
//			formulaPriceConfig.addProductType(car);
//			formulaPriceConfig.addPriceFragmentType(vatVal);
//			formulaPriceConfig.addPriceFragmentType(vatNet);
//			formulaPriceConfig.getName().setText(langID, "Car Part - Wheel");
//			fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
//			fallbackFormulaCell.setFormula(
//					Organisation.DEVIL_ORGANISATION_ID,
//					PriceFragmentType.TOTAL_PRICEFRAGMENTTYPEID,
//					"cell.resolvePriceCellsAmount(\n" +
//					"	new AbsolutePriceCoordinate(\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		\""+car.getPrimaryKey()+"\",\n" +
//					"		null\n" +
//					"	)\n" +
//					") * 0.1;");
//
//			fallbackFormulaCell.setFormula(vatNet, "cell.resolvePriceCellsAmount(\n" +
//					"	new AbsolutePriceCoordinate(\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
//					"	)\n"+
//					") / 1.16;");
//			fallbackFormulaCell.setFormula(vatVal, "cell.resolvePriceCellsAmount(\n" +
//					"	new AbsolutePriceCoordinate(\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
//					"	)\n"+
//					")\n" +
//					
////						"/ 1.16 * 0.16;");
//					
//					"\n" +
//					"-\n" +
//					"\n" +
//					"cell.resolvePriceCellsAmount(\n" +
//					"	new AbsolutePriceCoordinate(\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		null,\n" +
//					"		\""+getRootOrganisationID()+"/vat-de-16-net\"\n" +
//					"	)\n"+
//					");");
//
//			wheel.setInnerPriceConfig(formulaPriceConfig);
//
//			// package 4 wheels inside the bmw320i
//			NestedProductType wheelInsideBMW = bmw320i.createNestedProductType(wheel);
//			wheelInsideBMW.setQuantity(4);
//
//			// calculate prices
//			PriceCalculator priceCalculator = new PriceCalculator(bmw320i);
//			priceCalculator.preparePriceCalculation(accounting);
//			priceCalculator.calculatePrices();
//
//			LOGGER.info("Initialization of JFireChezFrancois complete!");
//		} finally {
//			pm.close();
//		}
//	}

}
