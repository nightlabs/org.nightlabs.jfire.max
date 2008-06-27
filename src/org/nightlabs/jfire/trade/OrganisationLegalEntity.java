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

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * All organisations known by a JFire organisation are represented in its datastore
 * with instances of this class. Note, that this also includes the local organisation. 
 * The primary key of the instances will be made up using
 * the following schema:
 * <ul>
 *   <li>organisationID: The organistionID of the represented organisation.</li>
 *   <li>anchorTypeID: {@link LegalEntity#ANCHOR_TYPE_ID_LEGAL_ENTITY}</li>
 *   <li>anchorID: fully qualified class name of {@link OrganisationLegalEntity}</li>
 * </ul> 
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
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
implements StoreCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_ORGANISATION = "OrganisationLegalEntity.organisation";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_ORGANISATION_LEGAL_ENTITY = "OrganisationLegalEntity.this";

//	public static final String ANCHOR_TYPE_ID_ORGANISATION = "Organisation";

	public static String getPrimaryKey(String organisationID, String anchorTypeID)
	{
		return Anchor.getPrimaryKey(organisationID, anchorTypeID, OrganisationLegalEntity.class.getName());
	}

	protected OrganisationLegalEntity() { }

	protected OrganisationLegalEntity(org.nightlabs.jfire.organisation.Organisation organisation)
	{
		super(organisation.getOrganisationID(), OrganisationLegalEntity.class.getName());
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("*************OrganisationLegalEntity****************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
//		System.out.println("******************************************");
		// TODO: The person and organisation should be set here, but is not because of JPOX bug, see public static OrganisationLegalEntity getOrganisationLegalEntity(...)
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
	private Organisation organisation;

	/**
	 * @return Returns the organisation.
	 */
	public Organisation getOrganisation()
	{
		if (organisation == null)
			organisation = Organisation.getOrganisation(getPersistenceManager(), getOrganisationID());

		return organisation;
	}

	/**
	 * @param pm The {@link PersistenceManager} to use.
	 * @param organisationID The id specifying the <tt>Organisation</tt> for which the <tt>LegalEntity</tt> is desired.
	 * @return the <tt>OrganisationLegalEntity</tt> for the given <tt>organisationID</tt>.
	 * If the <tt>OrganisationLegalEntity</tt> does not exist, it will be automatically
	 * created, if the {@link Organisation} exists. If it does not exist, this method
	 * will either throw an exception or return <tt>null</tt> - dependent on the parameter
	 * <tt>throwExceptionIfNotExistent</tt>.
	 */
	public static OrganisationLegalEntity getOrganisationLegalEntity(
			PersistenceManager pm,
			String organisationID
	)
	{
		OrganisationLegalEntity organisationLegalEntity = null;
		AnchorID orgAnchorID = getOrganisationLegalEntityID(organisationID);
		try {
			pm.getExtent(OrganisationLegalEntity.class);
			organisationLegalEntity = (OrganisationLegalEntity)pm.getObjectById(orgAnchorID);
		} catch (JDOObjectNotFoundException e) {
			Organisation organisation = Organisation.getOrganisation(pm, organisationID, true);

			if (organisation != null) {
				organisationLegalEntity = new OrganisationLegalEntity(organisation);
				try {
					organisationLegalEntity = pm.makePersistent(organisationLegalEntity);
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

				// We MUST NOT call Trader.getTrader(...) here if it's the local organisation, because this would result in an endless recursion.
				// This method here is called by Accounting.getAccounting(...) and Trader.getTrader(...) calls Accounting.getAccounting(...).
				// As the mandator-organisation doesn't need a customer-group anyway, it's no problem to filter out this situation.
				if (!organisationID.equals(LocalOrganisation.getLocalOrganisation(pm).getOrganisationID())) {
					organisationLegalEntity.setDefaultCustomerGroup(Trader.getTrader(pm).getDefaultCustomerGroupForKnownCustomer());
					// TODO should we use another defaultCustomerGroup for organisation-legal-entities?
				}
			}
		}
		return organisationLegalEntity;
	}


	public void jdoPreStore() {
		// TODO JPOX Workaround - would be great to set this automatically
//		if (organisation == null)
//			organisation = Organisation.getOrganisation(getPersistenceManager(), getOrganisationID());
//		if (getPerson() == null)
//			setPerson(organisation.getPerson());
	}
	
	/**
	 * Returns the {@link AnchorID} referencing the {@link OrganisationLegalEntity}
	 * of the given organisationID.
	 * 
	 * @param organisationID The id of the organisation to reference.
	 * @return The {@link AnchorID} referencing the {@link OrganisationLegalEntity}
	 * of the given organisationID.
	 */
	public static AnchorID getOrganisationLegalEntityID(String organisationID) {
		return AnchorID.create(organisationID, ANCHOR_TYPE_ID_LEGAL_ENTITY, OrganisationLegalEntity.class.getName());
	}
	
}
