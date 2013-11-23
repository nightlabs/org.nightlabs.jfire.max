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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.CrossTradeDeliveryCoordinator;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.FetchGroupsTrade;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.TradeManagerRemote;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;
import org.nightlabs.util.CollectionUtil;

/**
 * @ejb.bean name="jfire/ejb/JFireTrade/StoreManagerHelper"
 *					 jndi-name="jfire/ejb/JFireTrade/StoreManagerHelper"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public abstract class StoreManagerHelperBean // I think this class is not used anymore, but I'm not sure and don't have time to check it now. Marco.
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;

	private static final String[] FETCH_GROUPS_DELIVERY_NOTE = new String[] {
		FetchPlan.DEFAULT,
		DeliveryNote.FETCH_GROUP_ARTICLES,
		DeliveryNote.FETCH_GROUP_CREATE_USER,
		DeliveryNote.FETCH_GROUP_CUSTOMER,
		DeliveryNote.FETCH_GROUP_FINALIZE_USER,
		DeliveryNote.FETCH_GROUP_VENDOR,
//		Article.FETCH_GROUP_ORDER, // should already be set - no need to detach
//		Article.FETCH_GROUP_OFFER, // should already be set - no need to detach
		Article.FETCH_GROUP_INVOICE,
		Article.FETCH_GROUP_DELIVERY_NOTE,
	};

	/**
	 * @!ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public DeliveryNoteID createAndReplicateVendorDeliveryNote(Set<ArticleID> articleIDs)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if (articleIDs.isEmpty())
				throw new IllegalArgumentException("articleIDs is empty!");

			String partnerOrganisationID = null;
			for (ArticleID articleID : articleIDs) {
				if (partnerOrganisationID == null)
					partnerOrganisationID = articleID.organisationID;
				else if (!partnerOrganisationID.equals(articleID.organisationID))
					throw new IllegalArgumentException("OrganisationID mismatch! All articles need to be from the same organisation! " + partnerOrganisationID + " != " + articleID.organisationID);
			}

			StoreManagerRemote remoteStoreManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class,
					Lookup.getInitialContextProperties(createPersistenceManager(), partnerOrganisationID)
			);

			DeliveryNote deliveryNote;
			deliveryNote = remoteStoreManager.createDeliveryNote(
					articleIDs,
					null, // deliveryNoteIDPrefix => use default
					true,
					FETCH_GROUPS_DELIVERY_NOTE, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			for (Article article : deliveryNote.getArticles()) { // TODO JPOX WORKAROUND: JPOX doesn't store the data again, because it does some optimization and doesn't recognize that the datastore is different.
				article.setDeliveryNote(deliveryNote);
				// TODO JPOX WORKAROUND: JDOHelper.makeDirty(...) doesn't work with detached objects :-( At least it didn't a while ago. Please tell me when this is fixed. Marco :-)
				JDOHelper.makeDirty(article, "deliveryNote"); // @erik: is it possible to mark all fields dirty? or even better: make jpox aware of working with different datastores?
			}

			User user = User.getUser(pm, getPrincipal());

			deliveryNote = pm.makePersistent(deliveryNote);
			new DeliveryNoteLocal(deliveryNote); // self-registering

			// create a Jbpm ProcessInstance
			ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) createPersistenceManager().getObjectById(
					ProcessDefinitionAssignmentID.create(DeliveryNote.class, TradeSide.customerCrossOrganisation));
			processDefinitionAssignment.createProcessInstance(null, user, deliveryNote);

			DeliveryNoteID deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);
			assert deliveryNoteID != null : "deliveryNoteID != null";
			return deliveryNoteID;
		} finally {
			pm.close();
		}
	}

	/**
	 * @!ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void findAndReleaseCrossTradeArticlesForProductIDs(Map<String, ? extends Collection<ProductID>> organisationID2productIDs)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {

			for (Map.Entry<String, ? extends Collection<ProductID>> me : organisationID2productIDs.entrySet()) {
				String partnerOrganisationID = me.getKey();
				List<Product> products = NLJDOHelper.getObjectList(pm, me.getValue(), Product.class);
				Set<Article> partnerArticles = new HashSet<Article>(products.size());

				for (Product product : products) {
					ProductLocal nestedProductLocal = product.getProductLocal();
					Article purchaseArticle = nestedProductLocal.getPurchaseArticle();
					if (purchaseArticle != null) {
						if (purchaseArticle.getReversingArticle() != null)
							partnerArticles.add(purchaseArticle.getReversingArticle());
						else
							partnerArticles.add(purchaseArticle);
					}

					nestedProductLocal.setPurchaseArticle(null);
				}

				if (!partnerArticles.isEmpty()) {
					TradeManagerRemote tm = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, Lookup.getInitialContextProperties(pm, partnerOrganisationID));
					Set<ArticleID> articleIDs = NLJDOHelper.getObjectIDSet(partnerArticles);
					Collection<? extends Article> articlesToReplicate = CollectionUtil.castCollection(tm.releaseArticles(
							articleIDs, true, true,
							new String[] {
									FetchPlan.DEFAULT, FetchGroupsTrade.FETCH_GROUP_ARTICLE_CROSS_TRADE_REPLICATION
							},
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
					));
					NLJDOHelper.makeDirtyAllFieldsRecursively(articlesToReplicate);
					articlesToReplicate = pm.makePersistentAll(articlesToReplicate);
				}
			} // for (Map.Entry<String, List<Product>> me : organisationID2partnerNestedProducts.entrySet()) {

		} finally {
			pm.close();
		}
	}

	/**
	 * @!ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void createReversingCrossTradeArticlesForProductIDs(Map<String, ? extends Collection<ProductID>> organisationID2productIDs)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());

			for (Map.Entry<String, ? extends Collection<ProductID>> me : organisationID2productIDs.entrySet()) {
				String partnerOrganisationID = me.getKey();
				List<Product> products = NLJDOHelper.getObjectList(pm, me.getValue(), Product.class);
				Set<ArticleID> reversedArticleIDs = new HashSet<ArticleID>(products.size());

				for (Product product : products) {
					ProductLocal nestedProductLocal = product.getProductLocal();
					Article purchaseArticle = nestedProductLocal.getPurchaseArticle();
					if (purchaseArticle.getReversingArticle() == null)
						reversedArticleIDs.add((ArticleID) JDOHelper.getObjectId(purchaseArticle));
				}

				if (!reversedArticleIDs.isEmpty()) {
					if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
						pm.flush(); // TODO JPOX WORKAROUND - maybe it helps against the update-problem (see 2nd workaround below)

					TradeManagerRemote tradeManager = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, Lookup.getInitialContextProperties(pm, partnerOrganisationID));
					Offer offer = tradeManager.createCrossTradeReverseOffer(reversedArticleIDs, null);
					NLJDOHelper.makeDirtyAllFieldsRecursively(offer);
//					offer.makeAllDirty();
					offer = pm.makePersistent(offer);

					// TODO JPOX WORKAROUND BEGIN
					if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) {
						OfferID offerID = (OfferID) JDOHelper.getObjectId(offer);
						pm.flush();
						pm.evictAll();
						offer = (Offer) pm.getObjectById(offerID);
					}
					// TODO JPOX WORKAROUND END

					// sanity check we should IMHO always do - even without the JPOX workaround being present
					for (Article article : offer.getArticles()) {
						article.checkReversing();
					}

					// create the local objects
					new OfferLocal(offer); // self-registering
					for (Article article : offer.getArticles()) {
						article.createArticleLocal(user); // self-registering
					}
					// TODO we should add it to the OfferRequirements, if it is delivered back during the release of a "front-end-article"
				}
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @!ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void deliverReversingCrossTradeArticlesForProductIDs(Map<String, ? extends Collection<ProductID>> organisationID2productIDs)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());

			for (Map.Entry<String, ? extends Collection<ProductID>> me1 : organisationID2productIDs.entrySet()) {
//				String partnerOrganisationID = me1.getKey();
				List<Product> products = NLJDOHelper.getObjectList(pm, me1.getValue(), Product.class);
				Map<CrossTradeDeliveryCoordinator, Set<Article>> crossTradeDeliveryCoordinator2reversingArticles = new HashMap<CrossTradeDeliveryCoordinator, Set<Article>>();

				for (Product product : products) {
					ProductLocal nestedProductLocal = product.getProductLocal();
					Article purchaseArticle = nestedProductLocal.getPurchaseArticle();
					if (purchaseArticle.getReversingArticle() == null)
						throw new IllegalStateException("purchaseArticle.getReversingArticle() == null for product \"" + product.getPrimaryKey() + "\"!");

					CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator = product.getProductType().getDeliveryConfiguration().getCrossTradeDeliveryCoordinator();
					Set<Article> reversingBackendArticles = crossTradeDeliveryCoordinator2reversingArticles.get(crossTradeDeliveryCoordinator);
					if (reversingBackendArticles == null) {
						reversingBackendArticles = new HashSet<Article>();
						crossTradeDeliveryCoordinator2reversingArticles.put(crossTradeDeliveryCoordinator, reversingBackendArticles);
					}
					reversingBackendArticles.add(purchaseArticle.getReversingArticle());
				}

				for (Map.Entry<CrossTradeDeliveryCoordinator, Set<Article>> me2 : crossTradeDeliveryCoordinator2reversingArticles.entrySet())
					me2.getKey().performCrossTradeDelivery(user, me2.getValue());

			} // for (Map.Entry<String, ? extends Collection<ProductID>> me1 : organisationID2productIDs.entrySet()) {
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @ejb.interface-method view-type="local"
//	 * @ejb.transaction type="RequiresNew"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void createProductTransfersAndDisassemble(ProductID packageProductID, Map<AnchorID, ? extends Collection<ProductID>> nestedProductIDsByHome)
//	throws Exception
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Store store = Store.getStore(pm);
//			User user = User.getUser(pm, getPrincipal());
//			Product packageProduct = (Product) pm.getObjectById(packageProductID);
//			Set<Anchor> involvedAnchors = new HashSet<Anchor>();
//			LinkedList<ProductTransfer> productTransfers = new LinkedList<ProductTransfer>();
//			boolean failed = true;
//			try {
//
////				Anchor thisProductHome = packageProduct.getProductType().getProductTypeLocal().getHome();
//				Anchor thisProductHome = store.getLocalStorekeeper().getHomeRepository(packageProduct);
//				for (Map.Entry<AnchorID, ? extends Collection<ProductID>> me : nestedProductIDsByHome.entrySet()) {
//					Anchor nestedProductHome = (Anchor) pm.getObjectById(me.getKey());
//					Set<Product> nestedProducts = NLJDOHelper.getObjectSet(pm, me.getValue(), Product.class);
//					// transfer from this to nested
//					if (!thisProductHome.getPrimaryKey().equals(nestedProductHome.getPrimaryKey())) {
//						ProductTransfer productTransfer = new ProductTransfer(null, user, thisProductHome, nestedProductHome, nestedProducts);
//						productTransfer = pm.makePersistent(productTransfer);
//						productTransfer.bookTransfer(user, involvedAnchors);
//						productTransfers.add(productTransfer);
//					}
//				}
//
//				// and finally check the integrity of the involved anchors after all the transfers
//				Anchor.checkIntegrity(productTransfers, involvedAnchors);
//
//				failed = false;
//			} finally {
//				if (failed)
//					Anchor.resetIntegrity(productTransfers, involvedAnchors);
//			}
//
//			packageProduct.getProductLocal().removeAllNestedProductLocals();
//			packageProduct.getProductLocal().setAssembled(false);
//		} finally {
//			pm.close();
//		}
//	}

}
