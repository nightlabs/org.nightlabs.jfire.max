/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.accounting;

import java.util.Collection;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;

import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * A {@link MoneyTransfer} is associated to one {@link Invoice}. It is 
 * used for the transfers made when an {@link Invoice} is booked as well as for
 * those transfers made for a {@link Payment}. The property {@link #getBookType()}
 * is then set accordingly to either {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_PayableObjectMoneyTransfer")
@Queries(
	@javax.jdo.annotations.Query(
		name="getPayableObjectMoneyTransfersForPO",
		value="SELECT WHERE this.payableObject == :payableObject && this.bookType == :bookType")
)
@Inheritance(strategy=InheritanceStrategy.SUBCLASS_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class PayableObjectMoneyTransfer<PO extends PayableObject>
	extends MoneyTransfer
{
	private static final long serialVersionUID = 1L;
	
	public enum BookType {
		/**
		 * Book-type set when the transfer is used as sub-transfer for a booking process.
		 */
		book,
		
		/**
		 * Book-type set when the transfer is used as sub-transfer made for a {@link Payment}.
		 */
		pay;
	}
	
	/**
	 * The payable object this money transfer is related with.
	 */
	private PO payableObject;

	/**
	 * The type of MoneyTransfer, i.e. either booking or paying.
	 */
	@Persistent(nullValue=NullValue.EXCEPTION)
	private BookType bookType;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PayableObjectMoneyTransfer() { }

	private static Currency getCurrency(PayableObject payableObject)
	{
		if (payableObject == null)
			throw new IllegalArgumentException("invoice must not be null!");

		return (Currency) payableObject.getCurrency();
	}

	/**
	 * Create a new {@link PayableObjectMoneyTransfer} with no container.
	 * 
	 * @param bookType The book-type for the new transfer. The type defines whether the transfer is used for a booking or a payment.
	 * @param initiator The user that initiated the transfer.
	 * @param from The from-anchor for the new transfer.
	 * @param to The to-anchor for the new transfer.
	 * @param invoice The {@link Invoice} the new transfer should be linked to.
	 * @param amount The amount of the transfer.
	 */
	public PayableObjectMoneyTransfer(
			BookType bookType,
			User initiator, Anchor from, Anchor to,
			PO payableObject, long amount)
	{
		super(null, initiator, from, to, getCurrency(payableObject), amount);
		assert payableObject != null;
		assert bookType != null;
		this.payableObject = payableObject;
		this.bookType = bookType;
	}

	/**
	 * Create a new {@link PayableObjectMoneyTransfer} for the given container.
	 * 
	 * @param bookType The book-type for the new transfer. The type defines whether the transfer is used for a booking or a payment.
	 * @param containerMoneyTransfer The container-transfer for the new account.
	 * @param from The from-anchor for the new transfer.
	 * @param to The to-anchor for the new transfer.
	 * @param invoice The {@link Invoice} the new transfer should be linked to.
	 * @param amount The amount of the transfer.
	 */
	public PayableObjectMoneyTransfer(
			BookType bookType,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
			PO payableObject, long amount)
	{
		super(containerMoneyTransfer, from, to, amount);
		assert payableObject != null;
		assert bookType != null;
		
		this.payableObject = payableObject;
		this.bookType = bookType;
	}

	/**
	 * @return Returns the payable object this transfer is associated to.
	 */
	public PO getPayableObject()
	{
		return payableObject;
	}
	
	/**
	 * Returns the book-type of this transfer. The book type tells whether the transfer was used during a booking or payment.
	 * The return value should be one of {@link BookType#book} or {@link BookType#pay}.
	 *  
	 * @return The book-type.
	 */
	public BookType getBookType()
	{
		return bookType;
	}
	
	/**
	 * Sets the bookType of this money transfer.
	 * @param bookType
	 */
	protected void setBookType(BookType bookType)
	{
		this.bookType = bookType;
	}

	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#bookTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void bookTransfer(User user, Set<Anchor> involvedAnchors)
	{
		bookTransferAtPayableObject(user, involvedAnchors, payableObject);
		super.bookTransfer(user, involvedAnchors);
	}

	protected abstract void bookTransferAtPayableObject(User user, Set<Anchor> involvedAnchors, PayableObject payableObject);


	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#rollbackTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
		rollbackTransferAtPayableObject(user, involvedAnchors, payableObject);
		super.rollbackTransfer(user, involvedAnchors);
	}

	protected abstract void rollbackTransferAtPayableObject(User user, Set<Anchor> involvedAnchors, PayableObject payableObject);
	
	@Override
	protected String internalGetDescription()
	{
		return super.internalGetDescription() + 
			" of type '" + getBookType() +
			"' for Object with id '" + getPayableObject().getPayableObjectID() + "'";
	}

	/**
	 * Get those {@link PayableObjectMoneyTransfer}s that where made for the given invoice and bookType.
	 * The bookType can be one of the constants in this class {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @param payableObject The invoice to find the transfers for.
	 * @param bookType The bookType to find the transfers for. Use one of the constants {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
	 * @return Those {@link PayableObjectMoneyTransfer}s that where made for the given invoice and bookType
	 */
	@SuppressWarnings("unchecked")
	public static Collection<PayableObjectMoneyTransfer> getPayableObjectMoneyTransfers(
			PersistenceManager pm, PayableObject payableObject, BookType bookType
			)
	{
		Query q = pm.newNamedQuery(PayableObjectMoneyTransfer.class, "getPayableObjectMoneyTransfersForPO");
		return (Collection<PayableObjectMoneyTransfer>) q.execute(payableObject, bookType);
	}
}
