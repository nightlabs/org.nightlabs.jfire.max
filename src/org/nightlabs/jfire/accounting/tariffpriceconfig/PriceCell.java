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

package org.nightlabs.jfire.accounting.tariffpriceconfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.tariffpriceconfig.id.PriceCellID"
 *		detachable="true"
 *		table="JFireTrade_PriceCell"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="PriceCell.price" fields="price"
 * @jdo.fetch-group name="PriceCell.priceConfig" fields="priceConfig"
 * @jdo.fetch-group name="PriceCell.priceCoordinate" fields="priceCoordinate"
 * @jdo.fetch-group name="PriceCell.this" fetch-groups="default" fields="price, priceConfig, priceCoordinate"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="price, priceCoordinate"
 *
 * @jdo.query name = "getPriceCellsForCustomerGroupAndCurrency" query = "SELECT
 *		WHERE
 *			this.priceConfig == paramPriceConfig &&
 *			this.priceCoordinate.customerGroupPK == paramCustomerGroupPK &&
 *			this.priceCoordinate.currencyID == paramCurrencyID
 *		PARAMETERS StablePriceConfig paramPriceConfig, String paramCustomerGroupPK, String paramCurrencyID
 *		IMPORTS import java.lang.String; import org.nightlabs.jfire.accounting.tariffpriceconfig.StablePriceConfig"
 */
public class PriceCell implements Serializable
{
	public static final String FETCH_GROUP_PRICE = "PriceCell.price";
	public static final String FETCH_GROUP_PRICE_CONFIG = "PriceCell.priceConfig";
	public static final String FETCH_GROUP_PRICE_COORDINATE = "PriceCell.priceCoordinate";
	public static final String FETCH_GROUP_THIS_PRICE_CELL = "PriceCell.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceConfigID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceConfig priceConfig;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceCoordinate priceCoordinate;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price price;

	/**
	 * @deprecated Only for JDO!
	 */
	protected PriceCell()
	{
	}

	public PriceCell(IPriceCoordinate priceCoordinate)
	{
		this.priceConfig = priceCoordinate.getPriceConfig();
		this.organisationID = priceConfig.getOrganisationID();
		this.priceConfigID = priceConfig.getPriceConfigID();
		this.priceID = priceConfig.createPriceID();

		if (!(priceCoordinate instanceof PriceCoordinate))
			throw new IllegalArgumentException("priceCoordinate of type \""+priceCoordinate.getClass().getName()+"\" implements IPriceCoordinate, but does NOT extend \""+PriceCoordinate.class.getName()+"\"! It MUST (directly or indirectly) extend this class!");

		this.priceCoordinate = (PriceCoordinate)priceCoordinate;
		this.price = new Price(
				organisationID, priceConfigID, priceID,
				priceConfig.getCurrency(priceCoordinate.getCurrencyID(), true));
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the price.
	 */
	public Price getPrice()
	{
		return price;
	}
	/**
	 * @return Returns the priceConfig.
	 */
	public IPriceConfig getPriceConfig()
	{
		return priceConfig;
	}
	/**
	 * @return Returns the priceConfigID.
	 */
	public long getPriceConfigID()
	{
		return priceConfigID;
	}
	/**
	 * @return Returns the priceCoordinate.
	 */
	public PriceCoordinate getPriceCoordinate()
	{
		return priceCoordinate;
	}
	/**
	 * @return Returns the formulaID.
	 */
	public long getPriceID()
	{
		return priceID;
	}

	/**
	 * The cell has no value or a value that is not valid anymore (because of a re-calc).
	 * If the calculation process comes to a cell with this status, the cell will be calculated.
	 * @see #CALCULATIONSTATUS_INPROCESS
	 * @see #CALCULATIONSTATUS_CLEAN
	 */
	public static final String CALCULATIONSTATUS_DIRTY = "dirty";
	/**
	 * The cell is currently calculated. If the calculation process stumbles over a
	 * cell that has this value, it is facing a circular reference.
	 * @see #CALCULATIONSTATUS_DIRTY
	 * @see #CALCULATIONSTATUS_CLEAN
	 */
	public static final String CALCULATIONSTATUS_INPROCESS = "inProcess";
	/**
	 * The cell has a valid value and does not need to be calculated. The opposite of dirty.
	 * @see #CALCULATIONSTATUS_DIRTY
	 * @see #CALCULATIONSTATUS_INPROCESS
	 */
	public static final String CALCULATIONSTATUS_CLEAN = "clean";

	/**
	 * This <tt>Map</tt> is used to store the status of each PriceFragment's amount calculation.
	 * The real <tt>Price.amount</tt> (which is not a fragment) is managed as a fragment
	 * in the price calculation (and the formulas) with the priceFragmentTypeID "TOTAL".
	 * This <tt>Map</tt> is used to detect circular references in the formulas, too.
	 * <p>
	 * key: String priceFragmentTypeID<br/>
	 * value: String calculationStatus
	 *
	 * @jdo.field persistence-modifier="none"
	 *
	 * @see org.nightlabs.jfire.accounting.PriceFragmentType#PRICEFRAGMENTTYPEID_TOTAL
	 */
	protected transient Map priceFragmentsCalculationStatus = null;

	/**
	 * This method drops all stati which is equivalent to setting them to
	 * <tt>CALCULATIONSTATUS_DIRTY</tt>.
	 */
	public void resetPriceFragmentCalculationStati()
	{
		priceFragmentsCalculationStatus = null;
	}

	public String getPriceFragmentCalculationStatus(String priceFragmentTypeID)
	{
		if (priceFragmentsCalculationStatus == null)
			return CALCULATIONSTATUS_DIRTY;

		String status = (String) priceFragmentsCalculationStatus.get(priceFragmentTypeID);
		if (status == null)
			return CALCULATIONSTATUS_DIRTY;

		return status;
	}

	public void setPriceFragmentCalculationStatus(String priceFragmentTypeID, String status)
	{
		if (priceFragmentsCalculationStatus == null)
			priceFragmentsCalculationStatus = new HashMap();
		priceFragmentsCalculationStatus.put(priceFragmentTypeID, status);
	}
}
