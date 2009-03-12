package org.nightlabs.jfire.accounting.tariffuserset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserLocalID;

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
{
	private static final long serialVersionUID = 1L;

	public static ResellerTariffUserSet getResellerTariffUserSetForBackendTariffUserSet(PersistenceManager pm, TariffUserSet backendTariffUserSet)
	{
		if (backendTariffUserSet == null)
			throw new IllegalArgumentException("backendTariffUserSet must not be null!");

		Query q = pm.newNamedQuery(ResellerTariffUserSet.class, "getResellerTariffUserSetForBackendTariffUserSet");
		return (ResellerTariffUserSet) q.execute(backendTariffUserSet);
	}

	public static ResellerTariffUserSet configureResellerTariffUserSetForBackendTariffUserSet(PersistenceManager pm, TariffUserSet backendTariffUserSet)
	{
		ResellerTariffUserSet resellerTariffUserSet = getResellerTariffUserSetForBackendTariffUserSet(pm, backendTariffUserSet);
		if (resellerTariffUserSet == null) {
			resellerTariffUserSet = new ResellerTariffUserSet(backendTariffUserSet);
			resellerTariffUserSet = pm.makePersistent(resellerTariffUserSet);
		}

		if (backendTariffUserSet.getOrganisationID().equals(resellerTariffUserSet.getOrganisationID()))
			throw new IllegalStateException("backendTariffUserSet.organisationID == resellerTariffUserSet.organisationID == \"" + backendTariffUserSet.getOrganisationID() + "\" :: currentUser == " + SecurityReflector.getUserDescriptor());

		resellerTariffUserSet.setTariffUserSetController(new TariffUserSetControllerServerImpl(pm));

		UserLocalID resellerUserLocalID = UserLocalID.create(
				backendTariffUserSet.getOrganisationID(),
				User.USER_ID_PREFIX_TYPE_ORGANISATION + resellerTariffUserSet.getOrganisationID(),
				backendTariffUserSet.getOrganisationID()
		);

		AuthorizedObjectRef resellerUserLocalAuthorizedObjectRef = backendTariffUserSet.getAuthorizedObjectRef(resellerUserLocalID);
		if (resellerUserLocalAuthorizedObjectRef == null) {
			// If this AuthorizedObjectRef does not exist, we simply remove everything from the resellerTariffUserSet.
			for (AuthorizedObjectRef aor : new ArrayList<AuthorizedObjectRef>(resellerTariffUserSet.getAuthorizedObjectRefs()))
				resellerTariffUserSet.removeAuthorizedObject(aor.getAuthorizedObjectIDAsOID());
		}
		else {
			// We have to assign exactly those rights to all local users that we have in the resellerUserLocalAuthorizedObjectRef.
			Collection<? extends UserLocal> userLocals = UserLocal.getLocalUserLocals(pm);
			Set<UserLocalID> userLocalIDs = NLJDOHelper.getObjectIDSet(userLocals);

			// Remove all AuthorizedObjectRef (e.g. user-security-groups) from the DefaultTariffUserSet that shouldn't be there.
			resellerTariffUserSet.retainAuthorizedObjects(userLocalIDs);

			Collection<? extends TariffRef> tariffRefs = resellerUserLocalAuthorizedObjectRef.getTariffRefs();
			Set<Tariff> tariffs = new HashSet<Tariff>(tariffRefs.size());
			for (TariffRef tariffRef : tariffRefs)
				tariffs.add(tariffRef.getTariff());

			for (UserLocalID userLocalID : userLocalIDs) {
				AuthorizedObjectRef authorizedObjectRef = resellerTariffUserSet.addAuthorizedObject(userLocalID);
				authorizedObjectRef.retainTariffs(tariffs);
				authorizedObjectRef.addTariffs(tariffs);
			}
		}

		return resellerTariffUserSet;
	}

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
		super(IDGenerator.getOrganisationID(), ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(TariffUserSet.class)));
		this.backendTariffUserSet = backendTariffUserSet;

		if (backendTariffUserSet == null)
			throw new IllegalArgumentException("backendTariffUserSet must not be null!");
	}

	public TariffUserSet getBackendTariffUserSet() {
		return backendTariffUserSet;
	}
}
