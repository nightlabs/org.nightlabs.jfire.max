package org.nightlabs.jfire.issue.history;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.history.id.IssueHistoryItemID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * The {@link IssueHistoryItem} class represents a history which recorded the change of each {@link Issue}.
 *
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 **/
@PersistenceCapable(
	objectIdClass=IssueHistoryItemID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueHistoryItem")
@FetchGroups({
	@FetchGroup(
		name=IssueHistoryItem.FETCH_GROUP_ISSUE,
		members=@Persistent(name="issue")
	),
	@FetchGroup(
		name=IssueHistoryItem.FETCH_GROUP_USER,
		members=@Persistent(name="user")
	),
	@FetchGroup(
			name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
			members=@Persistent(name="user")
	),
//	@FetchGroup(
//		name=IssueHistoryItem.FETCH_GROUP_ICON_16X16_DATA,
//		members=@Persistent(name="icon16x16Data")),
//	@FetchGroup(
//		name=IssueHistoryItem.FETCH_GROUP_HISTORY_ACTION,
//		members=@Persistent(name="issueHistoryAction")),
})
@Queries(
	@javax.jdo.annotations.Query(
		name=IssueHistoryItem.QUERY_ISSUE_HISTORYIDS_BY_ORGANISATION_ID_AND_ISSUE_ID,
		value="SELECT WHERE this.issueID == :issueID && this.organisationID == :organisationID")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class IssueHistoryItem implements Serializable {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IssueHistoryItem.class);

	public static final String FETCH_GROUP_ISSUE = "IssueHistoryItem.issue";
	public static final String FETCH_GROUP_USER = "IssueHistoryItem.user";

	public static final String QUERY_ISSUE_HISTORYIDS_BY_ORGANISATION_ID_AND_ISSUE_ID = "getIssueHistoryIDsByOrganisationIDAndIssueID";	// <-- FIXME See notes.



//	// --- 8< --- KaiExperiments: since 27.05.2009 ------------------
//	public static final String FETCH_GROUP_ICON_16X16_DATA = "IssueHistoryItem.icon16x16Data";
//	public static final String FETCH_GROUP_HISTORY_ACTION = "IssueHistoryItem.issueHistoryAction";
//	// ------ KaiExperiments ----- >8 -------------------------------


	/**
	 * This is the organisationID to which the issue history belongs. Within one organisation,
	 * all the issue histories have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long issueID;

	@PrimaryKey
	private long issueHistoryItemID;



	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User user;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Issue issue;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createTimestamp;

//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	@Column(sqlType="CLOB")
//	private String description; // TODO Revise this field.
//
//
//
//	// --- 8< --- KaiExperiments: since 27.05.2009 ------------------
//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	@Column(sqlType="BLOB")
//	private byte[] icon16x16Data;
//
//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	private IssueHistoryAction issueHistoryAction;	// <-- May need to revise this naming convention...
//	// ------ KaiExperiments ----- >8 -------------------------------




	/**
	 * @deprecated Constructor exists only for JDO!
	 */
	@Deprecated
	protected IssueHistoryItem() { }

	/**
	 * Creates a new instance of an IssueHistoryItem.
	 */
	public IssueHistoryItem(String organisationID, User user, Issue oldIssue, Issue newIssue, long issueHistoryItemID) {
		Organisation.assertValidOrganisationID(organisationID);
		if (oldIssue == null)
			throw new NullPointerException("newIssue");

		this.organisationID = organisationID;
		this.issueID = oldIssue.getIssueID();

		this.issue = oldIssue;
		this.issueHistoryItemID = issueHistoryItemID;

		this.createTimestamp = new Date();
		this.user = user;


//		generateHistory(oldIssue, newIssue); // <-- FIXME Marked for removal. See notes 25 May 2009. Kai.
	}

	/**
	 * Creates a new instance of an IssueHistoryItem.
	 * The dummy constructor for testings...
	 */
	public IssueHistoryItem(boolean dummy, User user, Issue oldPersistentIssue, Issue newDetachedIssue) {
		this(IDGenerator.getOrganisationID(), user, oldPersistentIssue, newDetachedIssue, IDGenerator.nextID(IssueHistoryItem.class));
	}


	/**
	 * Gets the organisation id.
	 * @return the organisation id.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Gets the issue id.
	 * @return the issue id.
	 */
	public long getIssueID() {
		return issueID;
	}

	/**
	 * Gets the issue history id.
	 * @return
	 */
	public long getIssueHistoryItemID() {
		return issueHistoryItemID;
	}

	/**
	 * Gets the {@link Issue}.
	 * @return the {@link Issue}.
	 */
	public Issue getIssue(){
		return issue;
	}

	/**
	 * Gets the created time's {@link Date}.
	 * @return the created time's {@link Date}.
	 */
	public Date getCreateTimestamp() {
		return createTimestamp;
	}

//	/**
//	 * Gets the string of change.
//	 * @return the string of change.
//	 */
//	public String getChange() {
//		return change;
//	}

	/**
	 * Gets the {@link User}.
	 * @return the {@link User}.
	 */
	public User getUser() {
		return user;
	}
	/**
	 * @param pm The <code>PersistenceManager</code> that should be used to access the datastore.
	 * @param issue
	 * @return Returns instances of <code>IssueHistoryItem</code>.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<IssueHistoryItem> getIssueHistoryItemsByIssue(PersistenceManager pm, IssueID issueID)
	{
		Query q = pm.newNamedQuery(IssueHistoryItem.class, IssueHistoryItem.QUERY_ISSUE_HISTORYIDS_BY_ORGANISATION_ID_AND_ISSUE_ID);

		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("issueID", issueID.issueID);
		params.put("organisationID", issueID.organisationID);

		return (Collection<IssueHistoryItem>)q.executeWithMap(params);
	}







	// -------------------------------------------------------------------------------------------------------------------------|
	/**
	 * This is to be generated on the fly, and as such, makes it more flexible and easier to control when we desire
	 * any special 'textual' condition(s) for displaying the information related to the history of an Issue.
	 * @return the text description of the change history pertaining to the corresponding IssueHistoryItem.
	 */
//	public String getDescription() { return description; } // <-- [Make abstract?]
	public abstract String getDescription();

	/**
	 * This method can be used to assist in displaying the icon information of an IssueHistoryItem appearing on the
	 * description cell in the {@link IssueHistoryTable}, in addition to the method {@link IssueHistoryItem.#getDescription()}.
	 * @return the 16x16 icon image of the corresponding IssueHistoryItem.
	 */
//	public byte[] getIcon16x16Data() { return icon16x16Data; } // <-- [Make abstract?]
	public abstract byte[] getIcon16x16Data();



//	// -------------------------------------------------------------------------------------------------------------------------|
//	/**
//	 * Sets the description text for this {@link IssueHistoryItem}.
//	 */
//	public void setDescription(String description) { this.description = description; }
//
//	/**
//	 * @param icon16x16Data the icon to set for this {@link IssueHistoryItem}.
//	 */
//	public void setIcon16x16Data(byte[] icon16x16Data) { this.icon16x16Data = icon16x16Data; }
//
//	/**
//	 * @return the 'action' affected for this corresponding {@link IssueHistoryItem}.
//	 */
//	public IssueHistoryAction getIssueHistoryAction() { return issueHistoryAction; }
//
//	/**
//	 * @param issueHistoryAction sets the 'action' affected for this corresponding {@link IssueHistoryItem}.
//	 */
//	public void setIssueHistoryAction(IssueHistoryAction issueHistoryAction) { this.issueHistoryAction = issueHistoryAction; }




//	// -------------------------------------------------------------------------------------------------------------------------|
//	// TODO Rewrite in favour of utilizing the IssueHistoryItemFactory. See notes 15 May 2009. Kai.
//	@Deprecated
//	private void generateHistory(Issue oldIssue, Issue newIssue)
//	{
//		StringBuffer changeText = new StringBuffer();
//
//		if (!Util.equals(oldIssue.getDescription().getText(), newIssue.getDescription().getText()))
//		{
//			changeText.append("Changed description");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getDescription().getText());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getDescription().getText());
//			changeText.append("\n");
//
//		}
//
//		if (!Util.equals(oldIssue.getSubject().getText(), newIssue.getSubject().getText()))
//		{
//			changeText.append("Changed subject");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getSubject().getText());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getSubject().getText());
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.getComments(), newIssue.getComments()))
//		{
//			changeText.append("Add Comment(s)");
////			changeText.append(" from ");
////			changeText.append();
////			changeText.append(" ---> ");
////			changeText.append();
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.getAssignee(), newIssue.getAssignee()))
//		{
//			changeText.append("Changed assignee");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getAssignee() == null? " - " : oldIssue.getAssignee().getName());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getAssignee() == null? " - " : newIssue.getAssignee().getName());
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.getReporter(), newIssue.getReporter()))
//		{
//			changeText.append("Changed reporter");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getReporter().getName());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getReporter().getName());
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.getIssueFileAttachments().size(), newIssue.getIssueFileAttachments().size()))
//		{
//			changeText.append("Changed file attachments");
////			changeText.append(" from ");
////			changeText.append();
////			changeText.append(" ---> ");
////			changeText.append();
//			changeText.append("\n");
//		}
//
////		if (!Util.equals(oldIssue.getIssueLinks(), newIssue.getIssueLinks()))
////		{
////			changeText.append("Changed issue links");
//////			changeText.append(" from ");
//////			changeText.append();
//////			changeText.append(" ---> ");
//////			changeText.append();
////			changeText.append("\n");
////		}
//
//
//		// --- 8< --- KaiExperiments: since 14.05.2009 ------------------
//		if (!Util.equals(oldIssue.getIssueMarkers().size(), newIssue.getIssueMarkers().size())) {
//			int oldSize = oldIssue.getIssueMarkers().size();
//			int newSize = newIssue.getIssueMarkers().size();
//
//			if (oldSize < newSize)	changeText.append("A new issue marker was added.");
//			if (oldSize > newSize)	changeText.append("An old issue marker was deleted.");
//		}
//		// ------ KaiExperiments ----- >8 -------------------------------
//
//
//		if (!Util.equals(oldIssue.getIssuePriority(), newIssue.getIssuePriority()))
//		{
//			changeText.append("Changed priority");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getIssuePriority().getIssuePriorityText().getText());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getIssuePriority().getIssuePriorityText().getText());
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.getIssueSeverityType(), newIssue.getIssueSeverityType()))
//		{
//			changeText.append("Changed severity type");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getIssueSeverityType().getIssueSeverityTypeText().getText());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getIssueSeverityType().getIssueSeverityTypeText().getText());
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.getIssueResolution(), newIssue.getIssueResolution()))
//		{
//			changeText.append("Changed resolution");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getIssueResolution() == null?"None":oldIssue.getIssueResolution().getName().getText());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getIssueResolution().getName().getText());
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.getIssueType(), newIssue.getIssueType()))
//		{
//			changeText.append("Changed issue type");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getIssueType().getName().getText());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getIssueType().getName().getText());
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.getState().getStateDefinition(), newIssue.getState().getStateDefinition()))
//		{
//			changeText.append("Changed state");
//			changeText.append(" from ");
//			changeText.append(oldIssue.getState().getStateDefinition().getName().getText());
//			changeText.append(" ---> ");
//			changeText.append(newIssue.getState().getStateDefinition().getName().getText());
//			changeText.append("\n");
//		}
//
//		if (!Util.equals(oldIssue.isStarted(), newIssue.isStarted()))
//		{
//			changeText.append("Changed status");
//			changeText.append(" from ");
//			changeText.append(oldIssue.isStarted() ? "Working" : "Stopped");
//			changeText.append(" ---> ");
//			changeText.append(newIssue.isStarted() ? "Working" : "Stopped");
//		}
//
//		this.description = changeText.toString();
//	}

	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object.
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager issueHistoryItemPM = JDOHelper.getPersistenceManager(this);
		if (issueHistoryItemPM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return issueHistoryItemPM;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueHistoryItem)) return false;
		IssueHistoryItem o = (IssueHistoryItem) obj;
		return
			Util.equals(this.organisationID, o.organisationID) &&
			Util.equals(this.issueID, o.issueID) &&
			Util.equals(this.issueHistoryItemID, o.issueHistoryItemID);
	}

	@Override
	public int hashCode()
	{
		return
			(31 * Util.hashCode(organisationID)) +
			Util.hashCode(issueID) ^
			Util.hashCode(issueHistoryItemID);
	}
}