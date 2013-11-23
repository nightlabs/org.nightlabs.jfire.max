package org.nightlabs.jfire.issue.issuemarker;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.issuemarker.id.IssueMarkerID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * An {@link IssueMarker} is used to mark a (special/important/dire) part (or portion/section/stage) of an {@link ExtendedIssue},
 * which can later be collated/assembled in a summary-view to give a quick (useful, tooltip?) information about it, that when
 * clicked upon, shall bring the user directly to the part (or portion/section/stage) of the {@link ExtendedIssue} that the
 * {@link IssueMarker} represents.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=IssueMarkerID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueMarker"
)
@FetchGroups({
	@FetchGroup(
			name=IssueMarker.FETCH_GROUP_NAME,
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name=IssueMarker.FETCH_GROUP_DESCRIPTION,
			members=@Persistent(name="description")
	),
	@FetchGroup(
			name=IssueMarker.FETCH_GROUP_ICON_16X16_DATA,
			members=@Persistent(name="icon16x16Data")
	),
	@FetchGroup(
			name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
			members={@Persistent(name="name"), @Persistent(name="icon16x16Data")}
	)
})
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueMarker implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "IssueMarker.name";
	public static final String FETCH_GROUP_DESCRIPTION = "IssueMarker.description";
	public static final String FETCH_GROUP_ICON_16X16_DATA = "IssueMarker.icon16x16Data";

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long issueMarkerID;

	@Persistent(mappedBy="issueMarker", dependent="true")
	private IssueMarkerName name;

	@Persistent(mappedBy="issueMarker", dependent="true")
	private IssueMarkerDescription description;


	/**
	 * @deprecated Constructor exists only for JDO!
	 */
	@Deprecated
	protected IssueMarker() {}


	public IssueMarker(IssueMarkerID issueMarkerID)
	{
		if (issueMarkerID == null) {
			issueMarkerID = IssueMarkerID.create(IDGenerator.getOrganisationID(), IDGenerator.nextID(IssueMarker.class));
		}
		Organisation.assertValidOrganisationID(issueMarkerID.organisationID);
		this.organisationID = issueMarkerID.organisationID;
		this.issueMarkerID = issueMarkerID.issueMarkerID;

		name = new IssueMarkerName(this);
		description = new IssueMarkerDescription(this);
	}



	// :: --- [ ICONs ] ------------------------------------------------------------------------------------|
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] icon16x16Data;

	/**
	 * @return the 16x16 icon of this IssueMarker.
	 */
	public byte[] getIcon16x16Data()                   { return icon16x16Data; }

	/**
	 * @param icon16x16Data the icon to set for this IssueMarker.
	 */
	public void setIcon16x16Data(byte[] icon16x16Data) { this.icon16x16Data = icon16x16Data; }


	// :: --- [ Miscellaneous necessities ] ----------------------------------------------------------------|
	/**
	 * @return the organisation ID attached to this IssueMarker.
	 */
	public String getOrganisationID()              { return organisationID; }

	/**
	 * @return the ID of this IssueMarker.
	 */
	public long getIssueMarkerID()                 { return issueMarkerID; }

	/**
	 * @return the name of the IssueMarker.
	 */
	public IssueMarkerName getName()               { return name; }

	/**
	 * @return the description texts of the IssueMarker.
	 */
	public IssueMarkerDescription getDescription() { return description; }


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (issueMarkerID ^ (issueMarkerID >>> 32));
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IssueMarker other = (IssueMarker) obj;
		if (issueMarkerID != other.issueMarkerID)
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.String#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + issueMarkerID + ']';
	}
}
