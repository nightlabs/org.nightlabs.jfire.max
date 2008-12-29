package org.nightlabs.jfire.accounting.tariffuserset;

import java.io.Serializable;
import java.util.Map;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.util.Util;

/**
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.tariffuserset.id.TariffRefID"
 *		detachable="true"
 *		table="JFireTrade_TariffRef"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="tariffUserSetOrganisationID, tariffUserSetID, authorizedObjectID, tariffOrganisationID, tariffID"
 */
public class TariffRef
implements Serializable
{
	private static final long serialVersionUID = 1L;

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
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String tariffOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String tariffID;

	/**
	 * This field is the result of {@link Tariff#getPrimaryKey()} and is used by {@link AuthorizedObjectRef}
	 * as mapped-by-key for a {@link Map}.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@SuppressWarnings("unused")
	private String tariffPK;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private TariffUserSet tariffUserSet;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private AuthorizedObjectRef authorizedObjectRef;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Tariff tariff;

	/**
	 * The number of both direct and indirect references to this <code>TariffRef</code>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int referenceCount;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean directlyReferenced;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TariffRef() { }

	public TariffRef(AuthorizedObjectRef authorizedObjectRef, Tariff tariff) {
		this.tariffUserSet = authorizedObjectRef.getTariffUserSet();
		this.authorizedObjectRef = authorizedObjectRef;
		this.authorizedObjectID = authorizedObjectRef.getAuthorizedObjectID();
		this.tariff = tariff;
		this.tariffUserSetOrganisationID = tariffUserSet.getOrganisationID();
		this.tariffUserSetID = tariffUserSet.getTariffUserSetID();
		this.tariffOrganisationID = tariff.getOrganisationID();
		this.tariffID = tariff.getTariffID();
		this.tariffPK = tariff.getPrimaryKey();
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

	public String getTariffOrganisationID() {
		return tariffOrganisationID;
	}
	public String getTariffID() {
		return tariffID;
	}

	public TariffUserSet getTariffUserSet() {
		return tariffUserSet;
	}

	public AuthorizedObjectRef getAuthorizedObjectRef() {
		return authorizedObjectRef;
	}

	public Tariff getTariff() {
		return tariff;
	}

	public int getReferenceCount() {
		return referenceCount;
	}

	public int incReferenceCount(int count) {
		if (count < 0)
			return decReferenceCount(-count);

		referenceCount += count;
		return referenceCount;
	}

	public int decReferenceCount(int count) {
		if (count < 0)
			return incReferenceCount(-count);

		referenceCount -= count;

		if (referenceCount < 0)
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
			incReferenceCount(1);
		else
			decReferenceCount(1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tariffUserSetOrganisationID == null) ? 0 : tariffUserSetOrganisationID.hashCode());
		result = prime * result + ((tariffUserSetID == null) ? 0 : tariffUserSetID.hashCode());
		result = prime * result + ((authorizedObjectID == null) ? 0 : authorizedObjectID.hashCode());
		result = prime * result + ((tariffOrganisationID == null) ? 0 : tariffOrganisationID.hashCode());
		result = prime * result + ((tariffID == null) ? 0 : tariffID.hashCode());
		return result;

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final TariffRef other = (TariffRef) obj;

		return (
				Util.equals(this.tariffID, other.tariffID) &&
				Util.equals(this.tariffOrganisationID, other.tariffOrganisationID) &&
				Util.equals(this.authorizedObjectID, other.authorizedObjectID) &&
				Util.equals(this.tariffUserSetID, other.tariffUserSetID) &&
				Util.equals(this.tariffUserSetOrganisationID, other.tariffUserSetOrganisationID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + tariffUserSetOrganisationID + ',' + tariffUserSetID + ',' + authorizedObjectID + ',' + tariffOrganisationID + ',' + tariffID + ']';
	}
}
