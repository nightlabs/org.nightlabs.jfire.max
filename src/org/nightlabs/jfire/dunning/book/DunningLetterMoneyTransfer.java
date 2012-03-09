package org.nightlabs.jfire.dunning.book;

import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

@PersistenceCapable(detachable="true")
public class DunningLetterMoneyTransfer
	extends PayableObjectMoneyTransfer<DunningLetter>
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningLetterMoneyTransfer() { }

	/**
	 * Create a new {@link DunningLetterMoneyTransfer} with no container.
	 * 
	 * @param bookType The book-type for the new transfer. The type defines whether the transfer is used for a booking or a payment.
	 * @param initiator The user that initiated the transfer.
	 * @param from The from-anchor for the new transfer.
	 * @param to The to-anchor for the new transfer.
	 * @param dunningLetter The {@link DunningLetter} the new transfer should be linked to.
	 * @param amount The amount of the transfer.
	 */
	public DunningLetterMoneyTransfer(
			BookType bookType,
			User initiator, Anchor from, Anchor to,
			DunningLetter dunningLetter, long amount)
	{
		super(bookType, initiator, from, to, dunningLetter, amount);
	}

	/**
	 * Create a new {@link DunningLetterMoneyTransfer} for the given container.
	 * 
	 * @param bookType The book-type for the new transfer. The type defines whether the transfer is used for a booking or a payment.
	 * @param containerMoneyTransfer The container-transfer for the new account.
	 * @param from The from-anchor for the new transfer.
	 * @param to The to-anchor for the new transfer.
	 * @param dunningLetter The {@link DunningLetter} the new transfer should be linked to.
	 * @param amount The amount of the transfer.
	 */
	public DunningLetterMoneyTransfer(
			BookType bookType,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
			DunningLetter dunningLetter, long amount)
	{
		super(bookType, containerMoneyTransfer, from, to, dunningLetter, amount);
	}

	/**
	 * 
	 * @param user
	 * @param involvedAnchors
	 */
	protected void bookTransferAtPayableObject(User user, Set<Anchor> involvedAnchors, PayableObject payableObject)
	{
		if (!BookType.pay.equals(getBookType()))
			return;

		DunningLetter dunningLetter = (DunningLetter) payableObject;
		long oldAmountPaid = dunningLetter.getAmountPaidExcludingInvoices();
		dunningLetter.setAmountPaidExcludingInvoices(oldAmountPaid + getAmount());
		
		long amountToPay = dunningLetter.getAmountToPay();
		if (amountToPay == 0)
		{
			// TODO: something else to do?? (Marius)
			dunningLetter.setOutstanding(false);
		}
	}

	@Override
	protected void rollbackTransferAtPayableObject(User user, Set<Anchor> involvedAnchors, PayableObject payableObject)
	{
		// TODO Auto-generated method stub FIXME: What to do with this?
	}

}
