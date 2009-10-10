/**
 *
 */
package org.nightlabs.jfire.reporting.scripting.javaclass.prop;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySetMetaData.Entry;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * A reporting data set script that gives access to the datafields of a {@link org.nightlabs.jfire.prop.PropertySet}.
 * Currently only {@link NumberDataField}, {@link II18nTextDataField} and {@link ImageDataField} field types are supported.
 * <p>
 * <ul>
 *   <li>For {@link NumberDataField}s columns of either {@link DataType#INTEGER} or {@link DataType#DOUBLE} will be added,
 *       depending on how the corresponding {@link NumberStructField} is configured</li>. The column value will then
 *       be either {@link Integer} or {@link Double}.
 *   </li>
 *   <li>For {@link II18nTextDataField}s columns of either {@link DataType#STRING} will be added. The column value
 *       will be translated using the {@link Locale} from {@link JFireReportingHelper#getLocale()}.
 *   </li>
 *   <li>For {@link ImageDataField}s columns of either {@link DataType#STRING} will be added. The column value
 *       will the String representation of the {@link ImageDataField}s id-object, or the empty string if no
 *       image was was stored for that field. The image URLs can then be obtained using
 *       {@link PropertySetReportingHelper#getImageURL(String)} passing column value.
 *   </li>
 * </ul>
 * </p>
 * The script takes one parameter:
 * <ul>
 *   <li><code>propertySetID</code>: The {@link PropertySetID} of the {@link PropertySet} to access.</li>
 * </ul>
 * <p>
 * At design-time the script does not know for which linkClass and scope the metad-data should be created,
 * that's why the script requires two query-properties, that are available before the query is prepared.
 * <ul>
 *   <li><code>linkClass</code>: The linkClass of the structure</li>
 *   <li><code>scope</code>: The scope of the structure</li>
 * </ul>
 * </p>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PropertySet
extends AbstractJFSScriptExecutorDelegate
{

	private static final Logger logger = Logger.getLogger(PropertySet.class);

	public static final String PARAMETER_NAME_PROPERTY_SET_ID = "propertySetID";

	public static final String PROPERTY_NAME_ORGANISATION_ID = "organisationID"; // defaults to dev-org, if not specified - TODO make it required later?! Provide possibility to reference local organisation by variable?! @Bieber: Please, think about it. Marco.
	public static final String PROPERTY_NAME_LINK_CLASS = "linkClass";
	public static final String PROPERTY_NAME_STRUCT_SCOPE = "structScope";
	public static final String PROPERTY_NAME_STRUCT_LOCAL_SCOPE = "structLocalScope";

	public PropertySet() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate#getJFSQueryPropertySetMetaData()
	 */
	@Override
	public IJFSQueryPropertySetMetaData getJFSQueryPropertySetMetaData() {
		JFSQueryPropertySetMetaData metaData = new JFSQueryPropertySetMetaData();
		metaData.addEntry(new Entry(PROPERTY_NAME_ORGANISATION_ID, false)); // TODO see above at declaration of PROPERTY_NAME_ORGANISATION_ID
		metaData.addEntry(new Entry(PROPERTY_NAME_LINK_CLASS, true));
		metaData.addEntry(new Entry(PROPERTY_NAME_STRUCT_SCOPE, false));
		metaData.addEntry(new Entry(PROPERTY_NAME_STRUCT_LOCAL_SCOPE, false));
		return metaData;
	}

	private JFSResultSetMetaData metaData;

	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			IStruct struct = getStruct();
			metaData.addColumn("DisplayName", DataType.STRING);
			for (Iterator<StructBlock> iter = getSortedBlocks(struct).values().iterator(); iter.hasNext();) {
				StructBlock structBlock = iter.next();
				SortedMap<String, StructField<? extends DataField>> sortedFields = new TreeMap<String, StructField<? extends DataField>>();
				for (Iterator<StructField<? extends DataField>> iterator = structBlock.getStructFields().iterator(); iterator.hasNext();) {
					StructField<? extends DataField> structField = iterator.next();
					sortedFields.put(structField.getPrimaryKey(), structField);
				}
				for (Iterator<StructField<? extends DataField>> iterator = sortedFields.values().iterator(); iterator.hasNext();) {
					StructField<? extends DataField> structField = iterator.next();
					if (structField instanceof NumberStructField) {
						NumberStructField numberStructField = (NumberStructField) structField;
						if (numberStructField.isInteger())
							metaData.addColumn(getColumnName(structField), DataType.INTEGER);
						else
							metaData.addColumn(getColumnName(structField), DataType.DOUBLE);
					}
					else if (structField instanceof DateStructField) {
						metaData.addColumn(getColumnName(structField), DataType.DATE);
					}
					else if (structField instanceof SelectionStructField) {
						metaData.addColumn(getColumnName(structField) + "_structFieldValueID", DataType.STRING);
						metaData.addColumn(getColumnName(structField) + "_text", DataType.STRING);
						metaData.addColumn(getColumnName(structField), DataType.STRING); // TODO deprecated - for downward compatibility only - should be removed for JFire 1.2
					}
					else if (II18nTextDataField.class.isAssignableFrom(structField.getDataFieldClass())) {
						metaData.addColumn(getColumnName(structField), DataType.STRING);
					}
					else if (ImageDataField.class.isAssignableFrom(structField.getDataFieldClass())) {
						metaData.addColumn(getColumnName(structField), DataType.BLOB);
					}
				}
			}
		}
		return metaData;
	}

	/**
	 * @return The {@link StructLocal} based on the linkClass, structScope and structLocalScope
	 *         set in the {@link JFSQueryPropertySet} of this delegate.
	 */
	protected IStruct getStruct() {
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		String organisationID = getJFSQueryPropertySet().getProperties().get(PROPERTY_NAME_ORGANISATION_ID);
		if (organisationID == null || "".equals(organisationID))
			organisationID = Organisation.DEV_ORGANISATION_ID; // TODO see above at PROPERTY_NAME_ORGANISATION_ID

		String linkClass = getJFSQueryPropertySet().getProperties().get(PROPERTY_NAME_LINK_CLASS);
		if (linkClass == null || "".equals(linkClass)) {
			throw new IllegalArgumentException("Query property linkClass was not set.");
		}
		String structScope = getJFSQueryPropertySet().getProperties().get(PROPERTY_NAME_STRUCT_SCOPE);
		if (structScope == null || "".equals(structScope)) {
			logger.debug("Query property " + PROPERTY_NAME_STRUCT_SCOPE + " was not set, using '" + Struct.DEFAULT_SCOPE + "' instead");
			structScope = Struct.DEFAULT_SCOPE;
		}
		String structLocalScope = getJFSQueryPropertySet().getProperties().get(PROPERTY_NAME_STRUCT_SCOPE);
		if (structLocalScope == null || "".equals(structLocalScope)) {
			logger.debug("Query property " + PROPERTY_NAME_STRUCT_LOCAL_SCOPE + " was not set, using '" + StructLocal.DEFAULT_SCOPE + "' instead");
			structLocalScope = StructLocal.DEFAULT_SCOPE;
		}
		return StructLocal.getStructLocal(pm, organisationID, linkClass, structScope, structLocalScope);
	}

	protected SortedMap<String, StructBlock> getSortedBlocks(IStruct struct) {
		SortedMap<String, StructBlock> sortedBlocks = new TreeMap<String, StructBlock>();
		for (Iterator<StructBlock> iter = struct.getStructBlocks().iterator(); iter.hasNext();) {
			StructBlock structBlock = iter.next();
			sortedBlocks.put(structBlock.getPrimaryKey(), structBlock);
		}
		return sortedBlocks;
	}

	protected String getColumnName(StructField<? extends DataField> structField) {
		return structField.getStructBlockID() + "_" + structField.getStructFieldID();
	}

	protected String getOrganisation() { // TODO shouldn't this be named "getOrganisationID()" and in what relationship does it stand to PROPERTY_NAME_ORGANISATION_ID (which I just added)? Marco.
		return SecurityReflector.getUserDescriptor().getOrganisationID();
	}

	protected org.nightlabs.jfire.prop.PropertySet getPropertySet() {
		PropertySetID propertySetID = (PropertySetID) getParameterValue(PARAMETER_NAME_PROPERTY_SET_ID);
		if (propertySetID == null)
			throw new IllegalArgumentException("Parameter " + PARAMETER_NAME_PROPERTY_SET_ID + " is not set.");

		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		org.nightlabs.jfire.prop.PropertySet propertySet = (org.nightlabs.jfire.prop.PropertySet) pm.getObjectById(propertySetID);
		return propertySet;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		JFSResultSet resultSet = new JFSResultSet((JFSResultSetMetaData)getResultSetMetaData());

		org.nightlabs.jfire.prop.PropertySet propertySet = getPropertySet();

		IStruct struct = StructLocal.getStructLocal(
				getPersistenceManager(),
				propertySet.getStructOrganisationID(), // TODO this should be the one specified above (see getStruct())!!! This applies IMHO to the other params, too. @Bieber: We have to talk about this again! Marco.
				propertySet.getStructLinkClass(),
				propertySet.getStructScope(), propertySet.getStructLocalScope());

		List<Object> elements = new LinkedList<Object>();
		Locale locale = JFireReportingHelper.getLocale();
		elements.add(propertySet.getDisplayName());
		for (Iterator<StructBlock> iter = getSortedBlocks(struct).values().iterator(); iter.hasNext();) {
			StructBlock structBlock = iter.next();
			SortedMap<String, StructField<? extends DataField>> sortedFields = new TreeMap<String, StructField<? extends DataField>>();
			for (Iterator<StructField<? extends DataField>> iterator = structBlock.getStructFields().iterator(); iterator.hasNext();) {
				StructField<? extends DataField> structField = iterator.next();
				sortedFields.put(structField.getPrimaryKey(), structField);
			}
			for (Iterator<StructField<? extends DataField>> iterator = sortedFields.values().iterator(); iterator.hasNext();) {
				StructField<? extends DataField> structField = iterator.next();
				DataField field;
				try {
					field = propertySet.getPersistentDataFieldByIndex((StructFieldID)JDOHelper.getObjectId(structField), 0);
				} catch (Exception e) {
					throw new ScriptException(e);
				}
				if (structField instanceof NumberStructField) {
					NumberStructField numberStructField = (NumberStructField) structField;
					if (numberStructField.isInteger())
						elements.add(field != null ? ((NumberDataField)field).getIntValue() : null);
					else
						elements.add(field != null ? ((NumberDataField)field).getDoubleValue() : null);
				}
				else if (structField instanceof DateStructField) {
					elements.add(field != null ? ((DateDataField)field).getDate() : null);
				}
				else if (structField instanceof SelectionStructField) {
					if (field != null) {
						elements.add(((SelectionDataField) field).getStructFieldValueID());
						elements.add(((II18nTextDataField) field).getI18nText().getText(locale));
						elements.add(((II18nTextDataField) field).getI18nText().getText(locale)); // TODO deprecated - should be removed for JFire 1.2 - see above in getResultSetMetaData()
					} else {
						elements.add("");
						elements.add("");
						elements.add("");
					}
				}
				else if (II18nTextDataField.class.isAssignableFrom(structField.getDataFieldClass())) {
					if (field != null) {
						elements.add(((II18nTextDataField) field).getI18nText().getText(locale));
					} else {
						elements.add("");
					}
				} else if (ImageDataField.class.isAssignableFrom(structField.getDataFieldClass())) {
					if (field != null && !field.isEmpty()) {
						elements.add(((ImageDataField)field).getPlainContent());
					} else {
						elements.add(null);
					}
				}
			}
		}
		resultSet.addRow(elements.toArray());
		resultSet.init();
		return resultSet;
	}
}