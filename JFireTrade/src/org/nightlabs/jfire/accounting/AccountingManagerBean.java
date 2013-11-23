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

package org.nightlabs.jfire.accounting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.PartnerPayPayableObjectAccountantDelegate;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.accounting.book.mappingbased.MappingBasedAccountantDelegate;
import org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowDimension;
import org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowMapping;
import org.nightlabs.jfire.accounting.book.mappingbased.OwnerDimension;
import org.nightlabs.jfire.accounting.book.mappingbased.PFMoneyFlowMapping;
import org.nightlabs.jfire.accounting.book.mappingbased.PriceFragmentDimension;
import org.nightlabs.jfire.accounting.book.mappingbased.SourceOrganisationDimension;
import org.nightlabs.jfire.accounting.book.mappingbased.MappingBasedAccountantDelegate.ResolvedMapEntry;
import org.nightlabs.jfire.accounting.book.mappingbased.MappingBasedAccountantDelegate.ResolvedMapKey;
import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfigUtil;
import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.id.TariffMappingID;
import org.nightlabs.jfire.accounting.pay.CheckRequirementsEnvironment;
import org.nightlabs.jfire.accounting.pay.ModeOfPayment;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentConst;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.PaymentHelperLocal;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessorBankTransferGermany;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessorCash;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessorCreditCardDummyForClientPayment;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessorDebitNoteGermany;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessorNonPayment;
import org.nightlabs.jfire.accounting.pay.config.ModeOfPaymentConfigModule;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID;
import org.nightlabs.jfire.accounting.pay.id.PaymentDataID;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.accounting.priceconfig.AffectedProductType;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.accounting.prop.InvoiceStruct;
import org.nightlabs.jfire.accounting.query.MoneyTransferIDQuery;
import org.nightlabs.jfire.accounting.query.MoneyTransferQuery;
import org.nightlabs.jfire.accounting.tariffuserset.ResellerTariffUserSetFactory;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.entityuserset.notification.EntityUserSetNotificationFilterEntry;
import org.nightlabs.jfire.idgenerator.IDNamespaceDefault;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.jfire.transfer.id.TransferID;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Alexander Bieber - alex[AT]nightlabs[DOT]de
 *
 * @ejb.bean name="jfire/ejb/JFireTrade/AccountingManager"
 *           jndi-name="jfire/ejb/JFireTrade/AccountingManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class AccountingManagerBean
extends BaseSessionBeanImpl
implements AccountingManagerRemote, AccountingManagerLocal
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AccountingManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise()
	throws IOException
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
//			SecurityChangeListenerTariffUserSet.register(pm);
			pm.getExtent(PFMoneyFlowMapping.class);
			pm.getExtent(OwnerDimension.class);
			pm.getExtent(PriceFragmentDimension.class);

			MoneyFlowDimension priceFragmentDimension = MoneyFlowDimension.getMoneyFlowDimension(pm, PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID);
			if (priceFragmentDimension == null) {
				priceFragmentDimension = new PriceFragmentDimension();
				priceFragmentDimension = pm.makePersistent(priceFragmentDimension);
			}

			MoneyFlowDimension ownerDimension = MoneyFlowDimension.getMoneyFlowDimension(pm, OwnerDimension.MONEY_FLOW_DIMENSION_ID);
			if (ownerDimension == null) {
				ownerDimension = new OwnerDimension();
				ownerDimension = pm.makePersistent(ownerDimension);
			}

			MoneyFlowDimension sourceOrgDimension = MoneyFlowDimension.getMoneyFlowDimension(pm, SourceOrganisationDimension.MONEY_FLOW_DIMENSION_ID);
			if (sourceOrgDimension == null) {
				sourceOrgDimension = new SourceOrganisationDimension();
				sourceOrgDimension = pm.makePersistent(sourceOrgDimension);
			}

			PayableObjectMoneyTransferFactory invoiceTransferFactory = PayableObjectMoneyTransferFactoryJDO.getFactory(
					pm, null, PartnerPayPayableObjectAccountantDelegate.PAYABLE_OBJECT_FACTORY_SCOPE, Invoice.class
					);
			if (invoiceTransferFactory == null)
			{
				invoiceTransferFactory = 
					new InvoiceMoneyTransferFactoryJDO(PartnerPayPayableObjectAccountantDelegate.PAYABLE_OBJECT_FACTORY_SCOPE);
				pm.makePersistent(invoiceTransferFactory);
			}

			pm.getExtent(AccountType.class);
			try {
				pm.getObjectById(AccountType.ACCOUNT_TYPE_ID_UNCOLLECTABLE);
			} catch (final JDOObjectNotFoundException x) {
				final AccountType accountType = pm.makePersistent(new AccountType(AccountType.ACCOUNT_TYPE_ID_UNCOLLECTABLE, false));
				accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Uncollectable");
				accountType.getName().setText(Locale.GERMAN.getLanguage(), "Uneinbringlich");
			}

			pm.getFetchPlan().addGroup(FetchPlan.DEFAULT);
			Price.setDefaultTotalPriceFragmentType(pm.detachCopy(PriceFragmentType.getTotalPriceFragmentType(pm)));

			initRegisterConfigModules(pm);

			BugfixWrongOutstandingFlag.fix(pm);
			
			UpdateTo12AccountantStructure.update(pm);

			// check, whether the datastore is already initialized
			try {
				pm.getObjectById(AccountType.ACCOUNT_TYPE_ID_OUTSIDE);
				return; // already initialized
			} catch (final JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}

			// Initalise standard property set structures for articleContainers
			InvoiceStruct.getInvoiceStructLocal(pm);

			pm.makePersistent(
					new ResellerTariffUserSetFactory(Organisation.DEV_ORGANISATION_ID, ResellerTariffUserSetFactory.class.getName(), Tariff.class)
			);
			pm.makePersistent(
					new EntityUserSetNotificationFilterEntry(Organisation.DEV_ORGANISATION_ID, Tariff.class.getName(), Tariff.class)
			);

			// create and persist the AccountTypes
			AccountType accountType;
			accountType = pm.makePersistent(new AccountType(AccountType.ACCOUNT_TYPE_ID_OUTSIDE, true));
			accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Outside");
			accountType.getName().setText(Locale.GERMAN.getLanguage(), "Außerhalb");

			accountType = pm.makePersistent(new AccountType(AccountType.ACCOUNT_TYPE_ID_LOCAL_EXPENSE, false));
			accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Expense");
			accountType.getName().setText(Locale.GERMAN.getLanguage(), "Aufwand");

			accountType = pm.makePersistent(new AccountType(AccountType.ACCOUNT_TYPE_ID_LOCAL_REVENUE, false));
			accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Revenue");
			accountType.getName().setText(Locale.GERMAN.getLanguage(), "Einnahme");

			accountType = pm.makePersistent(new AccountType(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER, false));
			accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Customer");
			accountType.getName().setText(Locale.GERMAN.getLanguage(), "Kunde");

			accountType = pm.makePersistent(new AccountType(AccountType.ACCOUNT_TYPE_ID_PARTNER_NEUTRAL, false));
			accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Business partner");
			accountType.getName().setText(Locale.GERMAN.getLanguage(), "Geschäftspartner");

			accountType = pm.makePersistent(new AccountType(AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR, false));
			accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Vendor");
			accountType.getName().setText(Locale.GERMAN.getLanguage(), "Lieferant");

			accountType = pm.makePersistent(new AccountType(AccountType.ACCOUNT_TYPE_ID_SUMMARY, false));
			accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Summary");
			accountType.getName().setText(Locale.GERMAN.getLanguage(), "Summenkonto");

			final Accounting accounting = Accounting.getAccounting(pm);
			final Trader trader = Trader.getTrader(pm);




			final LegalEntity anonymousCustomer = LegalEntity.getAnonymousLegalEntity(pm);
			final CustomerGroup anonymousCustomerGroup = anonymousCustomer.getDefaultCustomerGroup();

			//		 create some ModeOfPayments
			// Cash
			ModeOfPayment modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_CASH);
			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Cash");
			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Bargeld");
			modeOfPayment.getName().setText(Locale.FRENCH.getLanguage(), "Argent Liquide");
			ModeOfPaymentFlavour modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_CASH);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Cash");
			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Bargeld");
			modeOfPaymentFlavour.getName().setText(Locale.FRENCH.getLanguage(), "Argent Liquide");
			modeOfPaymentFlavour.loadIconFromResource();
			pm.makePersistent(modeOfPayment);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);
			anonymousCustomerGroup.addModeOfPayment(modeOfPayment);

			// we need this later for payment processor registration
			final ModeOfPayment modeOfPaymentCash = modeOfPayment;

			// No payment - this is a dummy MOP which means, the payment is postponed without
			//   specifying a certain real MOP
			modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_NON_PAYMENT);
			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Payment");
			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Nichtzahlung");
			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_NON_PAYMENT);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Payment");
			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Nichtzahlung");
			modeOfPaymentFlavour.loadIconFromResource();
			pm.makePersistent(modeOfPayment);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);

			// we need this later for payment processor registration
			final ModeOfPayment modeOfPaymentNonPayment = modeOfPayment;


			// Credit Card - VISA, Master, AmEx, Diners
			modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_CREDIT_CARD);
			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Credit Card");
			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Kreditkarte");
			modeOfPayment.getName().setText(Locale.FRENCH.getLanguage(), "Carte de Crédit");
			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_VISA);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "VISA");
			modeOfPaymentFlavour.loadIconFromResource();
			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_MASTER_CARD);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "MasterCard");
			modeOfPaymentFlavour.loadIconFromResource();
			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_AMERICAN_EXPRESS);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "American Express");
			modeOfPaymentFlavour.loadIconFromResource();
			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_DINERS_CLUB);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Diners Club");
			modeOfPaymentFlavour.loadIconFromResource();
			pm.makePersistent(modeOfPayment);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);
			anonymousCustomerGroup.addModeOfPayment(modeOfPayment);

			// we need this later for payment processor registration
			final ModeOfPayment modeOfPaymentCreditCard = modeOfPayment;

			// Bank Transfer
			modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_BANK_TRANSFER);
			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Bank Transfer");
			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Überweisung");
			modeOfPayment.getName().setText(Locale.FRENCH.getLanguage(), "Virement");
			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_BANK_TRANSFER);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Bank Transfer");
			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Überweisung");
			modeOfPaymentFlavour.getName().setText(Locale.FRENCH.getLanguage(), "Virement");
			modeOfPaymentFlavour.loadIconFromResource();
			pm.makePersistent(modeOfPayment);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);

			// we need this later for payment processor registration
			final ModeOfPayment modeOfPaymentBankTransfer = modeOfPayment;

			// Debit Note
			modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_DEBIT_NOTE);
			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Debit Note");
			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Lastschrift");
			modeOfPayment.getName().setText(Locale.FRENCH.getLanguage(), "Note de Débit");
			modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_DEBIT_NOTE);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Debit Note");
			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Lastschrift");
			modeOfPaymentFlavour.getName().setText(Locale.FRENCH.getLanguage(), "Note de Débit");
			modeOfPaymentFlavour.loadIconFromResource();
			pm.makePersistent(modeOfPayment);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);

			// we need this later for payment processor registration
			final ModeOfPayment modeOfPaymentDebitNote = modeOfPayment;


			// create some ServerPaymentProcessors
			final ServerPaymentProcessorCash serverPaymentProcessorCash = ServerPaymentProcessorCash.getServerPaymentProcessorCash(pm);
			serverPaymentProcessorCash.getName().setText(Locale.ENGLISH.getLanguage(), "Cash Payment");
			serverPaymentProcessorCash.getName().setText(Locale.GERMAN.getLanguage(), "Barzahlung");
			serverPaymentProcessorCash.getName().setText(Locale.FRENCH.getLanguage(), "Paiement Argent Liquide");
			serverPaymentProcessorCash.addModeOfPayment(modeOfPaymentCash);

			final ServerPaymentProcessorNonPayment serverPaymentProcessorNonPayment =
				ServerPaymentProcessorNonPayment.getServerPaymentProcessorNonPayment(pm);
			serverPaymentProcessorNonPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Payment (payment will be postponed)");
			serverPaymentProcessorNonPayment.getName().setText(Locale.GERMAN.getLanguage(), "Nichtzahlung (Zahlung wird verschoben)");
			serverPaymentProcessorNonPayment.addModeOfPayment(modeOfPaymentNonPayment);

			final ServerPaymentProcessorCreditCardDummyForClientPayment serverPaymentProcessorCreditCardDummyForClientPayment =
				ServerPaymentProcessorCreditCardDummyForClientPayment.getServerPaymentProcessorCreditCardDummyForClientPayment(pm);
			serverPaymentProcessorCreditCardDummyForClientPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Dummy for client-sided Credit Card Payment");
			serverPaymentProcessorCreditCardDummyForClientPayment.getName().setText(Locale.GERMAN.getLanguage(), "Pseudo-Modul für client-seitige Kreditkarten-Zahlungen");
			serverPaymentProcessorCreditCardDummyForClientPayment.addModeOfPayment(modeOfPaymentCreditCard);

			final ServerPaymentProcessorBankTransferGermany serverPaymentProcessorBankTransferGermany = ServerPaymentProcessorBankTransferGermany.getServerPaymentProcessorBankTransferGermany(pm);
			serverPaymentProcessorBankTransferGermany.getName().setText(Locale.ENGLISH.getLanguage(), "Bank transfer within Germany");
			serverPaymentProcessorBankTransferGermany.getName().setText(Locale.GERMAN.getLanguage(), "Überweisung innerhalb Deutschlands");
			serverPaymentProcessorBankTransferGermany.addModeOfPayment(modeOfPaymentBankTransfer);

			final ServerPaymentProcessorDebitNoteGermany serverPaymentProcessorDebitNoteGermany = ServerPaymentProcessorDebitNoteGermany.getServerPaymentProcessorDebitNoteGermany(pm);
			serverPaymentProcessorDebitNoteGermany.getName().setText(Locale.ENGLISH.getLanguage(), "Debit Note within Germany");
			serverPaymentProcessorDebitNoteGermany.getName().setText(Locale.GERMAN.getLanguage(), "Lastschrift innerhalb Deutschlands");
			serverPaymentProcessorDebitNoteGermany.addModeOfPayment(modeOfPaymentDebitNote);


			// persist process definitions
			ProcessDefinition processDefinitionInvoiceCustomerLocal;
			processDefinitionInvoiceCustomerLocal = accounting.storeProcessDefinitionInvoice(TradeSide.customerLocal, ProcessDefinitionAssignment.class.getResource("invoice/customer/local/"));
			pm.makePersistent(new ProcessDefinitionAssignment(Invoice.class, TradeSide.customerLocal, processDefinitionInvoiceCustomerLocal));

			ProcessDefinition processDefinitionInvoiceCustomerCrossOrg;
			processDefinitionInvoiceCustomerCrossOrg = accounting.storeProcessDefinitionInvoice(TradeSide.customerCrossOrganisation, ProcessDefinitionAssignment.class.getResource("invoice/customer/crossorganisation/"));
			pm.makePersistent(new ProcessDefinitionAssignment(Invoice.class, TradeSide.customerCrossOrganisation, processDefinitionInvoiceCustomerCrossOrg));

			ProcessDefinition processDefinitionInvoiceVendor;
			processDefinitionInvoiceVendor = accounting.storeProcessDefinitionInvoice(TradeSide.vendor, ProcessDefinitionAssignment.class.getResource("invoice/vendor/"));
			pm.makePersistent(new ProcessDefinitionAssignment(Invoice.class, TradeSide.vendor, processDefinitionInvoiceVendor));


			// deactive IDGenerator's cache for invoice
			final IDNamespaceDefault idNamespaceDefault = IDNamespaceDefault.createIDNamespaceDefault(pm, getOrganisationID(), Invoice.class);
			idNamespaceDefault.setCacheSizeServer(0);
			idNamespaceDefault.setCacheSizeClient(0);

			pm.makePersistent(new EditLockTypeInvoice(EditLockTypeInvoice.EDIT_LOCK_TYPE_ID));
			
		} finally {
			pm.close();
		}
	}

	/**
	 * Called by {@link #initialise()} and registeres the
	 * config-modules in their config-setup.
	 *
	 * This method checks itself whether initialisation
	 * was performed already and therefore can be safely
	 * called anytime in the process.
	 */
	private void initRegisterConfigModules(final PersistenceManager pm)
	{
		boolean needsUpdate = false;
		// Register all User - ConfigModules
		ConfigSetup configSetup = ConfigSetup.getConfigSetup(
				pm,
				getOrganisationID(),
				UserConfigSetup.CONFIG_SETUP_TYPE_USER
			);
		if (!configSetup.getConfigModuleClasses().contains(ModeOfPaymentConfigModule.class.getName())) {
			configSetup.getConfigModuleClasses().add(ModeOfPaymentConfigModule.class.getName());
			needsUpdate = true;
		}

		// Register all Workstation - ConfigModules
		configSetup = ConfigSetup.getConfigSetup(
				pm,
				getOrganisationID(),
				WorkstationConfigSetup.CONFIG_SETUP_TYPE_WORKSTATION
			);
		if (!configSetup.getConfigModuleClasses().contains(ModeOfPaymentConfigModule.class.getName())) {
			configSetup.getConfigModuleClasses().add(ModeOfPaymentConfigModule.class.getName());
			needsUpdate = true;
		}
		if (needsUpdate)
			ConfigSetup.ensureAllPrerequisites(pm);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getTariffMappingIDs()
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
	public Set<TariffMappingID> getTariffMappingIDs()
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Query q = pm.newQuery(TariffMapping.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<TariffMappingID>((Collection<? extends TariffMappingID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getTariffMappings(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<TariffMapping> getTariffMappings(final Collection<TariffMappingID> tariffMappingIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, tariffMappingIDs, TariffMapping.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#createTariffMapping(org.nightlabs.jfire.accounting.id.TariffID, org.nightlabs.jfire.accounting.id.TariffID, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editTariffMapping")
	@Override
	public TariffMapping createTariffMapping(final TariffID localTariffID, final TariffID partnerTariffID, final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			final TariffMapping tm = TariffMapping.create(pm, localTariffID, partnerTariffID);
			if (!get)
				return null;

			return pm.detachCopy(tm);
		} finally {
			pm.close();
		}
	}

//	@RolesAllowed("_Guest_")
//	@SuppressWarnings("unchecked")
//	@Override
//	public Set<TariffID> getTariffIDs(String organisationID, boolean inverse)
//	{
//		return getTariffIDs(organisationID, inverse, (String[]) null);
//	}

	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
	public Set<TariffID> getTariffIDs(final String organisationID, final boolean inverse)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Map<String, Object> params = new HashMap<String, Object>();
			final StringBuilder filter = new StringBuilder();
			final Query q = pm.newQuery(Tariff.class); // TODO include subclasses or not? Marco.
//			final Query q = pm.newQuery(pm.getExtent(Tariff.class, false));
			q.setResult("JDOHelper.getObjectId(this)");

			if (organisationID != null)
				filter.append("this.organisationID " + (inverse ? "!=" : "==") + " :organisationID");

			params.put("organisationID", organisationID);

//			if (filter.length() > 0)
//				filter.append(" && ");
//
//			filter.append("this.scope == :scope");
//			params.put("scope", Tariff.SCOPE_GLOBAL);

//			if (scope != null && scope.length != 0) {
//				boolean closeBracket = false;
//				if (filter.length() > 0) {
//					closeBracket = true;
//					filter.append(" && (");
//				}
//
//				for (int i = 0; i < scope.length; ++i) {
//					if (i > 0)
//						filter.append(" || ");
//
//					filter.append("this.scope == :scope_").append(i);
//					params.put("scope_" + i, scope[i]);
//				}
//
//				if (closeBracket)
//					filter.append(")");
//			}

			q.setFilter(filter.toString());

			return new HashSet<TariffID>((Collection<? extends TariffID>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getTariffs(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<Tariff> getTariffs(final Collection<TariffID> tariffIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		// TODO filter Tariffs according to visibility-configuration for the currently logged-in user.
		// We should additionally introduce a new access right that allows to see all (suppress filtering)
		// or alternatively only use org.nightlabs.jfire.accounting.editTariff, too. Marco.

		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, tariffIDs, Tariff.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#storeTariff(org.nightlabs.jfire.accounting.Tariff, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editTariff")
	@Override
	public Tariff storeTariff(final Tariff tariff, final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, tariff, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getCurrencies(java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Currency> getCurrencies(final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			final Query q = pm.newQuery(Currency.class);
			return pm.detachCopyAll((Collection<Currency>)q.execute());
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<Currency> getCurrencies(final Collection<CurrencyID> currencyIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, currencyIDs, Currency.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
	public Collection<CurrencyID> getCurrencyIDs() {
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Query q = pm.newQuery(Currency.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<CurrencyID>((Collection<? extends CurrencyID>) q.execute());
		} finally {
			pm.close();
		}
	}

//	/**
//	* @ejb.interface-method
//	* @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	* @ejb.permission role-name="org.nightlabs.jfire.accounting.queryAccounts"
//	*/
//	public Collection<Account> getAccounts(AccountSearchFilter searchFilter,  String[] fetchGroups, int maxFetchDepth)
//	{
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//	if (fetchGroups != null)
//	pm.getFetchPlan().setGroups(fetchGroups);

//	searchFilter.setPersistenceManager(pm);
//	Collection<Account> accounts = (Collection<Account>) searchFilter.getResult();

//	if (fetchGroups != null)
//	pm.getFetchPlan().setGroups(fetchGroups);

////	Collection result = pm.detachCopyAll(accounts);
//	return NLJDOHelper.getDetachedQueryResultAsSet(pm, accounts);
//	} finally {
//	pm.close();
//	}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAccountIDs(org.nightlabs.jfire.accounting.AccountSearchFilter)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryAccounts")
	@Override
	public Set<AnchorID> getAccountIDs(final AccountSearchFilter searchFilter)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			searchFilter.setPersistenceManager(pm);
			return NLJDOHelper.getObjectIDSet(searchFilter.getResult());
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAccounts(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryAccounts")
	@Override
	public List<Account> getAccounts(final Collection<AnchorID> accountIDs,  final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, accountIDs, Account.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#storeAccount(org.nightlabs.jfire.accounting.Account, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editAccount")
	@Override
	public Account storeAccount(final Account account, final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			if (!account.getOrganisationID().equals(getOrganisationID()))
				throw new IllegalArgumentException("Given Account was created for a different organisation, can not store to this datastore!");

			final Account result = NLJDOHelper.storeJDO(pm, account, get, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#setAccountSummaryAccounts(org.nightlabs.jfire.transfer.id.AnchorID, java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editAccount")
	@Override
	public void setAccountSummaryAccounts(final AnchorID anchorID, final Collection<AnchorID> _summaryAccountIDs)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Set<AnchorID> summaryAccountIDs = new HashSet<AnchorID>(_summaryAccountIDs);

			final Account account = (Account)pm.getObjectById(anchorID);
			final Set<SummaryAccount> summaryAccountsToRemove = new HashSet<SummaryAccount>();
			for (final SummaryAccount sa : account.getSummaryAccounts()) {
				if (!summaryAccountIDs.remove(JDOHelper.getObjectId(sa)))
					summaryAccountsToRemove.add(sa);
			}

			for (final SummaryAccount summaryAccount : summaryAccountsToRemove)
				account.removeSummaryAccount(summaryAccount);

			for (final Iterator<AnchorID> iter = summaryAccountIDs.iterator(); iter.hasNext();) {
				final AnchorID summaryAccountID = iter.next();
				final SummaryAccount summaryAccount = (SummaryAccount)pm.getObjectById(summaryAccountID);
				account.addSummaryAccount(summaryAccount);
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#setSummaryAccountSummedAccounts(org.nightlabs.jfire.transfer.id.AnchorID, java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editAccount")
	@Override
	public void setSummaryAccountSummedAccounts(final AnchorID summaryAccountID, final Collection<AnchorID> _summedAccountIDs)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Set<AnchorID> summedAccountIDs = new HashSet<AnchorID>(_summedAccountIDs);

			final SummaryAccount summaryAccount = (SummaryAccount)pm.getObjectById(summaryAccountID);
			final Set<Account> accountsToRemove = new HashSet<Account>();
			for (final Account a : summaryAccount.getSummedAccounts()) {
				if (!summedAccountIDs.remove(JDOHelper.getObjectId(a)))
					accountsToRemove.add(a);
			}

			for (final Account account : accountsToRemove)
				summaryAccount.removeSummedAccount(account);

			for (final Iterator<AnchorID> iter = summedAccountIDs.iterator(); iter.hasNext();) {
				final AnchorID accountID = iter.next();
				final Account account = (Account)pm.getObjectById(accountID);
				summaryAccount.addSummedAccount(account);
			}
		} finally {
			pm.close();
		}
	}

//	/**
//	* TODO is this method still used? What about inheritance? It should be possible to control the inheritance-meta-data via this method, too.
//	* Assign the LocalAccountantDelegate defined by the given localAccountantDelegateID
//	* to the ProductType defined by the given productTypeID.
//	*
//	* @param productTypeID The ProductTypeID of the ProductType to which the delegate should be assigned.
//	* @param localAccountantDelegateID The LocalAccountantDelegateID of the LocalAccountantDelegate to assign.
//	*
//	* @ejb.interface-method
//	* @ejb.transaction type="Required"
//	* @ejb.permission role-name="_Guest_"
//	*/
//	public void assignLocalAccountantDelegateToProductType(
//	ProductTypeID productTypeID,
//	LocalAccountantDelegateID localAccountantDelegateID
//	)
//	{
//	// TODO we should check the Authority of the given ProductType! => authorize this action via the authority!
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	ProductType productType = (ProductType) pm.getObjectById(productTypeID);
//	LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(localAccountantDelegateID);
//	productType.getProductTypeLocal().setLocalAccountantDelegate(localAccountantDelegate);
//	} finally {
//	pm.close();
//	}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getTopLevelAccountantDelegates(java.lang.Class)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates")
	@Override
	public Collection<LocalAccountantDelegateID> getTopLevelAccountantDelegates(final Class<? extends LocalAccountantDelegate> delegateClass)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Collection<? extends LocalAccountantDelegate> delegates = LocalAccountantDelegate.getTopLevelDelegates(pm, delegateClass);
			return NLJDOHelper.getObjectIDSet(delegates);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getChildAccountantDelegates(org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates")
	@Override
	public Collection<LocalAccountantDelegateID> getChildAccountantDelegates(final LocalAccountantDelegateID delegateID)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Collection<? extends LocalAccountantDelegate> delegates = LocalAccountantDelegate.getChildDelegates(
					pm,
					delegateID.organisationID,
					delegateID.localAccountantDelegateID
			);
			return NLJDOHelper.getObjectIDSet(delegates);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getLocalAccountantDelegates(java.util.Collection, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates")
	@Override
	public Collection<LocalAccountantDelegate> getLocalAccountantDelegates(final Collection<LocalAccountantDelegateID> delegateIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, delegateIDs, LocalAccountantDelegate.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getLocalAccountantDelegate(org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates")
	@Override
	public LocalAccountantDelegate getLocalAccountantDelegate(final LocalAccountantDelegateID delegateID, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			final LocalAccountantDelegate delegate = (LocalAccountantDelegate)pm.getObjectById(delegateID);
			return pm.detachCopy(delegate);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#storeLocalAccountantDelegate(org.nightlabs.jfire.accounting.book.LocalAccountantDelegate, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editLocalAccountantDelegate")
	@Override
	public LocalAccountantDelegate storeLocalAccountantDelegate(
			final LocalAccountantDelegate delegate,
			final boolean get,
			final String[] fetchGroups, final int maxFetchDepth
	)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, delegate, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#storeMoneyFlowMapping(org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowMapping, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editLocalAccountantDelegate")
	@Override
	public MoneyFlowMapping storeMoneyFlowMapping(final MoneyFlowMapping mapping, final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return NLJDOHelper.storeJDO(pm, mapping, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	protected Map<ResolvedMapKey, ResolvedMapEntry> getResolvedMoneyFlowMappings(
			final PersistenceManager pm,
			final LocalAccountantDelegateID localAccountantDelegateID,
			final ProductTypeID productTypeID,
			final String[] mappingFetchGroups,
			final int maxFetchDepth
	)
	{
		if (mappingFetchGroups != null)
			pm.getFetchPlan().setGroups(mappingFetchGroups);
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

		final ProductType productType = (ProductType) pm.getObjectById(productTypeID);
		final LocalAccountantDelegate delegate = (LocalAccountantDelegate) pm.getObjectById(localAccountantDelegateID);
		if (!(delegate instanceof MappingBasedAccountantDelegate))
			throw new IllegalArgumentException("MoneyFlowMappings can only be resolved for instances of " + MappingBasedAccountantDelegate.class.getName() + " but object with ID \"" + localAccountantDelegateID + "\" is an instance of " + delegate.getClass().getName());
		final Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings = ((MappingBasedAccountantDelegate) delegate).resolveProductTypeMappings(productType);
		final Map<ResolvedMapKey, ResolvedMapEntry> result = new HashMap<ResolvedMapKey, ResolvedMapEntry>();
		for (final Entry<ResolvedMapKey, ResolvedMapEntry> entry : resolvedMappings.entrySet()) {
			final ResolvedMapEntry persitentMapEntry = entry.getValue();
			final ResolvedMapEntry mapEntry = new ResolvedMapEntry();

			for (final Map.Entry<String, MoneyFlowMapping> resolvedEntry : persitentMapEntry.getResolvedMappings().entrySet()) {
				final MoneyFlowMapping persistentMapping = resolvedEntry.getValue();
				final MoneyFlowMapping detachedMapping = pm.detachCopy(persistentMapping);
				mapEntry.getResolvedMappings().put(resolvedEntry.getKey(), detachedMapping);
			}
			result.put(entry.getKey(), mapEntry);
		}
		return result;

	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getResolvedMoneyFlowMappings(org.nightlabs.jfire.store.id.ProductTypeID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates")
	@Override
	public Map<ResolvedMapKey, ResolvedMapEntry> getResolvedMoneyFlowMappings(final ProductTypeID productTypeID, final String[] mappingFetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final ProductType productType = (ProductType)pm.getObjectById(productTypeID);
			final LocalAccountantDelegate delegate = productType.getProductTypeLocal().getLocalAccountantDelegate();
			if (delegate == null) {
				// TODO maybe we should have a DefaultLocalAccountantDelegate, similar to the Store logic, where there is a DefaultLocalStorekeeperDelegate.
				throw new IllegalArgumentException("The ProductType with id "+productTypeID+" does not have a LocalAccountantDelegate assigned to it.");
			}
			return getResolvedMoneyFlowMappings(pm, (LocalAccountantDelegateID) JDOHelper.getObjectId(delegate), productTypeID, mappingFetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getResolvedMoneyFlowMappings(org.nightlabs.jfire.store.id.ProductTypeID, org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates")
	@Override
	public Map<ResolvedMapKey, ResolvedMapEntry> getResolvedMoneyFlowMappings(final ProductTypeID productTypeID, final LocalAccountantDelegateID delegateID, final String[] mappingFetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			if (mappingFetchGroups != null)
				pm.getFetchPlan().setGroups(mappingFetchGroups);
			return getResolvedMoneyFlowMappings(pm, delegateID, productTypeID, mappingFetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getPriceFragmentTypes(java.util.Collection, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<PriceFragmentType> getPriceFragmentTypes(final Collection<PriceFragmentTypeID> priceFragmentTypeIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return NLJDOHelper.getDetachedObjectList(pm, priceFragmentTypeIDs, PriceFragmentType.class, fetchGroups, maxFetchDepth);
//			if (priceFragmentTypeIDs == null) {
//			Query q = pm.newQuery(PriceFragmentType.class);
//			return pm.detachCopyAll((Collection<PriceFragmentType>)q.execute());
//			}

//			Collection<PriceFragmentType> result = new LinkedList<PriceFragmentType>();
//			for (Iterator iter = priceFragmentTypeIDs.iterator(); iter.hasNext();) {
//			PriceFragmentTypeID priceFragmentTypeID = (PriceFragmentTypeID) iter.next();
//			PriceFragmentType pType = (PriceFragmentType)pm.getObjectById(priceFragmentTypeID);
//			result.add(pType);
//			}
//			return pm.detachCopyAll(result);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getPriceFragmentTypeIDs()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<PriceFragmentTypeID> getPriceFragmentTypeIDs()
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Query q = pm.newQuery(PriceFragmentType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			final Collection<PriceFragmentTypeID> c = CollectionUtil.castCollection((Collection<?>)q.execute());
			return new HashSet<PriceFragmentTypeID>(c);
		} finally {
			pm.close();
		}
	}

//	/**
//	* @ejb.interface-method
//	* @ejb.transaction type="Required"
//	* @ejb.permission role-name="_Guest_"
//	*/
//	public PriceFragmentType getPriceFragmentType(PriceFragmentTypeID priceFragmentTypeID, String[] fetchGroups, int maxFetchDepth)
//	{
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//	if (fetchGroups != null)
//	pm.getFetchPlan().setGroups(fetchGroups);

//	PriceFragmentType priceFragmentType = (PriceFragmentType)pm.getObjectById(priceFragmentTypeID);
//	return pm.detachCopy(priceFragmentType);
//	} finally {
//	pm.close();
//	}
//	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#createInvoice(java.util.Collection, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editInvoice")
	@Override
	public Invoice createInvoice(
			final Collection<ArticleID> articleIDs, final String invoiceIDPrefix,
			final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	throws InvoiceEditException
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Article.class);
			final User user = User.getUser(pm, getPrincipal());
			final Trader trader = Trader.getTrader(pm);
			final Accounting accounting = trader.getAccounting();

			final ArrayList<Article> articles = new ArrayList<Article>(articleIDs.size());
			for (final ArticleID articleID : articleIDs) {
				final Article article = (Article) pm.getObjectById(articleID);
				final Offer offer = article.getOffer();
				trader.validateOffer(offer);
				trader.acceptOfferImplicitely(offer);
				articles.add(article);
			}

			final Invoice invoice = accounting.createInvoice(user, articles, invoiceIDPrefix);
			accounting.validateInvoice(invoice);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS + FetchPlan.DETACH_UNLOAD_FIELDS);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return pm.detachCopy(invoice);
			}
			return null;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#createInvoice(org.nightlabs.jfire.trade.id.ArticleContainerID, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editInvoice")
	@Override
	public Invoice createInvoice(
			final ArticleContainerID articleContainerID, final String invoiceIDPrefix,
			final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	throws InvoiceEditException
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			if (articleContainerID == null)
				throw new IllegalArgumentException("articleContainerID must not be null!");

			if (articleContainerID instanceof OrderID)
				pm.getExtent(Order.class);
			else if (articleContainerID instanceof OfferID)
				pm.getExtent(Offer.class);
			else if (articleContainerID instanceof DeliveryNoteID)
				pm.getExtent(DeliveryNote.class);
			else
				throw new IllegalArgumentException("articleContainerID must be an instance of OrderID, OfferID or DeliveryNoteID, but is " + articleContainerID.getClass().getName());

			final ArticleContainer articleContainer = (ArticleContainer)pm.getObjectById(articleContainerID);

			final User user = User.getUser(pm, getPrincipal());
			final Trader trader = Trader.getTrader(pm);
			final Accounting accounting = trader.getAccounting();

			if (articleContainer instanceof Offer) {
				final Offer offer = (Offer) articleContainer;
//				OfferLocal offerLocal = offer.getOfferLocal();
				trader.validateOffer(offer);
				trader.acceptOfferImplicitely(offer);
//				trader.finalizeOffer(user, offer);
//				trader.acceptOffer(user, offerLocal);
//				trader.confirmOffer(user, offerLocal);
			}
			else {
				final Set<Offer> offers = new HashSet<Offer>();
				for (final Article article : articleContainer.getArticles()) {
					final Offer offer = article.getOffer();
					offers.add(offer);
				}
				for (final Iterator<Offer> it = offers.iterator(); it.hasNext(); ) {
					final Offer offer = it.next();
					if (!offer.isAborted()) {
						trader.validateOffer(offer);
						trader.acceptOfferImplicitely(offer);
					}
//					trader.finalizeOffer(user, offer);
//					trader.acceptOffer(user, offerLocal);
//					trader.confirmOffer(user, offerLocal);
				}
			}

			final Invoice invoice = accounting.createInvoice(user, articleContainer, invoiceIDPrefix);
			accounting.validateInvoice(invoice);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS + FetchPlan.DETACH_UNLOAD_FIELDS);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return pm.detachCopy(invoice);
			}
			return null;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#addArticlesToInvoice(org.nightlabs.jfire.accounting.id.InvoiceID, java.util.Collection, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editInvoice")
	@Override
	public Invoice addArticlesToInvoice(
			final InvoiceID invoiceID, final Collection<ArticleID> articleIDs,
			final boolean validate, final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	throws InvoiceEditException
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Invoice.class);
			pm.getExtent(Article.class);
			final Trader trader = Trader.getTrader(pm);

			final Invoice invoice = (Invoice) pm.getObjectById(invoiceID);
			final Collection<Article> articles = new ArrayList<Article>(articleIDs.size());
			for (final ArticleID articleID : articleIDs) {
				final Article article = (Article) pm.getObjectById(articleID);
				final Offer offer = article.getOffer();
				trader.validateOffer(offer);
				trader.acceptOfferImplicitely(offer);
				articles.add(article);
			}

			final Accounting accounting = Accounting.getAccounting(pm);
			accounting.addArticlesToInvoice(User.getUser(pm, getPrincipal()), invoice, articles);

			if (validate)
				accounting.validateInvoice(invoice);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(invoice);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#removeArticlesFromInvoice(org.nightlabs.jfire.accounting.id.InvoiceID, java.util.Collection, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editInvoice")
	@Override
	public Invoice removeArticlesFromInvoice(
			final InvoiceID invoiceID, final Collection<ArticleID> articleIDs,
			final boolean validate, final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	throws InvoiceEditException
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Invoice.class);
			pm.getExtent(Article.class);
			final Invoice invoice = (Invoice) pm.getObjectById(invoiceID);
			final Collection<Article> articles = NLJDOHelper.getObjectList(pm, articleIDs, Article.class);
//			Collection<Article> articles = new ArrayList<Article>(articleIDs.size());
//			for (ArticleID articleID : articleIDs) {
//			articles.add((Article) pm.getObjectById(articleID));
//			}

			final Accounting accounting = Accounting.getAccounting(pm);
			accounting.removeArticlesFromInvoice(User.getUser(pm, getPrincipal()), invoice, articles);

			if (!get)
				return null;

			if (validate)
				accounting.validateInvoice(invoice);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(invoice);
		} finally {
			pm.close();
		}
	}

	@EJB
	private AccountingManagerLocal accountingManagerLocal;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#payBegin(java.util.List)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.pay")
	@Override
	public List<PaymentResult> payBegin(final List<PaymentData> paymentDataList)
	{
		try {
			final List<PaymentResult> resList = new ArrayList<PaymentResult>();
			for (final PaymentData paymentData : paymentDataList) {
				PaymentResult res = null;
				try {
					res = accountingManagerLocal._payBegin(paymentData);
				} catch (final Throwable t) {
					PaymentException x = null;
					if (t instanceof PaymentException)
						x = (PaymentException)t;
					else {
						final int i = ExceptionUtils.indexOfThrowable(t, PaymentException.class);
						if (i >= 0)
							x = (PaymentException)ExceptionUtils.getThrowables(t)[i];
					}
					if (x != null)
						res = x.getPaymentResult();
					else
						res = new PaymentResult(getOrganisationID(), t);
				}
				if (res != null)
					resList.add(res);

			}
			return resList;
		} catch (final RuntimeException x) {
			throw x;
		} catch (final Exception x) {
			throw new RuntimeException(x);
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#payBegin(org.nightlabs.jfire.accounting.pay.PaymentData)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.pay")
	@Override
	public PaymentResult payBegin(final PaymentData paymentData)
	{
		return _payBegin(paymentData);
	}

	@EJB
	private PaymentHelperLocal paymentHelperLocal;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerLocal#_payBegin(org.nightlabs.jfire.accounting.pay.PaymentData)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public PaymentResult _payBegin(final PaymentData paymentData)
	{
		if (paymentData == null)
			throw new NullPointerException("paymentData");

		if (paymentData.getPayment() == null)
			throw new NullPointerException("paymentData.getPayment() is null!");

		if (paymentData.getPayment().getPartnerID() == null) {
			// if no partner is defined, at least one invoice must be known!
			if (paymentData.getPayment().getPayableObjectIDs() == null)
				throw new NullPointerException("paymentData.getPayment().getPartnerID() and paymentData.getPayment().getInvoiceIDs() are both null! One of them must be specified, because I need to know who's paying!");

			if (paymentData.getPayment().getPayableObjectIDs().isEmpty())
				throw new NullPointerException("paymentData.getPayment().getPartnerID() is null and paymentData.getPayment().getInvoiceIDs() is empty! If no partner is specified explicitely, I need at least one invoice to find out who's paying!");
		}

		if (paymentData.getPayment().getCurrencyID() == null)
			throw new NullPointerException("paymentData.getPayment().getCurrencyID() is null!");

		if (paymentData.getPayment().getAmount() < 0)
			throw new IllegalArgumentException("paymentData.getPayment().getAmount() < 0!");

		if (paymentData.getPayment().getModeOfPaymentFlavourID() == null)
			throw new NullPointerException("paymentData.getPayment().getModeOfPaymentFlavourID() is null!");

		if (paymentData.getPayment().getPayBeginClientResult() == null)
			throw new NullPointerException("paymentData.getPayment().getPayBeginClientResult() is null!");

		// Store paymentData into the database within a NEW TRANSACTION to prevent it
		// from being deleted (if this method fails later and causes a rollback).
		final PaymentDataID paymentDataID = paymentHelperLocal.payBegin_storePaymentData(paymentData);

		final String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return paymentHelperLocal.payBegin_internal(paymentDataID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (final Throwable t) {
			logger.error("payBegin_internal(...) failed: " + paymentDataID, t);
			final PaymentResult payBeginServerResult = new PaymentResult(getOrganisationID(), t);

			try {
				return paymentHelperLocal.payBegin_storePayBeginServerResult(
						PaymentID.create(paymentDataID), payBeginServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			} catch (final RuntimeException x) {
				throw x;
			} catch (final Exception x) {
				throw new RuntimeException(x);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#payDoWork(java.util.List, java.util.List, boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<PaymentResult> payDoWork(final List<PaymentID> paymentIDs, final List<PaymentResult> payDoWorkClientResults, final boolean forceRollback)
	{
		if (paymentIDs.size() != payDoWorkClientResults.size())
			throw new IllegalArgumentException("paymentIDs.size() != payDoWorkClientResults.size()!!!");

		final List<PaymentResult> resList = new ArrayList<PaymentResult>();
		final Iterator<PaymentResult> itResults = payDoWorkClientResults.iterator();
		for (final Iterator<PaymentID> itIDs = paymentIDs.iterator(); itIDs.hasNext(); ) {
			final PaymentID paymentID = itIDs.next();
			final PaymentResult payDoWorkClientResult = itResults.next();

			PaymentResult res = null;
			try {
				res = accountingManagerLocal._payDoWork(paymentID, payDoWorkClientResult, forceRollback);
			} catch (final Throwable t) {
				PaymentException x = null;
				if (t instanceof PaymentException)
					x = (PaymentException)t;
				else {
					final int i = ExceptionUtils.indexOfThrowable(t, PaymentException.class);
					if (i >= 0)
						x = (PaymentException)ExceptionUtils.getThrowables(t)[i];
				}
				if (x != null)
					res = x.getPaymentResult();
				else
					res = new PaymentResult(getOrganisationID(), t);
			}
			if (res != null)
				resList.add(res);

		}
		return resList;
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#payDoWork(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public PaymentResult payDoWork(
			final PaymentID paymentID,
			final PaymentResult payEndClientResult,
			final boolean forceRollback)
	{
		return _payDoWork(paymentID, payEndClientResult, forceRollback);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerLocal#_payDoWork(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public PaymentResult _payDoWork(
			final PaymentID paymentID,
			final PaymentResult payDoWorkClientResult,
			final boolean forceRollback)
	{
		if (paymentID == null)
			throw new NullPointerException("paymentID");

		if (payDoWorkClientResult == null)
			throw new NullPointerException("payDoWorkClientResult");

		// Store payDoWorkClientResult into the database within a NEW TRANSACTION to
		// prevent it from being lost (if this method fails later and causes a rollback).
		try {
			paymentHelperLocal.payDoWork_storePayDoWorkClientResult(
					paymentID, payDoWorkClientResult, forceRollback
			);
		} catch (final RuntimeException x) {
			throw x;
		} catch (final Exception x) {
			throw new RuntimeException(x);
		}

		final String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return paymentHelperLocal.payDoWork_internal(paymentID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (final Throwable t) {
			logger.error("payDoWork_internal(...) failed: " + paymentID, t);
			final PaymentResult payDoWorkServerResult = new PaymentResult(getOrganisationID(), t);

			try {
				final PaymentResult payDoWorkServerResult_detached = paymentHelperLocal.payDoWork_storePayDoWorkServerResult(
						paymentID, payDoWorkServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				return payDoWorkServerResult_detached;
			} catch (final RuntimeException x) {
				throw x;
			} catch (final Exception x) {
				throw new RuntimeException(x);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#payEnd(java.util.List, java.util.List, boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<PaymentResult> payEnd(final List<PaymentID> paymentIDs, final List<PaymentResult> payEndClientResults, final boolean forceRollback)
	{
		try {
			if (paymentIDs.size() != payEndClientResults.size())
				throw new IllegalArgumentException("paymentIDs.size() != payEndClientResults.size()!!!");

			final List<PaymentResult> resList = new ArrayList<PaymentResult>();
			final Iterator<PaymentResult> itResults = payEndClientResults.iterator();
			for (final Iterator<PaymentID> itIDs = paymentIDs.iterator(); itIDs.hasNext(); ) {
				final PaymentID paymentID = itIDs.next();
				final PaymentResult payEndClientResult = itResults.next();

				PaymentResult res = null;
				try {
					res = accountingManagerLocal._payEnd(paymentID, payEndClientResult, forceRollback);
				} catch (final Throwable t) {
					PaymentException x = null;
					if (t instanceof PaymentException)
						x = (PaymentException)t;
					else {
						final int i = ExceptionUtils.indexOfThrowable(t, PaymentException.class);
						if (i >= 0)
							x = (PaymentException)ExceptionUtils.getThrowables(t)[i];
					}
					if (x != null)
						res = x.getPaymentResult();
					else
						res = new PaymentResult(getOrganisationID(), t);
				}
				if (res != null)
					resList.add(res);

			}
			return resList;
		} catch (final RuntimeException x) {
			throw x;
		} catch (final Exception x) {
			throw new RuntimeException(x);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#payEnd(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public PaymentResult payEnd(
			final PaymentID paymentID,
			final PaymentResult payEndClientResult,
			final boolean forceRollback)
	{
		return _payEnd(paymentID, payEndClientResult, forceRollback);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerLocal#_payEnd(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public PaymentResult _payEnd(
			final PaymentID paymentID,
			final PaymentResult payEndClientResult,
			final boolean forceRollback)
	{
		if (paymentID == null)
			throw new NullPointerException("paymentID");

		if (payEndClientResult == null)
			throw new NullPointerException("payEndClientResult");

		// Store payEndClientResult into the database within a NEW TRANSACTION to
		// prevent it from being lost (if this method fails later and causes a rollback).
		try {
			paymentHelperLocal.payEnd_storePayEndClientResult(
					paymentID, payEndClientResult, forceRollback
			);
		} catch (final RuntimeException x) {
			throw x;
		} catch (final Exception x) {
			throw new RuntimeException(x);
		}

		final String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return paymentHelperLocal.payEnd_internal(paymentID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (final Throwable t) {
			logger.error("payEnd_internal(...) failed: " + paymentID, t);
			final PaymentResult payEndServerResult = new PaymentResult(getOrganisationID(), t);

			try {
				final PaymentResult payEndServerResult_detached = paymentHelperLocal.payEnd_storePayEndServerResult(
						paymentID, payEndServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				paymentHelperLocal.payRollback(paymentID);

				return payEndServerResult_detached;
			} catch (final RuntimeException x) {
				throw x;
			} catch (final Exception x) {
				throw new RuntimeException(x);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getInvoiceIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryInvoices")
	@SuppressWarnings("unchecked")
	@Override
	public Set<InvoiceID> getInvoiceIDs(QueryCollection<? extends AbstractJDOQuery> invoiceQueries)
	{
		if (invoiceQueries == null)
			return null;

		if (! Invoice.class.isAssignableFrom(invoiceQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ invoiceQueries.getResultClassName());
		}

		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (! (invoiceQueries instanceof JDOQueryCollectionDecorator))
			{
				invoiceQueries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(invoiceQueries);
			}

			final JDOQueryCollectionDecorator<AbstractJDOQuery> queryCollection =
				(JDOQueryCollectionDecorator<AbstractJDOQuery>) invoiceQueries;

			queryCollection.setPersistenceManager(pm);
			final Collection<Invoice> invoices = (Collection<Invoice>) queryCollection.executeQueries();

			return NLJDOHelper.getObjectIDSet(invoices);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getInvoices(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryInvoices")
	@Override
	public List<Invoice> getInvoices(final Set<InvoiceID> invoiceIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, invoiceIDs, Invoice.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getInvoiceIDs(org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, long, long)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryInvoices")
	@Override
	public List<InvoiceID> getInvoiceIDs(final AnchorID vendorID, final AnchorID customerID, final AnchorID endCustomerID, final long rangeBeginIdx, final long rangeEndIdx)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return new ArrayList<InvoiceID>(Invoice.getInvoiceIDs(pm, vendorID, customerID, endCustomerID, rangeBeginIdx, rangeEndIdx));
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups != null)
//			pm.getFetchPlan().setGroups(fetchGroups);
//			return (List) pm.detachCopyAll(Invoice.getInvoices(pm, vendorID, customerID, rangeBeginIdx, rangeEndIdx));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getNonFinalizedInvoices(org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryInvoices")
	@Override
	public List<Invoice> getNonFinalizedInvoices(final AnchorID vendorID, final AnchorID customerID, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return (List<Invoice>) pm.detachCopyAll(Invoice.getNonFinalizedInvoices(pm, vendorID, customerID));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAvailableModeOfPaymentFlavoursForAllCustomerGroups(java.util.Collection, byte, boolean, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
			final Collection<CustomerGroupID> customerGroupIDs, final byte mergeMode, final boolean filterByConfig, final String[] fetchGroups, final int maxFetchDepth)
			{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			final Collection<ModeOfPaymentFlavour> c = ModeOfPaymentFlavour.getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
					pm, customerGroupIDs, mergeMode, filterByConfig);

			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
			}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAllModeOfPaymentIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<ModeOfPaymentID> getAllModeOfPaymentIDs() {
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Query q = pm.newQuery(ModeOfPayment.class);
			q.setResult("JDOHelper.getObjectId(this)");
			final Collection<ModeOfPaymentID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<ModeOfPaymentID>(c);
//			return ModeOfPayment.getAllModeOfPaymentIDs(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getModeOfPayments(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ModeOfPayment> getModeOfPayments(final Set<ModeOfPaymentID> modeOfPaymentIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, modeOfPaymentIDs, ModeOfPayment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAllModeOfPaymentFlavourIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<ModeOfPaymentFlavourID> getAllModeOfPaymentFlavourIDs() {
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Query q = pm.newQuery(ModeOfPaymentFlavour.class);
			q.setResult("JDOHelper.getObjectId(this)");
			final Collection<ModeOfPaymentFlavourID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<ModeOfPaymentFlavourID>(c);
//			return ModeOfPaymentFlavour.getAllModeOfPaymentFlavourIDs(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getModeOfPaymentFlavours(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ModeOfPaymentFlavour> getModeOfPaymentFlavours(final Set<ModeOfPaymentFlavourID> modeOfPaymentFlavourIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, modeOfPaymentFlavourIDs, ModeOfPaymentFlavour.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getServerPaymentProcessorsForOneModeOfPaymentFlavour(org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID, org.nightlabs.jfire.accounting.pay.CheckRequirementsEnvironment, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ServerPaymentProcessor> getServerPaymentProcessorsForOneModeOfPaymentFlavour(
			final ModeOfPaymentFlavourID modeOfPaymentFlavourID,
			final CheckRequirementsEnvironment checkRequirementsEnvironment,
			final String[] fetchGroups, final int maxFetchDepth)
			{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<ServerPaymentProcessor> c = ServerPaymentProcessor.getServerPaymentProcessorsForOneModeOfPaymentFlavour(
					pm, modeOfPaymentFlavourID);

			for (final ServerPaymentProcessor pp : c) {
				pp.checkRequirements(checkRequirementsEnvironment);
			}

			// Because the checkRequirements method might have manipulated the fetch-plan, we set it again.
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			c = pm.detachCopyAll(c);
			return c;
		} finally {
			pm.close();
		}
			}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAvailableModeOfPaymentFlavoursForOneCustomerGroup(org.nightlabs.jfire.trade.id.CustomerGroupID, boolean, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForOneCustomerGroup(final CustomerGroupID customerGroupID, final boolean filterByConfig, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			final Collection<ModeOfPaymentFlavour> c = ModeOfPaymentFlavour.getAvailableModeOfPaymentFlavoursForOneCustomerGroup(
					pm, customerGroupID, filterByConfig);

			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getProductTypeForPriceConfigEditing(org.nightlabs.jfire.store.id.ProductTypeID)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.editPriceConfiguration")
	@Override
	public ProductType getProductTypeForPriceConfigEditing(final ProductTypeID productTypeID)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return GridPriceConfigUtil.detachProductTypeForPriceConfigEditing(pm, productTypeID);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#signalInvoice(org.nightlabs.jfire.accounting.id.InvoiceID, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editInvoice")
	@Override
	public void signalInvoice(final InvoiceID invoiceID, final String jbpmTransitionName)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			Accounting.getAccounting(pm).signalInvoice(invoiceID, jbpmTransitionName);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAffectedProductTypes(java.util.Set, org.nightlabs.jfire.store.id.ProductTypeID, org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.editPriceConfiguration")
	@Override
	public Map<PriceConfigID, List<AffectedProductType>> getAffectedProductTypes(final Set<PriceConfigID> priceConfigIDs, final ProductTypeID productTypeID, final PriceConfigID innerPriceConfigID)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			// TODO implement this method correctly to take the will-be-assigned innerPriceConfig into account instead of the currently assigned one!
			return PriceConfigUtil.getAffectedProductTypes(pm, priceConfigIDs, productTypeID, innerPriceConfigID);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAccountIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryAccounts")
	@SuppressWarnings("unchecked")
	@Override
	public Set<AnchorID> getAccountIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! Account.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (!(queries instanceof JDOQueryCollectionDecorator))
			{
				queries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(queries);
			}
			final JDOQueryCollectionDecorator<AbstractJDOQuery> decoratedQueries =
				(JDOQueryCollectionDecorator<AbstractJDOQuery>) queries;

			decoratedQueries.setPersistenceManager(pm);
			final Collection<Account> accounts = (Collection<Account>) decoratedQueries.executeQueries();

			return NLJDOHelper.getObjectIDSet(accounts);
		} finally {
			pm.close();
		}
	}

//	/**
//	* assign a new {@link LocalAccountantDelegate} to a {@link ProductType}
//	*
//	* @param productTypeID the id of the productType to assign a new {@link LocalAccountantDelegate} to
//	* @param delegateID The ID of the LocalAccountantDelegate to assign
//	* @param get Whether or not to return the a newly detached version of the stored productType
//	* @param fetchGroups The fetchGroups to detach
//	* @param maxFetchDepth the maxium fetchDepth
//	* @throws ModuleException
//	*
//	* @ejb.interface-method
//	* @ejb.transaction type="Required"
//	* @ejb.permission role-name="_Guest_"
//	*/
//	public ProductType assignLocalAccountantDelegate(ProductTypeID productTypeID,
//	LocalAccountantDelegateID delegateID,
//	boolean get,
//	String[] fetchGroups, int maxFetchDepth
//	)
//	throws ModuleException
//	{
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	ProductType productType = (ProductType) pm.getObjectById(productTypeID);
//	LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(delegateID);
//	productType.setLocalAccountantDelegate(localAccountantDelegate);
//	if (!get)
//	return null;

//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//	if (fetchGroups != null)
//	pm.getFetchPlan().setGroups(fetchGroups);

//	return (ProductType) pm.detachCopy(productType);
//	} finally {
//	pm.close();
//	}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#createManualMoneyTransfer(org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.accounting.id.CurrencyID, long, org.nightlabs.i18n.I18nText, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.manualMoneyTransfer")
	@Override
	public ManualMoneyTransfer createManualMoneyTransfer(
			final AnchorID fromID, final AnchorID toID, final CurrencyID currencyID, final long amount, final I18nText reason,
			final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try{
			// the JavaEE server knows who we are - get the User object corresponding to the currently working principal
			final User user = User.getUser(pm, getPrincipal());

			// initialise meta-data for Account & Currency - not really necessary, but still safer
			pm.getExtent(Account.class);
			pm.getExtent(Currency.class);

			// load the objects for the given IDs
			final Account from = (Account) pm.getObjectById(fromID);
			final Account to = (Account) pm.getObjectById(toID);
			final Currency currency = (Currency) pm.getObjectById(currencyID);

			// delegate to Accounting for the actual creation + booking
			final ManualMoneyTransfer manualMoneyTransfer = Accounting.getAccounting(pm).createManualMoneyTransfer(user, from, to, currency, amount, reason);

			// if the client doesn't need it, we simply return null here
			if (!get)
				return null;

			// otherwise, we set the desired fetch-plan
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			// and return the detached ManualMoneyTransfer
			return pm.detachCopy(manualMoneyTransfer);
		} finally{
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getMoneyTransferIDs(org.nightlabs.jfire.accounting.query.MoneyTransferIDQuery)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryMoneyTransfers")
	@SuppressWarnings("unchecked")
	@Override
	public List<TransferID> getMoneyTransferIDs(final MoneyTransferIDQuery productTransferIDQuery)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			productTransferIDQuery.setPersistenceManager(pm);
			return new ArrayList<TransferID>((Collection<TransferID>) productTransferIDQuery.getResult());
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getMoneyTransferIDs(java.util.Collection)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryMoneyTransfers")
	@SuppressWarnings("unchecked")
	public List<TransferID> getMoneyTransferIDs(final Collection<MoneyTransferQuery> moneyTransferQueries)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			Collection<MoneyTransfer> productTransfers = null;
			for (final MoneyTransferQuery productTransferQuery : moneyTransferQueries) {
				productTransferQuery.setPersistenceManager(pm);
				productTransferQuery.setCandidates(productTransfers);
				productTransfers = (Collection<MoneyTransfer>) productTransferQuery.getResult();
			}

			return NLJDOHelper.getObjectIDList(productTransfers);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getMoneyTransfers(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryMoneyTransfers")
	@Override
	public List<MoneyTransfer> getMoneyTransfers(final Collection<TransferID> moneyTransferIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, moneyTransferIDs, MoneyTransfer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAccountTypeIDs()
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
	public Set<AccountTypeID> getAccountTypeIDs()
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			final Query q = pm.newQuery(AccountType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<AccountTypeID>((Collection<? extends AccountTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}

	
	@RolesAllowed("_Guest_")
	@Override
	public Set<PaymentID> getPaymentIDsForPayableObjectID(ObjectID payableObjectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			final PayableObject payableObject = (PayableObject) pm.getObjectById(payableObjectID);
			return NLJDOHelper.getObjectIDSet(Payment.getPaymentsForPayableObject(pm, payableObject));
		} finally {
			pm.close();
		}		
	}
	
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAccountTypes(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<Payment> getPayments(final Collection<PaymentID> paymentIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, paymentIDs, Payment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getAccountTypes(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<AccountType> getAccountTypes(final Collection<AccountTypeID> accountTypeIDs, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, accountTypeIDs, AccountType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#storePriceFragmentType(org.nightlabs.jfire.accounting.PriceFragmentType, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editPriceFragmentType")
	@Override
	public PriceFragmentType storePriceFragmentType(final PriceFragmentType priceFragmentType, final boolean get, final String[] fetchGroups, final int maxFetchDepth)
	{
		final PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, priceFragmentType, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("org.nightlabs.jfire.accounting.editCurrency")
	@Override
	public Currency storeCurrency(final Currency currency, final boolean get,final String[] fetchGroups, final int maxFetchDepth) {
		final PersistenceManager pm = createPersistenceManager();
		try {
             return NLJDOHelper.storeJDO(pm, currency, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.AccountingManagerRemote#getMoneyTransferIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryAccounts")
	@SuppressWarnings("unchecked")
	@Override
	public Set<TransferID> getMoneyTransferIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (!MoneyTransfer.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		final PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (!(queries instanceof JDOQueryCollectionDecorator))
			{
				queries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(queries);
			}
			final JDOQueryCollectionDecorator<AbstractJDOQuery> decoratedQueries =
				(JDOQueryCollectionDecorator<AbstractJDOQuery>) queries;

			decoratedQueries.setPersistenceManager(pm);
			final Collection<MoneyTransfer> moneyTransfers = (Collection<MoneyTransfer>) decoratedQueries.executeQueries();

			return NLJDOHelper.getObjectIDSet(moneyTransfers);
		} finally {
			pm.close();
		}
	}
}
