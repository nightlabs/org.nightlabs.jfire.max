package org.nightlabs.jfire.chezfrancois;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.config.Config;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.book.mappingbased.MappingBasedAccountantDelegate;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.chezfrancois.resource.Messages;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldValueNotFoundException;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.UserSecurityGroupRef;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.OrganisationConfigModule;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
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
	throws CannotPublishProductTypeException, CannotConfirmProductTypeException, CannotMakeProductTypeSaleableException, DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException, SecurityException, PriceCalculationException, StructFieldValueNotFoundException, StructFieldNotFoundException, StructBlockNotFoundException
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
		SimpleProductType bottleCarmenere = dataCreator.createCategory(bottleRed, "bottle-carmenere",
		"Carmenere");
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
		SimpleProductType bottleMerlotItaly = dataCreator.createCategory(bottleMerlot, "bottle-merlot-italy",
				"Italy", "Italien", "Italie");
		SimpleProductType bottleMerlotChile = dataCreator.createCategory(bottleMerlot, "bottle-merlot-chile",
				"Chile", "Chile", "Chile");
		SimpleProductType bottCarmenereChile = dataCreator.createCategory(bottleCarmenere, "bottle-carmenere-chile",
				"Chile", "Chile", "Chile");

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
		SimpleProductType bottleMerlotItaly2001 = dataCreator.createLeaf(bottleMerlotItaly, "bottle-merlot-italy-2001", priceConfigCheapWines,
				"Merlot 2001 (Italy)", "Merlot 2001 (Italien)", "Merlot 2001 (Italy)");
		dataCreator.createWineProperties(pm, bottleMerlotItaly2001, "Merlot from California 2001", "Merlot aus Kalifornien 2001",
				"Merlot from the south of Italy (vintage 2001)", "Merlot aus dem Süden Kaliforniens (Jahrgang 2001)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);
		SimpleProductType bottleMerlotChile2003 = dataCreator.createLeaf(bottleMerlotChile, "bottle-merlot-chile-2003", priceConfigCheapWines,
				"Merlot 2003 (Chile)", "Merlot 2003 (Chile)", "Merlot 2003 (Chile)");
		dataCreator.createWineProperties(pm, bottleMerlotChile2003, "Merlot from Chile 2003", "Merlot aus Chile 2003",
				"Merlot from the south of Chile (vintage 2003)", "Merlot aus dem Süden Chilenisch (Jahrgang 2003)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);
		SimpleProductType bottleCarmenereChile2000 = dataCreator.createLeaf(bottCarmenereChile, "bottle-carmenere-chile-2000", priceConfigMiddleWines,
				"Carmenere 2000 (Chile)", "Carmenere 2000 (Chile)", "Carmenere 2003 (Chile)");
		dataCreator.createWineProperties(pm, bottleCarmenereChile2000, "Carmenere from Chile 2000", "Carmenere aus Chile 2000",
				"Carmenere from the south of Chile (vintage 2000)", "Carmenere aus dem Süden Chilenisch (Jahrgang 2000)", "merlot_small.jpg", contentTypeJpeg, "merlot_large.jpg", contentTypeJpeg);

		
		

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
		SimpleProductType boxCarmenere = dataCreator.createCategory(boxRed, "box-carmenere",
		"Carmenere");
		
		SimpleProductType boxWhite = dataCreator.createCategory(box, "box-white", "White");

		IInnerPriceConfig priceConfigBox6Bottles90Percent = dataCreator.createFormulaPriceConfig(
				new Tariff[] {tariffNormalPrice, tariffGoldCard}, new String[] {
						"cell.resolvePriceCellsAmount(\n" +
						"	ProductTypeID.create(\""+bottle.getOrganisationID()+"\", \""+bottle.getProductTypeID()+"\")\n" +
						") * -0.1;"
						,
						"cell.resolvePriceCellsAmount(\n" +
						"	ProductTypeID.create(\""+bottle.getOrganisationID()+"\", \""+bottle.getProductTypeID()+"\")\n" +
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
		SimpleProductType boxMerlotItaly = dataCreator.createCategory(boxMerlot, "box-merlot-italy",
				"Italy", "Italien", "Italie");
		SimpleProductType boxMerlotChile = dataCreator.createCategory(boxMerlot, "box-merlot-chile",
				"Chile", "Chile", "Chile");
		SimpleProductType boxCarmenereChile = dataCreator.createCategory(boxCarmenere, "box-carmenere-chile",
				"Chile", "Chile", "Chile");
		
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

		SimpleProductType boxMerlotItaly2001 = dataCreator.createLeaf(boxMerlotItaly, "box-merlot-italy-2001", priceConfigBox6Bottles90Percent,
				"Box (6): Merlot 2001 (Italy)", "Karton (6): Merlot 2001 (Italien)", "Caisse (6): Merlot 2001 (Italie)");
		dataCreator.createWineProperties(pm, boxMerlotItaly2001, "Box (6): Merlot 2001 (Australia) from Italy", "Karton Merlot aus Italien",
				"Box (6): Merlot 2001 (Italy) from Italy", "Karton Merlot aus Italien", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
		// these products is used in Unit Testing 
		SimpleProductType boxMerlotChile2003 = dataCreator.createLeaf(boxMerlotChile, "box-merlot-chile-2003", priceConfigBox6Bottles90Percent,
				"Box (6): Merlot 2003 Chile)", "Karton (6): Merlot 2003 (Chile)", "Caisse (6): Merlot 2001 (Chile)");
		dataCreator.createWineProperties(pm, boxMerlotChile2003, "Box (6): Merlot 2001 (Chile) from Chile", "Karton Merlot aus Chile",
				"Box (6): Merlot 2003 (Chile) from Chile", "Karton Merlot aus chile", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
		SimpleProductType boxCarmenereChile2000 = dataCreator.createLeaf(boxCarmenereChile, "box-carmenere-chile-2000", priceConfigBox6Bottles90Percent,
				"Box (6): Carmenere 2000 Chile)", "Karton (6): Merlot 2003 (Chile)", "Caisse (6): Carmenere 2000 (Chile)");
		dataCreator.createWineProperties(pm, boxMerlotChile2003, "Box (6): Carmenere 2000 (Chile) from Chile", "Karton Carmenere aus Chilenisch",
				"Box (6): Carmenere 2000 (Chile) from Chile", "Karton Carmenere aus Chilenisch", "merlot_box_small.jpg", contentTypeJpeg, "merlot_box_large.jpg", contentTypeJpeg);
		
		

		boxMerlotAustralia2001.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotAustralia2001.getProductTypeLocal()).setQuantity(6);
		boxMerlotAustralia2004.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotAustralia2004.getProductTypeLocal()).setQuantity(6);
		boxMerlotFrance2001.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotFrance2001.getProductTypeLocal()).setQuantity(6);
		boxMerlotCalifornia2003.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotCalifornia2003.getProductTypeLocal()).setQuantity(6);
		boxMerlotItaly2001.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotItaly2001.getProductTypeLocal()).setQuantity(6);
		boxMerlotChile2003.getProductTypeLocal().createNestedProductTypeLocal(bottleMerlotChile2003.getProductTypeLocal()).setQuantity(6);
		boxCarmenereChile2000.getProductTypeLocal().createNestedProductTypeLocal(bottleCarmenereChile2000.getProductTypeLocal()).setQuantity(6);

		boxMerlotAustralia2001.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.nestedProductTypeLocals).setValueInherited(false);
		boxMerlotAustralia2004.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.nestedProductTypeLocals).setValueInherited(false);
		boxMerlotFrance2001.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.nestedProductTypeLocals).setValueInherited(false);
		boxMerlotCalifornia2003.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.nestedProductTypeLocals).setValueInherited(false);
		boxMerlotItaly2001.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.nestedProductTypeLocals).setValueInherited(false);
		boxMerlotChile2003.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.nestedProductTypeLocals).setValueInherited(false);
		boxCarmenereChile2000.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.nestedProductTypeLocals).setValueInherited(false);

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
		MappingBasedAccountantDelegate wineAccountantDelegate = new MappingBasedAccountantDelegate(organisationID, "wineAccountantDelegate");
		wine.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.localAccountantDelegate).setValueInherited(false);
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

		MappingBasedAccountantDelegate accessoriesAccountantDelegate = new MappingBasedAccountantDelegate(organisationID, "accessoriesAccountantDelegate");
		accessories.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.localAccountantDelegate).setValueInherited(false);
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

		// create UserSecurity Groups
		UserSecurityGroup userSecurityGroup = new UserSecurityGroup(organisationID, "SalesAgents");
		userSecurityGroup.setName("Sales Agents");
		userSecurityGroup.setDescription("This is the sales agents group");
		userSecurityGroup = pm.makePersistent(userSecurityGroup);

		userSecurityGroup = new UserSecurityGroup(organisationID, "SalesManagers");
		userSecurityGroup.setName("Sales Managers");
		userSecurityGroup.setDescription("This is the sales managers group");
		userSecurityGroup = pm.makePersistent(userSecurityGroup);

		userSecurityGroup = new UserSecurityGroup(organisationID, "Statistics");
		userSecurityGroup.setName("Statistics");
		userSecurityGroup.setDescription("This group consists out of the statistics guys.");
		userSecurityGroup = pm.makePersistent(userSecurityGroup);

		userSecurityGroup = new UserSecurityGroup(organisationID, "TheOthers");
		userSecurityGroup.setName("The Others");
		userSecurityGroup.setDescription("This group represents all users which don't fit in any other group");
		userSecurityGroup = pm.makePersistent(userSecurityGroup);

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

//		// create some more users --> See below for more detailed users. Kai.
//		User user00 = dataCreator.createUser("user00", "test", "Chez Francois", "Miller", "Adam", "adam.miller@chezfrancois.co.th");
//		LegalEntity legalEntity00 = dataCreator.createLegalEntity(user00.getPerson());
//		legalEntity00.setDefaultCustomerGroup(customerGroupDefault);
//		User user01 = dataCreator.createUser("user01", "test", "Chez Francois", "Miller", "Eva", "eva.miller@chezfrancois.co.th");
//		LegalEntity legalEntity01 = dataCreator.createLegalEntity(user01.getPerson());
//		legalEntity01.setDefaultCustomerGroup(customerGroupDefault);
//		User user03 = dataCreator.createUser("alex", "test", "NightLabs GmbH", "Bieber", "Alex", "alex@nightlabs.de");
//		LegalEntity legalEntity03 = dataCreator.createLegalEntity(user03.getPerson());
//		legalEntity03.setDefaultCustomerGroup(customerGroupDefault);


		LegalEntity vendor  = dataCreator.createVendor1();

		// add simple products demo for a demo vendor to test the purchase
		boxMerlotItaly2001.setVendor(vendor);
		boxMerlotItaly2001.setOwner(vendor);
		boxMerlotItaly2001.getFieldMetaData(ProductType.FieldName.vendor).setValueInherited(false);
		boxMerlotItaly2001.getFieldMetaData(ProductType.FieldName.owner).setValueInherited(false);

		bottleMerlotItaly2001.setVendor(vendor);
		bottleMerlotItaly2001.setOwner(vendor);
		bottleMerlotItaly2001.getFieldMetaData(ProductType.FieldName.vendor).setValueInherited(false);
		bottleMerlotItaly2001.getFieldMetaData(ProductType.FieldName.owner).setValueInherited(false);

		boxCarmenereChile2000.setVendor(vendor);
		boxCarmenereChile2000.setOwner(vendor);
		boxCarmenereChile2000.getFieldMetaData(ProductType.FieldName.vendor).setValueInherited(false);
		boxCarmenereChile2000.getFieldMetaData(ProductType.FieldName.owner).setValueInherited(false);
		
		
		bottleCarmenereChile2000.setVendor(vendor);
		bottleCarmenereChile2000.setOwner(vendor);
		bottleCarmenereChile2000.getFieldMetaData(ProductType.FieldName.vendor).setValueInherited(false);
		bottleCarmenereChile2000.getFieldMetaData(ProductType.FieldName.owner).setValueInherited(false);
		
		
//		Person person = dataCreator.createPerson("NightLabs GmbH", "Marco", "Schulze", "marco@nightlabs.de", new Date(),
//				PersonStruct.PERSONALDATA_SALUTATION_MR, "Dr.", "Teststrasse", "79100", "Freiburg", "Baden-Württemberg", "Deutschland",
//				"49", "761", "123456789", "49", "761", "987654321", "Marco Schulze", "123456789", "68090000", "TestBank",
//				"Marco Schulze", "1234567890",
//				01, 2008, "Comment for Marco Schulze");
//		User user02 = dataCreator.createUser("marco", "test", person);



		userSecurityGroup = new UserSecurityGroup(organisationID, "Administrators");
		userSecurityGroup.setName("Administrators");
		userSecurityGroup.setDescription("This group has all access rights within its organisation.");
		userSecurityGroup = pm.makePersistent(userSecurityGroup);

		Authority authority = (Authority) pm.getObjectById(AuthorityID.create(organisationID, Authority.AUTHORITY_ID_ORGANISATION));
		UserSecurityGroupRef userGroupRef = (UserSecurityGroupRef) authority.createAuthorizedObjectRef(userSecurityGroup);

//		authority.createAuthorizedObjectRef(user00.getUserLocal());
//		authority.createAuthorizedObjectRef(user01.getUserLocal());


//		{ // TODO WORKAROUND DATANUCLEUS
//		pm.flush();
//		pm.evictAll();
//		dataCreator = new DataCreatorSimpleTrade(pm, User.getUser(pm, getPrincipal()));
//		userSecurityGroup = (UserSecurityGroup) pm.getObjectById(UserSecurityGroupID.create(organisationID, "Administrators"));
//		user00 = (User) pm.getObjectById(UserID.create(organisationID, "user00"));
//		user01 = (User) pm.getObjectById(UserID.create(organisationID, "user01"));
//		user02 = (User) pm.getObjectById(UserID.create(organisationID, "marco"));
//		user03 = (User) pm.getObjectById(UserID.create(organisationID, "alex"));
//		}

//		userSecurityGroup.addMember(user00.getUserLocal());
//		userSecurityGroup.addMember(user01.getUserLocal());
//		userSecurityGroup.addMember(user02.getUserLocal());
//		userSecurityGroup.addMember(user03.getUserLocal());


		// --- 8< --- KaiExperiments: since 27.07.2009 ------------------
		// Data references.
		DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		int totDemoPersonCnt = 7;

		// Generate Demo Persons.
		LegalEntity legalEntity00 = null;	// Marked for later: dataCreator.createOrderForEndcustomer(legalEntity00), from the original codes, see below.
		for (int i=1; i<=totDemoPersonCnt; i++) {
			// Retrieve information.
			String company = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_company_" + i);
			String name = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_name_" + i);
			String firstName = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_firstName_" + i);
			String eMail = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_eMail_" + i);
			String dateOfBirth = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_dateOfBirth_" + i);
			String salutation = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_salutation_" + i);
			String title = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_title_" + i);
			String postAdress = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_postAdress_" + i);
			String postCode = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_postCode_" + i);
			String postCity = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_postCity_" + i);
			String postRegion = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_postRegion_" + i);
			String postCountry = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_postCountry_" + i);
			String phoneCountryCode = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_phoneCountryCode_" + i);
			String phoneAreaCode = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_phoneAreaCode_" + i);
			String phoneNumber = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_phoneNumber_" + i);
			String faxCountryCode = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_faxCountryCode_" + i);
			String faxAreaCode = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_faxAreaCode_" + i);
			String faxNumber = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_faxNumber_" + i);
			String bankAccountHolder = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_bankAccountHolder_" + i);
			String bankAccountNumber = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_bankAccountNumber_" + i);
			String bankCode = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_bankCode_" + i);
			String bankName = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_bankName_" + i);
			String creditCardHolder = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_creditCardHolder_" + i);
			String creditCardNumber = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_creditCardNumber_" + i);
			int creditCardExpiryMonth = Integer.parseInt( Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_creditCardExpiryMonth_" + i) );
			int creditCardExpiryYear = Integer.parseInt( Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_creditCardExpiryYear_" + i) );
			String comment = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_comment_" + i);
			String userID = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_userID_" + i);
			String password = Messages.getString("org.nightlabs.jfire.chezfrancois.InitialiserSimpleTrade.person_password_" + i);

			Date dob;
			try { dob = formatter.parse( dateOfBirth ); }
			catch (ParseException e) { dob = new Date(); }


			// Create the Person (a person in a JFire datastore).
			Person person = dataCreator.createPerson(
					           company, name, firstName, eMail, dob, salutation, title, postAdress, postCode,
					           postCity, postRegion, postCountry, phoneCountryCode, phoneAreaCode, phoneNumber, faxCountryCode,
					           faxAreaCode, faxNumber, bankAccountHolder, bankAccountNumber, bankCode, bankName, creditCardHolder, creditCardNumber,
					           creditCardExpiryMonth, creditCardExpiryYear, comment);

			// Create LegalEntity (a business partner to a JFire organisation).
			LegalEntity legalEntity = dataCreator.createLegalEntity(person);
			legalEntity.setDefaultCustomerGroup(customerGroupDefault);
			if (userID.equals("user00")) legalEntity00 = legalEntity;


			// Create User.
			User user = dataCreator.createUser(userID, password, person);
			userSecurityGroup.addMember(user.getUserLocal());

			// Other operation(s) on specific users.
			if (userID.equals("user00") || userID.equals("user01"))
				authority.createAuthorizedObjectRef(user.getUserLocal());


			// Done.
			logger.info(" ::: -------------------------->> Person created: [" + i + "] \"" + person.getDisplayName() + "\""
					+ " (User ID: " + user.getUserID() + ")"
					+ " (User type: " + user.getUserType() + ")");
		}
		// ------ KaiExperiments ----- >8 -------------------------------



//		Authority authority = (Authority) pm.getObjectById(AuthorityID.create(organisationID, Authority.AUTHORITY_ID_ORGANISATION));
//
//		UserSecurityGroupRef userGroupRef = (UserSecurityGroupRef) authority.createAuthorizedObjectRef(userSecurityGroup);
//		authority.createAuthorizedObjectRef(user00.getUserLocal());
//		authority.createAuthorizedObjectRef(user01.getUserLocal());

		for (Iterator<RoleGroup> it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
			RoleGroup roleGroup = it.next();
			RoleGroupRef roleGroupRef = authority.getRoleGroupRef(roleGroup.getRoleGroupID());

			if (roleGroupRef == null)
				continue;

			userGroupRef.addRoleGroupRef(roleGroupRef);
		}

		logger.info("Chezfrancois Created all SimpleProductTypes; publishing, confirming and making available now!");
		dataCreator.makeAllLeavesSaleable();

		dataCreator.createOrderForEndcustomer(LegalEntity.getAnonymousLegalEntity(pm));
		dataCreator.createOrderForEndcustomer(LegalEntity.getAnonymousLegalEntity(pm));
		dataCreator.createOrderForEndcustomer(legalEntity00);
		dataCreator.createOrderForEndcustomer(LegalEntity.getAnonymousLegalEntity(pm));
		dataCreator.createOrderForEndcustomer(legalEntity00);

		logger.info("Initialization of JFireChezFrancois complete!");

		logger.info("Initializing JDO for Article.class...");
		pm.getExtent(Article.class);
		logger.info("Initializing JDO for Article.class complete!");
	}

}
