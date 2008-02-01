package org.nightlabs.jfire.store;

/**
 * A subclass of {@link Product} can optionally implement this interface in order to model bundle-products instead of
 * individual products.
 * <p>
 * In contrast to managing individual products, one instance of {@link BundleProduct} manages a certain quantity
 * of a given {@link Unit}. In order to prevent calculation errors that result from the inability of the double
 * data type to represent every real number, a quantity is expressed as a long. To model decimal values, the {@link Unit#getDecimalDigitCount()}
 * is used.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public interface BundleProduct
{
//	/**
//	 * Get the approximate quantity as a double. This is the same as first calling {@link #getUnit()} and then {@link Unit#toDouble(long)} 
//	 *
//	 * @return the quantity as double.
//	 */
//	double getQuantityAsDouble();

	/**
	 * Get the virtual quantity of this product. Of course, this is only meaningful together with the
	 * {@link Unit} returned by {@link #getUnit()}.
	 *
	 * @return the virtual quanity.
	 */
	long getQuantity();

	/**
	 * Set the virtual quantity of this product.
	 *
	 * @param quantity the new quantity.
	 */
	void setQuantity(long quantity);

	/**
	 * Get the packaging unit.
	 *
	 * @return the unit.
	 */
	Unit getUnit();

	/**
	 * Set the packaging unit.
	 *
	 * @param unit the new unit.
	 */
	void setUnit(Unit unit);
}
