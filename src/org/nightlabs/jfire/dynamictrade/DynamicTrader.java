package org.nightlabs.jfire.dynamictrade;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.dynamictrade.recurring.DynamicProductTypeRecurringArticle;
import org.nightlabs.jfire.dynamictrade.recurring.DynamicProductTypeRecurringArticleCreator;
import org.nightlabs.jfire.dynamictrade.store.DynamicProduct;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.Unit;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.trade.recurring.RecurringOrder;
import org.nightlabs.jfire.trade.recurring.RecurringTrader;

/**
 *
 *
 * @author Fitas Amine <fitas[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.dynamictrade.id.DynamicTraderID"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicTrader"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DynamicTrader {


	private static final Logger logger = Logger.getLogger(DynamicTrader.class);

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;


	/**
	 * This method returns the singleton instance of {@link DynamicTrader}. If there is no
	 * instance of {@link DynamicTrader} in the datastore, yet, it will be created.
	 *
	 * @param pm The PersistenceManager to retrieve the {@link DynamicTrader} with.
	 * @return The {@link DynamicTrader} of the datastore of the given PersistenceManager.
	 */
	public static DynamicTrader getDynamicTrader(PersistenceManager pm)
	{
		Iterator<?> it = pm.getExtent(DynamicTrader.class).iterator();
		if (it.hasNext()) {
			DynamicTrader dynamicTrader = (DynamicTrader) it.next();
			return dynamicTrader;
		}

		DynamicTrader dynamicTrader = new DynamicTrader();
		dynamicTrader.organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		dynamicTrader = pm.makePersistent(dynamicTrader);

		return dynamicTrader;
	}


	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		String res = organisationID;
		if (res == null)
			SecurityReflector.getUserDescriptor().getOrganisationID();

		return res;
	}

	/**
	 * @return the {@link PersistenceManager} associated with this {@link RecurringTrader}
	 * will fail if the instance is not attached.
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
			"This instance of Trader is currently not attached to a datastore! Cannot get a PersistenceManager!");

		return pm;
	}


	/**
	 * creates a new Dynamic Recurring Article
	 */
	public	Article createRecurringArticle(SegmentID segmentID,
			OfferID offerID,
			ProductTypeID productTypeID,
			long quantity,
			UnitID unitID,
			TariffID tariffID,
			I18nText productName,
			Price singlePrice
	) 
	throws ModuleException
	{

		if (segmentID == null)     throw new IllegalArgumentException("segmentID must not be null!");
		// offerID can be null
		if (productTypeID == null) throw new IllegalArgumentException("productTypeID must not be null!");
		// quantity can be everything
		if (unitID == null) throw new IllegalArgumentException("unitID must not be null!");
		if (tariffID == null)      throw new IllegalArgumentException("tariffID must not be null!");
		if (productName == null)   throw new IllegalArgumentException("productName must not be null!");


		PersistenceManager pm = getPersistenceManager();

		Trader trader = Trader.getTrader(pm);
		RecurringTrader recurringTrader = RecurringTrader.getRecurringTrader(pm);
		Store store = Store.getStore(pm);
		Segment segment = (Segment) pm.getObjectById(segmentID);
		Order order = segment.getOrder();
		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		

		pm.getExtent(Unit.class);
		Unit unit = (Unit) pm.getObjectById(unitID);

		pm.getExtent(DynamicProductType.class);
		ProductType pt = (ProductType) pm.getObjectById(productTypeID);
		if (!(pt instanceof DynamicProductType))
			throw new IllegalArgumentException("productTypeID \""+productTypeID+"\" specifies a ProductType of type \""+pt.getClass().getName()+"\", but must be \""+DynamicProductType.class.getName()+"\"!");

		DynamicProductType productType = (DynamicProductType)pt;


		Authority.resolveSecuringAuthority(
				pm,
				productType.getProductTypeLocal(),
				ResolveSecuringAuthorityStrategy.organisation // must be "organisation", because the role "sellProductType" is not checked on EJB method level!
		).assertContainsRoleRef(
				SecurityReflector.getUserDescriptor().getUserObjectID(),
				org.nightlabs.jfire.trade.RoleConstants.sellProductType
		);

		Tariff tariff = (Tariff) pm.getObjectById(tariffID);

		// find an Offer within the Order which is not finalized - or create one
		Offer offer;
		if (offerID == null) {
			Collection<Offer> offers = Offer.getNonFinalizedNonEndedOffers(pm, order);
			if (!offers.isEmpty()) {
				offer = offers.iterator().next();
			}
			else {		
				offer = recurringTrader.createRecurringOffer(user, (RecurringOrder) order, null);
			}
		}
		else {
			pm.getExtent(Offer.class);
			offer = (Offer) pm.getObjectById(offerID);
		}
		
		DynamicProductInfo productInfo = new DynamicProductInfoImpl(productName, quantity, unit, singlePrice);
	
		Collection<? extends DynamicProductTypeRecurringArticle> articles = (Collection<? extends DynamicProductTypeRecurringArticle>) trader.createArticles(
				user, offer, segment,
				Collections.singleton(pt),
				new  DynamicProductTypeRecurringArticleCreator(tariff, productInfo));

		if (articles.size() != 1)
			throw new IllegalStateException("trader.createArticles(...) created " + articles.size() + " instead of exactly 1 article!");

		return articles.iterator().next();
	}



	public Article createArticle(
			SegmentID segmentID,
			OfferID offerID,
			ProductTypeID productTypeID,
			long quantity,
			UnitID unitID,
			TariffID tariffID,
			I18nText productName,
			Price singlePrice,
			boolean allocate,
			boolean allocateSynchronously)
	throws ModuleException
	{
		if (segmentID == null)     throw new IllegalArgumentException("segmentID must not be null!");
		// offerID can be null
		if (productTypeID == null) throw new IllegalArgumentException("productTypeID must not be null!");
		// quantity can be everything
		if (unitID == null) throw new IllegalArgumentException("unitID must not be null!");
		if (tariffID == null)      throw new IllegalArgumentException("tariffID must not be null!");
		if (productName == null)   throw new IllegalArgumentException("productName must not be null!");
		if (singlePrice == null)   throw new IllegalArgumentException("singlePrice must not be null!");

		PersistenceManager pm = getPersistenceManager();

		Trader trader = Trader.getTrader(pm);
		Store store = Store.getStore(pm);
		Segment segment = (Segment) pm.getObjectById(segmentID);
		Order order = segment.getOrder();
		User user = SecurityReflector.getUserDescriptor().getUser(pm);



		pm.getExtent(Unit.class);
		Unit unit = (Unit) pm.getObjectById(unitID);

		pm.getExtent(DynamicProductType.class);
		ProductType pt = (ProductType) pm.getObjectById(productTypeID);
		if (!(pt instanceof DynamicProductType))
			throw new IllegalArgumentException("productTypeID \""+productTypeID+"\" specifies a ProductType of type \""+pt.getClass().getName()+"\", but must be \""+DynamicProductType.class.getName()+"\"!");

		DynamicProductType productType = (DynamicProductType)pt;

		Authority.resolveSecuringAuthority(
				pm,
				productType.getProductTypeLocal(),
				ResolveSecuringAuthorityStrategy.organisation // must be "organisation", because the role "sellProductType" is not checked on EJB method level!
		).assertContainsRoleRef(
				SecurityReflector.getUserDescriptor().getUserObjectID(),
				org.nightlabs.jfire.trade.RoleConstants.sellProductType
		);

		Tariff tariff = (Tariff) pm.getObjectById(tariffID);

		// find an Offer within the Order which is not finalized - or create one
		Offer offer;
		if (offerID == null) {
			Collection<Offer> offers = Offer.getNonFinalizedNonEndedOffers(pm, order);
			if (!offers.isEmpty()) {
				offer = offers.iterator().next();
			}
			else {
				offer = trader.createOffer(user, order, null); // TODO offerIDPrefix ???
			}
		}
		else {
			pm.getExtent(Offer.class);
			offer = (Offer) pm.getObjectById(offerID);
		}

		// find / create Products
		Collection<? extends Product> products = store.findProducts(user, productType, null, null); // we create exactly one => no NestedProductTypeLocal needed
		if (products.size() != 1)
			throw new IllegalStateException("store.findProducts(...) created " + products.size() + " instead of exactly 1 product!");

		DynamicProduct product = (DynamicProduct) products.iterator().next();
		product.setSinglePrice(singlePrice);
		product.getName().copyFrom(productName);
		product.setQuantity(quantity);
		product.setUnit(unit);

		Collection<? extends Article> articles = trader.createArticles(
				user, offer, segment,
				products,
				new ArticleCreator(tariff),
				allocate, allocateSynchronously);

		if (articles.size() != 1)
			throw new IllegalStateException("trader.createArticles(...) created " + articles.size() + " instead of exactly 1 article!");

		return articles.iterator().next();
	}

}
