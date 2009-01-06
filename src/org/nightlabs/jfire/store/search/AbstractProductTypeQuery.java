package org.nightlabs.jfire.store.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypePermissionFlagSet;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.CollectionUtil;

/**
 * Searches {@link ProductType}s. Every field that's <code>null</code> is ignored,
 * every field containing a value will cause the query to filter all non-matching instances.
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class AbstractProductTypeQuery
extends VendorDependentQuery
implements ISaleAccessQuery
{
	private static final long serialVersionUID = 20081118L;

	private String fullTextLanguageID = null;
	private boolean fullTextSearchRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String fullTextSearchExpr;
	private String fullTextSearch = null;

	private Boolean published = null;
	private Boolean confirmed = null;
	private Boolean saleable = null;
	private Boolean closed = null;

//	private Boolean permissionGrantedToSee = Boolean.TRUE;
	private Boolean permissionGrantedToSell = null;
	private Boolean permissionGrantedToReverse = null;

	private int minNestedProductTypeAmount = -1;
	private int maxNestedProductTypeAmount = -1;
	private AnchorID ownerID = null;
	private ProductTypeGroupID productTypeGroupID = null;
	private String organisationID = null;
	// this value is initially set to avoid finding productType categories by default
	private Byte inheritanceNature = ProductType.INHERITANCE_NATURE_LEAF;

	// Is used for permissionGranted* checks. Currently, it's simply assigned to
	// the current user's value - this might later be manually assignable and non-transient.
	private transient UserID userID = null;

	public static final class FieldName
	{
		public static final String closed = "closed";
		public static final String confirmed = "confirmed";
		public static final String fullTextSearchRegex = "fullTextSearchRegex";
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

//		public static final String permissionGrantedToSee = "permissionGrantedToSee";
		public static final String permissionGrantedToSell = "permissionGrantedToSell";
		public static final String permissionGrantedToReverse = "permissionGrantedToReverse";
	}

	@Override
	protected Query createQuery()
	{
		return getPersistenceManager().newQuery(getCandidateClass());
	}

// TODO temporarily deactivated the inclusion of the ProductTypePermissionFlagSet into the query. Have to file a DataNucleus bug and put URL here!!! Marco.
// This code produces EXTREMELY slow queries (because of CROSS JOIN):
//
// The following JDOQL:
//
//	SELECT FROM org.nightlabs.crossticket.trade.store.Event
//	WHERE true
//	 && this.organisationID == productTypePermissionFlagSetSee.productTypeOrganisationID
//	 && this.productTypeID == productTypePermissionFlagSetSee.productTypeID
//	 && productTypePermissionFlagSetSee.userOrganisationID == :userID.organisationID
//	 && productTypePermissionFlagSetSee.userID == :userID.userID
//	 && productTypePermissionFlagSetSee.flagsSeeProductType == 0
//	 && this.inheritanceNature == :inheritanceNature
//	 && JDOHelper.getObjectId(this.vendor) == :vendorID
//	 && ( this.performanceTimes.contains(varPerformanceTime))
//	VARIABLES org.nightlabs.jfire.store.ProductTypePermissionFlagSet productTypePermissionFlagSetSee; org.nightlabs.crossticket.trade.store.PerformanceTime varPerformanceTime"
//
// is translated to the following highly ineffecient SQL:
//
//	SELECT
//	  'org.nightlabs.crossticket.trade.store.Event        ' AS NUCMETADATA,
//	  this_1.opt_version,
//	  this_1.close_timestamp,
//	  this_1.confirmed,
//	  this_1.inheritance_nature,
//	  this_1.organisation_id,
//	  this_1.package_nature,
//	  this_1.product_available,
//	  this_1.product_type_id,
//	  this_1.published,
//	  this_1.saleable,
//	  this.event_package,
//	  this.next_performance_time_id,
//	  this.performance_container
//	FROM crosstickettrade_event this
//	LEFT OUTER JOIN crosstickettrade_resellerevent subelement0
//	  ON
//	    subelement0.organisation_id = this.organisation_id AND
//	    subelement0.product_type_id = this.product_type_id
//	INNER JOIN jfiretrade_producttype this_1
//	  ON
//	    this_1.organisation_id = this.organisation_id AND
//	    this_1.product_type_id = this.product_type_id
//	CROSS JOIN jfiretrade_producttypepermissionflagset unbound_product_type_permission_flag_set_see
//	WHERE
//	  (
//	    EXISTS (
//	      SELECT 1
//	      FROM crosstickettrade_performancetime this_performancetimes_varperformancetime
//	      WHERE
//		  this_performancetimes_varperformancetime.event_organisation_id_oid = this.organisation_id AND
//		  this_performancetimes_varperformancetime.event_product_type_id_oid = this.product_type_id AND
//		  this_1.organisation_id = unbound_product_type_permission_flag_set_see.product_type_organisation_id AND
//		  this_1.product_type_id = unbound_product_type_permission_flag_set_see.product_type_id AND
//		  unbound_product_type_permission_flag_set_see.user_organisation_id = 'chezfrancois.jfire.org' AND
//		  unbound_product_type_permission_flag_set_see.user_id = 'francois' AND
//		  unbound_product_type_permission_flag_set_see.flags_see_product_type = 0 AND
//		  this_1.inheritance_nature = 12 AND
//		  'org.nightlabs.jfire.trade.OrganisationLegalEntity' = this_1.vendor_anchor_id_oid AND
//		  'LegalEntity' = this_1.vendor_anchor_type_id_oid AND
//		  'chezfrancois.jfire.org' = this_1.vendor_organisation_id_oid
//	    )
//	  ) AND
//	  subelement0.organisation_id IS NULL AND
//	  subelement0.product_type_id IS NULL
//	UNION
//	  SELECT 'org.nightlabs.crossticket.trade.store.ResellerEvent' AS NUCMETADATA,
//	  this_1.opt_version,
//	  this_1.close_timestamp,
//	  this_1.confirmed,
//	  this_1.inheritance_nature,
//	  this_1.organisation_id,
//	  this_1.package_nature,
//	  this_1.product_available,
//	  this_1.product_type_id,
//	  this_1.published,this_1.saleable,
//	  this.event_package,
//	  this.next_performance_time_id,
//	  this.performance_container
//	FROM crosstickettrade_event this
//	INNER JOIN crosstickettrade_resellerevent `element`
//	  ON
//	    `element`.organisation_id = this.organisation_id AND
//	    `element`.product_type_id = this.product_type_id
//	INNER JOIN jfiretrade_producttype this_1
//	  ON
//	    this_1.organisation_id = this.organisation_id AND
//	    this_1.product_type_id = this.product_type_id
//	CROSS JOIN jfiretrade_producttypepermissionflagset unbound_product_type_permission_flag_set_see
//	WHERE
//	  (
//	    EXISTS (
//	      SELECT 1
//	      FROM crosstickettrade_performancetime this_performancetimes_varperformancetime
//	      WHERE this_performancetimes_varperformancetime.event_organisation_id_oid = this.organisation_id AND
//		this_performancetimes_varperformancetime.event_product_type_id_oid = this.product_type_id AND
//		this_1.organisation_id = unbound_product_type_permission_flag_set_see.product_type_organisation_id AND
//		this_1.product_type_id = unbound_product_type_permission_flag_set_see.product_type_id AND
//		unbound_product_type_permission_flag_set_see.user_organisation_id = 'chezfrancois.jfire.org' AND
//		unbound_product_type_permission_flag_set_see.user_id = 'francois' AND
//		unbound_product_type_permission_flag_set_see.flags_see_product_type = 0 AND
//		this_1.inheritance_nature = 12 AND
//		'org.nightlabs.jfire.trade.OrganisationLegalEntity' = this_1.vendor_anchor_id_oid AND
//		'LegalEntity' = this_1.vendor_anchor_type_id_oid AND
//		'chezfrancois.jfire.org' = this_1.vendor_organisation_id_oid
//	    )
//	  )



//	protected void populateFilterPermissionGranted(
//			String productTypePermissionFlagSetFlagsFieldName,
//			boolean permissionGranted, String variableName
//	)
//	{
//		StringBuilder filter = getFilter();
//		addVariable(ProductTypePermissionFlagSet.class, variableName);
////		// joining via primary key - hopefully faster than other fields
//		filter.append("\n && this.organisationID == " + variableName + ".productTypeOrganisationID");
//		filter.append("\n && this.productTypeID == " + variableName + ".productTypeID");
//		filter.append("\n && " + variableName + ".userOrganisationID == :userID.organisationID");
//		filter.append("\n && " + variableName + ".userID == :userID.userID");
//		filter.append("\n && " + variableName + '.' + productTypePermissionFlagSetFlagsFieldName + " ");
//		if (permissionGranted)
//			filter.append("== 0");
//		else
//			filter.append("!= 0");
//	}

	@Override
	protected void prepareQuery(Query q)
	{
		StringBuilder filter = getFilter();
		filter.append("true");

		userID = SecurityReflector.getUserDescriptor().getUserObjectID();

// TODO temporarily deactivated the method populateFilterPermissionGranted(...) - see above!
//		if (!User.USER_ID_SYSTEM.equals(userID.userID)) // the system user is allowed to see/do everything and has no ProductTypePermissionFlagSet. TODO maybe we should give him ProductTypePermissionFlagSet entries?!
//			populateFilterPermissionGranted("flagsSeeProductType", true, "productTypePermissionFlagSetSee");
//
//		if (isFieldEnabled(FieldName.permissionGrantedToSell) && permissionGrantedToSell != null) {
//			if (!User.USER_ID_SYSTEM.equals(userID.userID) || !permissionGrantedToSell) // the system user is allowed to see/do everything and has no ProductTypePermissionFlagSet. TODO maybe we should give him ProductTypePermissionFlagSet entries?!
//				populateFilterPermissionGranted("flagsSellProductType", permissionGrantedToSell, "productTypePermissionFlagSetSell");
//		}
//
//		if (isFieldEnabled(FieldName.permissionGrantedToReverse) && permissionGrantedToReverse != null) {
//			if (!User.USER_ID_SYSTEM.equals(userID.userID) || !permissionGrantedToReverse) // the system user is allowed to see/do everything and has no ProductTypePermissionFlagSet. TODO maybe we should give him ProductTypePermissionFlagSet entries?!
//				populateFilterPermissionGranted("flagsReverseProductType", permissionGrantedToReverse, "productTypePermissionFlagSetReverse");
//		}

		if (isFieldEnabled(FieldName.fullTextSearch) && fullTextSearch != null) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, ProductType.FieldName.name);
			filter.append("\n )");
		}

		if (isFieldEnabled(FieldName.published) && published != null)
			filter.append("\n && this."+ProductType.FieldName.published+" == :published");

		if (isFieldEnabled(FieldName.confirmed) && confirmed != null)
			filter.append("\n && this."+ProductType.FieldName.confirmed+" == :confirmed");

		if (isFieldEnabled(FieldName.saleable) && saleable != null)
			filter.append("\n && this."+ProductType.FieldName.saleable+" == :saleable");

		if (isFieldEnabled(FieldName.closed) && closed != null) {
			filter.append("\n && this."+ProductType.FieldName.closeTimestamp);
			if (closed)
				filter.append(" != null");
			else
				filter.append(" == null");
		}

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
		q.declareVariables(getVars());
	}

	// TODO DataNucleus WORKAROUND: remove this temporary workaround (the whole implementation of postProcessQueryResult) and activate the populateFilterPermissionGranted(...) above.
	@Override
	protected Object postProcessQueryResult(Object result) {
		Collection<? extends ProductType> rawProductTypes = CollectionUtil.castCollection((Collection<?>) super.postProcessQueryResult(result));
		List<ProductType> filteredProductTypes = new ArrayList<ProductType>(rawProductTypes.size());
		PersistenceManager pm = getPersistenceManager();

		if (userID == null)
			throw new IllegalStateException("userID is null! Was prepareQuery(...) not called before?!");

		iterateProductTypes: for (ProductType productType : rawProductTypes) {
			ProductTypePermissionFlagSet ptpfs;
			try {
				ptpfs = (ProductTypePermissionFlagSet) pm.getObjectById(
						ProductTypePermissionFlagSetID.create((ProductTypeID)JDOHelper.getObjectId(productType), userID)
				);
			} catch (JDOObjectNotFoundException x) {
				continue iterateProductTypes;
			}


			if (!User.USER_ID_SYSTEM.equals(userID.userID)) { // the system user is allowed to see/do everything and has no ProductTypePermissionFlagSet. TODO maybe we should give him ProductTypePermissionFlagSet entries?!
				if (ptpfs.getFlags(org.nightlabs.jfire.store.RoleConstants.seeProductType) != 0)
					continue iterateProductTypes;
			}

			if (isFieldEnabled(FieldName.permissionGrantedToSell) && permissionGrantedToSell != null) {
				if (!User.USER_ID_SYSTEM.equals(userID.userID) || !permissionGrantedToSell) { // the system user is allowed to see/do everything and has no ProductTypePermissionFlagSet. TODO maybe we should give him ProductTypePermissionFlagSet entries?!
					if (ptpfs.getFlags(org.nightlabs.jfire.trade.RoleConstants.sellProductType) != 0)
						continue iterateProductTypes;
				}
			}

			if (isFieldEnabled(FieldName.permissionGrantedToReverse) && permissionGrantedToReverse != null) {
				if (!User.USER_ID_SYSTEM.equals(userID.userID) || !permissionGrantedToReverse) { // the system user is allowed to see/do everything and has no ProductTypePermissionFlagSet. TODO maybe we should give him ProductTypePermissionFlagSet entries?!
					if (ptpfs.getFlags(org.nightlabs.jfire.trade.RoleConstants.reverseProductType) != 0)
						continue iterateProductTypes;
				}
			}


			filteredProductTypes.add(productType);
		}

		return filteredProductTypes;
	}

	protected void addFullTextSearch(StringBuilder filter, String member) {
		String varName = member+"Var";
		addVariable(String.class, varName);
		String containsStr = "containsValue("+varName+")";
		if (fullTextLanguageID != null)
			containsStr = "containsEntry(\""+fullTextLanguageID+"\","+varName+")";
		fullTextSearchExpr = isFullTextSearchRegex() ? fullTextSearch : ".*" + fullTextSearch + ".*";
		filter.append("\n (\n" +
				"  this."+member+".names."+containsStr+"\n" +
				"  && "+varName+".toLowerCase().matches(:fullTextSearchExpr.toLowerCase())" +
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
	 * @return Whether the value set with {@link #setFullTextSearch(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setFullTextSearch(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isFullTextSearchRegex() {
		return fullTextSearchRegex;
	}

	/**
	 * Sets whether the value set with {@link #setFullTextSearch(String)} represents a
	 * regular expression.
	 *
	 * @param fullTextSearchRegex The fullTextSearchRegex to search.
	 */
	public void setFullTextSearchRegex(boolean fullTextSearchRegex) {
		final boolean oldFullTextSearchRegex = this.fullTextSearchRegex;
		this.fullTextSearchRegex = fullTextSearchRegex;
		notifyListeners(FieldName.fullTextSearchRegex, oldFullTextSearchRegex, fullTextSearchRegex);
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

//	public Boolean getPermissionGrantedToSee() {
//		return permissionGrantedToSee;
//	}
//	public void setPermissionGrantedToSee(Boolean permissionGrantedToSee) {
//		Boolean oldPermissionGrantedToSee = this.permissionGrantedToSee;
//		this.permissionGrantedToSee = permissionGrantedToSee;
//		notifyListeners(FieldName.permissionGrantedToSee, oldPermissionGrantedToSee, permissionGrantedToSee);
//	}
	public Boolean getPermissionGrantedToSell() {
		return permissionGrantedToSell;
	}
	public void setPermissionGrantedToSell(Boolean permissionGrantedToSell) {
		Boolean oldPermissionGrantedToSell = this.permissionGrantedToSell;
		this.permissionGrantedToSell = permissionGrantedToSell;
		notifyListeners(FieldName.permissionGrantedToSell, oldPermissionGrantedToSell, permissionGrantedToSell);
	}
	public Boolean getPermissionGrantedToReverse() {
		return permissionGrantedToReverse;
	}
	public void setPermissionGrantedToReverse(Boolean permissionGrantedToReverse) {
		Boolean oldPermissionGrantedToReverse = this.permissionGrantedToReverse;
		this.permissionGrantedToReverse = permissionGrantedToReverse;
		notifyListeners(FieldName.permissionGrantedToReverse, oldPermissionGrantedToReverse, permissionGrantedToReverse);
	}
}
