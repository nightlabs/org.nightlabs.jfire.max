package org.nightlabs.jfire.trade;

/**
 * A value of this enum is used in some methods to specify, what side the local organisation
 * (referenced by its legal entity)
 * is in a trade transaction. There are 2 principal possibilities: It can perform a purchase and thus
 * be the customer or it can perform a sale transaction and thus be the vendor.
 * <p>
 * When the local organisation is the customer, there are again 2 possibilities:
 * <ul><li>The vendor is another JFire organisation and therefore the <code>ArticleContainer</code>s are created by it and arrive
 * at the customer (i.e. here) being already in state "sent".</li>
 * <li>The vendor is a locally managed {@link LegalEntity} (and <b>not</b> an {@link OrganisationLegalEntity}).</li>
 * </ul>
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public enum TradeSide
{
	/**
	 * The local organisation performs a sale transaction (and thus is the vendor).
	 */
	vendor,

	/**
	 * The local organisation takes part as the customer at a cross-organisation purchase transaction.
	 */
	customerCrossOrganisation,

	/**
	 * The local organisation performs a purchase transaction with a locally managed vendor (not another JFire organisation).
	 */
	customerLocal
}
