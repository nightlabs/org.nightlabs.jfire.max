/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.util.Util;

/**
 * {@link ReportTextPartConfiguration}s are a list of {@link ReportTextPart}s that
 * can be linked to a report category or a report layout. Additionally they can be
 * linked to arbitrary objects within the JFire data-store.
 * <p>
 * A {@link ReportTextPartConfiguration} linked to a {@link ReportRegistryItem}
 * should have its linkedObjectID set to <code>null</code>. 
 * A {@link ReportTextPartConfiguration} that should be linked to an object
 * in the JFire datastore can optionally set the reportRegistryItem member
 * to indicate that this configuration is only valid in the context of this item.
 * </p>
 * <p>
 * {@link #getReportTextPartConfiguration(PersistenceManager, ObjectID, ReportRegistryItemID)
 * and its overloaded method can be used to search for the {@link ReportTextPartConfiguration} 
 * linked to an object in the data-store.  
 * </p>
 * <p>
 * The search pattern applied is that first the configuration for the specific object is searched
 * and then the configuration linked to the currently rendered report is searched. 
 * This mechanism can be used to define default values for a report SELECT layout/category
 * and overwrite those for specific objects.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @jdo.fetch-group name="ReportTextPartConfiguration.reportRegistryItem" fetch-groups="default" fields="reportRegistryItem"
 * @jdo.fetch-group name="ReportTextPartConfiguration.reportTextParts" fetch-groups="default" fields="reportTextParts"
 * 
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
 *	query="SELECT this
 *		WHERE 
 *			this.linkedObjectID == :paramLinkedObjectID &&
 *			(this.reportRegistryItem == :paramReportRegistryItem || this.reportRegistryItem == null)"
 *
 * @jdo.query
 *	name="getReportTextPartConfigurationByReportRegistryItem"
 *	query="SELECT this
 *		WHERE 
 *			this.reportRegistryItem == :paramReportRegistryItem &&
 *			this.linkedObjectID == null"
 *
 */
public class ReportTextPartConfiguration implements Serializable {
	
	private static final long serialVersionUID = 20080929L;

	private static Logger logger = Logger.getLogger(ReportTextPartConfiguration.class);
	
	public static final String FETCH_GROUP_REPORT_REGISTRY_ITEM = "ReportTextPartConfiguration.reportRegistryItem";
	public static final String FETCH_GROUP_REPORT_TEXT_PARTS = "ReportTextPartConfiguration.reportTextParts";
	
	
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
	private ReportRegistryItem reportRegistryItem;
	
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
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean synthetic = false;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private ReportRegistryItemID synthesizeTemplateReportRegistryItemID;
	
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
	 * id object is persisted. Therefore the objects a configuration can be linked to  
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
	 * Get the {@link ReportRegistryItem} this {@link ReportTextPartConfiguration}
	 * is linked to. This is the report layout/category this configuration is valid in.
	 * 
	 * @return The {@link ReportRegistryItem} this {@link ReportTextPartConfiguration}
	 */
	public ReportRegistryItem getReportRegistryItem() {
		return reportRegistryItem;
	}
	
	/**
	 * Sets the {@link ReportRegistryItem} this {@link ReportTextPartConfiguration} 
	 * is linked to. This is the report layout/category this configuration is valid in.
	 * 
	 * @param reportRegistryItem The item to set.
	 */
	public void setReportRegistryItem(ReportRegistryItem reportRegistryItem) {
		this.reportRegistryItem = reportRegistryItem;
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
	 * Checks whether this {@link ReportTextPartConfiguration} was created artificially for a non-existing one,
	 * i.e. it was created as the copy of the configuration linked to a report layout/category when searching for
	 * the configuration linked to some other object in the data-store.
	 * <p>
	 * Synthetic {@link ReportTextPartConfiguration} are always detached and will 
	 * contain new (not yet persisted) {@link ReportTextPart}s.
	 * </p>  
	 * 
	 * @return Whether this configuration was created for a non existing one.
	 */
	public boolean isSynthetic() {
		return synthetic;
	}
	
	/**
	 * @param synthetic The synthetic to store.
	 */
	private void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}
	
	private void setSynthesizeTemplateReportRegistryItemID(ReportRegistryItemID synthesizeTemplateReportRegistryItemID) {
		this.synthesizeTemplateReportRegistryItemID = synthesizeTemplateReportRegistryItemID;
	}

	/**
	 * @return 
	 * 			If this {@link ReportTextPartConfiguration} is synthetic, this method returns the {@link ReportRegistryItemID}
	 * 			of this {@link ReportRegistryItem} that was the template/basis for the new synthetic one.	
	 */
	public ReportRegistryItemID getSynthesizeTemplateReportRegistryItemID() {
		return synthesizeTemplateReportRegistryItemID;
	}
	
	/**
	 * Creates a new, synthetic {@link ReportTextPartConfiguration} with copies of the
	 * values of the given one. The returned configuration can be made persistent.
	 *  
	 * @param pm The PersistenceManager to use.
	 * @param reportTextPartConfiguration The configuration to clone.
	 * @param reportRegistryItemID The reportRegistryItemID the new config should be synthesized for.
	 * @param linkedObjectID The ObjectID to link the new configuration to.
	 * @param fetchGroups The fetch-groups to detach parts of the configuration with.
	 * @param maxFetchDepth The maximum fetch-depth to use while detaching.
	 * @return A new but persistable {@link ReportTextPartConfiguration} with copies of the values of the given one.
	 */
	@SuppressWarnings("unchecked")
	private static ReportTextPartConfiguration synthesizeReportTextPartConfiguration(
			PersistenceManager pm, ReportTextPartConfiguration reportTextPartConfiguration,
			ReportRegistryItemID reportRegistryItemID, ObjectID linkedObjectID, 
			String[] fetchGroups, int maxFetchDepth) {
		
		ReportTextPartConfiguration result = new ReportTextPartConfiguration(
				reportTextPartConfiguration.getOrganisationID(), 
				IDGenerator.nextID(ReportTextPartConfiguration.class));
		
		result.setSynthetic(true);
		if (reportTextPartConfiguration.getReportRegistryItem() != null) {
			result.setSynthesizeTemplateReportRegistryItemID(
					(ReportRegistryItemID) JDOHelper.getObjectId(reportTextPartConfiguration.getReportRegistryItem()));
		}
		Set<String> oldFetchGroups = pm.getFetchPlan().getGroups();
		int oldFetchDepth = pm.getFetchPlan().getMaxFetchDepth();
		try {
			pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			
			ReportRegistryItem reportRegistryItem = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
			if (reportTextPartConfiguration.getReportRegistryItem() != null) {
				result.setReportRegistryItem(pm.detachCopy(reportRegistryItem));
			}
			
			result.setLinkedObjectID(linkedObjectID);
			
			for (ReportTextPart textPart : reportTextPartConfiguration.getReportTextParts()) {
				ReportTextPart newPart = new ReportTextPart(result, textPart.getReportTextPartID());
				newPart.setType(textPart.getType());
				newPart.getName().copyFrom(textPart.getName());
				newPart.getContent().copyFrom(textPart.getContent());
				result.addReportTextPart(newPart);
			}
			
		} finally {
			pm.getFetchPlan().setGroups(oldFetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(oldFetchDepth);
		}
		return result;		
	}
	
	/**
	 * Returns the first {@link ReportTextPartConfiguration} that is linked to the given
	 * linkedObjectID and to the given {@link ReportRegistryItem} or linked to no ReportRegistryItem, 
	 * i.e. linkedObjectID matches and reportRegistryItem is <code>null</code>.
	 * Hereby the one linked to the given {@link ReportRegistryItem} will be returned if both
	 * one linked to an item and one that is not are found.
	 *    
	 * @param pm The {@link PersistenceManager} to use.
	 * @param reportRegistryItem The {@link ReportRegistryItem} the {@link ReportTextPartConfiguration} should be searched for.
	 * @param linkedObjectID The {@link ObjectID} of the object the {@link ReportTextPartConfiguration} should be used for.
	 * @return The first {@link ReportTextPartConfiguration} that is linked to the given
	 *         linkedObjectID and the given {@link ReportRegistryItem}, or <code>null</code> if none could be found.
	 */
	@SuppressWarnings("unchecked")
	public static ReportTextPartConfiguration getReportTextPartConfigurationByLinkedObject(PersistenceManager pm, ReportRegistryItem reportRegistryItem, ObjectID linkedObjectID) {
		if (logger.isDebugEnabled())
			logger.debug("Searching for ReportTextPartConfiguration by linkedObjectID " + linkedObjectID);
		Query q = pm.newNamedQuery(ReportTextPartConfiguration.class, "getReportTextPartConfigurationByLinkedObject");
		Collection<ReportTextPartConfiguration> configs = 
			(Collection<ReportTextPartConfiguration>) q.execute(linkedObjectID.toString(), reportRegistryItem);
		if (configs.size() > 0) {
			// We found some configurations linked to the given linkedObjectID,
			// now find the one linked to the given reportRegistryItem as well.
			for (ReportTextPartConfiguration reportTextPartConfiguration : configs) {
				ReportRegistryItem reportItem = reportTextPartConfiguration.getReportRegistryItem(); 
				if (reportItem != null && reportItem.equals(reportRegistryItem)) {					
					if (logger.isDebugEnabled())
						logger.debug("Found ReportTextPartConfiguration by linkedObjectID " + linkedObjectID + ": " + reportTextPartConfiguration);
					return reportTextPartConfiguration;
				}
			}
			if (logger.isDebugEnabled())
				logger.debug("No ReportTextPartConfiguration found by linkedObjectID " + linkedObjectID + ", trying to find one linked to no reportRegistryItem");
			// None could be found linked to the given reportRegistryItem, maybe we find one
			// linked to no reportRegistryItem at all?
			for (ReportTextPartConfiguration reportTextPartConfiguration : configs) {
				if (reportTextPartConfiguration.getReportRegistryItem() == null)					
					if (logger.isDebugEnabled())
						logger.debug("Found ReportTextPartConfiguration by linkedObjectID " + linkedObjectID + " and linked to no reportRegistryItem:  " + reportTextPartConfiguration);
					return reportTextPartConfiguration;				
			}
			// Well, nothing found
			return null;
		}
		return null;
	}
	
	/**
	 * Returns the first {@link ReportTextPartConfiguration} that is linked to the given
	 * ReportRegistryItem and where the linkedObject of that configuration is <code>null</code>.
	 * <p>
	 * Note, that it's theoretically possible that more than one {@link ReportTextPartConfiguration}
	 * exists that is linked to the given object, but this is an illegal state in the data-store
	 * and will be ignored by this method.
	 * </p>
	 *    
	 * @param pm The {@link PersistenceManager} to use.
	 * @param reportRegistryItem The {@link ReportRegistryItem} the {@link ReportTextPartConfiguration} should be used for.
	 * @return The first {@link ReportTextPartConfiguration} that is linked to the given
	 *         linkedObjectID, or <code>null</code> if none could be found.
	 */
	@SuppressWarnings("unchecked")
	public static ReportTextPartConfiguration getReportTextPartConfigurationByReportRegistryItem(PersistenceManager pm, ReportRegistryItem reportRegistryItem) {
		if (logger.isDebugEnabled())
			logger.debug("Searching for ReportTextPartConfiguration by reportRegistryItem " + reportRegistryItem);
		Query q = pm.newNamedQuery(ReportTextPartConfiguration.class, "getReportTextPartConfigurationByReportRegistryItem");
		Collection<ReportTextPartConfiguration> configs = 
			(Collection<ReportTextPartConfiguration>) q.execute(reportRegistryItem);
		// Found some, return the first one.
		if (configs.size() >= 1) {
			ReportTextPartConfiguration config = configs.iterator().next();
			if (logger.isDebugEnabled())
				logger.debug("Found ReportTextPartConfiguration by reportRegistryItem " + reportRegistryItem + ": " + config);
			return config;
		}
		return null;
	}
	
	/**
	 * Searches the {@link ReportTextPartConfiguration} for the given linkedObjectID and reportRegistryItemID. 
	 * If none can be found in the data-store this method will search for a {@link ReportTextPartConfiguration} 
	 * linked to one of the parent {@link ReportCategory}s of the given reportRegistryItemID.
	 * <p>
	 * Additionally this method can be used to get a synthetic, new {@link ReportTextPartConfiguration}
	 * linked to the given linkedObjectID. A synthetic {@link ReportTextPartConfiguration} will have the
	 * values of that one found when searching for the given reportRegistryItem. The synthetic configuration 
	 * will not be persisted and the parts of it referencing existing/persisted objects will be detached.
	 * </p>
	 * 
	 * @param pm 
	 * 			The {@link PersistenceManager} to use.
	 * @param linkedObjectID 
	 * 			The {@link ObjectID} a linked {@link ReportTextPartConfiguration} should be found for.
	 * @param reportRegistryItemID 
	 * 			The {@link ReportRegistryItemID} to start the search for a {@link ReportTextPartConfiguration}
	 * 			that is linked to a {@link ReportRegistryItem} should be started from.
	 * @param synthesize 
	 * 			Whether to synthesize a new {@link ReportTextPartConfiguration} when none directly linked to the
	 * 			given linkedObjectID was found but one was found linked to a reportRegistryItem.
	 * @param fetchGroups
	 * 			The fetch-groups used - when synthesizing a new configuration - to detach those parts of the
	 * 			the found configuration that reference already persisted object.                             
	 * @param maxFetchDepth
	 * 			The maximum fetch-depth used - when synthesizing a new configuration - to detach those parts of the
	 * 			the found configuration that reference already persisted object.                             
	 * @return 
	 * 			The {@link ReportTextPartConfiguration} either found (or synthesized) for the given linkedObjectID 
	 * 			or the configuration found for a {@link ReportRegistryItem}. If nothing can be found, <code>null</code> will be returned.
	 */
	public static ReportTextPartConfiguration getReportTextPartConfiguration(
			PersistenceManager pm, ReportRegistryItemID reportRegistryItemID, ObjectID linkedObjectID, 
			boolean synthesize, String[] fetchGroups, int maxFetchDepth) {
		
		if (logger.isDebugEnabled())
			logger.debug("Searching ReportTextPartConfiguration for reportRegistryItemID =  " + reportRegistryItemID +
					"linkedObjectID = " + linkedObjectID + ", synthesize = " + synthesize);
		ReportRegistryItem item = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
		ReportTextPartConfiguration configuration = getReportTextPartConfigurationByLinkedObject(pm, item, linkedObjectID);
		
		if (configuration != null) {
			if (logger.isDebugEnabled())
				logger.debug("Found configuration directly linked to linkedObjectID");
			// The configuration could be found. 
			return configuration;
		}
		
		if (logger.isDebugEnabled())
			logger.debug("No configuration found directly linked to linkedObjectID, searching for reportRegistryItemID");
		// search for the configuration linked to a report registry item
		configuration = getReportTextPartConfiguration(pm, item);
		if (logger.isDebugEnabled()) {
			if (configuration == null)
				logger.debug("No configuration searching for reportRegistryItemID, returning null");
			else 
				logger.debug("Found configuration searching for reportRegistryItemID.");
		}
		
		if (configuration != null && synthesize) {
			if (logger.isDebugEnabled())
				logger.debug("Synthesizing found configuration");
			configuration = synthesizeReportTextPartConfiguration(pm, configuration, reportRegistryItemID, linkedObjectID, fetchGroups, maxFetchDepth);
		}
		if (logger.isDebugEnabled())
			logger.debug("Return configuration: " + configuration);
		return configuration;
	}

	/**
	 * Searches for a {@link ReportTextPartConfiguration} linked to the given
	 * {@link ReportRegistryItem} or one of its parent {@link ReportCategory}s.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @param reportRegistryItem The {@link ReportRegistryItem} to search a configuration for.
	 * @return The {@link ReportTextPartConfiguration} registered to the given item or one of its parents, 
	 *         or <code>null</code> if none could be found.
	 */
	public static ReportTextPartConfiguration getReportTextPartConfiguration(PersistenceManager pm, ReportRegistryItem reportRegistryItem) {
		
		if (logger.isDebugEnabled())
			logger.debug("Searching ReportTextPartConfiguration for reportRegistryItem " + reportRegistryItem);
		ReportTextPartConfiguration configuration = getReportTextPartConfigurationByReportRegistryItem(pm, reportRegistryItem);
		
		if (configuration != null) {
			if (logger.isDebugEnabled())
				logger.debug("Found ReportTextPartConfiguration for reportRegistryItem " + reportRegistryItem + ": " + configuration);
			// The configuration could be found. 
			return configuration;
		}
		
		// The configuration could not be found, try for the reportReistryItems:
		ReportRegistryItem nextSearchReportRegistryItem = null;
		// we've already searched for a configuration linked to a ReportRegistryItemID
		// now we try to find something for the parent-ReportCategory
		nextSearchReportRegistryItem = reportRegistryItem.getParentCategory();
		
		if (logger.isDebugEnabled())
			logger.debug("No ReportTextPartConfiguration for reportRegistryItem " + reportRegistryItem + ", searching for parent category " + nextSearchReportRegistryItem);
		if (nextSearchReportRegistryItem == null) {
			// Well, no parent ReportCategory seems to be left
			// so the search is finished, we haven't found anything
			return null;
		}
		// recurse
		return getReportTextPartConfiguration(pm, nextSearchReportRegistryItem);
	}
	
	/**
	 * Searches for a {@link ReportTextPartConfiguration} linked to the given
	 * {@link ReportRegistryItem} or one of its parent {@link ReportCategory}s.
	 * <p>
	 * Additionally this method can be used to get a synthetic, new {@link ReportTextPartConfiguration}
	 * linked to the given {@link ReportRegistryItem}. A synthetic {@link ReportTextPartConfiguration} will have the
	 * values of that one found when searching for a parent category. The synthetic configuration 
	 * will not be persisted and the parts of it referencing existing/persisted objects will be detached.
	 * </p>
	 * 
	 * @param pm 
	 * 			The {@link PersistenceManager} to use.
	 * @param reportRegistryItem 
	 * 			The {@link ReportRegistryItem} to search a configuration for.
	 * @param synthesize 
	 * 			Whether to synthesize a new {@link ReportTextPartConfiguration} when none directly linked to the
	 * 			given reportRegistryItem was found but one was found linked to parent category.
	 * @param fetchGroups
	 * 			The fetch-groups used - when synthesizing a new configuration - to detach those parts of the
	 * 			the found configuration that reference already persisted object.                             
	 * @param maxFetchDepth
	 * 			The maximum fetch-depth used - when synthesizing a new configuration - to detach those parts of the
	 * 			the found configuration that reference already persisted object.                             
	 * @return 
	 * 			The {@link ReportTextPartConfiguration} registered to the given item or one of its parents,
	 * 			or <code>null</code> if none could be found.
	 */
	public static ReportTextPartConfiguration getReportTextPartConfiguration(
			PersistenceManager pm, ReportRegistryItem reportRegistryItem, 
			boolean synthesize, String[] fetchGroups, int maxFetchDepth) {
		
		ReportTextPartConfiguration config = getReportTextPartConfiguration(pm, reportRegistryItem);
		if (config == null)
			return null;
		if (!reportRegistryItem.equals(config.getReportRegistryItem()) && synthesize) {
			// have to synthesize a new one
			ReportRegistryItemID reportRegistryItemID = (ReportRegistryItemID) JDOHelper.getObjectId(reportRegistryItem);
			config = synthesizeReportTextPartConfiguration(pm, config, reportRegistryItemID, null, fetchGroups, maxFetchDepth);
		}
		return config;
	}
}