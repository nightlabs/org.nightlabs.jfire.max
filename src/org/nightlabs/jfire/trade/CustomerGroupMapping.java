package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.CustomerGroupMappingID;
import org.nightlabs.util.Utils;

/**
 * <p>
 * The <code>CustomerGroupMapping</code>s define how a foreign (partner) {@link CustomerGroup} is mapped to a local one. This
 * is a bidirectionally unique mapping. Hence, for every local {@link CustomerGroup} and every {@link #partnerCustomerGroupOrganisationID}
 * there is exactly one partner-{@link CustomerGroup}. And for every partner-{@link CustomerGroup}, there's exactly one local <code>CustomerGroup</code>.
 * </p>
 * <p>
 * The <code>CustomerGroupMapping</code>s are used with the {@link GridPriceConfig}, because local {@link ProductType}s are always sold with local
 * <code>CustomerGroup</code>s, but they can package imported partner-{@link ProductType}s which use the partner's <code>CustomerGroup</code>s. 
 * </p>
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.CustomerGroupMappingID"
 *		detachable="true"
 *		table="JFireTrade_CustomerGroupMapping"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="partnerCustomerGroupOrganisationID, partnerCustomerGroupCustomerGroupID, localCustomerGroupOrganisationID, localCustomerGroupCustomerGroupID"
 *
 * @jdo.fetch-group name="CustomerGroupMapping.partnerCustomerGroup" fields="partnerCustomerGroup"
 * @jdo.fetch-group name="CustomerGroupMapping.localCustomerGroup" fields="localCustomerGroup"
 *
 * @jdo.query
 *		name="getCustomerGroupMappingForPartnerCustomerGroup"
 *		query="SELECT UNIQUE
 *				WHERE
 *					this.partnerCustomerGroupOrganisationID == :partnerCustomerGroupOrganisationID &&
 *					this.partnerCustomerGroupCustomerGroupID == :partnerCustomerGroupCustomerGroupID"
 *
 * @jdo.query
 *		name="getCustomerGroupMappingForLocalCustomerGroupAndPartner"
 *		query="SELECT UNIQUE
 *				WHERE
 *					this.partnerCustomerGroupOrganisationID == :partnerCustomerGroupOrganisationID &&
 *					this.localCustomerGroupOrganisationID == :localCustomerGroupOrganisationID &&
 *					this.localCustomerGroupCustomerGroupID == :localCustomerGroupCustomerGroupID"
 */
public class CustomerGroupMapping
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PARTNER_CUSTOMER_GROUP = "CustomerGroupMapping.partnerCustomerGroup";
	public static final String FETCH_GROUP_LOCAL_CUSTOMER_GROUP = "CustomerGroupMapping.localCustomerGroup";

	@SuppressWarnings("unchecked")
	public static Collection<CustomerGroupMapping> getCustomerGroupMappings(PersistenceManager pm)
	{
		Query q = pm.newQuery(CustomerGroupMapping.class);
		return (Collection<CustomerGroupMapping>) q.execute();
	}

	/**
	 * If the mapping for the given <code>partnerCustomerGroupID</code> and <code>localCustomerGroupID</code> already exists, this method returns it without
	 * any action. Otherwise it will be created, if it would not infringe on the rule of bidirectional uniqueness.
	 *
	 * @param pm The door to the datastore.
	 * @param partnerCustomerGroupID Reference to the partner's {@link CustomerGroup}.
	 * @param localCustomerGroupID Reference to the local {@link CustomerGroup}.
	 * @return The {@link CustomerGroupMapping} for the given customerGroups.
	 */
	public static CustomerGroupMapping createCustomerGroupMapping(PersistenceManager pm, CustomerGroupID partnerCustomerGroupID, CustomerGroupID localCustomerGroupID)
	{
		pm.getExtent(CustomerGroupMapping.class);
		CustomerGroupMappingID customerGroupMappingID = CustomerGroupMappingID.create(
				partnerCustomerGroupID.organisationID, partnerCustomerGroupID.customerGroupID,
				localCustomerGroupID.organisationID, localCustomerGroupID.customerGroupID);
		CustomerGroupMapping customerGroupMapping;
		try {
			customerGroupMapping = (CustomerGroupMapping) pm.getObjectById(customerGroupMappingID);
			customerGroupMapping.getLocalCustomerGroup(); // ensure JPOX bug doesn't affect us
			// it exists => return it
			return customerGroupMapping;
		} catch (JDOObjectNotFoundException x) {
			// not yet existing => we'll create it
		}

		// ensure that the partner-CustomerGroup is not yet mapped
		CustomerGroupMapping tm = getCustomerGroupMappingForPartnerCustomerGroup(pm, partnerCustomerGroupID);
		if (tm != null)
			throw new IllegalStateException("The partner-CustomerGroup is already mapped to another local CustomerGroup! " + JDOHelper.getObjectId(tm));

		// ensure that the local CustomerGroup is not yet mapped for this partner-organisation
		tm = getCustomerGroupMappingForLocalCustomerGroupAndPartner(pm, localCustomerGroupID, partnerCustomerGroupID.organisationID);
		if (tm != null)
			throw new IllegalStateException("For the partner-organisation " + partnerCustomerGroupID.organisationID + " the local CustomerGroup is already mapped to another partner-CustomerGroup! " + JDOHelper.getObjectId(tm));

		// if we come here, there are no collisions => create the new CustomerGroupMapping
		pm.getExtent(CustomerGroup.class);
		CustomerGroup partnerCustomerGroup = (CustomerGroup) pm.getObjectById(partnerCustomerGroupID);
		CustomerGroup localCustomerGroup = (CustomerGroup) pm.getObjectById(localCustomerGroupID);

		customerGroupMapping = new CustomerGroupMapping(partnerCustomerGroup, localCustomerGroup);
		return (CustomerGroupMapping) pm.makePersistent(customerGroupMapping);
	}

	/**
	 * @param pm Accessor to the datastore.
	 * @param partnerCustomerGroupID The ID of the partner-{@link CustomerGroup} for which to search a {@link CustomerGroupMapping}.
	 * @return <code>null</code>, if there is no {@link CustomerGroupMapping} for the given <code>partnerCustomerGroupID</code> or the appropriate instance.
	 */
	public static CustomerGroupMapping getCustomerGroupMappingForPartnerCustomerGroup(PersistenceManager pm, CustomerGroupID partnerCustomerGroupID)
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("partnerCustomerGroupOrganisationID", partnerCustomerGroupID.organisationID);
		params.put("partnerCustomerGroupCustomerGroupID", partnerCustomerGroupID.customerGroupID);
		Query q = pm.newNamedQuery(CustomerGroupMapping.class, "getCustomerGroupMappingForPartnerCustomerGroup");
		return (CustomerGroupMapping) q.executeWithMap(params);
	}

	public static CustomerGroupMapping getCustomerGroupMappingForLocalCustomerGroupAndPartner(PersistenceManager pm, CustomerGroupID localCustomerGroupID, String partnerCustomerGroupOrganisationID)
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("partnerCustomerGroupOrganisationID", partnerCustomerGroupOrganisationID);
		params.put("localCustomerGroupOrganisationID", localCustomerGroupID.organisationID);
		params.put("localCustomerGroupCustomerGroupID", localCustomerGroupID.customerGroupID);
		Query q = pm.newNamedQuery(CustomerGroupMapping.class, "getCustomerGroupMappingForLocalCustomerGroupAndPartner");
		return (CustomerGroupMapping) q.executeWithMap(params);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String partnerCustomerGroupOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String partnerCustomerGroupCustomerGroupID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String localCustomerGroupOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String localCustomerGroupCustomerGroupID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private CustomerGroup partnerCustomerGroup;
	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private CustomerGroup localCustomerGroup;

	/**
	 * @deprecated Only for JDO!
	 */
	protected CustomerGroupMapping() { }

	public CustomerGroupMapping(CustomerGroup partnerCustomerGroup, CustomerGroup localCustomerGroup)
	{
		this.partnerCustomerGroup = partnerCustomerGroup;
		this.localCustomerGroup = localCustomerGroup;

		this.partnerCustomerGroupOrganisationID = partnerCustomerGroup.getOrganisationID();
		this.partnerCustomerGroupCustomerGroupID = partnerCustomerGroup.getCustomerGroupID();

		this.localCustomerGroupOrganisationID = localCustomerGroup.getOrganisationID();
		this.localCustomerGroupCustomerGroupID = localCustomerGroup.getCustomerGroupID();
	}

	public String getPartnerCustomerGroupOrganisationID()
	{
		return partnerCustomerGroupOrganisationID;
	}
	public String getPartnerCustomerGroupCustomerGroupID()
	{
		return partnerCustomerGroupCustomerGroupID;
	}
	public String getLocalCustomerGroupOrganisationID()
	{
		return localCustomerGroupOrganisationID;
	}
	public String getLocalCustomerGroupCustomerGroupID()
	{
		return localCustomerGroupCustomerGroupID;
	}
	public CustomerGroup getPartnerCustomerGroup()
	{
		return partnerCustomerGroup;
	}
	public CustomerGroup getLocalCustomerGroup()
	{
		return localCustomerGroup;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient CustomerGroupID partnerCustomerGroupID;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient CustomerGroupID localCustomerGroupID;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient String partnerCustomerGroupPK;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient String localCustomerGroupPK;

	public CustomerGroupID getPartnerCustomerGroupID()
	{
		if (partnerCustomerGroupID == null)
			partnerCustomerGroupID = CustomerGroupID.create(partnerCustomerGroupOrganisationID, partnerCustomerGroupCustomerGroupID);

		return partnerCustomerGroupID;
	}
	public CustomerGroupID getLocalCustomerGroupID()
	{
		if (localCustomerGroupID == null)
			localCustomerGroupID = CustomerGroupID.create(localCustomerGroupOrganisationID, localCustomerGroupCustomerGroupID);

		return localCustomerGroupID;
	}
	public String getPartnerCustomerGroupPK()
	{
		if (partnerCustomerGroupPK == null)
			partnerCustomerGroupPK = CustomerGroup.getPrimaryKey(partnerCustomerGroupOrganisationID, partnerCustomerGroupCustomerGroupID);

		return partnerCustomerGroupPK;
	}
	public String getLocalCustomerGroupPK()
	{
		if (localCustomerGroupPK == null)
			localCustomerGroupPK = CustomerGroup.getPrimaryKey(localCustomerGroupOrganisationID, localCustomerGroupCustomerGroupID);

		return localCustomerGroupPK;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof CustomerGroupMapping)) return false;
		CustomerGroupMapping o = (CustomerGroupMapping) obj;
		return
				Utils.equals(o.partnerCustomerGroupOrganisationID, this.partnerCustomerGroupOrganisationID) &&
				Utils.equals(o.partnerCustomerGroupCustomerGroupID, this.partnerCustomerGroupCustomerGroupID) &&
				Utils.equals(o.localCustomerGroupOrganisationID, this.localCustomerGroupOrganisationID) &&
				Utils.equals(o.localCustomerGroupCustomerGroupID, this.localCustomerGroupCustomerGroupID);
	}
	@Override
	public int hashCode()
	{
		return
				Utils.hashCode(partnerCustomerGroupOrganisationID) +
				Utils.hashCode(partnerCustomerGroupCustomerGroupID) +
				Utils.hashCode(localCustomerGroupOrganisationID) +
				Utils.hashCode(localCustomerGroupCustomerGroupID);
	}
}
