package org.nightlabs.jfire.accounting.tariffuserset;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.IEntityUserSet;
import org.nightlabs.jfire.entityuserset.ResellerEntityUserSetFactory;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable identity-type="application" detachable="true"
 * @jdo.inheritance strategy="superclass-table"
 */
public class ResellerTariffUserSetFactory extends ResellerEntityUserSetFactory<Tariff>
{
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ResellerTariffUserSetFactory() { }

	public ResellerTariffUserSetFactory(String organisationID, String resellerEntityUserSetFactoryID, Class<? extends Tariff> entityClass) {
		super(organisationID, resellerEntityUserSetFactoryID, entityClass);
	}

	@Override
	protected ResellerTariffUserSet createResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Tariff> backendEntityUserSet) {
		return new ResellerTariffUserSet((TariffUserSet)backendEntityUserSet);
	}

	@Override
	protected Class<? extends ResellerTariffUserSet> getResellerEntityUserSetClass() {
		return ResellerTariffUserSet.class;
	}

	@Override
	public ResellerTariffUserSet configureResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Tariff> backendEntityUserSet) {
		return (ResellerTariffUserSet) super.configureResellerEntityUserSetForBackendEntityUserSet(backendEntityUserSet);
	}

	@Override
	public ResellerTariffUserSet getResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Tariff> backendEntityUserSet, boolean throwExceptionIfNotFound) {
		return (ResellerTariffUserSet) super.getResellerEntityUserSetForBackendEntityUserSet(backendEntityUserSet, throwExceptionIfNotFound);
	}
}
