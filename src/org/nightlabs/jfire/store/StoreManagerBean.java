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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDNamespaceDefault;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryHelperLocal;
import org.nightlabs.jfire.store.deliver.DeliveryHelperUtil;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryConst;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorJFire;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorManual;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorNonDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour.ModeOfDeliveryFlavourProductTypeGroup;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour.ModeOfDeliveryFlavourProductTypeGroupCarrier;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.DeliveryNoteLocalID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.search.ProductTypeQuery;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 *
 * @ejb.bean name="jfire/ejb/JFireTrade/StoreManager"	
 *					 jndi-name="jfire/ejb/JFireTrade/StoreManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class StoreManagerBean 
extends BaseSessionBeanImpl
implements SessionBean 
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(StoreManagerBean.class);
	
	////////////////////// EJB "constuctor" ////////////////////////////
	
	/**
	 * @ejb.create-method	
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() 
	throws CreateException
	{
//		try
//		{
//			LOGGER.debug("StoreManagerBean created by " + this.getPrincipalString());
//		}
//		catch (Exception e)
//		{
//			throw new CreateException(e.getMessage());
//		}
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	public void unsetSessionContext()
	{
		super.unsetSessionContext();
	}

	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise() throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(ModeOfDelivery.class);
			try {
				pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);

				// it already exists, hence initialization is already done
				return;
			} catch (JDOObjectNotFoundException x) {
				// not yet initialized
			}


//			// create the essential DeliveryNoteStateDefinitions
//			JbpmConstantsDeliveryNote deliveryNoteStateDefinitionUtil;
//
//			deliveryNoteStateDefinitionUtil = new JbpmConstantsDeliveryNote(JbpmConstantsDeliveryNote.STATE_DEFINITION_ID_CREATED);
//			deliveryNoteStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "created");
//			deliveryNoteStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The DeliveryNote has been newly created. This is the first state in the DeliveryNote related workflow.");
//			pm.makePersistent(deliveryNoteStateDefinitionUtil);
//
//			deliveryNoteStateDefinitionUtil = new JbpmConstantsDeliveryNote(JbpmConstantsDeliveryNote.STATE_DEFINITION_ID_FINALIZED);
//			deliveryNoteStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "finalized");
//			deliveryNoteStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The DeliveryNote was finalized. After that, it cannot be modified anymore. A modification would require cancellation and recreation.");
//			pm.makePersistent(deliveryNoteStateDefinitionUtil);
//
//			deliveryNoteStateDefinitionUtil = new JbpmConstantsDeliveryNote(JbpmConstantsDeliveryNote.STATE_DEFINITION_ID_BOOKED);
//			deliveryNoteStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "booked");
//			deliveryNoteStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The DeliveryNote has been booked. That means, all the product transfers for all Articles has been performed internally onto the configured Repositories.");
//			pm.makePersistent(deliveryNoteStateDefinitionUtil);
//
//			deliveryNoteStateDefinitionUtil = new JbpmConstantsDeliveryNote(JbpmConstantsDeliveryNote.STATE_DEFINITION_ID_CANCELLED);
//			deliveryNoteStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "cancelled");
//			deliveryNoteStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The DeliveryNote was cancelled after finalization (and maybe after booking). In case it was already booked, a reversing booking has been done. The Article.deliveryNote fields are nulled and the Articles within the DeliveryNote have been replaced by referencingArticles.");
//			pm.makePersistent(deliveryNoteStateDefinitionUtil);
//
//			deliveryNoteStateDefinitionUtil = new JbpmConstantsDeliveryNote(JbpmConstantsDeliveryNote.STATE_DEFINITION_ID_DELIVERED);
//			deliveryNoteStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "delivered");
//			deliveryNoteStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "All Articles of the DeliveryNote were delivered. There's no Article left that still needs to be delivered.");
//			pm.makePersistent(deliveryNoteStateDefinitionUtil);

			// TODO deploy process definitions!


			Store store = Store.getStore(pm);
			Trader trader = Trader.getTrader(pm);

			LegalEntity anonymousCustomer = LegalEntity.getAnonymousCustomer(pm);
			CustomerGroup anonymousCustomerGroup = anonymousCustomer.getDefaultCustomerGroup();


			//		 create fundamental set of ModeOfDelivery/ModeOfDeliveryFlavour
			// manual
			ModeOfDelivery modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Personal Delivery (manually from hand to hand)");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Persönliche Lieferung (manuell von Hand zu Hand)");
			ModeOfDeliveryFlavour modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEVIL_ORGANISATION_ID, "manual");
			modeOfDeliveryFlavour.loadIconFromResource();
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Personal Delivery (manually from hand to hand)");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Persönliche Lieferung (manuell von Hand zu Hand)");
			pm.makePersistent(modeOfDelivery);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
			anonymousCustomerGroup.addModeOfDelivery(modeOfDelivery);

			ModeOfDelivery modeOfDeliveryManual = modeOfDelivery;


			// nonDelivery
			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_NON_DELIVERY);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Delivery");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Nichtversand");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEVIL_ORGANISATION_ID, "nonDelivery");
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Delivery");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Nichtversand");
			pm.makePersistent(modeOfDelivery);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);

			ModeOfDelivery modeOfDeliveryNonDelivery = modeOfDelivery;


			// mailing.physical
			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MAILING_PHYSICAL);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Mailing Delivery (physical)");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Postversand (physisch)");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEVIL_ORGANISATION_ID, "mailing.physical.default");
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Mailing Delivery by default service");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Postversand via Standard-Dienstleister");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEVIL_ORGANISATION_ID, "mailing.physical.DHL");
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Mailing Delivery via DHL");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Postversand via DHL");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEVIL_ORGANISATION_ID, "mailing.physical.UPS");
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Mailing Delivery via UPS");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Postversand via UPS");
			pm.makePersistent(modeOfDelivery);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
			anonymousCustomerGroup.addModeOfDelivery(modeOfDelivery);

			// mailing.virtual
			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MAILING_VIRTUAL);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Virtual Delivery (online)");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Virtuelle Lieferung (online)");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEVIL_ORGANISATION_ID, "mailing.virtual.email");
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Delivery by eMail");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Zustellung via eMail");
			pm.makePersistent(modeOfDelivery);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
			anonymousCustomerGroup.addModeOfDelivery(modeOfDelivery);

			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_JFIRE);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Internal Delivery");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "JFire-interne Lieferung");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(ModeOfDeliveryConst.MODE_OF_DELIVERY_FLAVOUR_ID_JFIRE);
			ModeOfDeliveryFlavour modeOfDeliveryFlavourJFire = modeOfDeliveryFlavour;
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Internal Delivery");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "JFire-interne Lieferung");
			pm.makePersistent(modeOfDelivery);
//			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
//			anonymousCustomerGroup.addModeOfDelivery(modeOfDelivery);

			// create some ServerDeliveryProcessor s
			ServerDeliveryProcessorManual serverDeliveryProcessorManual = ServerDeliveryProcessorManual.getServerDeliveryProcessorManual(pm);
			serverDeliveryProcessorManual.addModeOfDelivery(modeOfDeliveryManual);
			serverDeliveryProcessorManual.getName().setText(Locale.ENGLISH.getLanguage(), "Manual Delivery (no digital action)");
			serverDeliveryProcessorManual.getName().setText(Locale.GERMAN.getLanguage(), "Manuelle Lieferung (nicht-digitale Aktion)");

			ServerDeliveryProcessorNonDelivery serverDeliveryProcessorNonDelivery = ServerDeliveryProcessorNonDelivery.getServerDeliveryProcessorNonDelivery(pm);
			serverDeliveryProcessorNonDelivery.addModeOfDelivery(modeOfDeliveryNonDelivery);
			serverDeliveryProcessorNonDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Delivery (delivery will be postponed)");
			serverDeliveryProcessorNonDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Nichtlieferung (Lieferung wird verschoben)");

			ServerDeliveryProcessorJFire serverDeliveryProcessorJFire = ServerDeliveryProcessorJFire.getServerDeliveryProcessorJFire(pm);
			serverDeliveryProcessorJFire.addModeOfDeliveryFlavour(modeOfDeliveryFlavourJFire);
			serverDeliveryProcessorJFire.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Internal Delivery");

			// persist process definitions
			ProcessDefinition processDefinitionDeliveryNoteCustomer;
			processDefinitionDeliveryNoteCustomer = store.storeProcessDefinitionDeliveryNote(TradeSide.customer, ProcessDefinitionAssignment.class.getResource("deliverynote/customer/"));
			pm.makePersistent(new ProcessDefinitionAssignment(DeliveryNote.class, TradeSide.customer, processDefinitionDeliveryNoteCustomer));

			ProcessDefinition processDefinitionDeliveryNoteVendor;
			processDefinitionDeliveryNoteVendor = store.storeProcessDefinitionDeliveryNote(TradeSide.vendor, ProcessDefinitionAssignment.class.getResource("deliverynote/vendor/"));
			pm.makePersistent(new ProcessDefinitionAssignment(DeliveryNote.class, TradeSide.vendor, processDefinitionDeliveryNoteVendor));

			ProcessDefinition processDefinitionReceptionNoteCustomer;
			processDefinitionReceptionNoteCustomer = store.storeProcessDefinitionReceptionNote(TradeSide.customer, ProcessDefinitionAssignment.class.getResource("receptionnote/customer/"));
			pm.makePersistent(new ProcessDefinitionAssignment(ReceptionNote.class, TradeSide.customer, processDefinitionReceptionNoteCustomer));

			// TODO create and persist ProcessDefinition for ReceptionNote.Vendor

			IDNamespaceDefault idNamespaceDefault = IDNamespaceDefault.createIDNamespaceDefault(pm, getOrganisationID(), DeliveryNote.class);
			idNamespaceDefault.setCacheSizeServer(0);
			idNamespaceDefault.setCacheSizeClient(0);

			pm.makePersistent(new EditLockTypeDeliveryNote(EditLockTypeDeliveryNote.EDIT_LOCK_TYPE_ID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */ 
	@SuppressWarnings("unchecked")
	public List<DeliveryNote> getDeliveryNotes(Set<DeliveryNoteID> deliveryNoteIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, deliveryNoteIDs, DeliveryNote.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */ 
	public DeliveryNote getDeliveryNote(DeliveryNoteID deliveryID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (DeliveryNote) pm.detachCopy(pm.getObjectById(deliveryID));
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Searches with the given searchFilter for {@link ProductType}s.
	 *  
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */ 
	public Collection searchProductTypes(ProductTypeSearchFilter searchFilter, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Collection productTypes = searchFilter.executeQuery(pm);
			Collection result = pm.detachCopyAll(productTypes);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Searches with the given searchFilter for {@link ProductTypeGroup}s.
	 * This method will return the detached ProductTypeGroups and no further
	 * filtering. 
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public Collection searchProductTypeGroups(ProductTypeGroupSearchFilter searchFilter, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection productTypeGroups = searchFilter.executeQuery(pm);
			Collection result = pm.detachCopyAll(productTypeGroups);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Searches with the given searchFilter for {@link ProductTypeGroup}s.
	 * This method creates a ProductTypeGroupSearchResult out of the
	 * result an suppresses all ProductTypes in the groups lists that are
	 * not published or isSaleable() of the ProductType does not equal to the
	 * sableable parameter. 
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public ProductTypeGroupSearchResult searchProductTypeGroups(ProductTypeGroupSearchFilter searchFilter, boolean saleable)
	{
		PersistenceManager pm = getPersistenceManager();		
		try {
			ProductTypeGroupSearchResult searchResult = new ProductTypeGroupSearchResult();
			
			Collection productTypeGroups = searchFilter.executeQuery(pm);
			for (Iterator iter = productTypeGroups.iterator(); iter.hasNext();) {
				ProductTypeGroup group = (ProductTypeGroup) iter.next();
				searchResult.addEntry(group);
				for (Iterator iterator = group.getProductTypes().iterator(); iterator
						.hasNext();) {
					ProductType type = (ProductType) iterator.next();
					if (type.isPublished() && (type.isSaleable() == saleable)) {
						searchResult.addType(group, type);
					}
				}
			}
			
			return searchResult;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */ 
	public ProductType getProductType(ProductTypeID productTypeID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (ProductType) pm.detachCopy(pm.getObjectById(productTypeID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */ 
	public List<ProductType> getProductTypes(Set<ProductTypeID> productTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, productTypeIDs, ProductType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public ProductTypeStatus setProductTypeStatus_published(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}

			pm.getExtent(ProductType.class);
			Store store = Store.getStore(pm);
			ProductType productType = (ProductType) pm.getObjectById(productTypeID);
			store.setProductTypeStatus_published(User.getUser(pm, getPrincipal()), productType);

			if (!get)
				return null;
			else
				return (ProductTypeStatus) pm.detachCopy(store.getProductTypeStatusTracker(productTypeID, true).getCurrentStatus());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public ProductTypeStatus setProductTypeStatus_confirmed(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}

			pm.getExtent(ProductType.class);
			Store store = Store.getStore(pm);
			ProductType productType = (ProductType) pm.getObjectById(productTypeID);
			store.setProductTypeStatus_confirmed(User.getUser(pm, getPrincipal()), productType);

			if (!get)
				return null;
			else
				return (ProductTypeStatus) pm.detachCopy(store.getProductTypeStatusTracker(productTypeID, true).getCurrentStatus());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public ProductTypeStatus setProductTypeStatus_saleable(ProductTypeID productTypeID, boolean saleable, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}

			pm.getExtent(ProductType.class);
			Store store = Store.getStore(pm);
			ProductType productType = (ProductType) pm.getObjectById(productTypeID);
			store.setProductTypeStatus_saleable(User.getUser(pm, getPrincipal()), productType, saleable);

			if (!get)
				return null;
			else
				return (ProductTypeStatus) pm.detachCopy(store.getProductTypeStatusTracker(productTypeID, true).getCurrentStatus());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public ProductTypeStatus setProductTypeStatus_closed(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}

			pm.getExtent(ProductType.class);
			Store store = Store.getStore(pm);
			ProductType productType = (ProductType) pm.getObjectById(productTypeID);
			store.setProductTypeStatus_closed(User.getUser(pm, getPrincipal()), productType);

			if (!get)
				return null;
			else
				return (ProductTypeStatus) pm.detachCopy(store.getProductTypeStatusTracker(productTypeID, true).getCurrentStatus());
		} finally {
			pm.close();
		}
	}

	/**
	 * @param productTypeIDs Instances of {@link ProductTypeID}.
	 * @param customerGroupIDs Instances of {@link org.nightlabs.jfire.trade.id.CustomerGroupID}.
	 * @param mergeMode One of {@link ModeOfDeliveryFlavour#MERGE_MODE_SUBTRACTIVE} or {@link ModeOfDeliveryFlavour#MERGE_MODE_ADDITIVE}
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ModeOfDeliveryFlavourProductTypeGroupCarrier
			getModeOfDeliveryFlavourProductTypeGroupCarrier(
					Collection productTypeIDs,
					Collection customerGroupIDs,
					byte mergeMode,
					String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ModeOfDeliveryFlavourProductTypeGroupCarrier res =
				ModeOfDeliveryFlavour.getModeOfDeliveryFlavourProductTypeGroupCarrier(
						pm, productTypeIDs, customerGroupIDs, mergeMode);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ModeOfDeliveryFlavourProductTypeGroupCarrier res_detached = new ModeOfDeliveryFlavourProductTypeGroupCarrier(customerGroupIDs);
			for (Iterator it = res.getModeOfDeliveryFlavours().iterator(); it.hasNext(); ) {
				ModeOfDeliveryFlavour modf = (ModeOfDeliveryFlavour) it.next();

				res_detached.addModeOfDeliveryFlavour(
						(ModeOfDeliveryFlavour) pm.detachCopy(modf));
			}

			for (Iterator it = res.getModeOfDeliveryFlavourProductTypeGroups().iterator(); it.hasNext(); ) {
				ModeOfDeliveryFlavourProductTypeGroup group = (ModeOfDeliveryFlavourProductTypeGroup) it.next();
				res_detached.addModeOfDeliveryFlavourProductTypeGroup(group);
			}

			return res_detached;
		} finally {
			pm.close();
		}
	}

	/**
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Supports"
	 */
	public Collection<ServerDeliveryProcessor> getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
			ModeOfDeliveryFlavourID modeOfDeliveryFlavourID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<ServerDeliveryProcessor> c = ServerDeliveryProcessor.getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
					pm, modeOfDeliveryFlavourID);
			
			Map<String, String> requirementMsgMap = new HashMap<String, String>();
			
			for (ServerDeliveryProcessor pp : c) {
				pp.checkRequirements();
				requirementMsgMap.put(pp.getServerDeliveryProcessorID(), pp.getRequirementCheckKey());
			}

			c = pm.detachCopyAll(c);
			
			for (ServerDeliveryProcessor pp : c) {
				String reqMsg = requirementMsgMap.get(pp.getServerDeliveryProcessorID());
				pp.setRequirementCheckKey(reqMsg);
			}

			return c;
			
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates an DeliveryNote for all specified <code>Article</code>s. If
	 * get is true, a detached version of the new DeliveryNote will be returned.
	 *
	 * @param articleIDs The {@link ArticleID}s of those {@link Article}s that shall be added to the new <code>DeliveryNote</code>.
	 * @param get Whether a detached version of the created DeliveryNote should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the deliveryNote should be detached with.
	 * @return Detached DeliveryNote or null.
	 * @throws DeliveryNoteEditException 
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryNote createDeliveryNote(
			Collection articleIDs, String deliveryNoteIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryNoteEditException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Article.class);
			User user = User.getUser(pm, getPrincipal());
			Trader trader = Trader.getTrader(pm);
			Store store = trader.getStore();

			ArrayList articles = new ArrayList(articleIDs.size());
			for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				Article article = (Article) pm.getObjectById(articleID);
				Offer offer = article.getOffer();
				OfferLocal offerLocal = offer.getOfferLocal();
				trader.validateOffer(offer);
				trader.acceptOfferImplicitely(offer);
//				trader.finalizeOffer(user, offer);
//				trader.acceptOffer(user, offerLocal);
//				trader.confirmOffer(user, offerLocal);
				articles.add(article);
			}

			DeliveryNote deliveryNote = store.createDeliveryNote(user, articles, deliveryNoteIDPrefix);
			store.validateDeliveryNote(deliveryNote);

			if (get) {
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS + FetchPlan.DETACH_UNLOAD_FIELDS);
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return (DeliveryNote)pm.detachCopy(deliveryNote);
			}
			return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates an DeliveryNote for all <tt>Article</tt>s the Offer identified by
	 * the given offerID. If get is true a detached version of the
	 * DeliveryNote will be returned.
	 * 
	 * @param offerID OfferID of the offer to be delivered.
	 * @param get Whether a detached version of the created DeliveryNote should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the deliveryNote should be detached with.
	 * @return Detached DeliveryNote or null.
	 * @throws DeliveryNoteEditException 
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryNote createDeliveryNote(
			ArticleContainerID articleContainerID, String deliveryNoteIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryNoteEditException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (articleContainerID == null)
				throw new IllegalArgumentException("articleContainerID must not be null!");

			if (articleContainerID instanceof OrderID)
				pm.getExtent(Order.class);
			else if (articleContainerID instanceof OfferID)
				pm.getExtent(Offer.class);
			else if (articleContainerID instanceof InvoiceID)
				pm.getExtent(Invoice.class);
			else
				throw new IllegalArgumentException("articleContainerID must be an instance of OrderID, OfferID or InvoiceID, but is " + articleContainerID.getClass().getName());

			ArticleContainer articleContainer = (ArticleContainer)pm.getObjectById(articleContainerID);

			User user = User.getUser(pm, getPrincipal());
			Trader trader = Trader.getTrader(pm);
			Store store = trader.getStore();

			if (articleContainer instanceof Offer) {
				Offer offer = (Offer) articleContainer;
				OfferLocal offerLocal = offer.getOfferLocal();
				trader.validateOffer(offer);
				trader.acceptOfferImplicitely(offer);
//				trader.finalizeOffer(user, offer);
//				trader.acceptOffer(user, offerLocal);
//				trader.confirmOffer(user, offerLocal);
			}
			else {
				for (Iterator it = articleContainer.getArticles().iterator(); it.hasNext(); ) {
					Article article = (Article) it.next();
					Offer offer = article.getOffer();
					OfferLocal offerLocal = offer.getOfferLocal();
					trader.validateOffer(offer);
					trader.acceptOfferImplicitely(offer);
//					trader.finalizeOffer(user, offer);
//					trader.acceptOffer(user, offerLocal);
//					trader.confirmOffer(user, offerLocal);
				}
			}

			DeliveryNote deliveryNote = store.createDeliveryNote(user, articleContainer, deliveryNoteIDPrefix);
			store.validateDeliveryNote(deliveryNote);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS + FetchPlan.DETACH_UNLOAD_FIELDS);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return (DeliveryNote)pm.detachCopy(deliveryNote);
			}
			return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws DeliveryNoteEditException 
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryNote addArticlesToDeliveryNote(
			DeliveryNoteID deliveryNoteID, Collection articleIDs,
			boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryNoteEditException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(DeliveryNote.class);
			pm.getExtent(Article.class);
			DeliveryNote deliveryNote = (DeliveryNote) pm.getObjectById(deliveryNoteID);
			Collection articles = new ArrayList(articleIDs.size());
			for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				articles.add(pm.getObjectById(articleID));
			}

			Store store = Store.getStore(pm);
			store.addArticlesToDeliveryNote(User.getUser(pm, getPrincipal()), deliveryNote, articles);

			if (validate)
				store.validateDeliveryNote(deliveryNote);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (DeliveryNote)pm.detachCopy(deliveryNote);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws DeliveryNoteEditException
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryNote removeArticlesFromDeliveryNote(
			DeliveryNoteID deliveryNoteID, Collection articleIDs,
			boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryNoteEditException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(DeliveryNote.class);
			pm.getExtent(Article.class);
			DeliveryNote deliveryNote = (DeliveryNote) pm.getObjectById(deliveryNoteID);
			Collection articles = new ArrayList(articleIDs.size());
			for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				articles.add(pm.getObjectById(articleID));
			}

			Store store = Store.getStore(pm);
			store.removeArticlesFromDeliveryNote(User.getUser(pm, getPrincipal()), deliveryNote, articles);

			if (validate)
				store.validateDeliveryNote(deliveryNote);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (DeliveryNote)pm.detachCopy(deliveryNote);
		} finally {
			pm.close();
		}
	}

	/**
	 * @param deliveryDataList A <tt>List</tt> of {@link DeliveryData}.
	 * @return A <tt>List</tt> with instances of {@link DeliveryResult} in the same
	 *		order as and corresponding to the {@link DeliveryData} objects passed in
	 *		<tt>deliveryDataList</tt>.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public List deliverBegin(List deliveryDataList)
	throws ModuleException
	{
		try {
			StoreManagerLocal storeManagerLocal = StoreManagerUtil.getLocalHome().create();
	
			List resList = new ArrayList();
			for (Iterator it = deliveryDataList.iterator(); it.hasNext(); ) {
				DeliveryData deliveryData = (DeliveryData) it.next();

				DeliveryResult res = null;
				try {
					res = storeManagerLocal._deliverBegin(deliveryData);
				} catch (Throwable t) {
					DeliveryException x = null;
					if (t instanceof DeliveryException)
						x = (DeliveryException)t;
					else {
						int i = ExceptionUtils.indexOfThrowable(t, DeliveryException.class);
						if (i >= 0)
							x = (DeliveryException)ExceptionUtils.getThrowables(t)[i];
					}
					if (x != null)
						res = x.getDeliveryResult();
					else
						res = new DeliveryResult(getOrganisationID(), t);
				}
				if (res != null)
					resList.add(res);

			}
			return resList;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * @param serverDeliveryProcessorID Might be <tt>null</tt>.
	 * @param deliveryDirection Either
	 *		{@link ServerDeliveryProcessor#DELIVERY_DIRECTION_INCOMING}
	 *		or {@link ServerDeliveryProcessor#DELIVERY_DIRECTION_OUTGOING}.
	 *
	 * @throws ModuleException
	 *
	 * @see Store#deliverBegin(User, DeliveryData)
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryResult deliverBegin(DeliveryData deliveryData)
	throws ModuleException
	{
		return _deliverBegin(deliveryData);		
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryResult _deliverBegin(DeliveryData deliveryData)
	throws ModuleException
	{
		if (deliveryData == null)
			throw new NullPointerException("deliveryData");

		if (deliveryData.getDelivery() == null)
			throw new NullPointerException("deliveryData.getDelivery() is null!");

//		if (deliveryData.getDelivery().getPartnerID() == null) {
//			// if no partner is defined, at least one deliveryNote must be known!
//			if (deliveryData.getDelivery().getDeliveryNoteIDs() == null)
//				throw new NullPointerException("deliveryData.getDelivery().getPartnerID() and deliveryData.getDelivery().getDeliveryNoteIDs() are both null! One of them must be specified, because I need to know who's delivering!"); 
//
//			if (deliveryData.getDelivery().getDeliveryNoteIDs().isEmpty())
//				throw new NullPointerException("deliveryData.getDelivery().getPartnerID() is null and deliveryData.getDelivery().getDeliveryNoteIDs() is empty! If no partner is specified explicitely, I need at least one invoice to find out who's delivering!");
//		}
		if (deliveryData.getDelivery().getArticleIDs() == null)
			throw new IllegalArgumentException("deliveryData.getDelivery().getArticleIDs() is null!");

		if (deliveryData.getDelivery().getArticleIDs().isEmpty())
			throw new IllegalArgumentException("deliveryData.getDelivery().getArticleIDs() is empty!");

//		if (deliveryData.getDelivery().getCurrencyID() == null)
//			throw new NullPointerException("deliveryData.getDelivery().getCurrencyID() is null!");
//
//		if (deliveryData.getDelivery().getAmount() < 0)
//			throw new IllegalArgumentException("deliveryData.getDelivery().getAmount() < 0!");

		if (deliveryData.getDelivery().getModeOfDeliveryFlavourID() == null)
			throw new IllegalArgumentException("deliveryData.getDelivery().getModeOfDeliveryFlavourID() is null!");

		if (deliveryData.getDelivery().getClientDeliveryProcessorFactoryID() == null)
			throw new IllegalArgumentException("deliveryData.getDelivery().getClientDeliveryProcessorFactoryID() is null!");

		if (deliveryData.getDelivery().getDeliverBeginClientResult() == null)
			throw new IllegalArgumentException("deliveryData.getDelivery().getDeliverBeginClientResult() is null!");

		// Store deliveryData into the database within a NEW TRANSACTION to prevent it
		// from being deleted (if this method fails later and causes a rollback).
		DeliveryDataID deliveryDataID;
		DeliveryHelperLocal deliveryHelperLocal;
		try {
			deliveryHelperLocal = DeliveryHelperUtil.getLocalHome().create();
			deliveryDataID = deliveryHelperLocal.deliverBegin_storeDeliveryData(deliveryData);
		} catch (ModuleException x) {
			throw x;
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}

		String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return deliveryHelperLocal.deliverBegin_internal(deliveryDataID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (Throwable t) {
			DeliveryResult deliverBeginServerResult = new DeliveryResult(getOrganisationID(), t);

			try {
				return deliveryHelperLocal.deliverBegin_storeDeliverBeginServerResult(
						DeliveryID.create(deliveryDataID), deliverBeginServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			} catch (ModuleException x) {
				throw x;
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}
		}
	}

	
	/**
	 * @param deliveryIDs Instances of {@link DeliveryID}
	 * @param deliverEndClientResults Instances of {@link DeliveryResult} corresponding
	 *		to the <tt>deliveryIDs</tt>. Hence, both lists must have the same number of items.
	 * @param forceRollback If <tt>true</tt> all deliveries will be rolled back, even if they
	 *		have been successful so far.
	 * @return A <tt>List</tt> with instances of {@link DeliveryResult} in the same
	 *		order as and corresponding to the {@link DeliveryID} objects passed in
	 *		<tt>deliveryIDs</tt>.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public List deliverEnd(List deliveryIDs, List deliverEndClientResults, boolean forceRollback)
	throws ModuleException
	{
		try {
			StoreManagerLocal storeManagerLocal = StoreManagerUtil.getLocalHome().create();

			if (deliveryIDs.size() != deliverEndClientResults.size())
				throw new IllegalArgumentException("deliveryIDs.size() != deliverEndClientResults.size()!!!");

			List resList = new ArrayList();
			Iterator itResults = deliverEndClientResults.iterator();
			for (Iterator itIDs = deliveryIDs.iterator(); itIDs.hasNext(); ) {
				DeliveryID deliveryID = (DeliveryID) itIDs.next();
				DeliveryResult deliverEndClientResult = (DeliveryResult) itResults.next();

				DeliveryResult res = null;
				try {
					res = storeManagerLocal._deliverEnd(deliveryID, deliverEndClientResult, forceRollback);
				} catch (Throwable t) {
					DeliveryException x = null;
					if (t instanceof DeliveryException)
						x = (DeliveryException)t;
					else {
						int i = ExceptionUtils.indexOfThrowable(t, DeliveryException.class);
						if (i >= 0)
							x = (DeliveryException)ExceptionUtils.getThrowables(t)[i];
					}
					if (x != null)
						res = x.getDeliveryResult();
					else
						res = new DeliveryResult(getOrganisationID(), t);
				}
				if (res != null)
					resList.add(res);

			}
			return resList;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}


	/**
	 * @param deliveryIDs Instances of {@link DeliveryID}
	 * @param deliverDoWorkClientResults Instances of {@link DeliveryResult} corresponding
	 *		to the <tt>deliveryIDs</tt>. Hence, both lists must have the same number of items.
	 * @param forceRollback If <tt>true</tt> all deliveries will be rolled back, even if they
	 *		have been successful so far.
	 * @return A <tt>List</tt> with instances of {@link DeliveryResult} in the same
	 *		order as and corresponding to the {@link DeliveryID} objects passed in
	 *		<tt>deliveryIDs</tt>.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public List deliverDoWork(List deliveryIDs, List deliverDoWorkClientResults, boolean forceRollback)
	throws ModuleException
	{
		try {
			StoreManagerLocal storeManagerLocal = StoreManagerUtil.getLocalHome().create();

			if (deliveryIDs.size() != deliverDoWorkClientResults.size())
				throw new IllegalArgumentException("deliveryIDs.size() != deliverDoWorkClientResults.size()!!!");

			List resList = new ArrayList();
			Iterator itResults = deliverDoWorkClientResults.iterator();
			for (Iterator itIDs = deliveryIDs.iterator(); itIDs.hasNext(); ) {
				DeliveryID deliveryID = (DeliveryID) itIDs.next();
				DeliveryResult deliverDoWorkClientResult = (DeliveryResult) itResults.next();

				DeliveryResult res = null;
				try {
					res = storeManagerLocal._deliverDoWork(deliveryID, deliverDoWorkClientResult, forceRollback);
				} catch (Throwable t) {
					DeliveryException x = null;
					if (t instanceof DeliveryException)
						x = (DeliveryException)t;
					else {
						int i = ExceptionUtils.indexOfThrowable(t, DeliveryException.class);
						if (i >= 0)
							x = (DeliveryException)ExceptionUtils.getThrowables(t)[i];
					}
					if (x != null)
						res = x.getDeliveryResult();
					else
						res = new DeliveryResult(getOrganisationID(), t);
				}
				if (res != null)
					resList.add(res);

			}
			return resList;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}


	/**
	 * @throws ModuleException
	 *
	 * @see Accounting#deliverEnd(User, DeliveryData)
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryResult deliverDoWork(
			DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult,
			boolean forceRollback)
	throws ModuleException
	{
		return _deliverDoWork(deliveryID, deliverEndClientResult, forceRollback);
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryResult _deliverDoWork(
			DeliveryID deliveryID,
			DeliveryResult deliverDoWorkClientResult,
			boolean forceRollback)
	throws ModuleException
	{
		if (deliveryID == null)
			throw new NullPointerException("deliveryID");

		if (deliverDoWorkClientResult == null)
			throw new NullPointerException("deliverDoWorkClientResult");

		// Store deliverDoWorkClientResult into the database within a NEW TRANSACTION to
		// prevent it from being lost (if this method fails later and causes a rollback).
		DeliveryHelperLocal deliveryHelperLocal;
		try {
			deliveryHelperLocal = DeliveryHelperUtil.getLocalHome().create();
			deliveryHelperLocal.deliverDoWork_storeDeliverDoWorkClientResult(
					deliveryID, deliverDoWorkClientResult, forceRollback);
		} catch (ModuleException x) {
			throw x;
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}

		String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return deliveryHelperLocal.deliverDoWork_internal(deliveryID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (Throwable t) {
			DeliveryResult deliverDoWorkServerResult = new DeliveryResult(getOrganisationID(), t);

			try {
				DeliveryResult deliverDoWorkServerResult_detached = deliveryHelperLocal.deliverDoWork_storeDeliverDoWorkServerResult(
						deliveryID, deliverDoWorkServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				return deliverDoWorkServerResult_detached;
			} catch (ModuleException x) {
				throw x;
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}
		}
	}


	/**
	 * @throws ModuleException
	 *
	 * @see Accounting#deliverEnd(User, DeliveryData)
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryResult deliverEnd(
			DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult,
			boolean forceRollback)
	throws ModuleException
	{
		return _deliverEnd(deliveryID, deliverEndClientResult, forceRollback);
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryResult _deliverEnd(
			DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult,
			boolean forceRollback)
	throws ModuleException
	{
		if (deliveryID == null)
			throw new NullPointerException("deliveryID");

		if (deliverEndClientResult == null)
			throw new NullPointerException("deliverEndClientResult");

		// Store deliverEndClientResult into the database within a NEW TRANSACTION to
		// prevent it from being lost (if this method fails later and causes a rollback).
		DeliveryHelperLocal deliveryHelperLocal;
		try {
			deliveryHelperLocal = DeliveryHelperUtil.getLocalHome().create();
			deliveryHelperLocal.deliverEnd_storeDeliverEndClientResult(
					deliveryID, deliverEndClientResult, forceRollback);
		} catch (ModuleException x) {
			throw x;
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}

		String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return deliveryHelperLocal.deliverEnd_internal(deliveryID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (Throwable t) {
			DeliveryResult deliverEndServerResult = new DeliveryResult(getOrganisationID(), t);

			try {
				DeliveryResult deliverEndServerResult_detached = deliveryHelperLocal.deliverEnd_storeDeliverEndServerResult(
						deliveryID, deliverEndServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				deliveryHelperLocal.deliverRollback(deliveryID);

				return deliverEndServerResult_detached;
			} catch (ModuleException x) {
				throw x;
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}
		}
	}

	/**
	 * This method queries all <code>DeliveryNote</code>s which exist between the given vendor and customer.
	 * They are ordered by deliveryNoteID descending (means newest first).
	 *
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link DeliveryNote}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public List<DeliveryNoteID> getDeliveryNoteIDs(AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return new ArrayList<DeliveryNoteID>(DeliveryNote.getDeliveryNoteIDs(pm, vendorID, customerID, rangeBeginIdx, rangeEndIdx));
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//			return (List) pm.detachCopyAll(DeliveryNote.getDeliveryNoteIDs(pm, vendorID, customerID, rangeBeginIdx, rangeEndIdx));
		} finally {
			pm.close();
		}
	}

	/**
	 * This method queries all <code>Invoice</code>s which exist between the given vendor and customer and
	 * are not yet finalized. They are ordered by invoiceID descending (means newest first).
	 *
 	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public List getNonFinalizedDeliveryNotes(AnchorID vendorID, AnchorID customerID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return (List) pm.detachCopyAll(DeliveryNote.getNonFinalizedDeliveryNotes(pm, vendorID, customerID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void signalDeliveryNote(DeliveryNoteID deliveryNoteID, String jbpmTransitionName)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			DeliveryNoteLocal deliveryNoteLocal = (DeliveryNoteLocal) pm.getObjectById(DeliveryNoteLocalID.create(deliveryNoteID));
			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
			try {
				ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(deliveryNoteLocal.getJbpmProcessInstanceId());
				processInstance.signal(jbpmTransitionName);
			} finally {
				jbpmContext.close();
			}
		} finally {
			pm.close();
		}
	}

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Set<ProductTypeID> getProductTypeIDs(Collection<ProductTypeQuery> productTypeQueries)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			Set<ProductType> productTypes = null;
			for (ProductTypeQuery query : productTypeQueries) {
				query.setPersistenceManager(pm);
				query.setCandidates(productTypes);
				productTypes = new HashSet<ProductType>(query.getResult());
			}

			return NLJDOHelper.getObjectIDSet(productTypes);
		} finally {
			pm.close();
		}
	}		
//	/**
//	 * Sets the <tt>published</tt> property of the specified <tt>ProductType</tt>
//	 * to <tt>true</tt>.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type = "Required"
//	 */ 
//	public ProductType publishProductType(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getExtent(ProductType.class);
//			ProductType pt = (ProductType) pm.getObjectById(productTypeID);
//			pt.setPublished(true);
//			
//			if (!get)
//				return null;
//				
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			return (ProductType) pm.detachCopy(pt);
//		} finally {
//			pm.close();
//		}
//	}

//	////////////////////// product types ////////////////////////////
//
//	/**
//	 * Adds a product type. If the productID is null, a new one
//	 * will be created.
//	 * 
//	 * @throws ModuleException
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="StoreManager-addProductType"
//	 * @ejb.transaction type = "Required"
//	 **/
//	public void addProductType(ProductType productType) 
//	throws StoreException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				IDGenerator idgen = IDGeneratorUtil.getHome().create();
//				try {
//					String compID = productType.getProductID();
//					if(compID == null)
//					{
//						long newID = idgen.generateIDLong(ProductType.class.getName());
//						compID = this.getOrganisationID() + "/" + new Long(newID);
//					}
//					ProductType pt = new ProductType(compID);
//					pm.makePersistent(pt);
//				} finally	{
//					idgen.remove();
//				}
//			} finally {
//				pm.close();
//			}
//		} catch (Exception e) {
//			throw new StoreException(e);
//		}		
//	}
//	
//	/**
//	 * update a product type
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="StoreManager-updateProductType"
//	 * @ejb.transaction type = "Required"
//	 **/
//	public void updateProductType(ProductType productType) 
//	throws StoreException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				pm.attachCopy(productType, false);
////				pm.getExtent(ProductType.class, true);
////				try {
////					ProductType pt = (ProductType)pm.getObjectId(productType.getProductTypeID());
////					// TO_DO: implement update with detach/attach
////					// pm.attachCopy(productType, true);
////					throw new StoreException("NYI");
////				}
////				catch (JDOObjectNotFoundException e) 
////				{
////					throw new StoreException("Could not find product type whith ID " + productType.getProductTypeID(), e);
////				}
//			} finally {
//				pm.close();
//			}
//		} catch (Exception x) {
//			throw new StoreException(x);
//		}
//	}
//	
////	/**
////	 * add a product type
////	 * @throws ModuleException
////	 * 
////	 * @ejb.interface-method
////	 * @ejb.permission role-name="StoreManager-remove-producttype"
////	 * @ejb.transaction type = "Required"
////	 **/
////	public void removeProductType(String productID) 
////	throws StoreException
////	{
////		initialize();
////		pm.getExtent(ProductType.class, true);
////		try
////		{
////			ProductType pt = (ProductType)pm.getObjectId(productID);
////			pm.deletePersistent(pt);
////		}
////		catch (JDOObjectNotFoundException e) 
////		{
////			// do nothing
////		}
////	}
//
//	////////////////////// products ////////////////////////////
//	
//	/**
//	 * add a product
//	 * @throws ModuleException
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="StoreManager-add-product"
//	 * @ejb.transaction type = "Required"
//	 **/
//	public void addProduct(OldProduct product) 
//	throws StoreException
//	{
//		if (product == null)
//			throw new NullPointerException("product must not be null!");
//		
//		if(product.getProductType() == null)
//			throw new NullPointerException("product.productType must not be null!");
//
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				IDGenerator idgen = IDGeneratorUtil.getHome().create();
//				try {
//					if (product.getProductID() == null) 
//						product.setProductID(this.getOrganisationID() + "/" + new Long(idgen.generateIDLong(OldProduct.class.getName())));
//
//					ProductStatus pw = new ProductStatus(product.getProductID(), product);
//					pm.makePersistent(pw);
//				} finally {
//					idgen.remove();
//				}
//			} finally {
//				pm.close();
//			}
//		} catch (Exception e) {
//			throw new StoreException(e);
//		}
//	}
//	
//	
//	/**
//	 * update a product type
//	 * @throws ModuleException
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="StoreManager-update-product"
//	 * @ejb.transaction type = "Required"
//	 **/
//	public void updateProduct(OldProduct product) 
//	throws StoreException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				pm.attachCopy(product, false);
////				pm.getExtent(OldProduct.class, true);
////				try
////				{
////					OldProduct p = (OldProduct)pm.getObjectId(product.getProductID());
////					// TO DO: implement update with detach/attach
////					throw new StoreException("NYI");
////				}
////				catch (JDOObjectNotFoundException e) 
////				{
////					throw new StoreException("Could not find product whith ID " + product.getProductID(), e);
////				}
//			} finally {
//				pm.close();
//			}
//		} catch (Exception x) {
//			throw new StoreException(x);
//		}
//	}
//	
////	/**
////	 * remove a product
////	 * @throws ModuleException
////	 * 
////	 * @ejb.interface-method
////	 * @ejb.permission role-name="StoreManager-remove-product"
////	 * @ejb.transaction type = "Required"
////	 **/
////	public void removeProduct(String productID) 
////	throws StoreException
////	{
////		initialize();
////		pm.getExtent(OldProduct.class, true);
////		try
////		{
////			ProductStatus pw = (ProductStatus)pm.getObjectId(productID);
////			pm.deletePersistent(pw);
////		}
////		catch (JDOObjectNotFoundException e) 
////		{
////			// do nothing
////		}
////	}
//	
//	/**
//	 * remove a product
//	 * @throws ModuleException
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type = "Required"
//	 **/
//	public void test() 
//	throws StoreException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				ProductType productType = new ProductType(getOrganisationID()+"/"+"productType"+System.currentTimeMillis());
//				pm.makePersistent(productType);
//				
//				
//			} finally {
//				pm.close();
//			}
//		} catch (Exception x) {
//			throw new StoreException(x);
//		}
//	}
//	
//	////////////////////// operations ////////////////////////////
//	
//	
//	
//	////////////////////// EJB stuff ////////////////////////////
//	
//	public void ejbActivate() throws EJBException, RemoteException
//	{
////		try
////		{
////			initialize();
////		}
////		catch (StoreException e)
////		{
////			throw new EJBException(e);
////		}
//	}
//
//	public void ejbPassivate() throws EJBException, RemoteException
//	{
//		ejbRemove();
//	}
//	
//	public void ejbRemove() throws EJBException, RemoteException
//	{
//	}
//	
	////////////////////// internal ////////////////////////////

}
