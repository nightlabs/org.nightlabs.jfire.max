package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.store.DeliveryNote;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class DeliveryNoteQuery
	extends AbstractArticleContainerQuery
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getArticleContainerIDMemberName() {
		return "deliveryNoteID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "deliveryNoteIDPrefix";
	}

	@Override
	protected Class<DeliveryNote> initCandidateClass()
	{
		return DeliveryNote.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuffer filter)
	{
		// no additional fields needed yet 
	}
}
