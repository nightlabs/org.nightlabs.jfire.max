package org.nightlabs.jfire.dynamictrade.accounting.priceconfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDynamicTrade_DynamicTradePriceConfig")
@FetchGroups({
	@FetchGroup(
		name="DynamicTradePriceConfig.inputPriceFragmentTypes",
		members=@Persistent(name="inputPriceFragmentTypes")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsPriceConfig.edit",
		members=@Persistent(name="inputPriceFragmentTypes"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DynamicTradePriceConfig
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
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireDynamicTrade_DynamicTradePriceConfig_inputPriceFragmentTypes",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<PriceFragmentType> inputPriceFragmentTypes;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Set<PriceFragmentType> inputPriceFragmentTypes_readonly = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicTradePriceConfig() { }

	public DynamicTradePriceConfig(final PriceConfigID priceConfigID)
	{
		super(priceConfigID);
		this.inputPriceFragmentTypes = new HashSet<PriceFragmentType>();
	}

	public Set<PriceFragmentType> getInputPriceFragmentTypes()
	{
		if (inputPriceFragmentTypes_readonly == null)
			inputPriceFragmentTypes_readonly = Collections.unmodifiableSet(inputPriceFragmentTypes);

		return inputPriceFragmentTypes_readonly;
	}

	public void addInputPriceFragmentType(final PriceFragmentType priceFragmentType)
	{
		inputPriceFragmentTypes.add(priceFragmentType);
	}

	public void removeInputPriceFragmentType(final PriceFragmentType priceFragmentType)
	{
		inputPriceFragmentTypes.remove(priceFragmentType);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * {@link DynamicTradePriceConfig} overrides this method and exposes it publicly.
	 * </p>
	 */
	@Override
	public void clearPackagingResultPriceConfigs() {
		super.clearPackagingResultPriceConfigs();
	}

//	@Override
//	public void addProductType(ProductType productType)
//	{
//		throw new UnsupportedOperationException("DynamicTradePriceConfig does not support nesting of other ProductTypes!");
//	}

	@Override
	public void jdoPreAttach() {
		super.jdoPreAttach();
		try {
			clearPackagingResultPriceConfigs(); // we never store the results, because prices are dynamic
		} catch (final JDODetachedFieldAccessException x) {
			// silently ignore it - the field packagingResultPriceConfigs is not detached => no problem.
		}
	}

	@Override
	public void jdoPostAttach(final Object detached) {
		super.jdoPostAttach(detached);

		// make sure that the field packagingResultPriceConfigs is really empty
		// (in case the JDODetachedFieldAccessException above in jdoPreAttach() was
		// not caused by this field (highly unlikely)).
		clearPackagingResultPriceConfigs(); // we never store the results, because prices are dynamic
	}

	@Override
	public void jdoPreStore()
	{
		super.jdoPreStore();
		clearPackagingResultPriceConfigs(); // we never store the results, because prices are dynamic
	}
}
