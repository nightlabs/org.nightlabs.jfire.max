package org.nightlabs.jfire.accounting.tariffuserset;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.IResellerEntityUserSet;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable identity-type="application" detachable="true" table="JFireTrade_ResellerTariffUserSet"
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ResellerTariffUserSet")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ResellerTariffUserSet
extends TariffUserSet
implements IResellerEntityUserSet<Tariff>
{
	private static final long serialVersionUID = 2L;

	/**
	 * The tariff-user-set of the supplier, on which this <code>ResellerTariffUserSet</code> is based. This field can be null only if this instance is itself replicated to another organisation.
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private TariffUserSet backendTariffUserSet;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ResellerTariffUserSet() { }

	public ResellerTariffUserSet(TariffUserSet backendTariffUserSet) {
		super(DUMMY);
		this.backendTariffUserSet = backendTariffUserSet;

		if (backendTariffUserSet == null)
			throw new IllegalArgumentException("backendTariffUserSet must not be null!");
	}

	@Override
	public TariffUserSet getBackendEntityUserSet() {
		return backendTariffUserSet;
	}
}
