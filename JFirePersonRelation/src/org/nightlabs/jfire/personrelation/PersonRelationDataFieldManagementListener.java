/**
 * 
 */
package org.nightlabs.jfire.personrelation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;

import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.PersonRelationType.PredefinedRelationTypes;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Helper class that will install JDO-lifecycle-listeners that will monitor
 * created/deleted PersonRelations and changed DataFields in order to copy the
 * values of certain DataFields from the fromPerson of PersonRelations to the
 * toPerson.
 * <p>
 * This class is useful if you want to auto-manage values of the toPerson based
 * on the PersonRelation. For example an instance of this listener is installed
 * to monitor {@link PredefinedRelationTypes#employing} and that will copy the
 * PERSONAL_DATA_COMPANY datafield to the employee.
 * </p>
 * <p>
 * A listener is created with the id of the PersonRelationType it should monitor
 * an with a list of StructFieldIDs that should be copied to the toPerson.
 * </p>
 * 
 * @author abieber
 */
public class PersonRelationDataFieldManagementListener {

	private static Logger logger = LoggerFactory.getLogger(PersonRelationDataFieldManagementListener.class);
	
	private PersonRelationTypeID personRelationTypeID;
	private Collection<StructFieldID> structFieldIDs;
	private boolean needsDisplayNameAdjustment = true;

	private InstanceLifecycleListener createPersonRelationListener = new CreateLifecycleListener() {
		@Override
		public void postCreate(InstanceLifecycleEvent event) {
			Object source = event.getSource();
			if (source instanceof PersonRelation) {
				PersonRelation personRelation = (PersonRelation) source;
				PersonRelationTypeID typeID = (PersonRelationTypeID) JDOHelper.getObjectId(personRelation.getPersonRelationType());
				if (personRelationTypeID.equals(typeID)) {
					if (logger.isDebugEnabled()) {
						logger.debug(getClassName() + " Caught create-event for PersonRelation of type {}.", typeID);
					}
					Collection<PersonRelation> relations = filterPersonRelations(null, Collections.singleton(personRelation));
					if (logger.isTraceEnabled()) {
						logger.trace(getClassName() + " Have {} PersonRelations after filtering.", relations != null ? relations.size() : 0);
					}
					if (relations != null && relations.size() == 1) {
						if (logger.isTraceEnabled()) {
							logger.trace(getClassName() + " Will adjust to-person StructFields for PersonRelation of type {}. StructFields to adjust {}", typeID, structFieldsStr(structFieldIDs));
						}
						// A new PersonRelation of our monitored type was created
						// We get the to-person and set the managed-By flag for all dataFields
						adjustToPersonFields(personRelation, null);
					}
				}
			}
		}
	};

	private DeleteLifecycleListener deletePersonRelationListener = new DeleteLifecycleListener() {

		@Override
		public void postDelete(InstanceLifecycleEvent arg0) {
			// Nothing to do here
		}

		@Override
		public void preDelete(InstanceLifecycleEvent event) {
			Object source = event.getSource();
			if (source instanceof PersonRelation) {
				PersonRelation personRelation = (PersonRelation) source;
				PersonRelationTypeID typeID = (PersonRelationTypeID) JDOHelper.getObjectId(personRelation.getPersonRelationType());
				if (personRelationTypeID.equals(typeID)) {
					if (logger.isDebugEnabled()) {
						logger.debug(getClassName() + " Caught delete-event for PersonRelation of type {}.", typeID);
					}
					Collection<PersonRelation> relations = filterPersonRelations(null, Collections.singleton(personRelation));
					if (logger.isTraceEnabled()) {
						logger.trace(getClassName() + " Have {} PersonRelations after filtering.", relations != null ? relations.size() : 0);
					}
					if (relations != null && relations.size() == 1) {
						// A PersonRelation of our monitored type is about to be deleted, we unhook the managedBy of the to-persons datafields
						removeToPersonFieldsManagedBy(personRelation);
					}
				}
			}
		}
	};

	private DirtyLifecycleListener dirtyDataFieldListener = new DirtyLifecycleListener() {

		@Override
		public void preDirty(InstanceLifecycleEvent event) {
		}

		@Override
		public void postDirty(InstanceLifecycleEvent event) {
			Object source = event.getSource();
			if (source instanceof DataField) {
				PersistenceManager pm = JDOHelper.getPersistenceManager(source);
				DataField dataField = (DataField) source;
				adjustFieldsForChangedDataField(pm, dataField);
			}
		}
	};

	// The attachListener seems to be obsolete, as the dirty-listener ist triggered on attach as well
//	private AttachLifecycleListener attachDataFieldListener = new AttachLifecycleListener() {
//		@Override
//		public void postAttach(InstanceLifecycleEvent event) {
//			Object source = event.getPersistentInstance();
//			if (source instanceof DataField) {
//				PersistenceManager pm = JDOHelper.getPersistenceManager(source);
//				DataField dataField = (DataField) source;
//				adjustFieldsForChangedDataField(pm, dataField);
//			}
//		}
//		@Override
//		public void preAttach(InstanceLifecycleEvent event) {
//			// We do our work in postAttach
//		}
//	};

	private void removeToPersonFieldsManagedBy(PersonRelation personRelation) {
		if (logger.isDebugEnabled()) {
			logger.debug(getClassName() + " Unhook managedBy for the following fields of {}. Fields to unhook {}", propStr(personRelation.getTo()), structFieldsStr(structFieldIDs));
		}
		for (StructFieldID structFieldID : structFieldIDs) {
			DataField persistentDataField = retrieveToPersonDataField(personRelation, structFieldID, false);
			if (persistentDataField != null) {
				persistentDataField.setManagedBy(null);
			}
		}
	}

	private void adjustToPersonFields(PersonRelation personRelation, StructFieldID structFieldIDToAdjust) {
		Collection<StructFieldID> fieldsToAdjust = structFieldIDToAdjust != null ? Collections.singleton(structFieldIDToAdjust) : structFieldIDs;
		if (logger.isDebugEnabled()) {
			logger.debug(getClassName() + " Adjusting {} for {} caused by change in {}", new Object[] {structFieldsStr(fieldsToAdjust), propStr(personRelation.getTo()), propStr(personRelation.getFrom())});
		}
		for (StructFieldID structFieldID : fieldsToAdjust) {
			if (logger.isTraceEnabled()) {
				logger.trace(getClassName() + " Adjusting {} for {} caused by change in {}", new Object[] {structFieldsStr(structFieldID), propStr(personRelation.getTo()), propStr(personRelation.getFrom())});
			}
			
			DataField fromPersonDataField = retrieveFromPersonDataField(personRelation, structFieldID);
			if (fromPersonDataField == null) {
				logger.error(this + " did return null in retrieveFromPersonDataField. Will abort adjustToPersonFields for " + structFieldID + ".", new NullPointerException());
				continue;
			}
			
			DataField toPersonDataField = retrieveToPersonDataField(personRelation, structFieldID, true);
			if (toPersonDataField == null) {
				logger.error(this + " did return null in retrieveToPersonDataField. Will abort adjustToPersonFields for " + structFieldID + ".", new NullPointerException());
				continue;
			}
			
			toPersonDataField.setManagedBy(PersonRelation.class.getSimpleName() + "-" + personRelationTypeID.personRelationTypeID);
			toPersonDataField.setData(fromPersonDataField.getData());
		}
		
		if (isNeedsDisplayNameAdjustment() && personRelation.getTo().isAutoGenerateDisplayName()) {
			if (logger.isTraceEnabled()) {
				logger.trace(getClassName() + " Adjusting autogenerated display-name for {} caused by change in {}", new Object[] {propStr(personRelation.getTo()), propStr(personRelation.getFrom())});
			}
			PersistenceManager pm = JDOHelper.getPersistenceManager(personRelation.getTo());
			IStruct structure = (IStruct) pm.getObjectById(personRelation.getTo().getStructLocalObjectID());
			personRelation.getTo().setDisplayName(null, structure);
		}
	}

	/**
	 * This method is called in order to retrieve the DataField that should be copied to the toPerson for the given relation and structField.
	 * <p>
	 * The default implementation will use the first DataField with that StructFieldID (by index). Sub-classes may overwrite in order to better address the DataField.
	 * </p>
	 *   
	 * @param personRelation The {@link PersonRelation} whose from-Person changed.
	 * @param structFieldID The {@link StructFieldID} of the DataField whose value should be copied for the given relation.
	 * @return A DataField whose {@link DataField#getData()} will be used to copy the value to the toPersons datafield.
	 */
	protected DataField retrieveFromPersonDataField(PersonRelation personRelation, StructFieldID structFieldID) {
		DataField dataField = personRelation.getFrom().getPersistentDataFieldByIndex(structFieldID, 0);
		if (dataField == null) {
			logger.warn("retrieveFromPersonDataField() did not find the field in the person, will auto-create one.", new Throwable());
			return personRelation.getFrom().getCreatePersistentDataField(structFieldID);
		} else if (dataField.isEmpty()) {
			logger.warn("retrieveFromPersonDataField() found empty field in the person, no data will be copied.", new Throwable());
		}
		return dataField;
	}
	
	/**
	 * This method is called in order to retrieve the DataField where the data from the fromPerson should be copied to for the given relation and structField.
	 * <p>
	 * The default implementation will use the first DataField with that StructFieldID (by index). Sub-classes may overwrite in order to better address the DataField.
	 * </p>
	 *   
	 * @param personRelation The {@link PersonRelation} whose from-Person changed and whose to-Person should be adjusted.
	 * @param structFieldID The {@link StructFieldID} of the DataField the value should be copied to for the given relation.
	 * @param autoCreate Whether to auto-create if not existing. This will be false when a delete-event was caught so no non-existing field will be unhooked.
	 * @return The DataField where the data from the fromPerson should be copied to.
	 */
	protected DataField retrieveToPersonDataField(PersonRelation personRelation, StructFieldID structFieldID, boolean autoCreate) {
		if (autoCreate)
			return personRelation.getTo().getCreatePersistentDataField(structFieldID);
		else
			return personRelation.getTo().getPersistentDataFieldByIndex(structFieldID, 0);
	}
	
	private void adjustFieldsForChangedDataField(PersistenceManager pm, DataField dataField) {
		if (structFieldIDs.contains(dataField.getStructFieldIDObj())) {
			// A field of the ones we are interested in was changed.
			// See, if there is a relation from its PropertySet of the type we monitor.
			PersonRelationFilterCriteria filterCriteria = new PersonRelationFilterCriteria();
			PropertySetID fromPersonID = PropertySetID.create(dataField.getOrganisationID(), dataField.getPropertySetID());
			if (logger.isDebugEnabled()) {
				logger.debug(getClassName() + " Chaught change of {} for {}. Searching for relations", new Object[] {structFieldsStr(dataField.getStructFieldIDObj()), propStr(fromPersonID)});
			}
			filterCriteria.setFromPersonID(fromPersonID);
			filterCriteria.setPersonRelationTypeIncludeIDs(Collections.singleton(personRelationTypeID));
			filterCriteria.setFilterByPersonAuthority(false); // TODO: Think about this again: We also adapt Person that the current user is not allowed to see/edit?
			Collection<PersonRelationID> personRelationIDs = PersonRelationAccess.getPersonRelationIDs(pm, filterCriteria);
			if (logger.isTraceEnabled()) {
				logger.trace(getClassName() + " Found {} PersonRelations to adjust", personRelationIDs != null ? personRelationIDs.size() : 0);
			}
			if (personRelationIDs != null && personRelationIDs.size() > 0) {
				// There are PersonRelations of our monitored type from the modified Property set
				// let's adjust the toPersons fields
				@SuppressWarnings("unchecked")
				Collection<PersonRelation> personRelations = pm.getObjectsById(personRelationIDs);
				personRelations = filterPersonRelations(dataField, personRelations);
				if (logger.isTraceEnabled()) {
					logger.trace(getClassName() + " Have {} PersonRelations after filter", personRelations != null ? personRelations.size() : 0);
				}				
				for (PersonRelation personRelation : personRelations) {
					adjustToPersonFields(personRelation, dataField.getStructFieldIDObj());
				}
			}
		}
	}

	/**
	 * This method is called with all PersonRelations found that go from a
	 * changed PropertySet. Subclasses may overwrite in order to modify the list
	 * of processed relations.
	 * 
	 * @param changedDataField
	 *            The DataField that has changed and should be copied to the
	 *            managed relation-to-persons. Might be <code>null</code> in
	 *            case this is called for newly created of deleted
	 *            PersonRelations.
	 * @param personRelations
	 *            The {@link PersonRelation} found that should be processed
	 * 
	 * @return All {@link PersonRelation}s that should be processed according to
	 *         this {@link PersonRelationDataFieldManagementListener}.
	 */
	protected Collection<PersonRelation> filterPersonRelations(DataField changedDataField, Collection<PersonRelation> personRelations) {
		return personRelations;
	}

	/**
	 * Create a new {@link PersonRelationDataFieldManagementListener} and
	 * install its JDO-lifecycle-listeners.
	 * <p>
	 * The listeners will monitor {@link PersonRelation}s of the given type and
	 * copy the values of the data-fields with the given {@link StructFieldID}s
	 * to the relations toPersons.
	 * </p>
	 * 
	 * @param pmf
	 *            The {@link PersistenceManagerFactory} the listeners can be
	 *            added to.
	 * @param personRelationTypeID
	 *            The id of the {@link PersonRelationType} that should be
	 *            monitored by this listener
	 * @param structFieldIDs
	 *            The {@link StructFieldID}s of the data-fields that should be
	 *            automatically copied from the fromPerson of a relation to the
	 *            toPerson.
	 */
	public PersonRelationDataFieldManagementListener(PersistenceManagerFactory pmf, PersonRelationTypeID personRelationTypeID, Collection<StructFieldID> structFieldIDs) {
		this.personRelationTypeID = personRelationTypeID;
		this.structFieldIDs = structFieldIDs;
		pmf.addInstanceLifecycleListener(createPersonRelationListener, new Class[] {PersonRelation.class});
		pmf.addInstanceLifecycleListener(deletePersonRelationListener, new Class[] {PersonRelation.class});
//		pmf.addInstanceLifecycleListener(attachDataFieldListener, new Class[] {DataField.class});
		pmf.addInstanceLifecycleListener(dirtyDataFieldListener, new Class[] {DataField.class});
	}
	
	/**
	 * Define whether a change in the monitored data-fields of this listener needs to trigger a display-name auto-adjustment of the managed {@link Person}s.
	 * <p>The default value is <code>true</code></p>
	 *  
	 * @param needsDisplayNameAdjust Whether a display-name adjustment should be performed.
	 */
	public void setNeedsDisplayNameAdjustment(boolean needsDisplayNameAdjust) {
		this.needsDisplayNameAdjustment = needsDisplayNameAdjust;
	}
	
	/**
	 * @return Whether a display-name adjustment should be performed.
	 */
	public boolean isNeedsDisplayNameAdjustment() {
		return needsDisplayNameAdjustment;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("{");
		sb.append("personRelationTypeID = ").append(personRelationTypeID);
		sb.append(", structFieldIDs = ").append(structFieldIDs);
		sb.append("}");
		return sb.toString();
	}

	/**
	 * For debug output
	 */
	private String structFieldsStr(StructFieldID... structFieldIDs) {
		return structFieldsStr(Arrays.asList(structFieldIDs));
	}
	
	/**
	 * For debug output
	 */
	private String structFieldsStr(Collection<StructFieldID> structFieldIDs) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (StructFieldID structFieldID : structFieldIDs) {
			if (sb.length() > 1)
				sb.append(", ");
			sb.append(structFieldID.structFieldID);
		} 
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * For debug output
	 */
	private String propStr(Object person) {
		StringBuilder sb = new StringBuilder();
		sb.append("PropertySet[");
		if (person instanceof PropertySetID) {
			sb.append(((PropertySetID)person).propertySetID);
		} else if (person instanceof PropertySet) {
			sb.append(((PropertySet)person).getPropertySetID());
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * For debug output
	 */
	private String getClassName() {
		return PersonRelationDataFieldManagementListener.this.getClass().getSimpleName();
	}
	
}
