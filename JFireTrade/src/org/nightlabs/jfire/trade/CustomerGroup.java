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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.pay.ModeOfPayment;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.util.Util;


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
 * @jdo.fetch-group name="FetchGroupsPriceConfig.this" fields="name, modeOfPayments, modeOfPaymentFlavours, modeOfDeliveries, modeOfDeliveryFlavours"
 */
@PersistenceCapable(
	objectIdClass=CustomerGroupID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_CustomerGroup")
@FetchGroups({
	@FetchGroup(
		name=CustomerGroup.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsPriceConfig.edit",
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsPriceConfig.this",
		members={@Persistent(name="name"), @Persistent(name="modeOfPayments"), @Persistent(name="modeOfPaymentFlavours"), @Persistent(name="modeOfDeliveries"), @Persistent(name="modeOfDeliveryFlavours")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class CustomerGroup implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String CUSTOMER_GROUP_ID_ANONYMOUS = "CustomerGroup-anonymous";
	public static final String CUSTOMER_GROUP_ID_DEFAULT = "CustomerGroup-default";
	public static final String CUSTOMER_GROUP_ID_RESELLER = "CustomerGroup-reseller";

	public static final String FETCH_GROUP_NAME = "CustomerGroup.name";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_CUSTOMER_GROUP = "CustomerGroup.this";

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
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String customerGroupID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="customerGroup"
	 */
	@Persistent(
		dependent="true",
		mappedBy="customerGroup",
		persistenceModifier=PersistenceModifier.PERSISTENT)
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
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_CustomerGroup_modeOfPayments",
		persistenceModifier=PersistenceModifier.PERSISTENT)
		private Map<String, ModeOfPayment> modeOfPayments;

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
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_CustomerGroup_modeOfPaymentFlavours",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, ModeOfPaymentFlavour> modeOfPaymentFlavours;

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
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_CustomerGroup_modeOfDeliveries",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, ModeOfDelivery> modeOfDeliveries;

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
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_CustomerGroup_modeOfDeliveryFlavours",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, ModeOfDeliveryFlavour> modeOfDeliveryFlavours;

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
		modeOfPayments = new HashMap<String, ModeOfPayment>();
		modeOfPaymentFlavours = new HashMap<String, ModeOfPaymentFlavour>();
		modeOfDeliveries = new HashMap<String, ModeOfDelivery>();
		modeOfDeliveryFlavours = new HashMap<String, ModeOfDeliveryFlavour>();
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
	public Collection<ModeOfPaymentFlavour> getModeOfPaymentFlavours()
	{
		return Collections.unmodifiableCollection(modeOfPaymentFlavours.values());
	}
	/**
	 * @return Returns the modeOfPayments.
	 */
	public Collection<ModeOfPayment> getModeOfPayments()
	{
		return Collections.unmodifiableCollection(modeOfPayments.values());
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
	public Collection<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours()
	{
		return Collections.unmodifiableCollection(modeOfDeliveryFlavours.values());
	}
	/**
	 * @return Returns the modeOfDeliverys.
	 */
	public Collection<ModeOfDelivery> getModeOfDeliveries()
	{
		return Collections.unmodifiableCollection(modeOfDeliveries.values());
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
		return Util.equals(this.organisationID, o.organisationID) && Util.equals(this.customerGroupID, o.customerGroupID);
	}
	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(customerGroupID);
	}
}
