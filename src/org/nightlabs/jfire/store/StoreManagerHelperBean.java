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

import java.rmi.RemoteException;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;

/**
 * @ejb.bean name="jfire/ejb/JFireTrade/StoreManagerHelper"	
 *					 jndi-name="jfire/ejb/JFireTrade/StoreManagerHelper"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 */
public abstract class StoreManagerHelperBean 
extends BaseSessionBeanImpl
implements SessionBean 
{	
	////////////////////// EJB "constuctor" ////////////////////////////
	
	/**
	 * @ejb.create-method	
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() 
	throws CreateException
	{
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
	}

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext()
	{
		super.unsetSessionContext();
	}

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
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryNoteID createAndReplicateVendorDeliveryNote(Set<ArticleID> articleIDs)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
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

			StoreManager remoteStoreManager = StoreManagerUtil.getHome(
					Lookup.getInitialContextProperties(getPersistenceManager(), partnerOrganisationID)
			).create();

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
			ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
					ProcessDefinitionAssignmentID.create(DeliveryNote.class, TradeSide.customer));
			processDefinitionAssignment.createProcessInstance(null, user, deliveryNote);

			DeliveryNoteID deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);
			assert deliveryNoteID != null : "deliveryNoteID != null";
			return deliveryNoteID;
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @ejb.interface-method view-type="local"
//	 * @ejb.transaction type="RequiresNew"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void testDeliveryNote(DeliveryNoteID deliveryNoteID)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			try {
//				pm.getObjectById(deliveryNoteID);
//			} catch (JDOObjectNotFoundException e) {
//				Logger.getLogger(StoreManagerHelperBean.class).error("DeliveryNote does not exist in organisation "+ getOrganisationID() +": " + deliveryNoteID);
//			}
//		} finally {
//			pm.close();
//		}
//	}
}
