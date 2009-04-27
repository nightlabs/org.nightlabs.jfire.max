package org.nightlabs.jfire.issue;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.organisation.Organisation;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 *  This extended class of {@link IssueLinkTypeIssueToIssue} used for creating duplicated relation between {@link Issue}s.
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.issue.IssueLinkTypeIssueToIssue"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)

public class IssueLinkTypeDuplicate
extends IssueLinkTypeIssueToIssue
{
	private static final long serialVersionUID = 1L;

	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_DUPLICATE = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "duplicate");

	/**
	 * @deprecated Only for JDO!
	 */
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

	@Override
	public IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkTypeID newIssueLinkTypeID) {
		return this;
	}
}
