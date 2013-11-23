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

package org.nightlabs.jfire.accounting.gridpriceconfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.gridpriceconfig.id.PriceCellID;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.gridpriceconfig.id.PriceCellID"
 *		detachable="true"
 *		table="JFireTrade_PriceCell"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, priceCellID"
 *
 * @jdo.fetch-group name="PriceCell.price" fields="price"
 * @jdo.fetch-group name="PriceCell.priceConfig" fields="priceConfig"
 * @jdo.fetch-group name="PriceCell.priceCoordinate" fields="priceCoordinate"
 * @!jdo.fetch-group name="PriceCell.this" fetch-groups="default" fields="price, priceConfig, priceCoordinate"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="priceConfig[-1], price[-1], priceCoordinate[-1]"
 *
 * @jdo.query name = "getPriceCellsForCustomerGroupAndCurrency" query = "SELECT
 *		WHERE
 *			this.priceConfig == paramPriceConfig &&
 *			this.priceCoordinate.customerGroupPK == paramCustomerGroupPK &&
 *			this.priceCoordinate.currencyID == paramCurrencyID
 *		PARAMETERS StablePriceConfig paramPriceConfig, String paramCustomerGroupPK, String paramCurrencyID
 *		import java.lang.String; import org.nightlabs.jfire.accounting.gridpriceconfig.StablePriceConfig"
 */
@PersistenceCapable(
	objectIdClass=PriceCellID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_PriceCell")
@FetchGroups({
	@FetchGroup(
		name=PriceCell.FETCH_GROUP_PRICE,
		members=@Persistent(name="price")),
	@FetchGroup(
		name=PriceCell.FETCH_GROUP_PRICE_CONFIG,
		members=@Persistent(name="priceConfig")),
	@FetchGroup(
		name=PriceCell.FETCH_GROUP_PRICE_COORDINATE,
		members=@Persistent(name="priceCoordinate")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsPriceConfig.edit",
		members={@Persistent(
			name="priceConfig",
			recursionDepth=-1), @Persistent(
			name="price",
			recursionDepth=-1), @Persistent(
			name="priceCoordinate",
			recursionDepth=-1)})
})
@Queries(
	@Query(
		name="getPriceCellsForCustomerGroupAndCurrency",
		value="SELECT WHERE this.priceConfig == paramPriceConfig && this.priceCoordinate.customerGroupPK == paramCustomerGroupPK && this.priceCoordinate.currencyID == paramCurrencyID PARAMETERS StablePriceConfig paramPriceConfig, String paramCustomerGroupPK, String paramCurrencyID import java.lang.String; import org.nightlabs.jfire.accounting.gridpriceconfig.StablePriceConfig")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PriceCell implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PriceCell.class);

	public static final String FETCH_GROUP_PRICE = "PriceCell.price";
	public static final String FETCH_GROUP_PRICE_CONFIG = "PriceCell.priceConfig";
	public static final String FETCH_GROUP_PRICE_COORDINATE = "PriceCell.priceCoordinate";
//	/**
//	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
//	 */
//	public static final String FETCH_GROUP_THIS_PRICE_CELL = "PriceCell.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String priceConfigID;
//
//	/**
//	 * @jdo.field primary-key="true"
//	 */
//	private long priceID;
	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long priceCellID;

	/**
	 * @!jdo.field persistence-modifier="persistent" null-value="exception"
	 * TODO DataNucleus workaround: the above null-value="exception" is correct but causes exceptions during cross-datastore-replication
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StablePriceConfig priceConfig;

	/**
	 * @!jdo.field persistence-modifier="persistent" dependent="true" null-value="exception"
	 * TODO DataNucleus workaround: the above null-value="exception" is correct but causes exceptions during cross-datastore-replication
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	private PriceCoordinate priceCoordinate;

	@Persistent(
			persistenceModifier=PersistenceModifier.PERSISTENT,
			dependent="true"
	)
	private Price price;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PriceCell()
	{
		if (logger.isDebugEnabled())
			logger.debug("PriceCell jdo-only-constructor: " + this);
	}

	public PriceCell(IPriceCoordinate priceCoordinate)
	{
		this.priceConfig = (StablePriceConfig) priceCoordinate.getPriceConfig();
		this.organisationID = priceConfig.getOrganisationID();
//		this.priceConfigID = priceConfig.getPriceConfigID();
//		this.priceID = priceConfig.createPriceID();
		if (!IDGenerator.getOrganisationID().equals(this.organisationID))
			throw new IllegalStateException("IDGenerator.organisationID != this.organisationID :: " + IDGenerator.getOrganisationID() + " != " + this.organisationID);

		// Even though the priceCellID is named differently, we use the same value as the priceID (of the Price instance created below) because
		// that makes looking up things in the database easier. Thus, the following use of Price.class is *NOT* a mistake, but absolutely correct.
		this.priceCellID = IDGenerator.nextID(Price.class);

		if (logger.isDebugEnabled())
			logger.debug("PriceCell manual-constructor: " + this + " priceCoordinate=" + priceCoordinate);

		if (!(priceCoordinate instanceof PriceCoordinate))
			throw new IllegalArgumentException("priceCoordinate of type \""+priceCoordinate.getClass().getName()+"\" implements IPriceCoordinate, but does NOT extend \""+PriceCoordinate.class.getName()+"\"! It MUST (directly or indirectly) extend this class!");

		this.priceCoordinate = (PriceCoordinate)priceCoordinate;
		this.price = new Price(
				organisationID, priceCellID, // priceID,
				priceConfig.getCurrency(priceCoordinate.getCurrencyID(), true)
		);
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
//	/**
//	 * @return Returns the priceConfigID.
//	 */
//	public String getPriceConfigID()
//	{
//		return priceConfigID;
//	}
	/**
	 * @return Returns the priceCoordinate.
	 */
	public PriceCoordinate getPriceCoordinate()
	{
		try {
			return priceCoordinate;
		} catch (Exception x) {
			logger.info("getPriceCoordinate: " + this, x);
			if (x instanceof RuntimeException)
				throw (RuntimeException)x;
			else
				throw new RuntimeException(x);
		}
	}
//	/**
//	 * @return Returns the formulaID.
//	 */
//	public long getPriceID()
//	{
//		return priceID;
//	}
	public long getPriceCellID() {
		return priceCellID;
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
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected transient Map<String, String> priceFragmentsCalculationStatus = null;

	/**
	 * This method drops all stati which is equivalent to setting them to
	 * <tt>CALCULATIONSTATUS_DIRTY</tt>.
	 */
	public void resetPriceFragmentCalculationStatus()
	{
		priceFragmentsCalculationStatus = null;
	}

	public String getPriceFragmentCalculationStatus(String priceFragmentTypePK)
	{
		if (priceFragmentsCalculationStatus == null)
			return CALCULATIONSTATUS_DIRTY;

		String status = priceFragmentsCalculationStatus.get(priceFragmentTypePK);
		if (status == null)
			return CALCULATIONSTATUS_DIRTY;

		return status;
	}

	public void setPriceFragmentCalculationStatus(String priceFragmentTypePK, String status)
	{
		if (priceFragmentTypePK == null)
			throw new IllegalArgumentException("priceFragmentTypePK must not be null!");
		if (status == null)
			throw new IllegalArgumentException("status must not be null!");

		if (priceFragmentsCalculationStatus == null)
			priceFragmentsCalculationStatus = new HashMap<String, String>();

		priceFragmentsCalculationStatus.put(priceFragmentTypePK, status);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
//		result = prime * result + ((priceConfigID == null) ? 0 : priceConfigID.hashCode());
		result = prime * result + (int) (priceCellID ^ (priceCellID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final PriceCell other = (PriceCell) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
//				Util.equals(this.priceConfigID, other.priceConfigID) &&
				Util.equals(this.priceCellID, other.priceCellID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(priceCellID) + ']';
	}
}
