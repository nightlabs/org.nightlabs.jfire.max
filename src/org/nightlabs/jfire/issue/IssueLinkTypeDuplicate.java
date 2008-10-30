package org.nightlabs.jfire.issue;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.issue.IssueLinkTypeIssueToIssue"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */ 
public class IssueLinkTypeDuplicate
extends IssueLinkTypeIssueToIssue
{
	private static final long serialVersionUID = 1L;

	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_DUPLICATE = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "duplicate");

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueLinkTypeDuplicate() { }

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
