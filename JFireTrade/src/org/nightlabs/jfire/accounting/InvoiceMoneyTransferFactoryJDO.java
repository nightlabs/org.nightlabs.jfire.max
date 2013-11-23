package org.nightlabs.jfire.accounting;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer.BookType;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * @author mheinzmann
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true"
)
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class InvoiceMoneyTransferFactoryJDO
	extends PayableObjectMoneyTransferFactoryJDO
{
	/**
	 * @deprecated only for JDO.
	 */
	protected InvoiceMoneyTransferFactoryJDO() { }

	public InvoiceMoneyTransferFactoryJDO(String scope)
	{
		super(scope, Invoice.class);
	}

	public InvoiceMoneyTransferFactoryJDO(String organisationID, String scope)
	{
		super(organisationID, scope, Invoice.class);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.PayableObjectMoneyTransferFactoryJDO#doCreateMoneyTransfer(org.nightlabs.jfire.accounting.MoneyTransfer, org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer.BookType, org.nightlabs.jfire.accounting.pay.PayableObject, org.nightlabs.jfire.transfer.Anchor, org.nightlabs.jfire.transfer.Anchor, long)
	 */
	@Override
	protected PayableObjectMoneyTransfer<?> doCreateMoneyTransfer(MoneyTransfer container, BookType bookType,
			PayableObject payableObject, Anchor from, Anchor to, long amount)
	{
		return new InvoiceMoneyTransfer(bookType, container, from, to, (Invoice) payableObject, amount);
	}

}
