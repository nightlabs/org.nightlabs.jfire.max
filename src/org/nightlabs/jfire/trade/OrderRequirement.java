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
 * @jdo.create-objectid-class field-order="organisationID, orderID"
 */
public class OrderRequirement
	implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long orderID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Order order;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Trader trader;

	public OrderRequirement() { }
	
	public OrderRequirement(Trader trader, Order order)
	{
		if (trader == null)
			throw new NullPointerException("trader");
		
		if (order == null)
			throw new NullPointerException("order");

		this.order = order;
		this.organisationID = order.getOrganisationID();
		this.orderID = order.getOrderID();
	}
	
	/**
	 * key: String anchorPK (of the vendor LegalEntity)<br/>
	 * value: Order order
	 * <br/><br/>
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="Order"
	 *
	 * @jdo.join
	 */
	private Map ordersByVendor = new HashMap();
	
	
	public void addOrder(Order order) {
		ordersByVendor.put(order.getVendor().getPrimaryKey(), order);
	}
	
	/**
	 * This method returns the Order for the given vendor or null,
	 * if it does not exist.
	 *
	 * @param vendor
	 * @return Returns the order for the given vendor or <tt>null</tt>.
	 */
	public Order getOrder(OrganisationLegalEntity vendor)
	{
		return (Order)ordersByVendor.get(vendor.getPrimaryKey());
	}
}
