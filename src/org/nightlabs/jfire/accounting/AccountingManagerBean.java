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
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.MoneyFlowDimension;
import org.nightlabs.jfire.accounting.book.MoneyFlowMapping;
import org.nightlabs.jfire.accounting.book.fragmentbased.OwnerDimension;
import org.nightlabs.jfire.accounting.book.fragmentbased.PriceFragmentDimension;
import org.nightlabs.jfire.accounting.book.fragmentbased.SourceOrganisationDimension;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
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
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessorSaferPay;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.PaymentDataID;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.query.InvoiceQuery;
import org.nightlabs.jfire.accounting.tariffpriceconfig.TariffPriceConfig;
import org.nightlabs.jfire.accounting.tariffpriceconfig.TariffPriceConfigManagerBean;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDNamespaceDefault;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Alexander Bieber - alex[AT]nightlabs[DOT]de
 * 
 * @ejb.bean name="jfire/ejb/JFireTrade/AccountingManager"	
 *           jndi-name="jfire/ejb/JFireTrade/AccountingManager"
 *           type="Stateless" 
 *           transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class AccountingManagerBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(AccountingManagerBean.class);

	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}
	
	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}

	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialize method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialize()
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
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


//			// create the essential InvoiceStateDefinitions
//			JbpmConstantsInvoice invoiceStateDefinitionUtil;
//
//			invoiceStateDefinitionUtil = new JbpmConstantsInvoice(JbpmConstantsInvoice.STATE_DEFINITION_ID_CREATED);
//			invoiceStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "created");
//			invoiceStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Invoice has been newly created. This is the first state in the Invoice related workflow.");
//			pm.makePersistent(invoiceStateDefinitionUtil);
//
//			invoiceStateDefinitionUtil = new JbpmConstantsInvoice(JbpmConstantsInvoice.STATE_DEFINITION_ID_FINALIZED);
//			invoiceStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "finalized");
//			invoiceStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Invoice has been finalized. After that, it cannot be modified anymore. A modification would require cancellation and recreation.");
//			pm.makePersistent(invoiceStateDefinitionUtil);
//
//			invoiceStateDefinitionUtil = new JbpmConstantsInvoice(JbpmConstantsInvoice.STATE_DEFINITION_ID_BOOKED);
//			invoiceStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "booked");
//			invoiceStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Invoice has been booked. That means, all the money for all Articles has been transferred internally onto the configured Accounts.");
//			pm.makePersistent(invoiceStateDefinitionUtil);
//
//			invoiceStateDefinitionUtil = new JbpmConstantsInvoice(JbpmConstantsInvoice.STATE_DEFINITION_ID_CANCELLED);
//			invoiceStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "cancelled");
//			invoiceStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Invoice was cancelled after finalization (and maybe after booking). In case it was already booked, a reversing booking has been done. The Article.invoice fields are nulled and the Articles within the Invoice have been replaced by referencingArticles.");
//			pm.makePersistent(invoiceStateDefinitionUtil);
//
//			invoiceStateDefinitionUtil = new JbpmConstantsInvoice(JbpmConstantsInvoice.STATE_DEFINITION_ID_DOUBTFUL);
//			invoiceStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "doubtful");
//			invoiceStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The debt became doubtful. That means the person owing money (usually the customer, if it's not refunding) became financially unstable.");
//			pm.makePersistent(invoiceStateDefinitionUtil);
//
//			invoiceStateDefinitionUtil = new JbpmConstantsInvoice(JbpmConstantsInvoice.STATE_DEFINITION_ID_PAID);
//			invoiceStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "paid");
//			invoiceStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Invoice was paid completely. There's no money outstanding anymore.");
//			pm.makePersistent(invoiceStateDefinitionUtil);
//
//			invoiceStateDefinitionUtil = new JbpmConstantsInvoice(JbpmConstantsInvoice.STATE_DEFINITION_ID_UNCOLLECTABLE);
//			invoiceStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "uncollectable");
//			invoiceStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Invoice will never be paid. Usually this happens when the customer becomes bankrupt.");
//			pm.makePersistent(invoiceStateDefinitionUtil);

			// TODO deploy ProcessDefinitions!


			// create PriceFragmentTypes for Swiss and German VAT
			PriceFragmentType priceFragmentType = new PriceFragmentType(getRootOrganisationID(), "vat-de-16-net");
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Germany 16% Net");
			priceFragmentType.getName().setText(Locale.GERMAN.getLanguage(), "MwSt. Deutschland 16% Netto");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(getRootOrganisationID(), "vat-de-16-val");
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Germany 16% Value");
			priceFragmentType.getName().setText(Locale.GERMAN.getLanguage(), "MwSt. Deutschland 16% Wert");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(getRootOrganisationID(), "vat-de-7-net");
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Germany 7% Net");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(getRootOrganisationID(), "vat-de-7-val");
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Germany 7% Value");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(getRootOrganisationID(), "vat-ch-7_6-net");
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Switzerland 7.6% Net");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			priceFragmentType = new PriceFragmentType(getRootOrganisationID(), "vat-ch-7_6-val");
			priceFragmentType.getName().setText(Locale.ENGLISH.getLanguage(), "VAT Switzerland 7.6% Value");
			priceFragmentType.setContainerPriceFragmentType(PriceFragmentType.getTotalPriceFragmentType(pm));
			pm.makePersistent(priceFragmentType);

			Accounting accounting = Accounting.getAccounting(pm);
			Trader trader = Trader.getTrader(pm);




			LegalEntity anonymousCustomer = LegalEntity.getAnonymousCustomer(pm);
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

			ServerPaymentProcessorSaferPay serverPaymentProcessorSaferPay = ServerPaymentProcessorSaferPay.getServerPaymentProcessorSaferPay(pm);
			serverPaymentProcessorSaferPay.getName().setText(Locale.ENGLISH.getLanguage(), "SaferPay");
			serverPaymentProcessorSaferPay.addModeOfPayment(modeOfPaymentCreditCard);

			ServerPaymentProcessorBankTransferGermany serverPaymentProcessorBankTransferGermany = ServerPaymentProcessorBankTransferGermany.getServerPaymentProcessorBankTransferGermany(pm);
			serverPaymentProcessorBankTransferGermany.getName().setText(Locale.ENGLISH.getLanguage(), "Bank transfer within Germany");
			serverPaymentProcessorBankTransferGermany.getName().setText(Locale.GERMAN.getLanguage(), "Überweisung innerhalb Deutschlands");
			serverPaymentProcessorBankTransferGermany.addModeOfPayment(modeOfPaymentBankTransfer);

			ServerPaymentProcessorDebitNoteGermany serverPaymentProcessorDebitNoteGermany = ServerPaymentProcessorDebitNoteGermany.getServerPaymentProcessorDebitNoteGermany(pm);
			serverPaymentProcessorDebitNoteGermany.getName().setText(Locale.ENGLISH.getLanguage(), "Debit Note within Germany");
			serverPaymentProcessorDebitNoteGermany.getName().setText(Locale.GERMAN.getLanguage(), "Lastschrift innerhalb Deutschlands");
			serverPaymentProcessorDebitNoteGermany.addModeOfPayment(modeOfPaymentDebitNote);

			IDNamespaceDefault idNamespaceDefault = IDNamespaceDefault.createIDNamespaceDefault(pm, getOrganisationID(), Invoice.class);
			idNamespaceDefault.setCacheSizeServer(0);
			idNamespaceDefault.setCacheSizeClient(0);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getTariffs(String[] fetchGroups, int maxFetchDepth)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			FetchPlan fetchPlan = pm.getFetchPlan();
			fetchPlan.setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				fetchPlan.setGroups(fetchGroups);

			Collection res = new ArrayList();
			for (Iterator it = pm.getExtent(Tariff.class, true).iterator(); it.hasNext(); ) {
				Tariff t = (Tariff)it.next();
				res.add(pm.detachCopy(t));
			}

			return res;
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Tariff storeTariff(Tariff tariff, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (Tariff) NLJDOHelper.storeJDO(pm, tariff, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getCustomerGroups(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Query q = pm.newQuery(CustomerGroup.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public CustomerGroup storeCustomerGroup(CustomerGroup customerGroup, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (CustomerGroup) NLJDOHelper.storeJDO(pm, customerGroup, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getCurrencies(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(Currency.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Account getAccount(AnchorID accountID, String[] fetchGroups, int maxFetchDepth) 
	throws ModuleException
	{
		return getAccount(Account.getPrimaryKey(accountID.organisationID, accountID.anchorTypeID, accountID.anchorID), fetchGroups, maxFetchDepth);
	}

		/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Account getAccount(String accountPK, String[] fetchGroups, int maxFetchDepth) 
	throws ModuleException
	{
		Account result = null;
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(Account.class);
			q.declareParameters("java.lang.String accountPK");
			q.setFilter("this.primaryKey == accountPK");
			Collection found = (Collection)q.executeWithArray(new Object[]{accountPK});
			if (found.size() != 1)
				throw new IllegalArgumentException("Could not find Account with primary key \""+accountPK+"\"");
			result = (Account)pm.detachCopyAll(found).iterator().next();
			return result;
		} finally {
			pm.close();
		}
			
	}
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getAccounts(AccountSearchFilter searchFilter,  String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Collection accounts = searchFilter.executeQuery(pm);

			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Collection result = pm.detachCopyAll(accounts);
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Account createMandatorAccount(String anchorID, String currencyID, boolean createShadowAccount, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Currency currency = null;
			try {
				currency = (Currency)pm.getObjectById(CurrencyID.create(currencyID));
			} catch(JDOObjectNotFoundException e) {
				throw new ModuleException("No currency with currencyID "+currencyID+" known.", e);
			}
			
			Account newAccount = Accounting.getAccounting(pm).createMandatorAccount(anchorID, currency, createShadowAccount);
			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return (Account)pm.detachCopy(newAccount);
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
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Account storeAccount(Account account, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (!JDOHelper.isDetached(account))
				throw new IllegalArgumentException("storeAccount can only accept detached Accounts. Use create_*_Account methods to create an Account.");
			if (!account.getOrganisationID().equals(getOrganisationID()))
				throw new IllegalArgumentException("Given Account was created for a different organisation, can not store to this datastore!");
			
			Account result = (Account)NLJDOHelper.storeJDO(pm, account, get, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * @param anchorID The anchorID of the Account wich shadowAccounts are to be set
	 * @param shadowAccounts A Collection of the AnchorIDs of the ShadowAccounts to be set to the given Account.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void setAccountShadowAccounts(AnchorID anchorID, Collection shadowAccounts)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Account account = (Account)pm.getObjectById(anchorID);
			account.getShadowAccounts().clear();
			for (Iterator iter = shadowAccounts.iterator(); iter.hasNext();) {
				AnchorID shadowID = (AnchorID) iter.next();
				ShadowAccount shadowAccount = (ShadowAccount)pm.getObjectById(shadowID);
				account.getShadowAccounts().add(shadowAccount);
				shadowAccount.getShadowedAccounts().remove(account);
				shadowAccount.getShadowedAccounts().add(account);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @param anchorID The anchorID of the ShadowAccount wich shadowedAccounts are to be set
	 * @param accounts A Collection of the AnchorIDs of the Accounts to be shadowed by the given ShadowAccount.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void setShadowAccountShadowedAccounts(AnchorID anchorID, Collection accounts)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ShadowAccount shadowAccount = (ShadowAccount)pm.getObjectById(anchorID);
			shadowAccount.getShadowedAccounts().clear();			
			for (Iterator iter = accounts.iterator(); iter.hasNext();) {
				AnchorID accountID = (AnchorID) iter.next();
				Account account = (Account)pm.getObjectById(accountID);
				account.getShadowAccounts().remove(shadowAccount);
				account.getShadowAccounts().add(shadowAccount);
				shadowAccount.getShadowedAccounts().add(account);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * Assign the LocalAccountantDelegate defined by the given localAccountantDelegateID
	 * to the ProductType defined by the given productTypeID.
	 * 
	 * @param productTypeID The ProductTypeID of the ProductType to which the delegate should be assigned.
	 * @param localAccountantDelegateID The LocalAccountantDelegateID of the LocalAccountantDelegate to assign.
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void assignLocalAccountantDelegateToProductType(
			ProductTypeID productTypeID,
			LocalAccountantDelegateID localAccountantDelegateID
	)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ProductType productType = (ProductType) pm.getObjectById(productTypeID);
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(localAccountantDelegateID);
			productType.setLocalAccountantDelegate(localAccountantDelegate);
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns a Clloection of {@link LocalAccountantDelegateID} not detached
	 * delegates.
	 * 
	 * @param delegateClass The class/type of delegates that should be returned.
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getTopLevelAccountantDelegates(Class delegateClass) 
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection delegates = LocalAccountantDelegate.getTopLevelDelegates(pm, delegateClass);
			Collection result = new LinkedList();
			for (Iterator iter = delegates.iterator(); iter.hasNext();) {
				LocalAccountantDelegate delegate = (LocalAccountantDelegate) iter.next();
				result.add(JDOHelper.getObjectId(delegate));
			}
			return result;
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
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getChildAccountantDelegates(LocalAccountantDelegateID delegateID) 
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection delegates = LocalAccountantDelegate.getChildDelegates(
					pm, 
					delegateID.organisationID,
					delegateID.localAccountantDelegateID
				);
			Collection result = new LinkedList();
			for (Iterator iter = delegates.iterator(); iter.hasNext();) {
				LocalAccountantDelegate delegate = (LocalAccountantDelegate) iter.next();
				result.add(JDOHelper.getObjectId(delegate));
			}
			return result;
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
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getLocalAccountantDelegates(Collection delegateIDs, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Collection result = new LinkedList();
			for (Iterator iter = delegateIDs.iterator(); iter.hasNext();) {
				LocalAccountantDelegateID delegateID = null;
				try {
					delegateID = (LocalAccountantDelegateID) iter.next();
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Please pass only instances of LocalAccountantDelegateID as members of the delegateIDs paramaeter to this method.");
				}
				LocalAccountantDelegate delegate = (LocalAccountantDelegate)pm.getObjectById(delegateID);
				result.add(pm.detachCopy(delegate));
			}
			return result;
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
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public LocalAccountantDelegate getLocalAccountantDelegate(LocalAccountantDelegateID delegateID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			LocalAccountantDelegate delegate = (LocalAccountantDelegate)pm.getObjectById(delegateID);
			return (LocalAccountantDelegate)pm.detachCopy(delegate);
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
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public LocalAccountantDelegate storeLocalAccountantDelegate(
			LocalAccountantDelegate delegate, 
			boolean get, 
			String[] fetchGroups, int maxFetchDepth
		)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (LocalAccountantDelegate)NLJDOHelper.storeJDO(pm, delegate, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public MoneyFlowMapping storeMoneyFlowMapping(MoneyFlowMapping mapping, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return (MoneyFlowMapping) NLJDOHelper.storeJDO(pm, mapping, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Map getResolvedMoneyFlowMappings(ProductTypeID productTypeID, String[] mappingFetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (mappingFetchGroups != null)
				pm.getFetchPlan().setGroups(mappingFetchGroups);
			
			ProductType productType = (ProductType)pm.getObjectById(productTypeID);
			LocalAccountantDelegate delegate = productType.getLocalAccountantDelegate();
			if (delegate == null)
				throw new IllegalArgumentException("The ProductType with id "+productTypeID+" does not have a LocalAccountantDelegate assigned to it.");
			Map resolvedMappings = delegate.resolveProductTypeMappings(productType);
			Map result = new HashMap();
			for (Iterator iter = resolvedMappings.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				
				LocalAccountantDelegate.ResolvedMapEntry persitentMapEntry = (LocalAccountantDelegate.ResolvedMapEntry)entry.getValue();				
				LocalAccountantDelegate.ResolvedMapEntry mapEntry = new LocalAccountantDelegate.ResolvedMapEntry();
				
				for (Iterator iterator = persitentMapEntry.getResolvedMappings().entrySet().iterator(); iterator.hasNext();) {
					Map.Entry resolvedEntry = (Map.Entry) iterator.next();
					MoneyFlowMapping persistentMapping = (MoneyFlowMapping)resolvedEntry.getValue();
					MoneyFlowMapping detachedMapping = (MoneyFlowMapping)pm.detachCopy(persistentMapping);
					mapEntry.getResolvedMappings().put((String)resolvedEntry.getKey(), detachedMapping);
				}
				
				result.put(entry.getKey(), mapEntry);				
			}
			return result;
			
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @throws ModuleException
//	 * @deprecated use {@link #getPriceFragmentTypes(Collection, String[], int)} 
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.transaction type = "Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public Collection getPriceFragmentTypes(String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//			Query q = pm.newQuery(PriceFragmentType.class);
//			return pm.detachCopyAll((Collection)q.execute());
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * @param priceFragmentTypeIDs Can be <code>null</code> in order to return ALL {@link PriceFragmentType}s or a collection of {@link PriceFragmentTypeID}s to return only a subset.
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getPriceFragmentTypes(Collection priceFragmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			if (priceFragmentTypeIDs == null) {
				Query q = pm.newQuery(PriceFragmentType.class);
				return pm.detachCopyAll((Collection)q.execute());
			}

			Collection result = new LinkedList();
			for (Iterator iter = priceFragmentTypeIDs.iterator(); iter.hasNext();) {
				PriceFragmentTypeID priceFragmentTypeID = (PriceFragmentTypeID) iter.next();
				PriceFragmentType pType = (PriceFragmentType)pm.getObjectById(priceFragmentTypeID);
				result.add(pType);
			}
			return pm.detachCopyAll(result);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns a Collection of PriceFragmentTypeID of all known PriceFragementTypes
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getPriceFragmentTypeIDs()
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(PriceFragmentType.class);
			Collection pTypes = (Collection)q.execute();
			Collection result = new LinkedList();
			for (Iterator iter = pTypes.iterator(); iter.hasNext();) {
				PriceFragmentType pType = (PriceFragmentType) iter.next();
				result.add(JDOHelper.getObjectId(pType));
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PriceFragmentType getPriceFragmentType(PriceFragmentTypeID priceFragmentTypeID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			PriceFragmentType priceFragmentType = (PriceFragmentType)pm.getObjectById(priceFragmentTypeID);
			return (PriceFragmentType)pm.detachCopy(priceFragmentType);
		} finally {
			pm.close();
		}
	}


	/**
	 * Creates an Invoice for all specified <code>Article</code>s. If
	 * get is true, a detached version of the new Invoice will be returned.
	 *
	 * @param articleIDs The {@link ArticleID}s of those {@link Article}s that shall be added to the new <code>Invoice</code>.
	 * @param get Whether a detached version of the created Invoice should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the invoice should be detached with.
	 * @return Detached Invoice or null.
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Invoice createInvoice(
			Collection articleIDs, String invoiceIDPrefix, 
			boolean get, String[] fetchGroups, int maxFetchDepth)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Article.class);
			User user = User.getUser(pm, getPrincipal());
			Trader trader = Trader.getTrader(pm);
			Accounting accounting = trader.getAccounting();

			ArrayList articles = new ArrayList(articleIDs.size());
			for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				Article article = (Article) pm.getObjectById(articleID);
				Offer offer = article.getOffer();
				OfferLocal offerLocal = offer.getOfferLocal();
				trader.validateOffer(offer);
				trader.finalizeOffer(user, offer);
				trader.acceptOffer(user, offerLocal);
				trader.confirmOffer(user, offerLocal);
				articles.add(article);
			}

			Invoice invoice = accounting.createInvoice(user, articles, invoiceIDPrefix);
			accounting.validateInvoice(invoice);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS + FetchPlan.DETACH_UNLOAD_FIELDS);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return (Invoice)pm.detachCopy(invoice);
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Invoice createInvoice(
			ArticleContainerID articleContainerID, String invoiceIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
		throws ModuleException
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
				OfferLocal offerLocal = offer.getOfferLocal();
				trader.validateOffer(offer);
				trader.finalizeOffer(user, offer);
				trader.acceptOffer(user, offerLocal);
				trader.confirmOffer(user, offerLocal);
			}
			else {
				for (Iterator it = articleContainer.getArticles().iterator(); it.hasNext(); ) {
					Article article = (Article) it.next();
					Offer offer = article.getOffer();
					OfferLocal offerLocal = offer.getOfferLocal();
					trader.validateOffer(offer);
					trader.finalizeOffer(user, offer);
					trader.acceptOffer(user, offerLocal);
					trader.confirmOffer(user, offerLocal);
				}
			}

			Invoice invoice = accounting.createInvoice(user, articleContainer, invoiceIDPrefix);
			accounting.validateInvoice(invoice);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS + FetchPlan.DETACH_UNLOAD_FIELDS);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return (Invoice)pm.detachCopy(invoice);
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
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Invoice addArticlesToInvoice(
			InvoiceID invoiceID, Collection articleIDs,
			boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Invoice.class);
			pm.getExtent(Article.class);
			Invoice invoice = (Invoice) pm.getObjectById(invoiceID);
			Collection articles = new ArrayList(articleIDs.size());
			for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				articles.add(pm.getObjectById(articleID));
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

			return (Invoice)pm.detachCopy(invoice);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Invoice removeArticlesFromInvoice(
			InvoiceID invoiceID, Collection articleIDs,
			boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Invoice.class);
			pm.getExtent(Article.class);
			Invoice invoice = (Invoice) pm.getObjectById(invoiceID);
			Collection articles = new ArrayList(articleIDs.size());
			for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				articles.add(pm.getObjectById(articleID));
			}

			Accounting accounting = Accounting.getAccounting(pm);
			accounting.removeArticlesFromInvoice(User.getUser(pm, getPrincipal()), invoice, articles);

			if (!get)
				return null;

			if (validate)
				accounting.validateInvoice(invoice);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Invoice)pm.detachCopy(invoice);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void finalizeInvoice(InvoiceID invoiceID)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());
			finalizeInvoice(pm, user, invoiceID);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void finalizeInvoices(Collection invoiceIDs)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());
			for (Iterator iter = invoiceIDs.iterator(); iter.hasNext();) {
				InvoiceID invoiceID = (InvoiceID) iter.next();
				finalizeInvoice(pm, user, invoiceID);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void bookInvoice(InvoiceID invoiceID, boolean finalizeIfNecessary, boolean silentlyIgnoreBookedInvoice)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());
			bookInvoice(pm, user, invoiceID, finalizeIfNecessary, silentlyIgnoreBookedInvoice);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void bookInvoices(Collection invoiceIDs, boolean finalizeIfNecessary, boolean silentlyIgnoreBookedInvoice)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());
			for (Iterator iter = invoiceIDs.iterator(); iter.hasNext();) {
				InvoiceID invoiceID = (InvoiceID) iter.next();
				bookInvoice(pm, user, invoiceID, finalizeIfNecessary, silentlyIgnoreBookedInvoice);
			}
		} finally {
			pm.close();
		}
	}

	protected void bookInvoice(
			PersistenceManager pm, User user, InvoiceID invoiceID,
			boolean finalizeIfNecessary, boolean silentlyIgnoreBookedInvoice)
	throws ModuleException
	{
		Invoice invoice = null;
		try {
			invoice = (Invoice)pm.getObjectById(invoiceID);
		} catch (JDOObjectNotFoundException e) {
			throw new ModuleException("Could not find an Invoice in datastore for invcoiceID: "+invoiceID, e);
		}
		Accounting.getAccounting(pm).bookInvoice(user, invoice, finalizeIfNecessary, silentlyIgnoreBookedInvoice);
	}

	protected void finalizeInvoice(PersistenceManager pm, User user, InvoiceID invoiceID)
	throws ModuleException
	{
		Invoice invoice = null;
		try {
			invoice = (Invoice)pm.getObjectById(invoiceID);
		} catch (JDOObjectNotFoundException e) {
			throw new ModuleException("Could not find an Invoice in datastore for invcoiceID: "+invoiceID, e);
		}
		Accounting.getAccounting(pm).finalizeInvoice(user, invoice);
	}


	/**
	 * @param paymentDataList A <tt>List</tt> of {@link PaymentData}.
	 * @return A <tt>List</tt> with instances of {@link PaymentResult} in the same
	 *		order as and corresponding to the {@link PaymentData} objects passed in
	 *		<tt>paymentDataList</tt>.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public List payBegin(List paymentDataList)
	throws ModuleException
	{
		try {
			AccountingManagerLocal accountingManagerLocal = AccountingManagerUtil.getLocalHome().create();

			List resList = new ArrayList();
			for (Iterator it = paymentDataList.iterator(); it.hasNext(); ) {
				PaymentData paymentData = (PaymentData) it.next();

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
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payBegin(PaymentData paymentData)
	throws ModuleException
	{
		return _payBegin(paymentData);
	}
	
	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
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
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public List payDoWork(List paymentIDs, List payDoWorkClientResults, boolean forceRollback)
	throws ModuleException
	{
		try {
			AccountingManagerLocal accountingManagerLocal = AccountingManagerUtil.getLocalHome().create();

			if (paymentIDs.size() != payDoWorkClientResults.size())
				throw new IllegalArgumentException("paymentIDs.size() != payDoWorkClientResults.size()!!!");

			List resList = new ArrayList();
			Iterator itResults = payDoWorkClientResults.iterator();
			for (Iterator itIDs = paymentIDs.iterator(); itIDs.hasNext(); ) {
				PaymentID paymentID = (PaymentID) itIDs.next();
				PaymentResult payDoWorkClientResult = (PaymentResult) itResults.next();

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
	 * @throws ModuleException
	 *
	 * @see Accounting#payEnd(User, PaymentData)
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
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
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
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
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public List payEnd(List paymentIDs, List payEndClientResults, boolean forceRollback)
	throws ModuleException
	{
		try {
			AccountingManagerLocal accountingManagerLocal = AccountingManagerUtil.getLocalHome().create();

			if (paymentIDs.size() != payEndClientResults.size())
				throw new IllegalArgumentException("paymentIDs.size() != payEndClientResults.size()!!!");

			List resList = new ArrayList();
			Iterator itResults = payEndClientResults.iterator();
			for (Iterator itIDs = paymentIDs.iterator(); itIDs.hasNext(); ) {
				PaymentID paymentID = (PaymentID) itIDs.next();
				PaymentResult payEndClientResult = (PaymentResult) itResults.next();

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
	 * @throws ModuleException
	 *
	 * @see Accounting#payEnd(User, PaymentData)
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
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
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public int createMoneyFlowMappingID()
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return Accounting.getAccounting(pm).createMoneyFlowMappingID();
		} finally {
			pm.close();
		}
	}

//	/**
//	 * Returns a Configurator for MoneyFlowMappings initially handling
//	 * all MoneyFlowMappings found according to the given filters. Set
//	 * a parameter to null to have it ignored in the filter. 
//	 * 
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.transaction type = "Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public PFMoneyFlowMappingConfigurator getPFMoneyFlowMappingConfigurator(
//			String accountantDelegateID,
//			String productTypePK, 
//			String priceFragmentTypePK, 
//			String currencyID, 
//			String packageType, 
//			String[] fetchGroups
//	)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Query q = pm.newQuery(PFMoneyFlowMapping.class);
//			StringBuffer filter = new StringBuffer();
//
//			if (productTypePK != null) {
////				filter.append("this.productTypePK == qProductTypePK");
//				filter.append("this.productTypePK == '"+productTypePK+"'");
//			}
//
//			if (priceFragmentTypePK != null) {
//				if (filter.length() > 0) {
//					filter.append(" && ");
//				}
////				filter.append("this.priceFragmentTypePK == qPriceFragmentTypePK");
//				filter.append("this.priceFragmentTypePK == '"+priceFragmentTypePK+"'");
//			}
//			
//			if (currencyID != null){
//				if (filter.length() > 0) {
//					filter.append(" && ");
//				}
////				filter.append("this.currencyID == qCurrencyID");
//				filter.append("this.currencyID == '"+currencyID+"'");
//			}
//			
//			if (packageType != null){
//				if (filter.length() > 0) {
//					filter.append(" && ");
//				}
////				filter.append("this.packageType == qPackageType");
//				filter.append("this.packageType == '"+packageType+"'");
//			}
//			
//			if (filter.length() > 0)
//				filter.append(" && ");
//			if (accountantDelegateID == null)
//				filter.append("this.accountantDelegateID == null");
//			else
//				filter.append("this.accountantDelegateID == \""+accountantDelegateID+"\"");
//			
//			if (filter.length() > 0) {
////				q.declareParameters(params.toString());
//				q.setFilter(filter.toString());
//			}
//			Collection mappings = null;
//			try {
//				mappings = (Collection)q.execute(); //WithArray(new Object[]{productTypePK, priceFragmentTypePK, currencyID, packageType});
//			} catch(Throwable t) {
//				LOGGER.error("Error executing PFMoneyFlowMapping query: ", t);
//				throw new RuntimeException(t);
//			}
//			
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(Utils.array2ArrayList(fetchGroups));
//			
//			Collection detachedMappings = pm.detachCopyAll(mappings);
//			
//			PFMoneyFlowMappingConfigurator result = new PFMoneyFlowMappingConfigurator(getOrganisationID(), detachedMappings, null);
//			result.setAccountantDelegateID(accountantDelegateID);
//			return result;			
//		} finally {
//			pm.close();
//		}
//	}
	
	
//	/**
//	 * Returns a Map with key productTypePK and value PFMoneyFlowConfigurator
//	 * holding all declared global Mappings for each element of the 
//	 * given list of productTypePKs.
//	 * 
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.transaction type = "Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public Map getPFMoneyFlowMappingConfigurators (
//			Collection productTypePKs, 
//			String[] fetchGroups
//	)
//	throws ModuleException 
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Query q = pm.newQuery(PFMoneyFlowMapping.class);
//			StringBuffer filter = new StringBuffer();
//			
//			Map result = new HashMap(); 
//			
//			for (Iterator iter = productTypePKs.iterator(); iter.hasNext();) {
//				String productTypePK = (String) iter.next();
//				result.put(
//						productTypePK,
//						getPFMoneyFlowMappingConfigurator(
//								null,
//								productTypePK,
//								null,
//								null,
//								null,
//								fetchGroups
//						)
//				);
//			}
//			return result;			
//		} finally {
//			pm.close();
//		}
//	}
	

//	/**
//	 * Store the MoneyFlowMappings managed by the given Configurator.
//	 * @param configurator The configurator to store.
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.transaction type = "Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void storeMoneyFlowMappingConfiguration(PFMoneyFlowMappingConfigurator configurator)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			configurator.validateAndStore(pm);
//		} finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * Store the MoneyFlowMappings managed by the given Configurators.
//	 * 
//	 * @param configurators A Collection of PFMoneyFlowMappingConfigurators to store.
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.transaction type = "Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void storeMoneyFlowMappingConfiguration(Collection configurators)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			for (Iterator iter = configurators.iterator(); iter.hasNext();) {
//				PFMoneyFlowMappingConfigurator configurator = (PFMoneyFlowMappingConfigurator) iter.next();
//				configurator.validateAndStore(pm);
//			}
//		} finally {
//			pm.close();
//		}
//	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */ 
	public Invoice getInvoice(InvoiceID invoiceID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Invoice) pm.detachCopy(pm.getObjectById(invoiceID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @param invoiceQueries Instances of {@link InvoiceQuery} that shall be chained
	 *		in order to retrieve the result. The result of one query is passed to the
	 *		next one using the {@link JDOQuery#setCandidates(Collection)}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Set<InvoiceID> getInvoiceIDs(Collection<InvoiceQuery> invoiceQueries)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			Set<Invoice> invoices = null;
			for (InvoiceQuery query : invoiceQueries) {
				query.setPersistenceManager(pm);
				query.setCandidates(invoices);
				invoices = new HashSet<Invoice>(query.getResult());
			}

			return NLJDOHelper.getObjectIDSet(invoices);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public List<InvoiceID> getInvoiceIDs(AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return new ArrayList<InvoiceID>(Invoice.getInvoiceIDs(pm, vendorID, customerID, rangeBeginIdx, rangeEndIdx));
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
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
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public List getNonFinalizedInvoices(AnchorID vendorID, AnchorID customerID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return (List) pm.detachCopyAll(Invoice.getNonFinalizedInvoices(pm, vendorID, customerID));
		} finally {
			pm.close();
		}
	}

//	/**
//	 * This method looks up the {@link LegalEntity} specified by the given {@link AnchorID}
//	 * and returns all {@link ModeOfPaymentFlavour}s that are attached to the
//	 * default {@link CustomerGroup} of the <tt>LegalEntity</tt>.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type = "Supports"
//	 */
//	public Collection getAvailableModeOfPaymentFlavoursForOneCustomer(AnchorID legalEntityID, String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getExtent(LegalEntity.class);
//			Anchor anchor = (Anchor) pm.getObjectById(legalEntityID);
//			if (!(anchor instanceof LegalEntity))
//				throw new IllegalArgumentException("Given anchorID \""+legalEntityID+"\" does not represent an instance of LegalEntity, but "+anchor.getClass().getName()+"!");
//
//			LegalEntity legalEntity = (LegalEntity)anchor;
//			if (legalEntity.getDefaultCustomerGroup() == null)
//				throw new IllegalStateException("There is no default CustomerGroup assigned to the LegalEntity "+legalEntity.getPrimaryKey()+"!");
//
//			CustomerGroupID customerGroupID = (CustomerGroupID) pm.getObjectId(legalEntity.getDefaultCustomerGroup());
//
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			Collection c = ModeOfPaymentFlavour.getAvailableModeOfPaymentFlavoursForOneCustomerGroup(
//					pm, customerGroupID);
//
//			return pm.detachCopyAll(c);
//		} finally {
//			pm.close();
//		}
//	}
	
	/**
	 * @param customerGroupIDs A <tt>Collection</tt> of {@link CustomerGroupID}. If <tt>null</tt>, all {@link ModeOfPaymentFlavour}s will be returned.
	 * @param mergeMode one of {@link ModeOfPaymentFlavour#MERGE_MODE_SUBTRACTIVE} or {@link ModeOfPaymentFlavour#MERGE_MODE_ADDITIVE}
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Supports"
	 */
	public Collection getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
			Collection customerGroupIDs, byte mergeMode, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection c = ModeOfPaymentFlavour.getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
					pm, customerGroupIDs, mergeMode);

			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
	}

	/**
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Supports"
	 */
	public Collection getServerPaymentProcessorsForOneModeOfPaymentFlavour(
			ModeOfPaymentFlavourID modeOfPaymentFlavourID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection c = ServerPaymentProcessor.getServerPaymentProcessorsForOneModeOfPaymentFlavour(
					pm, modeOfPaymentFlavourID);

			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Supports"
	 */
	public Collection getAvailableModeOfPaymentFlavoursForOneCustomerGroup(CustomerGroupID customerGroupID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection c = ModeOfPaymentFlavour.getAvailableModeOfPaymentFlavoursForOneCustomerGroup(
					pm, customerGroupID);

			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
	}

	/**
	 * @param productTypeID The object ID of the desired ProductType.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public ProductType getProductTypeForPriceConfigEditing(
			ProductTypeID productTypeID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			FetchPlan fetchPlan = pm.getFetchPlan();

			System.out.println("***********************************************************************");
			System.out.println("***********************************************************************");
			System.out.println("***********************************************************************");
			System.out.println("***********************************************************************");

			int detachmentOptions = fetchPlan.getDetachmentOptions();
			System.out.println("detachmentOptions: " + detachmentOptions);
			System.out.println("detachmentOptions & DETACH_LOAD_FIELDS: " + ((detachmentOptions & FetchPlan.DETACH_LOAD_FIELDS) != 0 ? true : false));
			System.out.println("detachmentOptions & DETACH_UNLOAD_FIELDS: " + ((detachmentOptions & FetchPlan.DETACH_UNLOAD_FIELDS) != 0 ? true : false));

			fetchPlan.setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS);
			fetchPlan.setMaxFetchDepth(-1);
			fetchPlan.setGroups(new String[] {FetchPlan.DEFAULT, FetchGroupsPriceConfig.FETCH_GROUP_EDIT});

			detachmentOptions = fetchPlan.getDetachmentOptions();
			System.out.println("***********************************************************************");
			System.out.println("detachmentOptions: " + detachmentOptions);
			System.out.println("detachmentOptions & DETACH_LOAD_FIELDS: " + ((detachmentOptions & FetchPlan.DETACH_LOAD_FIELDS) != 0 ? true : false));
			System.out.println("detachmentOptions & DETACH_UNLOAD_FIELDS: " + ((detachmentOptions & FetchPlan.DETACH_UNLOAD_FIELDS) != 0 ? true : false));

			pm.getExtent(ProductType.class);
			ProductType res = (ProductType) pm.getObjectById(productTypeID);
			res.getName().getTexts();

			// load main price configs
			res.getPackagePriceConfig();
			res.getInnerPriceConfig();

			// load all extended ProductType-s
			ProductType pt = res;
			while (pt != null) {
				pt = pt.getExtendedProductType();
				System.out.println(pt == null ? null : pt.getPrimaryKey());
			}

			// load all nested ProductType-s
			for (NestedProductType npt : res.getNestedProductTypes()) {
				npt.getPackageProductType();
				ProductType ipt = npt.getInnerProductType();
				ipt.getName().getTexts();
				ipt.getPackagePriceConfig();
				ipt.getInnerPriceConfig();
				pt = ipt;
				while (pt != null) {
					pt = pt.getExtendedProductType();
					System.out.println(pt == null ? null : pt.getPrimaryKey());
				}
			}

//			System.out.println("***********************************************************************");
//			System.out.println("***********************************************************************");
//			System.out.println("***********************************************************************");
//			System.out.println("***********************************************************************");

			ProductType detachedRes = (ProductType) pm.detachCopy(res);

			// FIXME WORKAROUND for JPOX - begin
			resolveExtendedProductTypes(pm, res, detachedRes);

			for (NestedProductType attached_npt : res.getNestedProductTypes()) {
				NestedProductType detached_npt = detachedRes.getNestedProductType(attached_npt.getInnerProductTypePrimaryKey(), true);

				resolveExtendedProductTypes(pm,
						attached_npt.getInnerProductType(), 
						detached_npt.getInnerProductType());
			}

			// FIXME WORKAROUND for JPOX - end

			if (logger.isDebugEnabled()) {
				LinkedList<ProductType> productTypes = new LinkedList<ProductType>();
				productTypes.add(detachedRes);

				for (NestedProductType npt : detachedRes.getNestedProductTypes())
					productTypes.add(npt.getInnerProductType());

				for (ProductType productType : productTypes) {
					logger.debug("getProductTypeForPriceConfigEditing: productType="+productType.getPrimaryKey());
					if (productType.getInnerPriceConfig() instanceof TariffPriceConfig) {
						logger.debug("innerPriceConfig:");
						TariffPriceConfigManagerBean.logTariffPriceConfig((TariffPriceConfig)productType.getInnerPriceConfig());
					}
					if (productType.getPackagePriceConfig() instanceof TariffPriceConfig) {
						logger.debug("packagePriceConfig:");
						TariffPriceConfigManagerBean.logTariffPriceConfig((TariffPriceConfig)productType.getPackagePriceConfig());
					}
				}
			}

			return detachedRes;
		} finally {
			pm.close();
		}
	}

// FIXME WORKAROUND for JPOX - begin
	private static void resolveExtendedProductTypes(PersistenceManager pm, ProductType attachedPT, ProductType detachedPT)
	{
		while (attachedPT != null) {
			attachedPT = attachedPT.getExtendedProductType();
			System.out.println(attachedPT == null ? null : attachedPT.getPrimaryKey());
			ProductType extPT = null;
			if (attachedPT != null)
				extPT = (ProductType) pm.detachCopy(attachedPT);
			try {
				Method method = ProductType.class.getDeclaredMethod("setExtendedProductType", new Class[] {ProductType.class});
				method.setAccessible(true);
				method.invoke(detachedPT, extPT);
			} catch (Exception e) {
				e.printStackTrace();
			}
			detachedPT = extPT;
		}
	}
// FIXME WORKAROUND for JPOX - end


//	/**
//	 * @param productTypeID The object ID of the desired ProductType.
//	 * @param fetchGroups These fetch-groups are applied to the main
//	 *		ProductType which is specified by <tt>productTypeID</tt> and
//	 *		the {@link NestedProductType}s. They should NOT
//	 *		include anything within the price config! Unlike as usual, they do not
//	 *		replace the fetchGroups
//	 *		used per default by this method, but they are appended.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.transaction type = "Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public ProductType getProductTypeForPriceConfigEditing(
//			ProductTypeID productTypeID,
//			String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getExtent(ProductType.class);
//
//			pm.getFetchPlan().setGroups(new String[] {
//					FetchPlan.DEFAULT,
//					ProductType.FETCH_GROUP_NESTED_PRODUCT_TYPES,
//					NestedProductType.FETCH_GROUP_INNER_PRODUCT_TYPE
//			});
//			if (fetchGroups != null) {
//				for (int i = 0; i < fetchGroups.length; ++i)
//					pm.getFetchPlan().addGroup(fetchGroups[i]);
//			}
//			ProductType productType = (ProductType) pm.getObjectById(productTypeID);
//			ProductType _productType = (ProductType) pm.detachCopy(productType);
//
//			pm.getFetchPlan().setGroups(new String[] {
//					FetchPlan.DEFAULT,
//					FetchGroupsPriceConfig.FETCH_GROUP_EDIT
//			});
//			detachCopyPriceConfigs(pm, productType, _productType);
//			for (Iterator it = _productType.getNestedProductTypes(false).iterator(); it.hasNext(); ) {
//				NestedProductType _npt = (NestedProductType) it.next();
//				NestedProductType npt = productType.getNestedProductType(
//						_npt.getInnerProductTypePrimaryKey(), true);
//				detachCopyPriceConfigs(
//						pm, npt.getInnerProductType(), _npt.getInnerProductType());
//			}
//
//			return _productType;
//		} finally {
//			pm.close();
//		}
//	}
//
//	private static void detachCopyPriceConfigs(
//			PersistenceManager pm, ProductType livingPT, ProductType detachedPT)
//	{
//		if (livingPT.getInnerPriceConfig() == null)
//			detachedPT.setInnerPriceConfig(null);
//		else
//			detachedPT.setInnerPriceConfig(
//					(IInnerPriceConfig) pm.detachCopy(livingPT.getInnerPriceConfig()));
//
//		if (livingPT.getPackagePriceConfig() == null)
//			detachedPT.setPackagePriceConfig(null);
//		else
//			detachedPT.setPackagePriceConfig(
//					(IPackagePriceConfig) pm.detachCopy(livingPT.getPackagePriceConfig()));
//	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void test()
	{
//		try {
////			System.out.println(getPrincipalString());
////			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
////			StoreManager storeManager = StoreManagerUtil.getHome(getInitialContextProps("orga00.nightlabs.de")).create();
////			storeManager.createProductTypeID();
//
//			AsyncInvoke.exec(
//					new Invocation() {
//						public Serializable invoke() throws Exception
//						{
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//							System.out.println(getPrincipalString());
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
////							System.out.println("sleeping 1 minute");
////							try { Thread.sleep(60000); } catch (InterruptedException x) { }
////							System.out.println("waking up after sleeping 1 minute");
////							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
////							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
////							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//
//							StoreManager storeManager = StoreManagerUtil.getHome(getInitialContextProps("orga00.nightlabs.de")).create();
//							String res = storeManager.createProductTypeID();
//							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//							return res;
//						}
//					},
//					new SuccessCallback() {
//						public void handle(AsyncInvokeEnvelope envelope,Object result) throws Exception
//						{
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//							System.out.println(getPrincipalString());
//							System.out.println(result);
//							System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
//						}
//					},
//					new ErrorCallback(){
//						public void handle(AsyncInvokeEnvelope envelope, Throwable error)
//						throws Exception
//						{
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//							System.out.println(getPrincipalString());
//							error.printStackTrace(System.out);
//							System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
//						}
//					},
//					new UndeliverableCallback() {
//						/**
//						 * @see org.nightlabs.jfire.asyncinvoke.UndeliverableCallback#handle(org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope)
//						 */
//						public void handle(AsyncInvokeEnvelope envelope) throws Exception
//						{
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//							System.out.println(getPrincipalString());
//							System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//						}
//					});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
