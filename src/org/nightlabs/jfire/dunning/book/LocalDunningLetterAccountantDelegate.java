package org.nightlabs.jfire.dunning.book;

import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.book.AccountantDelegate;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_LocalDunningLetterAccountantDelegate"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class LocalDunningLetterAccountantDelegate extends AccountantDelegate
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Do not use! Only for JDO!
	 */
	@Deprecated
	protected LocalDunningLetterAccountantDelegate() {
	}

	public LocalDunningLetterAccountantDelegate(OrganisationLegalEntity mandator, String accountantDelegateID) {
		super(mandator.getOrganisationID(), accountantDelegateID);
		this.mandator = mandator;
	}

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private OrganisationLegalEntity mandator;

	@Override
	public void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors) {
//		// An Accountant gets all bookings and has to decide himself what to do.
//		if (! (transfer instanceof BookInvoiceMoneyTransfer))
//			return;
//
//		BookInvoiceMoneyTransfer bookTransfer = (BookInvoiceMoneyTransfer)transfer;
//		Invoice invoice = bookTransfer.getInvoice();
//
//		// find the delegates
//		Map<Article, LocalAccountantDelegate> delegates = new HashMap<Article, LocalAccountantDelegate>();
//		for (Article article : invoice.getArticles()) {
//			LocalAccountantDelegate delegate = article.getProductType().getProductTypeLocal().getLocalAccountantDelegate();
//			if (delegate == null) // TODO maybe we should have a default one like there is a DefaultLocalStorekeeperDelegate, too.
//				throw new IllegalStateException("Could not find LocalAccountantDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+".");
//
//			delegates.put(article, delegate);
//		}
//
//		Set<LocalAccountantDelegate> distinctDelegates = new HashSet<LocalAccountantDelegate>(delegates.values());
//		// call preBookInvoice
//		for (LocalAccountantDelegate delegate : distinctDelegates) {
//			delegate.preBookArticles(getMandator(), user, invoice, bookTransfer, involvedAnchors);
//		}
//
//		// book the individual articles
//		for (Article article : invoice.getArticles()) {
//			LocalAccountantDelegate delegate = delegates.get(article);
//			if (delegate == null)
//				throw new IllegalStateException("Could not find LocalAccountantDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+", although already resolved prior.");
//			// let the delegate do the job
//			delegate.bookArticle(getMandator(), user, invoice, article, bookTransfer, involvedAnchors);
//		}
//
//		// call postBookInvoice
//		for (LocalAccountantDelegate delegate : distinctDelegates) {
//			delegate.postBookArticles(getMandator(), user, invoice, bookTransfer, involvedAnchors);
//		}
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of LocalDunningLetterAccountantDelegate is not persistent. Can't get PersistenceManager");
		return pm;
	}

	protected OrganisationLegalEntity getMandator() {
		return mandator;
	}
}
