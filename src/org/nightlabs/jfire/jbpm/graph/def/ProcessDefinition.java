package org.nightlabs.jfire.jbpm.graph.def;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.jbpm.JbpmContext;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID"
 *		detachable="true"
 *		table="JFireJbpm_ProcessDefinition"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, processDefinitionID"
 */
public class ProcessDefinition
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static ProcessDefinitionID getProcessDefinitionID(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		String pdName = jbpmProcessDefinition.getName();
		String[] parts = pdName.split("/");
		if (parts.length != 2)
			throw new IllegalArgumentException("jbpmProcessDefinition.name must contain exactly one '/' - i.e. it must be composed out of organisationID and processDefinitionIDString: " + pdName);

		return ProcessDefinitionID.create(parts[0], parts[1]);
	}

	/**
	 * @param jbpmProcessDefinitionURL The URL pointing to the processdefinition.xml file
	 * @return the newly created {@link ProcessDefinition} (or the old one with a new version, if it previously existed.
	 * @throws IOException 
	 */
	public static org.jbpm.graph.def.ProcessDefinition readProcessDefinition(URL jbpmProcessDefinitionURL)
	throws IOException
	{
		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition;

		InputStream in = jbpmProcessDefinitionURL.openStream();
		if (in == null)
			throw new FileNotFoundException("Could not open input stream for " + jbpmProcessDefinitionURL);
		try {
			Reader reader = new InputStreamReader(in);
			JpdlXmlReader jpdlXmlReader = new JpdlXmlReader(reader);
			jbpmProcessDefinition = jpdlXmlReader.readProcessDefinition();
			jpdlXmlReader.close();

		} finally {
			in.close();
		}

		return jbpmProcessDefinition;
	}

	/**
	 * @param jbpmContext The jbpm context to work in. Can be <code>null</code> which will cause a context to be implicitely created and closed.
	 * @param jbpmProcessDefinitionURL The URL pointing to the processdefinition.xml file
	 * @return the newly created {@link ProcessDefinition} (or the old one with a new version, if it previously existed.
	 * @throws IOException 
	 */
	public static ProcessDefinition storeProcessDefinition(PersistenceManager pm, JbpmContext jbpmContext, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		pm.getExtent(ProcessDefinition.class);

		boolean closeJbpmContext = false;
		if (jbpmContext == null) {
			closeJbpmContext = true;
			jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
		}
		try {
			jbpmContext.deployProcessDefinition(jbpmProcessDefinition);

			ProcessDefinitionID processDefinitionID = getProcessDefinitionID(jbpmProcessDefinition);
			ProcessDefinition processDefinition;
			try {
				processDefinition = (ProcessDefinition) pm.getObjectById(processDefinitionID);
				processDefinition.createProcessDefinitionVersion(jbpmProcessDefinition);
			} catch (JDOObjectNotFoundException x) {
				processDefinition = (ProcessDefinition) pm.makePersistent(new ProcessDefinition(processDefinitionID, jbpmProcessDefinition));
			}
			return processDefinition;
		} finally {
			if (closeJbpmContext)
				jbpmContext.close();
		}
	}

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
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private ProcessDefinitionVersion processDefinitionVersion;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="ProcessDefinitionVersion"
	 *		dependent-element="true"
	 */
	private List<ProcessDefinitionVersion> processDefinitionVersions;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProcessDefinition()
	{
	}
	protected ProcessDefinition(ProcessDefinitionID processDefinitionID, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		this(processDefinitionID.organisationID, processDefinitionID.processDefinitionID, jbpmProcessDefinition);
	}
	protected ProcessDefinition(String organisationID, String processDefinitionID, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		this.organisationID = organisationID;
		this.processDefinitionID = processDefinitionID;
		processDefinitionVersions = new ArrayList<ProcessDefinitionVersion>();
		createProcessDefinitionVersion(jbpmProcessDefinition);
	}

	/**
	 * Creates a new instance of {@link ProcessDefinitionVersion} from the passed <code>jbpmProcessDefinition</code>
	 * and makes it the current version (retrievable via {@link #getProcessDefinitionVersion()}).
	 * @param jbpmProcessDefinition
	 * @return
	 */
	public ProcessDefinitionVersion createProcessDefinitionVersion(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		processDefinitionVersion = new ProcessDefinitionVersion(this, jbpmProcessDefinition);
		processDefinitionVersions.add(processDefinitionVersion);
		return processDefinitionVersion;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getProcessDefinitionID()
	{
		return processDefinitionID;
	}

	/**
	 * Returns the current (= newest) version of this <code>ProcessDefinition</code>.
	 *
	 * @return the current (= newest) version of this <code>ProcessDefinition</code>. 
	 */
	public ProcessDefinitionVersion getProcessDefinitionVersion()
	{
		return processDefinitionVersion;
	}

	/**
	 * Returns a read-only list of all versions (newest last).
	 * @return a read-only list of all versions (newest last).
	 */
	public List<ProcessDefinitionVersion> getProcessDefinitionVersions()
	{
		return Collections.unmodifiableList(processDefinitionVersions);
	}
}
