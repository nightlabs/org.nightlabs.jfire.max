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
 * A DEFAULT {@link ConfigModule} detailing the instructions on how to display Issue-related items in the IssueTable.
 * Basically, this contains the {@link IColumnDescriptor}s from which building an IssueTable shall be
 * based upon.
 *
 * @author khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueTableConfigModule"
)
@FetchGroups({
	@FetchGroup(
		name=IssueTableConfigModule.FETCH_GROUP_COLUMNDESCRIPTORS,
		members=@Persistent(name="columnDescriptors"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueTableConfigModule extends ConfigModule implements IColumnConfiguration {
	private static final long serialVersionUID = 3769679411926708820L;

	public static final String FETCH_GROUP_COLUMNDESCRIPTORS = "IssueTableConfigModule.columnDescriptors";

	@Join
	@Persistent(
		table="JFireIssueTracking_IssueTableConfigModule_columnDescriptors",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<ColumnDescriptor> columnDescriptors;

	@Override
	public void init() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null!");

		// Die Defaultconfiguration des Tisches.
		columnDescriptors = getIssueTableColumnDescriptors(
				Issue.FieldName.issueID,
				Issue.FieldName.createTimestamp,
				Issue.FieldName.issueType,
				Issue.FieldName.subject,
				Issue.FieldName.description,
				Issue.FieldName.issueMarkers,
				Issue.FieldName.issueSeverityType,
				Issue.FieldName.issuePriority,
				Issue.FieldName.state
			);
	}


	/**
	 * Creates the columns with defalt variables filled in.
	 */
	public List<ColumnDescriptor> getIssueTableColumnDescriptors(String... fieldNames) {
		MultiLanguagePropertiesBundle propertiesBundle = new MultiLanguagePropertiesBundle("org.nightlabs.jfire.issue.resource.messages", IssueTableConfigModule.class.getClassLoader(), true);
		String organisationID = IDGenerator.getOrganisationID();

		List<ColumnDescriptor> columnDescs = new LinkedList<ColumnDescriptor>();
		for (String fieldName : fieldNames) {
			ColumnDescriptorID colDescID = ColumnDescriptorID.create(IDGenerator.nextID(ColumnDescriptor.class), organisationID);
			columnDescs.add(IssueTableColumnDescriptorFactory.fillColumnDescriptorInfos(new ColumnDescriptor(colDescID), fieldName, propertiesBundle));
		}

		return columnDescs;
	}



	// ------------------------------------------------------------------------ || -------------------------------------------->>
	// [Section] Implementation of the IColumnConfiguration.
	// ------------------------------------------------------------------------ || -------------------------------------------->>
	/**
	 * @return the list of {@link IColumnDescriptor}s for this IssueTableConfigModule.
	 */
	public List<? extends IColumnContentDescriptor> getColumnDescriptors() {
		return columnDescriptors;
	}

	public <T extends IColumnContentDescriptor> boolean removeColumnDescriptor(T columnDescriptor) {
		return columnDescriptors.remove(columnDescriptor);
	}

	public void setColumnDescriptors(List<ColumnDescriptor> columnDescriptors) {
		this.columnDescriptors = columnDescriptors;
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
