package org.nightlabs.jfire.chezfrancois;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueManagerBean;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueResolution;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.id.IssueTypeID;
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
	 * Prepares the creation of the Demo Data.
	 * TODO The sister classes extending the {@link Initialiser} throw a series of exception here. Go figure what is needed for this scenario. Kai
	 */
	public void createDemoData() {
		// Get a reference to the IssueManagerBean.
		IssueManagerRemote issueMgr = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());

		// Determine if we need to create the demo data at all.
		if ( !issueMgr.getIssueIDs().isEmpty() ) {
			if (logger.isDebugEnabled())
				logger.info("  ------------------------>> DEMO DATA for Issues already exist; a total of " + issueMgr.getIssueIDs().size() + " in the datastore.");
			return;
		}


		// ::: Search for IssueType.DEFAULT_ISSUE_TYPE_ID.
		IssueType issueTypeDefault = null;
		final Query allIDsQuery = pm.newNamedQuery(IssueType.class, IssueType.QUERY_ALL_ISSUETYPE_IDS);
		Set<IssueTypeID> issueTypeIDs = CollectionUtil.createHashSetFromCollection( allIDsQuery.execute() );

		for (IssueTypeID issueTypeID : issueTypeIDs)
			if (issueTypeID.issueTypeID.equals(IssueType.DEFAULT_ISSUE_TYPE_ID)) {
				issueTypeDefault = (IssueType) pm.getObjectById(issueTypeID);
				break;
			}

		if (issueTypeDefault == null) return;	// <-- Should throw something here??


		// Proceed only if the default IssueType can be found. We should otherwise create one??
		String organisationID = getOrganisationID();
		User sysUser = User.getUser(pm, getPrincipal());
		String baseName = "org.nightlabs.jfire.issue.resource.messages";  // } --> FIXME Change these to read from the appropriate file at the appropriate location! Kai.
		ClassLoader loader = IssueManagerBean.class.getClassLoader();     // }

		int totDemoIssueCnt = 56;
		boolean isCreateArbitraryIssueToIssueLinks = !true;
		List<IssuePriority> def_issuePriorities = issueTypeDefault.getIssuePriorities();
		List<IssueResolution> def_issueResolutions = issueTypeDefault.getIssueResolutions();
		List<IssueSeverityType> def_issueSeverityTypes = issueTypeDefault.getIssueSeverityTypes();
		List<Issue> demoIssues = new ArrayList<Issue>(totDemoIssueCnt);

		Random rndGen = new Random( System.currentTimeMillis() );
		pm.getExtent(Issue.class);
		for (int i=0; i<totDemoIssueCnt; i++) {
			Issue demoIssue = new Issue(organisationID, IDGenerator.nextID(Issue.class), issueTypeDefault);

			// Essential issue settings
			demoIssue.setReporter(sysUser);
			demoIssue.setIssuePriority( def_issuePriorities.get( rndGen.nextInt(def_issuePriorities.size()) ) );
			demoIssue.setIssueResolution( def_issueResolutions.get( rndGen.nextInt(def_issueResolutions.size()) ) );
			demoIssue.setIssueSeverityType( def_issueSeverityTypes.get( rndGen.nextInt(def_issueSeverityTypes.size()) ) );

			// Subject and description
			demoIssue.getSubject().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.IssueManagerBean.issueSubject" + (i+1)); //$NON-NLS-1$
			demoIssue.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.IssueManagerBean.issueDescription" + (i+1)); //$NON-NLS-1$

			if (logger.isDebugEnabled()) {
				logger.info(" ::: Creating Issue: (ID:" + demoIssue.getIssueID() + ") "  + demoIssue.getSubject().getText() + " [" + demoIssue.getDescription().getText() + "]");
			}

			// IssueMarkers.
			// <-- TODO Still need to read off the database first.


			// TODO Randomly (and playfully?) create links between Issues.
			if (isCreateArbitraryIssueToIssueLinks && demoIssues.size() > 5) {
				List<Integer> usedIndexRefs = new ArrayList<Integer>();
				while (rndGen.nextInt(100) < 67 && usedIndexRefs.size() < demoIssues.size()/2) {
					int index = rndGen.nextInt(demoIssues.size());
					if ( !usedIndexRefs.contains(index) ) {
//						demoIssue.createIssueLink(issueLinkTypeParent, demoIssues.get(index));
						usedIndexRefs.add(index);
					}
				}
			}


			// Done and store.
			demoIssues.add(demoIssue);
			issueMgr.storeIssue(demoIssue, false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		}

	}
}
