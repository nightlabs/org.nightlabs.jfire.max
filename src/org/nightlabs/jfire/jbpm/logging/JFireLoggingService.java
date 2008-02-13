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

import java.util.Iterator;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.jbpm.graph.log.TransitionLog;
import org.jbpm.logging.LoggingService;
import org.jbpm.logging.log.ProcessLog;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * 
 * This is a JFire specific class for Jbpm. It logs workflow
 * transitions with JFire User, JFire organistation, Date, etc.
 * 
 * @author Jasper Siepkes <siepkes[AT]serviceplanet[DOT]nl>
 */
public class JFireLoggingService implements LoggingService {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(JFireLoggingService.class);
	
	private PersistenceManager pm;
	
	private String OrganisationID;
	
	public JFireLoggingService () {
		logger.debug("Instantiated");
		//Get the ID of the current organisation
		this.OrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		//Obtain a persistency manager for the current organisation
		this.pm = new Lookup(OrganisationID).getPersistenceManager();
	}

	public void log(ProcessLog processLog) {
		if(processLog.getParent() != null) {
			if (processLog.getParent().getChildren() != null) {
				logger.debug("Processing "+processLog.getParent().getChildren().size()+" workflow log(s)");
				Iterator logIterator = processLog.getParent().getChildren().iterator();
				
				while (logIterator.hasNext()) {
					Object log = logIterator.next();
					if (log instanceof TransitionLog) { //We are only interested in workflow transitions.
					TransitionLog transitionLog = (TransitionLog)log;
					
					pm.makePersistent(new LogEntry(OrganisationID, processLog.getDate(),
							transitionLog.getSourceNode().getFullyQualifiedName(),
							transitionLog.getDestinationNode().getFullyQualifiedName(),
							transitionLog.getTransition().getName(),
							processLog.getToken().getProcessInstance().getId(),
							SecurityReflector.getUserDescriptor().getUser(pm)
					));
					}
				}
			}
		}
	}

	public void close() {
		//this.pm.close();
		logger.debug("Closed");
	}
}
