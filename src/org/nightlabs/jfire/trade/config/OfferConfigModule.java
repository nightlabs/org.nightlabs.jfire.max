package org.nightlabs.jfire.trade.config;

import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.trade.Offer;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 *  @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_OfferConfigModule"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class OfferConfigModule
		extends ConfigModule
{
	private static final long serialVersionUID = 1L;

	private long expiryDurationMSecUnfinalized;

	private long expiryDurationMSecFinalized;

	public OfferConfigModule() { }

	@Override
	@Implement
	public void init() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null!");

		expiryDurationMSecUnfinalized = 24L * 60 * 60 * 1000;
		expiryDurationMSecFinalized = 14L * 24 * 60 * 60 * 1000;
	}

	public long getExpiryDurationMSecFinalized() {
		return expiryDurationMSecFinalized;
	}
	public void setExpiryDurationMSecFinalized(long expiryDurationMSecFinalized) {
		if (expiryDurationMSecFinalized < 0)
			throw new IllegalArgumentException("expiryDurationMSecFinalized < 0");

		this.expiryDurationMSecFinalized = expiryDurationMSecFinalized;
	}
	public long getExpiryDurationMSecUnfinalized() {
		return expiryDurationMSecUnfinalized;
	}
	public void setExpiryDurationMSecUnfinalized(long expiryDurationMSecUnfinalized) {
		if (expiryDurationMSecUnfinalized < 0)
			throw new IllegalArgumentException("expiryDurationMSecUnfinalized < 0");

		this.expiryDurationMSecUnfinalized = expiryDurationMSecUnfinalized;
	}

	public void setOfferExpiry(Offer offer)
	{
		if (offer.isFinalized()) // unmodifiable after finalization!
			return;

		if (offer.isExpiryTimestampFinalizedAutoManaged())
			offer.setExpiryTimestampFinalized(new Date(System.currentTimeMillis() + expiryDurationMSecFinalized));

		if (offer.isExpiryTimestampUnfinalizedAutoManaged())
			offer.setExpiryTimestampUnfinalized(new Date(System.currentTimeMillis() + expiryDurationMSecUnfinalized));
	}
}
