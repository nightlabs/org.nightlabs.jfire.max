/*
 * Created on Jul 15, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig;

import org.nightlabs.ipanema.accounting.PriceFragmentType;
import org.nightlabs.ipanema.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.ipanema.accounting.priceconfig.IPriceConfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IFormulaPriceConfig extends IInnerPriceConfig
{
	/**
	 * Calls {@link #adoptParameters(TariffPriceConfig, boolean)} with <tt>onlyAdd=false</tt>.
	 */
	void adoptParameters(IPriceConfig other);
	
	/**
	 * This method adjusts the own parameter config according to the given other
	 * TicketingPriceConfig. After this method has been called, this instance
	 * has the same <tt>CustomerGroup</tt> s, <tt>SaleMode</tt> s, <tt>Tariff</tt> s,
	 * <tt>CategorySet</tt> s and <tt>Currency</tt> s as the other. While adopting it,
	 * the two descendants <tt>FormulaPriceConfig</tt> and <tt>StablePriceConfig</tt>
	 * create missing formula/price cells and remove cells that are not needed anymore.
	 * <p>
	 * Note, that this method leaves the <tt>PriceFragmentType</tt> s untouched!
	 * <tt>PriceFragmentType</tt> s are different, because they do not define cells, but
	 * are fragments within a cell. Additionally, we need to handover all fragments and
	 * cannot ignore any. All the other parameters are filtered by whatever the guiding
	 * inner price config defines, but <tt>PriceFragmentType</tt> s are merged (i.e. one
	 * occurence anywhere in the package forces the packagePriceConfig to know it).
	 *
	 * @param other The other TariffPriceConfig from which to take over the parameter config.
	 * @param onlyAdd If this is true, no parameter will be removed and only missing params added.
	 */
	void adoptParameters(IPriceConfig _other, boolean onlyAdd);

	/**
	 * @param throwExceptionIfNotExistent If <tt>true</tt> this method throws a
	 *		<tt>NullPointerException</tt> with a nice message or, if <tt>false</tt>,
	 *		it returns <tt>null</tt>, in case, no <tt>FormulaCell</tt> is existing as
	 *		fallback.
	 *
	 * @return Returns a <tt>FormulaCell</tt> that serves as fallback if no specific one
	 * for a certain coordinate exists or <tt>null</tt> if the fallbackFormulaCell is not
	 * defined. If <tt>throwExceptionIfNotExistent==true</tt>, this method never returns
	 * <tt>null</tt>. 
	 */
	FormulaCell getFallbackFormulaCell(boolean throwExceptionIfNotExistent);

	FormulaCell createFallbackFormulaCell();

	void setFallbackFormula(PriceFragmentType priceFragmentType, String formula);

	FormulaCell getFormulaCell(IPriceCoordinate priceCoordinate, boolean throwExceptionIfNotExistent);

	void setFormula(IPriceCoordinate priceCoordinate, PriceFragmentType priceFragmentType, String formula);
}
