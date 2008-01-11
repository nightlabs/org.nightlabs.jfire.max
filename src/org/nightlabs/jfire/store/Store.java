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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.book.BookProductTransfer;
import org.nightlabs.jfire.store.book.LocalStorekeeper;
import org.nightlabs.jfire.store.book.PartnerStorekeeper;
import org.nightlabs.jfire.store.deliver.DeliverProductTransfer;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryActionHandler;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryLocal;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypeStatusTrackerID;
import org.nightlabs.jfire.store.jbpm.ActionHandlerBookDeliveryNote;
import org.nightlabs.jfire.store.jbpm.ActionHandlerBookDeliveryNoteImplicitely;
import org.nightlabs.jfire.store.jbpm.ActionHandlerFinalizeDeliveryNote;
import org.nightlabs.jfire.store.jbpm.JbpmConstantsDeliveryNote;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.TradeConfigModule;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

/**
 * The Store is responsible for managing all ProductTypes and Products
 * within the store. There exists exactly one instance in the jdo datastore.
 * 
 * @author marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.StoreID"
 *		detachable="true"
 *		table="JFireTrade_Store"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
public class Store
implements StoreCallback
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Store.class);
	
	/**
	 * This method returns the singleton instance of Store. If there is
	 * no instance of Store in the datastore, yet, it will be created.
	 *
	 * @param pm
	 * @return
	 */
	public static Store getStore(PersistenceManager pm)
	{
		Iterator<?> it = pm.getExtent(Store.class).iterator();
		if (it.hasNext()) {
			Store store = (Store)it.next();

			// TODO remove this debug stuff
			String securityReflectorOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			if (!securityReflectorOrganisationID.equals(store.getOrganisationID()))
				throw new IllegalStateException("SecurityReflector returned organisationID " + securityReflectorOrganisationID + " but Store.organisationID=" + store.getOrganisationID());
			// TODO end debug

			return store;
		}

		Store store = new Store();

		// initialize the organisationID
		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
		String organisationID = localOrganisation.getOrganisation().getOrganisationID();
		store.organisationID = organisationID;
		store.mandator = OrganisationLegalEntity.getOrganisationLegalEntity(pm, organisationID, OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION, true);
		store.localStorekeeper = new LocalStorekeeper(store.mandator, LocalStorekeeper.class.getName());
		store.mandator.setStorekeeper(store.localStorekeeper);
		store.partnerStorekeeper = new PartnerStorekeeper(organisationID, PartnerStorekeeper.class.getName());

		store = (Store) pm.makePersistent(store);
		return store;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalStorekeeper localStorekeeper;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PartnerStorekeeper partnerStorekeeper;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */ 
	private OrganisationLegalEntity mandator;

	protected Store() { }

//	/**
//	 * key: String productPrimaryKey {organisationID + / + productID}<br/>
//	 * value: ProductType productType
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="ProductType"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 *
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 201"
//	 */
//	protected Map productTypes = new HashMap();
	
//	/**
//	 * key: String productPrimaryKey {organisationID + / + productID}<br/>
//	 * value: ProductStatusTracker productStatusTracker
//	 * 
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="ProductStatusTracker"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 * 
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 201"
//	 */
//	protected Map productTypeStatusTrackers = new HashMap();

//	/**
//	 * key: String productPrimaryKey {organisationID + / + productID}<br/>
//	 * value: ProductType product
//	 * 
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="ProductType"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 * 
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 201"
//	 */
//	protected Map products = new HashMap();
	
//	/**
//	 * key: String productPrimaryKey {organisationID + / + productID}<br/>
//	 * value: ProductStatusTracker productStatusTracker
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="ProductStatusTracker"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 *
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 201"
//	 */
//	protected Map productStatusTrackers = new HashMap();

//	/**
//	 * key: String productPrimaryKey {organisationID + / + productID}<br/>
//	 * value: ProductTransferTracker productTransferTracker
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="ProductTransferTracker"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 *
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 201"
//	 */
//	protected Map productTransferTrackers = new HashMap();

	public void setProductTypeStatus_published(User user, ProductType productType) 
	throws CannotPublishProductTypeException
	{
		if (productType == null)
			throw new IllegalArgumentException("productType must not be null!");

		if (productType.isPublished())
			return;

		productType.setPublished(true);
		getProductTypeStatusTracker(productType, true).newCurrentStatus(user);
	}

	public void setProductTypeStatus_confirmed(User user, ProductType productType) 
	throws CannotConfirmProductTypeException
	{
		if (productType == null)
			throw new IllegalArgumentException("productType must not be null!");

		if (productType.isConfirmed())
			return;

		productType.setConfirmed(true);
		getProductTypeStatusTracker(productType, true).newCurrentStatus(user);
	}

	public void setProductTypeStatus_saleable(User user, ProductType productType, boolean saleable) 
	throws CannotMakeProductTypeSaleableException
	{
		if (productType.isSaleable() == saleable)
			return;

		productType.setSaleable(saleable);
		getProductTypeStatusTracker(productType, true).newCurrentStatus(user);
	}

	public void setProductTypeStatus_closed(User user, ProductType productType)
	{
		if (productType.isClosed())
			return;

		productType.setClosed(true);
		getProductTypeStatusTracker(productType, true).newCurrentStatus(user);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @param organisationID The organisationID to set.
	 */
	protected void setOrganisationID(String organisationID)
	{
		this.organisationID = organisationID;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Store is currently not persistent! Cannot obtain a PersistenceManager!");
		return pm;
	}

	public boolean containsProductType(ProductType productType)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getObjectById(ProductTypeID.create(
				productType.getOrganisationID(), productType.getProductTypeID()));

			return true;
		} catch (JDOObjectNotFoundException x) {
			return false;
		}
	}

	/**
	 * @param user Which user is adding this productType.
	 * @param productType
	 */
	public ProductType addProductType(User user, ProductType productType)
	{
		PersistenceManager pm = getPersistenceManager();
		productType = pm.makePersistent(productType);

		// JPOX WORKAROUND there seems to be a JPOX bug causing the object not to be cleanly replaced by the attached one
		{
			ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
			pm.evictAll();
			productType = (ProductType) pm.getObjectById(productTypeID);
		}

//		15:34:20,427 ERROR [LogInterceptor] RuntimeException in method: public abstract org.nightlabs.ipanema.ticketing.store.Event org.nightlabs.ipanema.ticketing.TicketingManager.storeEvent(org.nightlabs.ipanema.ticketing.store.Event,boolean,java.lang.String[],int) throws org.nightlabs.ModuleException,java.rmi.RemoteException:
//			javax.jdo.JDODetachedFieldAccessException: You have just attempted to access field "extendedProductType" yet this field was not detached when you detached the object. Either dont access this field, or detach the field when detaching the object.
//			        at org.nightlabs.jfire.store.ProductType.jdoGetextendedProductType(ProductType.java)
//			        at org.nightlabs.jfire.store.ProductType.getExtendedProductType(ProductType.java:683)
//			        at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator._resolvableProductTypes_registerWithAnchestors(PriceCalculator.java:300)
//			        at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator.preparePriceCalculation_createResolvableProductTypesMap(PriceCalculator.java:282)
//			        at org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator.preparePriceCalculation(PriceCalculator.java:159)
//			        at org.nightlabs.ipanema.ticketing.TicketingManagerBean.storeEvent(TicketingManagerBean.java:602)
//			        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//			        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//			        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			        at java.lang.reflect.Method.invoke(Method.java:585)
//			        at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//			        at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//			        at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//			        at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//			        at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//			        at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//			        at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//			        at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//			        at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//			        at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//			        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:138)
//			        at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//			        at org.jboss.ejb.Container.invoke(Container.java:960)
//			        at sun.reflect.GeneratedMethodAccessor110.invoke(Unknown Source)
//			        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			        at java.lang.reflect.Method.invoke(Method.java:585)
//			        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//			        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//			        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//			        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//			        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//			        at org.jboss.invocation.unified.server.UnifiedInvoker.invoke(UnifiedInvoker.java:231)
//			        at sun.reflect.GeneratedMethodAccessor139.invoke(Unknown Source)
//			        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			        at java.lang.reflect.Method.invoke(Method.java:585)
//			        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//			        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//			        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//			        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//			        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//			        at javax.management.MBeanServerInvocationHandler.invoke(MBeanServerInvocationHandler.java:201)
//			        at $Proxy16.invoke(Unknown Source)
//			        at org.jboss.remoting.ServerInvoker.invoke(ServerInvoker.java:734)
//			        at org.jboss.remoting.transport.socket.ServerThread.processInvocation(ServerThread.java:560)
//			        at org.jboss.remoting.transport.socket.ServerThread.dorun(ServerThread.java:383)
//			        at org.jboss.remoting.transport.socket.ServerThread.run(ServerThread.java:165)

		if (productType.getProductTypeLocal() != null)
			throw new IllegalArgumentException("This ProductType has already a ProductTypeLocal assigned! Obviously you either called Store.addProductType(...) twice or you detached a ProductTypeLocal from a remote organisation! Both is illegal!");

		// TODO remove this and put all the logic from ProductTypeStatusTracker into ProductTypeLocal!
		if (organisationID.equals(productType.getOrganisationID())) {
			ProductTypeStatusTracker productTypeStatusTracker = new ProductTypeStatusTracker(productType, user);
			pm.makePersistent(productTypeStatusTracker);
		}

		if (productType.getOwner() == null)
			productType.setOwner(getMandator());

		if (productType.getVendor() == null)
			productType.setVendor(getMandator());

//		ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(getPersistenceManager(), productType.getClass());
//		Repository defaultHomeRepository = productTypeActionHandler.getDefaultHomeRepository(productType);

		productType.createProductTypeLocal(user);
		// TODO JPOX WORKAROUND - begin
		try {
			pm.flush();
		} catch (Exception x) {
			logger.warn("JPOX bug: creating ProductTypeLocal caused an exception!", x);
		}

//	19:30:18,983 WARN  [SQL] Insert of object "org.nightlabs.jfire.voucher.store.VoucherTypeLocal@8a80d8" using statement "INSERT INTO `JFIRETRADE_PRODUCTTYPELOCAL` (`PRODUCT_TYPE_ORGANISATION_ID_OID`,`PRODUCT_TYPE_PRODUCT_TYPE_ID_OID`,`LOCAL_ACCOUNTANT_DELEGATE_LOCAL_ACCOUNTANT_DELEGATE_ID_OID`,`LOCAL_ACCOUNTANT_DELEGATE_ORGANISATION_ID_OID`,`LOCAL_STOREKEEPER_DELEGATE_LOCAL_STOREKEEPER_DELEGATE_ID_OID`,`LOCAL_STOREKEEPER_DELEGATE_ORGANISATION_ID_OID`,`HOME_ANCHOR_ID_OID`,`HOME_ANCHOR_TYPE_ID_OID`,`HOME_ORGANISATION_ID_OID`,`ORGANISATION_ID`,`PRODUCT_TYPE_ID`) VALUES (?,?,?,?,?,?,?,?,?,?,?)" failed : Duplicate entry 'chezfrancois.jfire.org-ostern_a7n' for key 1
//	19:30:18,987 ERROR [LogInterceptor] RuntimeException in method: public abstract org.nightlabs.jfire.voucher.store.VoucherType org.nightlabs.jfire.voucher.VoucherManager.storeVoucherType(org.nightlabs.jfire.voucher.store.VoucherType,boolean,java.lang.String[],int) throws java.rmi.RemoteException:
//	javax.jdo.JDODataStoreException: Insert of object "org.nightlabs.jfire.voucher.store.VoucherTypeLocal@8a80d8" using statement "INSERT INTO `JFIRETRADE_PRODUCTTYPELOCAL` (`PRODUCT_TYPE_ORGANISATION_ID_OID`,`PRODUCT_TYPE_PRODUCT_TYPE_ID_OID`,`LOCAL_ACCOUNTANT_DELEGATE_LOCAL_ACCOUNTANT_DELEGATE_ID_OID`,`LOCAL_ACCOUNTANT_DELEGATE_ORGANISATION_ID_OID`,`LOCAL_STOREKEEPER_DELEGATE_LOCAL_STOREKEEPER_DELEGATE_ID_OID`,`LOCAL_STOREKEEPER_DELEGATE_ORGANISATION_ID_OID`,`HOME_ANCHOR_ID_OID`,`HOME_ANCHOR_TYPE_ID_OID`,`HOME_ORGANISATION_ID_OID`,`ORGANISATION_ID`,`PRODUCT_TYPE_ID`) VALUES (?,?,?,?,?,?,?,?,?,?,?)" failed : Duplicate entry 'chezfrancois.jfire.org-ostern_a7n' for key 1
//	        at org.jpox.jdo.JPOXJDOHelper.getJDOExceptionForJPOXException(JPOXJDOHelper.java:283)
//	        at org.jpox.AbstractPersistenceManager.jdoMakePersistent(AbstractPersistenceManager.java:594)
//	        at org.jpox.AbstractPersistenceManager.makePersistent(AbstractPersistenceManager.java:614)
//	        at org.nightlabs.jfire.store.Store.addProductType(Store.java:430)
//	        at org.nightlabs.jfire.voucher.VoucherManagerBean.storeVoucherType(VoucherManagerBean.java:485)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//	        at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//	        at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//	        at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//	        at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//	        at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//	        at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//	        at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//	        at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//	        at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//	        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:138)
//	        at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//	        at org.jboss.ejb.Container.invoke(Container.java:960)
//	        at sun.reflect.GeneratedMethodAccessor115.invoke(Unknown Source)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//	        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//	        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//	        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//	        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//	        at org.jboss.invocation.unified.server.UnifiedInvoker.invoke(UnifiedInvoker.java:231)
//	        at sun.reflect.GeneratedMethodAccessor132.invoke(Unknown Source)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//	        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//	        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//	        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//	        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//	        at javax.management.MBeanServerInvocationHandler.invoke(MBeanServerInvocationHandler.java:201)
//	        at $Proxy16.invoke(Unknown Source)
//	        at org.jboss.remoting.ServerInvoker.invoke(ServerInvoker.java:734)
//	        at org.jboss.remoting.transport.socket.ServerThread.processInvocation(ServerThread.java:560)
//	        at org.jboss.remoting.transport.socket.ServerThread.dorun(ServerThread.java:383)
//	        at org.jboss.remoting.transport.socket.ServerThread.run(ServerThread.java:165)
//	NestedThrowablesStackTrace:
//	com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException: Duplicate entry 'chezfrancois.jfire.org-ostern_a7n' for key 1
//	        at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:931)
//	        at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:2870)
//	        at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:1573)
//	        at com.mysql.jdbc.ServerPreparedStatement.serverExecute(ServerPreparedStatement.java:1160)
//	        at com.mysql.jdbc.ServerPreparedStatement.executeInternal(ServerPreparedStatement.java:685)
//	        at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:1400)
//	        at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:1314)
//	        at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:1299)
//	        at org.apache.commons.dbcp.DelegatingPreparedStatement.executeUpdate(DelegatingPreparedStatement.java:101)
//	        at org.apache.commons.dbcp.DelegatingPreparedStatement.executeUpdate(DelegatingPreparedStatement.java:101)
//	        at org.jpox.store.rdbms.SQLController.executeStatementUpdate(SQLController.java:368)
//	        at org.jpox.store.rdbms.request.InsertRequest.execute(InsertRequest.java:363)
//	        at org.jpox.store.rdbms.table.ClassTable.insert(ClassTable.java:2653)
//	        at org.jpox.store.rdbms.table.ClassTable.insert(ClassTable.java:2649)
//	        at org.jpox.store.MappedStoreManager.insertObject(MappedStoreManager.java:177)
//	        at org.jpox.state.JDOStateManagerImpl.internalMakePersistent(JDOStateManagerImpl.java:2943)
//	        at org.jpox.state.JDOStateManagerImpl.flush(JDOStateManagerImpl.java:4227)
//	        at org.jpox.state.JDOStateManagerImpl.insertionCompleted(JDOStateManagerImpl.java:3147)
//	        at org.jpox.state.JDOStateManagerImpl.changeActivityState(JDOStateManagerImpl.java:3045)
//	        at org.jpox.store.rdbms.request.InsertRequest.execute(InsertRequest.java:373)
//	        at org.jpox.store.rdbms.table.ClassTable.insert(ClassTable.java:2653)
//	        at org.jpox.store.MappedStoreManager.insertObject(MappedStoreManager.java:177)
//	        at org.jpox.state.JDOStateManagerImpl.internalMakePersistent(JDOStateManagerImpl.java:2943)
//	        at org.jpox.state.JDOStateManagerImpl.makePersistent(JDOStateManagerImpl.java:2923)
//	        at org.jpox.ObjectManagerImpl.persistObjectInternal(ObjectManagerImpl.java:1088)
//	        at org.jpox.ObjectManagerImpl.persistObject(ObjectManagerImpl.java:987)
//	        at org.jpox.AbstractPersistenceManager.jdoMakePersistent(AbstractPersistenceManager.java:589)
//	        at org.jpox.AbstractPersistenceManager.makePersistent(AbstractPersistenceManager.java:614)
//	        at org.nightlabs.jfire.store.Store.addProductType(Store.java:430)
//	        at org.nightlabs.jfire.voucher.VoucherManagerBean.storeVoucherType(VoucherManagerBean.java:485)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//	        at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//	        at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//	        at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//	        at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//	        at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//	        at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//	        at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//	        at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//	        at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//	        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:138)
//	        at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//	        at org.jboss.ejb.Container.invoke(Container.java:960)
//	        at sun.reflect.GeneratedMethodAccessor115.invoke(Unknown Source)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//	        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//	        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//	        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//	        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//	        at org.jboss.invocation.unified.server.UnifiedInvoker.invoke(UnifiedInvoker.java:231)
//	        at sun.reflect.GeneratedMethodAccessor132.invoke(Unknown Source)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//	        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//	        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//	        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//	        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//	        at javax.management.MBeanServerInvocationHandler.invoke(MBeanServerInvocationHandler.java:201)
//	        at $Proxy16.invoke(Unknown Source)
//	        at org.jboss.remoting.ServerInvoker.invoke(ServerInvoker.java:734)
//	        at org.jboss.remoting.transport.socket.ServerThread.processInvocation(ServerThread.java:560)
//	        at org.jboss.remoting.transport.socket.ServerThread.dorun(ServerThread.java:383)
//	        at org.jboss.remoting.transport.socket.ServerThread.run(ServerThread.java:165)


		{
			ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
			pm.evictAll();
			productType = (ProductType) pm.getObjectById(productTypeID);
			if (productType.getProductTypeLocal() == null)
				throw new IllegalStateException("JPOX Workaround failed: There's no ProductTypeLocal!");
		}
		// TODO JPOX WORKAROUND - end
		
		return productType;
	}

	protected Repository getInitialRepositoryForLocalProduct(Product product)
	{
		return getLocalStorekeeper().getInitialRepositoryForLocalProduct(product);
	}

	protected Repository getInitialRepositoryForForeignProduct(Product product)
	{
		return getPartnerStorekeeper().getInitialRepositoryForForeignProduct(product);
	}

	/**
	 * @param user Which user is adding this product.
	 * @param product The <tt>Product</tt> that shall be added.
	 * @!param initialRepository The <tt>Repository</tt> into which the <tt>product</tt> is "born"
	 */
	public Product addProduct(User user, Product product) // , Repository initialRepository)
	{
		// note that the product might already be persistent - e.g. when importing from another organisation (cross-trade).
		PersistenceManager pm = getPersistenceManager();
		product = (Product) pm.makePersistent(product);

		if (product.getProductLocal() != null)
			throw new IllegalArgumentException("This Product has already a ProductLocal assigned! Obviously you either called Store.addProduct(...) twice or you detached a ProductLocal from a remote organisation! Both is illegal!");

//		Repository initialRepository = ProductTypeActionHandler.getProductTypeActionHandler(pm, product.getProductType().getClass()).getInitialRepository(product);

		Repository initialRepository;
		if (this.getOrganisationID().equals(product.getOrganisationID()))
			initialRepository = getInitialRepositoryForLocalProduct(product);
		else
			initialRepository = getInitialRepositoryForForeignProduct(product);

		product.createProductLocal(user, initialRepository);
		return product;
	}

	public ProductType getProductType(String organisationID, String productTypeID, boolean throwExceptionIfNotFound)
	{
		PersistenceManager pm = getPersistenceManager();
		pm.getExtent(ProductType.class);

		try {
			return (ProductType) pm.getObjectById(ProductTypeID.create(organisationID, productTypeID));
		} catch (JDOObjectNotFoundException x) {
			if (throwExceptionIfNotFound)
				throw x;
		}

		return null;
	}
	public ProductTypeStatusTracker getProductTypeStatusTracker(String organisationID, String productTypeID, boolean throwExceptionIfNotFound)
	{
		PersistenceManager pm = getPersistenceManager();
		pm.getExtent(ProductTypeStatusTracker.class);
		ProductTypeStatusTracker res = null;
		try {
			res = (ProductTypeStatusTracker) pm.getObjectById(
					ProductTypeStatusTrackerID.create(organisationID, productTypeID));
		} catch (JDOObjectNotFoundException x) {
			if (throwExceptionIfNotFound)
				throw x;
		}
		return res;
	}
	public ProductTypeStatusTracker getProductTypeStatusTracker(ProductTypeID productTypeID, boolean throwExceptionIfNotFound)
	{
		return getProductTypeStatusTracker(productTypeID.organisationID, productTypeID.productTypeID, throwExceptionIfNotFound);
	}
	public ProductTypeStatusTracker getProductTypeStatusTracker(ProductType productType, boolean throwExceptionIfNotFound)
	{
		return getProductTypeStatusTracker((ProductTypeID)JDOHelper.getObjectId(productType), throwExceptionIfNotFound);
	}

	public Product getProduct(String organisationID, long productID, boolean throwExceptionIfNotFound)
	{
		PersistenceManager pm = getPersistenceManager();
		pm.getExtent(Product.class);
		try {
			return (Product) pm.getObjectById(ProductID.create(organisationID, productID));
		} catch (JDOObjectNotFoundException x) {
			if (throwExceptionIfNotFound)
				throw x;
		}

		return null;
	}
//	public ProductStatusTracker getProductStatusTracker(Product product, boolean throwExceptionIfNotFound)
//	{
//		if (product == null)
//			throw new NullPointerException("product must not be null!");
//
//		PersistenceManager pm = getPersistenceManager();
//		pm.getExtent(ProductStatusTracker.class);
//		try {
//			return (ProductStatusTracker) pm.getObjectById(
//					ProductStatusTrackerID.create(product.getOrganisationID(), product.getProductID()));
//		} catch (JDOObjectNotFoundException x) {
//			if (throwExceptionIfNotFound)
//				throw x;
//		}
//		return null;
//	}
//	public ProductTransferTracker getProductTransferTracker(
//			String organisationID, String productID, boolean throwExceptionIfNotFound)
//	{
//		// TODO
//		throw new UnsupportedOperationException("NYI");
//	}
//	public ProductTransferTracker getProductTransferTracker(ProductType product, boolean throwExceptionIfNotFound)
//	{
//		// TODO
//		throw new UnsupportedOperationException("NYI");
//	}

//	protected ProductTransfer transferProducts(ProductTransfer container, User initiator, Anchor from, Anchor to, Collection products)
//	{
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("This store is not persistent! I don't have a PersistenceManager!");
//
//		ProductTransfer productTransfer = new ProductTransfer(this, container, initiator, from, to, products);
//		for (Iterator it = products.iterator(); it.hasNext(); ) {
//			Product product = (Product)it.next();
////			getProductTransferTracker(product, true).addProductTransfer(productTransfer);
//		}
//		// TODO We should make sure that all products can be transferred (e.g. to check packaged products).
//		// And probably we need to do much more...
//		return productTransfer;
//	}
//
//	public ProductTransfer transferProducts(User initiator, Anchor from, Anchor to, ProductTransfer container)
//	{
//		return transferProducts(container, initiator, from, to, container.products.values());
//	}
//
//	public ProductTransfer transferProducts(User initiator, Anchor from, Anchor to, Collection products)
//	{
//		return transferProducts(null, initiator, from, to, products);
//	}

	/**
	 * @param productType Can be <code>null</code>, if <code>nestedProductTypeLocal</code> is defined.
	 * @param nestedProductTypeLocal Can be <code>null</code>, if <code>productType</code> is defined (usually, when it is the top-level-producttype).
	 * @return Returns <tt>Collection</tt> of suitable <tt>Product</tt>s or <tt>null</tt> if nothing is available.
	 */
	public Collection<? extends Product> findProducts(User user, ProductType productType, NestedProductTypeLocal nestedProductTypeLocal, ProductLocator productLocator)
	{
		if (nestedProductTypeLocal == null && productType == null)
			throw new IllegalArgumentException("productType and nestedProductTypeLocal are both null! One of them must be defined!");

		if (nestedProductTypeLocal != null)
			productType = nestedProductTypeLocal.getInnerProductTypeLocal().getProductType();

		ProductTypeActionHandler ptah = ProductTypeActionHandler.getProductTypeActionHandler(
				getPersistenceManager(), productType.getClass());

		return ptah.findProducts(
				user, productType, nestedProductTypeLocal, productLocator);
	}

	/**
	 * @return Returns the mandator.
	 */
	public OrganisationLegalEntity getMandator()
	{
		return mandator;
	}

	protected static Map<ProductTypeActionHandler, Set<Article>> getProductTypeActionHandler2ArticlesMap(
			PersistenceManager pm, Collection<? extends Article> articles)
	{
		Map<ProductTypeActionHandler, Set<Article>> productTypeActionHandler2Articles = new HashMap<ProductTypeActionHandler, Set<Article>>();
		for (Article article : articles) {
			ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(
					pm, article.getProductType().getClass());
			Set<Article> as = (Set<Article>) productTypeActionHandler2Articles.get(productTypeActionHandler);
			if (as == null) {
				as = new HashSet<Article>();
				productTypeActionHandler2Articles.put(productTypeActionHandler, as);
			}
			as.add(article);
		}

		return productTypeActionHandler2Articles;
	}

	public void addArticlesToDeliveryNote(User user, DeliveryNote deliveryNote, Collection<? extends Article> articles)
	throws DeliveryNoteEditException
	{
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
			deliveryNote.addArticle(article);
		}

		Map productTypeActionHandler2Articles = getProductTypeActionHandler2ArticlesMap(getPersistenceManager(), articles);
		for (Iterator it = productTypeActionHandler2Articles.entrySet().iterator(); it.hasNext();) {
			Map.Entry me = (Map.Entry) it.next();
			((ProductTypeActionHandler) me.getKey()).onAddArticlesToDeliveryNote(user, this, deliveryNote, (Collection) me.getValue());
		}
	}

	public void removeArticlesFromDeliveryNote(User user, DeliveryNote deliveryNote, Collection articles)
	throws DeliveryNoteEditException
	{
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
			deliveryNote.removeArticle(article);
		}
	}

	/**
	 * Creates a new DeliveryNote with the given articles.
	 * Checks whether vendor and customer are the same for all involved offers
	 * whether not articles are associated to another <tt>DeliverNote</tt>. If
	 * one check
	 * fails a DeliveryNoteEditException will be thrown.
	 *
	 * @param user The user which is responsible for creation of this invoice.
	 * @param articles The {@link Article}s that shall be added to the invoice. Must not be empty (because the customer is looked up from the articles).
	 * @param deliveryNoteIDPrefix Which prefix shall be used (i.e. what namespace for the newly generated deliveryNoteID). If this is <code>null</code>, the
	 *		user's default value will be used. 
	 */
	public DeliveryNote createDeliveryNote(
			User user, Collection articles, String deliveryNoteIDPrefix)
	throws DeliveryNoteEditException
	{
		if (articles.size() <= 0)
			throw new DeliveryNoteEditException(
				DeliveryNoteEditException.REASON_NO_ARTICLES,
				"Cannot create a DeliveryNote without Articles!"
			);

		// Make sure all Articles are not yet in a DeliveryNote.
		// all offers have the same vendor and customer
		// and all offers have the same currency
		String vendorPK = null;
		OrganisationLegalEntity vendorLE = null;
		String customerPK = null;
		LegalEntity customerLE = null;
		for (Iterator iter = articles.iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			
			if (vendorPK == null) {
				vendorLE = article.getOffer().getOrder().getVendor();
				vendorPK = vendorLE.getPrimaryKey();
			}
			if (customerPK == null) {
				customerLE = article.getOffer().getOrder().getCustomer();
				customerPK = customerLE.getPrimaryKey();
			}

			Offer articleOffer = article.getOffer();
			Order articleOrder = articleOffer.getOrder();
			
			if (!articleOffer.getOfferLocal().isAccepted()) {
				throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_OFFER_NOT_ACCEPTED, 
					"At least one involved offer is not accepted!",
					(ArticleID) JDOHelper.getObjectId(article)
				);
			}

			if (!vendorPK.equals(articleOrder.getVendor().getPrimaryKey()) 
						|| 
					!customerPK.equals(articleOrder.getCustomer().getPrimaryKey()) 
					)
			{
				throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_ANCHORS_DONT_MATCH,				
					"Vendor and customer are not equal for all involved orders, can not create DeliveryNote!!"
				);
			}

			if (article.getDeliveryNote() != null) {
//				DeliveryNoteID invoiceID = DeliveryNoteID.create(article.getDeliveryNote().getOrganisationID(), article.getDeliveryNote().getDeliveryNoteID());
				DeliveryNote deliveryNote = article.getDeliveryNote();
				throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_ARTICLE_ALREADY_IN_DELIVERY_NOTE,
					"Article already in a delivery note. Article "+article.getPrimaryKey()+", DeliveryNote "+deliveryNote.getPrimaryKey(), 
					(ArticleID) JDOHelper.getObjectId(article), 
					(DeliveryNoteID) JDOHelper.getObjectId(deliveryNote)
				);
			}

		}

		if (!vendorPK.equals(getMandator().getPrimaryKey()))
			throw new DeliveryNoteEditException(
				DeliveryNoteEditException.REASON_FOREIGN_ORGANISATION,
				"Attempt to create a DeliveryNote not with the local organisation as vendor. Vendor is "+vendorPK
			);

		if (deliveryNoteIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			try {
				tradeConfigModule = (TradeConfigModule) Config.getConfig(
						getPersistenceManager(), organisationID, user).createConfigModule(TradeConfigModule.class);
			} catch (ModuleException x) {
				throw new RuntimeException(x); // should not happen.
			}

			deliveryNoteIDPrefix = tradeConfigModule.getActiveIDPrefixCf(DeliveryNote.class.getName()).getDefaultIDPrefix();
		}

		DeliveryNote deliveryNote = new DeliveryNote(
				user, vendorLE, customerLE,
				deliveryNoteIDPrefix, IDGenerator.nextID(DeliveryNote.class, deliveryNoteIDPrefix));
		new DeliveryNoteLocal(deliveryNote); // self-registering
		getPersistenceManager().makePersistent(deliveryNote);

		ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
				ProcessDefinitionAssignmentID.create(DeliveryNote.class, TradeSide.vendor));
		processDefinitionAssignment.createProcessInstance(null, user, deliveryNote);

		for (Iterator iter = articles.iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			deliveryNote.addArticle(article);
		}

		Map productTypeActionHandler2Articles = getProductTypeActionHandler2ArticlesMap(getPersistenceManager(), articles);
		for (Iterator it = productTypeActionHandler2Articles.entrySet().iterator(); it.hasNext();) {
			Map.Entry me = (Map.Entry) it.next();
			((ProductTypeActionHandler) me.getKey()).onAddArticlesToDeliveryNote(user, this, deliveryNote, (Collection) me.getValue());
		}

		return deliveryNote;
	}

	/**
	 * Creates and persists a {@link DeliveryNote} for all {@link Article}s of the
	 * given <tt>Offer</tt>.
	 * It silently ignores <tt>Article</tt>s that are already in an other
	 * <tt>DeliveryNote</tt>.
	 *
	 * @param user
	 * @param offer
	 * @return a new DeliveryNote
	 * @throws DeliveryNoteEditException
	 */
	public DeliveryNote createDeliveryNote(User user, ArticleContainer articleContainer, String deliveryNoteIDPrefix) 
	throws DeliveryNoteEditException 
	{
		ArrayList articles = new ArrayList();
		for (Iterator it = articleContainer.getArticles().iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
			if (article.getDeliveryNote() == null)
				articles.add(article);
		}
		return createDeliveryNote(user, articles, deliveryNoteIDPrefix);
	}

	public void validateDeliveryNote(DeliveryNote deliveryNote)
	{
		deliveryNote.validate();
	}

	/**
	 * @return Never returns <tt>null</tt>. If {@link Delivery#getServerDeliveryProcessorID()}
	 *		returns <tt>null</tt>, a suitable processor is searched according to the given
	 *		<tt>ModeOfDeliveryFlavour</tt>. If no processor can be found at all, an
	 *		<tt>IllegalStateException</tt> is thrown.
	 */
	protected ServerDeliveryProcessor getServerDeliveryProcessor(
			Delivery delivery)
	{
		ModeOfDeliveryFlavour modeOfDeliveryFlavour = delivery.getModeOfDeliveryFlavour();
		ServerDeliveryProcessorID serverDeliveryProcessorID = delivery.getServerDeliveryProcessorID();
		// get ServerDeliveryProcessor, if serverDeliveryProcessorID is defined.
		ServerDeliveryProcessor serverDeliveryProcessor = null;
		if (serverDeliveryProcessorID != null) {
			PersistenceManager pm = getPersistenceManager();
			pm.getExtent(ServerDeliveryProcessor.class);
			serverDeliveryProcessor = (ServerDeliveryProcessor) pm.getObjectById(serverDeliveryProcessorID);
		}

		if (serverDeliveryProcessor == null) {
			Collection c = ServerDeliveryProcessor.getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
					getPersistenceManager(),
					modeOfDeliveryFlavour);
			if (c.isEmpty())
				throw new IllegalStateException("No ServerDeliveryProcessor registered for ModeOfDeliveryFlavour \""+modeOfDeliveryFlavour.getPrimaryKey()+"\"!");

			serverDeliveryProcessor = (ServerDeliveryProcessor) c.iterator().next();
		} // if (serverDeliveryProcessor == null) {

		return serverDeliveryProcessor;
	}

	/**
	 * This method is a noop, if the offer is already accepted. If the Offer cannot be accepted implicitely
	 * (either because the business partner doesn't allow implicit acceptance or because the jBPM token is at
	 * a position where this is not possible, an exception is thrown).
	 */
	protected void bookDeliveryNoteImplicitely(DeliveryNote deliveryNote)
	{
		DeliveryNoteID deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);
		if (State.hasState(getPersistenceManager(), deliveryNoteID, JbpmConstantsDeliveryNote.Both.NODE_NAME_BOOKED))
			return;

		JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
		try {
			ProcessInstance processInstance = jbpmContext.getProcessInstance(deliveryNote.getDeliveryNoteLocal().getJbpmProcessInstanceId());
			processInstance.signal(JbpmConstantsDeliveryNote.Vendor.TRANSITION_NAME_BOOK_IMPLICITELY);
		} finally {
			jbpmContext.close();
		}
	}

	protected static enum DeliverStage
	{
		deliverBegin,
		deliverDoWork,
		deliverEnd
	}

	/**
	 * @param deliveryNotes Can be null. Should be a <tt>Collection</tt> of {@link DeliveryNote}
	 * @param stage This method is called twice during 
	 * @return Either <tt>null</tt>, in case no DeliveryNote was passed or the partner
	 *		(if at least one DeliveryNote has been passed in <tt>deliveryNotes</tt>).
	 */
	protected LegalEntity bookDeliveryNotesImplicitelyAndGetPartner(DeliverStage deliverStage, Collection deliveryNotes)
	{
		if (deliveryNotes == null)
			return null;

//	 check currency and find out partner
		// ...maybe it will later be possible to deliver an deliveryNote in a different
		// currency, but currently this is not possible.
//		LegalEntity mandator = getMandator();
		LegalEntity mandator = getMandator();
		LegalEntity partner = null;
		for (Iterator it = deliveryNotes.iterator(); it.hasNext(); ) {
			DeliveryNote deliveryNote = (DeliveryNote) it.next();

			if (mandator.equals(deliveryNote.getVendor())) {
				if (partner == null)
					partner = deliveryNote.getCustomer();
				else {
					if (!partner.equals(deliveryNote.getCustomer()))
						throw new IllegalArgumentException("Customer of deliveryNote \"" + deliveryNote.getPrimaryKey() + "\" does not match other deliveryNotes' partners! Expected partner \"" + partner.getPrimaryKey() + "\", but found \"" + deliveryNote.getCustomer().getPrimaryKey() + "\"!");
				}

				if (DeliverStage.deliverBegin == deliverStage)
					bookDeliveryNoteImplicitely(deliveryNote);
			} // vendor is mandator
			else {
				if (!mandator.equals(deliveryNote.getCustomer()))
					throw new IllegalArgumentException("The deliveryNote \""+deliveryNote.getPrimaryKey()+"\" has nothing to do with the mandator (\"" + mandator.getPrimaryKey() + "\")!");

				if (partner == null)
					partner = deliveryNote.getVendor();
				else {
					if (!partner.equals(deliveryNote.getVendor()))
						throw new IllegalArgumentException("Vendor of deliveryNote \"" + deliveryNote.getPrimaryKey() + "\" does not match other deliveryNotes' partners! Expected partner \"" + partner.getPrimaryKey() + "\", but found \"" + deliveryNote.getVendor().getPrimaryKey() + "\"!");
				}

				if (DeliverStage.deliverEnd == deliverStage)
					bookDeliveryNoteImplicitely(deliveryNote);
			}
		}

		return partner;
	}

//	/**
//	 * @param user The {@link User} who is responsible for the delivery.
//	 * @param deliveryDataList Instances of {@link DeliveryData}.
//	 * @return Returns instances of {@link DeliveryResult} corresponding to the
//	 *		{@link DeliveryData} objects passed in <tt>deliveryDataList</tt>.
//	 */
//	public List deliverBegin(User user, List deliveryDataList)
//	{
//		List resList = new ArrayList();
//		for (Iterator it = deliveryDataList.iterator(); it.hasNext(); ) {
//			DeliveryData deliveryData = (DeliveryData) it.next();
//			try {
//				DeliveryResult res = deliverBegin(user, deliveryData);
//				resList.add(res);
//			} catch (DeliveryException x) {
//				resList.add(x.getDeliveryResult());
//			} catch (Throwable t) {
//				
//			}
//		}
//		return resList;
//	}

	public LocalStorekeeper getLocalStorekeeper()
	{
		return localStorekeeper;
	}

	public PartnerStorekeeper getPartnerStorekeeper()
	{
		return partnerStorekeeper;
	}

	/**
	 * Finalizes a deliveryNote and sends it to the involved 
	 * organisation if neccessary.
	 * 
	 * @param finalizer
	 * @param deliveryNote
	 */
	public void finalizeDeliveryNote(User finalizer, DeliveryNote deliveryNote) {
		if (deliveryNote.isFinalized())
			return;

		if (!deliveryNote.getVendor().getPrimaryKey().equals(getMandator().getPrimaryKey()))
			throw new IllegalArgumentException("Can not finalize a deliveryNote where mandator is not vendor of this deliveryNote!");

		// deliveryNote.setFinalized(...) does nothing, if it is already finalized.
		deliveryNote.setFinalized(finalizer);
		if (deliveryNote.getCustomer() instanceof OrganisationLegalEntity) {
			// TODO: Put the Invoice in the queue on this organisations server ...
		}
	}

	/**
	 * This method is called by {@link ActionHandlerBookDeliveryNote}.
	 */
	public void onBookDeliveryNote(User initiator, DeliveryNote deliveryNote)
	{
		DeliveryNoteLocal deliveryNoteLocal = deliveryNote.getDeliveryNoteLocal();
//		if (deliveryNoteLocal.isBooked()) {
//			if (!silentlyIgnoreBookedInvoice)
//				throw new IllegalStateException("Invoice \""+deliveryNote.getPrimaryKey()+"\" has already been booked!");
//
//			return;
//		}
//
//		if (!deliveryNote.isFinalized()) {
//			if (!finalizeIfNecessary)
//				throw new IllegalStateException("Invoice \""+deliveryNote.getPrimaryKey()+"\" is not finalized!");
//
//			finalizeDeliveryNote(initiator, deliveryNote);
//		}

		if (deliveryNoteLocal.isBooked())
			return;

//		LegalEntity from = null;
//		LegalEntity to = null;
//
//		if (deliveryNote.getPrice().getAmount() >= 0) {
//			from = deliveryNote.getCustomer();
//			to = deliveryNote.getVendor();
//		}
//		else {
//			from = deliveryNote.getVendor();
//			to = deliveryNote.getCustomer();
//		}
//
//		// The LocalAccountant is assigned to the mandator in any case, because it is
//		// assigned during creation of Accounting. Hence, we don't need to check whether
//		// from or to is the other side.
//		if (from.getAccountant() == null)
//			from.setAccountant(getPartnerAccountant());
//
//		if (to.getAccountant() == null)
//			to.setAccountant(getPartnerAccountant());
//
//		// create the BookMoneyTransfer with positive amount but in the right direction
//		if (deliveryNote.getPrice().getAmount() < 0) {
//			LegalEntity tmp = from;
//			from = to;
//			to = tmp;
//		}
//
//		BookProductTransfer interLegalEntityMoneyTransfer = new BookProductTransfer(
//			this,
//			initiator,
//			from,
//			to,			
//			deliveryNote
//		);
//		HashSet<Anchor> involvedAnchors = new HashMap();
//		interLegalEntityMoneyTransfer.bookTransfer(initiator, involvedAnchors);

		if (deliveryNote.getCustomer().getStorekeeper() == null)
			deliveryNote.getCustomer().setStorekeeper(getPartnerStorekeeper());

		if (deliveryNote.getVendor().getStorekeeper() == null)
			deliveryNote.getVendor().setStorekeeper(getPartnerStorekeeper());

		// The booking works only with ProductReferences - the ProductLocal is not touched (Product is never touched anyway by deliveries or bookings) 
		Set<Anchor> involvedAnchors = new HashSet<Anchor>();
		List<BookProductTransfer> bookProductTransfers = BookProductTransfer.createBookProductTransfers(initiator, deliveryNote);
		boolean failed = true;
		try {
			for (BookProductTransfer bookProductTransfer : bookProductTransfers)
				bookProductTransfer.bookTransfer(initiator, involvedAnchors);

			checkIntegrity(bookProductTransfers, involvedAnchors);

			failed = false;
		} finally {
			if (failed)
				Anchor.resetIntegrity(bookProductTransfers, involvedAnchors);
		}

		deliveryNoteLocal.setBooked(initiator);

		for (DeliveryNoteActionHandler deliveryNoteActionHandler : deliveryNoteLocal.getDeliveryNoteActionHandlers()) {
			deliveryNoteActionHandler.onBook(initiator, deliveryNote);
		}
	}

	/**
	 * 1. The LegalEntity checks, whether the {@link ProductReference}s of the current transaction have quantity = 0.<br/>
	 * 2. The Repositories do NOT check anything themselves (except maybe whether they're too full (=> no space)),
	 *    but is checked by this method whether all {@link ProductReference}s have a quantity between -1 and +1 and only
	 *    exactly ONE Repository has -1 and ONE repository has +1.<br/>
	 * 3. productLocal.quantity is updated.
	 *
	 * @param containers Instances of {@link Transfer}
	 * @param involvedAnchors
	 */
	protected void checkIntegrity(Collection<? extends ProductTransfer> containers, Set<Anchor> involvedAnchors)
	{
		PersistenceManager pm = getPersistenceManager();

		// We collect the products from all container-transfers.
		Set<Product> products = new HashSet<Product>();
		for (ProductTransfer container : containers)
			products.addAll(container.getProducts());

		// These two Maps should store for each Product where it's coming from and
		// where it's going to because of the given containers.
		Map<Product, ProductReference> fromProductReferenceByProductMap = new HashMap<Product, ProductReference>();
		Map<Product, ProductReference> toProductReferenceByProductMap = new HashMap<Product, ProductReference>();

		for (Anchor anchor : involvedAnchors) {
			// Give every involved Anchor the possibility to check itself.
			anchor.checkIntegrity(containers);

			// LegalEntity checked itself already! Therefore all its ProductReferences have quantity = 0
			// and we don't need to iterate the products (and search for ProductReferences).
			if (anchor instanceof LegalEntity)
				continue;

			// If the current Anchor is the final destination or the very source of the transfer of
			// any product, put it into one of the product2xxxMaps 
			for (Product product : products) {
				ProductReference productReference = ProductReference.getProductReference(pm, anchor, product, false); // not every involved anchor has a reference to every product, because the transfers take different routes. hence the result may be null
				if (productReference == null)
					continue;

				int qty = productReference.getQuantity();
				switch (qty) {
					case -1: {
						ProductReference pr = (ProductReference) fromProductReferenceByProductMap.get(product);
						if (pr != null)
							throw new IllegalStateException("The Product \"" + product.getPrimaryKey() + "\" has more than one ProductReference with quantity = -1: ProductReference \"" + pr.getPrimaryKey() + "\" and \"" + productReference.getPrimaryKey() + "\" (and maybe even more)!");

						fromProductReferenceByProductMap.put(product, productReference);
						break;
					}
					case 0:
						// nothing
						break;
					case 1: {
						ProductReference pr = (ProductReference) toProductReferenceByProductMap.get(product);
						if (pr != null)
							throw new IllegalStateException("The Product \"" + product.getPrimaryKey() + "\" has more than one ProductReference with quantity = +1: ProductReference \"" + pr.getPrimaryKey() + "\" and \"" + productReference.getPrimaryKey() + "\" (and maybe even more)!");

						toProductReferenceByProductMap.put(product, productReference);
						break;
					}
					default:
					throw new IllegalStateException("ProductReference for Anchor \""+anchor.getPrimaryKey()+"\" and Product \""+product.getPrimaryKey()+"\" has illegal quantity = " + qty + "! Quantity must be >= -1 and <= 1!");
				}
			}
		}

		// If we came here, all is fine. Note, that products.size() and the size of the two maps might
		// differ. This can happen, because we checked only those ProductReferences that are part of this transaction
		// and there might be a "neighbour" chain that caused the beginning/end of this chain to be 0.

// Marco: productLocal.quantity is updated in Repository now already!
//		// We update productLocal.quantity now. This works always, because a 
//		for (Iterator itP = products.iterator(); itP.hasNext(); ) {
//			Product product = (Product) itP.next();
//			ProductLocal productLocal = product.getProductLocal();
//			ProductReference productReferenceSource = (ProductReference) product2SourceProductHoleMap.get(product);
//			ProductReference productReferenceDest = (ProductReference) product2DestinationProductHoleMap.get(product);
//
//			Repository repositorySource = productReferenceSource == null ? null : (Repository) productReferenceSource.getAnchor();
//			Repository repositoryDest = productReferenceDest == null ? null : (Repository) productReferenceDest.getAnchor();
//
//			if (repositorySource != null && repositorySource.isOutside())
//				productLocal.incQuantity();
//
//			if (repositoryDest != null && repositoryDest.isOutside())
//				productLocal.decQuantity();
//		}
	}

	/**
	 * If the {@link ProductLocal#get} 
	 *
	 * @param products
	 */
	public void consolidateProductReferences(Collection<Product> products)
	{
		PersistenceManager pm = getPersistenceManager();

		for (Product product : products) {
			if (logger.isDebugEnabled())
				logger.debug("consolidateProductReferences: product.class=" + product.getClass().getName() + " product.primaryKey=" + product.getPrimaryKey());

			Collection<? extends ProductReference> productReferencesSource = ProductReference.getProductReferences(pm, product, -1);
			Collection<? extends ProductReference> productReferencesDest = ProductReference.getProductReferences(pm, product, 1);

			if (logger.isDebugEnabled()) {
				logger.debug("consolidateProductReferences: productReferencesSource.size()=" + productReferencesSource.size() + " productReferencesDest.size()=" + productReferencesDest.size());

				for (ProductReference productReference : productReferencesSource) {
					logger.debug("consolidateProductReferences: productReferencesSource: anchor.primaryKey=" + productReference.getAnchor().getPrimaryKey());
				}
				for (ProductReference productReference : productReferencesDest) {
					logger.debug("consolidateProductReferences: productReferencesDest: anchor.primaryKey=" + productReference.getAnchor().getPrimaryKey());
				}
			}

			int size = productReferencesSource.size();
			if (size != productReferencesDest.size())
				throw new IllegalStateException("Product \"" + product.getPrimaryKey() + "\" has " + productReferencesSource.size() + " ProductReferences with quantity = -1, but " + productReferencesDest.size() + " ProductReferences with quantity = +1! The number of both should be the same!");

			// If we have multiple "chains" of product-transfers, we cannot consolidate.
			if (size != 1) {
				logger.warn("consolidateProductReferences: productReferencesSource.size()!=1 => multiple chains of transfers => cannot consolidate");

				continue;
			}

			ProductReference productReferenceSource = (ProductReference) productReferencesSource.iterator().next();
			ProductReference productReferenceDest = (ProductReference) productReferencesDest.iterator().next();
			Repository repositorySource = (Repository) productReferenceSource.getAnchor();
			Repository repositoryDest = (Repository) productReferenceDest.getAnchor();
			ProductLocal productLocal = product.getProductLocal();

			if (productLocal.getQuantity() < 0) {
				if (logger.isDebugEnabled())
					logger.debug("consolidateProductReferences: productLocal.quantity=" + productLocal.getQuantity() + " => cannot delete ProductReferences!");
			}
			else {
				if (logger.isDebugEnabled())
					logger.debug("consolidateProductReferences: deleting ProductReferences.");

				pm.deletePersistentAll(ProductReference.getProductReferences(pm, product));

				// put the product to the final destination
				Repository currentRepository = (Repository) productLocal.getAnchor();
				if (!currentRepository.getRepositoryType().isOutside() && !repositorySource.equals(currentRepository))
					throw new IllegalStateException("Product \"" + product.getPrimaryKey() + "\" is currently in a different inside repository (\"" + currentRepository.getPrimaryKey() + "\") than the transfer chain starts (\"" + repositorySource.getPrimaryKey() + "\")!");

//				setProductLocalAnchorRecursively(product, repositoryDest);
				product.getProductLocal().setAnchor(repositoryDest);
				// we do not track nested product's repositories because 1st they might be dissolved anyway (i.e. consumed) and
				// 2nd, we shouldn't do this here
			}
		}
	}

//	/**
//	 * This method sets the Anchor for all (including the nested) Products.
//	 *
//	 * @param productLocal
//	 * @param anchor
//	 */
//	protected void setProductLocalAnchorRecursively(Product product, Anchor anchor)
//	{
//		ProductLocal productLocal = product.getProductLocal();
//		productLocal.setAnchor(anchor);
//		// TODO I should somehow ensure, that the nested products really can be delivered - or does it work this way, because
//		// we call it only when ProductLocal.quantity >= 0???!
//
//// The nested products are handled during assembling/disassembling
////		for (Iterator it = productLocal.getNestedProducts().iterator(); it.hasNext(); )
////			setProductLocalAnchorRecursively((Product)it.next(), anchor);
//	}

	protected void deliverBegin_checkArticles(Collection<? extends Article> articles)
	{
		for (Article article : articles) {
			if (article.isReversed())
				throw new IllegalArgumentException("Article " + article.getPrimaryKey() + " is reversed and therefore cannot be delivered!");

			if (article.isReversing() && !article.getReversedArticle().getArticleLocal().isDelivered())
				throw new IllegalArgumentException("Article " + article.getPrimaryKey() + " is reversing the Article " + article.getReversedArticle().getPrimaryKey() + " which was not delivered! Cannot return something that has never left!");
		}
	}

	public DeliveryResult deliverBegin(User user, DeliveryData deliveryData)
	throws DeliveryException
	{
		if (user == null)
			throw new NullPointerException("user");

		if (deliveryData == null)
			throw new NullPointerException("deliveryData");

		if (deliveryData.getDelivery() == null)
			throw new NullPointerException("deliveryData.getDelivery() returns null! localOrganisation="+getOrganisationID());

		ServerDeliveryProcessor serverDeliveryProcessor = getServerDeliveryProcessor(
				deliveryData.getDelivery());

		LegalEntity partner = null;
		if (deliveryData.getDelivery().getDeliveryNotes() != null) {
			partner = bookDeliveryNotesImplicitelyAndGetPartner(DeliverStage.deliverBegin, deliveryData.getDelivery().getDeliveryNotes());
		}
		else
			throw new IllegalArgumentException("Delivery is not possible anymore without delivery notes! This exception should never happen. localOrganisation="+getOrganisationID());

		if (partner == null) {
			partner = deliveryData.getDelivery().getPartner();
		}
		else {
			if (!partner.getPrimaryKey().equals(deliveryData.getDelivery().getPartner().getPrimaryKey()))
				throw new IllegalArgumentException("deliveryData.getDelivery().getPartner() does not match the partner of deliveryData.getDelivery().getDeliveryNotes()! deliveryNotes' partner is \"" + partner.getPrimaryKey() + "\" but delivery.partner is \"" + deliveryData.getDelivery().getPartner().getPrimaryKey() + "\" localOrganisation="+getOrganisationID());
		}

		deliverBegin_checkArticles(deliveryData.getDelivery().getArticles());

		if (partner.getStorekeeper() == null)
			partner.setStorekeeper(getPartnerStorekeeper());
		
//		The DeliveryLocal object is normally created in DeliveryHelperBean#deliverBegin_storeDeliveryData(DeliveryData).
//		But some use cases do not use this API, this is why we create it here if it does not exist yet.  
		if (deliveryData.getDelivery().getDeliveryLocal() == null)
			new DeliveryLocal(deliveryData.getDelivery());

//	 call server-sided delivery processor's first phase
		DeliverProductTransfer deliverProductTransfer = serverDeliveryProcessor.deliverBegin(
				new DeliverParams(this, user, deliveryData));

		DeliveryResult serverDeliveryResult;
		serverDeliveryResult = deliveryData.getDelivery().getDeliverBeginServerResult();
		if (serverDeliveryResult == null)
			throw new DeliveryException(
					new DeliveryResult(
							DeliveryResult.CODE_FAILED,
							"deliveryData.getDelivery().getDeliverBeginServerResult() returned null! You probably forgot to set it in your ServerDeliveryProcessor (\""+serverDeliveryProcessor.getPrimaryKey()+"\")! localOrganisation="+getOrganisationID(),
							(Throwable)null));

		if (serverDeliveryResult.isFailed())
			throw new DeliveryException(serverDeliveryResult);

//		// I don't know why, but without the following line, it is not set in the datastore.
//		deliveryData.getDelivery().setDeliverBeginServerResult(serverDeliveryResult);


		try {
			for (DeliveryNote deliveryNote : deliveryData.getDelivery().getDeliveryNotes()) {
				for (DeliveryNoteActionHandler deliveryNoteActionHandler : deliveryNote.getDeliveryNoteLocal().getDeliveryNoteActionHandlers()) {
					deliveryNoteActionHandler.onDeliverBegin(user, deliveryData, deliveryNote);
				}
			}
		} catch (DeliveryException x) {
			throw x;
		} catch (Exception x) {
			throw new DeliveryException(
					new DeliveryResult(
							DeliveryResult.CODE_FAILED,
							"Calling DeliveryNoteActionHandler.onDeliverBegin failed! localOrganisation="+getOrganisationID(),
							x));
		}
		
		Set<Delivery> precursorDeliverySet = deliveryData.getDelivery().getPrecursorSet();		
		for (Delivery precursorDelivery : precursorDeliverySet) {
			try {
				for (DeliveryActionHandler deliveryActionHandler : precursorDelivery.getDeliveryLocal().getDeliveryActionHandlers()) {
					deliveryActionHandler.onFollowUpDeliverBegin(deliveryData.getDelivery(), precursorDelivery);
				}
			} catch (DeliveryException x) {
				throw x;
			} catch (Exception e) {
				throw new DeliveryException(new DeliveryResult(
						DeliveryResult.CODE_FAILED, "Calling DeliveryActionHandler.onFollowUpDeliverBegin failed! localOrganisation="+getOrganisationID(), e)); 
			}
		}


		if (deliveryData.getDelivery().isPostponed()) {
			// if we have a DeliverProductTransfer, we need to delete it from datastore
			if (deliverProductTransfer != null) {
				if (deliverProductTransfer.isBookedFrom() || deliverProductTransfer.isBookedTo())
					throw new IllegalStateException("DeliverProductTransfer is already booked! You should never book the DeliverProductTransfer in your ServerDeliveryProcessor! Check the class \""+serverDeliveryProcessor.getClass()+"\"! localOrganisation="+getOrganisationID());

				getPersistenceManager().deletePersistent(deliverProductTransfer);
				deliverProductTransfer = null;
			}
		}
		else { // not postponed
			if (!serverDeliveryResult.isApproved())
				throw new DeliveryException(serverDeliveryResult);

			if (deliverProductTransfer == null)
				throw new NullPointerException("serverDeliveryProcessor.deliverBegin(...) returned null but Delivery is NOT postponed! You are only allowed (and you should) return null, if you postpone a Delivery! serverDeliveryProcessorPK=\""+serverDeliveryProcessor.getPrimaryKey()+"\" localOrganisation="+getOrganisationID());

			Set<Anchor> involvedAnchors = new HashSet<Anchor>();
			ArrayList<DeliverProductTransfer> containers = new ArrayList<DeliverProductTransfer>(1);
			containers.add(deliverProductTransfer);
			boolean failed = true;
			try {
				deliverProductTransfer.bookTransfer(user, involvedAnchors);
	
				// check consistence
				checkIntegrity(containers, involvedAnchors);

				failed = false;
			} finally {
				if (failed)
					Anchor.resetIntegrity(containers, involvedAnchors);
			}
		}

		return serverDeliveryResult;
	}

//	protected void transferProductsWithPartner(LegalEntity partner, ProductTransfer productTransfer)
//	{
//		// we group the products that come from the same repository - so we have less ProductTransfers
//		Map repository2ProductList = new HashMap();
//
//		for (Iterator it = productTransfer.getProducts().iterator(); it.hasNext(); ) {
//			Product product = (Product) it.next();
//			Repository repository = (Repository)product.getProductLocal().getAnchor();
//			List productList = (List)repository2ProductList.get(repository);
//			if (productList == null) {
//				productList = new ArrayList();
//				repository2ProductList.put(repository, productList);
//			}
//			productList.add(product);
//		}
//
//		for (Iterator it = repository2ProductList.entrySet().iterator(); it.hasNext(); ) {
//			Map.Entry me = (Map.Entry)it.next();
//			Repository repository = (Repository) me.getKey();
//			List productList = (List) me.getValue();
//
//			Anchor from;
//			Anchor to;
//			switch (productTransfer.getAnchorType(partner)) {
//				case Transfer.ANCHORTYPE_FROM:
//					from = partner;
//					to = repository; // FIXME This is wrong! The target must be found out differently (e.g. registry or another config - or the one it came originally from?!)
//					break;
//				case Transfer.ANCHORTYPE_TO:
//					from = repository;
//					to = partner;
//					break;
//				default:
//					throw new IllegalStateException("Partner LegalEntity is neither from nor to of transfer!");
//			}
////			new ProductTransfer(this, );
//		}
//	}

	public DeliveryResult deliverDoWork(
			User user, DeliveryData deliveryData)
	throws DeliveryException
	{
		if (user == null)
			throw new NullPointerException("user");

		if (deliveryData == null)
			throw new NullPointerException("deliveryData");

		boolean postponed = deliveryData.getDelivery().isPostponed();

		ServerDeliveryProcessor serverDeliveryProcessor = getServerDeliveryProcessor(
				deliveryData.getDelivery());

//	 call server-sided delivery processor's second phase
		serverDeliveryProcessor.deliverDoWork(
				new DeliverParams(this, user, deliveryData));

		DeliveryResult serverDeliveryResult;
		serverDeliveryResult = deliveryData.getDelivery().getDeliverDoWorkServerResult();
		if (serverDeliveryResult == null)
			throw new DeliveryException(
					new DeliveryResult(
							DeliveryResult.CODE_FAILED,
							"deliveryData.getDelivery().getDeliverDoWorkServerResult() returned null! You probably forgot to set it in your ServerDeliveryProcessor (\""+serverDeliveryProcessor.getPrimaryKey()+"\")!",
							(Throwable)null));

		if (serverDeliveryResult.isFailed())
			throw new DeliveryException(serverDeliveryResult);

		if (postponed) {
			if (!DeliveryResult.CODE_POSTPONED.equals(serverDeliveryResult.getCode()) && !serverDeliveryResult.isRolledBack()) {
				String msg = "The Delivery \"" + deliveryData.getDelivery().getPrimaryKey() + "\" is marked postponed, but the DeliveryProcessor \"" + serverDeliveryProcessor.getPrimaryKey() + "\" did neither rollback nor return DeliveryResult.CODE_POSTPONED! Instead it returned code=\"" + serverDeliveryResult.getCode() + "\" text=\"" + serverDeliveryResult.getText() + "\"";
				logger.warn(msg, new IllegalStateException(msg));
			}
		}
		else {
			if (!serverDeliveryResult.isDelivered())
				throw new DeliveryException(serverDeliveryResult);
		}

		try {
			for (DeliveryNote deliveryNote : deliveryData.getDelivery().getDeliveryNotes()) {
				for (DeliveryNoteActionHandler deliveryNoteActionHandler : deliveryNote.getDeliveryNoteLocal().getDeliveryNoteActionHandlers()) {
					deliveryNoteActionHandler.onDeliverDoWork(user, deliveryData, deliveryNote);
				}
			}
		} catch (DeliveryException x) {
			throw x;
		} catch (Exception x) {
			throw new DeliveryException(
					new DeliveryResult(
							DeliveryResult.CODE_FAILED,
							"Calling DeliveryNoteActionHandler.onDeliverDoWork failed!",
							x));
		}
		
		Set<Delivery> precursorDeliverySet = deliveryData.getDelivery().getPrecursorSet();		
		for (Delivery precursorDelivery : precursorDeliverySet) {
			try {
				for (DeliveryActionHandler deliveryActionHandler : precursorDelivery.getDeliveryLocal().getDeliveryActionHandlers()) {
					deliveryActionHandler.onFollowUpDeliverDoWork(deliveryData.getDelivery(), precursorDelivery);
				}
			} catch (DeliveryException x) {
				throw x;
			} catch (Exception e) {
				throw new DeliveryException(new DeliveryResult(
						DeliveryResult.CODE_FAILED, "Calling DeliveryActionHandler.onFollowUpDeliverDoWork failed! localOrganisation="+getOrganisationID(), e)); 
			}
		}

		return serverDeliveryResult;
	}


	public DeliveryResult deliverEnd(
			User user, DeliveryData deliveryData)
	throws DeliveryException
	{
		if (user == null)
			throw new NullPointerException("user");

		if (deliveryData == null)
			throw new NullPointerException("deliveryData");

		boolean postponed = deliveryData.getDelivery().isPostponed();

		ServerDeliveryProcessor serverDeliveryProcessor = getServerDeliveryProcessor(
				deliveryData.getDelivery());

//	 call server-sided delivery processor's third phase
		serverDeliveryProcessor.deliverEnd(
				new DeliverParams(this, user, deliveryData));

		DeliveryResult serverDeliveryResult;
		serverDeliveryResult = deliveryData.getDelivery().getDeliverEndServerResult();
		if (serverDeliveryResult == null)
			throw new DeliveryException(
					new DeliveryResult(
							DeliveryResult.CODE_FAILED,
							"deliveryData.getDelivery().getDeliverEndServerResult() returned null! You probably forgot to set it in your ServerDeliveryProcessor (\""+serverDeliveryProcessor.getPrimaryKey()+"\")!",
							(Throwable)null));

		if (serverDeliveryResult.isFailed())
			throw new DeliveryException(serverDeliveryResult);

		if (postponed) {
			if (!DeliveryResult.CODE_POSTPONED.equals(serverDeliveryResult.getCode()) && !serverDeliveryResult.isRolledBack()) {
				String msg = "The Delivery \"" + deliveryData.getDelivery().getPrimaryKey() + "\" is marked postponed, but the DeliveryProcessor \"" + serverDeliveryProcessor.getPrimaryKey() + "\" did neither rollback nor return DeliveryResult.CODE_POSTPONED! Instead it returned code=\"" + serverDeliveryResult.getCode() + "\" text=\"" + serverDeliveryResult.getText() + "\"";
				logger.warn(msg, new IllegalStateException(msg));
			}
		}

		if (deliveryData.getDelivery().isForceRollback() || deliveryData.getDelivery().isPostponed()) {
			DeliverProductTransfer deliverProductTransfer = DeliverProductTransfer.getDeliverProductTransferForDelivery(
					getPersistenceManager(), deliveryData.getDelivery());

			if (deliverProductTransfer != null) {
				logger.warn("Your Delivery \""+deliveryData.getDelivery()+"\" has first " +
						"created a deliverProductTransfer and decided afterwards (in deliverEnd) to" +
						"postpone. This is not nice! Now I have to rollback your " +
						"DeliverProductTransfer! You should postpone a Delivery always in deliverBegin!");

				deliverRollback(user, deliveryData);
			}
			else
				deliveryData.getDelivery().clearPending();
		}
		else {
			if (!serverDeliveryResult.isDelivered())
				throw new DeliveryException(serverDeliveryResult);
		}

		if (deliveryData.getDelivery().isPending() && !deliveryData.getDelivery().isFailed())
			throw new IllegalStateException("Delivery should not be pending anymore, because failed is false! How's that possible?");


		try {
			for (DeliveryNote deliveryNote : deliveryData.getDelivery().getDeliveryNotes()) {
				for (DeliveryNoteActionHandler deliveryNoteActionHandler : deliveryNote.getDeliveryNoteLocal().getDeliveryNoteActionHandlers()) {
					deliveryNoteActionHandler.onDeliverEnd(user, deliveryData, deliveryNote);
				}
			}
		} catch (Exception x) {
			throw new DeliveryException(
					new DeliveryResult(
							DeliveryResult.CODE_FAILED,
							"Calling DeliveryNoteActionHandler.onDeliverEnd failed!",
							x));
		}
		
		Set<Delivery> precursorDeliverySet = deliveryData.getDelivery().getPrecursorSet();		
		for (Delivery precursorDelivery : precursorDeliverySet) {
			try {
				for (DeliveryActionHandler deliveryActionHandler : precursorDelivery.getDeliveryLocal().getDeliveryActionHandlers()) {
					deliveryActionHandler.onFollowUpDeliverEnd(deliveryData.getDelivery(), precursorDelivery);
				}
			} catch (DeliveryException x) {
				throw x;
			} catch (Exception e) {
				throw new DeliveryException(new DeliveryResult(
						DeliveryResult.CODE_FAILED, "Calling DeliveryActionHandler.onFollowUpDeliverEnd failed! localOrganisation="+getOrganisationID(), e)); 
			}
		}

		try {
			ArrayList deliveryNotesToBookImplicitely = new ArrayList(deliveryData.getDelivery().getDeliveryNotes().size());

			for (DeliveryNote deliveryNote : deliveryData.getDelivery().getDeliveryNotes()) {
				DeliveryNoteLocal deliveryNoteLocal = deliveryNote.getDeliveryNoteLocal();
				boolean outstanding = false;
				for (Article article : deliveryNote.getArticles()) {
					if (!article.getArticleLocal().isDelivered())
						outstanding = true;
				}

				if (!outstanding) { // deliveryNoteLocal.isOutstanding()) {
					JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
					try {
						ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(deliveryNoteLocal.getJbpmProcessInstanceId());
						if (!JbpmConstantsDeliveryNote.Both.NODE_NAME_DELIVERED.equals(processInstance.getRootToken().getNode().getName())) {
							processInstance.signal(JbpmConstantsDeliveryNote.Both.TRANSITION_NAME_DELIVER);
						}
					} finally {
						jbpmContext.close();
					}
					deliveryNotesToBookImplicitely.add(deliveryNote);
				}
			}

			bookDeliveryNotesImplicitelyAndGetPartner(DeliverStage.deliverEnd, deliveryNotesToBookImplicitely);
		} catch (Exception x) {
			throw new DeliveryException(
					new DeliveryResult(
							DeliveryResult.CODE_FAILED,
							"Signalling transition \"" + JbpmConstantsDeliveryNote.Both.TRANSITION_NAME_DELIVER + "\" failed!",
							x));
		}

		return serverDeliveryResult;
	}


	/**
	 * This method is called to rollback a delivery. It removes all transfers
	 * and the accounts adjust their balance.
	 * <p>
	 * It is not integrated within deliverxxxEnd
	 * (e.g. {@link #deliverEnd(User, DeliveryData)}),
	 * because it needs to be called within a separate transaction. 
	 */
	public void deliverRollback(
			User user, DeliveryData deliveryData)
	{
		Delivery delivery = deliveryData.getDelivery();

		DeliverProductTransfer deliverProductTransfer = DeliverProductTransfer.getDeliverProductTransferForDelivery(
				getPersistenceManager(), delivery);

		if (!deliveryData.getDelivery().isPending())
			throw new IllegalStateException("Delivery \"" + deliveryData.getDelivery().getPrimaryKey() + "\" is not pending! Cannot rollback!");

		if (deliverProductTransfer == null) {
			delivery.setRollbackStatus(Delivery.ROLLBACK_STATUS_DONE_WITHOUT_ACTION);
			return;
		}

		PersistenceManager pm = getPersistenceManager();

		Set involvedAnchors = new HashSet();
		ArrayList containers = new ArrayList(1);
		containers.add(deliverProductTransfer);
		boolean failed = true;
		try {
	
			for (Iterator it = deliverProductTransfer.getChildren().iterator(); it.hasNext(); ) {
				ProductTransfer productTransfer = (ProductTransfer) it.next();
	
				if (productTransfer.isBooked())
					productTransfer.rollbackTransfer(user, involvedAnchors);
	
				pm.deletePersistent(productTransfer);
			}
	
			if (deliverProductTransfer.isBooked())
				deliverProductTransfer.rollbackTransfer(user, involvedAnchors);
	
			checkIntegrity(containers, involvedAnchors);

			failed = false;
		} finally {
			if (failed)
				Anchor.resetIntegrity(containers, involvedAnchors);
		}

		pm.deletePersistent(deliverProductTransfer);

		delivery.setRollbackStatus(Delivery.ROLLBACK_STATUS_DONE_NORMAL);
	}

	public void jdoPreStore()
	{
	}

	public ProcessDefinition storeProcessDefinitionReceptionNote(TradeSide tradeSide, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();

		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);

		// we add the events+actionhandlers
		ActionHandlerNodeEnter.register(jbpmProcessDefinition);

		// TODO implement this completely!

		// store it
		ProcessDefinition processDefinition = ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition, jbpmProcessDefinitionURL);
		ProcessDefinitionID processDefinitionID = (ProcessDefinitionID) JDOHelper.getObjectId(processDefinition);

		switch (tradeSide) {
			case vendor:
			{
			}
			break;
			case customer:
			{
			}
			break;
			default:
				throw new IllegalStateException("Unknown TradeSide: " + tradeSide);
		}

		return processDefinition;
	}

	public ProcessDefinition storeProcessDefinitionDeliveryNote(TradeSide tradeSide, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();

		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);

		// we add the events+actionhandlers
		ActionHandlerNodeEnter.register(jbpmProcessDefinition);

		if (TradeSide.vendor == tradeSide) {
			ActionHandlerFinalizeDeliveryNote.register(jbpmProcessDefinition);
			ActionHandlerBookDeliveryNoteImplicitely.register(jbpmProcessDefinition);
		}

		ActionHandlerBookDeliveryNote.register(jbpmProcessDefinition);

		// store it
		ProcessDefinition processDefinition = ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition, jbpmProcessDefinitionURL);
		ProcessDefinitionID processDefinitionID = (ProcessDefinitionID) JDOHelper.getObjectId(processDefinition);

		switch (tradeSide) {
			case vendor:
			{

				for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsDeliveryNote.Vendor.TRANSITION_NAME_BOOK_IMPLICITELY)) {
					transition.setUserExecutable(false);
				}
			}
			break;
			case customer:
			{
			}
			break;
			default:
				throw new IllegalStateException("Unknown TradeSide: " + tradeSide);
		}

		return processDefinition;
	}
}
