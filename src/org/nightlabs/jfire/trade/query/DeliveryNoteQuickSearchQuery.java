/**
 * 
 */
package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.store.DeliveryNote;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class DeliveryNoteQuickSearchQuery 
extends AbstractArticleContainerQuickSearchQuery 
{	
	@Override
	public Class getArticleContainerClass() {
		return DeliveryNote.class;
	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "deliveryNoteID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "deliveryNoteIDPrefix";
	}
}
