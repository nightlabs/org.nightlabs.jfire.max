package org.nightlabs.jfire.dynamictrade.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDynamicTrade_DynamicProductTypeActionHandler")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class DynamicProductTypeActionHandler
		extends ProductTypeActionHandler
{
	public static final AuthorityTypeID AUTHORITY_TYPE_ID = AuthorityTypeID.create(DynamicProductType.class.getName());

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
	public Collection<? extends Product> findProducts(User user,
			ProductType productType, NestedProductTypeLocal nestedProductTypeLocal, ProductLocator productLocator)
	{
		DynamicProductType spt = (DynamicProductType) productType;
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);
		int qty = nestedProductTypeLocal == null ? 1 : nestedProductTypeLocal.getQuantity();
		ArrayList<DynamicProduct> res = new ArrayList<DynamicProduct>(qty);
		for (int i = 0; i < qty; ++i) {
			DynamicProduct product = new DynamicProduct(spt, Product.createProductID());
			product = (DynamicProduct) store.addProduct(user, product); // , (Repository)spt.getProductTypeLocal().getHome());
			res.add(product);
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
		throw new UnsupportedOperationException("DynamicProductTypes do not support cross-trade!");
	}

	@Override
	public AuthorityTypeID getAuthorityTypeID(ProductType rootProductType) {
		return AUTHORITY_TYPE_ID;
	}

//	@Override
//	protected AuthorityType createAuthorityType(AuthorityTypeID authorityTypeID, ProductType rootProductType) {
//		AuthorityType authorityType = new AuthorityType(authorityTypeID);
//
//		authorityType.getName().setText(Locale.ENGLISH.getLanguage(), "Dynamic product types");
//		authorityType.getDescription().setText(Locale.ENGLISH.getLanguage(), "Authorities of this type control the access rights for dynamic product types.");
//
//		authorityType.getName().setText(Locale.GERMAN.getLanguage(), "Dynamische Produkttypen");
//		authorityType.getDescription().setText(Locale.GERMAN.getLanguage(), "Vollmachten dieses Typs kontrollieren den Zugriff auf dynamische Produkttypen.");
//
//		// TODO configure access rights completely - implement manual checking where necessary!
//		return authorityType;
//	}
}
