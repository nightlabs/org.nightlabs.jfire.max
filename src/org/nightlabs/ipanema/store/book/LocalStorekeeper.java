/*
 * Created on Oct 21, 2005
 */
package org.nightlabs.ipanema.store.book;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.store.DeliveryNote;
import org.nightlabs.ipanema.store.ProductTransfer;
import org.nightlabs.ipanema.trade.Article;
import org.nightlabs.ipanema.trade.LegalEntity;
import org.nightlabs.ipanema.trade.OrganisationLegalEntity;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		persistence-capable-superclass = "org.nightlabs.ipanema.store.book.Storekeeper"
 *		detachable = "true"
 *		table="JFireTrade_LocalStorekeeper"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class LocalStorekeeper extends Storekeeper
{
	/**
	 * @deprecated Only for JDO!
	 */
	protected LocalStorekeeper() { }

	public LocalStorekeeper(OrganisationLegalEntity mandator, String storekeeperID)
	{
		super(mandator.getOrganisationID(), storekeeperID);
		this.mandator = mandator;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private OrganisationLegalEntity mandator;

	protected OrganisationLegalEntity getMandator()
	{
		return mandator;
	}

	public void bookTransfer(User user, LegalEntity mandator,
			ProductTransfer transfer, Map involvedAnchors)
	{
		// A Storekeeper gets all bookings and has to decide himself what to do.
		if (! (transfer instanceof BookProductTransfer))
			return;

		BookProductTransfer bookTransfer = (BookProductTransfer)transfer;		
		DeliveryNote deliveryNote = bookTransfer.getDeliveryNote();

		// find the delegates
		Map delegates = new HashMap();
		for (Iterator iter = deliveryNote.getArticles().iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			LocalStorekeeperDelegate delegate = article.getProductType().getLocalStorekeeperDelegate();
			if (delegate == null) {
				delegate = DefaultLocalStorekeeperDelegate.getDefaultLocalStorekeeperDelegate(getPersistenceManager());
//				throw new IllegalStateException("Could not find LocalStorekeeperDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+".");
				article.getProductType().setLocalStorekeeperDelegate(delegate);
			}
			delegates.put(article, delegate);
		}
		Set distinctDelegates = new HashSet(delegates.values());
		// call preBookDeliveryNote
		for (Iterator iter = distinctDelegates.iterator(); iter.hasNext();) {
			LocalStorekeeperDelegate delegate = (LocalStorekeeperDelegate) iter.next();
			delegate.preBookArticles(getMandator(), user, deliveryNote, bookTransfer, involvedAnchors);
		}
		// book the individual articles
		for (Iterator iter = deliveryNote.getArticles().iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			LocalStorekeeperDelegate delegate = (LocalStorekeeperDelegate) delegates.get(article);
			if (delegate == null)
				throw new IllegalStateException("Could not find LocalStorekeeperDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+", although already resolved prior.");
			// let the delegate do the job
			delegate.bookArticle(getMandator(), user, deliveryNote, article, bookTransfer, involvedAnchors);
		}
		// call postBookDeliveryNote
		for (Iterator iter = distinctDelegates.iterator(); iter.hasNext();) {
			LocalStorekeeperDelegate delegate = (LocalStorekeeperDelegate) iter.next();
			delegate.postBookArticles(getMandator(), user, deliveryNote, bookTransfer, involvedAnchors);
		}
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of LocalStorekeeper is not persistent. Can't get PersistenceManager");
		return pm;
	}

}
