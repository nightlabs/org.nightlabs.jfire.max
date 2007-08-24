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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.jbpm.ActionHandlerFinalizeInvoice;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Utils;

/**
 * @author Alexander Bieber <!-- alex at nightlabs dot de -->
 * @author Marco Schulze - Marco at NightLabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.InvoiceID"
 *		detachable="true"
 *		table="JFireTrade_Invoice"
 *
 * @jdo.implements name="org.nightlabs.jfire.trade.ArticleContainer"
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.Statable"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, invoiceIDPrefix, invoiceID"
 *		add-interfaces="org.nightlabs.jfire.trade.id.ArticleContainerID"
 *
 * @jdo.query
 *		name="getInvoiceIDsByVendorAndCustomer"
 *		query="SELECT JDOHelper.getObjectId(this)
 *			WHERE vendor.organisationID == paramVendorID_organisationID &&
 *            vendor.anchorID == paramVendorID_anchorID &&
 *			      customer.organisationID == paramCustomerID_organisationID &&
 *            customer.anchorID == paramCustomerID_anchorID
 *			PARAMETERS String paramVendorID_organisationID, String paramVendorID_anchorID,
 *                 String paramCustomerID_organisationID, String paramCustomerID_anchorID
 *			import java.lang.String
 *			ORDER BY invoiceID DESC"
 *
 * @jdo.query
 *		name="getNonFinalizedInvoicesByVendorAndCustomer"
 *		query="SELECT
 *			WHERE vendor.organisationID == paramVendorID_organisationID &&
 *            vendor.anchorID == paramVendorID_anchorID &&
 *			      customer.organisationID == paramCustomerID_organisationID &&
 *            customer.anchorID == paramCustomerID_anchorID &&
 *            finalizeDT == null
 *			PARAMETERS String paramVendorID_organisationID, String paramVendorID_anchorID,
 *                 String paramCustomerID_organisationID, String paramCustomerID_anchorID
 *			import java.lang.String
 *			ORDER BY invoiceID DESC"
 *
 * @jdo.fetch-group name="Invoice.invoiceLocal" fields="invoiceLocal"
 * @jdo.fetch-group name="Invoice.articles" fields="articles"
 * @jdo.fetch-group name="Invoice.createUser" fields="createUser"
 * @jdo.fetch-group name="Invoice.currency" fields="currency"
 * @jdo.fetch-group name="Invoice.customer" fields="customer"
 * @jdo.fetch-group name="Invoice.discount" fields="discount"
 * @jdo.fetch-group name="Invoice.finalizeUser" fields="finalizeUser"
 * @jdo.fetch-group name="Invoice.price" fields="price"
 * @jdo.fetch-group name="Invoice.vendor" fields="vendor"
 * @jdo.fetch-group name="Invoice.this" fetch-groups="default" fields="invoiceLocal, articles, createUser, currency, customer, discount, finalizeUser, price, vendor, state, states"
 *
 * @jdo.fetch-group name="Statable.state" fields="state"
 * @jdo.fetch-group name="Statable.states" fields="states"
 *
 **/
public class Invoice
implements Serializable, ArticleContainer, Statable, DetachCallback
{
	public static final String FETCH_GROUP_INVOICE_LOCAL = "Invoice.invoiceLocal";
	public static final String FETCH_GROUP_ARTICLES = "Invoice.articles";
	public static final String FETCH_GROUP_CREATE_USER = "Invoice.createUser";
	public static final String FETCH_GROUP_CURRENCY = "Invoice.currency";
	public static final String FETCH_GROUP_CUSTOMER = "Invoice.customer";
	public static final String FETCH_GROUP_DISCOUNT = "Invoice.discount";
	public static final String FETCH_GROUP_FINALIZE_USER = "Invoice.finalizeUser";
	public static final String FETCH_GROUP_PRICE = "Invoice.price";
	public static final String FETCH_GROUP_VENDOR = "Invoice.vendor";
	public static final String FETCH_GROUP_THIS_INVOICE = "Invoice.this";

	// the following fetch-groups are virtual and processed in the detach callback
	public static final String FETCH_GROUP_VENDOR_ID = "Invoice.vendorID";
	public static final String FETCH_GROUP_CUSTOMER_ID = "Invoice.customerID";

	/**
	 * This method queries all <code>Invoice</code>s which exist between the given vendor and customer.
	 * They are ordered by orderID descending (means newest first).
	 *
	 * @param pm The PersistenceManager to be used for accessing the datastore.
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link Invoice}.
	 */
	@SuppressWarnings("unchecked")
	public static List<InvoiceID> getInvoiceIDs(PersistenceManager pm, AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	{
		Query query = pm.newNamedQuery(Invoice.class, "getInvoiceIDsByVendorAndCustomer");
		Map params = new HashMap();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);

		if (rangeBeginIdx >= 0 && rangeEndIdx >= 0)
			query.setRange(rangeBeginIdx, rangeEndIdx);

		return (List<InvoiceID>) query.executeWithMap(params);
	}

	public static List getNonFinalizedInvoices(PersistenceManager pm, AnchorID vendorID, AnchorID customerID)
	{
		Query query = pm.newNamedQuery(Invoice.class, "getNonFinalizedInvoicesByVendorAndCustomer");
		Map params = new HashMap();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);

		return (List) query.executeWithMap(params);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String invoiceIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long invoiceID;

	/**
	 * @deprecated Only for JDO!
	 */
	protected Invoice() {}

	public Invoice(
			User creator, OrganisationLegalEntity vendor, LegalEntity customer,
			String invoiceIDPrefix, long _invoiceID, Currency currency)
	{
		if (creator == null)
			throw new IllegalArgumentException("creator must not be null!");

		if (vendor == null)
			throw new IllegalArgumentException("vendor must not be null!");

		if (customer == null)
			throw new IllegalArgumentException("customer must not be null!");

		ObjectIDUtil.assertValidIDString(invoiceIDPrefix, "invoiceIDPrefix");

		if (_invoiceID < 0)
			throw new IllegalArgumentException("invoiceID < 0");

		if (currency == null)
			throw new NullPointerException("currency");
		
		PersistenceManager pm = JDOHelper.getPersistenceManager(vendor);
		if (pm == null)
			throw new IllegalStateException("vendor is not persistent! Could not get a PersistenceManager from it!");

		Accounting accounting = Accounting.getAccounting(pm);
		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();

		this.organisationID = vendor.getOrganisationID();
		this.invoiceIDPrefix = invoiceIDPrefix;
		this.invoiceID = _invoiceID;
		this.createDT = new Date(System.currentTimeMillis());
		this.createUser = creator;
		this.vendor = vendor;
		this.customer = customer;
		this.currency = currency;
		this.primaryKey = getPrimaryKey(this.organisationID, this.invoiceIDPrefix, this.invoiceID);
//		this.desiredModeOfPaymentFlavour = desiredModeOfPaymentFlavour;
		this.price = new Price(
				accountingPriceConfig.getOrganisationID(), accountingPriceConfig.getPriceConfigID(),
				accountingPriceConfig.createPriceID(), currency);

		articles = new HashSet<Article>();
	}

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="invoice"	 
	 */
	private InvoiceLocal invoiceLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private OrganisationLegalEntity vendor;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private LegalEntity customer;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID vendorID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean vendorID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID customerID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean customerID_detached = false;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private State state;

	/**
	 * This is the history of <b>public</b> {@link State}s with the newest last and the oldest first.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		table="JFireTrade_Invoice_states"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private List<State> states;

//	/**
//	 * key: String articlePK (organisationID/articleID)<br/>
//	 * value: Article article
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="org.nightlabs.jfire.trade.Article"
//	 *		mapped-by="invoice"
//	 *
//	 * @jdo.key mapped-by="primaryKey"
//	 *
//	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
//	 */
//	private Map articles = new HashMap();

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.trade.Article"
	 *		mapped-by="invoice"
	 */
	private Set<Article> articles;

	/**
	 * @return the same as {@link #getStatableLocal()}
	 */
	public InvoiceLocal getInvoiceLocal()
	{
		return invoiceLocal;
	}
	public StatableLocal getStatableLocal()
	{
		return invoiceLocal;
	}
	protected void setInvoiceLocal(InvoiceLocal invoiceLocal)
	{
		this.invoiceLocal = invoiceLocal;
	}

	/**
	 * Adds an Article, if this Invoice is not yet finalized, 
	 * the article's offer has the same vendor and customer as this Invoice, 
	 * and the article is not yet part of another invoice.
	 * <p>
	 * NEVER use this method directly (within the server)! Call {@link Accounting#addArticlesToInvoice(User, Invoice, Collection)}
	 * instead!
	 * </p>
	 *
	 * @param article
	 */
	public void addArticle(Article article)
	throws InvoiceEditException
	{
		Offer articleOffer = article.getOffer();
		Order articleOrder = articleOffer.getOrder();
		ArticleID articleID = (ArticleID) JDOHelper.getObjectId(article);
		if (isFinalized())			
			throw new InvoiceEditException(
					InvoiceEditException.REASON_INVOICE_FINALIZED, 
					"Invoice is finalized, can not change any more!", 
					articleID
				);

		if (!vendor.getPrimaryKey().equals(articleOrder.getVendor().getPrimaryKey()) 
					|| 
				!customer.getPrimaryKey().equals(articleOrder.getCustomer().getPrimaryKey()) 
				)
		{
			throw new InvoiceEditException(
				InvoiceEditException.REASON_ANCHORS_DONT_MATCH,				
				"Vendor and customer are not equal for the Article to add and the invoice, can not add the offerItem!!"
			);
		}

		InvoiceID invoiceID = article.getInvoiceID();
		if (invoiceID != null) {
			throw new InvoiceEditException(
				InvoiceEditException.REASON_ARTICLE_ALREADY_IN_INVOICE,
				"Article already in an invoice. Article "+articleID+", Invoice "+invoiceID, 
				articleID, 
				invoiceID
			);
		}

		if (!article.getOffer().getOfferLocal().isAccepted()) {
			throw new InvoiceEditException(
				InvoiceEditException.REASON_OFFER_NOT_ACCEPTED,
				"At least one involved offer is not confirmed!",
				articleID
			);
		}

		if (!getCurrency().getCurrencyID().equals(article.getPrice().getCurrency().getCurrencyID()))
			throw new InvoiceEditException(
				InvoiceEditException.REASON_MULTIPLE_CURRENCIES,
				"Cannot add an Article with a different currency ("+article.getPrice().getCurrency().getCurrencyID()+") than this Invoice's one ("+getCurrency().getCurrencyID()+")!"
			);

		if (article.isReversed())
			throw new InvoiceEditException(
					InvoiceEditException.REASON_REVERSED_ARTICLE,
					"A reversed Article cannot be added to an Invoice!",
					articleID);

		if (article.isReversing() && article.getReversedArticle().getInvoiceID() == null)
			throw new InvoiceEditException(
					InvoiceEditException.REASON_REVERSING_ARTICLE,
					"The reversed Article is not in an Invoice! Cannot add the reversing Article to one in this case!",
					articleID);

		articles.add(article);

		this.valid = false;	
		article.setInvoice(this);		
	}

	public void removeArticle(Article article)
	throws InvoiceEditException
	{
		if (isFinalized())
			throw new InvoiceEditException(InvoiceEditException.REASON_INVOICE_FINALIZED, "Invoice is finalized, can not change any more!");

		if (articles.contains(article)) {
			articles.remove(article);
			this.valid = false;
			article.setInvoice(null);
		}
	}

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private Discount discount;
	
	/**
	 * Creation date of this Invoice.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createDT;
	
	/**
	 * The user who created this Invoice.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User createUser = null;

	/**
	 * This member represents the sum of all prices of all invoice items.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price price;

	/**
	 * An Invoice is only valid after {@link #validate()} has been called.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean valid = false;

	/**
	 * This method calculates the price and does some checks. Before the <tt>Invoice</tt>
	 * can be finalized, it must be validated. If it is already valid, it returns without any action.
	 * <p>
	 * Do NOT call this method directly! Use {@link Accounting#validateInvoice(Invoice)} instead!
	 */
	protected void validate()
	{
		if (this.valid)
			return;

		calculatePrice();
		this.valid = true;
	}

	/**
	 * This method is called by {@link #validate()}.
	 */
	protected void calculatePrice() {
		price.clearFragments();
		price.setAmount(0);

		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article)it.next();
			price.sumPrice(article.getPrice());
		}
	}

	/**
	 * This member stores the user who finilized this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User finalizeUser = null;

	/**
	 * This member stores when this <tt>Invoice</tt> was finalized.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date finalizeDT  = null;
	
	/**
	 * Represents the currency of all offerItems within this invoice.
	 * An Invoice can only contain offerItems with the same currency.
	 */
	private Currency currency;

	public static String getPrimaryKey(String organisationID, String invoiceIDPrefix, long invoiceID)
	{
		return organisationID + '/' + invoiceIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(invoiceID);
	}

	public String getPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * @param transfer This is a local transfer either from the partner's
	 *		<tt>Account</tt> to the <tt>LegalEntity partner</tt> or from the
	 *		<tt>LegalEntity</tt> to its <tt>Account</tt>.
	 * @param rollback If the payment has failed, this method is called again to
	 *		readjust the amountPaid (rollback the InvoiceMoneyTransfer)
	 */
	public void bookInvoiceMoneyTransfer(InvoiceMoneyTransfer transfer, boolean rollback)
	{
		if (!InvoiceMoneyTransfer.BOOK_TYPE_PAY.equals(transfer.getBookType()))
			return;

		boolean vendorIsFrom = transfer.getAnchorType(vendor) == Transfer.ANCHORTYPE_FROM;
		boolean vendorIsTo = transfer.getAnchorType(vendor) == Transfer.ANCHORTYPE_TO;
		boolean customerIsFrom = transfer.getAnchorType(customer) == Transfer.ANCHORTYPE_FROM;
		boolean customerIsTo = transfer.getAnchorType(customer) == Transfer.ANCHORTYPE_TO;

		if (!vendorIsFrom && !vendorIsTo && !customerIsFrom && !customerIsTo)
			throw new IllegalArgumentException("Transfer \""+transfer.getPrimaryKey()+"\" && Invoice \""+this.getPrimaryKey()+"\": Transfer and invoice are not related!");

		long amount;
		if (vendorIsTo || customerIsFrom)
			amount = transfer.getAmount();
		else
			amount = - transfer.getAmount();

		if (rollback)
			amount *= -1;

		InvoiceLocal invoiceLocal = getInvoiceLocal();
		invoiceLocal.incAmountPaid(amount);

		if (invoiceLocal.getAmountToPay() == 0) {
			invoiceLocal.setOutstanding(false);
		}
		else {
			invoiceLocal.setOutstanding(true);
		}
	}

	public Date getCreateDT() {
		return createDT;
	}
	public User getCreateUser() {
		return createUser;
	}
	public LegalEntity getCustomer() {
		return customer;
	}

	public AnchorID getVendorID()
	{
		if (vendorID == null && !vendorID_detached)
			vendorID = (AnchorID) JDOHelper.getObjectId(vendor);

		return vendorID;
	}

	public AnchorID getCustomerID()
	{
		if (customerID == null && !customerID_detached)
			customerID = (AnchorID) JDOHelper.getObjectId(customer);

		return customerID;
	}

	public String getInvoiceIDPrefix()
	{
		return invoiceIDPrefix;
	}
	public String getArticleContainerIDPrefix()
	{
		return getInvoiceIDPrefix();
	}
	public long getInvoiceID() {
		return invoiceID;
	}
	public long getArticleContainerID()
	{
		return getInvoiceID();
	}
	public String getInvoiceIDAsString() {
		return ObjectIDUtil.longObjectIDFieldToString(invoiceID);
	}
	public String getArticleContainerIDAsString()
	{
		return getInvoiceIDAsString();
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<Article> _articles = null;

	public Set<Article> getArticles()
	{
		if (_articles == null)
			_articles = Collections.unmodifiableSet(articles);

		return _articles;
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public Price getPrice() {
		return price;
	}
	public OrganisationLegalEntity getVendor() {
		return vendor;
	}
//	protected void setCurrency(Currency currency) {
//		this.currency = currency;
//	}
	public Currency getCurrency() {
		return currency;
	}
	/**
	 * @return Returns the valid.
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * This method must not be called directly! it is triggered via {@link ActionHandlerFinalizeInvoice}.
	 */
	public void setFinalized(User finalizer) {
		if (isFinalized())
			return;

		if (!isValid())
			throw new IllegalStateException("This Invoice is not valid! Call validate() before!");
		this.finalizeUser = finalizer;
		this.finalizeDT = new Date(System.currentTimeMillis());
	}
	/**
	 * This member is set to true as soon as all desired 
	 * {@link Article}s were added to this offer. A finalized 
	 * Invoice can not be altered any more.
	 */
	public boolean isFinalized() {
		return finalizeDT != null;
	}
	public User getFinalizeUser() {
		return finalizeUser;		
	}
	public Date getFinalizeDT() {
		return finalizeDT;
	}
	public Discount getDiscount() {
		return discount;
	}
	public void setDiscount(Discount discount) {
		this.discount = discount;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Invoice is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	public void jdoPreDetach()
	{
	}
	public void jdoPostDetach(Object _attached)
	{
		Invoice attached = (Invoice)_attached;
		Invoice detached = this;
		Collection fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();

		if (fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
			detached.vendorID = attached.getVendorID();
			detached.vendorID_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
			detached.customerID = attached.getCustomerID();
			detached.customerID_detached = true;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof Invoice))
			return false;

		Invoice o = (Invoice) obj;

		return
				Utils.equals(this.organisationID, o.organisationID) && 
				Utils.equals(this.invoiceIDPrefix, o.invoiceIDPrefix) &&
				this.invoiceID == o.invoiceID;
	}

	@Override
	public int hashCode()
	{
		return
				Utils.hashCode(this.organisationID) ^ 
				Utils.hashCode(this.invoiceIDPrefix) ^
				Utils.hashCode(this.invoiceID);
	}

	/**
	 * This method is <b>not</b> intended to be called directly. It is called by
	 * {@link State#State(String, long, User, Statable, org.nightlabs.jfire.jbpm.graph.def.StateDefinition)}
	 * which is called automatically by {@link ActionHandlerNodeEnter}, if this <code>ActionHandler</code> is registered.
	 */
	public void setState(State currentState)
	{
		if (currentState == null)
			throw new IllegalArgumentException("state must not be null!");

		if (!currentState.getStateDefinition().isPublicState())
			throw new IllegalArgumentException("state.stateDefinition.publicState is false!");

		this.state = (State) currentState;
		this.states.add((State) currentState);
	}

	public State getState()
	{
		return state;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient List<State> _states = null;

	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}
}
