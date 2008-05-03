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
import java.util.HashSet;
import java.util.Set;

import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.gridpriceconfig.id.PriceCoordinateID"
 *		detachable="true"
 *		table="JFireTrade_PriceCoordinate"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, priceCoordinateID"
 *
 * @jdo.fetch-group name="PriceCoordinate.priceConfig" fields="priceConfig"
 * @jdo.fetch-group name="PriceCoordinate.this" fetch-groups="default" fields="priceConfig"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="priceConfig"
 */
public class PriceCoordinate implements Serializable, StoreCallback, IPriceCoordinate
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(PriceCoordinate.class);

	public static final String FETCH_GROUP_PRICE_CONFIG = "PriceCoordinate.priceConfig";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_PRICE_COORDINATE = "PriceCoordinate.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID = null;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceCoordinateID = -1;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private String customerGroupPK;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private String tariffPK;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private String currencyID;

	/**
	 * @!jdo.field persistence-modifier="persistent" null-value="exception"
	 * TODO the above null-value="exception" is correct but causes a problem when replicating to another datastore due to a jpox bug
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceConfig priceConfig;

	public PriceCoordinate()
	{
	}

	public PriceCoordinate(PriceConfig priceConfig, IPriceCoordinate priceCoordinate)
	{
		this.priceConfig = priceConfig;

		this.customerGroupPK = priceCoordinate.getCustomerGroupPK();
		this.tariffPK = priceCoordinate.getTariffPK();
		this.currencyID = priceCoordinate.getCurrencyID();
	}

	/**
	 * <strong>WARNING:</strong> An instance created by this constructor cannot be persisted
	 * into the database! It is only intended for usage as address in javascript formulas or
	 * similar purposes!
	 * <p>
	 * This constructor takes the values for each dimension as the java5 open parameter
	 * (internally handled as array) <code>dimensionValues</code>. Every class
	 * (usually implementing {@link ObjectID}) defines one dimension of the coordinate.
	 * This means, every instance in the array <code>dimensionValues</code>
	 * must have a different type.
	 * </p>
	 * <p>
	 * Additionally, it converts {@link String}s starting with {@link ObjectIDUtil#JDO_PREFIX} +
	 * {@link ObjectIDUtil#JDO_PREFIX_SEPARATOR} to {@link ObjectID}s using the method
	 * {@link ObjectIDUtil#createObjectID(String)}.
	 * </p>
	 * <p>
	 * The base implementation of <code>PriceCoordinate</code> processes values for the following classes:
	 * <ul>
	 * <li>{@link CustomerGroupID}</li>
	 * <li>{@link TariffID}</li>
	 * <li>{@link CurrencyID}</li>
	 * </ul>
	 * In order to make extending <code>PriceCoordinate</code> easier, you can pass instances of other classes as
	 * well! Hence, when you extend <code>PriceCoordinate</code>, you can simply first pass all <code>dimensionValues</code>
	 * to the super constructor and then search for your additional classes in the array. It's recommended
	 * to use the method {@link #getDimensionValue(Object[], Class)} for this purpose.
	 * </p>
	 * <p>
	 * It is the contract of a <code>PriceCoordinate</code> created by this constructor that every
	 * missing dimension value will refer to the current cell's location. Means, if you create an
	 * instance of this class in the cell [customerGroup="default", tariff="normal", currency="EUR"]
	 * and you specify only [customerGroup="anonymous", tariff="students"], then the currency will
	 * stay <code>null</code> and thus point to "EUR".
	 * </p>
	 *
	 * @!param customerGroupPK Either <tt>null</tt> (which means the same <tt>CustomerGroup</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link CustomerGroup#getPrimaryKey()}).
	 * @!param tariffPK Either <tt>null</tt> (which means the same <tt>Tariff</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link Tariff#getPrimaryKey()}).
	 * @!param currencyID Either <tt>null</tt> (which means the same <tt>Currency</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link Currency#getCurrencyID()}).
	 *
	 * @param dimensionValues values for each dimension, together making up the coordinate.
	 */
	public PriceCoordinate(Object ... dimensionValues)
	{
		convertStringsToObjectIDs(dimensionValues);
		assertDimensionValuesUniqueClasses(dimensionValues);

		CustomerGroupID customerGroupID = (CustomerGroupID) getDimensionValue(dimensionValues, CustomerGroupID.class);
		if (customerGroupID != null)
			this.customerGroupPK = customerGroupID.getPrimaryKey();

		TariffID tariffID = (TariffID) getDimensionValue(dimensionValues, TariffID.class);
		if (tariffID != null)
			this.tariffPK = tariffID.getPrimaryKey();

		CurrencyID currencyID = (CurrencyID) getDimensionValue(dimensionValues, CurrencyID.class);
		if (currencyID != null)
			this.currencyID = currencyID.currencyID;
	}
	/**
	 * @deprecated Only used for downward compatibility! Use {@link #PriceCoordinate(Object[])} instead!
	 */
	@Deprecated
	public PriceCoordinate(String customerGroupPK, String tariffPK, String currencyID)
	{
		this.customerGroupPK = customerGroupPK;
		this.tariffPK = tariffPK;
		this.currencyID = currencyID;
	}

	private static void convertStringsToObjectIDs(Object[] dimensionValues)
	{
		String jdoStart = null;
		for (int i = 0; i < dimensionValues.length; i++) {
			Object dimensionValue = dimensionValues[i];
			if (dimensionValue instanceof String) {
				String s = (String)dimensionValue;

				if (jdoStart == null)
					jdoStart = ObjectIDUtil.JDO_PREFIX + ObjectIDUtil.JDO_PREFIX_SEPARATOR;
				
				if (s.startsWith(jdoStart))
					dimensionValues[i] = ObjectIDUtil.createObjectID(s);
			}
		}
	}

	/**
	 * This method ensures that in <code>dimensionValues</code> each class exists
	 * only once. As every class defines one dimension, multiple instances of the same class are not valid.
	 *
	 * @param dimensionValues values for each dimension, together making up the coordinate.
	 */
	private static void assertDimensionValuesUniqueClasses(Object[] dimensionValues)
	{
		if (dimensionValues == null || dimensionValues.length == 0)
			return;

		Set dimensionValueClasses = new HashSet(dimensionValues.length);
		for (int i = 0; i < dimensionValues.length; i++) {
			Object dimensionValue = dimensionValues[i];
			if (dimensionValue == null)
				continue;

			Class dimensionValueClass = dimensionValue.getClass();
			if (dimensionValueClasses.contains(dimensionValueClass))
				throw new IllegalArgumentException("dimensionValues contains multiple values for the same dimensionClass: " + dimensionValueClass.getName());
		}
	}

	/**
	 * This method searches the first instance of the type specified by <code>dimensionClass</code>
	 * in the given <code>dimensionValues</code> and returns it. If there is none, it returns <code>null</code>.
	 *
	 * @param dimensionValues the parts (i.e. one value per dimension) of the coordinate
	 * @param dimensionClass the class specifying the dimension
	 * @return the first instance of the specified class or <code>null</code>, if it does not exist.
	 */
	protected static Object getDimensionValue(Object[] dimensionValues, Class<? extends Object> dimensionClass)
	{
		for (int i = 0; i < dimensionValues.length; i++) {
			Object dimensionValue = dimensionValues[i];
			if (dimensionClass.isInstance(dimensionValue))
				return dimensionValue;
		}
		return null;
	}

	public PriceCoordinate(IPriceCoordinate priceCoordinate)
	{
		this.priceConfig = priceCoordinate.getPriceConfig();

		this.customerGroupPK = priceCoordinate.getCustomerGroupPK();
		this.tariffPK = priceCoordinate.getTariffPK();
		this.currencyID = priceCoordinate.getCurrencyID();
	}

	/**
	 * This constructor creates a <tt>PriceCoordinate</tt> which is identical to
	 * <tt>currentCell</tt> except for the dimensions defined in <tt>address</tt>.
	 * This means every field in <tt>address</tt> which is NOT <tt>null</tt> overrides
	 * the value from <tt>currentCell</tt>.
	 *
	 * @param currentCell The reference address.
	 * @param address All fields that are different from the reference address.
	 */
	public PriceCoordinate(IPriceCoordinate currentCell, IPriceCoordinate address)
	{
		this.priceConfig = currentCell.getPriceConfig();

		this.customerGroupPK =
				address.getCustomerGroupPK() != null ?
						address.getCustomerGroupPK() : currentCell.getCustomerGroupPK();
		this.tariffPK =
				address.getTariffPK() != null ?
						address.getTariffPK() : currentCell.getTariffPK();
		this.currencyID =
				address.getCurrencyID() != null ?
						address.getCurrencyID() : currentCell.getCurrencyID();
	}

//	/**
//	 * Every parameter - except <tt>priceCoordinate</tt> can be <tt>null</tt>. If a parameter
//	 * is null, the value from <tt>priceCoordinate</tt> is taken - otherwise the given value
//	 * overrides.
//	 *
//	 * @param priceCoordinate
//	 * @param customerGroupPK
//	 * @param tariffPK
//	 * @param currencyID
//	 */
//	public PriceCoordinate(IPriceCoordinate priceCoordinate,
//			String customerGroupPK, String tariffPK,
//			String currencyID)
//	{
//		this.priceConfig = priceCoordinate.getPriceConfig();
//
//		this.customerGroupPK = customerGroupPK != null ? customerGroupPK : priceCoordinate.getCustomerGroupPK();
//		this.tariffPK = tariffPK != null ? tariffPK : priceCoordinate.getTariffPK();
//		this.currencyID = currencyID != null ? currencyID : priceCoordinate.getCurrencyID();
//	}

	public PriceCoordinate(
			PriceConfig priceConfig,
			CustomerGroup customerGroup,
			Tariff tariff, Currency currency)
	{
		this.priceConfig = priceConfig;
		
		this.customerGroupPK = customerGroup.getPrimaryKey();
		this.tariffPK = tariff.getPrimaryKey();
		this.currencyID = currency.getCurrencyID();
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient String tariffOrganisationID = null;

	protected String getFirstPartOfPrimaryKeyString(String pk)
	{
		if (pk == null)
			return null;

		int i = pk.indexOf('/');
		if (i < 0)
			throw new IllegalStateException("pk does not contain '/': " + pk);

		return pk.substring(0, i);
	}

	public String getTariffOrganisationID()
	{
		if (tariffOrganisationID == null)
			tariffOrganisationID = getFirstPartOfPrimaryKeyString(tariffPK);

		return tariffOrganisationID;
	}

	/**
	 * This method ignores the member <tt>PriceCoordinate.priceConfig</tt> <strong>and
	 * the primary key</strong> to allow
	 * cross-PriceConfig-addressing of cells using <tt>PriceCoordinate</tt> instances.
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof IPriceCoordinate))
			return false;

		IPriceCoordinate other = (IPriceCoordinate)obj;

		return
				Util.equals(this.customerGroupPK, other.getCustomerGroupPK()) &&
				Util.equals(this.tariffPK, other.getTariffPK()) &&
				Util.equals(this.currencyID, other.getCurrencyID());
	}

	/**
	 * This method ignores the member <tt>PriceCoordinate.priceConfig</tt> <strong>and the
	 * primary key</strong> to allow
	 * cross-PriceConfig-addressing of cells using <tt>PriceCoordinate</tt> instances.
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (thisHashCode == 0)
			thisHashCode = toString().hashCode();
		return thisHashCode;
	}
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient String thisString = null;

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (thisString == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getClass().getName());
			sb.append('{');
			sb.append(this.customerGroupPK);
			sb.append(',');
			sb.append(this.tariffPK);
			sb.append(',');
			sb.append(this.currencyID);
			sb.append('}');
			thisString = sb.toString();
		}
		return thisString;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient int thisHashCode = 0;

	public String getCurrencyID()
	{
		return currencyID;
	}

	public String getCustomerGroupPK()
	{
		return customerGroupPK;
	}

	public PriceConfig getPriceConfig()
	{
		return priceConfig;
	}

	public String getTariffPK()
	{
		return tariffPK;
	}

	public void setCurrencyID(String currencyID)
	{
		this.currencyID = currencyID;
		thisString = null;
		thisHashCode = 0;
	}

	public void setCustomerGroupPK(String customerGroupPK)
	{
		this.customerGroupPK = customerGroupPK;
		thisString = null;
		thisHashCode = 0;
	}

	public void setTariffPK(String tariffPK)
	{
		this.tariffPK = tariffPK;
		this.tariffOrganisationID = null;
		thisString = null;
		thisHashCode = 0;
	}

	public void jdoPreStore()
	{
		if (priceConfig == null)
			logger.warn("The field 'priceConfig' is null! This means, this PriceCoordinate has only been created on the fly for calculation reasons.", new Exception());
//			throw new IllegalStateException("The field 'priceConfig' is null! This means, this PriceCoordinate has only been created on the fly for calculation reasons. How the hell did it come here? I cannot persist it!");

		if (organisationID == null || priceCoordinateID < 0) {
//			logger.info("This PriceCoordinate does not have an ID - will assign one!", new Exception());
			organisationID = IDGenerator.getOrganisationID();
			priceCoordinateID = IDGenerator.nextID(PriceCoordinate.class);
//			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//			Accounting accounting = Accounting.getAccounting(pm);
//			this.organisationID = accounting.getOrganisationID();
//			this.priceCoordinateID = accounting.createPriceCoordinateID();
		}
	}

}
