package org.nightlabs.jfire.swifttrade.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductTypeActionHandler"
 *		detachable="true"
 *		table="JFireSwiftTrade_SwiftProductTypeActionHandler"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class SwiftProductTypeActionHandler
		extends ProductTypeActionHandler
{
	/**
	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
	 * the {@link Repository} which becomes the factory-output-repository for all
	 * newly created {@link SwiftProduct}s.
	 */
	public static final String ANCHOR_ID_REPOSITORY_HOME_LOCAL = SwiftProductType.class.getName() + ".local";

//	/**
//	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
//	 * the {@link Repository} which is used for products that are bought from a foreign organisation.
//	 */
//	public static final String ANCHOR_ID_REPOSITORY_HOME_FOREIGN = SwiftProductType.class.getName() + ".foreign";

	public static Repository getDefaultHome(PersistenceManager pm, SwiftProductType simpleProductType)
	{
		Store store = Store.getStore(pm);
		if (store.getOrganisationID().equals(simpleProductType.getOrganisationID()))
			return getDefaultLocalHome(pm, store);
		else
			throw new UnsupportedOperationException("SwiftTrade does not support cross-trade-functionality!");
//			return getDefaultForeignHome(pm, store);
	}

	private static AnchorID localHomeID = null;

	protected static Repository getDefaultLocalHome(PersistenceManager pm, Store store)
	{
		if (localHomeID == null) {
			localHomeID = AnchorID.create(
					store.getOrganisationID(),
					Repository.ANCHOR_TYPE_ID_HOME,
					ANCHOR_ID_REPOSITORY_HOME_LOCAL);
		}

		pm.getExtent(Repository.class);
		Repository home;
		try {
			home = (Repository) pm.getObjectById(localHomeID);
		} catch (JDOObjectNotFoundException x) {
			home = new Repository(
					localHomeID.organisationID,
					localHomeID.anchorTypeID,
					localHomeID.anchorID,
					store.getMandator(), false);

			pm.makePersistent(home);
		}

		return home;
	}

	private static AnchorID foreignHomeID = null;

//	protected static Repository getDefaultForeignHome(PersistenceManager pm, Store store)
//	{
//		if (foreignHomeID == null) {
//			foreignHomeID = AnchorID.create(
//					store.getOrganisationID(),
//					Repository.ANCHOR_TYPE_ID_HOME,
//					ANCHOR_ID_REPOSITORY_HOME_FOREIGN);
//		}
//
//		pm.getExtent(Repository.class);
//		Repository home;
//		try {
//			home = (Repository) pm.getObjectById(foreignHomeID);
//		} catch (JDOObjectNotFoundException x) {
//			home = new Repository(
//					foreignHomeID.organisationID,
//					foreignHomeID.anchorTypeID,
//					foreignHomeID.anchorID,
//					store.getMandator(), false);
//
//			pm.makePersistent(home);
//		}
//
//		return home;
//	}


	/**
	 * @deprecated Only for JDO!
	 */
	protected SwiftProductTypeActionHandler() { }

	/**
	 * @see ProductTypeActionHandler#ProductTypeActionHandler(String, String, Class)
	 */
	public SwiftProductTypeActionHandler(String organisationID,
			String productTypeActionHandlerID, Class productTypeClass)
	{
		super(organisationID, productTypeActionHandlerID, productTypeClass);
	}

	@SuppressWarnings("unchecked")
	@Implement
	public Collection<? extends Product> findProducts(User user,
			ProductType productType, NestedProductType nestedProductType, ProductLocator productLocator)
	{
		SwiftProductType spt = (SwiftProductType) productType;
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);
		ArrayList res = new ArrayList();
		int qty = nestedProductType == null ? 1 : nestedProductType.getQuantity();
		for (int i = 0; i < qty; ++i) {
			SwiftProduct product = new SwiftProduct(spt, SwiftProduct.createProductID());
			product = (SwiftProduct) store.addProduct(user, product, (Repository)spt.getProductTypeLocal().getHome());
			res.add(product);
		}
		return res;
//		SwiftProductType spt = (SwiftProductType) productType;
//		SwiftProductTypeLocal sptl = (SwiftProductTypeLocal) productType.getProductTypeLocal();
//		int qty = nestedProductType == null ? 1 : nestedProductType.getQuantity();
//		PersistenceManager pm = getPersistenceManager();
//
//		Store store = Store.getStore(pm);
//		// search for an available product
//		Query q = pm.newQuery(SwiftProduct.class);
//		q.setFilter("productType == pProductType && productLocal.available");
//		q.declareParameters("SwiftProductType pProductType");
//		q.declareImports("import " + SwiftProductType.class.getName());
//		Collection availableProducts = (Collection) q.execute(this); // Product.getProducts(pm, this, ProductStatus.STATUS_AVAILABLE);
//		ArrayList res = new ArrayList();
//		Iterator iteratorAvailableProducts = availableProducts.iterator();
//		for (int i = 0; i < qty; ++i) {
//			SwiftProduct product = null;
//			if (iteratorAvailableProducts.hasNext()) {
//				product = (SwiftProduct) iteratorAvailableProducts.next();
//				res.add(product);
//			}
//			else {
//				// create products only if this product type is ours
//				if (productType.getOrganisationID().equals(store.getOrganisationID())) {
//					long createdProductCount = sptl.getCreatedProductCount();
//					if (sptl.getMaxProductCount() < 0 || createdProductCount + 1 <= sptl.getMaxProductCount()) {
//						product = new SwiftProduct(spt, SwiftProduct.createProductID());
//						sptl.setCreatedProductCount(createdProductCount + 1);
//
//						product = (SwiftProduct) store.addProduct(user, product, (Repository)spt.getProductTypeLocal().getHome());
//						res.add(product);
//					}
//				} // This productType is factored by this organisation
//				else
//					throw new UnsupportedOperationException("NYI");
//			}
//		}
//		return res;
	}


	@Implement
	public Collection<? extends Article> createCrossTradeArticles(
			User user, Product localPackageProduct, Article localArticle,
			String partnerOrganisationID, Hashtable partnerInitialContextProperties,
			Offer partnerOffer, OfferID partnerOfferID, SegmentID partnerSegmentID,
			ProductType nestedProductType, Collection<NestedProductType> nestedProductTypes) throws Exception
	{
		throw new UnsupportedOperationException("SwiftProductTypes do not support cross-trade!");
	}

}
