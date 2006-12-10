package org.nightlabs.jfire.jbpm.graph.def;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.jbpm.JbpmContext;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.util.Utils;

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
	 * @jdo.column length="50"
	 */
	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true" indexed="true"
	 */
	private long jbpmProcessDefinitionId;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProcessDefinition processDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column jdbc-type="LONGVARCHAR"
	 */
	private String processDefinitionXml;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column jdbc-type="LONGVARCHAR"
	 */
	private String gpdXml;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String processImageType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte[] processImageData;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProcessDefinitionVersion()
	{
	}

	protected ProcessDefinitionVersion(
			ProcessDefinition processDefinition, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition,
			URL jbpmProcessDefinitionURL)
	throws IOException
	{
		this.processDefinition = processDefinition;
		this.organisationID = processDefinition.getOrganisationID();
		this.processDefinitionID = processDefinition.getProcessDefinitionID();
		this.jbpmProcessDefinitionId = jbpmProcessDefinition.getId();

		loadFiles(jbpmProcessDefinitionURL);
	}

	private void loadFiles(URL jbpmProcessDefinitionURL)
	throws IOException
	{
		InputStream in;

		in = new URL(jbpmProcessDefinitionURL, "processdefinition.xml").openStream();
		try {
			this.processDefinitionXml = Utils.readTextFile(in);
		} finally {
			in.close();
		}

		in = new URL(jbpmProcessDefinitionURL, "gpd.xml").openStream();
		try {
			this.gpdXml = Utils.readTextFile(in);
		} finally {
			in.close();
		}

		this.processImageType = "image/jpeg"; // currently, it's always a jpg

		in = new URL(jbpmProcessDefinitionURL, "processimage.jpg").openStream();
		try {
			this.processImageData = new DataBuffer(20480, Integer.MAX_VALUE, in).createByteArray();
		} finally {
			in.close();
		}
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getProcessDefinitionID()
	{
		return processDefinitionID;
	}
	public ProcessDefinition getProcessDefinition()
	{
		return processDefinition;
	}
	public long getJbpmProcessDefinitionId()
	{
		return jbpmProcessDefinitionId;
	}

	public org.jbpm.graph.def.ProcessDefinition getJbpmProcessDefinition(JbpmContext jbpmContext)
	{
		return jbpmContext.getGraphSession().getProcessDefinition(jbpmProcessDefinitionId);
	}

	public String getProcessDefinitionXml()
	{
		return processDefinitionXml;
	}
	public String getGpdXml()
	{
		return gpdXml;
	}
	public String getProcessImageType()
	{
		return processImageType;
	}
	public byte[] getProcessImageData()
	{
		return processImageData;
	}
}
