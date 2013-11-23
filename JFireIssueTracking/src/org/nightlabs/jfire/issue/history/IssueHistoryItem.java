package org.nightlabs.jfire.issue.history;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.history.id.IssueHistoryItemID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	)
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
	private static final Logger logger = LoggerFactory.getLogger(IssueHistoryItem.class);

	public static final String FETCH_GROUP_ISSUE = "IssueHistoryItem.issue";
	public static final String FETCH_GROUP_USER = "IssueHistoryItem.user";

	public static final String QUERY_ISSUE_HISTORYIDS_BY_ORGANISATION_ID_AND_ISSUE_ID = "getIssueHistoryIDsByOrganisationIDAndIssueID";	// <-- FIXME See notes.


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

	/**
	 * @deprecated Constructor exists only for JDO!
	 */
	@Deprecated
	protected IssueHistoryItem() { }

	/**
	 * Creates a new instance of an IssueHistoryItem.
	 */
	public IssueHistoryItem(String organisationID, User user, Issue oldPersistentIssue, long issueHistoryItemID) {
		Organisation.assertValidOrganisationID(organisationID);
		if (oldPersistentIssue == null)
			throw new NullPointerException("newIssue");

		this.organisationID = organisationID;
		this.issueID = oldPersistentIssue.getIssueID();

		this.issue = oldPersistentIssue;
		this.issueHistoryItemID = issueHistoryItemID;

		this.createTimestamp = new Date();
		this.user = user;
	}

	/**
	 * Creates a new instance of an IssueHistoryItem.
	 * The dummy constructor for testings...
	 */
	public IssueHistoryItem(boolean dummy, User user, Issue oldPersistentIssue) {
		this(IDGenerator.getOrganisationID(), user, oldPersistentIssue, IDGenerator.nextID(IssueHistoryItem.class));
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
	public static Collection<IssueHistoryItem> getIssueHistoryItemsByIssue(PersistenceManager pm, IssueID issueID) {
		Query q = pm.newNamedQuery(IssueHistoryItem.class, IssueHistoryItem.QUERY_ISSUE_HISTORYIDS_BY_ORGANISATION_ID_AND_ISSUE_ID);

		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("issueID", issueID.issueID);
		params.put("organisationID", issueID.organisationID);

		@SuppressWarnings("unchecked")
		Collection<IssueHistoryItem> c = (Collection<IssueHistoryItem>)q.executeWithMap(params);
		return c;
	}

	// -------------------------------------------------------------------------------------------------------------------------|
	/**
	 * This is to be generated on the fly, and as such, makes it more flexible and easier to control when we desire
	 * any special 'textual' condition(s) for displaying the information related to the history of an Issue.
	 * @return the text description of the change history pertaining to the corresponding IssueHistoryItem.
	 */
	public abstract String getDescription();

	/**
	 * This method can be used to assist in displaying the icon information of an IssueHistoryItem appearing on the
	 * description cell in the {@link IssueHistoryTable}, in addition to the method {@link IssueHistoryItem.#getDescription()}.
	 * @return the 16x16 icon image of the corresponding IssueHistoryItem.
	 */
	public abstract byte[] getIcon16x16Data();


//	/**
//	 * Internal method.
//	 * @return The PersistenceManager associated with this object.
//	 */
//	protected PersistenceManager getPersistenceManager() {
//		PersistenceManager issueHistoryItemPM = JDOHelper.getPersistenceManager(this);
//		if (issueHistoryItemPM == null)
//			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");
//
//		return issueHistoryItemPM;
//	}

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