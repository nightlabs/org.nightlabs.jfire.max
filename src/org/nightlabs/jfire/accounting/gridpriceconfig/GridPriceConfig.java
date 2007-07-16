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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.TariffMapping;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
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
public abstract class GridPriceConfig extends PriceConfig
{
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
	private Map<String, Tariff> tariffs = new HashMap<String, Tariff>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected GridPriceConfig() { }

	/**
	 * @param organisationID
	 * @param priceConfigID
	 */
	public GridPriceConfig(String organisationID, long priceConfigID)
	{
		super(organisationID, priceConfigID);
	}

	@Implement
	public boolean isDependentOnOffer()
	{
		return false;
	}

	/**
	 * @return Returns the customerGroups.
	 */
	public Collection<CustomerGroup> getCustomerGroups()
	{
		return customerGroups.values();
	}
	public boolean addCustomerGroup(CustomerGroup customerGroup)
	{
		return null == customerGroups.put(customerGroup.getPrimaryKey(), customerGroup);
	}
	public CustomerGroup getCustomerGroup(String organisationID, String customerGroupID, boolean throwExceptionIfNotExistent)
	{
		CustomerGroup customerGroup = (CustomerGroup) customerGroups.get(CustomerGroup.getPrimaryKey(organisationID, customerGroupID));
		if (customerGroup == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No CustomerGroup registered with organisationID=\""+organisationID+"\" customerGroupID=\""+customerGroupID+"\"!");
		return customerGroup;
	}
	public boolean containsCustomerGroup(CustomerGroup customerGroup)
	{
		return customerGroups.containsKey(customerGroup.getPrimaryKey());
	}
	public CustomerGroup removeCustomerGroup(String organisationID, String customerGroupID)
	{
		return (CustomerGroup) customerGroups.remove(
				CustomerGroup.getPrimaryKey(organisationID, customerGroupID));
	}

	/**
	 * @return Returns the tariffs.
	 */
	public Collection<Tariff> getTariffs()
	{
		return tariffs.values();
	}
	public boolean addTariff(Tariff tariff)
	{
		return null == tariffs.put(tariff.getPrimaryKey(), tariff);
	}
	public Tariff getTariff(String organisationID, String tariffID, boolean throwExceptionIfNotExistent)
	{
		Tariff tariff = (Tariff) tariffs.get(Tariff.getPrimaryKey(organisationID, tariffID));
		if (tariff == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("There is no Tariff registered with organisationID=\""+organisationID+"\" tariffID=\""+tariffID+"\"!");

		return tariff;
	}
	public boolean containsTariff(Tariff tariff)
	{
		return tariffs.containsKey(tariff.getPrimaryKey());
	}
	public Tariff removeTariff(String organisationID, String tariffID)
	{
		return (Tariff) tariffs.remove(Tariff.getPrimaryKey(organisationID, tariffID));
	}
	
	/**
	 * Calls {@link #adoptParameters(GridPriceConfig, boolean)} with <tt>onlyAdd=false</tt>.
	 */
	public void adoptParameters(IPriceConfig other)
	{
		adoptParameters(other, false);
	}
	
	/**
	 * This method adjusts the own parameter config according to the given other
	 * TicketingPriceConfig. After this method has been called, this instance
	 * has the same <tt>CustomerGroup</tt> s, <tt>SaleMode</tt> s, <tt>Tariff</tt> s,
	 * <tt>CategorySet</tt> s and <tt>Currency</tt> s as the other. While adopting it,
	 * the two descendants <tt>FormulaPriceConfig</tt> and <tt>StablePriceConfig</tt>
	 * create missing formula/price cells and remove cells that are not needed anymore.
	 * <p>
	 * Note, that this method leaves the <tt>PriceFragmentType</tt> s untouched!
	 * <tt>PriceFragmentType</tt> s are different, because they do not define cells, but
	 * are fragments within a cell. Additionally, we need to handover all fragments and
	 * cannot ignore any. All the other parameters are filtered by whatever the guiding
	 * inner price config defines, but <tt>PriceFragmentType</tt> s are merged (i.e. one
	 * occurence anywhere in the package forces the packagePriceConfig to know it).
	 *
	 * @param other The other GridPriceConfig from which to take over the parameter config.
	 * @param onlyAdd If this is true, no parameter will be removed and only missing params added.
	 */
	public void adoptParameters(IPriceConfig _other, boolean onlyAdd)
	{
		if (_other == null)
			throw new IllegalArgumentException("Param 'IPriceConfig other' must not be null!");

		if (!(_other instanceof GridPriceConfig))
			throw new IllegalArgumentException("other is an instance of \""+_other.getClass().getName()+"\" but must be GridPriceConfig!");

		GridPriceConfig other = (GridPriceConfig)_other;

		// assimilate CustomerGroup s
		if (!onlyAdd) {
			HashSet customerGroupsToRemove = onlyAdd ? null : new HashSet();
			for (Iterator it = this.getCustomerGroups().iterator(); it.hasNext(); ) {
				CustomerGroup cg = (CustomerGroup)it.next();
				if (!onlyAdd && !other.containsCustomerGroup(cg))
					customerGroupsToRemove.add(cg);
			}
//		if (!onlyAdd) {
			for (Iterator it = customerGroupsToRemove.iterator(); it.hasNext(); ) {
				CustomerGroup cg = (CustomerGroup)it.next();
				this.removeCustomerGroup(cg.getOrganisationID(), cg.getCustomerGroupID());
			}
		}
		for (Iterator it = other.getCustomerGroups().iterator(); it.hasNext(); )
			this.addCustomerGroup((CustomerGroup)it.next());


		// assimilate Tariff s
		if (!onlyAdd) {
			HashSet tariffsToRemove = onlyAdd ? null : new HashSet();
			for (Iterator it = this.getTariffs().iterator(); it.hasNext(); ) {
				Tariff t = (Tariff)it.next();
				if (!onlyAdd && !other.containsTariff(t))
					tariffsToRemove.add(t);
			}
//			if (!onlyAdd) {
			for (Iterator it = tariffsToRemove.iterator(); it.hasNext(); ) {
				Tariff t = (Tariff)it.next();
				this.removeTariff(t.getOrganisationID(), t.getTariffID());
			}
		}
		for (Iterator it = other.getTariffs().iterator(); it.hasNext(); )
			this.addTariff((Tariff)it.next());


		// assimilate Currency s
		if (!onlyAdd) {
			HashSet currenciesToRemove = onlyAdd ? null : new HashSet();
			for (Iterator it = this.getCurrencies().iterator(); it.hasNext(); ) {
				Currency c = (Currency)it.next();
				if (!onlyAdd && !other.containsCurrency(c))
					currenciesToRemove.add(c);
			}
//			if (!onlyAdd) {
			for (Iterator it = currenciesToRemove.iterator(); it.hasNext(); ) {
				Currency c = (Currency)it.next();
				this.removeCurrency(c.getCurrencyID());
			}
		}
		for (Iterator it = other.getCurrencies().iterator(); it.hasNext(); )
			this.addCurrency((Currency)it.next());


		// assimilate PriceFragmenType s
		if (!onlyAdd) {
			HashSet priceFragmenTypesToRemove = onlyAdd ? null : new HashSet();
			for (Iterator it = this.getPriceFragmentTypes().iterator(); it.hasNext(); ) {
				PriceFragmentType pft = (PriceFragmentType)it.next();
				if (!onlyAdd && !other.containsPriceFragmentType(pft))
					priceFragmenTypesToRemove.add(pft);
			}
//			if (!onlyAdd) {
			for (Iterator it = priceFragmenTypesToRemove.iterator(); it.hasNext(); ) {
				PriceFragmentType pft = (PriceFragmentType)it.next();
				this.removePriceFragmentType(pft.getOrganisationID(), pft.getPriceFragmentTypeID());
			}
		}
		for (Iterator it = other.getPriceFragmentTypes().iterator(); it.hasNext(); )
			this.addPriceFragmentType((PriceFragmentType)it.next());

	}
	
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of StablePriceConfig is currently not persistent and attached to the datastore!");
		return pm;
	}

//	public abstract PriceCell getPriceCell(PriceCoordinate priceCoordinate, boolean throwExceptionIfNotExistent);

	protected Tariff getTariff(Article article)
	{
		if (this.getOrganisationID().equals(article.getOrganisationID()))
			return article.getTariff();
		else {
			PersistenceManager pm = getPersistenceManager();

			TariffID localTariffID = (TariffID) JDOHelper.getObjectId(article.getTariff());
			String partnerTariffOrganisationID = this.getOrganisationID();
			TariffMapping tariffMapping = TariffMapping.getTariffMappingForLocalTariffAndPartner(pm, localTariffID, partnerTariffOrganisationID);

			if (tariffMapping == null)
				throw new IllegalStateException("Could not find TariffMapping for local Tariff \"" + localTariffID + "\" and partnerOrganisation \"" + partnerTariffOrganisationID + "\"!");

			return tariffMapping.getPartnerTariff();
		}
	}

	protected CustomerGroup getCustomerGroup(Article article)
	{
		if (this.getOrganisationID().equals(article.getOrganisationID()))
			return article.getOffer().getOrder().getCustomerGroup();
		else {
			PersistenceManager pm = getPersistenceManager();

			CustomerGroupID localCustomerGroupID = (CustomerGroupID) JDOHelper.getObjectId(article.getOffer().getOrder().getCustomerGroup());
			String partnerCustomerGroupOrganisationID = this.getOrganisationID();
			CustomerGroupMapping customerGroupMapping = CustomerGroupMapping.getCustomerGroupMappingForLocalCustomerGroupAndPartner(
					pm, localCustomerGroupID, partnerCustomerGroupOrganisationID);

			if (customerGroupMapping == null)
				throw new IllegalStateException("Could not find CustomerGroupMapping for local CustomerGroup \"" + localCustomerGroupID + "\" and partnerOrganisation \"" + partnerCustomerGroupOrganisationID + "\"!");

			return customerGroupMapping.getPartnerCustomerGroup();
		}
	}
}
