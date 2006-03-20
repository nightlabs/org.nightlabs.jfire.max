package org.nightlabs.jfire.chezfrancois;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.tariffpriceconfig.FormulaCell;
import org.nightlabs.jfire.accounting.tariffpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.tariffpriceconfig.PriceCalculator;
import org.nightlabs.jfire.accounting.tariffpriceconfig.StablePriceConfig;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Trader;

public class DataCreator
{
	private PersistenceManager pm;
	private String organisationID;
	private User user;
	private SimpleProductType rootSimpleProductType;
	private Store store;
	private Accounting accounting;
	private String rootOrganisationID;

	public DataCreator(User user)
	{
		this.user = user;
		this.organisationID = user.getOrganisationID();
		this.pm = JDOHelper.getPersistenceManager(user);
		
		pm.getExtent(SimpleProductType.class);
		rootSimpleProductType = (SimpleProductType) pm.getObjectById(
				ProductTypeID.create(organisationID, SimpleProductType.class.getName()));

		store = Store.getStore(pm);
		accounting = Accounting.getAccounting(pm);

		try {
			InitialContext ctx = new InitialContext();
			try {
				rootOrganisationID = Organisation.getRootOrganisationID(ctx);
			} finally {
				ctx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	private String languageID = "en";

	protected SimpleProductType createCategory(SimpleProductType parent, String productTypeID, String name)
	{
		if (parent == null)
			parent = rootSimpleProductType;

		try {
			return (SimpleProductType) pm.getObjectById(ProductTypeID.create(organisationID, productTypeID));
		} catch (JDOObjectNotFoundException x) {
			// not yet existent => create the object!
		}

		SimpleProductType pt = new SimpleProductType(
				organisationID, productTypeID, parent, null, 
				ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
		pt.getName().setText(languageID, name);

		store.addProductType(user, pt, SimpleProductType.getDefaultHome(pm, pt));
		store.setProductTypeStatus_published(user, pt);

		return pt;
	}

	private List<SimpleProductType> createdLeafs = new ArrayList<SimpleProductType>(); 

	public SimpleProductType createLeaf(SimpleProductType category, String productTypeID, String name, IInnerPriceConfig innerPriceConfig)
	{
		SimpleProductType pt = new SimpleProductType(
				organisationID, productTypeID, category, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
		pt.getName().setText(Locale.ENGLISH.getLanguage(), name);
		pt.setPackagePriceConfig(new StablePriceConfig(organisationID, accounting.createPriceConfigID()));
		pt.setInnerPriceConfig(innerPriceConfig);
		store.addProductType(user, pt, SimpleProductType.getDefaultHome(pm, pt));

		store.setProductTypeStatus_published(user, pt);
		store.setProductTypeStatus_confirmed(user, pt);
		store.setProductTypeStatus_saleable(user, pt, true);
		
		createdLeafs.add(pt);

		return pt;
	}
	
	public void calculatePrices()
	throws ModuleException
	{
		for (SimpleProductType pt : createdLeafs) {
			if (pt.getInnerPriceConfig() != null && pt.getPackagePriceConfig() != null)
				((StablePriceConfig)pt.getPackagePriceConfig()).adoptParameters(pt.getInnerPriceConfig());

			PriceCalculator priceCalculator = new PriceCalculator(pt);
			priceCalculator.preparePriceCalculation(accounting);
			priceCalculator.calculatePrices();
		}
	}

	public IInnerPriceConfig createInnerPercentagePriceConfig(String name, int percentage, ProductType packageProductType)
	{
		FormulaPriceConfig formulaPriceConfig = new FormulaPriceConfig(organisationID, accounting.createPriceConfigID());
		PriceFragmentType vatNet = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(rootOrganisationID, "vat-de-16-net"));
		PriceFragmentType vatVal = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(rootOrganisationID, "vat-de-16-val"));
		formulaPriceConfig.addProductType(packageProductType);
		formulaPriceConfig.addPriceFragmentType(vatVal);
		formulaPriceConfig.addPriceFragmentType(vatNet);
		formulaPriceConfig.getName().setText(languageID, name);
		FormulaCell fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
		fallbackFormulaCell.setFormula(
				Organisation.DEVIL_ORGANISATION_ID,
				PriceFragmentType.TOTAL_PRICEFRAGMENTTYPEID,
				"cell.resolvePriceCellsAmount(\n" +
				"	new AbsolutePriceCoordinate(\n" +
				"		null,\n" +
				"		null,\n" +
				"		null,\n" +
				"		\""+packageProductType.getPrimaryKey()+"\",\n" +
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
				
//					"/ 1.16 * 0.16;");
				
				"\n" +
				"-\n" +
				"\n" +
				"cell.resolvePriceCellsAmount(\n" +
				"	new AbsolutePriceCoordinate(\n" +
				"		null,\n" +
				"		null,\n" +
				"		null,\n" +
				"		null,\n" +
				"		\""+rootOrganisationID+"/vat-de-16-net\"\n" +
				"	)\n"+
				");");

		return formulaPriceConfig;
	}

	public IInnerPriceConfig createFixPriceConfig(String name, Tariff[] tariffs, long[] prices)
	{
		pm.getExtent(Currency.class);
		Currency euro = (Currency) pm.getObjectById(CurrencyID.create("EUR"));

//	 create the price config "Car - Middle Class"
		PriceFragmentType totalPriceFragmentType = PriceFragmentType.getTotalPriceFragmentType(pm);
		PriceFragmentType vatNet = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(rootOrganisationID, "vat-de-16-net"));
		PriceFragmentType vatVal = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(rootOrganisationID, "vat-de-16-val"));

		Accounting accounting = Accounting.getAccounting(pm);
		Trader trader = Trader.getTrader(pm);
		StablePriceConfig stablePriceConfig = new StablePriceConfig(organisationID, accounting.createPriceConfigID());
		FormulaPriceConfig formulaPriceConfig = new FormulaPriceConfig(organisationID, accounting.createPriceConfigID());
		formulaPriceConfig.getName().setText(languageID, name);
		
		CustomerGroup customerGroupDefault = trader.getDefaultCustomerGroupForKnownCustomer();
		CustomerGroup customerGroupAnonymous = LegalEntity.getAnonymousCustomer(pm).getDefaultCustomerGroup();
		formulaPriceConfig.addCustomerGroup(customerGroupDefault);
		formulaPriceConfig.addCustomerGroup(customerGroupAnonymous);
		formulaPriceConfig.addCurrency(euro);
		for (int i = 0; i < tariffs.length; i++) {
			Tariff tariff = tariffs[i];
			formulaPriceConfig.addTariff(tariff);			
		}
//			formulaPriceConfig.addProductType(rootSimpleProductType);
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

//					"/ 1.16 * 0.16");

				"\n" +
				"-\n" +
				"\n" +
				"cell.resolvePriceCellsAmount(\n" +
				"	new AbsolutePriceCoordinate(\n" +
				"		null,\n" +
				"		null,\n" +
				"		null,\n" +
				"		null,\n" +
				"		\""+rootOrganisationID+"/vat-de-16-net\"\n" +
				"	)\n" +
				");");

		for (int i = 0; i < prices.length; i++) {
			long price = prices[i];
			Tariff tariff = tariffs[i];
		
			FormulaCell cell = formulaPriceConfig.createFormulaCell(customerGroupDefault, tariff, euro);
			cell.setFormula(totalPriceFragmentType, String.valueOf(price));
		}

		return formulaPriceConfig;
	}
}
