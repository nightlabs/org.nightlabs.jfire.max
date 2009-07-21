package org.nightlabs.jfire.store.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.Article;

public class ActionHandlerDeliveryNoteProcessEnd
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		// if it was a failure/abortion, we clear the backreference of the articles to their current invoice.
		if (!JbpmConstantsDeliveryNote.Both.NODE_NAME_DELIVERED.equals(executionContext.getToken().getNode().getName())) {
			DeliveryNote deliveryNote = (DeliveryNote) getStatable();
			for (Article article : deliveryNote.getArticles()) {
				article.setDeliveryNote(null);
			}
		}
	}
}
