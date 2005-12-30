/*
 * Created 	on Feb 17, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting.book;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		persistence-capable-superclass = "org.nightlabs.jfire.accounting.book.Accountant"
 *		detachable = "true"
 *		table="JFireTrade_LocalAccountant"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class LocalAccountant extends Accountant {
	
	/**
	 * @deprecated Do not use! Only for JDO!
	 */
	protected LocalAccountant() {
	}

	/**
	 * @param mandator
	 * @param accountantID
	 */
	public LocalAccountant(OrganisationLegalEntity mandator, String accountantID) {
		super(mandator.getOrganisationID(), accountantID);
		this.mandator = mandator;
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private OrganisationLegalEntity mandator;

		
	/**
	 * Takes care of MoneyTransfers. This Accountant only handles
	 * {@link BookMoneyTransfer}s and silently ignores all other {@link MoneyTransfer}s.
	 * This method dispatches the right amounts from the associated Invoices to the configured
	 * Accounts.
	 * 
	 * @see org.nightlabs.jfire.accounting.book.Accountant#bookTransfer(User, LegalEntity, MoneyTransfer, Map)
	 */
	public void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Map involvedAnchors) {
		// An Accountant gets all bookings and has to decide himself what to do.
		if (! (transfer instanceof BookMoneyTransfer))
			return;

		BookMoneyTransfer bookTransfer = (BookMoneyTransfer)transfer;		
		Invoice invoice = bookTransfer.getInvoice();

		// find the delegates
		Map delegates = new HashMap();
		for (Iterator iter = invoice.getArticles().iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			LocalAccountantDelegate delegate = article.getProductType().getLocalAccountantDelegate();
			if (delegate == null)
				throw new IllegalStateException("Could not find LocalAccountantDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+".");

			delegates.put(article, delegate);
		}
		Set distinctDelegates = new HashSet(delegates.values());
		// call preBookInvoice
		for (Iterator iter = distinctDelegates.iterator(); iter.hasNext();) {
			LocalAccountantDelegate delegate = (LocalAccountantDelegate) iter.next();
			delegate.preBookArticles(getMandator(), user, invoice, bookTransfer, involvedAnchors);
		}
		// book the individual articles
		for (Iterator iter = invoice.getArticles().iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			LocalAccountantDelegate delegate = (LocalAccountantDelegate) delegates.get(article);
			if (delegate == null)
				throw new IllegalStateException("Could not find LocalAccountantDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+", although already resolved prior.");
			// let the delegate do the job
			delegate.bookArticle(getMandator(), user, invoice, article, bookTransfer, involvedAnchors);
		}
		// call postBookInvoice
		for (Iterator iter = distinctDelegates.iterator(); iter.hasNext();) {
			LocalAccountantDelegate delegate = (LocalAccountantDelegate) iter.next();
			delegate.postBookArticles(getMandator(), user, invoice, bookTransfer, involvedAnchors);
		}
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of LocalAccountant is not persistent. Can't get PersistenceManager");
		return pm;
	}

	protected OrganisationLegalEntity getMandator() {
		return mandator;	
	}

}
