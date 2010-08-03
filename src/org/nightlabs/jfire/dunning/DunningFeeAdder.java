package org.nightlabs.jfire.dunning;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.dunning.id.DunningFeeAdderID;

/**
 * The DunningFeeAdder encapsulates the logic deciding if and what fees 
 * to add to a newly created DunningLetter. 
 * 
 * As there may be arbitrarily many creations of DunningLetters in a fixed 
 * time frame, it is not advisable to always add the default fees for every 
 * generated letter. This class is where one can implement the desired behaviour.
 * 
 * We will provide a customer-friendly default implementation: 
 * The DunningFeeAdderCustomerFriendly.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
	objectIdClass=DunningFeeAdderID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_DunningFeeAdder")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class DunningFeeAdder
implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningFeeAdderID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningFeeAdder() { }

	public DunningFeeAdder(String organisationID, String dunningFeeAdderID)
	{
		this.organisationID = organisationID;
		this.dunningFeeAdderID = dunningFeeAdderID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getDunningFeeAdderID()
	{
		return dunningFeeAdderID;
	}

	public abstract void addDunningFee(DunningLetter dunningLetter);

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of DunningFeeAdder has no PersistenceManager assigned!");
		return pm;
	}
		}