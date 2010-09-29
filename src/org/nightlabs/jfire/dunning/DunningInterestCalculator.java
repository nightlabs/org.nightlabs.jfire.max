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

	public void calculateInterest(InvoiceDunningStep invoiceDunningStep, DunningLetter prevLetter, DunningLetter letter) {
		if (prevLetter == null) {
			for (DunningLetterEntry entry : letter.getDunnedInvoices()) {
				createNewInterest(entry);
			}
		}
		else {
			List<DunningLetterEntry> entries = letter.getDunnedInvoices();
			List<DunningLetterEntry> prevEntries = prevLetter.getDunnedInvoices();
			int days = getDays();
			for (DunningLetterEntry entry : entries) {
				Date firstDay = getFirstDay(entry);
				Date lastDay = getLastDay(entry);
				long dayDifTime = lastDay.getTime() - firstDay.getTime();
				long dayDiffDays = dayDifTime / (24 * 60 * 60 * 1000);
				
				int entryIndex = prevEntries.indexOf(entry);
				if (entryIndex != -1) {
					DunningLetterEntry prevEntry = prevEntries.get(entryIndex);
					int dunningLevel = entry.getDunningLevel();
					InvoiceDunningStep invDunningStep = dunningConfig.getInvoiceDunningStep(dunningLevel);
					BigDecimal interestPercentage = invDunningStep.getInterestPercentage();
					
					DunningInterest prevInterest = prevEntry.getDunningInterests().get(prevEntry.getDunningInterests().size() - 1);
					DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextIDString(DunningInterest.class), entry, prevInterest);
					interest.setInterestPercentage(interestPercentage);
					interest.setCreditPeriodFromIncl(firstDay);
					interest.setCreditPeriodToExcl(lastDay);
					
//					double dop = (entry.getPriceIncludingInvoice().getAmount() * interestPercentage.doubleValue()) / days;
//					double amountToPay = dop * dayDiffDays;
					
//					interest.setInterestAmount(interestAmount)
//					interest.setAmountToPay(amountToPay);
					
					
					entry.addDunningInterest(interest);
				}
				else {
					createNewInterest(entry);
				}
			}	
		}
	}

	private void createNewInterest(DunningLetterEntry entry) {
		int dunningLevel = entry.getDunningLevel();
		InvoiceDunningStep invDunningStep = dunningConfig.getInvoiceDunningStep(dunningLevel);

		BigDecimal interestPercentage = invDunningStep.getInterestPercentage();
		long interestAmount = interestPercentage.longValue() * entry.getPriceIncludingInvoice().getAmount();
		long amountToPay = interestAmount;
		
		DunningInterest interest = new DunningInterest(organisationID, IDGenerator.nextIDString(DunningInterest.class), entry, null);
		interest.setInterestPercentage(interestPercentage);
//		interest.setInterestAmount(interestAmount);
		interest.setAmountToPay(amountToPay);
		interest.setAmountPaid(0);
		
		entry.addDunningInterest(interest);
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