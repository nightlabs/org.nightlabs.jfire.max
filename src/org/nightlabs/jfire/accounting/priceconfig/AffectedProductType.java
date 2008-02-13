package org.nightlabs.jfire.accounting.priceconfig;

import java.io.Serializable;

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
}
