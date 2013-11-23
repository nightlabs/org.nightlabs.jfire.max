package org.nightlabs.jfire.accounting.priceconfig;

import java.io.Serializable;

import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;

public class AffectedProductType
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static enum CauseType {
		ROOT,
		SIBLING,
		NESTED
	};

	private ProductTypeID causeProductTypeID;

	/**
	 * This defines, how the {@link #productTypeID} is affected by {@link #causeProductTypeID}.
	 * If {@link #causeProductTypeID} is a sibling of {@link #productTypeID}, the type will be
	 * {@link CauseType#SIBLING}. If {@link #causeProductTypeID} references a package producttype
	 * ({@link ProductType#getPackageNature()} == {@link ProductType#PACKAGE_NATURE_OUTER}) and
	 * it contains an inner-price-config (virtual self-packaging)
	 * If {@link #causeProductTypeID} is nested within {@link #productTypeID},
	 * it is {@link CauseType#NESTED}.
	 */
	private CauseType causeType;

	private ProductTypeID productTypeID;

	/**
	 * @param causeProductTypeID Can be <code>null</code>, if causeType is {@link CauseType#ROOT}.
	 * @param causeType The relationship between causeProductTypeID and productTypeID - i.e. why productTypeID is affected.
	 * @param productTypeID References the {@link ProductType} which is affected by the modification of {@link #causeProductTypeID} or
	 *		the root of the changes (i.e. the productType that is directly affected by the user).
	 */
	public AffectedProductType(ProductTypeID causeProductTypeID, CauseType causeType, ProductTypeID productTypeID)
	{
		this.causeProductTypeID = causeProductTypeID;

		if (causeType == null)
			throw new IllegalArgumentException("causeType must not be null!");
		this.causeType = causeType;

		if (causeProductTypeID == null && !CauseType.ROOT.equals(causeType))
			throw new IllegalArgumentException("causeProductTypeID == null but causeType is not ROOT!");

		if (productTypeID == null)
			throw new IllegalArgumentException("productTypeID must not be null!");
		this.productTypeID = productTypeID;
	}

	/**
	 * @return the modified productType which affects this productType (returned by {@link #getProductTypeID()})
	 *		to be changed. This can be <code>null</code>, if the productType is the root of the changes.
	 */
	public ProductTypeID getCauseProductTypeID()
	{
		return causeProductTypeID;
	}
	public CauseType getCauseType()
	{
		return causeType;
	}
	public ProductTypeID getProductTypeID()
	{
		return productTypeID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((causeProductTypeID == null) ? 0 : causeProductTypeID
						.hashCode());
		result = prime * result
				+ ((causeType == null) ? 0 : causeType.hashCode());
		result = prime * result
				+ ((productTypeID == null) ? 0 : productTypeID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AffectedProductType other = (AffectedProductType) obj;
		if (causeProductTypeID == null) {
			if (other.causeProductTypeID != null)
				return false;
		} else if (!causeProductTypeID.equals(other.causeProductTypeID))
			return false;
		if (causeType != other.causeType)
			return false;
		if (productTypeID == null) {
			if (other.productTypeID != null)
				return false;
		} else if (!productTypeID.equals(other.productTypeID))
			return false;
		return true;
	}
	
}
