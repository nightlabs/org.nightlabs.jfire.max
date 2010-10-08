package org.nightlabs.jfire.dunning;

import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_BookDunningLetterMoneyTransfer")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
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

	/**
	 * @return Returns the invoice this transfer is associated to.
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
//		bookTransferAtInvoice(user, involvedAnchors);
		super.bookTransfer(user, involvedAnchors);
	}

//	protected void bookTransferAtInvoice(User user, Set<Anchor> involvedAnchors)
//	{
//		invoice.bookBookDunningLetterMoneyTransfer(this, false);
//	}


	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#rollbackTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
//		rollbackTransferAtInvoice(user, involvedAnchors);
		super.rollbackTransfer(user, involvedAnchors);
	}

//	protected void rollbackTransferAtInvoice(User user, Set<Anchor> involvedAnchors)
//	{
//		invoice.bookBookDunningLetterMoneyTransfer(this, true);
//	}
}
