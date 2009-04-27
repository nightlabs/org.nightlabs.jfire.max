package org.nightlabs.jfire.dynamictrade;

import java.util.Collection;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.trade.Article;

@Remote
public interface DynamicTradeManagerRemote
{

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It creates the root DynamicProductType for the organisation itself.
	 * DynamicProductTypes of other organisations cannot be imported or
	 * traded as reseller.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
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
	Set<ProductTypeID> getChildDynamicProductTypeIDs(
			ProductTypeID parentDynamicProductTypeID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	DynamicProductType storeDynamicProductType(
			DynamicProductType dynamicProductType, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editPriceConfiguration"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editPriceConfiguration")
	Collection<DynamicTradePriceConfig> storeDynamicTradePriceConfigs(
			Collection<DynamicTradePriceConfig> priceConfigs, boolean get,
			AssignInnerPriceConfigCommand assignInnerPriceConfigCommand)
			throws PriceCalculationException;

	/**
	 * Get the object-ids of all {@link DynamicTradePriceConfig}s.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	@SuppressWarnings("unchecked")
	Set<PriceConfigID> getDynamicTradePriceConfigIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	List<DynamicTradePriceConfig> getDynamicTradePriceConfigs(
			Collection<PriceConfigID> dynamicTradePriceConfigIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * creates a new Dynamic Recurring Article
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Article createRecurringArticle(SegmentID segmentID, OfferID offerID,
			ProductTypeID productTypeID, long quantity, UnitID unitID,
			TariffID tariffID, I18nText productName, Price singlePrice,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Article createArticle(SegmentID segmentID, OfferID offerID,
			ProductTypeID productTypeID, long quantity, UnitID unitID,
			TariffID tariffID, I18nText productName, Price singlePrice,
			boolean allocate, boolean allocateSynchronously, String[] fetchGroups,
			int maxFetchDepth) throws ModuleException;

	/**
	 * @param articleID Specifies the {@link Article} that should be changed. Must not be <code>null</code>.
	 * @param quantity If <code>null</code>, no change will happen to this property - otherwise it will be updated (causes recalculation of the offer's price).
	 * @param unitID If <code>null</code>, no change will happen to this property - otherwise it will be updated.
	 * @param productName If <code>null</code>, no change will happen to this property - otherwise it will be updated.
	 * @param singlePrice If <code>null</code>, no change will happen to this property - otherwise it will be updated (causes recalculation of the offer's price).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Article modifyArticle(ArticleID articleID, Long quantity, UnitID unitID,
			TariffID tariffID, I18nText productName, Price singlePrice, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	String ping(String message);

}