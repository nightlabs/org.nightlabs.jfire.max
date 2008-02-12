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
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.trade.id.OrderRequirementID;

/**
 * There exists one instance of OrderRequirement for each Order on the side of
 * its vendor. It bundles all orders that are used to fulfill the main order.
 * <br/><br/>
 * This object is never transferred to another vendor/customer organisation and resides
 * only in the datastore of the vendor for whose Order it has been created.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.OrderRequirementID"
 *		detachable="true"
 *		table="JFireTrade_OrderRequirement"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, orderIDPrefix, orderID"
 */
public class OrderRequirement
	implements Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String orderIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long orderID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Order order;

	/**
	 * This method returns a previously existing {@link OrderRequirement} or creates and persists
	 * a new instance if not existent.
	 */
	public static OrderRequirement getOrderRequirement(PersistenceManager pm, Order order)
	{
		OrderRequirementID orderRequirementID = OrderRequirementID.create(order.getOrganisationID(), order.getOrderIDPrefix(), order.getOrderID());
		pm.getExtent(OrderRequirement.class);
		try {
			OrderRequirement res = (OrderRequirement) pm.getObjectById(orderRequirementID);
			res.getOrder();
			return res;
		} catch (JDOObjectNotFoundException x) {
			return pm.makePersistent(new OrderRequirement(order));
		}
	}

	public OrderRequirement() { }

	public OrderRequirement(Order order)
	{
		if (order == null)
			throw new NullPointerException("order");

		this.order = order;
		this.organisationID = order.getOrganisationID();
		this.orderIDPrefix = order.getOrderIDPrefix();
		this.orderID = order.getOrderID();
	}

	public Order getOrder()
	{
		return order;
	}

	/**
	 * key: LegalEntity vendor<br/>
	 * value: Order order
	 * <br/><br/>
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="LegalEntity"
	 *		value-type="Order"
	 *		null-value="exception"
	 *		table="JFireTrade_OrderRequirement_vendor2order"
	 *
	 * @jdo.join
	 */
	private Map vendor2order = new HashMap();

	public void addPartnerOrder(Order order) {
		LegalEntity vendor = order.getVendor();

		Order other = (Order) vendor2order.get(vendor);
		if (order.equals(other))
			return; // nothing to do

		if (other != null)
			throw new IllegalStateException("Vendor-Order cannot be added, because another Order is already assigned for this vendor! order.primaryKey=" + order.getPrimaryKey() + " otherOrder.primaryKey=" + other.getPrimaryKey());

		vendor2order.put(vendor, order);
	}
	
	/**
	 * This method returns the Order for the given vendor or null,
	 * if it does not exist.
	 *
	 * @param vendor
	 * @return Returns the order for the given vendor or <tt>null</tt>.
	 */
	public Order getPartnerOrder(LegalEntity vendor)
	{
		return (Order)vendor2order.get(vendor);
	}
}
