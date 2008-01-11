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
import java.net.URL;
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

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.book.BookMoneyTransfer;
import org.nightlabs.jfire.accounting.book.LocalAccountant;
import org.nightlabs.jfire.accounting.book.PartnerAccountant;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.jbpm.ActionHandlerBookInvoice;
import org.nightlabs.jfire.accounting.jbpm.ActionHandlerBookInvoiceImplicitely;
import org.nightlabs.jfire.accounting.jbpm.ActionHandlerFinalizeInvoice;
import org.nightlabs.jfire.accounting.jbpm.JbpmConstantsInvoice;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.pay.PayMoneyTransfer;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentActionHandler;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.PaymentLocal;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.TradeConfigModule;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.AccountingID"
 *		detachable="true"
 *		table="JFireTrade_Accounting"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
public class Accounting
implements StoreCallback
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Accounting.class);

	/**
	 * This method returns the singleton instance of Accounting. If there is
	 * no instance of Accounting in the datastore, yet, it will be created.
	 *
	 * @param pm
	 * @return
	 */
	public static Accounting getAccounting(PersistenceManager pm)
	{
		Iterator it = pm.getExtent(Accounting.class).iterator();
		if (it.hasNext())
			return (Accounting)it.next();

		Accounting accounting = new Accounting();

		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		accounting.organisationID = organisationID;
		accounting.mandator = OrganisationLegalEntity.getOrganisationLegalEntity(pm, organisationID, true); // new OrganisationLegalEntity(localOrganisation.getOrganisation());
		accounting.accountingPriceConfig = new AccountingPriceConfig(IDGenerator.getOrganisationID(), PriceConfig.createPriceConfigID());
		accounting.localAccountant = new LocalAccountant(accounting.mandator, LocalAccountant.class.getName());
		accounting.mandator.setAccountant(accounting.localAccountant);
		accounting.partnerAccountant = new PartnerAccountant(organisationID, PartnerAccountant.class.getName());

		accounting = (Accounting) pm.makePersistent(accounting);
		return accounting;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */ 
	private OrganisationLegalEntity mandator;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private AccountingPriceConfig accountingPriceConfig;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalAccountant localAccountant;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PartnerAccountant partnerAccountant;

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private long nextPriceCoordinateID = 0;
//	private static long _nextPriceCoordinateID = -1;
//	private static Object _nextPriceCoordinateIDMutex = new Object();

//	public long createPriceCoordinateID()
//	{
//		synchronized (_nextPriceCoordinateIDMutex) {
//			if (_nextPriceCoordinateID < 0)
//				_nextPriceCoordinateID = nextPriceCoordinateID;
//
//			long res = _nextPriceCoordinateID++;
//			nextPriceCoordinateID = _nextPriceCoordinateID;
//			return res;
//		}
//	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager accountingPM = JDOHelper.getPersistenceManager(this);
		if (accountingPM == null)
			throw new IllegalStateException("This instance of Accounting is not persistent, can not get a PersistenceManager!");

		return accountingPM;
	}
	
	public OrganisationLegalEntity getMandator() {
		return mandator;
	}


	/**
	 * @return Returns the accountingPriceConfig.
	 */
	public AccountingPriceConfig getAccountingPriceConfig()
	{
		return accountingPriceConfig;
	}

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */	
//	private int nextMoneyFlowMappingID = 0;
//	private static int _nextMoneyFlowMappingID = -1;
//	private static Object _nextMoneyFlowMappingIDMutex = new Object();
//
//	// TODO replace this by usage of IDGenerator!
//	public int createMoneyFlowMappingID() {
//		synchronized(_nextMoneyFlowMappingIDMutex) {
//			if (_nextMoneyFlowMappingID < 0)
//				_nextMoneyFlowMappingID = nextMoneyFlowMappingID;
//
//			int res = _nextMoneyFlowMappingID;
//			_nextMoneyFlowMappingID = res + 1;
//			nextMoneyFlowMappingID = _nextMoneyFlowMappingID;
//			return res;
//		}
//	}

	/**
	 * Creates a new Invoice with the given articles.
	 * Checks whether vendor and customer are the same for all involved offers
	 * whether not articles are associated to another invoice
	 * and if all article prices are in the same currency. If one check
	 * fails a InvoiceEditException will be thrown.
	 *
	 * @param user The user which is responsible for creation of this invoice.
	 * @param articles The {@link Article}s that shall be added to the invoice. Must not be empty (because the customer is looked up from the articles). 
	 */
	public Invoice createInvoice(User user, Collection articles, String invoiceIDPrefix)
	throws InvoiceEditException
	{
		if (articles.size() <= 0)
			throw new InvoiceEditException(
				InvoiceEditException.REASON_NO_ARTICLES,
				"Cannot create an Invoice without Articles!"
			);
		
		// Make sure all offerItems are not yet in an invoice.
		// all offers have the same vendor and customer
		// and all offers have the same currency
		String vendorPK = null;
		OrganisationLegalEntity vendorLE = null;
		String customerPK = null;
		LegalEntity customerLE = null;
		Currency invoiceCurrency = null;
		for (Iterator iter = articles.iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			
			if (vendorPK == null) {
				vendorLE = article.getOffer().getOrder().getVendor();
				vendorPK = vendorLE.getPrimaryKey();
			}
			if (customerPK == null) {
				customerLE = article.getOffer().getOrder().getCustomer();
				customerPK = customerLE.getPrimaryKey();
			}
			if (invoiceCurrency == null)
				invoiceCurrency = article.getPrice().getCurrency();

			Offer articleOffer = article.getOffer();
			Order articleOrder = articleOffer.getOrder();
			
			if (!articleOffer.getOfferLocal().isAccepted()) {
				throw new InvoiceEditException(
					InvoiceEditException.REASON_OFFER_NOT_ACCEPTED, 
					"At least one involved offer is not accepted!",
					(ArticleID) JDOHelper.getObjectId(article)
				);
			}

			if (!vendorPK.equals(articleOrder.getVendor().getPrimaryKey()) 
						|| 
					!customerPK.equals(articleOrder.getCustomer().getPrimaryKey()) 
					)
			{
				throw new InvoiceEditException(
					InvoiceEditException.REASON_ANCHORS_DONT_MATCH,				
					"Vendor and customer are not equal for all involved orders, can not create Invoice!!"
				);
			}

			if (article.getInvoice() != null) {
				Invoice invoice = article.getInvoice();
				throw new InvoiceEditException(
					InvoiceEditException.REASON_ARTICLE_ALREADY_IN_INVOICE,
					"Article already in an invoice. Article "+article.getPrimaryKey()+", Invoice "+invoice.getPrimaryKey(), 
					(ArticleID) JDOHelper.getObjectId(article), 
					(InvoiceID) JDOHelper.getObjectId(invoice)
				);
			}

			if (!invoiceCurrency.getCurrencyID().equals(article.getPrice().getCurrency().getCurrencyID()))
				throw new InvoiceEditException(
					InvoiceEditException.REASON_MULTIPLE_CURRENCIES,
					"Can not create an Invoice with more than one Currency!"
				);
		}

		if (!vendorPK.equals(getMandator().getPrimaryKey()))
			throw new InvoiceEditException(
				InvoiceEditException.REASON_FOREIGN_ORGANISATION,
				"Attempt to create a Invoice not with the local organisation as vendor. Vendor is "+vendorPK
			);

		if (invoiceIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			try {
				tradeConfigModule = (TradeConfigModule) Config.getConfig(
						getPersistenceManager(), organisationID, user).createConfigModule(TradeConfigModule.class);
			} catch (ModuleException x) {
				throw new RuntimeException(x); // should not happen.
			}

			invoiceIDPrefix = tradeConfigModule.getActiveIDPrefixCf(Invoice.class.getName()).getDefaultIDPrefix();
		}

		Invoice invoice = new Invoice(
				user, vendorLE, customerLE,
				invoiceIDPrefix, IDGenerator.nextID(Invoice.class, invoiceIDPrefix),
				invoiceCurrency);
		new InvoiceLocal(invoice); // registers itself in the invoice
		invoice = (Invoice) getPersistenceManager().makePersistent(invoice);

		ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
				ProcessDefinitionAssignmentID.create(Invoice.class, TradeSide.vendor));
		processDefinitionAssignment.createProcessInstance(null, user, invoice);

		addArticlesToInvoice(user, invoice, articles);
//		for (Iterator iter = articles.iterator(); iter.hasNext();) {
//			Article article = (Article) iter.next();
//			invoice.addArticle(article);
//		}

		return invoice;
	}

	public void addArticlesToInvoice(User user, Invoice invoice, Collection articles)
	throws InvoiceEditException
	{
		Map productTypeActionHandler2Articles = new HashMap();
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
			invoice.addArticle(article);

			ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(
					getPersistenceManager(), article.getProductType().getClass());
			List al = (List) productTypeActionHandler2Articles.get(productTypeActionHandler);
			if (al == null) {
				al = new LinkedList();
				productTypeActionHandler2Articles.put(productTypeActionHandler, al);
			}
			al.add(article);
		}
		for (Iterator it = productTypeActionHandler2Articles.entrySet().iterator(); it.hasNext();) {
			Map.Entry me = (Map.Entry) it.next();
			((ProductTypeActionHandler) me.getKey()).onAddArticlesToInvoice(user, this, invoice, (List) me.getValue());
		}
	}

	public void removeArticlesFromInvoice(User user, Invoice invoice, Collection<? extends Article> articles)
	throws InvoiceEditException
	{
		for (Article article : articles) {
			invoice.removeArticle(article);
		}
	}

	/**
	 * Creates and persists an invoice for all <tt>Article</tt>s of the given <tt>Offer</tt>.
	 * It ignores <tt>Article</tt>s that are already in an other <tt>Invoice</tt>.
	 *
	 * @param user
	 * @param offer
	 * @return a new Invoice
	 * @throws InvoiceEditException
	 */
	public Invoice createInvoice(User user, ArticleContainer articleContainer, String invoiceIDPrefix) 
	throws InvoiceEditException 
	{
		ArrayList<Article> articles = new ArrayList<Article>();
		for (Article article : articleContainer.getArticles()) {
			if (article.getInvoice() == null)
				articles.add(article);
		}
		return createInvoice(user, articles, invoiceIDPrefix);
	}

	/**
	 * @return Returns the localAccountant.
	 */
	public LocalAccountant getLocalAccountant()
	{
		return localAccountant;
	}
	/**
	 * @return Returns the partnerAccountant.
	 */
	public PartnerAccountant getPartnerAccountant()
	{
		return partnerAccountant;
	}

	public void validateInvoice(Invoice invoice)
	{
		// this does nothing, if it is already valid
		invoice.validate();
	}

	/**
	 * Books the given Invoice. You must NOT call this method directly. It is called
	 * by {@link ActionHandlerBookInvoice}.
	 * 
	 * @param user The {@link User} who is responsible for booking.
	 * @param invoice The {@link Invoice} that should be booked.
	 * @param finalizeIfNecessary An invoice can only be booked, if finalized. Shall this method finalize,
	 *		if not yet done (otherwhise an exception is thrown).
	 * @param silentlyIgnoreBookedInvoice If the given invoice has already been booked, this method throws an
	 *		exception if this param is <tt>false</tt> or returns silently without doing anything if this
	 *		param is <tt>true</tt>.
	 */
	public void onBookInvoice(User user, Invoice invoice)
	{
		InvoiceLocal invoiceLocal = invoice.getInvoiceLocal();

//		if (invoiceLocal.isBooked()) {
//			if (!silentlyIgnoreBookedInvoice)
//				throw new IllegalStateException("Invoice \""+invoice.getPrimaryKey()+"\" has already been booked!");
//
//			return;
//		}
//
//		if (!invoice.isFinalized()) {
//			if (!finalizeIfNecessary)
//				throw new IllegalStateException("Invoice \""+invoice.getPrimaryKey()+"\" is not finalized!");
//
//			finalizeInvoice(user, invoice);
//		}

		if (invoiceLocal.isBooked())
			return;


		PersistenceManager pm = getPersistenceManager();

		LegalEntity from = invoice.getCustomer();
		LegalEntity to = invoice.getVendor();

//		if (invoice.getPrice().getAmount() >= 0) {
//			from = invoice.getCustomer();
//			to = invoice.getVendor();
//		}
//		else {
//			from = invoice.getVendor();
//			to = invoice.getCustomer();
//		}

		if (logger.isDebugEnabled())
			logger.debug("Invoice " + invoice.getPrimaryKey() + ": price=" + invoice.getPrice().getAmount() + invoice.getPrice().getCurrency().getCurrencyID() + " vendor=" + invoice.getVendor().getPrimaryKey() + " customer=" + invoice.getCustomer().getPrimaryKey());

		// The LocalAccountant is assigned to the mandator in any case, because it is
		// assigned during creation of Accounting. Hence, we don't need to check whether
		// from or to is the other side.
		if (from.getAccountant() == null)
			from.setAccountant(getPartnerAccountant());

		if (to.getAccountant() == null)
			to.setAccountant(getPartnerAccountant());

		// create the BookMoneyTransfer with positive amount but in the right direction
		if (invoice.getPrice().getAmount() < 0) {
			LegalEntity tmp = from;
			from = to;
			to = tmp;
		}

		BookMoneyTransfer bookMoneyTransfer = new BookMoneyTransfer(
				user,
				from,
				to,			
				invoice
			);
		bookMoneyTransfer = (BookMoneyTransfer) pm.makePersistent(bookMoneyTransfer);

		Set<Anchor> involvedAnchors = new HashSet<Anchor>();
		ArrayList<Transfer> containers = new ArrayList<Transfer>(1);
		containers.add(bookMoneyTransfer);
		boolean failed = true;
		try {
			bookMoneyTransfer.bookTransfer(user, involvedAnchors);
	
			// check consistence
			Anchor.checkIntegrity(containers, involvedAnchors);

			failed = false;
		} finally  {
			if (failed)
				Anchor.resetIntegrity(containers, involvedAnchors);
		}

		invoiceLocal.setBooked(user);

		for (InvoiceActionHandler invoiceActionHandler : invoiceLocal.getInvoiceActionHandlers()) {
			invoiceActionHandler.onBook(user, invoice);
		}
	}

	public PaymentResult payDoWork(
			User user, PaymentData paymentData)
	throws PaymentException
	{
		if (user == null)
			throw new NullPointerException("user");

		if (paymentData == null)
			throw new NullPointerException("paymentData");

		boolean postponed = paymentData.getPayment().isPostponed();

		ServerPaymentProcessor serverPaymentProcessor = getServerPaymentProcessor(
				paymentData.getPayment());

//	 call server-sided payment processor's second phase
		serverPaymentProcessor.payDoWork(
				new PayParams(this, user, paymentData));

		PaymentResult serverPaymentResult;
		serverPaymentResult = paymentData.getPayment().getPayDoWorkServerResult();
		if (serverPaymentResult == null)
			throw new PaymentException(
					new PaymentResult(
							PaymentResult.CODE_FAILED,
							"paymentData.getPayment().getPayDoWorkServerResult() returned null! You probably forgot to set it in your ServerPaymentProcessor (\""+serverPaymentProcessor.getPrimaryKey()+"\")!",
							(Throwable)null));

		if (serverPaymentResult.isFailed())
			throw new PaymentException(serverPaymentResult);

		if (postponed) {
			if (!PaymentResult.CODE_POSTPONED.equals(serverPaymentResult.getCode()) && !serverPaymentResult.isRolledBack()) {
				String msg = "The Payment \"" + paymentData.getPayment().getPrimaryKey() + "\" is marked postponed, but the PaymentProcessor \"" + serverPaymentProcessor.getPrimaryKey() + "\" did neither rollback nor return PaymentResult.CODE_POSTPONED! Instead it returned code=\"" + serverPaymentResult.getCode() + "\" text=\"" + serverPaymentResult.getText() + "\"";
				logger.warn(msg, new IllegalStateException(msg));
			}
		}
		else {
			if (!serverPaymentResult.isPaid())
				throw new PaymentException(serverPaymentResult);
		}

		try {
			for (Invoice invoice : paymentData.getPayment().getInvoices()) {
				for (InvoiceActionHandler invoiceActionHandler : invoice.getInvoiceLocal().getInvoiceActionHandlers()) {
					invoiceActionHandler.onPayDoWork(user, paymentData, invoice);
				}
			}
		} catch (PaymentException x) {
			throw x;
		} catch (Exception x) {
			throw new PaymentException(
					new PaymentResult(
							PaymentResult.CODE_FAILED,
							"Calling InvoiceActionHandler.onPayDoWork failed!",
							x));
		}
		
		try {
			for (PaymentActionHandler paymentActionHandler : paymentData.getPayment().getPaymentLocal().getPaymentActionHandlers()) {
				paymentActionHandler.onPayDoWork(paymentData);
			}
		} catch (PaymentException x) {
			throw x;
		} catch (Exception e) {
			throw new PaymentException(new PaymentResult(
					PaymentResult.CODE_FAILED, "Calling PaymentActionHandler.onPayDoWork failed! localOrganisation="+getOrganisationID(), e)); 
		}

		return serverPaymentResult;
	}

	public PaymentResult payEnd(
			User user, PaymentData paymentData)
	throws PaymentException
	{
		if (user == null)
			throw new NullPointerException("user");

		if (paymentData == null)
			throw new NullPointerException("paymentData");

		boolean postponed = paymentData.getPayment().isPostponed();

		ServerPaymentProcessor serverPaymentProcessor = getServerPaymentProcessor(
				paymentData.getPayment());

//		LegalEntity partner = getPartner(payment.getInvoices(), payment.getCurrency());

//	 call server-sided payment processor's second phase
		serverPaymentProcessor.payEnd(
				new PayParams(this, user, paymentData));

		PaymentResult serverPaymentResult;
		serverPaymentResult = paymentData.getPayment().getPayEndServerResult();
		if (serverPaymentResult == null)
			throw new PaymentException(
					new PaymentResult(
							PaymentResult.CODE_FAILED,
							"paymentData.getPayment().getPayEndServerResult() returned null! You probably forgot to set it in your ServerPaymentProcessor (\""+serverPaymentProcessor.getPrimaryKey()+"\")!",
							(Throwable)null));

//		// I don't know why, but without the following line, it is not set in the datastore.
//		paymentData.getPayment().setPayBeginServerResult(serverPaymentResult);

		if (serverPaymentResult.isFailed())
			throw new PaymentException(serverPaymentResult);

		if (postponed) {
			if (!PaymentResult.CODE_POSTPONED.equals(serverPaymentResult.getCode()) && !serverPaymentResult.isRolledBack()) {
				String msg = "The Payment \"" + paymentData.getPayment().getPrimaryKey() + "\" is marked postponed, but the PaymentProcessor \"" + serverPaymentProcessor.getPrimaryKey() + "\" did neither rollback nor return PaymentResult.CODE_POSTPONED! Instead it returned code=\"" + serverPaymentResult.getCode() + "\" text=\"" + serverPaymentResult.getText() + "\"";
				logger.warn(msg, new IllegalStateException(msg));
			}
		}

		if (paymentData.getPayment().isPostponed()) {
			PayMoneyTransfer payMoneyTransfer = PayMoneyTransfer.getPayMoneyTransferForPayment(
					getPersistenceManager(), paymentData.getPayment());

			if (payMoneyTransfer != null) {
				logger.warn("Your Payment \""+paymentData.getPayment()+"\" has first " +
						"created a payMoneyTransfer and decided afterwards (in payEnd) to" +
						"postpone. This is not nice! Now I have to rollback your " +
						"PayMoneyTransfer! You should postpone a Payment always in payBegin!");

				payRollback(user, paymentData);
			}
			else
				paymentData.getPayment().clearPending();
		}
		else {
			if (!serverPaymentResult.isPaid())
				throw new PaymentException(serverPaymentResult);
		}

		if (paymentData.getPayment().isPending() && !paymentData.getPayment().isFailed())
			throw new IllegalStateException("Payment should not be pending anymore, because failed is false! How's that possible?");

		try {
			for (Invoice invoice : paymentData.getPayment().getInvoices()) {
				for (InvoiceActionHandler invoiceActionHandler : invoice.getInvoiceLocal().getInvoiceActionHandlers()) {
					invoiceActionHandler.onPayEnd(user, paymentData, invoice);
				}
			}
		} catch (Exception x) {
			throw new PaymentException(
					new PaymentResult(
							PaymentResult.CODE_FAILED,
							"Calling InvoiceActionHandler.onPayEnd failed!",
							x));
		}
		
		try {
			for (PaymentActionHandler paymentActionHandler : paymentData.getPayment().getPaymentLocal().getPaymentActionHandlers()) {
				paymentActionHandler.onPayEnd(paymentData);
			}
		} catch (PaymentException x) {
			throw x;
		} catch (Exception e) {
			throw new PaymentException(new PaymentResult(
					PaymentResult.CODE_FAILED, "Calling PaymentActionHandler.onPayEnd failed! localOrganisation="+getOrganisationID(), e)); 
		}

		try {
			for (Invoice invoice : paymentData.getPayment().getInvoices()) {
				InvoiceLocal invoiceLocal = invoice.getInvoiceLocal();
				if (invoiceLocal.getAmountToPay() == 0) {
					JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
					try {
						ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(invoiceLocal.getJbpmProcessInstanceId());
						if (!JbpmConstantsInvoice.Both.NODE_NAME_PAID.equals(processInstance.getRootToken().getNode().getName())) {
							processInstance.signal(JbpmConstantsInvoice.Both.TRANSITION_NAME_PAY);
						}
					} finally {
						jbpmContext.close();
					}
				}
			}
		} catch (Exception x) {
			throw new PaymentException(
					new PaymentResult(
							PaymentResult.CODE_FAILED,
							"Signalling transition \"" + JbpmConstantsInvoice.Both.TRANSITION_NAME_PAY + "\" failed!",
							x));
		}

		return serverPaymentResult;
	}


	/**
	 * This method is called to rollback a payment. It removes all transfers
	 * and the accounts adjust their balance.
	 * <p>
	 * It is not integrated within payxxxEnd
	 * (e.g. {@link #payInvoicesEnd(User, PaymentData, PayMoneyTransfer)}),
	 * because it needs to be called within a separate transaction. 
	 */
	public void payRollback(
			User user, PaymentData paymentData)
	{
		Payment payment = paymentData.getPayment();
		
		PayMoneyTransfer payMoneyTransfer = PayMoneyTransfer.getPayMoneyTransferForPayment(
				getPersistenceManager(), payment);

		if (!paymentData.getPayment().isPending())
			throw new IllegalStateException("Payment \"" + paymentData.getPayment().getPrimaryKey() + "\" is not pending! Cannot rollback!");

		if (payMoneyTransfer == null) {
			payment.setRollbackStatus(Payment.ROLLBACK_STATUS_DONE_WITHOUT_ACTION);
			return;
		}

		PersistenceManager pm = getPersistenceManager();

		Set<Anchor> involvedAnchors = new HashSet<Anchor>();
		ArrayList<Transfer> containers = new ArrayList<Transfer>(1);
		containers.add(payMoneyTransfer);
		boolean failed = true;
		try {

			for (Iterator it = payMoneyTransfer.getChildren().iterator(); it.hasNext(); ) {
				MoneyTransfer moneyTransfer = (MoneyTransfer) it.next();

				if (moneyTransfer.isBooked())
					moneyTransfer.rollbackTransfer(user, involvedAnchors);

				pm.deletePersistent(moneyTransfer);
			}

			if (payMoneyTransfer.isBooked())
				payMoneyTransfer.rollbackTransfer(user, involvedAnchors);

			Anchor.checkIntegrity(containers, involvedAnchors);

			failed = false;
		} finally  {
			if (failed)
				Anchor.resetIntegrity(containers, involvedAnchors);
		}

		pm.deletePersistent(payMoneyTransfer);

		payment.setRollbackStatus(Payment.ROLLBACK_STATUS_DONE_NORMAL);
	}


	/**
	 * Performs payment with the given amount associated to the given invoices.
	 *
	 * @param user
	 * @param invoices
	 * @param currency
	 * @param amount
	 * @param serverPaymentProcessor May be <tt>null</tt>. If <tt>null</tt>, an
	 *		appropriate <tt>ServerPaymentProcessor</tt> will be searched for the given
	 *		<tt>ModeOfPaymentFlavour</tt>. Note that, if multiple <tt>ServerPaymentProcessor</tt>s
	 *		are found, it is arbitrary which one of them will be used.
	 * @param paymentDirection One of {@link ServerPaymentProcessor#PAYMENT_DIRECTION_INCOMING} or {@link ServerPaymentProcessor#PAYMENT_DIRECTION_OUTGOING}.
	 * @param paymentData Either <tt>null</tt> or a descendent of {@link PaymentData} depending on what your client sided payment processor has created.
	 * @param clientPaymentResult Before the server payment is done, a client-sided payment can be done.
	 *
	 * @return Returns the <tt>PaymentResult</tt>, which is additionally set in
	 *		<tt>paymentData.getPayment().setPayBeginServerResult(...)</tt>.
	 *
	 * @throws PaymentException 
	 */
	public PaymentResult payBegin(
			User user, PaymentData paymentData)
	throws PaymentException
	{
		if (user == null)
			throw new NullPointerException("user");

		if (paymentData == null)
			throw new NullPointerException("paymentData");

		if (paymentData.getPayment() == null)
			throw new NullPointerException("paymentData.getPayment() returns null");

//		if (paymentData.getPayment().getInvoices() == null)
//			throw new NullPointerException("paymentData.getPayment().getInvoices() returns null");
//
//		if (invoices.size()<= 0)
//			throw new IllegalArgumentException("This kind of payment must be associated with at last one Invoice!");

		if (paymentData.getPayment().getCurrency() == null)
			throw new NullPointerException("paymentData.getPayment().getCurrency() returns null!");

		if (paymentData.getPayment().getAmount() < 0)
			throw new IllegalArgumentException("paymentData.getPayment().getAmount() < 0! amount must NOT be negative! Use paymentDirection to specify the direction of the money flow.");

		if (paymentData.getPayment().getModeOfPaymentFlavour() == null)
			throw new NullPointerException("paymentData.getPayment().getModeOfPaymentFlavour()");

		ServerPaymentProcessor serverPaymentProcessor = getServerPaymentProcessor(
				paymentData.getPayment());

		LegalEntity partner = null;
		if (paymentData.getPayment().getInvoices() != null) {
			partner = bookInvoicesImplicitelyAndGetPartner(user,
					paymentData.getPayment().getInvoices(),
					paymentData.getPayment().getCurrency());
		}
		if (partner == null) {
			partner = paymentData.getPayment().getPartner();
		}
		else {
			if (!partner.getPrimaryKey().equals(paymentData.getPayment().getPartner().getPrimaryKey()))
				throw new IllegalArgumentException("paymentData.getPayment().getPartner() does not match the partner of paymentData.getPayment().getInvoices()!");
		}

		if (partner.getAccountant() == null)
			partner.setAccountant(getPartnerAccountant());
		
//	The PaymentLocal object is normally created in PaymentHelperBean#payBegin_storePaymentData(PaymentData).
//	But some use cases do not use this API, this is why we create it here if it does not exist yet.  
		if (paymentData.getPayment().getPaymentLocal() == null)
			new PaymentLocal(paymentData.getPayment());

		// call server-sided payment processor's first phase
		PayMoneyTransfer payMoneyTransfer = serverPaymentProcessor.payBegin(
				new PayParams(this, user, paymentData));

		PaymentResult serverPaymentResult;
		serverPaymentResult = paymentData.getPayment().getPayBeginServerResult();
		if (serverPaymentResult == null)
			throw new PaymentException(
					new PaymentResult(
							PaymentResult.CODE_FAILED,
							"paymentData.getPayment().getPayBeginServerResult() returned null! You probably forgot to set it in your ServerPaymentProcessor (\""+serverPaymentProcessor.getPrimaryKey()+"\")!",
							(Throwable)null));

		if (serverPaymentResult.isFailed())
			throw new PaymentException(serverPaymentResult);

//		// I don't know why, but without the following line, it is not set in the datastore.
//		paymentData.getPayment().setPayBeginServerResult(serverPaymentResult);

		try {
			for (Invoice invoice : paymentData.getPayment().getInvoices()) {
				for (InvoiceActionHandler invoiceActionHandler : invoice.getInvoiceLocal().getInvoiceActionHandlers()) {
					invoiceActionHandler.onPayBegin(user, paymentData, invoice);
				}
			}
		} catch (PaymentException x) {
			throw x;
		} catch (Exception x) {
			throw new PaymentException(
					new PaymentResult(
							PaymentResult.CODE_FAILED,
							"Calling InvoiceActionHandler.onPayBegin failed!",
							x));
		}
		
		try {
			for (PaymentActionHandler paymentActionHandler : paymentData.getPayment().getPaymentLocal().getPaymentActionHandlers()) {
				paymentActionHandler.onPayBegin(paymentData);
			}
		} catch (PaymentException x) {
			throw x;
		} catch (Exception e) {
			throw new PaymentException(new PaymentResult(
					PaymentResult.CODE_FAILED, "Calling PaymentActionHandler.onPayBegin failed! localOrganisation="+getOrganisationID(), e)); 
		}

		if (paymentData.getPayment().isPostponed()) {
			// if we have a PayMoneyTransfer, we need to delete it from datastore
			if (payMoneyTransfer != null) {
				if (payMoneyTransfer.isBookedFrom() || payMoneyTransfer.isBookedTo())
					throw new IllegalStateException("PayMoneyTransfer is already booked! You should never book the PayMoneyTransfer in your ServerPaymentProcessor! Check the class \""+serverPaymentProcessor.getClass()+"\"!");

				getPersistenceManager().deletePersistent(payMoneyTransfer);
				payMoneyTransfer = null;
			}
		}
		else { // not postponed
			if (!serverPaymentResult.isApproved())
				throw new PaymentException(serverPaymentResult);

			if (payMoneyTransfer == null)
				throw new NullPointerException("serverPaymentProcessor.payBegin(...) returned null but Payment is NOT postponed! You are only allowed (and you should) return null, if you postpone a Payment! serverPaymentProcessorPK=\""+serverPaymentProcessor.getPrimaryKey()+"\"");

			Set<Anchor> involvedAnchors = new HashSet<Anchor>();
			ArrayList<Transfer> containers = new ArrayList<Transfer>(1);
			containers.add(payMoneyTransfer);
			boolean failed = true;
			try {
				payMoneyTransfer.bookTransfer(user, involvedAnchors);

				// check consistence
				Anchor.checkIntegrity(containers, involvedAnchors);

				failed = false;
			} finally  {
				if (failed)
					Anchor.resetIntegrity(containers, involvedAnchors);
			}

		}

		return serverPaymentResult;
	}


	/**
	 * @return Never returns <tt>null</tt>. If {@link Payment#getServerPaymentProcessorID()}
	 *		returns <tt>null</tt>, a suitable processor is searched according to the given
	 *		<tt>ModeOfPaymentFlavour</tt>. If no processor can be found at all, an
	 *		<tt>IllegalStateException</tt> is thrown.
	 */
	protected ServerPaymentProcessor getServerPaymentProcessor(
			Payment payment)
	{
		ModeOfPaymentFlavour modeOfPaymentFlavour = payment.getModeOfPaymentFlavour();
		ServerPaymentProcessorID serverPaymentProcessorID = payment.getServerPaymentProcessorID();
		// get ServerPaymentProcessor, if serverPaymentProcessorID is defined.
		ServerPaymentProcessor serverPaymentProcessor = null;
		if (serverPaymentProcessorID != null) {
			PersistenceManager pm = getPersistenceManager();
			pm.getExtent(ServerPaymentProcessor.class);
			serverPaymentProcessor = (ServerPaymentProcessor) pm.getObjectById(serverPaymentProcessorID);
		}

		if (serverPaymentProcessor == null) {
			Collection c = ServerPaymentProcessor.getServerPaymentProcessorsForOneModeOfPaymentFlavour(
					getPersistenceManager(),
					modeOfPaymentFlavour);
			if (c.isEmpty())
				throw new IllegalStateException("No ServerPaymentProcessor registered for ModeOfPaymentFlavour \""+modeOfPaymentFlavour.getPrimaryKey()+"\"!");

			serverPaymentProcessor = (ServerPaymentProcessor) c.iterator().next();
		} // if (serverPaymentProcessor == null) {

		return serverPaymentProcessor;
	}

	/**
	 * This method is a noop, if the offer is already accepted. If the Offer cannot be accepted implicitely
	 * (either because the business partner doesn't allow implicit acceptance or because the jBPM token is at
	 * a position where this is not possible, an exception is thrown).
	 */
	protected void bookInvoiceImplicitely(Invoice invoice)
	{
		InvoiceID invoiceID = (InvoiceID) JDOHelper.getObjectId(invoice);
		if (State.hasState(getPersistenceManager(), invoiceID, JbpmConstantsInvoice.Both.NODE_NAME_BOOKED))
			return;

		JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
		try {
			ProcessInstance processInstance = jbpmContext.getProcessInstance(invoice.getInvoiceLocal().getJbpmProcessInstanceId());
			processInstance.signal(JbpmConstantsInvoice.Vendor.TRANSITION_NAME_BOOK_IMPLICITELY);
		} finally {
			jbpmContext.close();
		}
	}

	/**
	 * @param invoices Can be null. Should be a <tt>Collection</tt> of {@link Invoice}
	 * @param currency Can be null.
	 * @return Either <tt>null</tt>, in case no Invoice was passed or the partner
	 *		(if at least one Invoice has been passed in <tt>invoices</tt>).
	 */
	protected LegalEntity bookInvoicesImplicitelyAndGetPartner(User user, Collection invoices, Currency currency)
	{
		if (invoices == null)
			return null;

//	 check currency and find out partner
		// ...maybe it will later be possible to pay an invoice in a different
		// currency, but currently this is not possible.
//		LegalEntity mandator = getMandator();
		String mandatorPK = getMandator().getPrimaryKey();
		LegalEntity partner = null;
		for (Iterator it = invoices.iterator(); it.hasNext(); ) {
			Invoice invoice = (Invoice) it.next();

//			finalizeInvoice(user, invoice);
			bookInvoiceImplicitely(invoice);

			if (currency == null)
				currency = invoice.getCurrency();
			else {
				if (!currency.getCurrencyID().equals(invoice.getCurrency().getCurrencyID()))
					throw new IllegalArgumentException("The invoice \""+invoice.getPrimaryKey()+"\" does not match the currency " + currency.getCurrencyID() + "!");
			}

			if (mandatorPK.equals(invoice.getVendor().getPrimaryKey())) {
				if (partner == null)
					partner = invoice.getCustomer();
				else {
					String foundPartnerPK = invoice.getCustomer().getPrimaryKey();
					if (!partner.getPrimaryKey().equals(foundPartnerPK))
						throw new IllegalArgumentException("Customer of invoice \"" + invoice.getPrimaryKey() + "\" does not match other invoices' partners! Expected partner \"" + partner.getPrimaryKey() + "\", but found \"" + foundPartnerPK + "\"!");
				}
			} // vendor is mandator
			else {
				if (!mandatorPK.equals(invoice.getCustomer().getPrimaryKey()))
					throw new IllegalArgumentException("The invoice \""+invoice.getPrimaryKey()+"\" has nothing to do with the mandator (\"" + mandator.getPrimaryKey() + "\")!");

				if (partner == null)
					partner = invoice.getVendor();
				else {
					String foundPartnerPK = invoice.getVendor().getPrimaryKey();
					if (!partner.getPrimaryKey().equals(foundPartnerPK))
						throw new IllegalArgumentException("Vendor of invoice \"" + invoice.getPrimaryKey() + "\" does not match other invoices' partners! Expected partner \"" + partner.getPrimaryKey() + "\", but found \"" + foundPartnerPK + "\"!");
				}
			}
		}

		return partner;
	}

//	/**
//	 * Finalizes an invoice and sends it to the involved 
//	 * organisation if neccessary.
//	 * 
//	 * @param finalizer
//	 * @param invoice
//	 */
//	public void finalizeInvoice(User finalizer, Invoice invoice) {
//		if (invoice.isFinalized())
//			return;
//
//		if (!invoice.getVendor().getPrimaryKey().equals(getMandator().getPrimaryKey()))
//			throw new IllegalArgumentException("Can not finalize an invoice where mandator is not vendor of this invoice!");
//
//		// invoice.setFinalized(...) does nothing, if it is already finalized.
//		invoice.setFinalized(finalizer);
//		if (invoice.getCustomer() instanceof OrganisationLegalEntity) {
//			// TODO: Put the Invoice in the queue on this organisations server ...
//			throw new UnsupportedOperationException("NYI");
//		}
//	}

//	/**
//	 * Creates and persists a new Account for the mandator.
//	 * 
//	 * @param anchorID
//	 * @param currency
//	 * @return
//	 */
//	public Account createMandatorAccount(String anchorID, Currency currency, boolean createSummaryAccount) {
//		Account newAccount = null;
//		if (createSummaryAccount) {
//			newAccount = new SummaryAccount(
//					getMandator().getOrganisationID(),
//					anchorID, 
//					getMandator(), 
//					currency
//			);
//		}
//		else {
//			newAccount = new Account(
//					getMandator().getOrganisationID(),
//					Account.ANCHOR_TYPE_ID_LOCAL_NORMAL,
//					anchorID,
//					getMandator(),
//					currency
//			);
//		}
//		getPersistenceManager().makePersistent(newAccount);
//		return newAccount;
//	}

	/**
	 * Finds (and creates if neccessary) the right Account for the given LegalEntity and Currency.
	 * 
	 * @param accountType See {@link Account} for static anchorTypeID definitions
	 * @param partner The legal entity the account should be searched for. 
	 * @param currency The currency the account should record.
	 * @return The found or created acccount. Never null.
	 */
	public Account getPartnerAccount(AccountType accountType, LegalEntity partner, Currency currency) {
		if (partner == null)
			throw new IllegalArgumentException("Parameter partner must not be null!");
		if (currency == null)
			throw new IllegalArgumentException("Parameter currency must not be null!");

		Collection<Account> accounts = (Collection<Account>) Account.getAccounts(getPersistenceManager(), accountType, partner, currency);
		// there should be only one account, but in case a user later adds one, we don't throw an exception
		Account account = accounts.isEmpty() ? null : accounts.iterator().next();
		if (account == null) {
			// TODO how to generate the IDs here? Give the user the possibility to define rules (e.g. number ranges)
			account = new Account(
					this.getOrganisationID(),
					"partner." + ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Anchor.class, Account.ANCHOR_TYPE_ID_ACCOUNT + "partner")),
					accountType, partner, currency);
			account = getPersistenceManager().makePersistent(account);
			account.setOwner(partner);
		}

//		String searchAccountID = accountType.getOrganisationID() + ':' + accountType.getAccountTypeID() + ':' + partner.getOrganisationID() + ':' + partner.getAnchorID() + ':' + currency.getCurrencyID(); 
//		AnchorID anchorID = AnchorID.create(this.getOrganisationID(), Account.ANCHOR_TYPE_ID_ACCOUNT, searchAccountID);
//
//		Account account = null;
//		Object o = null;
//		try {
//			o = getPersistenceManager().getObjectById(anchorID);
//			account = (Account)o;
//		} 
//		catch (ClassCastException ce)  {
//			IllegalStateException ill = new IllegalStateException("Found persistent object with oid "+anchorID+" but is not of type Account but "+o.getClass().getName());
//			ill.initCause(ce);
//			throw ill;
//		}
//		catch (JDOObjectNotFoundException je) {
//			// account not existing, create it 
//			account = new Account(this.getOrganisationID(), searchAccountID, partner, currency, accountType);
//			account = getPersistenceManager().makePersistent(account);
//			account.setOwner(partner);
//		}
//
//		if (account == null)
//			throw new IllegalStateException("Account with oid "+anchorID+" could neither be found nor created!");
//		
//		if (!account.getOwner().equals(partner))
//			throw new IllegalStateException("An account for oid "+anchorID+" could be found, but its owner is not the partner the search was performed for. Owner: "+account.getOwner().getPrimaryKey()+", Partner: "+partner.getPrimaryKey());
//
//		if (!account.getAccountType().equals(accountType))
//			throw new IllegalStateException("An account for oid "+anchorID+" could be found, but its accountType is not the accountType the search was performed for. assignedAccountType: "+JDOHelper.getObjectId(account.getAccountType())+", expectedAccountType: "+JDOHelper.getObjectId(accountType));

		return account;
	}

	public void jdoPreStore()
	{
//		if (_nextMoneyFlowMappingID >= 0 && nextMoneyFlowMappingID != _nextMoneyFlowMappingID)
//			nextMoneyFlowMappingID = _nextMoneyFlowMappingID;
	}

	private static void setStateDefinitionProperties(
			ProcessDefinition processDefinition, String jbpmNodeName,
			String name, String description, boolean publicState)
	{
		StateDefinition stateDefinition;
		try {
			stateDefinition = StateDefinition.getStateDefinition(processDefinition, jbpmNodeName);
		} catch (JDOObjectNotFoundException x) {
			logger.warn("The ProcessDefinition \"" + processDefinition.getJbpmProcessDefinitionName() + "\" does not contain a jBPM Node named \"" + jbpmNodeName + "\"!");
			return;
		}
		stateDefinition.getName().setText(Locale.ENGLISH.getLanguage(), name);
		stateDefinition.getDescription().setText(Locale.ENGLISH.getLanguage(), description);
		stateDefinition.setPublicState(publicState);
	}

	public ProcessDefinition storeProcessDefinitionInvoice(TradeSide tradeSide, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();

		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);

		// we add the events+actionhandlers
		ActionHandlerNodeEnter.register(jbpmProcessDefinition);

		if (TradeSide.vendor == tradeSide) {
			ActionHandlerFinalizeInvoice.register(jbpmProcessDefinition);
			ActionHandlerBookInvoiceImplicitely.register(jbpmProcessDefinition);
		}

		ActionHandlerBookInvoice.register(jbpmProcessDefinition);

		// store the process definition
		ProcessDefinition processDefinition = ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition, jbpmProcessDefinitionURL);
		ProcessDefinitionID processDefinitionID = (ProcessDefinitionID) JDOHelper.getObjectId(processDefinition);

		setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Both.NODE_NAME_BOOKED,
				"booked",
				"Booked.",
				true);

		setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Both.NODE_NAME_SENT,
				"sent",
				"sent",
				true);

		setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Both.NODE_NAME_PAID,
				"paid",
				"paid",
				true);

		switch (tradeSide) {
			case vendor:
			{
				// give known StateDefinitions a name and a description
				setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Vendor.NODE_NAME_CREATED,
						"created",
						"The Invoice has been newly created. This is the first state in the Invoice related workflow.",
						true);

				setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Vendor.NODE_NAME_ABORTED,
						"aborted",
						"Aborted.",
						true);

				setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Vendor.NODE_NAME_BOOKED_UNRECEIVABLE,
						"booked unreceivable",
						"booked unreceivable",
						true);

				setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Vendor.NODE_NAME_DOUBTFUL,
						"doubtful",
						"doubtful",
						true);

				setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Vendor.NODE_NAME_FINALIZED,
						"finalized",
						"finalized",
						true);

				setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Vendor.NODE_NAME_SENT_PRE_COLLECTION_LETTER,
						"sent pre-collection letter",
						"sent pre-collection letter",
						true);

				setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Vendor.NODE_NAME_SENT_REMINDER,
						"sent reminder",
						"sent reminder",
						true);

				setStateDefinitionProperties(processDefinition, JbpmConstantsInvoice.Vendor.NODE_NAME_UNCOLLECTABLE,
						"uncollectable",
						"uncollectable",
						true);

				for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsInvoice.Vendor.TRANSITION_NAME_BOOK_IMPLICITELY)) {
					transition.setUserExecutable(false);
				}
			}
			break;
			case customer:
			{

			}
			break;
			default:
				throw new IllegalStateException("Unknown TradeSide: " + tradeSide);
		}


		return processDefinition;
	}

	/**
	 * Create and book a manual money transfer between two accounts.
	 *
	 * TODO document this method completely!
	 *
	 * @param user
	 * @param from
	 * @param to
	 * @param currency
	 * @param amount
	 * @param reason
	 * @return
	 */
	public ManualMoneyTransfer createManualMoneyTransfer(
			User user, Account from, Account to,
			Currency currency, long amount,
			I18nText reason)
	{
		PersistenceManager pm = getPersistenceManager();

		// create the new ManualMoneyTransfer and persist it
		ManualMoneyTransfer manualMoneyTransfer = new ManualMoneyTransfer(user, from, to, currency, amount);
		manualMoneyTransfer.getReason().copyFrom(reason);
		manualMoneyTransfer = pm.makePersistent(manualMoneyTransfer);

		// now we still have to book the new transfer
		Set<Anchor> involvedAnchors = new HashSet<Anchor>();

		ArrayList<Transfer> containers = new ArrayList<Transfer>(1);
		containers.add(manualMoneyTransfer);
		boolean failed = true;
		try {
			manualMoneyTransfer.bookTransfer(user, involvedAnchors);
	
			// check consistence
			Anchor.checkIntegrity(containers, involvedAnchors);

			failed = false;
		} finally  {
			if (failed)
				Anchor.resetIntegrity(containers, involvedAnchors);
		}

		return manualMoneyTransfer;
	}
}
