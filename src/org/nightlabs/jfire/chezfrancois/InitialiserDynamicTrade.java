package org.nightlabs.jfire.chezfrancois;

import java.util.Locale;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.book.mappingbased.PFMappingAccountantDelegate;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaCell;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCoordinate;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;

public class InitialiserDynamicTrade
extends Initialiser
{
	public InitialiserDynamicTrade(PersistenceManager pm, JFirePrincipal principal)
	{
		super(pm, principal);
	}

	public void createDemoData()
	throws ModuleException, CannotPublishProductTypeException, CannotConfirmProductTypeException, CannotMakeProductTypeSaleableException
	{
		String organisationID = getOrganisationID();

		pm.getExtent(DynamicProductType.class);
		ProductTypeID softwareDevelopmentID = ProductTypeID.create(organisationID, "softwareDevelopment");
		ProductTypeID serviceID = ProductTypeID.create(organisationID, "service");
		ProductTypeID miscID = ProductTypeID.create(organisationID, "softwareDevelopment.misc");
		try {
			pm.getObjectById(softwareDevelopmentID);
			return; // it exists => do nothing
		} catch (JDOObjectNotFoundException x) {
			// ignore this exception and create demo date
		}

		DataCreatorDynamicTrade dataCreator = new DataCreatorDynamicTrade(pm, User.getUser(pm, getPrincipal()));

		// create Tariffs: normal price, gold card
		Tariff tariffNormalPrice = dataCreator.getTariffNormalPrice();
		Tariff tariffGoldCard = dataCreator.getTariffGoldCard();

		DynamicTradePriceConfig priceConfig = new DynamicTradePriceConfig(IDGenerator.getOrganisationID(), PriceConfig.createPriceConfigID());
		PriceFragmentType vatNet = dataCreator.getPriceFragmentTypeVatNet();
		PriceFragmentType vatVal = dataCreator.getPriceFragmentTypeVatVal();
		priceConfig.addPriceFragmentType(vatVal);
		priceConfig.addPriceFragmentType(vatNet);

		priceConfig.addInputPriceFragmentType(vatNet);

		CustomerGroupID customerGroupID = (CustomerGroupID) JDOHelper.getObjectId(dataCreator.getCustomerGroupAnonymous());
		priceConfig.addCustomerGroup(dataCreator.getCustomerGroupDefault());
		priceConfig.addCustomerGroup(dataCreator.getCustomerGroupAnonymous());

		CurrencyID currencyID = (CurrencyID) JDOHelper.getObjectId(dataCreator.getCurrencyEUR());
		priceConfig.addCurrency(dataCreator.getCurrencyEUR());

		TariffID tariffIDNormalPrice = (TariffID) JDOHelper.getObjectId(tariffNormalPrice);
		TariffID tariffIDGoldCard = (TariffID) JDOHelper.getObjectId(tariffGoldCard);
		priceConfig.addTariff(tariffNormalPrice);
		priceConfig.addTariff(tariffGoldCard);

		priceConfig.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
		priceConfig.getName().setText(Locale.GERMAN.getLanguage(), "Standard");


		FormulaCell fallbackFormulaCell = priceConfig.createFallbackFormulaCell();
		fallbackFormulaCell.setFormula(dataCreator.getPriceFragmentTypeTotal(),
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + vatNet.getOrganisationID() + "\", \"" + vatNet.getPriceFragmentTypeID() + "\")\n" +
				"	)\n" +
				")\n" +
				"+\n" +
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + vatVal.getOrganisationID() + "\", \"" + vatVal.getPriceFragmentTypeID() + "\")\n" +
				"	)\n" +
				")");

		fallbackFormulaCell.setFormula(vatNet,
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		CustomerGroupID.create(\"" + organisationID + "\", \"CustomerGroup-anonymous\")\n" +
				"	)\n" +
				");");

		fallbackFormulaCell.setFormula(vatVal,
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + vatNet.getOrganisationID() + "\", \"" + vatNet.getPriceFragmentTypeID() + "\")\n" +
				"	)\n" +
				") * 0.19");


		priceConfig.createFormulaCell(new PriceCoordinate(customerGroupID, currencyID, tariffIDNormalPrice)).setFormula(vatNet, "15000");
		priceConfig.createFormulaCell(new PriceCoordinate(customerGroupID, currencyID, tariffIDGoldCard)).setFormula(vatNet, "10000");

		dataCreator.getRootDynamicProductType().setInnerPriceConfig(priceConfig);
		
		// create Accounts
		Account accountSoftwareDevelopmentVatNet_revenue = dataCreator.createLocalRevenueAccount("software-development-vat-net.eur", "Software Development Net Revenue (EUR)");
		Account accountSoftwareDevelopmentVatVal_revenue = dataCreator.createLocalRevenueAccount("software-development-vat-val.eur", "Software Development VAT Revenue (EUR)");
		Account accountServiceVatNet_revenue = dataCreator.createLocalRevenueAccount("service-vat-net.eur", "Service Net Revenue (EUR)");
		Account accountServiceVatVal_revenue = dataCreator.createLocalRevenueAccount("service-vat-val.eur", "Service VAT Revenue (EUR)");

		Account accountSoftwareDevelopmentVatNet_expense = dataCreator.createLocalExpenseAccount("software-development-vat-net.eur", "Software Development Net Expense (EUR)");
		Account accountSoftwareDevelopmentVatVal_expense = dataCreator.createLocalExpenseAccount("software-development-vat-val.eur", "Software Development VAT Expense (EUR)");
		Account accountServiceVatNet_expense = dataCreator.createLocalExpenseAccount("service-vat-net.eur", "Service Net Expense (EUR)");
		Account accountServiceVatVal_expense = dataCreator.createLocalExpenseAccount("service-vat-val.eur", "Service VAT Expense (EUR)");
		
		DynamicProductType softwareDevelopment = dataCreator.createCategory(null, softwareDevelopmentID.productTypeID, null, "Software Development", "Software-Entwicklung");
		
		// FIXME
		@SuppressWarnings("unused")
		DynamicProductType swDevJFire = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.jfire", null, "JFire");
		@SuppressWarnings("unused")
		DynamicProductType swDevProjectA = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.projectA", null, "Project A", "Projekt A");
		@SuppressWarnings("unused")
		DynamicProductType swDevProjectB = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.projectB", null, "Project B", "Projekt B");
		@SuppressWarnings("unused")
		DynamicProductType swDevProjectC = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.projectC", null, "Project C", "Projekt C");

		
		DynamicProductType service = dataCreator.createCategory(null, serviceID.productTypeID, null, "Service", "Dienstleistung");
		
		
		// FIXME
		@SuppressWarnings("unused")
		DynamicProductType serviceJFire = dataCreator.createLeaf(service, "service.jfire", null, "JFire");
		@SuppressWarnings("unused")
		DynamicProductType serviceNetwork = dataCreator.createLeaf(service, "service.network", null, "Network", "Netzwerk");
		@SuppressWarnings("unused")
		DynamicProductType serviceWebserver = dataCreator.createLeaf(service, "service.webserver", null, "Webserver");
		
		DynamicProductType misc = dataCreator.createLeaf(null, miscID.productTypeID, null, "Miscellaneous", "Verschiedenes");
		

		// configure moneyflow
		PFMappingAccountantDelegate swDelegate = new PFMappingAccountantDelegate(organisationID, "softwareAccountantDelegate");

		swDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(softwareDevelopment, dataCreator.getPriceFragmentTypeTotal(),
						accountSoftwareDevelopmentVatNet_revenue, accountSoftwareDevelopmentVatNet_expense));
		swDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(softwareDevelopment, dataCreator.getPriceFragmentTypeVatVal(),
						accountSoftwareDevelopmentVatVal_revenue, accountSoftwareDevelopmentVatVal_expense));

		PFMappingAccountantDelegate serviceDelegate = new PFMappingAccountantDelegate(organisationID, "serviceAccountantDelegate");

		serviceDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(service, dataCreator.getPriceFragmentTypeTotal(),
						accountServiceVatNet_revenue, accountServiceVatNet_expense));
		serviceDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(service, dataCreator.getPriceFragmentTypeVatVal(),
						accountServiceVatVal_revenue, accountServiceVatVal_expense));


		PFMappingAccountantDelegate miscDelegate = new PFMappingAccountantDelegate(organisationID, "miscAccountantDelegate");

		miscDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(misc, dataCreator.getPriceFragmentTypeTotal(),
						accountServiceVatNet_revenue, accountServiceVatNet_expense));
		miscDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(misc, dataCreator.getPriceFragmentTypeVatVal(),
						accountServiceVatVal_revenue, accountServiceVatVal_expense));

		dataCreator.getRootDynamicProductType().applyInheritance();

		softwareDevelopment.getProductTypeLocal().getFieldMetaData("localAccountantDelegate").setValueInherited(false);
		softwareDevelopment.getProductTypeLocal().setLocalAccountantDelegate(swDelegate);
		softwareDevelopment.applyInheritance();
		service.getProductTypeLocal().getFieldMetaData("localAccountantDelegate").setValueInherited(false);
		service.getProductTypeLocal().setLocalAccountantDelegate(serviceDelegate);
		service.applyInheritance();
		misc.getProductTypeLocal().getFieldMetaData("localAccountantDelegate").setValueInherited(false);
		misc.getProductTypeLocal().setLocalAccountantDelegate(miscDelegate);
		misc.applyInheritance();

		dataCreator.makeAllLeavesSaleable();
	}
}
