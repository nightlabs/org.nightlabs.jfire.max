package org.nightlabs.jfire.accounting.tariffuserset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.util.Util;

/**
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.tariffuserset.id.TariffUserSetID"
 *		detachable="true"
 *		table="JFireTrade_TariffUserSet"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, tariffUserSetID"
 */
public class TariffUserSet
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String tariffUserSetID;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="org.nightlabs.jfire.accounting.tariffuserset.AuthorizedObjectRef"
	 *		mapped-by="tariffUserSet"
	 *		dependent-value="true"
	 *
	 * @jdo.key mapped-by="authorizedObjectID"
	 */
	private Map<String, AuthorizedObjectRef> authorizedObjectRefs;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient TariffUserSetController tariffUserSetController;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TariffUserSet() { }

	public TariffUserSet(String organisationID, String tariffUserSetID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(tariffUserSetID, "tariffUserSetID");
		this.organisationID = organisationID;
		this.tariffUserSetID = tariffUserSetID;

		authorizedObjectRefs = new HashMap<String, AuthorizedObjectRef>();
	}

	public void setTariffUserSetController(TariffUserSetController tariffUserSetController) {
		this.tariffUserSetController = tariffUserSetController;
	}

	public TariffUserSetController getTariffUserSetController() {
		return tariffUserSetController;
	}

	public TariffUserSetController getTariffUserSetController(boolean throwExceptionIfNotAssigned) {
		if (tariffUserSetController == null)
			throw new IllegalStateException("There is no TariffUserSetController assigned! You must call setTariffUserSetController(...) before!");

		return tariffUserSetController;
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public String getTariffUserSetID() {
		return tariffUserSetID;
	}

	protected AuthorizedObjectRef createAuthorizedObjectRef(AuthorizedObjectID authorizedObjectID)
	{
		String authorizedObjectIDAsString = authorizedObjectID.toString();
		AuthorizedObjectRef authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectIDAsString);
		if (authorizedObjectRef == null) {
			authorizedObjectRef = new AuthorizedObjectRef(this, authorizedObjectID);
			authorizedObjectRefs.put(authorizedObjectIDAsString, authorizedObjectRef);
		};
		return authorizedObjectRef;
	}

	private void addAuthorizedObjectIndirectly(AuthorizedObjectID authorizedObjectID)
	{
		AuthorizedObjectRef authorizedObjectRef = createAuthorizedObjectRef(authorizedObjectID);
		authorizedObjectRef.incReferenceCount();

		Set<AuthorizedObjectID> memberIDs = getTariffUserSetController(true).getUserSecurityGroupMemberIDs(authorizedObjectID);
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				addAuthorizedObjectIndirectly(memberID);
			}
		}
	}

	private void removeAuthorizedObjectIndirectly(AuthorizedObjectID authorizedObjectID)
	{
		String authorizedObjectIDAsString = authorizedObjectID.toString();
		AuthorizedObjectRef authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectIDAsString);
		if (authorizedObjectRef == null)
			return;

		Set<AuthorizedObjectID> memberIDs = getTariffUserSetController(true).getUserSecurityGroupMemberIDs(authorizedObjectID);
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				removeAuthorizedObjectIndirectly(memberID);
			}
		}

		if (authorizedObjectRef.decReferenceCount() == 0)
			authorizedObjectRefs.remove(authorizedObjectIDAsString);
	}

	/**
	 * Add a {@link User} (via the object-id of its {@link UserLocal}) or a {@link UserSecurityGroup}
	 * (via its {@link UserSecurityGroupID}) to this <code>TariffUserSet</code>.
	 *
	 * @param authorizedObjectID
	 */
	public AuthorizedObjectRef addAuthorizedObject(AuthorizedObjectID authorizedObjectID)
	{
		AuthorizedObjectRef authorizedObjectRef = createAuthorizedObjectRef(authorizedObjectID);
		if (authorizedObjectRef.isDirectlyReferenced())
			return authorizedObjectRef;

		authorizedObjectRef.setDirectlyReferenced(true);

		Set<AuthorizedObjectID> memberIDs = getTariffUserSetController(true).getUserSecurityGroupMemberIDs(authorizedObjectID);
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				addAuthorizedObjectIndirectly(memberID);
			}
		}

		return authorizedObjectRef;
	}

	public void removeAuthorizedObject(AuthorizedObjectID authorizedObjectID)
	{
		String authorizedObjectIDAsString = authorizedObjectID.toString();
		AuthorizedObjectRef authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectIDAsString);
		if (authorizedObjectRef == null)
			return;

		if (!authorizedObjectRef.isDirectlyReferenced())
			return;

		for (TariffRef tariffRef : new ArrayList<TariffRef>(authorizedObjectRef.getTariffRefs())) {
			authorizedObjectRef.removeTariff(tariffRef.getTariff());
		}

		authorizedObjectRef.setDirectlyReferenced(false);

		Set<AuthorizedObjectID> memberIDs = getTariffUserSetController(true).getUserSecurityGroupMemberIDs(authorizedObjectID);
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				removeAuthorizedObjectIndirectly(memberID);
			}
		}

		if (authorizedObjectRef.getReferenceCount() == 0)
			authorizedObjectRefs.remove(authorizedObjectIDAsString);
	}

	public AuthorizedObjectRef getAuthorizedObjectRef(AuthorizedObjectID authorizedObjectID)
	{
		String authorizedObjectIDAsString = authorizedObjectID.toString();
		AuthorizedObjectRef authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectIDAsString);
		return authorizedObjectRef;
	}

	protected void internalRemoveAuthorizedObjectRef(AuthorizedObjectRef authorizedObjectRef) {
		if (!this.equals(authorizedObjectRef.getTariffUserSet()))
			throw new IllegalArgumentException("this != authorizedObjectRef.getTariffUserSet() :: " + this + " != " + authorizedObjectRef.getTariffUserSet());

		AuthorizedObjectRef internalAuthorizedObjectRef = authorizedObjectRefs.remove(authorizedObjectRef.getAuthorizedObjectID());
		if (internalAuthorizedObjectRef == null)
			throw new IllegalStateException("AuthorizedObjectRef not found! " + authorizedObjectRef);

		if (!internalAuthorizedObjectRef.equals(authorizedObjectRef))
			throw new IllegalStateException("internalAuthorizedObjectRef != authorizedObjectRef :: " + internalAuthorizedObjectRef + " != " + authorizedObjectRef);

		if (internalAuthorizedObjectRef.getReferenceCount() != 0)
			throw new IllegalStateException("internalAuthorizedObjectRef.referenceCount != 0 :: internalAuthorizedObjectRef = " + internalAuthorizedObjectRef + " :: internalAuthorizedObjectRef.referenceCount = " + internalAuthorizedObjectRef.getReferenceCount());

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((tariffUserSetID == null) ? 0 : tariffUserSetID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final TariffUserSet other = (TariffUserSet) obj;

		return (
				Util.equals(this.tariffUserSetID, other.tariffUserSetID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + tariffUserSetID + ']';
	}
}
