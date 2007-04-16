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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.accounting.pay.ModeOfPayment;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.util.Utils;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.CustomerGroupID"
 *		detachable="true"
 *		table="JFireTrade_CustomerGroup"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, customerGroupID"
 *		include-body="id/CustomerGroupID.body.inc"
 *
 * @jdo.fetch-group name="CustomerGroup.name" fields="name"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="name"
 */
public class CustomerGroup implements Serializable
{
	public static final String CUSTOMER_GROUP_ID_ANONYMOUS = "CustomerGroup-anonymous";
	public static final String CUSTOMER_GROUP_ID_DEFAULT = "CustomerGroup-default";

	public static final String FETCH_GROUP_NAME = "CustomerGroup.name";

//	public static final String DEFAULT_CUSTOMER_GROUP_ID = "default";
//
//	public static CustomerGroup getDefaultCustomerGroup(PersistenceManager pm)
//	{
//		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
//		CustomerGroup defaultCustomerGroup;
//		try {
//			pm.getExtent(CustomerGroup.class);
//			defaultCustomerGroup = (CustomerGroup) pm.getObjectById(CustomerGroupID.create(organisationID, DEFAULT_CUSTOMER_GROUP_ID));
//		} catch (JDOObjectNotFoundException x) {
//			defaultCustomerGroup = new CustomerGroup(organisationID, CustomerGroup.DEFAULT_CUSTOMER_GROUP_ID);
//			defaultCustomerGroup.getName().setText(Locale.GERMAN.getLanguage(), "Standard");
//			defaultCustomerGroup.getName().setText(Locale.FRENCH.getLanguage(), "Standard");
//			defaultCustomerGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
//			pm.makePersistent(defaultCustomerGroup);
//		}
//		return defaultCustomerGroup;
//	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String customerGroupID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="customerGroup"
	 */
	private CustomerGroupName name;

	/**
	 * This <tt>Map</tt> stores all {@link ModeOfPayment}s which are available for this
	 * <tt>CustomerGroup</tt>. This means that all their flavours are included. To
	 * make only some specific {@link ModeOfPaymentFlavour}s available, they
	 * must be put into {@link #modeOfPaymentFlavours}.
	 * <p>
	 * key: String modeOfPaymentPK<br/>
	 * value: ModeOfPayment modeOfPayment
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfPayment"
	 *		table="JFireTrade_CustomerGroup_modeOfPayments"
	 *
	 * @jdo.join
	 */
	private Map modeOfPayments = new HashMap();

	/**
	 * Unlike {@link #modeOfPayments}, this <tt>Map</tt> allows to provide a subset of the
	 * {@link ModeOfPaymentFlavour}s to a <tt>CustomerGroup</tt> if not all of a given
	 * {@link ModeOfPayment} shall be available.
	 * <p>
	 * key: String modeOfPaymentFlavourPK<br/>
	 * value: ModeOfPaymentFlavour modeOfPaymentFlavour
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfPaymentFlavour"
	 *		table="JFireTrade_CustomerGroup_modeOfPaymentFlavours"
	 *
	 * @jdo.join
	 */
	private Map modeOfPaymentFlavours = new HashMap();

	/**
	 * This <tt>Map</tt> stores all {@link ModeOfDelivery}s which are available for this
	 * <tt>CustomerGroup</tt>. This means that all their flavours are included. To
	 * make only some specific {@link ModeOfDeliveryFlavour}s available, they
	 * must be put into {@link #modeOfDeliveryFlavours}.
	 * <p>
	 * key: String modeOfDeliveryPK<br/>
	 * value: ModeOfDelivery modeOfDelivery
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfDelivery"
	 *		table="JFireTrade_CustomerGroup_modeOfDeliveries"
	 *
	 * @jdo.join
	 */
	private Map modeOfDeliveries = new HashMap();

	/**
	 * Unlike {@link #modeOfDeliveries}, this <tt>Map</tt> allows to provide a subset of the
	 * {@link ModeOfDeliveryFlavour}s to a <tt>CustomerGroup</tt> if not all of a given
	 * {@link ModeOfDelivery} shall be available.
	 * <p>
	 * key: String modeOfDeliveryFlavourPK<br/>
	 * value: ModeOfDeliveryFlavour modeOfDeliveryFlavour
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfDeliveryFlavour"
	 *		table="JFireTrade_CustomerGroup_modeOfDeliveryFlavours"
	 *
	 * @jdo.join
	 */
	private Map modeOfDeliveryFlavours = new HashMap();

//	/**
//	 * key: String tariffPK<br/>
//	 * value: Tariff tariff
//	 * <br/><br/>
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="Tariff"
//	 *
//	 * @jdo.join
//	 */
//	protected Map tariffs = new HashMap();

	protected CustomerGroup() { }

	public CustomerGroup(String organisationID, String customerGroupID)
	{
		this.organisationID = organisationID;
		this.customerGroupID = customerGroupID;
		this.primaryKey = getPrimaryKey(organisationID, customerGroupID);
		this.name = new CustomerGroupName(this);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the customerGroupID.
	 */
	public String getCustomerGroupID()
	{
		return customerGroupID;
	}

	public static String getPrimaryKey(String organisationID, String customerGroupID)
	{
		return organisationID + '/' + customerGroupID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * @return Returns the name.
	 */
	public CustomerGroupName getName()
	{
		return name;
	}
	/**
	 * @return Returns the modeOfPaymentFlavours.
	 */
	public Collection getModeOfPaymentFlavours()
	{
		return modeOfPaymentFlavours.values();
	}
	/**
	 * @return Returns the modeOfPayments.
	 */
	public Collection getModeOfPayments()
	{
		return modeOfPayments.values();
	}

	public void addModeOfPayment(ModeOfPayment modeOfPayment)
	{
		modeOfPayments.put(modeOfPayment.getPrimaryKey(), modeOfPayment);
	}
	public void addModeOfPaymentFlavour(ModeOfPaymentFlavour modeOfPaymentFlavour)
	{
		modeOfPaymentFlavours.put(modeOfPaymentFlavour.getPrimaryKey(), modeOfPaymentFlavour);
	}
	public void removeModeOfPayment(ModeOfPayment modeOfPayment)
	{
		removeModeOfPayment(modeOfPayment.getPrimaryKey());
	}
	public void removeModeOfPayment(String modeOfPaymentPK)
	{
		modeOfPayments.remove(modeOfPaymentPK);
	}
	public void removeModeOfPaymentFlavour(ModeOfPaymentFlavour modeOfPaymentFlavour)
	{
		removeModeOfPaymentFlavour(modeOfPaymentFlavour.getPrimaryKey());
	}
	public void removeModeOfPaymentFlavour(String modeOfPaymentFlavourPK)
	{
		modeOfPaymentFlavours.remove(modeOfPaymentFlavourPK);
	}


	/**
	 * @return Returns the modeOfDeliveryFlavours.
	 */
	public Collection getModeOfDeliveryFlavours()
	{
		return modeOfDeliveryFlavours.values();
	}
	/**
	 * @return Returns the modeOfDeliverys.
	 */
	public Collection getModeOfDeliveries()
	{
		return modeOfDeliveries.values();
	}

	public void addModeOfDelivery(ModeOfDelivery modeOfDelivery)
	{
		modeOfDeliveries.put(modeOfDelivery.getPrimaryKey(), modeOfDelivery);
	}
	public void addModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		modeOfDeliveryFlavours.put(modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
	}
	public void removeModeOfDelivery(ModeOfDelivery modeOfDelivery)
	{
		removeModeOfDelivery(modeOfDelivery.getPrimaryKey());
	}
	public void removeModeOfDelivery(String modeOfDeliveryPK)
	{
		modeOfDeliveries.remove(modeOfDeliveryPK);
	}
	public void removeModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		removeModeOfDeliveryFlavour(modeOfDeliveryFlavour.getPrimaryKey());
	}
	public void removeModeOfDeliveryFlavour(String modeOfDeliveryFlavourPK)
	{
		modeOfDeliveryFlavours.remove(modeOfDeliveryFlavourPK);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof CustomerGroup)) return false;
		CustomerGroup o = (CustomerGroup) obj;
		return Utils.equals(this.organisationID, o.organisationID) && Utils.equals(this.customerGroupID, o.customerGroupID);
	}
	@Override
	public int hashCode()
	{
		return Utils.hashCode(organisationID) + Utils.hashCode(customerGroupID);
	}
}
