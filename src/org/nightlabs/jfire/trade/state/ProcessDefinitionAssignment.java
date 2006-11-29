package org.nightlabs.jfire.trade.state;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.Offer;

/**
 * Instances of this class control which ProcessDefinition will be instantiated for new
 * {@link Statable} instances (e.g. new {@link Offer}s).
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.ProcessDefinitionAssignmentID"
 *		detachable="true"
 *		table="JFireTrade_ProcessDefinitionAssignment"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ProcessDefinitionAssignment
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The fully qualified class name of a class implementing {@link Statable}. Usually, this
	 * will be one of {@link Offer}, {@link Invoice} or {@link DeliveryNote}.
	 *
	 * @jdo.field primary-key="true"
	 */
	private String statableClass;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private ProcessDefinition processDefinition;

	protected ProcessDefinitionAssignment()
	{
	}
	public ProcessDefinitionAssignment(String statableClass, ProcessDefinition processDefinition)
	{
		this.statableClass = statableClass;
		this.processDefinition = processDefinition;
	}

	public String getStatableClass()
	{
		return statableClass;
	}
	public void setProcessDefinition(ProcessDefinition processDefinition)
	{
		this.processDefinition = processDefinition;
	}
	public ProcessDefinition getProcessDefinition()
	{
		return processDefinition;
	}
}
