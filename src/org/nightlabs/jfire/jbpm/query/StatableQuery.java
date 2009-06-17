package org.nightlabs.jfire.jbpm.query;

import java.util.Date;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
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
	protected void prepareQuery(Query q)
	{
		StringBuffer filter = new StringBuffer();

		filter.append("true");

		if (isFieldEnabled(FieldName.stateDefinitionID) && stateDefinitionID != null)
		{
			if (onlyInSelectedState || isFieldEnabled(FieldName.onlyInSelectedState)) {
				filter.append("\n && (this.states.contains(stateVar))");
			}
			if (notInSelectedState || isFieldEnabled(FieldName.notInSelectedState)) {
				filter.append("\n && !(this.states.contains(stateVar))");
			}

			// TODO: JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
			// WORKAROUND:
			filter.append("\n && (" +
					"this.state.stateDefinition.processDefinitionOrganisationID == \""+stateDefinitionID.processDefinitionOrganisationID+"\" && " +
					"this.state.stateDefinition.processDefinitionID == \""+stateDefinitionID.processDefinitionID+"\" && " +
					"this.state.stateDefinition.stateDefinitionOrganisationID == \""+stateDefinitionID.stateDefinitionOrganisationID+"\" && " +
					"this.state.stateDefinition.stateDefinitionID == \""+stateDefinitionID.stateDefinitionID+"\"" +
					")");
		}

		if (isFieldEnabled(FieldName.processDefinitionID) && processDefinitionID != null)
		{
			filter.append("\n && (" +
					"JDOHelper.getObjectId(this.state.stateDefinition.processDefinition) == :processDefinitionID" +
			")");
		}

		if (isFieldEnabled(FieldName.stateCreateDTMin) && stateCreateDTMin != null)
			filter.append("\n && this.createDT >= :stateCreateDTMin");

		if (isFieldEnabled(FieldName.stateCreateDTMax) && stateCreateDTMax != null)
			filter.append("\n && this.createDT <= :stateCreateDTMax");

		logger.debug("filter == "+filter);

		q.setFilter(filter.toString());
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
}