package org.nightlabs.jfire.jbpm.extension;

import org.nightlabs.i18n.I18nTextBuffer;


/**
 * 
 * this class serves as a container of information of the extended node
 * of a process definition extended JBPM file.
 *
 * @author Fitas Amine - fitas at nightlabs dot de)
 * 
 */
public class ExtendedNodeDescriptor {
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	protected static final char PREFIX_SEPARATOR = '#';
	protected static final char PREFIX_SUBNODE = '$';	
	/**
	 * The Node ID which consists of Node type + PREFIX_SEPARATOR + Node name.
	 */	
	private String nodeID;
	/**
	 * the extended node description mark tag.
	 */		
	private I18nTextBuffer description; // the extended node description
	/**
	 * the extended node name tag.
	 */		
	private I18nTextBuffer name;
	/**
	 * the icon reference file of the node.
	 */		
	private String iconFile;
	/**
	 * if we should mark a transition as 'userExecutable' {@link org.nightlabs.jfire.jbpm.graph.def.Transition#isUserExecutable()} .
	 */		
	private Boolean UserExecutable;
	/**
	 * mark a state/node as 'publicState'{@link org.nightlabs.jfire.jbpm.graph.def.StateDefinition#isPublicState()).
	 */			
	private Boolean publicState;

	public I18nTextBuffer getName() {
		return name;
	}

	public void setName(I18nTextBuffer name) {
		this.name = name;
	}

	public ExtendedNodeDescriptor() {
		super();
	}

	public ExtendedNodeDescriptor(String nodeID, I18nTextBuffer name, I18nTextBuffer description) {
		super();
		this.nodeID = nodeID;
		this.description = description;
		this.name = name;
		this.UserExecutable = false;	
		this.publicState = false;		
	}

	public String getNodeID() {
		return nodeID;
	}
	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public I18nTextBuffer getDescription() {
		return description;
	}

	public void setDescription(I18nTextBuffer description) {
		this.description = description;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}
	/**
	 * creates a new node ID.
	 *   
	 * @param nodetype the node type of JBPM examples (start-state, end-state etc..).
	 * @param name the the actuall name of the node or the attribute name.
	 */		
	public static String createNodeIDPrefix(String nodetype,String name)
	{	
		return nodetype + PREFIX_SEPARATOR + name;
	}
	/**
	 * creates a new  subnode ID.
	 *  
	 * @param node the node ID.
	 * @param subnode the subnode ID.
	 */		
	public static String createSubNodeIDPrefix(String node,String subnode)
	{	
		StringBuilder sb = new StringBuilder();
		if (node != null && !node.isEmpty())
			sb.append(node).append(PREFIX_SUBNODE);
		sb.append(subnode);
		return sb.toString();
	}

	public Boolean getUserExecutable() {
		return UserExecutable;
	}

	public void setUserExecutable(Boolean userExecutable) {
		UserExecutable = userExecutable;
	}

	public Boolean getPublicState() {
		return publicState;
	}

	public void setPublicState(Boolean publicState) {
		this.publicState = publicState;
	}


}


