package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * According to http://zinsmethoden.de/ (unfortunately only in German),
 * there are many possibilities to calculate an interest. The questions
 * that need to be decided by this calculator are: <br>

 * <br>1.How many days does the year have? This is important to divide the percentage accordingly (i.e. is a delay of payment of 1 day calculated as dueAmount * percentage / 360 or dueAmount * percentage / 365 or is the real calendar used)?
 * <br>2.What's the first day in the interest calculation? Is it the due date or the following day at midnight?
 * <br>3.What's the last day in the interest calculation? There are many options: The new due date, the finalization date of the DunningLetter or the creation date of the DunningLetter. And is this day included or excluded in the period?
 *
 * <br><br>Due to all these questions, the DunningInterestCalculator is abstract. But we provide a pretty customer-friendly default implementation (see below).

 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningInterestCalculatorID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_InterestCalculator"
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningInterestCalculator
	implements Serializable, CloneableWithContext
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningInterestCalculatorID;

	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningInterestCalculator() { }

	/**
	 * For every relevant year (i.e. the first and the last year) the real
	 * calendar number of days (which is at least 365) is used.
	 */
	protected int getDaysOfYear(int year)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		return calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Set the interests for Invoice corresponding to the given DunningLetterEntries.
	 * 
	 * @param config
	 * @param calculationDate The date of until which all interests of the letter shall be calculated.
	 * @param prevLetterEntry
	 * @param newLetterEntry
	 */
	public abstract void generateDunningInterests(
			DunningConfig config, Date calculationDate, 
			DunningLetterEntry prevLetterEntry, DunningLetterEntry newLetterEntry
	);

	/**
	 * Create an instance of <code>DunningInterestCalculator</code> .
	 */
	public DunningInterestCalculator(String organisationID, long dunningInterestCalculatorID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningInterestCalculatorID = dunningInterestCalculatorID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getDunningInterestCalculatorID()
	{
		return dunningInterestCalculatorID;
	}
	
	@Override
	public DunningInterestCalculator clone(CloneContext context, boolean cloneReferences)
	{
//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
//		DunningInterestCalculator clone = (DunningInterestCalculator) super.clone();
		DunningInterestCalculator clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
//	END OF WORKAROUND

		clone.dunningInterestCalculatorID = IDGenerator.nextID(DunningInterestCalculator.class);
		return clone;
	}
	
	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
	}
}