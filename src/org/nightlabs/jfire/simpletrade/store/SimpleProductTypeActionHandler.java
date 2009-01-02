package org.nightlabs.jfire.simpletrade.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.TariffMapping;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.util.CollectionUtil;

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
	public static final AuthorityTypeID AUTHORITY_TYPE_ID = AuthorityTypeID.create(SimpleProductType.class.getName());

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SimpleProductTypeActionHandler() { }

	/**
	 * @see ProductTypeActionHandler#ProductTypeActionHandler(String, String, Class)
	 */
	public SimpleProductTypeActionHandler(String organisationID,
			String productTypeActionHandlerID, Class<? extends SimpleProductType> productTypeClass)
	{
		super(organisationID, productTypeActionHandlerID, productTypeClass);
	}

	@Override
	public Collection<? extends Product> findProducts(User user,
			ProductType productType, NestedProductTypeLocal nestedProductTypeLocal, ProductLocator productLocator)
	{
		SimpleProductType spt = (SimpleProductType) productType;
		SimpleProductTypeLocal sptl = (SimpleProductTypeLocal) productType.getProductTypeLocal();
		int qty = nestedProductTypeLocal == null ? 1 : nestedProductTypeLocal.getQuantity();
		PersistenceManager pm = getPersistenceManager();

		Store store = Store.getStore(pm);
		// search for an available product
		Query q = pm.newQuery(SimpleProduct.class);
		q.setFilter("this.productType == :productType && this.productLocal.available");
		Collection<? extends SimpleProduct> availableProducts = CollectionUtil.castCollection((Collection<?>)q.execute(productType));
		List<SimpleProduct> res = new ArrayList<SimpleProduct>();
		Iterator<? extends SimpleProduct> iteratorAvailableProducts = availableProducts.iterator();
		for (int i = 0; i < qty; ++i) {
			SimpleProduct product = null;
			if (iteratorAvailableProducts.hasNext()) {
				product = iteratorAvailableProducts.next();
				res.add(product);
			}
			else {
				// create products only if this product type is ours
				if (productType.getOrganisationID().equals(store.getOrganisationID())) {
					long createdProductCount = sptl.getCreatedProductCount();
					if (sptl.getMaxProductCount() < 0 || createdProductCount + 1 <= sptl.getMaxProductCount()) {
						product = new SimpleProduct(spt, Product.createProductID());
						sptl.setCreatedProductCount(createdProductCount + 1);

						product = (SimpleProduct) store.addProduct(user, product);
						res.add(product);
					}
				} // This productType is factored by this organisation
				else
					throw new UnsupportedOperationException("Cannot create foreign products! The ProductType is not owned by this organisation!");
			}
		}
		return res;
	}


	@Override
	protected Collection<? extends Article> createCrossTradeArticles(
			User user, Product localPackageProduct, Article localArticle,
			String partnerOrganisationID, Hashtable<?, ?> partnerInitialContextProperties,
			Offer partnerOffer, OfferID partnerOfferID, SegmentID partnerSegmentID,
			ProductType nestedProductType, Collection<NestedProductTypeLocal> nestedProductTypeLocals) throws Exception
	{
		SimpleTradeManager stm = JFireEjbUtil.getBean(SimpleTradeManager.class, partnerInitialContextProperties);
		int qty = 0;
		for (NestedProductTypeLocal npt : nestedProductTypeLocals) {
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
					Article.FETCH_GROUP_CURRENCY, Article.FETCH_GROUP_TARIFF,
					Article.FETCH_GROUP_PRODUCT, Article.FETCH_GROUP_PRODUCT_TYPE, Article.FETCH_GROUP_SEGMENT, Price.FETCH_GROUP_CURRENCY,
					Product.FETCH_GROUP_PRODUCT_TYPE,
					Price.FETCH_GROUP_FRAGMENTS, ArticlePrice.FETCH_GROUP_PACKAGE_ARTICLE_PRICE
				}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
	}

	@Override
	public AuthorityTypeID getAuthorityTypeID(ProductType rootProductType) {
		return AUTHORITY_TYPE_ID;
	}

//	@Override
//	protected AuthorityType createAuthorityType(AuthorityTypeID authorityTypeID, ProductType rootProductType) {
//		PersistenceManager pm = getPersistenceManager();
//		AuthorityType authorityType = new AuthorityType(authorityTypeID);
//
//		authorityType.getName().setText(Locale.ENGLISH.getLanguage(), "Simple product types");
//		authorityType.getDescription().setText(Locale.ENGLISH.getLanguage(), "Authorities of this type control the access rights for simple product types.");
//
//		authorityType.getName().setText(Locale.GERMAN.getLanguage(), "Einfache Produkttypen");
//		authorityType.getDescription().setText(Locale.GERMAN.getLanguage(), "Vollmachten dieses Typs kontrollieren den Zugriff auf einfache Produkttypen.");
//
//		// TODO configure access rights completely - implement manual checking where necessary!
////		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleConstants.seeProductType));
////		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleConstants.sellProductType));
////		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleConstants.reverseProductType));
////		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleConstants.editProductType));
//		return authorityType;
//	}

}
