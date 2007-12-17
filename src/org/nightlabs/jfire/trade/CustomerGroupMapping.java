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
import org.nightlabs.util.Util;

/**
 * <p>
 * The <code>CustomerGroupMapping</code>s define how a local {@link CustomerGroup} is mapped to a foreign (partner) one. This
 * is a unidirectionally unique mapping from local to partner for any given partner-organisationID. Hence, for every local
 * {@link CustomerGroup} and every {@link #partnerCustomerGroupOrganisationID}
 * there is exactly one partner-{@link CustomerGroup}. But for every partner-{@link CustomerGroup}, there might be multiple local <code>CustomerGroup</code>s.
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
 *		field-order="localCustomerGroupOrganisationID, localCustomerGroupCustomerGroupID, partnerCustomerGroupOrganisationID, partnerCustomerGroupCustomerGroupID"
 *
 * @jdo.fetch-group name="CustomerGroupMapping.partnerCustomerGroup" fields="partnerCustomerGroup"
 * @jdo.fetch-group name="CustomerGroupMapping.localCustomerGroup" fields="localCustomerGroup"
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
	 * @param localCustomerGroupID Reference to the local {@link CustomerGroup}.
	 * @param partnerCustomerGroupID Reference to the partner's {@link CustomerGroup}.
	 * @return The {@link CustomerGroupMapping} for the given customerGroups.
	 */
	public static CustomerGroupMapping create(PersistenceManager pm, CustomerGroupID localCustomerGroupID, CustomerGroupID partnerCustomerGroupID)
	{
		pm.getExtent(CustomerGroupMapping.class);
		CustomerGroupMappingID customerGroupMappingID = CustomerGroupMappingID.create(
				localCustomerGroupID.organisationID, localCustomerGroupID.customerGroupID,
				partnerCustomerGroupID.organisationID, partnerCustomerGroupID.customerGroupID
		);
		CustomerGroupMapping customerGroupMapping;
		try {
			customerGroupMapping = (CustomerGroupMapping) pm.getObjectById(customerGroupMappingID);
			customerGroupMapping.getLocalCustomerGroup(); // ensure JPOX bug doesn't affect us
			// it exists => return it
			return customerGroupMapping;
		} catch (JDOObjectNotFoundException x) {
			// not yet existing => we'll create it
		}

		// ensure that the local CustomerGroup is not yet mapped for this partner-organisation
		CustomerGroupMapping cgm = getCustomerGroupMappingForLocalCustomerGroupAndPartner(pm, localCustomerGroupID, partnerCustomerGroupID.organisationID);
		if (cgm != null)
			throw new IllegalStateException("For the partner-organisation " + partnerCustomerGroupID.organisationID + " the local CustomerGroup is already mapped to another partner-CustomerGroup! " + JDOHelper.getObjectId(cgm));

		// if we come here, there are no collisions => create the new CustomerGroupMapping
		pm.getExtent(CustomerGroup.class);
		CustomerGroup partnerCustomerGroup = (CustomerGroup) pm.getObjectById(partnerCustomerGroupID);
		CustomerGroup localCustomerGroup = (CustomerGroup) pm.getObjectById(localCustomerGroupID);

		customerGroupMapping = new CustomerGroupMapping(localCustomerGroup, partnerCustomerGroup);
		return pm.makePersistent(customerGroupMapping);
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
	@Deprecated
	protected CustomerGroupMapping() { }

	public CustomerGroupMapping(CustomerGroup localCustomerGroup, CustomerGroup partnerCustomerGroup)
	{
		this.localCustomerGroup = localCustomerGroup;
		this.partnerCustomerGroup = partnerCustomerGroup;
		
		this.localCustomerGroupOrganisationID = localCustomerGroup.getOrganisationID();
		this.localCustomerGroupCustomerGroupID = localCustomerGroup.getCustomerGroupID();

		this.partnerCustomerGroupOrganisationID = partnerCustomerGroup.getOrganisationID();
		this.partnerCustomerGroupCustomerGroupID = partnerCustomerGroup.getCustomerGroupID();
	}

	public String getLocalCustomerGroupOrganisationID()
	{
		return localCustomerGroupOrganisationID;
	}
	public String getLocalCustomerGroupCustomerGroupID()
	{
		return localCustomerGroupCustomerGroupID;
	}

	public String getPartnerCustomerGroupOrganisationID()
	{
		return partnerCustomerGroupOrganisationID;
	}
	public String getPartnerCustomerGroupCustomerGroupID()
	{
		return partnerCustomerGroupCustomerGroupID;
	}

	public CustomerGroup getLocalCustomerGroup()
	{
		return localCustomerGroup;
	}
	public CustomerGroup getPartnerCustomerGroup()
	{
		return partnerCustomerGroup;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient CustomerGroupID localCustomerGroupID;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient CustomerGroupID partnerCustomerGroupID;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient String localCustomerGroupPK;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient String partnerCustomerGroupPK;

	public CustomerGroupID getLocalCustomerGroupID()
	{
		if (localCustomerGroupID == null)
			localCustomerGroupID = CustomerGroupID.create(localCustomerGroupOrganisationID, localCustomerGroupCustomerGroupID);
		
		return localCustomerGroupID;
	}
	public CustomerGroupID getPartnerCustomerGroupID()
	{
		if (partnerCustomerGroupID == null)
			partnerCustomerGroupID = CustomerGroupID.create(partnerCustomerGroupOrganisationID, partnerCustomerGroupCustomerGroupID);

		return partnerCustomerGroupID;
	}
	public String getLocalCustomerGroupPK()
	{
		if (localCustomerGroupPK == null)
			localCustomerGroupPK = CustomerGroup.getPrimaryKey(localCustomerGroupOrganisationID, localCustomerGroupCustomerGroupID);
		
		return localCustomerGroupPK;
	}
	public String getPartnerCustomerGroupPK()
	{
		if (partnerCustomerGroupPK == null)
			partnerCustomerGroupPK = CustomerGroup.getPrimaryKey(partnerCustomerGroupOrganisationID, partnerCustomerGroupCustomerGroupID);

		return partnerCustomerGroupPK;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof CustomerGroupMapping)) return false;
		CustomerGroupMapping o = (CustomerGroupMapping) obj;
		return
				Util.equals(o.localCustomerGroupOrganisationID, this.localCustomerGroupOrganisationID) &&
				Util.equals(o.localCustomerGroupCustomerGroupID, this.localCustomerGroupCustomerGroupID) &&
				Util.equals(o.partnerCustomerGroupOrganisationID, this.partnerCustomerGroupOrganisationID) &&
				Util.equals(o.partnerCustomerGroupCustomerGroupID, this.partnerCustomerGroupCustomerGroupID);
	}
	@Override
	public int hashCode()
	{
		return
				Util.hashCode(localCustomerGroupOrganisationID) +
				Util.hashCode(localCustomerGroupCustomerGroupID) +
				Util.hashCode(partnerCustomerGroupOrganisationID) +
				Util.hashCode(partnerCustomerGroupCustomerGroupID);
	}
}
