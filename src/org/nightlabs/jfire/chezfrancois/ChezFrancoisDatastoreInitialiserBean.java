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
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.moduleregistry.MalformedVersionException;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.fragmentbased.PFMappingAccountantDelegate;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.UserGroupRef;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.timepattern.TimePatternFormatException;


/**
 * @ejb.bean name="jfire/ejb/JFireChezFrancois/ChezFrancoisDatastoreInitialiser"	
 *					 jndi-name="jfire/ejb/JFireChezFrancois/ChezFrancoisDatastoreInitialiser"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class ChezFrancoisDatastoreInitialiserBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ChezFrancoisDatastoreInitialiserBean.class);

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
	 * There seems to be a heisenbug in JPOX which causes it to fail sometimes with a "mc closed" error. Therefore, we simply perform
	 * the initialisation twice (if the first time succeeded, the second call is a noop anymway).
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Supports"
	 */
	public void initialise2() 
	throws Exception 
	{
		initialise();
	}

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It populates the datastore with the demo data.
	 * @throws MalformedVersionException 
	 * @throws TimePatternFormatException 
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Supports"
	 */
	public void initialise()
	throws Exception 
	{
		if (getOrganisationID().equals(getRootOrganisationID()))
			return;

		ChezFrancoisDatastoreInitialiserLocal initialiser = ChezFrancoisDatastoreInitialiserUtil.getLocalHome().create();
		initialiser.createModuleMetaData();
		initialiser.createDemoTimerTask();

		boolean initJFireSimpleTrade = false;
		try {
			Class.forName("org.nightlabs.jfire.simpletrade.store.SimpleProductType");
			initJFireSimpleTrade = true;
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireSimpleTrade is not deployed. Cannot create demo data for this module.");
		}
		if (initJFireSimpleTrade)
			initialiser.createDemoData_JFireSimpleTrade();

		boolean initJFireVoucher = false;
		try {
			Class.forName("org.nightlabs.jfire.voucher.store.VoucherType");
			initJFireVoucher = true;
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireVoucher is not deployed. Cannot create demo data for this module.");
		}
		if (initJFireVoucher)
			initialiser.createDemoData_JFireVoucher();

		boolean initJFireDynamicTrade = false;
		try {
			Class.forName("org.nightlabs.jfire.dynamictrade.store.DynamicProductType");
			initJFireDynamicTrade = true;
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireDynamicTrade is not deployed. Cannot create demo data for this module.");
		}
		if (initJFireDynamicTrade)
			initialiser.createDemoData_JFireDynamicTrade();
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createModuleMetaData()
	throws MalformedVersionException 
	{
		logger.info("createModuleMetaData: begin");

		PersistenceManager pm = this.getPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireChezFrancois");
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of JFireChezFrancois started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					"JFireChezFrancois", "1.0.0-0-beta", "1.0.0-0-beta");
			pm.makePersistent(moduleMetaData);


		} finally {
			pm.close();
			logger.info("createModuleMetaData: end");
		}
	}

	private String getDisplayName()
	{
		String displayName = "Chez Francois";
		if (ChezFrancoisServerInitialiser.ORGANISATION_ID_RESELLER.equals(getOrganisationID())) {
			displayName = "Reseller";
		}
		return displayName;
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createDemoTimerTask() throws TimePatternFormatException 
	{
		logger.info("createDemoTimerTask: begin");

		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getFetchPlan().setMaxFetchDepth(1);

			// registering demo timer task
			TaskID taskID = TaskID.create(
					// Organisation.DEVIL_ORGANISATION_ID, // the task can be modified by the organisation and thus it's maybe more logical to use the real organisationID - not devil
					getOrganisationID(),
					Task.TASK_TYPE_ID_SYSTEM, "ChezFrancois-DemoTimerTask");
			try {
				Task task = (Task) pm.getObjectById(taskID);
				task.getActiveExecID(); // WORKAROUND for jpox heisenbug
			} catch (JDOObjectNotFoundException x) {
				Task task = new Task(
						taskID.organisationID, taskID.taskTypeID, taskID.taskID,
						User.getUser(pm, getOrganisationID(), User.USERID_SYSTEM),
						ChezFrancoisDatastoreInitialiserHome.JNDI_NAME,
						"demoTimerTask");

				task.getName().setText(Locale.ENGLISH.getLanguage(), getDisplayName() + " Demo Timer Task");
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
		} finally {
			pm.close();
			logger.info("createDemoTimerTask: end");
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createDemoData_JFireVoucher()
	throws ModuleException 
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {

		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createDemoData_JFireDynamicTrade()
	throws ModuleException 
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {
			String organisationID = getOrganisationID();

			pm.getExtent(DynamicProductType.class);
			ProductTypeID softwareDevelopmentID = ProductTypeID.create(organisationID, "softwareDevelopment");
			try {
				pm.getObjectById(softwareDevelopmentID);
				return; // it exists => do nothing
			} catch (JDOObjectNotFoundException x) {
				// ignore this exception and create demo date
			}

			// create Tariffs: normal price, gold card
			Tariff tariffNormalPrice = getTariffNormalPrice(pm);
			Tariff tariffGoldCard = getTariffGoldCard(pm);

			
		} finally {
			pm.close();
		}
	}

	private static Tariff getTariffNormalPrice(PersistenceManager pm)
	{
		String organisationID = IDGenerator.getOrganisationID();
		pm.getExtent(Tariff.class);
		Tariff tariffNormalPrice;
		try {
			tariffNormalPrice = (Tariff) pm.getObjectById(TariffID.create(organisationID, 0));
		} catch (JDOObjectNotFoundException x) {
			tariffNormalPrice = (Tariff) pm.makePersistent(new Tariff(organisationID, IDGenerator.nextID(Tariff.class)));
			tariffNormalPrice.getName().setText(Locale.ENGLISH.getLanguage(), "Normal Price");
			tariffNormalPrice.getName().setText(Locale.GERMAN.getLanguage(), "Normaler Preis");
			tariffNormalPrice.getName().setText(Locale.FRENCH.getLanguage(), "Prix normal");
		}
		return tariffNormalPrice;
	}

	private static Tariff getTariffGoldCard(PersistenceManager pm)
	{
		String organisationID = IDGenerator.getOrganisationID();
		pm.getExtent(Tariff.class);
		Tariff tariffGoldCard;
		try {
			tariffGoldCard = (Tariff) pm.getObjectById(TariffID.create(organisationID, 1));
		} catch (JDOObjectNotFoundException x) {
			tariffGoldCard = (Tariff) pm.makePersistent(new Tariff(organisationID, IDGenerator.nextID(Tariff.class)));
			tariffGoldCard.getName().setText(Locale.ENGLISH.getLanguage(), "Gold Card");
			tariffGoldCard.getName().setText(Locale.GERMAN.getLanguage(), "Goldene Kundenkarte");
			tariffGoldCard.getName().setText(Locale.FRENCH.getLanguage(), "Carte d'or");
		}
		return tariffGoldCard;
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createDemoData_JFireSimpleTrade()
	throws ModuleException 
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {
			String organisationID = getOrganisationID();

			pm.getExtent(SimpleProductType.class);
			ProductTypeID wineID = ProductTypeID.create(organisationID, "wine");
			try {
				pm.getObjectById(wineID);
				return; // it exists => do nothing
			} catch (JDOObjectNotFoundException x) {
				// ignore this exception and create demo date
			}

			// create Tariffs: normal price, gold card
			Tariff tariffNormalPrice = getTariffNormalPrice(pm);
			Tariff tariffGoldCard = getTariffGoldCard(pm);

			DataCreatorSimpleTrade dataCreatorSimpleTrade = new DataCreatorSimpleTrade(pm, User.getUser(pm, getPrincipal()));
//			dataCreator.getRootSimpleProductType().getName().setText(languageID, displayName + " Wine Store");

			// create ProductTypes: wine (bottle)
			SimpleProductType wine = dataCreatorSimpleTrade.createCategory(null, wineID.productTypeID, 
					"Wine", "Wein", "Vin");
			SimpleProductType bottle = dataCreatorSimpleTrade.createCategory(wine, "bottle", 
					"Bottle", "Flasche", "Bouteille");
			SimpleProductType bottleRed = dataCreatorSimpleTrade.createCategory(bottle, "bottle-red", 
					"Red", "Rot", "Rouge");
			SimpleProductType bottleWhite = dataCreatorSimpleTrade.createCategory(bottle, "bottle-white", 
					"White", "Weiß", "Blanc");
			SimpleProductType bottleMerlot = dataCreatorSimpleTrade.createCategory(bottleRed, "bottle-merlot", 
			"Merlot");
			SimpleProductType bottleCabernetSauvignon = dataCreatorSimpleTrade.createCategory(bottleRed, "bottle-cabernet-sauvignon", 
			"Cabernet Sauvignon");
			SimpleProductType bottlePinotNoir = dataCreatorSimpleTrade.createCategory(bottleRed, "bottle-pinot-noir", 
			"Pinot Noir");

			SimpleProductType bottleMerlotAustralia = dataCreatorSimpleTrade.createCategory(bottleMerlot, "bottle-merlot-australia", 
					"Australia", "Australien", "Australie");
			SimpleProductType bottleMerlotFrance = dataCreatorSimpleTrade.createCategory(bottleMerlot, "bottle-merlot-france", 
					"France", "Frankreich", "France");
			SimpleProductType bottleMerlotCalifornia = dataCreatorSimpleTrade.createCategory(bottleMerlot, "bottle-merlot-california", 
					"California", "Kalifornien", "Californie");

			SimpleProductType bottleCabernetSauvignonFrance = dataCreatorSimpleTrade.createCategory(bottleCabernetSauvignon, "bottle-cabernet-sauvignon-france", 
					"France", "Frankreich", "France");
			SimpleProductType bottleCabernetSauvignonSouthAfrika = dataCreatorSimpleTrade.createCategory(bottleCabernetSauvignon, "bottle-cabernet-sauvignon-south-africa", 
					"South Africa", "Südafrika", "Afrique du Sud");

			IInnerPriceConfig priceConfigCheapWines = dataCreatorSimpleTrade.createFixPriceConfig(new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {399, 350}, 
					"Cheap Wines", "Billiger Wein", "Vin pas cher");
			IInnerPriceConfig priceConfigMiddleWines = dataCreatorSimpleTrade.createFixPriceConfig(new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {500, 420}, 
					"Middle Wines", "Mittlerer Wein", "Vin moyen");
			IInnerPriceConfig priceConfigExpensiveWines = dataCreatorSimpleTrade.createFixPriceConfig(new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {999, 830}, 
					"Expensive Wines", "Teurer Wein", "Vin cher");

			SimpleProductType bottleMerlotAustralia2001 = dataCreatorSimpleTrade.createLeaf(bottleMerlotAustralia, "bottle-merlot-australia-2001", priceConfigExpensiveWines, 
					"Merlot 2001 (Australia)", "Merlot 2001 (Australien)", "Merlot 2001 (Australie)");

			String contentTypeJpeg = "image/jpeg";

			dataCreatorSimpleTrade.createWineProperties(pm, bottleMerlotAustralia2001, "Merlot from Australia 2001", "Merlot aus Australien 2001", 
					"Merlot from the south of Australia (vintage 2001)", "Merlot aus dem Süden Australiens (Jahrgang 2001)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);
			SimpleProductType bottleMerlotAustralia2004 = dataCreatorSimpleTrade.createLeaf(bottleMerlotAustralia, "bottle-merlot-australia-2004", priceConfigCheapWines, 
					"Merlot 2004 (Australia)", "Merlot 2004 (Australien)", "Merlot 2004 (Australie)");
			dataCreatorSimpleTrade.createWineProperties(pm, bottleMerlotAustralia2004, "Merlot from Australia 2004", "Merlot aus Australien 2004", 
					"Merlot from the south of Australia (vintage 2004)", "Merlot aus dem Süden Australiens (Jahrgang 2004)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);
			SimpleProductType bottleMerlotFrance2001 = dataCreatorSimpleTrade.createLeaf(bottleMerlotFrance, "bottle-merlot-france-2001", priceConfigExpensiveWines, 
					"Merlot 2001 (France)", "Merlot 2001 (Frankreich)", "Merlot 2001 (France)");
			dataCreatorSimpleTrade.createWineProperties(pm, bottleMerlotFrance2001, "Merlot from France 2001", "Merlot aus Frankreich 2001", 
					"Merlot from the south of France (vintage 2001)", "Merlot aus dem Süden Frankreichs (Jahrgang 2001)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);
			SimpleProductType bottleMerlotCalifornia2003 = dataCreatorSimpleTrade.createLeaf(bottleMerlotCalifornia, "bottle-merlot-california-2003", priceConfigMiddleWines, 
					"Merlot 2003 (California)", "Merlot 2003 (Kalifornien)", "Merlot 2003 (Californie)");
			dataCreatorSimpleTrade.createWineProperties(pm, bottleMerlotCalifornia2003, "Merlot from California 2001", "Merlot aus Kalifornien 2001", 
					"Merlot from the south of Californias (vintage 2001)", "Merlot aus dem Süden Kaliforniens (Jahrgang 2001)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);

			SimpleProductType bottleCabernetSauvignonFrance2002 = dataCreatorSimpleTrade.createLeaf(bottleCabernetSauvignonFrance, "bottle-cabernet-sauvignon-france-2002", priceConfigMiddleWines, 
					"Cabernet Sauvignon 2002 (France)", "Cabernet Sauvignon 2002 (Frankreich)", "Cabernet Sauvignon 2002 (France)");
			dataCreatorSimpleTrade.createWineProperties(pm, bottleCabernetSauvignonFrance2002, "Cabernet Sauvignon from France 2002", "Cabernet Sauvignon aus Frankreich 2002", 
					"Cabernet Sauvignon from the south of France (vintage 2002)", "Merlot aus dem Süden Frankreichs (Jahrgang 2002)", "cabernet_small.jpg", contentTypeJpeg, "cabernet_large.jpg", contentTypeJpeg);
			SimpleProductType bottleCabernetSauvignonSouthAfrika2003 = dataCreatorSimpleTrade.createLeaf(bottleCabernetSauvignonSouthAfrika, "bottle-cabernet-sauvignon-south-africa-2003", priceConfigCheapWines, 
					"Cabernet Sauvignon 2003 (South Africa)", "Cabernet Sauvignon 2003 (Südafrika)", "Cabernet Sauvignon 2003 (Afrique du Sud)");
			dataCreatorSimpleTrade.createWineProperties(pm, bottleCabernetSauvignonSouthAfrika2003, "Cabernet Sauvignon from South Africa 2002", "Cabernet Sauvignon aus Südafrica 2002", 
					"Cabernet Sauvignon from the south of South Africa (vintage 2002)", "Merlot aus dem Süden Südafrikas (Jahrgang 2002)", "cabernet_small.jpg", contentTypeJpeg, "cabernet_large.jpg", contentTypeJpeg);

			// create ProductTypes: wine (box)
			SimpleProductType box = dataCreatorSimpleTrade.createCategory(wine, "box", 
					"Box", "Karton", "Caisse");
			SimpleProductType boxRed = dataCreatorSimpleTrade.createCategory(box, "box-red", 
					"Red", "Rot", "Rouge");
			SimpleProductType boxMerlot = dataCreatorSimpleTrade.createCategory(boxRed, "box-merlot", 
			"Merlot");

			SimpleProductType boxWhite = dataCreatorSimpleTrade.createCategory(box, "box-white", "White");

			IInnerPriceConfig priceConfigBox6Bottles90Percent = dataCreatorSimpleTrade.createFormulaPriceConfig(
					new Tariff[] {tariffNormalPrice, tariffGoldCard}, new String[] {
							"cell.resolvePriceCellsAmount(\n" +
							"	new Array(\n" +
							"		ProductTypeID.create(\""+bottle.getOrganisationID()+"\", \""+bottle.getProductTypeID()+"\")\n" +
							"	)\n" +
							") * -0.1;"
							,
							"cell.resolvePriceCellsAmount(\n" +
							"	new Array(\n" +
							"		ProductTypeID.create(\""+bottle.getOrganisationID()+"\", \""+bottle.getProductTypeID()+"\")\n" +
							"	)\n" +
							") * -0.1;"
					},
					"Box (6 bottles, 90%)", "Karton (6 Flaschen, 90%)", "Caisse (6 bouteilles, 90%)"
			);
			((FormulaPriceConfig)priceConfigBox6Bottles90Percent).addProductType(bottle);

			SimpleProductType boxMerlotAustralia = dataCreatorSimpleTrade.createCategory(boxMerlot, "box-merlot-australia", 
					"Australia", "Australien", "Australie");
			SimpleProductType boxMerlotFrance = dataCreatorSimpleTrade.createCategory(boxMerlot, "box-merlot-france", 
					"France", "Frankreich", "France");
			SimpleProductType boxMerlotCalifornia = dataCreatorSimpleTrade.createCategory(boxMerlot, "box-merlot-california", 
					"California", "Kalifornien", "Californie");

			SimpleProductType boxMerlotAustralia2001 = dataCreatorSimpleTrade.createLeaf(boxMerlotAustralia, "box-merlot-australia-2001", priceConfigBox6Bottles90Percent, 
					"Box (6): Merlot 2001 (Australia)", "Karton (6): Merlot 2001 (Australien)", "Caisse (6): Merlot 2001 (Australie)");
			dataCreatorSimpleTrade.createWineProperties(pm, boxMerlotAustralia2001, "Box (6): Merlot 2001 (Australia) from Australia", "Karton Merlo aus Australien", 
					"Box (6): Merlot 2001 from Australia", "Karton Merlot aus Australien", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
			SimpleProductType boxMerlotAustralia2004 = dataCreatorSimpleTrade.createLeaf(boxMerlotAustralia, "box-merlot-australia-2004", priceConfigBox6Bottles90Percent, 
					"Box (6): Merlot 2004 (Australia)", "Karton (6): Merlot 2004 (Australien)", "Caisse (6): Merlot 2004 (Australie)");
			dataCreatorSimpleTrade.createWineProperties(pm, boxMerlotAustralia2004, "Box (6): Merlot 2004 (Australia) from Australia", "Karton Merlo aus Australien", 
					"Box (6): Merlot 2001 from Australia", "Karton Merlot aus Australien", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
			SimpleProductType boxMerlotFrance2001 = dataCreatorSimpleTrade.createLeaf(boxMerlotFrance, "box-merlot-france-2001", priceConfigBox6Bottles90Percent, 
					"Box (6): Merlot 2001 (France)", "Karton (6): Merlot 2001 (Frankreich)", "Caisse (6): Merlot 2001 (France)");
			dataCreatorSimpleTrade.createWineProperties(pm, boxMerlotFrance2001, "Box (6): Merlot 2001 (France) from France", "Karton Merlo aus Frankreich", 
					"Box (6): Merlot 2001 from France", "Karton Merlot aus Frankreich", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
			SimpleProductType boxMerlotCalifornia2003 = dataCreatorSimpleTrade.createLeaf(boxMerlotCalifornia, "box-merlot-california-2003", priceConfigBox6Bottles90Percent, 
					"Box (6): Merlot 2003 (California)", "Karton (6): Merlot 2003 (Kalifornien)", "Caisse (6): Merlot 2003 (Californie)");
			dataCreatorSimpleTrade.createWineProperties(pm, boxMerlotAustralia2001, "Box (6): Merlot 2001 (Australia) from Australia", "Karton Merlo aus Australien", 
					"Box (6): Merlot 2001 (Australia) from Australia", "Karton Merlo aus Australien", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);

			boxMerlotAustralia2001.createNestedProductType(bottleMerlotAustralia2001).setQuantity(6);
			boxMerlotAustralia2004.createNestedProductType(bottleMerlotAustralia2004).setQuantity(6);
			boxMerlotFrance2001.createNestedProductType(bottleMerlotFrance2001).setQuantity(6);
			boxMerlotCalifornia2003.createNestedProductType(bottleMerlotCalifornia2003).setQuantity(6);

			boxMerlotAustralia2001.getFieldMetaData("nestedProductTypes").setValueInherited(false);
			boxMerlotAustralia2004.getFieldMetaData("nestedProductTypes").setValueInherited(false);
			boxMerlotFrance2001.getFieldMetaData("nestedProductTypes").setValueInherited(false);
			boxMerlotCalifornia2003.getFieldMetaData("nestedProductTypes").setValueInherited(false);

			// create ProductTypes: accessories
			IInnerPriceConfig priceConfigChocolate = dataCreatorSimpleTrade.createFixPriceConfig(
					new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {200, 150}, 
					"Chocolate", "Schokolade", "Chocolat");
			IInnerPriceConfig priceConfigCorkScrew = dataCreatorSimpleTrade.createFixPriceConfig(
					new Tariff[] {tariffNormalPrice, tariffGoldCard}, new long[] {600, 450}, 
					"Corkscrew", "Korkenzieher", "Tire-bouchon");

			SimpleProductType accessories = dataCreatorSimpleTrade.createCategory(null, "accessories", 
					"Accessories", "Zubehör", "Accessoires");
			SimpleProductType chocolate = dataCreatorSimpleTrade.createCategory(accessories, "chocolate", 
					"Chocolate", "Schokolade", "Chocolat");
			SimpleProductType sarotti = dataCreatorSimpleTrade.createLeaf(chocolate, "sarotti", priceConfigChocolate, 
			"Sarotti");
			SimpleProductType corkscrew = dataCreatorSimpleTrade.createCategory(accessories, "corkscrew", 
					"Corkscrew", "Korkenzieher", "Tire-bouchon");
			SimpleProductType corkscrewStainlessSteel = dataCreatorSimpleTrade.createLeaf(corkscrew, "corkscrew-stainless-steel", priceConfigCorkScrew, 
					"Corkscrew (stainless steel)", "Korkenzieher (Edelstahl)", "Tire-bouchon (inox)");

			dataCreatorSimpleTrade.calculatePrices();

			// create Accounts: Red Wine (bottle), White Wine (bottle), Red Wine (box), White Wine (box)
			Account accountBottleRedVatNet = dataCreatorSimpleTrade.createLocalAccount("bottle-red-vat-net.eur", "Bottle Red Net (EUR)");
			Account accountBottleRedVatVal = dataCreatorSimpleTrade.createLocalAccount("bottle-red-vat-val.eur", "Bottle Red VAT (EUR)");
			Account accountBottleWhiteVatNet = dataCreatorSimpleTrade.createLocalAccount("bottle-white-vat-net.eur", "Bottle White Net (EUR)");
			Account accountBottleWhiteVatVal = dataCreatorSimpleTrade.createLocalAccount("bottle-white-vat-val.eur", "Bottle White VAT (EUR)");

			Account accountBoxRedVatNet = dataCreatorSimpleTrade.createLocalAccount("box-red-vat-net.eur", "Box Red Net (EUR)");
			Account accountBoxRedVatVal = dataCreatorSimpleTrade.createLocalAccount("box-red-vat-val.eur", "Box Red VAT (EUR)");
			Account accountBoxWhiteVatNet = dataCreatorSimpleTrade.createLocalAccount("box-white-vat-net.eur", "Box White Net (EUR)");
			Account accountBoxWhiteVatVal = dataCreatorSimpleTrade.createLocalAccount("box-white-vat-val.eur", "Box White VAT (EUR)");

			// create Accounts: Accessories
			Account accessoriesVatNet = dataCreatorSimpleTrade.createLocalAccount("accessories-vat-net.eur", "Accessories Net (EUR)");
			Account accessoriesVatVal = dataCreatorSimpleTrade.createLocalAccount("accessories-vat-val.eur", "Accessories VAT (EUR)");

			// configure moneyflow
			LocalAccountantDelegate wineAccountantDelegate = new PFMappingAccountantDelegate(organisationID, "wineAccountantDelegate");
			wine.setLocalAccountantDelegate(wineAccountantDelegate);

			wineAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(bottleRed, dataCreatorSimpleTrade.getPriceFragmentTypeTotal(), accountBottleRedVatNet));
			wineAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(bottleRed, dataCreatorSimpleTrade.getPriceFragmentTypeVatVal(), accountBottleRedVatVal));

			wineAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(bottleWhite, dataCreatorSimpleTrade.getPriceFragmentTypeVatNet(), accountBottleWhiteVatNet));
			wineAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(bottleWhite, dataCreatorSimpleTrade.getPriceFragmentTypeVatVal(), accountBottleWhiteVatVal));

			wineAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(boxRed, dataCreatorSimpleTrade.getPriceFragmentTypeTotal(), accountBoxRedVatNet));
			wineAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(boxRed, dataCreatorSimpleTrade.getPriceFragmentTypeVatVal(), accountBoxRedVatVal));

			wineAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(boxWhite, dataCreatorSimpleTrade.getPriceFragmentTypeVatNet(), accountBoxWhiteVatNet));
			wineAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(boxWhite, dataCreatorSimpleTrade.getPriceFragmentTypeVatVal(), accountBoxWhiteVatVal));

			LocalAccountantDelegate accessoriesAccountantDelegate = new PFMappingAccountantDelegate(organisationID, "accessoriesAccountantDelegate");
			accessories.setLocalAccountantDelegate(accessoriesAccountantDelegate);

			accessoriesAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(accessories, dataCreatorSimpleTrade.getPriceFragmentTypeVatNet(), accessoriesVatNet));
			accessoriesAccountantDelegate.addMoneyFlowMapping(
					dataCreatorSimpleTrade.createPFMoneyFlowMapping(accessories, dataCreatorSimpleTrade.getPriceFragmentTypeVatVal(), accessoriesVatVal));


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

			Trader trader = Trader.getTrader(pm);
			CustomerGroup customerGroupDefault = trader.getDefaultCustomerGroupForKnownCustomer();

			// create some more users
			User user00 = dataCreatorSimpleTrade.createUser("user00", "test", "Chez Francois", "Miller", "Adam", "adam.miller@chezfrancois.co.th");
			LegalEntity legalEntity00 = dataCreatorSimpleTrade.createLegalEntity(user00.getPerson());
			legalEntity00.setDefaultCustomerGroup(customerGroupDefault);
			User user01 = dataCreatorSimpleTrade.createUser("user01", "test", "Chez Francois", "Miller", "Eva", "eva.miller@chezfrancois.co.th");
			LegalEntity legalEntity01 = dataCreatorSimpleTrade.createLegalEntity(user01.getPerson());
			legalEntity01.setDefaultCustomerGroup(customerGroupDefault);
			User user03 = dataCreatorSimpleTrade.createUser("alex", "test", "NightLabs GmbH", "Bieber", "Alex", "alex@nightlabs.de");
			LegalEntity legalEntity03 = dataCreatorSimpleTrade.createLegalEntity(user03.getPerson());
			legalEntity03.setDefaultCustomerGroup(customerGroupDefault);

			Person person = dataCreatorSimpleTrade.createPerson("NightLabs GmbH", "Marco", "Schulze", "marco@nightlabs.de", new Date(), 
					PersonStruct.PERSONALDATA_SALUTATION_MR, "Dr.", "Teststrasse", "79100", "Freiburg", "Baden-Württemberg", "Deutschland",
					"49", "761", "123456789", "49", "761", "987654321", "Marco Schulze", 123456789, "68090000", "TestBank", 
					"Marco Schulze", "1234567890", 
					01, 2008, "Comment for Marco Schulze");
			User user02 = dataCreatorSimpleTrade.createUser("marco", "test", person);				

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

			dataCreatorSimpleTrade.createOrderForEndcustomer(LegalEntity.getAnonymousCustomer(pm));
			dataCreatorSimpleTrade.createOrderForEndcustomer(LegalEntity.getAnonymousCustomer(pm));
			dataCreatorSimpleTrade.createOrderForEndcustomer(legalEntity00);
			dataCreatorSimpleTrade.createOrderForEndcustomer(LegalEntity.getAnonymousCustomer(pm));
			dataCreatorSimpleTrade.createOrderForEndcustomer(legalEntity00);

			logger.info("Initialization of JFireChezFrancois complete!");


			logger.info("Initializing JDO for Article.class...");
			pm.getExtent(Article.class);
			logger.info("Initializing JDO for Article.class complete!");

		} finally {
			pm.close();
		}
	}

	/**
	 * This is a demo task doing nothing.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public void demoTimerTask(TaskID taskID) 
	{
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");

		logger.info("demoTimerTask: entered for taskID " + taskID);
		logger.info("demoTimerTask: sleeping 5 sec");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ignore) { }
		logger.info("demoTimerTask: about to exit for taskID " + taskID);

		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
		logger.info("***************************************************************************************************************");
	}

}
