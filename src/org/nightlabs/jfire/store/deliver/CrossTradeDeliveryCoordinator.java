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
package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.DeliveryNoteLocal;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.deliver.id.CrossTradeDeliveryCoordinatorID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;
import org.nightlabs.jfire.transfer.id.AnchorID;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable identity-type="application"
 *                          objectid-class="org.nightlabs.jfire.store.deliver.id.CrossTradeDeliveryCoordinatorID"
 *                          detachable="true"
 *                          table="JFireTrade_CrossTradeDeliveryCoordinator"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, crossTradeDeliveryCoordinatorID"
 */
@PersistenceCapable(
	objectIdClass=CrossTradeDeliveryCoordinatorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_CrossTradeDeliveryCoordinator")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class CrossTradeDeliveryCoordinator implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CrossTradeDeliveryCoordinator.class);

	public static CrossTradeDeliveryCoordinator getDefaultCrossTradeDeliveryCoordinator(PersistenceManager pm)
	{
		CrossTradeDeliveryCoordinatorID id = CrossTradeDeliveryCoordinatorID.create(Organisation.DEV_ORGANISATION_ID, CrossTradeDeliveryCoordinator.class.getName());
		try {
			CrossTradeDeliveryCoordinator ctdc = (CrossTradeDeliveryCoordinator) pm.getObjectById(id);
			ctdc.getModeOfDeliveryFlavour();
			return ctdc;
		} catch (JDOObjectNotFoundException x) {
			CrossTradeDeliveryCoordinator ctdc = new
			CrossTradeDeliveryCoordinator(id.organisationID, id.crossTradeDeliveryCoordinatorID);

			ModeOfDeliveryFlavour modeOfDeliveryFlavour = (ModeOfDeliveryFlavour)
			pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_FLAVOUR_ID_JFIRE);
			ctdc.setModeOfDeliveryFlavour(modeOfDeliveryFlavour);

			ServerDeliveryProcessor serverDeliveryProcessor = ServerDeliveryProcessorJFire.getServerDeliveryProcessorJFire(pm);
			ctdc.setServerDeliveryProcessor(serverDeliveryProcessor);

			ctdc = pm.makePersistent(ctdc);
			return ctdc;
		}
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String crossTradeDeliveryCoordinatorID;

	private ModeOfDeliveryFlavour modeOfDeliveryFlavour;

	private ServerDeliveryProcessor serverDeliveryProcessor;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected CrossTradeDeliveryCoordinator()
	{
	}

	public CrossTradeDeliveryCoordinator(String organisationID,
			String crossTradeDeliveryCoordinatorID)
	{
		this.organisationID = organisationID;
		this.crossTradeDeliveryCoordinatorID = crossTradeDeliveryCoordinatorID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getCrossTradeDeliveryCoordinatorID()
	{
		return crossTradeDeliveryCoordinatorID;
	}

	public ModeOfDeliveryFlavour getModeOfDeliveryFlavour()
	{
		return modeOfDeliveryFlavour;
	}

	public void setModeOfDeliveryFlavour(
			ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		this.modeOfDeliveryFlavour = modeOfDeliveryFlavour;
	}

	public ServerDeliveryProcessor getServerDeliveryProcessor()
	{
		return serverDeliveryProcessor;
	}

	public void setServerDeliveryProcessor(
			ServerDeliveryProcessor serverDeliveryProcessor)
	{
		this.serverDeliveryProcessor = serverDeliveryProcessor;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of "
					+ this.getClass().getName() + " is currently not persistent!");

		return pm;
	}


//	/**
//	 * Perform a delivery between two organisations.
//	 * <p>
//	 * Since our current delivery API works with separate transactions, the newly created DeliveryNote
//	 * is not yet seen in the delivery-methods. Hence, this method will delegate to {@link StoreManagerHelperBean#createAndReplicateVendorDeliveryNote(Set)}
//	 * which is executed in a new, separate transaction.
//	 * In the long run, we should implement a special "fast-track-delivery" which will be used between organisations and work within one
//	 * transaction.
//	 * </p>
//	 *
//	 * @param articles The articles to be delivered.
//	 * @throws ModuleException If sth. goes wrong.
//	 */
//	public void performCrossTradeDelivery(Set<Article> articles)
//	throws ModuleException
//	{
//		try {
//			if (articles.isEmpty())
//				throw new IllegalArgumentException("Param articles is empty!");
//
//			// check whether all articles are to be delivered between the same
//			// LegalEntities and in the same direction
//			// and whether they are both OrganisationLegalEntities
//			OrganisationLegalEntity from = null;
//			OrganisationLegalEntity to = null;
//			for (Article article : articles) {
//				LegalEntity _from = article.getOrder().getVendor();
//				LegalEntity _to = article.getOrder().getCustomer();
//
//				if (!(_from instanceof OrganisationLegalEntity))
//					throw new IllegalArgumentException(
//							"Article.vendor is not an OrganisationLegalEntity: "
//									+ article.getPrimaryKey());
//
//				if (!(_to instanceof OrganisationLegalEntity))
//					throw new IllegalArgumentException(
//							"Article.customer is not an OrganisationLegalEntity: "
//									+ article.getPrimaryKey());
//
//				if (article.isReversing()) {
//					LegalEntity tmp = _from;
//					_from = _to;
//					_to = tmp;
//				}
//
//				if (from == null)
//					from = (OrganisationLegalEntity) _from;
//				else {
//					if (!from.equals(_from))
//						throw new IllegalArgumentException(
//								"Not all articles have the same from-LegalEntity (= sender) for the delivery!");
//				}
//
//				if (to == null)
//					to = (OrganisationLegalEntity) _to;
//				else {
//					if (!to.equals(_to))
//						throw new IllegalArgumentException(
//								"Not all articles have the same to-LegalEntity (= receipient) for the delivery!");
//				}
//			}
//
//			if (from == null)
//				throw new IllegalStateException("from is still null!");
//			if (to == null)
//				throw new IllegalStateException("to is still null!");
//
//			PersistenceManager pm = getPersistenceManager();
//			Store store = Store.getStore(pm);
//			OrganisationLegalEntity mandator = store.getMandator();
//
//			LegalEntity partner;
//			if (mandator.equals(from))
//				partner = to;
//			else if (mandator.equals(to))
//				partner = from;
//			else
//				throw new IllegalStateException(
//						"Neither from-LegalEntity nor to-LegalEntity is the local organisation!");
//
//			StoreManager localStoreManager = createStoreManager(null);
//			StoreManager remoteStoreManager = createStoreManager(partner.getOrganisationID());
//
//			Set<Article> articlesMissingDeliveryNote = new HashSet<Article>(articles.size());
//			for (Article article : articles) {
//				if (article.getDeliveryNote() == null)
//					articlesMissingDeliveryNote.add(article);
//			}
//
//			// before we deliver, we have to create a DeliveryNote
//			if (!articlesMissingDeliveryNote.isEmpty()) {
//				StoreManagerHelperLocal storeManagerHelperLocal = StoreManagerHelperUtil.getLocalHome().create();
//				Set<ArticleID> articleIDs = NLJDOHelper.getObjectIDSet(articlesMissingDeliveryNote);
//				storeManagerHelperLocal.createAndReplicateVendorDeliveryNote(articleIDs);
//			}
//
//			// from and to are now defined and we have to create a Delivery
//			// we actually create TWO deliveries - one for the local side and one for
//			// the remote side, where the client-side of each is fake
//			Delivery localDelivery = createDelivery(mandator, from, to, articles, false);
//			DeliveryID localDeliveryID = DeliveryID.create(localDelivery
//					.getOrganisationID(), localDelivery.getDeliveryID());
//			Delivery remoteDelivery = createDelivery(mandator, from, to, articles,
//					true);
//			DeliveryID remoteDeliveryID = DeliveryID.create(remoteDelivery
//					.getOrganisationID(), remoteDelivery.getDeliveryID());
//
//			DeliveryData localDeliveryData = createDeliveryData(mandator, from, to,
//					localDelivery, localDelivery, remoteDelivery);
//			DeliveryData remoteDeliveryData = createDeliveryData(mandator, from, to,
//					remoteDelivery, localDelivery, remoteDelivery);
//
//			// the local stuff is stored here - the remote stuff is solely stored in
//			// the remote organisation - at least for now
//
//			boolean forceRollback = false;
//
//			localDeliveryData.prepareUpload();
//			localDelivery = localDeliveryData.getDelivery();
//			remoteDeliveryData.prepareUpload();
//			remoteDelivery = remoteDeliveryData.getDelivery();
//
//			// step 1: simulate the client delivery for remote side
//			remoteDelivery.setDeliverBeginClientResult(new DeliveryResult(
//					DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));
//
//			// step 2: deliver begin with remote server
//			logger.info("performCrossTradeDelivery: step 2: deliver begin with remote organisation");
//			DeliveryResult localClientDeliverBeginResult;
//			try {
//				DeliveryResult remoteServerDeliverBeginResult = remoteStoreManager.deliverBegin(remoteDeliveryData);
//				if (remoteServerDeliverBeginResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 2 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							remoteServerDeliverBeginResult.getCode() +"\" DeliveryResult.text=\"" +
//							remoteServerDeliverBeginResult.getText() + "\"");
//
//					forceRollback = true;
//					localClientDeliverBeginResult = new DeliveryResult(
//							remoteServerDeliverBeginResult.getCode(),
//							remoteServerDeliverBeginResult.getText(), null);
//				}
//				else
//					localClientDeliverBeginResult = new DeliveryResult(
//							DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 2 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				localClientDeliverBeginResult = new DeliveryResult(t);
//			}
//
//			// step 3: simulate the client delivery for local side
//			localDelivery.setDeliverBeginClientResult(localClientDeliverBeginResult);
//
////			// TODO WORKAROUND
////			localStoreManager = createStoreManager(null);
//
//			// step 4: deliver begin with local server
//			logger.info("performCrossTradeDelivery: step 4: deliver begin with local organisation");
//			DeliveryResult remoteClientDeliveryDoWorkResult;
//			try {
//				DeliveryResult localServerDeliverBeginResult = localStoreManager.deliverBegin(localDeliveryData);
//				if (localServerDeliverBeginResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 4 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							localServerDeliverBeginResult.getCode() +"\" DeliveryResult.text=\"" +
//							localServerDeliverBeginResult.getText() + "\"");
//
//					forceRollback = true;
//					remoteClientDeliveryDoWorkResult = new DeliveryResult(
//							localServerDeliverBeginResult.getCode(),
//							localServerDeliverBeginResult.getText(), null);
//				}
//				else
//					remoteClientDeliveryDoWorkResult = new DeliveryResult(
//							DeliveryResult.CODE_DELIVERED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 4 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				remoteClientDeliveryDoWorkResult = new DeliveryResult(t);
//			}
//
////			// TODO WORKAROUND
////			remoteStoreManager = createStoreManager(localDelivery.getPartnerID().organisationID);
//
//			// step 5: deliver doWork with remote server
//			DeliveryResult localClientDeliveryDoWorkResult;
//			try {
//				DeliveryResult remoteServerDeliverDoWorkResult = remoteStoreManager.deliverDoWork(
//						remoteDeliveryID, remoteClientDeliveryDoWorkResult, forceRollback);
//				if (remoteServerDeliverDoWorkResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 5 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							remoteServerDeliverDoWorkResult.getCode() +"\" DeliveryResult.text=\"" +
//							remoteServerDeliverDoWorkResult.getText() + "\"");
//
//					forceRollback = true;
//					localClientDeliveryDoWorkResult = new DeliveryResult(
//							remoteServerDeliverDoWorkResult.getCode(),
//							remoteServerDeliverDoWorkResult.getText(), null);
//				}
//				else
//					localClientDeliveryDoWorkResult = new DeliveryResult(
//							DeliveryResult.CODE_DELIVERED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 5 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				localClientDeliveryDoWorkResult = new DeliveryResult(t);
//			}
//
////			// TODO WORKAROUND
////			localStoreManager = createStoreManager(null);
//
//			// step 6: deliver doWork with local server
//			DeliveryResult remoteClientDeliveryEndResult;
//			try {
//				DeliveryResult localServerDeliverDoWorkResult = localStoreManager.deliverDoWork(
//						localDeliveryID, localClientDeliveryDoWorkResult, forceRollback);
//				if (localServerDeliverDoWorkResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 6 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							localServerDeliverDoWorkResult.getCode() +"\" DeliveryResult.text=\"" +
//							localServerDeliverDoWorkResult.getText() + "\"");
//
//					forceRollback = true;
//					remoteClientDeliveryEndResult = new DeliveryResult(
//							localServerDeliverDoWorkResult.getCode(),
//							localServerDeliverDoWorkResult.getText(), null);
//				}
//				else
//					remoteClientDeliveryEndResult = new DeliveryResult(
//							DeliveryResult.CODE_COMMITTED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 6 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				remoteClientDeliveryEndResult = new DeliveryResult(t);
//			}
//
////			// TODO WORKAROUND
////			remoteStoreManager = createStoreManager(localDelivery.getPartnerID().organisationID);
//
//			// step 7: deliver end with remote server
//			DeliveryResult localClientDeliveryEndResult;
//			try {
//				DeliveryResult remoteServerDeliverEndResult = remoteStoreManager
//						.deliverEnd(remoteDeliveryID, remoteClientDeliveryEndResult,
//								forceRollback);
//				if (remoteServerDeliverEndResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 7 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							remoteServerDeliverEndResult.getCode() +"\" DeliveryResult.text=\"" +
//							remoteServerDeliverEndResult.getText() + "\"");
//
//					forceRollback = true;
//					localClientDeliveryEndResult = new DeliveryResult(
//							remoteServerDeliverEndResult.getCode(),
//							remoteServerDeliverEndResult.getText(), null);
//				}
//				else
//					localClientDeliveryEndResult = new DeliveryResult(
//							DeliveryResult.CODE_COMMITTED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 7 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				localClientDeliveryEndResult = new DeliveryResult(t);
//			}
//
////			// TODO WORKAROUND
////			localStoreManager = createStoreManager(null);
//
//			// step 8: deliver end with local server
//			try {
//				DeliveryResult localServerDeliverEndResult = localStoreManager
//						.deliverEnd(localDeliveryID, localClientDeliveryEndResult,
//								forceRollback);
//				if (localServerDeliverEndResult.isFailed())
//					Logger.getLogger(CrossTradeDeliveryCoordinator.class).error(
//							"localStoreManager.deliverEnd(...) failed! localServerDeliverEndResult.code="
//									+ localServerDeliverEndResult.getCode()
//									+ " localServerDeliverEndResult.text="
//									+ localServerDeliverEndResult.getText() + " localDeliveryID="
//									+ localDeliveryID);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 8: localStoreManager.deliverEnd(...) failed with Exception!", t);
//			}
//
//		} catch (Exception x) {
//			throw new ModuleException(x);
//		}
//	}






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

	private static DeliveryNoteID createAndReplicateVendorDeliveryNote(PersistenceManager pm, User user, Set<ArticleID> articleIDs)
	throws Exception
	{
		if (articleIDs.isEmpty())
			throw new IllegalArgumentException("articleIDs is empty!");

		String partnerOrganisationID = null;
		for (ArticleID articleID : articleIDs) {
			if (partnerOrganisationID == null)
				partnerOrganisationID = articleID.organisationID;
			else if (!partnerOrganisationID.equals(articleID.organisationID))
				throw new IllegalArgumentException("OrganisationID mismatch! All articles need to be from the same organisation! " + partnerOrganisationID + " != " + articleID.organisationID);
		}

		StoreManagerRemote remoteStoreManager = JFireEjb3Factory.getRemoteBean(
				StoreManagerRemote.class,
				Lookup.getInitialContextProperties(pm, partnerOrganisationID)
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

		new DeliveryNoteLocal(deliveryNote); // self-registering
		deliveryNote = pm.makePersistent(deliveryNote);

		// create a Jbpm ProcessInstance
		ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) pm.getObjectById(
				ProcessDefinitionAssignmentID.create(DeliveryNote.class, TradeSide.customerCrossOrganisation));
		processDefinitionAssignment.createProcessInstance(null, user, deliveryNote);

		DeliveryNoteID deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);
		assert deliveryNoteID != null : "deliveryNoteID != null";
		return deliveryNoteID;
	}

	/**
	 * Perform a delivery between two organisations.
	 * <p>
	 * This now works in one single XA transaction.
	 * </p>
	 *
	 * @param articles The articles to be delivered.
	 * @throws ModuleException If sth. goes wrong.
	 */
	public void performCrossTradeDelivery(User user, Set<Article> articles)
	throws ModuleException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("performCrossTradeDelivery: entered with " + articles.size() + " articles.");
			if (logger.isTraceEnabled()) {
				for (Article article : articles)
					logger.trace("performCrossTradeDelivery:   * " + article);
			}
		}

		try {
			if (articles.isEmpty())
				throw new IllegalArgumentException("Param articles is empty!");

			// check whether all articles are to be delivered between the same
			// LegalEntities and in the same direction
			// and whether they are both OrganisationLegalEntities
			OrganisationLegalEntity from = null;
			OrganisationLegalEntity to = null;
			for (Article article : articles) {
				LegalEntity _from = article.getOrder().getVendor();
				LegalEntity _to = article.getOrder().getCustomer();

				if (!(_from instanceof OrganisationLegalEntity))
					throw new IllegalArgumentException(
							"Article.vendor is not an OrganisationLegalEntity: "
									+ article.getPrimaryKey());

				if (!(_to instanceof OrganisationLegalEntity))
					throw new IllegalArgumentException(
							"Article.customer is not an OrganisationLegalEntity: "
									+ article.getPrimaryKey());

				if (article.isReversing()) {
					LegalEntity tmp = _from;
					_from = _to;
					_to = tmp;
				}

				if (from == null)
					from = (OrganisationLegalEntity) _from;
				else {
					if (!from.equals(_from))
						throw new IllegalArgumentException(
								"Not all articles have the same from-LegalEntity (= sender) for the delivery!");
				}

				if (to == null)
					to = (OrganisationLegalEntity) _to;
				else {
					if (!to.equals(_to))
						throw new IllegalArgumentException(
								"Not all articles have the same to-LegalEntity (= receipient) for the delivery!");
				}
			}

			if (from == null)
				throw new IllegalStateException("from is still null!");
			if (to == null)
				throw new IllegalStateException("to is still null!");

			PersistenceManager pm = getPersistenceManager();
			Store store = Store.getStore(pm);
			OrganisationLegalEntity mandator = store.getMandator();

			LegalEntity partner;
			if (mandator.equals(from))
				partner = to;
			else if (mandator.equals(to))
				partner = from;
			else
				throw new IllegalStateException(
						"Neither from-LegalEntity nor to-LegalEntity is the local organisation!");

			StoreManagerRemote localStoreManager = createStoreManager(null);
			StoreManagerRemote remoteStoreManager = createStoreManager(partner.getOrganisationID());

			Set<Article> articlesMissingDeliveryNote = new HashSet<Article>(articles.size());
			for (Article article : articles) {
				if (article.getDeliveryNote() == null)
					articlesMissingDeliveryNote.add(article);
			}

			// before we deliver, we have to create a DeliveryNote
			if (!articlesMissingDeliveryNote.isEmpty()) {
//				StoreManagerHelperLocal storeManagerHelperLocal = StoreManagerHelperUtil.getLocalHome().create();
				Set<ArticleID> articleIDs = NLJDOHelper.getObjectIDSet(articlesMissingDeliveryNote);
//				storeManagerHelperLocal.createAndReplicateVendorDeliveryNote(articleIDs);
				createAndReplicateVendorDeliveryNote(pm, user, articleIDs);
			}

			// from and to are now defined and we have to create a Delivery
			// we actually create TWO deliveries - one for the local side and one for
			// the remote side, where the client-side of each is fake
			Delivery localDelivery = createDelivery(mandator, from, to, articles, false);
//			DeliveryID localDeliveryID = DeliveryID.create(localDelivery.getOrganisationID(), localDelivery.getDeliveryID());
			Delivery remoteDelivery = createDelivery(mandator, from, to, articles, true);
//			DeliveryID remoteDeliveryID = DeliveryID.create(remoteDelivery.getOrganisationID(), remoteDelivery.getDeliveryID());

			DeliveryData localDeliveryData = createDeliveryData(mandator, from, to,
					localDelivery, localDelivery, remoteDelivery);
			DeliveryData remoteDeliveryData = createDeliveryData(mandator, from, to,
					remoteDelivery, localDelivery, remoteDelivery);

			// the local stuff is stored here - the remote stuff is solely stored in
			// the remote organisation - at least for now

			localDeliveryData.prepareUpload();
			localDelivery = localDeliveryData.getDelivery();
			remoteDeliveryData.prepareUpload();
			remoteDelivery = remoteDeliveryData.getDelivery();

			// simulate the client delivery for both sides
			remoteDelivery.setDeliverBeginClientResult(new DeliveryResult(DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));
			remoteDelivery.setDeliverDoWorkClientResult(new DeliveryResult(DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));
			remoteDelivery.setDeliverEndClientResult(new DeliveryResult(DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));

			localDelivery.setDeliverBeginClientResult(new DeliveryResult(DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));
			localDelivery.setDeliverDoWorkClientResult(new DeliveryResult(DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));
			localDelivery.setDeliverEndClientResult(new DeliveryResult(DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));

			localStoreManager.deliverInSingleTransaction(localDeliveryData);
			remoteStoreManager.deliverInSingleTransaction(remoteDeliveryData);

//			boolean forceRollback = false;
//
//			localDeliveryData.prepareUpload();
//			localDelivery = localDeliveryData.getDelivery();
//			remoteDeliveryData.prepareUpload();
//			remoteDelivery = remoteDeliveryData.getDelivery();
//
//			// step 1: simulate the client delivery for remote side
//			remoteDelivery.setDeliverBeginClientResult(new DeliveryResult(
//					DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));
//
//			// step 2: deliver begin with remote server
//			logger.info("performCrossTradeDelivery: step 2: deliver begin with remote organisation");
//			DeliveryResult localClientDeliverBeginResult;
//			try {
//				DeliveryResult remoteServerDeliverBeginResult = remoteStoreManager.deliverBegin(remoteDeliveryData);
//				if (remoteServerDeliverBeginResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 2 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							remoteServerDeliverBeginResult.getCode() +"\" DeliveryResult.text=\"" +
//							remoteServerDeliverBeginResult.getText() + "\"");
//
//					forceRollback = true;
//					localClientDeliverBeginResult = new DeliveryResult(
//							remoteServerDeliverBeginResult.getCode(),
//							remoteServerDeliverBeginResult.getText(), null);
//				}
//				else
//					localClientDeliverBeginResult = new DeliveryResult(
//							DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 2 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				localClientDeliverBeginResult = new DeliveryResult(t);
//			}
//
//			// step 3: simulate the client delivery for local side
//			localDelivery.setDeliverBeginClientResult(localClientDeliverBeginResult);
//
////			// TODO WORKAROUND
////			localStoreManager = createStoreManager(null);
//
//			// step 4: deliver begin with local server
//			logger.info("performCrossTradeDelivery: step 4: deliver begin with local organisation");
//			DeliveryResult remoteClientDeliveryDoWorkResult;
//			try {
//				DeliveryResult localServerDeliverBeginResult = localStoreManager.deliverBegin(localDeliveryData);
//				if (localServerDeliverBeginResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 4 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							localServerDeliverBeginResult.getCode() +"\" DeliveryResult.text=\"" +
//							localServerDeliverBeginResult.getText() + "\"");
//
//					forceRollback = true;
//					remoteClientDeliveryDoWorkResult = new DeliveryResult(
//							localServerDeliverBeginResult.getCode(),
//							localServerDeliverBeginResult.getText(), null);
//				}
//				else
//					remoteClientDeliveryDoWorkResult = new DeliveryResult(
//							DeliveryResult.CODE_DELIVERED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 4 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				remoteClientDeliveryDoWorkResult = new DeliveryResult(t);
//			}
//
////			// TODO WORKAROUND
////			remoteStoreManager = createStoreManager(localDelivery.getPartnerID().organisationID);
//
//			// step 5: deliver doWork with remote server
//			DeliveryResult localClientDeliveryDoWorkResult;
//			try {
//				DeliveryResult remoteServerDeliverDoWorkResult = remoteStoreManager.deliverDoWork(
//						remoteDeliveryID, remoteClientDeliveryDoWorkResult, forceRollback);
//				if (remoteServerDeliverDoWorkResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 5 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							remoteServerDeliverDoWorkResult.getCode() +"\" DeliveryResult.text=\"" +
//							remoteServerDeliverDoWorkResult.getText() + "\"");
//
//					forceRollback = true;
//					localClientDeliveryDoWorkResult = new DeliveryResult(
//							remoteServerDeliverDoWorkResult.getCode(),
//							remoteServerDeliverDoWorkResult.getText(), null);
//				}
//				else
//					localClientDeliveryDoWorkResult = new DeliveryResult(
//							DeliveryResult.CODE_DELIVERED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 5 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				localClientDeliveryDoWorkResult = new DeliveryResult(t);
//			}
//
////			// TODO WORKAROUND
////			localStoreManager = createStoreManager(null);
//
//			// step 6: deliver doWork with local server
//			DeliveryResult remoteClientDeliveryEndResult;
//			try {
//				DeliveryResult localServerDeliverDoWorkResult = localStoreManager.deliverDoWork(
//						localDeliveryID, localClientDeliveryDoWorkResult, forceRollback);
//				if (localServerDeliverDoWorkResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 6 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							localServerDeliverDoWorkResult.getCode() +"\" DeliveryResult.text=\"" +
//							localServerDeliverDoWorkResult.getText() + "\"");
//
//					forceRollback = true;
//					remoteClientDeliveryEndResult = new DeliveryResult(
//							localServerDeliverDoWorkResult.getCode(),
//							localServerDeliverDoWorkResult.getText(), null);
//				}
//				else
//					remoteClientDeliveryEndResult = new DeliveryResult(
//							DeliveryResult.CODE_COMMITTED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 6 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				remoteClientDeliveryEndResult = new DeliveryResult(t);
//			}
//
////			// TODO WORKAROUND
////			remoteStoreManager = createStoreManager(localDelivery.getPartnerID().organisationID);
//
//			// step 7: deliver end with remote server
//			DeliveryResult localClientDeliveryEndResult;
//			try {
//				DeliveryResult remoteServerDeliverEndResult = remoteStoreManager
//						.deliverEnd(remoteDeliveryID, remoteClientDeliveryEndResult,
//								forceRollback);
//				if (remoteServerDeliverEndResult.isFailed()) {
//					logger.error("performCrossTradeDelivery: deliver-step 7 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
//							remoteServerDeliverEndResult.getCode() +"\" DeliveryResult.text=\"" +
//							remoteServerDeliverEndResult.getText() + "\"");
//
//					forceRollback = true;
//					localClientDeliveryEndResult = new DeliveryResult(
//							remoteServerDeliverEndResult.getCode(),
//							remoteServerDeliverEndResult.getText(), null);
//				}
//				else
//					localClientDeliveryEndResult = new DeliveryResult(
//							DeliveryResult.CODE_COMMITTED_NO_EXTERNAL, null, null);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 7 failed with unexpected exception! Will force rollback.", t);
//				forceRollback = true;
//				localClientDeliveryEndResult = new DeliveryResult(t);
//			}
//
////			// TODO WORKAROUND
////			localStoreManager = createStoreManager(null);
//
//			// step 8: deliver end with local server
//			try {
//				DeliveryResult localServerDeliverEndResult = localStoreManager
//						.deliverEnd(localDeliveryID, localClientDeliveryEndResult,
//								forceRollback);
//				if (localServerDeliverEndResult.isFailed())
//					Logger.getLogger(CrossTradeDeliveryCoordinator.class).error(
//							"localStoreManager.deliverEnd(...) failed! localServerDeliverEndResult.code="
//									+ localServerDeliverEndResult.getCode()
//									+ " localServerDeliverEndResult.text="
//									+ localServerDeliverEndResult.getText() + " localDeliveryID="
//									+ localDeliveryID);
//			} catch (Throwable t) {
//				logger.error("performCrossTradeDelivery: deliver-step 8: localStoreManager.deliverEnd(...) failed with Exception!", t);
//			}

		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	protected StoreManagerRemote createStoreManager(String organisationID)
			throws RemoteException, CreateException, NamingException
	{
		if (organisationID == null
				|| IDGenerator.getOrganisationID().equals(organisationID)) {
			return JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, null);
		}

		return JFireEjb3Factory.getRemoteBean(
				StoreManagerRemote.class,
				Lookup.getInitialContextProperties(getPersistenceManager(), organisationID)
		);
	}

	/**
	 * @param mandator
	 *          The {@link OrganisationLegalEntity} representing the local
	 *          organisation. This is identical with either <code>from</code> or
	 *          <code>to</code>.
	 * @param from
	 *          The {@link OrganisationLegalEntity} which sends out the
	 *          {@link Article}s (and is thus the from-side of the delivery).
	 * @param to
	 *          The {@link OrganisationLegalEntity} which reveices the
	 *          {@link Article}s (and is thus the to-side of the delivery).
	 * @param articles
	 *          The {@link Article}s that are to be delivered.
	 * @param remote
	 *          <code>false</code>, if this method should create a
	 *          {@link Delivery} instance for the local organisation;
	 *          <code>true</code>, if the {@link Delivery} is to be used on the
	 *          remote-side.
	 * @return the newly created and parameterised {@link Delivery}.
	 */
	protected Delivery createDelivery(OrganisationLegalEntity mandator,
			OrganisationLegalEntity from, OrganisationLegalEntity to,
			Set<Article> articles, boolean remote)
	{
		Delivery delivery = new Delivery(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(Delivery.class));

		Set<ArticleID> articleIDs = NLJDOHelper.getObjectIDSet(articles);
		delivery.setArticleIDs(articleIDs);
		delivery.setModeOfDeliveryFlavourID(
				(ModeOfDeliveryFlavourID) JDOHelper.getObjectId(getModeOfDeliveryFlavour()));
		delivery.setServerDeliveryProcessorID(
				(ServerDeliveryProcessorID) JDOHelper.getObjectId(getServerDeliveryProcessor()));
		delivery.setClientDeliveryProcessorFactoryID(this.getClass().getName());

		LegalEntity partner;
		if (remote)
			partner = mandator;
		else
			partner = mandator.equals(from) ? to : from;

		delivery.setPartnerID((AnchorID) JDOHelper.getObjectId(partner));
		if (mandator.equals(from) ^ remote) // if we create the Delivery for the remote side, we inverse the
																				// direction, which is done by XOR in the most elegant way
			delivery.setDeliveryDirection(Delivery.DELIVERY_DIRECTION_OUTGOING);
		else
			delivery.setDeliveryDirection(Delivery.DELIVERY_DIRECTION_INCOMING);

		return delivery;
	}

	/**
	 * The default implementation of this method creates an instance of
	 * {@link DeliveryDataCrossTrade}. You can override this method if you need a
	 * different implementation. Note, that they need to be compatible with the
	 * {@link ServerDeliveryProcessor} which is referenced by the property
	 * {@link #getServerDeliveryProcessor()}.
	 *
	 * @param mandator
	 *          The {@link OrganisationLegalEntity} representing the local
	 *          organisation. This is identical with either <code>from</code> or
	 *          <code>to</code>.
	 * @param from
	 *          The {@link OrganisationLegalEntity} which sends out the
	 *          {@link Article}s (and is thus the from-side of the delivery).
	 * @param to
	 *          The {@link OrganisationLegalEntity} which reveices the
	 *          {@link Article}s (and is thus the to-side of the delivery).
	 * @param delivery
	 *          The {@link Delivery} for which to create a {@link DeliveryData} -
	 *          this is either identical with <code>localDelivery</code> or with
	 *          <code>remoteDelivery</code>.
	 * @param localDelivery
	 *          The {@link Delivery} instance which is used to handle the delivery
	 *          in the local organisation.
	 * @param remoteDelivery
	 *          The {@link Delivery} instance which is used to handle the delivery
	 *          in the remote organisation.
	 * @return the newly created {@link DeliveryData}.
	 */
	protected DeliveryData createDeliveryData(OrganisationLegalEntity mandator,
			OrganisationLegalEntity from, OrganisationLegalEntity to,
			Delivery delivery, Delivery localDelivery, Delivery remoteDelivery)
	{
		return new DeliveryDataCrossTrade(delivery, localDelivery, remoteDelivery);
	}
}
