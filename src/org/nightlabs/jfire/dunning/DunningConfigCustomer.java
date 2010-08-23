package org.nightlabs.jfire.dunning;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.dunning.id.DunningConfigCustomerID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * Binding-entity for establishing a relationship between one 
 * certain customer and a DunningConfig.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningConfigCustomerID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningConfigCustomer"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Queries({
	@javax.jdo.annotations.Query(
		name=DunningConfigCustomer.QUERY_GET_DUNNING_CONFIG_BY_CUSTOMER,
		value="SELECT WHERE this.customer == :customer")
})
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
public class DunningConfigCustomer 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningConfigCustomer.class);
	
	public static final DunningConfigCustomerID DUNNING_CONFIG_CUSTOMER_DEFAULT_ID = DunningConfigCustomerID.create(Organisation.DEV_ORGANISATION_ID, "Default");
	
	public static final String FETCH_GROUP_DUNNING_CONFIG = "DunningConfigCustomer.dunningConfig";
	public static final String FETCH_GROUP_CUSTOMER = "DunningConfigCustomer.customer";
	
	public static final String QUERY_GET_DUNNING_CONFIG_BY_CUSTOMER = "getReportRegistryItemByType";
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	@PrimaryKey
	@Column(length=100)
	private String dunningConfigCustomerID;
	
	/**
	 * The configuration that shall be assigned.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningConfig dunningConfig;
	
	/**
	 * The customer to which a DunningConfig should be assigned 
	 * or null to specify the default configuration.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity customer;
	
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	public DunningConfigCustomer() {
	}
	
	public DunningConfigCustomer(String organisationID, String dunningConfigCustomerID, DunningConfig dunningConfig, LegalEntity customer) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningConfigCustomerID, "dunningConfigCustomerID"); //$NON-NLS-1$
		
		this.organisationID = organisationID;
		this.dunningConfigCustomerID = dunningConfigCustomerID;
		
		this.dunningConfig = dunningConfig;
		this.customer = customer;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getDunningConfigCustomerID() {
		return dunningConfigCustomerID;
	}
	
	public DunningConfig getDunningConfig() {
		return dunningConfig;
	}
	
	public LegalEntity getCustomer() {
		return customer;
	}
}