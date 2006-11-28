package org.nightlabs.jfire.jbpm.graph.def;

import java.io.Serializable;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionVersionID"
 *		detachable="true"
 *		table="JFireJbpm_ProcessDefinitionVersion"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, processDefinitionID, jbpmProcessDefinitionId"
 */
public class ProcessDefinitionVersion
		implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true" indexed="true"
	 */
	private long jbpmProcessDefinitionId;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProcessDefinitionVersion()
	{
	}

	protected ProcessDefinitionVersion(ProcessDefinition processDefinition, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		this.organisationID = processDefinition.getOrganisationID();
		this.processDefinitionID = processDefinition.getProcessDefinitionID();
		this.jbpmProcessDefinitionId = jbpmProcessDefinition.getId();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getProcessDefinitionID()
	{
		return processDefinitionID;
	}
	public long getJbpmProcessDefinitionId()
	{
		return jbpmProcessDefinitionId;
	}
}
