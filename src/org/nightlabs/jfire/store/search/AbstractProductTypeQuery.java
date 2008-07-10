package org.nightlabs.jfire.store.search;

import java.util.List;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductType.FieldName;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * Searches {@link ProductType}s. Every field that's <code>null</code> is ignored,
 * every field containing a value will cause the query to filter all non-matching instances.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractProductTypeQuery
extends VendorDependentQuery
implements ISaleAccessQuery
{
	private static final Logger logger = Logger.getLogger(AbstractProductTypeQuery.class);
	 
	private static final long serialVersionUID = 3L;

	private String fullTextLanguageID = null;
	private String fullTextSearch = null;
	
	private Boolean published = null;
	private Boolean confirmed = null;
	private Boolean saleable = null;
	private Boolean closed = null;
	
	private int minNestedProductTypeAmount = -1;
	private int maxNestedProductTypeAmount = -1;
	private AnchorID ownerID = null;
//	private Boolean available = null;
//	private PriceConfigID innerPriceConfigID = null;	
//	private DeliveryConfigurationID deliveryConfigurationID = null;
//	private LocalAccountantDelegateID localAccountantDelegateID = null;
	private ProductTypeGroupID productTypeGroupID = null;	
	private String organisationID = null;
	// this value is initially set to avoid finding productType categories by default
	private Byte inheritanceNature = ProductType.INHERITANCE_NATURE_LEAF;
	
	// Property IDs used for the PropertyChangeListeners
	private static final String PROPERTY_PREFIX = "AbstractProductTypeQuery.";
	public static final String PROPERTY_AVAILABLE = PROPERTY_PREFIX + "available";
	public static final String PROPERTY_CLOSED = PROPERTY_PREFIX + "closed";
	public static final String PROPERTY_CONFIRMED = PROPERTY_PREFIX + "confirmed";
	public static final String PROPERTY_FULL_TEXT_LANGUAGE_ID = PROPERTY_PREFIX + "fullTextLanguageID";
	public static final String PROPERTY_FULL_TEXT_SEARCH = PROPERTY_PREFIX + "fullTextSearch";
	public static final String PROPERTY_MAX_NESTED_PRODUCTTYPE_AMOUNT = PROPERTY_PREFIX + "maxNestedProductTypeAmount";
	public static final String PROPERTY_MIN_NESTED_PRODUCTTYPE_AMOUNT = PROPERTY_PREFIX + "minNestedProductTypeAmount";
	public static final String PROPERTY_OWNER_ID = PROPERTY_PREFIX + "ownerID";
	public static final String PROPERTY_PRODUCTTYPE_GROUP_ID = PROPERTY_PREFIX + "productTypeGroupID";
	public static final String PROPERTY_PUBLISHED = PROPERTY_PREFIX + "published";
	public static final String PROPERTY_SALEABLE = PROPERTY_PREFIX + "saleable";
	public static final String PROPERTY_ORGANISATION_ID = PROPERTY_PREFIX + "organisationID";
	public static final String PROPERTY_INHERITANCE_NATURE = PROPERTY_PREFIX + "inheritanceNature";

//	public static final String PROPERTY_INNER_PRICE_CONFIG_ID = PROPERTY_PREFIX + "innerPriceConfigID";
//	public static final String PROPERTY_LOCAL_ACCOUNTANT_DELEGATE_ID = PROPERTY_PREFIX + "localAccountantDelegateID";	
//	public static final String PROPERTY_DELIVERY_CONFIGURATION_ID = PROPERTY_PREFIX + "deliveryConfigurationID";

	@Override
	protected Query createQuery()
	{
		return getPersistenceManager().newQuery(getCandidateClass());
	}
	
	@Override
	protected void prepareQuery(Query q)
	{
		super.prepareQuery(q);
		
		StringBuffer filter = getFilter();
		StringBuffer vars = getVars();
		
		filter.append("\n && ");
		filter.append("true");
		
		if (fullTextSearch != null) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, FieldName.name);
			filter.append("\n )");
		}
		
		if (published != null)
			filter.append("\n && this."+FieldName.published+" == :published");

		if (confirmed != null)
			filter.append("\n && this."+FieldName.confirmed+" == :confirmed");

		if (saleable != null)
			filter.append("\n && this."+FieldName.saleable+" == :saleable");

		if (closed != null)
			filter.append("\n && this."+FieldName.closed+" == :closed");

		if (minNestedProductTypeAmount >= 0)
			filter.append("\n && :minNestedProductTypeAmount < this."+FieldName.productTypeLocal+"."+
					org.nightlabs.jfire.store.ProductTypeLocal.FieldName.nestedProductTypeLocals+".size()");

		if (maxNestedProductTypeAmount >= 0)
			filter.append("\n && :maxNestedProductTypeAmount > this."+FieldName.productTypeLocal+"."+
					org.nightlabs.jfire.store.ProductTypeLocal.FieldName.nestedProductTypeLocals+".size()");

		if (ownerID != null) {
			filter.append("\n && JDOHelper.getObjectId(this."+FieldName.owner+") == :ownerID");
		}

//		if (innerPriceConfigID != null) {
//			filter.append("\n && JDOHelper.getObjectId(this.innerPriceConfig) == :innerPriceConfigID");
//		}
//		
//		if (deliveryConfigurationID != null) {
//			filter.append("\n && JDOHelper.getObjectId(this.deliveryConfiguration) == :deliveryConfigurationID");
//		}
//		
//		if (localAccountantDelegateID != null) {
//			filter.append("\n && JDOHelper.getObjectId(this.localAccountantDelegate) == :localAccountantDelegateID");
//		}

		if (productTypeGroupID != null) {
			filter.append("\n && JDOHelper.getObjectId(this."+FieldName.managedProductTypeGroup+") == :productTypeGroupID");
		}

		if (organisationID != null)
			filter.append("\n && this."+FieldName.organisationID+" == :organisationID");

		if (inheritanceNature != null)
			filter.append("\n && this."+FieldName.inheritanceNature+" == :inheritanceNature");

		if (logger.isDebugEnabled()) {
			logger.debug("Vars:");
			logger.debug(vars.toString());
			logger.debug("Filter:");
			logger.debug(filter.toString());			
		}

		q.setFilter(filter.toString());
		q.declareVariables(vars.toString());
	}

	protected void addFullTextSearch(StringBuffer filter, StringBuffer vars, String member) {
		if (vars.length() > 0)
			vars.append("; ");
		String varName = member+"Var";
		vars.append(String.class.getName()+" "+varName);
		String containsStr = "containsValue("+varName+")";
		if (fullTextLanguageID != null)
			containsStr = "containsEntry(\""+fullTextLanguageID+"\","+varName+")";
		filter.append("\n (\n" +
				"  this."+member+".names."+containsStr+"\n" +
				"  && "+varName+".toLowerCase().matches(:fullTextSearch.toLowerCase())" +
				" )");
	}
	
	/**
	 * @return the confirmed
	 */
	public Boolean getConfirmed() {
		return confirmed;
	}

	/**
	 * Sets the filter to include only ProductTypes whose confirmed flag matches the given value.
	 * @param confirmed the confirmed to set
	 */
	public void setConfirmed(Boolean confirmed)
	{
		final Boolean oldConfirmed = this.confirmed;
		this.confirmed = confirmed;
		notifyListeners(PROPERTY_CONFIRMED, oldConfirmed, confirmed);
	}

	/**
	 * @return the fullTextLanguageID
	 */
	public String getFullTextLanguageID() {
		return fullTextLanguageID;
	}

	/**
	 * @param fullTextLanguageID the fullTextLanguageID to set
	 */
	public void setFullTextLanguageID(String fullTextLanguageID)
	{
		final String oldFullTextLanguageID = this.fullTextLanguageID;
		this.fullTextLanguageID = fullTextLanguageID;
		notifyListeners(PROPERTY_FULL_TEXT_LANGUAGE_ID, oldFullTextLanguageID, fullTextLanguageID);
	}

	/**
	 * @return the fullTextSearch
	 */
	public String getFullTextSearch() {
		return fullTextSearch;
	}

	/**
	 * Sets the filter to include only ProductTypes whose name
	 * matches the given fullTextSearch. The match will be performed
	 * following the rules of {@link String#matches(String)}, i.e.
	 * you need to pass a regular expression here.
	 * 
	 * @param fullTextSearch the fullTextSearch to set
	 */
	public void setFullTextSearch(String fullTextSearch)
	{
		final String oldFullTextSearch = this.fullTextSearch;
		this.fullTextSearch = fullTextSearch;
		notifyListeners(PROPERTY_FULL_TEXT_SEARCH, oldFullTextSearch, fullTextSearch);
	}

	/**
	 * @return the published
	 */
	public Boolean getPublished() {
		return published;
	}

	/**
	 * Sets the filter to include only productTypes whose published flag matches the given value.
	 * @param published the published to set
	 */
	public void setPublished(Boolean published)
	{
		final Boolean oldPublished = this.published;
		this.published = published;
		notifyListeners(PROPERTY_PUBLISHED, oldPublished, published);
	}

	/**
	 * @return the saleable
	 */
	public Boolean getSaleable() {
		return saleable;
	}

	/**
	 * Sets the filter to include only productTypes whose saleable flag matches the given value.
	 * @param saleable the saleable to set
	 */
	public void setSaleable(Boolean saleable)
	{
		final Boolean oldSaleable = this.saleable;
		this.saleable = saleable;
		notifyListeners(PROPERTY_SALEABLE, oldSaleable, saleable);
	}

	/**
	 * returns the minNestedProductTypeAmount.
	 * @return the minNestedProductTypeAmount
	 */
	public int getMinNestedProductTypeAmount() {
		return minNestedProductTypeAmount;
	}

	/**
	 * sets the minNestedProductTypeAmount
	 * @param minNestedProductTypeAmount the minNestedProductTypeAmount to set
	 */
	public void setMinNestedProductTypeAmount(int minNestedProductTypeAmount)
	{
		final Integer oldMinNestedProductTypeAmount = this.minNestedProductTypeAmount;
		this.minNestedProductTypeAmount = minNestedProductTypeAmount;
		notifyListeners(PROPERTY_MIN_NESTED_PRODUCTTYPE_AMOUNT, oldMinNestedProductTypeAmount, 
			minNestedProductTypeAmount);
	}

	/**
	 * returns the maxNestedProductTypeAmount.
	 * @return the maxNestedProductTypeAmount
	 */
	public int getMaxNestedProductTypeAmount() {
		return maxNestedProductTypeAmount;
	}

	/**
	 * sets the maxNestedProductTypeAmount
	 * @param maxNestedProductTypeAmount the maxNestedProductTypeAmount to set
	 */
	public void setMaxNestedProductTypeAmount(int maxNestedProductTypeAmount)
	{
		final Integer oldMaxNestedProductTypeAmount = this.maxNestedProductTypeAmount;
		this.maxNestedProductTypeAmount = maxNestedProductTypeAmount;
		notifyListeners(PROPERTY_MAX_NESTED_PRODUCTTYPE_AMOUNT, oldMaxNestedProductTypeAmount, 
			maxNestedProductTypeAmount);
	}

//	/**
//	 * returns the innerPriceConfigID.
//	 * @return the innerPriceConfigID
//	 */
//	public PriceConfigID getInnerPriceConfigID() {
//		return innerPriceConfigID;
//	}
//
//	/**
//	 * sets the innerPriceConfigID
//	 * @param innerPriceConfigID the innerPriceConfigID to set
//	 */
//	public void setInnerPriceConfigID(PriceConfigID innerPriceConfigID)
//	{
//		final PriceConfigID oldInnerPriceConfigID = this.innerPriceConfigID;
//		this.innerPriceConfigID = innerPriceConfigID;
//		notifyListeners(PROPERTY_INNER_PRICE_CONFIG_ID, oldInnerPriceConfigID, innerPriceConfigID);
//	}

	/**
	 * returns the ownerID.
	 * @return the ownerID
	 */
	public AnchorID getOwnerID() {
		return ownerID;
	}

	/**
	 * sets the ownerID
	 * @param ownerID the ownerID to set
	 */
	public void setOwnerID(AnchorID ownerID)
	{
		final AnchorID oldOwnerID = this.ownerID;
		this.ownerID = ownerID;
		notifyListeners(PROPERTY_OWNER_ID, oldOwnerID, ownerID);
	}

//	/**
//	 * returns the deliveryConfigurationID.
//	 * @return the deliveryConfigurationID
//	 */
//	public DeliveryConfigurationID getDeliveryConfigurationID() {
//		return deliveryConfigurationID;
//	}
//
//	/**
//	 * sets the deliveryConfigurationID
//	 * @param deliveryConfigurationID the deliveryConfigurationID to set
//	 */
//	public void setDeliveryConfigurationID(DeliveryConfigurationID deliveryConfigurationID)
//	{
//		final DeliveryConfigurationID oldDeliveryConfigurationID = this.deliveryConfigurationID;
//		this.deliveryConfigurationID = deliveryConfigurationID;
//		notifyListeners(PROPERTY_DELIVERY_CONFIGURATION_ID, oldDeliveryConfigurationID, deliveryConfigurationID);
//	}
//
//	/**
//	 * returns the localAccountantDelegateID.
//	 * @return the localAccountantDelegateID
//	 */
//	public LocalAccountantDelegateID getLocalAccountantDelegateID() {
//		return localAccountantDelegateID;
//	}
//
//	/**
//	 * sets the localAccountantDelegateID
//	 * @param localAccountantDelegateID the localAccountantDelegateID to set
//	 */
//	public void setLocalAccountantDelegateID(LocalAccountantDelegateID localAccountantDelegateID)
//	{
//		final LocalAccountantDelegateID oldLocalAccountantDelegateID = this.localAccountantDelegateID;
//		this.localAccountantDelegateID = localAccountantDelegateID;
//		notifyListeners(PROPERTY_LOCAL_ACCOUNTANT_DELEGATE_ID, oldLocalAccountantDelegateID, 
//			localAccountantDelegateID);
//	}

	/**
	 * returns the productTypeGroupID.
	 * @return the productTypeGroupID
	 */
	public ProductTypeGroupID getProductTypeGroupID() {
		return productTypeGroupID;
	}

	/**
	 * sets the productTypeGroupID
	 * @param productTypeGroupID the productTypeGroupID to set
	 */
	public void setProductTypeGroupID(ProductTypeGroupID productTypeGroupID)
	{
		final ProductTypeGroupID oldProductTypeGroupID = this.productTypeGroupID;
		this.productTypeGroupID = productTypeGroupID;
		notifyListeners(PROPERTY_PRODUCTTYPE_GROUP_ID, oldProductTypeGroupID, productTypeGroupID);
	}

	/**
	 * returns the closed.
	 * @return the closed
	 */
	public Boolean getClosed() {
		return closed;
	}

	/**
	 * sets the closed
	 * @param closed the closed to set
	 */
	public void setClosed(Boolean closed)
	{
		final Boolean oldClosed = this.closed;
		this.closed = closed;
		notifyListeners(PROPERTY_CLOSED, oldClosed, closed);
	}

//	/**
//	 * returns the available.
//	 * @return the available
//	 */
//	public Boolean getAvailable() {
//		return available;
//	}
//
//	/**
//	 * sets the available
//	 * @param available the available to set
//	 */
//	public void setAvailable(Boolean available)
//	{
//		final Boolean oldAvailable = this.available;
//		this.available = available;
//		notifyListeners(PROPERTY_AVAILABLE, oldAvailable, available);
//	}

	/**
	 * Returns the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Sets the organisationID.
	 * @param organisationID the organisationID to set
	 */
	public void setOrganisationID(String organisationID) {
		String oldOrganisationID = this.organisationID;
		this.organisationID = organisationID;
		notifyListeners(PROPERTY_ORGANISATION_ID, oldOrganisationID, organisationID);
	}
	
	/**
	 * Returns the inheritanceNature.
	 * @return the inheritanceNature
	 */
	public Byte getInheritanceNature() {
		return inheritanceNature;
	}
	
	/**
	 * Sets the inheritanceNature.
	 * @param inheritanceNature the inheritanceNature to set
	 */
	public void setInheritanceNature(Byte inheritanceNature) {
		this.inheritanceNature = inheritanceNature;
	}

	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		List<FieldChangeCarrier> changedFields = super.getChangedFields(propertyName);
		boolean allFields = AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(propertyName);
		
//		if (allFields || PROPERTY_AVAILABLE.equals(propertyName))
//		{
//			changedFields.add( new FieldChangeCarrier(PROPERTY_AVAILABLE, available) );
//		}
		if (allFields || PROPERTY_CLOSED.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_CLOSED, closed) );
		}
		if (allFields || PROPERTY_CONFIRMED.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_CONFIRMED, confirmed) );
		}
		if (allFields || PROPERTY_FULL_TEXT_LANGUAGE_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_FULL_TEXT_LANGUAGE_ID, fullTextLanguageID) );
		}
		if (allFields || PROPERTY_FULL_TEXT_SEARCH.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_FULL_TEXT_SEARCH, fullTextSearch) );
		}
		if (allFields || PROPERTY_MAX_NESTED_PRODUCTTYPE_AMOUNT.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_MAX_NESTED_PRODUCTTYPE_AMOUNT, maxNestedProductTypeAmount) );
		}
		if (allFields || PROPERTY_MIN_NESTED_PRODUCTTYPE_AMOUNT.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_MIN_NESTED_PRODUCTTYPE_AMOUNT, minNestedProductTypeAmount) );
		}
		if (allFields || PROPERTY_OWNER_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_OWNER_ID, ownerID) );
		}
		if (allFields || PROPERTY_PRODUCTTYPE_GROUP_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_PRODUCTTYPE_GROUP_ID, productTypeGroupID) );
		}
		if (allFields || PROPERTY_PUBLISHED.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_PUBLISHED, published) );
		}
		if (allFields || PROPERTY_SALEABLE.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_SALEABLE, saleable) );
		}
		if (allFields || PROPERTY_ORGANISATION_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ORGANISATION_ID, organisationID) );
		}
		if (allFields || PROPERTY_INHERITANCE_NATURE.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_INHERITANCE_NATURE, inheritanceNature) );
		}
		
//		if (allFields || PROPERTY_DELIVERY_CONFIGURATION_ID.equals(propertyName))
//		{
//			changedFields.add( new FieldChangeCarrier(PROPERTY_DELIVERY_CONFIGURATION_ID, deliveryConfigurationID) );
//		}		
//		if (allFields || PROPERTY_INNER_PRICE_CONFIG_ID.equals(propertyName))
//		{
//			changedFields.add( new FieldChangeCarrier(PROPERTY_INNER_PRICE_CONFIG_ID, innerPriceConfigID) );
//		}
//		if (allFields || PROPERTY_LOCAL_ACCOUNTANT_DELEGATE_ID.equals(propertyName))
//		{
//			changedFields.add( new FieldChangeCarrier(PROPERTY_LOCAL_ACCOUNTANT_DELEGATE_ID, localAccountantDelegateID) );
//		}
		
		return changedFields;
	}
}
