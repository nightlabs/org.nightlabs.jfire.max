/**
 * 
 */
package org.nightlabs.jfire.reporting.scheduled;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.scheduled.id.ScheduledReportID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.timer.Task;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		objectIdClass=ScheduledReportID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireReporting_ScheduledReport")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
			fetchGroups={"default"},
			name=ScheduledReport.FETCH_GROUP_NAME,
			members=@Persistent(name=ScheduledReport.FieldName.name)),
	@FetchGroup(
		fetchGroups={"default"},
		name=ScheduledReport.FETCH_GROUP_USER,
		members=@Persistent(name=ScheduledReport.FieldName.user)),
	@FetchGroup(
			fetchGroups={"default"},
			name=ScheduledReport.FETCH_GROUP_TASK,
			members=@Persistent(name=ScheduledReport.FieldName.task)),
	@FetchGroup(
			fetchGroups={"default"},
			name=ScheduledReport.FETCH_GROUP_REPORTLAYOUT,
			members=@Persistent(name=ScheduledReport.FieldName.reportLayout)),
	@FetchGroup(
			fetchGroups={"default"},
			name=ScheduledReport.FETCH_GROUP_PARAMETER,
			members=@Persistent(name=ScheduledReport.FieldName.parameter)),
	@FetchGroup(
			fetchGroups={"default"},
			name=ScheduledReport.FETCH_GROUP_DELIVEROPTIONS,
			members=@Persistent(name=ScheduledReport.FieldName.deliverOptions))
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ScheduledReport implements Serializable, DetachCallback, AttachCallback {
	
	private static final long serialVersionUID = 20100106L;

	public static final String TASK_TYPE_SCHEDULED_REPORT_PROCESSING = "ScheduledReportProcessing";
	
	public static final class FieldName
	{
		public static final String organisationID = "organisationID";
		public static final String scheduledReportID = "scheduledReportID";
		public static final String name = "name";
		public static final String user = "user";
		public static final String userID = "userID";
		public static final String reportLayout = "reportLayout";
		public static final String reportLayoutID = "reportLayoutID";
		public static final String task = "task";
		public static final String parameter = "parameter";
		public static final String deliverOptions = "deliverOptions";
	};

	private static final String fetchGroupPrefix = "ScheduledReport.";// ScheduledReport.class.getSimpleName() + ".";
	
	public static final String FETCH_GROUP_SCHEDULEDREPORTID = fetchGroupPrefix + FieldName.scheduledReportID;
	public static final String FETCH_GROUP_NAME = fetchGroupPrefix + FieldName.name;
	public static final String FETCH_GROUP_USER = fetchGroupPrefix + FieldName.user;
	public static final String FETCH_GROUP_USER_ID = fetchGroupPrefix + FieldName.userID;
	public static final String FETCH_GROUP_REPORTLAYOUT = fetchGroupPrefix + FieldName.reportLayout;
	public static final String FETCH_GROUP_REPORTLAYOUT_ID = fetchGroupPrefix + FieldName.reportLayoutID;
	public static final String FETCH_GROUP_TASK = fetchGroupPrefix + FieldName.task;
	public static final String FETCH_GROUP_PARAMETER = fetchGroupPrefix + FieldName.parameter;
	public static final String FETCH_GROUP_DELIVEROPTIONS = fetchGroupPrefix + FieldName.deliverOptions;
	
	/** pk-part */
	@PrimaryKey
	private String organisationID;
	
	/** pk-part */
	@PrimaryKey
	private long scheduledReportID;
	
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT, dependent = "true", mappedBy = "scheduledReport")
	private ScheduledReportName name;

	/** Owner of this object, needs to be defined by constructor, immutable */
	@Persistent
	private User user;
	
	/** virtual id-detaching of user */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private UserID userID;
	@Persistent(persistenceModifier=PersistenceModifier.NONE)	
	private boolean userIDDetached = false;	
	
	/** ReportLayout that should be rendered, can be changed */
	@Persistent
	private ReportLayout reportLayout;
	
	/** virtual id-detaching of reportLayout */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private ReportRegistryItemID reportLayoutID;
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean reportLayoutIDDetached = false;	
	
	/** Task to configure time schedule for the rendering */
	@Persistent
	private Task task;
	
	/** Serialized {@link RenderReportRequest} with output-format, parameters */
	@Persistent
	private byte[] renderReportRequest;

	/** how/where to should the finished report should be delivered */
	@Persistent
	private IScheduledReportDeliveryDelegate deliveryDelegate;
	
	/**
	 * Only for JDO
	 */
	protected ScheduledReport() {
	}

	/**
	 * Constructs a new {@link ScheduledReport} for the given user.
	 * 
	 * @param user The owner of the new {@link ScheduledReport}.
	 * @param scheduledReportID The id of the new {@link ScheduledReport}.
	 */
	public ScheduledReport(User user, long scheduledReportID) {
		super();
		this.organisationID = user.getOrganisationID();
		this.scheduledReportID = scheduledReportID;
		this.user = user;
		this.name = new ScheduledReportName(this);
		this.task = new Task(
				user.getOrganisationID(),
				TASK_TYPE_SCHEDULED_REPORT_PROCESSING,
				ObjectIDUtil.longObjectIDFieldToString(scheduledReportID),
				user,
				ScheduledReportManagerLocal.class,
				"processScheduledReport"
			);
		this.task.setParam(this);
	}

	/**
	 * Returns the name of this {@link ScheduledReport}.
	 * @return The name of this {@link ScheduledReport}.
	 */
	public ScheduledReportName getName() {
		return name;
	}
	
	/**
	 * @return The user this report should be run for.
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * Get the id-object of the {@link User} that is the owner
	 * of this {@link ScheduledReport}. 
	 * <p>
	 * Note, that for detached copies of {@link ScheduledReport}
	 * this method will result in an {@link JDODetachedFieldAccessException}
	 * when the object was not detached with either
	 * {@link #FETCH_GROUP_USER} or {@link #FETCH_GROUP_USER_ID}. 
	 * </p>
	 * @return The id-object of the {@link User} that is the owner
	 * 		of this {@link ScheduledReport}.
	 */
	public UserID getUserID() {
		if (!userIDDetached && userID == null) {
			userID = (UserID) JDOHelper.getObjectId(user);
		}
		return userID;
	}

	/**
	 * Set the user the report should be run for.
	 * @param user The user to set.
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Returns the {@link ReportLayout} that should be rendered
	 * when the rendering of this {@link ScheduledReport} is due.
	 * The rendering will be performed with the parameters set.
	 * 
	 * @return The {@link ReportLayout} that should be rendered with the current parameters.
	 */
	public ReportLayout getReportLayout() {
		return reportLayout;
	}
	
	/**
	 * Get the id-object of the {@link ReportLayout} that should be rendered
	 * when the rendering of this {@link ScheduledReport} is due. 
	 * <p>
	 * Note, that for detached copies of {@link ScheduledReport}
	 * this method will result in an {@link JDODetachedFieldAccessException}
	 * when the object was not detached with either
	 * {@link #FETCH_GROUP_REPORTLAYOUT} or {@link #FETCH_GROUP_REPORTLAYOUT_ID}. 
	 * </p>
	 * @return The id-object of the {@link ReportLayout} that should be rendered
	 * 		when the rendering of this {@link ScheduledReport} is due.
	 */
	public ReportRegistryItemID getReportLayoutID() {
		if (!reportLayoutIDDetached && reportLayoutID == null) {
			reportLayoutID = (ReportRegistryItemID) JDOHelper.getObjectId(reportLayout);
		}
		return reportLayoutID;
	}

	/**
	 * Set the {@link ReportLayout} that should be rendered
	 * when the rendering of this {@link ScheduledReport} is due.
	 * 
	 * @param reportLayout The reportLayout to render.
	 */
	public void setReportLayout(ReportLayout reportLayout) {
		this.reportLayout = reportLayout;
	}

	/**
	 * Set the id-object of the the {@link ReportLayout} that should be rendered
	 * when the rendering of this {@link ScheduledReport} is due.
	 * <p>
	 * Note, that this method might only be called on detached instances
	 * of {@link ScheduledReport} that were detached with the fetch-group
	 * {@link #FETCH_GROUP_REPORTLAYOUT_ID}.
	 * When re-attached, the {@link #reportLayout} member will be set
	 * to reflect this setting (only if reportLayout was not detached!).
	 * </p>
	 * 
	 * @param reportLayoutID The id of reportLayout to render.
	 */
	public void setReportLayoutID(ReportRegistryItemID reportLayoutID) {
		if (!reportLayoutIDDetached) {
			throw new IllegalStateException("Call setReportLayoutID only on detached versions of ScheduledReport that were detached with the fetch-group '" + FETCH_GROUP_REPORTLAYOUT_ID + "'.");
		}
		this.reportLayoutID = reportLayoutID;
	}
	
	/**
	 * Returns the task that actually performs the rendering of
	 * the {@link #reportLayout}.
	 * 
	 * @return The task of this {@link ScheduledReport}.
	 */
	public Task getTask() {
		return task;
	}
	
	/**
	 * Returns the raw data of the serialized {@link RenderReportRequest} for this scheduled report.
	 * 
	 * @return The raw data of the serialized {@link RenderReportRequest} for this scheduled report.
	 */
	public byte[] getRenderReportRequest() {
		return renderReportRequest;
	}

	/**
	 * Set the raw data of the serialized {@link RenderReportRequest} for this scheduled report.
	 * This is persisted into the datastore.
	 * 
	 * @param renderReportRequest The raw data of the serialized {@link RenderReportRequest} for this scheduled report.
	 */
	public void setRenderReportRequest(byte[] renderReportRequest) {
		this.renderReportRequest = renderReportRequest;
	}

	/**
	 * Returns the {@link IScheduledReportDeliveryDelegate} for this scheduled report.
	 * <p>
	 * This stores how and where to a scheduled report should be send after its rendering is finished. 
	 * Additionally the work of actually delivering the report output is delegated to this object.
	 * </p>
	 * @return The {@link IScheduledReportDeliveryDelegate} for this scheduled report.
	 */
	public IScheduledReportDeliveryDelegate getDeliveryDelegate() {
		return deliveryDelegate;
	}
	
	/**
	 * Sets the {@link IScheduledReportDeliveryDelegate} for this scheduled report. 
	 * <p>
	 * This stores how and where to a scheduled report should be send after its rendering is finished. 
	 * Additionally the work of actually delivering the report output is delegated to this object.
	 * </p>
	 * 
	 * @param deliveryDelegate The {@link IScheduledReportDeliveryDelegate} for this scheduled report.
	 */
	public void setDeliveryDelegate(IScheduledReportDeliveryDelegate deliveryDelegate) {
		this.deliveryDelegate = deliveryDelegate;
	}

	/**
	 * @return The primary key part organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return The primary key part scheduledReportID.
	 */
	public long getScheduledReportID() {
		return scheduledReportID;
	}
	
	/**
	 * @return The associated {@link PersistenceManager} for attached instances.
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null) {
			throw new IllegalStateException("This instance of ScheduledReport is not attached, can't obtain PersistenceManager");
		}
		return pm;
	}

	/**
	 * Check for the virtual fetch-groups that store id-objects as members.
	 */
	@Override
	public void jdoPostDetach(Object obj) {
		ScheduledReport attached = (ScheduledReport) obj;
		ScheduledReport detached = this;
		FetchPlan fetchPlan = attached.getPersistenceManager().getFetchPlan();
		if (fetchPlan.getGroups().contains(FETCH_GROUP_REPORTLAYOUT_ID)) {
			detached.reportLayoutIDDetached = true;
			detached.reportLayoutID = attached.getReportLayoutID();
		}
		if (fetchPlan.getGroups().contains(FETCH_GROUP_USER_ID)) {
			detached.userIDDetached = true;
			detached.userID = attached.getUserID();
		}
	}
	
	@Override
	public void jdoPreDetach() {
		
	}

	/**
	 * Apply changes to detached report layout id-object to the persistent version.
	 */
	@Override
	public void jdoPostAttach(Object obj) {
		ScheduledReport detached = (ScheduledReport) obj;
		if (detached.reportLayoutIDDetached) {
			// reportLayoutID was detached, check if the reportLayout was not detached
			boolean reportLayoutDetached = true;
			try {
				detached.getReportLayout();
			} catch (JDODetachedFieldAccessException e) {
				reportLayoutDetached = false;
			}
			
			if (!reportLayoutDetached) {
				// we apply the setting for reportLayoutID of the detached version to the 
				// reportLayout property of the attached version.
				if (detached.getReportLayoutID() == null) {
					setReportLayout(null);
				} else {
					ReportLayout assignReportLayout = (ReportLayout) getPersistenceManager().getObjectById(detached.getReportLayoutID());
					setReportLayout(assignReportLayout);
				}
				
			}
		}
	}

	@Override
	public void jdoPreAttach() {
	}
	
	/**
	 * Get the list of {@link ScheduledReportID}s of the current user.
	 * 
	 * @param pm The PersistenceManager to use.
	 * @return The list of {@link ScheduledReportID}s of the current user.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ScheduledReportID> getScheduledReportIDsByUserID(PersistenceManager pm) {
		javax.jdo.Query q = pm.newQuery("SELECT JDOHelper.getObjectId(this) FROM " + ScheduledReport.class.getName()
				+ " WHERE JDOHelper.getObjectId(this.user) == :userID");
		return new LinkedList<ScheduledReportID>(
				(Collection<ScheduledReportID>) q.execute(SecurityReflector.getUserDescriptor().getUserObjectID()));
	}
	
}
