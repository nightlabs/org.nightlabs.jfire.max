package org.nightlabs.jfire.issue.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.i18n.MultiLanguagePropertiesBundle;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.issuemarker.IssueMarker;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.table.config.ColumnContentProperty;
import org.nightlabs.jfire.table.config.ColumnDescriptor;
import org.nightlabs.util.CollectionUtil;

/**
 * This factory contains ALL the information required to create and configure a user-defined
 * {@link ColumnDescriptor} for use in an IssueTable.
 *
 * @author khaireel at nightlabs dot de
 */
public final class IssueTableColumnDescriptorFactory {
	// Descriptor's "columnName".
	public static final Map<String, String> field2MultiLangPropBundleKey = new HashMap<String, String>();
	static {
		field2MultiLangPropBundleKey.put(Issue.FieldName.issueID, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.id.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.createTimestamp, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.date.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.issueType, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.type.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.subject, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.subject.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.description, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.description.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.issueMarkers, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.markers.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.issueSeverityType, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.severity.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.issuePriority, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.priority.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.state, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.state.text");
		field2MultiLangPropBundleKey.put(Issue.FieldName.deadlineTimestamp, "org.nightlabs.jfire.issue.config.IssueTableConfigModule.tableColumn.deadline.text");
	}

	// Descriptor's "columnWeight". Suggested DEFAULT values.
	public static final Map<String, Integer> field2ColumnWeight = new HashMap<String, Integer>();
	static {
		field2ColumnWeight.put(Issue.FieldName.issueID, 10);
		field2ColumnWeight.put(Issue.FieldName.createTimestamp, 20);
		field2ColumnWeight.put(Issue.FieldName.issueType, 20);
		field2ColumnWeight.put(Issue.FieldName.subject, 25);
		field2ColumnWeight.put(Issue.FieldName.description, 30);
		field2ColumnWeight.put(Issue.FieldName.issueMarkers, 10);
		field2ColumnWeight.put(Issue.FieldName.issueSeverityType, 15);
		field2ColumnWeight.put(Issue.FieldName.issuePriority, 15);
		field2ColumnWeight.put(Issue.FieldName.state, 15);
		field2ColumnWeight.put(Issue.FieldName.deadlineTimestamp, 20);
	}

	// Descriptor's "content-type".
	public static final Map<String, ColumnContentProperty> field2ColumnContentProperty = new HashMap<String, ColumnContentProperty>();
	static {
		field2ColumnContentProperty.put(Issue.FieldName.issueID, ColumnContentProperty.TEXT_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.createTimestamp, ColumnContentProperty.TEXT_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.issueType, ColumnContentProperty.TEXT_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.subject, ColumnContentProperty.TEXT_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.description, ColumnContentProperty.TEXT_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.issueMarkers, ColumnContentProperty.IMAGE_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.issueSeverityType, ColumnContentProperty.TEXT_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.issuePriority, ColumnContentProperty.TEXT_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.state, ColumnContentProperty.TEXT_ONLY);
		field2ColumnContentProperty.put(Issue.FieldName.deadlineTimestamp, ColumnContentProperty.TEXT_ONLY);
	}

	// Descriptor's "column-fetchGroups".
	public static final Map<String, String[]> field2ColumnFetchGroups = new HashMap<String, String[]>();
	static {
		field2ColumnFetchGroups.put(Issue.FieldName.issueID, new String[] {FetchPlan.DEFAULT});
		field2ColumnFetchGroups.put(Issue.FieldName.createTimestamp, new String[] {FetchPlan.DEFAULT});
		field2ColumnFetchGroups.put(Issue.FieldName.issueType, new String[] {FetchPlan.DEFAULT, Issue.FETCH_GROUP_ISSUE_TYPE, IssueType.FETCH_GROUP_NAME});
		field2ColumnFetchGroups.put(Issue.FieldName.subject, new String[] {FetchPlan.DEFAULT, Issue.FETCH_GROUP_SUBJECT});
		field2ColumnFetchGroups.put(Issue.FieldName.description, new String[] {FetchPlan.DEFAULT, Issue.FETCH_GROUP_DESCRIPTION});
		field2ColumnFetchGroups.put(Issue.FieldName.issueMarkers, new String[] {FetchPlan.DEFAULT,
				Issue.FETCH_GROUP_ISSUE_MARKERS,
				IssueMarker.FETCH_GROUP_NAME,
				IssueMarker.FETCH_GROUP_ICON_16X16_DATA});
		field2ColumnFetchGroups.put(Issue.FieldName.issueSeverityType, new String[] {FetchPlan.DEFAULT,
				Issue.FETCH_GROUP_ISSUE_SEVERITY_TYPE,
				IssueSeverityType.FETCH_GROUP_NAME});
		field2ColumnFetchGroups.put(Issue.FieldName.issuePriority, new String[] {FetchPlan.DEFAULT,
				Issue.FETCH_GROUP_ISSUE_PRIORITY,
				IssuePriority.FETCH_GROUP_NAME,});
		field2ColumnFetchGroups.put(Issue.FieldName.state, new String[] {FetchPlan.DEFAULT,
				Statable.FETCH_GROUP_STATE,
				Issue.FETCH_GROUP_ISSUE_LOCAL,
				StatableLocal.FETCH_GROUP_STATE,
				State.FETCH_GROUP_STATE_DEFINITION,
				StateDefinition.FETCH_GROUP_NAME});
		field2ColumnFetchGroups.put(Issue.FieldName.deadlineTimestamp, new String[] {FetchPlan.DEFAULT});
	}

	// Descriptor's "column fieldNames" (the current implementation has a one-to-one mapping, but in general, it is possible for one column to display more than one field)
	public static final Map<String, Set<String>> field2ColumnFieldNames = new HashMap<String, Set<String>>();
	static {
		field2ColumnFieldNames.put(Issue.FieldName.issueID, CollectionUtil.createHashSet(Issue.FieldName.issueID));
		field2ColumnFieldNames.put(Issue.FieldName.createTimestamp, CollectionUtil.createHashSet(Issue.FieldName.createTimestamp));
		field2ColumnFieldNames.put(Issue.FieldName.issueType, CollectionUtil.createHashSet(Issue.FieldName.issueType));
		field2ColumnFieldNames.put(Issue.FieldName.subject, CollectionUtil.createHashSet(Issue.FieldName.subject));
		field2ColumnFieldNames.put(Issue.FieldName.description, CollectionUtil.createHashSet(Issue.FieldName.description));
		field2ColumnFieldNames.put(Issue.FieldName.issueMarkers, CollectionUtil.createHashSet(Issue.FieldName.issueMarkers));
		field2ColumnFieldNames.put(Issue.FieldName.issueSeverityType, CollectionUtil.createHashSet(Issue.FieldName.issueSeverityType));
		field2ColumnFieldNames.put(Issue.FieldName.issuePriority, CollectionUtil.createHashSet(Issue.FieldName.issuePriority));
		field2ColumnFieldNames.put(Issue.FieldName.state, CollectionUtil.createHashSet(Issue.FieldName.state));
		field2ColumnFieldNames.put(Issue.FieldName.deadlineTimestamp, CollectionUtil.createHashSet(Issue.FieldName.deadlineTimestamp));
	}

////public static final ColumnDescriptorID ID_COLUMN_DESC = ColumnDescriptorID.create(columnDescriptionID, organisationID)
	//
//		public static ColumnDescriptorID createColumnDescriptorID(String organisationID, String prefix, String field)
//		{
//			return ColumnDescriptorID.create(IDGenerator.nextID(ColumnDescriptor.class), organisationID);
////			String columnDescriptorID = prefix + field;
////			return ColumnDescriptorID.create(columnDescriptorID, organisationID);
//		}


	/**
	 * Provided with an instantiated {@link ColumnDescriptor} and a valid {@link Issue} fieldName, this fills up the descriptor
	 * information of the given {@link ColumnDescriptor} with default values.
	 * @return the {@link ColumnDescriptor} whose internal variables are now filled with the default values (if the fieldName is recognised).
	 */
	public static ColumnDescriptor fillColumnDescriptorInfos(ColumnDescriptor columnDescriptor, String issueFieldName, MultiLanguagePropertiesBundle propsBundle) {
		if (field2ColumnFieldNames.containsKey(issueFieldName)) {
			columnDescriptor.getColumnName().readFromMultiLanguagePropertiesBundle(propsBundle, field2MultiLangPropBundleKey.get(issueFieldName));
			columnDescriptor.setDescriptorInfos(
					field2ColumnWeight.get(issueFieldName),
					field2ColumnContentProperty.get(issueFieldName),
					field2ColumnFieldNames.get(issueFieldName),
					field2ColumnFetchGroups.get(issueFieldName)
			);
		}

		return columnDescriptor;
	}

	/**
	 * Given an (ordered) Map of fieldNames to instantiated {@link ColumnDescriptor}s,
	 * we fill up the {@link ColumnDescriptor}s with the default information determined by this factory, based on its key fieldNames.
	 * @return the same map, but with the default variables in {@link ColumnDescriptor}s initialised.
	 */
	public static Map<String, ColumnDescriptor> fillColumnDescriptorInfos(Map<String, ColumnDescriptor> fieldName2ColumnDescriptors, MultiLanguagePropertiesBundle propsBundle) {
		for (String fieldName : fieldName2ColumnDescriptors.keySet()) {
			ColumnDescriptor colDesc = fieldName2ColumnDescriptors.get(fieldName);
			colDesc.getColumnName().readFromMultiLanguagePropertiesBundle(propsBundle, field2MultiLangPropBundleKey.get(fieldName));
			colDesc.setDescriptorInfos(field2ColumnWeight.get(fieldName), field2ColumnContentProperty.get(fieldName), field2ColumnFieldNames.get(fieldName), field2ColumnFetchGroups.get(fieldName));
		}

		return fieldName2ColumnDescriptors;
	}
}
