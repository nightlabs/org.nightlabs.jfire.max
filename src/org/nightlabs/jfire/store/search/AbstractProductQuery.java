/**
 * 
 */
package org.nightlabs.jfire.store.search;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.Product.FieldName;
import org.nightlabs.jfire.store.id.ProductTypeID;

/**
 * Abstract base class for queries which searches for {@link Product}s. 
 * Every field that's <code>null</code> is ignored,
 * every field containing a value will cause the query to filter all non-matching instances.
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public abstract class AbstractProductQuery 
extends AbstractJDOQuery 
{
	private static final String PROPERTY_PREFIX = "AbstractProductQuery";
	public static final String PROPERTY_ORGANISATION_ID = PROPERTY_PREFIX + "organisationID";
	public static final String PROPERTY_PRODUCT_ID = PROPERTY_PREFIX + "productID";
	public static final String PROPERTY_PRODUCT_TYPE_ID = PROPERTY_PREFIX + "productTypeID";
	
	private String organisationID;
	private Long productID;
	private ProductTypeID productTypeID;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractJDOQuery#prepareQuery(javax.jdo.Query)
	 */
	@Override
	protected void prepareQuery(Query q) {
		PersistenceManager pm = getPersistenceManager();
		StringBuffer filter = getFilter();
		filter.append("true");
	
		if (productTypeID != null) {
			filter.append("\n && JDOHelper.getObjectId(this."+FieldName.productType+") == :productTypeID");
		}
		if (organisationID != null) {
			filter.append("\n && this."+FieldName.organisationID+" == :organisationID");
		}
		if (productID != null) {
			filter.append("\n && this."+FieldName.productID+" == :productID");
		}
		
		q.setFilter(filter.toString());
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
		this.organisationID = organisationID;
	}
	
	/**
	 * Returns the productID.
	 * @return the productID
	 */
	public long getProductID() {
		return productID;
	}
	
	/**
	 * Sets the productID.
	 * @param productID the productID to set
	 */
	public void setProductID(long productID) {
		this.productID = productID;
	}
	
	/**
	 * Returns the productTypeID.
	 * @return the productTypeID
	 */
	public ProductTypeID getProductTypeID() {
		return productTypeID;
	}
	
	/**
	 * Sets the productTypeID.
	 * @param productTypeID the productTypeID to set
	 */
	public void setProductTypeID(ProductTypeID productTypeID) {
		this.productTypeID = productTypeID;
	}
	
	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		List<FieldChangeCarrier> changedFields = super.getChangedFields(propertyName);
		boolean allFields = AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(propertyName);
		
		if (allFields || PROPERTY_ORGANISATION_ID.equals(propertyName)) {
			changedFields.add( new FieldChangeCarrier(PROPERTY_ORGANISATION_ID, organisationID) );
		}
		if (allFields || PROPERTY_PRODUCT_ID.equals(propertyName)) {
			changedFields.add( new FieldChangeCarrier(PROPERTY_PRODUCT_ID, productID) );
		}
		if (allFields || PROPERTY_PRODUCT_TYPE_ID.equals(propertyName)){
			changedFields.add( new FieldChangeCarrier(PROPERTY_PRODUCT_TYPE_ID, productTypeID) );
		}		
		return changedFields;
	}	
}
