package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreCallback;
import javax.jdo.listener.StoreLifecycleListener;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueLinkID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueLink"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, issueLinkID"
 * 
 * @jdo.query
 *		name="getIssueLinksByIssueIDAndLinkedObjectID"
 *		query="SELECT
 *			WHERE this.issueID == :issueID && 
 *			this.issueID == :issueID &&
 *			this.issueLinkObjectID == :linkedObjectID                    
 *
 * @jdo.fetch-group name="IssueLink.linkedObjectID" fetch-groups="default" fields="linkedObjectID"
 * @jdo.fetch-group name="IssueLink.linkedObjectClass" fetch-groups="default" fields="linkedObjectClass"
 * @jdo.fetch-group name="IssueLink.issueLinkType" fetch-groups="default" fields="issueLinkType"
 *
 * @jdo.fetch-group name="IssueLink.this" fields="issue, issueLinkType, linkedObjectID"
 */ 
public class IssueLink
implements Serializable, DetachCallback, StoreCallback, DeleteCallback
{
	private static final Logger logger = Logger.getLogger(IssueLink.class);

	public static final String FETCH_GROUP_THIS_ISSUE_LINK = "IssueLink.this";
	public static final String FETCH_GROUP_ISSUE_LINK_TYPE = "IssueLink.issueLinkType";
	public static final String FETCH_GROUP_ISSUE_LINKED_OBJECT_ID = "IssueLink.linkedObjectID";
//	public static final String FETCH_GROUP_ISSUE_LINKED_OBJECT_CLASS = "IssueLink.linkedObjectClass";
	

	/**
	 * Virtual fetch-group (not processed by JDO) to obtain the non-persistent property
	 * {@link #getLinkedObject()}. This is done in the {@link DetachCallback} method
	 * {@link #jdoPostDetach(Object)}.
	 */
	public static final String FETCH_GROUP_LINKED_OBJECT = "IssueLink.linkedObject";

	/**
	 * Virtual fetch-group to obtain the class of the non-persistent property {@link #getLinkedObject()} -
	 * i.e. the property 
	 */
	public static final String FETCH_GROUP_LINKED_OBJECT_CLASS = "IssueLink.linkedObjectClass";

	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueLinkID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueLinkType issueLinkType;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private String linkedObjectID;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient ObjectID _linkedObjectID;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private Object linkedObject;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private Class<?> linkedObjectClass;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected IssueLink() {}

	public IssueLink(String organisationID, long issueLinkID, Issue issue, IssueLinkType issueLinkType, Object linkedObject) {
		if (issue == null)
			throw new IllegalArgumentException("issue must not be null!");

		if (linkedObject == null)
			throw new IllegalArgumentException("linkedObject must not be null!");

		if (!(linkedObject instanceof PersistenceCapable))
			throw new IllegalArgumentException("linkedObject must implement the interface "+ PersistenceCapable.class.getName() +"! linkedObject is an instance of " + linkedObject.getClass().getName() + ": " + linkedObject);

		Object linkedObjectID = JDOHelper.getObjectId(linkedObject);

		if (!(linkedObjectID instanceof ObjectID))
			throw new IllegalArgumentException("The object-id of linkedObject is not an instance of " + ObjectID.class.getName() + "! It's an instance of: " + (linkedObjectID == null ? null : linkedObjectID.getClass().getName()));

		this.organisationID = organisationID;
		this.issue = issue;
		this.issueLinkID = issueLinkID;

		this.linkedObject = linkedObject;
		this.linkedObjectID = linkedObjectID.toString();
		this.issueLinkType = issueLinkType;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public long getIssueLinkID() {
		return issueLinkID;
	}

	public Issue getIssue() {
		return issue;
	}

	public ObjectID getLinkedObjectID() {
		if (_linkedObjectID == null)
			_linkedObjectID = ObjectIDUtil.createObjectID(this.linkedObjectID);

		return _linkedObjectID;
	}
	
	public IssueLinkType getIssueLinkType() {
		return issueLinkType;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of IssueLink is currently not persistent! Cannot obtain a PersistenceManager!");
		return pm;
	}

	public Object getLinkedObject() {
		if (this.linkedObject == null)
			this.linkedObject = getPersistenceManager().getObjectById(getLinkedObjectID());

		return linkedObject;
	}

	public Class<?> getLinkedObjectClass() {
		if (this.linkedObjectClass == null)
			this.linkedObjectClass = getPersistenceManager().getObjectById(getLinkedObjectID()).getClass();

		return linkedObjectClass;
	}

	private static ThreadLocal<long[]> detach_linkedObject_recursionCounter = new ThreadLocal<long[]> () {
		@Override
		protected long[] initialValue() {
			long[] res = new long[1];
			res[0] = 0;
			return res;
		}
	};

	@Override
	public void jdoPostDetach(Object object) {
		IssueLink detached = this;
		IssueLink attached = (IssueLink) object;

		PersistenceManager pm = attached.getPersistenceManager();
		Set<?> fetchGroups = pm.getFetchPlan().getGroups();
		if (fetchGroups.contains(FETCH_GROUP_LINKED_OBJECT)) {
			long[] currentRecursionLevel = detach_linkedObject_recursionCounter.get();
			if (currentRecursionLevel[0] < 1) {
				++currentRecursionLevel[0];
				try {
					detached.linkedObject = pm.detachCopy(attached.getLinkedObject());
				} finally {
					--currentRecursionLevel[0];
				}
			}
		}

		if (fetchGroups.contains(FETCH_GROUP_LINKED_OBJECT_CLASS))
			detached.linkedObjectClass = attached.getLinkedObjectClass();
	}

	public String getPrimaryKey()
	{
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(issueLinkID);
	}

	@Override
	public void jdoPreDetach() {
	}

	@Override
	public void jdoPreStore() {
		if (logger.isDebugEnabled())
			logger.debug("jdoPreStore: about to store IssueLink: " + getPrimaryKey());

		getPersistenceManager().addInstanceLifecycleListener(new StoreLifecycleListener() {
			@Override
			public void preStore(InstanceLifecycleEvent event) {
			}
			@Override
			public void postStore(InstanceLifecycleEvent event) {
				if (!IssueLink.this.equals(event.getPersistentInstance()))
					return;

				getPersistenceManager().removeInstanceLifecycleListener(this);

				if (NLJDOHelper.exists(getPersistenceManager(), IssueLink.this)) {
					if (logger.isDebugEnabled())
						logger.debug("jdoPreStore: the IssueLink " + getPrimaryKey() + " already exists - no need to call the IssueLinkType's afterCreateIssueLink callback method.");
				}
				else {
					if (logger.isDebugEnabled())
						logger.debug("jdoPreStore: the IssueLink " + getPrimaryKey() + " does NOT yet exist - calling the IssueLinkType's afterCreateIssueLink callback method.");
					
					getIssueLinkType().postCreateIssueLink(IssueLink.this);
				}
			}
		}, new Class[] { IssueLink.class });
	}

	@Override
	public void jdoPreDelete() {
		if (logger.isDebugEnabled())
			logger.debug("jdoPreDelete: about to delete IssueLink: " + getPrimaryKey());

		getIssueLinkType().preDeleteIssueLink(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (issueLinkID ^ (issueLinkID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final IssueLink other = (IssueLink) obj;

		return
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.issueLinkID, other.issueLinkID);
	}
}
