package org.nightlabs.jfire.voucher.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.id.DeliveryNoteActionHandlerID;
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
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class VoucherTypeActionHandler
		extends ProductTypeActionHandler
{
	private static final Logger logger = Logger.getLogger(VoucherTypeActionHandler.class);

	public static final AuthorityTypeID AUTHORITY_TYPE_ID = AuthorityTypeID.create(Organisation.DEV_ORGANISATION_ID, VoucherType.class.getName());

//	/**
//	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
//	 * the {@link Repository} which becomes the factory-output-repository for all
//	 * newly created {@link SimpleProduct}s.
//	 */
//	public static final String ANCHOR_ID_REPOSITORY_HOME_LOCAL = VoucherType.class.getName() + ".local";
//
//	/**
//	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
//	 * the {@link Repository} which is used for products that are bought from a foreign organisation.
//	 */
//	public static final String ANCHOR_ID_REPOSITORY_HOME_FOREIGN = VoucherType.class.getName() + ".foreign";
//
//	public static Repository getDefaultHome(PersistenceManager pm, VoucherType voucherType)
//	{
//		Store store = Store.getStore(pm);
//		if (store.getOrganisationID().equals(voucherType.getOrganisationID()))
//			return getDefaultLocalHome(pm, store);
//		else
//			return getDefaultForeignHome(pm, store);
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
//
//	protected static Repository getDefaultForeignHome(PersistenceManager pm, Store store)
//	{
//		return Repository.createRepository(
//				pm,
//				store.getOrganisationID(),
//				Repository.ANCHOR_TYPE_ID_HOME,
//				ANCHOR_ID_REPOSITORY_HOME_FOREIGN,
//				store.getMandator(), false);
//	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherTypeActionHandler() { }

	public VoucherTypeActionHandler(String organisationID,
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
		if (logger.isDebugEnabled())
			logger.debug("findProducts entered. organisationID = " + getOrganisationID() + " productType=" + productType.getPrimaryKey());

		VoucherType vt = (VoucherType) productType;
		VoucherTypeLocal vtl = (VoucherTypeLocal) productType.getProductTypeLocal();
		int qty = nestedProductTypeLocal == null ? 1 : nestedProductTypeLocal.getQuantity();
		PersistenceManager pm = getPersistenceManager();

		Store store = Store.getStore(pm);
		// search for an available product
		Query q = pm.newQuery(Voucher.class);
		q.setFilter("productType == pProductType && productLocal.available");
		q.declareParameters("VoucherType pProductType");
		q.declareImports("import " + VoucherType.class.getName());
		Collection availableProducts = (Collection) q.execute(this); // Product.getProducts(pm, this, ProductStatus.STATUS_AVAILABLE);
		ArrayList res = new ArrayList();
		Iterator iteratorAvailableProducts = availableProducts.iterator();
		for (int i = 0; i < qty; ++i) {
			Voucher product = null;
			if (iteratorAvailableProducts.hasNext()) {
				product = (Voucher) iteratorAvailableProducts.next();
				res.add(product);
			}
			else {
				// create products only if this product type is ours
				if (productType.getOrganisationID().equals(store.getOrganisationID())) {
					long createdProductCount = vtl.getCreatedVoucherCount();
					if (vtl.getMaxVoucherCount() < 0 || createdProductCount + 1 <= vtl.getMaxVoucherCount()) {
						product = new Voucher(vt, Product.createProductID());
						vtl.setCreatedVoucherCount(createdProductCount + 1);

						store.addProduct(user, product); // , (Repository)vt.getProductTypeLocal().getHome());
						res.add(product);
					}
				} // This productType is factored by this organisation
				else
					throw new UnsupportedOperationException("NYI");
			}
		}
		return res;
	}

	@Override
	public void onAddArticlesToDeliveryNote(User user, Store store, DeliveryNote deliveryNote, Collection<? extends Article> articles)
	{
		if (logger.isDebugEnabled())
			logger.debug("onAddArticlesToDeliveryNote entered. organisationID=" + getOrganisationID() + " deliveryNote=" + deliveryNote.getPrimaryKey() + " articles.size()=" + articles.size());

//		super.onAddArticlesToDeliveryNote(user, store, deliveryNote, articles); // the super method is a noop - at least now. Marco.
		PersistenceManager pm = getPersistenceManager();

		pm.getExtent(VoucherDeliveryNoteActionHandler.class);
		VoucherDeliveryNoteActionHandler deliveryNoteActionHandler = (VoucherDeliveryNoteActionHandler) pm.getObjectById(DeliveryNoteActionHandlerID.create(Organisation.DEV_ORGANISATION_ID, VoucherDeliveryNoteActionHandler.class.getName()));
		deliveryNote.getDeliveryNoteLocal().addDeliveryNoteActionHandler(deliveryNoteActionHandler);
	}

	@Override
	@Implement
	protected Collection<? extends Article> createCrossTradeArticles(
			User user, Product localPackageProduct, Article localArticle,
			String partnerOrganisationID, Hashtable<?, ?> partnerInitialContextProperties,
			Offer partnerOffer, OfferID partnerOfferID, SegmentID partnerSegmentID, ProductType nestedProductType, Collection<NestedProductTypeLocal> nestedProductTypeLocals) throws Exception
	{
		throw new UnsupportedOperationException("Vouchers cannot be traded accross organisations!");
	}

	@Override
	public AuthorityTypeID getAuthorityTypeID(ProductType rootProductType) {
		return AUTHORITY_TYPE_ID;
	}

	@Override
	protected AuthorityType createAuthorityType(AuthorityTypeID authorityTypeID, ProductType rootProductType) {
		PersistenceManager pm = getPersistenceManager();
		AuthorityType authorityType = new AuthorityType(authorityTypeID);
		// TODO configure access rights completely - implement manual checking where necessary!
		// hmmm...maybe there should only be one "ProductType.edit" instead of one for each implementation - we have the authorities anyway and can control per ProductType if that right is available or not - so we should better reduce the number of rights!
		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleGroupID.create("JFireVoucher.VoucherType.edit")));
		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleGroupID.create("JFireVoucher.VoucherType.view")));
		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleGroupID.create("JFireVoucher.Voucher.sell")));
		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleGroupID.create("JFireVoucher.Voucher.reverse")));
//		authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleGroupID.create("JFireVoucher.Admin"))); // admin is probably not necessary
		return authorityType;
	}
}
