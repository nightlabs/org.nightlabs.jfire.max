package org.nightlabs.jfire.accounting.tariffuserset;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.IResellerEntityUserSet;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable identity-type="application" detachable="true" table="JFireTrade_ResellerTariffUserSet"
 * @jdo.inheritance strategy="new-table"
 */
public class ResellerTariffUserSet
extends TariffUserSet
implements IResellerEntityUserSet<Tariff>
{
	private static final long serialVersionUID = 2L;

	/**
	 * The tariff-user-set of the supplier, on which this <code>ResellerTariffUserSet</code> is based. This field can be null only if this instance is itself replicated to another organisation.
	 * @jdo.field persistence-modifier="persistent"
	 */
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
