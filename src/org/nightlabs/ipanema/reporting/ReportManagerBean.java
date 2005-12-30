/*
 * Created on Feb 21, 2005
 */
package org.nightlabs.ipanema.reporting;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.nightlabs.ModuleException;
import org.nightlabs.ipanema.base.BaseSessionBeanImpl;
import org.nightlabs.ipanema.base.JFireBaseEAR;
import org.nightlabs.ipanema.reporting.layout.RenderedReportLayout;
import org.nightlabs.ipanema.reporting.layout.ReportCategory;
import org.nightlabs.ipanema.reporting.layout.ReportRegistryItem;
import org.nightlabs.ipanema.reporting.layout.ReportRegistryItemCarrier;
import org.nightlabs.ipanema.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.ipanema.reporting.oda.jdojs.JDOJSResultSet;
import org.nightlabs.ipanema.reporting.oda.jdojs.JDOJSResultSetMetaData;
import org.nightlabs.ipanema.reporting.oda.jdojs.server.ServerJDOJSProxy;
import org.nightlabs.ipanema.reporting.oda.jdoql.JDOQLMetaDataParser;
import org.nightlabs.ipanema.reporting.oda.jdoql.JDOQLResultSetMetaData;
import org.nightlabs.ipanema.reporting.oda.jdoql.server.ServerJDOQLProxy;
import org.nightlabs.ipanema.reporting.platform.RAPlatformContext;
import org.nightlabs.ipanema.reporting.platform.ReportingManager;
import org.nightlabs.ipanema.reporting.platform.ReportingManagerFactory;
import org.nightlabs.ipanema.servermanager.JFireServerManager;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.util.Utils;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @ejb.bean name="ipanema/ejb/JFireReporting/ReportManager"	
 *					 jndi-name="ipanema/ejb/JFireReporting/ReportManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class ReportManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(ReportManagerBean.class);

	/**
	 * @see com.nightlabs.ipanema.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see com.nightlabs.ipanema.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"	
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }
	
	/**
	 * This method is called by the datastore initialization mechanism.
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireReport-admin"
	 * @ejb.transaction type = "Required"
	 */
	public void initialize() 
	throws ModuleException 
	{
		// TODO: Better check if platform initialized. Propose on birt forum.
		if (true) {
			RAPlatformContext platformContext = new RAPlatformContext();
			JFireServerManager ipanemaServerManager = getJFireServerManager();
			try {
				try {
					System.setProperty(
							Platform.PROPERTY_BIRT_HOME,
							Utils.addFinalSlash(
									ipanemaServerManager.getJFireServerConfigModule()
									.getJ2ee().getJ2eeDeployBaseDirectory())+
									"JFireReporting.ear"+File.separator+"birt"+File.separator
					);
					Platform.initialize(platformContext);
				} catch (Throwable t) {			
					LOGGER.log(Level.ERROR, "Initializing BIRT Platform failed!", t);
				}
			} finally {
				ipanemaServerManager.close();
			}
		}
		
		try {
			new ReportingManagerFactory(getInitialContext(getOrganisationID()), getOrganisationID()); // registers itself in JNDI
		} catch (Exception e) {
			LOGGER.error("Creating ReportingManagerFactory for organisation \""+getOrganisationID()+"\" failed!", e);
			throw new ModuleException(e);
		}

		

		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireBaseEAR.MODULE_NAME);
			if (moduleMetaData != null) {
			
				LOGGER.info("Initialization oJFireBase started...");
				
				// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
				moduleMetaData = new ModuleMetaData(
						JFireReportingEAR.MODULE_NAME, "1.0.0-0-beta", "1.0.0-0-beta");
				pm.makePersistent(moduleMetaData);
			}

			// Register internal report categories if not existent			
			ReportCategory offerCat = ReportCategory.getReportCategory(
					pm,
					getOrganisationID(),
					ReportCategory.INTERNAL_CATEGORY_TYPE_OFFER
				);
			if (offerCat == null) {
				offerCat = new ReportCategory(
						pm,
						null,
						getOrganisationID(),
						ReportCategory.INTERNAL_CATEGORY_TYPE_OFFER,
						true
					);
				offerCat.getName().setText(Locale.ENGLISH.getLanguage(), "Offer Layouts");
				offerCat.getName().setText(Locale.GERMAN.getLanguage(), "Angebots-Vorlagen");
				pm.makePersistent(offerCat);
			}
						
			ReportCategory orderCat = ReportCategory.getReportCategory(
					pm,
					getOrganisationID(),
					ReportCategory.INTERNAL_CATEGORY_TYPE_ORDER
				);
			if (orderCat == null) {
				orderCat = new ReportCategory(
						pm,
						null,
						getOrganisationID(),
						ReportCategory.INTERNAL_CATEGORY_TYPE_ORDER,
						true
					);
				orderCat.getName().setText(Locale.ENGLISH.getLanguage(), "Order Layouts");
				orderCat.getName().setText(Locale.GERMAN.getLanguage(), "Auftrags-Vorlagen");
				pm.makePersistent(orderCat);
			}
			
			ReportCategory invoiceCat = ReportCategory.getReportCategory(
					pm,
					getOrganisationID(),
					ReportCategory.INTERNAL_CATEGORY_TYPE_INVOICE
				);
			if (invoiceCat == null) {
				invoiceCat = new ReportCategory(
						pm,
						null,
						getOrganisationID(),
						ReportCategory.INTERNAL_CATEGORY_TYPE_INVOICE,
						true
					);
				invoiceCat.getName().setText(Locale.ENGLISH.getLanguage(), "Invoice Layouts");
				invoiceCat.getName().setText(Locale.GERMAN.getLanguage(), "Rechnungs-Vorlagen");
				pm.makePersistent(invoiceCat);
			}
			
			ReportCategory deliveryNoteCat = ReportCategory.getReportCategory(
					pm,
					getOrganisationID(),
					ReportCategory.INTERNAL_CATEGORY_TYPE_DELIVERY_NOTE
				);
			if (deliveryNoteCat == null) {
				deliveryNoteCat = new ReportCategory(
						pm,
						null,
						getOrganisationID(),
						ReportCategory.INTERNAL_CATEGORY_TYPE_DELIVERY_NOTE,
						true
					);
				deliveryNoteCat.getName().setText(Locale.ENGLISH.getLanguage(), "Deliverynote Layouts");
				deliveryNoteCat.getName().setText(Locale.GERMAN.getLanguage(), "Lieferschein-Vorlagen");
				pm.makePersistent(deliveryNoteCat);
			}
			
		} finally {
			pm.close();
		}
		
		
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public void test()
	throws ModuleException
	{
		JFireServerManager serverManager = getJFireServerManager();
		try {
			ReportingManagerFactory engineFactory = ReportingManagerFactory.getReportingManagerFactory(getInitialContext(getOrganisationID()), getOrganisationID());
			ReportEngine reportEngine =  engineFactory.getReportEngine();
			String filePre = Utils.addFinalSlash(serverManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory())+
			"JFireReporting.ear/birt/";
			IReportRunnable report = reportEngine.openReportDesign(filePre + "testJDO.rptdesign");
//			IReportRunnable report = reportEngine.openReportDesign(filePre + "testreport.rptdesign");
			IRunAndRenderTask task = reportEngine.createRunAndRenderTask(report);
			HTMLRenderOption options = new HTMLRenderOption( );
			options.setOutputFormat( HTMLRenderOption.OUTPUT_FORMAT_HTML );
			options.setOutputFileName( filePre + "testreport.html" );
			task.setRenderOption( options );
			task.run();
//			get
		} catch (Throwable t) {
			throw new ModuleException(t);
		} finally {
			serverManager.close();			
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public IResultSet fetchJDOQLResultSet(
			String queryText, 
			Map parameters,
			JDOQLResultSetMetaData metaData
		) 
	throws ModuleException
	{
		try {
			return ServerJDOQLProxy.executeQuery(
//					getOrganisationID(), 
					queryText, 
					parameters, 
					metaData,
					true,
					new String[] {FetchPlan.ALL}
				);
		} catch (Throwable t) {
			throw new ModuleException(t);
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public JDOQLResultSetMetaData getQueryMetaData(String organisationID, String queryText) 
	throws ModuleException
	{
		try {
			return JDOQLMetaDataParser.parseJDOQLMetaData(queryText);
		} catch (Throwable t) {
			throw new ModuleException(t);
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public JDOJSResultSetMetaData prepareJDOJSQuery(String prepareScript)
	throws ModuleException
	{
		return ServerJDOJSProxy.prepareJDOJSQuery(prepareScript);
	}
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public JDOJSResultSet fetchJDOJSResultSet(
			JDOJSResultSetMetaData metaData, 
			String prepareScript,
			IParameterMetaData parameterMetaData,
			Map parameters
		)
	throws ModuleException
	{
		return ServerJDOJSProxy.fetchJDOJSResultSet(
				metaData, 
				prepareScript,
				parameterMetaData,
				parameters
			);
	}
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ReportRegistryItem getReportRegistryItem (
			ReportRegistryItemID reportRegistryItemID,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportRegistryItem reportRegistryItem = (ReportRegistryItem)pm.getObjectById(reportRegistryItemID);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			ReportRegistryItem result = (ReportRegistryItem) pm.detachCopy(reportRegistryItem);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public List<ReportRegistryItem> getReportRegistryItems (
			List<ReportRegistryItemID> reportRegistryItemIDs,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			List<ReportRegistryItem> result = new ArrayList<ReportRegistryItem>();
			for (ReportRegistryItemID itemID : reportRegistryItemIDs) {
				ReportRegistryItem item = (ReportRegistryItem)pm.getObjectById(itemID);
				result.add((ReportRegistryItem)pm.detachCopy(item));
			}

			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getTopLevelReportRegistryItems (
			String organisationID,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection topLevelItems = ReportRegistryItem.getTopReportRegistryItems(pm, organisationID);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Collection result = (Collection) pm.detachCopyAll(topLevelItems);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<ReportRegistryItemCarrier> getTopLevelReportRegistryItemCarriers (String organisationID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection topLevelItems = ReportRegistryItem.getTopReportRegistryItems(pm, organisationID);
			Collection<ReportRegistryItemCarrier> result = new HashSet<ReportRegistryItemCarrier>();
			for (Iterator iter = topLevelItems.iterator(); iter.hasNext();) {
				ReportRegistryItem item = (ReportRegistryItem) iter.next();
				result.add(new ReportRegistryItemCarrier(null, item, true));
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	
	protected ReportingManagerFactory getReportingManagerFactory()
	throws ModuleException
	{
		try {
			return ReportingManagerFactory.getReportingManagerFactory(getInitialContext(getOrganisationID()), getOrganisationID());
		} catch (NamingException e) {
			throw new ModuleException(e);
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ReportRegistryItem storeRegistryItem (
			ReportRegistryItem reportRegistryItem,
			boolean get,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return (ReportRegistryItem)NLJDOHelper.storeJDO(pm, reportRegistryItem, get, fetchGroups);
		} finally {
			pm.close();
		}
	}
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public RenderedReportLayout renderReportLayout (
			ReportRegistryItemID reportLayoutID,
//			Birt.OutputFormat format,
			Map params,
			String format
//			Map<String,Object> params
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportingManager rm = getReportingManagerFactory().getReportingManager();
			try {
				try {
					return rm.renderReport(pm, reportLayoutID, params, Birt.parseOutputFormat(format));
				} catch (EngineException e) {
					throw new ModuleException(e);
				}
			} finally {
				rm.close();
			}
		} finally {
			pm.close();
		}
	}
	
	
	
	
}
