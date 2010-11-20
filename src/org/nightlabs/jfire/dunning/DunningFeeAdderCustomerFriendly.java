package org.nightlabs.jfire.dunning;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningFeeAdderID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * This is our default implementation of the DunningFeeAdder. It only
 * adds new fees if at least one of the dunned invoices has increased
 * its dunningLevel. If this happens, it will add the fees of the ProcessDunningStep
 * corresponding to that level.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningFeeAdderCustomerFriendly"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningFeeAdderCustomerFriendly
extends DunningFeeAdder
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningFeeAdderCustomerFriendly.class);

	public static final DunningFeeAdderID ID = DunningFeeAdderID.create(Organisation.DEV_ORGANISATION_ID, "Dunning Fee Adder Customer Friendly");

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningFeeAdderCustomerFriendly() { }

	public DunningFeeAdderCustomerFriendly(String organisationID, String dunningFeeAdderID)
	{
		super(organisationID, dunningFeeAdderID);
	}

	/**
	 * Adds new fees if at least one of the dunned invoices has increased
	 * its dunningLevel. If this happens, it will add the fees of the ProcessDunningStep
	 * corresponding to that level.
	 */
	@Override
	public void addDunningFee(DunningLetter prevDunningLetter, DunningLetter newDunningLetter) {
		DunningConfig dunningConfig = newDunningLetter.getDunningProcess().getDunningConfig();
		DunningProcess dunningProcess = newDunningLetter.getDunningProcess();

		// *** REV_marco_dunning ***
		// This implementation is wrong. It does not do what is explained in the javadoc.
		// As I understand it, it should find the highest *new* dunning-level. And then
		// add the fees ONCE for this dunning-level. But your code adds the fees multiple times
		// if there are multiple invoices with modified dunning-level.
		if (newDunningLetter.haveChangedItem()) {
			for (DunningLetterEntry prevEntry : prevDunningLetter.getDunnedInvoices()) {

				// *** REV_marco_dunning ***
				// Can this ever return an index >= 0? Is a prevEntry ever to be found in the NEW dunning letter?
				// IMHO every entry should be copied from one dunning letter to the other, thus there should never be one entry in both (prev + new) letters!
				int index = newDunningLetter.getDunnedInvoices().indexOf(prevEntry);
				if (index > 0) { // TODO what about 0??? Isn't that a valid index?!
					DunningLetterEntry entry = newDunningLetter.getDunnedInvoices().get(index);
					if (entry.getDunningLevel() > prevEntry.getDunningLevel()) {
						ProcessDunningStep processDunningStep = dunningConfig.getProcessDunningStep(entry.getDunningLevel());
						for (DunningFeeType dunningFeeType : processDunningStep.getFeeTypes()) {
							DunningFee fee = new DunningFee(newDunningLetter.getOrganisationID(), IDGenerator.nextIDString(DunningFee.class), null);
							fee.setAmountPaid(0);

							Price amountToPay = dunningFeeType.getCurrency2price().get(dunningProcess.getCurrency());
							fee.setAmountToPay(amountToPay.getAmount());
							fee.setDunningFeeType(dunningFeeType);

							newDunningLetter.addDunningFee(fee);
						}
					}
				}
			}
		}
	}
}