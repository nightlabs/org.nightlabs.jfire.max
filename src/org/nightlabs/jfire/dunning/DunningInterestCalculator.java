package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;

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
		table="JFireDunning_DunningInterestCalculator"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningInterestCalculator
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningInterestCalculator.class);

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningInterestCalculatorID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningConfig dunningConfig;
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningInterestCalculator() { }

	/**
	 * How many days does the year have? This is important to
	 * divide the percentage accordingly (i.e. is a delay of
	 * payment of 1 day calculated as dueAmount * percentage / 360
	 * or dueAmount * percentage / 365 or is the real calendar used)?
	 * @return
	 */
	// *** REV_marco_dunning ***
	// Why is this never used? And please give it a better name. getDays() doesn't say anything - maybe better getDaysOfYear() and then you might want to provide a year - i.e. getDaysOfYear(int year) or getDaysOfYear(Date timestampWithinYear).
	// And why is this method public? Think about what's the contract of a DunningInterestCalculator to the external world: Isn't
	// it only your generateDunningInterest(...) method? Why expose other things publicly? IMHO all non-external-API methods should be protected.
	public abstract int getDays();

	/**
	 * What's the first day in the interest calculation? Is it the
	 * due date or the following day at midnight?
	 * @param dunningLetterEntry
	 * @return
	 */
	public abstract Date getFirstDay(DunningLetterEntry dunningLetterEntry);

	/**
	 * What's the last day in the interest calculation? There are many options:
	 * The new due date, the finalization date of the DunningLetter or the
	 * creation date of the DunningLetter. And is this day included or excluded
	 * in the period?
	 * @param dunningLetterEntry
	 * @return
	 */
	public abstract Date getLastDay(DunningLetterEntry dunningLetterEntry);

	// *** REV_marco_dunning ***
	// Write better javadoc, please! A javadoc comment must decribe the method. That means it
	// would be more appropriate to start like this: "Create new instances of {@link DunningInterest} accoding to
	// the time passed ..."
	// Simply imagine, you had no idea about this method (i.e. never read any spec). What would you
	// expect to read here? What would help you to understand the behaviour of this method? What you copied here (from the spec)
	// definitely helps, but only as second or third paragraph. WRITE SOMETHING YOURSELF! Not only copy + paste things.
	// In this concrete case, here, you have two kinds of people who read this javadoc:
	// Those who want to USE this method and therefore need to understand what they have to feed into it to make it do what
	// they want.
	// And those people who want to OVERRIDE this method, because they want to implement a different way to handle interest
	// calculation. These people need to know what they have to do in their implementation.
	// In both cases, a clear contract would help. Every API method is part of a contract - i.e. defines a sub-contract. You
	// should use the javadoc to explain this sub-contract in human language. If you cannot do this properly, then you didn't think
	// enough about it or you didn't understand the specification properly. In both situations it might help to think through
	// the work flow. Think about when this method is called and what the situation looks like at this time (i.e. what objects exist
	// and what states do they have and what should change).
	/**
	 * It is important to note that the InterestCalculator needs to know
	 * the previous DunningInterests (from the previous DunningLetter)
	 * in order to be able to merge Interests from adjacent time intervals
	 * and to realize that partial payments have already been conducted!
	 *
	 * @param invoiceDunningStep
	 * @param prevLetter
	 * @param newLetter
	 */
	public void generateDunningInterest(InvoiceDunningStep invoiceDunningStep, DunningLetter prevLetter, DunningLetter newLetter) {
		if (prevLetter == null) { // New letter
			for (DunningLetterEntry entry : newLetter.getDunnedInvoices()) {
				DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextIDString(DunningInterest.class), entry, null);
				calculate(invoiceDunningStep, interest);
				entry.addDunningInterest(interest);
			}
		}
		else {
			List<DunningLetterEntry> newLetterEntries = newLetter.getDunnedInvoices();
			List<DunningLetterEntry> prevLetterEntries = prevLetter.getDunnedInvoices();
			BigDecimal interestPercentage = invoiceDunningStep.getInterestPercentage();

			for (DunningLetterEntry newLetterEntry : newLetterEntries) {
				Date firstDay = getFirstDay(newLetterEntry); //either the invoice due date or the extended due date
				Date lastDay = getLastDay(newLetterEntry);
				long dayDifTime = lastDay.getTime() - firstDay.getTime();
				long dayDiffDays = dayDifTime / (24l * 60l * 60l * 1000l);

				int entryIndex = prevLetterEntries.indexOf(newLetterEntry);
				if (entryIndex != -1) { //combination of an old one + additional time
					// *** REV_marco_dunning ***
					// What's this?! IMHO that's nonsense. The previous interest is the corresponding interest in the previous DunningLetter
					// and definitely not in the new DunningLetter! Think about what you are doing here & understand it!
					DunningInterest prevInterest = newLetterEntry.getDunningInterests().get(newLetterEntry.getDunningInterests().size() - 1);

					if (interestPercentage.equals(prevInterest.getInterestPercentage())) { //if the interest percentage didn't change
						Date extendedDate = new Date(lastDay.getTime() + newLetterEntry.getPeriodOfGraceMSec());
						prevInterest.setCreditPeriodToExcl(extendedDate);
						calculate(invoiceDunningStep, prevInterest);
					}
					else { //create new interest
						DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextIDString(DunningInterest.class), newLetterEntry, prevInterest);
						interest.setInterestPercentage(interestPercentage);
						interest.setCreditPeriodFromIncl(firstDay);
						interest.setCreditPeriodToExcl(lastDay);

						calculate(invoiceDunningStep, interest);
						newLetterEntry.addDunningInterest(interest);
					}
				}
				else { //create completely new one
					DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextIDString(DunningInterest.class), newLetterEntry, null);
					interest.setInterestPercentage(interestPercentage);
					interest.setCreditPeriodFromIncl(firstDay);
					interest.setCreditPeriodToExcl(lastDay);

					calculate(invoiceDunningStep, interest);
					newLetterEntry.addDunningInterest(interest);
				}
			}
		}
	}

	private void calculate(InvoiceDunningStep invDunningStep, DunningInterest interest) {
		BigDecimal interestPercentage = invDunningStep.getInterestPercentage();
		double interestAmount = interestPercentage.doubleValue() * interest.getBaseAmount(); //TODO Check if the base amount needed to be converted?
		double amountToPay = interestAmount - interest.getAmountPaid();

		interest.setAmountToPay(interest.getCurrency().toLong(amountToPay));
		interest.setInterestAmount(interest.getCurrency().toLong(interestAmount));
	}

	/**
	 * Create an instance of <code>DunningInterestCalculator</code>.
	 *
	 */
	public DunningInterestCalculator(String organisationID, String dunningInterestCalculatorID) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningInterestCalculatorID = dunningInterestCalculatorID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getDunningInterestCalculatorID() {
		return dunningInterestCalculatorID;
	}

	public void setDunningConfig(DunningConfig dunningConfig) {
		this.dunningConfig = dunningConfig;
	}

	public DunningConfig getDunningConfig() {
		return dunningConfig;
	}
}