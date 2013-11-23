package org.nightlabs.jfire.jbpm.graph.def;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import org.jbpm.JbpmContext;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.util.IOUtil;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionVersionID;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;

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
@PersistenceCapable(
	objectIdClass=ProcessDefinitionVersionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireJbpm_ProcessDefinitionVersion")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProcessDefinitionVersion
		implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true" indexed="true"
	 */
	@Element(indexed="true")
	@PrimaryKey
	private long jbpmProcessDefinitionId;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProcessDefinition processDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String processDefinitionXml;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String gpdXml;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String processImageType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] processImageData;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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
			this.processDefinitionXml = IOUtil.readTextFile(in);
		} finally {
			in.close();
		}

		in = new URL(jbpmProcessDefinitionURL, "gpd.xml").openStream();
		try {
			this.gpdXml = IOUtil.readTextFile(in);
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
