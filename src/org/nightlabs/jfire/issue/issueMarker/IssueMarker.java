package org.nightlabs.jfire.issue.issueMarker;

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
import org.nightlabs.jfire.issue.id.IssueMarkerID;
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
			members=@Persistent(name="name") ),
	@FetchGroup(
			name=IssueMarker.FETCH_GROUP_DESCRIPTION,
			members=@Persistent(name="description") ),
	@FetchGroup(
			name=IssueMarker.FETCH_GROUP_ICON_16X16_DATA,
			members=@Persistent(name="icon16x16Data") )
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

	@Persistent(mappedBy="issueMarker", persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueMarkerName name;

	@Persistent(mappedBy="issueMarker", persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueMarkerDescription description;


	/**
	 * @deprecated Constructor exists only for JDO!
	 */
	@Deprecated
	protected IssueMarker() {}


	public IssueMarker(boolean dummy)
	{
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(IssueMarker.class));
	}

	/**
	 * Creates a new instance of an IssueMarker.
	 */
	public IssueMarker(String organisationID, long issueMarkerID) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.issueMarkerID = issueMarkerID;

		name = new IssueMarkerName(this);
		description = new IssueMarkerDescription(this);
	}



	// :: --- [ ICONs ] ------------------------------------------------------------------------------------|
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */
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
	public long getID()                            { return issueMarkerID; }

	/**
	 * @return the name of the IssueMarker.
	 */
	public IssueMarkerName getName()               { return name; }

	/**
	 * @return the description texts of the IssueMarker.
	 */
	public IssueMarkerDescription getDescription() { return description; }
}
