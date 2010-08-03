package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
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
public class DunningFeeType 
implements Serializable
{
private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningFeeTypeID;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	private DunningFee dunningFee;
	
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
	public DunningFeeType(String organisationID, long dunningFeeTypeID, DunningFee dunningFee) {
		Organisation.assertValidOrganisationID(organisationID);
		
		this.organisationID = organisationID;
		this.dunningFeeTypeID = dunningFeeTypeID;
		this.dunningFee = dunningFee;
		
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
	
	public DunningFee getDunningFee() {
		return dunningFee;
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
}