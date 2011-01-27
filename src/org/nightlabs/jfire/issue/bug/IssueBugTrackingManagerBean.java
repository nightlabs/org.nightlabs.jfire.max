package org.nightlabs.jfire.issue.bug;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueLinkType;
import org.nightlabs.jfire.issue.IssueLinkTypeDuplicate;
import org.nightlabs.jfire.issue.IssueLinkTypeIssueToIssue;
import org.nightlabs.jfire.issue.IssueLinkTypeParentChild;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueResolution;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.issue.project.ProjectType;
import org.nightlabs.jfire.issue.prop.IssueStruct;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

/**
 * An EJB session bean used for initializing the properties for bug tracking.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class IssueBugTrackingManagerBean 
extends BaseSessionBeanImpl
implements IssueBugTrackingManagerRemote
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueBugTrackingManagerBean.class);
	
	/**
	 * This method is a shortcut to <code>getPrincipal().getLookup().getPersistenceManager()</code>.
	 * <p>
	 * <b>Important:</b> You must call {@link PersistenceManager#close()} at the end of your EJB method!
	 * </p>
	 *
	 * @return Returns the PersistenceManager assigned to the current user.
	 *
	 * @see getPrincipal()
	 */
	protected PersistenceManager createPersistenceManager()
	{
		return getPrincipal().getLookup().createPersistenceManager();
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			UserID systemUserID = UserID.create(getOrganisationID(), getUserID());
			User systemUser = (User)pm.getObjectById(systemUserID);
			// WORKAROUND JPOX Bug to avoid problems with creating workflows as State.statable is defined as interface and has subclassed implementations
			pm.getExtent(Issue.class);

			IssueStruct.getIssueStruct(pm);

			// The complete method is executed in *one* transaction. So if one thing fails, all fail.
			// => We check once at the beginning, if this module has already been initialised.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireIssueTrackerBugTrackingEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of " + JFireIssueTrackerBugTrackingEAR.MODULE_NAME + " started...");

			moduleMetaData = pm.makePersistent(
					ModuleMetaData.createModuleMetaDataFromManifest(JFireIssueTrackerBugTrackingEAR.MODULE_NAME, JFireIssueTrackerBugTrackingEAR.class)
			);

			String baseName = "org.nightlabs.jfire.issue.bug.resource.messages";
			ClassLoader loader = IssueBugTrackingManagerBean.class.getClassLoader();

			IssueType issueTypeDefault = new IssueType(getOrganisationID(), IssueType.DEFAULT_ISSUE_TYPE_ID);
			issueTypeDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueTypeDefault"); //$NON-NLS-1$
			issueTypeDefault = pm.makePersistent(issueTypeDefault);

			// Create the statuses
			IssueSeverityType issueSeverityTypeMinor = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MINOR);
			issueSeverityTypeMinor.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueSeverityTypeMinor"); //$NON-NLS-1$
			issueSeverityTypeMinor = pm.makePersistent(issueSeverityTypeMinor);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeMinor);

			IssueSeverityType issueSeverityTypeMajor = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MAJOR);
			issueSeverityTypeMajor.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueSeverityTypeMajor"); //$NON-NLS-1$
			issueSeverityTypeMajor = pm.makePersistent(issueSeverityTypeMajor);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeMajor);

			IssueSeverityType issueSeverityTypeCrash = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_CRASH);
			issueSeverityTypeCrash.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueSeverityTypeCrash"); //$NON-NLS-1$
			issueSeverityTypeCrash = pm.makePersistent(issueSeverityTypeCrash);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeCrash);

			IssueSeverityType issueSeverityTypeBlock = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_BLOCK);
			issueSeverityTypeBlock.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueSeverityTypeBlock"); //$NON-NLS-1$
			issueSeverityTypeBlock = pm.makePersistent(issueSeverityTypeBlock);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeBlock);

			IssueSeverityType issueSeverityTypeFeature = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_FEATURE);
			issueSeverityTypeFeature.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueSeverityTypeFeature"); //$NON-NLS-1$
			issueSeverityTypeFeature = pm.makePersistent(issueSeverityTypeFeature);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeFeature);

			IssueSeverityType issueSeverityTypeTrivial = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TRIVIAL);
			issueSeverityTypeTrivial.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueSeverityTypeTrivial"); //$NON-NLS-1$
			issueSeverityTypeTrivial = pm.makePersistent(issueSeverityTypeTrivial);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeTrivial);

			IssueSeverityType issueSeverityTypeText = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TEXT);
			issueSeverityTypeText.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueSeverityTypeText"); //$NON-NLS-1$
			issueSeverityTypeText = pm.makePersistent(issueSeverityTypeText);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeText);

			IssueSeverityType issueSeverityTypeTweak = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TWEAK);
			issueSeverityTypeTweak.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueSeverityTypeTweak"); //$NON-NLS-1$
			issueSeverityTypeTweak = pm.makePersistent(issueSeverityTypeTweak);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeTweak);

			////////////////////////////////////////////////////////
			// Create the priorities
			// check, whether the datastore is already initialized
			IssuePriority issuePriorityNone = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_NONE);
			issuePriorityNone.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issuePriorityNone"); //$NON-NLS-1$
			issuePriorityNone = pm.makePersistent(issuePriorityNone);
			issueTypeDefault.getIssuePriorities().add(issuePriorityNone);

			IssuePriority issuePriorityLow = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_LOW);
			issuePriorityLow.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issuePriorityLow"); //$NON-NLS-1$
			issuePriorityLow = pm.makePersistent(issuePriorityLow);
			issueTypeDefault.getIssuePriorities().add(issuePriorityLow);

			IssuePriority issuePriorityNormal = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_NORMAL);
			issuePriorityNormal.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issuePriorityNormal"); //$NON-NLS-1$
			issuePriorityNormal = pm.makePersistent(issuePriorityNormal);
			issueTypeDefault.getIssuePriorities().add(issuePriorityNormal);

			IssuePriority issuePriorityHigh = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_HIGH);
			issuePriorityHigh.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issuePriorityHigh"); //$NON-NLS-1$
			issuePriorityHigh = pm.makePersistent(issuePriorityHigh);
			issueTypeDefault.getIssuePriorities().add(issuePriorityHigh);

			IssuePriority issuePriorityUrgent = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_URGENT);
			issuePriorityUrgent.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issuePriorityUrgent"); //$NON-NLS-1$
			issuePriorityUrgent = pm.makePersistent(issuePriorityUrgent);
			issueTypeDefault.getIssuePriorities().add(issuePriorityUrgent);

			IssuePriority issuePriorityImmediate = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_IMMEDIATE);
			issuePriorityImmediate.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issuePriorityImmediate"); //$NON-NLS-1$
			issuePriorityImmediate = pm.makePersistent(issuePriorityImmediate);
			issueTypeDefault.getIssuePriorities().add(issuePriorityImmediate);


			// Create the resolutions
			IssueResolution issueResolutionNotAssigned = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_ID_NOT_ASSIGNED.issueResolutionID);
			issueResolutionNotAssigned.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueResolutionNotAssigned"); //$NON-NLS-1$
			issueResolutionNotAssigned = pm.makePersistent(issueResolutionNotAssigned);
			issueTypeDefault.getIssueResolutions().add(issueResolutionNotAssigned);

			IssueResolution issueResolutionOpen = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_OPEN);
			issueResolutionOpen.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueResolutionOpen"); //$NON-NLS-1$
			issueResolutionOpen = pm.makePersistent(issueResolutionOpen);
			issueTypeDefault.getIssueResolutions().add(issueResolutionOpen);

			IssueResolution issueResolutionFixed = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_FIXED);
			issueResolutionFixed.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueResolutionFixed"); //$NON-NLS-1$
			issueResolutionFixed = pm.makePersistent(issueResolutionFixed);
			issueTypeDefault.getIssueResolutions().add(issueResolutionFixed);

			IssueResolution issueResolutionReopened = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_REOPENED);
			issueResolutionReopened.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueResolutionReopened"); //$NON-NLS-1$
			issueResolutionReopened = pm.makePersistent(issueResolutionReopened);
			issueTypeDefault.getIssueResolutions().add(issueResolutionReopened);

			IssueResolution issueResolutionNotFixable = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_NOTFIXABLE);
			issueResolutionNotFixable.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueResolutionNotFixable"); //$NON-NLS-1$
			issueResolutionNotFixable = pm.makePersistent(issueResolutionNotFixable);
			issueTypeDefault.getIssueResolutions().add(issueResolutionNotFixable);

			IssueResolution issueResolutionWillNotFix = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_WILLNOTFIX);
			issueResolutionWillNotFix.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueResolutionWillNotFix"); //$NON-NLS-1$
			issueResolutionWillNotFix = pm.makePersistent(issueResolutionWillNotFix);
			issueTypeDefault.getIssueResolutions().add(issueResolutionWillNotFix);

			// Create the process definitions.
			issueTypeDefault.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));

			IssueType issueTypeCustomer = new IssueType(getOrganisationID(), "Customer");
			issueTypeCustomer.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.issueTypeCustomer"); //$NON-NLS-1$
			issueTypeCustomer = pm.makePersistent(issueTypeCustomer);

			// Create the process definitions.
			issueTypeCustomer.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));

			// ---[ ProjectTypes ]----------------------------------------------------------------------------------------------| Start |---
			pm.getExtent(ProjectType.class);

			ProjectType projectTypeDefault = new ProjectType(ProjectType.PROJECT_TYPE_ID_DEFAULT);
			projectTypeDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.projectTypeDefault"); //$NON-NLS-1$

			projectTypeDefault = pm.makePersistent(projectTypeDefault);

			ProjectType projectTypeSoftware = new ProjectType(IDGenerator.getOrganisationID(), "software");
			projectTypeSoftware.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.software"); //$NON-NLS-1$

			projectTypeSoftware = pm.makePersistent(projectTypeSoftware);

			// Create the projects
			pm.getExtent(Project.class);

			Project projectDefault = new Project(Project.PROJECT_ID_DEFAULT);
			projectDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.projectDefault"); //$NON-NLS-1$

			projectDefault.setProjectType(projectTypeDefault);
			projectDefault = pm.makePersistent(projectDefault);

			Project jfireProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			jfireProject.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.bug.IssueBugTrackingManagerBean.jfireProject"); //$NON-NLS-1$
			jfireProject.setProjectType(projectTypeSoftware);
			jfireProject = pm.makePersistent(jfireProject);
			// ---[ ProjectTypes ]------------------------------------------------------------------------------------------------| End |---
		} finally {
			pm.close();
		}
	}
	
//	private static void assignIssueMarkerIcon16x16(IssueMarker issueMarker, String iconFileName) throws IOException
//	{
//		InputStream in = Messages.class.getResourceAsStream(iconFileName);
//		if (in == null)
//			throw new IllegalArgumentException("There is no resource named \"" + iconFileName + "\" in the package " + Messages.class.getPackage().getName() + "!");
//
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		IOUtil.transferStreamData(in, out);
//		in.close();
//		out.close();
//
//		if (logger.isDebugEnabled())
//			logger.debug("--> Received: iconFilename: " + iconFileName);
//
//		issueMarker.setIcon16x16Data(out.toByteArray());
//	}
}
