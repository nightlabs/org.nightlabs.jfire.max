package org.nightlabs.jfire.accounting.tariffuserset;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.AuthorizedObjectRef;
import org.nightlabs.jfire.entityuserset.EntityRef;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_TariffRef")
@FetchGroups({
	@FetchGroup(
		name="AuthorizedObjectRef.entityRefs",
		members=@Persistent(name="tariff")),
	@FetchGroup(
		name="FetchGroupsEntityUserSet.replicateToReseller",
		members=@Persistent(name="tariff"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TariffRef extends EntityRef<Tariff>
{
	private static final long serialVersionUID = 2L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
