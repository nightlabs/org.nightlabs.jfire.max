/**
 * 
 */
package org.nightlabs.jfire.reporting.scripting.javaclass.prop;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Property;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.id.PropertyID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.TextStructField;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate;
import org.nightlabs.jfire.scripting.AbstractScriptExecutorJavaClassDelegate;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PropertySet
extends AbstractScriptExecutorJavaClassDelegate
implements ScriptExecutorJavaClassReportingDelegate
{
	
	/**
	 * Logger used by this class.
	 */
	private static final Logger logger = Logger.getLogger(PropertySet.class);

	public PropertySet() {
		super();
	}

	private JFSResultSetMetaData metaData;

	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
			IStruct struct = PersonStruct.getPersonStruct(getOrganisation(), pm);
			SortedMap<String, StructBlock> sortedBlocks = new TreeMap<String, StructBlock>();
			for (Iterator iter = struct.getStructBlocks().iterator(); iter.hasNext();) {			
				StructBlock structBlock = (StructBlock) iter.next();
				sortedBlocks.put(structBlock.getPrimaryKey(), structBlock);
			}
			for (Iterator iter = sortedBlocks.values().iterator(); iter.hasNext();) {			
				StructBlock structBlock = (StructBlock) iter.next();
				SortedMap<String, AbstractStructField> sortedFields = new TreeMap<String, AbstractStructField>();				
				for (Iterator iterator = structBlock.getStructFields().iterator(); iterator.hasNext();) {
					AbstractStructField structField = (AbstractStructField) iterator.next();
					sortedFields.put(structField.getPrimaryKey(), structField);
				}
				for (Iterator iterator = sortedFields.values().iterator(); iterator.hasNext();) {
					AbstractStructField structField = (AbstractStructField) iterator.next();
					if (structField instanceof TextStructField) {
						metaData.addColumn(structField.getStructFieldID(), DataType.STRING);
					}
				}			
			}
		}
		return metaData;
	}


	protected String getOrganisation() {
		return SecurityReflector.getUserDescriptor().getOrganisationID();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {		
		JFSResultSet resultSet = new JFSResultSet((JFSResultSetMetaData)getResultSetMetaData());
		PropertyID propertyID = (PropertyID) getParameterValue("propertyID");
		if (propertyID == null)
			throw new IllegalArgumentException("Parameter propertyID is not set.");
		
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		Set oldGroups = pm.getFetchPlan().getGroups();
		int oldFetchDepth = pm.getFetchPlan().getMaxFetchDepth();
		pm.getFetchPlan().setGroups(new String[] {FetchPlan.DEFAULT, Property.FETCH_GROUP_FULL_DATA});
		pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		
		Property property = (Property) pm.getObjectById(propertyID);
		logger.debug("Have property");
		
		IStruct struct = StructLocal.getStructLocal(property.getStructLocalLinkClass(), property.getStructLocalScope(), pm);
		property = (Property) pm.detachCopy(property);
		pm.getFetchPlan().setGroups(oldGroups);
		pm.getFetchPlan().setMaxFetchDepth(oldFetchDepth);
		logger.debug("Property detached");
		// have to detach, as explode might modify the person 
		struct.explodeProperty(property);
		List<String> textElements = new LinkedList<String>();
		SortedMap<String, StructBlock> sortedBlocks = new TreeMap<String, StructBlock>();
		for (Iterator iter = struct.getStructBlocks().iterator(); iter.hasNext();) {			
			StructBlock structBlock = (StructBlock) iter.next();
			sortedBlocks.put(structBlock.getPrimaryKey(), structBlock);
		}
		for (Iterator iter = sortedBlocks.values().iterator(); iter.hasNext();) {			
			StructBlock structBlock = (StructBlock) iter.next();
			SortedMap<String, AbstractStructField> sortedFields = new TreeMap<String, AbstractStructField>();
			for (Iterator iterator = structBlock.getStructFields().iterator(); iterator.hasNext();) {
				AbstractStructField structField = (AbstractStructField) iterator.next();
				sortedFields.put(structField.getPrimaryKey(), structField);
			}
			for (Iterator iterator = sortedFields.values().iterator(); iterator.hasNext();) {
				AbstractStructField structField = (AbstractStructField) iterator.next();
				if (structField instanceof TextStructField) {
					TextDataField field;
					try {
						field = (TextDataField) property.getDataField((StructFieldID)JDOHelper.getObjectId(structField));
					} catch (Exception e) {
						throw new ScriptException(e);
					}
					logger.debug("Adding field value: "+field.getText()+" for field: "+structField.getPrimaryKey());
					textElements.add(field.getText());
				}
			}			
		}
		resultSet.addRow(textElements.toArray());
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
	public ScriptExecutorJavaClass getScriptExecutorJavaClass() {
		return scriptExecutorJavaClass;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#setScriptExecutorJavaClass(org.nightlabs.jfire.scripting.ScriptExecutorJavaClass)
	 */
	public void setScriptExecutorJavaClass(ScriptExecutorJavaClass scriptExecutorJavaClass) {
		this.scriptExecutorJavaClass = scriptExecutorJavaClass;
	}

}
