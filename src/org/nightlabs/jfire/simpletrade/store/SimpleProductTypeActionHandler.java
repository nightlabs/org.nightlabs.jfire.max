package org.nightlabs.jfire.simpletrade.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.TariffMapping;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
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
 *		table="JFireSimpleTrade_SimpleProductTypeActionHandler"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class SimpleProductTypeActionHandler
		extends ProductTypeActionHandler
{
	/**
	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
	 * the {@link Repository} which becomes the factory-output-repository for all
	 * newly created {@link SimpleProduct}s.
	 */
	public static final String ANCHOR_ID_REPOSITORY_HOME_LOCAL = SimpleProductType.class.getName() + ".local";

	/**
	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
	 * the {@link Repository} which is used for products that are bought from a foreign organisation.
	 */
	public static final String ANCHOR_ID_REPOSITORY_HOME_FOREIGN = SimpleProductType.class.getName() + ".foreign";

	public static Repository getDefaultHome(PersistenceManager pm, SimpleProductType simpleProductType)
	{
		Store store = Store.getStore(pm);
		if (store.getOrganisationID().equals(simpleProductType.getOrganisationID()))
			return getDefaultLocalHome(pm, store);
		else
			return getDefaultForeignHome(pm, store);
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

	protected static Repository getDefaultForeignHome(PersistenceManager pm, Store store)
	{
		if (foreignHomeID == null) {
			foreignHomeID = AnchorID.create(
					store.getOrganisationID(),
					Repository.ANCHOR_TYPE_ID_HOME,
					ANCHOR_ID_REPOSITORY_HOME_FOREIGN);
		}

		pm.getExtent(Repository.class);
		Repository home;
		try {
			home = (Repository) pm.getObjectById(foreignHomeID);
		} catch (JDOObjectNotFoundException x) {
			home = new Repository(
					foreignHomeID.organisationID,
					foreignHomeID.anchorTypeID,
					foreignHomeID.anchorID,
					store.getMandator(), false);

			pm.makePersistent(home);
		}

		return home;
	}


	/**
	 * @deprecated Only for JDO!
	 */
	protected SimpleProductTypeActionHandler() { }

	/**
	 * @see ProductTypeActionHandler#ProductTypeActionHandler(String, String, Class)
	 */
	public SimpleProductTypeActionHandler(String organisationID,
			String productTypeActionHandlerID, Class productTypeClass)
	{
		super(organisationID, productTypeActionHandlerID, productTypeClass);
	}

	@SuppressWarnings("unchecked")
	@Implement
	public Collection<? extends Product> findProducts(User user,
			ProductType productType, NestedProductType nestedProductType, ProductLocator productLocator)
	{
		SimpleProductType spt = (SimpleProductType) productType;
		SimpleProductTypeLocal sptl = (SimpleProductTypeLocal) productType.getProductTypeLocal();
		int qty = nestedProductType == null ? 1 : nestedProductType.getQuantity();
		PersistenceManager pm = getPersistenceManager();

		Store store = Store.getStore(pm);
		// search for an available product
		Query q = pm.newQuery(SimpleProduct.class);
		q.setFilter("productType == pProductType && productLocal.available");
		q.declareParameters("SimpleProductType pProductType");
		q.declareImports("import " + SimpleProductType.class.getName());
		Collection availableProducts = (Collection) q.execute(this); // Product.getProducts(pm, this, ProductStatus.STATUS_AVAILABLE);
		ArrayList res = new ArrayList();
		Iterator iteratorAvailableProducts = availableProducts.iterator();
		for (int i = 0; i < qty; ++i) {
			SimpleProduct product = null;
			if (iteratorAvailableProducts.hasNext()) {
				product = (SimpleProduct) iteratorAvailableProducts.next();
				res.add(product);
			}
			else {
				// create products only if this product type is ours
				if (productType.getOrganisationID().equals(store.getOrganisationID())) {
					long createdProductCount = sptl.getCreatedProductCount();
					if (sptl.getMaxProductCount() < 0 || createdProductCount + 1 <= sptl.getMaxProductCount()) {
						product = new SimpleProduct(spt, SimpleProduct.createProductID());
						sptl.setCreatedProductCount(createdProductCount + 1);

						product = (SimpleProduct) store.addProduct(user, product, (Repository)spt.getProductTypeLocal().getHome());
						res.add(product);
					}
				} // This productType is factored by this organisation
				else
					throw new UnsupportedOperationException("NYI");
			}
		}
		return res;
	}


	@Implement
	public Collection<? extends Article> createCrossTradeArticles(
			User user, Product localPackageProduct, Article localArticle,
			String partnerOrganisationID, Hashtable partnerInitialContextProperties,
			Offer partnerOffer, OfferID partnerOfferID, SegmentID partnerSegmentID,
			ProductType nestedProductType, Collection<NestedProductType> nestedProductTypes) throws Exception
	{
		SimpleTradeManager stm = SimpleTradeManagerUtil.getHome(partnerInitialContextProperties).create();
		int qty = 0;
		for (NestedProductType npt : nestedProductTypes) {
			qty += npt.getQuantity();
		}

		TariffID localTariffID = (TariffID) JDOHelper.getObjectId(localArticle.getTariff());
		TariffMapping tariffMapping = TariffMapping.getTariffMappingForLocalTariffAndPartner(getPersistenceManager(), localTariffID, partnerOrganisationID);
		if (tariffMapping == null)
			throw new IllegalStateException("No TariffMapping existing for localTariff \"" + localArticle.getTariff().getPrimaryKey() + "\" and partnerOrganisationID \"" + partnerOrganisationID + "\"!");

		return stm.createArticles(
				partnerSegmentID, partnerOfferID,
				(ProductTypeID)JDOHelper.getObjectId(nestedProductType),
				qty, tariffMapping.getPartnerTariffID(), true, true,
				new String[] {
					FetchPlan.DEFAULT, Article.FETCH_GROUP_OFFER, Article.FETCH_GROUP_ORDER, Article.FETCH_GROUP_PRICE,
					Article.FETCH_GROUP_PRODUCT, Article.FETCH_GROUP_PRODUCT_TYPE, Article.FETCH_GROUP_SEGMENT, ArticlePrice.FETCH_GROUP_CURRENCY,
					ArticlePrice.FETCH_GROUP_FRAGMENTS, ArticlePrice.FETCH_GROUP_PACKAGE_ARTICLE_PRICE
				}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
	}


//	@Implement
//	public Collection<? extends Article> createCrossTradeArticles(User user, ProductType productType, int quantity, ProductLocator productLocators)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		Store store = Store.getStore(pm);
//		Trader trader = Trader.getTrader(pm);
//		NestedProductType pseudoNestedPT = null;
//		if (quantity != 1)
//			pseudoNestedPT = new NestedProductType(null, productType, quantity);
//
//		Collection products = store.findProducts(user, productType, pseudoNestedPT, null);
//
//		Collection articles = trader.createArticles(
//				user, offer, segment,
//				products,
//				new ArticleCreator(tariff),
//				true, false, true);
//
//		return null;
//	}

}
