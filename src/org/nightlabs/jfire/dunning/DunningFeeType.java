package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningFeeTypeID;
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
	@Column(length=100)
	private String dunningFeeTypeID;
	
	private DunningFee dunningFee;
	
	private DunningFeeTypeName name;
	
	private DunningFeeTypeDescription description;
	
	private Map<Currency, Price> currency2price;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningFeeType() { }
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getDunningFeeTypeID() {
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