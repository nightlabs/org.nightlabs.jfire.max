package org.nightlabs.jfire.simpletrade;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
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
	 * @throws ModuleException
	 * @throws CannotPublishProductTypeException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws CannotPublishProductTypeException;

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	Set<ProductTypeID> getChildSimpleProductTypeIDs(
			ProductTypeID parentSimpleProductTypeID);

	/**
	 * @return Returns a newly detached instance of <tt>SimpleProductType</tt> if <tt>get</tt> is true - otherwise <tt>null</tt>.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	SimpleProductType storeProductType(SimpleProductType productType,
			boolean get, String[] fetchGroups, int maxFetchDepth)
			throws PriceCalculationException;

	/**
	 * @return Returns the {@link PropertySet}s for the given simpleProductTypeIDs trimmed so that they only contain the given structFieldIDs.
	 * @see PropertySet#detachPropertySetWithTrimmedFieldList(PersistenceManager, PropertySet, Set, String[], int)
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	Map<ProductTypeID, PropertySet> getSimpleProductTypesPropertySets(
			Set<ProductTypeID> simpleProductTypeIDs,
			Set<StructFieldID> structFieldIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	Set<PriceConfigID> getFormulaPriceConfigIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.queryPriceConfigurations")
	List<FormulaPriceConfig> getFormulaPriceConfigs(
			Collection<PriceConfigID> formulaPriceConfigIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException in case there are not enough <tt>Product</tt>s available and the <tt>Product</tt>s cannot be created (because of a limit).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Collection<? extends Article> createArticles(SegmentID segmentID,
			OfferID offerID, Collection<ProductTypeID> productTypeIDs,
			TariffID tariffID, String[] fetchGroups, int maxFetchDepth)
			throws ModuleException;

	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException in case there are not enough <tt>Product</tt>s available and the <tt>Product</tt>s cannot be created (because of a limit).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Collection<? extends Article> createArticles(SegmentID segmentID,
			OfferID offerID, ProductTypeID productTypeID, int quantity,
			TariffID tariffID, boolean allocate, boolean allocateSynchronously,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<ProductTypeID> getPublishedSimpleProductTypeIDs();

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<SimpleProductType> getSimpleProductTypesForReseller(
			Collection<ProductTypeID> productTypeIDs);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	void importSimpleProductTypesForReselling(String emitterOrganisationID)
			throws JFireException;

	/**
	 * @return a <tt>Collection</tt> of {@link TariffPricePair}
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.sellProductType, org.nightlabs.jfire.accounting.queryPriceConfigurations"
	 */
	@RolesAllowed( { "org.nightlabs.jfire.trade.sellProductType",
			"org.nightlabs.jfire.accounting.queryPriceConfigurations" })
	Collection<TariffPricePair> getTariffPricePairs(
			ProductTypeID productTypeID, CustomerGroupID customerGroupID,
			CurrencyID currencyID, String[] tariffFetchGroups,
			String[] priceFetchGroups);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.accounting.editPriceConfiguration"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.accounting.editPriceConfiguration")
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
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editUnconfirmedProductType")
	Collection<OrganisationID> getCandidateOrganisationIDsForCrossTrade();

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	String ping(String message);

}