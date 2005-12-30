/*
 * Created on 27.10.2004
 */
package org.nightlabs.ipanema.accounting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;

import org.nightlabs.ipanema.accounting.book.BookMoneyTransfer;
import org.nightlabs.ipanema.accounting.book.LocalAccountant;
import org.nightlabs.ipanema.accounting.book.PartnerAccountant;
import org.nightlabs.ipanema.accounting.id.InvoiceID;
import org.nightlabs.ipanema.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.ipanema.accounting.pay.PayMoneyTransfer;
import org.nightlabs.ipanema.accounting.pay.Payment;
import org.nightlabs.ipanema.accounting.pay.PaymentData;
import org.nightlabs.ipanema.accounting.pay.PaymentException;
import org.nightlabs.ipanema.accounting.pay.PaymentResult;
import org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams;
import org.nightlabs.ipanema.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.ipanema.accounting.priceconfig.IPriceConfigIDProvider;
import org.nightlabs.ipanema.organisation.LocalOrganisation;
import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.trade.Article;
import org.nightlabs.ipanema.trade.ArticleContainer;
import org.nightlabs.ipanema.trade.LegalEntity;
import org.nightlabs.ipanema.trade.Offer;
import org.nightlabs.ipanema.trade.Order;
import org.nightlabs.ipanema.trade.OrganisationLegalEntity;
import org.nightlabs.ipanema.trade.id.ArticleID;
import org.nightlabs.ipanema.transfer.Anchor;
import org.nightlabs.ipanema.transfer.Transfer;
import org.nightlabs.ipanema.transfer.TransferRegistry;
import org.nightlabs.ipanema.transfer.id.AnchorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="datastore"
 *		detachable="true"
 *		table="JFireTrade_Accounting"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class Accounting
	implements TransferRegistry, IPriceConfigIDProvider, StoreCallback
{
	public static final Logger LOGGER = Logger.getLogger(Accounting.class);

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

		// initialize the organisationID and all other members
//		it = pm.getExtent(LocalOrganisation.class).iterator();
//		if (!it.hasNext())
//			throw new IllegalStateException("LocalOrganisation undefined in datastore!");
//		LocalOrganisation localOrganisation = (LocalOrganisation) it.next();
//		String organisationID = localOrganisation.getOrganisation().getOrganisationID();
		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		accounting.organisationID = organisationID;
		accounting.mandator = OrganisationLegalEntity.getOrganisationLegalEntity(pm, organisationID, OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION, true); // new OrganisationLegalEntity(localOrganisation.getOrganisation());
		accounting.accountingPriceConfig = new AccountingPriceConfig(organisationID, accounting.createPriceConfigID());
		accounting.localAccountant = new LocalAccountant(accounting.mandator, LocalAccountant.class.getName());
		accounting.mandator.setAccountant(accounting.localAccountant);
		accounting.partnerAccountant = new PartnerAccountant(organisationID, PartnerAccountant.class.getName());

		pm.makePersistent(accounting);
		return accounting;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
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

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long nextPriceCoordinateID = 0;
	private static long _nextPriceCoordinateID = -1;
	private static Object _nextPriceCoordinateIDMutex = new Object();

	public long createPriceCoordinateID()
	{
		synchronized (_nextPriceCoordinateIDMutex) {
			if (_nextPriceCoordinateID < 0)
				_nextPriceCoordinateID = nextPriceCoordinateID;

			long res = _nextPriceCoordinateID++;
			nextPriceCoordinateID = _nextPriceCoordinateID;
			return res;
		}
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager accountingPM = JDOHelper.getPersistenceManager(this);
		if (accountingPM == null)
			throw new IllegalStateException("This instance of Accounting is not persistent, can not get a PersistenceManager!");

		return accountingPM;
	}
	
	public OrganisationLegalEntity getMandator() {
		return mandator;
	}
	
//	/**
//	 * key: String productPK<br/>
//	 * value: ProductInfo productInfo
//	 * 
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="ProductInfo"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 *
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 201"
//	 */
//	protected Map productInfos = new HashMap();
	
//	public ProductInfo getProductInfo(String organisationID, String productID, boolean throwExceptionIfNotExistent)
//	{
//		String pk = ProductInfo.getPrimaryKey(organisationID, productID);
//		ProductInfo res = (ProductInfo) productInfos.get(pk);
//		if (res == null && throwExceptionIfNotExistent)
//			throw new IllegalArgumentException("No ProductInfo existing with organisationID=\""+organisationID+"\" and productID=\""+productID+"\"!");
//		return res;
//	}
//
//	public ProductInfo getProductInfo(ProductType product, boolean throwExceptionIfNotExistent)
//	{
//		String pk = product.getPrimaryKey();
//		ProductInfo res = (ProductInfo) productInfos.get(pk);
//		if (res == null && throwExceptionIfNotExistent)
//			throw new IllegalArgumentException("No ProductInfo existing for product \""+pk+"\"!");
//		return res;
//	}
//
//	public void addProductInfo(ProductInfo productInfo)
//	{
//		productInfos.put(productInfo.getPrimaryKey(), productInfo);
//	}

//	/**
//	 * This method creates a ProductInfo by looking up and delegating to
//	 * the ProductInfoFactory which is associated to the extended ProductInfo.
//	 * If there is either no extended ProductInfo or it does not have a
//	 * ProductInfoFactory associated, this method creates an instance of
//	 * <tt>ProductInfo</tt> (the root object).
//	 * <p>
//	 * If there exists already a ProductInfo for the given ProductType, it will
//	 * simply be returned without any changes.
//	 *
//	 * @param product The product for which to create a ProductInfo
//	 */
//	public ProductInfo createProductInfo(ProductType product)
//		throws ModuleException
//	{
//		String pk = product.getPrimaryKey();
//		ProductInfo productInfo = (ProductInfo) productInfos.get(pk);
//
//		if (productInfo == null) {
//			ProductInfo extendedProductInfo = null;
//			ProductInfoFactory productInfoFactory = null;
//			ProductType extendedProduct = product.getExtendedProductType();
//
//			if (extendedProduct != null)
//				extendedProductInfo = getProductInfo(extendedProduct, true);
//
//			if (extendedProductInfo != null)
//				productInfoFactory = extendedProductInfo.getProductInfoFactory();
//
//			if (productInfoFactory != null)
//				productInfo = productInfoFactory.createProductInfo(product, extendedProductInfo);
//			else
//				productInfo = new ProductInfo(product, extendedProductInfo);
//
//			addProductInfo(productInfo);
//			updatePackagedProductInfos(productInfo);
//		}
//
//		return productInfo;
//	}

	/**
	 * @return Returns the accountingPriceConfig.
	 */
	public AccountingPriceConfig getAccountingPriceConfig()
	{
		return accountingPriceConfig;
	}

//	/**
//	 * This method sets the <tt>packagedProductInfos</tt> to match the
//	 * <tt>packagedProducts</tt> of the <tt>ProductType</tt> that is analog
//	 * to the given <tt>ProductInfo</tt>
//	 *
//	 * @param productInfo The ProductInfo whose packagedProductInfos shall be updated.
//	 */
//	public void updatePackagedProductInfos(ProductInfo productInfo)
//	{
//		// Add all missing packaged ProductInfo s.
//
//		// In packagedProductPKs we store the String productPK s that should exist.
//		HashSet packagedProductPKs = new HashSet();
//		for (Iterator it = productInfo.getProduct().getPackagedProductTypes().iterator(); it.hasNext(); ) {
//			ProductType packagedProduct = (ProductType)it.next();
//			packagedProductPKs.add(packagedProduct.getPrimaryKey());
//			ProductInfo packagedProductInfo = productInfo.getPackagedProductInfo(
//					packagedProduct.getOrganisationID(), packagedProduct.getProductTypeID(), false);
//			if (packagedProductInfo == null) {
//				packagedProductInfo = getProductInfo(packagedProduct, true);
//				productInfo.addPackagedProductInfo(packagedProductInfo);
//			}
//		}
//
//
//		// Remove all packaged ProductInfo s which are not registered in packagedProductPKs
//
//		// Because we cannot remove while iterating, we need to store everything that should
//		// be deleted in packagedProductInfosToDelete
//		HashSet packagedProductInfosToDelete = new HashSet();
//		for (Iterator it = productInfo.getPackagedProductInfos().iterator(); it.hasNext(); ) {
//			ProductInfo packagedProductInfo = (ProductInfo)it.next();
//			if (!packagedProductPKs.contains(packagedProductInfo.getPrimaryKey()))
//				packagedProductInfosToDelete.add(packagedProductInfo);
//		}
//
//		for (Iterator it = packagedProductInfosToDelete.iterator(); it.hasNext(); ) {
//			ProductInfo packagedProductInfo = (ProductInfo)it.next();
//			productInfo.removePackagedProductInfo(
//					packagedProductInfo.getOrganisationID(), packagedProductInfo.getProductID());
//		}
//	}

/////////// begin implementation of TransferRegistry /////////////
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	
	private long nextTransferID = 0;
	private static long _nextTransferID = -1;
	private static Object _nextTransferIDMutex = new Object();

	/**
	 * This method adds an instance of Transfer. This is not necessary, if the Transfer has been created
	 * by this organisation, because every Transfer does a self-registration.
	 *
	 * @param transfer
	 */
	public void addTransfer(Transfer transfer)
	{
		if (transfer == null)
			throw new NullPointerException("transfer is null!");

		if (transfer.getOrganisationID() == null)
			throw new NullPointerException("transfer.organisationID is null!");

		if (transfer.getTransferID() < 0)
			throw new NullPointerException("transfer.transferID < 0!");

		getPersistenceManager().makePersistent(transfer);
	}

	public long createTransferID(String transferTypeID)
	{
		if (!MoneyTransfer.TRANSFERTYPEID.equals(transferTypeID))
			throw new IllegalArgumentException("This implementation of TransferRegistry manages only Transfers with transferTypeID=\""+MoneyTransfer.TRANSFERTYPEID+"\"!");

		synchronized (_nextTransferIDMutex) {
			if (_nextTransferID < 0)
				_nextTransferID = nextTransferID;

			long res = _nextTransferID++;
			nextTransferID = _nextTransferID;
			return res;
		}
	}
/////////// end implementation of TransferRegistry /////////////

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	
	private long nextPriceConfigID = 0;
	private static long _nextPriceConfigID = -1;
	private static Object _nextPriceConfigIDMutex = new Object();

	public long createPriceConfigID() {
		synchronized (_nextPriceConfigIDMutex) {
			if (_nextPriceConfigID < 0)
				_nextPriceConfigID = nextPriceConfigID;

			long res = _nextPriceConfigID++;
			nextPriceConfigID = _nextPriceConfigID;
			return res;
		}
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	
	private int nextMoneyFlowMappingID = 0;
	private static int _nextMoneyFlowMappingID = -1;
	private static Object _nextMoneyFlowMappingIDMutex = new Object();
	
	public int createMoneyFlowMappingID() {
		synchronized(_nextMoneyFlowMappingIDMutex) {
			if (_nextMoneyFlowMappingID < 0)
				_nextMoneyFlowMappingID = nextMoneyFlowMappingID;

			int res = _nextMoneyFlowMappingID;
			_nextMoneyFlowMappingID = res + 1;
			nextMoneyFlowMappingID = _nextMoneyFlowMappingID;
			return res;
		}
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	
	private long nextInvoiceID = 0;
	private static long _nextInvoiceID = -1;
	private static Object _nextInvoiceIDMutex = new Object();

	protected long createInvoiceID() {
		synchronized (_nextInvoiceIDMutex) {
			if (_nextInvoiceID < 0)
				_nextInvoiceID = nextInvoiceID;

			long res = _nextInvoiceID;
			_nextInvoiceID = res + 1;
			nextInvoiceID = _nextInvoiceID;
			return res;
		}
	}

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
	public Invoice createInvoice(User user, Collection articles)
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
			
			if (!articleOffer.getOfferLocal().isConfirmed()) {
				throw new InvoiceEditException(
					InvoiceEditException.REASON_OFFER_NOT_CONFIRMED, 
					"At least one involved offer is not confirmed!",
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

		Invoice invoice = new Invoice(
				user, vendorLE, customerLE, createInvoiceID(), invoiceCurrency); // , desiredModeOfPaymentFlavour);
		new InvoiceLocal(invoice); // registers itself in the invoice
		getPersistenceManager().makePersistent(invoice);
		for (Iterator iter = articles.iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			invoice.addArticle(article);
		}

		return invoice;
	}

	public void addArticlesToInvoice(Invoice invoice, Collection articles)
	throws InvoiceEditException
	{
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
			invoice.addArticle(article);
		}
	}

	public void removeArticlesFromInvoice(Invoice invoice, Collection articles)
	throws InvoiceEditException
	{
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
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
	public Invoice createInvoice(User user, ArticleContainer articleContainer) 
	throws InvoiceEditException 
	{
		ArrayList articles = new ArrayList();
		for (Iterator it = articleContainer.getArticles().iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
			if (article.getInvoice() == null)
				articles.add(article);
		}
		return createInvoice(user, articles);
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
	 * Books the given Invoice.
	 * 
	 * @param initiator The {@link User} who is responsible for booking.
	 * @param invoice The {@link Invoice} that should be booked.
	 * @param finalizeIfNecessary An invoice can only be booked, if finalized. Shall this method finalize,
	 *		if not yet done (otherwhise an exception is thrown).
	 * @param silentlyIgnoreBookedInvoice If the given invoice has already been booked, this method throws an
	 *		exception if this param is <tt>false</tt> or returns silently without doing anything if this
	 *		param is <tt>true</tt>.
	 */
	public void bookInvoice(User initiator, Invoice invoice, boolean finalizeIfNecessary, boolean silentlyIgnoreBookedInvoice)
	{
		InvoiceLocal invoiceLocal = invoice.getInvoiceLocal();
		if (invoiceLocal.isBooked()) {
			if (!silentlyIgnoreBookedInvoice)
				throw new IllegalStateException("Invoice \""+invoice.getPrimaryKey()+"\" has already been booked!");

			return;
		}

		if (!invoice.isFinalized()) {
			if (!finalizeIfNecessary)
				throw new IllegalStateException("Invoice \""+invoice.getPrimaryKey()+"\" is not finalized!");

			finalizeInvoice(initiator, invoice);
		}

		LegalEntity from = null;
		LegalEntity to = null;

		if (invoice.getPrice().getAmount() >= 0) {
			from = invoice.getCustomer();
			to = invoice.getVendor();
		}
		else {
			from = invoice.getVendor();
			to = invoice.getCustomer();
		}

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
				this,
				initiator,
				from,
				to,			
				invoice
			);

		HashMap involvedAnchors = new HashMap();
		ArrayList containers = new ArrayList(1);
		containers.add(bookMoneyTransfer);
		boolean failed = true;
		try {
			bookMoneyTransfer.bookTransfer(initiator, involvedAnchors);
	
			// check consistence
			Anchor.checkIntegrity(containers, involvedAnchors);

			failed = false;
		} finally  {
			if (failed)
				Anchor.resetIntegrity(containers, involvedAnchors);
		}

		invoiceLocal.setBooked(initiator);
	}

	public PaymentResult payDoWork(
			User user, PaymentData paymentData)
	throws PaymentException
	{
		if (user == null)
			throw new NullPointerException("user");

		if (paymentData == null)
			throw new NullPointerException("paymentData");

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
							getOrganisationID(),
							PaymentResult.CODE_FAILED,
							"paymentData.getPayment().getPayDoWorkServerResult() returned null! You probably forgot to set it in your ServerPaymentProcessor (\""+serverPaymentProcessor.getPrimaryKey()+"\")!",
							(Throwable)null));

		if (serverPaymentResult.isFailed())
			throw new PaymentException(serverPaymentResult);

		if (!serverPaymentResult.isPaid())
			throw new PaymentException(serverPaymentResult);

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
							getOrganisationID(),
							PaymentResult.CODE_FAILED,
							"paymentData.getPayment().getPayEndServerResult() returned null! You probably forgot to set it in your ServerPaymentProcessor (\""+serverPaymentProcessor.getPrimaryKey()+"\")!",
							(Throwable)null));

//		// I don't know why, but without the following line, it is not set in the datastore.
//		paymentData.getPayment().setPayBeginServerResult(serverPaymentResult);

		if (serverPaymentResult.isFailed())
			throw new PaymentException(serverPaymentResult);

		if (paymentData.getPayment().isPostponed()) {
			PayMoneyTransfer payMoneyTransfer = PayMoneyTransfer.getPayMoneyTransferForPayment(
					getPersistenceManager(), paymentData.getPayment());

			if (payMoneyTransfer != null) {
				LOGGER.warn("Your Payment \""+paymentData.getPayment()+"\" has first " +
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

		Map involvedAnchors = new HashMap();
		ArrayList containers = new ArrayList(1);
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

// TODO remove this test.
//		if (Math.random() < 10)
//			throw new PaymentException(
//					new PaymentResult(
//							getOrganisationID(), 
//							PaymentResult.CODE_FAILED,
//							"Test",
//							null));
// END test

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
			partner = finalizeInvoicesAndGetPartner(user,
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

		// call server-sided payment processor's first phase
		PayMoneyTransfer payMoneyTransfer = serverPaymentProcessor.payBegin(
				new PayParams(this, user, paymentData));

		PaymentResult serverPaymentResult;
		serverPaymentResult = paymentData.getPayment().getPayBeginServerResult();
		if (serverPaymentResult == null)
			throw new PaymentException(
					new PaymentResult(
							getOrganisationID(),
							PaymentResult.CODE_FAILED,
							"paymentData.getPayment().getPayBeginServerResult() returned null! You probably forgot to set it in your ServerPaymentProcessor (\""+serverPaymentProcessor.getPrimaryKey()+"\")!",
							(Throwable)null));

		if (serverPaymentResult.isFailed())
			throw new PaymentException(serverPaymentResult);

//		// I don't know why, but without the following line, it is not set in the datastore.
//		paymentData.getPayment().setPayBeginServerResult(serverPaymentResult);

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

			HashMap involvedAnchors = new HashMap();
			ArrayList containers = new ArrayList(1);
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
	 * @param invoices Can be null. Should be a <tt>Collection</tt> of {@link Invoice}
	 * @param currency Can be null.
	 * @return Either <tt>null</tt>, in case no Invoice was passed or the partner
	 *		(if at least one Invoice has been passed in <tt>invoices</tt>).
	 */
	protected LegalEntity finalizeInvoicesAndGetPartner(User user, Collection invoices, Currency currency)
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

			validateInvoice(invoice);
			finalizeInvoice(user, invoice);

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

	/**
	 * Finalizes an invoice and sends it to the involved 
	 * organisation if neccessary.
	 * 
	 * @param finalizer
	 * @param invoice
	 */
	public void finalizeInvoice(User finalizer, Invoice invoice) {
		if (invoice.isFinalized())
			return;

		if (!invoice.getVendor().getPrimaryKey().equals(getMandator().getPrimaryKey()))
			throw new IllegalArgumentException("Can not finalize an invoice where mandator is not vendor of this invoice!");

		// invoice.setFinalized(...) does nothing, if it is already finalized.
		invoice.setFinalized(finalizer);
		if (invoice.getCustomer() instanceof OrganisationLegalEntity) {
			// TODO: Put the Invoice in the queue on this organisations server ...
		}
	}

	/**
	 * Creates and persists a new Account for the mandator.
	 * 
	 * @param anchorID
	 * @param currency
	 * @return
	 */
	public Account createMandatorAccount(String anchorID, Currency currency, boolean createShadowAccount) {
		Account newAccount = null;
		if (createShadowAccount) {
			newAccount = new ShadowAccount(
					getMandator().getOrganisationID(),
					anchorID, 
					getMandator(), 
					currency
			);
		}
		else {
			newAccount = new Account(
					getMandator().getOrganisationID(),
					Account.ANCHOR_TYPE_ID_LOCAL_NORMAL,
					anchorID,
					getMandator(),
					currency
			);
		}
		getPersistenceManager().makePersistent(newAccount);
		return newAccount;
	}
	
	/**
	 * Finds (and creates if neccessary) the right Account for the given LegalEntity and Currency.
	 * 
	 * @param anchorTypeID See {@link Account} for static anchorTypeID definitions
	 * @param partner The legal entity the account should be searched for. 
	 * @param currency The currency the account should record.
	 * @return The found or created acccount. Never null.
	 */
	public Account getPartnerAccount(String anchorTypeID, LegalEntity partner, Currency currency) {
		if (partner == null)
			throw new IllegalArgumentException("Parameter partner must not be null!");
		if (currency == null)
			throw new IllegalArgumentException("Parameter currency must not be null!");
		
		String searchAccountID = partner.getAnchorID()+"-"+currency.getCurrencyID(); 
		AnchorID anchorID = AnchorID.create(this.getOrganisationID(), anchorTypeID, searchAccountID);
		
		Account account = null;
		Object o = null;
		try {
			o = getPersistenceManager().getObjectById(anchorID);
			account = (Account)o;
		} 
		catch (ClassCastException ce)  {
			IllegalStateException ill = new IllegalStateException("Found persistent object with oid "+anchorID+" but is not of type Account but "+o.getClass().getName());
			ill.initCause(ce);
			throw ill;
		}
		catch (JDOObjectNotFoundException je) {
			// account not existing, create it 
			account = new Account(this.getOrganisationID(), anchorTypeID, searchAccountID, partner, currency);
			getPersistenceManager().makePersistent(account);
			account.setOwner(partner);
		}
		
		if (account == null)
			throw new IllegalStateException("Account with oid "+anchorID+" could neither be found nor created!");
		
		if (!account.getOwner().getPrimaryKey().equals(partner.getPrimaryKey()))
			throw new IllegalStateException("A account for oid "+anchorID+" could be found, but its owner is not the partner the search was performed for. Owner: "+account.getOwner().getPrimaryKey()+", Partner: "+partner.getPrimaryKey());
		
		return account;
	}

	public void jdoPreStore()
	{
		if (_nextInvoiceID >= 0 && nextInvoiceID != _nextInvoiceID)
			nextInvoiceID = _nextInvoiceID;

		if (_nextMoneyFlowMappingID >= 0 && nextMoneyFlowMappingID != _nextMoneyFlowMappingID)
			nextMoneyFlowMappingID = _nextMoneyFlowMappingID;

		if (_nextTransferID >= 0 && nextTransferID != _nextTransferID)
			nextTransferID = _nextTransferID;

		if (_nextPriceConfigID >= 0 && nextPriceConfigID != _nextPriceConfigID)
			nextPriceConfigID = _nextPriceConfigID;

		if (_nextPriceCoordinateID >= 0 && nextPriceCoordinateID != _nextPriceCoordinateID)
			nextPriceCoordinateID = _nextPriceCoordinateID;
	}
}
