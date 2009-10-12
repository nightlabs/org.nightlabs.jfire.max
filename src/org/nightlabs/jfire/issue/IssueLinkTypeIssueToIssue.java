package org.nightlabs.jfire.issue;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.history.IssueHistoryItemAction;
import org.nightlabs.jfire.issue.history.IssueLinkHistoryItem;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.organisation.Organisation;
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
public class IssueLinkTypeIssueToIssue extends IssueLinkType {
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

	// The following IssueLinkTypes are commonly existing as instances of IssueLinkTypeIssueToIssue - there might,
	// however, be more (including ones that might have been created in the UI - once we have UI for it).
//	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_DUPLICATE = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "duplicate");
	//We better make duplicate asymmetric, too.

	/**
	 * This IssueLinkType replaces the generic "related" {@link IssueLinkType#ISSUE_LINK_TYPE_ID_RELATED} for Issues
	 * (the default "related" is blacklisted of the linked-object-class "Issue").
	 */
	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_RELATED_ISSUE = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "relatedIssue");

//@Kai: It's not nice to have this code here which knows subclasses of this IssueType! This method
// should have (an) abstract method(s) that provides the information about the mapping. This abstract
// method could alternatively be non-abstract (i.e. a default implementation) that returns the same
// type (to reverse the back-reference-issue-link). Only those subclasses that need different values (i.e. parent/child)
// would override this method to return the "partner" issue-type. Marco.
//
//	// Note: It seems that the way that this was being utilised, we should have this as a static method instead, and then
//	//       make this a general class(?) to handle IssueToIssue; since nothing in the methods refer to any of the internal
//	//       variables in this class, or in the super class. Kai.
//	// --- 8< --- KaiExperiments: since 19.06.2009 ------------------
//	/**
//	 * Given the current {@link IssueLinkTypeID}, return its corresponding reverse symmetric {@link IssueLinkType}.
//	 */
//	public static IssueLinkType getReverseSymmetricIssueLinkType(PersistenceManager pm, IssueLinkTypeID issueLinkTypeID) {
//		if (IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_PARENT.equals(issueLinkTypeID))   return (IssueLinkType) pm.getObjectById(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_CHILD);
//		if (IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_CHILD.equals(issueLinkTypeID))    return (IssueLinkType) pm.getObjectById(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_PARENT);
//		if (IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_DUPLICATE.equals(issueLinkTypeID))  return (IssueLinkType) pm.getObjectById(IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_DUPLICATE);
//		if (IssueLinkType.ISSUE_LINK_TYPE_ID_RELATED.equals(issueLinkTypeID))             return (IssueLinkType) pm.getObjectById(IssueLinkType.ISSUE_LINK_TYPE_ID_RELATED);
//
//		throw new IllegalArgumentException("Illegal issueLinkTypeID " + issueLinkTypeID + "!");
//	}
//	// ------ KaiExperiments ----- >8 -------------------------------

	protected IssueLinkTypeID getReverseIssueLinkTypeID()
	{
		return null;
	}

	private static IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkType issueLinkType)
	{
		IssueLinkTypeID issueLinkTypeID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType);
		if (issueLinkTypeID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(newIssueLink.getIssueLinkType()) returned null!");

		IssueLinkTypeID issueLinkTypeID_reverse = null;
		if (issueLinkType instanceof IssueLinkTypeIssueToIssue) {
			issueLinkTypeID_reverse = ((IssueLinkTypeIssueToIssue)issueLinkType).getReverseIssueLinkTypeID();
		}
		if (issueLinkTypeID_reverse == null)
			issueLinkTypeID_reverse = issueLinkTypeID;

		IssueLinkType issueLinkType_reverse = (IssueLinkType) pm.getObjectById(issueLinkTypeID_reverse);
		return issueLinkType_reverse;
	}

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
		IssueLinkType issueLinkType_B = IssueLinkTypeIssueToIssue.getReverseIssueLinkType(pm, issueLinkType_A);

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
				Issue.class,
				(ObjectID)JDOHelper.getObjectId(issue_A)
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

		// On 'Delete-Issue' guard:
		// This prevents 'un-ending' loop when either Issue is persistent; i.e. when we attempt to delete an Issue, the
		// routine triggers the jdoPreDelete() routine of the IssueLinks. Kai
		if ( JDOHelper.isDeleted(issue_B) ) return;

		// -- 1. [Remove the reverse link]--------------------------------------------------------------------------------------|
		Issue issue_A = issueLink_A.getIssue();

//		// Les Debourg...
//		boolean onA_PER = JDOHelper.isPersistent(issue_A);
//		boolean onA_DET = JDOHelper.isDetached(issue_A);
//		boolean onA_DEL = JDOHelper.isDeleted(issue_A);
//		boolean onA_TRA = JDOHelper.isTransactional(issue_A);
//
//		boolean onB_PER = JDOHelper.isPersistent(issue_B);
//		boolean onB_DET = JDOHelper.isDetached(issue_B);
//		boolean onB_DEL = JDOHelper.isDeleted(issue_B);
//		boolean onB_TRA = JDOHelper.isTransactional(issue_B);


		IssueLinkType issueLinkType_A = issueLink_A.getIssueLinkType();
		IssueLinkType issueLinkType_B = IssueLinkTypeIssueToIssue.getReverseIssueLinkType(pm, issueLinkType_A);

		// Find the correct reverse link to be removed.
		// --> Aha! There is NO guard to prevent ETERNAL recursion??
		//     Well, that's because we dont need for a guard here; since the link would have already been removed. Kai.
		// --> UNFORTUNATELY, this bit doesnt exactly work when we try to DELETE an Issue. <--- WOO HOO!! Solved!!!! Kai.
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
					Issue.class,
					(ObjectID)JDOHelper.getObjectId(issue_A)
			);

			// Assuming here that the PersistenceManager is still open && valid.
			NLJDOHelper.storeJDO(
					pm, ilhItemOtherSide, false,
					new String[] {FetchPlan.DEFAULT, FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
			);
		}
	}

	@Override
	protected void postCreateIssueLink(PersistenceManager pm, IssueLink newIssueLink) {
		super.postCreateIssueLink(pm, newIssueLink);
		IssueLinkTypeIssueToIssue.generateIssueToIssueReverseLink(pm, newIssueLink, (Issue)newIssueLink.getLinkedObject());
	}

	@Override
	protected void preDeleteIssueLink(PersistenceManager pm, IssueLink issueLinkToBeDeleted) {
		IssueLinkTypeIssueToIssue.removeIssueToIssueReverseLink(pm, issueLinkToBeDeleted, (Issue)issueLinkToBeDeleted.getLinkedObject());
		super.preDeleteIssueLink(pm, issueLinkToBeDeleted);
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
