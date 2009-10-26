package org.nightlabs.jfire.issue.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.IssueLinkType;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueLinkHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public /*abstract */class IssueLinkHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Note: Most interesting one yet! This should deal with mainly two things:
		//        (i) the Object linked to the current Issue, and
		//       (ii) the 'relationship' of the link between the Issue and the Object.
		//
		// Also, handling IssueLinks is definitely not a straight-forward task. Suppose that the
		// linkedObject of the current Issue is also an Issue, we should then also have its history
		// amended accordingly. For example:
		//   -- Suppose we created a link in [Issue A] to relate to [Issue B] as [Child of].
		//   -- Then it should also be reflected in Issue B's history that a link has been created in
		//      [Issue B] to related to [Issue A] as [Parent of].
		//
		// (This information of the links should already be available through the IssueLink, right?) -- Right. See notes 16.06.2009. Kai.
		Set<IssueLink> oldIssueLinks = oldPersistentIssue.getIssueLinks();
		Set<IssueLink> newIssueLinks = newDetachedIssue.getIssueLinks();

		// (i) Check for newly created IssueLinks.
		Collection<IssueLink> addedIssueLinks = new ArrayList<IssueLink>();
		if (oldIssueLinks.isEmpty()) {
			addedIssueLinks.addAll(newIssueLinks);
		}
		else {
			for (IssueLink issueLink : newIssueLinks)
				if (!oldIssueLinks.contains(issueLink)) addedIssueLinks.add(issueLink);
		}
		// (ii) Check for recently severed IssueLinks.
		Collection<IssueLink> removedIssueLinks = new ArrayList<IssueLink>();
		if (newIssueLinks.isEmpty()) {
			removedIssueLinks.addAll(oldIssueLinks);
		}
		else {
			for (IssueLink issueLink : oldIssueLinks)
				if (!newIssueLinks.contains(issueLink))
					removedIssueLinks.add(issueLink);
		}

		// (iii) Collate them all, and generate the necessary IssueHistoryItem.
		Collection<IssueHistoryItem> issueLinkHistoryItems = new ArrayList<IssueHistoryItem>();
		for (IssueLink issueLink : addedIssueLinks) {
			// [Default behaviour]
			// Create a new IssueHistoryItem to indicate that a new IssueLink has been created.
			// Generate the IssueHistoryItem for the forward link.
			Object linkedObject = getPersistenceManager().getObjectById( issueLink.getLinkedObjectID() );
			Class<?> linkedObjectClass = linkedObject.getClass();
			IssueLinkHistoryItem item = createIssueLinkHistoryItem(
					user, oldPersistentIssue,
					issueLink.getIssueLinkType(), 	// <-- The relationship between the Issue and the linkedObject.
					IssueHistoryItemAction.ADDED,
					linkedObjectClass,
					(ObjectID)JDOHelper.getObjectId(linkedObject)
			);
			if (item != null)
				issueLinkHistoryItems.add(item);
//			issueLinkHistoryItems.add( new IssueLinkHistoryItem(
//					user, oldPersistentIssue,
//					issueLink.getIssueLinkType(), 	// <-- The relationship between the Issue and the linkedObject.
//					IssueHistoryItemAction.ADDED,
//					linkedObjectClass,
//					(ObjectID)JDOHelper.getObjectId(linkedObject)
////					,
////					getReadableLinkedObjectID(linkedObject, issueLink) // <-- Reconsider this approach, please. Kai.
//			));

			// [Additional behaviour] :: The reverse-symmetric link for Issue-to-Issue.
			// --> Note: This is done through the method 'postCreateIssueLink(...)' in IssueLinkTypeIssueToIssue.
		}

		// Similarly, when a link is removed, we should note the severance of the link of BOTH objects.
		for (IssueLink issueLink : removedIssueLinks) {
			// [Default behaviour]
			Object linkedObject = getPersistenceManager().getObjectById( issueLink.getLinkedObjectID() );
			Class<?> linkedObjectClass = linkedObject.getClass();
			IssueLinkHistoryItem item = createIssueLinkHistoryItem(
					user, oldPersistentIssue,
					issueLink.getIssueLinkType(),
					IssueHistoryItemAction.REMOVED,
					linkedObjectClass,
					(ObjectID)JDOHelper.getObjectId(linkedObject)
			);
			if (item != null)
				issueLinkHistoryItems.add(item);

//			issueLinkHistoryItems.add( new IssueLinkHistoryItem(
//					user, oldPersistentIssue,
//					issueLink.getIssueLinkType(),
//					IssueHistoryItemAction.REMOVED,
//					linkedObjectClass,
//					(ObjectID)JDOHelper.getObjectId(linkedObject)
////					,
////					getReadableLinkedObjectID(linkedObject, issueLink) // <-- Reconsider this approach, please. Kai.
//			));

			// [Additional behaviour] :: The reverse-symmetric link for Issue-to-Issue.
			// --> Note: This is done through the method 'postCreateIssueLink(...)' in IssueLinkTypeIssueToIssue.
		}

		return issueLinkHistoryItems;
	}

	protected /*abstract*/ IssueLinkHistoryItem createIssueLinkHistoryItem(User user,
			Issue issue,
			IssueLinkType issueLinkType,
			IssueHistoryItemAction issueHistoryItemAction,
			Class<?> linkedObjectClass,
			ObjectID linkedObjectID)
	{
		return new IssueLinkHistoryItem(user, issue, issueLinkType, issueHistoryItemAction, linkedObjectClass, linkedObjectID);
	}
//	);

//	/**
//	 * Specific text to return to indicate the identity of the linked object
//	 * for human-readable display on the IssueHistoryLinks.
//	 * TODO Finish this properly; consider the following:
//	 *        1. Person -- Person [OK]
//	 *        2. Trade -- Order [OK]
//	 *        3. Trade -- Offer [OK]
//	 *        4. Issue -- Issue [OK]
//	 *        5. Accounting -- Invoice
//	 *        6. Store -- Delivery Note
//	 *        7. Store -- Reception Note
//	 */
//	protected String getReadableLinkedObjectID(Object linkedObject, IssueLink issueLink) {
//		if ( linkedObject instanceof Person )
//			return ((Person)linkedObject).getDisplayName();
//
//		if ( linkedObject instanceof Issue ) {
//			Issue issue = (Issue)linkedObject;
//			return ObjectIDUtil.longObjectIDFieldToString(issue.getIssueID()) + " "
//			+ issue.getSubject().getText();	// <-- Not good. Think of an alternative, please. Kai.
//		}
//
//		// FIXME commented the following lines as a dependency on trade is not acceptable here.
//		// Think of another way doing this. Marc
//		// -- Not exactly the best of methods, but this should do for now while I think of something better. Kai
//
//		//if ( linkedObject instanceof Order )
//		//	return ((Order)linkedObject).getPrimaryKey();
//
//		//if ( linkedObject instanceof Offer )
//		//	return ((Offer)linkedObject).getPrimaryKey();
//
//		// Note: (From current standards)
//		// 1. objClassName: Offer (jdo/org.nightlabs.jfire.trade.id.OfferID?organisationID=chezfrancois.jfire.org&offerIDPrefix=2009&offerID=2)
//		// 2. objClassName: Order (jdo/org.nightlabs.jfire.trade.id.OrderID?organisationID=chezfrancois.jfire.org&orderIDPrefix=2009&orderID=8)
//		// 3. objClassName: Invoice (jdo/org.nightlabs.jfire.accounting.id.InvoiceID?organisationID=chezfrancois.jfire.org&invoiceIDPrefix=2009&invoiceID=3)
//		// 4. objClassName: DeliveryNote (jdo/org.nightlabs.jfire.store.id.DeliveryNoteID?organisationID=chezfrancois.jfire.org&deliveryNoteIDPrefix=2009&deliveryNoteID=5)
//		String objClassName = linkedObject.getClass().getSimpleName(); ===> Offer
//		String[] objIDInfos = issueLink.getLinkedObjectID().toString().split("&"); {jdo/org.nightlabs.jfire.trade.id.OfferID?organisationID=chezfrancois.jfire.org, offerIDPrefix=2009, offerID=2}
//		if (objClassName.equals("Offer") || objClassName.equals("Order") || objClassName.equals("Invoice") || objClassName.equals("DeliveryNote")) {
//			String objClassNameLC = objClassName.substring(1); //objClassName.toLowerCase(); ==> Offer
//			StringBuffer displayName = new StringBuffer();
//			for (String objIDInfo : objIDInfos) {
//				if (objIDInfo.contains(objClassNameLC + "IDPrefix=")) { offerIDPrefix=
//					String[] idPrefix = objIDInfo.split("="); {offerIDPrefix, 2009}
//					displayName.append(idPrefix[1]); 2009
//				}
//
//				if (objIDInfo.contains(objClassNameLC + "ID=")) { offerID=
//					String[] idNum = objIDInfo.split("=");
//					displayName.append("/" + idNum[1]);
//				}
//			}
//
//			return displayName.toString(); 2009
//		}
//
//		return issueLink.getLinkedObjectID().toString();
//	}

}