package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Unique;

import org.nightlabs.jfire.dunning.id.DunningConfigCustomerID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * Binding-entity for establishing a relationship between one 
 * certain customer and a DunningConfig.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningConfigCustomer.FETCH_GROUP_DUNNING_CONFIG,
		members=@Persistent(name="dunningConfig")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningConfigCustomer.FETCH_GROUP_CUSTOMER,
		members=@Persistent(name="customer")
	)
})
@Queries({
	@javax.jdo.annotations.Query(
			name=DunningConfigCustomer.QUERY_GET_DUNNING_CONFIG_BY_CUSTOMER,
			value="SELECT this.dunningConfig WHERE JDOHelper.getObjectId(this.customer) == :customerID"
	)
})
@PersistenceCapable(
		objectIdClass=DunningConfigCustomerID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_ConfigCustomer"
)
@Unique(name="DunningConfig_Customer", members={"dunningConfig", "customer"})
public class DunningConfigCustomer 
	implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String DUNNING_CONFIG_CUSTOMER_DEFAULT_ID = "Default";
	
	public static final String FETCH_GROUP_DUNNING_CONFIG = "DunningConfigCustomer.dunningConfig";
	public static final String FETCH_GROUP_CUSTOMER = "DunningConfigCustomer.customer";
	
	public static final String QUERY_GET_DUNNING_CONFIG_BY_CUSTOMER = "getDunningConfigByCustomer";
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	@PrimaryKey
	private long dunningConfigCustomerID;
	
	/**
	 * The configuration that shall be assigned.
	 */
	private DunningConfig dunningConfig;
	
	/**
	 * The customer to which a DunningConfig should be assigned 
	 * or null to specify the default configuration.
	 */
	private LegalEntity customer;
	
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningConfigCustomer() {}
	
	public DunningConfigCustomer(DunningConfig dunningConfig, LegalEntity customer)
	{
		assert dunningConfig != null : "Given dunningConfig must NOT be null!";
		assert customer != null : "Given customer must NOT be null!";
		this.organisationID = dunningConfig.getOrganisationID();
		this.dunningConfigCustomerID = IDGenerator.nextID(DunningConfigCustomer.class);
		
		this.dunningConfig = dunningConfig;
		this.customer = customer;
	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}
	
	public long getDunningConfigCustomerID()
	{
		return dunningConfigCustomerID;
	}
	
	public DunningConfig getDunningConfig()
	{
		return dunningConfig;
	}
	
	public LegalEntity getCustomer()
	{
		return customer;
	}
	
	public static DunningConfig getDunningConfigByCustomer(PersistenceManager pm, AnchorID customerID)
	{
		Query query = pm.newNamedQuery(DunningConfigCustomer.class, QUERY_GET_DUNNING_CONFIG_BY_CUSTOMER);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customerID", customerID);
		@SuppressWarnings("unchecked")
		List<DunningConfig> result = (List<DunningConfig>) query.executeWithMap(params);
		if (result == null || result.isEmpty())
			return null;
		
		return result.iterator().next();
	}
}