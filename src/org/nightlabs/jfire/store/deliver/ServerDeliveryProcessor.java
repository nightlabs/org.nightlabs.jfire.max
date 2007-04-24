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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.book.PartnerStorekeeper;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID"
 *		detachable="true"
 *		table="JFireTrade_ServerDeliveryProcessor"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, serverDeliveryProcessorID"
 *
 * @jdo.fetch-group name="ServerDeliveryProcessor.name" fields="name"
 * @jdo.fetch-group name="ServerDeliveryProcessor.modeOfDeliveries" fields="modeOfDeliveries"
 * @jdo.fetch-group name="ServerDeliveryProcessor.modeOfDeliveryFlavours" fields="modeOfDeliveryFlavours"
 * @jdo.fetch-group name="ServerDeliveryProcessor.this" fetch-groups="default" fields="name, modeOfDeliveries, modeOfDeliveryFlavours"
 *
 * @jdo.query name="getServerDeliveryProcessorsForOneModeOfDeliveryFlavour_WORKAROUND1"
 *            query="SELECT
 *            	WHERE
 *            		modeOfDeliveryFlavour.organisationID == paramOrganisationID &&
 *            		modeOfDeliveryFlavour.modeOfDeliveryFlavourID == paramModeOfDeliveryFlavourID &&
 *            		this.modeOfDeliveryFlavours.containsValue(modeOfDeliveryFlavour)
 *            	VARIABLES ModeOfDeliveryFlavour modeOfDeliveryFlavour
 *            	PARAMETERS String paramOrganisationID, String paramModeOfDeliveryFlavourID
 *            	import java.lang.String;
 *            	import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour"
 *
 * FIXME The following script doesn't work because of a current JPOX bug. Fortunately, the workaround below is functional.
 * @!jdo.query name="getServerDeliveryProcessorsForOneModeOfDeliveryFlavour_WORKAROUND2"
 *            query="SELECT
 *            	WHERE
 *            		modeOfDeliveryFlavour.organisationID == paramOrganisationID &&
 *            		modeOfDeliveryFlavour.modeOfDeliveryFlavourID == paramModeOfDeliveryFlavourID &&
 *            		this.modeOfDeliveries.containsValue(modeOfDeliveryFlavour.modeOfDelivery)
 *            	VARIABLES ModeOfDeliveryFlavour modeOfDeliveryFlavour
 *            	PARAMETERS String paramOrganisationID, String paramModeOfDeliveryFlavourID
 *            	import java.lang.String;
 *            	import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour"
 *
 * @jdo.query name="getServerDeliveryProcessorsForOneModeOfDeliveryFlavour_WORKAROUND2"
 *            query="SELECT
 *            	WHERE
 *            		modeOfDeliveryFlavour.organisationID == paramOrganisationID &&
 *            		modeOfDeliveryFlavour.modeOfDeliveryFlavourID == paramModeOfDeliveryFlavourID &&
 *            		modeOfDelivery == modeOfDeliveryFlavour.modeOfDelivery &&
 *            		this.modeOfDeliveries.containsValue(modeOfDelivery)
 *            	VARIABLES ModeOfDeliveryFlavour modeOfDeliveryFlavour; ModeOfDelivery modeOfDelivery
 *            	PARAMETERS String paramOrganisationID, String paramModeOfDeliveryFlavourID
 *            	import java.lang.String;
 *            	import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
 *            	import org.nightlabs.jfire.store.deliver.ModeOfDelivery"
 */
public abstract class ServerDeliveryProcessor
implements Serializable
{
	public static final String FETCH_GROUP_NAME = "ServerDeliveryProcessor.name";

	public static final String FETCH_GROUP_MODE_OF_DELIVERIES = "ServerDeliveryProcessor.modeOfDeliveries";

	public static final String FETCH_GROUP_MODE_OF_DELIVERY_FLAVOURS = "ServerDeliveryProcessor.modeOfDeliveryFlavours";

	public static final String FETCH_GROUP_THIS_SERVER_DELIVERY_PROCESSOR = "ServerDeliveryProcessor.this";


	/**
	 * @return Returns a <tt>Collection</tt> with instances of type
	 *         {@link ServerDeliveryProcessor}.
	 */
	public static Collection getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
			PersistenceManager pm, ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		return getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(pm,
				modeOfDeliveryFlavour.getOrganisationID(), modeOfDeliveryFlavour
						.getModeOfDeliveryFlavourID());
	}

	/**
	 * @return Returns a <tt>Collection</tt> with instances of type
	 *         {@link ServerDeliveryProcessor}.
	 */
	public static Collection getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
			PersistenceManager pm, ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
	{
		return getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(pm,
				modeOfDeliveryFlavourID.organisationID,
				modeOfDeliveryFlavourID.modeOfDeliveryFlavourID);
	}

	/**
	 * @return Returns a <tt>Collection</tt> with instances of type
	 *         {@link ServerDeliveryProcessor}.
	 */
	public static Collection getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
			PersistenceManager pm, String organisationID,
			String modeOfDeliveryFlavourID)
	{
		Map m = new HashMap();

		Query query = pm.newNamedQuery(ServerDeliveryProcessor.class,
				"getServerDeliveryProcessorsForOneModeOfDeliveryFlavour_WORKAROUND1");
		for (Iterator it = ((Collection) query.execute(organisationID,
				modeOfDeliveryFlavourID)).iterator(); it.hasNext();) {
			ServerDeliveryProcessor p = (ServerDeliveryProcessor) it.next();
			m.put(p.getPrimaryKey(), p);
		}

		query = pm.newNamedQuery(ServerDeliveryProcessor.class,
				"getServerDeliveryProcessorsForOneModeOfDeliveryFlavour_WORKAROUND2");
		for (Iterator it = ((Collection) query.execute(organisationID,
				modeOfDeliveryFlavourID)).iterator(); it.hasNext();) {
			ServerDeliveryProcessor p = (ServerDeliveryProcessor) it.next();
			m.put(p.getPrimaryKey(), p);
		}

		return m.values();
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String serverDeliveryProcessorID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * This <tt>Map</tt> stores all {@link ModeOfDelivery}s which supported by
	 * this <tt>ServerDeliveryProcessor</tt>. This means that all their flavours
	 * are included. If only some specific {@link ModeOfDeliveryFlavour}s are
	 * supported, they must be put into {@link #modeOfDeliveryFlavours}.
	 * <p>
	 * key: String modeOfDeliveryPK <br/>value: ModeOfDelivery modeOfDelivery
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfDelivery"
	 *		table="JFireTrade_ServerDeliveryProcessor_modeOfDeliveries"
	 * 
	 * @jdo.join
	 */
	private Map modeOfDeliveries = new HashMap();

	/**
	 * Unlike {@link #modeOfDeliverys}, this <tt>Map</tt> allows
	 * <tt>ServerDeliveryProcessor</tt> to declare the support of a subset of the
	 * {@link ModeOfDeliveryFlavour}s if not all of a given {@link ModeOfDelivery}
	 * are supported.
	 * <p>
	 * key: String modeOfDeliveryFlavourPK <br/>value: ModeOfDeliveryFlavour
	 * modeOfDeliveryFlavour
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfDeliveryFlavour"
	 *		table="JFireTrade_ServerDeliveryProcessor_modeOfDeliveryFlavours"
	 * 
	 * @jdo.join
	 */
	private Map modeOfDeliveryFlavours = new HashMap();

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="serverDeliveryProcessor"
	 */
	private ServerDeliveryProcessorName name;

	/**
	 * @deprecated Only of JDO!
	 */
	protected ServerDeliveryProcessor()
	{
	}

	public ServerDeliveryProcessor(String organisationID, String serverDeliveryProcessorID)
	{
		this.organisationID = organisationID;
		this.serverDeliveryProcessorID = serverDeliveryProcessorID;
		this.primaryKey = getPrimaryKey(organisationID, serverDeliveryProcessorID);
		this.name = new ServerDeliveryProcessorName(this);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the serverDeliveryProcessorID.
	 */
	public String getServerDeliveryProcessorID()
	{
		return serverDeliveryProcessorID;
	}
	
	public static String getPrimaryKey(String organisationID,
			String serverDeliveryProcessorID)
	{
		return organisationID + '/' + serverDeliveryProcessorID;
	}

	/**
	 * @return Returns the primaryKey.
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	
	public void addModeOfDelivery(ModeOfDelivery modeOfDelivery)
	{
		modeOfDeliveries.put(modeOfDelivery.getPrimaryKey(), modeOfDelivery);
	}

	public void removeModeOfDelivery(ModeOfDeliveryID modeOfDeliveryID)
	{
		modeOfDeliveries.remove(ModeOfDelivery.getPrimaryKey(
				modeOfDeliveryID.organisationID, modeOfDeliveryID.modeOfDeliveryID));
	}

	public void removeModeOfDelivery(String modeOfDeliveryPK)
	{
		modeOfDeliveries.remove(modeOfDeliveryPK);
	}

	/**
	 * @return Returns the modeOfDeliveries.
	 */
	public Collection getModeOfDeliveries()
	{
		return modeOfDeliveries.values();
	}

	public void addModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		modeOfDeliveryFlavours.put(modeOfDeliveryFlavour.getPrimaryKey(),
				modeOfDeliveryFlavour);
	}

	public void removeModeOfDeliveryFlavour(
			ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
	{
		modeOfDeliveryFlavours.remove(ModeOfDeliveryFlavour.getPrimaryKey(
				modeOfDeliveryFlavourID.organisationID,
				modeOfDeliveryFlavourID.modeOfDeliveryFlavourID));
	}

	public void removeModeOfDeliveryFlavour(String modeOfDeliveryFlavourPK)
	{
		modeOfDeliveryFlavours.remove(modeOfDeliveryFlavourPK);
	}

	/**
	 * @return Returns the modeOfDeliveryFlavours.
	 */
	public Collection getModeOfDeliveryFlavours()
	{
		return modeOfDeliveryFlavours.values();
	}
	
	/**
	 * @return Returns the name.
	 */
	public ServerDeliveryProcessorName getName()
	{
		return name;
	}

	/**
	 * The default implementation of this method returns <tt>null</tt>, meaning that
	 * all client delivery processors (which are registered on the same
	 * {@link ModeOfDelivery}s/{@link ModeOfDeliveryFlavour}s are allowed. If you
	 * want to reduce it to a certain subset, you can return here a <tt>Set</tt>
	 * containing all IDs (as specified in the client's <tt>plugin.xml</tt>).
	 * <p>
	 * Note, that this method may be called in both, client and server.
	 *
	 * @return Returns either <tt>null</tt> or a <tt>Set</tt> of <tt>String</tt>,
	 *		where each one represents an extension id (for the extension-point
	 *		"org.nightlabs.jfire.trade.clientDeliveryProcessorFactory") as specified
	 *		in the client's <tt>plugin.xml</tt>.
	 *
	 * @see #getExcludedClientDeliveryProcessorFactoryIDs()
	 */
	public Set getIncludedClientDeliveryProcessorFactoryIDs()
	{
		return null;
	}

	/**
	 * Unlike {@link #getIncludedClientDeliveryProcessorFactoryIDs()}, this
	 * method rather explicitely excludes client delivery processors. You can only
	 * overwrite one of the methods, because this one is ignored if
	 * {@link #getIncludedClientDeliveryProcessorFactoryIDs()}
	 * returned a result <tt>!= null</tt>. Either include or exclude.
	 * <p>
	 * The default implementation of this method returns <tt>null</tt>.
	 * <p>
	 * Note, that this method may be called in both, client and server.
	 *
	 * @return Returns either <tt>null</tt> or a <tt>Set</tt> of <tt>String</tt>,
	 *		where each one represents an extension id (for the extension-point
	 *		"org.nightlabs.jfire.trade.clientDeliveryProcessorFactory") as specified
	 *		in the client's <tt>plugin.xml</tt>.
	 *
	 * @see #getIncludedClientDeliveryProcessorFactoryIDs()
	 */
	public Set getExcludedClientDeliveryProcessorFactoryIDs()
	{
		return null;
	}

	public static class DeliverParams
	{
		public Store store;

		public User user;

		public DeliveryData deliveryData;

		/**
		 * @param store
		 * @param user
		 * @param deliveryData The <tt>DeliveryData</tt> (including <tt>Delivery</tt>) of the current process.
		 */
		public DeliverParams(Store store, User user,
				DeliveryData deliveryData)
		{
			this.store = store;
			this.user = user;
			this.deliveryData = deliveryData;
		}
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
					"This ServerDeliveryProcessor ("
							+ this.getPrimaryKey()
							+ ") is currently not persistent! Cannot obtain a PersistenceManager!");
		return pm;
	}

	protected Repository getRepositoryOutside(DeliverParams deliverParams,
			String repositoryIDPrefix)
	{
		PersistenceManager pm = getPersistenceManager();

		String organisationID = deliverParams.store.getOrganisationID();
		Delivery delivery = deliverParams.deliveryData.getDelivery();
		LegalEntity partner = delivery.getPartner();
		return PartnerStorekeeper.createPartnerOutsideRepository(pm, organisationID, partner);

//		String repositoryID = repositoryIDPrefix + '-' + partner.getOrganisationID() + '.' + partner.getAnchorTypeID() + '.' + partner.getAnchorID();
//
//		Repository repository;
//		try {
//			pm.getExtent(Repository.class);
//			repository = (Repository) pm.getObjectById(AnchorID.create(organisationID,
//					Repository.ANCHOR_TYPE_ID_OUTSIDE,
//					repositoryID));
//		} catch (JDOObjectNotFoundException x) {
////			OrganisationLegalEntity organisationLegalEntity = OrganisationLegalEntity
////					.getOrganisationLegalEntity(pm, organisationID, Account.ANCHOR_TYPE_ID_OUTSIDE, true);
//			repository = new Repository(
//					organisationID,
//					Repository.ANCHOR_TYPE_ID_OUTSIDE,
//					repositoryID,
//					partner, true);
//			pm.makePersistent(repository);
//		}
//		return repository;
	}

	/**
	 * This method is called by the default implementation of
	 * {@link #deliverBegin(DeliverParams)} to get the "outside"
	 * <tt>Anchor</tt> for a delivery. This might be a different repository for each
	 * processor to provide easy checking possibility with an external agency
	 * (e.g. DHL).
	 * <p>
	 * Even if you overwrite the deliver(...) method, you should use this method to
	 * obtain your "outside world anchor" to keep the API consistent.
	 * <p>
	 * It is a good idea to call {@link #getRepositoryOutside(DeliverParams, String)}.
	 *
	 * @return Returns the <tt>Anchor</tt> from which or to which the money is
	 *         flowing outside the organisation.
	 */
	public abstract Anchor getAnchorOutside(DeliverParams deliverParams);

	/**
	 * Overwrite this method to implement the first phase of your server sided
	 * delivery, if overwriting {@link #externalDeliverBegin(DeliverParams)} is not sufficient.
	 * <p>
	 * Note, that you MUST either throw a <tt>DeliveryException</tt> or call sth. like
	 * <tt>deliverParams.deliveryData.getDelivery().setDeliverBeginServerResult(deliverBeginServerResult);</tt>.
	 *
	 * @return The base implementation of this method returns a new instance of <tt>DeliverProductTransfer</tt>
	 *		if no error occured and {@link Delivery#isPostponed()}<tt> == false</tt>. If
	 *		it has been postponed, this method returns <tt>null</tt>. If the delivery failed,
	 *		a <tt>DeliveryException</tt> is thrown (hence, no result).
	 *
	 * @throws DeliveryException
	 *           If sth. goes wrong you can either manually create a
	 *           DeliverApproveResult indicating failure or throw a DeliveryException.
	 */
	public DeliverProductTransfer deliverBegin(DeliverParams deliverParams)
	throws DeliveryException
	{
		// find out from and to (one is the virtual treasury and one the partner's LegalEntity)
		Anchor from = null;
		Anchor to = null;

		if (Delivery.DELIVERY_DIRECTION_INCOMING.equals(
				deliverParams.deliveryData.getDelivery().getDeliveryDirection())) {
			from = getAnchorOutside(deliverParams);
			to = deliverParams.deliveryData.getDelivery().getPartner();
		}
		else {
			from = deliverParams.deliveryData.getDelivery().getPartner();
			to = getAnchorOutside(deliverParams);
		}

		DeliveryResult deliverBeginServerResult;
		deliverBeginServerResult = externalDeliverBegin(deliverParams);

		if (deliverBeginServerResult == null) {
			deliverBeginServerResult = new DeliveryResult(
					deliverParams.store.getOrganisationID(),
					DeliveryResult.CODE_APPROVED_NO_EXTERNAL,
					(String)null,
					(Throwable)null
					);
		}
		else if (deliverBeginServerResult.isFailed()) // in case of external failure, we don't even create a deliverProductTransfer
			throw new DeliveryException(deliverBeginServerResult); // It would be OK to create it, as the rollback would remove it anyway. And it would - though created - not be booked by the anchors.

		deliverParams.deliveryData.getDelivery().setDeliverBeginServerResult(deliverBeginServerResult);

		if (deliverParams.deliveryData.getDelivery().isPostponed())
			return null;

		DeliverProductTransfer deliverProductTransfer = new DeliverProductTransfer(
				deliverParams.store,
				null,
				deliverParams.user,
				from,
				to,
				deliverParams.deliveryData.getDelivery()
			);

		return deliverProductTransfer;
	}

	/**
	 * This is the second phase of the delivery process, executed after
	 * {@link #deliverBegin(DeliverParams)} and before {@link #deliverEnd(DeliverParams)}. It
	 * delegates to {@link #externalDeliverDoWork(DeliverParams)}.
	 */
	public void deliverDoWork(DeliverParams deliverParams)
	throws DeliveryException
	{
		Delivery delivery = deliverParams.deliveryData.getDelivery();
		DeliveryResult deliverDoWorkServerResult = externalDeliverDoWork(deliverParams); 
	
		if (deliverDoWorkServerResult == null) {
			deliverDoWorkServerResult = new DeliveryResult(
					deliverParams.store.getOrganisationID(),
					DeliveryResult.CODE_DELIVERED_NO_EXTERNAL,
					(String)null,
					(Throwable)null);
		}

		deliverParams.deliveryData.getDelivery().setDeliverDoWorkServerResult(deliverDoWorkServerResult);
	}

	/**
	 * This is the external part of the second phase of the delivery process.
	 * <p>
	 * In your implementation of this method, you must do the actual delivery in
	 * the external system if - and only if - it can still be rolled back afterwards,
	 * in case another payment/delivery (executed together) fails. If your external
	 * delivery process supports only two phases (approve & commit/rollback), you
	 * should simply return <tt>null</tt>.
	 * <p> 
	 * This method is called after
	 * {@link #externalDeliverBegin(DeliverParams)} and before
	 * {@link #externalDeliverCommit(DeliverParams)}/{@link #externalDeliverRollback(DeliverParams)}.
	 *
	 * @return Return simply <tt>null</tt> if you have no external work to do.
	 *         Otherwise, you must return a meaningful instance of
	 *         <tt>DeliveryResult</tt>.
	 *         Usually with {@link DeliveryResult#CODE_COMMITTED_WITH_EXTERNAL}.
	 * @throws DeliveryException
	 *           Throw a DeliveryException, if your operation fails.
	 */
	protected abstract DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
	throws DeliveryException;


	/**
	 * Implement this method to perform an external approval or similar initialization
	 * of the delivery. At least the data should be checked for integrity!
	 * <p>
	 * If your
	 * external delivery interface does not support two-phase deliverys (approve+deliver),
	 * you should return <tt>null</tt> (if all was successful) and perform your
	 * one-step-delivery in {@link #externalDeliverEnd(DeliverParams)}.
	 *
	 * @return Return simply <tt>null</tt> if you have no external work to do.
	 *         Otherwise, you must return a meaningful instance of
	 *         <tt>DeliveryResult</tt>.
	 * @throws DeliveryException
	 *           Throw a DeliveryException, if your operation fails.
	 */
	protected abstract DeliveryResult externalDeliverBegin(DeliverParams deliverParams)
			throws DeliveryException;

	/**
	 * This is the third phase of the server delivery.
	 * <p>
	 * In this phase, you must either commit or rollback the delivery in the
	 * external system. The default implementation of this method calls
	 * {@link #externalDeliverEnd(DeliverParams)} which you should overwrite instead
	 * of this method.
	 * <p>
	 * Note, that you MUST either throw a <tt>DeliveryException</tt> or call sth. like
	 * <tt>deliverParams.deliveryData.getDelivery().setDeliverEndServerResult(deliverEndServerResult);</tt>.
	 * It's much wiser not to override this method but just implement {@link #externalDeliverCommit(DeliverParams)
	 * and {@link #externalDeliverRollback(DeliverParams).
	 *
	 * @param deliverParams
	 * 
	 * @throws DeliveryException
	 *           If sth. goes wrong you can either manually create a
	 *           DeliverApproveResult indicating failure or throw a DeliveryException.
	 */
	public void deliverEnd(DeliverParams deliverParams)
	throws DeliveryException
	{
		Delivery delivery = deliverParams.deliveryData.getDelivery();
		DeliveryResult deliverEndServerResult;
		if (delivery.isForceRollback() || delivery.isFailed()) {
			deliverEndServerResult = externalDeliverRollback(deliverParams); 

			if (deliverEndServerResult == null) {
				deliverEndServerResult = new DeliveryResult(
						deliverParams.store.getOrganisationID(),
						DeliveryResult.CODE_ROLLED_BACK_NO_EXTERNAL,
						(String)null,
						(Throwable)null);
			}
		}
		else {
			deliverEndServerResult = externalDeliverCommit(deliverParams); 

			if (deliverEndServerResult == null) {
				deliverEndServerResult = new DeliveryResult(
						deliverParams.store.getOrganisationID(),
						DeliveryResult.CODE_COMMITTED_NO_EXTERNAL,
						(String)null,
						(Throwable)null);
			}
		}

		deliverParams.deliveryData.getDelivery().setDeliverEndServerResult(deliverEndServerResult);
	}

	/**
	 * In your implementation of this method, you must commit the delivery in
	 * the external system.
	 *
	 * @return Return simply <tt>null</tt> if you have no external work to do.
	 *         Otherwise, you must return a meaningful instance of
	 *         <tt>DeliveryResult</tt>.
	 *         Usually with {@link DeliveryResult#CODE_DELIVERED_WITH_EXTERNAL}.
	 * @throws DeliveryException
	 *           Throw a DeliveryException, if your operation fails.
	 */
	protected abstract DeliveryResult externalDeliverCommit(DeliverParams deliverParams)
	throws DeliveryException;
	
	/**
	 * In your implementation of this method, you must rollback the delivery in
	 * the external system. 
	 *
	 * @return Return simply <tt>null</tt> if you have no external work to do.
	 *         Otherwise, you must return a meaningful instance of
	 *         <tt>DeliveryResult</tt>.
	 *         Usually with {@link DeliveryResult#CODE_ROLLED_BACK_WITH_EXTERNAL}.
	 * @throws DeliveryException
	 *           Throw a DeliveryException, if your operation fails.
	 */
	protected abstract DeliveryResult externalDeliverRollback(DeliverParams deliverParams)
	throws DeliveryException;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private String requirementCheckKey;
	
	/**
	 * This method returns null if all requirements are met and a descriptive string
	 * otherwise.
	 * 
	 * @return null if all requirements are met and a descriptive string otherwise.
	 */
	public String getRequirementCheckKey() {
		return requirementCheckKey;
	}
	
	public void setRequirementCheckKey(String reqCheckKey) {
		this.requirementCheckKey = reqCheckKey;
	}
	
	/**
	 * This method is not supposed to be called from outside.
	 * Extendors should implement {@link #_checkRequirements()} instead of this method.
	 */
	public void checkRequirements() {
		requirementCheckKey = _checkRequirements();
	}
	
	/**
	 * Extendors should override this method if their {@link ServerDeliveryProcessor} if
	 * it needs to ensure some requirements before it can be used. If everything is ok, this
	 * method should return null. Otherwise a string describing the failure should be returned.
	 * 
	 * @return null if everything is ok, a descriptive string otherwise.
	 */
	protected String _checkRequirements() {
		return null;
	}
}
