package org.nightlabs.jfire.trade.endcustomer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.store.ProductType;

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
 * @jdo.fetch-group name="EndCustomerTransferPolicy.structFields" fields="structFields"
 */
public class EndCustomerTransferPolicy
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "EndCustomerTransferPolicy.name";
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

	public Set<StructField<? extends DataField>> getStructFields() {
		return Collections.unmodifiableSet(structFields);
	}
}
