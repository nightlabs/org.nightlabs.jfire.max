package org.nightlabs.jfire.issue.config;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.i18n.MultiLanguagePropertiesBundle;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.table.config.ColumnDescriptor;
import org.nightlabs.jfire.table.config.IColumnConfiguration;
import org.nightlabs.jfire.table.config.IColumnContentDescriptor;
import org.nightlabs.jfire.table.config.IColumnDescriptor;
import org.nightlabs.jfire.table.config.id.ColumnDescriptorID;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot]  de
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireIssueTracking_PersonIssueLinkTableConfigModule"
)
@FetchGroups({
	@FetchGroup(
		name=PersonIssueLinkTableConfigModule.FETCH_GROUP_COLUMNDESCRIPTORS,
		members=@Persistent(name="columnDescriptors"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PersonIssueLinkTableConfigModule extends ConfigModule implements IColumnConfiguration
{
	private static final long serialVersionUID = 6386044112803896555L;

	public static final String FETCH_GROUP_COLUMNDESCRIPTORS = "PersonIssueLinkTableConfigModule.columnDescriptors";

	@Join
	@Persistent(
		table="JFireIssueTracking_PersonIssueLinkTableConfigModule_columnDescriptors",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<ColumnDescriptor> columnDescriptors;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModule#init()
	 */
	@Override
	public void init()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null!");

		// its better to have unique ids for each config module in case you need e.g. different weights
//		columnDescriptors = IssueTableColumnDescriptorFactory.generateIssueTableColumnDescriptors(pm, new LinkedList<ColumnDescriptor>(), IDGenerator.getOrganisationID(),
//				Issue.FieldName.issueID,
//				Issue.FieldName.subject,
//				Issue.FieldName.issueMarkers,
//				Issue.FieldName.issuePriority,
//				Issue.FieldName.deadlineTimestamp
//		);

		MultiLanguagePropertiesBundle propertiesBundle = new MultiLanguagePropertiesBundle("org.nightlabs.jfire.issue.resource.messages", PersonIssueLinkTableConfigModule.class.getClassLoader(), true);

		// TODO Maybe use dev organisation id
		String organisationID = IDGenerator.getOrganisationID();
		String prefix = "PersonIssueLinkTableConfigModule";
		columnDescriptors = new LinkedList<ColumnDescriptor>();

		// --> 1. ID
		ColumnDescriptorID colDesc_ID_ID = IssueTableColumnDescriptorFactory.createColumnDescriptorID(organisationID, prefix, Issue.FieldName.issueID);
		columnDescriptors.add(IssueTableColumnDescriptorFactory.fillColumnDescriptorInfos(new ColumnDescriptor(colDesc_ID_ID), Issue.FieldName.issueID, propertiesBundle));
//		colDesc_ID.getColumnName().readFromMultiLanguagePropertiesBundle(propertiesBundle, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.id.text");
//		colDesc_ID.setDescriptorInfos(10, ColumnContentProperty.TEXT_ONLY, CollectionUtil.createHashSet(Issue.FieldName.issueID),
//				new String[] {FetchPlan.DEFAULT});
//		columnDescriptors.add(colDesc_ID);

		// --> 2. Subject
		ColumnDescriptorID colDesc_Subject_ID = IssueTableColumnDescriptorFactory.createColumnDescriptorID(organisationID, prefix, Issue.FieldName.subject);
		ColumnDescriptor colDesc_Subject = IssueTableColumnDescriptorFactory.fillColumnDescriptorInfos(new ColumnDescriptor(colDesc_Subject_ID), Issue.FieldName.subject, propertiesBundle);
		colDesc_Subject.setWeight(50);
		columnDescriptors.add(colDesc_Subject);
//		colDesc_Subject.getColumnName().readFromMultiLanguagePropertiesBundle(propertiesBundle, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.subject.text");
//		colDesc_Subject.setDescriptorInfos(50, ColumnContentProperty.TEXT_ONLY, CollectionUtil.createHashSet(Issue.FieldName.subject),
//				new String[] {FetchPlan.DEFAULT, Issue.FETCH_GROUP_SUBJECT});
//		columnDescriptors.add(colDesc_Subject);

		// --> 3. Issue markers
		ColumnDescriptorID colDesc_Markers_ID = IssueTableColumnDescriptorFactory.createColumnDescriptorID(organisationID, prefix, Issue.FieldName.issueMarkers);
		ColumnDescriptor colDesc_Markers = IssueTableColumnDescriptorFactory.fillColumnDescriptorInfos(new ColumnDescriptor(colDesc_Markers_ID), Issue.FieldName.issueMarkers, propertiesBundle);
		colDesc_Markers.setWeight(20);
		columnDescriptors.add(colDesc_Markers);
//		ColumnDescriptor colDesc_Markers = new ColumnDescriptor(colDesc_Markers_ID);
//		colDesc_Markers.getColumnName().readFromMultiLanguagePropertiesBundle(propertiesBundle, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.column.markers.text");
//		colDesc_Markers.setDescriptorInfos(20, ColumnContentProperty.IMAGE_ONLY, CollectionUtil.createHashSet(Issue.FieldName.issueMarkers),
//				new String[] {FetchPlan.DEFAULT,
//					Issue.FETCH_GROUP_ISSUE_MARKERS,
//					IssueMarker.FETCH_GROUP_NAME,
//					IssueMarker.FETCH_GROUP_ICON_16X16_DATA});
//		columnDescriptors.add(colDesc_Markers);

		// --> 4. Priority
		ColumnDescriptorID colDesc_Priority_ID = IssueTableColumnDescriptorFactory.createColumnDescriptorID(organisationID, prefix, Issue.FieldName.issuePriority);
		ColumnDescriptor colDesc_Priority = IssueTableColumnDescriptorFactory.fillColumnDescriptorInfos(new ColumnDescriptor(colDesc_Priority_ID), Issue.FieldName.issuePriority, propertiesBundle);
		colDesc_Priority.setWeight(20);
		columnDescriptors.add(colDesc_Priority);
//		ColumnDescriptor colDesc_Priority = new ColumnDescriptor(colDesc_Priority_ID);
//		colDesc_Priority.getColumnName().readFromMultiLanguagePropertiesBundle(propertiesBundle, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.priority.text");
//		colDesc_Priority.setDescriptorInfos(20, ColumnContentProperty.TEXT_ONLY, CollectionUtil.createHashSet(Issue.FieldName.issuePriority),
//				new String[] {FetchPlan.DEFAULT,
//					Issue.FETCH_GROUP_ISSUE_PRIORITY,
//					IssuePriority.FETCH_GROUP_NAME,});
//		columnDescriptors.add(colDesc_Priority);

		// --> 5. Deadline
		ColumnDescriptorID colDesc_Deadline_ID = IssueTableColumnDescriptorFactory.createColumnDescriptorID(organisationID, prefix, Issue.FieldName.deadlineTimestamp);
		columnDescriptors.add(IssueTableColumnDescriptorFactory.fillColumnDescriptorInfos(new ColumnDescriptor(colDesc_Deadline_ID), Issue.FieldName.deadlineTimestamp, propertiesBundle));
//		ColumnDescriptor colDesc_Deadline = new ColumnDescriptor(colDesc_Deadline_ID);
//		colDesc_Deadline.getColumnName().readFromMultiLanguagePropertiesBundle(propertiesBundle, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.deadline.text");
//		colDesc_Deadline.setDescriptorInfos(20, ColumnContentProperty.TEXT_ONLY, CollectionUtil.createHashSet(Issue.FieldName.deadlineTimestamp),
//				new String[] {FetchPlan.DEFAULT});
//		columnDescriptors.add(colDesc_Deadline);
	}

	/**
	 * @return the list of {@link IColumnDescriptor}s for this IssueTableConfigModule.
	 */
	public List<? extends IColumnContentDescriptor> getColumnDescriptors() {
		return columnDescriptors;
	}

	/**
	 * @return the collated Set of (non-repeated) fetch-group elements from all the {@link ColumnDescriptor}s within.
	 */
	public String[] getAllColumnFetchGroups() {
		if (columnDescriptors == null)
			return null;

		Set<String> combinedFetchGroups = new HashSet<String>();
		for (ColumnDescriptor columnDescriptor : columnDescriptors)
			for (String fetchGroup : columnDescriptor.getFetchGroups())
				combinedFetchGroups.add(fetchGroup);

		String[] fetchGroups = CollectionUtil.collection2TypedArray(combinedFetchGroups, String.class);
		return fetchGroups;
	}
}
