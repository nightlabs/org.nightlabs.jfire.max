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

package org.nightlabs.jfire.trade;

import java.util.Map;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.LegalEntity"
 *		detachable="true"
 *		table="JFireTrade_OrganisationLegalEntity"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="OrganisationLegalEntity.organisation" fields="organisation"
 * @jdo.fetch-group name="OrganisationLegalEntity.this" fetch-groups="default" fields="organisation"
 */
public class OrganisationLegalEntity extends LegalEntity
//implements StoreCallback
{
	public static final String FETCH_GROUP_ORGANISATION = "OrganisationLegalEntity.organisation";
	public static final String FETCH_GROUP_THIS_ORGANISATION_LEGAL_ENTITY = "OrganisationLegalEntity.this";
	
	public static final String ANCHOR_TYPE_ID_ORGANISATION = "Organisation";

	public static String getPrimaryKey(String organisationID, String anchorTypeID)
	{
		return Anchor.getPrimaryKey(organisationID, anchorTypeID, OrganisationLegalEntity.class.getName());
	}

//	/**
//	 * key: String orderPK<br/>
//	 * value: Order order
//	 * <br/><br/>
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="Order"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 */
//	private Map orders = new HashMap();

//	private String localOrganisationID;
//	private boolean localOrganisation;

	protected OrganisationLegalEntity() { }

	// TODO Why do I have to pass the anchorTypeID? Isn't this the same for all OrganisationLegalEntities?! Marco.
	protected OrganisationLegalEntity(org.nightlabs.jfire.organisation.Organisation organisation, String anchorTypeID)
	{
		super(organisation.getOrganisationID(), anchorTypeID, OrganisationLegalEntity.class.getName());
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("*************OrganisationLegalEntity****************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		// TODO: The person and organisation should be set here, but is not because of JPOX but, see
//		this.organisation = organisation;
//		this.setPerson(organisation.getPerson());
		
//		PersistenceManager pm = JDOHelper.getPersistenceManager(organisation);
//		if (pm == null)
//			throw new NullPointerException("organisation has no PersistenceManager attached! Can use this constructor only with a persistent non-detached organisation.");
//		localOrganisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
//		localOrganisation = localOrganisationID.equals(getOrganisationID());
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private org.nightlabs.jfire.organisation.Organisation organisation;

	/**
	 * @return Returns the organisation.
	 */
	public org.nightlabs.jfire.organisation.Organisation getOrganisation()
	{
		return organisation;
	}
	

//	/**
//	 * @return Returns the localOrganisation.
//	 */
//	public boolean isLocalOrganisation()
//	{
//		return localOrganisation;
//	}
//	/**
//	 * @return Returns the localOrganisationID.
//	 */
//	protected String getLocalOrganisationID()
//	{
//		return localOrganisationID;
//	}

	/**
	 * @param pm The {@link PersistenceManager} to use.
	 * @param organisationID The id specifying the <tt>Organisation</tt> for which the <tt>LegalEntity</tt> is desired.
	 * @param anchorTypeID Use public static finals declared in this class
	 * @param throwExceptionIfNotExistent If <tt>false</tt>, this method will return null,	 * 
	 * if the {@link Organisation} specified by <tt>organisationID</tt> does not exist.  
	 *
	 * @return the <tt>OrganisationLegalEntity</tt> for the given <tt>organisationID</tt>.
	 * If the <tt>OrganisationLegalEntity</tt> does not exist, it will be automatically
	 * created, if the {@link Organisation} exists. If it does not exist, this method
	 * will either throw an exception or return <tt>null</tt> - dependent on the parameter
	 * <tt>throwExceptionIfNotExistent</tt>.  
	 */
	// TODO Why do I have to pass the anchorTypeID? Isn't this the same for all OrganisationLegalEntities?! Marco.
	public static OrganisationLegalEntity getOrganisationLegalEntity(
			PersistenceManager pm, String organisationID, String anchorTypeID, boolean throwExceptionIfNotExistent)
	{
		OrganisationLegalEntity organisationLegalEntity = null; 
		try {
			pm.getExtent(OrganisationLegalEntity.class);
			organisationLegalEntity = (OrganisationLegalEntity)pm.getObjectById(
					AnchorID.create(organisationID, anchorTypeID, OrganisationLegalEntity.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			Organisation organisation = Organisation.getOrganisation(
					pm, organisationID, throwExceptionIfNotExistent);

			if (organisationID != null) {
				
				organisationLegalEntity = new OrganisationLegalEntity(organisation, anchorTypeID);
				try {
					organisationLegalEntity = (OrganisationLegalEntity) pm.makePersistent(organisationLegalEntity);
				} catch (JDODataStoreException workaround) {
					System.out.println();
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("*****OrganisationLegalEntity workaround**********");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
					System.out.println("******************************************");
				}
				// TODO: This should be done in the constructor of OrganisationLegalEntity
				organisationLegalEntity.organisation = organisation;
				organisationLegalEntity.setPerson(organisation.getPerson());
			}
		}
		return organisationLegalEntity;
	}

//	public void jdoPreStore() {
//		if (getPerson() == null)
//			setPerson(organisation.getPerson());
//	}
}
