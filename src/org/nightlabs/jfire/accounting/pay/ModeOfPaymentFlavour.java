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

package org.nightlabs.jfire.accounting.pay;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.id.CustomerGroupID;

/**
 * A <tt>ModeOfPaymentFlavour</tt> is a subkind of <tt>ModeOfPayment</tt>. An example
 * might be "Credit Card" as <tt>ModeOfPayment</tt> and "VISA" or "Master" as
 * <tt>ModeOfPaymentFlavour</tt>. All flavours of one mode of payment require the same
 * data for processing.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID"
 *		detachable="true"
 *		table="JFireTrade_ModeOfPaymentFlavour"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @!jdo.query // this fails with: Cannot perform operation "in" on UnboundVariable and MapValueLiteral "(?,?)"
 *		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup"
 *		query="SELECT
 *			WHERE
 *				paramCustomerGroup.modeOfPaymentFlavours.containsValue(this) ||
 *				(
 *					paramCustomerGroup.modeOfPayments.containsValue(modeOfPayment) &&
 *			 		modeOfPayment.flavours.containsValue(this)
 *				)
 *			PARAMETERS CustomerGroup paramCustomerGroup
 *			VARIABLES ModeOfPayment modeOfPayment
 *			IMPORTS import org.nightlabs.jfire.trade.CustomerGroup; import org.nightlabs.jfire.accounting.pay.ModeOfPayment"
 *
 * @!jdo.query
 *		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup"
 *		query="SELECT
 *			WHERE
 *				(
 *					customerGroup.organisationID == paramOrganisationID &&
 *					customerGroup.customerGroupID == paramCustomerGroupID
 *				)
 *				&&
 *				(
 *					customerGroup.modeOfPaymentFlavours.containsValue(this) ||
 *					(
 *						customerGroup.modeOfPayments.containsValue(modeOfPayment) &&
 *				 		modeOfPayment.flavours.containsValue(this)
 *					)
 *				)
 *			PARAMETERS String paramOrganisationID, String paramCustomerGroupID
 *			VARIABLES CustomerGroup customerGroup; ModeOfPayment modeOfPayment
 *			IMPORTS
 *				import java.lang.String;
 *				import org.nightlabs.jfire.trade.CustomerGroup;
 *				import org.nightlabs.jfire.accounting.pay.ModeOfPayment"
 *
 * @jdo.query
 *		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND1"
 *		query="SELECT
 *			WHERE
 *				customerGroup.organisationID == paramOrganisationID &&
 *				customerGroup.customerGroupID == paramCustomerGroupID
 *				&&
 *				customerGroup.modeOfPaymentFlavours.containsValue(this)
 *			PARAMETERS String paramOrganisationID, String paramCustomerGroupID
 *			VARIABLES CustomerGroup customerGroup
 *			IMPORTS
 *				import java.lang.String;
 *				import org.nightlabs.jfire.trade.CustomerGroup"
 *
 * @jdo.query
 *		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND2"
 *		query="SELECT
 *			WHERE
 *				customerGroup.organisationID == paramOrganisationID &&
 *				customerGroup.customerGroupID == paramCustomerGroupID
 *				&&
 *				customerGroup.modeOfPayments.containsValue(modeOfPayment) &&
 *				modeOfPayment.flavours.containsValue(this)
 *			PARAMETERS String paramOrganisationID, String paramCustomerGroupID
 *			VARIABLES CustomerGroup customerGroup; ModeOfPayment modeOfPayment
 *			IMPORTS
 *				import java.lang.String;
 *				import org.nightlabs.jfire.trade.CustomerGroup;
 *				import org.nightlabs.jfire.accounting.pay.ModeOfPayment"
 *
 * @jdo.fetch-group name="ModeOfPaymentFlavour.name" fields="name"
 * @jdo.fetch-group name="ModeOfPaymentFlavour.modeOfPayment" fields="modeOfPayment"
 * @jdo.fetch-group name="ModeOfPaymentFlavour.this" fetch-groups="default" fields="modeOfPayment, name"
 */
public class ModeOfPaymentFlavour
implements Serializable
{
	public static final String FETCH_GROUP_NAME = "ModeOfPaymentFlavour.name";
	public static final String FETCH_GROUP_MODE_OF_PAYMENT = "ModeOfPaymentFlavour.modeOfPayment";
	public static final String FETCH_GROUP_THIS_MODE_OF_PAYMENT_FLAVOUR = "ModeOfPaymentFlavour.this";

	public static final byte MERGE_MODE_ADDITIVE = 1;
	public static final byte MERGE_MODE_SUBTRACTIVE = 2;
	
	/**
	 * @param customerGroupIDs A <tt>Collection</tt> of {@link CustomerGroupID}. Can be <tt>null</tt> in order to return all <tt>ModeOfPaymentFlavour</tt>s.
	 * @param mergeMode Whether the intersection or the combination of all <tt>CustomerGroup</tt> configurations shall be used.
	 *
	 * @return Returns those <tt>ModeOfPaymentFlavour</tt>s that are available for all given
	 *		<tt>CustomerGroup</tt>s. If <tt>mergeMode</tt> is {@link #MERGE_MODE_ADDITIVE},
	 *		they are combined like SQL UNION would do (means, if at least one
	 *		<tt>CustomerGroup</tt>
	 *		contains a certain <tt>ModeOfPaymentFlavour</tt>, it will be in the result).
	 *		If <tt>mergeMode</tt> is {@link #MERGE_MODE_SUBTRACTIVE}, only those
	 *		<tt>ModeOfPaymentFlavour</tt>s are returned that are available to all
	 *		<tt>CustomerGroup</tt>s.
	 */
	public static Collection getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
			PersistenceManager pm, Collection customerGroupIDs, byte mergeMode)
	{
		if (customerGroupIDs == null)
			return (Collection)pm.newQuery(ModeOfPaymentFlavour.class).execute();


		if (mergeMode != MERGE_MODE_ADDITIVE && mergeMode != MERGE_MODE_SUBTRACTIVE)
			throw new IllegalArgumentException("mergeMode invalid! Must be MERGE_MODE_ADDITIVE or MERGE_MODE_SUBTRACTIVE!");

		Map res = null;
		for (Iterator itCustomerGroups = customerGroupIDs.iterator(); itCustomerGroups.hasNext(); ) {
			CustomerGroupID customerGroupID = (CustomerGroupID) itCustomerGroups.next();

			Map m = getAvailableModeOfPaymentFlavoursMapForOneCustomerGroup(pm, customerGroupID.organisationID, customerGroupID.customerGroupID);

			if (res == null) {
				res = m;
			}
			else {
				if (mergeMode == MERGE_MODE_SUBTRACTIVE) {
					// remove all missing
					for (Iterator it = res.keySet().iterator(); it.hasNext(); ) {
						String mopfPK = (String) it.next();
						if (!m.containsKey(mopfPK))
							it.remove();
					}
				} // if (mergeMode == MERGE_MODE_SUBTRACTIVE) {
				else { // if (mergeMode == MERGE_MODE_ADDITIVE) {
					// add all additional
					for (Iterator it = res.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry me = (Map.Entry)it.next();
						String mopfPK = (String) me.getKey();
						ModeOfPaymentFlavour mopf = (ModeOfPaymentFlavour) me.getValue();
						res.put(mopfPK, mopf);
					}
				} // if (mergeMode == MERGE_MODE_ADDITIVE) {
			}
		}

		return res.values();
	}
	
	/**
	 * @see #getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager, String, String)
	 */
	public static Collection getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager pm, CustomerGroupID customerGroupID)
	{
//		pm.getExtent(CustomerGroup.class);
//		CustomerGroup customerGroup = (CustomerGroup) pm.getObjectById(customerGroupID);
		return getAvailableModeOfPaymentFlavoursForOneCustomerGroup(
				pm, customerGroupID.organisationID, customerGroupID.customerGroupID);
	}

//	/**
//	 * This method finds all <tt>ModeOfPaymentFlavour</tt>s which are assigned to
//	 * a {@link CustomerGroup} - either directly or indirectly via its owner
//	 * {@link ModeOfPayment}.
//	 *
//	 * @param pm The <tt>PersistenceManager</tt> with which to access the datastore.
//	 * @param customerGroup The <tt>CustomerGroup</tt> for which to find the <tt>ModeOfPaymentFlavour</tt>s.
//	 *
//	 * @return A <tt>Collection</tt> with instances of type <tt>ModeOfPaymentFlavour</tt>.
//	 */
//	public static Collection getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager pm, CustomerGroup customerGroup)
//	{
//		Query query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAvailableModeOfPaymentFlavoursForOneCustomerGroup");
//		return (Collection)query.execute(customerGroup);
//	}

	/**
	 * This method finds all <tt>ModeOfPaymentFlavour</tt>s which are assigned to
	 * a {@link CustomerGroup} - either directly or indirectly via its owner
	 * {@link ModeOfPayment}.
	 *
	 * @param pm The <tt>PersistenceManager</tt> with which to access the datastore.
	 * @param organisationID The first part of the primary key of the <tt>CustomerGroup</tt>.
	 * @param customerGroupID The second part of the primary key of the <tt>CustomerGroup</tt>.
	 *
	 * @return A <tt>Collection</tt> with instances of type <tt>ModeOfPaymentFlavour</tt>.
	 *
	 * @see #getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager, CustomerGroupID)
	 */
	public static Collection getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager pm, String organisationID, String customerGroupID)
	{
		return getAvailableModeOfPaymentFlavoursMapForOneCustomerGroup(
				pm, organisationID, customerGroupID).values();
	}

	protected static Map getAvailableModeOfPaymentFlavoursMapForOneCustomerGroup(PersistenceManager pm, String organisationID, String customerGroupID)
	{
		// return getAvailableModeOfPaymentFlavoursForOneCustomerGroup(pm, CustomerGroupID.create(organisationID, customerGroupID));

//		Query query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAvailableModeOfPaymentFlavoursForOneCustomerGroup");
//		return (Collection)query.execute(organisationID, customerGroupID);

		// WORKAROUND The normal query returns an empty result, probably because of issues with ORs. 
		Map m = new HashMap();
		Query query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND1");
		for (Iterator it = ((Collection)query.execute(organisationID, customerGroupID)).iterator(); it.hasNext(); ) {
			ModeOfPaymentFlavour modeOfPaymentFlavour = (ModeOfPaymentFlavour) it.next();
			m.put(modeOfPaymentFlavour.getPrimaryKey(), modeOfPaymentFlavour);
		}

		query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND2");
		for (Iterator it = ((Collection)query.execute(organisationID, customerGroupID)).iterator(); it.hasNext(); ) {
			ModeOfPaymentFlavour modeOfPaymentFlavour = (ModeOfPaymentFlavour) it.next();
			m.put(modeOfPaymentFlavour.getPrimaryKey(), modeOfPaymentFlavour);
		}

		return m;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String modeOfPaymentFlavourID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ModeOfPayment modeOfPayment;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="modeOfPaymentFlavour"
	 */
	private ModeOfPaymentFlavourName name;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ModeOfPaymentFlavour()
	{
	}

	public ModeOfPaymentFlavour(String organisationID, String modeOfPaymentFlavourID, ModeOfPayment modeOfPayment)
	{
		this.organisationID = organisationID;
		this.modeOfPaymentFlavourID = modeOfPaymentFlavourID;
		this.primaryKey = getPrimaryKey(organisationID, modeOfPaymentFlavourID);
		this.modeOfPayment = modeOfPayment;
		this.name = new ModeOfPaymentFlavourName(this);
	}

	/**
	 * @return Returns the modeOfPayment.
	 */
	public ModeOfPayment getModeOfPayment()
	{
		return modeOfPayment;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the modeOfPaymentFlavourID.
	 */
	public String getModeOfPaymentFlavourID()
	{
		return modeOfPaymentFlavourID;
	}

	public static String getPrimaryKey(String organisationID, String modeOfPaymentFlavourID)
	{
		return organisationID + '/' + modeOfPaymentFlavourID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	/**
	 * @return Returns the name.
	 */
	public ModeOfPaymentFlavourName getName()
	{
		return name;
	}
}
