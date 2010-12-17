package org.nightlabs.jfire.dunning.book;


import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * A {@link BookDunningLetterMoneyTransfer} is created when an {@link DunningLetter} is booked. 
 * It is the container transfer of all sub-transfers made for this booking process. 
 * <p>
 * The transfer happens between two LegalEntities. This means, 
 * <tt>from</tt> and <tt>to</tt> must be instances of <tt>LegalEntity</tt>.
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_BookDunningLetterMoneyTransfer")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class BookDunningLetterMoneyTransfer extends DunningLetterMoneyTransfer
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(BookDunningLetterMoneyTransfer.class);

	/**
	 * @deprecated Only of JDO!
	 */
	@Deprecated
	protected BookDunningLetterMoneyTransfer() { }

	/**
	 * BookDunningLetterMoneyTransfer associated to only one DunningLetter, as it is used for dunningLetter-bookings.
	 *
	 * @param initiator The user that initiated this transfer.
	 * @param from The from-anchor for the new transfer.
	 * @param to The to-anchor for the new transfer.
	 * @param dunningLetter The {@link DunningLetter} the new transfer should be 
	 */
	public BookDunningLetterMoneyTransfer(User initiator, Anchor from, Anchor to, DunningLetter dunningLetter)
	{
		super(BOOK_TYPE_BOOK, initiator, from, to, dunningLetter, dunningLetter.getAmountPaidExcludingInvoices());

		if (!(from instanceof LegalEntity))
			throw new IllegalArgumentException("from must be an instance of LegalEntity, but is of type " + from.getClass().getName());

		if (!(to instanceof LegalEntity))
			throw new IllegalArgumentException("to must be an instance of LegalEntity, but is of type " + from.getClass().getName());

		if (logger.isDebugEnabled())
			logger.debug("constructor: from=" + from.getPrimaryKey() + " to=" + to.getPrimaryKey());
	}
	
	@Override
	protected String internalGetDescription() {
		return String.format(
				"Booking of dunningLetter %s",
				getDunningLetter().getDunningLetterID()
			);
	}

}
