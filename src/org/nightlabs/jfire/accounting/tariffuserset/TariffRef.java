package org.nightlabs.jfire.accounting.tariffuserset;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.AuthorizedObjectRef;
import org.nightlabs.jfire.entityuserset.EntityRef;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable identity-type="application" detachable="true" table="JFireTrade_TariffRef"
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="AuthorizedObjectRef.entityRefs" fields="tariff"
 *
 * @jdo.fetch-group name="FetchGroupsEntityUserSet.replicateToReseller" fields="tariff"
 */
public class TariffRef extends EntityRef<Tariff>
{
	private static final long serialVersionUID = 2L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Tariff tariff;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TariffRef() { }

	public TariffRef(AuthorizedObjectRef<Tariff> authorizedObjectRef, Tariff entity) {
		super(authorizedObjectRef, entity);
	}

	@Override
	public Tariff getEntity() {
		return tariff;
	}

	@Override
	protected void setEntity(Tariff entity) {
		this.tariff = entity;
	}

}
