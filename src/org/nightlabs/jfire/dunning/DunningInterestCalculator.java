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

	public void createDunningInterest(InvoiceDunningStep invoiceDunningStep, DunningLetter prevLetter, DunningLetter letter) {
		if (prevLetter == null) {
			for (DunningLetterEntry entry : letter.getDunnedInvoices()) {
				DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextIDString(DunningInterest.class), entry, null);
				calculate(invoiceDunningStep, interest);
				entry.addDunningInterest(interest);
			}
		}
		else {
			List<DunningLetterEntry> entries = letter.getDunnedInvoices();
			List<DunningLetterEntry> prevEntries = prevLetter.getDunnedInvoices();
			for (DunningLetterEntry entry : entries) {
				Date firstDay = getFirstDay(entry);
				Date lastDay = getLastDay(entry);
				long dayDifTime = lastDay.getTime() - firstDay.getTime();
				long dayDiffDays = dayDifTime / (24l * 60l * 60l * 1000l);
				
				int entryIndex = prevEntries.indexOf(entry);
				if (entryIndex != -1) {
					DunningLetterEntry prevEntry = prevEntries.get(entryIndex);
					DunningInterest prevInterest = prevEntry.getDunningInterests().get(prevEntry.getDunningInterests().size() - 1);

					BigDecimal interestPercentage = invoiceDunningStep.getInterestPercentage();
					if (interestPercentage.equals(prevInterest.getInterestPercentage())) {
						prevInterest.setCreditPeriodToExcl(lastDay);
						calculate(invoiceDunningStep, prevInterest);
					}
					else {
						DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextIDString(DunningInterest.class), entry, prevInterest);
						interest.setInterestPercentage(interestPercentage);
						interest.setCreditPeriodFromIncl(firstDay);
						interest.setCreditPeriodToExcl(lastDay);

						calculate(invoiceDunningStep, interest);
						entry.addDunningInterest(interest);
					}
				}
				else {
					DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextIDString(DunningInterest.class), entry, null);
					calculate(invoiceDunningStep, interest);
					entry.addDunningInterest(interest);
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