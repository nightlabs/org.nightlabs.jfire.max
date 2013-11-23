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

import javax.jdo.JDOHelper;
import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import org.nightlabs.jfire.accounting.gridpriceconfig.id.PriceCoordinateID;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

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
@PersistenceCapable(
	objectIdClass=PriceCoordinateID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_PriceCoordinate")
@FetchGroups({
	@FetchGroup(
		name=PriceCoordinate.FETCH_GROUP_PRICE_CONFIG,
		members=@Persistent(name="priceConfig")),
	@FetchGroup(
		fetchGroups={"default"},
		name=PriceCoordinate.FETCH_GROUP_THIS_PRICE_COORDINATE,
		members=@Persistent(name="priceConfig")),
	@FetchGroup(
		name="FetchGroupsPriceConfig.edit",
		members=@Persistent(name="priceConfig"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PriceCoordinate implements Serializable, StoreCallback, IPriceCoordinate
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(PriceCoordinate.class);

	public static final String FETCH_GROUP_PRICE_CONFIG = "PriceCoordinate.priceConfig";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_PRICE_COORDINATE = "PriceCoordinate.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID = null;
	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long priceCoordinateID = -1;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String customerGroupPK;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String tariffPK;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String currencyID;

	/**
	 * @!jdo.field persistence-modifier="persistent" null-value="exception"
	 * TODO the above null-value="exception" is correct but causes a problem when replicating to another datastore due to a jpox bug
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PriceConfig priceConfig;

	public PriceCoordinate() { }

	/**
	 * <strong>WARNING:</strong> When using this constructor in java code, one of the
	 * arguments <b>must</b> be the PriceConfig to which this coordinate belongs!
	 * Otherwise, it cannot be persisted into the database! Without the owning price config
	 * in the parameter list, it is only intended for usage as address in javascript formulas!
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
	 * Furthermore, instead of passing object-ids it is possible to pass JDO objects directly.
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

		this.priceConfig = getPriceConfigFromDimensionValues(dimensionValues);
		IPriceCoordinate otherPriceCoordinate = getPriceCoordinateFromDimensionValues(dimensionValues);
		if (otherPriceCoordinate != null) {
			this.customerGroupPK = otherPriceCoordinate.getCustomerGroupPK();
			this.tariffPK = otherPriceCoordinate.getTariffPK();
			this.currencyID = otherPriceCoordinate.getCurrencyID();
		}

		CustomerGroupID customerGroupID = (CustomerGroupID) getDimensionValue(dimensionValues, CustomerGroupID.class, CustomerGroup.class);
		if (customerGroupID != null)
			this.customerGroupPK = customerGroupID.getPrimaryKey();

		TariffID tariffID = (TariffID) getDimensionValue(dimensionValues, TariffID.class, Tariff.class);
		if (tariffID != null)
			this.tariffPK = tariffID.getPrimaryKey();

		CurrencyID currencyID = (CurrencyID) getDimensionValue(dimensionValues, CurrencyID.class, Currency.class);
		if (currencyID != null)
			this.currencyID = currencyID.currencyID;
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

		Set<Class<?>> dimensionValueClasses = new HashSet<Class<?>>(dimensionValues.length);
		for (int i = 0; i < dimensionValues.length; i++) {
			Object dimensionValue = dimensionValues[i];
			if (dimensionValue == null)
				continue;

			Class<?> dimensionValueClass = dimensionValue.getClass();
			if (!dimensionValueClasses.add(dimensionValueClass))
				throw new IllegalArgumentException("dimensionValues contains multiple values for the same dimensionClass: " + dimensionValueClass.getName());
		}
	}

	protected static IPriceCoordinate getPriceCoordinateFromDimensionValues(Object[] dimensionValues)
	{
		for (int i = 0; i < dimensionValues.length; i++) {
			Object dimensionValue = dimensionValues[i];
			if (dimensionValue instanceof IPriceCoordinate)
				return (IPriceCoordinate) dimensionValue;
		}
		return null;
	}

	protected static PriceConfig getPriceConfigFromDimensionValues(Object[] dimensionValues)
	{
		for (int i = 0; i < dimensionValues.length; i++) {
			Object dimensionValue = dimensionValues[i];
			if (dimensionValue instanceof IPriceConfig)
				return (PriceConfig) dimensionValue; // every class implementing IPriceConfig must be a subclass of PriceConfig
		}
		return null;
	}

	/**
	 * This method searches the first instance of the type specified by <code>dimensionClass</code>
	 * in the given <code>dimensionValues</code> and returns it. If there is none, it returns <code>null</code>.
	 *
	 * @param dimensionValues the parts (i.e. one value per dimension) of the coordinate
	 * @param dimensionObjectIDClass the class specifying the dimension
	 * @return the first instance of the specified class or <code>null</code>, if it does not exist.
	 */
	protected static Object getDimensionValue(Object[] dimensionValues, Class<?> dimensionObjectIDClass, Class<?> dimensionObjectClass)
	{
		for (int i = 0; i < dimensionValues.length; i++) {
			Object dimensionValue = dimensionValues[i];
			if (dimensionObjectIDClass.isInstance(dimensionValue))
				return dimensionValue;

			if (dimensionObjectClass.isInstance(dimensionValue)) {
				Object objectID = JDOHelper.getObjectId(dimensionValue);
				if (objectID == null)
					throw new IllegalArgumentException("dimensionValue \"" + dimensionValue + "\" is an instance of " + dimensionObjectClass + " but has no object-id assigned!");

				return objectID;
			}
		}
		return null;
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

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
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

	@Override
	public void assertAllDimensionValuesAssigned() {
		if (customerGroupPK == null)
			throw new IllegalStateException("customerGroupPK == null");

		if (tariffPK == null)
			throw new IllegalStateException("tariffPK == null");

		if (currencyID == null)
			throw new IllegalStateException("currencyID == null");
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
		return (Util.hashCode(customerGroupPK) * 31 + Util.hashCode(tariffPK)) * 31 + Util.hashCode(currencyID);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append('[');
		sb.append(this.customerGroupPK);
		sb.append(',');
		sb.append(this.tariffPK);
		sb.append(',');
		sb.append(this.currencyID);
		sb.append(']');
		return sb.toString();
	}

	@Override
	public String getCurrencyID()
	{
		return currencyID;
	}

	@Override
	public String getCustomerGroupPK()
	{
		return customerGroupPK;
	}

	@Override
	public PriceConfig getPriceConfig()
	{
		return priceConfig;
	}

	@Override
	public String getTariffPK()
	{
		return tariffPK;
	}

	@Override
	public void setCurrencyID(String currencyID)
	{
		this.currencyID = currencyID;
	}

	@Override
	public void setCustomerGroupPK(String customerGroupPK)
	{
		this.customerGroupPK = customerGroupPK;
	}

	@Override
	public void setTariffPK(String tariffPK)
	{
		this.tariffPK = tariffPK;
		this.tariffOrganisationID = null;
	}

	@Override
	public void jdoPreStore()
	{
		if (priceConfig == null)
			logger.warn("The field 'priceConfig' is null! This means, this PriceCoordinate has only been created on the fly for calculation reasons.", new Exception());
//			throw new IllegalStateException("The field 'priceConfig' is null! This means, this PriceCoordinate has only been created on the fly for calculation reasons. How the hell did it come here? I cannot persist it!");

		if (organisationID == null || priceCoordinateID < 0) {
//			logger.info("This PriceCoordinate does not have an ID - will assign one!", new Exception());
			organisationID = IDGenerator.getOrganisationID();
			priceCoordinateID = IDGenerator.nextID(PriceCoordinate.class);
		}
	}

	@Override
	public IPriceCoordinate copyForPriceCalculation() {
		PriceCoordinate copy = new PriceCoordinate();
		copy.setCustomerGroupPK(this.getCustomerGroupPK());
		copy.setTariffPK(this.getTariffPK());
		copy.setCurrencyID(this.getCurrencyID());
		return copy;
	}
}
