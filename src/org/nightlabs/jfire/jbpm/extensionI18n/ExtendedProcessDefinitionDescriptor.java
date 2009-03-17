/**
 * 
 */
package org.nightlabs.jfire.jbpm.extensionI18n;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.NodeTypes;

/**
 * 
 *  this class serves as utility map for holding the {@link ExtendedNodeDescriptor) classes 
 *   
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ExtendedProcessDefinitionDescriptor {

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;


	private Map<String, ExtendedNodeDescriptor> descriptors = new HashMap<String, ExtendedNodeDescriptor>();

	
	public ExtendedProcessDefinitionDescriptor() {
	}

	
	protected void addNodeDescriptor(String nodeID, ExtendedNodeDescriptor descriptor) {
		descriptors.put(nodeID, descriptor);
	}
	
	public Collection<ExtendedNodeDescriptor> getExtendedNodeDescriptors() {
		return Collections.unmodifiableCollection(descriptors.values());
	}
	/**
	 * Retrieves the ExtendedNodeDescriptor by a given Node.
	 * 
	 * @param node the node to be searched of.
	 */
	public ExtendedNodeDescriptor getExtendedNodeDescriptor(Node node) {
		// construct the ID of the Map
		String nodeID = ExtendedNodeDescriptor.createNodeIDPrefix(NodeTypes.getNodeName(node.getClass()), node.getName());
		return getExtendedNodeDescriptor(nodeID);
	}

	/**
	 * Retrieves the ExtendedNodeDescriptor by a given Transition.
	 * 
	 * @param transition the transition node.
	 */
	public ExtendedNodeDescriptor getExtendedNodeDescriptor(Transition transition) {
		// construct the ID of the Map
		String transID = ExtendedNodeDescriptor.createNodeIDPrefix("transition", transition.getName());
		String parentID = ExtendedNodeDescriptor.createNodeIDPrefix(NodeTypes.getNodeName(transition.getFrom().getClass()), transition.getFrom().getName());
		return getExtendedNodeDescriptor(ExtendedNodeDescriptor.createSubNodeIDPrefix(parentID, transID));
	}
	/**
	 * retrieves the descriptor based on the Key Node ID.
	 * 
	 * @param transition The transition node.
	 */
	public ExtendedNodeDescriptor getExtendedNodeDescriptor(String nodeID) {
		return descriptors.get(nodeID);
	}
}
