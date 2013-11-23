package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;
import org.nightlabs.jfire.trade.recurring.RecurringOffer;

/**
 * @author Fitas Amine - fitas <at> nightlabs <dot> de
 */
public class RecurredOfferQuery extends AbstractArticleContainerQuery {

	private static final long serialVersionUID = 1L;
	
	private OfferID recurringOfferID;
	private RecurringOffer recurringOffer = null;

	@Override
	protected Class<RecurredOffer> initCandidateClass()
	{
		return RecurredOffer.class;
	}


	@Override
	protected void checkAdditionalFields(StringBuilder filter) {
		if (recurringOfferID != null) {
			//		filter.append("\n && JDOHelper.getObjectId(this.price.currency) == :currencyID");
			recurringOffer = (RecurringOffer) getPersistenceManager().getObjectById(recurringOfferID);
			filter.append("\n && this.recurringOffer == :recurringOffer");
		}	
	}


	@Override
	public String getArticleContainerIDMemberName() {
		return "offerID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "offerIDPrefix";
	}

	public OfferID getRecurringOfferID()
	{
		return recurringOfferID;
	}

	public void setRecurringOfferID(OfferID offerID)
	{
		this.recurringOfferID = offerID;
	}
}