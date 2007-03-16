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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.ValueProviderCategory;
import org.nightlabs.jfire.reporting.parameter.ValueProviderInputParameter;
import org.nightlabs.jfire.reporting.parameter.config.AcquisitionParameterConfig;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase;
import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ValueConsumerBinding;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderProvider;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.store.id.ProductTypeID;
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
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireReporting-admin"
	 * @ejb.transaction type = "Required"
	 */
	public void initializeReporting() 
	throws ModuleException 
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {
			
			ReportingInitializer.initialize(pm, jfireServerManager, Organisation.DEVIL_ORGANISATION_ID);
			
			initializeReportParameterAcquisition(pm);
			
		} finally {
			pm.close();
			jfireServerManager.close();
		}
		
		
	}
	
	private void initializeReportParameterAcquisition(final PersistenceManager pm) {
		ValueProviderCategoryID categoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_REPORTS);
		ValueProviderCategory rootCategory = createValueProviderCategory(pm, null, categoryID);
		rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Trade reports");
		
		ValueProviderCategoryID leCategoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_LEGAL_ENTITY);
		ValueProviderCategory leCategory = createValueProviderCategory(pm, rootCategory, leCategoryID);
		leCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Legal entity parameters");
		
		ValueProviderID leSearchID = ValueProviderID.create(
				Organisation.DEVIL_ORGANISATION_ID, 
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_LEGAL_ENTITY, 
				ReportingTradeConstants.VALUE_PROVIDER_ID_LEGAL_ENTITY_SEARCH
			);
		ValueProvider leSearch = createValueProvider(pm, leCategory, leSearchID, AnchorID.class.getName());
		leSearch.getName().setText(Locale.ENGLISH.getLanguage(), "Customer search");
		leSearch.getDescription().setText(Locale.ENGLISH.getLanguage(), "Customer search");
		
		ValueProviderCategoryID docsCategoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS);
		ValueProviderCategory docsCategory = createValueProviderCategory(pm, rootCategory, docsCategoryID);
		docsCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Trade documents parameters");
		
		ValueProviderCategoryID invoiceCategoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE);
		ValueProviderCategory invoiceCategory = createValueProviderCategory(pm, rootCategory, invoiceCategoryID);
		invoiceCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Invoice parameters");
		
		ValueProviderID invoiceByCustomerID = ValueProviderID.create(
				Organisation.DEVIL_ORGANISATION_ID, 
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE, 
				ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_CUSTOMER
			);
		ValueProvider invoiceByCustomer = createValueProvider(pm, invoiceCategory, invoiceByCustomerID, InvoiceID.class.getName());
		invoiceByCustomer.getName().setText(Locale.ENGLISH.getLanguage(), "Invoice of customer");
		invoiceByCustomer.getDescription().setText(Locale.ENGLISH.getLanguage(), "Invoice of customer");
		logger.debug("Creating parameters for "+invoiceByCustomerID);
		invoiceByCustomer.getInputParameters().clear();
		pm.flush();
		invoiceByCustomer.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "customer", AnchorID.class.getName()));
		
		
		ValueProviderID invoiceByCustomerAndPeriodID = ValueProviderID.create(
				Organisation.DEVIL_ORGANISATION_ID, 
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE, 
				ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_CUSTOMER_AND_PERIOD
			);
		ValueProvider invoiceByCustomerAndPeriod = createValueProvider(pm, invoiceCategory, invoiceByCustomerAndPeriodID, InvoiceID.class.getName());
		invoiceByCustomerAndPeriod.getName().setText(Locale.ENGLISH.getLanguage(), "Invoice of customer in period");
		invoiceByCustomerAndPeriod.getDescription().setText(Locale.ENGLISH.getLanguage(), "Invoice of customer in period");
		logger.debug("Creating parameters for "+invoiceByCustomerAndPeriodID);
		invoiceByCustomerAndPeriod.getInputParameters().clear();
		pm.flush();
		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "customer", AnchorID.class.getName()));
		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "from", Date.class.getName()));
		invoiceByCustomerAndPeriod.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "to", Date.class.getName()));
		
		
		ValueProviderID invoiceByArticleTypeID = ValueProviderID.create(
				Organisation.DEVIL_ORGANISATION_ID, 
				ReportingTradeConstants.VALUE_PROVIDER_CATEGORY_ID_TRADE_DOCUMENTS_INVOICE, 
				ReportingTradeConstants.VALUE_PROVIDER_ID_TRADE_DOCUMENTS_INVOICE_BY_ARTICLE_TYPE
			);
		ValueProvider invoiceByArticleType = createValueProvider(pm, invoiceCategory, invoiceByArticleTypeID, InvoiceID.class.getName());
		invoiceByArticleType.getName().setText(Locale.ENGLISH.getLanguage(), "Invoice by article type");
		invoiceByArticleType.getDescription().setText(Locale.ENGLISH.getLanguage(), "Invoice by article type");
		logger.debug("Creating parameters for "+invoiceByArticleTypeID);
		invoiceByArticleType.getInputParameters().clear();
		pm.flush();
		invoiceByArticleType.addInputParameter(new ValueProviderInputParameter(invoiceByCustomer, "productType", ProductTypeID.class.getName()));		
		
		
		
		// Configure report layout
		
		ReportRegistryItemID defInvoiceID = ReportRegistryItemID.create(
				Organisation.DEVIL_ORGANISATION_ID, 
				ReportingTradeConstants.REPORT_REGISTRY_ITEM_TYPE_INVOICE, 
				ReportingTradeConstants.REPORT_REGISTRY_ITEM_ID_DEFAULT_INVOICE_LAYOUT
			);
		ReportLayout defInvoice = (ReportLayout) pm.getObjectById(defInvoiceID);
		logger.debug("Have report layout");
		
		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		
		ReportParameterAcquisitionSetup setup = ReportParameterAcquisitionSetup.getSetupForReportLayout(pm, (ReportRegistryItemID)JDOHelper.getObjectId(defInvoice));
		if (setup != null) {
			for (Map.Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> entry : setup.getValueAcquisitionSetups().entrySet()) {
//				pm.deletePersistent(entry.getKey());
//				pm.deletePersistent(entry.getValue());
				for (ValueConsumerBinding binding : entry.getValue().getValueConsumerBindings()) {
					pm.deletePersistent(binding);
				}				
			}
			setup.setDefaultSetup(null);
//			pm.flush();
//			setup.getValueAcquisitionSetups().clear();
			pm.flush();
			pm.deletePersistent(setup);
			pm.flush();
		}
		setup = new ReportParameterAcquisitionSetup(organisationID, IDGenerator.nextID(ReportParameterAcquisitionSetup.class), defInvoice);
		setup = (ReportParameterAcquisitionSetup) pm.makePersistent(setup);
//		else  {
//			setup.getValueAcquisitionSetups().clear();
//			pm.flush();
//		}
		logger.debug("Created NEW setup");

		ReportParameterAcquisitionUseCase defUseCase = new ReportParameterAcquisitionUseCase(setup, ReportParameterAcquisitionUseCase.USE_CASE_ID_DEFAULT);
		defUseCase.getName().setText(Locale.ENGLISH.getLanguage(), "Default Usecase");
		logger.debug("Created NEW default use case");
		
		ValueAcquisitionSetup valueAcquisitionSetup = new ValueAcquisitionSetup(organisationID, IDGenerator.nextID(ValueAcquisitionSetup.class), setup, defUseCase);
		logger.debug("Created ValueAcquisitionSetup");
		List<AcquisitionParameterConfig> parameterConfigs = new ArrayList<AcquisitionParameterConfig>();
		AcquisitionParameterConfig pc1 = new AcquisitionParameterConfig(valueAcquisitionSetup);
		pc1.setParameterID("invoiceID");
		pc1.setParameterType(InvoiceID.class.getName());
		parameterConfigs.add(pc1);
		valueAcquisitionSetup.setParameterConfigs(parameterConfigs);
		logger.debug("Created parameter config");
		
		Set<ValueProviderConfig> providerConfigs = new HashSet<ValueProviderConfig>();
		ValueProviderConfig vpc1 = new ValueProviderConfig(valueAcquisitionSetup);
		vpc1.setValueProvider(leSearch);
		
		ValueProviderConfig vpc2 = new ValueProviderConfig(valueAcquisitionSetup);
		vpc2.setValueProvider(invoiceByCustomer);
		
		providerConfigs.add(vpc1);
		providerConfigs.add(vpc2);
		valueAcquisitionSetup.setValueProviderConfigs(providerConfigs);
		logger.debug("Created value provider config");
		
		Set<ValueConsumerBinding> bindings = new HashSet<ValueConsumerBinding>();
		ValueConsumerBinding b1 = new ValueConsumerBinding(organisationID, IDGenerator.nextID(ValueConsumerBinding.class), valueAcquisitionSetup);
		b1.setConsumer(pc1);
		b1.setParameterID("invoiceID");
		b1.setProvider(vpc2);
		
		ValueConsumerBinding b2 = new ValueConsumerBinding(organisationID, IDGenerator.nextID(ValueConsumerBinding.class), valueAcquisitionSetup);
		b2.setConsumer(vpc2);
		b2.setParameterID("customer");
		b2.setProvider(vpc1);
		bindings.add(b1);
		bindings.add(b2);
		valueAcquisitionSetup.setValueConsumerBindings(bindings);
		logger.debug("Created bindings");

		setup.getValueAcquisitionSetups().put(defUseCase, valueAcquisitionSetup);
		setup.setDefaultSetup(valueAcquisitionSetup);

		ValueProviderProvider provider = new ValueProviderProvider() {
			public ValueProvider getValueProvider(ValueProviderConfig valueProviderConfig) {
				return (ValueProvider) pm.getObjectById(ValueProviderID.create(valueProviderConfig.getValueProviderOrganisationID(), valueProviderConfig.getValueProviderCategoryID(), valueProviderConfig.getValueProviderID()));
			}
		};
		
		setup.getDefaultSetup().createAcquisitionSequence(provider);
		logger.debug("Persisted setup");
	}
	
	private ValueProviderCategory createValueProviderCategory(PersistenceManager pm, ValueProviderCategory parent, ValueProviderCategoryID categoryID) {
		ValueProviderCategory category = null;
		try {
			category = (ValueProviderCategory) pm.getObjectById(categoryID);
			logger.debug("Have ValueProviderCategory "+categoryID);
		} catch (JDOObjectNotFoundException e) {
			logger.debug("Creating ValueProviderCategory "+categoryID);
			category = new ValueProviderCategory(parent, categoryID.organisationID, categoryID.valueProviderCategoryID, true);
			category = (ValueProviderCategory) pm.makePersistent(category);
			logger.debug("Created ValueProviderCategory "+categoryID);
		}
		return category;
	}
	
	private ValueProvider createValueProvider(PersistenceManager pm, ValueProviderCategory category, ValueProviderID valueProviderID, String outputType) {
		ValueProvider valueProvider = null;
		try {
			valueProvider = (ValueProvider) pm.getObjectById(valueProviderID);
			logger.debug("Have ValueProvider "+valueProviderID);
		} catch (JDOObjectNotFoundException e) {
			logger.debug("Creating ValueProvider "+valueProviderID);
			valueProvider = new ValueProvider(category, valueProviderID.valueProviderID, outputType);
			valueProvider = (ValueProvider) pm.makePersistent(valueProvider);
			logger.debug("Created ValueProvider "+valueProviderID);
		}
		return valueProvider;
	}
}
