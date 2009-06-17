package org.nightlabs.jfire.issue;

import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.history.IssueHistoryItemAction;
import org.nightlabs.jfire.issue.history.IssueLinkHistoryItem;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;

/**
 * This abstract class extended of {@link IssueLinkType} provides the process for creating the other way round of an {@link IssueLinkTypeIssueToIssue}
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public abstract class IssueLinkTypeIssueToIssue
extends IssueLinkType {
	private static final long serialVersionUID = 3707016637193680759L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueLinkTypeIssueToIssue() { }

	/**
	 *
	 * @param issueLinkTypeID
	 */
	public IssueLinkTypeIssueToIssue(IssueLinkTypeID issueLinkTypeID) {
		super(issueLinkTypeID);
	}

	public abstract IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkTypeID newIssueLinkTypeID);

	@Override
	protected void postCreateIssueLink(PersistenceManager pm, IssueLink issueLink_thisSide) {
		// Notations:
		//     ________________                                     _________________
		//    [                ]♦---- issueLinkType_thisSide ---->>[                 ]
		//    [ issue_thisSide ]                                   [ issue_otherSide ]
		//    [________________]<<--- issueLinkType_otherSide ----♦[_________________]

		// -- 1. [Create the reverse link]--------------------------------------------------------------------------------------|
		IssueLinkType issueLinkType_thisSide = issueLink_thisSide.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType_thisSide);
		if (issueLinkTypeID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(newIssueLink.getIssueLinkType()) returned null!");

		IssueLinkType issueLinkType_otherSide = getReverseIssueLinkType(pm, issueLinkTypeID);

		Issue issue_thisSide = issueLink_thisSide.getIssue();
		Issue issue_otherSide = (Issue)issueLink_thisSide.getLinkedObject();

		// Guard: Prevents this from causing an ETERNAL recursion.
		// => find out, if the other side already has an issue-link of the required type pointing back
		Set<IssueLink> issueLinksOfIssueOnOtherSide = issue_otherSide.getIssueLinks();
		for (IssueLink issueLinkOfIssueOnOtherSide : issueLinksOfIssueOnOtherSide) {
			if (issueLinkOfIssueOnOtherSide.getIssueLinkType().equals(issueLinkType_otherSide)
					&& issueLinkOfIssueOnOtherSide.getLinkedObject().equals(issue_thisSide)); // issueLink_thisSide.getIssue()))
				return;
		}

		issue_otherSide.createIssueLink(issueLinkType_otherSide, issue_thisSide);


		// -- 2. [Create the IssueLinkHistoryItems for issueOtherSide accordingly]----------------------------------------------|
		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		// Note: The IssueLinkHistory item for 'issueThisSide' would already have been handled in the normal routine.
		//       We do not do it here, because an Issue links to any arbitrary object; and this here, is a special case.
		final IssueLinkHistoryItem ilhItemOtherSide = new IssueLinkHistoryItem(
				user, issue_otherSide, issueLinkType_otherSide,
				IssueHistoryItemAction.ADDED,
				Issue.class.getName(),
				issue_thisSide.getIssueIDAsString()
		);

		// Assuming here that the PersistenceManager is still open && valid.
		NLJDOHelper.storeJDO(
				pm, ilhItemOtherSide, false,
				new String[] {FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
		);
	}

	@Override
	protected void preDeleteIssueLink(PersistenceManager pm, IssueLink issueLink_thisSide) {
		// Notations:
		//     ________________                                     _________________
		//    [                ]♦---- issueLinkType_thisSide ---->>[                 ]
		//    [ issue_thisSide ]                                   [ issue_otherSide ]
		//    [________________]<<--- issueLinkType_otherSide ----♦[_________________]

		// From previous references:
		//    A. issueLinkToBeDeleted == issueLink_ThisSide
		//    B. issueLinkTypeToBeDeleted == issueLinkType_thisSide
		//    C. issueLinkTypeOnAnotherSide == issueLinkType_otherSide
		//    D. issueOnOtherSide == issue_otherSide
		//    E. anotherSideObject == linkedObject_otherSide
		//    F. thisSideObject == object_thisSide
		//    G. issueLinkOnOtherSide == issueLink_otherSide

		// -- 1. [Remove the reverse link]--------------------------------------------------------------------------------------|
		IssueLinkType issueLinkType_thisSide = issueLink_thisSide.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeIDToBeDeleted = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType_thisSide);
		if (issueLinkTypeIDToBeDeleted == null)
			throw new IllegalStateException("JDOHelper.getObjectId(issueLinkToBeDeleted.getIssueLinkType()) returned null!");

		IssueLinkType issueLinkType_otherSide = getReverseIssueLinkType(pm, issueLinkTypeIDToBeDeleted);

		Issue issue_thisSide = issueLink_thisSide.getIssue();
		Issue issue_otherSide = (Issue)issueLink_thisSide.getLinkedObject();


		// Find the correct reverse link to be removed.
		// --> Aha! There is NO guard to prevent ETERNAL recursion?? Well, that's because we dont need for a guard here.
		boolean isIssueLinkFound = false;
		Set<IssueLink> issueLinksOnOtherSide = issue_otherSide.getIssueLinks();
		for (IssueLink issueLink_otherSide : issueLinksOnOtherSide) {
			Object linkedObject_otherSide = issueLink_otherSide.getLinkedObject();
			if ( linkedObject_otherSide.equals(issue_thisSide) ) {
				if (issueLinkType_otherSide.equals(issueLink_otherSide.getIssueLinkType())) {
					issue_otherSide.removeIssueLink(issueLink_otherSide);
					isIssueLinkFound = true;
					break;
				}
			}
		}


		// -- 2. [Create the IssueLinkHistoryItems for issueOtherSide accordingly]----------------------------------------------|
		if (isIssueLinkFound) {
			User user = SecurityReflector.getUserDescriptor().getUser(pm);

			// Note: The IssueLinkHistory item for 'issueThisSide' would already have been handled in the normal routine.
			//       We do not do it here, because an Issue links to any arbitrary object; and this here, is a special case.
			final IssueLinkHistoryItem ilhItemOtherSide = new IssueLinkHistoryItem(
					user, issue_otherSide, issueLinkType_otherSide,
					IssueHistoryItemAction.REMOVED,
					Issue.class.getName(),
					issue_thisSide.getIssueIDAsString()
			);

			// Assuming here that the PersistenceManager is still open && valid.
			NLJDOHelper.storeJDO(
					pm, ilhItemOtherSide, false,
					new String[] {FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
			);
		}

	}
}
