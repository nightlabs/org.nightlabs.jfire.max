package org.nightlabs.jfire.trade.jbpm;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Trader;

public class ActionHandlerOfferProcessEnd
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

//	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
//	{
//		Action action = new Action(new Delegation(ActionHandlerOfferProcessEnd.class.getName()));
//		action.setName(ActionHandlerOfferProcessEnd.class.getName());
//
//		Event event = new Event("node-enter");
//		event.addAction(action);
//
//		jbpmProcessDefinition.addEvent(event)
//
//		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED);
//		if (finalized == null)
//			throw new IllegalArgumentException("The node \""+ JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");
//
//		finalized.addEvent(event);
//	}

	@Override
	@Implement
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Offer offer = (Offer) getStatable();

		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		// if it was a failure, we release normal articles and unregister reversing articles
		if (!JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED.equals(executionContext.getToken().getNode().getName())) {
			Set<Article> articlesToBeReleased = null;
			Set<Article> reversingArticlesToBeUnregistered = null;
			for (Article article : offer.getArticles()) {
				if (article.isReversing()) {
					if (reversingArticlesToBeUnregistered == null)
						reversingArticlesToBeUnregistered = new HashSet<Article>(offer.getArticles().size());

					reversingArticlesToBeUnregistered.add(article);
				}
				else {
					if (articlesToBeReleased == null)
						articlesToBeReleased = new HashSet<Article>(offer.getArticles().size());

					articlesToBeReleased.add(article);
				}
			}

			Trader trader = Trader.getTrader(pm);

			if (reversingArticlesToBeUnregistered != null && !reversingArticlesToBeUnregistered.isEmpty())
				trader.unregisterReversingArticles(user, reversingArticlesToBeUnregistered);

			if (articlesToBeReleased != null && !articlesToBeReleased.isEmpty())
				trader.releaseArticles(user, articlesToBeReleased, false, false, true);
		}
	}
}
