/*
 * Created 	on Sep 16, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.accounting.book.fragmentbased;

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

import org.nightlabs.ipanema.accounting.Invoice;
import org.nightlabs.ipanema.accounting.PriceFragment;
import org.nightlabs.ipanema.accounting.PriceFragmentType;
import org.nightlabs.ipanema.accounting.book.BookMoneyTransfer;
import org.nightlabs.ipanema.accounting.book.LocalAccountantDelegate;
import org.nightlabs.ipanema.accounting.book.MoneyFlowMapping;
import org.nightlabs.ipanema.accounting.id.PriceFragmentTypeID;
import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.store.ProductType;
import org.nightlabs.ipanema.store.id.ProductTypeID;
import org.nightlabs.ipanema.trade.Article;
import org.nightlabs.ipanema.trade.ArticlePrice;
import org.nightlabs.ipanema.trade.OrganisationLegalEntity;
import org.nightlabs.util.Utils;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.book.LocalAccountantDelegate"
 *		detachable="true"
 *		table="JFireTrade_PFMappingAccountantDelegate"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 */
public class PFMappingAccountantDelegate extends
		LocalAccountantDelegate {

	/**
	 * @deprecated Only for JDO
	 */
	public PFMappingAccountantDelegate() {
		super();
	}

	/**
	 * @param accountant
	 * @param productType
	 */
	public PFMappingAccountantDelegate(String organisationID,
			String localAccountantID) {
		super(organisationID, localAccountantID);
	}
	

	/**
	 * @param parent
	 * @param organisationID
	 * @param localAccountantDelegateID
	 */
	public PFMappingAccountantDelegate(LocalAccountantDelegate parent, String organisationID, String localAccountantDelegateID) {
		super(parent, organisationID, localAccountantDelegateID);
	}	

	/**
	 * key: Invoice invoice<br/>
	 * value: Map resolvedMappings
	 * 
	 * Used to store resolved mappings temporaly per invoice-book
	 * @jdo.field persistence-modifier="none"
	 */
	private Map resolvedPTypeMappings = new HashMap();
	
	
	
	/**
	 * @see org.nightlabs.ipanema.accounting.book.LocalAccountantDelegate#preBookArticles(org.nightlabs.ipanema.trade.OrganisationLegalEntity, org.nightlabs.ipanema.security.User, org.nightlabs.ipanema.accounting.Invoice, BookMoneyTransfer, Map)
	 */
	public void preBookArticles(OrganisationLegalEntity mandator, User user, Invoice invoice, BookMoneyTransfer bookTransfer, Map involvedAnchors) {
		if (resolvedPTypeMappings.containsKey(invoice))
			return;
		resolvedPTypeMappings.put(invoice, resolveProductTypeMappings(invoice));
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.book.LocalAccountantDelegate#postBookArticles(org.nightlabs.ipanema.trade.OrganisationLegalEntity, org.nightlabs.ipanema.security.User, org.nightlabs.ipanema.accounting.Invoice, BookMoneyTransfer, Map)
	 */
	public void postBookArticles(OrganisationLegalEntity mandator, User user, Invoice invoice, BookMoneyTransfer bookTransfer, Map involvedAnchors) {
		if (resolvedPTypeMappings.containsKey(invoice))
			resolvedPTypeMappings.remove(invoice);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.book.LocalAccountantDelegate#bookArticle(org.nightlabs.ipanema.trade.OrganisationLegalEntity, org.nightlabs.ipanema.security.User, org.nightlabs.ipanema.accounting.Invoice, org.nightlabs.ipanema.trade.ArticlePrice, org.nightlabs.ipanema.accounting.book.BookMoneyTransfer, java.util.Map)
	 */
	public void bookArticle(OrganisationLegalEntity mandator, User user,
			Invoice invoice, Article article , BookMoneyTransfer container,
			Map involvedAnchors) {
		Map resolvedMappings = (Map) resolvedPTypeMappings.get(invoice);
		if (resolvedMappings == null)
			throw new IllegalStateException("Could not find resolved mappings for invoice "+JDOHelper.getObjectId(invoice)+" can not book article. Was preBookInvoice() called?");
		LinkedList articlePrices = new LinkedList();
		articlePrices.add(article.getPrice());
		/*
		 * key: Anchor from
		 * value: Map toTransfers
		 *   key: Anchor to
		 *   value: Collection of BookInvoiceTransfer transfers
		 */
		Map bookInvoiceTransfers = new HashMap();

		bookProductTypeParts(mandator, user, resolvedMappings, articlePrices, bookInvoiceTransfers, 0, container, involvedAnchors);
		bookInvoiceTransfers(user, bookInvoiceTransfers, container, involvedAnchors);
	}
	
	

	/**
	 * @see org.nightlabs.ipanema.accounting.book.LocalAccountantDelegate#getMoneyFlowMappingKey(org.nightlabs.ipanema.store.ProductType, java.lang.String, java.util.Map, org.nightlabs.ipanema.accounting.Currency)
	 */
	public String getMoneyFlowMappingKey(
			ProductTypeID productTypeID, 
			String packageType,
			Map dimensionValues,
			String currencyID
		) 
	{
		return PFMoneyFlowMapping.getMappingKey(
				ProductType.getPrimaryKey(productTypeID.organisationID, productTypeID.productTypeID), 
				packageType, currencyID, 
				(String)dimensionValues.get(OwnerDimension.MONEY_FLOW_DIMENSION_ID),
				(String)dimensionValues.get(SourceOrganisationDimension.MONEY_FLOW_DIMENSION_ID),
				(String)dimensionValues.get(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID)
			);
	}
	
	

	public static List DIMENSION_IDS = Utils.array2ArrayList(
			new String[] {
					OwnerDimension.MONEY_FLOW_DIMENSION_ID,
					SourceOrganisationDimension.MONEY_FLOW_DIMENSION_ID,
					PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID
				}
		);
	
	/**
	 * @see org.nightlabs.ipanema.accounting.book.LocalAccountantDelegate#getMoneyFlowDimensionIDs()
	 */
	public List getMoneyFlowDimensionIDs() {
		return DIMENSION_IDS;
	}

	protected void internalBookProductTypeParts(
			OrganisationLegalEntity mandator, User user, Map resolvedMappings, 
			LinkedList articlePriceStack, ArticlePrice articlePrice, 
			Map bookInvoiceTransfers,
			ProductType productType, String packageType,
			int delegationLevel, 
			BookMoneyTransfer container, Map involvedAnchors
		)
	{
		internalBookProductTypePartsByDimension(
				mandator, user, 
				resolvedMappings, articlePriceStack, articlePrice, 
				bookInvoiceTransfers,
				productType, packageType,
				delegationLevel,
				container, involvedAnchors
			);
	}
	
	/**
	 * @see org.nightlabs.ipanema.accounting.book.LocalAccountantDelegate#getBookInvoiceTransfersForDimensionValues(org.nightlabs.ipanema.trade.OrganisationLegalEntity, java.util.LinkedList, java.util.Map, org.nightlabs.ipanema.accounting.book.MoneyFlowMapping, java.util.Map)
	 */
	public Collection getBookInvoiceTransfersForDimensionValues(
			OrganisationLegalEntity mandator, 
			LinkedList articlePriceStack, 
			Map dimensionValues, 
			MoneyFlowMapping resolvedMapping, 
			Map resolvedMappings
		) 
	{
		// get the priceFragment of interest
		String priceFragmentTypePK = (String)dimensionValues.get(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID);
		if (priceFragmentTypePK == null || "".equals(priceFragmentTypePK))
			throw new IllegalArgumentException("No value could be found in the dimensionValues Map for Dimension "+PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID);
		
		PriceFragmentTypeID typeID = PriceFragmentType.primaryKeyToPriceFragmentTypeID(priceFragmentTypePK);		
		PriceFragmentType priceFragmentType = (PriceFragmentType) getPersistenceManager().getObjectById(typeID);
	
		// now if it is total then book all fragments contained in total
		if (priceFragmentType.getPriceFragmentTypeID().equals(PriceFragmentType.TOTAL_PRICEFRAGMENTTYPEID)) 
			return getBookInvoiceTransferForContainerFragmentType(
					mandator,
					priceFragmentType, articlePriceStack, dimensionValues,
					resolvedMapping, resolvedMappings
				);
		else
			// else return the transfer for the single fragment
			return getBookInvoiceTransfersForSingleFragmentType(
					mandator,
					priceFragmentType, articlePriceStack, dimensionValues,
					resolvedMapping, resolvedMappings
				);
		
	}

	/**
	 * Searches for transfers for all PriceFragments the given one is the container. 
	 */
	protected Collection getBookInvoiceTransferForContainerFragmentType(
			OrganisationLegalEntity mandator, 
			PriceFragmentType priceFragmentType,
			LinkedList articlePriceStack, 
			Map dimensionValues, 
			MoneyFlowMapping resolvedMapping, 
			Map resolvedMappings
		) 
	{
		ArticlePrice price = (ArticlePrice)articlePriceStack.getFirst();
		// Get the PriceFragementTypes involved in this price
		Set containedFragmentTypes = new HashSet();
		for (Iterator iter = price.getFragments().iterator(); iter.hasNext();) {
			PriceFragment fragment = (PriceFragment) iter.next();
			PriceFragmentType container = fragment.getPriceFragmentType().getContainerPriceFragmentType();
			if (container != null) {
				if (priceFragmentType.getPrimaryKey().equals(container.getPrimaryKey())) {
					containedFragmentTypes.add(fragment.getPriceFragmentType());
				}
			}
		}		
		
		LinkedList result = new LinkedList();
		
		// iterate all contained types 
		for (Iterator iter = containedFragmentTypes.iterator(); iter.hasNext();) {
			PriceFragmentType containedType = (PriceFragmentType) iter.next();
			Map fakeDimValues = new HashMap(dimensionValues);
			fakeDimValues.put(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID, containedType.getPrimaryKey());
			// find the transfer for the current type
			Collection singleTransfers = getBookInvoiceTransfersForSingleFragmentType(
					mandator,
					containedType,
					articlePriceStack,
					fakeDimValues,
					resolvedMapping,
					resolvedMappings
				);
			result.addAll(singleTransfers);
		}
		return result;
	}

	protected Collection getBookInvoiceTransfersForSingleFragmentType(
			OrganisationLegalEntity mandator, 
			PriceFragmentType priceFragmentType, 
			LinkedList articlePriceStack, 
			Map dimensionValues, 
			MoneyFlowMapping resolvedMapping, 
			Map resolvedMappings
		) 
	{
		Collection result = new ArrayList();
		BookInvoiceTransfer transfer = getBookInvoiceTransferForSingleFragmentType(
				priceFragmentType, articlePriceStack, dimensionValues,
				resolvedMapping, resolvedMappings				
			);
		if (transfer == null) {
			transfer = new BookInvoiceTransfer(
					mandator,
					resolvedMapping.getAccount(),
					resolvedMapping.getArticlePriceDimensionAmount(
							dimensionValues, 
							(ArticlePrice)articlePriceStack.getFirst()
						)
				); 
		}
		result.add(transfer);
		return result;
	}
	
	protected BookInvoiceTransfer getBookInvoiceTransferForSingleFragmentType(
			PriceFragmentType priceFragmentType, 
			LinkedList articlePriceStack, 
			Map dimensionValues, 
			MoneyFlowMapping resolvedMapping, 
			Map resolvedMappings
		) 
	{
		List result = new LinkedList();
		LinkedList priceFragmentTypes = new LinkedList();		
		priceFragmentTypes.add(priceFragmentType);
		
		// if we look for the priceFragmentType of interest for total in all upper packages
		if (!PriceFragmentType.TOTAL_PRICEFRAGMENTTYPEID.equals(priceFragmentType.getPriceFragmentTypeID()))
			priceFragmentTypes.add(PriceFragmentType.getTotalPriceFragmentType(getPersistenceManager()));
		
		for (Iterator itPriceFragmentTypes = priceFragmentTypes.iterator(); itPriceFragmentTypes.hasNext(); ) {
			PriceFragmentType searchPriceFragmentType = (PriceFragmentType) itPriceFragmentTypes.next();
			
			Iterator iterator = articlePriceStack.iterator();
			if (iterator.hasNext()) // if (searchPriceFragmentType.getPrimaryKey().equals(priceFragmentType.getPrimaryKey()))
				iterator.next(); // we skip the first entry if we are working with the current pricefragmenttype, because that's our current productType
			while (iterator.hasNext()) {
				ArticlePrice upperArticlePrice = (ArticlePrice)iterator.next();
				String upperPackageType = getPackageType(upperArticlePrice);
				Map fakeDimValues = new HashMap(dimensionValues);
				fakeDimValues.put(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID, searchPriceFragmentType.getPrimaryKey());
				PFMoneyFlowMapping packagingUpperMapping = (PFMoneyFlowMapping)getMoneyFlowMapping(
						resolvedMappings, 
						upperArticlePrice.getProductType(),
						upperPackageType,
						fakeDimValues,
						upperArticlePrice.getCurrency()
				);
				
				if (packagingUpperMapping != null) {
					return
							new LocalAccountantDelegate.BookInvoiceTransfer(
									packagingUpperMapping.getAccount(),
									resolvedMapping.getAccount(),
									resolvedMapping.getArticlePriceDimensionAmount(
											dimensionValues, 
											(ArticlePrice)articlePriceStack.getFirst()
										)
								); 
						
				}				
			}
		} // while (iterator.hasNext()) {
		return null;
	}
}
