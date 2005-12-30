/*
 * Created 	on Sep 15, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting.book;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;

import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.util.Utils;

/**
 * LocalAccountantDelegates are used by {@link org.nightlabs.jfire.accounting.book.LocalAccountant}
 * to assist in the booking procedure. A Delegate is registered per ProductType 
 * and will be asked to {@link #bookArticle(OrganisationLegalEntity, User, Invoice, ArticlePrice, BookMoneyTransfer, Map)}
 * when an invoice is booked.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class="org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID"
 *		detachable="true"
 *		table="JFireTrade_LocalAccountantDelegate"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, localAccountantDelegateID"
 *
 * @jdo.query
 *		name="getLocalAccountantDelegate"
 *		query="SELECT UNIQUE this
 *			WHERE organisationID == paramOrganisationID &&
 *            localAccountantDelegateID == paramLocalAccountantDelegateID
 *			PARAMETERS String paramOrganisationID, String paramLocalAccountantDelegateID
 *			IMPORTS import java.lang.String"
 *
 * @jdo.query
 *		name="getChildDelegates"
 *		query="SELECT
 *			WHERE 
 *						this.extendedAccountantDelegate != null &&
 *						this.extendedAccountantDelegate.organisationID == paramOrganisationID &&
 *						this.extendedAccountantDelegate.localAccountantDelegateID == paramLocalAccountantDelegateID
 *			PARAMETERS String paramOrganisationID, String paramLocalAccountantDelegateID
 *			IMPORTS import java.lang.String"
 *
 * @jdo.fetch-group name="LocalAccountantDelegate.moneyFlowMappings" fields="moneyFlowMappings"
 * @jdo.fetch-group name="LocalAccountantDelegate.name" fields="name"
 * @jdo.fetch-group name="LocalAccountantDelegate.extendedAccountantDelegate" fields="extendedAccountantDelegate"
 * @jdo.fetch-group name="LocalAccountantDelegate.this" fetch-groups="default" fields="moneyFlowMappings, name, extendedAccountantDelegate"
 *
 */
public abstract class LocalAccountantDelegate implements Serializable {
	
	protected static final Logger LOGGER = Logger.getLogger(LocalAccountantDelegate.class);
	
	public static final String FETCH_GROUP_MONEY_FLOW_MAPPINGS = "LocalAccountantDelegate.moneyFlowMappings";
	public static final String FETCH_GROUP_NAME = "LocalAccountantDelegate.name";
	public static final String FETCH_GROUP_EXTENDED_ACCOUNTANT_DELEGATE = "LocalAccountantDelegate.extendedAccountantDelegate";
	public static final String FETCH_GROUP_THIS_LOCAL_ACCOUNTANT_DELEGATE = "LocalAccountantDelegate.this";
	
	public static final String QUERY_GET_LOCAL_ACCOUNTANT_DELEGATE = "getLocalAccountantDelegate";
	public static final String QUERY_GET_CHILD_DELEGATES = "getChildDelegates";
	
	/**
	 * @deprecated Only for JDO 
	 */
	public LocalAccountantDelegate() {}
	
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String localAccountantDelegateID;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="MoneyFlowMapping"
	 *		mapped-by="localAccountantDelegate"
	 *
	 */
	private Set moneyFlowMappings;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="localAccountantDelegate"
	 */
	private LocalAccountantDelegateName name;
	
	
	// TODO: write generic resolve engine for plugable dimensions
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalAccountantDelegate extendedAccountantDelegate;

	public LocalAccountantDelegate(String organisationID, String localAccountantDelegateID) {
		this.organisationID = organisationID;
		this.localAccountantDelegateID = localAccountantDelegateID;
		this.name = new LocalAccountantDelegateName(this);
	}
	
	public LocalAccountantDelegate(LocalAccountantDelegate parent, String organisationID, String localAccountantDelegateID) {
		this(organisationID, localAccountantDelegateID);
		this.extendedAccountantDelegate = parent;
	}
	
	public void addMoneyFlowMapping(MoneyFlowMapping mapping) {
		if (mapping.getLocalAccountantDelegate() == null)
			mapping.setLocalAccountantDelegate(this);
		else {
			if (!JDOHelper.getObjectId(mapping.getLocalAccountantDelegate()).equals(JDOHelper.getObjectId(this)))
				mapping.setLocalAccountantDelegate(this);
		}
		moneyFlowMappings.add(mapping);
	}
	
	public void removeMoneyFlowMapping(MoneyFlowMapping mapping) {
		moneyFlowMappings.remove(mapping);
	}
	
		/**
	 * Returns the list of Dimensions this LocalAccountantDelegate knows.
	 */
	public abstract List getMoneyFlowDimensionIDs();
	
	
	/* ************ Getter / Setter ***************** */

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 */
	public String getLocalAccountantDelegateID() {
		return localAccountantDelegateID;
	}

	/**
	 * @return Returns the extendedAccountantDelegate.
	 */
	public LocalAccountantDelegate getExtendedAccountantDelegate() {
		return extendedAccountantDelegate;
	}

	/**
	 * @return Returns the moneyFlowMappings.
	 */
	public Set getMoneyFlowMappings() {
		return moneyFlowMappings;
	}
	
	/**
	 * @return Returns the name of this LocalAccountantDelegate
	 */
	public LocalAccountantDelegateName getName() {
		return name;
	}
		
	
	
	/* ************************* Resolve mapping code ************************* */

	/**
	 * Helper class used as key in the resolvedMappings Map.
	 */
	public static class ResolvedMapKey implements Serializable {
		private ProductTypeID productTypeID;
		private String packageType;
		public ResolvedMapKey(ProductTypeID productTypeID, String packageType) {
			this.productTypeID = productTypeID;
			this.packageType = packageType;
		}
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			return obj.toString().equals(toString());
		}
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return toString().hashCode();
		}
		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return packageType +"/"+productTypeID.toString();
		}
		
	}
	
	/**
	 * Helper class to hold resolved mappings for one ProductType 
	 */
	public static class ResolvedMapEnty implements Serializable {
		private ProductType productType;
		private int delegationLevel;
		/**
		 * key: String MoneyFlowMapping.getMappingKey(dimensionValues)
		 * value: MoneyFlowMapping mapping
		 */
		Map resolvedMappings = new HashMap();
		/**
		 * @return Returns the productType.
		 */
		public ProductType getProductType() {
			return productType;
		}
		/**
		 * @param productType The productType to set.
		 */
		public void setProductType(ProductType productType) {
			this.productType = productType;
		}
		/**
		 * @return Returns the resolvedMappings.
		 */
		public Map getResolvedMappings() {
			return resolvedMappings;
		}
		/**
		 * @param resolvedMappings The resolvedMappings to set.
		 */
		public void setResolvedMappings(Map resolvedMappings) {
			this.resolvedMappings = resolvedMappings;
		}
		/**
		 * @return Returns the delegationLevel
		 */
		public int getDelegationLevel() {
			return delegationLevel;
		}
		/**
		 * @param wasDelegated The wasDelegated to set.
		 */
		public void setDelegationLevel(int delegationLevel) {
			this.delegationLevel = delegationLevel;
		}		
	}
	
	/**
	 * Inferface used when resolving Mappings. 
	 */
	public static interface ResolveProductTypeProvider {
		ProductType getProductType();
		String getPackageType();
		Collection getNestedProviders();
	}
	
	/**
	 * ResloveProvider on basis of the ProductType structure
	 */
	private static class ProductTypeProvider implements ResolveProductTypeProvider {
		private ProductType productType;
		private String packageType;
		
		public ProductTypeProvider(String packageType, ProductType productType) {
			this.productType = productType;
			this.packageType = packageType;
		}
		
		public ProductType getProductType() {
			return productType;
		}

		public String getPackageType() {
			return packageType;
		}
		
		private List nested;
		public Collection getNestedProviders() {
			if (nested == null) {
				nested = new LinkedList();
				for (Iterator iter = productType.getNestedProductTypes().iterator(); iter.hasNext();) {
					NestedProductType nestedType = (NestedProductType) iter.next();
					nested.add(
							new ProductTypeProvider(
									LocalAccountantDelegate.getPackageType(nestedType),
									nestedType.getInnerProductType()
								)
					);
				}
			}
			return nested;
		}
	}
	
	/**
	 * ResloveProvider based on an ArticlePrice and its nested ones,
	 * so on the snapshot at Offer finalization.
	 */
	private static class ArticlePriceTypeProvider implements ResolveProductTypeProvider {
		private ArticlePrice articlePrice;
		private String packageType;
		
		public ArticlePriceTypeProvider(String packageType, ArticlePrice articlePrice) {
			this.articlePrice = articlePrice;
			this.packageType = packageType;
		}
		
		public ProductType getProductType() {
			return articlePrice.getProductType();
		}

		public String getPackageType() {
			return packageType;
		}
		
		private List nested;
		public Collection getNestedProviders() {
			if (nested == null) {
				nested = new LinkedList();
				for (Iterator iter = articlePrice.getNestedArticlePrices().iterator(); iter.hasNext();) {
					ArticlePrice nestedPrice = (ArticlePrice) iter.next();
					nested.add(
							new ArticlePriceTypeProvider(
									LocalAccountantDelegate.getPackageType(nestedPrice),
									nestedPrice
								)
					);
				}
			}
			return nested;
		}
	}
	
	/**
	 * Resolves all MoneyFlowMappings for the ProductTypes involved in the
	 * given invoice. For a docu on the structure of the result see
	 * {@link #resolveProductTypeMappings(ProductType)} 
	 * 
	 * @param invoice The Invoice mappings should be resolved for.
	 * @return A map with key {@link ResolvedMapKey} and value {@link ResolvedMapEnty}
	 */
	public Map resolveProductTypeMappings(Invoice invoice) {
		Map result = new HashMap();
		for (Iterator iter = invoice.getArticles().iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			ArticlePriceTypeProvider provider = new ArticlePriceTypeProvider(getPackageType(article.getPrice()), article.getPrice());
			resolveProductTypeMappings(provider, provider.getPackageType(), result, 0);
		}
		return result;
	}
	
	/**
	 * Resolves all MoneyFlowMappings for the given productType and all 
	 * its nested ProductTypes.
	 * 
	 * A Map with key {@link ResolvedMapKey} and value {@link ResolvedMapEnty} is 
	 * returned here. Within the ResolvedMapEntries mappings are stored in an
	 * Map with key (String) mappingKey and value {@link MoneyFlowMapping}.
	 * Upon resolving mappings the delegation hierarchy and the productType 
	 * hierarchy are itearted from top down. Hereby matching mappings
	 * will be added to the resolved map with a fake mappingKey pretending that 
	 * the mapping was made for the ProductType searched for. 
	 * By that mappings from child delegates and for child-ProductTypes will 
	 * overwrite the ones from their parents.
	 * For example:
	 * <pre>
	 *   PType2  ---extends---> PType1
	 *   Delegate1
	 *     |    -> Mapping: PType2 (dimensions) -> Account1
	 *     |
	 *   extends
	 *     |
	 *     |
	 *   Delegate2
	 *          -> Mapping: PType1 (dimensions) -> Account2
	 *          -> Mapping: PType2 (dimensions) -> Account3 
	 * </pre>
	 * If now Delegate2 is asked for the mappings of PType2, this will result in
	 * one Mapping pointing to Account3. First the mapping of Delegate1(parent)
	 * is added. Then the mapping for PType1(pType-parent) will overwrite the
	 * mapping just to be overwritten finally by the mapping for PType2.   
	 * 
	 * @param productType The ProductType to resolve mappings for.
	 * @return A map with key {@link ResolvedMapKey} and value {@link ResolvedMapEnty}
	 */
	public Map resolveProductTypeMappings(ProductType productType) {
		Map result = new HashMap();
		resolveProductTypeMappings(productType, result, 0);
		return result;
	}
	

	/**
	 * Resolves mappings for the given productType and
	 * {@link MoneyFlowMapping#PACKAGE_TYPE_PACKAGE} by calling 
	 * {@link #resolveProductTypeMappings(ProductType, String, Map, int)}.
	 * 
	 * @param productType The ProductType to resolve mappings for
	 * @param resolvedMappings The Map to store the resolved ResolvedMapEntries 
	 * @param delegationLevel The current delegationLevel (>0 indicates that the current this was delegated to this LocalAccountantDelegate)
	 * @see #resolveProductTypeMappings(ProductType, String, Map, int) 
	 */
	public void resolveProductTypeMappings(ProductType productType, Map resolvedMappings, int delegationLevel) {
		ProductTypeProvider provider = new ProductTypeProvider(getPackageType(productType), productType);
		resolveProductTypeMappings(provider, provider.getPackageType(), resolvedMappings, delegationLevel);
	}

	/**
	 * Resolves the mappings for the given productType. If there are
	 * Mappings defined within this LocalAccountantDelegate for the given ProductType
	 * and packageType, then the configuration is defined by this delegate for the
	 * given ProductType and packageType. This mehtod will resolve
	 * the mappings for the given productType and recurse into its nested ProductTypes.
	 * If no mapping was defined for the given ProductType or one of its Parents
	 * neither in this delegate nor in one of its parents, then the 
	 * LocalAccountantDelegate of the given ProductType will be asked to
	 * resolve the Mappings according to its mapping-configuration.
	 * 
	 * @param productTypeProvider The ProductTypeProvider to get the ProductType structure to resolve for.
	 * @param packageType The packageType mappings should be resolved for
	 * @param resolvedMappings The Map to store the resolved ResolvedMapEntries 
	 * @param delegationLevel The current delegationLevel (>0 indicates that the current this was delegated to this LocalAccountantDelegate)
	 */
	public void resolveProductTypeMappings(ResolveProductTypeProvider productTypeProvider, String packageType, Map resolvedMappings, int delegationLevel) {
		ResolvedMapEnty entry = addProductTypeMappings(
				productTypeProvider.getProductType(), 
				productTypeProvider.getPackageType(), 
				delegationLevel
			);
		if (entry != null) {
			entry.setDelegationLevel(delegationLevel);
			ResolvedMapKey entryKey = new ResolvedMapKey((ProductTypeID)JDOHelper.getObjectId(entry.getProductType()), packageType); 
			resolvedMappings.put(entryKey, entry);
			for (Iterator iter = productTypeProvider.getNestedProviders().iterator(); iter.hasNext();) {
				ResolveProductTypeProvider nested = (ResolveProductTypeProvider) iter.next();
				resolveProductTypeMappings(nested, nested.getPackageType(), resolvedMappings, delegationLevel);
			}
		}
		else {
			LocalAccountantDelegate delegate = productTypeProvider.getProductType().getLocalAccountantDelegate();
			if (delegate == null)
				// No delegate assigned to the productType
				return;
			
			if (JDOHelper.getObjectId(delegate).equals(JDOHelper.getObjectId(this)))
				// We are the delegate assigned to the productType 
				return;
			delegate.resolveProductTypeMappings(productTypeProvider, packageType, resolvedMappings, delegationLevel+1);		
		}
	}

	/**
	 * Returns a ResolvedMapEntry with all Mappings concerning the given productType
	 * or null if nothing is defined for the given productType or one of its parents
	 * within this delegate or one of its parents.
	 * See {@link #resolveProductTypeMappings(ProductType)} on how mappings
	 * can overwrite each other concerning delegate- and productType-hierarcy.
	 */
	protected ResolvedMapEnty addProductTypeMappings(ProductType productType, String packageType, int delegationLevel) {
		LinkedList delegateHierarchy = new LinkedList();
		LocalAccountantDelegate delegateRun = this;
		while (delegateRun != null) {
			delegateHierarchy.add(delegateRun);
			delegateRun = delegateRun.getExtendedAccountantDelegate();
		}
		
		ResolvedMapEnty entry = null;
		// go through the delegate hierarchy
		while (!delegateHierarchy.isEmpty()) {
			LocalAccountantDelegate delegate = (LocalAccountantDelegate) delegateHierarchy.removeLast();
			
			LinkedList productTypeHierarchy = new LinkedList();
			ProductType pTypeRun = productType;
			while (pTypeRun != null) {
				productTypeHierarchy.add(pTypeRun);
				pTypeRun = pTypeRun.getExtendedProductType();
			}
			// go through the productType hierarchy
			// and add all matching mappings to
			while (!productTypeHierarchy.isEmpty()) {
				ProductType pType = (ProductType) productTypeHierarchy.removeLast();
				for (Iterator iterator = delegate.getMoneyFlowMappings().iterator(); iterator.hasNext();) {
					MoneyFlowMapping mapping = (MoneyFlowMapping)iterator.next();
					if (mapping.matches(pType, packageType)) {
						if (entry == null) {
							entry = new ResolvedMapEnty();
							entry.setProductType(productType);
							entry.setDelegationLevel(delegationLevel);
						}
						// The entries made here by the mapping should 
						// fake the mapping key and pretend the mapping was made on
						// exacly the productType the mapping was searched for.
						// Doing so mappings from parent-delegates as well as for
						// parent-productTypes are overwritten by children declerations 
						mapping.addMappingsToMap(productType, entry.getResolvedMappings());
					}
				}
			}
		}
		return entry; 
	}

	/* *************************** BOOK Logic ********************************* */


	/**
	 * Book the article with the given article-price.
	 * A LocalAccountantDelegate should decide based on its
	 * configuration to which accounts the money is to be booked.
	 * 
	 * Subclasses may delegate the work here to {@link #bookProductTypeParts(OrganisationLegalEntity, User, Map, LinkedList, int, BookMoneyTransfer, Map)}
	 * and only Provide new dimensions.
	 * @param mandator The organisation the LocalAccountant books for.
	 * @param user The user that initiated the booking.
	 * @param invoice The invoice that is currently booked.
	 * @param article The Article to book.
	 * @param container The Container transfer, that is the transfer from the customer to the vendor of the invoice 
	 * @param involvedAnchors A List of involved Anchors, so they can be checked after the booking
	 */
	public abstract void bookArticle(
			OrganisationLegalEntity mandator,
			User user,
			Invoice invoice,
			Article article, 
			BookMoneyTransfer container, 
			Map involvedAnchors
		);


	/**
	 * Called by LocalAccountant before all articles of an invoice are booked.
	 * Gives the delegate the chance to initialize.
	 * @param bookTransfer TODO
	 * @param involvedAnchors TODO
	 */
	public void preBookArticles(OrganisationLegalEntity mandator, User user, Invoice invoice, BookMoneyTransfer bookTransfer, Map involvedAnchors) {}

	/**
	 * Called by LocalAccountant before all articles of an invoice are booked.
	 * Gives the delegate the chance to clean up.
	 * @param bookTransfer TODO
	 * @param involvedAnchors TODO
	 */
	public void postBookArticles(OrganisationLegalEntity mandator, User user, Invoice invoice, BookMoneyTransfer bookTransfer, Map involvedAnchors) {}

	/**
	 * Tries to book all money concerning the given ProductType. It is intented to
	 * be called from the implementation of {@link #bookArticle(OrganisationLegalEntity, User, Invoice, Article, BookMoneyTransfer, Map)}
	 * with the top-level ArticlePrice in the articlePriceStack parameter.  
	 * 
	 * Looks up the resolved mapping entries and decides on basis of the 
	 * delegationLevel in the entries whether to book the money itself or delegate
	 * to the LocalAccountantDelegate assigned to the ProductType currently in
	 * the stack an call this method for this delegate.
	 * If no delegation is done this implementation of bookProductTypeParts
	 * will call abstract {@link #internalBookProductTypeParts(OrganisationLegalEntity, User, Map, LinkedList, ArticlePrice, ProductType, String, int, BookMoneyTransfer, Map)}
	 * which should do the job.  
	 *
	 * @param mandator The mandator to book for.
	 * @param user The user that initiated the booking.
	 * @param resolvedMappings The map of resolved mappings.
	 * @param articlePriceStack A Stack of ArticlePrices representing the ProductType packaging
	 * @param delegationLevel The level of delegation calls to this method
	 * @param container The Container transfer, that is the transfer from the customer to the vendor of the invoice 
	 * @param involvedAnchors A List of involved Anchors, so they can be checked after the booking
	 */
	public void bookProductTypeParts(
			OrganisationLegalEntity mandator,			
			User user,
			Map resolvedMappings,
			LinkedList articlePriceStack,
			Map bookInvoiceTransfers,
			int delegationLevel,
			BookMoneyTransfer container, Map involvedAnchors
		)
	{
		ArticlePrice articlePrice = (ArticlePrice) articlePriceStack.peek();
		ProductType productType = articlePrice.getProductType();
 		boolean topLevel = articlePrice.getPackageArticlePrice() == null;
		String packageType = getPackageType(articlePrice);

		int nextDelegationLevel = getDelegationLevel(resolvedMappings, productType, packageType);
		
		if (
				(nextDelegationLevel > 0) &&
				(delegationLevel < nextDelegationLevel)
			) 
		{
			// Have to delegate the booking of this product-type to its own delegate
			productType.getLocalAccountantDelegate().bookProductTypeParts(
					mandator,
					user, 
					resolvedMappings,
					articlePriceStack,
					bookInvoiceTransfers,
					delegationLevel+1,
					container,
					involvedAnchors
				);
			return;
		}
		
		
		
		internalBookProductTypeParts(
				mandator, user, 
				resolvedMappings, articlePriceStack, articlePrice,
				bookInvoiceTransfers,
				productType, packageType,
				delegationLevel,
				container, involvedAnchors
			);
		
	}
			
	/**
	 * Called by {@link #bookProductTypeParts(OrganisationLegalEntity, User, Map, LinkedList, int, BookMoneyTransfer, Map)}
	 * to actually book the money for the given productType and packageType
	 * after the delegation was resolved.
	 * Subclasses may delegate the work here to {@link #internalBookProductTypePartsByDimension(OrganisationLegalEntity, User, Map, LinkedList, ArticlePrice, ProductType, String, int, BookMoneyTransfer, Map)}
	 * which for all possible dimension values books the money based on the 
	 * (Dimension)-Mappings to the appropiate Accounts.
	 */
	protected abstract void internalBookProductTypeParts(
			OrganisationLegalEntity mandator,			
			User user,
			Map resolvedMappings,
			LinkedList articlePriceStack,
			ArticlePrice articlePrice,
			Map bookInvoiceTransfers,
			ProductType productType, 
			String packageType,
			int delegationLevel,
			BookMoneyTransfer container, Map involvedAnchors
		);

	/**
	 * Can be used as default implementation of {@link #internalBookProductTypeParts(OrganisationLegalEntity, User, Map, LinkedList, ArticlePrice, ProductType, String, int, BookMoneyTransfer, Map)}.
	 * Spans all possible dimension values and books the amount to the appropriate
	 * Account based on the (Dimension)-Mappings in the resolvedMappings Map.
	 */
	protected void internalBookProductTypePartsByDimension(
			OrganisationLegalEntity mandator,			
			User user,
			Map resolvedMappings,
			LinkedList articlePriceStack,
			ArticlePrice articlePrice,
			Map bookInvoiceTransfers,
			ProductType productType, 
			String packageType,
			int delegationLevel,
			BookMoneyTransfer container, Map involvedAnchors
		) 
	{
		
		if (haveToDelegateBooking(resolvedMappings, productType, packageType, delegationLevel)) {
			productType.getLocalAccountantDelegate().bookProductTypeParts(
					mandator,
					user,
					resolvedMappings,
					articlePriceStack,
					bookInvoiceTransfers,
					delegationLevel +1,
					container,
					involvedAnchors
				);
			return;
		}
		Currency currency = articlePrice.getCurrency();
		
		List dimensionIDs = getMoneyFlowDimensionIDs();
				
		List dimensions = getDimensionLeafNodes(productType);
		
		for (Iterator iter = dimensions.iterator(); iter.hasNext();) {
			DimensionNode dimensionNode = (DimensionNode) iter.next();
			
			Anchor from = null;
			Anchor to = null;
			
			MoneyFlowMapping mapping = getMoneyFlowMapping(
					resolvedMappings, productType, packageType, 
					dimensionNode.getDimensionValues(), currency
				);
			
			if (mapping != null) {
				
				// have configuration for this productType, packageType and dimensionValues
				to = mapping.getAccount();
				
				// resolve the anchor to get the money from 
				Collection bookTransfers = getBookInvoiceTransfersForDimensionValues(
						mandator,
						articlePriceStack, 
						dimensionNode.getDimensionValues(),
						mapping,
						resolvedMappings
					);
				for (Iterator iterator = bookTransfers.iterator(); iterator.hasNext();) {
					BookInvoiceTransfer biTransfer = (BookInvoiceTransfer) iterator.next();
					if (biTransfer.getFrom() == null || biTransfer.getFrom().getPrimaryKey().equals(to.getPrimaryKey()))
						biTransfer.setFrom(mandator);
					
					// reverse if amount negative
					if (biTransfer.getAmount() < 0) {
						Anchor tmpAnchor = biTransfer.getTo();
						biTransfer.setFrom(to);
						biTransfer.setTo(tmpAnchor);
					}
					
					if (biTransfer.getFrom().getPrimaryKey().equals(biTransfer.getTo().getPrimaryKey()))
						continue;
					
					Map toTransfers = (Map)bookInvoiceTransfers.get(biTransfer.getFrom());
					if (toTransfers == null) {
						toTransfers = new HashMap();
						bookInvoiceTransfers.put(biTransfer.getFrom(), toTransfers);
					}
					
					Collection transfers = (Collection)toTransfers.get(biTransfer.getTo());
					if (transfers == null) {
						transfers = new LinkedList();
						toTransfers.put(biTransfer.getTo(), transfers);
					}
					transfers.add(biTransfer);
				}
			} // if (mapping != null)
			
		} // for (Iterator iter = articlePrice.getFragments().iterator(); iter.hasNext();) {

//	 recurse
		for (Iterator iterator = articlePrice.getNestedArticlePrices().iterator(); iterator.hasNext();) {
			ArticlePrice nestedArticlePrice = (ArticlePrice) iterator.next();
			articlePriceStack.addFirst(nestedArticlePrice);
			bookProductTypeParts(
					mandator, user, 
					resolvedMappings, articlePriceStack, 
					bookInvoiceTransfers, 
					delegationLevel, 
					container, involvedAnchors
				);
		}

		articlePriceStack.poll();
	}

	/**
	 * Used to accumulate transfer information for the  
	 * booking of one invoice.
	 */
	public static class BookInvoiceTransfer {
		private Anchor from;
		private Anchor to;
		private long amount;
		
		public BookInvoiceTransfer(Anchor from, Anchor to, long amount) {
			this.from = from;
			this.to = to;
			this.amount = amount;
		}

		/**
		 * @return Returns the amount.
		 */
		public long getAmount() {
			return amount;
		}

		/**
		 * @param amount The amount to set.
		 */
		public void setAmount(long amount) {
			this.amount = amount;
		}

		/**
		 * @return Returns the from.
		 */
		public Anchor getFrom() {
			return from;
		}

		/**
		 * @param from The from to set.
		 */
		public void setFrom(Anchor from) {
			this.from = from;
		}

		/**
		 * @return Returns the to.
		 */
		public Anchor getTo() {
			return to;
		}

		/**
		 * @param to The to to set.
		 */
		public void setTo(Anchor to) {
			this.to = to;
		}
	}
	
	/**
	 * Should return a List of {@link BookInvoiceTransfer} with transfers
	 * that collect all neccessary money from the right accounts and
	 * transfer it to the account specified for given dimensionValues and
	 * ProductType (first in articlePriceStack).  
	 * 
	 * @param articlePriceStack The current articlePriceStack.
	 * @param dimensionValues A Map with the current dimension values (key: String value: String)
	 * @param resolvedMapping The MoneyFlowMapping causing the call to this method (contains the target Anchor (Account))
	 * @param resolvedMappings A Map with the resolved Mappings for all involved ProductTypes
	 */
	public abstract Collection getBookInvoiceTransfersForDimensionValues(
			OrganisationLegalEntity mandator,
			LinkedList articlePriceStack, 
			Map dimensionValues,
			MoneyFlowMapping resolvedMapping,
			Map resolvedMappings
		);
	
	/**
	 * Should return the mapping key for the given combination of 
	 * ProductType, packageType and dimension values stored in the given Map.
	 * 
	 * @param productTypeID The ProductType a mapping is searched for.
	 * @param packageType The packageType for the searched mapping.
	 * @param dimensionValues A Map with defined values for all dimensions of this delegate. (key: String value: String)
	 * @param currency The currency for the searched mapping.
	 */
	public abstract String getMoneyFlowMappingKey(
			ProductTypeID productTypeID, 
			String packageType,
			Map dimensionValues,
			String currencyID
		);

	/**
	 * Finds the mapping for the given productType,
	 *  
	 * @param resolvedMappings
	 * @param productType
	 * @param dimensionValues
	 * @param currency
	 * @param packageType
	 * @return
	 */
	protected MoneyFlowMapping getMoneyFlowMapping(
			Map resolvedMappings, 
			ProductType productType, 
			String packageType,
			Map dimensionValues,
			Currency currency
	) {
		return getMoneyFlowMapping(
				resolvedMappings, 
				(ProductTypeID)JDOHelper.getObjectId(productType),
				packageType,
				dimensionValues,
				currency.getCurrencyID()
			);
	}
	
	/**
	 * Finds the mapping for the given productType,
	 *  
	 * @param resolvedMappings
	 * @param productType
	 * @param dimensionValues
	 * @param currency
	 * @param packageType
	 * @return
	 */
	protected MoneyFlowMapping getMoneyFlowMapping(
			Map resolvedMappings, 
			ProductTypeID productTypeID, 
			String packageType,
			Map dimensionValues,
			String currencyID
	) {
		ResolvedMapKey key = new ResolvedMapKey(productTypeID, packageType);
		ResolvedMapEnty entry = (ResolvedMapEnty)resolvedMappings.get(key);
		if (entry == null)
			return null;
		MoneyFlowMapping mapping = (MoneyFlowMapping)entry.getResolvedMappings().get(
				getMoneyFlowMappingKey(
						productTypeID,
						packageType,
						dimensionValues,
						currencyID
					)
			);
		return mapping;
	}
	
	/**
	 * Checks whether the resolving of mappings and the booking of article
	 * has to be delegated to the LocalAccountantDelegate of the givenProductType.
	 * This will be true if no mapping was defined for this ProductType (or parents)
	 * within this LocalAccountantDelegate (or parents).
	 */
	protected boolean haveToDelegateBooking(Map resolvedMappings, ProductType productType, String packageType, int delegationLevel) {
		ResolvedMapKey key = new ResolvedMapKey((ProductTypeID)JDOHelper.getObjectId(productType), packageType); 
		ResolvedMapEnty entry = (ResolvedMapEnty)resolvedMappings.get(key);
		if (entry == null)
			return false;
		else
			return entry.getDelegationLevel() > delegationLevel;
	}
	
	/**
	 * Returns the delegationLevel in the ResolvedMapEntry found for the given
	 * productType and packageType.
	 */
	protected int getDelegationLevel(Map resolvedMappings, ProductType productType, String packageType) {
		ResolvedMapKey key = new ResolvedMapKey((ProductTypeID)JDOHelper.getObjectId(productType), packageType); 
		ResolvedMapEnty entry = (ResolvedMapEnty)resolvedMappings.get(key);
		if (entry == null)
			return 0;
		else
			return entry.getDelegationLevel();	
	}

	/**
	 * Returns the package type of the given ProductType.
	 * It will be {@link MoneyFlowMapping#PACKAGE_TYPE_INNER} if the ProductType
	 * is virtually self packaged in the given articlePrice or the package-nature
	 * of the ProductType is {@link ProductType#PACKAGE_NATURE_INNER}. Will return
	 * {@link MoneyFlowMapping#PACKAGE_TYPE_PACKAGE} otherwise.
	 */
	public static String getPackageType(ArticlePrice articlePrice)
	{
		return (articlePrice.isVirtualInner() || articlePrice.getProductType().isPackageInner()) ?
				MoneyFlowMapping.PACKAGE_TYPE_INNER : MoneyFlowMapping.PACKAGE_TYPE_PACKAGE;
	}
	
	/**
	 * Returns the package type of the given ProductType.
	 * It will be {@link MoneyFlowMapping#PACKAGE_TYPE_PACKAGE} if the ProductType
	 * is of package nature PACKAGE_NATURE_OUTER and has a packagePriceConfig
	 * assigned. Will return {@link MoneyFlowMapping#PACKAGE_TYPE_INNER} otherwise.
	 */
	public static String getPackageType(ProductType productType)
	{
		if (productType.isPackageOuter() && (productType.getPackagePriceConfig() == null))
			throw new IllegalStateException("ProductType "+productType.getPrimaryKey()+" has package-nature PACKAGE_NATURE_OUTER but not packagePriceConfig assigned.");
		
		return (productType.isPackageOuter()) ?
				MoneyFlowMapping.PACKAGE_TYPE_PACKAGE : MoneyFlowMapping.PACKAGE_TYPE_INNER;
	}
	
	/**
	 * Returns the package type of the given NestedProductType.
	 * It will be {@link MoneyFlowMapping#PACKAGE_TYPE_INNER} if the ProductType
	 * is virtually self packaged in the given the package-nature of the 
	 * nested inner ProductType is {@link ProductType#PACKAGE_NATURE_INNER}. 
	 * Will return {@link MoneyFlowMapping#PACKAGE_TYPE_PACKAGE} otherwise.
	 */
	public static String getPackageType(NestedProductType nestedProductType)
	{
		String innerPPK = nestedProductType.getInnerProductTypePrimaryKey();
		String packagePPK = nestedProductType.getPackageProductType().getPrimaryKey();
		return (packagePPK.equals(innerPPK) || nestedProductType.getInnerProductType().isPackageInner()) ?
				MoneyFlowMapping.PACKAGE_TYPE_INNER : MoneyFlowMapping.PACKAGE_TYPE_PACKAGE;
	}
	
	/* ************************* Resolve dimension code *********************** */
	/**
	 * Helper class to resolve all dimension values 
	 */
	protected static class DimensionNode {
		String dimensionPath;
		Map dimensionValues = new HashMap();
		DimensionNode parentDimension;
		List children = new LinkedList();
		
		/**
		 * @return Returns the children.
		 */
		public List getChildren() {
			return children;
		}

		/**
		 * @param children The children to set.
		 */
		public void setChildren(List children) {
			this.children = children;
		}

		/**
		 * @return Returns the dimensionPath.
		 */
		public String getDimensionPath() {
			return dimensionPath;
		}

		/**
		 * @param dimensionPath The dimensionPath to set.
		 */
		public void setDimensionPath(String dimensionPath) {
			this.dimensionPath = dimensionPath;
		}

		/**
		 * @return Returns the dimensionValues.
		 */
		public Map getDimensionValues() {
			return dimensionValues;
		}

		/**
		 * @param dimensionValues The dimensionValues to set.
		 */
		public void setDimensionValues(Map dimensionValues) {
			this.dimensionValues = dimensionValues;
		}

		/**
		 * @return Returns the parentDimension.
		 */
		public DimensionNode getParentDimension() {
			return parentDimension;
		}

		/**
		 * @param parentDimension The parentDimension to set.
		 */
		public void setParentDimension(DimensionNode parentDimension) {
			this.parentDimension = parentDimension;
		}
	}
	
	/**
	 * Returns a node with all dimensionValues in a tree structure.
	 */
	public DimensionNode getRootNode(ProductType productType) {
		DimensionNode node = new DimensionNode();
		List dimensionIDs = getMoneyFlowDimensionIDs();
		makeDimensionNodes(node, dimensionIDs, 0, productType);
		return node;
	}

	/**
	 * Build DimensionNodes for all dimensions in the level given by idx as 
	 * children of the given parent and then recurses into the next level.
	 */
	private void makeDimensionNodes(DimensionNode parent, List dimensionIDs, int idx, ProductType productType) {
		String moneyFlowDimensionID = (String)dimensionIDs.get(idx);
		String[] dimValues = (String[])Utils.collection2TypedArray(getDimensionValues(moneyFlowDimensionID, productType), String.class);
		for (int i = 0; i < dimValues.length; i++) {
			DimensionNode node = new DimensionNode();
			parent.children.add(node);
			node.dimensionValues = new HashMap(parent.dimensionValues);
			node.dimensionValues.put(moneyFlowDimensionID, dimValues[i]);
			if (parent.dimensionPath != null)
				node.dimensionPath = parent.dimensionPath + "/" + dimValues[i];
			else 
				node.dimensionPath = dimValues[i];
			if (idx != dimensionIDs.size()-1)
				makeDimensionNodes(node, dimensionIDs, ++idx, productType);
		}
	}
	 
	/**
	 * Return a list with all leaf DimensionNodes in the resolved tree,
	 * so the list will contain every possible dimension-value-permutation.
	 */
	public List getDimensionLeafNodes(ProductType productType) {
		List leaves = new LinkedList();
		addDimensionLeaves(leaves, getRootNode(productType));
		return leaves;
	}
	
	/**
	 * Adds the DimensionNodes to the given list if it is a leaf.
	 * Recurses into the nodes children.
	 */
	private void addDimensionLeaves(List leaves, DimensionNode node)  {
		if (node.children.size() <= 0)
			leaves.add(node);
		else {
			for (Iterator iter = node.children.iterator(); iter.hasNext();) {
				DimensionNode childNode = (DimensionNode) iter.next();
				addDimensionLeaves(leaves, childNode);
			}
		}
	}
	
	/**
	 * Get all possible values of the MoneyFlowDimension with the given
	 * moneyFlowDimensionID.
	 * 
	 * @param moneyFlowDimensionID The id of the MoneyFlowDimension the the values should be searched for. 
	 */
	protected List getDimensionValues(String moneyFlowDimensionID, ProductType productType) {
		MoneyFlowDimension dimension = MoneyFlowDimension.getMoneyFlowDimension(getPersistenceManager(), moneyFlowDimensionID);
		if (dimension == null)
			throw new IllegalStateException("Could not find MoneyFlowDimension for id "+moneyFlowDimensionID+".");
		return Utils.array2ArrayList(dimension.getValues(productType));
	}
	
	
	
	/**
	 * @return The persistenceManager for this AccountantDelegate
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of PFMappingAccountantDelegate is not persistent. Can't get PersistenceManager");
		return pm;
	}
	
	
	protected void bookInvoiceTransfers(User user, Map bookInvoiceTransfers, BookMoneyTransfer container, Map involvedAnchors) {
		for (Iterator iter = bookInvoiceTransfers.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Anchor from = (Anchor)entry.getKey();
			Map toTransfers = (Map)entry.getValue();
			LOGGER.info("bookInvoiceTransfers for Anchor from "+from.getPrimaryKey());
			for (Iterator iterator = toTransfers.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry toEntry = (Map.Entry) iterator.next();
				Anchor to = (Anchor)toEntry.getKey();
				Collection transfers = (Collection)toEntry.getValue();
				long balance = 0;
				for (Iterator it = transfers.iterator(); it.hasNext();) {
					BookInvoiceTransfer transfer = (BookInvoiceTransfer) it.next();
					LOGGER.info("  bookInvoiceTransfers for Anchor to "+transfer.getTo().getPrimaryKey()+" with amount: "+transfer.getAmount());
					balance += transfer.getAmount();
				}
				Anchor aFrom = (balance > 0) ? from : to;
				Anchor aTo = (balance > 0) ? to : from;
				if (balance != 0) {
					InvoiceMoneyTransfer moneyTransfer = new InvoiceMoneyTransfer(
							InvoiceMoneyTransfer.BOOK_TYPE_BOOK,
							Accounting.getAccounting(getPersistenceManager()),
							container, aFrom, aTo,
							container.getInvoice(),
							Math.abs(balance)				
						);
//					getPersistenceManager().makePersistent(moneyTransfer); // done in constructor
					moneyTransfer.bookTransfer(user, involvedAnchors);					
				}
			}
		}
	}

	
	/* ************ Static helper functions ********************* */

	/**
	 * Helper method to get all LocalAccountantDelegates that are not inherited
	 * from another Delegate.
	 */
	public static Collection getTopLevelDelegates(PersistenceManager pm, Class delegateClass) {
		Query q = pm.newQuery(pm.getExtent(delegateClass,false));
		q.setFilter("this.extendedAccountantDelegate == null");
		return (Collection)q.execute();
	}

	/**
	 * Helper method to get all LocalAccountantDelegates that are children of
	 * the Delegate defined by the given organisationID and localAccountantDelegateID.
	 * 
	 * @param pm The PersistenceManager to use.
	 * @param organisationID The organisationID of the parent delegate.
	 * @param localAccountantDelegateID The localAccountantDelegateID of the parent delegate
	 */
	public static Collection getChildDelegates(
			PersistenceManager pm, 
			String organisationID, 
			String localAccountantDelegateID
		)
	{
		Query q = pm.newNamedQuery(LocalAccountantDelegate.class, QUERY_GET_CHILD_DELEGATES);
		return (Collection)q.execute(organisationID, localAccountantDelegateID);
	}
	
	
}
