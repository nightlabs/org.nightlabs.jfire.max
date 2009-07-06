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
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.IssueLinkType;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueLink}s.
 *
 * An {@link IssueLink} is either ADDED or REMOVED in a history action, or to be candidly correct,
 * a link is either CREATED or SEVERED, respectively.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueLinkHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="issueLinkType")}
	)
})
public class IssueLinkHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueLinkType issueLinkType;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueHistoryItemAction issueHistoryItemAction;



	@Persistent(nullValue=NullValue.EXCEPTION)    // }
	private String linkedObjectClassName;         // } <-- TODO Needs to be improved, so that we can also display the 'human-recognisable' linked object.
                                                  // }
	@Persistent(nullValue=NullValue.EXCEPTION)    // }
	private String linkedObjectID;                // }



	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueLinkHistoryItem() {}

	/**
	 * Creates a new instance of an IssueLinkHistoryItem.
	 */
	public IssueLinkHistoryItem
	(User user, Issue issue, IssueLinkType issueLinkType, IssueHistoryItemAction issueHistoryItemAction, String linkedObjectClassName, String linkedObjectID) {
		super(true, user, issue);
		this.issueLinkType = issueLinkType;
		this.issueHistoryItemAction = issueHistoryItemAction;

		// --- 8< --- KaiExperiments: since 08.06.2009 ------------------
		this.linkedObjectClassName = linkedObjectClassName;
		this.linkedObjectID = linkedObjectID;
		// ------ KaiExperiments ----- >8 -------------------------------
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		String linkedObjInfoTxt = linkedObjectClassName + " [ID: " + linkedObjectID + "]";
		switch (issueHistoryItemAction) {
			case ADDED:
				return String.format("Issue link CREATED: This Issue is now \"%s\" to \"%s\".", issueLinkType.getName().getText(), linkedObjInfoTxt);
			case REMOVED:
				return String.format("Issue link SEVERED: This Issue is no longer \"%s\" to \"%s\".", issueLinkType.getName().getText(), linkedObjInfoTxt);
			default:
				throw new IllegalStateException("Unknown issueHistoryItemAction: " + issueHistoryItemAction);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}

}
