package org.nightlabs.jfire.issue.history;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link StateDefinition}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueStateDefinitionHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="oldStateDefinition"), @Persistent(name="newStateDefinition")}
	)
})
public class IssueStateDefinitionHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private StateDefinition oldStateDefinition;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private StateDefinition newStateDefinition;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueStateDefinitionHistoryItem() {}

	/**
	 * Creates a new instance of an IssueStateDefinitionHistoryItem.
	 */
	public IssueStateDefinitionHistoryItem(User user, Issue issue, StateDefinition oldStateDefinition, StateDefinition newStateDefinition) {
		super(true, user, issue);
		this.oldStateDefinition = oldStateDefinition;
		this.newStateDefinition = newStateDefinition;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return String.format("MODIFIED: State definition from \"%s\" to \"%s\".", oldStateDefinition.getName().getText(), newStateDefinition.getName().getText());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}
}
