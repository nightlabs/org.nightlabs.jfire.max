/*
 * Created on Oct 23, 2005
 */
package org.nightlabs.ipanema.store;

import java.util.Collection;

import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.transfer.Anchor;
import org.nightlabs.ipanema.transfer.Transfer;
import org.nightlabs.ipanema.transfer.TransferRegistry;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.store.ProductTransfer"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteProductTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryNoteProductTransfer extends ProductTransfer
{
	public static final String BOOK_TYPE_BOOK = "book";
	public static final String BOOK_TYPE_DELIVER = "deliver";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private DeliveryNote deliveryNote;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String bookType;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryNoteProductTransfer() { }

	public DeliveryNoteProductTransfer(
			String bookType, TransferRegistry transferRegistry,
			User initiator, Anchor from, Anchor to,
			DeliveryNote deliveryNote, Collection products)
	{
		super(transferRegistry, (ProductTransfer)null, initiator, from, to, products);

		if (deliveryNote == null)
			throw new IllegalArgumentException("deliveryNote must not be null!");

		this.deliveryNote = deliveryNote;

		this.setBookType(bookType);
	}

	public DeliveryNoteProductTransfer(
			String bookType, TransferRegistry transferRegistry,
			Transfer container, Anchor from, Anchor to,
			DeliveryNote deliveryNote, Collection products)
	{
		super(transferRegistry, container, (User)null, from, to, products);

		if (deliveryNote == null)
			throw new IllegalArgumentException("deliveryNote must not be null!");

		this.deliveryNote = deliveryNote;

		this.setBookType(bookType);
	}

	public DeliveryNote getDeliveryNote()
	{
		return deliveryNote;
	}

	/**
	 * @param bookType The bookType to set.
	 */
	protected void setBookType(String bookType)
	{
		if (!BOOK_TYPE_BOOK.equals(bookType) &&
				!BOOK_TYPE_DELIVER.equals(bookType))
			throw new IllegalArgumentException("bookType \""+bookType+"\" is invalid! Must be BOOK_TYPE_BOOK or BOOK_TYPE_DELIVER!");

		this.bookType = bookType;
	}

	public String getBookType()
	{
		return bookType;
	}
}
