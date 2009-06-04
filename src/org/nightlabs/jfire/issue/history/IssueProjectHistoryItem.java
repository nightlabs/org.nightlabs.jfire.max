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
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link Project}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueProjectHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="oldProject"), @Persistent(name="newProject")}
	)
})
public class IssueProjectHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Project oldProject;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Project newProject;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueProjectHistoryItem() {}

	/**
	 * Creates a new instance of an IssueProjectHistoryItem.
	 */
	public IssueProjectHistoryItem(User user, Issue issue, Project oldProject, Project newProject) {
		super(true, user, issue);
		this.oldProject = oldProject;
		this.newProject = newProject;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return String.format("MODIFIED: Project from \"%s\" to \"%s\".", oldProject.getName().getText(), newProject.getName().getText());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}
}
