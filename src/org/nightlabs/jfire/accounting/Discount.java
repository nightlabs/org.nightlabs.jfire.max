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

package org.nightlabs.jfire.accounting;

import java.util.Date;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.DiscountID"
 *		detachable="true"
 *		table="JFireTrade_Discount"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, discountID"
 */
public abstract class Discount {

	/**
	 * @deprecated Only for JDO!
	 */
	protected Discount() {
	}
	
	/**
	 * @param invoice
	 * @param discountID
	 */
	public Discount(Invoice invoice, long discountID) {
		this.invoice = invoice;
		this.organisationID = invoice.getOrganisationID();
		this.discountID = discountID;
		invoice.setDiscount(this);
	}
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long discountID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */ 
	private Invoice invoice;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */ 
	private boolean booked;
	
	/**
	 * Will be called when the Invoice changes and after finalization. 
	 */
	public abstract void update();
	
	/**
	 * Every Discount should provide a Price depending on the given date.
	 * 
	 * @param date
	 * @return
	 */
	public abstract Price getDiscountPrice(Date date);

	public boolean isBooked() {
		return booked;
	}

	/**
	 * Sets the booked
	 * @param booked
	 */
	public void setBooked(boolean booked) {
		this.booked = booked;
	}

	/**
	 * Returns the discountID
	 *  
	 * @return The discountID
	 */
	public long getDiscountID() {
		return discountID;
	}

	/**
	 * Returns the associated Invoice
	 * 
	 * @return The associated invoice
	 */
	public Invoice getInvoice() {
		return invoice;
	}

	/**
	 * Returns the organisationID
	 *  
	 * @return The organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}
}
