/* JFire - it's hot - Free ERP System - http://jfire.org                       *
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

package org.nightlabs.jfire.jbpm.logging;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;

/**
 * @author Jasper Siepkes <siepkes[AT]serviceplanet[DOT]nl>
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.jbpm.logging.id.logEntryID"
 *		detachable = "true"
 *		table = "JFireJbpm_Log"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class field-order = "organisationID, logEntryID"
 */
public class LogEntry implements Serializable {

	////////BEGIN OF PRIMARY KEY(S) ////////////
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long logEntryID;
	////////END OF PRIMARY KEY(S) //////////////
	
	/**
	 * The date the workflow transition took place.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date date;
	
	/**
	 * The name of the Transition from source to destination node.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String transitionName;
	
	/**
	 * The user who initiated the workflow transition.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User user;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long processInstanceID;
	
	/**
	 * The node where the token was before the transition.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String sourceNode;
	
	/**
	 * The node where the token has traveld to.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String destinationNode;

	//////// BEGIN OF CONSTRUCTORS /////////////
	/**
	 * @deprecated This constructor is here because JDO requires it. Its not intended for any other purpose!
	 */
	@Deprecated
	protected LogEntry() { }
	
	/**
	 * Default constructor
	 * 
	 * @param organisationID - The ID of the organisation in which the workflow transition takes place.
	 * @param date - The Date the start of the workflow transition takes place.
	 * @param sourceNode - The Workflow node from which the transition originates (Where the token started).
	 * @param destinationNode - The Workflow node the transition leads to (Where the token is now). 
	 * @param transitionName - The name of the transition itself.
	 * @param processInstanceID - The ID of the instance of the workflow.
	 * @param user - The User who initiated the transition.
	 */
	public LogEntry (String organisationID, 
			Date date, 
			String sourceNode, 
			String destinationNode, 
			String transitionName,
			long processInstanceID,
			User user) {
		this.organisationID = organisationID;
		this.logEntryID = createLogEntryID();
		this.date = date;
		this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
		this.transitionName = transitionName;
		this.processInstanceID = processInstanceID;
		this.user = user;
	}

	//////END OF CONSTRUCTORS /////////////////
	
	public static long createLogEntryID() {
		return IDGenerator.nextID(LogEntry.class);
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDestinationNode() {
		return destinationNode;
	}

	public void setDestinationNode(String destinationNode) {
		this.destinationNode = destinationNode;
	}

	public long getLogEntryID() {
		return logEntryID;
	}

	public void setLogEntryID(long logEntryID) {
		this.logEntryID = logEntryID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	public long getProcessInstanceID() {
		return processInstanceID;
	}

	public void setProcessInstanceID(long processID) {
		this.processInstanceID = processID;
	}

	public String getSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(String sourceNode) {
		this.sourceNode = sourceNode;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setTransitionName(String transitionName) {
		this.transitionName = transitionName;
	}

	public String getTransitionName() {
		return transitionName;
	}	
}
