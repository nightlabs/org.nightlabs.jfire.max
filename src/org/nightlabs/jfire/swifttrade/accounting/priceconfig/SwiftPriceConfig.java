package org.nightlabs.jfire.swifttrade.accounting.priceconfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 *
 * @jdo.fetch-group name="SwiftPriceConfig.inputPriceFragmentTypes" fields="inputPriceFragmentTypes"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="inputPriceFragmentTypes"
 */
public class SwiftPriceConfig
extends FormulaPriceConfig
{
	private static final long serialVersionUID = 1L;

	/**
	 * This <code>Set</code> specifies which {@link PriceFragmentType}s (having been
	 * added by {@link FormulaPriceConfig#addPriceFragmentType(PriceFragmentType)})
	 * will be used as input values. The input values are obtained from the user
	 * whenever an {@link org.nightlabs.jfire.trade.Article} is added to an
	 * {@link org.nightlabs.jfire.trade.Offer}. Then, the price for this article is
	 * calculated based on these values.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="PriceFragmentType"
	 *		table="JFireSwiftTrade_SwiftPriceConfig_inputPriceFragmentTypes"
	 *
	 * @jdo.join
	 */
	private Set<PriceFragmentType> inputPriceFragmentTypes;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<PriceFragmentType> inputPriceFragmentTypes_readonly = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected SwiftPriceConfig() { }

	public SwiftPriceConfig(String organisationID, long priceConfigID)
	{
		super(organisationID, priceConfigID);
		this.inputPriceFragmentTypes = new HashSet<PriceFragmentType>();
	}

	public Set<PriceFragmentType> getInputPriceFragmentTypes()
	{
		if (inputPriceFragmentTypes_readonly == null)
			inputPriceFragmentTypes_readonly = Collections.unmodifiableSet(inputPriceFragmentTypes);

		return inputPriceFragmentTypes_readonly;
	}

	public void addInputPriceFragmentType(PriceFragmentType priceFragmentType)
	{
		inputPriceFragmentTypes.add(priceFragmentType);
	}

	public void removeInputPriceFragmentType(PriceFragmentType priceFragmentType)
	{
		inputPriceFragmentTypes.remove(priceFragmentType);
	}

//	@Override
//	public void addProductType(ProductType productType)
//	{
//		throw new UnsupportedOperationException("SwiftPriceConfig does not support nesting of other ProductTypes!");
//	}

	@Override
	public void jdoPreStore()
	{
		super.jdoPreStore();
		clearPackagingResultPriceConfigs(); // we never store the results, because prices are dynamic
	}
}
