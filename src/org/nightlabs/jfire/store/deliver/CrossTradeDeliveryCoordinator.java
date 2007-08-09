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
import java.util.Iterator;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerHelperLocal;
import org.nightlabs.jfire.store.StoreManagerHelperUtil;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.deliver.id.CrossTradeDeliveryCoordinatorID;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.transfer.id.AnchorID;

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
public class CrossTradeDeliveryCoordinator implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CrossTradeDeliveryCoordinator.class);

	public static CrossTradeDeliveryCoordinator getDefaultCrossTradeDeliveryCoordinator(PersistenceManager pm)
	{
		CrossTradeDeliveryCoordinatorID id = CrossTradeDeliveryCoordinatorID.create(Organisation.DEVIL_ORGANISATION_ID, CrossTradeDeliveryCoordinator.class.getName());
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

			ctdc = (CrossTradeDeliveryCoordinator) pm.makePersistent(ctdc);
			return ctdc;
		}
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private String crossTradeDeliveryCoordinatorID;

	private ModeOfDeliveryFlavour modeOfDeliveryFlavour;

	private ServerDeliveryProcessor serverDeliveryProcessor;

	/**
	 * @deprecated Only for JDO!
	 */
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

	public static final String[] FETCH_GROUPS_DELIVERY_NOTE = new String[] {
			FetchPlan.DEFAULT,
			DeliveryNote.FETCH_GROUP_ARTICLES,
			DeliveryNote.FETCH_GROUP_CREATE_USER,
			DeliveryNote.FETCH_GROUP_CUSTOMER,
			DeliveryNote.FETCH_GROUP_FINALIZE_USER,
			DeliveryNote.FETCH_GROUP_VENDOR,
//			Article.FETCH_GROUP_ORDER, // should already be set - no need to detach
//			Article.FETCH_GROUP_OFFER, // should already be set - no need to detach
			Article.FETCH_GROUP_INVOICE,
			Article.FETCH_GROUP_DELIVERY_NOTE,
	};

	protected static class PerformDeliveryInvocation extends Invocation
	{
		@Implement
		public Serializable invoke()
		throws Exception
		{
			

			return null;
		}
	}

	/**
	 * Perform a delivery between two organisations.
	 * <p>
	 * Since our current delivery API works with separate transactions, the newly created DeliveryNote
	 * is not yet seen in the delivery-methods. Hence, this method will delegate to {@link StoreManagerHelperLocal#storeDeliveryNote(DeliveryNote)}
	 * which is executed in a new, separate transaction.
	 * In the long run, we should implement a special "fast-track-delivery" which will be used between organisations and work within one
	 * transaction.
	 * </p>
	 *
	 * @param articles The articles to be delivered.
	 * @throws ModuleException If sth. goes wrong.
	 */
	public void performCrossTradeDelivery(Set<Article> articles)
	throws ModuleException
	{
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

			StoreManager localStoreManager = createStoreManager(null);
			StoreManager remoteStoreManager = createStoreManager(partner.getOrganisationID());

			Set articlesMissingDeliveryNote = new HashSet(articles.size());
			for (Iterator itA = articles.iterator(); itA.hasNext(); ) {
				Article article = (Article) itA.next();
				if (article.getDeliveryNote() == null)
					articlesMissingDeliveryNote.add(article);
			}

			// before we deliver, we have to create a DeliveryNote
			{
				// TODO why does this work? Shouldn't the transaction on the remote side be pending as
				// long as this one here is? We use XA - hence, it seems that XA doesn't work. Is this
				// due to our CascadedAuthentication stuff?
				// Once the XA works fine, we need to create (and use here) a 2nd method which is tagged
				// @ejb.transaction type="RequiresNew"
				DeliveryNote deliveryNote;
				deliveryNote = remoteStoreManager.createDeliveryNote(
						NLJDOHelper.getObjectIDList(articlesMissingDeliveryNote),
						null, // deliveryNoteIDPrefix => use default
						true,
						FETCH_GROUPS_DELIVERY_NOTE, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				// Locally, the transactions work fine and cause a problem:
				// Because we cannot deliver locally without the DeliveryNote
				// and the current transaction is not yet finished when the
				// delivery is performed in *separate* transactions, we delegate this
				// to a sub-transaction as well.
				StoreManagerHelperLocal storeManagerHelperLocal = StoreManagerHelperUtil.getLocalHome().create();
				storeManagerHelperLocal.storeDeliveryNoteFromVendorOrganisation(deliveryNote);
//				storeManagerHelperLocal.testDeliveryNote((DeliveryNoteID) JDOHelper.getObjectId(deliveryNote));
			}

			// from and to are now defined and we have to create a Delivery
			// we actually create TWO deliveries - one for the local side and one for
			// the remote side, where the client-side of each is fake
			Delivery localDelivery = createDelivery(mandator, from, to, articles, false);
			DeliveryID localDeliveryID = DeliveryID.create(localDelivery
					.getOrganisationID(), localDelivery.getDeliveryID());
			Delivery remoteDelivery = createDelivery(mandator, from, to, articles,
					true);
			DeliveryID remoteDeliveryID = DeliveryID.create(remoteDelivery
					.getOrganisationID(), remoteDelivery.getDeliveryID());

			DeliveryData localDeliveryData = createDeliveryData(mandator, from, to,
					localDelivery, localDelivery, remoteDelivery);
			DeliveryData remoteDeliveryData = createDeliveryData(mandator, from, to,
					remoteDelivery, localDelivery, remoteDelivery);

			// the local stuff is stored here - the remote stuff is solely stored in
			// the remote organisation - at least for now

			boolean forceRollback = false;

			localDeliveryData.prepareUpload();
			localDelivery = localDeliveryData.getDelivery();
			remoteDeliveryData.prepareUpload();
			remoteDelivery = remoteDeliveryData.getDelivery();

			// step 1: simulate the client delivery for remote side
			remoteDelivery.setDeliverBeginClientResult(new DeliveryResult(
					DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null));

			// step 2: deliver begin with remote server
			logger.info("performCrossTradeDelivery: step 2: deliver begin with remote organisation");
			DeliveryResult localClientDeliverBeginResult;
			try {
				DeliveryResult remoteServerDeliverBeginResult = remoteStoreManager.deliverBegin(remoteDeliveryData);
				if (remoteServerDeliverBeginResult.isFailed()) {
					logger.error("performCrossTradeDelivery: deliver-step 2 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
							remoteServerDeliverBeginResult.getCode() +"\" DeliveryResult.text=\"" +
							remoteServerDeliverBeginResult.getText() + "\"");

					forceRollback = true;
					localClientDeliverBeginResult = new DeliveryResult(
							remoteServerDeliverBeginResult.getCode(),
							remoteServerDeliverBeginResult.getText(), null);
				}
				else
					localClientDeliverBeginResult = new DeliveryResult(
							DeliveryResult.CODE_APPROVED_NO_EXTERNAL, null, null);
			} catch (Throwable t) {
				logger.error("performCrossTradeDelivery: deliver-step 2 failed with unexpected exception! Will force rollback.", t);
				forceRollback = true;
				localClientDeliverBeginResult = new DeliveryResult(t);
			}

			// step 3: simulate the client delivery for local side
			localDelivery.setDeliverBeginClientResult(localClientDeliverBeginResult);
			
//			// TODO WORKAROUND
//			localStoreManager = createStoreManager(null);

			// step 4: deliver begin with local server
			logger.info("performCrossTradeDelivery: step 4: deliver begin with local organisation");
			DeliveryResult remoteClientDeliveryDoWorkResult;
			try {
				DeliveryResult localServerDeliverBeginResult = localStoreManager.deliverBegin(localDeliveryData);
				if (localServerDeliverBeginResult.isFailed()) {
					logger.error("performCrossTradeDelivery: deliver-step 4 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
							localServerDeliverBeginResult.getCode() +"\" DeliveryResult.text=\"" +
							localServerDeliverBeginResult.getText() + "\"");

					forceRollback = true;
					remoteClientDeliveryDoWorkResult = new DeliveryResult(
							localServerDeliverBeginResult.getCode(),
							localServerDeliverBeginResult.getText(), null);
				}
				else
					remoteClientDeliveryDoWorkResult = new DeliveryResult(
							DeliveryResult.CODE_DELIVERED_NO_EXTERNAL, null, null);
			} catch (Throwable t) {
				logger.error("performCrossTradeDelivery: deliver-step 4 failed with unexpected exception! Will force rollback.", t);
				forceRollback = true;
				remoteClientDeliveryDoWorkResult = new DeliveryResult(t);
			}

//			// TODO WORKAROUND
//			remoteStoreManager = createStoreManager(localDelivery.getPartnerID().organisationID);

			// step 5: deliver doWork with remote server
			DeliveryResult localClientDeliveryDoWorkResult;
			try {
				DeliveryResult remoteServerDeliverDoWorkResult = remoteStoreManager.deliverDoWork(
						remoteDeliveryID, remoteClientDeliveryDoWorkResult, forceRollback);
				if (remoteServerDeliverDoWorkResult.isFailed()) {
					logger.error("performCrossTradeDelivery: deliver-step 5 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
							remoteServerDeliverDoWorkResult.getCode() +"\" DeliveryResult.text=\"" +
							remoteServerDeliverDoWorkResult.getText() + "\"");

					forceRollback = true;
					localClientDeliveryDoWorkResult = new DeliveryResult(
							remoteServerDeliverDoWorkResult.getCode(),
							remoteServerDeliverDoWorkResult.getText(), null);
				}
				else
					localClientDeliveryDoWorkResult = new DeliveryResult(
							DeliveryResult.CODE_DELIVERED_NO_EXTERNAL, null, null);
			} catch (Throwable t) {
				logger.error("performCrossTradeDelivery: deliver-step 5 failed with unexpected exception! Will force rollback.", t);
				forceRollback = true;
				localClientDeliveryDoWorkResult = new DeliveryResult(t);
			}

//			// TODO WORKAROUND
//			localStoreManager = createStoreManager(null);

			// step 6: deliver doWork with local server
			DeliveryResult remoteClientDeliveryEndResult;
			try {
				DeliveryResult localServerDeliverDoWorkResult = localStoreManager.deliverDoWork(
						localDeliveryID, localClientDeliveryDoWorkResult, forceRollback);
				if (localServerDeliverDoWorkResult.isFailed()) {
					logger.error("performCrossTradeDelivery: deliver-step 6 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
							localServerDeliverDoWorkResult.getCode() +"\" DeliveryResult.text=\"" +
							localServerDeliverDoWorkResult.getText() + "\"");

					forceRollback = true;
					remoteClientDeliveryEndResult = new DeliveryResult(
							localServerDeliverDoWorkResult.getCode(),
							localServerDeliverDoWorkResult.getText(), null);
				}
				else
					remoteClientDeliveryEndResult = new DeliveryResult(
							DeliveryResult.CODE_COMMITTED_NO_EXTERNAL, null, null);
			} catch (Throwable t) {
				logger.error("performCrossTradeDelivery: deliver-step 6 failed with unexpected exception! Will force rollback.", t);
				forceRollback = true;
				remoteClientDeliveryEndResult = new DeliveryResult(t);
			}

//			// TODO WORKAROUND
//			remoteStoreManager = createStoreManager(localDelivery.getPartnerID().organisationID);

			// step 7: deliver end with remote server
			DeliveryResult localClientDeliveryEndResult;
			try {
				DeliveryResult remoteServerDeliverEndResult = remoteStoreManager
						.deliverEnd(remoteDeliveryID, remoteClientDeliveryEndResult,
								forceRollback);
				if (remoteServerDeliverEndResult.isFailed()) {
					logger.error("performCrossTradeDelivery: deliver-step 7 failed with DeliveryResult! Will force rollback. DeliveryResult.code=\"" +
							remoteServerDeliverEndResult.getCode() +"\" DeliveryResult.text=\"" +
							remoteServerDeliverEndResult.getText() + "\"");

					forceRollback = true;
					localClientDeliveryEndResult = new DeliveryResult(
							remoteServerDeliverEndResult.getCode(),
							remoteServerDeliverEndResult.getText(), null);
				}
				else
					localClientDeliveryEndResult = new DeliveryResult(
							DeliveryResult.CODE_COMMITTED_NO_EXTERNAL, null, null);
			} catch (Throwable t) {
				logger.error("performCrossTradeDelivery: deliver-step 7 failed with unexpected exception! Will force rollback.", t);
				forceRollback = true;
				localClientDeliveryEndResult = new DeliveryResult(t);
			}

//			// TODO WORKAROUND
//			localStoreManager = createStoreManager(null);

			// step 8: deliver end with local server
			try {
				DeliveryResult localServerDeliverEndResult = localStoreManager
						.deliverEnd(localDeliveryID, localClientDeliveryEndResult,
								forceRollback);
				if (localServerDeliverEndResult.isFailed())
					Logger.getLogger(CrossTradeDeliveryCoordinator.class).error(
							"localStoreManager.deliverEnd(...) failed! localServerDeliverEndResult.code="
									+ localServerDeliverEndResult.getCode()
									+ " localServerDeliverEndResult.text="
									+ localServerDeliverEndResult.getText() + " localDeliveryID="
									+ localDeliveryID);
			} catch (Throwable t) {
				logger.error("performCrossTradeDelivery: deliver-step 8: localStoreManager.deliverEnd(...) failed with Exception!", t);
			}

		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	protected StoreManager createStoreManager(String organisationID)
			throws RemoteException, CreateException, NamingException
	{
		if (organisationID == null
				|| IDGenerator.getOrganisationID().equals(organisationID)) {
// TO DO Workaround: The CascadedAuthentication stuff seems to fail (i.e. the StoreManager proxy created here does not point to the local organisation anymore after the remote one has been used) // I think, it works again. If this proves correct, remove these outcommented lines.
			return StoreManagerUtil.getHome().create();
//			return StoreManagerUtil.getHome(
//					Lookup.getInitialContextProperties(getPersistenceManager(),
//							IDGenerator.getOrganisationID())).create();
		}

		return StoreManagerUtil.getHome(
				Lookup.getInitialContextProperties(getPersistenceManager(),
						organisationID)).create();
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
