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

package org.nightlabs.jfire.reporting.trade;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.ReportingInitialiserException;
import org.nightlabs.jfire.reporting.parameter.ReportParameterUtil;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.ValueProviderCategory;
import org.nightlabs.jfire.reporting.parameter.ValueProviderInputParameter;
import org.nightlabs.jfire.reporting.parameter.ReportParameterUtil.NameEntry;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * Bean to initialize the default reports and report scripts.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @ejb.bean name="jfire/ejb/JFireReportingTrade/ReportManagerTrade"	
 *					 jndi-name="jfire/ejb/JFireReportingTrade/ReportManagerTrade"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class ReportManagerTradeBean
extends BaseSessionBeanImpl
implements SessionBean
{
	public static final Logger logger = Logger.getLogger(ReportManagerTradeBean.class);

	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
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
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type = "Required"
	 */
	public void initializeScripting() 
	throws ModuleException 
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {
			
			ScriptingInitializer.initialize(pm, jfireServerManager, Organisation.DEVIL_ORGANISATION_ID);
			
		} finally {
			pm.close();
			jfireServerManager.close();
		}
		
		
	}
	
	/**
	 * This method is called by the datastore initialization mechanism.
	 * @throws ReportingInitializerException 
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireReporting-admin"
	 * @ejb.transaction type = "Required"
	 */
	public void initializeReporting() throws ReportingInitialiserException 
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {
			
			initializeReportParameterAcquisition(pm);
			
			ReportingInitialiser.initialise(pm, jfireServerManager, Organisation.DEVIL_ORGANISATION_ID);
			
			
		} finally {
			pm.close();
			jfireServerManager.close();
		}
		
		
	}
	
	private void initializeReportParameterAcquisition(final PersistenceManager pm) {
		ValueProviderCategoryID categoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_REPORTS);
		ValueProviderCategory rootCategory = ReportParameterUtil.createValueProviderCategory(
				pm, null, categoryID, 
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Trade reports")}
			);
		
		ValueProviderCategoryID leCategoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_LEGAL_ENTITY);
		ValueProviderCategory leCategory = ReportParameterUtil.createValueProviderCategory(
				pm, rootCategory, leCategoryID, 
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Legal entity parameters")}
			);
		
		ReportParameterUtil.createValueProvider(pm, leCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_LEGAL_ENTITY_SEARCH, AnchorID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Customer search")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Customer search")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a customer")}
				);
		
		ValueProviderCategoryID docsCategoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS);
		ReportParameterUtil.createValueProviderCategory(
				pm, null, docsCategoryID, 
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Trade documents parameters")}
			);
		
		ValueProviderCategoryID invoiceCategoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE);
		ValueProviderCategory invoiceCategory = ReportParameterUtil.createValueProviderCategory(
				pm, rootCategory, invoiceCategoryID, 
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice parameters")}
			);
		
		ValueProvider invoiceByCustomer = ReportParameterUtil.createValueProvider(
				pm, invoiceCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_CUSTOMER, ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice of customer")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an invoice")}
			);
		invoiceByCustomer.getInputParameters().clear();
		pm.flush();
		invoiceByCustomer.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "customer", AnchorID.class.getName()));
		
		
		ValueProvider invoiceByCustomerAndPeriod = ReportParameterUtil.createValueProvider(
				pm, invoiceCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_CUSTOMER_AND_PERIOD, ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice of customer in period")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice of customer in period")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an invoice")}
			);
		invoiceByCustomerAndPeriod.getInputParameters().clear();
		pm.flush();
		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "customer", AnchorID.class.getName()));
		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "from", Date.class.getName()));
		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "to", Date.class.getName()));
		
		
		ValueProvider invoiceByArticleType = ReportParameterUtil.createValueProvider(
				pm, invoiceCategory, ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_ARTICLE_TYPE, ArticleContainerID.class.getName(),
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice by article type")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Invoice by article type")},
				new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an invoice")}
			);
		invoiceByArticleType.getInputParameters().clear();
		pm.flush();
		invoiceByArticleType.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "productType", ProductTypeID.class.getName()));		
		
		
		
//		// Configure report layout
//		
//		ReportRegistryItemID defInvoiceID = ReportRegistryItemID.create(
//				Organisation.DEVIL_ORGANISATION_ID, 
//				ReportingTradeConstants.REPORT_REGISTRY_ITEM_TYPE_INVOICE, 
//				ReportingTradeConstants.REPORT_REGISTRY_ITEM_ID_DEFAULT_INVOICE_LAYOUT
//			);
//		ReportLayout defInvoice = (ReportLayout) pm.getObjectById(defInvoiceID);
//		logger.debug("Have report layout");
//		
//		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
//		
//		ReportParameterAcquisitionSetup setup = ReportParameterAcquisitionSetup.getSetupForReportLayout(pm, (ReportRegistryItemID)JDOHelper.getObjectId(defInvoice));
//		if (setup != null) {
//			for (Map.Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> entry : setup.getValueAcquisitionSetups().entrySet()) {
////				pm.deletePersistent(entry.getKey());
////				pm.deletePersistent(entry.getValue());
//				for (ValueConsumerBinding binding : entry.getValue().getValueConsumerBindings()) {
//					pm.deletePersistent(binding);
//				}				
//			}
//			setup.setDefaultSetup(null);
////			pm.flush();
////			setup.getValueAcquisitionSetups().clear();
//			pm.flush();
//			pm.deletePersistent(setup);
//			pm.flush();
//		}
//		setup = new ReportParameterAcquisitionSetup(organisationID, IDGenerator.nextID(ReportParameterAcquisitionSetup.class), defInvoice);
//		setup = (ReportParameterAcquisitionSetup) pm.makePersistent(setup);
////		else  {
////			setup.getValueAcquisitionSetups().clear();
////			pm.flush();
////		}
//		logger.debug("Created NEW setup");
//
//		ReportParameterAcquisitionUseCase defUseCase = new ReportParameterAcquisitionUseCase(setup, ReportParameterAcquisitionUseCase.USE_CASE_ID_DEFAULT);
//		defUseCase.getName().setText(Locale.ENGLISH.getLanguage(), "Default Usecase");
//		logger.debug("Created NEW default use case");
//		
//		ValueAcquisitionSetup valueAcquisitionSetup = new ValueAcquisitionSetup(organisationID, IDGenerator.nextID(ValueAcquisitionSetup.class), setup, defUseCase);
//		logger.debug("Created ValueAcquisitionSetup");
//		List<AcquisitionParameterConfig> parameterConfigs = new ArrayList<AcquisitionParameterConfig>();
//		AcquisitionParameterConfig pc1 = new AcquisitionParameterConfig(valueAcquisitionSetup);
//		pc1.setParameterID("invoiceID");
//		pc1.setParameterType(InvoiceID.class.getName());
//		parameterConfigs.add(pc1);
//		valueAcquisitionSetup.setParameterConfigs(parameterConfigs);
//		logger.debug("Created parameter config");
//		
//		Set<ValueProviderConfig> providerConfigs = new HashSet<ValueProviderConfig>();
//		ValueProviderConfig vpc1 = new ValueProviderConfig(valueAcquisitionSetup, IDGenerator.nextID(ValueProviderConfig.class));
//		vpc1.setValueProvider(leSearch);
//		
//		ValueProviderConfig vpc2 = new ValueProviderConfig(valueAcquisitionSetup, IDGenerator.nextID(ValueProviderConfig.class));
//		vpc2.setValueProvider(invoiceByCustomer);
//		
//		providerConfigs.add(vpc1);
//		providerConfigs.add(vpc2);
//		valueAcquisitionSetup.setValueProviderConfigs(providerConfigs);
//		logger.debug("Created value provider config");
//		
//		Set<ValueConsumerBinding> bindings = new HashSet<ValueConsumerBinding>();
//		ValueConsumerBinding b1 = new ValueConsumerBinding(organisationID, IDGenerator.nextID(ValueConsumerBinding.class), valueAcquisitionSetup);
//		b1.setConsumer(pc1);
//		b1.setParameterID("invoiceID");
//		b1.setProvider(vpc2);
//		
//		ValueConsumerBinding b2 = new ValueConsumerBinding(organisationID, IDGenerator.nextID(ValueConsumerBinding.class), valueAcquisitionSetup);
//		b2.setConsumer(vpc2);
//		b2.setParameterID("customer");
//		b2.setProvider(vpc1);
//		bindings.add(b1);
//		bindings.add(b2);
//		valueAcquisitionSetup.setValueConsumerBindings(bindings);
//		logger.debug("Created bindings");
//
//		setup.getValueAcquisitionSetups().put(defUseCase, valueAcquisitionSetup);
//		setup.setDefaultSetup(valueAcquisitionSetup);
//
//		ValueProviderProvider provider = new ValueProviderProvider() {
//			public ValueProvider getValueProvider(ValueProviderConfig valueProviderConfig) {
//				return (ValueProvider) pm.getObjectById(ValueProviderID.create(valueProviderConfig.getValueProviderOrganisationID(), valueProviderConfig.getValueProviderCategoryID(), valueProviderConfig.getValueProviderID()));
//			}
//		};
//		
//		setup.getDefaultSetup().createAcquisitionSequence(provider);
//		logger.debug("Persisted setup");
	}
}
