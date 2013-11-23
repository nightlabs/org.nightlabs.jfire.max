package org.nightlabs.jfire.chezfrancois;

import java.util.Locale;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.book.mappingbased.MappingBasedAccountantDelegate;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaCell;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCoordinate;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.id.CustomerGroupID;

public class InitialiserDynamicTrade
extends Initialiser
{
	public InitialiserDynamicTrade(final PersistenceManager pm, final JFirePrincipal principal)
	{
		super(pm, principal);
	}

	public void createDemoData()
	throws CannotPublishProductTypeException, CannotConfirmProductTypeException, CannotMakeProductTypeSaleableException, DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException
	{
		final String organisationID = getOrganisationID();

		pm.getExtent(DynamicProductType.class);
		final ProductTypeID softwareDevelopmentID = ProductTypeID.create(organisationID, "softwareDevelopment");
		final ProductTypeID serviceID = ProductTypeID.create(organisationID, "service");
		final ProductTypeID miscID = ProductTypeID.create(organisationID, "softwareDevelopment.misc");
		try {
			pm.getObjectById(softwareDevelopmentID);
			return; // it exists => do nothing
		} catch (final JDOObjectNotFoundException x) {
			// ignore this exception and create demo date
		}

		final DataCreatorDynamicTrade dataCreator = new DataCreatorDynamicTrade(pm, User.getUser(pm, getPrincipal()));

		// create Tariffs: normal price, gold card
		final Tariff tariffNormalPrice = dataCreator.getTariffNormalPrice();
		final Tariff tariffGoldCard = dataCreator.getTariffGoldCard();

		final DynamicTradePriceConfig priceConfig = new DynamicTradePriceConfig(null);
		final PriceFragmentType vatNet = dataCreator.getPriceFragmentTypeVatNet();
		final PriceFragmentType vatVal = dataCreator.getPriceFragmentTypeVatVal();
		priceConfig.addPriceFragmentType(vatVal);
		priceConfig.addPriceFragmentType(vatNet);

		priceConfig.addInputPriceFragmentType(vatNet);

		final CustomerGroupID customerGroupID = (CustomerGroupID) JDOHelper.getObjectId(dataCreator.getCustomerGroupAnonymous());
		priceConfig.addCustomerGroup(dataCreator.getCustomerGroupDefault());
		priceConfig.addCustomerGroup(dataCreator.getCustomerGroupAnonymous());

		final CurrencyID currencyID = (CurrencyID) JDOHelper.getObjectId(dataCreator.getCurrencyEUR());
		priceConfig.addCurrency(dataCreator.getCurrencyEUR());

		final TariffID tariffIDNormalPrice = (TariffID) JDOHelper.getObjectId(tariffNormalPrice);
		final TariffID tariffIDGoldCard = (TariffID) JDOHelper.getObjectId(tariffGoldCard);
		priceConfig.addTariff(tariffNormalPrice);
		priceConfig.addTariff(tariffGoldCard);

		priceConfig.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
		priceConfig.getName().setText(Locale.GERMAN.getLanguage(), "Standard");


		final FormulaCell fallbackFormulaCell = priceConfig.createFallbackFormulaCell();
		fallbackFormulaCell.setFormula(dataCreator.getPriceFragmentTypeTotal(),
				"cell.resolvePriceCellsAmount(\n" +
				"	PriceFragmentTypeID.create(\"" + vatNet.getOrganisationID() + "\", \"" + vatNet.getPriceFragmentTypeID() + "\")\n" +
				")\n" +
				"+\n" +
				"cell.resolvePriceCellsAmount(\n" +
				"	PriceFragmentTypeID.create(\"" + vatVal.getOrganisationID() + "\", \"" + vatVal.getPriceFragmentTypeID() + "\")\n" +
				")");

		fallbackFormulaCell.setFormula(vatNet,
				"cell.resolvePriceCellsAmount(\n" +
				"	CustomerGroupID.create(\"" + organisationID + "\", \"CustomerGroup-anonymous\")\n" +
				");");

		fallbackFormulaCell.setFormula(vatVal,
				"cell.resolvePriceCellsAmount(\n" +
				"	PriceFragmentTypeID.create(\"" + vatNet.getOrganisationID() + "\", \"" + vatNet.getPriceFragmentTypeID() + "\")\n" +
				") * 0.19");


		priceConfig.createFormulaCell(new PriceCoordinate(customerGroupID, currencyID, tariffIDNormalPrice)).setFormula(vatNet, "15000");
		priceConfig.createFormulaCell(new PriceCoordinate(customerGroupID, currencyID, tariffIDGoldCard)).setFormula(vatNet, "10000");

		dataCreator.getRootDynamicProductType().setInnerPriceConfig(priceConfig);

		// create Accounts
		final Account accountSoftwareDevelopmentVatNet_revenue = dataCreator.createLocalRevenueAccount("software-development-vat-net.eur", "Software Development Net Revenue (EUR)");
		final Account accountSoftwareDevelopmentVatVal_revenue = dataCreator.createLocalRevenueAccount("software-development-vat-val.eur", "Software Development VAT Revenue (EUR)");
		final Account accountServiceVatNet_revenue = dataCreator.createLocalRevenueAccount("service-vat-net.eur", "Service Net Revenue (EUR)");
		final Account accountServiceVatVal_revenue = dataCreator.createLocalRevenueAccount("service-vat-val.eur", "Service VAT Revenue (EUR)");

		final Account accountSoftwareDevelopmentVatNet_expense = dataCreator.createLocalExpenseAccount("software-development-vat-net.eur", "Software Development Net Expense (EUR)");
		final Account accountSoftwareDevelopmentVatVal_expense = dataCreator.createLocalExpenseAccount("software-development-vat-val.eur", "Software Development VAT Expense (EUR)");
		final Account accountServiceVatNet_expense = dataCreator.createLocalExpenseAccount("service-vat-net.eur", "Service Net Expense (EUR)");
		final Account accountServiceVatVal_expense = dataCreator.createLocalExpenseAccount("service-vat-val.eur", "Service VAT Expense (EUR)");

		final DynamicProductType softwareDevelopment = dataCreator.createCategory(null, softwareDevelopmentID.productTypeID, null, "Software Development", "Software-Entwicklung");

		final LegalEntity vendor  = dataCreator.createVendor1();

		// FIXME
		@SuppressWarnings("unused")
		final
		DynamicProductType swDevJFire = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.jfire", null, null, "JFire");
		@SuppressWarnings("unused")
		final
		DynamicProductType swDevProjectA = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.projectA", null, null, "Project A", "Projekt A");
		@SuppressWarnings("unused")
		final
		DynamicProductType swDevProjectB = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.projectB", null, null, "Project B", "Projekt B");
		@SuppressWarnings("unused")
		final
		DynamicProductType swDevProjectC = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.projectC", null, null, "Project C", "Projekt C");
		@SuppressWarnings("unused")
		final
		DynamicProductType swDevProjectD = dataCreator.createLeaf(softwareDevelopment, "softwareDevelopment.projectD", null, vendor, "Project D", "Projekt D");

		final DynamicProductType service = dataCreator.createCategory(null, serviceID.productTypeID, null, "Service", "Dienstleistung");

		// FIXME
		@SuppressWarnings("unused")
		final
		DynamicProductType serviceJFire = dataCreator.createLeaf(service, "service.jfire", null, null, "JFire");
		@SuppressWarnings("unused")
		final
		DynamicProductType serviceNetwork = dataCreator.createLeaf(service, "service.network", null, null, "Network", "Netzwerk");
		@SuppressWarnings("unused")
		final
		DynamicProductType serviceWebserver = dataCreator.createLeaf(service, "service.webserver", null, null, "Webserver");

		final DynamicProductType misc = dataCreator.createLeaf(null, miscID.productTypeID, null, null, "Miscellaneous", "Verschiedenes");

	// can not be set here because setting the vendor for an already confirmed ProductType is not possible
//		swDevProjectD.setVendor(legalEntityVendor);

		// configure moneyflow
		final MappingBasedAccountantDelegate swDelegate = new MappingBasedAccountantDelegate(organisationID, "softwareAccountantDelegate");

		swDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(softwareDevelopment, dataCreator.getPriceFragmentTypeTotal(),
						accountSoftwareDevelopmentVatNet_revenue, accountSoftwareDevelopmentVatNet_expense));
		swDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(softwareDevelopment, dataCreator.getPriceFragmentTypeVatVal(),
						accountSoftwareDevelopmentVatVal_revenue, accountSoftwareDevelopmentVatVal_expense));

		final MappingBasedAccountantDelegate serviceDelegate = new MappingBasedAccountantDelegate(organisationID, "serviceAccountantDelegate");

		serviceDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(service, dataCreator.getPriceFragmentTypeTotal(),
						accountServiceVatNet_revenue, accountServiceVatNet_expense));
		serviceDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(service, dataCreator.getPriceFragmentTypeVatVal(),
						accountServiceVatVal_revenue, accountServiceVatVal_expense));


		final MappingBasedAccountantDelegate miscDelegate = new MappingBasedAccountantDelegate(organisationID, "miscAccountantDelegate");

		miscDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(misc, dataCreator.getPriceFragmentTypeTotal(),
						accountServiceVatNet_revenue, accountServiceVatNet_expense));
		miscDelegate.addMoneyFlowMapping(
				dataCreator.createPFMoneyFlowMapping(misc, dataCreator.getPriceFragmentTypeVatVal(),
						accountServiceVatVal_revenue, accountServiceVatVal_expense));

		dataCreator.getRootDynamicProductType().applyInheritance();

		softwareDevelopment.getProductTypeLocal().getFieldMetaData(
				ProductTypeLocal.FieldName.localAccountantDelegate).setValueInherited(false);
		softwareDevelopment.getProductTypeLocal().setLocalAccountantDelegate(swDelegate);
		softwareDevelopment.applyInheritance();
		service.getProductTypeLocal().getFieldMetaData(
				ProductTypeLocal.FieldName.localAccountantDelegate).setValueInherited(false);
		service.getProductTypeLocal().setLocalAccountantDelegate(serviceDelegate);
		service.applyInheritance();
		misc.getProductTypeLocal().getFieldMetaData(
				ProductTypeLocal.FieldName.localAccountantDelegate).setValueInherited(false);
		misc.getProductTypeLocal().setLocalAccountantDelegate(miscDelegate);
		misc.applyInheritance();

		dataCreator.makeAllLeavesSaleable();
	}
}
