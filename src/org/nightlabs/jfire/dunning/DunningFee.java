package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningFeeID;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningFeeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningFee")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningFee 
implements Serializable
{
private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningFeeID;
	
	private DunningLetterEntry dunningLetterEntry;
	
	private DunningFeeType dunningFeeType;
	
	private Price price;
	
	private long amountPaid;
	
	private transient long amountToPay;
	
	private Date paidDT;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningFee() { }
}
