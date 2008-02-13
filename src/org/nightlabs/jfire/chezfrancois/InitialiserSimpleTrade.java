package org.nightlabs.jfire.chezfrancois;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.config.Config;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.book.mappingbased.PFMappingAccountantDelegate;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.base.JFirePrincipal;
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
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.OrganisationConfigModule;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Trader;

public class InitialiserSimpleTrade
extends Initialiser
{
	private static final Logger logger = Logger.getLogger(InitialiserSimpleTrade.class);

	public InitialiserSimpleTrade(PersistenceManager pm, JFirePrincipal principal)
	{
		super(pm, principal);
	}

	public void createDemoData()
	throws ModuleException, CannotPublishProductTypeException, CannotConfirmProductTypeException, CannotMakeProductTypeSaleableException
	{
		String organisationID = getOrganisationID();

		pm.getExtent(SimpleProductType.class);
		ProductTypeID wineID = ProductTypeID.create(organisationID, "wine");
		try {
			pm.getObjectById(wineID);
			return; // it exists => do nothing
		} catch (JDOObjectNotFoundException x) {
			// ignore this exception and create demo date
		}

		DataCreatorSimpleTrade dataCreator = new DataCreatorSimpleTrade(pm, User.getUser(pm, getPrincipal()));
//		dataCreator.getRootSimpleProductType().getName().setText(languageID, displayName + " Wine Store");

		// create Tariffs: normal price, gold card
		Tariff tariffNormalPrice = dataCreator.getTariffNormalPrice();
		Tariff tariffGoldCard = dataCreator.getTariffGoldCard();


		// create ProductTypes: wine (bottle)
		SimpleProductType wine = dataCreator.createCategory(null, wineID.productTypeID,
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
		
		// FIXME
		@SuppressWarnings("unused")
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

		String contentTypeJpeg = "image/jpeg";

		dataCreator.createWineProperties(pm, bottleMerlotAustralia2001, "Merlot from Australia 2001", "Merlot aus Australien 2001",
				"Merlot from the south of Australia (vintage 2001)", "Merlot aus dem Süden Australiens (Jahrgang 2001)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);
		SimpleProductType bottleMerlotAustralia2004 = dataCreator.createLeaf(bottleMerlotAustralia, "bottle-merlot-australia-2004", priceConfigCheapWines,
				"Merlot 2004 (Australia)", "Merlot 2004 (Australien)", "Merlot 2004 (Australie)");
		dataCreator.createWineProperties(pm, bottleMerlotAustralia2004, "Merlot from Australia 2004", "Merlot aus Australien 2004",
				"Merlot from the south of Australia (vintage 2004)", "Merlot aus dem Süden Australiens (Jahrgang 2004)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);
		SimpleProductType bottleMerlotFrance2001 = dataCreator.createLeaf(bottleMerlotFrance, "bottle-merlot-france-2001", priceConfigExpensiveWines,
				"Merlot 2001 (France)", "Merlot 2001 (Frankreich)", "Merlot 2001 (France)");
		dataCreator.createWineProperties(pm, bottleMerlotFrance2001, "Merlot from France 2001", "Merlot aus Frankreich 2001",
				"Merlot from the south of France (vintage 2001)", "Merlot aus dem Süden Frankreichs (Jahrgang 2001)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);
		SimpleProductType bottleMerlotCalifornia2003 = dataCreator.createLeaf(bottleMerlotCalifornia, "bottle-merlot-california-2003", priceConfigMiddleWines,
				"Merlot 2003 (California)", "Merlot 2003 (Kalifornien)", "Merlot 2003 (Californie)");
		dataCreator.createWineProperties(pm, bottleMerlotCalifornia2003, "Merlot from California 2001", "Merlot aus Kalifornien 2001",
				"Merlot from the south of Californias (vintage 2001)", "Merlot aus dem Süden Kaliforniens (Jahrgang 2001)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);

		SimpleProductType bottleCabernetSauvignonFrance2002 = dataCreator.createLeaf(bottleCabernetSauvignonFrance, "bottle-cabernet-sauvignon-france-2002", priceConfigMiddleWines,
				"Cabernet Sauvignon 2002 (France)", "Cabernet Sauvignon 2002 (Frankreich)", "Cabernet Sauvignon 2002 (France)");
		dataCreator.createWineProperties(pm, bottleCabernetSauvignonFrance2002, "Cabernet Sauvignon from France 2002", "Cabernet Sauvignon aus Frankreich 2002",
				"Cabernet Sauvignon from the south of France (vintage 2002)", "Merlot aus dem Süden Frankreichs (Jahrgang 2002)", "cabernet_small.jpg", contentTypeJpeg, "cabernet_large.jpg", contentTypeJpeg);
		SimpleProductType bottleCabernetSauvignonSouthAfrika2003 = dataCreator.createLeaf(bottleCabernetSauvignonSouthAfrika, "bottle-cabernet-sauvignon-south-africa-2003", priceConfigCheapWines,
				"Cabernet Sauvignon 2003 (South Africa)", "Cabernet Sauvignon 2003 (Südafrika)", "Cabernet Sauvignon 2003 (Afrique du Sud)");
		dataCreator.createWineProperties(pm, bottleCabernetSauvignonSouthAfrika2003, "Cabernet Sauvignon from South Africa 2002", "Cabernet Sauvignon aus Südafrica 2002",
				"Cabernet Sauvignon from the south of South Africa (vintage 2002)", "Merlot aus dem Süden Südafrikas (Jahrgang 2002)", "cabernet_small.jpg", contentTypeJpeg, "cabernet_large.jpg", contentTypeJpeg);

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

		SimpleProductType boxMerlotAustralia = dataCreator.createCategory(boxMerlot, "box-merlot-australia",
				"Australia", "Australien", "Australie");
		SimpleProductType boxMerlotFrance = dataCreator.createCategory(boxMerlot, "box-merlot-france",
				"France", "Frankreich", "France");
		SimpleProductType boxMerlotCalifornia = dataCreator.createCategory(boxMerlot, "box-merlot-california",
				"California", "Kalifornien", "Californie");

		SimpleProductType boxMerlotAustralia2001 = dataCreator.createLeaf(boxMerlotAustralia, "box-merlot-australia-2001", priceConfigBox6Bottles90Percent,
				"Box (6): Merlot 2001 (Australia)", "Karton (6): Merlot 2001 (Australien)", "Caisse (6): Merlot 2001 (Australie)");
		dataCreator.createWineProperties(pm, boxMerlotAustralia2001, "Box (6): Merlot 2001 (Australia) from Australia", "Karton Merlot aus Australien",
				"Box (6): Merlot 2001 from Australia", "Karton Merlot aus Australien", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
		SimpleProductType boxMerlotAustralia2004 = dataCreator.createLeaf(boxMerlotAustralia, "box-merlot-australia-2004", priceConfigBox6Bottles90Percent,
				"Box (6): Merlot 2004 (Australia)", "Karton (6): Merlot 2004 (Australien)", "Caisse (6): Merlot 2004 (Australie)");
		dataCreator.createWineProperties(pm, boxMerlotAustralia2004, "Box (6): Merlot 2004 (Australia) from Australia", "Karton Merlot aus Australien",
				"Box (6): Merlot 2001 from Australia", "Karton Merlot aus Australien", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
		SimpleProductType boxMerlotFrance2001 = dataCreator.createLeaf(boxMerlotFrance, "box-merlot-france-2001", priceConfigBox6Bottles90Percent,
				"Box (6): Merlot 2001 (France)", "Karton (6): Merlot 2001 (Frankreich)", "Caisse (6): Merlot 2001 (France)");
		dataCreator.createWineProperties(pm, boxMerlotFrance2001, "Box (6): Merlot 2001 (France) from France", "Karton Merlot aus Frankreich",
				"Box (6): Merlot 2001 from France", "Karton Merlot aus Frankreich", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
		SimpleProductType boxMerlotCalifornia2003 = dataCreator.createLeaf(boxMerlotCalifornia, "box-merlot-california-2003", priceConfigBox6Bottles90Percent,
				"Box (6): Merlot 2003 (California)", "Karton (6): Merlot 2003 (Kalifornien)", "Caisse (6): Merlot 2003 (Californie)");
		dataCreator.createWineProperties(pm, boxMerlotAustralia2001, "Box (6): Merlot 2001 (Australia) from Australia", "Karton Merlot aus Australien",
				"Box (6): Merlot 2001 (Australia) from Australia", "Karton Merlot aus Australien", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);

		boxMerlotAustralia2001.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotAustralia2001.getProductTypeLocal()).setQuantity(6);
		boxMerlotAustralia2004.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotAustralia2004.getProductTypeLocal()).setQuantity(6);
		boxMerlotFrance2001.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotFrance2001.getProductTypeLocal()).setQuantity(6);
		boxMerlotCalifornia2003.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotCalifornia2003.getProductTypeLocal()).setQuantity(6);

		boxMerlotAustralia2001.getProductTypeLocal().getFieldMetaData("nestedProductTypeLocals").setValueInherited(false);
		boxMerlotAustralia2004.getProductTypeLocal().getFieldMetaData("nestedProductTypeLocals").setValueInherited(false);
		boxMerlotFrance2001.getProductTypeLocal().getFieldMetaData("nestedProductTypeLocals").setValueInherited(false);
		boxMerlotCalifornia2003.getProductTypeLocal().getFieldMetaData("nestedProductTypeLocals").setValueInherited(false);

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
		
		// FIXME
		@SuppressWarnings("unused")
		SimpleProductType sarotti = dataCreator.createLeaf(chocolate, "sarotti", priceConfigChocolate,
		"Sarotti");
		SimpleProductType corkscrew = dataCreator.createCategory(accessories, "corkscrew",
				"Corkscrew", "Korkenzieher", "Tire-bouchon");
		
		// FIXME
		@SuppressWarnings("unused")
		SimpleProductType corkscrewStainlessSteel = dataCreator.createLeaf(corkscrew, "corkscrew-stainless-steel", priceConfigCorkScrew,
				"Corkscrew (stainless steel)", "Korkenzieher (Edelstahl)", "Tire-bouchon (inox)");

		dataCreator.calculatePrices();

		// create Accounts: Red Wine (bottle), White Wine (bottle), Red Wine (box), White Wine (box)
		Account accountBottleRedVatNet_revenue = dataCreator.createLocalRevenueAccount("bottle-red-vat-net.eur", "Bottle Red Net Revenue (EUR)");
		Account accountBottleRedVatVal_revenue = dataCreator.createLocalRevenueAccount("bottle-red-vat-val.eur", "Bottle Red VAT Revenue (EUR)");
		Account accountBottleWhiteVatNet_revenue = dataCreator.createLocalRevenueAccount("bottle-white-vat-net.eur", "Bottle White Net Revenue (EUR)");
		Account accountBottleWhiteVatVal_revenue = dataCreator.createLocalRevenueAccount("bottle-white-vat-val.eur", "Bottle White VAT Revenue (EUR)");

		Account accountBoxRedVatNet_revenue = dataCreator.createLocalRevenueAccount("box-red-vat-net.eur", "Box Red Net Revenue (EUR)");
		Account accountBoxRedVatVal_revenue = dataCreator.createLocalRevenueAccount("box-red-vat-val.eur", "Box Red VAT Revenue (EUR)");
		Account accountBoxWhiteVatNet_revenue = dataCreator.createLocalRevenueAccount("box-white-vat-net.eur", "Box White Net Revenue (EUR)");
		Account accountBoxWhiteVatVal_revenue = dataCreator.createLocalRevenueAccount("box-white-vat-val.eur", "Box White VAT Revenue (EUR)");

		Account accountBottleRedVatNet_expense = dataCreator.createLocalExpenseAccount("bottle-red-vat-net.eur", "Bottle Red Net Expense (EUR)");
		Account accountBottleRedVatVal_expense = dataCreator.createLocalExpenseAccount("bottle-red-vat-val.eur", "Bottle Red VAT Expense (EUR)");
		Account accountBottleWhiteVatNet_expense = dataCreator.createLocalExpenseAccount("bottle-white-vat-net.eur", "Bottle White Net Expense (EUR)");
		Account accountBottleWhiteVatVal_expense = dataCreator.createLocalExpenseAccount("bottle-white-vat-val.eur", "Bottle White VAT Expense (EUR)");

		Account accountBoxRedVatNet_expense = dataCreator.createLocalExpenseAccount("box-red-vat-net.eur", "Box Red Net Expense (EUR)");
		Account accountBoxRedVatVal_expense = dataCreator.createLocalExpenseAccount("box-red-vat-val.eur", "Box Red VAT Expense (EUR)");
		Account accountBoxWhiteVatNet_expense = dataCreator.createLocalExpenseAccount("box-white-vat-net.eur", "Box White Net Expense (EUR)");
		Account accountBoxWhiteVatVal_expense = dataCreator.createLocalExpenseAccount("box-white-vat-val.eur", "Box White VAT Expense (EUR)");


		// create Accounts: Accessories
		Account accessoriesVatNet_revenue = dataCreator.createLocalRevenueAccount("accessories-vat-net.eur", "Accessories Net Revenue (EUR)");
		Account accessoriesVatVal_revenue = dataCreator.createLocalRevenueAccount("accessories-vat-val.eur", "Accessories VAT Revenue (EUR)");

		Account accessoriesVatNet_expense = dataCreator.createLocalExpenseAccount("accessories-vat-net.eur", "Accessories Net Expense (EUR)");
		Account accessoriesVatVal_expense = dataCreator.createLocalExpenseAccount("accessories-vat-val.eur", "Accessories VAT Expense (EUR)");

		// configure moneyflow
		PFMappingAccountantDelegate wineAccountantDelegate = new PFMappingAccountantDelegate(organisationID, "wineAccountantDelegate");
		wine.getProductTypeLocal().getFieldMetaData("localAccountantDelegate").setValueInherited(false);
		wine.getProductTypeLocal().setLocalAccountantDelegate(wineAccountantDelegate);

		wineAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(bottleRed, dataCreator.getPriceFragmentTypeTotal(),
						accountBottleRedVatNet_revenue, accountBottleRedVatNet_expense));
		wineAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(bottleRed, dataCreator.getPriceFragmentTypeVatVal(),
						accountBottleRedVatVal_revenue, accountBottleRedVatVal_expense));

		wineAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(bottleWhite, dataCreator.getPriceFragmentTypeVatNet(),
						accountBottleWhiteVatNet_revenue, accountBottleWhiteVatNet_expense));
		wineAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(bottleWhite, dataCreator.getPriceFragmentTypeVatVal(),
						accountBottleWhiteVatVal_revenue, accountBottleWhiteVatVal_expense));

		wineAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(boxRed, dataCreator.getPriceFragmentTypeTotal(),
						accountBoxRedVatNet_revenue, accountBoxRedVatNet_expense));
		wineAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(boxRed, dataCreator.getPriceFragmentTypeVatVal(),
						accountBoxRedVatVal_revenue, accountBoxRedVatVal_expense));

		wineAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(boxWhite, dataCreator.getPriceFragmentTypeVatNet(),
						accountBoxWhiteVatNet_revenue, accountBoxWhiteVatNet_expense));
		wineAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(boxWhite, dataCreator.getPriceFragmentTypeVatVal(),
						accountBoxWhiteVatVal_revenue, accountBoxWhiteVatVal_expense));

		PFMappingAccountantDelegate accessoriesAccountantDelegate = new PFMappingAccountantDelegate(organisationID, "accessoriesAccountantDelegate");
		accessories.getProductTypeLocal().getFieldMetaData("localAccountantDelegate").setValueInherited(false);
		accessories.getProductTypeLocal().setLocalAccountantDelegate(accessoriesAccountantDelegate);

		accessoriesAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(accessories, dataCreator.getPriceFragmentTypeVatNet(),
						accessoriesVatNet_revenue, accessoriesVatNet_expense));
		accessoriesAccountantDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(accessories, dataCreator.getPriceFragmentTypeVatVal(),
						accessoriesVatVal_revenue, accessoriesVatVal_expense));


		// apply inheritance (e.g. because of LocalAccountantDelegates)
		wine.applyInheritance();
		accessories.applyInheritance();

		logger.info("Chezfrancois Created all SimpleProductTypes; publishing, confirming and making available now!");
		dataCreator.makeAllLeavesSaleable();
		
		
		UserGroup userGroup = new UserGroup(organisationID, User.USERID_PREFIX_TYPE_USERGROUP + "SalesAgents");
		userGroup.setName("Sales Agents");
		userGroup.setDescription("This group is blablabla.");
		new UserLocal(userGroup);
		userGroup = pm.makePersistent(userGroup);

		userGroup = new UserGroup(organisationID, User.USERID_PREFIX_TYPE_USERGROUP + "SalesManagers");
		userGroup.setName("Sales Managers");
		userGroup.setDescription("This group is blablabla.");
		new UserLocal(userGroup);
		userGroup = pm.makePersistent(userGroup);

		userGroup = new UserGroup(organisationID, User.USERID_PREFIX_TYPE_USERGROUP + "Statistics");
		userGroup.setName("Statistics");
		userGroup.setDescription("This group consists out of the statistics guys.");
		new UserLocal(userGroup);
		userGroup = pm.makePersistent(userGroup);

		userGroup = new UserGroup(organisationID, User.USERID_PREFIX_TYPE_USERGROUP + "TheOthers");
		userGroup.setName("The Others");
		userGroup.setDescription("This group is trallali trallala.");
		new UserLocal(userGroup);
		userGroup = pm.makePersistent(userGroup);

		Trader trader = Trader.getTrader(pm);
		CustomerGroup customerGroupDefault = trader.getDefaultCustomerGroupForKnownCustomer();

		OrganisationConfigModule cfMod = Config.sharedInstance().createConfigModule(OrganisationConfigModule.class);
		for (Iterator<OrganisationCf> iter = cfMod.getOrganisations().iterator(); iter.hasNext();) {
			OrganisationCf orgCf = iter.next();
			if (!orgCf.getOrganisationID().equals(getOrganisationID()))
				continue;
			Set<String> adminIDs = orgCf.getServerAdmins();
			for (String adminID	: adminIDs) {
				// FIXME
				@SuppressWarnings("unused")
				User admin = User.getUser(pm, orgCf.getOrganisationID(), adminID);
			}
		}
		
		// create some more users
		User user00 = dataCreator.createUser("user00", "test", "Chez Francois", "Miller", "Adam", "adam.miller@chezfrancois.co.th");
		LegalEntity legalEntity00 = dataCreator.createLegalEntity(user00.getPerson());
		legalEntity00.setDefaultCustomerGroup(customerGroupDefault);
		User user01 = dataCreator.createUser("user01", "test", "Chez Francois", "Miller", "Eva", "eva.miller@chezfrancois.co.th");
		LegalEntity legalEntity01 = dataCreator.createLegalEntity(user01.getPerson());
		legalEntity01.setDefaultCustomerGroup(customerGroupDefault);
		User user03 = dataCreator.createUser("alex", "test", "NightLabs GmbH", "Bieber", "Alex", "alex@nightlabs.de");
		LegalEntity legalEntity03 = dataCreator.createLegalEntity(user03.getPerson());
		legalEntity03.setDefaultCustomerGroup(customerGroupDefault);

		Person person = dataCreator.createPerson("NightLabs GmbH", "Marco", "Schulze", "marco@nightlabs.de", new Date(),
				PersonStruct.PERSONALDATA_SALUTATION_MR, "Dr.", "Teststrasse", "79100", "Freiburg", "Baden-Württemberg", "Deutschland",
				"49", "761", "123456789", "49", "761", "987654321", "Marco Schulze", 123456789, "68090000", "TestBank",
				"Marco Schulze", "1234567890",
				01, 2008, "Comment for Marco Schulze");
		User user02 = dataCreator.createUser("marco", "test", person);

		userGroup = new UserGroup(organisationID, User.USERID_PREFIX_TYPE_USERGROUP + "Administrators");
		userGroup.setName("Administrators");
		userGroup.setDescription("This group has all access rights within its organisation.");
		new UserLocal(userGroup);
		userGroup = pm.makePersistent(userGroup);
		userGroup.addUser(user00);
		userGroup.addUser(user01);
		userGroup.addUser(user02);
		userGroup.addUser(user03);

		Authority authority = (Authority) pm.getObjectById(AuthorityID.create(organisationID, Authority.AUTHORITY_ID_ORGANISATION));

		UserGroupRef userGroupRef = (UserGroupRef) authority.createUserRef(userGroup);
		authority.createUserRef(user00);
		authority.createUserRef(user01);

		for (Iterator<RoleGroup> it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
			RoleGroup roleGroup = it.next();
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
	}

}
