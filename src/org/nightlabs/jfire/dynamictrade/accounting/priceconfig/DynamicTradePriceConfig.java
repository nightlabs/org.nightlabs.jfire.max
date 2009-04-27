package org.nightlabs.jfire.dynamictrade.accounting.priceconfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicTradePriceConfig"
 *
 * @!jdo.inheritance strategy="superclass-table" @!TODO JPOX WORKAROUND: Using superclass-table here causes weird errors - see: http://www.jpox.org/servlet/forum/viewthread?thread=4874
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="DynamicTradePriceConfig.inputPriceFragmentTypes" fields="inputPriceFragmentTypes"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="inputPriceFragmentTypes"
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
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="PriceFragmentType"
	 *		table="JFireDynamicTrade_DynamicTradePriceConfig_inputPriceFragmentTypes"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireDynamicTrade_DynamicTradePriceConfig_inputPriceFragmentTypes",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<PriceFragmentType> inputPriceFragmentTypes;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Set<PriceFragmentType> inputPriceFragmentTypes_readonly = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicTradePriceConfig() { }

	public DynamicTradePriceConfig(String organisationID, String priceConfigID)
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
		} catch (JDODetachedFieldAccessException x) {
			// silently ignore it - the field packagingResultPriceConfigs is not detached => no problem.
		}
	}

	@Override
	public void jdoPostAttach(Object detached) {
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
