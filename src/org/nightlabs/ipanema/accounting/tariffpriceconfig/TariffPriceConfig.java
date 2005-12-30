/*
 * Created on Feb 26, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.accounting.Currency;
import org.nightlabs.ipanema.accounting.PriceFragmentType;
import org.nightlabs.ipanema.accounting.Tariff;
import org.nightlabs.ipanema.accounting.priceconfig.IPriceConfig;
import org.nightlabs.ipanema.accounting.priceconfig.PriceConfig;
import org.nightlabs.ipanema.trade.Article;
import org.nightlabs.ipanema.trade.CustomerGroup;

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
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.priceconfig.PriceConfig"
 *		detachable="true"
 *		table="JFireTrade_TariffPriceConfig"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="TariffPriceConfig.customerGroups" fields="customerGroups"
 * @jdo.fetch-group name="TariffPriceConfig.tariffs" fields="tariffs"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="customerGroups, tariffs"
 */
public abstract class TariffPriceConfig extends PriceConfig
{
	public static final String FETCH_GROUP_CUSTOMER_GROUPS = "TariffPriceConfig.customerGroups";
	public static final String FETCH_GROUP_TARIFFS = "TariffPriceConfig.tariffs";

	/**
	 * key: String customerGroupPK<br/>
	 * value: CustomerGroup customerGroup
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="CustomerGroup"
	 *		table="JFireTrade_TariffPriceConfig_customerGroups"
	 *
	 * @jdo.join
	 */
	private Map customerGroups = new HashMap();

	/**
	 * key: String tariffPK<br/>
	 * value: Tariff tariff
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="Tariff"
	 *		table="JFireTrade_TariffPriceConfig_tariffs"
	 *
	 * @jdo.join
	 */
	private Map tariffs = new HashMap();

	protected TariffPriceConfig()
	{
	}
	/**
	 * @param organisationID
	 * @param priceConfigID
	 */
	public TariffPriceConfig(String organisationID, long priceConfigID)
	{
		super(organisationID, priceConfigID);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.priceconfig.PriceConfig#isDependentOnOffer()
	 */
	public boolean isDependentOnOffer()
	{
		return false;
	}

	/**
	 * @return Returns the customerGroups.
	 */
	public Collection getCustomerGroups()
	{
		return customerGroups.values();
	}
	public void addCustomerGroup(CustomerGroup customerGroup)
	{
		customerGroups.put(customerGroup.getPrimaryKey(), customerGroup);
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
	public Collection getTariffs()
	{
		return tariffs.values();
	}
	public void addTariff(Tariff tariff)
	{
		tariffs.put(tariff.getPrimaryKey(), tariff);
	}
	public Tariff getTariff(String organisationID, long tariffID, boolean throwExceptionIfNotExistent)
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
	public Tariff removeTariff(String organisationID, long tariffID)
	{
		return (Tariff) tariffs.remove(Tariff.getPrimaryKey(organisationID, tariffID));
	}
	
	/**
	 * Calls {@link #adoptParameters(TariffPriceConfig, boolean)} with <tt>onlyAdd=false</tt>.
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
	 * @param other The other TariffPriceConfig from which to take over the parameter config.
	 * @param onlyAdd If this is true, no parameter will be removed and only missing params added.
	 */
	public void adoptParameters(IPriceConfig _other, boolean onlyAdd)
	{
		if (_other == null)
			throw new NullPointerException("Param 'IPriceConfig other' must not be null!");

		if (!(_other instanceof TariffPriceConfig))
			throw new IllegalArgumentException("other is an instance of \""+_other.getClass().getName()+"\" but must be TariffPriceConfig!");

		TariffPriceConfig other = (TariffPriceConfig)_other;

		// assimilate CustomerGroup s
		HashSet customerGroupsToRemove = onlyAdd ? null : new HashSet();
		for (Iterator it = this.getCustomerGroups().iterator(); it.hasNext(); ) {
			CustomerGroup cg = (CustomerGroup)it.next();
			if (!onlyAdd && !other.containsCustomerGroup(cg))
				customerGroupsToRemove.add(cg);
		}
		if (!onlyAdd) {
			for (Iterator it = customerGroupsToRemove.iterator(); it.hasNext(); ) {
				CustomerGroup cg = (CustomerGroup)it.next();
				this.removeCustomerGroup(cg.getOrganisationID(), cg.getCustomerGroupID());
			}
		}
		for (Iterator it = other.getCustomerGroups().iterator(); it.hasNext(); )
			this.addCustomerGroup((CustomerGroup)it.next());


		// assimilate Tariff s
		HashSet tariffsToRemove = onlyAdd ? null : new HashSet();
		for (Iterator it = this.getTariffs().iterator(); it.hasNext(); ) {
			Tariff t = (Tariff)it.next();
			if (!onlyAdd && !other.containsTariff(t))
				tariffsToRemove.add(t);
		}
		if (!onlyAdd) {
			for (Iterator it = tariffsToRemove.iterator(); it.hasNext(); ) {
				Tariff t = (Tariff)it.next();
				this.removeTariff(t.getOrganisationID(), t.getTariffID());
			}
		}
		for (Iterator it = other.getTariffs().iterator(); it.hasNext(); )
			this.addTariff((Tariff)it.next());


		// assimilate Currency s
		HashSet currenciesToRemove = onlyAdd ? null : new HashSet();
		for (Iterator it = this.getCurrencies().iterator(); it.hasNext(); ) {
			Currency c = (Currency)it.next();
			if (!onlyAdd && !other.containsCurrency(c))
				currenciesToRemove.add(c);
		}
		if (!onlyAdd) {
			for (Iterator it = currenciesToRemove.iterator(); it.hasNext(); ) {
				Currency c = (Currency)it.next();
				this.removeCurrency(c.getCurrencyID());
			}
		}
		for (Iterator it = other.getCurrencies().iterator(); it.hasNext(); )
			this.addCurrency((Currency)it.next());


		// assimilate PriceFragmenType s
		HashSet priceFragmenTypesToRemove = onlyAdd ? null : new HashSet();
		for (Iterator it = this.getPriceFragmentTypes().iterator(); it.hasNext(); ) {
			PriceFragmentType pft = (PriceFragmentType)it.next();
			if (!onlyAdd && !other.containsPriceFragmentType(pft))
				priceFragmenTypesToRemove.add(pft);
		}
		if (!onlyAdd) {
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

	protected CustomerGroup getCustomerGroup(Article article)
	{
		PersistenceManager pm = getPersistenceManager();

		return article.getOffer().getOrder().getCustomerGroup();
////	TODO we need to lookup the customerGroup differently later. We must not rely on it being an end customer
////	In fact, it should be the same for the end customer as for every other customer: Store the desired customerGroup in the offer!
//		CustomerGroup customerGroup;
//		if (article.getOffer().getOrder().getCustomer() instanceof OrganisationLegalEntity)
//			throw new UnsupportedOperationException("Currently only end-customers are supported!");
//		else
//			customerGroup = Accounting.getAccounting(pm).getCustomerGroupForEndCustomer();
//
//		return customerGroup;
	}
}
