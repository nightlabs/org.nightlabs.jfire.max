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
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.extension.ExtendedNodeDescriptor;
import org.nightlabs.jfire.jbpm.extension.ExtendedProcessDefinitionDescriptor;
import org.nightlabs.jfire.jbpm.extension.JpdlXmlExtensionReader;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.security.User;



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
 *
 * @jdo.fetch-group name="ProcessDefinition.this" fetch-groups="default" fields="processDefinitionVersion, processDefinitionVersions"
 */
@PersistenceCapable(
	objectIdClass=ProcessDefinitionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireJbpm_ProcessDefinition")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name=ProcessDefinition.FETCH_GROUP_THIS_PROCESS_DEFINITION,
		members={@Persistent(name="processDefinitionVersion"), @Persistent(name="processDefinitionVersions")})
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProcessDefinition
implements Serializable
{
	private static final Logger logger = Logger.getLogger(ProcessDefinition.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_PROCESS_DEFINITION = "ProcessDefinition.this";

	public static State createStartState(PersistenceManager pm, User user, Statable statable,
			org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		if (logger.isDebugEnabled())
			logger.debug("createStartState: user=" + JDOHelper.getObjectId(user) + " statable=" + JDOHelper.getObjectId(statable) + " jbpmProcessDefinition=" + JDOHelper.getObjectId(jbpmProcessDefinition));

		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(StateDefinition.getStateDefinitionID(jbpmProcessDefinition.getStartState()));

		if (logger.isDebugEnabled())
			logger.debug("createStartState: stateDefinition=" + JDOHelper.getObjectId(stateDefinition));

		return stateDefinition.createState(user, statable);
//		return (State) pm.makePersistent(
//				new State(
//						IDGenerator.getOrganisationID(), IDGenerator.nextID(State.class),
//						user, statable, stateDefinition));
//		return stateDefinition.createState(user, statable);
	}

	public static ProcessDefinitionID getProcessDefinitionID(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		String pdName = jbpmProcessDefinition.getName();
		String[] parts = pdName.split(":");
		if (parts.length != 2)
			throw new IllegalArgumentException("jbpmProcessDefinition.name must contain exactly one ':' - i.e. it must be composed out of organisationID and processDefinitionIDString: " + pdName);

		return ProcessDefinitionID.create(parts[0], parts[1]);
	}

	/**
	 * @param jbpmProcessDefinitionURL The URL pointing to the processdefinition.xml file's directory
	 * @return the newly created {@link ProcessDefinition} (or the old one with a new version, if it previously existed.
	 * @throws IOException
	 */
	public static org.jbpm.graph.def.ProcessDefinition readProcessDefinition(URL jbpmProcessDefinitionURL)
	throws IOException
	{
		if (jbpmProcessDefinitionURL == null)
			throw new IllegalArgumentException("jbpmProcessDefinitionURL == null");

		jbpmProcessDefinitionURL = new URL(jbpmProcessDefinitionURL, "processdefinition.xml");

		try {
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
		} catch (Throwable t) {
			logger.error("reading process definition failed: " + jbpmProcessDefinitionURL, t);
			if (t instanceof IOException)
				throw (IOException)t;
			if (t instanceof RuntimeException)
				throw (RuntimeException)t;
			throw new RuntimeException(t);
		}
	}

	/**
	 * @param jbpmContext The jbpm context to work in. Can be <code>null</code> which will cause a context to be implicitely created and closed.
	 * @param jbpmProcessDefinitionURL The URL pointing to the processdefinition.xml file's directory
	 * @return the newly created {@link ProcessDefinition} (or the old one with a new version, if it previously existed.
	 * @throws IOException
	 */
	public static ProcessDefinition storeProcessDefinition(
			PersistenceManager pm, JbpmContext jbpmContext, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition,
			URL jbpmProcessDefinitionURL)
	throws IOException
	{

		ExtendedProcessDefinitionDescriptor processDefinitionDescriptor = null;

		pm.getExtent(ProcessDefinition.class);

//		// register the main action handler ActionHandlerNodeEnter
//		ActionHandlerNodeEnter.register(jbpmProcessDefinition);

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
				processDefinition.createProcessDefinitionVersion(jbpmProcessDefinition, jbpmProcessDefinitionURL);
			} catch (JDOObjectNotFoundException x) {
				processDefinition = pm.makePersistent(
						new ProcessDefinition(processDefinitionID, jbpmProcessDefinition, jbpmProcessDefinitionURL));
			}

			URL jbpmExtensionURL = null;
			try {
				// read the jbpm extension process file
				jbpmExtensionURL = new URL(jbpmProcessDefinitionURL, "processdefinition-extension.xml");
				InputStream extensionInput = jbpmExtensionURL.openStream();
				// a simple check to throws I/O exception if file doesnt exist.
				if (jbpmExtensionURL.openConnection().getInputStream()!= null)
				{
					try {
						Reader extensionReader = new InputStreamReader(extensionInput);
						JpdlXmlExtensionReader jpdlXmlReaderExtension = new JpdlXmlExtensionReader(extensionReader);
						processDefinitionDescriptor = jpdlXmlReaderExtension.getExtendedProcessDefinitionDescriptor();
					} finally {
						extensionInput.close();
					}
				} else
					logger.info("Input stream is null in process definition extension:" + jbpmProcessDefinitionURL);

			}
			catch (FileNotFoundException e) {
				logger.info("No additional process definition extension file found: " + jbpmExtensionURL);
			}
			catch (Throwable t) {
				if (t instanceof IOException)
					logger.info("reading process definition extension failed because of IO Exception: " + jbpmProcessDefinitionURL, t);
				if (t instanceof RuntimeException)
					logger.info("reading process definition extension failed because of Runtime Exception: " + jbpmProcessDefinitionURL, t);
				else
					logger.info("reading process definition extension failed:" + jbpmProcessDefinitionURL, t);
			}

			// create StateDefinitions
			for (Iterator<?> itNode = jbpmProcessDefinition.getNodes().iterator(); itNode.hasNext(); ) {
				Node node = (Node) itNode.next();
				//				if (node instanceof StartState ||
				//						node instanceof EndState ||
				//						node instanceof org.jbpm.graph.node.State)
				//				{
				StateDefinition stateDefinition = pm.makePersistent(new StateDefinition(processDefinition, node));
				// look up for the extended node if it has matches
				if(processDefinitionDescriptor != null)
				{
					ExtendedNodeDescriptor extendedNode = processDefinitionDescriptor.getExtendedNodeDescriptor(node);
					if(extendedNode != null)
					{
						// set the name and description from the extended I18in Node
						stateDefinition.getName().copyFrom(extendedNode.getName());
						stateDefinition.getDescription().copyFrom(extendedNode.getDescription());
						stateDefinition.setPublicState(extendedNode.getPublicState());
					}

				}
				// create Transitions
				if (node.getLeavingTransitions() != null) {
					for (Iterator <?>itTransition = node.getLeavingTransitions().iterator(); itTransition.hasNext(); ) {
						org.jbpm.graph.def.Transition jbpmTransition = (org.jbpm.graph.def.Transition) itTransition.next();
						//							TransitionID transitionID = Transition.getTransitionID(jbpmTransition);
						Transition transition = pm.makePersistent(new Transition(stateDefinition, jbpmTransition.getName()));
						if(processDefinitionDescriptor != null)
						{
							ExtendedNodeDescriptor transitionExtendedNode = processDefinitionDescriptor.getExtendedNodeDescriptor(jbpmTransition);
							if( transitionExtendedNode != null)
							{
								// SET THE NAME AND DESCRIPTION FROM THE EXTENDED I18IN NODE
								transition.getName().copyFrom(transitionExtendedNode.getName());
								transition.getDescription().copyFrom(transitionExtendedNode.getDescription());
								transition.setUserExecutable(transitionExtendedNode.getUserExecutable());
							}
						}
					}
				}
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
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProcessDefinitionVersion processDefinitionVersion;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="ProcessDefinitionVersion"
	 *		dependent-element="true"
	 *		mapped-by="processDefinition"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="processDefinition",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<ProcessDefinitionVersion> processDefinitionVersions;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProcessDefinition()
	{
	}
	protected ProcessDefinition(ProcessDefinitionID processDefinitionID, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		this(processDefinitionID.organisationID, processDefinitionID.processDefinitionID, jbpmProcessDefinition,
				jbpmProcessDefinitionURL);
	}
	protected ProcessDefinition(String organisationID, String processDefinitionID, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition,
			URL jbpmProcessDefinitionURL)
	throws IOException
	{
		this.organisationID = organisationID;
		this.processDefinitionID = processDefinitionID;
		processDefinitionVersions = new ArrayList<ProcessDefinitionVersion>();
		createProcessDefinitionVersion(jbpmProcessDefinition, jbpmProcessDefinitionURL);
	}

	/**
	 * Creates a new instance of {@link ProcessDefinitionVersion} from the passed <code>jbpmProcessDefinition</code>
	 * and makes it the current version (retrievable via {@link #getProcessDefinitionVersion()}).
	 * @param jbpmProcessDefinition
	 * @return
	 * @throws IOException
	 */
	public ProcessDefinitionVersion createProcessDefinitionVersion(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		processDefinitionVersion = new ProcessDefinitionVersion(this, jbpmProcessDefinition, jbpmProcessDefinitionURL);
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

	public String getJbpmProcessDefinitionName()
	{
		return organisationID + ':' + processDefinitionID;
	}
}
