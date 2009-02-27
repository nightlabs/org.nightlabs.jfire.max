package org.nightlabs.jfire.trade.endcustomer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.util.Util;

/**
 * An instance of this class specifies how an end-customer will be transferred from
 * the reseller to the supplier. It is specified by the supplier and assigned to a
 * {@link ProductType}.
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.endcustomer.id.EndCustomerTransferPolicyID"
 *		detachable="true"
 *		table="JFireTrade_EndCustomerTransferPolicy"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, endCustomerTransferPolicyID"
 *
 * @jdo.fetch-group name="EndCustomerTransferPolicy.name" fields="name"
 * @jdo.fetch-group name="EndCustomerTransferPolicy.description" fields="description"
 * @jdo.fetch-group name="EndCustomerTransferPolicy.structFields" fields="structFields"
 */
public class EndCustomerTransferPolicy
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "EndCustomerTransferPolicy.name";
	public static final String FETCH_GROUP_DESCRIPTION = "EndCustomerTransferPolicy.description";
	public static final String FETCH_GROUP_STRUCT_FIELDS = "EndCustomerTransferPolicy.structFields";

	/**
	 * @jdo.field primary-key="true"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long endCustomerTransferPolicyID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="endCustomerTransferPolicy"
	 */
	private EndCustomerTransferPolicyName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="endCustomerTransferPolicy"
	 */
	private EndCustomerTransferPolicyDescription description;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.StructField"
	 *		table="JFireTrade_EndCustomerTransferPolicy_structFields"
	 * @jdo.join
	 */
	private Set<StructField<? extends DataField>> structFields;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EndCustomerTransferPolicy() { }

	public EndCustomerTransferPolicy(String organisationID, long endCustomerTransferPolicyID) {
		this.organisationID = organisationID;
		this.endCustomerTransferPolicyID = endCustomerTransferPolicyID;

		structFields = new HashSet<StructField<? extends DataField>>();
		name = new EndCustomerTransferPolicyName(this);
		description = new EndCustomerTransferPolicyDescription(this);
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public long getEndCustomerTransferPolicyID() {
		return endCustomerTransferPolicyID;
	}

	public EndCustomerTransferPolicyName getName() {
		return name;
	}

	public EndCustomerTransferPolicyDescription getDescription() {
		return description;
	}

	public Set<StructField<? extends DataField>> getStructFields() {
		return Collections.unmodifiableSet(structFields);
	}

	public boolean addStructField(StructField<? extends DataField> structField) {
		return structFields.add(structField);
	}

	public boolean removeStructField(StructField<? extends DataField> structField) {
		return structFields.remove(structField);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (endCustomerTransferPolicyID ^ (endCustomerTransferPolicyID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		EndCustomerTransferPolicy other = (EndCustomerTransferPolicy) obj;
		return (
				Util.equals(this.endCustomerTransferPolicyID, other.endCustomerTransferPolicyID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(endCustomerTransferPolicyID) + ']';
	}
}
