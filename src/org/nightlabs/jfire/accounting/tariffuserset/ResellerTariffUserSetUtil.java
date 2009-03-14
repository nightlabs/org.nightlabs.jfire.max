package org.nightlabs.jfire.accounting.tariffuserset;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.IEntityUserSet;
import org.nightlabs.jfire.entityuserset.ResellerEntityUserSetUtil;

public class ResellerTariffUserSetUtil extends ResellerEntityUserSetUtil<Tariff, ResellerTariffUserSet>
{
	public ResellerTariffUserSetUtil(PersistenceManager pm) {
		super(pm);
	}

	@Override
	protected ResellerTariffUserSet createResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Tariff> backendEntityUserSet) {
		return new ResellerTariffUserSet((TariffUserSet)backendEntityUserSet);
	}

	@Override
	protected Class<? extends ResellerTariffUserSet> getResellerEntityUserSetClass() {
		return ResellerTariffUserSet.class;
	}
}
