package org.nightlabs.jfire.jbpm.extensionI18n;


/**
 * 
 * this class is the node information of the actionHandler
 *
 * @author Fitas Amine - fitas at nightlabs dot de)
 * 
 */
public class ExtendedActionHandlerNode {
	
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the Action node name.
	 */	
	private String actionHandlerNodeName; 
	/**
	 * the class name of the action handler.
	 */
	private String actionHandlerClassName; // 
	/**
	 * node type (node-enter etc..).
	 */
	private String actionHandlerEventType;
	
	
	
	public ExtendedActionHandlerNode(String actionHandlerNodeName,String actionHandlerClassName,String actionHandlerEventType)
	{
		super();
		this.actionHandlerNodeName = actionHandlerNodeName;
		this.actionHandlerClassName = actionHandlerClassName;
		this.actionHandlerEventType = actionHandlerEventType;	
	}

	
	public String getActionHandlerNodeName() {
		return actionHandlerNodeName;
	}

	public void setActionHandlerNodeName(String actionHandlerNodeName) {
		this.actionHandlerNodeName = actionHandlerNodeName;
	}

	public String getActionHandlerClassName() {
		return actionHandlerClassName;
	}

	public void setActionHandlerClassName(String actionHandlerClassName) {
		this.actionHandlerClassName = actionHandlerClassName;
	}

	public String getActionHandlerEventType() {
		return actionHandlerEventType;
	}

	public void setActionHandlerEventType(String actionHandlerEventType) {
		this.actionHandlerEventType = actionHandlerEventType;
	}


}
