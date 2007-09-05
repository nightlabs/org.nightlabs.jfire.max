/**
 * 
 */
package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.store.ReceptionNote;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class ReceptionNoteQuickSearchQuery 
extends AbstractArticleContainerQuickSearchQuery 
{
	@Override
	public Class getArticleContainerClass() {
		return ReceptionNote.class;
	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "receptionNoteID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "receptionNoteIDPrefix";
	}
}
