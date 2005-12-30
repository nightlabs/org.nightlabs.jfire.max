/*
 * Created on Oct 23, 2005
 */
package org.nightlabs.ipanema.store.book;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.store.DeliveryNote;
import org.nightlabs.ipanema.store.DeliveryNoteProductTransfer;
import org.nightlabs.ipanema.trade.Article;
import org.nightlabs.ipanema.trade.LegalEntity;
import org.nightlabs.ipanema.transfer.Anchor;
import org.nightlabs.ipanema.transfer.TransferRegistry;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.store.DeliveryNoteProductTransfer"
 *		detachable="true"
 *		table="JFireTrade_BookProductTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class BookProductTransfer extends DeliveryNoteProductTransfer
{
	/**
	 * @deprecated Only for JDO!
	 */
	protected BookProductTransfer() { }

	private static Collection getProducts(DeliveryNote deliveryNote, boolean reversing)
	{
		if (deliveryNote == null)
			throw new IllegalArgumentException("deliveryNote must not be null!");

		Collection articles = deliveryNote.getArticles();
		Collection products = null;
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();

			if (article.isReversing() == reversing) {
				if (products == null)
					products = new ArrayList(articles.size());

				products.add(article.getProduct());
			}
		}
		return products;
	}

	/**
	 * This method creates one or two <code>BookProductTransfer</code>s, depending
	 * on whether there are only "normal" articles, only reversing articles or both.
	 *
	 * @param deliveryNote
	 * @return
	 */
	public static List createBookProductTransfers(
			TransferRegistry transferRegistry, User initiator, DeliveryNote deliveryNote)
	{
		if (deliveryNote.getDeliveryNoteLocal().isBooked())
			throw new IllegalArgumentException("deliveryNote is already booked!");

		List res = new ArrayList(2);
		Collection productsNormal = getProducts(deliveryNote, false);
		Collection productsReversing = getProducts(deliveryNote, true);

		if (productsNormal != null)
			res.add(new BookProductTransfer(
					transferRegistry, initiator,
					deliveryNote.getVendor(), deliveryNote.getCustomer(),
					deliveryNote, productsNormal));

		if (productsReversing != null)
			res.add(new BookProductTransfer(
					transferRegistry, initiator,
					deliveryNote.getCustomer(), deliveryNote.getVendor(),
					deliveryNote, productsReversing));

		return res;
	}

	protected BookProductTransfer(
			TransferRegistry transferRegistry, User initiator,
			Anchor from, Anchor to, DeliveryNote deliveryNote, Collection products)
	{
		super(BOOK_TYPE_BOOK, transferRegistry, initiator, from, to, deliveryNote, products);

		if (!(from instanceof LegalEntity))
			throw new IllegalArgumentException("from must be an instance of LegalEntity, but is of type " + from.getClass().getName());

		if (!(to instanceof LegalEntity))
			throw new IllegalArgumentException("to must be an instance of LegalEntity, but is of type " + from.getClass().getName());
	}
}
