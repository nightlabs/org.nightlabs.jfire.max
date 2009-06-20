package org.nightlabs.jfire.dynamictrade;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.dynamictrade.template.DynamicProductTemplate;
import org.nightlabs.jfire.dynamictrade.template.id.DynamicProductTemplateID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;

@Remote
public interface DynamicTradeManagerRemote
{

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It creates the root DynamicProductType for the organisation itself.
	 * DynamicProductTypes of other organisations cannot be imported or
	 * traded as reseller.
	 */
	void initialise() throws Exception;

	Set<ProductTypeID> getChildDynamicProductTypeIDs(
			ProductTypeID parentDynamicProductTypeID);

	DynamicProductType storeDynamicProductType(
			DynamicProductType dynamicProductType, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	Collection<DynamicTradePriceConfig> storeDynamicTradePriceConfigs(
			Collection<DynamicTradePriceConfig> priceConfigs, boolean get,
			AssignInnerPriceConfigCommand assignInnerPriceConfigCommand)
			throws PriceCalculationException;

	/**
	 * Get the object-ids of all {@link DynamicTradePriceConfig}s.
	 */
	Set<PriceConfigID> getDynamicTradePriceConfigIDs();

	List<DynamicTradePriceConfig> getDynamicTradePriceConfigs(
			Collection<PriceConfigID> dynamicTradePriceConfigIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Creates a new dynamic recurring article.
	 */
	Article createRecurringArticle(SegmentID segmentID, OfferID offerID,
			ProductTypeID productTypeID, long quantity, UnitID unitID,
			TariffID tariffID, I18nText productName, Price singlePrice,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

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
	 */
	Article modifyArticle(ArticleID articleID, Long quantity, UnitID unitID,
			TariffID tariffID, I18nText productName, Price singlePrice, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	String ping(String message);

	DynamicProductTemplate storeDynamicProductTemplate(DynamicProductTemplate dynamicProductTemplate, boolean get, String[] fetchGroups, int maxFetchDepth);

	Collection<DynamicProductTemplateID> getChildDynamicProductTemplateIDs(DynamicProductTemplateID parentCategoryID);

	Collection<DynamicProductTemplate> getDynamicProductTemplates(
			Collection<DynamicProductTemplateID> dynamicProductTemplateIDs, String[] fetchGroups,
			int maxFetchDepth
	);

}