package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningFeeTypeID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.l10n.Currency;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningFeeTypeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningFeeType")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningConfig.FETCH_GROUP_NAME,
		members=@Persistent(name="name")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningConfig.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")
	)
})
public class DunningFeeType 
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "DunningFeeType.name";
	public static final String FETCH_GROUP_DESCRIPTION = "DunningFeeType.description";
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningFeeTypeID;
	
	@Persistent(
			dependent="true",
			mappedBy="dunningFeeType",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningFeeTypeName name;
	
	@Persistent(
			dependent="true",
			mappedBy="dunningFeeType",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningFeeTypeDescription description;
	
	/**
	 * The price to be charged additionally to the invoice amount, 
	 * the interest and all previous fees.
	 */
	@Join
	@Persistent(table="JFireDunning_DunningFeeType_currency2price")
	private Map<Currency, Price> currency2price;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningFeeType() { }
	
	/**
	 * Create an instance of <code>DunningFeeType</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param dunningFeeTypeID second part of the primary key. A local identifier within the namespace of the organisation.
	 */
	public DunningFeeType(String organisationID, long dunningFeeTypeID) {
		Organisation.assertValidOrganisationID(organisationID);
		
		this.organisationID = organisationID;
		this.dunningFeeTypeID = dunningFeeTypeID;
		
		this.name = new DunningFeeTypeName(this);
		this.description = new DunningFeeTypeDescription(organisationID, dunningFeeTypeID, this);
		
		this.currency2price = new HashMap<Currency, Price>();
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public long getDunningFeeTypeID() {
		return dunningFeeTypeID;
	}
	
	public DunningFeeTypeName getName() {
		return name;
	}
	
	public DunningFeeTypeDescription getDescription() {
		return description;
	}
	
	public Map<Currency, Price> getCurrency2price() {
		return currency2price;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (dunningFeeTypeID ^ (dunningFeeTypeID >>> 32));
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DunningFeeType other = (DunningFeeType) obj;
		if (dunningFeeTypeID != other.dunningFeeTypeID)
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DunningFeeType [dunningFeeTypeID=" + dunningFeeTypeID
				+ ", organisationID=" + organisationID + "]";
	}
}