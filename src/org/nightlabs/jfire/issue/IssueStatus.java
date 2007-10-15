package org.nightlabs.jfire.issue;

import java.io.Serializable;

/**
 * @author Chairat Kongarayawetchakun - chairatk at nightlabs dot de
 *
 * jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueStatusID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueStatus"
 *
 * jdo.inheritance strategy="new-table"
 *
 * jdo.create-objectid-class field-order="organisationID, productTypeID, statusID"
 */
public class IssueStatus 
implements Serializable{
//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String organisationID;
//
//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String issueID;
//
//	/**
//	 * @jdo.field primary-key="true"
//	 */
//	private int statusID;
//
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private Date timestamp;
//
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private boolean resolved;
//
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private boolean added;
//
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private boolean updated;
//
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private boolean closed;
//
//	protected IssueStatus()
//	{
//	}
//
//	public IssueStatus(/*ProductTypeStatusTracker productTypeStatusTracker, int statusID,*/ User user)
//	{
////		this.productTypeStatusTracker = productTypeStatusTracker;
////		this.organisationID = productTypeStatusTracker.getOrganisationID();
////		this.productTypeID = productTypeStatusTracker.getProductTypeID();
////		this.statusID = statusID;
////		this.productType = productTypeStatusTracker.getProductType();
////		this.user = user;
////		this.timestamp = new Date();
////
////		this.published = productType.isPublished();
////		this.confirmed = productType.isConfirmed();
////		this.saleable = productType.isSaleable();
////		this.closed = productType.isClosed();
//	}
//	public String getOrganisationID()
//	{
//		return organisationID;
//	}
//	public String getProductTypeID()
//	{
//		return productTypeID;
//	}
//	public int getStatusID()
//	{
//		return statusID;
//	}
//	public ProductTypeStatusTracker getProductTypeStatusTracker()
//	{
//		return productTypeStatusTracker;
//	}
//	public ProductType getProductType()
//	{
//		return productType;
//	}
//
//	public User getUser()
//	{
//		return user;
//	}
//	public Date getTimestamp()
//	{
//		return timestamp;
//	}
//
//	public boolean isPublished()
//	{
//		return published;
//	}
//	public boolean isConfirmed()
//	{
//		return confirmed;
//	}
//	public boolean isSaleable()
//	{
//		return saleable;
//	}
//	public boolean isClosed()
//	{
//		return closed;
//	}
}
