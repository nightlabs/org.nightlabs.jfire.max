package org.nightlabs.jfire.issue.history;

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueStateDefinitionHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueStateDefinitionHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// StateDefinition is modified from a drop-down combo, and will always be assigned a value.
		// Also: (i) No one can should be able to modify the 'name' field of a StateDefinition.
		//       (ii) There exists only a finite number of StateDefinitions.
		StateDefinition oldStateDefinition = oldPersistentIssue.getState().getStateDefinition();
		StateDefinition newStateDefinition = newDetachedIssue.getState().getStateDefinition();

		// So, we only to check whether the StateDefinitions have changed or not.
		if ( !oldStateDefinition.equals(newStateDefinition) )
			return IssueHistoryItemFactory.makeItemIntoCollection( new IssueStateDefinitionHistoryItem(user, oldPersistentIssue, oldStateDefinition, newStateDefinition) );

		return null;
	}

}
