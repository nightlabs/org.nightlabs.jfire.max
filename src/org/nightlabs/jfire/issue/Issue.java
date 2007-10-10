/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Utils;

/**
 * @author Chairat Kongarayawetchakun <!-- chairatk at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueID"
 *		detachable="true"
 *		table="JFireIssueTracking_Issue"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, issueID, issueTypeID"
 *
 * jdo.fetch-group name="Issue.createUser" fields="createUser"
 *
 **/
public class Issue
implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueTypeID;


	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date timestamp;

	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	protected Issue() { }

	public Issue(User creator)
	{
		if (creator == null)
			throw new NullPointerException("creator");

	}

	public String getOrganisationID() {
		return organisationID;
	}

	public static String getPrimaryKey(String organisationID, String deliveryNoteIDPrefix, long deliveryNoteID)
	{
		return organisationID + '/' + deliveryNoteIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(deliveryNoteID);
	}

	public String getPrimaryKey()
	{
		return ""/*primaryKey*/;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Issue is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	public void jdoPreDetach()
	{
	}
	public void jdoPostDetach(Object _attached)
	{
		Issue attached = (Issue)_attached;
		Issue detached = this;
		Collection fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof Issue))
			return false;

		Issue o = (Issue) obj;

		return
		Utils.equals(this.organisationID, o.organisationID); //&& 
	}

	@Override
	public int hashCode()
	{
		return
		Utils.hashCode(this.organisationID); //^
	}
}
