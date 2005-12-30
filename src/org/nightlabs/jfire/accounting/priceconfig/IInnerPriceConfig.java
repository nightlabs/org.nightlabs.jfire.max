/*
 * Created on Mar 8, 2005
 */
package org.nightlabs.jfire.accounting.priceconfig;



/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IInnerPriceConfig extends IPriceConfig
{
	public void setPackagingResultPriceConfig(
			String innerProductTypePK, String packageProductTypePK,
			IPriceConfig resultPriceConfig);

	public IPriceConfig getPackagingResultPriceConfig(
			String innerProductTypePK, String packageProductTypePK,
			boolean throwExceptionIfNotExistent);


//	/**
//	 * There are implementations of <tt>PriceConfig</tt> that are useable only within
//	 * product packages, because their values are indefinit (formulas depending on the
//	 * siblings within the package). Therefore a <tt>ProductType</tt> is not saleable directly
//	 * if such a <tt>PriceConfig</tt> is assigned.
//	 *
//	 * @return An implementation of <tt>PriceConfig</tt> must return <tt>true</tt>, if
//	 * it's prices are dependent on the <tt>ProductType</tt>'s siblings within a package
//	 * (or the package itself) and therefore, the <tt>ProductType</tt> cannot be sold outside
//	 * of a package.
//	 */
//	boolean requiresProductTypePackageInternal();

}