package org.nightlabs.jfire.store.search;

import javax.jdo.Query;

import org.nightlabs.jfire.store.ProductType;
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
	private static final long serialVersionUID = 4L;

	private String fullTextLanguageID = null;
	private String fullTextSearch = null;

	private Boolean published = null;
	private Boolean confirmed = null;
	private Boolean saleable = null;
	private Boolean closed = null;

	private int minNestedProductTypeAmount = -1;
	private int maxNestedProductTypeAmount = -1;
	private AnchorID ownerID = null;
	private ProductTypeGroupID productTypeGroupID = null;
	private String organisationID = null;
	// this value is initially set to avoid finding productType categories by default
	private Byte inheritanceNature = ProductType.INHERITANCE_NATURE_LEAF;

	public static final class FieldName
	{
		public static final String closed = "closed";
		public static final String confirmed = "confirmed";
		public static final String fullTextLanguageID = "fullTextLanguageID";
		public static final String fullTextSearch = "fullTextSearch";
		public static final String maxNestedProductTypeAmount = "maxNestedProductTypeAmount";
		public static final String minNestedProductTypeAmount = "minNestedProductTypeAmount";
		public static final String ownerID = "ownerID";
		public static final String productTypeGroupID = "productTypeGroupID";
		public static final String published = "published";
		public static final String saleable = "saleable";
		public static final String organisationID = "organisationID";
		public static final String inheritanceNature = "inheritanceNature";
	}

	@Override
	protected Query createQuery()
	{
		return getPersistenceManager().newQuery(getCandidateClass());
	}

	@Override
	protected void prepareQuery(Query q)
	{
		StringBuffer filter = getFilter();
		StringBuffer vars = getVars();

		filter.append("true");

		if (isFieldEnabled(FieldName.fullTextSearch) && fullTextSearch != null) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, ProductType.FieldName.name);
			filter.append("\n )");
		}

		if (isFieldEnabled(FieldName.published) && published != null)
			filter.append("\n && this."+ProductType.FieldName.published+" == :published");

		if (isFieldEnabled(FieldName.confirmed) && confirmed != null)
			filter.append("\n && this."+ProductType.FieldName.confirmed+" == :confirmed");

		if (isFieldEnabled(FieldName.saleable) && saleable != null)
			filter.append("\n && this."+ProductType.FieldName.saleable+" == :saleable");

		if (isFieldEnabled(FieldName.closed) && closed != null)
			filter.append("\n && this."+ProductType.FieldName.closed+" == :closed");

		if (isFieldEnabled(FieldName.minNestedProductTypeAmount) && minNestedProductTypeAmount >= 0)
			filter.append("\n && :minNestedProductTypeAmount < this."+ProductType.FieldName.productTypeLocal+"."+
					org.nightlabs.jfire.store.ProductTypeLocal.FieldName.nestedProductTypeLocals+".size()");

		if (isFieldEnabled(FieldName.maxNestedProductTypeAmount) && maxNestedProductTypeAmount >= 0)
			filter.append("\n && :maxNestedProductTypeAmount > this."+ProductType.FieldName.productTypeLocal+"."+
					org.nightlabs.jfire.store.ProductTypeLocal.FieldName.nestedProductTypeLocals+".size()");

		if (isFieldEnabled(FieldName.ownerID) && ownerID != null) {
			filter.append("\n && JDOHelper.getObjectId(this."+ProductType.FieldName.owner+") == :ownerID");
		}

		if (isFieldEnabled(FieldName.productTypeGroupID) && productTypeGroupID != null) {
			filter.append("\n && JDOHelper.getObjectId(this."+ProductType.FieldName.managedProductTypeGroup+") == :productTypeGroupID");
		}

		if (isFieldEnabled(FieldName.organisationID) && organisationID != null)
			filter.append("\n && this."+ProductType.FieldName.organisationID+" == :organisationID");

		if (isFieldEnabled(FieldName.inheritanceNature) && inheritanceNature != null)
			filter.append("\n && this."+ProductType.FieldName.inheritanceNature+" == :inheritanceNature");

		if (isFieldEnabled(VendorDependentQuery.FieldName.vendorID) && getVendorID() != null)
			filter.append("\n && JDOHelper.getObjectId(this."+ProductType.FieldName.vendor+") == :vendorID");

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
		notifyListeners(FieldName.confirmed, oldConfirmed, confirmed);
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
		notifyListeners(FieldName.fullTextLanguageID, oldFullTextLanguageID, fullTextLanguageID);
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
		notifyListeners(FieldName.fullTextSearch, oldFullTextSearch, fullTextSearch);
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
		notifyListeners(FieldName.published, oldPublished, published);
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
		notifyListeners(FieldName.saleable, oldSaleable, saleable);
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
		notifyListeners(FieldName.minNestedProductTypeAmount, oldMinNestedProductTypeAmount,
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
		notifyListeners(FieldName.maxNestedProductTypeAmount, oldMaxNestedProductTypeAmount,
			maxNestedProductTypeAmount);
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
	public void setOwnerID(AnchorID ownerID)
	{
		final AnchorID oldOwnerID = this.ownerID;
		this.ownerID = ownerID;
		notifyListeners(FieldName.ownerID, oldOwnerID, ownerID);
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
	public void setProductTypeGroupID(ProductTypeGroupID productTypeGroupID)
	{
		final ProductTypeGroupID oldProductTypeGroupID = this.productTypeGroupID;
		this.productTypeGroupID = productTypeGroupID;
		notifyListeners(FieldName.productTypeGroupID, oldProductTypeGroupID, productTypeGroupID);
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
		notifyListeners(FieldName.closed, oldClosed, closed);
	}

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
		notifyListeners(FieldName.organisationID, oldOrganisationID, organisationID);
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

}
