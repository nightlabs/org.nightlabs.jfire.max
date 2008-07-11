package org.nightlabs.jfire.store.reverse;

import org.nightlabs.jfire.trade.id.OfferID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class OfferNotAcceptedReverseProductError 
extends AbstractReverseProductError 
{
	private OfferID offerID;
	
	public OfferNotAcceptedReverseProductError() {
	}

	/**
	 * @param description
	 */
	public OfferNotAcceptedReverseProductError(String description) {
		super(description);
	}

	/**
	 * Returns the offerID.
	 * @return the offerID
	 */
	public OfferID getOfferID() {
		return offerID;
	}

	/**
	 * Sets the offerID.
	 * @param offerID the offerID to set
	 */
	public void setOfferID(OfferID offerID) {
		this.offerID = offerID;
	}

}
