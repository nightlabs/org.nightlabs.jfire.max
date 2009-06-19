package org.nightlabs.jfire.issue;

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
 * Whenever an {@link IssueLink} is created to link two {@link Issue}s, we have the opportunity to ensure
 * a bidirectional link between them. That is, we should be able to create the 'reverse' link for the current
 * forward link.
 *
 * Furthermore, we should also be able to ensure a 'symmetric' link between the two Issues. For example, if
 * Issue A is linked as a 'Parent of' Issue B, then correspondingly, Issue B must be linked as a 'Child of' Issue A.
 * And as for the other two {@link IssueLinkType}s, 'Duplicate' and 'Related', the reverse link should also be
 * of the same type.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public abstract class IssueLinkTypeIssueToIssue extends IssueLinkType {
	private static final long serialVersionUID = 3707016637193680759L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueLinkTypeIssueToIssue() { }

	/**
	 * Creates a new instance of an IssueLinkTypeIssueToIssue.
	 * @param issueLinkTypeID
	 */
	public IssueLinkTypeIssueToIssue(IssueLinkTypeID issueLinkTypeID) {
		super(issueLinkTypeID);
	}


//@Kai: It's not nice to have this code here which knows subclasses of this IssueType! This method
// should have (an) abstract method(s) that provides the information about the mapping. This abstract
// method could alternatively be non-abstract (i.e. a default implementation) that returns the same
// type (to reverse the back-reference-issue-link). Only those subclasses that need different values (i.e. parent/child)
// would override this method to return the "partner" issue-type. Marco.
//
	// Note: It seems that the way that this was being utilised, we should have this as a static method instead, and then
	//       make this a general class(?) to handle IssueToIssue; since nothing in the methods refer to any of the internal
	//       variables in this class, or in the super class. Kai.
	// --- 8< --- KaiExperiments: since 19.06.2009 ------------------
	/**
	 * Given the current {@link IssueLinkTypeID}, return its corresponding reverse symmetric {@link IssueLinkType}.
	 */
	public static IssueLinkType getReverseSymmetricIssueLinkType(PersistenceManager pm, IssueLinkTypeID issueLinkTypeID) {
		if (IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_PARENT.equals(issueLinkTypeID))   return (IssueLinkType) pm.getObjectById(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_CHILD);
		if (IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_CHILD.equals(issueLinkTypeID))    return (IssueLinkType) pm.getObjectById(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_PARENT);
		if (IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_DUPLICATE.equals(issueLinkTypeID))  return (IssueLinkType) pm.getObjectById(IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_DUPLICATE);
		if (IssueLinkType.ISSUE_LINK_TYPE_ID_RELATED.equals(issueLinkTypeID))             return (IssueLinkType) pm.getObjectById(IssueLinkType.ISSUE_LINK_TYPE_ID_RELATED);

		throw new IllegalArgumentException("Illegal issueLinkTypeID " + issueLinkTypeID + "!");
	}
	// ------ KaiExperiments ----- >8 -------------------------------



	/**
	 * Generates the corresponding (symmetric) reverse link between two {@link Issue}s.
	 * This will also generated the appropriate {@link IssueLinkHistoryItem} in the {@link Issue} on the other side.
	 * @param issueLink_A the newly created {@link IssueLink} linking Issue A and Issue B.
	 */
	public static void generateIssueToIssueReverseLink(PersistenceManager pm, IssueLink issueLink_A, Issue issue_B) {
		// Notations:
		//     ___________                              ___________
		//    [           ]♦---- issueLinkType_A ---->>[           ]
		//    [  issue_A  ]                            [  issue_B  ]
		//    [___________]<<--- issueLinkType_B -----♦[___________]

		// -- 1. [Create the reverse link]--------------------------------------------------------------------------------------|
		Issue issue_A = issueLink_A.getIssue();
		IssueLinkType issueLinkType_A = issueLink_A.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType_A);
		if (issueLinkTypeID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(newIssueLink.getIssueLinkType()) returned null!");

		IssueLinkType issueLinkType_B = IssueLinkTypeIssueToIssue.getReverseSymmetricIssueLinkType(pm, issueLinkTypeID);


		// Guard: Prevents this from causing an ETERNAL recursion.
		// => find out, if the other side already has an issue-link of the required type pointing back
		for (IssueLink issueLink_B : issue_B.getIssueLinks())
			if (issueLink_B.getIssueLinkType().equals(issueLinkType_B)) {
				if (issueLink_B.getLinkedObject().equals(issue_A))
					return;
			}

		// Create the link with the type issueLinkType_B.
		issue_B.createIssueLink(issueLinkType_B, issue_A);


		// -- 2. [Create the IssueLinkHistoryItems for issueOtherSide accordingly]----------------------------------------------|
		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		// Note: The IssueLinkHistory item for 'issueThisSide' would already have been handled in the normal routine.
		//       We do not do it here, because an Issue links to any arbitrary object; and this here, is a special case.
		final IssueLinkHistoryItem ilhItemOtherSide = new IssueLinkHistoryItem(
				user, issue_B, issueLinkType_B,
				IssueHistoryItemAction.ADDED,
				Issue.class.getName(),
				issue_A.getIssueIDAsString()
		);

		// Assuming here that the PersistenceManager is still open && valid.
		NLJDOHelper.storeJDO(
				pm, ilhItemOtherSide, false,
				new String[] {FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
		);
	}

	/**
	 * Removes the corresponding (symmetric) reverse link between two {@link Issue}s.
	 * This will also generated the appropriate {@link IssueLinkHistoryItem} in the {@link Issue} on the other side.
	 * @param issueLink_A the {@link IssueLink} to be removed from Issue B.
	 */
	public static void removeIssueToIssueReverseLink(PersistenceManager pm, IssueLink issueLink_A, Issue issue_B) {
		// Notations:
		//     ___________                              ___________
		//    [           ]♦---- issueLinkType_A ---->>[           ]
		//    [  issue_A  ]                            [  issue_B  ]
		//    [___________]<<--- issueLinkType_B -----♦[___________]

		// -- 1. [Remove the reverse link]--------------------------------------------------------------------------------------|
		Issue issue_A = issueLink_A.getIssue();
		IssueLinkType issueLinkType_A = issueLink_A.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeIDToBeDeleted = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType_A);
		if (issueLinkTypeIDToBeDeleted == null)
			throw new IllegalStateException("JDOHelper.getObjectId(issueLinkToBeDeleted.getIssueLinkType()) returned null!");

		IssueLinkType issueLinkType_B = IssueLinkTypeIssueToIssue.getReverseSymmetricIssueLinkType(pm, issueLinkTypeIDToBeDeleted);

		// Find the correct reverse link to be removed.
		// --> Aha! There is NO guard to prevent ETERNAL recursion?? Well, that's because we dont need for a guard here. Kai.
		boolean isIssueLinkFound = false;
		for (IssueLink issueLink_B : issue_B.getIssueLinks()) {
			Object linkedObject_B = issueLink_B.getLinkedObject();
			if ( linkedObject_B.equals(issue_A) ) {
				if (issueLinkType_B.equals(issueLink_B.getIssueLinkType())) {
					issue_B.removeIssueLink(issueLink_B);
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
					user, issue_B, issueLinkType_B,
					IssueHistoryItemAction.REMOVED,
					Issue.class.getName(),
					issue_A.getIssueIDAsString()
			);

			// Assuming here that the PersistenceManager is still open && valid.
			NLJDOHelper.storeJDO(
					pm, ilhItemOtherSide, false,
					new String[] {FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
			);
		}
	}


//	/**
//	 * Given the current {@link IssueLinkTypeID}, return its reverse symmetric {@link IssueLinkType}.
//	 */
//	public abstract IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkTypeID issueLinkTypeID);
//
//	@Override
//	protected void postCreateIssueLink(PersistenceManager pm, IssueLink issueLink_thisSide) {
//		// Notations:
//		//     ________________                                     _________________
//		//    [                ]♦---- issueLinkType_thisSide ---->>[                 ]
//		//    [ issue_thisSide ]                                   [ issue_otherSide ]
//		//    [________________]<<--- issueLinkType_otherSide ----♦[_________________]
//
//		// -- 1. [Create the reverse link]--------------------------------------------------------------------------------------|
//		IssueLinkType issueLinkType_thisSide = issueLink_thisSide.getIssueLinkType();
//		IssueLinkTypeID issueLinkTypeID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType_thisSide);
//		if (issueLinkTypeID == null)
//			throw new IllegalStateException("JDOHelper.getObjectId(newIssueLink.getIssueLinkType()) returned null!");
//
////		IssueLinkType issueLinkType_otherSide = getReverseIssueLinkType(pm, issueLinkTypeID);
//		IssueLinkType issueLinkType_otherSide = IssueLinkTypeIssueToIssue.getReverseSymmetricIssueLinkType(pm, issueLinkTypeID);
//
//		Issue issue_thisSide = issueLink_thisSide.getIssue();
//		Issue issue_otherSide = (Issue)issueLink_thisSide.getLinkedObject();
//
//		// Guard: Prevents this from causing an ETERNAL recursion.
//		// => find out, if the other side already has an issue-link of the required type pointing back
//		for (IssueLink issueLink_otherSide : issue_otherSide.getIssueLinks())
//			if (issueLink_otherSide.getIssueLinkType().equals(issueLinkType_otherSide)) {
//				if (issueLink_otherSide.getLinkedObject().equals(issue_thisSide))
//					return;
//			}
//
//		// OK to create the link.
//		issue_otherSide.createIssueLink(issueLinkType_otherSide, issue_thisSide);
//
//
//		// -- 2. [Create the IssueLinkHistoryItems for issueOtherSide accordingly]----------------------------------------------|
//		User user = SecurityReflector.getUserDescriptor().getUser(pm);
//
//		// Note: The IssueLinkHistory item for 'issueThisSide' would already have been handled in the normal routine.
//		//       We do not do it here, because an Issue links to any arbitrary object; and this here, is a special case.
//		final IssueLinkHistoryItem ilhItemOtherSide = new IssueLinkHistoryItem(
//				user, issue_otherSide, issueLinkType_otherSide,
//				IssueHistoryItemAction.ADDED,
//				Issue.class.getName(),
//				issue_thisSide.getIssueIDAsString()
//		);
//
//		// Assuming here that the PersistenceManager is still open && valid.
//		NLJDOHelper.storeJDO(
//				pm, ilhItemOtherSide, false,
//				new String[] {FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST},
//				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
//		);
//	}
//
//	@Override
//	protected void preDeleteIssueLink(PersistenceManager pm, IssueLink issueLink_thisSide) {
//		// Notations:
//		//     ________________                                     _________________
//		//    [                ]♦---- issueLinkType_thisSide ---->>[                 ]
//		//    [ issue_thisSide ]                                   [ issue_otherSide ]
//		//    [________________]<<--- issueLinkType_otherSide ----♦[_________________]
//
//		// -- 1. [Remove the reverse link]--------------------------------------------------------------------------------------|
//		IssueLinkType issueLinkType_thisSide = issueLink_thisSide.getIssueLinkType();
//		IssueLinkTypeID issueLinkTypeIDToBeDeleted = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType_thisSide);
//		if (issueLinkTypeIDToBeDeleted == null)
//			throw new IllegalStateException("JDOHelper.getObjectId(issueLinkToBeDeleted.getIssueLinkType()) returned null!");
//
////		IssueLinkType issueLinkType_otherSide = getReverseIssueLinkType(pm, issueLinkTypeIDToBeDeleted);
//		IssueLinkType issueLinkType_otherSide = IssueLinkTypeIssueToIssue.getReverseSymmetricIssueLinkType(pm, issueLinkTypeIDToBeDeleted);
//
//		Issue issue_thisSide = issueLink_thisSide.getIssue();
//		Issue issue_otherSide = (Issue)issueLink_thisSide.getLinkedObject();
//
//
//		// Find the correct reverse link to be removed.
//		// --> Aha! There is NO guard to prevent ETERNAL recursion?? Well, that's because we dont need for a guard here.
//		boolean isIssueLinkFound = false;
//		for (IssueLink issueLink_otherSide : issue_otherSide.getIssueLinks()) {
//			Object linkedObject_otherSide = issueLink_otherSide.getLinkedObject();
//			if ( linkedObject_otherSide.equals(issue_thisSide) ) {
//				if (issueLinkType_otherSide.equals(issueLink_otherSide.getIssueLinkType())) {
//					issue_otherSide.removeIssueLink(issueLink_otherSide);
//					isIssueLinkFound = true;
//					break;
//				}
//			}
//		}
//
//
//		// -- 2. [Create the IssueLinkHistoryItems for issueOtherSide accordingly]----------------------------------------------|
//		if (isIssueLinkFound) {
//			User user = SecurityReflector.getUserDescriptor().getUser(pm);
//
//			// Note: The IssueLinkHistory item for 'issueThisSide' would already have been handled in the normal routine.
//			//       We do not do it here, because an Issue links to any arbitrary object; and this here, is a special case.
//			final IssueLinkHistoryItem ilhItemOtherSide = new IssueLinkHistoryItem(
//					user, issue_otherSide, issueLinkType_otherSide,
//					IssueHistoryItemAction.REMOVED,
//					Issue.class.getName(),
//					issue_thisSide.getIssueIDAsString()
//			);
//
//			// Assuming here that the PersistenceManager is still open && valid.
//			NLJDOHelper.storeJDO(
//					pm, ilhItemOtherSide, false,
//					new String[] {FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST},
//					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
//			);
//		}
//
//	}

}
