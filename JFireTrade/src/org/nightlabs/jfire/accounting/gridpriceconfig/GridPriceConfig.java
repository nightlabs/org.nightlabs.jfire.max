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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.TariffMapping;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.CustomerGroupMapping;
import org.nightlabs.jfire.trade.id.CustomerGroupID;

/**
 * This implementation of <tt>PriceConfig</tt> manages cells
 * that are dependent on <tt>CustomerGroup</tt> and <tt>Tariff</tt>.
 * Each of these parameters must be imported using one of the methods
 * <tt>addCustomerGroup(..)</tt> or <tt>addTariff</tt>.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.priceconfig.PriceConfig"
 *		detachable="true"
 *		table="JFireTrade_GridPriceConfig"
 *
 * @jdo.inheritance strategy="superclass-table"
 *
 * @jdo.fetch-group name="GridPriceConfig.customerGroups" fields="customerGroups"
 * @jdo.fetch-group name="GridPriceConfig.tariffs" fields="tariffs"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="customerGroups, tariffs"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_GridPriceConfig")
@FetchGroups({
	@FetchGroup(
		name=GridPriceConfig.FETCH_GROUP_CUSTOMER_GROUPS,
		members=@Persistent(name="customerGroups")),
	@FetchGroup(
		name=GridPriceConfig.FETCH_GROUP_TARIFFS,
		members=@Persistent(name="tariffs")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsPriceConfig.edit",
		members={@Persistent(name="customerGroups"), @Persistent(name="tariffs")})
})
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public abstract class GridPriceConfig extends PriceConfig implements IGridPriceConfig
{
	private static final long serialVersionUID = 1L;
	public static final String FETCH_GROUP_CUSTOMER_GROUPS = "GridPriceConfig.customerGroups";
	public static final String FETCH_GROUP_TARIFFS = "GridPriceConfig.tariffs";

	/**
	 * key: String customerGroupPK<br/>
	 * value: CustomerGroup customerGroup
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="CustomerGroup"
	 *		table="JFireTrade_GridPriceConfig_customerGroups"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_GridPriceConfig_customerGroups",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, CustomerGroup> customerGroups = new HashMap<String, CustomerGroup>();

	/**
	 * key: String tariffPK<br/>
	 * value: Tariff tariff
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="Tariff"
	 *		table="JFireTrade_GridPriceConfig_tariffs"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_GridPriceConfig_tariffs",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, Tariff> tariffs = new HashMap<String, Tariff>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected GridPriceConfig() { }

	public GridPriceConfig(final PriceConfigID priceConfigID)
	{
		super(priceConfigID);
	}

	@Override
	public boolean isDependentOnOffer()
	{
		return false;
	}

	@Override
	public Collection<CustomerGroup> getCustomerGroups()
	{
		return Collections.unmodifiableCollection(customerGroups.values());
	}

	@Override
	public boolean addCustomerGroup(final CustomerGroup customerGroup)
	{
		if (customerGroups.containsKey(customerGroup.getPrimaryKey()))
			return false;

		customerGroups.put(customerGroup.getPrimaryKey(), customerGroup);
		return true;
	}

	@Override
	public CustomerGroup getCustomerGroup(final CustomerGroupID customerGroupID, final boolean throwExceptionIfNotExistent)
	{
		return getCustomerGroup(customerGroupID.organisationID, customerGroupID.customerGroupID, throwExceptionIfNotExistent);
	}

	@Override
	public CustomerGroup getCustomerGroup(final String organisationID, final String customerGroupID, final boolean throwExceptionIfNotExistent)
	{
		final CustomerGroup customerGroup = customerGroups.get(CustomerGroup.getPrimaryKey(organisationID, customerGroupID));
		if (customerGroup == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No CustomerGroup registered with organisationID=\""+organisationID+"\" customerGroupID=\""+customerGroupID+"\"!");
		return customerGroup;
	}

	@Override
	public void clearCustomerGroups() {
		for (final CustomerGroup customerGroup : new ArrayList<CustomerGroup>(customerGroups.values()))
			removeCustomerGroup(customerGroup.getOrganisationID(), customerGroup.getCustomerGroupID());
	}

	@Override
	public boolean containsCustomerGroup(final CustomerGroup customerGroup)
	{
		return customerGroups.containsKey(customerGroup.getPrimaryKey());
	}

	@Override
	public CustomerGroup removeCustomerGroup(final String organisationID, final String customerGroupID)
	{
		return customerGroups.remove(
				CustomerGroup.getPrimaryKey(organisationID, customerGroupID));
	}

	@Override
	public Collection<Tariff> getTariffs()
	{
		return Collections.unmodifiableCollection(tariffs.values());
	}

	@Override
	public boolean addTariff(final Tariff tariff)
	{
		if (tariffs.containsKey(tariff.getPrimaryKey()))
			return false;

		tariffs.put(tariff.getPrimaryKey(), tariff);
		return true;
	}

	@Override
	public Tariff getTariff(final TariffID tariffID, final boolean throwExceptionIfNotExistent)
	{
		return getTariff(tariffID.organisationID, tariffID.tariffID, throwExceptionIfNotExistent);
	}

	@Override
	public Tariff getTariff(final String organisationID, final String tariffID, final boolean throwExceptionIfNotExistent)
	{
		final Tariff tariff = tariffs.get(Tariff.getPrimaryKey(organisationID, tariffID));
		if (tariff == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("There is no Tariff registered with organisationID=\""+organisationID+"\" tariffID=\""+tariffID+"\"!");

		return tariff;
	}

	@Override
	public boolean containsTariff(final Tariff tariff)
	{
		return tariffs.containsKey(tariff.getPrimaryKey());
	}

	@Override
	public Tariff removeTariff(final String organisationID, final String tariffID)
	{
		return tariffs.remove(Tariff.getPrimaryKey(organisationID, tariffID));
	}

	@Override
	public void removeTariff(final Tariff tariff)
	{
		tariffs.remove(Tariff.getPrimaryKey(tariff.getOrganisationID(), tariff.getTariffID()));
	}

	@Override
	public void clearTariffs()
	{
		for (final Tariff tariff : new ArrayList<Tariff>(tariffs.values()))
			removeTariff(tariff);
	}

	@Override
	public void adoptParameters(final IPriceConfig other)
	{
		adoptParameters(other, false);
	}

	@Override
	public void adoptParameters(final IPriceConfig _other, final boolean onlyAdd)
	{
		if (_other == null)
			throw new IllegalArgumentException("Param 'IPriceConfig other' must not be null!");

		if (!(_other instanceof GridPriceConfig))
			throw new IllegalArgumentException("other is an instance of \""+_other.getClass().getName()+"\" but must be GridPriceConfig!");

		final GridPriceConfig other = (GridPriceConfig)_other;

		// assimilate CustomerGroup s
		if (!onlyAdd) {
			final Set<CustomerGroup> customerGroupsToRemove = onlyAdd ? null : new HashSet<CustomerGroup>();
			for (final Iterator<CustomerGroup> it = this.getCustomerGroups().iterator(); it.hasNext(); ) {
				final CustomerGroup cg = it.next();
				if (!onlyAdd && !other.containsCustomerGroup(cg))
					customerGroupsToRemove.add(cg);
			}
//		if (!onlyAdd) {
			for (final Iterator<CustomerGroup> it = customerGroupsToRemove.iterator(); it.hasNext(); ) {
				final CustomerGroup cg = it.next();
				this.removeCustomerGroup(cg.getOrganisationID(), cg.getCustomerGroupID());
			}
		}
		for (final Iterator<CustomerGroup> it = other.getCustomerGroups().iterator(); it.hasNext(); )
			this.addCustomerGroup(it.next());


		// assimilate Tariff s
		if (!onlyAdd) {
			final Set<Tariff> tariffsToRemove = onlyAdd ? null : new HashSet<Tariff>();
			for (final Iterator<Tariff> it = this.getTariffs().iterator(); it.hasNext(); ) {
				final Tariff t = it.next();
				if (!onlyAdd && !other.containsTariff(t))
					tariffsToRemove.add(t);
			}
//			if (!onlyAdd) {
			for (final Iterator<Tariff> it = tariffsToRemove.iterator(); it.hasNext(); ) {
				final Tariff t = it.next();
				this.removeTariff(t.getOrganisationID(), t.getTariffID());
			}
		}
		for (final Iterator<Tariff> it = other.getTariffs().iterator(); it.hasNext(); )
			this.addTariff(it.next());


		// assimilate Currency s
		if (!onlyAdd) {
			final Set<Currency> currenciesToRemove = onlyAdd ? null : new HashSet<Currency>();
			for (final Iterator<Currency> it = this.getCurrencies().iterator(); it.hasNext(); ) {
				final Currency c = it.next();
				if (!onlyAdd && !other.containsCurrency(c))
					currenciesToRemove.add(c);
			}
//			if (!onlyAdd) {
			for (final Iterator<Currency> it = currenciesToRemove.iterator(); it.hasNext(); ) {
				final Currency c = it.next();
				this.removeCurrency(c.getCurrencyID());
			}
		}
		for (final Iterator<Currency> it = other.getCurrencies().iterator(); it.hasNext(); )
			this.addCurrency(it.next());


		// assimilate PriceFragmenType s
		if (!onlyAdd) {
			final Set<PriceFragmentType> priceFragmenTypesToRemove = onlyAdd ? null : new HashSet<PriceFragmentType>();
			for (final Iterator<PriceFragmentType> it = this.getPriceFragmentTypes().iterator(); it.hasNext(); ) {
				final PriceFragmentType pft = it.next();
				if (!onlyAdd && !other.containsPriceFragmentType(pft))
					priceFragmenTypesToRemove.add(pft);
			}
//			if (!onlyAdd) {
			for (final Iterator<PriceFragmentType> it = priceFragmenTypesToRemove.iterator(); it.hasNext(); ) {
				final PriceFragmentType pft = it.next();
				this.removePriceFragmentType(pft.getOrganisationID(), pft.getPriceFragmentTypeID());
			}
		}
		for (final Iterator<PriceFragmentType> it = other.getPriceFragmentTypes().iterator(); it.hasNext(); )
			this.addPriceFragmentType(it.next());

	}

	protected PersistenceManager getPersistenceManager()
	{
		final PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of StablePriceConfig is currently not persistent and attached to the datastore!");
		return pm;
	}

//	public abstract PriceCell getPriceCell(PriceCoordinate priceCoordinate, boolean throwExceptionIfNotExistent);

	protected Tariff getTariff(final Article article)
	{
		if (this.getOrganisationID().equals(article.getOrganisationID()))
			return article.getTariff();
		else {
			final PersistenceManager pm = getPersistenceManager();

			final TariffID localTariffID = (TariffID) JDOHelper.getObjectId(article.getTariff());
			final String partnerTariffOrganisationID = this.getOrganisationID();
			final TariffMapping tariffMapping = TariffMapping.getTariffMappingForLocalTariffAndPartner(pm, localTariffID, partnerTariffOrganisationID);

			if (tariffMapping == null)
				throw new IllegalStateException("Could not find TariffMapping for local Tariff \"" + localTariffID + "\" and partnerOrganisation \"" + partnerTariffOrganisationID + "\"!");

			return tariffMapping.getPartnerTariff();
		}
	}

	protected CustomerGroup getCustomerGroup(final Article article)
	{
		if (this.getOrganisationID().equals(article.getOrganisationID()))
			return article.getOffer().getOrder().getCustomerGroup();
		else {
			final PersistenceManager pm = getPersistenceManager();

			final CustomerGroupID localCustomerGroupID = (CustomerGroupID) JDOHelper.getObjectId(article.getOffer().getOrder().getCustomerGroup());
			final String partnerCustomerGroupOrganisationID = this.getOrganisationID();
			final CustomerGroupMapping customerGroupMapping = CustomerGroupMapping.getCustomerGroupMappingForLocalCustomerGroupAndPartner(
					pm, localCustomerGroupID, partnerCustomerGroupOrganisationID);

			if (customerGroupMapping == null)
				throw new IllegalStateException("Could not find CustomerGroupMapping for local CustomerGroup \"" + localCustomerGroupID + "\" and partnerOrganisation \"" + partnerCustomerGroupOrganisationID + "\"!");

			return customerGroupMapping.getPartnerCustomerGroup();
		}
	}
}
