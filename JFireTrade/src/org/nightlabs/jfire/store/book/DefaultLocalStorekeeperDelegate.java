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

package org.nightlabs.jfire.store.book;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.RepositoryType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.book.id.LocalStorekeeperDelegateID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

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
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class DefaultLocalStorekeeperDelegate extends LocalStorekeeperDelegate
{
	private static final long serialVersionUID = 1L;

	public static DefaultLocalStorekeeperDelegate getDefaultLocalStorekeeperDelegate(PersistenceManager pm)
	{
		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

		// TODO remove this debug stuff
		{
			String securityReflectorOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			if (!securityReflectorOrganisationID.equals(organisationID))
				throw new IllegalStateException("SecurityReflector returned organisationID " + securityReflectorOrganisationID + " but LocalOrganisation.organisationID=" + organisationID);
		}
		// TODO end debug

		LocalStorekeeperDelegateID localStorekeeperDelegateID = LocalStorekeeperDelegateID.create(
					organisationID,
					DefaultLocalStorekeeperDelegate.class.getName()
		);

		pm.getExtent(DefaultLocalStorekeeperDelegate.class);
		try {
			return (DefaultLocalStorekeeperDelegate) pm.getObjectById(localStorekeeperDelegateID);
		} catch (JDOObjectNotFoundException x) {
			DefaultLocalStorekeeperDelegate delegate = new DefaultLocalStorekeeperDelegate(
					localStorekeeperDelegateID.organisationID,
					localStorekeeperDelegateID.localStorekeeperDelegateID
			);
			return pm.makePersistent(delegate);
		}
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DefaultLocalStorekeeperDelegate() { }

	public DefaultLocalStorekeeperDelegate(String organisationID, String localStorekeeperDelegateID)
	{
		super(organisationID, localStorekeeperDelegateID);
	}

	private static ThreadLocal<Map<String,Map<Repository,List<Product>>>> organisationID2repository2productsTL = new ThreadLocal<Map<String,Map<Repository,List<Product>>>>() {
		@Override
		protected Map<String,Map<Repository,List<Product>>> initialValue()
		{
			return new HashMap<String, Map<Repository,List<Product>>>();
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

	@Override
	public void preBookArticles(OrganisationLegalEntity mandator, User user, DeliveryNote deliveryNote, BookProductTransfer bookTransfer, Set<Anchor> involvedAnchors)
	{
		Map<String,Map<Repository,List<Product>>> organisationID2repository2products = organisationID2repository2productsTL.get();
		Map<Repository,List<Product>> repository2products = organisationID2repository2products.get(IDGenerator.getOrganisationID());
		if (repository2products != null)
			repository2products.clear();
	}

	/**
	 * This method does not yet book the <code>article</code>, but only groups all products by their
	 * {@link org.nightlabs.jfire.store.ProductType}. The actual booking is done by
	 * {@link #postBookArticles(OrganisationLegalEntity, User, DeliveryNote, BookProductTransfer, Map)}.
	 *
	 * @see org.nightlabs.jfire.store.book.LocalStorekeeperDelegate#bookArticle(org.nightlabs.jfire.trade.OrganisationLegalEntity, org.nightlabs.jfire.security.User, org.nightlabs.jfire.store.DeliveryNote, org.nightlabs.jfire.trade.Article, org.nightlabs.jfire.store.book.BookProductTransfer, java.util.Map)
	 */
	@Override
	public void bookArticle(OrganisationLegalEntity mandator, User user,
			DeliveryNote deliveryNote, Article article,
			BookProductTransfer bookTransfer, Set<Anchor> involvedAnchors)
	{
		Repository repository = getRepositoryForBooking(article.getProduct());
		
		Map<String,Map<Repository,List<Product>>> organisationID2repository2products = organisationID2repository2productsTL.get();
		Map<Repository,List<Product>> repository2products = organisationID2repository2products.get(IDGenerator.getOrganisationID());
		if (repository2products == null) {
			repository2products = new HashMap<Repository, List<Product>>();
			organisationID2repository2products.put(IDGenerator.getOrganisationID(), repository2products);
		}

		List<Product> products = repository2products.get(repository);
		if (products == null) {
			products = new LinkedList<Product>();
			repository2products.put(repository, products);
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
	 * <p>
	 * The products are transferred
	 * </p>
	 *
	 * @see org.nightlabs.jfire.store.book.LocalStorekeeperDelegate#postBookArticles(org.nightlabs.jfire.trade.OrganisationLegalEntity, org.nightlabs.jfire.security.User, org.nightlabs.jfire.store.DeliveryNote, org.nightlabs.jfire.store.book.BookProductTransfer, java.util.Map)
	 */
	@Override
	public void postBookArticles(OrganisationLegalEntity mandator, User user, DeliveryNote deliveryNote, BookProductTransfer bookTransfer, Set<Anchor> involvedAnchors)
	{
		Map<String,Map<Repository,List<Product>>> organisationID2repository2products = organisationID2repository2productsTL.get();
		Map<Repository,List<Product>> repository2products = organisationID2repository2products.get(IDGenerator.getOrganisationID());
		if (repository2products != null) {
			for (Map.Entry<Repository,List<Product>> me : repository2products.entrySet()) {
				Repository repository = me.getKey();
				List<Product> products = me.getValue();

				// transfer products
				ProductTransfer productTransfer;
				if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_TO)
					productTransfer = new ProductTransfer(bookTransfer, user, mandator, repository, products);
				else if (bookTransfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_FROM)
					productTransfer = new ProductTransfer(bookTransfer, user, repository, mandator, products);
				else
					throw new IllegalStateException("mandator is neither 'from' nor 'to' of bookTransfer!");

				productTransfer = getPersistenceManager().makePersistent(productTransfer);
				productTransfer.bookTransfer(user, involvedAnchors);
			}
		} // if (productType2products != null) {

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

	@Override
	public Repository getInitialRepositoryForLocalProduct(Product product)
	{
		if (!getStore().getMandator().equals(product.getProductType().getVendor()))
			throw new IllegalStateException("The product is a foreign product! This should never happen, since Store should call getPartnerStorekeeper().getInitialForeignRepository(...)!");

		return getHomeRepository(product);
	}

	protected String getAnchorIDForLocalHomeRepository(ProductType productType)
	{
		return RepositoryType.ANCHOR_TYPE_ID_PREFIX_HOME + productType.getClass().getName();
	}

	protected String getAnchorIDForForeignHomeRepository(ProductType productType)
	{
		return RepositoryType.ANCHOR_TYPE_ID_PREFIX_HOME + productType.getClass().getName() + '#' + productType.getOrganisationID();
	}

	/**
	 * Get the "home" repository of a product. This is the repository where instances of the product are usually kept
	 * while they are in the local organisation.
	 */
	@Override
	public Repository getHomeRepository(Product product)
	{
		ProductType productType = product.getProductType();
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);

		RepositoryType repositoryType = (RepositoryType) pm.getObjectById(RepositoryType.REPOSITORY_TYPE_ID_HOME);

		return Repository.createRepository(
				pm,
				store.getOrganisationID(),
				(
						store.getOrganisationID().equals(productType.getOrganisationID()) ?
								getAnchorIDForLocalHomeRepository(productType) : getAnchorIDForForeignHomeRepository(productType)
				),
				repositoryType,
				store.getMandator());
	}

	/**
	 * This method is called internally by {@link #bookArticle(OrganisationLegalEntity, User, DeliveryNote, Article, BookProductTransfer, Set)}
	 * in order to find out from/to which repository a transfer needs to be created
	 * (with the mandator - the local {@link OrganisationLegalEntity} - on the other end of the transfer).
	 *
	 * @param product the product for which to find out
	 * @return
	 */
	protected Repository getRepositoryForBooking(Product product)
	{
		return getHomeRepository(product);
//		if (getStore().getOrganisationID().equals(product.getOrganisationID()))
//			return getInitialRepositoryForLocalProduct(product);
//		else
//			return getStore().getPartnerStorekeeper().getInitialRepositoryForForeignProduct(product);
	}
}
