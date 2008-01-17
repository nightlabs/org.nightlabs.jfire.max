/**
 * 
 */
package org.nightlabs.jfire.reporting.scripting.javaclass.prop;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.model.css.Property;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * A reporting data set script that gives access to the datafields of a {@link org.nightlabs.jfire.prop.PropertySet}.
 * Currently only {@link NumberDataField} and {@link II18nTextDataField} field types are supported.
 * The script takes one parameter:
 * <ul>
 *   <li><code>propertyID</code>: The {@link PropertyID} of the {@link Property} to access.</li>
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
	
	/**
	 * Logger used by this class.
	 */
	private static final Logger logger = Logger.getLogger(PropertySet.class);

	public static final String PARAMETER_NAME_PROPERTY_SET_ID = "propertySetID";
	
	public static final String PROPERTY_NAME_LINK_CLASS = "linkClass";
	public static final String PROPERTY_NAME_SCOPE = "scope";

	public PropertySet() {
		super();
	}

	private JFSResultSetMetaData metaData;

	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
			String linkClass = getJFSQueryPropertySet().getProperties().get(PROPERTY_NAME_LINK_CLASS);;
			if (linkClass == null || "".equals(linkClass)) {
				throw new IllegalArgumentException("Query property linkClass was not set.");
			}
			String scope = getJFSQueryPropertySet().getProperties().get(PROPERTY_NAME_SCOPE);
			if (scope == null || "".equals(scope)) {
				throw new IllegalArgumentException("Query property scope was not set.");
			}
			metaData.addColumn("DisplayName", DataType.STRING);
			IStruct struct = StructLocal.getStructLocal(linkClass, scope, pm);
			SortedMap<String, StructBlock> sortedBlocks = new TreeMap<String, StructBlock>();
			for (Iterator<StructBlock> iter = struct.getStructBlocks().iterator(); iter.hasNext();) {			
				StructBlock structBlock = iter.next();
				sortedBlocks.put(structBlock.getPrimaryKey(), structBlock);
			}
			for (Iterator<StructBlock> iter = sortedBlocks.values().iterator(); iter.hasNext();) {			
				StructBlock structBlock = iter.next();
				SortedMap<String, StructField> sortedFields = new TreeMap<String, StructField>();				
				for (Iterator<StructField> iterator = structBlock.getStructFields().iterator(); iterator.hasNext();) {
					StructField structField = iterator.next();
					sortedFields.put(structField.getPrimaryKey(), structField);
				}
				for (Iterator<StructField> iterator = sortedFields.values().iterator(); iterator.hasNext();) {
					StructField structField = iterator.next();
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
					else if (II18nTextDataField.class.isAssignableFrom(structField.getDataFieldClass()))
						metaData.addColumn(getColumnName(structField), DataType.STRING);
				}			
			}
		}
		return metaData;
	}

	
	protected String getColumnName(StructField structField) {
		return structField.getStructBlockID() + "_" + structField.getStructFieldID();
	}

	protected String getOrganisation() {
		return SecurityReflector.getUserDescriptor().getOrganisationID();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {		
		JFSResultSet resultSet = new JFSResultSet((JFSResultSetMetaData)getResultSetMetaData());
		PropertySetID propertySetID = (PropertySetID) getParameterValue(PARAMETER_NAME_PROPERTY_SET_ID);
		if (propertySetID == null)
			throw new IllegalArgumentException("Parameter " + PARAMETER_NAME_PROPERTY_SET_ID + " is not set.");
		
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		Set oldGroups = pm.getFetchPlan().getGroups();
		int oldFetchDepth = pm.getFetchPlan().getMaxFetchDepth();
		pm.getFetchPlan().setGroups(new String[] {FetchPlan.DEFAULT, org.nightlabs.jfire.prop.PropertySet.FETCH_GROUP_FULL_DATA});
		pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		
		org.nightlabs.jfire.prop.PropertySet propertySet = (org.nightlabs.jfire.prop.PropertySet) pm.getObjectById(propertySetID);
		logger.debug("Have propertySet");
		
		IStruct struct = StructLocal.getStructLocal(propertySet.getStructLocalLinkClass(), propertySet.getStructLocalScope(), pm);
		propertySet = pm.detachCopy(propertySet);
		pm.getFetchPlan().setGroups(oldGroups);
		pm.getFetchPlan().setMaxFetchDepth(oldFetchDepth);
		logger.debug("Property detached");
		// have to detach, as explode might modify the person 
		propertySet.inflate(struct);
		List<Object> elements = new LinkedList<Object>();
		Locale locale = JFireReportingHelper.getLocale();
		SortedMap<String, StructBlock> sortedBlocks = new TreeMap<String, StructBlock>();
		elements.add(propertySet.getDisplayName());
		for (Iterator<StructBlock> iter = struct.getStructBlocks().iterator(); iter.hasNext();) {			
			StructBlock structBlock = iter.next();
			sortedBlocks.put(structBlock.getPrimaryKey(), structBlock);
		}		
		for (Iterator<StructBlock> iter = sortedBlocks.values().iterator(); iter.hasNext();) {			
			StructBlock structBlock = iter.next();
			SortedMap<String, StructField> sortedFields = new TreeMap<String, StructField>();
			for (Iterator<StructField> iterator = structBlock.getStructFields().iterator(); iterator.hasNext();) {
				StructField structField = iterator.next();
				sortedFields.put(structField.getPrimaryKey(), structField);
			}
			for (Iterator<StructField> iterator = sortedFields.values().iterator(); iterator.hasNext();) {
				StructField structField = iterator.next();
				DataField field;
				try {
					field = propertySet.getDataField((StructFieldID)JDOHelper.getObjectId(structField));
				} catch (Exception e) {
					throw new ScriptException(e);
				}
				if (structField instanceof NumberStructField) {
					NumberStructField numberStructField = (NumberStructField) structField;
					if (numberStructField.isInteger())
						elements.add(((NumberDataField)field).getIntValue());					
					else
						elements.add(((NumberDataField)field).getDoubleValue());
				}
				else if (structField instanceof DateStructField) {
					elements.add(((DateDataField)field).getDate());
				}
				else if (II18nTextDataField.class.isAssignableFrom(structField.getDataFieldClass())) {
					if (field.isEmpty()) {
						elements.add("");
					} else {
						elements.add(((II18nTextDataField) field).getText(locale));
					}
				}
			}			
		}
		resultSet.addRow(elements.toArray());
		resultSet.init();
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doPrepare()
	 */
	public void doPrepare() throws ScriptException {
	}

	private ScriptExecutorJavaClass scriptExecutorJavaClass;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#getScriptExecutorJavaClass()
	 */
	@Override
	public ScriptExecutorJavaClass getScriptExecutorJavaClass() {
		return scriptExecutorJavaClass;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#setScriptExecutorJavaClass(org.nightlabs.jfire.scripting.ScriptExecutorJavaClass)
	 */
	@Override
	public void setScriptExecutorJavaClass(ScriptExecutorJavaClass scriptExecutorJavaClass) {
		this.scriptExecutorJavaClass = scriptExecutorJavaClass;
	}

}
