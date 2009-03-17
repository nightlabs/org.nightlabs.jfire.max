package org.nightlabs.jfire.jbpm.extensionI18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.NodeTypes;
import org.jbpm.jpdl.JpdlException;
import org.jbpm.jpdl.xml.Problem;
import org.jbpm.jpdl.xml.ProblemListener;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.apache.log4j.Logger;

/**
 * this class reads and parse an XML process definition Extension file, it does loads the
 * extended nodes in a {@link ExtendedProcessDefinitionDescriptor) map class.
 * 
 * 
 * @author Fitas Amine - fitas at nightlabs dot de)
 * 
 */
public class JpdlXmlExtensionReader implements ProblemListener {

	private static final long serialVersionUID = 1L;

	private static final List<String> EXTENDABLE_NODE_NAMES = Arrays.asList(new String[] {
			"start-state", "end-state", "state", "node", "transition"
	});
	protected InputSource inputSource = null;
	// TODO: Please parameterize this!
	protected List<Problem> problems = new ArrayList<Problem>();
	protected ProblemListener problemListener = null;
	boolean hadStartState;
	// the nodes that may contain the tag publicState
	private static final List<String> PUBLIC_STATE_NODE_NAMES = Arrays.asList(new String[] {
			"start-state", "end-state", "state", "node"
	});
	
	public ExtendedProcessDefinitionDescriptor getExtendedProcessDefinitionDescriptor() {
		ExtendedProcessDefinitionDescriptor descriptor = new ExtendedProcessDefinitionDescriptor();
		readProcessDefinitionExtension(descriptor);
		return descriptor;
	}

	public ExtendedNodeDescriptor getExendedDescByTransition(
			Transition transition) {

		return null;
	}

	public JpdlXmlExtensionReader(InputSource inputSource) {
		this.inputSource = inputSource;
	}

	public JpdlXmlExtensionReader(InputSource inputSource,
			ProblemListener problemListener) {
		this.inputSource = inputSource;
		this.problemListener = problemListener;
	}

	public JpdlXmlExtensionReader(Reader reader) {
		this(new InputSource(reader));
	}
	/**
	 * closes the file input stream
	 */	
	private void close() throws IOException {
		InputStream byteStream = inputSource.getByteStream();
		if (byteStream != null)
			byteStream.close();
		else {
			Reader charStream = inputSource.getCharacterStream();
			if (charStream != null)
				charStream.close();
		}
	}

	public void addProblem(Problem problem) {
		problems.add(problem);
		if (problemListener != null)
			problemListener.addProblem(problem);
	}

	public void addError(String description) {
		logger.error("invalid process xml: " + description);
		addProblem(new Problem(Problem.LEVEL_ERROR, description));
	}

	public void addError(String description, Throwable exception) {
		logger.error("invalid process xml: " + description, exception);
		addProblem(new Problem(Problem.LEVEL_ERROR, description, exception));
	}

	public void addWarning(String description) {
		logger.warn("process xml warning: " + description);
		addProblem(new Problem(Problem.LEVEL_WARNING, description));
	}

	/**
	 * Parses the input source and starts the extension read process.
	 */
	protected void readProcessDefinitionExtension(ExtendedProcessDefinitionDescriptor descriptor) {
		// initialize the problems lists
		problems = new ArrayList<Problem>();

		hadStartState = false;
		try {
			// parse the document into a dom tree
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(inputSource);
			readDocument(document, descriptor);

			//			Document document = JpdlParser.parse(inputSource, this);
			//			Element root = document.getRootElement();

			close();
		} catch (Exception e) {
			logger.error("couldn't parse process definition extension", e);
			addProblem(new Problem(Problem.LEVEL_ERROR,
					"couldn't parse process definition extension", e));
		}

		if (Problem.containsProblemsOfLevel(problems, Problem.LEVEL_ERROR)) {
			throw new JpdlException(problems);
		}

	}

	/**
	 * Iterates the child elements of the the document root
	 * and starts the recursive parsing of the extension elements. 
	 * @param document The document to read.
	 * @param descriptor TODO
	 */
	protected void readDocument(Document document, ExtendedProcessDefinitionDescriptor descriptor) {
		Element docRoot = document.getDocumentElement();
		List<Element> childElements = findChildElements(docRoot);
		for (Element childElement : childElements) {
			String nodeName = childElement.getNodeName();
			// get the node type
			Class<?> nodeType = NodeTypes.getNodeType(nodeName);
			if (nodeType != null) {
				// read the common node parts of the element
				// check for duplicate start-states
				if (nodeName.equals("start-state") && (hadStartState)) {
					logger.error("max one start-state allowed in a process");
					addError("max one start-state allowed in a process");
				} else {
					readExtendedElement(childElement, "", descriptor);
				}
				if (nodeName.equals("start-state")) {
					logger.debug("found the start-state node");
					hadStartState = true;
				}
			}
		}
	}

	// simple function to check if the element has the boolean attribute and 
	Boolean hasBoolAttribute(Element element, String attribute)
	{
		String nodeAttr = element.getAttribute(attribute);
		if (nodeAttr == null || nodeAttr.isEmpty())
			return false;
		
		return Boolean.parseBoolean(nodeAttr);
	}
	
	
	/**
	 * Reads the extensions of the given element (uses {@link #parseElementExtensions(Element, String)})
	 * and recurses to read the extensions of sub-elements.
	 * 
	 * @param nodeElement The element to read.
	 * @param parentNodeID The node id of the parent element.
	 * @param descriptor TODO
	 */
	protected void readExtendedElement(Element nodeElement, String parentNodeID, ExtendedProcessDefinitionDescriptor descriptor) {
		// get the action name
		String nodeName = nodeElement.getNodeName();
		if (EXTENDABLE_NODE_NAMES.contains(nodeName)) {
			// all extendable nodes need a name attribute
			String nodeNameAttr = nodeElement.getAttribute("name");
			if (nodeNameAttr == null || nodeNameAttr.isEmpty()) {
				throw new IllegalStateException("Found extendable node '" + nodeName + "' of parent '" + parentNodeID + "' with no/invalid name attribute");
			}

			// create the node ID
			String nodeID = ExtendedNodeDescriptor.createSubNodeIDPrefix(parentNodeID, ExtendedNodeDescriptor.createNodeIDPrefix(nodeName, nodeNameAttr));			

			ExtendedNodeDescriptor nodeDescriptor = parseElementExtensions(nodeElement, nodeID);
			if (nodeDescriptor != null) {
				descriptor.addNodeDescriptor(nodeID, nodeDescriptor);
			}
			// recurse
			List<Element> children = findChildElements(nodeElement);
			for (Element child : children) {
				readExtendedElement(child, nodeID, descriptor);
			}


		}
	}

	
	/**
	 * Builds the {@link ExtendedNodeDescriptor} from the extension nodes
	 * found in the given element and puts it into the map under the given nodeID.
	 * 
	 * @param element The element to create the descriptor for.
	 * @param nodeID The id the descriptor should be stored under.
	 */
	private ExtendedNodeDescriptor parseElementExtensions(Element element, String nodeID) {
		I18nTextBuffer buffer = new I18nTextBuffer();
		I18nTextBuffer name = new I18nTextBuffer();
		String iconFile = "";		
		
		// create descriptor
		ExtendedNodeDescriptor nodeDescriptor = new ExtendedNodeDescriptor(nodeID, name, buffer);
		
		// read names
		NodeList nameElements = element.getElementsByTagName("name");
		for (int i = 0; i < nameElements.getLength(); i++) {
			Element nameElement = (Element) nameElements.item(i);
			String languageID = nameElement.getAttribute("language");
			if (languageID == null || languageID.isEmpty()) {
				throw new IllegalStateException("Found name element with invalid/no language attribute for element " + element.getNodeName() + "(name=" + element.getAttribute("name") + ").");
			}
			String value = nameElement.getTextContent();
			name.setText(languageID, value);
		}

		// read descriptions
		NodeList descriptionElements = element.getElementsByTagName("description");
		for (int i = 0; i < descriptionElements.getLength(); i++) {
			Element descriptionElement = (Element) descriptionElements.item(i);
			String languageID = descriptionElement.getAttribute("language");
			if (languageID == null || languageID.isEmpty()) {
				throw new IllegalStateException("Found name element with invalid/no language attribute for element " + element.getNodeName() + "(name=" + element.getAttribute("name") + ").");
			}
			String value = descriptionElement.getTextContent();
			buffer.setText(languageID, value);
		}

		// read icon file name 
		Element iconElement = (Element) NLDOMUtil.findSingleNode(element, "icon");
		if (iconElement != null) {
			iconFile = iconElement.getAttribute("file");
		}

		nodeDescriptor.setIconFile(iconFile); // set the icon file
		// set the userExecutable attribute for a transition.
		if(element.getNodeName().equals("transition"))	
		{	
			String userExecutable = element.getAttribute( "userExecutable");
			if (userExecutable == null || userExecutable.isEmpty()) 
				// The default value of 'userExecutable' should be true if not defined.
				nodeDescriptor.setUserExecutable(true);  
			else	
				nodeDescriptor.setUserExecutable(Boolean.parseBoolean(userExecutable));
		}
		else
		{	
			if(PUBLIC_STATE_NODE_NAMES.contains(element.getNodeName()))
			{
				// set publicState for the node 
				String publicState = element.getAttribute( "publicState");
				if (publicState == null || publicState.isEmpty()) 
					// The default value of 'publicState' should be false if not defined.
					nodeDescriptor.setPublicState(false);
				else
					nodeDescriptor.setPublicState(Boolean.parseBoolean(publicState));
			}
		}

		return nodeDescriptor;
	}

	private static List<Element> findChildElements(Element parent) {
		List<Element> result = new LinkedList<Element>();
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				result.add((Element) children.item(i));
			}
		}
		return result;
	}

	private static final Logger logger = Logger.getLogger(JpdlXmlExtensionReader.class);
}
