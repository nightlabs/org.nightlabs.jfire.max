package org.nightlabs.jfire.dunning.book;

import java.util.Set;

import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

public class BookDunningLetterMoneyTransfer 
extends MoneyTransfer
{
	private static final long serialVersionUID = 1L;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetter dunningLetter;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected BookDunningLetterMoneyTransfer() { }

	public BookDunningLetterMoneyTransfer(
			String bookType,
			User initiator, Anchor from, Anchor to,
			DunningLetter dunningLetter, long amount)
	{
		super(null, initiator, from, to, dunningLetter.getDunningProcess().getCurrency(), amount);
		this.dunningLetter = dunningLetter;
	}

	public BookDunningLetterMoneyTransfer(
			String bookType,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
			DunningLetter dunningLetter, long amount)
	{
		super(containerMoneyTransfer, from, to, amount);
		this.dunningLetter = dunningLetter;
	}

	/**
	 * @return Returns the dunningLetter this transfer is associated to.
	 */
	public DunningLetter getDunningLetter()
	{
		return dunningLetter;
	}

	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#bookTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void bookTransfer(User user, Set<Anchor> involvedAnchors)
	{
		super.bookTransfer(user, involvedAnchors);
	}

	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#rollbackTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
		super.rollbackTransfer(user, involvedAnchors);
	}
}
