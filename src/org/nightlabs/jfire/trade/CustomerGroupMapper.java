package org.nightlabs.jfire.trade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.trade.id.CustomerGroupID;

public class CustomerGroupMapper
{
	private Collection<CustomerGroupMapping> customerGroupMappings;

//	private Map<CustomerGroupID, CustomerGroupMapping> partnerCustomerGroupID2customerGroupMapping = null;
	/**
	 * localCustomerGroupID => partnerOrganisationID => CustomerGroupMapping
	 */
	private Map<CustomerGroupID, Map<String, CustomerGroupMapping>> localCustomerGroupID2partnerOrganisationID2customerGroupMapping = null;

	/**
	 * This is a convenience constructor calling {@link #CustomerGroupMapper(Collection)} with the result of
	 * {@link CustomerGroupMapping#getCustomerGroupMappings(PersistenceManager)}.
	 *
	 * @param pm This <code>PersistenceManager</code> is used only within the constructor in order to load the {@link CustomerGroupMapping}s. It is not kept within
	 *		the instance of <code>CustomerGroupMapper</code>.
	 */
	public CustomerGroupMapper(PersistenceManager pm)
	{
		this(CustomerGroupMapping.getCustomerGroupMappings(pm));
	}

	public CustomerGroupMapper(Collection<CustomerGroupMapping> customerGroupMappings)
	{
		this.customerGroupMappings = customerGroupMappings;
	}

//	protected Map<CustomerGroupID, CustomerGroupMapping> getPartnerCustomerGroupID2customerGroupMapping()
//	{
//		if (partnerCustomerGroupID2customerGroupMapping == null) {
//			Map<CustomerGroupID, CustomerGroupMapping> partnerCustomerGroupID2customerGroupMapping = new HashMap<CustomerGroupID, CustomerGroupMapping>();
//
//			for (CustomerGroupMapping customerGroupMapping : customerGroupMappings)
//				partnerCustomerGroupID2customerGroupMapping.put(customerGroupMapping.getPartnerCustomerGroupID(), customerGroupMapping);
//
//			this.partnerCustomerGroupID2customerGroupMapping = partnerCustomerGroupID2customerGroupMapping;
//		}
//		return partnerCustomerGroupID2customerGroupMapping;
//	}

	protected Map<CustomerGroupID, Map<String, CustomerGroupMapping>> getLocalCustomerGroupID2partnerOrganisationID2customerGroupMapping()
	{
		if (localCustomerGroupID2partnerOrganisationID2customerGroupMapping == null) {
			Map<CustomerGroupID, Map<String, CustomerGroupMapping>> localCustomerGroupID2partnerOrganisationID2customerGroupMapping = new HashMap<CustomerGroupID, Map<String,CustomerGroupMapping>>();
			
			for (CustomerGroupMapping customerGroupMapping : customerGroupMappings) {
				Map<String, CustomerGroupMapping> partnerOrganisationID2customerGroupMapping = localCustomerGroupID2partnerOrganisationID2customerGroupMapping.get(customerGroupMapping.getLocalCustomerGroupID());
				if (partnerOrganisationID2customerGroupMapping == null) {
					partnerOrganisationID2customerGroupMapping = new HashMap<String, CustomerGroupMapping>();
					localCustomerGroupID2partnerOrganisationID2customerGroupMapping.put(customerGroupMapping.getLocalCustomerGroupID(), partnerOrganisationID2customerGroupMapping);
				}
				partnerOrganisationID2customerGroupMapping.put(customerGroupMapping.getPartnerCustomerGroupOrganisationID(), customerGroupMapping);
			}

			this.localCustomerGroupID2partnerOrganisationID2customerGroupMapping = localCustomerGroupID2partnerOrganisationID2customerGroupMapping;
		}

		return localCustomerGroupID2partnerOrganisationID2customerGroupMapping;
	}

	/**
	 * @param localCustomerGroupID references a customer-group of the local organisation which is reselling a product-type.
	 * @param partnerOrganisationID references the partner-organisation whose product-type is resold.
	 * @param throwExceptionIfNotFound controls whether to return <code>null</code> or to throw an exception of no mapping can be found.
	 * @return the id of the customer-group in the partner-organisation.
	 */
	public CustomerGroupID getPartnerCustomerGroupID(CustomerGroupID localCustomerGroupID, String partnerOrganisationID, boolean throwExceptionIfNotFound)
	{
		if (partnerOrganisationID.equals(localCustomerGroupID.organisationID))
			return localCustomerGroupID;

		CustomerGroupID res = null;

		Map<String, CustomerGroupMapping> partnerOrganisationID2customerGroupMapping = getLocalCustomerGroupID2partnerOrganisationID2customerGroupMapping().get(localCustomerGroupID);
		if (partnerOrganisationID2customerGroupMapping != null) {
			CustomerGroupMapping tm = partnerOrganisationID2customerGroupMapping.get(partnerOrganisationID);
			if (tm != null)
				res = tm.getPartnerCustomerGroupID();
		}

//		if (res == null) {
//			CustomerGroupMapping tm = getPartnerCustomerGroupID2customerGroupMapping().get(sourceCustomerGroupID);
//			if (tm != null) {
//				res = tm.getLocalCustomerGroupID();
//
//				if (!productTypeOrganisationID.equals(res.organisationID)) { // should never happen that we map from one partner-org to another partner-org, but...
//					return getCustomerGroupIDForProductType(res, productTypeOrganisationID, throwExceptionIfNotFound);
//				}
//			}
//		}

		if (throwExceptionIfNotFound && res == null)
			throw new IllegalArgumentException("No mapping found to map from sourceCustomerGroupID.organisationID=\"" + localCustomerGroupID.organisationID + "\" to productTypeOrganisationID=\"" + partnerOrganisationID + "\"!");

		return res;
	}

}
