package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.base.security.integration.ldap.InetOrgPersonLDAPServerType;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPScriptSet;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.SambaLDAPServerType;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.base.security.integration.ldap.scripts.LDAPScriptUtil;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.exception.IllegalStructureModificationException;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.util.NLLocale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class contains all methods dealing LDAP attributes mapping on {@link Person} datafields. It has no internal state 
 * and is not intended to have any instances created (contains static methods only). It was created to move some code away
 * from {@link LDAPServer} class in order to keep it a bit smaller and simplier.</p>
 * 
 * <p>LDAP attributes are mapped on {@link Person} datafields according to {@link LDAPAttributeSyncPolicy} set to {@link LDAPServer}
 * which is processing synchronization. Possible values are: ALL, MANDATORY_ONLY and NONE. See {@link LDAPAttributeSyncPolicy} enum.</p>
 * 
 * <p>ALL means that all attributes from {@link LDAPServer}'s schema which are NOT mapped to {@link Person} datafields by {@link LDAPScriptSet}
 * will be mapped by this class (mapping means that needed {@link StructField}s will be created in {@link Person} {@link StructLocal}).
 * MANDATORY_ONLY means that only attributes which are mandatory in {@link LDAPServer}'s schema will be mapped.
 * NONE means that nothing will be mapped by this class and all the m apping is done inside {@link LDAPScriptSet} so system administrator
 * is fully responsible for setting it up.</p>
 * 
 * <p>There could be several {@link LDAPServer} insances existing and every one of them could have it's own {@link LDAPAttributeSyncPolicy}
 * configured. Since {@link Person} structure is common for everybody here's how it works with several {@link LDAPServer}s:
 * whenever {@link LDAPAttributeSyncPolicy} is changed on any {@link LDAPServer} we call structure modification in {@link #modifyPersonStructure(PersistenceManager, LDAPServer)}.
 * But before actual modification we query for other {@link LDAPServer} instances which have the same {@link LDAPAttributeSyncPolicy} or higher 
 * (ALL is "higher" than MANDATORY_ONLY etc.). If query returns any result we do not modify Person structure and assume that it was already
 * modified when policy was changed earlier for another {@link LDAPServer} instance. When sync policy is set to a "lower" value 
 * (i.e. NONE instead of ALL) we again query for another {@link LDAPServer}s which have higher sync policy value and therefore are 
 * interested in maintaining Person structure. If any result is returned by this query we do not proceed with structure modification 
 * (removing {@link StructField}s)</p>
 * 
 * <p>See issue 1975 for more details: https://www.jfire.org/modules/bugs/view.php?id=1975</p>
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class AttributeStructFieldSyncHelper {

	private static final Logger logger = LoggerFactory.getLogger(AttributeStructFieldSyncHelper.class);
	
	/**
	 * Possible values for {@link LDAPAttributeSyncPolicy}. See their description in {@link AttributeStructFieldSyncHelper}.
	 * 
	 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
	 *
	 */
	public enum LDAPAttributeSyncPolicy{
		
		ALL("all"),
		MANDATORY_ONLY("mandatory only"),
		NONE("none");
		
        private String stringValue;
        
        private LDAPAttributeSyncPolicy(String stringValue){
        	this.stringValue = stringValue;
        }
        
        public String stringValue(){
        	return stringValue;
        }
        
		/**
		 * Get possible {@link LDAPAttributeSyncPolicy}s names.
		 * 
		 * @return names of {@link LDAPAttributeSyncPolicy}s as {@link String}
		 */
        public static String[] getPossibleAttributeSyncPolicyValues(){
    		LDAPAttributeSyncPolicy[] possibleValues = LDAPAttributeSyncPolicy.values();
    		String[] names = new String[possibleValues.length];
    		for (int i = 0; i < possibleValues.length; i++) {
    			names[i] = possibleValues[i].stringValue();
    		}
    		return names;
    	}

        /**
         * Get {@link Enum} element by {@link String} value.
         * 
         * @param stringValue
         * @return {@link LDAPAttributeSyncPolicy} element
         */
    	public static LDAPAttributeSyncPolicy findAttributeSyncPolicyByStringValue(String stringValue){
    	    for(LDAPAttributeSyncPolicy v : LDAPAttributeSyncPolicy.values()){
    	        if (v.stringValue().equals(stringValue)){
    	            return v;
    	        }
    	    }
    	    return null;
    	}
	}

	/**
	 * Convinient container class which holds {@link StructFieldID}, {@link StructField} name and corresponding {@link DataField} class
	 * for an LDAP attribute which will be mapped to {@link Person} datafields. Actual descriptors are defined in {@link LDAPServer}'s 
	 * type classes (i.e. see {@link InetOrgPersonLDAPServerType} or {@link SambaLDAPServerType}).
	 * 
	 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
	 *
	 */
	public static class AttributeStructFieldDescriptor{
		
		private StructFieldID structFieldID;
		private String structFieldName;
		private Class<? extends StructField<? extends DataField>> structFieldClass;

		public AttributeStructFieldDescriptor(
				StructFieldID structFieldID,
				String structFieldName,
				Class<? extends StructField<? extends DataField>> structFieldClass) {
			this.structFieldID = structFieldID;
			this.structFieldName = structFieldName;
			this.structFieldClass = structFieldClass;
		}

		public Class<? extends StructField<? extends DataField>> getStructFieldClass() {
			return structFieldClass;
		}
		
		public StructFieldID getStructFieldID() {
			return structFieldID;
		}
		
		public String getStructFieldName() {
			return structFieldName;
		}
	}

	/**
	 * Modifies {@link Person} {@link StructLocal} according to {@link LDAPAttributeSyncPolicy} set to given {@link LDAPServer}.
	 * 
	 * @param pm {@link PersistenceManager} to be used
	 * @param ldapServer {@link LDAPServer} which currently performs synchronization
	 */
	public static synchronized void modifyPersonStructure(PersistenceManager pm, LDAPServer ldapServer){
		
		Struct personStruct = Struct.getStruct(Organisation.DEV_ORGANISATION_ID, Person.class, Person.STRUCT_SCOPE, pm);
		IStruct personStructLocal = StructLocal.getStructLocal(
				pm, Organisation.DEV_ORGANISATION_ID, Person.class.getName(), personStruct.getStructScope(), Person.STRUCT_LOCAL_SCOPE);
		try {
			
			LDAPAttributeSyncPolicy attributeSyncPolicy = ldapServer.getAttributeSyncPolicy();
			UserManagementSystemType<?> serverType = ldapServer.getType();
			if (!(serverType instanceof IAttributeStructFieldDescriptorProvider)){
				throw new IllegalStateException("LDAPServer should have a type instance of IAttributeStructFieldDescriptorProvider!");
			}
			IAttributeStructFieldDescriptorProvider descriptorProvider = (IAttributeStructFieldDescriptorProvider) serverType;
			
			if (LDAPAttributeSyncPolicy.ALL.equals(attributeSyncPolicy)){
				
				createPersonStructFieldsForAttributes(
						personStructLocal, descriptorProvider.getAttributeStructBlockID(), descriptorProvider.getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy.ALL));
				
			}else if (LDAPAttributeSyncPolicy.MANDATORY_ONLY.equals(attributeSyncPolicy)){
				
				Collection<LDAPServer> servers = LDAPServer.findLDAPServersByAttributeSyncPolicy(pm, serverType, LDAPAttributeSyncPolicy.ALL);
				if (servers.size() == 0){
					Collection<AttributeStructFieldDescriptor> descriptorsToRemove = new ArrayList<AttributeStructFieldDescriptor>(
							descriptorProvider.getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy.ALL));
					descriptorsToRemove.removeAll(descriptorProvider.getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy.MANDATORY_ONLY));
					removePersonStructFieldsForAttributes(pm, personStructLocal, descriptorProvider.getAttributeStructBlockID(), descriptorsToRemove, false);
				}
				servers = LDAPServer.findLDAPServersByAttributeSyncPolicy(pm, serverType, LDAPAttributeSyncPolicy.MANDATORY_ONLY);
				if (servers.size() <= 1){
					createPersonStructFieldsForAttributes(
							personStructLocal, descriptorProvider.getAttributeStructBlockID(), descriptorProvider.getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy.MANDATORY_ONLY));
				}
				
			}else if (LDAPAttributeSyncPolicy.NONE.equals(attributeSyncPolicy)){
				
				Collection<LDAPServer> servers = LDAPServer.findLDAPServersByAttributeSyncPolicy(pm, serverType, LDAPAttributeSyncPolicy.ALL);
				if (servers.size() == 0){
					Collection<AttributeStructFieldDescriptor> descriptorsToRemove = new ArrayList<AttributeStructFieldDescriptor>(
							descriptorProvider.getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy.ALL));
					descriptorsToRemove.removeAll(descriptorProvider.getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy.MANDATORY_ONLY));
					removePersonStructFieldsForAttributes(
							pm, personStructLocal, descriptorProvider.getAttributeStructBlockID(), descriptorProvider.getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy.ALL), false);
				}
				servers = LDAPServer.findLDAPServersByAttributeSyncPolicy(pm, serverType, LDAPAttributeSyncPolicy.MANDATORY_ONLY);
				if (servers.size() == 0){
					removePersonStructFieldsForAttributes(
							pm, personStructLocal, descriptorProvider.getAttributeStructBlockID(), descriptorProvider.getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy.MANDATORY_ONLY), true);
				}
				
			} else {
				throw new RuntimeException(
						"Specified LDAPAttributeSyncPolicy is either null or unknown! String value: " + attributeSyncPolicy.stringValue());
			}
		} catch (Exception e) {
			throw new RuntimeException("Can't modify Person structure!", e);
		}
	}

	/**
	 * Returns {@link LDAPAttributeSet} with attributes mapped from {@link Person} datafields according to given {@link Collection} of 
	 * {@link AttributeStructFieldDescriptor}s.
	 * 
	 * @param jfireObject JFire object being synchronized (either {@link User} or {@link Person})
	 * @param attributeDescriptors Descriptors of attributes which should be taken for synchronization
	 * @return {@link LDAPAttributeSet} with attributes to be synchronized to LDAP directory
	 */
	public static LDAPAttributeSet getAttributesForSync(Object jfireObject, Collection<AttributeStructFieldDescriptor> attributeDescriptors){
		Person person = null;
		if (jfireObject instanceof Person){
			person = (Person) jfireObject;
		}else if (jfireObject instanceof User){
			person = ((User) jfireObject).getPerson();
		}
		LDAPAttributeSet attributes = new LDAPAttributeSet();
		if (person != null){
			for (AttributeStructFieldDescriptor descriptor : attributeDescriptors){
				Object dataFieldValue = getPersonDataFieldValue(person, descriptor.getStructFieldID());
				Object attributeValue = null;
				if (dataFieldValue instanceof String
						|| dataFieldValue instanceof byte[]){
					attributeValue = dataFieldValue;
				}else if (dataFieldValue instanceof Number){
					attributeValue = String.valueOf(dataFieldValue);
				}else if (dataFieldValue != null){
					attributeValue = dataFieldValue.toString();
				}
				attributes.createAttribute(descriptor.getStructFieldID().structFieldID, attributeValue);
			}
		}
		return attributes;
	}

	/**
	 * Sets mapped attributes for to given {@link Person} datafields according to given {@link AttributeStructFieldDescriptor}s.
	 * 
	 * @param pm {@link PersistenceManager} to be used
	 * @param person {@link Person} to set datafields to
	 * @param structBlockID {@link StructBlockID} which contains corresponding {@link StructField}s
	 * @param attributes {@link LDAPAttributeSet} with attributes from LDAP directory
	 * @param attributeDescriptors descriptors to be used for mapping
	 * @throws LDAPSyncException
	 */
	public static void setPersonDataForAttributes(
			PersistenceManager pm, Person person, StructBlockID structBlockID, 
			LDAPAttributeSet attributes, Collection<AttributeStructFieldDescriptor> attributeDescriptors) throws LDAPSyncException{

		StructLocalID structLocalId = person.getStructLocalObjectID();
		IStruct personStruct = StructLocal.getStructLocal(
				pm, Organisation.DEV_ORGANISATION_ID, structLocalId.linkClass, structLocalId.structScope, structLocalId.structLocalScope
		);
		createPersonStructFieldsForAttributes(personStruct, structBlockID, attributeDescriptors);

		if (attributeDescriptors.size() > 0){
			person.inflate(personStruct);
			for (AttributeStructFieldDescriptor descriptor : attributeDescriptors){
				try {
					DataField dataField = person.getDataField(descriptor.getStructFieldID());
					Object attributeValue = LDAPScriptUtil.getAttributeValue(attributes, descriptor.getStructFieldID().structFieldID, null);
					if (dataField.supportsInputType(attributeValue.getClass())){
						dataField.setData(attributeValue);
					}else if (attributeValue instanceof String){
						// check for concrete DataField types which do not support String input
						if (dataField instanceof NumberDataField){
							Number value = NumberFormat.getNumberInstance().parse((String) attributeValue);
							dataField.setData(value);
						}else if (dataField instanceof DateDataField){
							Date value = SimpleDateFormat.getInstance().parse((String) attributeValue);
							dataField.setData(value);
						}else{
							logger.warn(
									String.format(
											"Value can't be set for datafield %s because it's type is not determined, value string: %s", dataField.getStructFieldIDObj().toString(), attributeValue));
						}
					}else{
						// byte[] values should be processed earlier
						logger.warn(
								String.format(
										"Value can't be set for datafield %s because it's type is not determined, value string: %s", dataField.getStructFieldIDObj().toString(), attributeValue));
					}
				} catch (Exception e) {
					throw new LDAPSyncException("Exception while setting Person datafields!", e);
				}
			}
			person.deflate();
			pm.makePersistent(person);
		}
	}

	private static void createPersonStructFieldsForAttributes(
			IStruct personStruct, StructBlockID attributesStructBlockID, Collection<AttributeStructFieldDescriptor> attributeDescriptors){

		StructBlock structBlock = createStructBlock(personStruct, attributesStructBlockID);
		
		for (AttributeStructFieldDescriptor descriptor : attributeDescriptors){
			createStructField(structBlock, descriptor.getStructFieldID(), descriptor.getStructFieldClass(), descriptor.getStructFieldName());
		}
	}

	private static StructBlock createStructBlock(IStruct personStruct, StructBlockID structBlockID){
		StructBlock structBlock = null;
		try {
			structBlock = personStruct.getStructBlock(structBlockID);
		} catch (StructBlockNotFoundException x) {
			structBlock = new StructBlock(personStruct, structBlockID);
			try {
				personStruct.addStructBlock(structBlock);
			} catch (Exception e) {  // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		return structBlock;
	}
	
	private static void createStructField(StructBlock structBlock, StructFieldID structFieldID, Class<? extends StructField<? extends DataField>> structFieldClass, String structName){
		try {
			structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			try{
				StructField<? extends DataField> field = structFieldClass.getConstructor(
						StructBlock.class, StructFieldID.class).newInstance(structBlock, structFieldID);
				field.getName().setText(NLLocale.getDefault(), structName);
				structBlock.addStructField(field);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void removePersonStructFieldsForAttributes(
			PersistenceManager pm, IStruct personStruct, StructBlockID attributesStructBlockID, 
			Collection<AttributeStructFieldDescriptor> attributeDescriptors, boolean removeStructBlock) throws IllegalStructureModificationException{
		
		// select all DataFieldsIDs to be removed
		Query q1 = pm.newQuery(DataField.class);
		q1.declareParameters("java.util.Collection paramStructFieldsOrganisationIDs, java.util.Collection paramStructFieldsIDs, String paramStructBlockOrganisationID, String paramStructBlockID");
		q1.setFilter("this.structBlockOrganisationID == paramStructBlockOrganisationID && this.structBlockID == paramStructBlockID " +
				"&& paramStructFieldsOrganisationIDs.contains(this.structFieldOrganisationID) " +
				"&& paramStructFieldsIDs.contains(this.structFieldID)");
		q1.setResult("this");
				
		Collection<String> structFieldsIDs = new HashSet<String>();
		Collection<String> structFieldsOrganisationIDs = new HashSet<String>();
		for (AttributeStructFieldDescriptor descriptor : attributeDescriptors){
			structFieldsIDs.add(descriptor.getStructFieldID().structFieldID);
			structFieldsOrganisationIDs.add(descriptor.getStructFieldID().structFieldOrganisationID);
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramStructFieldsOrganisationIDs", structFieldsOrganisationIDs);
		params.put("paramStructFieldsIDs", structFieldsIDs);
		params.put("paramStructBlockOrganisationID", attributesStructBlockID.structBlockOrganisationID);
		params.put("paramStructBlockID", attributesStructBlockID.structBlockID);
		
		Collection<DataField> dataFieldsFound = (Collection<DataField>) q1.executeWithMap(params);
		
		// remove found DataFields from Persons using found DataFieldIDs
		// FIXME: probably there will be a beter idea how to do that without calling to internal remove?
		for (DataField dataField : dataFieldsFound){
			Query q2 = pm.newQuery(Person.class);
			q2.setFilter("this.dataFields.contains(:dataField)");
			q2.setResult("this");
			Collection<Person> personsFound = (Collection<Person>) q2.execute(dataField);
			for (Person person : personsFound){
				person.internalRemoveDataFieldFromPersistentCollection(dataField);
			}
			q2.closeAll();
		}
		
		// remove DataField objects itself
		pm.deletePersistentAll(dataFieldsFound);
		
		// load StructBlock
		StructBlock structBlock = null;
		try {
			structBlock = personStruct.getStructBlock(attributesStructBlockID);
		} catch (StructBlockNotFoundException x) {
			try{
				structBlock = (StructBlock) pm.getObjectById(attributesStructBlockID);
			}catch(JDOObjectNotFoundException e){
				// do nothing, StructBlock not existing, return
				return;
			}
		}
		if (structBlock == null){
			return;
		}
		
		// remove StructFields from StructBlock
		for (AttributeStructFieldDescriptor descriptor : attributeDescriptors){
			StructField<? extends DataField> field = null;
			try{
				field = structBlock.getStructField(descriptor.getStructFieldID());
			}catch(StructFieldNotFoundException e){
				// do nothing, block does not contain this field
			}
			if (field != null){
				structBlock.removeStructField(field);
			}
		}
		
		// remove StructFields itself
		Query q3 = pm.newQuery(StructField.class);
		q3.declareParameters("java.util.Collection paramStructFieldsOrganisationIDs, java.util.Collection paramStructFieldsIDs, String paramStructBlockOrganisationID, String paramStructBlockID");
		q3.setFilter("this.structBlockOrganisationID == paramStructBlockOrganisationID && this.structBlockID == paramStructBlockID " +
				"&& paramStructFieldsOrganisationIDs.contains(this.structFieldOrganisationID) " +
				"&& paramStructFieldsIDs.contains(this.structFieldID)");
		params = new HashMap<String, Object>();
		params.put("paramStructFieldsOrganisationIDs", structFieldsOrganisationIDs);
		params.put("paramStructFieldsIDs", structFieldsIDs);
		params.put("paramStructBlockOrganisationID", attributesStructBlockID.structBlockOrganisationID);
		params.put("paramStructBlockID", attributesStructBlockID.structBlockID);
		q3.deletePersistentAll(params);
		
		q1.closeAll();
		q3.closeAll();

		// remove StructBlock if needed
		if (removeStructBlock){
			try {
				personStruct.getStructBlock(structBlock.getStructBlockIDObj());
				personStruct.removeStructBlock(structBlock);
			} catch (StructBlockNotFoundException x) {
				// do nothing, StructBlock is not in the person struct
			}
			pm.deletePersistent(structBlock);
		}
	}
	
	private static Object getPersonDataFieldValue(Person person, StructFieldID fieldID){
		return person.getPersistentDataFieldByIndex(fieldID, 0)!=null?person.getPersistentDataFieldByIndex(fieldID, 0).getData():null;	
	}

}
