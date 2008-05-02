package org.nightlabs.jfire.trade.jbpm;

import java.io.Serializable;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.TradeSide;

/**
 * Instances of this class control which ProcessDefinition will be instantiated for new
 * {@link Statable} instances (e.g. new {@link Offer}s).
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID"
 *		detachable="true"
 *		table="JFireTrade_ProcessDefinitionAssignment"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="statableClass, tradeSide"
 *		include-body="id/ProcessDefinitionAssignmentID.body.inc"
 *
 * @jdo.query name="getProcessDefinitionsForStatableClass"
 *		query="SELECT processDefinition WHERE this.statableClass == :statableClass"
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ProcessDefinitionAssignment
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * returns a List of {@link ProcessDefinition} defined for the given {@link Statable} class
	 * 
	 * @param pm the {@link PersistenceManager} to use
	 * @param statableClass the name of the {@link Statable} class as String
	 * @return a List of {@link ProcessDefinition} defined for the given statable class
	 */
	public static List<ProcessDefinition> getProcessDefinitions(PersistenceManager pm, String statableClass)
	{
		Query q = pm.newNamedQuery(ProcessDefinitionAssignment.class, "getProcessDefinitionsForStatableClass");
		return (List<ProcessDefinition>) q.execute(statableClass);
	}
	
	/**
	 * The fully qualified class name of a class implementing {@link Statable}. Usually, this
	 * will be one of {@link Offer}, {@link Invoice} or {@link DeliveryNote}.
	 *
	 * @jdo.field primary-key="true"
	 */
	private String statableClass;

//	/**
//	 * @jdo.field primary-key="true" persistence-modifier="persistent"
//	 */
//	private TradeSide tradeSide;

	/**
	 * TODO This is only a String because of a jpox bug! It should be the enum directly!
	 *
	 * java.lang.ClassCastException: java.lang.String
	 *         at org.jpox.store.mapping.EnumMapping.setObject(EnumMapping.java:104)
	 *         at org.jpox.store.rdbms.query.StatementText.setParameters(StatementText.java:262)
	 *         at org.jpox.store.rdbms.query.StatementText.prepareStatement(StatementText.java:221)
	 *         at org.jpox.store.rdbms.query.StatementText.prepareStatement(StatementText.java:246)
	 *         at org.jpox.store.rdbms.RDBMSStoreHelper.getClassNameForIdKeyUsingDiscriminator(RDBMSStoreHelper.java:782)
	 *         at org.jpox.store.rdbms.RDBMSManager.getClassNameForObjectID(RDBMSManager.java:1302)
	 *         at org.jpox.AbstractPersistenceManager.getObjectById(AbstractPersistenceManager.java:2583)
	 *         at org.jpox.AbstractPersistenceManager.getObjectById(AbstractPersistenceManager.java:2532)
	 *         at org.jpox.resource.PersistenceManagerImpl.getObjectById(PersistenceManagerImpl.java:617)
	 *         at org.jpox.resource.PersistenceManagerImpl.getObjectById(PersistenceManagerImpl.java:605)
	 *         at org.nightlabs.jfire.trade.Trader.createOffer(Trader.java:586)
	 *
	 * @jdo.field primary-key="true" persistence-modifier="persistent"
	 * @jdo.column length="50"
	 */
	private String tradeSide;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private ProcessDefinition processDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private String jbpmProcessDefinitionName;

	protected ProcessDefinitionAssignment()
	{
	}
	public ProcessDefinitionAssignment(Class statableClass, TradeSide tradeSide, ProcessDefinition processDefinition)
	{
		this(statableClass.getName(), tradeSide, processDefinition);
		if (!Statable.class.isAssignableFrom(statableClass))
			throw new IllegalArgumentException("statableClass does not implement interface Statable: " + statableClass);
	}
	public ProcessDefinitionAssignment(String statableClass, TradeSide tradeSide, ProcessDefinition processDefinition)
	{
		this.statableClass = statableClass;
		this.tradeSide = tradeSide.toString();
		this.processDefinition = processDefinition;
		this.jbpmProcessDefinitionName = processDefinition.getJbpmProcessDefinitionName();
	}

	public String getStatableClass()
	{
		return statableClass;
	}
	public TradeSide getTradeSide()
	{
		return TradeSide.valueOf(tradeSide);
	}
	public void setProcessDefinition(ProcessDefinition processDefinition)
	{
		this.processDefinition = processDefinition;
	}
	public ProcessDefinition getProcessDefinition()
	{
		return processDefinition;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager!");
		return pm;
	}

	public ProcessInstance createProcessInstance(JbpmContext jbpmContext, User user, Statable statable)
	{
		boolean closeJbpmContext = false;
		if (jbpmContext == null) {
			closeJbpmContext = true;
			jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
		}
		try {
			ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate(
					this.getProcessDefinition().getJbpmProcessDefinitionName());

			statable.getStatableLocal().setJbpmProcessInstanceId(processInstance.getId());

			processInstance.getContextInstance().setVariable(AbstractActionHandler.VARIABLE_NAME_STATABLE_ID, JDOHelper.getObjectId(statable).toString());

			ActionHandlerNodeEnter.createStartState(
					getPersistenceManager(), user, statable, processInstance.getProcessDefinition());

			return processInstance;
		} finally {
			if (closeJbpmContext)
				jbpmContext.close();
		}
	}
}
