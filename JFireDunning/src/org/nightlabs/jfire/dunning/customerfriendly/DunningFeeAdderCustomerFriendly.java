package org.nightlabs.jfire.dunning.customerfriendly;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.dunning.DunningConfig;
import org.nightlabs.jfire.dunning.DunningFee;
import org.nightlabs.jfire.dunning.DunningFeeAdder;
import org.nightlabs.jfire.dunning.DunningFeeType;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.dunning.DunningLetterEntry;
import org.nightlabs.jfire.dunning.DunningStep;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * A customer-friendly DunningFeeAdder.
 * 
 * <p>
 * 	This is our default implementation of the DunningFeeAdder. <br/> 
 *	It only adds new fees if at least one of the dunned invoices has increased its dunningLevel. 
 * 	If this happens, it will add the fees of the ProcessDunningStep corresponding to the highest level any invoices 
 * 	has gotten into.
 * </p>
 *
 * @author Marius Heinzmann <!-- Marius[DOT]Heinzmann[AT]NightLabs[DOT]de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningFeeAdderCustomerFriendly")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class DunningFeeAdderCustomerFriendly
	extends DunningFeeAdder
{
	private static final long serialVersionUID = 1L;

	public DunningFeeAdderCustomerFriendly(String organisationID)
	{
		this(organisationID, IDGenerator.nextID(DunningFeeAdder.class));
	}

	public DunningFeeAdderCustomerFriendly(String organisationID, long dunningFeeAdderID)
	{
		super(organisationID, dunningFeeAdderID);
	}

	/**
	 * This is our default implementation of the DunningFeeAdder. 
	 * It only adds new fees if at least one of the dunned invoices has increased its dunningLevel. 
	 * If this happens, it will add the fees of the ProcessDunningStep corresponding to the highest level any invoices 
	 * has gotten into.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void addDunningFees(
			DunningLetter prevDunningLetter, DunningLetter newDunningLetter, DunningConfig dunningConfig, Currency currency)
	{
		if (newDunningLetter.getEntries() == null || newDunningLetter.getEntries().isEmpty())
			throw new IllegalArgumentException("Cannot add DunningFees to a letter that does not contain a single entry!" +
					"newLetter = " + newDunningLetter);
		
		// create new instances of fees for the already existing ones and make them reference their original fee.
		if (prevDunningLetter != null)
		{
			if (prevDunningLetter.getDunningFees() != null)
			{
				for (DunningFee oldFee : prevDunningLetter.getDunningFees())
				{
					DunningFee newFee = oldFee.getDunningFeeType().createDunningFee(
							newDunningLetter.getOrganisationID(), currency, oldFee
					); 
					newDunningLetter.addDunningFee(newFee);
				}
			}
		}
		
		int highestNewlyReachedLevel = getHighestNewlyReachedDunningLevel(prevDunningLetter, newDunningLetter);
		
		// no new invoice reached a new level
		if (highestNewlyReachedLevel < 0)
			return;
			
		
		DunningStep dunningStep = dunningConfig.getDunningStep(highestNewlyReachedLevel);
		if (dunningStep == null)
			throw new IllegalStateException("No processDunningStep could be found for level=" + 
					highestNewlyReachedLevel + " and DunningConfig=" + dunningConfig);

		for (DunningFeeType dunningFeeType : dunningStep.getFeeTypes())
		{
			DunningFee fee = dunningFeeType.createDunningFee(newDunningLetter.getOrganisationID(), currency, null);
			newDunningLetter.addDunningFee(fee);
		}
	}

	private int getHighestNewlyReachedDunningLevel(DunningLetter prevDunningLetter, DunningLetter newDunningLetter)
	{
		int highestNewlyReachedLevel = -1;
		
		// if there was a previous letter, then search for the highest new level any entry has reached
		if (prevDunningLetter != null && prevDunningLetter.getEntries() != null)
		{
			for (DunningLetterEntry entry : newDunningLetter.getEntries())
			{
				DunningLetterEntry oldEntry = prevDunningLetter.getEntry(entry.getInvoice());
				
				// we have a newly added invoice (note this can also happen when manually triggered, hence level can be > 0)!
				if (oldEntry == null)
				{
					if (entry.getDunningLevel() > highestNewlyReachedLevel)
						highestNewlyReachedLevel = entry.getDunningLevel();
				}
				else
				{
					// the invoice was already included in the last letter, so we need to check if the dunningLevel increased.
					int oldLevel = oldEntry.getDunningLevel();
					int newLevel = entry.getDunningLevel();
					if (newLevel > oldLevel && newLevel > highestNewlyReachedLevel)
						highestNewlyReachedLevel = newLevel;
				}
			} // for (DunningLetterEntry entry : newDunningLetter.getEntries())
		}
		else
		{
			// if there was non previous letter, go through all new entries and take highest level.
			if (newDunningLetter.getEntries() != null)
			{
				for (DunningLetterEntry newLetterEntry : newDunningLetter.getEntries())
				{
					if (newLetterEntry.getDunningLevel() > highestNewlyReachedLevel)
						highestNewlyReachedLevel = newLetterEntry.getDunningLevel();
				}
			}
		}
		
		return highestNewlyReachedLevel;
	}
}