package org.nightlabs.jfire.jbpm.query;

import java.util.Date;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;

/**
 * A Query for searching for Implementations of {@link Statable}
 * org.nightlabs.jfire.jbpm.query
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class StatableQuery
	extends AbstractJDOQuery<Statable>
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(StatableQuery.class);
	
//	public abstract Class getStatableClass();
	
	public StatableQuery(Class<? extends Statable> statableClass)
	{
		if (statableClass == null)
			throw new IllegalArgumentException("Param statableClass must not be null");
		
		if (!Statable.class.isAssignableFrom(statableClass))
			throw new IllegalArgumentException("Param statableClass must implement the interface "+Statable.class);
		
		this.statableClass = statableClass;
	}
	
	/**
	 * the Implementation class of the {@link Statable} Interface
	 */
	private Class<? extends Statable> statableClass = null;
	public Class<? extends Statable> getStatableClass() {
		return statableClass;
	}
	
	@Override
	protected Query prepareQuery()
	{
		Query q = getPersistenceManager().newQuery(getStatableClass());
		StringBuffer filter = new StringBuffer();
		
		filter.append(" true");
		
		if (stateDefinitionID != null)
		{
			if (!onlyInSelectedState) {
				filter.append("\n && (this.states.contains(stateVar) && ( " +
						"stateVar.stateDefinition.processDefinitionOrganisationID == \""+stateDefinitionID.processDefinitionOrganisationID+"\" && " +
						"stateVar.stateDefinition.processDefinitionID == \""+stateDefinitionID.processDefinitionID+"\" && " +
						"stateVar.stateDefinition.stateDefinitionOrganisationID == \""+stateDefinitionID.stateDefinitionOrganisationID+"\" && " +
						"stateVar.stateDefinition.stateDefinitionID == \""+stateDefinitionID.stateDefinitionID+"\"" +
						"))");
				
				q.declareImports("import "+State.class.getName());
				q.declareVariables(State.class.getName()+" stateVar");
			} else {
				// TODO: JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//				filter.append("\n && JDOHelper.getObjectId(this.stateDefinition) == :stateDefinitionID");
				// WORKAROUND:
				filter.append("\n && (" +
						"this.state.stateDefinition.processDefinitionOrganisationID == \""+stateDefinitionID.processDefinitionOrganisationID+"\" && " +
						"this.state.stateDefinition.processDefinitionID == \""+stateDefinitionID.processDefinitionID+"\" && " +
						"this.state.stateDefinition.stateDefinitionOrganisationID == \""+stateDefinitionID.stateDefinitionOrganisationID+"\" && " +
						"this.state.stateDefinition.stateDefinitionID == \""+stateDefinitionID.stateDefinitionID+"\"" +
						")");
			}
		}

		if (stateCreateDTMin != null)
			filter.append("\n && this.createDT >= :stateCreateDTMin");

		if (stateCreateDTMax != null)
			filter.append("\n && this.createDT <= :stateCreateDTMax");
	
		logger.debug("filter == "+filter);
		
		q.setFilter(filter.toString());
		
		return q;
	}

	/**
	 * the {@link StateDefinitionID} to search for
	 */
	private StateDefinitionID stateDefinitionID = null;
	public StateDefinitionID getStateDefinitionID() {
		return stateDefinitionID;
	}
	public void setStateDefinitionID(StateDefinitionID stateDefinitionID) {
		this.stateDefinitionID = stateDefinitionID;
	}

	/**
	 * determines the lower limit of the createDT of the {@link Statable} for the given
	 * stateDefinitionID
	 */
	private Date stateCreateDTMin = null;
	public Date getStateCreateDTMin() {
		return stateCreateDTMin;
	}
	public void setStateCreateDTMin(Date stateCreateDTMin) {
		this.stateCreateDTMin = stateCreateDTMin;
	}

	/**
	 * determines the upper limit of the createDT of the {@link Statable} for the given
	 * stateDefinitionID
	 */
	private Date stateCreateDTMax = null;
	public Date getStateCreateDTMax() {
		return stateCreateDTMax;
	}
	public void setStateCreateDTMax(Date stateCreateDTMax) {
		this.stateCreateDTMax = stateCreateDTMax;
	}

	/**
	 * if onlyInSelectedState is true, the query matches only, if the StateDefinitionID of the
	 * Statable.getState().getStateDefinition() is exact the one in the query
	 * 
	 * if onlyInSelectedState is false the query matches, if the {@link Statable} once,
	 * has passed the getStateDefinitionID() of the query
	 * 
	 */
	private boolean onlyInSelectedState = false;
	public boolean isOnlyInSelectedState() {
		return onlyInSelectedState;
	}
	public void setOnlyInSelectedState(boolean onlyInSelectedState) {
		this.onlyInSelectedState = onlyInSelectedState;
	}

	@Override
	protected Class<Statable> init()
	{
		return Statable.class;
	}
	
}
