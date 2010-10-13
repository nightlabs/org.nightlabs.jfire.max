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

package org.nightlabs.jfire.accounting.book;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * A {@link LocalBookInvoiceAccountantDelegate} is responsible to split money to the {@link Organisation}s
 * own accounts when payments are received/booked and to collect money from the correct
 * accounts when payments are done by the organisation.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_LocalBookInvoiceAccountantDelegate"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class LocalBookInvoiceAccountantDelegate extends AccountantDelegate
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Do not use! Only for JDO!
	 */
	@Deprecated
	protected LocalBookInvoiceAccountantDelegate() {
	}

	public LocalBookInvoiceAccountantDelegate(OrganisationLegalEntity mandator, String accountantDelegateID) {
		super(mandator.getOrganisationID(), accountantDelegateID);
		this.mandator = mandator;
	}

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private OrganisationLegalEntity mandator;

	/**
	 * Takes care of MoneyTransfers. This Accountant only handles
	 * {@link BookInvoiceMoneyTransfer}s and silently ignores all other {@link MoneyTransfer}s.
	 * It will created sub-transfers with the given {@link BookInvoiceMoneyTransfer} as container.
	 * <p>
	 * This method searches for the {@link LocalAccountantDelegate}s configured for the
	 * {@link ProductType}s of the articles found in the {@link Invoice} referenced by the
	 * given {@link BookInvoiceMoneyTransfer} and delegates the work of creating the correct
	 * sub-transfers to the according {@link LocalAccountantDelegate} found.
	 * </p>
	 *
	 * @see org.nightlabs.jfire.accounting.book.Accountant#bookTransfer(User, LegalEntity, MoneyTransfer, Map)
	 */
	@Override
	public void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors) {
		// An Accountant gets all bookings and has to decide himself what to do.
		if (! (transfer instanceof BookInvoiceMoneyTransfer))
			return;

		BookInvoiceMoneyTransfer bookTransfer = (BookInvoiceMoneyTransfer)transfer;
		Invoice invoice = bookTransfer.getInvoice();

		// find the delegates
		Map<Article, LocalAccountantDelegate> delegates = new HashMap<Article, LocalAccountantDelegate>();
		for (Article article : invoice.getArticles()) {
			LocalAccountantDelegate delegate = article.getProductType().getProductTypeLocal().getLocalAccountantDelegate();
			if (delegate == null) // TODO maybe we should have a default one like there is a DefaultLocalStorekeeperDelegate, too.
				throw new IllegalStateException("Could not find LocalAccountantDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+".");

			delegates.put(article, delegate);
		}

		Set<LocalAccountantDelegate> distinctDelegates = new HashSet<LocalAccountantDelegate>(delegates.values());
		// call preBookInvoice
		for (LocalAccountantDelegate delegate : distinctDelegates) {
			delegate.preBookArticles(getMandator(), user, invoice, bookTransfer, involvedAnchors);
		}

		// book the individual articles
		for (Article article : invoice.getArticles()) {
			LocalAccountantDelegate delegate = delegates.get(article);
			if (delegate == null)
				throw new IllegalStateException("Could not find LocalAccountantDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+", although already resolved prior.");
			// let the delegate do the job
			delegate.bookArticle(getMandator(), user, invoice, article, bookTransfer, involvedAnchors);
		}

		// call postBookInvoice
		for (LocalAccountantDelegate delegate : distinctDelegates) {
			delegate.postBookArticles(getMandator(), user, invoice, bookTransfer, involvedAnchors);
		}
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of LocalBookInvoiceAccountantDelegate is not persistent. Can't get PersistenceManager");
		return pm;
	}

	protected OrganisationLegalEntity getMandator() {
		return mandator;
	}
}
