package org.nightlabs.jfire.personrelation.issuetracking;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueComment;
import org.nightlabs.jfire.issue.IssueDescription;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.dao.IssueLinkDAO;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueLinkID;
import org.nightlabs.jfire.jdo.notification.TreeNodeMultiParentResolver;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.PersonRelation;
import org.nightlabs.jfire.personrelation.dao.PersonRelationDAO;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.progress.NullProgressMonitor;

public class PersonRelationIssueParentResolver
implements TreeNodeMultiParentResolver
{
	private static final long serialVersionUID = 1L;

	@Override
	public Collection<ObjectID> getParentObjectIDs(Object jdoObject) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(jdoObject);
		IssueID issueID = null;
		Issue issue = null;
		if (jdoObject instanceof IssueLink) {
			IssueLink issueLink = (IssueLink) jdoObject;
			ObjectID linkedObjectID = issueLink.getLinkedObjectID();
			if (linkedObjectID instanceof PropertySetID && Person.class.isAssignableFrom(issueLink.getLinkedObjectClass())) {
				// We need to return all PersonRelationIDs to this person and the person's ID itself.
				PropertySetID personID = (PropertySetID) linkedObjectID;
				Collection<ObjectID> result = new LinkedList<ObjectID>();
				result.add(personID);

				if (pm != null) {
					// We are in the server.
					Person person = (Person) pm.getObjectById(personID);
					Collection<? extends PersonRelation> personRelations = PersonRelation.getPersonRelations(pm, null, null, person);
					Set<PersonRelationID> personRelationIDs = NLJDOHelper.getObjectIDSet(personRelations);
					result.addAll(personRelationIDs);
				}
				else {
					// We are in the client.
					Collection<PersonRelationID> personRelationIDs = PersonRelationDAO.sharedInstance().getPersonRelationIDs(null, null, personID, new NullProgressMonitor());
					result.addAll(personRelationIDs);
				}
				return result;
			}
		}
		else if (jdoObject instanceof IssueDescription) {
			// Find all IssueLinks of the Issue of this IssueDescription, which point to a Person.
			IssueDescription issueDescription = (IssueDescription) jdoObject;
			if (pm != null) {
				// We are in the server.
				issueID = IssueID.create(issueDescription.getOrganisationID(), issueDescription.getIssueID());
				issue = (Issue) pm.getObjectById(issueID);
			}
			else {
				// We are in the client. This implementation isn't efficient, but IMHO this method shouldn't be called that often.
				issueID = IssueID.create(issueDescription.getOrganisationID(), issueDescription.getIssueID());
			}
		}
		else if (jdoObject instanceof IssueComment) {
			// Find all IssueLinks of the Issue of this IssueComment, which point to a Person.
			IssueComment issueComment = (IssueComment) jdoObject;
			if (pm != null) {
				// We are in the server.
				issue = issueComment.getIssue();
				issueID = (IssueID) JDOHelper.getObjectId(issue);
				if (issueID == null)
					throw new IllegalStateException("JDOHelper.getObjectId(issue) returned null! " + issue);
			}
			else {
				// We are in the client. This implementation isn't efficient, but IMHO this method shouldn't be called that often.
				issueID = issueComment.getIssueID();
			}
		}

		if (issueID != null) {
			if (pm != null) {
				// We are in the server.
				if (issue == null)
					throw new IllegalStateException("issueID != null but issue == null");

				Collection<ObjectID> result = new LinkedList<ObjectID>();
				result.add(issueID);
				for (IssueLink issueLink : issue.getIssueLinks()) {
					if (Person.class.isAssignableFrom(issueLink.getLinkedObjectClass())) {
						IssueLinkID issueLinkID = (IssueLinkID) JDOHelper.getObjectId(issueLink);
						if (issueLinkID == null)
							throw new IllegalStateException("JDOHelper.getObjectId(issueLinkID) returned null! " + issueLinkID);

						result.add(issueLinkID);
					}
				}
				return result;
			}
			else {
				// We are in the client. This implementation isn't efficient, but IMHO this method shouldn't be called that often.
				Collection<ObjectID> result = new LinkedList<ObjectID>();
				result.add(issueID);
				Collection<IssueLinkID> c = IssueLinkDAO.sharedInstance().getIssueLinkIDsForIssueAndLinkedObjectClass(issueID, Person.class, new NullProgressMonitor());
				result.addAll(c);
				return result;
			}
		}

		return Collections.emptySet();
	}

}
