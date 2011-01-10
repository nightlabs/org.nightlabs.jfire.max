package org.nightlabs.jfire.dunning.book;

import java.util.Collection;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_DunningLetterMoneyTransfer")
@Queries(
	@javax.jdo.annotations.Query(
		name="getDunningLetterMoneyTransfersForDunningLetter",
		value="SELECT WHERE this.dunningLetter == :pDunningLetter && this.bookType == :pBookType ")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningLetterMoneyTransfer
extends MoneyTransfer
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Book-type set when the transfer is used as sub-transfer for a booking process.
	 */
	public static final String BOOK_TYPE_BOOK = "book";
	/**
	 * Book-type set when the transfer is used as sub-transfer made for a {@link Payment}.
	 */
	public static final String BOOK_TYPE_PAY = "pay";

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetter dunningLetter;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String bookType;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningLetterMoneyTransfer() { }

	private static Currency getCurrency(DunningLetter dunningLetter)
	{
		if (dunningLetter == null)
			throw new IllegalArgumentException("dunningLetter must not be null!");

		return dunningLetter.getDunningProcess().getCurrency();
	}

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
			String bookType,
			User initiator, Anchor from, Anchor to,
			DunningLetter dunningLetter, long amount)
	{
		super(null, initiator, from, to, getCurrency(dunningLetter), amount);
		this.dunningLetter = dunningLetter;
		this.setBookType(bookType);
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
			String bookType,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
			DunningLetter dunningLetter, long amount)
	{
		super(containerMoneyTransfer, from, to, amount);
		this.dunningLetter = dunningLetter;
		this.setBookType(bookType);
	}

	/**
	 * @return Returns the dunningLetter this transfer is associated to.
	 */
	public DunningLetter getDunningLetter()
	{
		return dunningLetter;
	}

	/**
	 * Set the book-type of this transfer.
	 * 
	 * @param bookType The bookType to set.
	 */
	protected void setBookType(String bookType)
	{
		if (!BOOK_TYPE_BOOK.equals(bookType) &&
				!BOOK_TYPE_PAY.equals(bookType))
			throw new IllegalArgumentException("bookType \""+bookType+"\" is invalid! Must be BOOK_TYPE_BOOK or BOOK_TYPE_PAY!");

		this.bookType = bookType;
	}

	/**
	 * Returns the book-type of this transfer. The book type tells whether the transfer was used during a booking or payment.
	 * The return value should be one of {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
	 *  
	 * @return The book-type.
	 */
	public String getBookType()
	{
		return bookType;
	}

	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#bookTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void bookTransfer(User user, Set<Anchor> involvedAnchors)
	{
		bookTransferAtDunningLetter(user, involvedAnchors);
		super.bookTransfer(user, involvedAnchors);
	}

	protected void bookTransferAtDunningLetter(User user, Set<Anchor> involvedAnchors)
	{
//		dunningLetter.bookDunningLetterMoneyTransfer(this, false);
	}


	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#rollbackTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
		rollbackTransferAtDunningLetter(user, involvedAnchors);
		super.rollbackTransfer(user, involvedAnchors);
	}

	protected void rollbackTransferAtDunningLetter(User user, Set<Anchor> involvedAnchors)
	{
//		dunningLetter.bookDunningLetter(this, true);
	}

	/**
	 * Get those {@link DunningLetterMoneyTransfer}s that where made for the given dunningLetter and bookType.
	 * The bookType can be one of the constants in this class {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @param dunningLetter The dunningLetter to find the transfers for.
	 * @param bookType The bookType to find the transfers for. Use one of the constants {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
	 * @return Those {@link DunningLetterMoneyTransfer}s that where made for the given dunningLetter and bookType
	 */
	@SuppressWarnings("unchecked")
	public static Collection<DunningLetterMoneyTransfer> getDunningLetterMoneyTransfers(PersistenceManager pm, DunningLetter dunningLetter, String bookType) {
		Query q = pm.newNamedQuery(DunningLetterMoneyTransfer.class, "getDunningLetterMoneyTransfersForDunningLetter");
		return (Collection<DunningLetterMoneyTransfer>) q.execute(dunningLetter, bookType);
	}
}
