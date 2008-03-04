package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.store.ReceptionNote;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class ReceptionNoteQuickSearchQuery
	extends AbstractArticleContainerQuickSearchQuery<ReceptionNote>
{
	private static final long serialVersionUID = 1L;

//	@Override
//	public Class getArticleContainerClass() {
//		return ReceptionNote.class;
//	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "receptionNoteID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "receptionNoteIDPrefix";
	}

	@Override
	protected Class<ReceptionNote> init()
	{
		return ReceptionNote.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuffer filter)
	{
		// no additional fields needed yet 
	}
}
