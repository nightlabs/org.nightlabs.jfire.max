package org.nightlabs.jfire.trade;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.DeliveryNote;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 *  @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_TradeConfigModule"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class TradeConfigModule
		extends ConfigModule
{
	/**
	 * Which currency is used by the user group / user.
	 * TODO Maybe we should store the Currency per Organisation, because usually all accounting activities are
	 * performed in ONE currency?!
	 * IMHO we need one Currency per Organisation specifying which currency is used for the accounting and then
	 * we still need somehow different currencies for the users and even maybe the possibility to change the currency
	 * while being in a sale process.
	 */
	private Currency currency;

	/**
	 * key: String articleContainerClassName <br/>
	 * value: IDPrefixCf
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="IDPrefixCf"
	 *		table="JFireTrade_TradeConfigModule_idPrefixCfs"
	 *		@!mapped-by="tradeConfigModule" // not possible because of inheritance - we don't want to copy IDPrefixCfs!
	 *
	 * @!jdo.key mapped-by="articleContainerClassName"
	 * @jdo.join
	 */
	private Map idPrefixCfs = null;

	public TradeConfigModule() { }

	@Implement
	public void init() {
		idPrefixCfs = new HashMap();
	}

	public Currency getCurrency()
	{
		return currency;
	}
	public void setCurrency(Currency currency)
	{
		this.currency = currency;
	}

	/**
	 * Creates only if necessary - if it already exists, the previously created IDPrefixCf will be returned.
	 *
	 * @param articleContainerClassName The class name of the articleContainer for which to fetch the
	 *		settings or {@link IDPrefixCf#ARTICLE_CONTAINER_CLASS_NAME_GLOBAL } for the global ones (if you don't want different settings for order/offer/invoice/deliveryNote).
	 * @return Returns the desired IDPrefixCf assigned to the given <code>articleContainerClassName</code>.
	 */
	public IDPrefixCf createIDPrefixCf(String articleContainerClassName)
	{
		IDPrefixCf res = getIDPrefixCf(articleContainerClassName, false);
		if (res == null) {
			res = new IDPrefixCf(
					IDGenerator.getOrganisationID(),
					IDGenerator.nextID(IDPrefixCf.class),
					this, articleContainerClassName);
			idPrefixCfs.put(res.getArticleContainerClassName(), res);
		}
		return res;
	}

	/**
	 * This method first looks, whether an IDPrefixCf exists for the given articleContainerClassName.
	 * If not, it will return the one for {@link IDPrefixCf#ARTICLE_CONTAINER_CLASS_NAME_GLOBAL } (which is
	 * created if necessary).
	 *
	 * @param articleContainerClassName
	 * @param throwExceptionIfNotFound
	 * @return Returns an instance of {@link IDPrefixCf} - never returns <code>null</code>.
	 */
	public IDPrefixCf getActiveIDPrefixCf(String articleContainerClassName)
	{
		IDPrefixCf cf = getIDPrefixCf(articleContainerClassName, false);
		if (cf == null) {
			cf = createIDPrefixCf(IDPrefixCf.ARTICLE_CONTAINER_CLASS_NAME_GLOBAL);
		}
		return cf;
	}

	public IDPrefixCf getIDPrefixCf(String articleContainerClassName, boolean throwExceptionIfNotFound)
	{
		if (!IDPrefixCf.ARTICLE_CONTAINER_CLASS_NAME_GLOBAL.equals(articleContainerClassName) &&
				!Order.class.getName().equals(articleContainerClassName) &&
				!Offer.class.getName().equals(articleContainerClassName) &&
				!Invoice.class.getName().equals(articleContainerClassName) &&
				!DeliveryNote.class.getName().equals(articleContainerClassName))
			throw new IllegalArgumentException("articleContainerClassName invalid!");

		IDPrefixCf res = (IDPrefixCf) idPrefixCfs.get(articleContainerClassName);
		if (res == null && throwExceptionIfNotFound)
			throw new IllegalStateException("No IDPrefixCf registered for articleContainerClassName=\"" + articleContainerClassName + "\"");

		return res;
	}
}
