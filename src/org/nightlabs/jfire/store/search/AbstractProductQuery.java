package org.nightlabs.jfire.store.search;

import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.store.Product;
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
	public static final class FieldName
	{
		public static final String organisationID = "organisationID";
		public static final String productID = "productID";
		public static final String productTypeID = "productTypeID";
	}

	private String organisationID;
	private Long productID;
	private ProductTypeID productTypeID;

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractJDOQuery#prepareQuery(javax.jdo.Query)
	 */
	@Override
	protected void prepareQuery(Query q) {
//		PersistenceManager pm = getPersistenceManager();
		StringBuilder filter = getFilter();
		filter.append("true");

		if (isFieldEnabled(FieldName.productTypeID) && productTypeID != null) {
			filter.append("\n && JDOHelper.getObjectId(this."+Product.FieldName.productType+") == :"+FieldName.productTypeID);
		}
		if (isFieldEnabled(FieldName.organisationID) && organisationID != null) {
			filter.append("\n && this."+Product.FieldName.organisationID+" == :"+FieldName.organisationID);
		}
		if (isFieldEnabled(FieldName.productID) && productID != null) {
			filter.append("\n && this."+Product.FieldName.productID+" == :"+FieldName.productID);
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
}