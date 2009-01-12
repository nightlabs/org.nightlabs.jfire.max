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
import java.rmi.RemoteException;
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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.accounting.book.mappingbased.MappingBasedAccountantDelegate;
import org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowDimension;
import org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowMapping;
import org.nightlabs.jfire.accounting.book.mappingbased.OwnerDimension;
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
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.PaymentHelperLocal;
import org.nightlabs.jfire.accounting.pay.PaymentHelperUtil;
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
import org.nightlabs.jfire.accounting.query.MoneyTransferIDQuery;
import org.nightlabs.jfire.accounting.query.MoneyTransferQuery;
import org.nightlabs.jfire.accounting.tariffuserset.SecurityChangeListenerTariffUserSet;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.idgenerator.IDNamespaceDefault;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
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
import org.nightlabs.jfire.trade.query.InvoiceQuery;
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
public abstract class AccountingManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AccountingManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
	}
	/**
	 * {@inheritDoc}
	 *
	 * @ejb.permission unchecked="true"
	 */
	@Override
	public void ejbRemove() throws EJBException, RemoteException
	{
	}
	@Override
	public void ejbActivate() throws EJBException, RemoteException
	{
	}
	@Override
	public void ejbPassivate() throws EJBException, RemoteException
	{
	}

	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise()
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			SecurityChangeListenerTariffUserSet.register(pm);

			MoneyFlowDimension priceFragmentDimension = MoneyFlowDimension.getMoneyFlowDimension(pm, PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID);
			if (priceFragmentDimension == null) {
				priceFragmentDimension = new PriceFragmentDimension();
				pm.makePersistent(priceFragmentDimension);
			}

			MoneyFlowDimension ownerDimension = MoneyFlowDimension.getMoneyFlowDimension(pm, OwnerDimension.MONEY_FLOW_DIMENSION_ID);
			if (ownerDimension == null) {
				ownerDimension = new OwnerDimension();
				pm.makePersistent(ownerDimension);
			}

			MoneyFlowDimension sourceOrgDimension = MoneyFlowDimension.getMoneyFlowDimension(pm, SourceOrganisationDimension.MONEY_FLOW_DIMENSION_ID);
			if (sourceOrgDimension == null) {
				sourceOrgDimension = new SourceOrganisationDimension();
				pm.makePersistent(sourceOrgDimension);
			}

			initRegisterConfigModules(pm);
			
			// check, whether the datastore is already initialized
			pm.getExtent(Currency.class);
			try {
				pm.getObjectById(CurrencyID.create("EUR"), true);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}

			// Create the currencies EUR and CHF
			Currency currency;

			// TODO currencySymbol (second "EUR") should be €,
			// but this doesn't work yet because of a charset problem with the db
			currency = new Currency("EUR", "EUR", 2);
			pm.makePersistent(currency);

			currency = new Currency("CHF", "CHF", 2);
			pm.makePersistent(currency);


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


			// create PriceFragmentTypes for Swiss and German VAT
			PriceFragmentType priceFragmentType = new PriceFragmentType(PriceFragmentTypeHelper.getDE().VAT_DE_19_NET);
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Germany 19% Net");
			priceFragmentType.getName().setText(Locale.GERMAN.getLanguage(), "MwSt. Deutschland 19% Netto");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(PriceFragmentTypeHelper.getDE().VAT_DE_19_VAL);
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Germany 19% Value");
			priceFragmentType.getName().setText(Locale.GERMAN.getLanguage(), "MwSt. Deutschland 19% Wert");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(PriceFragmentTypeHelper.getDE().VAT_DE_7_NET);
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Germany 7% Net");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(PriceFragmentTypeHelper.getDE().VAT_DE_7_VAL);
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Germany 7% Value");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(PriceFragmentTypeHelper.getCH().VAT_CH_7_6_NET);
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Switzerland 7.6% Net");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(PriceFragmentTypeHelper.getCH().VAT_CH_7_6_VAL);
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Switzerland 7.6% Value");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			Accounting accounting = Accounting.getAccounting(pm);
			Trader trader = Trader.getTrader(pm);




			LegalEntity anonymousCustomer = LegalEntity.getAnonymousLegalEntity(pm);
			CustomerGroup anonymousCustomerGroup = anonymousCustomer.getDefaultCustomerGroup();

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
			ModeOfPayment modeOfPaymentCash = modeOfPayment;

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
			ModeOfPayment modeOfPaymentNonPayment = modeOfPayment;


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
			ModeOfPayment modeOfPaymentCreditCard = modeOfPayment;

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
			ModeOfPayment modeOfPaymentBankTransfer = modeOfPayment;

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
			ModeOfPayment modeOfPaymentDebitNote = modeOfPayment;


			// create some ServerPaymentProcessors
			ServerPaymentProcessorCash serverPaymentProcessorCash = ServerPaymentProcessorCash.getServerPaymentProcessorCash(pm);
			serverPaymentProcessorCash.getName().setText(Locale.ENGLISH.getLanguage(), "Cash Payment");
			serverPaymentProcessorCash.getName().setText(Locale.GERMAN.getLanguage(), "Barzahlung");
			serverPaymentProcessorCash.getName().setText(Locale.FRENCH.getLanguage(), "Paiement Argent Liquide");
			serverPaymentProcessorCash.addModeOfPayment(modeOfPaymentCash);

			ServerPaymentProcessorNonPayment serverPaymentProcessorNonPayment =
				ServerPaymentProcessorNonPayment.getServerPaymentProcessorNonPayment(pm);
			serverPaymentProcessorNonPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Payment (payment will be postponed)");
			serverPaymentProcessorNonPayment.getName().setText(Locale.GERMAN.getLanguage(), "Nichtzahlung (Zahlung wird verschoben)");
			serverPaymentProcessorNonPayment.addModeOfPayment(modeOfPaymentNonPayment);

			ServerPaymentProcessorCreditCardDummyForClientPayment serverPaymentProcessorCreditCardDummyForClientPayment =
				ServerPaymentProcessorCreditCardDummyForClientPayment.getServerPaymentProcessorCreditCardDummyForClientPayment(pm);
			serverPaymentProcessorCreditCardDummyForClientPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Dummy for client-sided Credit Card Payment");
			serverPaymentProcessorCreditCardDummyForClientPayment.getName().setText(Locale.GERMAN.getLanguage(), "Pseudo-Modul für client-seitige Kreditkarten-Zahlungen");
			serverPaymentProcessorCreditCardDummyForClientPayment.addModeOfPayment(modeOfPaymentCreditCard);

			ServerPaymentProcessorBankTransferGermany serverPaymentProcessorBankTransferGermany = ServerPaymentProcessorBankTransferGermany.getServerPaymentProcessorBankTransferGermany(pm);
			serverPaymentProcessorBankTransferGermany.getName().setText(Locale.ENGLISH.getLanguage(), "Bank transfer within Germany");
			serverPaymentProcessorBankTransferGermany.getName().setText(Locale.GERMAN.getLanguage(), "Überweisung innerhalb Deutschlands");
			serverPaymentProcessorBankTransferGermany.addModeOfPayment(modeOfPaymentBankTransfer);

			ServerPaymentProcessorDebitNoteGermany serverPaymentProcessorDebitNoteGermany = ServerPaymentProcessorDebitNoteGermany.getServerPaymentProcessorDebitNoteGermany(pm);
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
			IDNamespaceDefault idNamespaceDefault = IDNamespaceDefault.createIDNamespaceDefault(pm, getOrganisationID(), Invoice.class);
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
	private void initRegisterConfigModules(PersistenceManager pm)
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
	
	/**
	 * Get the object-ids of all existing {@link TariffMapping}s.
	 * <p>
	 * This method can be called by everyone, because it does not reveal critical data.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<TariffMappingID> getTariffMappingIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(TariffMapping.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<TariffMappingID>((Collection<? extends TariffMappingID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the object-ids of all existing {@link TariffMapping}s.
	 * <p>
	 * This method can be called by everyone, because it does not reveal critical data.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<TariffMapping> getTariffMappings(Collection<TariffMappingID> tariffMappingIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, tariffMappingIDs, TariffMapping.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editTariffMapping"
	 */
	public TariffMapping createTariffMapping(TariffID localTariffID, TariffID partnerTariffID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			TariffMapping tm = TariffMapping.create(pm, localTariffID, partnerTariffID);
			if (!get)
				return null;

			return pm.detachCopy(tm);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the object-ids of all {@link Tariff}s or the ones matching (or not matching, if <code>inverse == true</code>)
	 * the given <code>organisationID</code>.
	 * <p>
	 * This method can be called by everyone, because it does not reveal any sensitive information.
	 * </p>
	 *
	 * @param organisationID <code>null</code> in order to get all tariffs (no filtering). non-<code>null</code> to filter by <code>organisationID</code>.
	 * @param inverse This applies only if <code>organisationID != null</code>. If <code>true</code>, it will return all {@link TariffID}s where the <code>organisationID</code>
	 *		is NOT the one passed as parameter <code>organisationID</code>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<TariffID> getTariffIDs(String organisationID, boolean inverse)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Tariff.class);
			q.setResult("JDOHelper.getObjectId(this)");
			if (organisationID != null)
				q.setFilter("this.organisationID " + (inverse ? "!=" : "==") + " :organisationID");

			return new HashSet<TariffID>((Collection<? extends TariffID>) q.execute(organisationID));
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the {@link Tariff}s for the specified object-ids.
	 * <p>
	 * Everyone can call this method. At the moment, all requested {@link Tariff}s will be returned. Later on, this
	 * method will filter the result and only give the user those <code>Tariff</code>s he is allowed to see.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<Tariff> getTariffs(Collection<TariffID> tariffIDs, String[] fetchGroups, int maxFetchDepth)
	{
		// TODO filter Tariffs according to visibility-configuration for the currently logged-in user.
		// We should additionally introduce a new access right that allows to see all (suppress filtering)
		// or alternatively only use org.nightlabs.jfire.accounting.editTariff, too. Marco.

		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, tariffIDs, Tariff.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Save a new or modified <code>Tariff</code> to the server.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editTariff"
	 */
	public Tariff storeTariff(Tariff tariff, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, tariff, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get all {@link Currency} instances.
	 * <p>
	 * This method can be called by everyone, because currencies are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<Currency> getCurrencies(String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(Currency.class);
			return pm.detachCopyAll((Collection<Currency>)q.execute());
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

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryAccounts"
	 */
	@SuppressWarnings("unchecked")
	public Set<AnchorID> getAccountIDs(AccountSearchFilter searchFilter)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			searchFilter.setPersistenceManager(pm);
			return NLJDOHelper.getObjectIDSet(searchFilter.getResult());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryAccounts"
	 */
	@SuppressWarnings("unchecked")
	public List<Account> getAccounts(Collection<AnchorID> accountIDs,  String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, accountIDs, Account.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editAccount"
	 */
	public Account storeAccount(Account account, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (!account.getOrganisationID().equals(getOrganisationID()))
				throw new IllegalArgumentException("Given Account was created for a different organisation, can not store to this datastore!");

			Account result = NLJDOHelper.storeJDO(pm, account, get, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * @param anchorID The anchorID of the Account wich SummaryAccounts are to be set
	 * @param SummaryAccounts A Collection of the AnchorIDs of the SummaryAccounts to be set to the given Account.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editAccount"
	 */
	public void setAccountSummaryAccounts(AnchorID anchorID, Collection<AnchorID> _summaryAccountIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Set<AnchorID> summaryAccountIDs = new HashSet<AnchorID>(_summaryAccountIDs);

			Account account = (Account)pm.getObjectById(anchorID);
			Set<SummaryAccount> summaryAccountsToRemove = new HashSet<SummaryAccount>();
			for (SummaryAccount sa : account.getSummaryAccounts()) {
				if (!summaryAccountIDs.remove(JDOHelper.getObjectId(sa)))
					summaryAccountsToRemove.add(sa);
			}

			for (SummaryAccount summaryAccount : summaryAccountsToRemove)
				account.removeSummaryAccount(summaryAccount);

			for (Iterator<AnchorID> iter = summaryAccountIDs.iterator(); iter.hasNext();) {
				AnchorID summaryAccountID = iter.next();
				SummaryAccount summaryAccount = (SummaryAccount)pm.getObjectById(summaryAccountID);
				account.addSummaryAccount(summaryAccount);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @param summaryAccountID The anchorID of the SummaryAccount wich summedAccounts are to be set
	 * @param summedAccountIDs A Collection of the AnchorIDs of the Accounts to be summed up by the given SummaryAccount.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editAccount"
	 */
	public void setSummaryAccountSummedAccounts(AnchorID summaryAccountID, Collection<AnchorID> _summedAccountIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Set<AnchorID> summedAccountIDs = new HashSet<AnchorID>(_summedAccountIDs);

			SummaryAccount summaryAccount = (SummaryAccount)pm.getObjectById(summaryAccountID);
			Set<Account> accountsToRemove = new HashSet<Account>();
			for (Account a : summaryAccount.getSummedAccounts()) {
				if (!summedAccountIDs.remove(JDOHelper.getObjectId(a)))
					accountsToRemove.add(a);
			}

			for (Account account : accountsToRemove)
				summaryAccount.removeSummedAccount(account);

			for (Iterator<AnchorID> iter = summedAccountIDs.iterator(); iter.hasNext();) {
				AnchorID accountID = iter.next();
				Account account = (Account)pm.getObjectById(accountID);
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

	/**
	 * Returns a Collection of {@link LocalAccountantDelegateID} not detached
	 * delegates.
	 *
	 * @param delegateClass The class/type of delegates that should be returned.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryLocalAccountantDelegates"
	 */
	public Collection<LocalAccountantDelegateID> getTopLevelAccountantDelegates(Class<? extends LocalAccountantDelegate> delegateClass)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<? extends LocalAccountantDelegate> delegates = LocalAccountantDelegate.getTopLevelDelegates(pm, delegateClass);
			return NLJDOHelper.getObjectIDSet(delegates);
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns a Clloection of {@link LocalAccountantDelegateID} not detached
	 * delegates which have the given delegate as extendedLocalAccountantDelegate
	 *
	 * @param delegateID The LocalAccountantDelegateID children should be searched for.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryLocalAccountantDelegates"
	 */
	public Collection<LocalAccountantDelegateID> getChildAccountantDelegates(LocalAccountantDelegateID delegateID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<? extends LocalAccountantDelegate> delegates = LocalAccountantDelegate.getChildDelegates(
					pm,
					delegateID.organisationID,
					delegateID.localAccountantDelegateID
			);
			return NLJDOHelper.getObjectIDSet(delegates);
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns detached instances of the LocalAccountantDelegates referenced by
	 * the given LocalAccountantDelegateIDs in the delegateIDs parameter.
	 *
	 * @param delegateIDs The LocalAccountantDelegateID of the delegates to return.
	 * @param fetchGroups The fetchGroups to detach the delegates with.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryLocalAccountantDelegates"
	 */
	public Collection<LocalAccountantDelegate> getLocalAccountantDelegates(Collection<LocalAccountantDelegateID> delegateIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, delegateIDs, LocalAccountantDelegate.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns the detached LocalAccountantDelegates referenced by
	 * the given LocalAccountantDelegateID.
	 *
	 * @param delegateID The LocalAccountantDelegateID of the delegate to return.
	 * @param fetchGroups The fetchGroups to detach the delegates with.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryLocalAccountantDelegates"
	 */
	public LocalAccountantDelegate getLocalAccountantDelegate(LocalAccountantDelegateID delegateID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			LocalAccountantDelegate delegate = (LocalAccountantDelegate)pm.getObjectById(delegateID);
			return pm.detachCopy(delegate);
		} finally {
			pm.close();
		}
	}

	/**
	 * Stores the given LocalAccountantDelegate and returns a newly detached
	 * version of it if desired.
	 *
	 * @param delegate The LocalAccountantDelegate to store.
	 * @param get Whether or not to return the a newly detached version of the stored delegate.
	 * @param fetchGroups The fetchGroups to detach the stored delegate with.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editLocalAccountantDelegate"
	 */
	public LocalAccountantDelegate storeLocalAccountantDelegate(
			LocalAccountantDelegate delegate,
			boolean get,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, delegate, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editLocalAccountantDelegate"
	 * @ejb.transaction type="Required"
	 */
	public MoneyFlowMapping storeMoneyFlowMapping(MoneyFlowMapping mapping, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return NLJDOHelper.storeJDO(pm, mapping, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	protected Map<ResolvedMapKey, ResolvedMapEntry> getResolvedMoneyFlowMappings(
			PersistenceManager pm,
			LocalAccountantDelegateID localAccountantDelegateID,
			ProductTypeID productTypeID,
			String[] mappingFetchGroups,
			int maxFetchDepth
	)
	{
		if (mappingFetchGroups != null)
			pm.getFetchPlan().setGroups(mappingFetchGroups);
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

		ProductType productType = (ProductType) pm.getObjectById(productTypeID);
		LocalAccountantDelegate delegate = (LocalAccountantDelegate) pm.getObjectById(localAccountantDelegateID);
		if (!(delegate instanceof MappingBasedAccountantDelegate))
			throw new IllegalArgumentException("MoneyFlowMappings can only be resolved for instances of " + MappingBasedAccountantDelegate.class.getName() + " but object with ID \"" + localAccountantDelegateID + "\" is an instance of " + delegate.getClass().getName());
		Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings = ((MappingBasedAccountantDelegate) delegate).resolveProductTypeMappings(productType);
		Map<ResolvedMapKey, ResolvedMapEntry> result = new HashMap<ResolvedMapKey, ResolvedMapEntry>();
		for (Entry<ResolvedMapKey, ResolvedMapEntry> entry : resolvedMappings.entrySet()) {
			ResolvedMapEntry persitentMapEntry = entry.getValue();
			ResolvedMapEntry mapEntry = new ResolvedMapEntry();

			for (Map.Entry<String, MoneyFlowMapping> resolvedEntry : persitentMapEntry.getResolvedMappings().entrySet()) {
				MoneyFlowMapping persistentMapping = resolvedEntry.getValue();
				MoneyFlowMapping detachedMapping = pm.detachCopy(persistentMapping);
				mapEntry.getResolvedMappings().put(resolvedEntry.getKey(), detachedMapping);
			}
			result.put(entry.getKey(), mapEntry);
		}
		return result;

	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryLocalAccountantDelegates"
	 */
	public Map<ResolvedMapKey, ResolvedMapEntry> getResolvedMoneyFlowMappings(ProductTypeID productTypeID, String[] mappingFetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ProductType productType = (ProductType)pm.getObjectById(productTypeID);
			LocalAccountantDelegate delegate = productType.getProductTypeLocal().getLocalAccountantDelegate();
			if (delegate == null) {
				// TODO maybe we should have a DefaultLocalAccountantDelegate, similar to the Store logic, where there is a DefaultLocalStorekeeperDelegate.
				throw new IllegalArgumentException("The ProductType with id "+productTypeID+" does not have a LocalAccountantDelegate assigned to it.");
			}
			return getResolvedMoneyFlowMappings(pm, (LocalAccountantDelegateID) JDOHelper.getObjectId(delegate), productTypeID, mappingFetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryLocalAccountantDelegates"
	 */
	public Map<ResolvedMapKey, ResolvedMapEntry> getResolvedMoneyFlowMappings(ProductTypeID productTypeID, LocalAccountantDelegateID delegateID, String[] mappingFetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (mappingFetchGroups != null)
				pm.getFetchPlan().setGroups(mappingFetchGroups);
			return getResolvedMoneyFlowMappings(pm, delegateID, productTypeID, mappingFetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get all {@link PriceFragmentType}s or those specified by <code>priceFragmentTypeIDs</code>.
	 * <p>
	 * This method can be called by everyone, since price fragment types don't reveal any information
	 * except their name. This is - at least for now - not considered confidential.
	 * </p>
	 *
	 * @param priceFragmentTypeIDs Can be <code>null</code> in order to return ALL {@link PriceFragmentType}s or a collection of {@link PriceFragmentTypeID}s to return only a subset.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection<PriceFragmentType> getPriceFragmentTypes(Collection<PriceFragmentTypeID> priceFragmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * Get a <code>Collection</code> of <code>PriceFragmentTypeID</code> of all known <code>PriceFragementType</code>s.
	 * <p>
	 * This method can be called by everyone, because the object-ids don't reveal any confidential information.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection<PriceFragmentTypeID> getPriceFragmentTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(PriceFragmentType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<PriceFragmentTypeID> c = CollectionUtil.castCollection((Collection<?>)q.execute());
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


	/**
	 * Creates an Invoice for all specified <code>Article</code>s. If
	 * get is true, a detached version of the new Invoice will be returned.
	 *
	 * @param articleIDs The {@link ArticleID}s of those {@link Article}s that shall be added to the new <code>Invoice</code>.
	 * @param get Whether a detached version of the created Invoice should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the invoice should be detached with.
	 * @return Detached Invoice or null.
	 *
	 * @throws InvoiceEditException if the invoice cannot be created with the given parameters.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editInvoice"
	 */
	public Invoice createInvoice(
			Collection<ArticleID> articleIDs, String invoiceIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws InvoiceEditException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Article.class);
			User user = User.getUser(pm, getPrincipal());
			Trader trader = Trader.getTrader(pm);
			Accounting accounting = trader.getAccounting();

			ArrayList<Article> articles = new ArrayList<Article>(articleIDs.size());
			for (Iterator<ArticleID> it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = it.next();
				Article article = (Article) pm.getObjectById(articleID);
				Offer offer = article.getOffer();
//				OfferLocal offerLocal = offer.getOfferLocal();
				trader.validateOffer(offer);
				trader.acceptOfferImplicitely(offer);
//				trader.finalizeOffer(user, offer);
//				trader.acceptOffer(user, offerLocal);
//				trader.confirmOffer(user, offerLocal);
				articles.add(article);
			}

			Invoice invoice = accounting.createInvoice(user, articles, invoiceIDPrefix);
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

	/**
	 * Creates an Invoice for all Articles of the Offer identified by
	 * the given offerID. If get is true, a detached version of the
	 * new Invoice will be returned.
	 *
	 * @param offerID OfferID of the offer to be billed.
	 * @param get Whether a detached version of the created Invoice should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the invoice should be detached with.
	 * @return Detached Invoice or null.
	 *
	 * @throws InvoiceEditException if the invoice cannot be created with the given parameters.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editInvoice"
	 */
	public Invoice createInvoice(
			ArticleContainerID articleContainerID, String invoiceIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws InvoiceEditException
	{
		PersistenceManager pm = getPersistenceManager();
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

			ArticleContainer articleContainer = (ArticleContainer)pm.getObjectById(articleContainerID);

			User user = User.getUser(pm, getPrincipal());
			Trader trader = Trader.getTrader(pm);
			Accounting accounting = trader.getAccounting();

			if (articleContainer instanceof Offer) {
				Offer offer = (Offer) articleContainer;
//				OfferLocal offerLocal = offer.getOfferLocal();
				trader.validateOffer(offer);
				trader.acceptOfferImplicitely(offer);
//				trader.finalizeOffer(user, offer);
//				trader.acceptOffer(user, offerLocal);
//				trader.confirmOffer(user, offerLocal);
			}
			else {
				Set<Offer> offers = new HashSet<Offer>();
				for (Article article : articleContainer.getArticles()) {
					Offer offer = article.getOffer();
					offers.add(offer);
				}
				for (Iterator<Offer> it = offers.iterator(); it.hasNext(); ) {
					Offer offer = it.next();
					trader.validateOffer(offer);
					trader.acceptOfferImplicitely(offer);
//					trader.finalizeOffer(user, offer);
//					trader.acceptOffer(user, offerLocal);
//					trader.confirmOffer(user, offerLocal);
				}
			}

			Invoice invoice = accounting.createInvoice(user, articleContainer, invoiceIDPrefix);
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

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editInvoice"
	 */
	public Invoice addArticlesToInvoice(
			InvoiceID invoiceID, Collection<ArticleID> articleIDs,
			boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Invoice.class);
			pm.getExtent(Article.class);
			Invoice invoice = (Invoice) pm.getObjectById(invoiceID);
			Collection<Article> articles = new ArrayList<Article>(articleIDs.size());
			for (ArticleID articleID : articleIDs) {
				articles.add((Article) pm.getObjectById(articleID));
			}

			Accounting accounting = Accounting.getAccounting(pm);
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

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editInvoice"
	 */
	public Invoice removeArticlesFromInvoice(
			InvoiceID invoiceID, Collection<ArticleID> articleIDs,
			boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Invoice.class);
			pm.getExtent(Article.class);
			Invoice invoice = (Invoice) pm.getObjectById(invoiceID);
			Collection<Article> articles = NLJDOHelper.getObjectList(pm, articleIDs, Article.class);
//			Collection<Article> articles = new ArrayList<Article>(articleIDs.size());
//			for (ArticleID articleID : articleIDs) {
//			articles.add((Article) pm.getObjectById(articleID));
//			}

			Accounting accounting = Accounting.getAccounting(pm);
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


	/**
	 * @param paymentDataList A <tt>List</tt> of {@link PaymentData}.
	 * @return A <tt>List</tt> with instances of {@link PaymentResult} in the same
	 *		order as and corresponding to the {@link PaymentData} objects passed in
	 *		<tt>paymentDataList</tt>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.pay"
	 */
	public List<PaymentResult> payBegin(List<PaymentData> paymentDataList)
	throws ModuleException
	{
		try {
			AccountingManagerLocal accountingManagerLocal = AccountingManagerUtil.getLocalHome().create();

			List<PaymentResult> resList = new ArrayList<PaymentResult>();
			for (PaymentData paymentData : paymentDataList) {
				PaymentResult res = null;
				try {
					res = accountingManagerLocal._payBegin(paymentData);
				} catch (Throwable t) {
					PaymentException x = null;
					if (t instanceof PaymentException)
						x = (PaymentException)t;
					else {
						int i = ExceptionUtils.indexOfThrowable(t, PaymentException.class);
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
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}


	/**
	 * @param serverPaymentProcessorID Might be <tt>null</tt>.
	 * @param paymentDirection Either
	 *		{@link ServerPaymentProcessor#PAYMENT_DIRECTION_INCOMING}
	 *		or {@link ServerPaymentProcessor#PAYMENT_DIRECTION_OUTGOING}.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#payBegin(User, PaymentData)
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.pay"
	 */
	public PaymentResult payBegin(PaymentData paymentData)
	throws ModuleException
	{
		return _payBegin(paymentData);
	}

	/**
	 * This method does not require access right control, because it can only be called internally (see view-type="local").
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult _payBegin(PaymentData paymentData)
	throws ModuleException
	{
		if (paymentData == null)
			throw new NullPointerException("paymentData");

		if (paymentData.getPayment() == null)
			throw new NullPointerException("paymentData.getPayment() is null!");

		if (paymentData.getPayment().getPartnerID() == null) {
			// if no partner is defined, at least one invoice must be known!
			if (paymentData.getPayment().getInvoiceIDs() == null)
				throw new NullPointerException("paymentData.getPayment().getPartnerID() and paymentData.getPayment().getInvoiceIDs() are both null! One of them must be specified, because I need to know who's paying!");

			if (paymentData.getPayment().getInvoiceIDs().isEmpty())
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
		PaymentDataID paymentDataID;
		PaymentHelperLocal paymentHelperLocal;
		try {
			paymentHelperLocal = PaymentHelperUtil.getLocalHome().create();
			paymentDataID = paymentHelperLocal.payBegin_storePaymentData(paymentData);
		} catch (ModuleException x) {
			throw x;
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}

		String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return paymentHelperLocal.payBegin_internal(paymentDataID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (Throwable t) {
			logger.error("payBegin_internal(...) failed: " + paymentDataID, t);
			PaymentResult payBeginServerResult = new PaymentResult(getOrganisationID(), t);

			try {
				return paymentHelperLocal.payBegin_storePayBeginServerResult(
						PaymentID.create(paymentDataID), payBeginServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			} catch (ModuleException x) {
				throw x;
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}
		}
	}


	/**
	 * This method does not require access right control, because it can only be performed, if <code>payBegin</code> was
	 * called before, which is restricted.
	 *
	 * @param paymentIDs Instances of {@link PaymentID}
	 * @param payDoWorkClientResults Instances of {@link PaymentResult} corresponding
	 *		to the <tt>paymentIDs</tt>. Hence, both lists must have the same number of items.
	 * @param forceRollback If <tt>true</tt> all payies will be rolled back, even if they
	 *		have been successful so far.
	 * @return A <tt>List</tt> with instances of {@link PaymentResult} in the same
	 *		order as and corresponding to the {@link PaymentID} objects passed in
	 *		<tt>paymentIDs</tt>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<PaymentResult> payDoWork(List<PaymentID> paymentIDs, List<PaymentResult> payDoWorkClientResults, boolean forceRollback)
	throws ModuleException
	{
		try {
			AccountingManagerLocal accountingManagerLocal = AccountingManagerUtil.getLocalHome().create();

			if (paymentIDs.size() != payDoWorkClientResults.size())
				throw new IllegalArgumentException("paymentIDs.size() != payDoWorkClientResults.size()!!!");

			List<PaymentResult> resList = new ArrayList<PaymentResult>();
			Iterator<PaymentResult> itResults = payDoWorkClientResults.iterator();
			for (Iterator<PaymentID> itIDs = paymentIDs.iterator(); itIDs.hasNext(); ) {
				PaymentID paymentID = itIDs.next();
				PaymentResult payDoWorkClientResult = itResults.next();

				PaymentResult res = null;
				try {
					res = accountingManagerLocal._payDoWork(paymentID, payDoWorkClientResult, forceRollback);
				} catch (Throwable t) {
					PaymentException x = null;
					if (t instanceof PaymentException)
						x = (PaymentException)t;
					else {
						int i = ExceptionUtils.indexOfThrowable(t, PaymentException.class);
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
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}


	/**
	 * This method does not require access right control, because it can only be performed, if <code>payBegin</code> was
	 * called before, which is restricted.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#payEnd(User, PaymentData)
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payDoWork(
			PaymentID paymentID,
			PaymentResult payEndClientResult,
			boolean forceRollback)
	throws ModuleException
	{
		return _payDoWork(paymentID, payEndClientResult, forceRollback);
	}

	/**
	 * This method does not require access right control, because it can only be called internally (see view-type="local").
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult _payDoWork(
			PaymentID paymentID,
			PaymentResult payDoWorkClientResult,
			boolean forceRollback)
	throws ModuleException
	{
		if (paymentID == null)
			throw new NullPointerException("paymentID");

		if (payDoWorkClientResult == null)
			throw new NullPointerException("payDoWorkClientResult");

		// Store payDoWorkClientResult into the database within a NEW TRANSACTION to
		// prevent it from being lost (if this method fails later and causes a rollback).
		PaymentHelperLocal paymentHelperLocal;
		try {
			paymentHelperLocal = PaymentHelperUtil.getLocalHome().create();
			paymentHelperLocal.payDoWork_storePayDoWorkClientResult(
					paymentID, payDoWorkClientResult, forceRollback);
		} catch (ModuleException x) {
			throw x;
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}

		String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return paymentHelperLocal.payDoWork_internal(paymentID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (Throwable t) {
			logger.error("payDoWork_internal(...) failed: " + paymentID, t);
			PaymentResult payDoWorkServerResult = new PaymentResult(getOrganisationID(), t);

			try {
				PaymentResult payDoWorkServerResult_detached = paymentHelperLocal.payDoWork_storePayDoWorkServerResult(
						paymentID, payDoWorkServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				return payDoWorkServerResult_detached;
			} catch (ModuleException x) {
				throw x;
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}
		}
	}


	/**
	 * This method does not require access right control, because it can only be performed, if <code>payBegin</code> was
	 * called before, which is restricted.
	 *
	 * @param paymentIDs Instances of {@link PaymentID}
	 * @param payEndClientResults Instances of {@link PaymentResult} corresponding
	 *		to the <tt>paymentIDs</tt>. Hence, both lists must have the same number of items.
	 * @param forceRollback If <tt>true</tt> all payments will be rolled back, even if they
	 *		have been successful so far.
	 * @return A <tt>List</tt> with instances of {@link PaymentResult} in the same
	 *		order as and corresponding to the {@link PaymentID} objects passed in
	 *		<tt>paymentIDs</tt>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<PaymentResult> payEnd(List<PaymentID> paymentIDs, List<PaymentResult> payEndClientResults, boolean forceRollback)
	throws ModuleException
	{
		try {
			AccountingManagerLocal accountingManagerLocal = AccountingManagerUtil.getLocalHome().create();

			if (paymentIDs.size() != payEndClientResults.size())
				throw new IllegalArgumentException("paymentIDs.size() != payEndClientResults.size()!!!");

			List<PaymentResult> resList = new ArrayList<PaymentResult>();
			Iterator<PaymentResult> itResults = payEndClientResults.iterator();
			for (Iterator<PaymentID> itIDs = paymentIDs.iterator(); itIDs.hasNext(); ) {
				PaymentID paymentID = itIDs.next();
				PaymentResult payEndClientResult = itResults.next();

				PaymentResult res = null;
				try {
					res = accountingManagerLocal._payEnd(paymentID, payEndClientResult, forceRollback);
				} catch (Throwable t) {
					PaymentException x = null;
					if (t instanceof PaymentException)
						x = (PaymentException)t;
					else {
						int i = ExceptionUtils.indexOfThrowable(t, PaymentException.class);
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
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * This method does not require access right control, because it can only be performed, if <code>payBegin</code> was
	 * called before, which is restricted.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#payEnd(User, PaymentData)
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payEnd(
			PaymentID paymentID,
			PaymentResult payEndClientResult,
			boolean forceRollback)
	throws ModuleException
	{
		return _payEnd(paymentID, payEndClientResult, forceRollback);
	}

	/**
	 * This method does not require access right control, because it can only be called internally (see view-type="local").
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult _payEnd(
			PaymentID paymentID,
			PaymentResult payEndClientResult,
			boolean forceRollback)
	throws ModuleException
	{
		if (paymentID == null)
			throw new NullPointerException("paymentID");

		if (payEndClientResult == null)
			throw new NullPointerException("payEndClientResult");

		// Store payEndClientResult into the database within a NEW TRANSACTION to
		// prevent it from being lost (if this method fails later and causes a rollback).
		PaymentHelperLocal paymentHelperLocal;
		try {
			paymentHelperLocal = PaymentHelperUtil.getLocalHome().create();
			paymentHelperLocal.payEnd_storePayEndClientResult(
					paymentID, payEndClientResult, forceRollback);
		} catch (ModuleException x) {
			throw x;
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}

		String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return paymentHelperLocal.payEnd_internal(paymentID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (Throwable t) {
			logger.error("payEnd_internal(...) failed: " + paymentID, t);
			PaymentResult payEndServerResult = new PaymentResult(getOrganisationID(), t);

			try {
				PaymentResult payEndServerResult_detached = paymentHelperLocal.payEnd_storePayEndServerResult(
						paymentID, payEndServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				paymentHelperLocal.payRollback(paymentID);

				return payEndServerResult_detached;
			} catch (ModuleException x) {
				throw x;
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}
		}
	}

	/**
	 * @param invoiceQueries Instances of {@link InvoiceQuery} that shall be chained
	 *		in order to retrieve the result. The result of one query is passed to the
	 *		next one using the {@link AbstractJDOQuery#setCandidates(Collection)}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryInvoices"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<InvoiceID> getInvoiceIDs(QueryCollection<? extends AbstractJDOQuery> invoiceQueries)
	{
		if (invoiceQueries == null)
			return null;

		if (! Invoice.class.isAssignableFrom(invoiceQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ invoiceQueries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (! (invoiceQueries instanceof JDOQueryCollectionDecorator))
			{
				invoiceQueries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(invoiceQueries);
			}

			JDOQueryCollectionDecorator<AbstractJDOQuery> queryCollection =
				(JDOQueryCollectionDecorator<AbstractJDOQuery>) invoiceQueries;

			queryCollection.setPersistenceManager(pm);
			Collection<Invoice> invoices = (Collection<Invoice>) queryCollection.executeQueries();

			return NLJDOHelper.getObjectIDSet(invoices);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryInvoices"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public List<Invoice> getInvoices(Set<InvoiceID> invoiceIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, invoiceIDs, Invoice.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method queries all <code>Invoice</code>s which exist between the given vendor and customer.
	 * They are ordered by invoiceID descending (means newest first).
	 *
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link Invoice}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryInvoices"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public List<InvoiceID> getInvoiceIDs(AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return new ArrayList<InvoiceID>(Invoice.getInvoiceIDs(pm, vendorID, customerID, rangeBeginIdx, rangeEndIdx));
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups != null)
//			pm.getFetchPlan().setGroups(fetchGroups);
//			return (List) pm.detachCopyAll(Invoice.getInvoices(pm, vendorID, customerID, rangeBeginIdx, rangeEndIdx));
		} finally {
			pm.close();
		}
	}

	/**
	 * This method queries all <code>Invoice</code>s which exist between the given vendor and customer and
	 * are not yet finalized. They are ordered by invoiceID descending (means newest first).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryInvoices"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public List<Invoice> getNonFinalizedInvoices(AnchorID vendorID, AnchorID customerID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return (List<Invoice>) pm.detachCopyAll(Invoice.getNonFinalizedInvoices(pm, vendorID, customerID));
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the mode of payment flavours that are available to the specified customer groups.
	 * <p>
	 * This method can be called by everyone, because mode of payment flavours are not considered confidential.
	 * </p>
	 *
	 * @param customerGroupIDs A <tt>Collection</tt> of {@link CustomerGroupID}. If <tt>null</tt>, all {@link ModeOfPaymentFlavour}s will be returned.
	 * @param mergeMode one of {@link ModeOfPaymentFlavour#MERGE_MODE_INTERSECTION} or {@link ModeOfPaymentFlavour#MERGE_MODE_UNION}
	 * @param filterByConfig 
	 * 		If this is <code>true</code> the flavours available found for the given customer-groups will also be filtered by the 
	 * 		intersection of the entries configured in the {@link ModeOfPaymentConfigModule} for the current user and the 
	 * 		workstation he is currently loggen on. 
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
			Collection<CustomerGroupID> customerGroupIDs, byte mergeMode, boolean filterByConfig, String[] fetchGroups, int maxFetchDepth)
			{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<ModeOfPaymentFlavour> c = ModeOfPaymentFlavour.getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
					pm, customerGroupIDs, mergeMode, filterByConfig);

			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
			}

	/**
	 * Get the object-ids of all modes of payment.
	 * <p>
	 * This method can be called by everyone, because the returned object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<ModeOfPaymentID> getAllModeOfPaymentIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			return ModeOfPayment.getAllModeOfPaymentIDs(pm);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the modes of payment that are specified by the given object-ids.
	 * <p>
	 * This method can be called by everyone, because modes of payment are not considered confidential.
	 * </p>
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<ModeOfPaymentFlavour> getModeOfPayments(Set<ModeOfPaymentID> modeOfPaymentIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, modeOfPaymentIDs, ModeOfPayment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the object-ids of all mode of payment flavours.
	 * <p>
	 * This method can be called by everyone, because the returned object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<ModeOfPaymentFlavourID> getAllModeOfPaymentFlavourIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			return ModeOfPaymentFlavour.getAllModeOfPaymentFlavourIDs(pm);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the mode of payment flavours that are specified by the given object-ids.
	 * <p>
	 * This method can be called by everyone, because mode of payment flavours are not considered confidential.
	 * </p>
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<ModeOfPaymentFlavour> getModeOfPaymentFlavours(Set<ModeOfPaymentFlavourID> modeOfPaymentFlavourIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, modeOfPaymentFlavourIDs, ModeOfPaymentFlavour.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the server payment processors available for the specified mode of payment flavour.
	 * <p>
	 * This method can be called by everyone, because the returned server payment processors do not reveal confidential data.
	 * </p>
	 *
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<ServerPaymentProcessor> getServerPaymentProcessorsForOneModeOfPaymentFlavour(
			ModeOfPaymentFlavourID modeOfPaymentFlavourID,
			CheckRequirementsEnvironment checkRequirementsEnvironment,
			String[] fetchGroups, int maxFetchDepth)
			{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<ServerPaymentProcessor> c = ServerPaymentProcessor.getServerPaymentProcessorsForOneModeOfPaymentFlavour(
					pm, modeOfPaymentFlavourID);

			for (ServerPaymentProcessor pp : c) {
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

	/**
	 * Get the mode of payment flavours that are available to the specified customer group.
	 * <p>
	 * This method can be called by everyone, because the mode of payment flavours are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForOneCustomerGroup(CustomerGroupID customerGroupID, boolean filterByConfig, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<ModeOfPaymentFlavour> c = ModeOfPaymentFlavour.getAvailableModeOfPaymentFlavoursForOneCustomerGroup(
					pm, customerGroupID, filterByConfig);

			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
	}

	/**
	 * @param productTypeID The object ID of the desired ProductType.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editPriceConfiguration"
	 */
	public ProductType getProductTypeForPriceConfigEditing(ProductTypeID productTypeID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return GridPriceConfigUtil.detachProductTypeForPriceConfigEditing(pm, productTypeID);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editInvoice"
	 */
	public void signalInvoice(InvoiceID invoiceID, String jbpmTransitionName)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Accounting.getAccounting(pm).signalInvoice(invoiceID, jbpmTransitionName);
		} finally {
			pm.close();
		}
	}

	/**
	 * @return The returned Map&lt;PriceConfigID, List&lt;AffectedProductType&gt;&gt; indicates which modified
	 *		price config would result in which products to have their prices recalculated.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editPriceConfiguration" // This method is AFAIK only called when editing and storing a price config. marco.
	 */
	public Map<PriceConfigID, List<AffectedProductType>> getAffectedProductTypes(Set<PriceConfigID> priceConfigIDs, ProductTypeID productTypeID, PriceConfigID innerPriceConfigID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			// TODO implement this method correctly to take the will-be-assigned innerPriceConfig into account instead of the currently assigned one!
			return PriceConfigUtil.getAffectedProductTypes(pm, priceConfigIDs, productTypeID, innerPriceConfigID);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryAccounts"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<AnchorID> getAccountIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! Account.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (!(queries instanceof JDOQueryCollectionDecorator))
			{
				queries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(queries);
			}
			JDOQueryCollectionDecorator<AbstractJDOQuery> decoratedQueries =
				(JDOQueryCollectionDecorator<AbstractJDOQuery>) queries;

			decoratedQueries.setPersistenceManager(pm);
			Collection<Account> accounts = (Collection<Account>) decoratedQueries.executeQueries();

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

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.manualMoneyTransfer"
	 */
	public ManualMoneyTransfer createManualMoneyTransfer(
			AnchorID fromID, AnchorID toID, CurrencyID currencyID, long amount, I18nText reason,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try{
			// the JavaEE server knows who we are - get the User object corresponding to the currently working principal
			User user = User.getUser(pm, getPrincipal());

			// initialise meta-data for Account & Currency - not really necessary, but still safer
			pm.getExtent(Account.class);
			pm.getExtent(Currency.class);

			// load the objects for the given IDs
			Account from = (Account) pm.getObjectById(fromID);
			Account to = (Account) pm.getObjectById(toID);
			Currency currency = (Currency) pm.getObjectById(currencyID);

			// delegate to Accounting for the actual creation + booking
			ManualMoneyTransfer manualMoneyTransfer = Accounting.getAccounting(pm).createManualMoneyTransfer(user, from, to, currency, amount, reason);

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

	/**
	 * This method is faster than {@link #getMoneyTransferIDs(Collection)}, because
	 * it directly queries object-ids.
	 *
	 * @param productTransferIDQuery The query to execute.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryMoneyTransfers"
	 */
	@SuppressWarnings("unchecked")
	public List<TransferID> getMoneyTransferIDs(MoneyTransferIDQuery productTransferIDQuery)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			productTransferIDQuery.setPersistenceManager(pm);
			return new ArrayList<TransferID>((Collection<TransferID>) productTransferIDQuery.getResult());
		} finally {
			pm.close();
		}
	}

	/**
	 * Unlike {@link #getMoneyTransferIDs(MoneyTransferIDQuery)}, this method allows
	 * for cascading multiple queries. It is slower than {@link #getMoneyTransferIDs(MoneyTransferIDQuery)}
	 * and should therefore only be used, if it's essentially necessary.
	 *
	 * @param moneyTransferQueries A <code>Collection</code> of {@link MoneyTransferQuery}. They will be executed
	 *		in the given order (if it's a <code>List</code>) and the result of the previous query will be passed as candidates
	 *		to the next query.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryMoneyTransfers"
	 */
	@SuppressWarnings("unchecked")
	public List<TransferID> getMoneyTransferIDs(Collection<MoneyTransferQuery> moneyTransferQueries)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<MoneyTransfer> productTransfers = null;
			for (MoneyTransferQuery productTransferQuery : moneyTransferQueries) {
				productTransferQuery.setPersistenceManager(pm);
				productTransferQuery.setCandidates(productTransfers);
				productTransfers = (Collection<MoneyTransfer>) productTransferQuery.getResult();
			}

			return NLJDOHelper.getObjectIDList(productTransfers);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryMoneyTransfers"
	 */
	public List<MoneyTransfer> getMoneyTransfers(Collection<TransferID> moneyTransferIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, moneyTransferIDs, MoneyTransfer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the object-ids of all {@link AccountType}s.
	 * <p>
	 * This method can be called by everyone, because the object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<AccountTypeID> getAccountTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(AccountType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<AccountTypeID>((Collection<? extends AccountTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the <code>AccountType</code>s for the specified object-ids.
	 * <p>
	 * This method can be called by everyone, because the <code>AccountType</code>s are not confidential.
	 * </p>
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<AccountType> getAccountTypes(Collection<AccountTypeID> accountTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, accountTypeIDs, AccountType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editPriceFragmentType"
	 */
	public PriceFragmentType storePriceFragmentType(PriceFragmentType priceFragmentType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, priceFragmentType, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@Override
	public String ping(String message) {
		return super.ping(message);
	}
}
