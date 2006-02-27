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

package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.DeliveryNoteID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNote"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryNoteID"
 *		add-interfaces="org.nightlabs.jfire.trade.id.ArticleContainerID"
 *
 * @jdo.query
 *		name="getDeliveryNotesByVendorAndCustomer"
 *		query="SELECT
 *			WHERE vendor.organisationID == paramVendorID_organisationID &&
 *            vendor.anchorID == paramVendorID_anchorID &&
 *			      customer.organisationID == paramCustomerID_organisationID &&
 *            customer.anchorID == paramCustomerID_anchorID
 *			PARAMETERS String paramVendorID_organisationID, String paramVendorID_anchorID,
 *                 String paramCustomerID_organisationID, String paramCustomerID_anchorID
 *			import java.lang.String
 *			ORDER BY deliveryNoteID DESC"
 *
 * @jdo.query
 *		name="getNonFinalizedDeliveryNotesByVendorAndCustomer"
 *		query="SELECT
 *			WHERE vendor.organisationID == paramVendorID_organisationID &&
 *            vendor.anchorID == paramVendorID_anchorID &&
 *			      customer.organisationID == paramCustomerID_organisationID &&
 *            customer.anchorID == paramCustomerID_anchorID &&
 *            finalizeDT == null
 *			PARAMETERS String paramVendorID_organisationID, String paramVendorID_anchorID,
 *                 String paramCustomerID_organisationID, String paramCustomerID_anchorID
 *			import java.lang.String
 *			ORDER BY deliveryNoteID DESC"
 *
 * @jdo.fetch-group name="DeliveryNote.deliveryNoteLocal" fields="deliveryNoteLocal"
 * @jdo.fetch-group name="DeliveryNote.articles" fields="articles"
 * @jdo.fetch-group name="DeliveryNote.createUser" fields="createUser"
 * @jdo.fetch-group name="DeliveryNote.customer" fields="customer"
 * @jdo.fetch-group name="DeliveryNote.finalizeUser" fields="finalizeUser"
 * @jdo.fetch-group name="DeliveryNote.vendor" fields="vendor"
 * @jdo.fetch-group name="DeliveryNote.this" fetch-groups="default" fields="deliveryNoteLocal, articles, createUser, customer, finalizeUser, vendor"
 **/
public class DeliveryNote
implements Serializable, ArticleContainer, DetachCallback
{
	public static final String FETCH_GROUP_DELIVERY_NOTE_LOCAL = "DeliveryNote.deliveryNoteLocal";
	public static final String FETCH_GROUP_ARTICLES = "DeliveryNote.articles";
	public static final String FETCH_GROUP_CREATE_USER = "DeliveryNote.createUser";
	public static final String FETCH_GROUP_CUSTOMER = "DeliveryNote.customer";
	public static final String FETCH_GROUP_FINALIZE_USER = "DeliveryNote.finalizeUser";
	public static final String FETCH_GROUP_VENDOR = "DeliveryNote.vendor";
	public static final String FETCH_GROUP_THIS_DELIVERY_NOTE = "DeliveryNote.this";

	// the following fetch-groups are virtual and processed in the detach callback
	public static final String FETCH_GROUP_VENDOR_ID = "DeliveryNote.vendorID";
	public static final String FETCH_GROUP_CUSTOMER_ID = "DeliveryNote.customerID";

	/**
	 * This method queries all <code>DeliveryNote</code>s which exist between the given vendor and customer.
	 * They are ordered by orderID descending (means newest first).
	 *
	 * @param pm The PersistenceManager to be used for accessing the datastore.
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link DeliveryNote}.
	 */
	public static List getDeliveryNotes(PersistenceManager pm, AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	{
		Query query = pm.newNamedQuery(DeliveryNote.class, "getDeliveryNotesByVendorAndCustomer");
		Map params = new HashMap();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);

		if (rangeBeginIdx >= 0 && rangeEndIdx >= 0)
			query.setRange(rangeBeginIdx, rangeEndIdx);

		return (List) query.executeWithMap(params);
	}

	public static List getNonFinalizedDeliveryNotes(PersistenceManager pm, AnchorID vendorID, AnchorID customerID)
	{
		Query query = pm.newNamedQuery(DeliveryNote.class, "getNonFinalizedDeliveryNotesByVendorAndCustomer");
		Map params = new HashMap();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);

		return (List) query.executeWithMap(params);
	}

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	protected DeliveryNote() { }

	public DeliveryNote(User creator, OrganisationLegalEntity vendor,
			LegalEntity customer, long _deliveryNoteID) {
		if (creator == null)
			throw new NullPointerException("creator");

		if (vendor == null)
			throw new NullPointerException("vendor");

		if (customer == null)
			throw new NullPointerException("customer");

		if (_deliveryNoteID < 0)
			throw new IllegalArgumentException("_deliveryNoteID < 0");

//		if (currency == null)
//			throw new NullPointerException("currency");
		
		PersistenceManager pm = JDOHelper.getPersistenceManager(vendor);
		if (pm == null)
			throw new IllegalStateException("vendor is not persistent! Could not get a PersistenceManager from it!");

//		Accounting accounting = Accounting.getAccounting(pm);
//		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();

		this.organisationID = vendor.getOrganisationID();
		this.deliveryNoteID = _deliveryNoteID;
		this.createDT = new Date(System.currentTimeMillis());
		this.createUser = creator;
		this.vendor = vendor;
		this.customer = customer;
//		this.currency = currency;
		this.primaryKey = getPrimaryKey(organisationID, deliveryNoteID);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long deliveryNoteID;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private DeliveryNoteLocal deliveryNoteLocal;

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
	 * key: String articlePK (organisationID/articleID)<br/>
	 * value: Article article
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="org.nightlabs.jfire.trade.Article"
	 *		mapped-by="deliveryNote"
	 *
	 * @jdo.key mapped-by="primaryKey"
	 *
	 * @!jdo.join
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
	 */
	private Map articles = new HashMap();

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private Date createDT;

	/**
	 * The user who created this DeliveryNote.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User createUser = null;

	public String getOrganisationID() {
		return organisationID;
	}
	public long getDeliveryNoteID() {
		return deliveryNoteID;
	}

	public static String getPrimaryKey(String organisationID, long deliveryID)
	{
		return organisationID + '/' + Long.toHexString(deliveryID);
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	public DeliveryNoteLocal getDeliveryNoteLocal()
	{
		return deliveryNoteLocal;
	}
	protected void setDeliveryNoteLocal(DeliveryNoteLocal deliveryNoteLocal)
	{
		this.deliveryNoteLocal = deliveryNoteLocal;
	}

	public Date getCreateDT() {
		return createDT;
	}
	public User getCreateUser()
	{
		return createUser;
	}
	public LegalEntity getCustomer() {
		return customer;
	}
	public OrganisationLegalEntity getVendor() {
		return vendor;
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

	public Collection getArticles() {
		return Collections.unmodifiableCollection(articles.values());
	}
	
	/**
	 * A DeliveryNote is only valid after {@link #validate()} has been called.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean valid = false;
	
	/**
	 * This member stores the user who finilized this DeliveryNote.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User finalizeUser = null;

	/**
	 * This member stores when this <tt>DeliveryNote</tt> was finalized.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date finalizeDT  = null;

	/**
	 * Adds an Article, if this DeliveryNote is not yet finalized, 
	 * the offerItems offer has the same vendor and customer as this DeliveryNote, 
	 * and the offerItem is not yet part of another invoice.
	 * <p>
	 * NEVER use this method directly (within the server)! Call {@link Store#addArticlesToDeliveryNote(DeliveryNote, Collection)}
	 * instead!
	 * </p>
	 *
	 * @param article
	 */
	public void addArticle(Article article)
	throws DeliveryNoteEditException
	{
		Offer articleOffer = article.getOffer();
		Order articleOrder = articleOffer.getOrder();
		ArticleID articleID = (ArticleID) JDOHelper.getObjectId(article);
		if (isFinalized())			
			throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_DELIVERY_NOTE_FINALIZED, 
					"DeliveryNote is finalized, can not change any more!", 
					articleID
				);

		if (!vendor.getPrimaryKey().equals(articleOrder.getVendor().getPrimaryKey()) 
					|| 
				!customer.getPrimaryKey().equals(articleOrder.getCustomer().getPrimaryKey()) 
				)
		{
			throw new DeliveryNoteEditException(
				DeliveryNoteEditException.REASON_ANCHORS_DONT_MATCH,				
				"Vendor and customer are not equal for the Article to add and the delivery note, can not add the article!"
			);
		}

		DeliveryNoteID deliveryNoteID = article.getDeliveryNoteID();
		if (deliveryNoteID != null) {
			throw new DeliveryNoteEditException(
				DeliveryNoteEditException.REASON_ARTICLE_ALREADY_IN_DELIVERY_NOTE,
				"Article already in a delivery note. Article "+articleID+", DeliveryNote "+deliveryNoteID, 
				articleID, 
				deliveryNoteID
			);
		}

		if (!article.getOffer().getOfferLocal().isConfirmed()) {
			throw new DeliveryNoteEditException(
				DeliveryNoteEditException.REASON_OFFER_NOT_CONFIRMED, 
				"At least one involved offer is not confirmed!",
				articleID
			);
		}

		if (article.isReversed())
			throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_REVERSED_ARTICLE,
					"A reversed Article cannot be added to a DeliveryNote!",
					articleID);

		if (article.isReversing() && article.getReversedArticle().getDeliveryNoteID() == null)
			throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_REVERSING_ARTICLE,
					"The reversed Article is not in a DeliveryNote! Cannot add the reversing Article to one in this case!",
					articleID);

//		if (!getCurrency().getCurrencyID().equals(article.getPrice().getCurrency().getCurrencyID()))
//			throw new DeliveryNoteEditException(
//				DeliveryNoteEditException.REASON_MULTIPLE_CURRENCIES,
//				"Cannot add an Article with a different currency ("+article.getPrice().getCurrency().getCurrencyID()+") than this DeliveryNote's one ("+getCurrency().getCurrencyID()+")!"
//			);

		articles.put(article.getPrimaryKey(), article);

		this.valid = false;	
		article.setDeliveryNote(this);		
	}
	
	public void removeArticle(Article article)
	throws DeliveryNoteEditException
	{
		if (isFinalized())
			throw new DeliveryNoteEditException(DeliveryNoteEditException.REASON_DELIVERY_NOTE_FINALIZED, "DeliveryNote is finalized, can not change any more!");

		String articlePK = article.getPrimaryKey();
		if (articles.containsKey(articlePK)) {
			articles.remove(articlePK);
			this.valid = false;
			article.setDeliveryNote(null);
		}
	}

	protected void validate()
	{
		this.valid = true;
	}

	/**
	 * @return Returns the valid.
	 */
	public boolean isValid()
	{
		return valid;
	}
	protected void setFinalized(User finalizer) {
		if (isFinalized())
			return;

		if (!isValid())
			throw new IllegalStateException("This DeliveryNote is not valid! Call validate() before!");
		this.finalizeUser = finalizer;
		this.finalizeDT = new Date(System.currentTimeMillis());
	}
	/**
	 * This member is set to true as soon as all desired 
	 * {@link Article}s were added to this delivery note. A finalized 
	 * DeliveryNote can not be altered any more.
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
	
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Order is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	public void jdoPreDetach()
	{
	}
	public void jdoPostDetach(Object _detached)
	{
		DeliveryNote attached = this;
		DeliveryNote detached = (DeliveryNote)_detached;
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
}
