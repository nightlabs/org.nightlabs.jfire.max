package org.nightlabs.jfire.chezfrancois;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueLinkType;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueResolution;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.history.IssueHistoryItem;
import org.nightlabs.jfire.issue.history.IssueHistoryItemAction;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.issuemarker.IssueMarker;
import org.nightlabs.jfire.issue.issuemarker.IssueMarkerHistoryItem;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.CollectionUtil;

/**
 * The {@link Initialiser} in preparing the demo data related to the IssueTracking project.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class InitialiserIssueTracking extends Initialiser {
	private static final Logger logger = Logger.getLogger(InitialiserIssueTracking.class);

	/**
	 * Creates a new instance of the InitialiserIssueTracking.
	 */
	public InitialiserIssueTracking(PersistenceManager pm, JFirePrincipal principal) {
		super(pm, principal);
	}

	/**
	 * Prepares the creation of the Demo Data. TODO The sister classes extending the {@link Initialiser}
	 * throw a series of exception here. Go figure what is needed for this scenario. Kai
	 */
	public void createDemoData() {
		// Get a reference to the IssueManagerBean.
		IssueManagerRemote issueMgr = JFireEjb3Factory.getRemoteBean(
				IssueManagerRemote.class, SecurityReflector
						.getInitialContextProperties());

		// Determine if we need to create the demo data at all.
		if (!issueMgr.getIssueIDs().isEmpty()) {
			if (logger.isDebugEnabled())
				logger.info(" ------------------------>> DEMO DATA for Issues already exist; a total of " + issueMgr.getIssueIDs().size() + " in the datastore."); //$NON-NLS-1$ //$NON-NLS-2$

			return;
		}

		// ::: Search for IssueType.DEFAULT_ISSUE_TYPE_ID.
		IssueType issueTypeDefault = null;
		final Query allIDsQuery = pm.newNamedQuery(IssueType.class, IssueType.QUERY_ALL_ISSUETYPE_IDS);
		Set<IssueTypeID> issueTypeIDs = CollectionUtil.createHashSetFromCollection(allIDsQuery.execute());

		for (IssueTypeID issueTypeID : issueTypeIDs)
			if (issueTypeID.issueTypeID.equals(IssueType.DEFAULT_ISSUE_TYPE_ID)) {
				issueTypeDefault = (IssueType) pm.getObjectById(issueTypeID);
				break;
			}

		// Proceed only if the default IssueType can be found. We should
		// otherwise create one??
		if (issueTypeDefault == null)
			return; // <-- Should throw something here??

		// Prepare all other necessary references.
		String organisationID = getOrganisationID();
		User sysUser = User.getUser(pm, getPrincipal());

		// Data references.
		String baseName = "org.nightlabs.jfire.chezfrancois.resource.messages";
		ClassLoader loader = InitialiserIssueTracking.class.getClassLoader();

		// Load ALL currently known IssuePriorities.
		List<IssuePriority> def_issuePriorities = issueTypeDefault.getIssuePriorities();

		// Load ALL currently known IssueResolutions.
		List<IssueResolution> def_issueResolutions = issueTypeDefault.getIssueResolutions();

		// Load ALL currently known IssueSeverityTypes.
		List<IssueSeverityType> def_issueSeverityTypes = issueTypeDefault.getIssueSeverityTypes();

		// Load ALL currently known IssueMarkers.
		List<IssueMarker> issueMarkers = NLJDOHelper.getObjectList(pm, issueMgr.getIssueMarkerIDs(), IssueMarker.class);
		if (logger.isDebugEnabled()) {
			logger.info(" ----> Found IssueMarkers");
			for (IssueMarker issueMarker : issueMarkers)
				logger.info("   |--> " + issueMarker.getDescription().getText()); //$NON-NLS-1$
		}

		// Load ALL currently known IssueLinkTypes.
		List<IssueLinkType> issueLinkTypes = NLJDOHelper.getObjectList(pm, issueMgr.getIssueLinkTypeIDs(), IssueLinkType.class);
		if (logger.isDebugEnabled()) {
			logger.info(" ----> Found IssueLinkTypes");
			for (IssueLinkType issueLinkType : issueLinkTypes)
				logger.info("   |--> " + issueLinkType.getName().getText()); //$NON-NLS-1$
		}

		// Other simulation settings.
		String statString = ""; //$NON-NLS-1$
		int totDemoIssueCnt = 56;
		boolean isCreateArbitraryIssueToIssueLinks = !true;

		pm.getExtent(Issue.class);
		pm.getExtent(IssueHistoryItem.class);
		Random rndGen = new Random(System.currentTimeMillis());

		// Generate demo data.
		int ctr = 0;
		List<Issue> demoIssues = new ArrayList<Issue>(totDemoIssueCnt);
		List<IssueID> demoIssueIDs = new ArrayList<IssueID>(totDemoIssueCnt);
		for (int i = 0; i < totDemoIssueCnt; i++) {
			Issue demoIssue = new Issue(organisationID, IDGenerator
					.nextID(Issue.class), issueTypeDefault);

			// Subject and description.
			demoIssue.setReporter(sysUser);
			demoIssue.getSubject().readFromProperties(baseName, loader, "org.nightlabs.jfire.chezfrancois.InitialiserIssueTracking.issueSubject" + (i + 1)); //$NON-NLS-1$
			demoIssue.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.chezfrancois.InitialiserIssueTracking.issueDescription" + (i + 1)); //$NON-NLS-1$
			statString = " ::::: Issue created -- (ID:" + demoIssue.getIssueID() + ") " + demoIssue.getSubject().getText() + " [" + demoIssue.getDescription().getText() + "]";

			// For integrity testings ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>> START
			// ~~ I. Randomly assign Priority, Resolution, and Severity Type.
			demoIssue.setIssuePriority(def_issuePriorities.get(rndGen.nextInt(def_issuePriorities.size())));
			demoIssue.setIssueResolution(def_issueResolutions.get(rndGen.nextInt(def_issueResolutions.size())));
			demoIssue.setIssueSeverityType(def_issueSeverityTypes.get(rndGen.nextInt(def_issueSeverityTypes.size())));

			// ~~ II. Randomly assign IssueMarkers.
			for (IssueMarker issueMarker : issueMarkers)
				if (rndGen.nextInt(100) < 40) {
					if (logger.isDebugEnabled())
						logger.info(" ~~ Marking Issue: " + issueMarker.getDescription().getText()); //$NON-NLS-1$

					demoIssue.addIssueMarker(issueMarker);
					ctr++;

					IssueHistoryItem issueHistoryItem = new IssueMarkerHistoryItem(sysUser, demoIssue, issueMarker, IssueHistoryItemAction.ADDED);
					issueHistoryItem = pm.makePersistent(issueHistoryItem);
				}

			statString += ", (IssueMarkers:" + ctr + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			ctr = 0;

			// ~~ III. Randomly create links between demoIssue and other Issue(s).
			if (isCreateArbitraryIssueToIssueLinks && demoIssues.size() > 5) {
				List<Integer> usedIndexRefs = new ArrayList<Integer>();
				int maxLinkNum = rndGen.nextInt(demoIssues.size() / 2);
				int totLinkTyp = issueLinkTypes.size();

				while (rndGen.nextInt(100) < 67 && usedIndexRefs.size() < maxLinkNum) {
					int index = rndGen.nextInt(demoIssues.size());

					if (!usedIndexRefs.contains(index)) {
						IssueLinkType issueLinkType = issueLinkTypes.get(rndGen.nextInt(totLinkTyp));
						Issue issueToLink = demoIssues.get(index);

						if (logger.isDebugEnabled())
							logger.info(" ~~ Linking Issue to Issue: (ID:" + issueToLink.getIssueID() + ") as " + issueLinkType.getName().getText()); //$NON-NLS-1$ //$NON-NLS-2$

						// IssueLink issueLink = new IssueLink(organisationID, IDGenerator.nextID(IssueLink.class), demoIssue, issueLinkType, issueToLink);
						// demoIssue.addIssueLink(issueLink);

						// demoIssue.createIssueLink(issueLinkType, issueToLink); // <-- This will generate the reverse symmetrical link. And the history too?
						// (IssueLinkType issueLinkType, ObjectID linkedObjectID, Class<?> linkedObjectClass)
						// ObjectID objID = (ObjectID)JDOHelper.getObjectId(issueToLink);

						// demoIssue.createIssueLink(issueLinkType, (ObjectID) JDOHelper.getObjectId(issueToLink), Issue.class);
						demoIssue.createIssueLink(issueLinkType, demoIssueIDs.get(index), Issue.class);

						usedIndexRefs.add(index);
						ctr++;
					}
				}

				statString += ", (Issue-to-Issue:" + ctr + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				ctr = 0;
			}


			// TODO ~~ IV. Randomly create links between demoIssue and Person(s).

			// Report status.
			if (logger.isDebugEnabled())
				logger.info(statString);
			// For integrity testings ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~<< END

			// Done and store.
			demoIssues.add(demoIssue);
			demoIssue = issueMgr.storeIssue(demoIssue, !false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			demoIssueIDs.add( (IssueID)JDOHelper.getObjectId(demoIssue) );	// Keep a copy of the corresponding IssueID for quick reference.
		}

	}
}

