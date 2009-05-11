package org.nightlabs.jfire.voucher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.scripting.ScriptRegistryItem;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.editor2d.LayoutMapForArticleIDSet;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.scripting.PreviewParameterSet;
import org.nightlabs.jfire.voucher.scripting.PreviewParameterValuesResult;
import org.nightlabs.jfire.voucher.scripting.VoucherLayout;
import org.nightlabs.jfire.voucher.scripting.VoucherScriptingConstants;
import org.nightlabs.jfire.voucher.scripting.id.VoucherLayoutID;
import org.nightlabs.jfire.voucher.store.VoucherKey;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.jfire.voucher.store.id.VoucherKeyID;

@Remote
public interface VoucherManagerRemote {

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws Exception;

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	Set<ProductTypeID> getChildVoucherTypeIDs(ProductTypeID parentVoucherTypeID);

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	Set<PriceConfigID> getVoucherPriceConfigIDs();

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	List<VoucherPriceConfig> getVoucherPriceConfigs(
			Collection<PriceConfigID> voucherPriceConfigIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	VoucherType storeVoucherType(VoucherType voucherType, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Collection<? extends Article> createArticles(SegmentID segmentID,
			OfferID offerID, Collection<ProductTypeID> productTypeIDs,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException
	 *           in case there are not enough <tt>Voucher</tt>s available and
	 *           the <tt>Product</tt>s cannot be created (because of a limit).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Collection<? extends Article> createArticles(SegmentID segmentID,
			OfferID offerID, ProductTypeID productTypeID, int quantity,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryLocalAccountantDelegates"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates")
	Set<LocalAccountantDelegateID> getVoucherLocalAccountantDelegateIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryLocalAccountantDelegates"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates")
	List<VoucherLocalAccountantDelegate> getVoucherLocalAccountantDelegates(
			Collection<LocalAccountantDelegateID> voucherLocalAccountantDelegateIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the {@link VoucherKeyID} for a given voucher-key-{@link String}.
	 * <p>
	 * Since this method is used when redeeming a voucher, this method can only be called by users
	 * having the right 'org.nightlabs.jfire.voucher.redeemVoucher' granted.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.redeemVoucher"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.voucher.redeemVoucher")
	VoucherKeyID getVoucherKeyID(String voucherKeyString);

	/**
	 * Get {@link VoucherKey}s for the given object-ids.
	 * <p>
	 * Since this method is used when redeeming a voucher, this method can only be called by users
	 * having the right 'org.nightlabs.jfire.voucher.redeemVoucher' granted.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.redeemVoucher"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.voucher.redeemVoucher")
	List<VoucherKey> getVoucherKeys(Collection<VoucherKeyID> voucherKeyIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @param voucherIDs
	 *          Specifies which vouchers shall be evaluated.
	 * @param allScripts
	 *          If <code>false</code>, only those scripts are evaluated that
	 *          are imported into the voucher design. If <code>true</code>, all
	 *          scripts with the
	 *          {@link ScriptRegistryItem#getScriptRegistryItemType()}
	 *          {@link VoucherScriptingConstants#SCRIPT_REGISTRY_ITEM_TYPE_VOUCHER}
	 *          will be executed and included in the result.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.sellProductType"
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.sellProductType")
	Map<ProductID, Map<ScriptRegistryItemID, Object>> getVoucherScriptingResults(
			Collection<ProductID> voucherIDs, boolean allScripts);

	/**
	 * Get some preview data which can be used in a graphical voucher editor
	 * or similar use cases to show meaningful data for the various data-source-scripts.
	 * <p>
	 * This method can be called by every authenticated user. We might restrict access in the future.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	PreviewParameterValuesResult getPreviewParameterValues(
			ProductTypeID voucherTypeID) throws ModuleException;

	/**
	 * Get some preview data which can be used in a graphical voucher editor
	 * or similar use cases to show meaningful data for the various data-source-scripts.
	 * <p>
	 * This method can be called by every authenticated user. We might restrict access in the future.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	Map<ProductID, Map<ScriptRegistryItemID, Object>> getPreviewVoucherData(
			PreviewParameterSet previewParameterSet) throws ModuleException;

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.sellProductType"
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.sellProductType")
	LayoutMapForArticleIDSet getVoucherLayoutMapForArticleIDSet(
			Collection<ArticleID> articleIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Returns a set of the Object-IDs of all {@link VoucherLayout}s.
	 *
	 * @return a set of the Object-IDs of all {@link VoucherLayout}s.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	Set<VoucherLayoutID> getAllVoucherLayoutIds();

	/**
	 * Replaces the voucherlayout identified by oldVoucherLayoutID with the given voucher layout and deletes the old voucher layout afterwards.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	void replaceVoucherLayout(VoucherLayoutID oldVoucherLayoutId,
			VoucherLayout newVoucherLayout);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	void deleteVoucherLayout(VoucherLayoutID voucherLayoutID);

	/**
	 * Get {@link VoucherLayout}s specified by their object-ids.
	 * <p>
	 * This method can be called by every authenticated user. We might restrict access in the future.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	List<VoucherLayout> getVoucherLayouts(
			Set<VoucherLayoutID> voucherLayoutIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	Set<VoucherLayoutID> getVoucherLayoutIdsByFileName(String fileName);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	VoucherLayout storeVoucherLayout(VoucherLayout voucherLayout, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.editVoucherLayout"
	 */
	@RolesAllowed("org.nightlabs.jfire.voucher.editVoucherLayout")
	Set<ProductTypeID> getVoucherTypeIdsByVoucherLayoutId(
			VoucherLayoutID voucherLayoutId);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	String ping(String message);

}