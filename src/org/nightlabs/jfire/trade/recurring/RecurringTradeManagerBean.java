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

package org.nightlabs.jfire.trade.recurring;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.version.MalformedVersionException;


/**
 * @ejb.bean name="jfire/ejb/JFireTrade/RecurringTradeManager"
 *					 jndi-name="jfire/ejb/JFireTrade/RecurringTradeManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class RecurringTradeManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(RecurringTradeManagerBean.class);
	
	private static final String RECURRING_OFFER_PROCESS_DEFINITION_NAME_VENDOR = "RecurringOffer.Vendor";

	////////////////////// EJB "constuctor" ////////////////////////////

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
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

	// //// begin EJB stuff ////
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext ctx)
		throws EJBException, RemoteException
	{
		super.setSessionContext(ctx);
	}
	// //// end EJB stuff ////

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise()
	throws IOException, MalformedVersionException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			RecurringTrader recurringTrader = RecurringTrader.getRecurringTrader(pm);
			
			// get Extents for the new classes is necessary because otherwise 
			// DataNucleus creates queries on non-existing tables when no instance of those
			// classes was persisted yet and the tables were not lazily created
			pm.getExtent(RecurringOrder.class);
			pm.getExtent(RecurringOffer.class);
			pm.getExtent(RecurredOffer.class);
			
			// persist process definitions
			ProcessDefinitionID processDefinitionOfferVendorID = ProcessDefinitionID.create(getOrganisationID(), RECURRING_OFFER_PROCESS_DEFINITION_NAME_VENDOR);
			ProcessDefinition processDefinitionOfferVendor;
			try {
				processDefinitionOfferVendor = (ProcessDefinition) pm.getObjectById(processDefinitionOfferVendorID);
			} catch (JDOObjectNotFoundException e) {
				processDefinitionOfferVendor = recurringTrader.storeProcessDefinitionRecurringOffer(TradeSide.vendor, ProcessDefinitionAssignment.class.getResource("recurring/offer/vendor/"));
				pm.makePersistent(new ProcessDefinitionAssignment(RecurringOffer.class, TradeSide.vendor, processDefinitionOfferVendor));
			}
			// TODO: Need process definitions for customer side later
			
			
		} finally {
			pm.close();
		}
	}

}
