package org.nightlabs.jfire.accounting.tariffuserset;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.AuthorizedObjectRef;
import org.nightlabs.jfire.entityuserset.EntityRef;
import org.nightlabs.jfire.entityuserset.EntityUserSet;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class TariffUserSet
extends EntityUserSet<Tariff>
{
	private static final long serialVersionUID = 3L;
	protected static final boolean DUMMY = false;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TariffUserSet() { }

	public TariffUserSet(boolean dummy) {
		super(Tariff.class);
	}

	public TariffUserSet(String organisationID, String entityUserSetID) {
		super(organisationID, Tariff.class, entityUserSetID);
	}

	@Override
	protected AuthorizedObjectRef<Tariff> createAuthorizedObjectRef(AuthorizedObjectID authorizedObjectID) {
		return new AuthorizedObjectRef<Tariff>(this, authorizedObjectID);
	}

	@Override
	protected EntityRef<Tariff> createEntityRef(AuthorizedObjectRef<Tariff> authorizedObjectRef, Tariff entity) {
		return new TariffRef(authorizedObjectRef, entity);
	}

}
