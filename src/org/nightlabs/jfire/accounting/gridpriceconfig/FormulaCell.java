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

import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;

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
 * @jdo.create-objectid-class field-order="organisationID, priceConfigID, formulaID"
 *
 * @jdo.fetch-group name="FormulaCell.priceConfig" fields="priceConfig"
 * @jdo.fetch-group name="FormulaCell.priceCoordinate" fields="priceCoordinate"
 * @jdo.fetch-group name="FormulaCell.priceFragmentFormulas" fields="priceFragmentFormulas"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="priceCoordinate, priceFragmentFormulas"
 */
public class FormulaCell implements Serializable
{
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
	 *
	 * @jdo.key-column length="100"
	 * @jdo.value-column jdbc-type="LONGVARCHAR"
	 *
	 * @jdo.join
	 */
	private Map priceFragmentFormulas = new HashMap();

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @!jdo.column length="100"
	 */
	private long priceConfigID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long formulaID;

	/**
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
	public void setMapOwner(PriceConfig mapOwner)
	{
		this.mapOwner = mapOwner;
	}

	public PriceConfig getMapOwner()
	{
		return mapOwner;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceCoordinate priceCoordinate;

	protected FormulaCell()
	{
	}
	/**
	 * This constructor is used for the <tt>FormulaConfig.fallbackFormulaCell</tt>,
	 * which doesn't have a <tt>PriceCoordinate</tt>.
	 *
	 * @param priceConfig The <tt>FormulaPriceConfig</tt> which created this cell.
	 */
	public FormulaCell(PriceConfig priceConfig)
	{
		this.priceConfig = priceConfig;
		this.organisationID = priceConfig.getOrganisationID();
		this.priceConfigID = priceConfig.getPriceConfigID();
		this.formulaID = priceConfig.createPriceID();
		this.priceCoordinate = null;
	}
	public FormulaCell(PriceCoordinate priceCoordinate)
	{
		this.priceConfig = priceCoordinate.getPriceConfig();
		this.organisationID = priceConfig.getOrganisationID();
		this.priceConfigID = priceConfig.getPriceConfigID();
		this.formulaID = priceConfig.createPriceID();
		this.priceCoordinate = priceCoordinate;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the priceConfigID.
	 */
	public long getPriceConfigID()
	{
		return priceConfigID;
	}
	/**
	 * @return Returns the formulaID.
	 */
	public long getFormulaID()
	{
		return formulaID;
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
	public String getFormula(PriceFragmentType priceFragmentType)
	{
		return getFormula(priceFragmentType.getOrganisationID(), priceFragmentType.getPriceFragmentTypeID());
	}
	/**
	 * @return Returns the formula or <tt>null</tt> if none is defined.
	 */
	public String getFormula(String priceFragmentTypeOrganisationID, String priceFragmentTypeID)
	{
		return (String) priceFragmentFormulas.get(
				PriceFragmentType.getPrimaryKey(priceFragmentTypeOrganisationID, priceFragmentTypeID));
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
}
