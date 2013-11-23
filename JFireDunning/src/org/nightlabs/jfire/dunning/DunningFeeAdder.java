package org.nightlabs.jfire.dunning;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.dunning.id.DunningFeeAdderID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.reflect.ReflectUtil;

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
 * @author Marius Heinzmann <!-- Marius[DOT]Heinzmann[AT]NightLabs[DOT]de -->
 */
@PersistenceCapable(
		objectIdClass=DunningFeeAdderID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_FeeAdder")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningFeeAdder
	implements Serializable, CloneableWithContext
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningFeeAdderID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningFeeAdder() { }

	public DunningFeeAdder(String organisationID, long dunningFeeAdderID)
	{
		Organisation.assertValidOrganisationID(organisationID);

		this.organisationID = organisationID;
		this.dunningFeeAdderID = dunningFeeAdderID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getDunningFeeAdderID()
	{
		return dunningFeeAdderID;
	}

	public abstract void addDunningFees(DunningLetter prevDunningLetter, DunningLetter newDunningLetter, DunningConfig config, Currency currency);
	
//	@Override
//	public DunningFeeAdder clone(CloneContext context)
//	{
//		if (context == null)
//			context = new DefaultCloneContext();
//		
//		return context.createClone(this);
//	}
	
	@Override
	public DunningFeeAdder clone(CloneContext context, boolean cloneReferences)
	{
//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
//		DunningFeeAdder clone = (DunningFeeAdder) super.clone();
		DunningFeeAdder clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
//	END OF WORKAROUND

		clone.dunningFeeAdderID = IDGenerator.nextID(DunningFeeAdder.class);
		return clone;
	}

	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
	}
	
}