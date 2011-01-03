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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.jbpm.ActionHandlerFinalizeInvoice;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.PricedArticleContainer;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

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
 * @jdo.version strategy="version-number"
 *
 * @jdo.implements name="org.nightlabs.jfire.trade.ArticleContainer"
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.Statable"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, invoiceIDPrefix, invoiceID"
 *		add-interfaces="org.nightlabs.jfire.trade.id.ArticleContainerID"
 *		include-body="id/InvoiceID.body.inc"
 *
 * @jdo.query
 *		name="getInvoiceIDsByVendorAndCustomer"
 *		query="SELECT JDOHelper.getObjectId(this)
 *          WHERE
 *            JDOHelper.getObjectId(vendor) == :vendorID &&
 *            JDOHelper.getObjectId(customer) == :customerID
 *			ORDER BY invoiceID DESC"
 *
 * @jdo.query
 *		name="getInvoiceIDsByVendorAndEndCustomer"
 *		query="SELECT JDOHelper.getObjectId(this)
 *          WHERE
 *            JDOHelper.getObjectId(vendor) == :vendorID &&
 *            JDOHelper.getObjectId(endCustomer) == :customerID
 *			ORDER BY invoiceID DESC"
 *
 * @jdo.query
 *		name="getInvoiceIDsByVendorAndCustomerAndEndCustomer"
 *		query="SELECT JDOHelper.getObjectId(this)
 *          WHERE
 *            JDOHelper.getObjectId(vendor) == :vendorID &&
 *            JDOHelper.getObjectId(customer) == :customerID &&
 *            JDOHelper.getObjectId(endCustomer) == :endCustomerID
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
 * @jdo.fetch-group name="Invoice.discount" fields="discount"
 * @jdo.fetch-group name="Invoice.finalizeUser" fields="finalizeUser"
 * @jdo.fetch-group name="Invoice.price" fields="price"
 * @jdo.fetch-group name="Invoice.this" fetch-groups="default" fields="invoiceLocal, articles, createUser, currency, customer, discount, finalizeUser, price, vendor, state, states"
 *
 * @jdo.fetch-group name="ArticleContainer.customer" fields="customer"
 * @jdo.fetch-group name="ArticleContainer.vendor" fields="vendor"
 * @jdo.fetch-group name="ArticleContainer.endCustomer" fields="endCustomer"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleContainerInEditor" fields="invoiceLocal, createUser, currency, customer, endCustomer, discount, finalizeUser, price, vendor, state, states"
 *
 * @jdo.fetch-group name="Statable.state" fields="state"
 * @jdo.fetch-group name="Statable.states" fields="states"
 *
 */
@PersistenceCapable(
	objectIdClass=InvoiceID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Invoice")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
		name=Invoice.FETCH_GROUP_INVOICE_LOCAL,
		members=@Persistent(name="invoiceLocal")),
	@FetchGroup(
		name=Invoice.FETCH_GROUP_ARTICLES,
		members=@Persistent(name="articles")),
	@FetchGroup(
		name=Invoice.FETCH_GROUP_CREATE_USER,
		members=@Persistent(name="createUser")),
	@FetchGroup(
		name=Invoice.FETCH_GROUP_CURRENCY,
		members=@Persistent(name="currency")),
	@FetchGroup(
		name=Invoice.FETCH_GROUP_DISCOUNT,
		members=@Persistent(name="discount")),
	@FetchGroup(
		name=Invoice.FETCH_GROUP_FINALIZE_USER,
		members=@Persistent(name="finalizeUser")),
	@FetchGroup(
		name=Invoice.FETCH_GROUP_PRICE,
		members=@Persistent(name="price")),
	@FetchGroup(
		fetchGroups={"default"},
		name=Invoice.FETCH_GROUP_THIS_INVOICE,
		members={@Persistent(name="invoiceLocal"), @Persistent(name="articles"), @Persistent(name="createUser"), @Persistent(name="currency"), @Persistent(name="customer"), @Persistent(name="discount"), @Persistent(name="finalizeUser"), @Persistent(name="price"), @Persistent(name="vendor"), @Persistent(name="state"), @Persistent(name="states")}),
	@FetchGroup(
		name=ArticleContainer.FETCH_GROUP_CUSTOMER,
		members=@Persistent(name="customer")),
	@FetchGroup(
		name=ArticleContainer.FETCH_GROUP_VENDOR,
		members=@Persistent(name="vendor")),
//	@FetchGroup(
//		name="ArticleContainer.endCustomer",
//		members=@Persistent(name="endCustomer")),
	@FetchGroup(
		name="FetchGroupsTrade.articleContainerInEditor",
		members={@Persistent(name="invoiceLocal"), @Persistent(name="createUser"), @Persistent(name="currency"), @Persistent(name="customer"), @Persistent(name="discount"), @Persistent(name="finalizeUser"), @Persistent(name="price"), @Persistent(name="vendor"), @Persistent(name="state"), @Persistent(name="states")}),
	@FetchGroup(
		name="Statable.state",
		members=@Persistent(name="state")),
	@FetchGroup(
		name="Statable.states",
		members=@Persistent(name="states")),
	@FetchGroup(
		name=ArticleContainer.FETCH_GROUP_PROPERTY_SET,
		members=@Persistent(name="propertySet")),
	@FetchGroup(
		name=ArticleContainer.FETCH_GROUP_ARTICLES,
		members=@Persistent(name="articles"))
})
@Queries({
	@javax.jdo.annotations.Query(
			name="getInvoiceIDsByVendorAndCustomer",
			value="SELECT JDOHelper.getObjectId(this) " +
					"WHERE JDOHelper.getObjectId(vendor) == :vendorID && " +
					"JDOHelper.getObjectId(customer) == :customerID " +
					"ORDER BY createDT DESC"
	),
	@javax.jdo.annotations.Query(
			name="getInvoiceIDsByVendorAndEndCustomer",
			value="SELECT JDOHelper.getObjectId(this) " +
					"WHERE JDOHelper.getObjectId(vendor) == :vendorID && " +
					"this.articles.contains(article) && " +
					"JDOHelper.getObjectId(article.endCustomer) == :customerID " +
					"VARIABLES org.nightlabs.jfire.trade.Article article " +
					"ORDER BY createDT DESC"
	),
//	@javax.jdo.annotations.Query(
//			name="getInvoiceIDsByVendorAndCustomerAndEndCustomer",
//			value="SELECT JDOHelper.getObjectId(this) WHERE JDOHelper.getObjectId(vendor) == :vendorID && JDOHelper.getObjectId(customer) == :customerID && JDOHelper.getObjectId(endCustomer) == :endCustomerID ORDER BY invoiceID DESC"
//	),
	@javax.jdo.annotations.Query(
			name="getNonFinalizedInvoicesByVendorAndCustomer",
			value="SELECT WHERE vendor.organisationID == paramVendorID_organisationID && vendor.anchorID == paramVendorID_anchorID && customer.organisationID == paramCustomerID_organisationID && customer.anchorID == paramCustomerID_anchorID && finalizeDT == null && invoiceLocal.processEnded == false PARAMETERS String paramVendorID_organisationID, String paramVendorID_anchorID, String paramCustomerID_organisationID, String paramCustomerID_anchorID import java.lang.String ORDER BY invoiceID DESC"
	),
	@javax.jdo.annotations.Query(
			name="getOverdueInvoices",
			value="SELECT WHERE this.organisationID == paramOrganisationID && dueDateForPayment < paramDate && finalizeDT != null PARAMETERS String paramOrganisationID, Date paramDate import java.lang.String, java.util.Date ORDER BY invoiceID DESC"
	),
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Invoice
implements Serializable, PayableObject, PricedArticleContainer, Statable, DetachCallback
{
	private static final long serialVersionUID = 1L;
	public static final String FETCH_GROUP_INVOICE_LOCAL = "Invoice.invoiceLocal";
	public static final String FETCH_GROUP_ARTICLES = "Invoice.articles";
	public static final String FETCH_GROUP_CREATE_USER = "Invoice.createUser";
	public static final String FETCH_GROUP_CURRENCY = "Invoice.currency";
	public static final String FETCH_GROUP_DISCOUNT = "Invoice.discount";
	public static final String FETCH_GROUP_FINALIZE_USER = "Invoice.finalizeUser";
	public static final String FETCH_GROUP_PRICE = "Invoice.price";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_INVOICE = "Invoice.this";

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
	public static List<InvoiceID> getInvoiceIDs(PersistenceManager pm, AnchorID vendorID, AnchorID customerID, AnchorID endCustomerID, long rangeBeginIdx, long rangeEndIdx)
	{
		if (customerID != null && endCustomerID != null) {
			throw new UnsupportedOperationException("NYI");
//			Query query = pm.newNamedQuery(Invoice.class, "getInvoiceIDsByVendorAndCustomerAndEndCustomer");
//			Map<String, Object> params = new HashMap<String, Object>();
//			params.put("vendorID", vendorID);
//			params.put("customerID", customerID);
//			params.put("endCustomerID", endCustomerID);
//
//			if (rangeBeginIdx >= 0 && rangeEndIdx >= 0)
//				query.setRange(rangeBeginIdx, rangeEndIdx);
//
//			return CollectionUtil.castList((List<?>) query.executeWithMap(params));
		}

		Query query = pm.newNamedQuery(Invoice.class, endCustomerID == null ? "getInvoiceIDsByVendorAndCustomer" : "getInvoiceIDsByVendorAndEndCustomer");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("vendorID", vendorID);
		params.put("customerID", endCustomerID != null ? endCustomerID : customerID);

		if (rangeBeginIdx >= 0 && rangeEndIdx >= 0)
			query.setRange(rangeBeginIdx, rangeEndIdx);

		return CollectionUtil.castList((List<?>) query.executeWithMap(params));
	}

	public static List<Invoice> getNonFinalizedInvoices(PersistenceManager pm, AnchorID vendorID, AnchorID customerID)
	{
		Query query = pm.newNamedQuery(Invoice.class, "getNonFinalizedInvoicesByVendorAndCustomer");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);

		return CollectionUtil.castList((List<?>) query.executeWithMap(params));
	}
	
	public static List<Invoice> getOverdueInvoices(PersistenceManager pm, String organisationID, Date date)
	{
		Query query = pm.newNamedQuery(Invoice.class, "getOverdueInvoices");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramOrganisationID", organisationID);
		params.put("paramDate", date);

		return CollectionUtil.castList((List<?>) query.executeWithMap(params));
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String invoiceIDPrefix;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long invoiceID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Invoice() {}

	public Invoice(
			User creator, LegalEntity vendor, LegalEntity customer,
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

//		Accounting accounting = Accounting.getAccounting(pm);
//		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();

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
		this.price = new Price(IDGenerator.getOrganisationID(), IDGenerator.nextID(Price.class), currency);
//				accountingPriceConfig.getOrganisationID(), accountingPriceConfig.getPriceConfigID(),
//				accountingPriceConfig.createPriceID(), currency);

		this.termOfPaymentMSec = TimeUnit.DAYS.toMillis(31); //31 days
		
		articles = new HashSet<Article>();

		String structScope = Struct.DEFAULT_SCOPE;
		String structLocalScope = StructLocal.DEFAULT_SCOPE;
		this.propertySet = new PropertySet(
				organisationID, IDGenerator.nextID(PropertySet.class),
				Organisation.DEV_ORGANISATION_ID,
				Invoice.class.getName(), structScope, structLocalScope);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="invoice" dependent="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="invoice",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private InvoiceLocal invoiceLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity vendor;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity customer;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	private LegalEntity endCustomer;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID vendorID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean vendorID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID customerID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean customerID_detached = false;

//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	@Persistent(persistenceModifier=PersistenceModifier.NONE)
//	private AnchorID endCustomerID = null;
//
//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	@Persistent(persistenceModifier=PersistenceModifier.NONE)
//	private boolean endCustomerID_detached = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_Invoice_states",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<State> states;

//	@Persistent(
//			mappedBy="invoice",
//			persistenceModifier=PersistenceModifier.PERSISTENT
//	)
	@Persistent(
			persistenceModifier=PersistenceModifier.PERSISTENT,
			table="JFireTrade_Invoice_articles"
	)
	@Join
	private Set<Article> articles;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int articleCount = 0;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Discount discount;

	/**
	 * Creation date of this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createDT;

	/**
	 * The user who created this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User createUser = null;

	/**
	 * This member represents the sum of all prices of all invoice items.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price price;

	/**
	 * An Invoice is only valid after {@link #validate()} has been called.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean valid = false;

	/**
	 * This member stores the user who finilized this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User finalizeUser = null;

	/**
	 * This member stores when this <tt>Invoice</tt> was finalized.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date finalizeDT  = null;

	/**
	 * Represents the currency of all offerItems within this invoice.
	 * An Invoice can only contain offerItems with the same currency.
	 */
	private Currency currency;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PropertySet propertySet;

	/**
	 * The time (in milliseconds) within which the payment is due. 
	 * It is counted from the timestamp of the invoice finalization 
	 * (Invoice.finalizeDT). Default value is 31 days, but this default 
	 * needs to be overridden by the JFireDunning module, if it is installed.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long termOfPaymentMSec;
	
	/**
	 * The date when the invoice should be paid. It is set to 
	 * finalizeDT +  termOfPaymentMSec when the invoice is finalized. 
	 * It cannot be changed after the finalization, hence the only way 
	 * to modify this value is to adjust the termOfPaymentMSec before 
	 * finalizing the invoice.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date dueDateForPayment;
	
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

		if (!article.isAllocated())
			throw new IllegalArgumentException("Article is not allocated! " + article);

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

		this.articleCount = articles.size();
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

		this.articleCount = articles.size();
	}

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

		for (Article article : articles) {
			price.sumPrice(article.getPrice());
		}
	}

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

	@Override
	public Date getCreateDT() {
		return createDT;
	}

	@Override
	public User getCreateUser() {
		return createUser;
	}

	@Override
	public LegalEntity getCustomer() {
		return customer;
	}

//	@Override
//	public LegalEntity getEndCustomer() {
//		return endCustomer;
//	}

	@Override
	public AnchorID getVendorID()
	{
		if (vendorID == null && !vendorID_detached)
			vendorID = (AnchorID) JDOHelper.getObjectId(vendor);

		return vendorID;
	}

	@Override
	public AnchorID getCustomerID()
	{
		if (customerID == null && !customerID_detached)
			customerID = (AnchorID) JDOHelper.getObjectId(customer);

		return customerID;
	}

//	@Override
//	public AnchorID getEndCustomerID()
//	{
//		if (endCustomerID == null && !endCustomerID_detached)
//			endCustomerID = (AnchorID) JDOHelper.getObjectId(endCustomer);
//
//		return endCustomerID;
//	}

	public String getInvoiceIDPrefix()
	{
		return invoiceIDPrefix;
	}

	@Override
	public String getArticleContainerIDPrefix()
	{
		return getInvoiceIDPrefix();
	}
	public long getInvoiceID() {
		return invoiceID;
	}

	@Override
	public long getArticleContainerID()
	{
		return getInvoiceID();
	}

	public String getInvoiceIDAsString() {
		return ObjectIDUtil.longObjectIDFieldToString(invoiceID);
	}

	@Override
	public String getArticleContainerIDAsString()
	{
		return getInvoiceIDAsString();
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
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

	public LegalEntity getVendor() {
		return vendor;
	}

//	protected void setCurrency(Currency currency) {
//		this.currency = currency;
//	}

	public Currency getCurrency() {
		return currency;
	}

	public Date getDueDateForPayment() {
		return dueDateForPayment;
	}
	
	public long getTermOfPaymentMSec() {
		return termOfPaymentMSec;
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
		this.dueDateForPayment = new Date(finalizeDT.getTime() + termOfPaymentMSec);
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
		Collection<String> fetchGroups = CollectionUtil.castSet(attached.getPersistenceManager().getFetchPlan().getGroups());

		if (fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
			detached.vendorID = attached.getVendorID();
			detached.vendorID_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
			detached.customerID = attached.getCustomerID();
			detached.customerID_detached = true;
		}

//		if (fetchGroups.contains(FETCH_GROUP_END_CUSTOMER_ID)) {
//			detached.endCustomerID = attached.getEndCustomerID();
//			detached.endCustomerID_detached = true;
//		}
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
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.invoiceIDPrefix, o.invoiceIDPrefix) &&
				this.invoiceID == o.invoiceID;
	}

	@Override
	public int hashCode()
	{
		return
				Util.hashCode(this.organisationID) ^
				Util.hashCode(this.invoiceIDPrefix) ^
				Util.hashCode(this.invoiceID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + invoiceIDPrefix + ',' + ObjectIDUtil.longObjectIDFieldToString(invoiceID) + ']';
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

		this.state = currentState;
		this.states.add(currentState);
	}

	public State getState()
	{
		return state;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient List<State> _states = null;

	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.ArticleContainer#getArticleCount()
	 */
	public int getArticleCount() {
		return articleCount;
	}

	@Override
	public PropertySet getPropertySet() {
		return propertySet;
	}


	protected void modifyCreateDT(Date createDT) {
		if (createDT == null)
			throw new IllegalArgumentException("createDT must not be null!");

		this.createDT = createDT;
	}

	protected void modifyCreateUser(User createUser) {
		if (createUser == null)
			throw new IllegalArgumentException("createUser must not be null!");

		this.createUser = createUser;
	}

	protected void modifyFinalizeDT(Date finalizeDT) {
		if (finalizeDT == null)
			throw new IllegalArgumentException("finalizeDT must not be null!");

		if (this.finalizeDT == null)
			throw new IllegalStateException("This invoice is not finalized! You must finalize it before modifying the finalizeDT!");

		this.finalizeDT = finalizeDT;
	}

	protected void modifyFinalizeUser(User finalizeUser) {
		if (finalizeUser == null)
			throw new IllegalArgumentException("finalizeUser must not be null!");

		if (this.finalizeUser == null)
			throw new IllegalStateException("This invoice is not finalized! You must finalize it before modifying the finalizeDT!");

		this.finalizeUser = finalizeUser;
	}
}
