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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @ejb.bean name="jfire/ejb/JFireTrade/DeliveryHelper"
 *           jndi-name="jfire/ejb/JFireTrade/DeliveryHelper"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class DeliveryHelperBean
extends BaseSessionBeanImpl
implements DeliveryHelperLocal
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(DeliveryHelperBean.class);

	public static DeliveryData deliverBegin_storeDeliveryData(PersistenceManager pm, User user, DeliveryData deliveryData)
	{
		// DeliveryLocal registers itself with the Delivery
		new DeliveryLocal(deliveryData.getDelivery());

		deliveryData.getDelivery().initUser(user);
		deliveryData = pm.makePersistent(deliveryData);

		if (deliveryData.getDelivery().getPartner() == null) {
			String mandatorPK = Store.getStore(pm).getMandator().getPrimaryKey();
			DeliveryNote deliveryNote = deliveryData.getDelivery().getDeliveryNotes().iterator().next();

			LegalEntity partner = deliveryNote.getCustomer();
			if (mandatorPK.equals(partner.getPrimaryKey()))
				partner = deliveryNote.getVendor();

			deliveryData.getDelivery().setPartner(partner);
		}

		return deliveryData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverBegin_storeDeliveryData(org.nightlabs.jfire.store.deliver.DeliveryData)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public DeliveryDataID deliverBegin_storeDeliveryData(DeliveryData deliveryData)
	throws ModuleException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("deliverBegin_storeDeliveryData: *** begin ******************************************* ");
			logger.debug("deliverBegin_storeDeliveryData: IDGenerator.getOrganisationID()=" + IDGenerator.getOrganisationID());
			logger.debug("deliverBegin_storeDeliveryData: this.getOrganisationID()=" + this.getOrganisationID());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
//			if (!JDOHelper.isNew(deliveryData))
//				throw new IllegalStateException("deliveryData is not new! this method must be called with a brand new DeliveryData object.");
//			if (JDOHelper.isDetached(deliveryData))
//				deliveryData = (DeliveryData) pm.attachCopy(deliveryData, false);
//			else

//			// DeliveryLocal registers itself with the Delivery
//			new DeliveryLocal(deliveryData.getDelivery());
//
//			deliveryData.getDelivery().initUser(User.getUser(pm, getPrincipal()));
//			deliveryData = pm.makePersistent(deliveryData);
//
//			if (deliveryData.getDelivery().getPartner() == null) {
//				String mandatorPK = Store.getStore(pm).getMandator().getPrimaryKey();
//				DeliveryNote deliveryNote = deliveryData.getDelivery().getDeliveryNotes().iterator().next();
//
//				LegalEntity partner = deliveryNote.getCustomer();
//				if (mandatorPK.equals(partner.getPrimaryKey()))
//					partner = deliveryNote.getVendor();
//
//				deliveryData.getDelivery().setPartner(partner);
//			}
			deliveryData = deliverBegin_storeDeliveryData(pm, User.getUser(pm, getPrincipal()), deliveryData);
			return (DeliveryDataID) JDOHelper.getObjectId(deliveryData);
		} finally {
			pm.close();
			if (logger.isDebugEnabled()) {
				logger.debug("deliverBegin_storeDeliveryData: *** end ******************************************* ");
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverBegin_internal(org.nightlabs.jfire.store.deliver.id.DeliveryDataID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public DeliveryResult deliverBegin_internal(
			DeliveryDataID deliveryDataID,
			String[] fetchGroups, int maxFetchDepth)
	throws DeliveryException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());

			pm.getExtent(DeliveryData.class);
			DeliveryData deliveryData = (DeliveryData) pm.getObjectById(deliveryDataID);

			// delegate to Store
			DeliveryResult deliverBeginServerResult = Store.getStore(pm).deliverBegin(user, deliveryData);

//			if (!JDOHelper.isPersistent(deliverBeginServerResult))
//				deliverBeginServerResult = pm.makePersistent(deliverBeginServerResult);

			deliverBeginServerResult = pm.makePersistent(deliverBeginServerResult);
			deliveryData.getDelivery().setDeliverBeginServerResult(deliverBeginServerResult);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			DeliveryResult deliverBeginServerResult_detached = pm.detachCopy(deliverBeginServerResult);
//			deliverBeginServerResult_detached.setError(deliverBeginServerResult.getError());

			return deliverBeginServerResult_detached;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverDoWork_storeDeliverDoWorkClientResult(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void deliverDoWork_storeDeliverDoWorkClientResult(DeliveryID deliveryID, DeliveryResult deliverDoWorkClientResult, boolean forceRollback)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Delivery.class);
			Delivery delivery = (Delivery) pm.getObjectById(deliveryID);
			delivery.setDeliverDoWorkClientResult(deliverDoWorkClientResult);
			if (forceRollback)
				delivery.setForceRollback();
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverDoWork_internal(org.nightlabs.jfire.store.deliver.id.DeliveryID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public DeliveryResult deliverDoWork_internal(DeliveryID deliveryID, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			DeliveryDataID deliveryDataID = DeliveryDataID.create(deliveryID);
			pm.getExtent(DeliveryData.class);
			DeliveryData deliveryData = (DeliveryData) pm.getObjectById(deliveryDataID);

			User user = User.getUser(pm, getPrincipal());

			DeliveryResult deliverDoWorkServerResult = Store.getStore(pm).deliverDoWork(
					user,
					deliveryData
					);

//			if (!JDOHelper.isPersistent(deliverDoWorkServerResult))
//				deliverDoWorkServerResult = pm.makePersistent(deliverDoWorkServerResult);
			deliverDoWorkServerResult = pm.makePersistent(deliverDoWorkServerResult);
			deliveryData.getDelivery().setDeliverDoWorkServerResult(deliverDoWorkServerResult);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			DeliveryResult deliverDoWorkServerResult_detached = pm.detachCopy(deliverDoWorkServerResult);
//			deliverDoWorkServerResult_detached.setError(deliverDoWorkServerResult.getError());
			return deliverDoWorkServerResult_detached;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverEnd_internal(org.nightlabs.jfire.store.deliver.id.DeliveryID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public DeliveryResult deliverEnd_internal(DeliveryID deliveryID, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryException, AsyncInvokeEnqueueException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			DeliveryDataID deliveryDataID = DeliveryDataID.create(deliveryID);
			pm.getExtent(DeliveryData.class);
			DeliveryData deliveryData = (DeliveryData) pm.getObjectById(deliveryDataID);

			User user = User.getUser(pm, getPrincipal());

			DeliveryResult deliverEndServerResult = Store.getStore(pm).deliverEnd(
					user,
					deliveryData
					);

			if (deliveryData.getDelivery().isFailed()) { // FIXME this is already called within deliverEnd - should it really be called twice?!?! Marco.
				Store.getStore(pm).deliverRollback(user, deliveryData);
			}

//			if (!JDOHelper.isPersistent(deliverEndServerResult))
//				deliverEndServerResult = pm.makePersistent(deliverEndServerResult);
			deliverEndServerResult = pm.makePersistent(deliverEndServerResult);
			deliveryData.getDelivery().setDeliverEndServerResult(deliverEndServerResult);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			// get DeliveryNoteIDs for booking
			Collection<DeliveryNoteID> deliveryNoteIDs = deliveryData.getDelivery().getDeliveryNoteIDs();
			DeliveryResult deliverEndServerResult_detached = pm.detachCopy(deliverEndServerResult);
//			deliverBeginServerResult_detached.setError(deliverBeginServerResult.getError());

			AsyncInvoke.exec(new ConsolidateProductReferencesInvocation(deliveryNoteIDs, 5000), true);

			return deliverEndServerResult_detached;
		} finally {
			pm.close();
		}
	}

	public static class ConsolidateProductReferencesInvocation extends Invocation
	{
		private static final long serialVersionUID = 1L;
		private static final Logger logger = Logger.getLogger(ConsolidateProductReferencesInvocation.class);

		private long createDT = System.currentTimeMillis();
		private Collection<DeliveryNoteID> deliveryNoteIDs;
		private long delayMSec;

		public ConsolidateProductReferencesInvocation(Collection<DeliveryNoteID> deliveryNoteIDs, long delayMSec)
		{
			if (deliveryNoteIDs == null)
				throw new IllegalArgumentException("deliveryNoteIDs must not be null!");

			if (delayMSec < 0)
				throw new IllegalArgumentException("delayMSec < 0!");

			if (delayMSec > 60000)
				throw new IllegalArgumentException("delayMSec > 60000!");

			this.deliveryNoteIDs = deliveryNoteIDs;
			this.delayMSec = delayMSec;

			logger.info("Created ConsolidateProductReferencesInvocation for " + deliveryNoteIDs.size() + " deliveryNotes with "+delayMSec+" msec delay.");
		}

		@Override
		public Serializable invoke() throws Exception
		{
			long wait = createDT + delayMSec - System.currentTimeMillis();
			if (wait > 0) {
				logger.info("invoke() called: Waiting " + wait + " msec before starting to consolidate.");
				try { Thread.sleep(wait); } catch (InterruptedException x) { }
			}

			PersistenceManager pm = getPersistenceManager();
			try {
				Store store = null;

				pm.getExtent(DeliveryNote.class);
				Set<DeliveryNote> deliveryNotes = new HashSet<DeliveryNote>();
				for (DeliveryNoteID deliveryNoteID : deliveryNoteIDs) {
					DeliveryNote deliveryNote = (DeliveryNote) pm.getObjectById(deliveryNoteID);
					deliveryNotes.add(deliveryNote);
				}

				Collection<Product> products = new HashSet<Product>();
				for (DeliveryNote deliveryNote : deliveryNotes) {
					for (Article article : deliveryNote.getArticles()) {
						products.add(article.getProduct());
					}
				}

				if (store == null)
					store = Store.getStore(pm);

				logger.info("Calling store.consolidateProductReferences(...) with " + products.size() + " products.");
				store.consolidateProductReferences(products);

			} finally {
				pm.close();
			}
			return null;
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverEnd_storeDeliverEndClientResult(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void deliverEnd_storeDeliverEndClientResult(
			DeliveryID deliveryID, DeliveryResult deliverEndClientResult, boolean forceRollback)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Delivery.class);
			Delivery delivery = (Delivery) pm.getObjectById(deliveryID);
			delivery.setDeliverEndClientResult(deliverEndClientResult);
			if (forceRollback)
				delivery.setForceRollback();
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverBegin_storeDeliverBeginServerResult(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public DeliveryResult deliverBegin_storeDeliverBeginServerResult(
			DeliveryID deliveryID, DeliveryResult deliverBeginServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Delivery.class);
			Delivery delivery = (Delivery) pm.getObjectById(deliveryID);
//			DeliveryData deliveryData = (DeliveryData) pm.getObjectById(DeliveryDataID.create(deliveryID));
//			Delivery delivery = deliveryData.getDelivery();

			deliverBeginServerResult = pm.makePersistent(deliverBeginServerResult);

			delivery.setDeliverBeginServerResult(deliverBeginServerResult);

			// trigger the ProductTypeActionHandler s
			Map<Class<? extends ProductType>, Set<Article>> productTypeClass2articleSet = Article.getProductTypeClass2articleSetMap(delivery.getArticles());
			for (Map.Entry<Class<? extends ProductType>, Set<Article>> me : productTypeClass2articleSet.entrySet()) {
				ProductTypeActionHandler.getProductTypeActionHandler(pm, me.getKey()).onDeliverBegin_storeDeliverBeginServerResult(getPrincipal(), delivery, me.getValue());
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(deliverBeginServerResult);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverDoWork_storeDeliverDoWorkServerResult(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public DeliveryResult deliverDoWork_storeDeliverDoWorkServerResult(
			DeliveryID deliveryID, DeliveryResult deliverDoWorkServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Delivery.class);
			Delivery delivery = (Delivery) pm.getObjectById(deliveryID);

			deliverDoWorkServerResult = pm.makePersistent(deliverDoWorkServerResult);

			delivery.setDeliverDoWorkServerResult(deliverDoWorkServerResult);

			// trigger the ProductTypeActionHandler s
			Map<Class<? extends ProductType>, Set<Article>> productTypeClass2articleSet = Article.getProductTypeClass2articleSetMap(delivery.getArticles());
			for (Map.Entry<Class<? extends ProductType>, Set<Article>> me : productTypeClass2articleSet.entrySet()) {
				ProductTypeActionHandler.getProductTypeActionHandler(pm, me.getKey()).onDeliverDoWork_storeDeliverDoWorkServerResult(getPrincipal(), delivery, me.getValue());
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(deliverDoWorkServerResult);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverEnd_storeDeliverEndServerResult(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public DeliveryResult deliverEnd_storeDeliverEndServerResult(
			DeliveryID deliveryID, DeliveryResult deliverEndServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Delivery.class);
			Delivery delivery = (Delivery) pm.getObjectById(deliveryID);

			deliverEndServerResult = pm.makePersistent(deliverEndServerResult);

			delivery.setDeliverEndServerResult(deliverEndServerResult);

			// trigger the ProductTypeActionHandler s
			Map<Class<? extends ProductType>, Set<Article>> productTypeClass2articleSet = Article.getProductTypeClass2articleSetMap(delivery.getArticles());
			for (Map.Entry<Class<? extends ProductType>, Set<Article>> me : productTypeClass2articleSet.entrySet()) {
				ProductTypeActionHandler.getProductTypeActionHandler(pm, me.getKey()).onDeliverEnd_storeDeliverEndServerResult(getPrincipal(), delivery, me.getValue());
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(deliverEndServerResult);
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryHelperLocal#deliverRollback(org.nightlabs.jfire.store.deliver.id.DeliveryID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void deliverRollback(DeliveryID deliveryID)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Delivery.class);
			// check whether the Delivery exists
			pm.getObjectById(deliveryID);

			User user = User.getUser(pm, getPrincipal());
			pm.getExtent(DeliveryData.class);
			DeliveryData deliveryData = (DeliveryData) pm.getObjectById(DeliveryDataID.create(deliveryID));
			Store.getStore(pm).deliverRollback(user, deliveryData);
		} finally {
			pm.close();
		}
	}

}
