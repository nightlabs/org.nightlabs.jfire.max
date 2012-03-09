package org.nightlabs.jfire.accounting;

import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer.BookType;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * Implementors have to create a MoneyTransfer matching the given PayableObject.
 * 
 * @author mheinzmann
 */
public interface PayableObjectMoneyTransferFactory
{
	/**
	 * Creates a MoneyTransfer matching the given PayableObject's type and adds the newly created transfer to the
	 * given container. 
	 * 
	 * @param container The container to add the newly created MoneyTransfer to. 
	 * @param bookType TODO
	 * @param payableObject The payable oject that can be referenced by the newly created MoneyTransfer and whose type
	 *                      has to be copeable with by the implementation. 
	 * @param from TODO
	 * @param to TODO
	 * @param amount TODO
	 * @return a MoneyTransfer matching the given PayableObject's type and adds the newly created transfer to the
	 * given container.
	 */
	PayableObjectMoneyTransfer<?> createMoneyTransfer(MoneyTransfer container, BookType bookType, PayableObject payableObject, Anchor from, Anchor to, long amount);
}
