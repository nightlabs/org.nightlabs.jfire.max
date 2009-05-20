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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.idgenerator.IDNamespaceDefault;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.query.StatableQuery;
import org.nightlabs.jfire.multitxjob.MultiTxJob;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.book.DefaultLocalStorekeeperDelegate;
import org.nightlabs.jfire.store.deliver.CheckRequirementsEnvironment;
import org.nightlabs.jfire.store.deliver.CrossTradeDeliveryCoordinator;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryHelperBean;
import org.nightlabs.jfire.store.deliver.DeliveryHelperLocal;
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
import org.nightlabs.jfire.store.deliver.DeliveryHelperBean.ConsolidateProductReferencesInvocation;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour.ModeOfDeliveryFlavourProductTypeGroup;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour.ModeOfDeliveryFlavourProductTypeGroupCarrier;
import org.nightlabs.jfire.store.deliver.config.ModeOfDeliveryConfigModule;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.store.deliver.id.DeliveryQueueID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ProductTypeActionHandlerID;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.jfire.store.id.ReceptionNoteID;
import org.nightlabs.jfire.store.id.RepositoryTypeID;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.jfire.store.prop.DeliveryNoteStruct;
import org.nightlabs.jfire.store.prop.ReceptionNoteStruct;
import org.nightlabs.jfire.store.query.ProductTransferQuery;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.jfire.store.search.ProductTypeIDTreeNode;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class StoreManagerBean
extends BaseSessionBeanImpl
implements StoreManagerRemote, StoreManagerLocal
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(StoreManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			initRegisterConfigModules(pm);

			initTimerTaskCalculateProductTypeAvailabilityPercentage(pm);

			pm.getExtent(ModeOfDelivery.class);
			try {
				pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);

				// it already exists, hence initialization is already done
				return;
			} catch (JDOObjectNotFoundException x) {
				// not yet initialized
			}

			pm.getExtent(ProductTypePermissionFlagSet.class); // We must already initialize the meta data here, because otherwise we run into deadlocks.

			SecurityChangeListenerProductTypePermission.register(pm);

			DefaultLocalStorekeeperDelegate.getDefaultLocalStorekeeperDelegate(pm);

			// Initalise standard property set structures for articleContainers
			DeliveryNoteStruct.getDeliveryNoteStructLocal(pm);
			ReceptionNoteStruct.getReceptionNoteStructLocal(pm);

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
	 * Called by {@link #initialise()} and registeres the
	 * config-modules in their config-setup.
	 *
	 * This method checks itself whether initialisation
	 * was performed already and therefore can be safely
	 * called anytime in the process.
	 */
	private void initRegisterConfigModules(PersistenceManager pm)
	{
		boolean needsUpdate = false;
		// Register all User - ConfigModules
		ConfigSetup configSetup = ConfigSetup.getConfigSetup(
				pm,
				getOrganisationID(),
				UserConfigSetup.CONFIG_SETUP_TYPE_USER
			);
		if (!configSetup.getConfigModuleClasses().contains(ModeOfDeliveryConfigModule.class.getName())) {
			configSetup.getConfigModuleClasses().add(ModeOfDeliveryConfigModule.class.getName());
			needsUpdate = true;
		}

		// Register all Workstation - ConfigModules
		configSetup = ConfigSetup.getConfigSetup(
				pm,
				getOrganisationID(),
				WorkstationConfigSetup.CONFIG_SETUP_TYPE_WORKSTATION
			);
		if (!configSetup.getConfigModuleClasses().contains(ModeOfDeliveryConfigModule.class.getName())) {
			configSetup.getConfigModuleClasses().add(ModeOfDeliveryConfigModule.class.getName());
			needsUpdate = true;
		}
		if (needsUpdate)
			ConfigSetup.ensureAllPrerequisites(pm);
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getUnitIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<UnitID> getUnitIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Unit.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<? extends UnitID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<UnitID>(c);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getUnits(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<Unit> getUnits(Collection<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, unitIDs, Unit.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getDeliveryNotes(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryDeliveryNotes")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getDeliveryNoteIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryDeliveryNotes")
	@SuppressWarnings("unchecked")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getProductTypes(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#setProductTypeStatus_published(org.nightlabs.jfire.store.id.ProductTypeID, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"org.nightlabs.jfire.store.editUnconfirmedProductType", "org.nightlabs.jfire.store.editConfirmedProductType"})
	@Override
	public ProductTypeStatusHistoryItem setProductTypeStatus_published(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
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
			ProductTypeStatusHistoryItem productTypeStatusHistoryItem = store.setProductTypeStatus_published(User.getUser(pm, getPrincipal()), productType);

			if (productTypeStatusHistoryItem == null || !get)
				return null;
			else
				return pm.detachCopy(productTypeStatusHistoryItem);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#setProductTypeStatus_confirmed(org.nightlabs.jfire.store.id.ProductTypeID, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"org.nightlabs.jfire.store.editUnconfirmedProductType", "org.nightlabs.jfire.store.editConfirmedProductType"})
	@Override
	public ProductTypeStatusHistoryItem setProductTypeStatus_confirmed(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
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
			ProductTypeStatusHistoryItem productTypeStatusHistoryItem = store.setProductTypeStatus_confirmed(User.getUser(pm, getPrincipal()), productType);

			if (productTypeStatusHistoryItem == null || !get)
				return null;
			else
				return pm.detachCopy(productTypeStatusHistoryItem);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#setProductTypeStatus_saleable(org.nightlabs.jfire.store.id.ProductTypeID, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"org.nightlabs.jfire.store.editUnconfirmedProductType", "org.nightlabs.jfire.store.editConfirmedProductType"})
	@Override
	public ProductTypeStatusHistoryItem setProductTypeStatus_saleable(ProductTypeID productTypeID, boolean saleable, boolean get, String[] fetchGroups, int maxFetchDepth)
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
			ProductTypeStatusHistoryItem productTypeStatusHistoryItem = store.setProductTypeStatus_saleable(User.getUser(pm, getPrincipal()), productType, saleable);

			if (productTypeStatusHistoryItem == null || !get)
				return null;
			else
				return pm.detachCopy(productTypeStatusHistoryItem);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#setProductTypeStatus_closed(org.nightlabs.jfire.store.id.ProductTypeID, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editConfirmedProductType")
	@Override
	public ProductTypeStatusHistoryItem setProductTypeStatus_closed(ProductTypeID productTypeID, boolean get, String[] fetchGroups, int maxFetchDepth)
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
			ProductTypeStatusHistoryItem productTypeStatusHistoryItem = store.setProductTypeStatus_closed(User.getUser(pm, getPrincipal()), productType);

			if (productTypeStatusHistoryItem == null || !get)
				return null;
			else
				return pm.detachCopy(productTypeStatusHistoryItem);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getModeOfDeliveryFlavourProductTypeGroupCarrier(java.util.Collection, java.util.Collection, byte, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ModeOfDeliveryFlavourProductTypeGroupCarrier
			getModeOfDeliveryFlavourProductTypeGroupCarrier(
					Collection<ProductTypeID> productTypeIDs,
					Collection<CustomerGroupID> customerGroupIDs,
					byte mergeMode, boolean filterByConfig,
					String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ModeOfDeliveryFlavourProductTypeGroupCarrier res =
				ModeOfDeliveryFlavour.getModeOfDeliveryFlavourProductTypeGroupCarrier(
						pm, productTypeIDs, customerGroupIDs, mergeMode, filterByConfig);

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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getModeOfDeliverys(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ModeOfDelivery> getModeOfDeliverys(Set<ModeOfDeliveryID> modeOfDeliveryIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, modeOfDeliveryIDs, ModeOfDelivery.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getAllModeOfDeliveryFlavourIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<ModeOfDeliveryFlavourID> getAllModeOfDeliveryFlavourIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			return ModeOfDeliveryFlavour.getAllModeOfDeliveryFlavourIDs(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getAllModeOfDeliveryIDs()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Set<ModeOfDeliveryID> getAllModeOfDeliveryIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			return ModeOfDelivery.getAllModeOfDeliveryIDs(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getModeOfDeliveryFlavours(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours(Set<ModeOfDeliveryFlavourID> modeOfDeliveryFlavourIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, modeOfDeliveryFlavourIDs, ModeOfDeliveryFlavour.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID, org.nightlabs.jfire.store.deliver.CheckRequirementsEnvironment, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#createDeliveryNote(java.util.Collection, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#createDeliveryNote(org.nightlabs.jfire.trade.id.ArticleContainerID, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	@Override
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
//				OfferLocal offerLocal = offer.getOfferLocal();
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
//					OfferLocal offerLocal = offer.getOfferLocal();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#addArticlesToDeliveryNote(org.nightlabs.jfire.store.id.DeliveryNoteID, java.util.Collection, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#removeArticlesFromDeliveryNote(org.nightlabs.jfire.store.id.DeliveryNoteID, java.util.Collection, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	@Override
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

	@EJB
	private StoreManagerLocal storeManagerLocal;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#deliverBegin(java.util.List)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.deliver")
	@Override
	public List<DeliveryResult> deliverBegin(List<DeliveryData> deliveryDataList)
	{
		try {
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
		} catch(RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#deliverInSingleTransaction(org.nightlabs.jfire.store.deliver.DeliveryData)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.deliver")
	@Override
	public DeliveryResult[] deliverInSingleTransaction(DeliveryData deliveryData)
	throws DeliveryException
	{
		if (deliveryData == null)
			throw new IllegalArgumentException("deliveryData == null");

		{
			Delivery delivery = deliveryData.getDelivery();
			if (delivery == null)
				throw new IllegalArgumentException("deliveryData.getDelivery() == null");

			if (delivery.getDeliverBeginClientResult() == null)
				throw new IllegalArgumentException("deliveryData.getDelivery().getDeliverBeginClientResult() == null");

			if (delivery.getDeliverDoWorkClientResult() == null)
				throw new IllegalArgumentException("deliveryData.getDelivery().getDeliverDoWorkClientResult() == null");

			if (delivery.getDeliverEndClientResult() == null)
				throw new IllegalArgumentException("deliveryData.getDelivery().getDeliverEndClientResult() == null");
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			DeliveryResult[] result = new DeliveryResult[3];

			User user = User.getUser(pm, getPrincipal());
			deliveryData = DeliveryHelperBean.deliverBegin_storeDeliveryData(pm, user, deliveryData);
			DeliveryID deliveryID = (DeliveryID) JDOHelper.getObjectId(deliveryData.getDelivery());
			if (deliveryID == null)
				throw new IllegalStateException("JDOHelper.getObjectId(deliveryData.getDelivery()) returned null!");

			DeliveryResult deliverBeginServerResult = Store.getStore(pm).deliverBegin(user, deliveryData);
			deliverBeginServerResult = pm.makePersistent(deliverBeginServerResult);
			deliveryData.getDelivery().setDeliverBeginServerResult(deliverBeginServerResult);
			result[0] = deliverBeginServerResult;
			if (deliverBeginServerResult.isFailed())
				throw new DeliveryException(deliverBeginServerResult);

			DeliveryResult deliverDoWorkServerResult = Store.getStore(pm).deliverDoWork(user, deliveryData);
			deliverDoWorkServerResult = pm.makePersistent(deliverDoWorkServerResult);
			deliveryData.getDelivery().setDeliverDoWorkServerResult(deliverDoWorkServerResult);
			result[1] = deliverDoWorkServerResult;
			if (deliverDoWorkServerResult.isFailed())
				throw new DeliveryException(deliverDoWorkServerResult);

			DeliveryResult deliverEndServerResult = Store.getStore(pm).deliverEnd(user, deliveryData);

			if (deliveryData.getDelivery().isFailed()) { // FIXME this is already called within deliverEnd - should it really be called twice?!?! Marco.
				Store.getStore(pm).deliverRollback(user, deliveryData);
			}

			deliverEndServerResult = pm.makePersistent(deliverEndServerResult);
			deliveryData.getDelivery().setDeliverEndServerResult(deliverEndServerResult);
			result[2] = deliverEndServerResult;
			if (deliverEndServerResult.isFailed())
				throw new DeliveryException(deliverEndServerResult);

			Collection<DeliveryNoteID> deliveryNoteIDs = deliveryData.getDelivery().getDeliveryNoteIDs();
			try {
				AsyncInvoke.exec(new CrossTradeDeliverInvocation(deliveryID), true);
				AsyncInvoke.exec(new ConsolidateProductReferencesInvocation(deliveryNoteIDs, 5000), true);
			} catch (AsyncInvokeEnqueueException e) {
				throw new RuntimeException(e);
			}

			return result;
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#deliverBegin(org.nightlabs.jfire.store.deliver.DeliveryData)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.deliver")
	@Override
	public DeliveryResult deliverBegin(DeliveryData deliveryData)
	{
		return _deliverBegin(deliveryData);
	}

	@EJB
	private DeliveryHelperLocal deliveryHelperLocal;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerLocal#_deliverBegin(org.nightlabs.jfire.store.deliver.DeliveryData)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public DeliveryResult _deliverBegin(DeliveryData deliveryData)
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
		DeliveryDataID deliveryDataID = deliveryHelperLocal.deliverBegin_storeDeliveryData(deliveryData);

		String[] fetchGroups = new String[] {FetchPlan.DEFAULT};

		try {

			return deliveryHelperLocal.deliverBegin_internal(deliveryDataID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		} catch (Throwable t) {
			DeliveryResult deliverBeginServerResult = new DeliveryResult(t);

			try {
				return deliveryHelperLocal.deliverBegin_storeDeliverBeginServerResult(
						DeliveryID.create(deliveryDataID), deliverBeginServerResult, true, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#deliverEnd(java.util.List, java.util.List, boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<DeliveryResult> deliverEnd(List<DeliveryID> deliveryIDs, List<DeliveryResult> deliverEndClientResults, boolean forceRollback)
	{
		try {
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
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#deliverDoWork(java.util.List, java.util.List, boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<DeliveryResult> deliverDoWork(List<DeliveryID> deliveryIDs, List<DeliveryResult> deliverDoWorkClientResults, boolean forceRollback)
	{
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
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#deliverDoWork(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public DeliveryResult deliverDoWork(
			DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult,
			boolean forceRollback)
	{
		return _deliverDoWork(deliveryID, deliverEndClientResult, forceRollback);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerLocal#_deliverDoWork(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public DeliveryResult _deliverDoWork(
			DeliveryID deliveryID,
			DeliveryResult deliverDoWorkClientResult,
			boolean forceRollback)
	{
		if (deliveryID == null)
			throw new NullPointerException("deliveryID");

		if (deliverDoWorkClientResult == null)
			throw new NullPointerException("deliverDoWorkClientResult");

		// Store deliverDoWorkClientResult into the database within a NEW TRANSACTION to
		// prevent it from being lost (if this method fails later and causes a rollback).
		try {
			deliveryHelperLocal.deliverDoWork_storeDeliverDoWorkClientResult(
					deliveryID, deliverDoWorkClientResult, forceRollback
			);
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
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
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#deliverEnd(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public DeliveryResult deliverEnd(
			DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult,
			boolean forceRollback)
	{
		return _deliverEnd(deliveryID, deliverEndClientResult, forceRollback);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerLocal#_deliverEnd(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public DeliveryResult _deliverEnd(
			DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult,
			boolean forceRollback)
	{
		if (deliveryID == null)
			throw new NullPointerException("deliveryID");

		if (deliverEndClientResult == null)
			throw new NullPointerException("deliverEndClientResult");

		// Store deliverEndClientResult into the database within a NEW TRANSACTION to
		// prevent it from being lost (if this method fails later and causes a rollback).
		try {
			deliveryHelperLocal.deliverEnd_storeDeliverEndClientResult(
					deliveryID, deliverEndClientResult, forceRollback
			);
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
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
			} catch (RuntimeException x) {
				throw x;
			} catch (Exception x) {
				throw new RuntimeException(x);
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
		public Serializable invoke()
				throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				String localOrganisationID = getOrganisationID();
				User user = User.getUser(pm, getPrincipal());

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

								Article backhandArticle = Article.getAllocatedArticle(pm, backhandOffer, nestedProductLocal.getProduct());
								if (backhandArticle == null)
									throw new IllegalStateException("Cannot find backhand-Article for local Article \"" + article.getPrimaryKey() + "\" + and nestedProductLocal \"" + nestedProductLocal.getPrimaryKey() + "\"");

								articleSet.add(backhandArticle);
							} // if (nestedProductLocal.getQuantity() < 1) {
						}
					}
				}

				if (productTypeClass2organisationID2articleSet != null) {
//					for (Map.Entry<Class, Map<String, Map<Boolean, Set<Article>>>> me1 : productTypeClass2organisationID2articleSet.entrySet()) {
//						Class productTypeClass = me1.getKey();
//						ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(pm, productTypeClass);
//						CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator = productTypeActionHandler.getCrossTradeDeliveryCoordinator();
					for (Map.Entry<CrossTradeDeliveryCoordinator, Map<String, Map<Boolean, Set<Article>>>> me1 : productTypeClass2organisationID2articleSet.entrySet()) {
						CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator = me1.getKey();

						for (Map.Entry<String, Map<Boolean, Set<Article>>> me2 : me1.getValue().entrySet()) {
//							String organisationID = me2.getKey();

							for (Map.Entry<Boolean, Set<Article>> me3 : me2.getValue().entrySet()) {
//								Boolean directionIncoming = me3.getKey();

								Set<Article> backhandArticles = me3.getValue();

								// Because of transactional problems, crossTradeDeliveryCoordinator.performCrossTradeDelivery(...) will spawn an additional AsyncInvoke
								// In the long run, we should implement a special "fast-track-delivery" which will be used between organisations and work within one
								// transaction. See javadoc of the performCrossTradeDelivery method.
								crossTradeDeliveryCoordinator.performCrossTradeDelivery(user, backhandArticles);
							}
						}
					}
				}

				return null;
			} finally {
				pm.close();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getDeliveryNoteIDs(org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, long, long)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.queryDeliveryNotes")
	@Override
	public List<DeliveryNoteID> getDeliveryNoteIDs(AnchorID vendorID, AnchorID customerID, AnchorID endCustomerID, long rangeBeginIdx, long rangeEndIdx)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return new ArrayList<DeliveryNoteID>(DeliveryNote.getDeliveryNoteIDs(pm, vendorID, customerID, endCustomerID, rangeBeginIdx, rangeEndIdx));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getDelivery(org.nightlabs.jfire.store.deliver.id.DeliveryID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.deliver")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getNonFinalizedDeliveryNotes(org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.queryDeliveryNotes")
	@Override
	public List<DeliveryNote> getNonFinalizedDeliveryNotes(AnchorID vendorID, AnchorID customerID, String[] fetchGroups, int maxFetchDepth)
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#signalDeliveryNote(org.nightlabs.jfire.store.id.DeliveryNoteID, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	@Override
	public void signalDeliveryNote(DeliveryNoteID deliveryNoteID, String jbpmTransitionName)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Store.getStore(pm).signalDeliveryNote(deliveryNoteID, jbpmTransitionName);
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getProductTypeIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Collection<ProductTypeID> getProductTypeIDs(QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries)
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
			@SuppressWarnings("unchecked")
			JDOQueryCollectionDecorator<AbstractProductTypeQuery> queries =
				(JDOQueryCollectionDecorator<AbstractProductTypeQuery>) productTypeQueries;

			queries.setPersistenceManager(pm);

			@SuppressWarnings("unchecked")
			Collection<ProductType> productTypes = (Collection<ProductType>) queries.executeQueries();

// Commented out the following filtering, because that's already done by the query using the ProductTypePermissionFlagSets. Marco.
//			productTypes = Authority.filterIndirectlySecuredObjects(
//					pm,
//					productTypes,
//					getPrincipal(),
//					RoleConstants.seeProductType,
//					ResolveSecuringAuthorityStrategy.allow);

			return NLJDOHelper.getObjectIDList(productTypes);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Collection<ProductTypeIDTreeNode> getProductTypeIDTree(QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries)
	{
		if (productTypeQueries == null)
			return null;

		if (! ProductType.class.isAssignableFrom(productTypeQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! Invalid return type= "+ productTypeQueries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			if (! (productTypeQueries instanceof JDOQueryCollectionDecorator))
			{
				productTypeQueries = new JDOQueryCollectionDecorator<AbstractProductTypeQuery>(productTypeQueries);
			}

			@SuppressWarnings("unchecked")
			JDOQueryCollectionDecorator<AbstractProductTypeQuery> queries =
				(JDOQueryCollectionDecorator<AbstractProductTypeQuery>) productTypeQueries;

			queries.setPersistenceManager(pm);

			@SuppressWarnings("unchecked")
			Collection<ProductType> productTypes = (Collection<ProductType>) queries.executeQueries();
			Collection<ProductTypeID> productTypeIDs = NLJDOHelper.getObjectIDList(productTypes);

			return ProductTypeIDTreeNode.buildTree(pm, productTypeIDs);
		} finally {
			pm.close();
		}
	}

//// TODO @Daniel: this new method checked-in by you today, but it's not used anywhere. Is it really necessary? Marco.
//	private Set<ProductTypeID> getInternalProductTypeIDs(QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries)
//	{
//		if (productTypeQueries == null)
//			return null;
//
//		if (! ProductType.class.isAssignableFrom(productTypeQueries.getResultClass()))
//		{
//			throw new RuntimeException("Given QueryCollection has invalid return type! " +
//					"Invalid return type= "+ productTypeQueries.getResultClassName());
//		}
//
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(1);
//			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
//
//			if (! (productTypeQueries instanceof JDOQueryCollectionDecorator))
//			{
//				productTypeQueries = new JDOQueryCollectionDecorator<AbstractProductTypeQuery>(productTypeQueries);
//			}
//			JDOQueryCollectionDecorator<AbstractProductTypeQuery> queries =
//				(JDOQueryCollectionDecorator<AbstractProductTypeQuery>) productTypeQueries;
//
//			queries.setPersistenceManager(pm);
//
//			Collection<ProductType> productTypes = (Collection<ProductType>) queries.executeQueries();
//
//			productTypes = Authority.filterIndirectlySecuredObjects(
//					pm,
//					productTypes,
//					getPrincipal(),
//					RoleConstants.seeProductType,
//					ResolveSecuringAuthorityStrategy.allow);
//
//			return NLJDOHelper.getObjectIDSet(productTypes);
//		} finally {
//			pm.close();
//		}
//	}

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

//	/**
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
//	 */
//	@SuppressWarnings("unchecked")
//	public ProductTypeGroupIDSearchResult getProductTypeGroupSearchResult(
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
//			pm.getFetchPlan().setGroups(FetchPlan.DEFAULT);
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
//			Collection<ProductTypeGroup> productTypeGroups = (Collection<ProductTypeGroup>) queries.executeQueries();
//
//			ProductTypeGroupIDSearchResult result = new ProductTypeGroupIDSearchResult();
//			for (Iterator<? extends ProductTypeGroup> iter = productTypeGroups.iterator(); iter.hasNext();) {
//				ProductTypeGroup group = iter.next();
//				ProductTypeGroupID groupID = (ProductTypeGroupID) JDOHelper.getObjectId(group);
//				result.addEntry(groupID);
//				for (Iterator<ProductType> iterator = group.getProductTypes().iterator(); iterator.hasNext();) {
//					ProductType type = iterator.next();
//					//					ProductTypeID typeID = (ProductTypeID) JDOHelper.getObjectId(type);
//					//					if (Authority.resolveSecuringAuthority(pm, type.getProductTypeLocal(), ResolveSecuringAuthorityStrategy.allow).containsRoleRef(getPrincipal(), RoleConstants.seeProductType))
//					//						result.addType(groupID, typeID);
//					// The filtering is already done in the method ProductTypeGroup.getProductTypes() - no need to filter again.
//					ProductTypeID typeID = (ProductTypeID) JDOHelper.getObjectId(type);
//					result.addType(groupID, typeID);
//				}
//			}
//			return result;
//		} finally {
//			pm.close();
//		}
//	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getProductTypeGroupIDSearchResultForProductTypeQueries(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
	public ProductTypeGroupIDSearchResult getProductTypeGroupIDSearchResultForProductTypeQueries(
			QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries)
	{
		long startTotal = System.currentTimeMillis();
		if (logger.isDebugEnabled())
			logger.debug("getProductTypeGroupIDSearchResultForProductTypeQueries: enter");

		if (productTypeQueries == null)
			return null;

		if (! ProductType.class.isAssignableFrom(productTypeQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ productTypeQueries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setGroups(FetchPlan.DEFAULT);

			if (! (productTypeQueries instanceof JDOQueryCollectionDecorator))
			{
				productTypeQueries = new JDOQueryCollectionDecorator<AbstractProductTypeQuery>(productTypeQueries);
			}
			JDOQueryCollectionDecorator<AbstractProductTypeQuery> queries =
				(JDOQueryCollectionDecorator<AbstractProductTypeQuery>) productTypeQueries;

			queries.setPersistenceManager(pm);

			ProductTypeGroupIDSearchResult result = new ProductTypeGroupIDSearchResult();

			long startExecuteQueries = System.currentTimeMillis();
			Collection<ProductType> productTypes = (Collection<ProductType>) queries.executeQueries();
			if (logger.isDebugEnabled())
				logger.debug("getProductTypeGroupIDSearchResultForProductTypeQueries: executeQueries took " + (System.currentTimeMillis() - startExecuteQueries) + " msec");

			for (ProductType productType : productTypes) {
				ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
				for (ProductTypeGroup productTypeGroup : productType.getProductTypeGroups()) {
					ProductTypeGroupID productTypeGroupID = (ProductTypeGroupID) JDOHelper.getObjectId(productTypeGroup);
					result.addEntry(productTypeGroupID);
					result.addType(productTypeGroupID, productTypeID);
				}
			}

			if (logger.isDebugEnabled())
				logger.debug("getProductTypeGroupIDSearchResultForProductTypeQueries: exit (took " + (System.currentTimeMillis() - startTotal) + " msec)");

			return result;
		} finally {
			pm.close();
		}
	}



	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getDeliveryQueuesById(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public Collection<DeliveryQueue> getDeliveryQueuesById(Set<DeliveryQueueID> deliveryQueueIds, String[] fetchGroups, int fetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setFetchSize(fetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<DeliveryQueue> c = CollectionUtil.castCollection(pm.getObjectsById(deliveryQueueIds));
			return pm.detachCopyAll(c);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#storeDeliveryQueues(java.util.Collection, boolean, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryQueue")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getAvailableDeliveryQueueIDs(boolean)
	 */
	@RolesAllowed("_Guest_")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getRepositoryIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryRepositories")
	@SuppressWarnings("unchecked")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getRepositories(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryRepositories")
	@Override
	public List<Repository> getRepositories(Collection<AnchorID> repositoryIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, repositoryIDs, Repository.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#storeRepository(org.nightlabs.jfire.store.Repository, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editRepository")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getProductTransferIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryProductTransfers")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getProductTransfers(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryProductTransfers")
	@Override
	public List<ProductTransfer> getProductTransfers(Collection<TransferID> productTransferIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, productTransferIDs, ProductTransfer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getRepositoryTypeIDs()
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getRepositoryTypes(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getProductTypeGroups(java.util.Collection, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Collection<ProductTypeGroup> getProductTypeGroups(Collection<ProductTypeGroupID>
		productTypeGroupIDs, String[] fetchGroups, int maxFetchDepth)
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getReceptionNoteIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryReceptionNotes")
	@SuppressWarnings("unchecked")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getReceptionNotes(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryReceptionNotes")
	@Override
	public List<ReceptionNote> getReceptionNotes(Set<ReceptionNoteID> receptionNoteIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, receptionNoteIDs, ReceptionNote.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getReservations(org.nightlabs.jfire.store.id.ProductTypeID, java.lang.String, int)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
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

			Collection<? extends ProductType> productTypes = CollectionUtil.castCollection(queries.executeQueries());

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

	private static final String calculateProductTypeAvailabilityPercentage_multiTxJobID = "calculateProductTypeAvailabilityPercentage";
	private static final int calculateProductTypeAvailabilityPercentage_chunkProductTypeQty = 3;

	/**
	 * The process in {@link #calculateProductTypeAvailabilityPercentage(TaskID)} breaks after this duration has been reached.
	 * Hence, it might take longer (if it is close to this duration and another chunk is begun).
	 */
	private static final long calculateProductTypeAvailabilityPercentage_breakDurationMSec = 90L * 1000L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#calculateProductTypeAvailabilityPercentage(org.nightlabs.jfire.timer.id.TaskID)
	 */
	@RolesAllowed("_System_")
	@Override
	public void calculateProductTypeAvailabilityPercentage(TaskID taskID)
	throws Exception
	{
		long startTimestamp = System.currentTimeMillis();
		boolean forceAtLeastOneChunk = true;
		PersistenceManager pm = getPersistenceManager();
		try {
			Map<Class<? extends ProductType>, ProductTypeActionHandler> productTypeClass2productTypeActionHandler = new HashMap<Class<? extends ProductType>, ProductTypeActionHandler>();

			Map<ProductTypeActionHandlerID, Map<ProductTypeID, Set<UserID>>> productTypeActionHandlerID2productTypeID2userIDs = CollectionUtil.castMap(
					(Map<?, ?>) MultiTxJob.popMultiTxJobPartData(pm, calculateProductTypeAvailabilityPercentage_multiTxJobID)
			);
			if (productTypeActionHandlerID2productTypeID2userIDs != null && productTypeActionHandlerID2productTypeID2userIDs.isEmpty())
				productTypeActionHandlerID2productTypeID2userIDs = null;

//			Map<ProductTypeActionHandler, Map<ProductType, Set<User>>> productTypeActionHandler2productType2users = new HashMap<ProductTypeActionHandler, Map<ProductType,Set<User>>>();

			if (productTypeActionHandlerID2productTypeID2userIDs != null) {
				if (logger.isDebugEnabled())
					logger.debug("calculateProductTypeAvailabilityPercentage: entered with MultiTxJob data pending => won't query new data.");
			}
			else {
				if (logger.isDebugEnabled())
					logger.debug("calculateProductTypeAvailabilityPercentage: entered without MultiTxJob data pending => querying fresh data.");

				productTypeActionHandlerID2productTypeID2userIDs = new HashMap<ProductTypeActionHandlerID, Map<ProductTypeID,Set<UserID>>>();
				forceAtLeastOneChunk = false;

				Query q = pm.newQuery(ProductTypePermissionFlagSet.class);
				q.setFilter("this.closed == false && this.expired == false");
				Collection<? extends ProductTypePermissionFlagSet> c = CollectionUtil.castCollection((Collection<?>) q.execute());
				for (ProductTypePermissionFlagSet productTypePermissionFlagSet : c) {
					ProductType productType = productTypePermissionFlagSet.getProductType();
					Class<? extends ProductType> productTypeClass = productType.getClass();
					ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
					User user = productTypePermissionFlagSet.getUser();
					UserID userID = (UserID) JDOHelper.getObjectId(user);

					ProductTypeActionHandler productTypeActionHandler = productTypeClass2productTypeActionHandler.get(productTypeClass);
					if (productTypeActionHandler == null) {
						productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(pm, productTypeClass);
						productTypeClass2productTypeActionHandler.put(productTypeClass, productTypeActionHandler);
					}
					ProductTypeActionHandlerID productTypeActionHandlerID = (ProductTypeActionHandlerID) JDOHelper.getObjectId(productTypeActionHandler);

					Map<ProductTypeID, Set<UserID>> productTypeID2userIDs = productTypeActionHandlerID2productTypeID2userIDs.get(productTypeActionHandlerID);
					if (productTypeID2userIDs == null) {
						productTypeID2userIDs = new HashMap<ProductTypeID, Set<UserID>>();
						productTypeActionHandlerID2productTypeID2userIDs.put(productTypeActionHandlerID, productTypeID2userIDs);
					}

					Set<UserID> userIDs = productTypeID2userIDs.get(productTypeID);
					if (userIDs == null) {
						userIDs = new HashSet<UserID>();
						productTypeID2userIDs.put(productTypeID, userIDs);
					}

					userIDs.add(userID);
				}
			}

			long productTypesDoneCount = 0;
			mainLoop: for (Iterator<Map.Entry<ProductTypeActionHandlerID, Map<ProductTypeID, Set<UserID>>>> it1 = productTypeActionHandlerID2productTypeID2userIDs.entrySet().iterator(); it1.hasNext(); ) {
				Map.Entry<ProductTypeActionHandlerID, Map<ProductTypeID, Set<UserID>>> me1 = it1.next();
				ProductTypeActionHandlerID productTypeActionHandlerID = me1.getKey();
				Map<ProductTypeID, Set<UserID>> productTypeID2userIDs = me1.getValue();
				ProductTypeActionHandler productTypeActionHandler = (ProductTypeActionHandler) pm.getObjectById(productTypeActionHandlerID);

				Map<ProductType, Set<User>> productType2users = new HashMap<ProductType, Set<User>>(calculateProductTypeAvailabilityPercentage_chunkProductTypeQty);
				Iterator<Map.Entry<ProductTypeID, Set<UserID>>> it2 = productTypeID2userIDs.entrySet().iterator();

				while (it2.hasNext()) {

					if (!forceAtLeastOneChunk) {
						if (System.currentTimeMillis() - startTimestamp > calculateProductTypeAvailabilityPercentage_breakDurationMSec)
							break mainLoop;
					}
					else
						forceAtLeastOneChunk = false;

					for (int i = 0; i < calculateProductTypeAvailabilityPercentage_chunkProductTypeQty; ++i) {
						if (!it2.hasNext())
							continue;

						Map.Entry<ProductTypeID, Set<UserID>> me2 = it2.next();
						it2.remove();

						if (me1.getValue().isEmpty())
							it1.remove();

						ProductType productType = (ProductType) pm.getObjectById(me2.getKey());
						Set<UserID> userIDs = me2.getValue();
						Set<User> users = NLJDOHelper.getObjectSet(pm, userIDs, User.class);
						productType2users.put(productType, users);
						++productTypesDoneCount;
					}

					Map<ProductType, Map<User, Double>> percentages = productTypeActionHandler.calculateProductTypeAvailabilityPercentage(productType2users);
					for (Map.Entry<ProductType, Set<User>> me2 : productType2users.entrySet()) {
						ProductType productType = me2.getKey();
						for (User user : me2.getValue()) {
							Map<User, Double> user2percentage = percentages.get(productType);
							if (user2percentage == null)
								throw new IllegalStateException(
										"ProductTypeActionHandler " + productTypeActionHandler + " in organisation \"" + getOrganisationID() + "\" returned an incomplete result in method calculateProductTypeAvailabilityPercentage(...): No entry for ProductType: " + productType
								);

							Double percentage = user2percentage.get(user);
							if (percentage == null)
								throw new IllegalStateException(
										"ProductTypeActionHandler " + productTypeActionHandler + " in organisation \"" + getOrganisationID() + "\" returned an incomplete result in method calculateProductTypeAvailabilityPercentage(...) for ProductType \"" + productType + "\": No entry for user: " + user
								);

							ProductTypePermissionFlagSet productTypePermissionFlagSet = (ProductTypePermissionFlagSet) pm.getObjectById(
									ProductTypePermissionFlagSetID.create(productType, user)
							);
							productTypePermissionFlagSet.setAvailabilityPercentage(percentage);
						}
					}

				} // while (it.hasNext()) {
			} // mainLoop

			if (!productTypeActionHandlerID2productTypeID2userIDs.isEmpty())
				MultiTxJob.createMultiTxJobPart(pm, calculateProductTypeAvailabilityPercentage_multiTxJobID, productTypeActionHandlerID2productTypeID2userIDs);

			if (logger.isDebugEnabled()) {
				long productTypesToDoCount = 0;
				for (Map.Entry<ProductTypeActionHandlerID, Map<ProductTypeID, Set<UserID>>> me1 : productTypeActionHandlerID2productTypeID2userIDs.entrySet()) {
					productTypesToDoCount += me1.getValue().size();
				}

				logger.debug("calculateProductTypeAvailabilityPercentage: exiting after having processed " + productTypesDoneCount + " product types (still " + productTypesToDoCount + " to do).");
			}

		} finally {
			pm.close();
		}
	}

	private void initTimerTaskCalculateProductTypeAvailabilityPercentage(PersistenceManager pm)
	throws Exception
	{
		TaskID taskID = TaskID.create(getOrganisationID(), Task.TASK_TYPE_ID_SYSTEM, "calculateProductTypeAvailabilityPercentage");
		try {
			pm.getObjectById(taskID);
			return; // no JDOObjectNotFoundException => it exists already => return without creating it
		} catch (JDOObjectNotFoundException x) {
			// fine - it does not exist => create it below
		}
		Task task = new Task(
				taskID,
				User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
				StoreManagerLocal.class, "calculateProductTypeAvailabilityPercentage"
		);
		task = pm.makePersistent(task);
		task.getTimePatternSet().createTimePattern("*", "*", "*", "*", "*", "9-59/10");

		task.getName().setText(Locale.ENGLISH.getLanguage(), "Calculate product type availability");
		task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This task calculates the availability percentage of all active product types.");

		task.getName().setText(Locale.GERMAN.getLanguage(), "Berechnung der Produkttyp-Verfügbarkeit");
		task.getDescription().setText(Locale.GERMAN.getLanguage(), "Dieser Task berechnet den Produkttyp-Verfügbarkeits-Prozentsatz für alle aktiven Produkttypen.");
		task.setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getMyProductTypePermissionFlagSetIDs(java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public Set<ProductTypePermissionFlagSetID> getMyProductTypePermissionFlagSetIDs(Collection<? extends ProductTypeID> productTypeIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());

			Collection<? extends ProductType> productTypes = CollectionUtil.castCollection(pm.getObjectsById(productTypeIDs));

			Set<ProductTypePermissionFlagSetID> result = new HashSet<ProductTypePermissionFlagSetID>();
			for (ProductType productType : productTypes) {
				ProductTypePermissionFlagSet productTypePermissionFlagSet = ProductTypePermissionFlagSet.getProductTypePermissionFlagSet(pm, productType, user, false);
				if (productTypePermissionFlagSet == null)
					continue;

				result.add((ProductTypePermissionFlagSetID) JDOHelper.getObjectId(productTypePermissionFlagSet));
			}
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getProductTypePermissionFlagSets(java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ProductTypePermissionFlagSet> getProductTypePermissionFlagSets(Collection<? extends ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());

			List<ProductTypePermissionFlagSet> result = new ArrayList<ProductTypePermissionFlagSet>();
			Collection<? extends ProductTypePermissionFlagSet> ptpfss = CollectionUtil.castCollection(pm.getObjectsById(productTypePermissionFlagSetIDs));
			for (ProductTypePermissionFlagSet ptpfs : ptpfss) {
				if (user.equals(ptpfs.getUser()))
					result.add(ptpfs);
			}

			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			return pm.detachCopyAll(result);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public long getRootProductTypeCount(Class<? extends ProductType> productTypeClass, boolean subclasses) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return ProductType.getRootProductTypeCount(pm, productTypeClass, subclasses);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Set<ProductTypeID> getRootProductTypeIDs(Class<? extends ProductType> productTypeClass, boolean subclasses) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<? extends ProductType> productTypes = ProductType.getRootProductTypes(pm, productTypeClass, subclasses);

			productTypes = Authority.filterIndirectlySecuredObjects(
					pm,
					productTypes,
					getPrincipal(),
					RoleConstants.seeProductType,
					ResolveSecuringAuthorityStrategy.allow
			);

			return NLJDOHelper.getObjectIDSet(productTypes);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Map<ProductTypeID, Long> getChildProductTypeCounts(Collection<ProductTypeID> parentProductTypeIDs) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Map<ProductTypeID, Long> result = new HashMap<ProductTypeID, Long>(parentProductTypeIDs.size());
			for (ProductTypeID parentProductTypeID : parentProductTypeIDs) {
				long count = ProductType.getChildProductTypeCount(pm, parentProductTypeID);
				result.put(parentProductTypeID, count);
			}
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.StoreManagerRemote#getChildProductTypeIDs(org.nightlabs.jfire.store.id.ProductTypeID)
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@Override
	public Collection<ProductTypeID> getChildProductTypeIDs(ProductTypeID parentProductTypeID) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<? extends ProductType> productTypes = ProductType.getChildProductTypes(pm, parentProductTypeID);

			productTypes = Authority.filterIndirectlySecuredObjects(
					pm,
					productTypes,
					getPrincipal(),
					RoleConstants.seeProductType,
					ResolveSecuringAuthorityStrategy.allow
			);

			return NLJDOHelper.getObjectIDList(productTypes);
		} finally {
			pm.close();
		}
	}
}
