package org.nightlabs.jfire.accounting.tariffuserset;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.IResellerEntityUserSet;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable identity-type="application" detachable="true" table="JFireTrade_ResellerTariffUserSet"
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.query name="getResellerTariffUserSetForBackendTariffUserSet" query="SELECT UNIQUE WHERE this.backendTariffUserSet == :backendTariffUserSet"
 */
public class ResellerTariffUserSet
extends TariffUserSet
implements IResellerEntityUserSet<Tariff>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent" unique="true" null-value="exception"
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
