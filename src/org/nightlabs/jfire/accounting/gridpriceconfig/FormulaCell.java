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

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.gridpriceconfig.id.FormulaCellID"
 *		detachable="true"
 *		table="JFireTrade_FormulaCell"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, formulaCellID"
 *
 * @jdo.fetch-group name="FormulaCell.priceConfig" fields="priceConfig"
 * @jdo.fetch-group name="FormulaCell.priceCoordinate" fields="priceCoordinate"
 * @jdo.fetch-group name="FormulaCell.priceFragmentFormulas" fields="priceFragmentFormulas"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="priceConfig[-1], priceCoordinate[-1], priceFragmentFormulas[-1]"
 */
public class FormulaCell implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PRICE_CONFIG = "FormulaCell.priceConfig";
	public static final String FETCH_GROUP_PRICE_COORDINATE = "FormulaCell.priceCoordinate";
	public static final String FETCH_GROUP_PRICE_FRAGMENT_FORMULAS = "FormulaCell.priceFragmentFormulas";

	/**
	 * key: String priceFragmentTypePK<br/>
	 * value: String formula
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="String"
	 *		table="JFireTrade_FormulaCell_priceFragmentFormulas"
	 *		null-value="exception"
	 *
	 * @jdo.key-column length="100"
	 * @jdo.value-column sql-type="CLOB"
	 *
	 * @jdo.join
	 */
	private Map<String, String> priceFragmentFormulas;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String priceConfigID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long formulaCellID;

	/**
	 * @!jdo.field persistence-modifier="persistent" null-value="exception"
	 * TODO DataNucleus workaround: the above null-value="exception" is correct but causes exceptions during cross-datastore-replication 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceConfig priceConfig;

	/**
	 * This is used in mapped-by of the FormulaPriceConfig.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceConfig mapOwner;

	/**
	 * @param mapOwner The mapOwner to set.
	 */
	public void setMapOwner(FormulaPriceConfig mapOwner)
	{
		this.mapOwner = mapOwner;
	}

	public FormulaPriceConfig getMapOwner()
	{
		return (FormulaPriceConfig) mapOwner;
	}

	/**
	 * This field is <code>null</code>, if this is the fallbackFormulaCell of a FormulaPriceConfig
	 * (see {@link #FormulaCell(PriceConfig)} below).
	 *
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private PriceCoordinate priceCoordinate;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected FormulaCell() { }

	/**
	 * This constructor is used for the <tt>FormulaPriceConfig.fallbackFormulaCell</tt>,
	 * which doesn't have a <tt>PriceCoordinate</tt>.
	 *
	 * @param priceConfig the <tt>FormulaPriceConfig</tt> which created this cell.
	 */
	public FormulaCell(FormulaPriceConfig priceConfig)
	{
		this.priceConfig = priceConfig;

		if (!(this.priceConfig instanceof IFormulaPriceConfig))
			throw new IllegalArgumentException("priceConfig must be an instance of IFormulaPriceConfig but is not! " + this.priceConfig);

		this.organisationID = priceConfig.getOrganisationID();
//		this.priceConfigID = priceConfig.getPriceConfigID();
//		this.formulaID = priceConfig.createPriceID();
		
		if (!IDGenerator.getOrganisationID().equals(this.organisationID))
			throw new IllegalStateException("IDGenerator.organisationID != this.organisationID :: " + IDGenerator.getOrganisationID() + " != " + this.organisationID);

		this.formulaCellID = IDGenerator.nextID(FormulaCell.class);
		
		this.priceCoordinate = null;
		this.priceFragmentFormulas = new HashMap<String, String>();
	}
	public FormulaCell(PriceCoordinate priceCoordinate)
	{
		this.priceConfig = (FormulaPriceConfig) priceCoordinate.getPriceConfig();

		if (!(this.priceConfig instanceof IFormulaPriceConfig))
			throw new IllegalArgumentException("priceCoordinate.priceConfig must be an instance of IFormulaPriceConfig but is not! " + this.priceConfig);

		this.organisationID = priceConfig.getOrganisationID();
//		this.priceConfigID = priceConfig.getPriceConfigID();
//		this.formulaID = priceConfig.createPriceID();
		if (!IDGenerator.getOrganisationID().equals(this.organisationID))
			throw new IllegalStateException("IDGenerator.organisationID != this.organisationID :: " + IDGenerator.getOrganisationID() + " != " + this.organisationID);

		this.formulaCellID = IDGenerator.nextID(FormulaCell.class);
		
		this.priceCoordinate = priceCoordinate;
		this.priceFragmentFormulas = new HashMap<String, String>();
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
//	/**
//	 * @return Returns the priceConfigID.
//	 */
//	public String getPriceConfigID()
//	{
//		return priceConfigID;
//	}
//	/**
//	 * @return Returns the formulaID.
//	 */
//	public long getFormulaID()
//	{
//		return formulaID;
//	}
	
	public long getFormulaCellID() {
		return formulaCellID;
	}
	
	/**
	 * @return Returns the priceConfig.
	 */
	public PriceConfig getPriceConfig()
	{
		return priceConfig;
	}
	/**
	 * @return Returns the priceCoordinate.
	 */
	public PriceCoordinate getPriceCoordinate()
	{
		return priceCoordinate;
	}
	public boolean isEmpty()
	{
		return priceFragmentFormulas.isEmpty();
	}
	public void clear()
	{
		priceFragmentFormulas.clear();
	}
	public String getFormula(PriceFragmentType priceFragmentType)
	{
		return getFormula(priceFragmentType.getOrganisationID(), priceFragmentType.getPriceFragmentTypeID());
	}
	/**
	 * @return Returns the formula or <tt>null</tt> if none is defined.
	 */
	public String getFormula(String priceFragmentTypeOrganisationID, String priceFragmentTypeID)
	{
		return priceFragmentFormulas.get(
				PriceFragmentType.getPrimaryKey(priceFragmentTypeOrganisationID, priceFragmentTypeID));
	}
	public void setFormula(PriceFragmentTypeID priceFragmentTypeID, String formula)
	{
		setFormula(
				priceFragmentTypeID.organisationID,
				priceFragmentTypeID.priceFragmentTypeID,
				formula);
	}
	public void setFormula(PriceFragmentType priceFragmentType, String formula)
	{
		setFormula(
				priceFragmentType.getOrganisationID(),
				priceFragmentType.getPriceFragmentTypeID(),
				formula);
	}
	/**
	 * @param formula The formula to set.
	 */
	public void setFormula(String priceFragmentTypeOrganisationID, String priceFragmentTypeID, String formula)
	{
		if (priceFragmentTypeOrganisationID == null)
			throw new NullPointerException("priceFragmentTypeOrganisationID");

		if (priceFragmentTypeID == null)
			throw new NullPointerException("priceFragmentTypeID");
		
		String priceFragmentTypePK = PriceFragmentType.getPrimaryKey(priceFragmentTypeOrganisationID, priceFragmentTypeID);

		if (formula == null || "".equals(formula))
			priceFragmentFormulas.remove(priceFragmentTypePK);
		else
			priceFragmentFormulas.put(priceFragmentTypePK, formula);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
//		result = prime * result + ((priceConfigID == null) ? 0 : priceConfigID.hashCode());
		result = prime * result + (int) (formulaCellID ^ (formulaCellID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		final FormulaCell other = (FormulaCell) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
//				Util.equals(this.priceConfigID, other.priceConfigID) &&
				Util.equals(this.formulaCellID, other.formulaCellID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(formulaCellID) + ']';
	}
}
