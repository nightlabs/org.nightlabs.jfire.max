package org.nightlabs.jfire.dynamictrade.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductTypeActionHandler"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicProductTypeActionHandler"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class DynamicProductTypeActionHandler
		extends ProductTypeActionHandler
{
	public static final AuthorityTypeID AUTHORITY_TYPE_ID = AuthorityTypeID.create(Organisation.DEV_ORGANISATION_ID, DynamicProductType.class.getName());

//	/**
//	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
//	 * the {@link Repository} which becomes the factory-output-repository for all
//	 * newly created {@link DynamicProduct}s.
//	 */
//	public static final String ANCHOR_ID_REPOSITORY_HOME_LOCAL = DynamicProductType.class.getName() + ".local";
//
////	/**
////	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
////	 * the {@link Repository} which is used for products that are bought from a foreign organisation.
////	 */
////	public static final String ANCHOR_ID_REPOSITORY_HOME_FOREIGN = DynamicProductType.class.getName() + ".foreign";
//
//	public static Repository getDefaultHome(PersistenceManager pm, DynamicProductType simpleProductType)
//	{
//		Store store = Store.getStore(pm);
//		if (store.getOrganisationID().equals(simpleProductType.getOrganisationID()))
//			return getDefaultLocalHome(pm, store);
//		else
//			throw new UnsupportedOperationException("DynamicTrade does not support cross-trade-functionality!");
////			return getDefaultForeignHome(pm, store);
//	}
//
//	protected static Repository getDefaultLocalHome(PersistenceManager pm, Store store)
//	{
//		return Repository.createRepository(
//				pm,
//				store.getOrganisationID(),
//				Repository.ANCHOR_TYPE_ID_HOME,
//				ANCHOR_ID_REPOSITORY_HOME_LOCAL,
//				store.getMandator(), false);
//	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicProductTypeActionHandler() { }

	/**
	 * @see ProductTypeActionHandler#ProductTypeActionHandler(String, String, Class)
	 */
	public DynamicProductTypeActionHandler(String organisationID,
			String productTypeActionHandlerID, Class<? extends ProductType> productTypeClass)
	{
		super(organisationID, productTypeActionHandlerID, productTypeClass);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Implement
	public Collection<? extends Product> findProducts(User user,
			ProductType productType, NestedProductTypeLocal nestedProductTypeLocal, ProductLocator productLocator)
	{
		DynamicProductType spt = (DynamicProductType) productType;
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);
		ArrayList res = new ArrayList();
		int qty = nestedProductTypeLocal == null ? 1 : nestedProductTypeLocal.getQuantity();
		for (int i = 0; i < qty; ++i) {
			DynamicProduct product = new DynamicProduct(spt, Product.createProductID());
			product = (DynamicProduct) store.addProduct(user, product); // , (Repository)spt.getProductTypeLocal().getHome());
			res.add(product);
		}
		return res;
//		DynamicProductType spt = (DynamicProductType) productType;
//		DynamicProductTypeLocal sptl = (DynamicProductTypeLocal) productType.getProductTypeLocal();
//		int qty = nestedProductTypeLocal == null ? 1 : nestedProductTypeLocal.getQuantity();
//		PersistenceManager pm = getPersistenceManager();
//
//		Store store = Store.getStore(pm);
//		// search for an available product
//		Query q = pm.newQuery(DynamicProduct.class);
//		q.setFilter("productType == pProductType && productLocal.available");
//		q.declareParameters("DynamicProductType pProductType");
//		q.declareImports("import " + DynamicProductType.class.getName());
//		Collection availableProducts = (Collection) q.execute(this); // Product.getProducts(pm, this, ProductStatus.STATUS_AVAILABLE);
//		ArrayList res = new ArrayList();
//		Iterator iteratorAvailableProducts = availableProducts.iterator();
//		for (int i = 0; i < qty; ++i) {
//			DynamicProduct product = null;
//			if (iteratorAvailableProducts.hasNext()) {
//				product = (DynamicProduct) iteratorAvailableProducts.next();
//				res.add(product);
//			}
//			else {
//				// create products only if this product type is ours
//				if (productType.getOrganisationID().equals(store.getOrganisationID())) {
//					long createdProductCount = sptl.getCreatedProductCount();
//					if (sptl.getMaxProductCount() < 0 || createdProductCount + 1 <= sptl.getMaxProductCount()) {
//						product = new DynamicProduct(spt, DynamicProduct.createProductID());
//						sptl.setCreatedProductCount(createdProductCount + 1);
//
//						product = (DynamicProduct) store.addProduct(user, product, (Repository)spt.getProductTypeLocal().getHome());
//						res.add(product);
//					}
//				} // This productType is factored by this organisation
//				else
//					throw new UnsupportedOperationException("NYI");
//			}
//		}
//		return res;
	}


	@Override
	@Implement
	protected Collection<? extends Article> createCrossTradeArticles(
			User user, Product localPackageProduct, Article localArticle,
			String partnerOrganisationID, Hashtable<?, ?> partnerInitialContextProperties,
			Offer partnerOffer, OfferID partnerOfferID, SegmentID partnerSegmentID,
			ProductType nestedProductType, Collection<NestedProductTypeLocal> nestedProductTypeLocals) throws Exception
	{
		throw new UnsupportedOperationException("DynamicProductTypes do not support cross-trade!");
	}

	@Override
	public AuthorityTypeID getAuthorityTypeID(ProductType rootProductType) {
		return AUTHORITY_TYPE_ID;
	}

	@Override
	protected AuthorityType createAuthorityType(AuthorityTypeID authorityTypeID, ProductType rootProductType) {
		AuthorityType authorityType = new AuthorityType(authorityTypeID);
		// TODO configure access rights completely - implement manual checking where necessary!
		return authorityType;
	}
}
