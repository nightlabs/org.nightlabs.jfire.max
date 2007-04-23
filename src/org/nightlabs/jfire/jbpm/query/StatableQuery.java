package org.nightlabs.jfire.jbpm.query;

import java.util.Date;
import java.util.Set;

import javax.jdo.Query;

import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;

/**
 * A Query for searching for Implementations of {@link Statable}
 * This {@link JDOQuery} assumes that the fields implementated
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class StatableQuery 
extends JDOQuery<Set<Statable>> 
{	
	@Override
	protected Query prepareQuery() 
	{
		Query q = getPersistenceManager().newQuery(Statable.class);
		StringBuffer filter = new StringBuffer();
		
		filter.append(" true");
		
		if (stateDefinitionID != null) 
		{
			// TODO: JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//				filter.append("\n && JDOHelper.getObjectId(this.stateDefinition) == :stateDefinitionID");
			// WORKAROUND:
			filter.append("\n && (" +
					"this.stateDefinition.organisationID == \""+stateDefinitionID.processDefinitionOrganisationID+"\" && " +
					"this.stateDefinition.processDefinitionID == \""+stateDefinitionID.processDefinitionID+"\" && " +
					"this.stateDefinition.stateDefinitionOrganisationID == \""+stateDefinitionID.stateDefinitionOrganisationID+"\"" +
					"this.stateDefinition.stateDefinitionID == \""+stateDefinitionID.stateDefinitionID+"\"" +					
							")");
		}

		if (stateCreateDTMin != null)
			filter.append("\n && this.createDT >= :stateCreateDTMin");

		if (stateCreateDTMax != null)
			filter.append("\n && this.createDT >= :stateCreateDTMax");		
		
		return q;
	}

	private StateDefinitionID stateDefinitionID = null;
	public StateDefinitionID getStateDefinitionID() {
		return stateDefinitionID;
	}
	public void setStateDefinitionID(StateDefinitionID stateDefinitionID) {
		this.stateDefinitionID = stateDefinitionID;
	}
	
	private Date stateCreateDTMin = null;
	public Date getStateCreateDTMin() {
		return stateCreateDTMin;
	}
	public void setStateCreateDTMin(Date stateCreateDTMin) {
		this.stateCreateDTMin = stateCreateDTMin;
	}

	private Date stateCreateDTMax = null;
	public Date getStateCreateDTMax() {
		return stateCreateDTMax;
	}
	public void setStateCreateDTMax(Date stateCreateDTMax) {
		this.stateCreateDTMax = stateCreateDTMax;
	}

}
