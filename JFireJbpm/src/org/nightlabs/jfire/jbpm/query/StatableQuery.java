package org.nightlabs.jfire.jbpm.query;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID;

/**
 * A Query for searching for Implementations of {@link Statable}
 * org.nightlabs.jfire.jbpm.query
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class StatableQuery
	extends AbstractJDOQuery
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(StatableQuery.class);

	public static final class FieldName
	{
		public static final String onlyInSelectedState = "onlyInSelectedState";
		public static final String notInSelectedState = "notInSelectedState";
		public static final String selectedStatePassed = "selectedStatePassed";
		public static final String selectedStateNotPassed = "selectedStateNotPassed";
		public static final String stateCreateDTMax = "stateCreateDTMax";
		public static final String stateCreateDTMin = "stateCreateDTMin";
		public static final String stateDefinitionID = "stateDefinitionID";
		public static final String processDefinitionID = "processDefinitionID";
	}

	public StatableQuery()
	{
	}

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

	/**
	 *
	 * @param statableClass
	 */
	public void setStatableClass(Class<? extends Statable> statableClass)
	{
		this.statableClass = statableClass;
	}

	@Override
	protected Query createQuery()
	{
		return getPersistenceManager().newQuery(getStatableClass());
	}

	
	@Override
	protected void configureQuery(Query q)
	{
		super.configureQuery(q);
		q.setOrdering("this."+getCreateDTFieldName()+" DESCENDING");
	}
	
	
	@Override
	protected void prepareQuery(Query q)
	{
		StringBuffer filter = new StringBuffer();

		filter.append("true");

		if (isFieldEnabled(FieldName.stateDefinitionID) && stateDefinitionID != null)
		{
			if (onlyInSelectedState || isFieldEnabled(FieldName.onlyInSelectedState)) {
				filter.append("\n && JDOHelper.getObjectId(this.state.stateDefinition) == :stateDefinitionID");
			}
			if (notInSelectedState || isFieldEnabled(FieldName.notInSelectedState)) {
				filter.append("\n && JDOHelper.getObjectId(this.state.stateDefinition) != :stateDefinitionID");
			}
			if (selectedStatePassed || isFieldEnabled(FieldName.selectedStatePassed)) {
				filter.append("\n && (this.states.contains(stateVar))");
				filter.append("\n && (JDOHelper.getObjectId(stateVar.stateDefinition) == :stateDefinitionID)");
			}
			// Unfortunately needed to move that to postProcessing => Bug in Datanucleus
//			if (selectedStateNotPassed || isFieldEnabled(FieldName.selectedStateNotPassed)) {
//				filter.append("\n && (!this.states.contains(stateVar))");
//				filter.append("\n && (JDOHelper.getObjectId(stateVar.stateDefinition) == :stateDefinitionID)");
//			}
		}

		if (isFieldEnabled(FieldName.processDefinitionID) && processDefinitionID != null)
		{
			filter.append("\n && (" +
					"JDOHelper.getObjectId(this.state.stateDefinition.processDefinition) == :processDefinitionID" +
			")");
		}

		if (isFieldEnabled(FieldName.stateCreateDTMin) && stateCreateDTMin != null)
			filter.append("\n && this."+getCreateDTFieldName()+" >= :stateCreateDTMin");

		if (isFieldEnabled(FieldName.stateCreateDTMax) && stateCreateDTMax != null)
			filter.append("\n && this."+getCreateDTFieldName()+" <= :stateCreateDTMax");

		logger.debug("filter == "+filter);

		q.setFilter(filter.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object postProcessQueryResult(Object result) {
		if (selectedStateNotPassed || isFieldEnabled(FieldName.selectedStateNotPassed)) {
			StateDefinition stateDef = (StateDefinition) getPersistenceManager().getObjectById(stateDefinitionID);
			Collection<Statable> statableResults = new LinkedList<Statable>();
			if (result instanceof Collection) {
				Collection<Object> resultCol = (Collection<Object>) result;
				resultRowLoop: for (Object object : resultCol) {
					if (object instanceof Statable) {
						Statable statable = (Statable) object;
						for (State state : statable.getStates()) {
							if (state.getStateDefinition().equals(stateDef)) {
								continue resultRowLoop;
							}
						}
						statableResults.add(statable);
					} else {
						return super.postProcessQueryResult(result);
					}
				}
				return statableResults;
			}
		}
		return super.postProcessQueryResult(result);
	}

	/**
	 * the {@link StateDefinitionID} to search for
	 */
	private StateDefinitionID stateDefinitionID = null;
	public StateDefinitionID getStateDefinitionID() {
		return stateDefinitionID;
	}
	public void setStateDefinitionID(StateDefinitionID stateDefinitionID)
	{
		final StateDefinitionID oldStateDefinitionID = this.stateDefinitionID;
		this.stateDefinitionID = stateDefinitionID;
		notifyListeners(FieldName.stateDefinitionID, oldStateDefinitionID, stateDefinitionID);
	}

	private ProcessDefinitionID processDefinitionID = null;
	/**
	 * @return the processDefinitionID
	 */
	public ProcessDefinitionID getProcessDefinitionID() {
		return processDefinitionID;
	}

	/**
	 * @param processDefinitionID the processDefinitionID to set
	 */
	public void setProcessDefinitionID(ProcessDefinitionID processDefinitionID) {
		this.processDefinitionID = processDefinitionID;
	}

	/**
	 * determines the lower limit of the createDT of the {@link Statable} for the given
	 * stateDefinitionID
	 */
	private Date stateCreateDTMin = null;
	public Date getStateCreateDTMin() {
		return stateCreateDTMin;
	}
	public void setStateCreateDTMin(Date stateCreateDTMin)
	{
		final Date oldStateCreateDTMin = this.stateCreateDTMin;
		this.stateCreateDTMin = stateCreateDTMin;
		notifyListeners(FieldName.stateCreateDTMin, oldStateCreateDTMin, stateCreateDTMin);
	}

	/**
	 * determines the upper limit of the createDT of the {@link Statable} for the given
	 * stateDefinitionID
	 */
	private Date stateCreateDTMax = null;
	public Date getStateCreateDTMax() {
		return stateCreateDTMax;
	}
	public void setStateCreateDTMax(Date stateCreateDTMax)
	{
		final Date oldStateCreateDTMax = this.stateCreateDTMax;
		this.stateCreateDTMax = stateCreateDTMax;
		notifyListeners(FieldName.stateCreateDTMax, oldStateCreateDTMax, stateCreateDTMax);
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
	public void setOnlyInSelectedState(boolean onlyInSelectedState)
	{
		final Boolean oldOnlyInSelectedState = this.onlyInSelectedState;
		this.onlyInSelectedState = onlyInSelectedState;
		notifyListeners(FieldName.onlyInSelectedState, oldOnlyInSelectedState,
			Boolean.valueOf(onlyInSelectedState));
	}

	private boolean notInSelectedState = false;

	/**
	 * @return the notInSelectedState
	 */
	public boolean isNotInSelectedState() {
		return notInSelectedState;
	}

	/**
	 * @param notInSelectedState the notInSelectedState to set
	 */
	public void setNotInSelectedState(boolean notInSelectedState) {
		final Boolean oldNotInSelectedState = this.notInSelectedState;
		this.notInSelectedState = notInSelectedState;
		notifyListeners(FieldName.notInSelectedState, oldNotInSelectedState,
			Boolean.valueOf(notInSelectedState));
	}
	
	
	private boolean selectedStatePassed = false;
	
	/**
	 * @return the selectedStatePassed
	 */
	public boolean isSelectedStatePassed() {
		return selectedStatePassed;
	}

	/**
	 * @param selectedStatePassed the selectedStatePassed to set
	 */
	public void setSelectedStatePassed(boolean selectedStatePassed) {
		final Boolean oldSelectedStatePassed = this.selectedStatePassed;
		this.selectedStatePassed = selectedStatePassed;
		notifyListeners(FieldName.selectedStatePassed, oldSelectedStatePassed,
			Boolean.valueOf(selectedStatePassed));
	}
	
	private boolean selectedStateNotPassed = false;
	
	/**
	 * @return the selectedStateNotPassed
	 */
	public boolean isSelectedNotStatePassed() {
		return selectedStateNotPassed;
	}

	/**
	 * @param selectedStateNotPassed the selectedStateNotPassed to set
	 */
	public void setSelectedStateNotPassed(boolean selectedStateNotPassed) {
		final Boolean oldSelectedStateNotPassed = this.selectedStateNotPassed;
		this.selectedStateNotPassed = selectedStateNotPassed;
		notifyListeners(FieldName.selectedStateNotPassed, oldSelectedStateNotPassed,
			Boolean.valueOf(selectedStateNotPassed));
	}

	@Override
	protected Class<Statable> initCandidateClass()
	{
		return Statable.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<? extends Statable> getCandidateClass()
	{
		if (statableClass != null)
			return statableClass;

		Class<?> cc = super.getCandidateClass();
		if (cc == null) // FIXME is this allowed?
			return null;

		if (!Statable.class.isAssignableFrom(cc))
			throw new ClassCastException("super.getCandidateClass() returned " + cc + " which does not implement " + Statable.class + "!!!");

		return (Class<? extends Statable>) cc;
	}

	/**
	 * We assume here that the result class == the set Statable class, but we can only return the
	 * correct information when we have it.
	 */
	@Override
	public Class<?> getResultClass()
	{
		return statableClass != null ? statableClass : super.getResultClass();
	}

	protected String getCreateDTFieldName() {
		return "createDT";
	}
}