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

import java.io.IOException;
import java.io.InputStream;

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
import org.nightlabs.jfire.workstation.Workstation;
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

		ChezFrancoisDatastoreInitialiserLocal initialiser = JFireEjb3Factory.getLocalBean(ChezFrancoisDatastoreInitialiserLocal.class);
		initialiser.configureLocalOrganisation(); // have to do this before createModuleMetaData as it checks for the ModuleMetaData
		initialiser.createModuleMetaData();

		initialiser.createDemoData_JFireSimpleTrade();
		initialiser.createDemoData_JFireVoucher();
		initialiser.createDemoData_JFireDynamicTrade();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.chezfrancois.ChezFrancoisDatastoreInitialiserRemote#createModuleMetaData()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void createModuleMetaData()
	throws MalformedVersionException
	{
		logger.trace("createModuleMetaData: begin");

		PersistenceManager pm = this.createPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireChezFrancois");
			if (moduleMetaData != null)
				return;

			logger.debug("Initialization of JFireChezFrancois started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					"JFireChezFrancois", "0.9.7-0-beta", "0.9.7-0-beta");
			pm.makePersistent(moduleMetaData);

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
		logger.trace("createModuleMetaData: begin");

		PersistenceManager pm = this.createPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireChezFrancois");
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
			logger.trace("createModuleMetaData: end");
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

}
