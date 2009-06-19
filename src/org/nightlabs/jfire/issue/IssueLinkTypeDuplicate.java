package org.nightlabs.jfire.issue;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * This extended class of {@link IssueLinkTypeIssueToIssue} used for creating the duplicated relation between {@link Issue}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class IssueLinkTypeDuplicate extends IssueLinkTypeIssueToIssue {
	private static final long serialVersionUID = 1L;

	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_DUPLICATE = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "duplicate");

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueLinkTypeDuplicate() { }

	/**
	 *
	 * @param issueLinkTypeID
	 */
	public IssueLinkTypeDuplicate(IssueLinkTypeID issueLinkTypeID) {
		super(issueLinkTypeID);
		if (!ISSUE_LINK_TYPE_ID_DUPLICATE.equals(issueLinkTypeID))
			throw new IllegalArgumentException("Illegal issueLinkTypeID! Only ISSUE_LINK_TYPE_ID_DUPLICATE is allowed! " + issueLinkTypeID);
	}

//	@Override
//	public IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkTypeID newIssueLinkTypeID) {
//		return this;
//	}
}
