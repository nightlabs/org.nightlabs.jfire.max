package org.nightlabs.jfire.accounting.tariffuserset;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.tariffuserset.id.AuthorizedObjectRefID"
 *		detachable="true"
 *		table="JFireTrade_AuthorizedObjectRef"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="tariffUserSetOrganisationID, tariffUserSetID, authorizedObjectID"
 *
 * @jdo.query
 *		name="getAuthorizedObjectRefsForAuthorizedObjectID"
 *		query="SELECT WHERE this.authorizedObjectID == :authorizedObjectID"
 */
public class AuthorizedObjectRef
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static Collection<? extends AuthorizedObjectRef> getAuthorizedObjectRefs(PersistenceManager pm, AuthorizedObjectID authorizedObjectID)
	{
		Query q = pm.newNamedQuery(AuthorizedObjectRef.class, "getAuthorizedObjectRefsForAuthorizedObjectID");
		return CollectionUtil.castCollection((Collection<?>) q.execute(authorizedObjectID.toString()));
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String tariffUserSetOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String tariffUserSetID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="255"
	 */
	private String authorizedObjectID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private TariffUserSet tariffUserSet;

	/**
	 * The number of both direct and indirect references to this <code>AuthorizedObjectRef</code>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int referenceCount;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean directlyReferenced;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="TariffRef"
	 *		dependent-value="true"
	 *		mapped-by="authorizedObjectRef"
	 *
	 * @jdo.key mapped-by="tariffPK"
	 */
	private Map<String, TariffRef> tariffRefs;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected AuthorizedObjectRef() { }

	public AuthorizedObjectRef(TariffUserSet tariffUserSet, AuthorizedObjectID authorizedObjectID)
	{
		this.tariffUserSet = tariffUserSet;
		this.tariffUserSetOrganisationID = tariffUserSet.getOrganisationID();
		this.tariffUserSetID = tariffUserSet.getTariffUserSetID();
		this.authorizedObjectID = authorizedObjectID.toString();
		tariffRefs = new HashMap<String, TariffRef>();
	}

	public String getTariffUserSetOrganisationID() {
		return tariffUserSetOrganisationID;
	}
	public String getTariffUserSetID() {
		return tariffUserSetID;
	}
	public String getAuthorizedObjectID() {
		return authorizedObjectID;
	}
	public AuthorizedObjectID getAuthorizedObjectIDAsOID() {
		return (AuthorizedObjectID) ObjectIDUtil.createObjectID(authorizedObjectID);
	}

	public TariffUserSet getTariffUserSet() {
		return tariffUserSet;
	}

	public int getReferenceCount() {
		return referenceCount;
	}

	public int incReferenceCount() {
		return ++referenceCount;
	}

	public int decReferenceCount() {
		if (--referenceCount < 0)
			throw new IllegalStateException("referenceCount < 0");

		return referenceCount;
	}

	public boolean isDirectlyReferenced() {
		return directlyReferenced;
	}
	public void setDirectlyReferenced(boolean directlyReferenced) {
		if (this.directlyReferenced == directlyReferenced)
			return;

		this.directlyReferenced = directlyReferenced;

		if (directlyReferenced)
			incReferenceCount();
		else
			decReferenceCount();
	}

	protected TariffRef createTariffRef(Tariff tariff)
	{
		TariffRef tariffRef = tariffRefs.get(tariff.getPrimaryKey());
		if (tariffRef == null) {
			tariffRef = new TariffRef(this, tariff);
			tariffRefs.put(tariff.getPrimaryKey(), tariffRef);
		}
		return tariffRef;
	}

	public TariffRef getTariffRef(Tariff tariff)
	{
		TariffRef tariffRef = tariffRefs.get(tariff.getPrimaryKey());
		return tariffRef;
	}

	private void addTariffIndirectly(Tariff tariff)
	{
		TariffRef tariffRef = createTariffRef(tariff);
		tariffRef.incReferenceCount(1);

		Set<AuthorizedObjectID> memberIDs = tariffUserSet.getTariffUserSetController(true).getUserSecurityGroupMemberIDs(this.getAuthorizedObjectIDAsOID());
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				AuthorizedObjectRef memberAuthorizedObjectRef = tariffUserSet.getAuthorizedObjectRef(memberID);
				if (memberAuthorizedObjectRef == null)
					throw new IllegalStateException("tariffUserSet.getAuthorizedObjectRef(memberID) returned null for memberID = " + memberID);

				memberAuthorizedObjectRef.addTariffIndirectly(tariff);
			}
		}
	}

	private void removeTariffIndirectly(Tariff tariff)
	{
		TariffRef tariffRef = tariffRefs.get(tariff.getPrimaryKey());
		if (tariffRef == null)
			return;

		tariffRef.decReferenceCount(1);

		Set<AuthorizedObjectID> memberIDs = tariffUserSet.getTariffUserSetController(true).getUserSecurityGroupMemberIDs(this.getAuthorizedObjectIDAsOID());
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				AuthorizedObjectRef memberAuthorizedObjectRef = tariffUserSet.getAuthorizedObjectRef(memberID);
				if (memberAuthorizedObjectRef == null)
					throw new IllegalStateException("tariffUserSet.getAuthorizedObjectRef(memberID) returned null for memberID = " + memberID);

				memberAuthorizedObjectRef.removeTariffIndirectly(tariff);
			}
		}

		if (tariffRef.getReferenceCount() == 0)
			tariffRefs.remove(tariff.getPrimaryKey());
	}

	public void addTariff(Tariff tariff)
	{
		TariffRef tariffRef = createTariffRef(tariff);
		if (tariffRef.isDirectlyReferenced())
			return;

		tariffRef.setDirectlyReferenced(true);

		Set<AuthorizedObjectID> memberIDs = tariffUserSet.getTariffUserSetController(true).getUserSecurityGroupMemberIDs(this.getAuthorizedObjectIDAsOID());
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				AuthorizedObjectRef memberAuthorizedObjectRef = tariffUserSet.getAuthorizedObjectRef(memberID);
				if (memberAuthorizedObjectRef == null)
					throw new IllegalStateException("tariffUserSet.getAuthorizedObjectRef(memberID) returned null for memberID = " + memberID);

				memberAuthorizedObjectRef.addTariffIndirectly(tariff);
			}
		}
	}

	public void removeTariff(Tariff tariff)
	{
		TariffRef tariffRef = tariffRefs.get(tariff.getPrimaryKey());
		if (tariffRef == null)
			return;

		if (!tariffRef.isDirectlyReferenced())
			return;

		tariffRef.setDirectlyReferenced(false);

		Set<AuthorizedObjectID> memberIDs = tariffUserSet.getTariffUserSetController(true).getUserSecurityGroupMemberIDs(this.getAuthorizedObjectIDAsOID());
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				AuthorizedObjectRef memberAuthorizedObjectRef = tariffUserSet.getAuthorizedObjectRef(memberID);
				if (memberAuthorizedObjectRef == null)
					throw new IllegalStateException("tariffUserSet.getAuthorizedObjectRef(memberID) returned null for memberID = " + memberID);

				memberAuthorizedObjectRef.removeTariffIndirectly(tariff);
			}
		}

		if (tariffRef.getReferenceCount() == 0)
			tariffRefs.remove(tariff.getPrimaryKey());
	}

	public Collection<TariffRef> getTariffRefs() {
		return Collections.unmodifiableCollection(tariffRefs.values());
	}

	protected void internalRemoveTariffRef(TariffRef tariffRef) {
		if (!this.equals(tariffRef.getAuthorizedObjectRef()))
			throw new IllegalArgumentException("this != tariffRef.getAuthorizedObjectRef() :: " + this + " != " + tariffRef.getAuthorizedObjectRef());

		TariffRef internalTariffRef = tariffRefs.remove(tariffRef.getTariff().getPrimaryKey());
		if (internalTariffRef == null)
			throw new IllegalStateException("TariffRef not found! " + tariffRef);

		if (!internalTariffRef.equals(tariffRef))
			throw new IllegalStateException("internalTariffRef != tariffRef :: " + internalTariffRef + " != " + tariffRef);

		if (internalTariffRef.getReferenceCount() != 0)
			throw new IllegalStateException("internalTariffRef.referenceCount != 0 :: internalTariffRef = " + internalTariffRef + " :: internalTariffRef.referenceCount = " + internalTariffRef.getReferenceCount());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tariffUserSetOrganisationID == null) ? 0 : tariffUserSetOrganisationID.hashCode());
		result = prime * result + ((tariffUserSetID == null) ? 0 : tariffUserSetID.hashCode());
		result = prime * result + ((authorizedObjectID == null) ? 0 : authorizedObjectID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final AuthorizedObjectRef other = (AuthorizedObjectRef) obj;

		return (
				Util.equals(this.authorizedObjectID, other.authorizedObjectID) &&
				Util.equals(this.tariffUserSetID, other.tariffUserSetID) &&
				Util.equals(this.tariffUserSetOrganisationID, other.tariffUserSetOrganisationID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + tariffUserSetOrganisationID + ',' + tariffUserSetID + ',' + authorizedObjectID + ']';
	}
}
