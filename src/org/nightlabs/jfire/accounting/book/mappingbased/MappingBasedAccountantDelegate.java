/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.accounting.book.mappingbased;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.PriceFragment;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.book.BookMoneyTransfer;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.util.CollectionUtil;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.accounting.book.LocalAccountantDelegate"
 *                          detachable="true"
 *                          table="JFireTrade_PFMappingAccountantDelegate"
 * 
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.fetch-group name="MappingBasedAccountantDelegate.moneyFlowMappings"
 *                  fields="moneyFlowMappings"
 * @jdo.fetch-group name="MappingBasedAccountantDelegate.this"
 *                  fields="moneyFlowMappings"
 */
public class MappingBasedAccountantDelegate
		extends LocalAccountantDelegate
{

	public static final String FETCH_GROUP_MONEY_FLOW_MAPPINGS = "MappingBasedAccountantDelegate.moneyFlowMappings";

	public static final String FETCH_GROUP_THIS_MAPPING_BASED_ACCOUNTANT_DELEGATE = "MappingBasedAccountantDelegate.this";

	/**
	 * @jdo.field persistence-modifier="persistent" collection-type="collection"
	 *            element-type="MoneyFlowMapping"
	 *            mapped-by="localAccountantDelegate"
	 * 
	 */
	private Set<MoneyFlowMapping> moneyFlowMappings;
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(MappingBasedAccountantDelegate.class);

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	public MappingBasedAccountantDelegate()
	{
		super();
	}

	/**
	 * @param accountant
	 * @param productType
	 */
	public MappingBasedAccountantDelegate(String organisationID,
			String localAccountantDelegateID)
	{
		super(organisationID, localAccountantDelegateID);
		this.moneyFlowMappings = new HashSet<MoneyFlowMapping>();
	}

	/**
	 * @param parent
	 * @param organisationID
	 * @param localAccountantDelegateID
	 */
	public MappingBasedAccountantDelegate(LocalAccountantDelegate parent,
			String organisationID, String localAccountantDelegateID)
	{
		super(parent, organisationID, localAccountantDelegateID);
		this.moneyFlowMappings = new HashSet<MoneyFlowMapping>();
	}

	public void addMoneyFlowMapping(MoneyFlowMapping mapping)
	{
		if (mapping.getLocalAccountantDelegate() == null)
			mapping.setLocalAccountantDelegate(this);
		else {
			if (!JDOHelper.getObjectId(mapping.getLocalAccountantDelegate()).equals(
					JDOHelper.getObjectId(this)))
				mapping.setLocalAccountantDelegate(this);
		}
		moneyFlowMappings.add(mapping);
	}

	public void removeMoneyFlowMapping(MoneyFlowMapping mapping)
	{
		moneyFlowMappings.remove(mapping);
	}

	/**
	 * @return Returns the moneyFlowMappings.
	 */
	public Set<MoneyFlowMapping> getMoneyFlowMappings()
	{
		return moneyFlowMappings;
	}

	/* ************************* Resolve mapping code ************************* */

	/**
	 * Helper class used as key in the resolvedMappings Map.
	 */
	public static class ResolvedMapKey
			implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private ProductTypeID productTypeID;

		private String packageType;

		public ResolvedMapKey(ProductTypeID productTypeID, String packageType)
		{
			this.productTypeID = productTypeID;
			this.packageType = packageType;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			return obj.toString().equals(toString());
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return toString().hashCode();
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return packageType + "/" + productTypeID.toString();
		}

	}

	/**
	 * Helper class to hold resolved mappings for one ProductType
	 */
	public static class ResolvedMapEntry
			implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private ProductType productType;

		private int delegationLevel;

		/**
		 * key: String MoneyFlowMapping.getMappingKey(dimensionValues) value:
		 * MoneyFlowMapping mapping
		 */
		Map<String, MoneyFlowMapping> resolvedMappings = new HashMap<String, MoneyFlowMapping>();

		/**
		 * @return Returns the productType.
		 */
		public ProductType getProductType()
		{
			return productType;
		}

		/**
		 * @param productType
		 *          The productType to set.
		 */
		public void setProductType(ProductType productType)
		{
			this.productType = productType;
		}

		/**
		 * @return Returns the resolvedMappings.
		 */
		public Map<String, MoneyFlowMapping> getResolvedMappings()
		{
			return resolvedMappings;
		}

		/**
		 * @param resolvedMappings
		 *          The resolvedMappings to set.
		 */
		public void setResolvedMappings(
				Map<String, MoneyFlowMapping> resolvedMappings)
		{
			this.resolvedMappings = resolvedMappings;
		}

		/**
		 * @return Returns the delegationLevel
		 */
		public int getDelegationLevel()
		{
			return delegationLevel;
		}

		/**
		 * @param wasDelegated
		 *          The wasDelegated to set.
		 */
		public void setDelegationLevel(int delegationLevel)
		{
			this.delegationLevel = delegationLevel;
		}
	}

	/**
	 * Inferface used when resolving Mappings.
	 */
	public static interface ResolveProductTypeProvider
	{
		ProductType getProductType();

		String getPackageType();

		Collection<ResolveProductTypeProvider> getNestedProviders();
	}

	/**
	 * ResloveProvider on basis of the ProductType structure
	 */
	private static class ProductTypeProvider
			implements ResolveProductTypeProvider
	{
		private ProductType productType;

		private String packageType;

		public ProductTypeProvider(String packageType, ProductType productType)
		{
			this.productType = productType;
			this.packageType = packageType;
		}

		public ProductType getProductType()
		{
			return productType;
		}

		public String getPackageType()
		{
			return packageType;
		}

		private List<ResolveProductTypeProvider> nested;

		public Collection<ResolveProductTypeProvider> getNestedProviders()
		{
			if (nested == null) {
				nested = new LinkedList<ResolveProductTypeProvider>();
				for (NestedProductTypeLocal nestedType : productType.getProductTypeLocal().getNestedProductTypeLocals()) {
					nested.add(new ProductTypeProvider(MappingBasedAccountantDelegate
							.getPackageType(nestedType), nestedType.getInnerProductTypeLocal().getProductType()));
				}
			}
			return nested;
		}
	}

	/**
	 * ResloveProvider based on an ArticlePrice and its nested ones, so on the
	 * snapshot at Offer finalization.
	 */
	private static class ArticlePriceTypeProvider
			implements ResolveProductTypeProvider
	{
		private ArticlePrice articlePrice;

		private String packageType;

		public ArticlePriceTypeProvider(String packageType,
				ArticlePrice articlePrice)
		{
			this.articlePrice = articlePrice;
			this.packageType = packageType;
		}

		public ProductType getProductType()
		{
			return articlePrice.getProductType();
		}

		public String getPackageType()
		{
			return packageType;
		}

		private List<ResolveProductTypeProvider> nested;

		public Collection<ResolveProductTypeProvider> getNestedProviders()
		{
			if (nested == null) {
				nested = new LinkedList<ResolveProductTypeProvider>();
				for (ArticlePrice nestedPrice : articlePrice.getNestedArticlePrices()) {
					nested.add(new ArticlePriceTypeProvider(MappingBasedAccountantDelegate
							.getPackageType(nestedPrice), nestedPrice));
				}
			}
			return nested;
		}
	}

	/**
	 * Resolves all MoneyFlowMappings for the ProductTypes involved in the given
	 * invoice. For a documentation on the structure of the result see
	 * {@link #resolveProductTypeMappings(ProductType)}
	 * 
	 * @param invoice
	 *          The Invoice mappings should be resolved for.
	 * @return A map with key {@link ResolvedMapKey} and value
	 *         {@link ResolvedMapEntry}
	 */
	public Map<ResolvedMapKey, ResolvedMapEntry> resolveProductTypeMappings(
			Invoice invoice)
	{
		 Map<ResolvedMapKey, ResolvedMapEntry> result = new HashMap<ResolvedMapKey, ResolvedMapEntry>();
		for (Article article : invoice.getArticles()) {
			ArticlePriceTypeProvider provider = new ArticlePriceTypeProvider(
					getPackageType(article.getPrice()), article.getPrice());
			resolveProductTypeMappings(provider, provider.getPackageType(), result, 0);
		}
		return result;
	}

	/**
	 * Resolves all MoneyFlowMappings for the given productType and all its nested
	 * ProductTypes.
	 * 
	 * A Map with key {@link ResolvedMapKey} and value {@link ResolvedMapEntry} is
	 * returned here. Within the ResolvedMapEntries mappings are stored in an Map
	 * with key (String) mappingKey and value {@link MoneyFlowMapping}. Upon
	 * resolving mappings the delegation hierarchy and the productType hierarchy
	 * are iterated from top down. Hereby all mappings that match mappings will be added to the
	 * resolved map with a fake mapping-key pretending that the mapping was made
	 * for the ProductType searched for. By that mappings from child delegates and
	 * for child-ProductTypes will overwrite the ones from their parents. For
	 * example:
	 * 
	 * <pre>
	 *   PType2  ---extends---&gt; PType1
	 *   Delegate1
	 *     |    -&gt; Mapping: PType2 (dimensions) -&gt; Account1
	 *     |
	 *   extends
	 *     |
	 *     |
	 *   Delegate2
	 *          -&gt; Mapping: PType1 (dimensions) -&gt; Account2
	 *          -&gt; Mapping: PType2 (dimensions) -&gt; Account3
	 * </pre>
	 * 
	 * If now Delegate2 is asked for the mappings of PType2, this will result in
	 * one Mapping pointing to Account3. First the mapping of Delegate1(parent) is
	 * added. Then the mapping for PType1(pType-parent) will overwrite the mapping
	 * just to be overwritten finally by the mapping for PType2.
	 * 
	 * @param productType
	 *          The ProductType to resolve mappings for.
	 * @return A map with key {@link ResolvedMapKey} and value
	 *         {@link ResolvedMapEntry}
	 */
	public Map<ResolvedMapKey, ResolvedMapEntry> resolveProductTypeMappings(
			ProductType productType)
	{
		 Map<ResolvedMapKey, ResolvedMapEntry> result = new HashMap<ResolvedMapKey, ResolvedMapEntry>();
		resolveProductTypeMappings(productType, result, 0);
		return result;
	}

	/**
	 * Resolves mappings for the given productType and
	 * {@link MoneyFlowMapping#PACKAGE_TYPE_PACKAGE} by calling
	 * {@link #resolveProductTypeMappings(ProductType, String, Map, int)}.
	 * 
	 * @param productType
	 *          The ProductType to resolve mappings for
	 * @param resolvedMappings
	 *          The Map to store the resolved ResolvedMapEntries
	 * @param delegationLevel
	 *          The current delegationLevel (>0 indicates that the current this
	 *          was delegated to this LocalAccountantDelegate)
	 * @see #resolveProductTypeMappings(ProductType, String, Map, int)
	 */
	public void resolveProductTypeMappings(ProductType productType,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			int delegationLevel)
	{
		ProductTypeProvider provider = new ProductTypeProvider(
				getPackageType(productType), productType);
		resolveProductTypeMappings(provider, provider.getPackageType(),
				resolvedMappings, delegationLevel);
	}

	/**
	 * Resolves the mappings for the given productType. If there are Mappings
	 * defined within this LocalAccountantDelegate for the given ProductType and
	 * packageType, then the configuration is defined by this delegate for the
	 * given ProductType and packageType. This method will resolve the mappings
	 * for the given productType and recurse into its nested ProductTypes. If no
	 * mapping was defined for the given ProductType or one of its Parents neither
	 * in this delegate nor in one of its parents, then the
	 * LocalAccountantDelegate of the given ProductType will be asked to resolve
	 * the Mappings according to its mapping-configuration.
	 * 
	 * @param productTypeProvider
	 *          The ProductTypeProvider to get the ProductType structure to
	 *          resolve for.
	 * @param packageType
	 *          The packageType mappings should be resolved for
	 * @param resolvedMappings
	 *          The Map to store the resolved ResolvedMapEntries
	 * @param delegationLevel
	 *          The current delegationLevel (>0 indicates that the current this
	 *          was delegated to this LocalAccountantDelegate)
	 */
	public void resolveProductTypeMappings(
			ResolveProductTypeProvider productTypeProvider, String packageType,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			int delegationLevel)
	{
		ResolvedMapEntry entry = addProductTypeMappings(
				productTypeProvider.getProductType(),
				productTypeProvider.getPackageType(),
				delegationLevel);

		if (entry != null) {
			entry.setDelegationLevel(delegationLevel);
			ResolvedMapKey entryKey = new ResolvedMapKey((ProductTypeID) JDOHelper.getObjectId(entry.getProductType()), packageType);
			resolvedMappings.put(entryKey, entry);
			for (ResolveProductTypeProvider nested : productTypeProvider.getNestedProviders()) {
				resolveProductTypeMappings(nested, nested.getPackageType(),
						resolvedMappings, delegationLevel);
			}
		}
		else {
			LocalAccountantDelegate _delegate = productTypeProvider.getProductType().getProductTypeLocal().getLocalAccountantDelegate();
			if (_delegate != null && !(_delegate instanceof MappingBasedAccountantDelegate))
				return; // not compatible

			MappingBasedAccountantDelegate delegate = (MappingBasedAccountantDelegate) _delegate;
			if (delegate == null)
				return; // No delegate assigned to the productType

			if (JDOHelper.getObjectId(delegate).equals(JDOHelper.getObjectId(this)))
				return; // We are the delegate assigned to the productType

			delegate.resolveProductTypeMappings(productTypeProvider, packageType, resolvedMappings, delegationLevel + 1);
		}
	}

	/**
	 * Returns a ResolvedMapEntry with all Mappings concerning the given
	 * productType or null if nothing is defined for the given productType or one
	 * of its parents within this delegate or one of its parents. See
	 * {@link #resolveProductTypeMappings(ProductType)} on how mappings can
	 * overwrite each other concerning delegate- and productType-hierarcy.
	 */
	protected ResolvedMapEntry addProductTypeMappings(ProductType productType,
			String packageType, int delegationLevel)
	{
		LinkedList<LocalAccountantDelegate> delegateHierarchy = new LinkedList<LocalAccountantDelegate>();
		LocalAccountantDelegate delegateRun = this;
		while (delegateRun != null) {
			delegateHierarchy.add(delegateRun);
			delegateRun = delegateRun.getExtendedAccountantDelegate();
		}

		ResolvedMapEntry entry = null;
		// go through the delegate hierarchy
		while (!delegateHierarchy.isEmpty()) {
			LocalAccountantDelegate _delegate = (LocalAccountantDelegate) delegateHierarchy
					.removeLast();
			if (!(_delegate instanceof MappingBasedAccountantDelegate)) {
				throw new IllegalStateException(
						"There is an inconsistencey in the hierarchy of the LocalAccountantDelegate: "
								+ _delegate.getLocalAccountantDelegateID()
								+ " one of its nodes is not an "
								+ MappingBasedAccountantDelegate.class.getSimpleName());
			}
			MappingBasedAccountantDelegate delegate = (MappingBasedAccountantDelegate) _delegate;
			LinkedList<ProductType> productTypeHierarchy = new LinkedList<ProductType>();
			ProductType pTypeRun = productType;
			while (pTypeRun != null) {
				productTypeHierarchy.add(pTypeRun);
				pTypeRun = pTypeRun.getExtendedProductType();
			}
			// go through the productType hierarchy
			// and add all matching mappings to
			while (!productTypeHierarchy.isEmpty()) {
				ProductType pType = productTypeHierarchy.removeLast();
				for (MoneyFlowMapping mapping : delegate.getMoneyFlowMappings()) {
					if (mapping.matches(pType, packageType)) {
						if (entry == null) {
							entry = new ResolvedMapEntry();
							entry.setProductType(productType);
							entry.setDelegationLevel(delegationLevel);
						}
						// The entries made here by the mapping should
						// fake the mapping key and pretend the mapping was made on
						// exactly the productType the mapping was searched for.
						// Doing so mappings from parent-delegates as well as for
						// parent-productTypes are overwritten by children declarations
						mapping.addMappingsToMap(productType, entry.getResolvedMappings());
					}
				}
			}
		}
		return entry;
	}

	/**
	 * key: Invoice invoice<br/> value: Map resolvedMappings
	 * 
	 * Used to store resolved mappings temporaly per invoice-book
	 * 
	 * @jdo.field persistence-modifier="none"
	 */
	private Map<Invoice, Map<ResolvedMapKey, ResolvedMapEntry>> resolvedPTypeMappings = new HashMap<Invoice, Map<ResolvedMapKey, ResolvedMapEntry>>();

	/**
	 * @see org.nightlabs.jfire.accounting.book.LocalAccountantDelegate#preBookArticles(org.nightlabs.jfire.trade.OrganisationLegalEntity,
	 *      org.nightlabs.jfire.security.User,
	 *      org.nightlabs.jfire.accounting.Invoice, BookMoneyTransfer, Map)
	 */
	@Override
	public void preBookArticles(OrganisationLegalEntity mandator, User user,
			Invoice invoice, BookMoneyTransfer bookTransfer,
			Set<Anchor> involvedAnchors)
	{
		if (resolvedPTypeMappings.containsKey(invoice))
			return;
		resolvedPTypeMappings.put(invoice, resolveProductTypeMappings(invoice));
	}

	/**
	 * @see org.nightlabs.jfire.accounting.book.LocalAccountantDelegate#postBookArticles(org.nightlabs.jfire.trade.OrganisationLegalEntity,
	 *      org.nightlabs.jfire.security.User,
	 *      org.nightlabs.jfire.accounting.Invoice, BookMoneyTransfer, Map)
	 */
	@Override
	public void postBookArticles(OrganisationLegalEntity mandator, User user,
			Invoice invoice, BookMoneyTransfer bookTransfer,
			Set<Anchor> involvedAnchors)
	{
		if (resolvedPTypeMappings.containsKey(invoice))
			resolvedPTypeMappings.remove(invoice);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.book.LocalAccountantDelegate#bookArticle(org.nightlabs.jfire.trade.OrganisationLegalEntity,
	 *      org.nightlabs.jfire.security.User,
	 *      org.nightlabs.jfire.accounting.Invoice,
	 *      org.nightlabs.jfire.trade.ArticlePrice,
	 *      org.nightlabs.jfire.accounting.book.BookMoneyTransfer, Map)
	 */
	@Override
	public void bookArticle(OrganisationLegalEntity mandator, User user,
			Invoice invoice, Article article, BookMoneyTransfer container,
			Set<Anchor> involvedAnchors)
	{
		Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings = resolvedPTypeMappings
				.get(invoice);
		if (resolvedMappings == null)
			throw new IllegalStateException(
					"Could not find resolved mappings for invoice "
							+ JDOHelper.getObjectId(invoice)
							+ " can not book article. Was preBookInvoice() called?");
		LinkedList<ArticlePrice> articlePrices = new LinkedList<ArticlePrice>();
		PersistenceManager pm = getPersistenceManager();
		PriceFragmentType totalType = PriceFragmentType
				.getTotalPriceFragmentType(pm);
		PriceFragmentType restType = new PriceFragmentType(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_REST.organisationID,
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_REST.priceFragmentTypeID);
		restType.setContainerPriceFragmentType(totalType);
		ArticlePrice articlePrice = article.getPrice();
		prepareArticlePrice(totalType, restType, articlePrice);
		articlePrices.add(articlePrice);
		/*
		 * key: Anchor from value: Map toTransfers key: Anchor to value: Collection
		 * of BookInvoiceTransfer transfers
		 */
		Map<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>> bookInvoiceTransfers = new HashMap<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>>();

		bookProductTypeParts(mandator, user, resolvedMappings, articlePrices,
				bookInvoiceTransfers, 0, container, involvedAnchors);
		bookInvoiceTransfers(user, bookInvoiceTransfers, container, involvedAnchors);
	}

	@Override
	public void bookProductTypeParts(OrganisationLegalEntity mandator, User user,
			LinkedList<ArticlePrice> articlePriceStack, int delegationLevel,
			BookMoneyTransfer container, Set<Anchor> involvedAnchors)
	{
		ArticlePrice articlePrice = articlePriceStack.peek();
		ProductTypeID pTypeID = (ProductTypeID) JDOHelper.getObjectId(articlePrice
				.getProductType());
		ProductType pType = (ProductType) getPersistenceManager().getObjectById(
				pTypeID);
		Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings = resolveProductTypeMappings(pType);
		Map<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>> bookInvoiceTransfers = new HashMap<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>>();
		bookProductTypeParts(mandator, user, resolvedMappings, articlePriceStack,
				bookInvoiceTransfers, delegationLevel, container, involvedAnchors);
	}

	/* *************************** BOOK Logic ********************************* */

	private void delegateBookProductTypeParts(
			OrganisationLegalEntity mandator,
			User user,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			ProductType productType,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>> bookInvoiceTransfers,
			int delegationLevel, BookMoneyTransfer container,
			Set<Anchor> involvedAnchors)
	{
		LocalAccountantDelegate delegate = productType.getProductTypeLocal().getLocalAccountantDelegate();
		if (delegate instanceof MappingBasedAccountantDelegate) {
			// Have to delegate the booking of this product-type to its own delegate
			((MappingBasedAccountantDelegate) delegate).bookProductTypeParts(mandator,
					user, resolvedMappings, articlePriceStack, bookInvoiceTransfers,
					delegationLevel + 1, container, involvedAnchors);
			return;
		}
		else {
			delegate.bookProductTypeParts(mandator, user, articlePriceStack,
					delegationLevel + 1, container, involvedAnchors);
		}
	}

	/**
	 * Tries to book all money concerning the given ProductType. It is intended to
	 * be called from the implementation of
	 * {@link #bookArticle(OrganisationLegalEntity, User, Invoice, Article, BookMoneyTransfer, Map)}
	 * with the top-level ArticlePrice in the articlePriceStack parameter.
	 * 
	 * Looks up the resolved mapping entries and decides on basis of the
	 * delegationLevel in the entries whether to book the money itself or delegate
	 * to the LocalAccountantDelegate assigned to the ProductType currently in the
	 * stack an call this method for this delegate.
	 * 
	 * If no delegation is done this implementation of bookProductTypeParts will
	 * call abstract
	 * {@link #internalBookProductTypeParts(OrganisationLegalEntity, User, Map, LinkedList, ArticlePrice, ProductType, String, int, BookMoneyTransfer, Map)}
	 * which should do the job.
	 * 
	 * @param mandator
	 *          The mandator to book for.
	 * @param user
	 *          The user that initiated the booking.
	 * @param resolvedMappings
	 *          The map of resolved MoneyFlowMappings.
	 * @param articlePriceStack
	 *          A Stack of ArticlePrices representing the ProductType packaging
	 * @param delegationLevel
	 *          The level of delegation calls to this method
	 * @param container
	 *          The Container transfer, that is the transfer from the customer to
	 *          the vendor of the invoice
	 * @param involvedAnchors
	 *          A List of involved Anchors, so they can be checked after the
	 *          booking
	 */
	public void bookProductTypeParts(
			OrganisationLegalEntity mandator,
			User user,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>> bookInvoiceTransfers,
			int delegationLevel, BookMoneyTransfer container,
			Set<Anchor> involvedAnchors)
	{
		ArticlePrice articlePrice = articlePriceStack.peek();
		ProductType productType = articlePrice.getProductType();
		// boolean topLevel = articlePrice.getPackageArticlePrice() == null;
		String packageType = getPackageType(articlePrice);

		int nextDelegationLevel = getDelegationLevel(resolvedMappings, productType,
				packageType);

		if ((nextDelegationLevel > 0) && (delegationLevel < nextDelegationLevel)) {
			delegateBookProductTypeParts(mandator, user, resolvedMappings,
					productType, articlePriceStack, bookInvoiceTransfers,
					delegationLevel, container, involvedAnchors);
		}

		internalBookProductTypeParts(mandator, user, resolvedMappings,
				articlePriceStack, articlePrice, bookInvoiceTransfers, productType,
				packageType, delegationLevel, container, involvedAnchors);

	}

	/**
	 * Can be used as default implementation of
	 * {@link #internalBookProductTypeParts(OrganisationLegalEntity, User, Map, LinkedList, ArticlePrice, ProductType, String, int, BookMoneyTransfer, Map)}.
	 * Spans all possible dimension values and books the amount to the appropriate
	 * Account based on the (Dimension)-Mappings in the resolvedMappings Map.
	 */
	protected void internalBookProductTypePartsByDimension(
			OrganisationLegalEntity mandator,
			User user,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			LinkedList<ArticlePrice> articlePriceStack,
			ArticlePrice articlePrice,
			Map<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>> bookInvoiceTransfers,
			ProductType productType, String packageType, int delegationLevel,
			BookMoneyTransfer container, Set<Anchor> involvedAnchors)
	{

		logger.info("bookProductTypePartsByDimension for article with PType "
				+ articlePrice.getProductType().getName().getText("en"));
		if (haveToDelegateBooking(resolvedMappings, productType, packageType,
				delegationLevel)) {
			delegateBookProductTypeParts(mandator, user, resolvedMappings,
					productType, articlePriceStack, bookInvoiceTransfers,
					delegationLevel, container, involvedAnchors);
			return;
		}
		Currency currency = articlePrice.getCurrency();

		// List dimensionIDs = getMoneyFlowDimensionIDs();
		Article bookArticle = articlePriceStack.get(0).getArticle();

		List dimensions = getDimensionLeafNodes(bookArticle, productType);

		for (Iterator iter = dimensions.iterator(); iter.hasNext();) {
			DimensionNode dimensionNode = (DimensionNode) iter.next();
			MoneyFlowMapping mapping = getMoneyFlowMapping(resolvedMappings,
					productType, packageType, dimensionNode.getDimensionValues(),
					currency);

			if (mapping != null) {

				// have configuration for this productType, packageType and
				// dimensionValues
				// resolve the anchor to get the money from
				Collection bookTransfers = getBookInvoiceTransfersForDimensionValues(
						mandator, articlePriceStack, dimensionNode.getDimensionValues(),
						mapping, resolvedMappings, container);
				for (Iterator iterator = bookTransfers.iterator(); iterator.hasNext();) {
					BookInvoiceTransfer biTransfer = (BookInvoiceTransfer) iterator
							.next();
					if (biTransfer.getFrom() == null
							|| biTransfer.getFrom().getPrimaryKey().equals(
									biTransfer.getTo().getPrimaryKey()))
						biTransfer.setFrom(mandator);

					// reverse if amount negative
					if (biTransfer.getAmount() < 0) {
						Anchor tmpAnchor = biTransfer.getFrom();
						biTransfer.setFrom(biTransfer.getTo());
						biTransfer.setTo(tmpAnchor);
					}

					if (biTransfer.getFrom().getPrimaryKey().equals(
							biTransfer.getTo().getPrimaryKey()))
						continue;

					Map<Anchor, Collection<BookInvoiceTransfer>> toTransfers = bookInvoiceTransfers
							.get(biTransfer.getFrom());
					if (toTransfers == null) {
						toTransfers = new HashMap<Anchor, Collection<BookInvoiceTransfer>>();
						bookInvoiceTransfers.put(biTransfer.getFrom(), toTransfers);
					}

					Collection<BookInvoiceTransfer> transfers = toTransfers
							.get(biTransfer.getTo());
					if (transfers == null) {
						transfers = new LinkedList<BookInvoiceTransfer>();
						toTransfers.put(biTransfer.getTo(), transfers);
					}
					transfers.add(biTransfer);
				}
			} // if (mapping != null)

		} // for (Iterator iter = articlePrice.getFragments().iterator();
			// iter.hasNext();) {
		// recurse
		for (ArticlePrice nestedArticlePrice : articlePrice.getNestedArticlePrices()) {
			articlePriceStack.addFirst(nestedArticlePrice);
			bookProductTypeParts(mandator, user, resolvedMappings, articlePriceStack,
					bookInvoiceTransfers, delegationLevel, container, involvedAnchors);
		}

		articlePriceStack.poll();
	}

	/**
	 * Used to accumulate transfer information for the booking of one invoice.
	 */
	public static class BookInvoiceTransfer
	{
		private Anchor from;

		private Anchor to;

		private long amount;

		public BookInvoiceTransfer(Anchor from, Anchor to, long amount)
		{
			this.from = from;
			this.to = to;
			this.amount = amount;
		}

		/**
		 * @return Returns the amount.
		 */
		public long getAmount()
		{
			return amount;
		}

		/**
		 * @param amount
		 *          The amount to set.
		 */
		public void setAmount(long amount)
		{
			this.amount = amount;
		}

		/**
		 * @return Returns the from.
		 */
		public Anchor getFrom()
		{
			return from;
		}

		/**
		 * @param from
		 *          The from to set.
		 */
		public void setFrom(Anchor from)
		{
			this.from = from;
		}

		/**
		 * @return Returns the to.
		 */
		public Anchor getTo()
		{
			return to;
		}

		/**
		 * @param to
		 *          The to to set.
		 */
		public void setTo(Anchor to)
		{
			this.to = to;
		}

		@Override
		public String toString()
		{
			return "BookInvoiceTransfer from " + from.getAnchorID() + " to "
					+ to.getAnchorID() + " amount " + getAmount();
		}
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
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			ProductType productType, String packageType,
			Map<String, String> dimensionValues, Currency currency)
	{
		return getMoneyFlowMapping(resolvedMappings, (ProductTypeID) JDOHelper
				.getObjectId(productType), packageType, dimensionValues, currency
				.getCurrencyID());
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
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			ProductTypeID productTypeID, String packageType,
			Map<String, String> dimensionValues, String currencyID)
	{
		ResolvedMapKey key = new ResolvedMapKey(productTypeID, packageType);
		ResolvedMapEntry entry = resolvedMappings.get(key);
		if (entry == null)
			return null;
		MoneyFlowMapping mapping = entry.getResolvedMappings()
				.get(
						getMoneyFlowMappingKey(productTypeID, packageType, dimensionValues,
								currencyID));
		return mapping;
	}

	/**
	 * Checks whether the resolving of mappings and the booking of article has to
	 * be delegated to the LocalAccountantDelegate of the givenProductType. This
	 * will be true if no mapping was defined for this ProductType (or parents)
	 * within this LocalAccountantDelegate (or parents).
	 */
	protected boolean haveToDelegateBooking(Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			ProductType productType, String packageType, int delegationLevel)
	{
		ResolvedMapKey key = new ResolvedMapKey((ProductTypeID)JDOHelper.getObjectId(productType), packageType);
		ResolvedMapEntry entry = resolvedMappings.get(key);
		if (entry == null)
			return false;
		else
			return entry.getDelegationLevel() > delegationLevel;
	}

	/**
	 * Returns the delegationLevel in the ResolvedMapEntry found for the given
	 * productType and packageType.
	 */
	protected int getDelegationLevel(Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			ProductType productType, String packageType)
	{
		ResolvedMapKey key = new ResolvedMapKey((ProductTypeID)JDOHelper.getObjectId(productType), packageType);
		ResolvedMapEntry entry = resolvedMappings.get(key);
		if (entry == null)
			return 0;
		else
			return entry.getDelegationLevel();
	}

	/**
	 * Returns the package type of the given ProductType. It will be
	 * {@link MoneyFlowMapping#PACKAGE_TYPE_INNER} if the ProductType is virtually
	 * self packaged in the given articlePrice or the package-nature of the
	 * ProductType is {@link ProductType#PACKAGE_NATURE_INNER}. Will return
	 * {@link MoneyFlowMapping#PACKAGE_TYPE_PACKAGE} otherwise.
	 */
	public static String getPackageType(ArticlePrice articlePrice)
	{
		return (articlePrice.isVirtualInner() || articlePrice.getProductType()
				.isPackageInner()) ? MoneyFlowMapping.PACKAGE_TYPE_INNER
				: MoneyFlowMapping.PACKAGE_TYPE_PACKAGE;
	}

	/**
	 * Returns the package type of the given ProductType. It will be
	 * {@link MoneyFlowMapping#PACKAGE_TYPE_PACKAGE} if the ProductType is of
	 * package nature PACKAGE_NATURE_OUTER and has a packagePriceConfig assigned.
	 * Will return {@link MoneyFlowMapping#PACKAGE_TYPE_INNER} otherwise.
	 */
	public static String getPackageType(ProductType productType)
	{
		if (productType.isPackageOuter()
				&& (productType.getPackagePriceConfig() == null))
			throw new IllegalStateException(
					"ProductType "
							+ productType.getPrimaryKey()
							+ " has package-nature PACKAGE_NATURE_OUTER but not packagePriceConfig assigned.");

		return (productType.isPackageOuter()) ? MoneyFlowMapping.PACKAGE_TYPE_PACKAGE
				: MoneyFlowMapping.PACKAGE_TYPE_INNER;
	}

	/**
	 * Returns the package type of the given NestedProductTypeLocal. It will be
	 * {@link MoneyFlowMapping#PACKAGE_TYPE_INNER} if the ProductType is virtually
	 * self packaged in the given the package-nature of the nested inner
	 * ProductType is {@link ProductType#PACKAGE_NATURE_INNER}. Will return
	 * {@link MoneyFlowMapping#PACKAGE_TYPE_PACKAGE} otherwise.
	 */
	public static String getPackageType(NestedProductTypeLocal nestedProductTypeLocal)
	{
		String innerPPK = nestedProductTypeLocal.getInnerProductTypePrimaryKey();
		String packagePPK = nestedProductTypeLocal.getPackageProductTypeLocal()
				.getPrimaryKey();
		return (packagePPK.equals(innerPPK) || nestedProductTypeLocal
				.getInnerProductTypeLocal().getProductType().isPackageInner()) ? MoneyFlowMapping.PACKAGE_TYPE_INNER
				: MoneyFlowMapping.PACKAGE_TYPE_PACKAGE;
	}

	/* ************************* Resolve dimension code *********************** */
	/**
	 * Helper class to resolve all dimension values
	 */
	protected static class DimensionNode
	{
		String dimensionPath;

		Map<String, String> dimensionValues = new HashMap<String, String>();

		DimensionNode parentDimension;

		List<DimensionNode> children = new LinkedList<DimensionNode>();

		/**
		 * @return Returns the children.
		 */
		public List<DimensionNode> getChildren()
		{
			return children;
		}

		/**
		 * @param children
		 *          The children to set.
		 */
		public void setChildren(List<DimensionNode> children)
		{
			this.children = children;
		}

		/**
		 * @return Returns the dimensionPath.
		 */
		public String getDimensionPath()
		{
			return dimensionPath;
		}

		/**
		 * @param dimensionPath
		 *          The dimensionPath to set.
		 */
		public void setDimensionPath(String dimensionPath)
		{
			this.dimensionPath = dimensionPath;
		}

		/**
		 * @return Returns the dimensionValues.
		 */
		public Map<String, String> getDimensionValues()
		{
			return dimensionValues;
		}

		/**
		 * @param dimensionValues
		 *          The dimensionValues to set.
		 */
		public void setDimensionValues(Map<String, String> dimensionValues)
		{
			this.dimensionValues = dimensionValues;
		}

		/**
		 * @return Returns the parentDimension.
		 */
		public DimensionNode getParentDimension()
		{
			return parentDimension;
		}

		/**
		 * @param parentDimension
		 *          The parentDimension to set.
		 */
		public void setParentDimension(DimensionNode parentDimension)
		{
			this.parentDimension = parentDimension;
		}

		@Override
		public String toString()
		{
			return dimensionPath;
		}
	}

	/**
	 * Returns a node with all dimensionValues in a tree structure.
	 * 
	 * @param bookArticle
	 *          TODO
	 */
	public DimensionNode getRootNode(Article bookArticle, ProductType productType)
	{
		DimensionNode node = new DimensionNode();
		List<String> dimensionIDs = getMoneyFlowDimensionIDs();
		makeDimensionNodes(node, dimensionIDs, 0, bookArticle, productType);
		return node;
	}

	/**
	 * Build DimensionNodes for all dimensions in the level given by idx as
	 * children of the given parent and then recurses into the next level.
	 * 
	 * @param bookArticle
	 *          TODO
	 */
	private void makeDimensionNodes(DimensionNode parent,
			List<String> dimensionIDs, int idx, Article bookArticle,
			ProductType productType)
	{
		String moneyFlowDimensionID = dimensionIDs.get(idx);
		String[] dimValues = CollectionUtil.collection2TypedArray(
				getDimensionValues(moneyFlowDimensionID, bookArticle, productType),
				String.class);
		for (int i = 0; i < dimValues.length; i++) {
			DimensionNode node = new DimensionNode();
			parent.children.add(node);
			node.dimensionValues = new HashMap<String, String>(parent.dimensionValues);
			node.dimensionValues.put(moneyFlowDimensionID, dimValues[i]);
			if (parent.dimensionPath != null)
				node.dimensionPath = parent.dimensionPath + "/" + dimValues[i];
			else
				node.dimensionPath = dimValues[i];
			if (idx != dimensionIDs.size() - 1)
				makeDimensionNodes(node, dimensionIDs, ++idx, bookArticle, productType);
		}
	}

	/**
	 * Return a list with all leaf DimensionNodes in the resolved tree, so the
	 * list will contain every possible dimension-value-permutation.
	 * 
	 * @param bookArticle
	 *          TODO
	 */
	public List<DimensionNode> getDimensionLeafNodes(Article bookArticle,
			ProductType productType)
	{
		List<DimensionNode> leaves = new LinkedList<DimensionNode>();
		addDimensionLeaves(leaves, getRootNode(bookArticle, productType));
		return leaves;
	}

	/**
	 * Adds the DimensionNodes to the given list if it is a leaf. Recurses into
	 * the nodes children.
	 */
	private void addDimensionLeaves(List<DimensionNode> leaves, DimensionNode node)
	{
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
	 * @param moneyFlowDimensionID
	 *          The id of the MoneyFlowDimension the the values should be searched
	 *          for.
	 * @param bookArticle
	 *          TODO
	 */
	protected List<String> getDimensionValues(String moneyFlowDimensionID,
			Article bookArticle, ProductType productType)
	{
		MoneyFlowDimension dimension = MoneyFlowDimension.getMoneyFlowDimension(
				getPersistenceManager(), moneyFlowDimensionID);
		if (dimension == null)
			throw new IllegalStateException(
					"Could not find MoneyFlowDimension for id " + moneyFlowDimensionID
							+ ".");
		return CollectionUtil.array2ArrayList(dimension.getValues(productType,
				bookArticle));
	}

	protected void bookInvoiceTransfers(
			User user,
			Map<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>> bookInvoiceTransfers,
			BookMoneyTransfer container, Set<Anchor> involvedAnchors)
	{
		PersistenceManager pm = getPersistenceManager();
		OrganisationLegalEntity mandator = Accounting.getAccounting(pm).getMandator();
		Invoice invoice = container.getInvoice();
		boolean revertTransferDirection = false;
		if (invoice.getCustomer().equals(mandator)) {
			// if the local organisation is the customer of the invoice
			// we revert the transfer direction of ALL resolved transfers!
			revertTransferDirection = true;
		}
		for (Iterator iter = bookInvoiceTransfers.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Anchor from = (Anchor) entry.getKey();
			Map toTransfers = (Map) entry.getValue();
			logger.info("Starting book BookInvoiceTransfers from "
					+ from.getPrimaryKey());
			for (Iterator iterator = toTransfers.entrySet().iterator(); iterator
					.hasNext();) {
				Map.Entry toEntry = (Map.Entry) iterator.next();
				Anchor to = (Anchor) toEntry.getKey();
				Collection transfers = (Collection) toEntry.getValue();
				long balance = 0;
				for (Iterator it = transfers.iterator(); it.hasNext();) {
					BookInvoiceTransfer transfer = (BookInvoiceTransfer) it.next();
					logger.info("  bookInvoiceTransfers to "
							+ transfer.getTo().getPrimaryKey() + " with amount: "
							+ transfer.getAmount());
					balance += transfer.getAmount();
				}
				// revert direction of transfers with negative amount.
				Anchor aFrom = (balance > 0) ? from : to;
				Anchor aTo = (balance > 0) ? to : from;
				
				if (revertTransferDirection) {
					Anchor tmp = aFrom;
					aFrom = aTo;
					aTo = tmp;
				}
				
				if (balance != 0) {
					InvoiceMoneyTransfer moneyTransfer = new InvoiceMoneyTransfer(
							InvoiceMoneyTransfer.BOOK_TYPE_BOOK, container, aFrom, aTo,
							container.getInvoice(), Math.abs(balance));
					moneyTransfer = pm
							.makePersistent(moneyTransfer);
					moneyTransfer.bookTransfer(user, involvedAnchors);
				}
			}
		}
	}

	/**
	 * Returns the mapping key for the given combination of ProductType,
	 * packageType and dimension values stored in the given Map.
	 * 
	 * @param productTypeID
	 *          The ProductType a mapping is searched for.
	 * @param packageType
	 *          The packageType for the searched mapping.
	 * @param dimensionValues
	 *          A Map with defined values for all dimensions of this delegate.
	 *          (key: String value: String)
	 * @param currency
	 *          The currency for the searched mapping.
	 */
	public String getMoneyFlowMappingKey(ProductTypeID productTypeID,
			String packageType, Map<String, String> dimensionValues, String currencyID)
	{
		String mappingKey = MoneyFlowMapping.getMappingKey(ProductType
				.getPrimaryKey(productTypeID.organisationID,
						productTypeID.productTypeID), packageType, currencyID);
		for (String dimensionID : getMoneyFlowDimensionIDs()) {
			mappingKey = mappingKey + "/"
					+ (dimensionValues.get(dimensionID));
		}
		return mappingKey;
	}

	public static List<String> DIMENSION_IDS = CollectionUtil
			.array2ArrayList(new String[] { OwnerDimension.MONEY_FLOW_DIMENSION_ID,
					SourceOrganisationDimension.MONEY_FLOW_DIMENSION_ID,
					PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID });

	/**
	 * @see org.nightlabs.jfire.accounting.book.LocalAccountantDelegate#getMoneyFlowDimensionIDs()
	 */
	public List<String> getMoneyFlowDimensionIDs()
	{
		return DIMENSION_IDS;
	}

	protected void internalBookProductTypeParts(
			OrganisationLegalEntity mandator,
			User user,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			LinkedList<ArticlePrice> articlePriceStack,
			ArticlePrice articlePrice,
			Map<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>> bookInvoiceTransfers,
			ProductType productType, String packageType, int delegationLevel,
			BookMoneyTransfer container, Set<Anchor> involvedAnchors)
	{
		internalBookProductTypePartsByDimension(mandator, user, resolvedMappings,
				articlePriceStack, articlePrice, bookInvoiceTransfers, productType,
				packageType, delegationLevel, container, involvedAnchors);
	}

	/**
	 * Returns a List of {@link BookInvoiceTransfer} with transfers that collect
	 * all neccessary money from the right accounts and transfer it to the account
	 * specified for given dimensionValues and ProductType (first in
	 * articlePriceStack).
	 * 
	 * @param articlePriceStack
	 *          The current articlePriceStack.
	 * @param dimensionValues
	 *          A Map with the current dimension values (key: String value:
	 *          String)
	 * @param resolvedMapping
	 *          The MoneyFlowMapping causing the call to this method (contains the
	 *          target Anchor (Account))
	 * @param resolvedMappings
	 *          A Map with the resolved Mappings for all involved ProductTypes
	 * @param bookMoneyTransfer
	 *          TODO
	 */
	public Collection<BookInvoiceTransfer> getBookInvoiceTransfersForDimensionValues(
			OrganisationLegalEntity mandator,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<String, String> dimensionValues, MoneyFlowMapping resolvedMapping,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			BookMoneyTransfer bookMoneyTransfer)
	{
		// get the priceFragment of interest
		String priceFragmentTypePK = dimensionValues
				.get(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID);
		if (priceFragmentTypePK == null || "".equals(priceFragmentTypePK))
			throw new IllegalArgumentException(
					"No value could be found in the dimensionValues Map for Dimension "
							+ PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID);

		PriceFragmentTypeID typeID = PriceFragmentType
				.primaryKeyToPriceFragmentTypeID(priceFragmentTypePK);
		PriceFragmentType priceFragmentType = (PriceFragmentType) getPersistenceManager()
				.getObjectById(typeID);

		// now if it is total then book all fragments contained in total
		// TODO: Check here if is container not only total
		if (priceFragmentType.getPriceFragmentTypeID().equals(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID))
			return getBookInvoiceTransferForContainerFragmentType(mandator,
					priceFragmentType, articlePriceStack, dimensionValues,
					resolvedMapping, resolvedMappings, bookMoneyTransfer);
		else
			// else return the transfer for the single fragment
			return getBookInvoiceTransfersForSingleFragmentType(mandator,
					priceFragmentType, articlePriceStack, dimensionValues,
					resolvedMapping, resolvedMappings, false, bookMoneyTransfer);

	}

	/**
	 * Searches for transfers for all PriceFragments the given one is the
	 * container.
	 * 
	 * @param bookMoneyTransfer
	 *          TODO
	 */
	protected Collection<BookInvoiceTransfer> getBookInvoiceTransferForContainerFragmentType(
			OrganisationLegalEntity mandator, PriceFragmentType priceFragmentType,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<String, String> dimensionValues, MoneyFlowMapping resolvedMapping,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			BookMoneyTransfer bookMoneyTransfer)
	{
		// Get the PriceFragmentTypes that are defined for the given container
		ArticlePrice price = articlePriceStack.getFirst();
		Set<PriceFragmentType> containedFragmentTypes = new HashSet<PriceFragmentType>();
		for (Iterator iter = price.getFragments().iterator(); iter.hasNext();) {
			PriceFragment fragment = (PriceFragment) iter.next();
			PriceFragmentType container = fragment.getPriceFragmentType()
					.getContainerPriceFragmentType();
			if (container != null) {
				if (priceFragmentType.getPrimaryKey().equals(container.getPrimaryKey())) {
					containedFragmentTypes.add(fragment.getPriceFragmentType());
				}
			}
		}

		Collection<BookInvoiceTransfer> result = new LinkedList<BookInvoiceTransfer>();

		// iterate all contained types
		for (Iterator iter = containedFragmentTypes.iterator(); iter.hasNext();) {
			PriceFragmentType containedType = (PriceFragmentType) iter.next();
			Map<String, String> fakeDimValues = new HashMap<String, String>(
					dimensionValues);
			fakeDimValues.put(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID,
					containedType.getPrimaryKey());
			// find the transfer for the current type
			Collection<BookInvoiceTransfer> singleTransfers = getBookInvoiceTransfersForSingleFragmentType(
					mandator, containedType, articlePriceStack, fakeDimValues,
					resolvedMapping, resolvedMappings, true, bookMoneyTransfer);
			result.addAll(singleTransfers);
		}
		return result;
	}

	protected Collection<BookInvoiceTransfer> getBookInvoiceTransfersForSingleFragmentType(
			OrganisationLegalEntity mandator,
			PriceFragmentType priceFragmentType,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<String, String> dimensionValues,
			MoneyFlowMapping resolvedMapping,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			boolean forContainer, BookMoneyTransfer bookMoneyTransfer
		)
	{
		Collection<BookInvoiceTransfer> result = new ArrayList<BookInvoiceTransfer>();
		BookInvoiceTransfer transfer = getBookInvoiceTransferForSingleFragmentType(
				mandator, priceFragmentType, articlePriceStack,
				dimensionValues, resolvedMapping,
				resolvedMappings, forContainer, bookMoneyTransfer
			);
		if (transfer == null) {
			// Get the articlePrice which has been added last - i.e. the most inner ArticlePrice.
			ArticlePrice articlePrice = articlePriceStack.getFirst();

			long amount = resolvedMapping.getArticlePriceDimensionAmount(
					dimensionValues,
					articlePrice
				);

			Invoice invoice = bookMoneyTransfer.getInvoice();
			// In order to get the Article, we get the last from the stack - i.e. the most outer ArticlePrice
			Article article = articlePriceStack.getLast().getArticle();

			// ensure that what we do is correct
			assert invoice.equals(article.getInvoice()) : "article.getInvoice() does not match current invoice! articlePK=\"" + article.getPrimaryKey() + "\" invoicePK=\"" + invoice.getPrimaryKey() + "\"";

			Account otherAccount;
			if (mandator.equals(invoice.getVendor())) {
				if (article.isReversing()) {
					otherAccount = resolvedMapping.getReverseRevenueAccount(); // can be null in datastore
					if (otherAccount == null)
						otherAccount = resolvedMapping.getRevenueAccount(); // cannot be null in datastore
				}
				else
					otherAccount = resolvedMapping.getRevenueAccount(); // cannot be null in datastore
			}
			else if (mandator.equals(invoice.getCustomer())) {
				if (article.isReversing()) {
					otherAccount = resolvedMapping.getReverseExpenseAccount(); // can be null in datastore
					if (otherAccount == null)
						otherAccount = resolvedMapping.getExpenseAccount(); // cannot be null in datastore
				}
				else
					otherAccount = resolvedMapping.getExpenseAccount(); // cannot be null in datastore
			}
			else
				throw new IllegalStateException("Mandator \"" + mandator.getPrimaryKey() + "\" is neither vendor nor customer of invoice \"" + invoice.getPrimaryKey() + "\"!");

			assert otherAccount == null;

			assert false : "Test Test Test"; // TODO remove this line!

			Anchor from;
			Anchor to;
			if (amount >= 0) {
				from = mandator;
				to = otherAccount;
			}
			else {
				to = mandator;
				from = otherAccount;
				amount *= -1;
			}

			transfer = new BookInvoiceTransfer(
					from,
					to,
					amount
				);
		}
		result.add(transfer);
		logBookInvoiceTransfers("getBookInvoiceTransferForSingleFragmentType ", result);
		
		return result;
	}

	/**
	 * Prepares the articlePrice so it (and all its nested articlePrices) has a
	 * PriceFragment defined that has the amount that is missing so that all
	 * Fragments with containerFragmentType as container together with the new
	 * fragment have the amount defined for containerFragmentType.
	 */
	protected void prepareArticlePrice(PriceFragmentType containerFragmentType,
			PriceFragmentType restPriceFragmentType, ArticlePrice articlePrice)
	{
		String totalPK = containerFragmentType.getPrimaryKey();
		long defAmount = 0;
		for (Iterator iter = articlePrice.getFragments().iterator(); iter.hasNext();) {
			PriceFragment fragment = (PriceFragment) iter.next();
			if (fragment.getPriceFragmentType().getContainerPriceFragmentType() == null)
				continue;
			if (fragment.getPriceFragmentType().getContainerPriceFragmentType()
					.getPrimaryKey().equals(totalPK)) {
				defAmount += fragment.getAmount();
			}
		}
		long totalAmount = articlePrice.getAmount(containerFragmentType);
		articlePrice
				.setAmount(restPriceFragmentType, totalAmount - defAmount, true);
		for (Iterator iter = articlePrice.getNestedArticlePrices().iterator(); iter
				.hasNext();) {
			ArticlePrice nestedArticlePrice = (ArticlePrice) iter.next();
			prepareArticlePrice(containerFragmentType, restPriceFragmentType,
					nestedArticlePrice);
		}
	}

	protected BookInvoiceTransfer getBookInvoiceTransferForSingleFragmentType(
			OrganisationLegalEntity mandator, PriceFragmentType priceFragmentType,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<String, String> dimensionValues, MoneyFlowMapping resolvedMapping,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			boolean forContainer, BookMoneyTransfer bookMoneyTransfer)
	{
		List<PriceFragmentType> priceFragmentTypes = new LinkedList<PriceFragmentType>();
		priceFragmentTypes.add(priceFragmentType);

		PriceFragmentType totalPType = PriceFragmentType
				.getTotalPriceFragmentType(getPersistenceManager());

		// if we look for the priceFragmentType of interest for total in all upper
		// packages
		// if
		// (!PriceFragmentType.TOTAL_PRICEFRAGMENTTYPEID.equals(priceFragmentType.getPriceFragmentTypeID()))
		priceFragmentTypes.add(totalPType);

		List<PriceFragmentType> totalPTypes = new ArrayList<PriceFragmentType>(1);
		totalPTypes.add(totalPType);

		Iterator iterator = articlePriceStack.iterator();
		logger.info("Search BookAnchors for single pricefragment: "
				+ priceFragmentType.getName().getText("en"));
		int count = 0;
		while (iterator.hasNext()) {
			ArticlePrice upperArticlePrice = (ArticlePrice) iterator.next();
			List<PriceFragmentType> pTypeParam = null;
			if (count == 0) {
				if (forContainer) {
					count++;
					continue;
				}
				else
					pTypeParam = totalPTypes;
				// for our own product-type we only check if total was booked for this
				// type already,
				// but only of we are not a part of a container booking
			}
			else
				pTypeParam = priceFragmentTypes;

			count++;

			logger.info("Search for ArticlePricePType: "
					+ upperArticlePrice.getProductType().getName().getText("en"));
			BookInvoiceTransfer result = getBookInvoiceTransferForSingleFragmentType(
					mandator, upperArticlePrice, articlePriceStack, dimensionValues,
					pTypeParam, resolvedMapping, resolvedMappings, bookMoneyTransfer);
			logger.info("Found: " + result);
			if (result != null)
				return result;

		}

		// for (Iterator itPriceFragmentTypes = priceFragmentTypes.iterator();
		// itPriceFragmentTypes.hasNext(); ) {
		// PriceFragmentType searchPriceFragmentType = (PriceFragmentType)
		// itPriceFragmentTypes.next();
		//
		// Iterator iterator = articlePriceStack.iterator();
		// if (iterator.hasNext()) // if
		// (searchPriceFragmentType.getPrimaryKey().equals(priceFragmentType.getPrimaryKey()))
		// iterator.next(); // we skip the first entry if we are working with the
		// current pricefragmenttype, because that's our current productType
		// while (iterator.hasNext()) {
		// ArticlePrice upperArticlePrice = (ArticlePrice)iterator.next();
		// String upperPackageType = getPackageType(upperArticlePrice);
		// Map<String, String> fakeDimValues = new HashMap<String,
		// String>(dimensionValues);
		// fakeDimValues.put(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID,
		// searchPriceFragmentType.getPrimaryKey());
		// PFMoneyFlowMapping packagingUpperMapping =
		// (PFMoneyFlowMapping)getMoneyFlowMapping(
		// resolvedMappings,
		// upperArticlePrice.getProductType(),
		// upperPackageType,
		// fakeDimValues,
		// upperArticlePrice.getCurrency()
		// );
		//
		// if (packagingUpperMapping != null) {
		// return
		// new LocalAccountantDelegate.BookInvoiceTransfer(
		// packagingUpperMapping.getAccount(),
		// resolvedMapping.getAccount(),
		// resolvedMapping.getArticlePriceDimensionAmount(
		// dimensionValues,
		// (ArticlePrice)articlePriceStack.getFirst()
		// )
		// );
		//
		// }
		// }
		// } // while (iterator.hasNext()) {
		return null;
	}

	private BookInvoiceTransfer getBookInvoiceTransferForSingleFragmentType(
			OrganisationLegalEntity mandator, ArticlePrice upperArticlePrice,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<String, String> dimensionValues,
			List<PriceFragmentType> searchPriceFragmentTypes,
			MoneyFlowMapping resolvedMapping,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			BookMoneyTransfer bookMoneyTransfer)
	{
		String upperPackageType = getPackageType(upperArticlePrice);
		for (PriceFragmentType searchPriceFragmentType : searchPriceFragmentTypes) {
			Map<String, String> fakeDimValues = new HashMap<String, String>(
					dimensionValues);
			fakeDimValues.put(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID,
					searchPriceFragmentType.getPrimaryKey());
			PFMoneyFlowMapping packagingUpperMapping = (PFMoneyFlowMapping) getMoneyFlowMapping(
					resolvedMappings, upperArticlePrice.getProductType(),
					upperPackageType, fakeDimValues, upperArticlePrice.getCurrency());

			if (packagingUpperMapping != null) {
				Anchor from;
				Anchor to;

				long amount = resolvedMapping.getArticlePriceDimensionAmount(
						dimensionValues, articlePriceStack.getFirst());

				
				Invoice invoice = bookMoneyTransfer.getInvoice();
				// In order to get the Article, we get the last from the stack - i.e. the most outer ArticlePrice
				Article article = articlePriceStack.getLast().getArticle();

				// ensure that what we do is correct
				assert invoice.equals(article.getInvoice()) : "article.getInvoice() does not match current invoice! articlePK=\"" + article.getPrimaryKey() + "\" invoicePK=\"" + invoice.getPrimaryKey() + "\"";

				Account packagingUpperMappingAccount;
				Account resolvedMappingAccount;
				if (mandator.equals(invoice.getVendor())) {
					if (article.isReversing()) {
						resolvedMappingAccount = resolvedMapping.getReverseRevenueAccount(); // can be null in datastore
						if (resolvedMappingAccount == null)
							resolvedMappingAccount = resolvedMapping.getRevenueAccount(); // cannot be null in datastore

						packagingUpperMappingAccount = packagingUpperMapping.getReverseRevenueAccount(); // can be null in datastore
						if (packagingUpperMappingAccount == null)
							packagingUpperMappingAccount = packagingUpperMapping.getRevenueAccount(); // cannot be null in datastore
					}
					else {
						resolvedMappingAccount = resolvedMapping.getRevenueAccount(); // cannot be null in datastore
						packagingUpperMappingAccount = packagingUpperMapping.getRevenueAccount(); // cannot be null in datastore
					}
				}
				else if (mandator.equals(invoice.getCustomer())) {
					if (article.isReversing()) {
						resolvedMappingAccount = resolvedMapping.getReverseExpenseAccount(); // can be null in datastore
						if (resolvedMappingAccount == null)
							resolvedMappingAccount = resolvedMapping.getExpenseAccount(); // cannot be null in datastore

						packagingUpperMappingAccount = packagingUpperMapping.getReverseExpenseAccount(); // can be null in datastore
						if (packagingUpperMappingAccount == null)
							packagingUpperMappingAccount = packagingUpperMapping.getExpenseAccount(); // cannot be null in datastore
					}
					else {
						resolvedMappingAccount = resolvedMapping.getExpenseAccount(); // cannot be null in datastore
						packagingUpperMappingAccount = packagingUpperMapping.getExpenseAccount(); // cannot be null in datastore
					}
				}
				else
					throw new IllegalStateException("Mandator \"" + mandator.getPrimaryKey() + "\" is neither vendor nor customer of invoice \"" + invoice.getPrimaryKey() + "\"!");

				assert resolvedMappingAccount == null;

				assert false : "Test Test Test"; // TODO remove this line!


				if (amount >= 0) {
					from = packagingUpperMappingAccount;
					to = resolvedMappingAccount;
				}
				else {
					to = packagingUpperMappingAccount;
					from = resolvedMappingAccount;
					amount *= -1;
				}

				return new MappingBasedAccountantDelegate.BookInvoiceTransfer(from, to,
						amount);

			}
		}
		return null;

	}

	private void logBookInvoiceTransfers(String prefix,
			Collection<BookInvoiceTransfer> transfers)
	{
		for (BookInvoiceTransfer transfer : transfers) {
			logger.info(prefix + transfer.toString());
		}
	}
}
