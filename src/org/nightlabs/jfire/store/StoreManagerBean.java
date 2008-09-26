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
import java.io.Serializable;
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
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.idgenerator.IDNamespaceDefault;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.query.StatableQuery;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.book.DefaultLocalStorekeeperDelegate;
import org.nightlabs.jfire.store.deliver.CheckRequirementsEnvironment;
import org.nightlabs.jfire.store.deliver.CrossTradeDeliveryCoordinator;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryHelperLocal;
import org.nightlabs.jfire.store.deliver.DeliveryHelperUtil;
import org.nightlabs.jfire.store.deliver.DeliveryQueue;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryConst;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorDeliveryQueue;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorJFire;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorMailingPhysicalDefault;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorManual;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorNonDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour.ModeOfDeliveryFlavourProductTypeGroup;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour.ModeOfDeliveryFlavourProductTypeGroupCarrier;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.store.deliver.id.DeliveryQueueID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.DeliveryNoteLocalID;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ReceptionNoteID;
import org.nightlabs.jfire.store.id.RepositoryTypeID;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.jfire.store.query.ProductTransferIDQuery;
import org.nightlabs.jfire.store.query.ProductTransferQuery;
import org.nightlabs.jfire.store.search.AbstractProductTypeGroupQuery;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.OfferRequirement;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.query.OfferQuery;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.jfire.transfer.id.TransferID;
import org.nightlabs.util.CollectionUtil;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @ejb.bean name="jfire/ejb/JFireTrade/StoreManager"
 *					 jndi-name="jfire/ejb/JFireTrade/StoreManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class StoreManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
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
	}

	/**
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

	/**
	 * Initialisation method called by the organisation-init framework to set up essential things for JFireTrade's store.
	 *
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
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

			DefaultLocalStorekeeperDelegate.getDefaultLocalStorekeeperDelegate(pm);

			// create & persist instances of RepositoryType
			RepositoryType repositoryType;

			repositoryType = pm.makePersistent(new RepositoryType(RepositoryType.REPOSITORY_TYPE_ID_HOME, false));
			repositoryType.getName().setText(Locale.ENGLISH.getLanguage(), "Home");
			repositoryType.getName().setText(Locale.GERMAN.getLanguage(), "Heim");

			repositoryType = pm.makePersistent(new RepositoryType(RepositoryType.REPOSITORY_TYPE_ID_OUTSIDE, true));
			repositoryType.getName().setText(Locale.ENGLISH.getLanguage(), "Outside");
			repositoryType.getName().setText(Locale.GERMAN.getLanguage(), "Außerhalb");

			repositoryType = pm.makePersistent(new RepositoryType(RepositoryType.REPOSITORY_TYPE_ID_PARTNER, false));
			repositoryType.getName().setText(Locale.ENGLISH.getLanguage(), "Business partner");
			repositoryType.getName().setText(Locale.GERMAN.getLanguage(), "Geschäftspartner");


			Store store = Store.getStore(pm);
			Trader trader = Trader.getTrader(pm);

			LegalEntity anonymousCustomer = LegalEntity.getAnonymousLegalEntity(pm);
			CustomerGroup anonymousCustomerGroup = anonymousCustomer.getDefaultCustomerGroup();

			//		 create fundamental set of ModeOfDelivery/ModeOfDeliveryFlavour
			// manual
			ModeOfDelivery modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Personal Delivery (manually from hand to hand)");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Persönliche Lieferung (manuell von Hand zu Hand)");
			ModeOfDeliveryFlavour modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEV_ORGANISATION_ID, "manual");
			modeOfDeliveryFlavour.loadIconFromResource();
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Personal Delivery (manually from hand to hand)");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Persönliche Lieferung (manuell von Hand zu Hand)");
			modeOfDelivery = pm.makePersistent(modeOfDelivery);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
			anonymousCustomerGroup.addModeOfDelivery(modeOfDelivery);

			ModeOfDelivery modeOfDeliveryManual = modeOfDelivery;


			// nonDelivery
			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_NON_DELIVERY);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Delivery");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Nichtlieferung");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEV_ORGANISATION_ID, "nonDelivery");
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Delivery");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Nichtlieferung");
			modeOfDelivery = pm.makePersistent(modeOfDelivery);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
			ModeOfDelivery modeOfDeliveryNonDelivery = modeOfDelivery;

			// mailing.physical
			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MAILING_PHYSICAL);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Mailing delivery (physical)");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Postversand (physisch)");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEV_ORGANISATION_ID, "mailing.physical.default");
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Mailing delivery via default service provider");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Postversand via Standard-Dienstleister");
			ModeOfDeliveryFlavour modeOfDeliveryFlavourMailingPhysicalDefault = modeOfDeliveryFlavour;
//			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEV_ORGANISATION_ID, "mailing.physical.DHL");
//			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Mailing delivery via DHL");
//			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Postversand via DHL");
//			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEV_ORGANISATION_ID, "mailing.physical.UPS");
//			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Mailing delivery via UPS");
//			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Postversand via UPS");
			modeOfDelivery = pm.makePersistent(modeOfDelivery);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
			anonymousCustomerGroup.addModeOfDelivery(modeOfDelivery);

			// mailing.virtual
			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MAILING_VIRTUAL);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Virtual Delivery (online)");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Virtuelle Lieferung (online)");
//			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEV_ORGANISATION_ID, "mailing.virtual.email");
//			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Delivery by eMail");
//			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Zustellung via eMail");
//			pm.makePersistent(modeOfDelivery);
//			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
//			anonymousCustomerGroup.addModeOfDelivery(modeOfDelivery);

			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_JFIRE);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Internal Delivery");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "JFire-interne Lieferung");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(ModeOfDeliveryConst.MODE_OF_DELIVERY_FLAVOUR_ID_JFIRE);
			ModeOfDeliveryFlavour modeOfDeliveryFlavourJFire = modeOfDeliveryFlavour;
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Internal Delivery");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "JFire-interne Lieferung");
			modeOfDelivery = pm.makePersistent(modeOfDelivery);
//			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
//			anonymousCustomerGroup.addModeOfDelivery(modeOfDelivery);

			// deliveryQueue
			modeOfDelivery = new ModeOfDelivery(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_DELIVER_TO_DELIVERY_QUEUE);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Deliver to Delivery Queue");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Lieferung in Lieferwarteschlange");
			modeOfDeliveryFlavour = modeOfDelivery.createFlavour(Organisation.DEV_ORGANISATION_ID, "deliverToDeliveryQueue");
			modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Deliver to Delivery Queue");
			modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Lieferung in Lieferwarteschlange");
			modeOfDelivery = pm.makePersistent(modeOfDelivery);
			ModeOfDelivery modeOfDeliveryDeliveryQueue = modeOfDelivery;
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfDelivery(modeOfDelivery);
			anonymousCustomerGroup.addModeOfDelivery(modeOfDeliveryDeliveryQueue);

			// create some ServerDeliveryProcessor s
			ServerDeliveryProcessorManual serverDeliveryProcessorManual = ServerDeliveryProcessorManual.getServerDeliveryProcessorManual(pm);
			serverDeliveryProcessorManual.addModeOfDelivery(modeOfDeliveryManual);
			serverDeliveryProcessorManual.getName().setText(Locale.ENGLISH.getLanguage(), "Manual Delivery (no digital action)");
			serverDeliveryProcessorManual.getName().setText(Locale.GERMAN.getLanguage(), "Manuelle Lieferung (nicht-digitale Aktion)");

			ServerDeliveryProcessorNonDelivery serverDeliveryProcessorNonDelivery = ServerDeliveryProcessorNonDelivery.getServerDeliveryProcessorNonDelivery(pm);
			serverDeliveryProcessorNonDelivery.addModeOfDelivery(modeOfDeliveryNonDelivery);
			serverDeliveryProcessorNonDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Non-Delivery (delivery will be postponed)");
			serverDeliveryProcessorNonDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Nichtlieferung (Lieferung wird verschoben)");

			ServerDeliveryProcessorMailingPhysicalDefault serverDeliveryProcessorMailingPhysicalDefault = ServerDeliveryProcessorMailingPhysicalDefault.getServerDeliveryProcessorMailingPhysicalDefault(pm);
			serverDeliveryProcessorMailingPhysicalDefault.addModeOfDeliveryFlavour(modeOfDeliveryFlavourMailingPhysicalDefault);
			serverDeliveryProcessorMailingPhysicalDefault.getName().setText(Locale.ENGLISH.getLanguage(), "Physical mail via default service provider");
			serverDeliveryProcessorMailingPhysicalDefault.getName().setText(Locale.GERMAN.getLanguage(), "Postversand via Standard-Dienstleister");

			ServerDeliveryProcessorDeliveryQueue serverDeliveryProcessorDeliveryQueue = ServerDeliveryProcessorDeliveryQueue.getServerDeliveryProcessorDeliveryQueue(pm);
			serverDeliveryProcessorDeliveryQueue.addModeOfDelivery(modeOfDeliveryDeliveryQueue);

			ServerDeliveryProcessorJFire serverDeliveryProcessorJFire = ServerDeliveryProcessorJFire.getServerDeliveryProcessorJFire(pm);
			serverDeliveryProcessorJFire.addModeOfDeliveryFlavour(modeOfDeliveryFlavourJFire);
			serverDeliveryProcessorJFire.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Internal Delivery");

			// persist process definitions
			ProcessDefinition processDefinitionDeliveryNoteCustomerLocal;
			processDefinitionDeliveryNoteCustomerLocal = store.storeProcessDefinitionDeliveryNote(TradeSide.customerLocal, ProcessDefinitionAssignment.class.getResource("deliverynote/customer/local/"));
			pm.makePersistent(new ProcessDefinitionAssignment(DeliveryNote.class, TradeSide.customerLocal, processDefinitionDeliveryNoteCustomerLocal));

			ProcessDefinition processDefinitionDeliveryNoteCustomerCrossOrg;
			processDefinitionDeliveryNoteCustomerCrossOrg = store.storeProcessDefinitionDeliveryNote(TradeSide.customerCrossOrganisation, ProcessDefinitionAssignment.class.getResource("deliverynote/customer/crossorganisation/"));
			pm.makePersistent(new ProcessDefinitionAssignment(DeliveryNote.class, TradeSide.customerCrossOrganisation, processDefinitionDeliveryNoteCustomerCrossOrg));

			ProcessDefinition processDefinitionDeliveryNoteVendor;
			processDefinitionDeliveryNoteVendor = store.storeProcessDefinitionDeliveryNote(TradeSide.vendor, ProcessDefinitionAssignment.class.getResource("deliverynote/vendor/"));
			pm.makePersistent(new ProcessDefinitionAssignment(DeliveryNote.class, TradeSide.vendor, processDefinitionDeliveryNoteVendor));

			ProcessDefinition processDefinitionReceptionNoteCustomer;
			processDefinitionReceptionNoteCustomer = store.storeProcessDefinitionReceptionNote(TradeSide.customerCrossOrganisation, ProcessDefinitionAssignment.class.getResource("receptionnote/customer/crossorganisation/"));
			pm.makePersistent(new ProcessDefinitionAssignment(ReceptionNote.class, TradeSide.customerCrossOrganisation, processDefinitionReceptionNoteCustomer));

			// TODO create and persist ProcessDefinition for ReceptionNote.Vendor
			// TODO and for customerLocal

			IDNamespaceDefault idNamespaceDefault = IDNamespaceDefault.createIDNamespaceDefault(pm, getOrganisationID(), DeliveryNote.class);
			idNamespaceDefault.setCacheSizeServer(0);
			idNamespaceDefault.setCacheSizeClient(0);

			pm.makePersistent(new EditLockTypeDeliveryNote(EditLockTypeDeliveryNote.EDIT_LOCK_TYPE_ID));


			Unit unit = new Unit(Organisation.DEV_ORGANISATION_ID, IDGenerator.nextID(Unit.class), 2);
			unit.getSymbol().setText(Locale.ENGLISH.getLanguage(), "h");
			unit.getName().setText(Locale.ENGLISH.getLanguage(), "hour");
			unit.getName().setText(Locale.GERMAN.getLanguage(), "Stunde");
			pm.makePersistent(unit);

			unit = new Unit(Organisation.DEV_ORGANISATION_ID, IDGenerator.nextID(Unit.class), 3);
			unit.getSymbol().setText(Locale.ENGLISH.getLanguage(), "pcs.");
			unit.getName().setText(Locale.ENGLISH.getLanguage(), "pieces");
			unit.getSymbol().setText(Locale.GERMAN.getLanguage(), "Stk.");
			unit.getName().setText(Locale.GERMAN.getLanguage(), "Stück");
			pm.makePersistent(unit);

			unit = new Unit(Organisation.DEV_ORGANISATION_ID, IDGenerator.nextID(Unit.class), 0);
			unit.getSymbol().setText(Locale.ENGLISH.getLanguage(), "()");
			unit.getName().setText(Locale.ENGLISH.getLanguage(), "(spot-rate)");
			unit.getSymbol().setText(Locale.GERMAN.getLanguage(), "()");
			unit.getName().setText(Locale.GERMAN.getLanguage(), "(pauschal)");
			pm.makePersistent(unit);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the object-ids of all {@link Unit}s known to the organisation.
	 * <p>
	 * This method can be called by everyone, because the object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<UnitID> getUnitIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Unit.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<UnitID>((Collection<? extends UnitID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the {@link Unit}s for the specified object-ids.
	 * <p>
	 * This method can be called by everyone, because {@link Unit}s are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public List<Unit> getUnits(Collection<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, unitIDs, Unit.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the {@link DeliveryNote}s for the specified object-ids.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
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

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 */
//	public DeliveryNote getDeliveryNote(DeliveryNoteID deliveryNoteID, String[] fetchGroups, int maxFetchDepth)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			return (DeliveryNote) pm.detachCopy(pm.getObjectById(deliveryNoteID));
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * Get the {@link DeliveryNote}s' object-ids that match the criteria specified by the given queries.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<DeliveryNoteID> getDeliveryNoteIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! DeliveryNote.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (! (queries instanceof JDOQueryCollectionDecorator))
			{
				queries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(queries);
			}

			JDOQueryCollectionDecorator<AbstractJDOQuery> decoratedCollection =
				(JDOQueryCollectionDecorator<AbstractJDOQuery>) queries;

			decoratedCollection.setPersistenceManager(pm);
			Collection<DeliveryNote> deliveryNotes =
				(Collection<DeliveryNote>) decoratedCollection.executeQueries();

			return NLJDOHelper.getObjectIDSet(deliveryNotes);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public List<ProductType> getProductTypes(Set<ProductTypeID> productTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

//			List<ProductTypeLocal> productTypeLocals = new ArrayList<ProductTypeLocal>(productTypeIDs.size());
//			for (ProductTypeID productTypeID : productTypeIDs) {
//				ProductType productType = (ProductType) pm.getObjectById(productTypeID);
//				ProductTypeLocal productTypeLocal = productType.getProductTypeLocal();
//				if (productTypeLocal == null)
//					throw new IllegalStateException("productType.productTypeLocal is null: " + productType);
//
//				productTypeLocals.add(productTypeLocal);
//			}
//
//			productTypeLocals = Authority.filterSecuredObjects(
//					pm,
//					productTypeLocals,
//					getPrincipal(),
//					RoleConstants.seeProductType,
//					ResolveSecuringAuthorityStrategy.allow
//			);
//
//			List<ProductType> productTypes = new ArrayList<ProductType>(productTypeLocals.size());
//			for (ProductTypeLocal productTypeLocal : productTypeLocals) {
//				ProductType productType = productTypeLocal.getProductType();
//				if (productType == null)
//					throw new IllegalStateException("productTypeLocal.productType is null: " + productTypeLocal);
//
//				productTypes.add(pm.detachCopy(productType));
//			}
			List<ProductType> productTypes = NLJDOHelper.getObjectList(pm, productTypeIDs, ProductType.class);

			productTypes = Authority.filterIndirectlySecuredObjects(
					pm,
					productTypes,
					getPrincipal(),
					RoleConstants.seeProductType,
					ResolveSecuringAuthorityStrategy.allow);

			productTypes = (List<ProductType>) pm.detachCopyAll(productTypes);

			return productTypes;

//			return NLJDOHelper.getDetachedObjectList(pm, productTypeIDs, ProductType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws CannotPublishProductTypeException
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType, org.nightlabs.jfire.store.editConfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	public ProductTypeStatus setProductTypeStatus_published(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws CannotPublishProductTypeException
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
				return pm.detachCopy(store.getProductTypeStatusTracker(productTypeID, true).getCurrentStatus());
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws CannotConfirmProductTypeException
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType, org.nightlabs.jfire.store.editConfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	public ProductTypeStatus setProductTypeStatus_confirmed(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws CannotConfirmProductTypeException
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
				return pm.detachCopy(store.getProductTypeStatusTracker(productTypeID, true).getCurrentStatus());
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws CannotMakeProductTypeSaleableException
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType, org.nightlabs.jfire.store.editConfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	public ProductTypeStatus setProductTypeStatus_saleable(ProductTypeID productTypeID, boolean saleable, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws CannotMakeProductTypeSaleableException
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
				return pm.detachCopy(store.getProductTypeStatusTracker(productTypeID, true).getCurrentStatus());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editConfirmedProductType"
	 * @ejb.transaction type="Required"
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
				return pm.detachCopy(store.getProductTypeStatusTracker(productTypeID, true).getCurrentStatus());
		} finally {
			pm.close();
		}
	}

	/**
	 * Get mode of delivery flavours available to the given customer-group(s) and the specified product-types.
	 * <p>
	 * This method can be called by everyone, because mode of delivery flavours are not considered confidential.
	 * </p>
	 *
	 * @param productTypeIDs Instances of {@link ProductTypeID}.
	 * @param customerGroupIDs Instances of {@link org.nightlabs.jfire.trade.id.CustomerGroupID}.
	 * @param mergeMode One of {@link ModeOfDeliveryFlavour#MERGE_MODE_SUBTRACTIVE} or {@link ModeOfDeliveryFlavour#MERGE_MODE_ADDITIVE}
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ModeOfDeliveryFlavourProductTypeGroupCarrier
			getModeOfDeliveryFlavourProductTypeGroupCarrier(
					Collection<ProductTypeID> productTypeIDs,
					Collection<CustomerGroupID> customerGroupIDs,
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
			for (Iterator<ModeOfDeliveryFlavour> it = res.getModeOfDeliveryFlavours().iterator(); it.hasNext(); ) {
				ModeOfDeliveryFlavour modf = it.next();

				res_detached.addModeOfDeliveryFlavour(
						pm.detachCopy(modf));
			}

			for (Iterator<ModeOfDeliveryFlavourProductTypeGroup> it = res.getModeOfDeliveryFlavourProductTypeGroups().iterator(); it.hasNext(); ) {
				ModeOfDeliveryFlavourProductTypeGroup group = it.next();
				res_detached.addModeOfDeliveryFlavourProductTypeGroup(group);
			}

			return res_detached;
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the server delivery processors that are available for the specified mode of delivery flavour.
	 * <p>
	 * This method can be called by everyone, because server delivery processors do not contain confidential information.
	 * </p>
	 *
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<ServerDeliveryProcessor> getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
			ModeOfDeliveryFlavourID modeOfDeliveryFlavourID,
			CheckRequirementsEnvironment checkRequirementsEnvironment,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<ServerDeliveryProcessor> c = ServerDeliveryProcessor.getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
					pm, modeOfDeliveryFlavourID);

			for (ServerDeliveryProcessor pp : c) {
				pp.checkRequirements(checkRequirementsEnvironment);
			}

			// Because the checkRequirements method might have manipulated the fetch-plan, we set it again.
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates a <code>DeliveryNote</code> for all specified <code>Article</code>s. If
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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
	 */
	public DeliveryNote createDeliveryNote(
			Collection<ArticleID> articleIDs, String deliveryNoteIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryNoteEditException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Article.class);
			User user = User.getUser(pm, getPrincipal());
			Trader trader = Trader.getTrader(pm);
			Store store = trader.getStore();

			List<Article> articles = new ArrayList<Article>(articleIDs.size());
			for (Iterator<ArticleID> it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = it.next();
				Article article = (Article) pm.getObjectById(articleID);
				Offer offer = article.getOffer();
//				OfferLocal offerLocal = offer.getOfferLocal();
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

				return pm.detachCopy(deliveryNote);
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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
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
				for (Iterator<Article> it = articleContainer.getArticles().iterator(); it.hasNext(); ) {
					Article article = it.next();
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

				return pm.detachCopy(deliveryNote);
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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
	 */
	public DeliveryNote addArticlesToDeliveryNote(
			DeliveryNoteID deliveryNoteID, Collection<ArticleID> articleIDs,
			boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryNoteEditException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(DeliveryNote.class);
			pm.getExtent(Article.class);
			DeliveryNote deliveryNote = (DeliveryNote) pm.getObjectById(deliveryNoteID);
			Collection<Article> articles = new ArrayList<Article>(articleIDs.size());
			for (Iterator<ArticleID> it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = it.next();
				articles.add((Article)pm.getObjectById(articleID));
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

			return pm.detachCopy(deliveryNote);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws DeliveryNoteEditException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
	 */
	public DeliveryNote removeArticlesFromDeliveryNote(
			DeliveryNoteID deliveryNoteID, Collection<ArticleID> articleIDs,
			boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DeliveryNoteEditException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(DeliveryNote.class);
			pm.getExtent(Article.class);
			DeliveryNote deliveryNote = (DeliveryNote) pm.getObjectById(deliveryNoteID);
			Collection<Article> articles = new ArrayList<Article>(articleIDs.size());
			for (Iterator<ArticleID> it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = it.next();
				articles.add((Article)pm.getObjectById(articleID));
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

			return pm.detachCopy(deliveryNote);
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
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.deliver"
	 */
	public List<DeliveryResult> deliverBegin(List<DeliveryData> deliveryDataList)
	throws ModuleException
	{
		try {
			StoreManagerLocal storeManagerLocal = StoreManagerUtil.getLocalHome().create();

			List<DeliveryResult> resList = new ArrayList<DeliveryResult>();
			for (Iterator<DeliveryData> it = deliveryDataList.iterator(); it.hasNext(); ) {
				DeliveryData deliveryData = it.next();

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
						res = new DeliveryResult(t);
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
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.deliver"
	 */
	public DeliveryResult deliverBegin(DeliveryData deliveryData)
	throws ModuleException
	{
		return _deliverBegin(deliveryData);
	}

	/**
	 * Because this method is only available locally, it does not require any authorization and can be called by everyone
	 * (authorization is required for the remotely available <code>deliverBegin</code> methods which lead to this method being called).
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public DeliveryResult _deliverBegin(DeliveryData deliveryData)
	throws ModuleException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("_deliverBegin: *** begin ******************************************* ");
			logger.debug("_deliverBegin: IDGenerator.getOrganisationID()=" + IDGenerator.getOrganisationID());
			logger.debug("_deliverBegin: this.getOrganisationID()=" + this.getOrganisationID());
		}

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
			DeliveryResult deliverBeginServerResult = new DeliveryResult(t);

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
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
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
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<DeliveryResult> deliverEnd(List<DeliveryID> deliveryIDs, List<DeliveryResult> deliverEndClientResults, boolean forceRollback)
	throws ModuleException
	{
		try {
			StoreManagerLocal storeManagerLocal = StoreManagerUtil.getLocalHome().create();

			if (deliveryIDs.size() != deliverEndClientResults.size())
				throw new IllegalArgumentException("deliveryIDs.size() != deliverEndClientResults.size()!!!");

			List<DeliveryResult> resList = new ArrayList<DeliveryResult>();
			Iterator<DeliveryResult> itResults = deliverEndClientResults.iterator();
			for (Iterator<DeliveryID> itIDs = deliveryIDs.iterator(); itIDs.hasNext(); ) {
				DeliveryID deliveryID = itIDs.next();
				DeliveryResult deliverEndClientResult = itResults.next();

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
						res = new DeliveryResult(t);
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
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
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
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<DeliveryResult> deliverDoWork(List<DeliveryID> deliveryIDs, List<DeliveryResult> deliverDoWorkClientResults, boolean forceRollback)
	throws ModuleException
	{
		try {
			StoreManagerLocal storeManagerLocal = StoreManagerUtil.getLocalHome().create();

			if (deliveryIDs.size() != deliverDoWorkClientResults.size())
				throw new IllegalArgumentException("deliveryIDs.size() != deliverDoWorkClientResults.size()!!!");

			List<DeliveryResult> resList = new ArrayList<DeliveryResult>();
			Iterator<DeliveryResult> itResults = deliverDoWorkClientResults.iterator();
			for (Iterator<DeliveryID> itIDs = deliveryIDs.iterator(); itIDs.hasNext(); ) {
				DeliveryID deliveryID = itIDs.next();
				DeliveryResult deliverDoWorkClientResult = itResults.next();

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
						res = new DeliveryResult(t);
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
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#deliverEnd(User, DeliveryData)
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
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
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
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
			DeliveryResult deliverDoWorkServerResult = new DeliveryResult(t);

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
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#deliverEnd(User, DeliveryData)
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
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
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
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

			DeliveryResult res = deliveryHelperLocal.deliverEnd_internal(deliveryID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			if (!res.isFailed())
				AsyncInvoke.exec(new CrossTradeDeliverInvocation(deliveryID), false); // no xa necessary, because deliveryHelperLocal.deliverEnd_internal(...) used a new transaction before

			return res;

		} catch (Throwable t) {
			DeliveryResult deliverEndServerResult = new DeliveryResult(t);

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

	protected static class CrossTradeDeliverInvocation
	extends Invocation
	{
		private static final long serialVersionUID = 1L;

		private final DeliveryID deliveryID;

		/**
		 * @param deliveryID This ID references the {@link Delivery} to the end-customer-side (or next-reseller-side). Hence, this
		 *		<code>CrossTradeDeliverInvocation</code> needs to split the <code>Article</code>s contained in this delivery by
		 *		back-hand supplier and by ProductType class, obtain the {@link ProductTypeActionHandler} for them and ask it
		 *		for the correct {@link CrossTradeDeliveryCoordinator} (via {@link ProductTypeActionHandler#getCrossTradeDeliveryCoordinator()}).
		 */
		public CrossTradeDeliverInvocation(DeliveryID deliveryID)
		{
			if (deliveryID == null)
				throw new IllegalArgumentException("deliveryID must not be null!");

			this.deliveryID = deliveryID;
		}

		@Override
		@Implement
		public Serializable invoke()
				throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			String localOrganisationID = getOrganisationID();

//			Map<Class, Map<String, Map<Boolean, Set<Article>>>> productTypeClass2organisationID2articleSet = null;
			Map<CrossTradeDeliveryCoordinator, Map<String, Map<Boolean, Set<Article>>>> productTypeClass2organisationID2articleSet = null;

			Delivery delivery = (Delivery) pm.getObjectById(deliveryID);
			for (Article article : delivery.getArticles()) {
				ProductLocal productLocal = article.getProduct().getProductLocal();

				for (ProductLocal nestedProductLocal : productLocal.getNestedProductLocals()) {
					if (!localOrganisationID.equals(nestedProductLocal.getOrganisationID())) {
						// supplier of the nested Product is a remote organisation => check, whether it's already here or needs delivery
						//
						// General info about ProductLocal.quantity:
						// ProductLocal.quantity is > 0 (usually 1), if it's available (i.e. in this store AND not nested in another product)
						// ProductLocal.quantity is = 0, if it's either (here in the store AND nested in another product) or (NOT here AND NOT nested)
						// ProductLocal.quantity is < 0 (usually -1), if it's not here in the store AND nested in another product
						//
						// Info about ProductLocal.quantity at this point:
						// Because we already delivered to our customer and now asynchronously obtain the supply, the quantity is normally -1.
						// If the Product was already obtained before (e.g. because it was sold and refunded, but not given back to the supplier),
						// then it should be 0, because the container has already been assembled and during assembling, the quantity of the nested
						// product is decremented. Hence, in this case, it is here in the store, but not available (due to being part of another
						// assembled product).
						if (nestedProductLocal.getQuantity() < 0) {
							// quantity indicates that the product is not here - is it still at supplier?

							if (!(nestedProductLocal.getAnchor() instanceof Repository))
								throw new IllegalStateException("The nested product is not in a Repository, but its anchor is of type \"" + (nestedProductLocal.getAnchor() == null ? null : nestedProductLocal.getAnchor().getClass().getName()) + "\"! localArticle \"" + article.getPrimaryKey() + "\" + nestedProductLocal \"" + nestedProductLocal.getPrimaryKey() + "\"");

							Repository nestedProductLocalRepository = (Repository) nestedProductLocal.getAnchor();

							if (!nestedProductLocalRepository.getRepositoryType().isOutside())
								throw new IllegalStateException("The nested product is not outside, but has a quanity < 0! localArticle \"" + article.getPrimaryKey() + "\" + nestedProductLocal \"" + nestedProductLocal.getPrimaryKey() + "\"");

							if (!nestedProductLocalRepository.getOwner().equals(OrganisationLegalEntity.getOrganisationLegalEntity(pm, nestedProductLocal.getOrganisationID())))
								throw new IllegalStateException("The nested product is not in an outside Repository belonging to the supplier! Has it already been delivered to another customer? localArticle \"" + article.getPrimaryKey() + "\" + nestedProductLocal \"" + nestedProductLocal.getPrimaryKey() + "\"");

							if (productTypeClass2organisationID2articleSet == null)
//								productTypeClass2organisationID2articleSet = new HashMap<Class, Map<String,Map<Boolean,Set<Article>>>>();
								productTypeClass2organisationID2articleSet = new HashMap<CrossTradeDeliveryCoordinator, Map<String,Map<Boolean,Set<Article>>>>();

							ProductType nestedProductType = nestedProductLocal.getProduct().getProductType();
//							Class nestedProductTypeClass = nestedProductTypeLocal.getClass();

							if (nestedProductType.getDeliveryConfiguration() == null)
								throw new IllegalStateException("productType.deliveryConfiguration is null!!! productType \"" + nestedProductType.getPrimaryKey() + "\" localArticle \"" + article.getPrimaryKey() + "\" + nestedProductLocal \"" + nestedProductLocal.getPrimaryKey() + "\"");

							CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator = nestedProductType.getDeliveryConfiguration().getCrossTradeDeliveryCoordinator();
							if (crossTradeDeliveryCoordinator == null)
								throw new IllegalStateException("productType.deliveryConfiguration.crossTradeDeliveryCoordinator is null!!! productType \"" + nestedProductType.getPrimaryKey() + "\" deliveryConfiguration \"" + nestedProductType.getDeliveryConfiguration().getPrimaryKey() + "\" localArticle \"" + article.getPrimaryKey() + "\" + nestedProductLocal \"" + nestedProductLocal.getPrimaryKey() + "\"");

							Map<String, Map<Boolean, Set<Article>>> organisationID2articleSet = productTypeClass2organisationID2articleSet.get(crossTradeDeliveryCoordinator);
							if (organisationID2articleSet == null) {
								organisationID2articleSet = new HashMap<String, Map<Boolean,Set<Article>>>();
								productTypeClass2organisationID2articleSet.put(crossTradeDeliveryCoordinator, organisationID2articleSet);
							}

							Map<Boolean, Set<Article>> direction2articleSet = organisationID2articleSet.get(nestedProductLocal.getOrganisationID());
							if (direction2articleSet == null) {
								direction2articleSet = new HashMap<Boolean, Set<Article>>();
								organisationID2articleSet.put(nestedProductLocal.getOrganisationID(), direction2articleSet);
							}

							LegalEntity partner = OrganisationLegalEntity.getOrganisationLegalEntity(pm, nestedProductLocal.getOrganisationID());

							Boolean directionIncoming = Boolean.TRUE;
							if (article.isReversing()) // TODO this should imho never happen - maybe throw an exception?
								directionIncoming = Boolean.FALSE;

							Set<Article> articleSet = direction2articleSet.get(directionIncoming);
							if (articleSet == null) {
								articleSet = new HashSet<Article>();
								direction2articleSet.put(directionIncoming, articleSet);
							}

							// find the backhand-Article for this nestedProductLocal
							OfferRequirement backhandOfferRequirement = OfferRequirement.getOfferRequirement(pm, article.getOffer(), true);

							Offer backhandOffer = backhandOfferRequirement.getPartnerOffer(partner);
							if (backhandOffer == null)
								throw new IllegalStateException("Cannot find backhand-Offer for local Article \"" + article.getPrimaryKey() + "\" + and nestedProductLocal \"" + nestedProductLocal.getPrimaryKey() + "\" and partnerLegalEntity \"" + partner.getPrimaryKey() + "\"");

							Article backhandArticle = Article.getArticle(pm, backhandOffer, nestedProductLocal.getProduct());
							if (backhandArticle == null)
								throw new IllegalStateException("Cannot find backhand-Article for local Article \"" + article.getPrimaryKey() + "\" + and nestedProductLocal \"" + nestedProductLocal.getPrimaryKey() + "\"");

							articleSet.add(backhandArticle);
						} // if (nestedProductLocal.getQuantity() < 1) {
					}
				}
			}

			if (productTypeClass2organisationID2articleSet != null) {
//				for (Map.Entry<Class, Map<String, Map<Boolean, Set<Article>>>> me1 : productTypeClass2organisationID2articleSet.entrySet()) {
//					Class productTypeClass = me1.getKey();
//					ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(pm, productTypeClass);
//					CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator = productTypeActionHandler.getCrossTradeDeliveryCoordinator();
				for (Map.Entry<CrossTradeDeliveryCoordinator, Map<String, Map<Boolean, Set<Article>>>> me1 : productTypeClass2organisationID2articleSet.entrySet()) {
					CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator = me1.getKey();

					for (Map.Entry<String, Map<Boolean, Set<Article>>> me2 : me1.getValue().entrySet()) {
//						String organisationID = me2.getKey();

						for (Map.Entry<Boolean, Set<Article>> me3 : me2.getValue().entrySet()) {
//							Boolean directionIncoming = me3.getKey();

							Set<Article> backhandArticles = me3.getValue();

							// Because of transactional problems, crossTradeDeliveryCoordinator.performCrossTradeDelivery(...) will spawn an additional AsyncInvoke
							// In the long run, we should implement a special "fast-track-delivery" which will be used between organisations and work within one
							// transaction. See javadoc of the performCrossTradeDelivery method.
							crossTradeDeliveryCoordinator.performCrossTradeDelivery(backhandArticles);
						}
					}
				}
			}

			return null;
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
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
	 * @ejb.transaction type="Required"
	 */
	public List<DeliveryNoteID> getDeliveryNoteIDs(AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return new ArrayList<DeliveryNoteID>(DeliveryNote.getDeliveryNoteIDs(pm, vendorID, customerID, rangeBeginIdx, rangeEndIdx));
		} finally {
			pm.close();
		}
	}

	/**
	 * This method returns the delivery with the respective ID.
	 * <p>
	 * Because this method is (currently) only used when performing a delivery, it requires
	 * the role <code>org.nightlabs.jfire.store.deliver</code> to be granted.
	 * </p>
	 *
	 * @param deliveryID The ID of the delivery to be retrieved.
	 * @param fetchGroups The fetch groups to be used.
	 * @param maxFetchDepth The max fetch depth to be used.
	 * @return A detached copy of the delivery with the respective ID.
	 *
 	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.deliver"
	 * @ejb.transaction type="Required"
	 */
	public Delivery getDelivery(DeliveryID deliveryID, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Delivery delivery = (Delivery) pm.getObjectById(deliveryID);
			return pm.detachCopy(delivery);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method queries all <code>Invoice</code>s which exist between the given vendor and customer and
	 * are not yet finalized. They are ordered by invoiceID descending (means newest first).
	 *
 	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
	 * @ejb.transaction type="Required"
	 */
	@SuppressWarnings("unchecked")
	public List<DeliveryNote> getNonFinalizedDeliveryNotes(AnchorID vendorID, AnchorID customerID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return (List<DeliveryNote>) pm.detachCopyAll(DeliveryNote.getNonFinalizedDeliveryNotes(pm, vendorID, customerID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
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

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 */
//	public Set<ProductID> getProductIDs(QueryCollection<? extends AbstractProductQuery> productQueries)
//	{
//		if (productQueries == null)
//			return null;
//
//		if (!Product.class.isAssignableFrom(productQueries.getResultClass())) {
//			throw new RuntimeException("Given QueryCollection has invalid return type! " +
//					"Invalid return type= "+ productQueries.getResultClassName());
//		}
//
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(1);
//			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
//
//			if (!(productQueries instanceof JDOQueryCollectionDecorator)){
//				productQueries = new JDOQueryCollectionDecorator<AbstractProductQuery>(productQueries);
//			}
//			JDOQueryCollectionDecorator<AbstractProductQuery> queries =
//				(JDOQueryCollectionDecorator<AbstractProductQuery>) productQueries;
//
//			queries.setPersistenceManager(pm);
//
//			Collection<Product> products = (Collection<Product>) queries.executeQueries();
//
//// TODO: Implement Authority checking here - only the role is missing - the rest is just the following line. marco.
////			products = Authority.filterSecuredObjects(pm, products, getPrincipal(), roleID);
//
//			return NLJDOHelper.getObjectIDSet(products);
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<ProductTypeID> getProductTypeIDs(QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries)
	{
		if (productTypeQueries == null)
			return null;

		if (! ProductType.class.isAssignableFrom(productTypeQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ productTypeQueries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (! (productTypeQueries instanceof JDOQueryCollectionDecorator))
			{
				productTypeQueries = new JDOQueryCollectionDecorator<AbstractProductTypeQuery>(productTypeQueries);
			}
			JDOQueryCollectionDecorator<AbstractProductTypeQuery> queries =
				(JDOQueryCollectionDecorator<AbstractProductTypeQuery>) productTypeQueries;

			queries.setPersistenceManager(pm);

			Collection<ProductType> productTypes = (Collection<ProductType>) queries.executeQueries();

			productTypes = Authority.filterIndirectlySecuredObjects(
					pm,
					productTypes,
					getPrincipal(),
					RoleConstants.seeProductType,
					ResolveSecuringAuthorityStrategy.allow);

			return NLJDOHelper.getObjectIDSet(productTypes);
		} finally {
			pm.close();
		}
	}

	private Set<ProductTypeID> getInternalProductTypeIDs(QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries)
	{
		if (productTypeQueries == null)
			return null;

		if (! ProductType.class.isAssignableFrom(productTypeQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ productTypeQueries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (! (productTypeQueries instanceof JDOQueryCollectionDecorator))
			{
				productTypeQueries = new JDOQueryCollectionDecorator<AbstractProductTypeQuery>(productTypeQueries);
			}
			JDOQueryCollectionDecorator<AbstractProductTypeQuery> queries =
				(JDOQueryCollectionDecorator<AbstractProductTypeQuery>) productTypeQueries;

			queries.setPersistenceManager(pm);

			Collection<ProductType> productTypes = (Collection<ProductType>) queries.executeQueries();

			productTypes = Authority.filterIndirectlySecuredObjects(
					pm,
					productTypes,
					getPrincipal(),
					RoleConstants.seeProductType,
					ResolveSecuringAuthorityStrategy.allow);

			return NLJDOHelper.getObjectIDSet(productTypes);
		} finally {
			pm.close();
		}
	}

//	/**
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 */
//	@SuppressWarnings("unchecked")
//	public Set<ProductTypeID> getProductTypes(QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries)
//	{
//		return getProductTypeIDs(productTypeQueries);
//	}

//	/**
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 */
//	@SuppressWarnings("unchecked")
//	public Set<ProductTypeGroupID> getProductTypeGroupIDs(
//			QueryCollection<? extends AbstractProductTypeGroupQuery> productTypeGroupQueries)
//	{
//		if (productTypeGroupQueries == null)
//			return null;
//
//		if (! ProductTypeGroup.class.isAssignableFrom(productTypeGroupQueries.getResultClass()))
//		{
//			throw new RuntimeException("Given QueryCollection has invalid return type! " +
//					"Invalid return type= "+ productTypeGroupQueries.getResultClassName());
//		}
//
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(1);
//			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
//
//			if (! (productTypeGroupQueries instanceof JDOQueryCollectionDecorator))
//			{
//				productTypeGroupQueries = new JDOQueryCollectionDecorator<AbstractProductTypeGroupQuery>(productTypeGroupQueries);
//			}
//			JDOQueryCollectionDecorator<AbstractProductTypeGroupQuery> queries =
//				(JDOQueryCollectionDecorator<AbstractProductTypeGroupQuery>) productTypeGroupQueries;
//
//			queries.setPersistenceManager(pm);
//
//			Collection<ProductTypeGroup> productTypeGroups_unfiltered = (Collection<ProductTypeGroup>) queries.executeQueries();
//
//			Collection<ProductTypeGroup> productTypeGroups = new ArrayList<ProductTypeGroup>(productTypeGroups_unfiltered.size());
//			for (ProductTypeGroup productTypeGroup : productTypeGroups_unfiltered) {
//				boolean hasFilteredProductTypes = !Authority.filterIndirectlySecuredObjects(
//						pm,
//						productTypeGroup.getProductTypes(),
//						getPrincipal(),
//						RoleConstants.seeProductType,
//						ResolveSecuringAuthorityStrategy.allow).isEmpty();
//
//				if (hasFilteredProductTypes)
//					productTypeGroups.add(productTypeGroup);
//			}
//
//			return NLJDOHelper.getObjectIDSet(productTypeGroups);
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public ProductTypeGroupIDSearchResult getProductTypeGroupSearchResult(
			QueryCollection<? extends AbstractProductTypeGroupQuery> productTypeGroupQueries)
	{
		if (productTypeGroupQueries == null)
			return null;

		if (! ProductTypeGroup.class.isAssignableFrom(productTypeGroupQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ productTypeGroupQueries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setGroups(FetchPlan.DEFAULT);

			if (! (productTypeGroupQueries instanceof JDOQueryCollectionDecorator))
			{
				productTypeGroupQueries = new JDOQueryCollectionDecorator<AbstractProductTypeGroupQuery>(productTypeGroupQueries);
			}
			JDOQueryCollectionDecorator<AbstractProductTypeGroupQuery> queries =
				(JDOQueryCollectionDecorator<AbstractProductTypeGroupQuery>) productTypeGroupQueries;

			queries.setPersistenceManager(pm);

			Collection<ProductTypeGroup> productTypeGroups = (Collection<ProductTypeGroup>) queries.executeQueries();

			ProductTypeGroupIDSearchResult result = new ProductTypeGroupIDSearchResult();
			for (Iterator<? extends ProductTypeGroup> iter = productTypeGroups.iterator(); iter.hasNext();) {
				ProductTypeGroup group = iter.next();
				ProductTypeGroupID groupID = (ProductTypeGroupID) JDOHelper.getObjectId(group);
				result.addEntry(groupID);
				for (Iterator<ProductType> iterator = group.getProductTypes().iterator(); iterator.hasNext();) {
					ProductType type = iterator.next();
					ProductTypeID typeID = (ProductTypeID) JDOHelper.getObjectId(type);
					if (Authority.resolveSecuringAuthority(pm, type.getProductTypeLocal(), ResolveSecuringAuthorityStrategy.allow).containsRoleRef(getPrincipal(), RoleConstants.seeProductType))
						result.addType(groupID, typeID);
				}
			}
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns the {@link DeliveryQueue}s identified by the given IDs.
	 * <p>
	 * Because the delivery queues used (and even visible) to a user are configured in a config module,
	 * there is at the moment no authorization required for this method (though, authentication is
	 * necessary - i.e this method cannot be called anonymously). This may change later (we may implement
	 * {@link SecuredObject} in {@link DeliveryQueue}).
	 * </p>
	 *
	 * @param deliveryQueueIds The IDs of the DeliveryQueues to be returned.
	 * @param fetchGroups The fetch groups to be used to detach the DeliveryQueues
	 * @param fetchDepth The fetch depth to be used to detach the DeliveryQueues (-1 for unlimited)
	 * @return the {@link DeliveryQueue}s identified by the given IDs.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection<DeliveryQueue> getDeliveryQueuesById(Set<DeliveryQueueID> deliveryQueueIds, String[] fetchGroups, int fetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setFetchSize(fetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(pm.getObjectsById(deliveryQueueIds));
		} finally {
			pm.close();
		}
	}

	/**
	 * Stores the given DeliveryQueues in the data store.
	 *
	 * @param deliveryQueues The {@link DeliveryQueue} to be stored
	 * @param get Indicates whether this method should return a collection containing the detached copies of the stored DeliveryQueues.
	 * @param fetchGroups The fetchGroups to be used when get == true
	 * @param fetchDepth The fetchDepth to be used when get == true
	 * @return A collection of the detached copies of the stored {@link DeliveryQueue}s
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryQueue"
	 */
	public Collection<DeliveryQueue> storeDeliveryQueues(Collection<DeliveryQueue> deliveryQueues, boolean get, String[] fetchGroups, int fetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setFetchSize(fetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			deliveryQueues = pm.makePersistentAll(deliveryQueues);

			if (get)
				return pm.detachCopyAll(deliveryQueues);
			else
				return null;
		} finally {
			pm.close();
		}
	}

//	/**
//	 * Returns all {@link DeliveryQueue}s available.
//	 * @return All {@link DeliveryQueue}s available.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 *
//	 * @param fetchGroups The desired fetch groups
//	 * @param fetchDepth The desired JDO fetch depth
//	 * @param includeDeleted Determines whether delivery queues marked as deleted are also returned.
//	 */
//	public Collection<DeliveryQueue> getAvailableDeliveryQueues(String[] fetchGroups, int fetchDepth, boolean includeDeleted) {
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(fetchDepth);
//
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			return pm.detachCopyAll(DeliveryQueue.getDeliveryQueues(pm, includeDeleted));
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * Returns the {@link DeliveryQueueID}s of all {@link DeliveryQueue}s available.
	 * @return The {@link DeliveryQueueID}s of all {@link DeliveryQueue}s available.
	 *
	 * @param includeDefunct Sets whether defunct delivery queues should be included in the returned collection.
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection<DeliveryQueueID> getAvailableDeliveryQueueIDs(boolean includeDefunct) {
		PersistenceManager pm = getPersistenceManager();
		try {
//			return NLJDOHelper.getDetachedQueryResultAsList(pm, DeliveryQueue.getDeliveryQueueIDs(pm, includeDefunct));
			return new ArrayList<DeliveryQueueID>(DeliveryQueue.getDeliveryQueueIDs(pm, includeDefunct));
		} finally {
			pm.close();
		}
	}

//	/**
//	 * Stores the given {@link DeliveryQueue}.
//	 * @param pq The {@link DeliveryQueue} to be stored.
//	 * @return A detached copy of the persisted {@link DeliveryQueue}.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public DeliveryQueue storeDeliveryQueue(DeliveryQueue pq) {
//		List<DeliveryQueue> tmp = new LinkedList<DeliveryQueue>();
//		tmp.add(pq);
//		return storeDeliveryQueues(tmp).get(0);
//	}
//
//	/**
//	 * Stores all {@link DeliveryQueue}s in the given collection.
//	 * @param deliveryQueues The collection of the {@link DeliveryQueue}s to be stored.
//	 * @return A list of detached copies of the stored delivery queues.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public List<DeliveryQueue> storeDeliveryQueues(Collection<DeliveryQueue> deliveryQueues) {
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			deliveryQueues = storeDeliveryQueues(deliveryQueues, pm);
//			return (List<DeliveryQueue>) pm.detachCopyAll(deliveryQueues);
//		} finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * Returns all {@link DeliveryQueue}s available without the ones that have been marked as deleted.
//	 * @return All {@link DeliveryQueue}s available without the ones that have been marked as deleted.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 *
//	 * @param fetchGroups The desired fetch groups
//	 * @param fetchDepth The desired JDO fetch depth
//	 */
//	public Collection<DeliveryQueue> getAvailableDeliveryQueues(String[] fetchGroups, int fetchDepth) {
//		return getAvailableDeliveryQueues(fetchGroups, fetchDepth, false);
//	}
//
//	private List<DeliveryQueue> storeDeliveryQueues(Collection<DeliveryQueue> deliveryQueues, PersistenceManager pm) {
//		List<DeliveryQueue> pqs = new LinkedList<DeliveryQueue>();
//		for (DeliveryQueue clientPQ : deliveryQueues) {
//			logger.debug("TicketingManagerBean.storeDeliveryQueue: Storing deliveryQueue " + clientPQ.getName().getText());
//			clientPQ = pm.makePersistent(clientPQ);
//			pqs.add(clientPQ);
//		}
//
//		return pqs;
//	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryRepositories"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<AnchorID> getRepositoryIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! Repository.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (! (queries instanceof JDOQueryCollectionDecorator))
			{
				queries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(queries);
			}
			JDOQueryCollectionDecorator<AbstractJDOQuery> repoQueries =
				(JDOQueryCollectionDecorator<AbstractJDOQuery>) queries;

			repoQueries.setPersistenceManager(pm);
			Collection<? extends Repository> repositories =
				(Collection<? extends Repository>) repoQueries.executeQueries();

			return NLJDOHelper.getObjectIDSet(repositories);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryRepositories"
	 */
	@SuppressWarnings("unchecked")
	public List<Repository> getRepositories(Collection<AnchorID> repositoryIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, repositoryIDs, Repository.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editRepository"
	 */
	public Repository storeRepository(Repository repository, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, repository, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * This method is faster than {@link #getProductTransferIDs(Collection)}, because
//	 * it directly queries object-ids.
//	 *
//	 * @param productTransferIDQuery The query to execute.
//	 *
//	 * @ejb.interface-method
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public List<TransferID> getProductTransferIDs(ProductTransferIDQuery productTransferIDQuery)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			productTransferIDQuery.setPersistenceManager(pm);
//			Collection<TransferID> transferIDs = CollectionUtil.castCollection(productTransferIDQuery.getResult());
//			return new ArrayList<TransferID>(transferIDs);
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * Unlike {@link #getProductTransferIDs(ProductTransferIDQuery)}, this method allows
	 * for cascading multiple queries. It is slower than {@link #getProductTransferIDs(ProductTransferIDQuery)}
	 * and should therefore only be used, if it's essentially necessary.
	 *
	 * @param productTransferQueries A <code>Collection</code> of {@link ProductTransferQuery}. They will be executed
	 *		in the given order (if it's a <code>List</code>) and the result of the previous query will be passed as candidates
	 *		to the next query.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryProductTransfers"
	 */
	public List<TransferID> getProductTransferIDs(QueryCollection<? extends ProductTransferQuery> productTransferQueries)
	{
		if (productTransferQueries == null)
			throw new IllegalArgumentException("productTransferQueries must not be null!");

		if (productTransferQueries.isEmpty())
			return new ArrayList<TransferID>(0);

		PersistenceManager pm = getPersistenceManager();
		try {
			if (ProductTransfer.class.isAssignableFrom(productTransferQueries.getResultClass())) {
//				Collection<ProductTransfer> productTransfers = null;
//				for (ProductTransferQuery productTransferQuery : productTransferQueries) {
//					productTransferQuery.setPersistenceManager(pm);
//					productTransferQuery.setCandidates(productTransfers);
//					productTransfers = CollectionUtil.castCollection(productTransferQuery.getResult());
//				}
//				return NLJDOHelper.getObjectIDList(productTransfers);

				JDOQueryCollectionDecorator<? extends ProductTransferQuery> decorator = new JDOQueryCollectionDecorator<ProductTransferQuery>(productTransferQueries);
				decorator.setPersistenceManager(pm);
				return NLJDOHelper.getObjectIDList(decorator.executeQueries());
			}
			else if (TransferID.class.isAssignableFrom(productTransferQueries.getResultClass())) {
				if (productTransferQueries.size() != 1)
					throw new IllegalArgumentException("productTransferQueries has result-class TransferID, but contains more than 1 query. Since a query returning object-ids is not cascadable, this is illegal!");

				JDOQueryCollectionDecorator<? extends ProductTransferQuery> decorator = new JDOQueryCollectionDecorator<ProductTransferQuery>(productTransferQueries);
				decorator.setPersistenceManager(pm);
				Collection<TransferID> productTransferIDs = CollectionUtil.castCollection(decorator.executeQueries());
				return new ArrayList<TransferID>(productTransferIDs);
			}
			else
				throw new RuntimeException("Given QueryCollection has invalid return type! Invalid return type: "+ productTransferQueries.getResultClassName());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryProductTransfers"
	 */
	public List<ProductTransfer> getProductTransfers(Collection<TransferID> productTransferIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, productTransferIDs, ProductTransfer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the object-ids of all {@link RepositoryType}s.
	 * <p>
	 * This method can be called by everyone, because the object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<RepositoryTypeID> getRepositoryTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(RepositoryType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<RepositoryTypeID>((Collection<? extends RepositoryTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the <code>RepositoryType</code>s for the specified object-ids.
	 * <p>
	 * This method can be called by everyone, because the <code>RepositoryType</code>s are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public List<RepositoryType> getRepositoryTypes(Collection<RepositoryTypeID> repositoryTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, repositoryTypeIDs, RepositoryType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @param productTypeGroupID The ID of the desired <code>ProductTypeGroup</code>.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.transaction type="Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public ProductTypeGroup getProductTypeGroup(ProductTypeGroupID productTypeGroupID,
//			String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			pm.getExtent(ProductTypeGroup.class);
//			return (ProductTypeGroup) pm.detachCopy(pm.getObjectById(productTypeGroupID));
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * @param productTypeGroupIDs Either <code>null</code> in order to return all or instances of {@link ProductTypeGroupID}
	 *		specifying a subset.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 */
	public Collection<ProductTypeGroup> getProductTypeGroups(Collection<ProductTypeGroupID>
		productTypeGroupIDs, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

			pm.getExtent(ProductTypeGroup.class);
			ArrayList<ProductTypeGroup> productTypeGroups = new ArrayList<ProductTypeGroup>(productTypeGroupIDs.size());
			for (ProductTypeGroupID productTypeGroupID : productTypeGroupIDs) {
				ProductTypeGroup productTypeGroup = (ProductTypeGroup) pm.getObjectById(productTypeGroupID);
				// The method ProductTypeGroup.getProductTypes() already filters the product-types by their
				// authorities. Therefore, we simply suppress those groups that contain no product type anymore after filtering.
				if (!productTypeGroup.getProductTypes().isEmpty())
					productTypeGroups.add(productTypeGroup);
			}

			return pm.detachCopyAll(productTypeGroups);
		} finally {
			pm.close();
		}
	}

	// TODO: when all jpox bugs are fixed, implement generic storeProductType-Method
//	/**
//	 * @return Returns a newly detached instance of <tt>ProductType</tt>
//	 * if <tt>get</tt> is true - otherwise <tt>null</tt>.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public ProductType storeProductType(ProductType productType, boolean get, String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//		if (productType == null)
//			throw new NullPointerException("productType");
//
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups == null)
//				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
//			else
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			boolean priceCalculationNeeded = false;
//			if (NLJDOHelper.exists(pm, productType)) {
//				// if the nestedProductTypes changed, we need to recalculate prices
//				// test first, whether they were detached
//				Map<String, NestedProductTypeLocal> newNestedProductTypes = new HashMap<String, NestedProductTypeLocal>();
//				try {
//					for (NestedProductTypeLocal npt : productType.getNestedProductTypes()) {
//						newNestedProductTypes.put(npt.getInnerProductTypePrimaryKey(), npt);
//						npt.getQuantity();
//					}
//				} catch (JDODetachedFieldAccessException x) {
//					newNestedProductTypes = null;
//				}
//
//				if (newNestedProductTypes != null) {
//					ProductType original = (ProductType) pm.getObjectById(JDOHelper.getObjectId(productType));
//					priceCalculationNeeded = !ProductType.compareNestedProductTypes(original.getNestedProductTypes(), newNestedProductTypes);
//				}
//
//				productType = (ProductType) pm.makePersistent(productType);
//			}
//			else {
//				// TODO: ProductTypeActionHandler.getDefaultHome() should be abstract
//				// and not static in implementation to make generic implementation possible
//				productType = (ProductType) Store.getStore(pm).addProductType(
//						User.getUser(pm, getPrincipal()),
//						productType,
//						ProductTypeActionHandler.getProductTypeActionHandler(
//								pm, productType.getClass().getDefaultHome(pm, productType)));
//
//				// make sure the prices are correct
//				priceCalculationNeeded = true;
//			}
//
//			if (priceCalculationNeeded) {
//				logger.info("storeProductType: price-calculation is necessary! Will recalculate the prices of " + JDOHelper.getObjectId(productType));
//				if (productType.getPackagePriceConfig() != null && productType.getInnerPriceConfig() != null) {
//					((IResultPriceConfig)productType.getPackagePriceConfig()).adoptParameters(
//							productType.getInnerPriceConfig());
//				}
//
//				// find out which productTypes package this one and recalculate their prices as well - recursively! siblings are automatically included in the package-recalculation
//				HashSet<ProductTypeID> processedProductTypeIDs = new HashSet<ProductTypeID>();
//				ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
//				for (AffectedProductType apt : PriceConfigUtil.getAffectedProductTypes(pm, productType)) {
//					if (!processedProductTypeIDs.add(apt.getProductTypeID()))
//						continue;
//
//					ProductType pt;
//					if (apt.getProductTypeID().equals(productTypeID))
//						pt = productType;
//					else
//						pt = (ProductType) pm.getObjectById(apt.getProductTypeID());
//
//					if (ProductType.PACKAGE_NATURE_OUTER == pt.getPackageNature() && pt.getPackagePriceConfig() != null) {
//						logger.info("storeProductType: price-calculation starting for: " + JDOHelper.getObjectId(pt));
//
//						PriceCalculator priceCalculator = new PriceCalculator(pt, new CustomerGroupMapper(pm), new TariffMapper(pm));
//						priceCalculator.preparePriceCalculation();
//						priceCalculator.calculatePrices();
//
//						logger.info("storeProductType: price-calculation complete for: " + JDOHelper.getObjectId(pt));
//					}
//				}
//			}
//			else
//				logger.info("storeProductType: price-calculation is NOT necessary! Stored ProductType without recalculation: " + JDOHelper.getObjectId(productType));
//
//			// take care about the inheritance
//			productType.applyInheritance();
//			// imho, the recalculation of the prices for the inherited ProductTypes is already implemented in JFireTrade. Marco.
//
//			if (!get)
//				return null;
//
//			return (ProductType) pm.detachCopy(productType);
//		} finally {
//			pm.close();
//		}
//	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryReceptionNotes"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<ReceptionNoteID> getReceptionNoteIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! ReceptionNote.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<AbstractJDOQuery> decoratedQueries;

			if (queries instanceof JDOQueryCollectionDecorator)
			{
				decoratedQueries = (JDOQueryCollectionDecorator<AbstractJDOQuery>) queries;
			}
			else
			{
				decoratedQueries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(queries);
			}

			decoratedQueries.setPersistenceManager(pm);
			Collection<ReceptionNote> receptionNotes =
				(Collection<ReceptionNote>) decoratedQueries.executeQueries();

			return NLJDOHelper.getObjectIDSet(receptionNotes);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryReceptionNotes"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public List<ReceptionNote> getReceptionNotes(Set<ReceptionNoteID> receptionNoteIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, receptionNoteIDs, ReceptionNote.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<OfferID> getReservations(ProductTypeID productTypeID, String fetchGroups, int maxFetchDepth)
	{
		// TODO; Maybe needs other role
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			QueryCollection<AbstractSearchQuery> queryCollection = new QueryCollection<AbstractSearchQuery>(Offer.class);
			OfferQuery offerQuery = new OfferQuery();
			offerQuery.setProductTypeID(productTypeID);
			queryCollection.add(offerQuery);

			StatableQuery statableQuery = new StatableQuery();
			statableQuery.setStatableClass(Offer.class);
			// TODO set finalized as criteria for StatableQuery
			queryCollection.add(statableQuery);

			if (! (queryCollection instanceof JDOQueryCollectionDecorator))
			{
				queryCollection = new JDOQueryCollectionDecorator<AbstractSearchQuery>(queryCollection);
			}
			JDOQueryCollectionDecorator<AbstractSearchQuery> queries =
				(JDOQueryCollectionDecorator<AbstractSearchQuery>) queryCollection;

			queries.setPersistenceManager(pm);

			Collection<ProductType> productTypes = (Collection<ProductType>) queries.executeQueries();

			productTypes = Authority.filterIndirectlySecuredObjects(
					pm,
					productTypes,
					getPrincipal(),
					RoleConstants.seeProductType,
					ResolveSecuringAuthorityStrategy.allow);

			return NLJDOHelper.getObjectIDSet(productTypes);
		} finally {
			pm.close();
		}
	}
}
