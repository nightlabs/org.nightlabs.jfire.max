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

package org.nightlabs.jfire.reporting.trade;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.config.id.ConfigModuleInitialiserID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.parameter.ReportParameterUtil;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.ValueProviderCategory;
import org.nightlabs.jfire.reporting.parameter.ValueProviderInputParameter;
import org.nightlabs.jfire.reporting.parameter.ReportParameterUtil.NameEntry;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.reporting.trade.config.ReportLayoutCfModInitialiserArticleContainerLayouts;
import org.nightlabs.jfire.scripting.ScriptingIntialiserException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * Bean to initialize the default reports and report scripts.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @ejb.bean name="jfire/ejb/JFireReportingTrade/ReportManagerTrade"
 *					 jndi-name="jfire/ejb/JFireReportingTrade/ReportManagerTrade"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ReportManagerTradeBean
extends BaseSessionBeanImpl
implements ReportManagerTradeRemote
{
	private static final long serialVersionUID = 1L;
	public static final Logger logger = Logger.getLogger(ReportManagerTradeBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.trade.ReportManagerTradeRemote#initializeScripting()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initializeScripting() throws ScriptingIntialiserException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {

			ScriptingInitialiser.initialize(pm, jfireServerManager, Organisation.DEV_ORGANISATION_ID);

		} finally {
			pm.close();
			jfireServerManager.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.trade.ReportManagerTradeRemote#initializeReporting()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initializeReporting() throws Exception
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireReportingTradeEAR.MODULE_NAME);
			if (moduleMetaData == null) {

				pm.makePersistent(new ModuleMetaData(
						JFireReportingTradeEAR.MODULE_NAME, "0.9.7.0.0.beta", "0.9.7.0.0.beta")
				);

				// initialise meta-data
				pm.getExtent(ReportLayoutCfModInitialiserArticleContainerLayouts.class);

				initializeReportParameterAcquisition(pm);


				ConfigModuleInitialiserID initialiserID = ReportLayoutCfModInitialiserArticleContainerLayouts.getConfigModuleInitialiserID(getOrganisationID());
				ReportLayoutCfModInitialiserArticleContainerLayouts initialiser = null;
				try {
					initialiser = (ReportLayoutCfModInitialiserArticleContainerLayouts) pm.getObjectById(initialiserID);
				} catch (JDOObjectNotFoundException e) {
					initialiser = new ReportLayoutCfModInitialiserArticleContainerLayouts(getOrganisationID());
					initialiser = pm.makePersistent(initialiser);
				}
			}
			// Report initialization is done on every run.
			// better have the layouts for the local organisation, than for the dev organisation
			ReportingInitialiser.initialise(pm, jfireServerManager, getOrganisationID());

		} finally {
			pm.close();
			jfireServerManager.close();
		}
	}

	private void initializeReportParameterAcquisition(final PersistenceManager pm) {
		ValueProviderCategoryID categoryID = ValueProviderCategoryID.create(Organisation.DEV_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_REPORTS);
		ValueProviderCategory rootCategory = ReportParameterUtil.createValueProviderCategory(
				pm, null, categoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Trade reports")}
			);

		ValueProviderCategoryID leCategoryID = ValueProviderCategoryID.create(Organisation.DEV_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_LEGAL_ENTITY);
		ValueProviderCategory leCategory = ReportParameterUtil.createValueProviderCategory(
				pm, rootCategory, leCategoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Legal entity parameters")}
			);

		ReportParameterUtil.createValueProvider(pm, leCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_LEGAL_ENTITY_SEARCH,
				AnchorID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Customer search")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Customer search")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a customer")}
			);

		ReportParameterUtil.createValueProvider(pm, leCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_LEGAL_ENTITY_SEARCH_MULTIPLE,
				Collection.class.getName() + "<" + AnchorID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Customer search multiple")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Customer search multiple")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a customer multiple customers")}
			);

		ValueProviderCategoryID docsCategoryID = ValueProviderCategoryID.create(
				Organisation.DEV_ORGANISATION_ID,
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS);
		ValueProviderCategory docsCategory = ReportParameterUtil.createValueProviderCategory(
				pm, rootCategory, docsCategoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Trade documents parameters")}
			);

		ValueProviderCategoryID invoiceCategoryID = ValueProviderCategoryID.create(
				Organisation.DEV_ORGANISATION_ID,
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE);
		ValueProviderCategory invoiceCategory = ReportParameterUtil.createValueProviderCategory(
				pm, docsCategory, invoiceCategoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice parameters")}
			);

		ValueProvider invoiceByCustomer = ReportParameterUtil.createValueProvider(
				pm, invoiceCategory,
				ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_CUSTOMER,
				ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an invoice")}
			);
		invoiceByCustomer.getInputParameters().clear();

		if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
			pm.flush();

		invoiceByCustomer.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "customer", AnchorID.class.getName()));


		ValueProvider invoiceByCustomerAndPeriod = ReportParameterUtil.createValueProvider(
				pm, invoiceCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_CUSTOMER_AND_PERIOD, ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice of customer in period")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice of customer in period")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an invoice")}
			);
		invoiceByCustomerAndPeriod.getInputParameters().clear();

		if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
			pm.flush();

		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "customer", AnchorID.class.getName()));
		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "from", Date.class.getName()));
		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "to", Date.class.getName()));


		ValueProvider invoiceByArticleType = ReportParameterUtil.createValueProvider(
				pm, invoiceCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_ARTICLE_TYPE, ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice by article type")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice by article type")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an invoice")}
			);
		invoiceByArticleType.getInputParameters().clear();

		if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
			pm.flush();

		invoiceByArticleType.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "productType", ProductTypeID.class.getName()));

/** New Parameters **/

		// order category
		ValueProviderCategoryID orderCategoryID = ValueProviderCategoryID.create(
				Organisation.DEV_ORGANISATION_ID,
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_ORDER);
		ValueProviderCategory orderCategory = ReportParameterUtil.createValueProviderCategory(
				pm, docsCategory, orderCategoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Order parameters")}
			);

		// order by customer category
		ValueProvider orderByCustomer = ReportParameterUtil.createValueProvider(
				pm, orderCategory,
				ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_ORDER_BY_CUSTOMER,
				ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Order of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Order of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an order")}
			);
		orderByCustomer.getInputParameters().clear();

		if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
			pm.flush();

		orderByCustomer.addInputParameter(new ValueProviderInputParameter(orderByCustomer, "customer", AnchorID.class.getName()));

		// offer category
		ValueProviderCategoryID offerCategoryID = ValueProviderCategoryID.create(
				Organisation.DEV_ORGANISATION_ID,
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_OFFER);
		ValueProviderCategory offerCategory = ReportParameterUtil.createValueProviderCategory(
				pm, docsCategory, offerCategoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Offer parameters")}
			);

		// offer by customer category
		ValueProvider offerByCustomer = ReportParameterUtil.createValueProvider(
				pm, offerCategory,
				ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_OFFER_BY_CUSTOMER,
				ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Offer of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Offer of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an offer")}
			);
		offerByCustomer.getInputParameters().clear();

		if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
			pm.flush();

		offerByCustomer.addInputParameter(new ValueProviderInputParameter(offerByCustomer, "customer", AnchorID.class.getName()));

		// deliveryNote category
		ValueProviderCategoryID deliveryNoteCategoryID = ValueProviderCategoryID.create(
				Organisation.DEV_ORGANISATION_ID,
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_DELIVERY_NOTE);
		ValueProviderCategory deliveryNoteCategory = ReportParameterUtil.createValueProviderCategory(
				pm, docsCategory, deliveryNoteCategoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "DeliveryNote parameters")}
			);

		// deliveryNote by customer category
		ValueProvider deliveryNoteByCustomer = ReportParameterUtil.createValueProvider(
				pm, deliveryNoteCategory,
				ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_DELIVERY_NOTE_BY_CUSTOMER,
				ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "DeliveryNote of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "DeliveryNote of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an delivery note")}
			);
		deliveryNoteByCustomer.getInputParameters().clear();

		if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
			pm.flush();

		deliveryNoteByCustomer.addInputParameter(new ValueProviderInputParameter(deliveryNoteByCustomer, "customer", AnchorID.class.getName()));


		ValueProviderCategoryID accountingCategoryID = ValueProviderCategoryID.create(
				Organisation.DEV_ORGANISATION_ID,
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_ACCOUNTING);
		ValueProviderCategory accountingCategory = ReportParameterUtil.createValueProviderCategory(
				pm, rootCategory, accountingCategoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Accounting parameters")}
			);

		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_CURRENCY,
				CurrencyID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Currency")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a currency")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a currency")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_CURRENCIES,
				Collection.class.getName() + "<" + CurrencyID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of currencies")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of currencies")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of currencies")}
			);

		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_TARIFF,
				TariffID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Tariff")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a tariff")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a fariff")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_TARIFFS,
				Collection.class.getName() + "<" + TariffID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of tariffs")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of tariffs")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of tariffs")}
			);

		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_PAYMENT,
				ModeOfPaymentID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Mode of payment")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a mode of payment")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a mode of payment")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_PAYMENTS,
				Collection.class.getName() + "<" + ModeOfPaymentID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of modes of payment")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of modes of payment")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of modes of payment")}
			);

		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_PAYMENT_FLAVOUR,
				ModeOfPaymentFlavourID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Mode of payment flavour")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a mode of payment flavour")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a mode of payment flavour")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_PAYMENT_FLAVOURS,
				Collection.class.getName() + "<" + ModeOfPaymentFlavourID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of mode of payment flavours")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of mode of payment flavours")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of mode of payment flavours")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_PAYMENT_FLAVOURS_BY_MODE_OF_PAYMENT,
				Collection.class.getName() + "<" + ModeOfPaymentFlavourID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of mode of payment flavours by their mode of payment")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of mode of payment flavours by their mode of payment")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of mode of payment flavours by their mode of payment")}
			);

		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_DELIVERY,
				ModeOfDeliveryID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Mode of delivery")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a mode of delivery")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a mode of delivery")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_DELIVERIES,
				Collection.class.getName() + "<" + ModeOfDeliveryID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of modes of delivery")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of modes of delivery")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of modes of delivery")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_DELIVERY_FLAVOUR,
				ModeOfDeliveryFlavourID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Mode of delivery flavour")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a mode of delivery flavour")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a mode of delivery flavour")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_DELIVERY_FLAVOURS,
				Collection.class.getName() + "<" + ModeOfDeliveryFlavourID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of mode of delivery  flavours")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of mode of delivery flavours")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of mode of delivery flavours")}
			);
		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_MODE_OF_DELIVERY_FLAVOURS_BY_MODE_OF_DELIVERY,
				Collection.class.getName() + "<" + ModeOfDeliveryFlavourID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of mode of delivery flavours by their mode of delivery")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of mode of delivery flavours by their mode of delivery")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of mode of delivery flavours by their mode of delivery")}
			);


		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_ACCOUNT_ID,
				AnchorID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Account")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an account")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an account")}
			);

		ReportParameterUtil.createValueProvider(pm, accountingCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_ACCOUNTING_ACCOUNT_IDS,
				Collection.class.getName() + "<" + AnchorID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of accounts")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of accounts")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of accounts")}
			);


		ValueProviderCategoryID storeCategoryID = ValueProviderCategoryID.create(
				Organisation.DEV_ORGANISATION_ID,
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_STORE);
		ValueProviderCategory storeCategory = ReportParameterUtil.createValueProviderCategory(
				pm, rootCategory, storeCategoryID,
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Store parameters")}
			);
		ReportParameterUtil.createValueProvider(pm, storeCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_STORE_REPOSITORY_ID,
				AnchorID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Repository")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an repository")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an repository")}
			);

		ReportParameterUtil.createValueProvider(pm, storeCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_STORE_REPOSITORY_IDS,
				Collection.class.getName() + "<" + AnchorID.class.getName() + ">",
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of repositories")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of repositories")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of repositories")}
			);

	}
}
