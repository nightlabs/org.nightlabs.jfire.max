package org.nightlabs.jfire.store.search;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.deliver.id.DeliveryConfigurationID;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * Searches {@link ProductType}s. Every field that's <code>null</code> is ignored,
 * every field containing a value will cause the query to filter all non-matching instances.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractProductTypeQuery<P extends ProductType>
	extends AbstractJDOQuery<P>
{
	private static final Logger logger = Logger.getLogger(AbstractProductTypeQuery.class);
	
	private static final long serialVersionUID = 3L;

	private String fullTextLanguageID = null;
	private String fullTextSearch = null;
	
	private Boolean published = null;
	private Boolean confirmed = null;
	private Boolean saleable = null;
	private Boolean closed = null;
	
	private PriceConfigID innerPriceConfigID = null;
	private int minNestedProductTypeAmount = -1;
	private int maxNestedProductTypeAmount = -1;
	private AnchorID ownerID = null;
	private Boolean available = null;
	private DeliveryConfigurationID deliveryConfigurationID = null;
	private LocalAccountantDelegateID localAccountantDelegateID = null;
	private ProductTypeGroupID productTypeGroupID = null;
	
	@Override
	protected Query prepareQuery()
	{
		// FIXME: Query also subclasses when JPOX problem is solved
		Query q = getPersistenceManager().newQuery(getPersistenceManager().getExtent(
				ProductType.class, false));
		
		StringBuffer filter = getFilter();
		StringBuffer vars = getVars();
		
		filter.append("\n");
		filter.append("true");
		
		if (fullTextSearch != null) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, "name");
			filter.append("\n )");
		}
		
		if (published != null)
			filter.append("\n && this.published == :published");
		
		if (confirmed != null)
			filter.append("\n && this.confirmed == :confirmed");
		
		if (saleable != null)
			filter.append("\n && this.saleable == :saleable");
				
		if (available != null)
			filter.append("\n && this.available == :available");
		
		if (innerPriceConfigID != null)
		{
			// TODO: JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("\n && JDOHelper.getObjectId(this.innerPriceConfig) == :innerPriceConfigID");
			// WORKAROUND:
			filter.append("\n && (" +
					"this.innerPriceConfig.organisationID == \""+innerPriceConfigID.organisationID+"\" && " +
					"this.innerPriceConfig.priceConfigID == \""+innerPriceConfigID.priceConfigID+"\"" +
							")");
		}
		
		if (minNestedProductTypeAmount >= 0)
			filter.append("\n && :minNestedProductTypeAmount < this.nestedProductTypes.size()");

		if (maxNestedProductTypeAmount >= 0)
			filter.append("\n && :maxNestedProductTypeAmount > this.nestedProductTypes.size()");
		
		if (ownerID != null)
		{
			// TODO: JDOHelper.getObjectId(this.*) does not seem to work
//			filter.append("\n && JDOHelper.getObjectId(this.owner) == :ownerID");
			// WORKAROUND:
			filter.append("\n && (" +
					"this.owner.organisationID == \""+ownerID.organisationID+"\" && " +
					"this.owner.anchorTypeID == \""+ownerID.anchorTypeID+"\" && " +
					"this.owner.anchorID == \""+ownerID.anchorID+"\"" +
							")");
		}
		
		if (deliveryConfigurationID != null)
		{
			// TODO: JDOHelper.getObjectId(this.*) does not seem to work
//		filter.append("\n && JDOHelper.getObjectId(this.deliveryConfiguration) == :deliveryConfigurationID");
			// WORKAROUND:
			filter.append("\n && (" +
				"this.deliveryConfigurationID.organisationID == \""+deliveryConfigurationID.organisationID+"\" && " +
				"this.deliveryConfigurationID.deliveryConfigurationID == \""+deliveryConfigurationID.deliveryConfigurationID+"\"" +
						")");
		}
		
		if (localAccountantDelegateID != null)
		{
			// TODO: JDOHelper.getObjectId(this.*) does not seem to work
//		filter.append("\n && JDOHelper.getObjectId(this.localAccountantDelegate) == :localAccountantDelegateID");
			// WORKAROUND:
			filter.append("\n && (" +
				"this.localAccountantDelegate.organisationID == \""+localAccountantDelegateID.organisationID+"\" && " +
				"this.localAccountantDelegate.localAccountantDelegateID == \""+localAccountantDelegateID.localAccountantDelegateID+"\"" +
						")");
		}

		if (productTypeGroupID != null)
		{
			// TODO: JDOHelper.getObjectId(this.*) does not seem to work
//		filter.append("\n && JDOHelper.getObjectId(this.managedProductTypeGroup) == :productTypeGroupID");
			// WORKAROUND:
			filter.append("\n && (" +
				"this.managedProductTypeGroup.organisationID == \""+productTypeGroupID.organisationID+"\" && " +
				"this.managedProductTypeGroup.productTypeGroupID == \""+productTypeGroupID.productTypeGroupID+"\"" +
						")");
		}
		
		logger.debug("Vars:");
		logger.debug(vars.toString());
		logger.debug("Filter:");
		logger.debug(filter.toString());
		
		q.setFilter(filter.toString());
		q.declareVariables(vars.toString());
		
		return q;
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

	protected void addImport(String importClass) {
//		getImports().append("import "+importClass+"; "+"\n");
		if (getImports().length() > 0)
			getImports().append("; ");
		
		getImports().append("import "+importClass);
	}

	protected void addVariable(String className, String variableName) {
//		getVars().append(className+" "+variableName+"; ");
		if (getVars().length() > 0)
			getVars().append("; ");
		
		getVars().append(className+" "+variableName);
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
	public void setConfirmed(Boolean confirmed) {
		this.confirmed = confirmed;
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
	public void setFullTextLanguageID(String fullTextLanguageID) {
		this.fullTextLanguageID = fullTextLanguageID;
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
	public void setFullTextSearch(String fullTextSearch) {
		this.fullTextSearch = fullTextSearch;
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
	public void setPublished(Boolean published) {
		this.published = published;
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
	public void setSaleable(Boolean saleable) {
		this.saleable = saleable;
	}

	private StringBuffer filter = new StringBuffer();
	protected StringBuffer getFilter() {
		return filter;
	}
	
	private StringBuffer vars = new StringBuffer();
	protected StringBuffer getVars() {
		return vars;
	}
	
	private StringBuffer imports = new StringBuffer();
	protected StringBuffer getImports() {
		return imports;
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
	public void setMinNestedProductTypeAmount(int minNestedProductTypeAmount) {
		this.minNestedProductTypeAmount = minNestedProductTypeAmount;
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
	public void setMaxNestedProductTypeAmount(int maxNestedProductTypeAmount) {
		this.maxNestedProductTypeAmount = maxNestedProductTypeAmount;
	}

	/**
	 * returns the innerPriceConfigID.
	 * @return the innerPriceConfigID
	 */
	public PriceConfigID getInnerPriceConfigID() {
		return innerPriceConfigID;
	}

	/**
	 * sets the innerPriceConfigID
	 * @param innerPriceConfigID the innerPriceConfigID to set
	 */
	public void setInnerPriceConfigID(PriceConfigID innerPriceConfigID) {
		this.innerPriceConfigID = innerPriceConfigID;
	}

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
	public void setOwnerID(AnchorID ownerID) {
		this.ownerID = ownerID;
	}

	/**
	 * returns the deliveryConfigurationID.
	 * @return the deliveryConfigurationID
	 */
	public DeliveryConfigurationID getDeliveryConfigurationID() {
		return deliveryConfigurationID;
	}

	/**
	 * sets the deliveryConfigurationID
	 * @param deliveryConfigurationID the deliveryConfigurationID to set
	 */
	public void setDeliveryConfigurationID(
			DeliveryConfigurationID deliveryConfigurationID) {
		this.deliveryConfigurationID = deliveryConfigurationID;
	}

	/**
	 * returns the localAccountantDelegateID.
	 * @return the localAccountantDelegateID
	 */
	public LocalAccountantDelegateID getLocalAccountantDelegateID() {
		return localAccountantDelegateID;
	}

	/**
	 * sets the localAccountantDelegateID
	 * @param localAccountantDelegateID the localAccountantDelegateID to set
	 */
	public void setLocalAccountantDelegateID(
			LocalAccountantDelegateID localAccountantDelegateID) {
		this.localAccountantDelegateID = localAccountantDelegateID;
	}

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
	public void setProductTypeGroupID(ProductTypeGroupID productTypeGroupID) {
		this.productTypeGroupID = productTypeGroupID;
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
	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	/**
	 * returns the available.
	 * @return the available
	 */
	public Boolean getAvailable() {
		return available;
	}

	/**
	 * sets the available
	 * @param available the available to set
	 */
	public void setAvailable(Boolean available) {
		this.available = available;
	}

}
