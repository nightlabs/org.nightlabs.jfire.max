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

package org.nightlabs.jfire.chezfrancois;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.server.data.dir.JFireServerDataDirectory;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.util.IOUtil;
import org.nightlabs.version.MalformedVersionException;


/**
 * @ejb.bean name="jfire/ejb/JFireChezFrancois/ChezFrancoisDatastoreInitialiser"
 *					 jndi-name="jfire/ejb/JFireChezFrancois/ChezFrancoisDatastoreInitialiser"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ChezFrancoisDatastoreInitialiserBean
extends BaseSessionBeanImpl
implements ChezFrancoisDatastoreInitialiserRemote, ChezFrancoisDatastoreInitialiserLocal
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ChezFrancoisDatastoreInitialiserBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.chezfrancois.ChezFrancoisDatastoreInitialiserRemote#initialise()
	 */
	@RolesAllowed("_System_")
	public void initialise()
	throws Exception
	{
		if (hasRootOrganisation() && getOrganisationID().equals(getRootOrganisationID()))
			return;
		
		if (!haveToInitializeOrganisation()) {
			return;
		}
		
		JFireServerDataDirectory.getJFireServerDataDirFile();

		ChezFrancoisDatastoreInitialiserLocal initialiser = JFireEjb3Factory.getLocalBean(ChezFrancoisDatastoreInitialiserLocal.class);
		initialiser.configureLocalOrganisation(); // have to do this before createModuleMetaData as it checks for the ModuleMetaData
		initialiser.createModuleMetaData();

		initialiser.createDemoData_JFireSimpleTrade();
		initialiser.createDemoData_JFireVoucher();
		initialiser.createDemoData_JFireDynamicTrade();

		// --- 8< --- KaiExperiments: since 10.07.2009 ---------------------------------------------------------|
		initialiser.createDemoData_JFireIssueTracking();
		// ------ KaiExperiments ----- >8 ----------------------------------------------------------------------|
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.chezfrancois.ChezFrancoisDatastoreInitialiserRemote#createModuleMetaData()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void createModuleMetaData()
	throws MalformedVersionException, IOException
	{
		logger.trace("createModuleMetaData: begin");

		PersistenceManager pm = this.createPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireChezFrancoisEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.debug("Initialization of JFireChezFrancois started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = pm.makePersistent(
					ModuleMetaData.createModuleMetaDataFromManifest(JFireChezFrancoisEAR.MODULE_NAME, JFireChezFrancoisEAR.class)
			);

			Workstation ws = new Workstation(getOrganisationID(), "ws00");
			ws.setDescription("Workstation 00");
			ws = pm.makePersistent(ws);

		} finally {
			pm.close();
			logger.trace("createModuleMetaData: end");
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.chezfrancois.ChezFrancoisDatastoreInitialiserRemote#configureLocalOrganisation()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void configureLocalOrganisation()
	throws MalformedVersionException, DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException
	{
		logger.trace("configureLocalOrganisation: begin");

		PersistenceManager pm = this.createPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireChezFrancoisEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.debug("Configuring JFireChezFrancois local organisation...");

			LocalOrganisation org = LocalOrganisation.getLocalOrganisation(pm);
			Person person = org.getOrganisation().getPerson();
			if (person == null) {
				person = new Person(org.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
				org.getOrganisation().setPerson(person);
				person = org.getOrganisation().getPerson();
			}
			pm.getFetchPlan().setGroups(
					FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_DATA_FIELDS, PropertySet.FETCH_GROUP_FULL_DATA
				);
			Person detachedPerson = pm.detachCopy(person);
//			StructLocal structLocal = StructLocal.getStructLocal(Person.class, Struct.DEFAULT_SCOPE, StructLocal.DEFAULT_SCOPE, pm);
			IStruct structLocal = PersonStruct.getPersonStructLocal(pm);
			detachedPerson.inflate(structLocal);
			ImageDataField photoField = (ImageDataField) detachedPerson.getDataField(PersonStruct.PERSONALDATA_PHOTO);
			InputStream in = getClass().getResourceAsStream("resource/jfire-logo.jpg");
			if (in != null) {
				try {
					photoField.loadStream(in, "jfire-logo.jpg", "image/jpeg");
				} catch (IOException e) {
					logger.error("Error loading image", e);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						logger.error("Error loading image", e);
					}
				}
			}
			detachedPerson.deflate();
			pm.makePersistent(detachedPerson);
		} finally {
			pm.close();
			logger.trace("configureLocalOrganisation: end");
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.chezfrancois.ChezFrancoisDatastoreInitialiserRemote#createDemoData_JFireVoucher()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	public void createDemoData_JFireVoucher()
	throws Exception
	{
		try {
			Class.forName("org.nightlabs.jfire.voucher.store.VoucherType");
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireVoucher is not deployed. Cannot create demo data for this module.");
			return;
		}

		PersistenceManager pm = this.createPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				new InitialiserVoucher(pm, getPrincipal()).createDemoData();
				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.chezfrancois.ChezFrancoisDatastoreInitialiserRemote#createDemoData_JFireDynamicTrade()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	public void createDemoData_JFireDynamicTrade()
	throws Exception
	{
		try {
			Class.forName("org.nightlabs.jfire.dynamictrade.store.DynamicProductType");
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireDynamicTrade is not deployed. Cannot create demo data for this module.");
			return;
		}

		PersistenceManager pm = this.createPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				new InitialiserDynamicTrade(pm, getPrincipal()).createDemoData();
				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.chezfrancois.ChezFrancoisDatastoreInitialiserRemote#createDemoData_JFireSimpleTrade()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	public void createDemoData_JFireSimpleTrade()
	throws Exception
	{
		try {
			Class.forName("org.nightlabs.jfire.simpletrade.store.SimpleProductType");
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireSimpleTrade is not deployed. Cannot create demo data for this module.");
			return;
		}

		PersistenceManager pm = this.createPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				new InitialiserSimpleTrade(pm, getPrincipal()).createDemoData();
				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}
		} finally {
			pm.close();
		}
	}


	// --- 8< --- KaiExperiments: since 10.07.2009 ---------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.chezfrancois.ChezFrancoisDatastoreInitialiserRemote#createDemoData_JFireIssueTracking()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	public void createDemoData_JFireIssueTracking()
	throws Exception
	{
		try {
			Class.forName("org.nightlabs.jfire.issue.IssueType");
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireIssueTracking is not deployed. Cannot create demo data for this module.");
			return;
		}

		PersistenceManager pm = this.createPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				new InitialiserIssueTracking(pm, getPrincipal()).createDemoData();
				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}
		} finally {
			pm.close();
		}
	}
	// ------ KaiExperiments ----- >8 ----------------------------------------------------------------------|
	

	/**
	 * Check whether the organisation this initializer runs for has to be intialized with demo data.
	 * This will look in the JFireServerDataDirectory (directory JFireChezFrancois) for a file named
	 * <code>JFireChezFrancois-initialize.properties</code>. 
	 * See the default JFireChezFrancois-initialize.properties file for an explanation of its contents. 
	 */
	private boolean haveToInitializeOrganisation() {
		try {
			return checkInitializeOrganisation(getOrganisationID());
		} catch (Exception e) {
			logger.error(
					"Will not initialize organisation " + getOrganisationID() + 
					" with JFireChezFrancois demo data because an exception was thrown.", 
					e);
			return false;
		}
	}
	
	private boolean checkInitializeOrganisation(String organisationID) throws IOException {
		File initConfDir = new File(JFireServerDataDirectory.getJFireServerDataDirFile(), "JFireChezFrancois");
		File initConfFile = new File(initConfDir, "JFireChezFrancois-initialize.properties");
		if (!initConfFile.exists()) {
			if (!initConfDir.mkdirs()) {
				throw new IOException("Could not create JFireChezFrancois-initialize config directory : " + initConfDir);
			}
			IOUtil.copyResource(
					ChezFrancoisDatastoreInitialiserBean.class, 
					"resource/JFireChezFrancois-initialize.properties", 
					initConfFile);
		}
		
		Properties props = new Properties();
		FileReader propsReader = new FileReader(initConfFile);
		try {
			props.load(propsReader);
		} finally {
			if (propsReader != null) {
				propsReader.close();
			}
		}
		
		Collection<Pattern> checkPatterns = new LinkedList<Pattern>();  
		int i = 0;
		String propValue = props.getProperty("initialize.organisationID." + (i++));
		while (propValue != null) {
			propValue = propValue.trim();
			if (propValue.startsWith("/") && propValue.endsWith("/")) {
				checkPatterns.add(Pattern.compile(propValue.substring(1, propValue.length() - 1)));
			} else {
				checkPatterns.add(Pattern.compile(Pattern.quote(propValue)));
			}
			propValue = props.getProperty("initialize.organisationID." + (i++));
		}
		
		for (Pattern pattern : checkPatterns) {
			if (pattern.matcher(organisationID).matches()) {
				return true;
			}
		}
		logger
				.info(getClass().getSimpleName() + " checked "
						+ checkPatterns.size()
						+ " configuration patterns for organisation " + organisationID + ", none matched, "
						+ "no demo-data will be created for this organisation.");
		return false;
	}
}
