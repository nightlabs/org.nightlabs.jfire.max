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

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * A PriceFragmentType defines a part out of which a <tt>Price</tt> may consist.
 * Normally, these are taxes. Examples are: vat-de-19, vat-de-7 or vat-ch-6_5
 * <br/><br/>
 * Not all <tt>Price</tt> s contain <tt>PriceFragment</tt> s for all <tt>PriceFragmentType</tt> s.
 * <tt>PriceFragmentType</tt> s are defined globally, but that does not mean that
 * every organisation knows all of them.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.PriceFragmentTypeID"
 *		detachable="true"
 *		table="JFireTrade_PriceFragmentType"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, priceFragmentTypeID"
 *		include-body="id/PriceFragmentTypeID.body.inc"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="PriceFragmentType.name" fields="name"
 * @jdo.fetch-group name="PriceFragmentType.containerPriceFragmentType" fields="containerPriceFragmentType"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="name"
 */
public class PriceFragmentType
	implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "PriceFragmentType.name";
	public static final String FETCH_GROUP_CONTAINER_PRICE_FRAGMENT_TYPE = "PriceFragmentType.containerPriceFragmentType";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String priceFragmentTypeID;

	protected PriceFragmentType() { }

	public PriceFragmentType(PriceFragmentTypeID priceFragmentTypeID)
	{
		this(priceFragmentTypeID.organisationID, priceFragmentTypeID.priceFragmentTypeID);
	}

	public PriceFragmentType(String organisationID, String priceFragmentTypeID)
	{
		this.organisationID = organisationID;
		this.priceFragmentTypeID = priceFragmentTypeID;
		this.primaryKey = getPrimaryKey(organisationID, priceFragmentTypeID);
		this.name = new PriceFragmentTypeName(this);
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the priceFragmentTypeID.
	 */
	public String getPriceFragmentTypeID()
	{
		return priceFragmentTypeID;
	}
	/**
	 * jdo.field persitence-modifier="persitent"
	 */
	protected String primaryKey;
	
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	
	public static String getPrimaryKey(String organisationID, String priceFragmentTypeID)
	{
		return organisationID + '/' + priceFragmentTypeID;
	}

	public static PriceFragmentTypeID primaryKeyToPriceFragmentTypeID(String primaryKey)
	{
		String[] parts = primaryKey.split("/");
		if (parts.length != 2) 
			throw new IllegalArgumentException("The given productTypePK "+primaryKey+" is illegal (more than one /)");
		return PriceFragmentTypeID.create(parts[0], parts[1]);
	}

	/**
	 * The {@link PriceFragmentTypeID} of the system-internal {@link PriceFragmentType} 'Total', that is the total price of a ProductType.
	 */
	public static final PriceFragmentTypeID PRICE_FRAGMENT_TYPE_ID_TOTAL = PriceFragmentTypeID.create(Organisation.DEV_ORGANISATION_ID, "_Total_");
	/**
	 * The {@link PriceFragmentTypeID} of the system-internal {@link PriceFragmentType} 'Rest', that is the missing amount of the parts of 'Total' 
	 * (those parts not defined in a {@link Price}), to form the 'Total' amount.
	 * <p>
	 * Note that the PriceFragment for this type is used as virtual price fragment and is not persisted to the datastore.
	 * </p>
	 */
	public static final PriceFragmentTypeID PRICE_FRAGMENT_TYPE_ID_REST = PriceFragmentTypeID.create(Organisation.DEV_ORGANISATION_ID, "_Rest_");

	/**
	 * This predefined priceFragmentType exists to allow a unified API for accesses
	 * to the priceFragments and the <tt>Price.amount</tt>. This is necessary e.g. in
	 * formulas.
	 * 
	 * @see Price#getAmount(String)
	 * @see Price#setAmount(String, long)
	 * @deprecated Use {@link #PRICE_FRAGMENT_TYPE_ID_TOTAL}
	 */
	@Deprecated
	public static final String TOTAL_PRICEFRAGMENTTYPEID = PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="priceFragmentType"
	 */
	private PriceFragmentTypeName name;

	/**
	 * Returns the PriceFragmentTypeName of this type.
	 * @return The PriceFragmentTypeName of this type.
	 */
	public PriceFragmentTypeName getName() {
		return name;
	}	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceFragmentType containerPriceFragmentType;

	/**
	 * The PriceFragmentType this one is contained in. 
	 * Currently the setter will only allow null or
	 * the PriceFragmentType with id {@link #TOTAL_PRICEFRAGMENTTYPEID} or null.
	 */
	public PriceFragmentType getContainerPriceFragmentType() {
		return containerPriceFragmentType;
	}

	/**
	 * Set the PriceFragment this one is contained in.
	 * Currently the setter will only allow null or
	 * the PriceFragmentType with id {@link #TOTAL_PRICEFRAGMENTTYPEID} or null.
	 */
	public void setContainerPriceFragmentType(PriceFragmentType containerPriceFragmentType) {
		this.containerPriceFragmentType = containerPriceFragmentType;
	}

	/**
	 * @return Returns the special predefined <tt>PriceFragmentType</tt> that represents the total price.
	 * The total price is no real price fragment, but can be managed by this <tt>PriceFragmentType</tt>
	 * to make formulas easier.
	 */
	public static PriceFragmentType getTotalPriceFragmentType(PersistenceManager pm)
	{
		pm.getExtent(PriceFragmentType.class);
		PriceFragmentType priceFragmentType;
		try {
			priceFragmentType = (PriceFragmentType) pm.getObjectById(PRICE_FRAGMENT_TYPE_ID_TOTAL);
		} catch (JDOObjectNotFoundException x) {
			priceFragmentType = new PriceFragmentType(PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID);
			pm.makePersistent(priceFragmentType);
		}
		return priceFragmentType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof PriceFragmentType)) return false;
		PriceFragmentType o = (PriceFragmentType) obj;
		return Util.equals(this.organisationID, o.organisationID) && Util.equals(this.priceFragmentTypeID, o.priceFragmentTypeID);
	}
	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(priceFragmentTypeID);
	}
}
