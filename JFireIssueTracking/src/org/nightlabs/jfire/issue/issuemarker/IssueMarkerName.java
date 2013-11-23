package org.nightlabs.jfire.issue.issuemarker;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.issuemarker.id.IssueMarkerNameID;

/**
 * The name identifier for an {@link IssueMarker}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=IssueMarkerNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueMarkerName"
)
@FetchGroups({
	@FetchGroup(
		name="IssueMarker.name",
		members={@Persistent(name="issueMarker"), @Persistent(name="names")}
	),
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="issueMarker"), @Persistent(name="names")}
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueMarkerName extends I18nText {
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long issueMarkerID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueMarker issueMarker;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueMarkerName() {}

	public IssueMarkerName(IssueMarker issueMarker) {
		this.organisationID = issueMarker.getOrganisationID();
		this.issueMarkerID = issueMarker.getIssueMarkerID();
		this.issueMarker = issueMarker;
		names = new HashMap<String, String>();
	}

	/**
	 * key: String languageID<br/>
	 * value: String name
	 */
	@Join
	@Persistent(
		table="JFireIssueTracking_IssueMarkerName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	private Map<String, String> names;

	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() { return names; }

	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) { return ObjectIDUtil.longObjectIDFieldToString(issueMarkerID); }

	/**
	 * @return the organisation ID attached to the original IssueMarker.
	 */
	public String getOrganisationID()   { return organisationID; }

	/**
	 * @return the ID of the original IssueMarker.
	 */
	public long getIssueMarkerID()      { return issueMarkerID; }

	/**
	 * @return the IssueMarker referred to by this IssueMarkerName.
	 */
	public IssueMarker getIssueMarker() { return issueMarker; }
}
