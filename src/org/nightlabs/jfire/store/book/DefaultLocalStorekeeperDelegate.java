/*
 * Created on Oct 23, 2005
 */
package org.nightlabs.jfire.store.book;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.book.id.LocalStorekeeperDelegateID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

/**
 * An instance of this class is automatically created and stored into the datastore
 * when necessary. This happens, when a {@link org.nightlabs.jfire.store.ProductType}
 * does not have a <code>LocalStorekeeperDelegate</code> assigned during the booking of a
 * {@link org.nightlabs.jfire.store.DeliveryNote}. In other words,
 * the method {@link org.nightlabs.jfire.store.Store#bookDeliveryNote(User, DeliveryNote, boolean, boolean)}
 * automatically assigns the per-datastore-singleton of this class (using
 * {@link org.nightlabs.jfire.store.ProductType#setLocalStorekeeperDelegate(LocalStorekeeperDelegate)}),
 * if {@link org.nightlabs.jfire.store.ProductType#getLocalStorekeeperDelegate()} returns
 * <code>null</code>.
 * <p>
 * See {@link #bookArticle(OrganisationLegalEntity, User, DeliveryNote, Article, BookProductTransfer, Map)}
 * to find out what it does.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="LocalStorekeeperDelegate"
 *		detachable="true"
 *		table="JFireTrade_DefaultLocalStorekeeperDelegate"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DefaultLocalStorekeeperDelegate extends LocalStorekeeperDelegate
{
	private static LocalStorekeeperDelegateID localStorekeeperDelegateID = null;

	public static DefaultLocalStorekeeperDelegate getDefaultLocalStorekeeperDelegate(PersistenceManager pm)
	{
		String organisationID = null;
		pm.getExtent(DefaultLocalStorekeeperDelegate.class);
		try {
			if (localStorekeeperDelegateID == null) {
				if (organisationID == null)
					organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

				localStorekeeperDelegateID = LocalStorekeeperDelegateID.create(
						organisationID,
						DefaultLocalStorekeeperDelegate.class.getName());
			}
			return (DefaultLocalStorekeeperDelegate) pm.getObjectById(localStorekeeperDelegateID);
		} catch (JDOObjectNotFoundException x) {
			if (organisationID == null)
				organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

			DefaultLocalStorekeeperDelegate delegate = new DefaultLocalStorekeeperDelegate(
					organisationID,
					DefaultLocalStorekeeperDelegate.class.getName());
			pm.makePersistent(delegate);
			localStorekeeperDelegateID = (LocalStorekeeperDelegateID) JDOHelper.getObjectId(delegate);
			return delegate;
		}
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected DefaultLocalStorekeeperDelegate() { }

	public DefaultLocalStorekeeperDelegate(String organisationID,
			String localStorekeeperDelegateID)
	{
		super(organisationID, localStorekeeperDelegateID);
	}

	/**
	 * Manages {@link Map}s with the following structure:<br/>
	 * Map articleGroups {<br/>
	 *		key: ProductType productType<br/>
	 *		value: LinkedList&lt;Product&gt; products
	 * }
	 */
	private static ThreadLocal productsByProductTypeTL = new ThreadLocal() {
		protected Object initialValue()
		{
			return new HashMap();
		}
	};

//	/**
//	 * Manages {@link Map}s with the following structure:<br/>
//	 * Map articleGroups {<br/>
//	 *		key: Class productTypeClass<br/>
//	 *		value: LinkedList&lt;Product&gt; products
//	 * }
//	 */
//	private static ThreadLocal productsByProductTypeClassTL = new ThreadLocal() {
//		protected Object initialValue()
//		{
//			return new HashMap();
//		}
//	};
//
//	/**
//	 * Manages {@link Map}s with the following structure:<br/>
//	 * Map articleGroups {<br/>
//	 *		key: Repository sourceRepository<br/>
//	 *		value: LinkedList&lt;Product&gt; products
//	 * }
//	 */
//	private static ThreadLocal productsBySourceRepositoryTL = new ThreadLocal() {
//		protected Object initialValue()
//		{
//			return new HashMap();
//		}
//	};

	public void preBookArticles(OrganisationLegalEntity mandator, User user, DeliveryNote deliveryNote, BookProductTransfer bookTransfer, Map involvedAnchors)
	{
		Map productsByProductTypeClass = (Map) productsByProductTypeTL.get();
		productsByProductTypeClass.clear();

//		if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_TO) {
//			Map productsByProductTypeClass = (Map) productsByProductTypeClassTL.get();
//			productsByProductTypeClass.clear();
//		}
//		else if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_FROM) {
//			Map productsBySourceRepository = (Map) productsBySourceRepositoryTL.get();
//			productsBySourceRepository.clear();
//		}
//		else
//			throw new IllegalStateException("mandator is neither 'from' nor 'to' of bookTransfer!");
	}

//	/**
//	 * There are two different cases:
//	 * <p>
//	 * In the first case, the mandator receives {@link Article}s - either 'normal' articles as a customer or
//	 * reversing articles as a vendor - and the delegate needs to send them to the repositories.
//	 * Hence, this method groups all {@link Article}s by {@link org.nightlabs.jfire.store.ProductType}.
//	 * As this implementation of <code>LocalStorekeeperDelegate</code> manages one
//	 * {@link org.nightlabs.jfire.store.Repository} per <code>ProductType</code>,
//	 * the method {@link #postBookArticles(OrganisationLegalEntity, User, DeliveryNote, BookProductTransfer, Map)} then
//	 * creates one {@link org.nightlabs.jfire.store.ProductTransfer} per <code>ProductType</code>.
//	 * </p>
//	 * <p>
//	 * In the second case, the mandator has to send {@link Article}s. This will cause a grouping
//	 * by source-repository.
//	 * </p>
//	 *
//	 * @see org.nightlabs.jfire.store.book.LocalStorekeeperDelegate#bookArticle(org.nightlabs.jfire.trade.OrganisationLegalEntity, org.nightlabs.jfire.security.User, org.nightlabs.jfire.store.DeliveryNote, org.nightlabs.jfire.trade.Article, org.nightlabs.jfire.store.book.BookProductTransfer, java.util.Map)
//	 */

	/**
	 * This method does not yet book the <code>article</code>, but only groups all products by their
	 * {@link org.nightlabs.jfire.store.ProductType}. The actual booking is done by
	 * {@link #postBookArticles(OrganisationLegalEntity, User, DeliveryNote, BookProductTransfer, Map)}.
	 *
	 * @see org.nightlabs.jfire.store.book.LocalStorekeeperDelegate#bookArticle(org.nightlabs.jfire.trade.OrganisationLegalEntity, org.nightlabs.jfire.security.User, org.nightlabs.jfire.store.DeliveryNote, org.nightlabs.jfire.trade.Article, org.nightlabs.jfire.store.book.BookProductTransfer, java.util.Map)
	 */
	public void bookArticle(OrganisationLegalEntity mandator, User user,
			DeliveryNote deliveryNote, Article article,
			BookProductTransfer bookTransfer, Map involvedAnchors)
	{
		Map productsByProductType = (Map) productsByProductTypeTL.get();
		ProductType productType = article.getProductType();
		LinkedList products = (LinkedList) productsByProductType.get(productType);
		if (products == null) {
			products = new LinkedList();
			productsByProductType.put(productType, products);
		}
		products.add(article.getProduct());

//		if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_TO) {
//			Map productsByProductTypeClass = (Map) productsByProductTypeClassTL.get();
//			Class productTypeClass = article.getProductType().getClass();
//			LinkedList products = (LinkedList) productsByProductTypeClass.get(productTypeClass);
//			if (products == null) {
//				products = new LinkedList();
//				productsByProductTypeClass.put(productTypeClass, products);
//			}
//			products.add(article.getProduct());
//		}
//		else if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_FROM) {
//			Product product = article.getProduct();
//			ProductLocal productLocal = product.getProductLocal();
//			if (productLocal == null)
//				throw new IllegalStateException("Product \"" + product.getPrimaryKey() + "\" has no ProductLocal instance assigned!");
//
//			Repository sourceRepository = (Repository)productLocal.getAnchor();
//			Map productsBySourceRepository = (Map) productsBySourceRepositoryTL.get();
//			LinkedList products = (LinkedList) productsBySourceRepository.get(sourceRepository);
//			if (products == null) {
//				products = new LinkedList();
//				productsBySourceRepository.put(sourceRepository, products);
//			}
//			products.add(product);
//		}
//		else
//			throw new IllegalStateException("mandator is neither 'from' nor 'to' of bookTransfer!");
	}

	/**
	 * This method generates one {@link ProductTransfer} per {@link org.nightlabs.jfire.store.ProductType}
	 * for the groups of <code>Product</code>s that have been created by
	 * {@link #bookArticle(OrganisationLegalEntity, User, DeliveryNote, Article, BookProductTransfer, Map)}
	 * before.
	 *
	 * @see org.nightlabs.jfire.store.book.LocalStorekeeperDelegate#postBookArticles(org.nightlabs.jfire.trade.OrganisationLegalEntity, org.nightlabs.jfire.security.User, org.nightlabs.jfire.store.DeliveryNote, org.nightlabs.jfire.store.book.BookProductTransfer, java.util.Map)
	 */
	public void postBookArticles(OrganisationLegalEntity mandator, User user, DeliveryNote deliveryNote, BookProductTransfer bookTransfer, Map involvedAnchors)
	{
		Store store = getStore();

		Map productsByProductType = (Map) productsByProductTypeTL.get();
		for (Iterator it = productsByProductType.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry me = (Map.Entry) it.next();
			ProductType productType = (ProductType) me.getKey();
			LinkedList products = (LinkedList) me.getValue();

			// get the home of the producttype
			Anchor home = productType.getProductTypeLocal().getHome();

			// transfer products
			ProductTransfer productTransfer;
			if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_TO)
				productTransfer = new ProductTransfer(store, bookTransfer, user, mandator, home, products);
			else if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_FROM)
				productTransfer = new ProductTransfer(store, bookTransfer, user, home, mandator, products);
			else
				throw new IllegalStateException("mandator is neither 'from' nor 'to' of bookTransfer!");

			productTransfer.bookTransfer(user, involvedAnchors);
		}

//		if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_TO) {
//			PersistenceManager pm = getPersistenceManager();
//			Map productsByProductTypeClass = (Map) productsByProductTypeClassTL.get();
//			for (Iterator it = productsByProductTypeClass.entrySet().iterator(); it.hasNext(); ) {
//				Map.Entry me = (Map.Entry) it.next();
//				Class productTypeClass = (Class) me.getKey();
//				LinkedList products = (LinkedList) me.getValue();
//
//				// find or create the destination repository
//				Repository repository = Repository.createRepository(
//						pm, getOrganisationID(), Repository.ANCHOR_TYPE_ID_BIN, productTypeClass.getName(), mandator, false);
//
//				// transfer products
//				ProductTransfer productTransfer = new ProductTransfer(store, bookTransfer, user, mandator, repository, products);
////				store.addTransfer(productTransfer);
//				productTransfer.bookTransfer(user, involvedAnchors);
//			}
//		}
//		else if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_FROM) {
//			Map productsBySourceRepository = (Map) productsBySourceRepositoryTL.get();
//			for (Iterator it = productsBySourceRepository.entrySet().iterator(); it.hasNext(); ) {
//				Map.Entry me = (Map.Entry) it.next();
//				Repository sourceRepository = (Repository) me.getKey();
//				LinkedList products = (LinkedList) me.getValue();
//
//				// transfer products
//				ProductTransfer productTransfer = new ProductTransfer(store, bookTransfer, user, sourceRepository, mandator, products);
////				store.addTransfer(productTransfer);
//				productTransfer.bookTransfer(user, involvedAnchors);
//			}
//		}
//		else
//			throw new IllegalStateException("mandator is neither 'from' nor 'to' of bookTransfer!");
	}
}
