package org.nightlabs.jfire.accounting;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.CurrencyOrganisationDefaultID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.trade.config.TradeConfigModule;

/**
 * Exactly one instance of this class is persisted into every datastore in order to
 * specify the organisation's default currency.
 * <p>
 * The {@link TradeConfigModule} accesses
 * it then to specify the default currency of a certain user or user-config-group.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		objectIdClass=CurrencyOrganisationDefaultID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_CurrencyOrganisationDefault"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class CurrencyOrganisationDefault
{
	public static CurrencyOrganisationDefault getCurrencyOrganisationDefault(PersistenceManager pm)
	{
		CurrencyOrganisationDefault result = null;
		{
			Iterator<CurrencyOrganisationDefault> it = pm.getExtent(CurrencyOrganisationDefault.class).iterator();
			if (it.hasNext())
				result = it.next();

			if (it.hasNext())
				throw new IllegalStateException("There are multiple instances of CurrencyOrganisationDefault in the datastore!");
		}

		if (result == null) {
			result = new CurrencyOrganisationDefault();
			result.organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

			Currency currency = null;
			try {
				currency = (Currency) pm.getObjectById(CurrencyID.create("EUR"));
			} catch (JDOObjectNotFoundException x) {
				// EUR does not exist - initialise below with the first one that exists
			}

			if (currency == null) {
				Query q = pm.newQuery(Currency.class);
				q.setOrdering("this.currencyID ASCENDING"); // we sort it to ensure that it's always the same
				Collection<?> c = (Collection<?>) q.execute();
				Iterator<?> it = c.iterator();
				if (it.hasNext())
					currency = (Currency) it.next();
				else
					throw new IllegalStateException("There is no Currency in the datastore! Cannot initialise CurrencyOrganisationDefault!");
			}

			result.currency = currency;

			// finally persist the new CurrencyOrganisationDefault
			result = pm.makePersistent(result);
		}

		return result;
	}

	@PrimaryKey
	private String organisationID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Currency currency;

	protected CurrencyOrganisationDefault() { }

	public String getOrganisationID() {
		return organisationID;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		if (currency == null)
			throw new IllegalArgumentException("currency must not be null!");

		this.currency = currency;
	}
}
