package org.nightlabs.jfire.simpletrade;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.gridpriceconfig.TariffPricePair;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.JFireException;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.NotAvailableException;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;

@Remote
public interface SimpleTradeManagerRemote {

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It creates the root simple product for the organisation itself.
	 * Simple products of other organisations must be imported.
	 *
	 * @throws CannotPublishProductTypeException
	 */
	void initialise() throws CannotPublishProductTypeException;

	Set<ProductTypeID> getChildSimpleProductTypeIDs(ProductTypeID parentSimpleProductTypeID);

	/**
	 * @return Returns a newly detached instance of <tt>SimpleProductType</tt> if <tt>get</tt> is true - otherwise <tt>null</tt>.
	 */
	SimpleProductType storeProductType(SimpleProductType productType, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws PriceCalculationException;

	/**
	 * @return Returns the {@link PropertySet}s for the given simpleProductTypeIDs trimmed so that they only contain the given structFieldIDs.
	 * @see PropertySet#detachPropertySetWithTrimmedFieldList(PersistenceManager, PropertySet, Set, String[], int)
	 */
	Map<ProductTypeID, PropertySet> getSimpleProductTypesPropertySets(Set<ProductTypeID> simpleProductTypeIDs, Set<StructFieldID> structFieldIDs, String[] fetchGroups, int maxFetchDepth);

	Set<PriceConfigID> getFormulaPriceConfigIDs();

	List<FormulaPriceConfig> getFormulaPriceConfigs(Collection<PriceConfigID> formulaPriceConfigIDs, String[] fetchGroups, int maxFetchDepth);

	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException in case there are not enough <tt>Product</tt>s available and the <tt>Product</tt>s cannot be created (because of a limit).
	 */
	Collection<? extends Article> createArticles(SegmentID segmentID,
			OfferID offerID, Collection<ProductTypeID> productTypeIDs,
			TariffID tariffID, String[] fetchGroups, int maxFetchDepth)
			throws NotAvailableException;

	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException in case there are not enough <tt>Product</tt>s available and the <tt>Product</tt>s cannot be created (because of a limit).
	 */
	Collection<? extends Article> createArticles(SegmentID segmentID,
			OfferID offerID, ProductTypeID productTypeID, int quantity,
			TariffID tariffID, boolean allocate, boolean allocateSynchronously,
			String[] fetchGroups, int maxFetchDepth) throws NotAvailableException;

	Set<ProductTypeID> getPublishedSimpleProductTypeIDs();

	List<SimpleProductType> getSimpleProductTypesForReseller(Collection<ProductTypeID> productTypeIDs);

	void importSimpleProductTypesForReselling(String emitterOrganisationID) throws JFireException;

	/**
	 * @return a <tt>Collection</tt> of {@link TariffPricePair}
	 */
	Collection<TariffPricePair> getTariffPricePairs(
			ProductTypeID productTypeID, CustomerGroupID customerGroupID,
			CurrencyID currencyID, String[] tariffFetchGroups,
			String[] priceFetchGroups);

	Collection<GridPriceConfig> storePriceConfigs(
			Collection<GridPriceConfig> priceConfigs, boolean get,
			AssignInnerPriceConfigCommand assignInnerPriceConfigCommand)
			throws PriceCalculationException;

	/**
	 * This method returns {@link OrganisationID}s for all {@link Organisation}s that are known to
	 * the current organisation, but excluding:
	 * <ul>
	 * <li>the current organisation</li>
	 * <li>all organisations for which already a subscribed root-simple-producttype exists</li>
	 * </ul>
	 */
	Collection<OrganisationID> getCandidateOrganisationIDsForCrossTrade();

	String ping(String message);
}