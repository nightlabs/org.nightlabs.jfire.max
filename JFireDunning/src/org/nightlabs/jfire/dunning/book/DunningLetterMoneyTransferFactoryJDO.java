package org.nightlabs.jfire.dunning.book;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransferFactoryJDO;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer.BookType;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * @author mheinzmann
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true"
)
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class DunningLetterMoneyTransferFactoryJDO
	extends PayableObjectMoneyTransferFactoryJDO
{
	/**
	 * @deprecated only for JDO.
	 */
	protected DunningLetterMoneyTransferFactoryJDO() { }

	public DunningLetterMoneyTransferFactoryJDO(String scope)
	{
		super(scope, DunningLetter.class);
	}

	public DunningLetterMoneyTransferFactoryJDO(String organisationID, String scope)
	{
		super(organisationID, scope, DunningLetter.class);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.PayableObjectMoneyTransferFactoryJDO#doCreateMoneyTransfer(org.nightlabs.jfire.accounting.MoneyTransfer, org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer.BookType, org.nightlabs.jfire.accounting.pay.PayableObject, org.nightlabs.jfire.transfer.Anchor, org.nightlabs.jfire.transfer.Anchor, long)
	 */
	@Override
	protected PayableObjectMoneyTransfer<?> doCreateMoneyTransfer(MoneyTransfer container, BookType bookType,
			PayableObject payableObject, Anchor from, Anchor to, long amount)
	{
		return new DunningLetterMoneyTransfer(bookType, container, from, to, (DunningLetter) payableObject, amount);
	}
}
