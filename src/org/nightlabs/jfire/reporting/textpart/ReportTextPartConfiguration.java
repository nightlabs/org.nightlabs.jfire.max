/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.util.Util;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.textpart.id.ReportTextPartConfigurationID"
 *		detachable = "true"
 *		table="JFireReporting_ReportTextPartConfiguration"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportTextPartConfigurationID"
 * 
 * @jdo.query
 *	name="getReportTextPartConfigurationByLinkedObject"
 *	query="SELECT UNIQUE this
 *		WHERE this.linkedObjectID == :paramLinkedObjectID"
 */
public class ReportTextPartConfiguration implements Serializable {
	
	private static final long serialVersionUID = 20080821L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long reportTextPartConfigurationID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String linkedObjectID;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient ObjectID linkedObjectIDObj;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.textpart.ReportTextPart"
	 *		mapped-by="reportTextPartConfiguration"
	 *		dependent-element="true"
	 */
	private List<ReportTextPart> reportTextParts;
	
	/**
	 * @deprecated Only for JDO.
	 */
	public ReportTextPartConfiguration() {}

	public ReportTextPartConfiguration(String organisationID, long reportTextPartConfigurationID) {
		this.organisationID = organisationID;
		this.reportTextPartConfigurationID = reportTextPartConfigurationID;
		this.reportTextParts = new ArrayList<ReportTextPart>();
	}
	
	/**
	 * @return The organisationID primary-key value of this {@link ReportTextPartConfiguration}.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return The reportTextPartConfigurationID primary-key value of this {@link ReportTextPartConfiguration}.
	 */
	public long getReportTextPartConfigurationID() {
		return reportTextPartConfigurationID;
	}
	
	/**
	 * Set the object this configuration should be linked to.
	 * Note that not the Object itself is but the String representation of its 
	 * id object and therefore the objects a configuration can be linked to 
	 * have to match two constraints: They have to be PersistenceCapable and 
	 * their id objects must be instances of {@link ObjectID}.
	 *  
	 * @param linkedObject The object this configuration should be linked to. 
	 */
	public void setLinkedObject(Object linkedObject) {
		if (linkedObject == null) {
			linkedObjectID = null;
			linkedObjectIDObj = null;
		} else {
			ObjectID objectID = null;
			try {
				objectID = (ObjectID) JDOHelper.getObjectId(linkedObject);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("This method requires an PersistenceCapable object whose id object is an instance of " + ObjectID.class.getSimpleName());
			}
			if (objectID == null) {
				throw new IllegalArgumentException("Pass only PersistenceCapable and already persisted objects to this method");
			}
			linkedObjectID = objectID.toString();
			linkedObjectIDObj = null;
		}
	}

	/**
	 * Set the {@link ObjectID} of the object this configuration should be linked to.
	 * Note that not the given objectID will be stored but its String representation. 
	 * 
	 * @param objectID The {@link ObjectID} of the object this configuration should be linked to.
	 */
	public void setLinkedObjectID(ObjectID objectID) {
		if (objectID == null) {
			linkedObjectID = null;
			linkedObjectIDObj = null;
		} else {
			linkedObjectID = objectID.toString();
			linkedObjectIDObj = Util.cloneSerializable(objectID);
		}
	}

	/**
	 * Returns the {@link ObjectID} of the object this configuration was linked to.
	 * It is created from the String representation that is stored with this
	 * configuration.
	 * @return The {@link ObjectID} of the object this configuration was linked to.
	 */
	public ObjectID getLinkedObjectID() {
		if (linkedObjectID == null)
			return null;
		if (linkedObjectIDObj == null) {
			linkedObjectIDObj = ObjectIDUtil.createObjectID(linkedObjectID);
		}
		return linkedObjectIDObj;
	}

	/**
	 * Returns an <b>unmodifiable</b> {@link List} of the {@link ReportTextPart}s in this configuration.
	 * @return An <b>unmodifiable</b> {@link List} of the {@link ReportTextPart}s in this configuration.
	 */
	public List<ReportTextPart> getReportTextParts() {
		return Collections.unmodifiableList(reportTextParts);
	}

	/**
	 * Adds the given {@link ReportTextPart} to the list of {@link ReportTextPart}s
	 * in this configuration.
	 * <p>
	 * Note that this method will throw an {@link IllegalArgumentException}
	 * if a {@link ReportTextPart} with a reportTextPartID like the given one
	 * already exists in this configuration.
	 * </p>
	 * 
	 * @param reportTextPart The {@link ReportTextPart} to add.
	 */
	public void addReportTextPart(ReportTextPart reportTextPart) {
		ReportTextPart part = getReportTextPart(reportTextPart.getReportTextPartID());
		if (part != null) {
			// TODO: Throw better exception
			throw new IllegalArgumentException("A ReportTextPart with the reportTextPartID of the given one already exists in this " + ReportTextPartConfiguration.class.getSimpleName() + ": " + part.getReportTextPartID());
		}
		reportTextParts.add(reportTextPart);
	}

	/**
	 * Removes the given {@link ReportTextPart} from the list of {@link ReportTextPart}s
	 * in this configuration. Will do nothing if the given {@link ReportTextPart} is
	 * not contained in this configuration.
	 * 
	 * @param reportTextPart The {@link ReportTextPart} to remove.
	 */
	public void removeReportTextPart(ReportTextPart reportTextPart) {
		reportTextParts.remove(reportTextPart);
	}
	
	/**
	 * Creates a new {@link ReportTextPart} with the given reportTextPartID 
	 * adds it to the list of {@link ReportTextPart}s of this configuration 
	 * and finally returns the new {@link ReportTextPart}.
	 * <p>
	 * Note that this method will throw an {@link IllegalArgumentException}
	 * if a {@link ReportTextPart} with the given reportTextPartID already
	 * exists in this configuration.
	 * </p> 
	 * 
	 * @param reportTextPartID The reportTextPartID for the new {@link ReportTextPart}.
	 * @return The newly created {@link ReportTextPart}.
	 */
	public ReportTextPart createReportTextPart(String reportTextPartID) {
		ReportTextPart part = getReportTextPart(reportTextPartID);
		if (part != null) {
			// TODO: Throw better exception
			throw new IllegalArgumentException("A ReportTextPart with the given reportTextPartID already exists in this " + ReportTextPartConfiguration.class.getSimpleName() + ": " + part.getReportTextPartID());
		}
		ReportTextPart reportTextPart = new ReportTextPart(this, reportTextPartID);
		reportTextParts.add(reportTextPart);
		return reportTextPart;
	}

	/**
	 * Searches for a {@link ReportTextPart} within this configuration that has 
	 * he given reporTextPartID and returns it if it can be found, otherwise
	 * <code>null</code> will be returned.
	 * 
	 * @param reportTextPartID The reportTextPartID to search for.
	 * @return The {@link ReportTextPart} with the given reportTextPartID or <code>null</code> if none could be found.
	 */
	public ReportTextPart getReportTextPart(String reportTextPartID) {
		for (ReportTextPart reportTextPart : getReportTextParts()) {
			if (reportTextPart.getReportTextPartID().equals(reportTextPartID)) {
				return reportTextPart;
			}
		}
		return null;
	}

	/**
	 * Searches the {@link ReportTextPartConfiguration} for the given linkedObjectID. If none can be found in the 
	 * datastore this method will search for a {@link ReportTextPartConfiguration}s linked to the given 
	 * {@link ReportRegistryItem} (referenced with reportRegistryItemID) or its parent {@link ReportCategory}s.
	 * Note that if the given linkedObjectID is itself a {@link ReportRegistryItemID} the second parameter will 
	 * be ignored.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @param linkedObjectID The {@link ObjectID} a linked {@link ReportTextPartConfiguration} should be found for.
	 * @param reportRegistryItemID The {@link ReportRegistryItemID} to start the search for a {@link ReportTextPartConfiguration}
	 *                             that is linked to a {@link ReportRegistryItem} should be started from.
	 * @return The {@link ReportTextPartConfiguration} found either for the given linkedObjectID or for a
	 *         {@link ReportRegistryItem}. If nothing can be found, <code>null</code> will be returned.
	 */
	public static final ReportTextPartConfiguration getReportTextPartConfiguration(
			PersistenceManager pm, ObjectID linkedObjectID, ReportRegistryItemID reportRegistryItemID) {
		
		Query q = pm.newNamedQuery(ReportTextPartConfiguration.class, "getReportTextPartConfigurationByLinkedObject");
		ReportTextPartConfiguration configuration = (ReportTextPartConfiguration) q.execute(linkedObjectID.toString());
		
		if (configuration != null) {
			// The configuration could be found. 
			return configuration;
		}
		
		// The configuration could not be found, try for the reportReistryItems:
		ReportRegistryItemID nextSearchReportRegistryItemID = null;
		if (linkedObjectID instanceof ReportRegistryItemID) {
			// we've already searched for a configuration linked to a ReportRegistryItemID
			// now we try to find something for the parent-ReportCategory
			ReportRegistryItem item = (ReportRegistryItem) pm.getObjectById(linkedObjectID);
			nextSearchReportRegistryItemID = item.getParentCategoryID();
		} else {
			// we've searched for an arbitrary object, now we start the search
			// in the ReportRegistryItems and start by the given one.
			nextSearchReportRegistryItemID = reportRegistryItemID;
		}
		
		if (nextSearchReportRegistryItemID == null) {
			// Well, no parent ReportCategory seems to be left
			// so the search is finished, we haven't found anything
			return null;
		}
		// recurse
		return getReportTextPartConfiguration(pm, nextSearchReportRegistryItemID, reportRegistryItemID);
	}
}