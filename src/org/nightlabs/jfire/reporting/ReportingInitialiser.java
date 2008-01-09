/**
 * 
 */
package org.nightlabs.jfire.reporting;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportLayoutLocalisationDataID;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.config.AcquisitionParameterConfig;
import org.nightlabs.jfire.reporting.parameter.config.IGraphicalInfoProvider;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase;
import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ValueConsumer;
import org.nightlabs.jfire.reporting.parameter.config.ValueConsumerBinding;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionUseCaseID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.IOUtil;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Helper class to initialize report layouts. The ReportingInitialiser will
 * recusursively scan a given directory and create a tree of {@link ReportCategory}s
 * and {@link ReportLayout}s according to the directory structure it finds.
 * </p>
 * <p>
 * For each folder the initializer finds a category will be created as child of
 * the upper directorys one. A descriptor file 'content.xml' can be placed in
 * the directory to define in detail what id and names the category and report layous 
 * should get.
 * </p> 
 * <p>
 * The {@link ReportInitialiser} will also create a parameter acquisition workflow
 * defined in the content.xml for the report layout. For further details about how to 
 * define the acquisition workflow see the content.xml dtd (http://www.nightlabs.de/dtd/reporting_initialiser_*.dtd) 
 * </p>
 * <p>
 * Additionally resource bundles used for localisation of the reports are created by the
 * initialzer. These files should be placed in a subfolder 'resource' for each category and should be 
 * prefixed with the report layout id. (e.g. DefaultInvoiceLayout_en_EN.properties)
 * </p> 
 * <p> 
 * The recommended usage is 
 * <ul>
 * <li><b>Create the initializer</b>: Use {@link #ReportingInitialiser(String, ReportCategory, String, JFireServerManager, PersistenceManager, String)} to create the initializer and set the
 * base category, root directory and fallback values for ids</li>
 * <li><b>Initialise (sub)directories</b>: Use {@link #initialise()} to start the initialisation</li>
 * </p>
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportingInitialiser {

	protected static Logger logger = Logger.getLogger(ReportingInitialiser.class);

	private String scriptSubDir;
	private ReportCategory baseCategory;
	private JFireServerManager jfsm;
	private PersistenceManager pm;
	private String organisationID;
	private String reportRegistryItemType;
	
	private SAXException parseException;
	private Map<File, Document> categoryDescriptors = new HashMap<File, Document>();

	/**
	 * Returns the ReportCategory with the given id-parameters. If neccessary (not found in the datastore) 
	 * it will be newly created.
	 *  
	 * @param pm The PersistenceManager to use.
	 * @param parent The parent category.
	 * @param organisationID Organisation id-parameter.
	 * @param reportRegistryItemType Item type id-parameter.
	 * @param reportRegistryItemID Registry item id-parameter.
	 * @param internal Whether this should be treated as internal categroy.
	 * @return The ReportCategory with the given id-parameters.
	 */
	public static final ReportCategory createCategory(PersistenceManager pm, ReportCategory parent, String organisationID, String reportRegistryItemType, String reportRegistryItemID, boolean internal)  
	{
		if (logger.isDebugEnabled())
			logger.debug("createCategory: categoryPK=\""+organisationID+'/'+reportRegistryItemType+'/'+reportRegistryItemID+"\" parent=\""+ (parent == null ? null : (parent.getOrganisationID()+'/'+parent.getReportRegistryItemType()+'/'+parent.getReportRegistryItemID())) + "\" internal="+internal);

		// initialise meta-data
		pm.getExtent(ReportCategory.class);

		ReportCategory category;
		try {
			category = (ReportCategory) pm.getObjectById(ReportRegistryItemID.create(organisationID, reportRegistryItemType, reportRegistryItemID));
			if (logger.isDebugEnabled()) {
				ReportRegistryItem p = category.getParentCategory();
				logger.debug("createCategory: already exists: categoryPK=\""+category.getOrganisationID()+'/'+category.getReportRegistryItemType()+'/'+category.getReportRegistryItemID()+"\" parent=\"" + (p == null ? null : (p.getOrganisationID()+'/'+p.getReportRegistryItemType()+'/'+p.getReportRegistryItemID())) + "\" internal="+category.isInternal());
			}
		} catch (JDOObjectNotFoundException e) {
			category = new ReportCategory(parent, organisationID, reportRegistryItemType, reportRegistryItemID, internal);

			if (logger.isDebugEnabled())
				logger.debug("createCategory: persisting new category: categoryPK=\""+category.getOrganisationID()+'/'+category.getReportRegistryItemType()+'/'+category.getReportRegistryItemID()+"\"");

			category = (ReportCategory)pm.makePersistent(category);
		}
		return category;
	}
	
	/**
	 * Creates a new ReportingInitialiser with the given parameters.
	 * 
	 * @param scriptSubDir This is the relative directory under the deploy base directory (e.g. "JFireReporting.ear/script/General")
	 * @param baseCategory The base category from where the category-tree will be build.
	 * @param jfsm The JFireServerManager to use
	 * @param pm The PersistenceManager to use.
	 * @param registryItemType is the type (identifier) for the reports in categories, sub-categories get the scriptRegistryItemType from their parent 
	 * @param organisationID If you're writing a JFire Community Project, this is {@link Organisation#DEV_ORGANISATION_ID}.
	 */
	public ReportingInitialiser(
			String scriptSubDir, ReportCategory baseCategory, String reportRegistryItemType, 
			JFireServerManager jfsm, PersistenceManager pm, String organisationID
		)
	{
		this.scriptSubDir = scriptSubDir;
		this.baseCategory = baseCategory;
		this.jfsm = jfsm;
		this.pm = pm;
		this.organisationID = organisationID;
		this.reportRegistryItemType = reportRegistryItemType;
	}

	private ScriptRegistry scriptRegistry = null;
	protected ScriptRegistry getScriptRegistry()
	{
		if (scriptRegistry == null)
			scriptRegistry = ScriptRegistry.getScriptRegistry(pm);

		return scriptRegistry;
	}

	/**
	 * Start the initializing process.
	 * 
	 * @throws ModuleException
	 */	
	public void initialise() 
	throws ReportingInitialiserException 
	{
		String j2eeBaseDir = jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File scriptDir = new File(j2eeBaseDir, scriptSubDir);

		if (!scriptDir.exists())
			throw new IllegalStateException("Script directory does not exist: " + scriptDir.getAbsolutePath());

		logger.debug("BEGIN initialization of Scripts");	
//		initDefaultParameterSets();

		// initialise meta-data
		pm.getExtent(ReportCategory.class);
		pm.getExtent(ReportLayout.class);
		pm.getExtent(AcquisitionParameterConfig.class);
		pm.getExtent(ReportParameterAcquisitionSetup.class);
		pm.getExtent(ReportParameterAcquisitionUseCase.class);
		pm.getExtent(ValueProvider.class);
		pm.getExtent(ValueAcquisitionSetup.class);

		createReportCategories(scriptDir, baseCategory);
	}

	private Document getCategoryDescriptor(File categoryDir) 
	throws SAXException, IOException 
	{
		Document doc = categoryDescriptors.get(categoryDir);
		if (doc == null) {
			final File contentFile = new File(categoryDir, "content.xml");
			if (contentFile.exists()) { 
				DOMParser parser = new DOMParser();
				parser.setErrorHandler(new ErrorHandler(){
					public void error(SAXParseException exception) throws SAXException {
						logger.error("Parse ("+contentFile+"): ", exception);
						parseException = exception;
					}

					public void fatalError(SAXParseException exception) throws SAXException {
						logger.fatal("Parse ("+contentFile+"): ", exception);
						parseException = exception;
					}

					public void warning(SAXParseException exception) throws SAXException {
						logger.warn("Parse ("+contentFile+"): ", exception);
					}
				});
				parseException = null;
				InputSource inputSource;
				try {
					inputSource = new InputSource(new FileInputStream(contentFile));
				} catch (FileNotFoundException e) {
					throw new IllegalStateException("Although checked with .exists() file "+contentFile+" does not seem to exist. ", e);
				}
				parser.parse(inputSource);
				if (parseException != null)
					throw parseException;
				doc = parser.getDocument();
				categoryDescriptors.put(categoryDir, doc);
			}
		}
		return doc;
	}
	
	private Node getReportDescriptor(File scriptFile, Document categoryDocument) 
	throws TransformerException 
	{
		Collection<Node> nodes = NLDOMUtil.findNodeList(categoryDocument, "report-category/report");
		for (Node scriptNode : nodes) {
			Node fNode = scriptNode.getAttributes().getNamedItem("file");
			if (fNode != null && scriptFile.getName().equals(fNode.getTextContent()))
				return scriptNode;
		}
		return null;
	}
	
	private void createElementName(Node elementNode, String elementName, I18nText name, String def) 
	{
		if (name == null) {
			logger.warn("createElementName called with null element!", new NullPointerException("name"));
			return;
		}		
		boolean nameSet = false;
		if (elementNode != null) {
			Collection<Node> nodes = NLDOMUtil.findNodeList(elementNode, elementName);
			for (Node node : nodes) {
				Node lIDNode = node.getAttributes().getNamedItem("language");
				if (lIDNode != null && !"".equals(lIDNode.getTextContent())) {
					name.setText(lIDNode.getTextContent(), node.getTextContent());
					nameSet = true;
				}
				else
					logger.warn("name element of node "+elementNode.getNodeName()+" has an invalid/missing language attribute");
			}
		}
		if (!nameSet && def != null)
			name.setText(Locale.ENGLISH.getLanguage(), def);
	}
	
	/**
	 * Recurses from the given directory and report categories 
	 * based on the &lt;report-category&gt; element found in the content.xml files there.
	 * 
	 * @param dir The directory to start recursion
	 * @param parent The {@link ReportCategory}
	 * @throws ReportingInitialiserException
	 */
	protected void createReportCategories(File dir, ReportCategory parent) 
	throws ReportingInitialiserException
	{
		try {
			String categoryID = dir.getName();
			String itemType = parent != null ? parent.getReportRegistryItemType() : reportRegistryItemType;
			boolean internal = false;
			Document catDocument = getCategoryDescriptor(dir);
			Node catNode = null;
			if (catDocument != null) {
				catNode = NLDOMUtil.findSingleNode(catDocument, "report-category");
				if (catNode != null) {
					Node typeAttr = catNode.getAttributes().getNamedItem("type");
					if (typeAttr != null && !"".equals(typeAttr.getTextContent()))
						itemType = typeAttr.getTextContent();
					
					Node idAttr = catNode.getAttributes().getNamedItem("id");
					if (idAttr != null && !"".equals(idAttr.getTextContent()))
						categoryID = idAttr.getTextContent();
					
					Node internalAttr = catNode.getAttributes().getNamedItem("internal");
					if (internalAttr != null && !"".equals(typeAttr.getTextContent()))
						internal = Boolean.parseBoolean(internalAttr.getTextContent());
				}
			}

			// Create the category
			ReportCategory category = createCategory(pm, parent, organisationID, itemType, categoryID, internal);

			// create the category name
			createElementName(catNode, "name", category.getName(), categoryID);
			createElementName(catNode, "description", category.getDescription(), null);
			logger.debug("create Script Category = "+category.getName());
			

			// Create reports
			File[] reports = dir.listFiles(scriptFileNameFilter);
			for (int j=0; j<reports.length; j++) {
				File reportFile = reports[j];
				try {
					createReportLayout(reportFile, category, catDocument, itemType);
				} catch (Exception e) {
					logger.warn("could NOT create ReportLayout for file " + reportFile + "!", e);
				}
			}

			
			// recurse
			File[] subDirs = dir.listFiles(dirFileFilter);		
			for (int i=0; i<subDirs.length; i++) {
				createReportCategories(subDirs[i], category);
			}
		} catch (IOException e) {
			throw new ReportingInitialiserException(e);
		} catch (SAXException e) {
			throw new ReportingInitialiserException(e);
		}
	}

	/**
	 * Creates a {@link ReportLayout} from the given file.
	 *  
	 * @param reportFile The file to create a new ReportFile from.
	 * @param category The category to add the report
	 * @param catDocument The document where meta-data of the report can be found
	 * @param reportRegistryItemType The reportRegistryItemType to use as fallback.
	 * @throws Exception
	 */
	protected void createReportLayout(
			File reportFile, ReportCategory category, Document catDocument, 
			String reportRegistryItemType) 
	throws Exception
	{
		Node reportNode = getReportDescriptor(reportFile, catDocument);				
		
		String reportID = IOUtil.getFileNameWithoutExtension(reportFile.getName());
		boolean overwriteOnInit = true;
		
		if (reportNode != null) {
			Node idNode = reportNode.getAttributes().getNamedItem("id");
			if (idNode != null && !"".equals(idNode.getTextContent()))
				reportID = idNode.getTextContent();
//			Node typeNode = reportNode.getAttributes().getNamedItem("type");
//			if (typeNode != null && !"".equals(typeNode.getTextContent()))
//				itemType = typeNode.getTextContent();
			Node overwriteNode = reportNode.getAttributes().getNamedItem("overwriteOnInit");
			if (overwriteNode != null && !"".equals(overwriteNode.getTextContent()))
				overwriteOnInit = Boolean.parseBoolean(overwriteNode.getTextContent());
		}
		
		logger.debug("create ReportLayout = " + reportID);				
		ReportLayout layout;
		boolean hadToBeCreated = false;
		try {
			pm.getExtent(ReportLayout.class);
			layout = (ReportLayout) pm.getObjectById(ReportRegistryItemID.create(
					organisationID,
					reportRegistryItemType,
					reportID)
			);
		} catch (JDOObjectNotFoundException e) {
			layout = new ReportLayout(category, organisationID, reportRegistryItemType, reportID);
			layout = (ReportLayout)pm.makePersistent(layout);
			hadToBeCreated = true;
		}

		boolean doInit = overwriteOnInit || hadToBeCreated; 
		if (doInit) {
			File layoutFile = createReportFileFromTemplate(reportFile);
			layout.loadFile(layoutFile);
			layoutFile.delete();
			createElementName(reportNode, "name", layout.getName(), reportID);
			createElementName(reportNode, "description", layout.getDescription(), null);
			createReportLocalisationBundle(reportFile, reportID, reportNode, layout);
		}
		// This has its own overwriteOnInit
		createReportParameterAcquisitionSetup(reportFile, reportNode, layout);
	}
	
	/**
	 * Creates a new report design file from the given one by replacing
	 * variables in the given File.
	 * <p>
	 * By now the variables replaced are:
	 * <ul>
	 * <li>${rootOrganisationID}</li>
	 * <li>${rootOrganisationIDConverted}</li>
	 * <li>${devilOrganisationID}</li>
	 * <li>${devilOrganisationIDConverted}</li>
	 * </ul>
	 * </p> 
	 * @param reportFile The template file to replace the variables in 
	 * @return The newly created file with all variables replaced.
	 * @throws Exception If something fails.
	 */
	protected File createReportFileFromTemplate(File reportFile) 
	throws Exception
	{
		File tmpFile = File.createTempFile(IOUtil.getFileNameWithoutExtension(reportFile.getName()), ".rptdesign", IOUtil.getTempDir());
		Map<String, String> variables = new HashMap<String, String>();
		try {
			InitialContext ctx = new InitialContext();
			String rootOrganisationID = Organisation.getRootOrganisationID(ctx);
			try {
				variables.put("rootOrganisationID", rootOrganisationID);
				variables.put("rootOrganisationIDConverted", convertToReportColumnString(rootOrganisationID));
				variables.put("devilOrganisationID", Organisation.DEV_ORGANISATION_ID);
				variables.put("devilOrganisationIDConverted", convertToReportColumnString(Organisation.DEV_ORGANISATION_ID));
			} finally {
				ctx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		IOUtil.replaceTemplateVariables(tmpFile, reportFile, IOUtil.CHARSET_NAME_UTF_8, variables);
		tmpFile.deleteOnExit();
		return tmpFile;
	}
	
	/**
	 * Returns the given string converted so that it can be used
	 * as name of dataset column in a report.
	 *  
	 * @param str The String to convert.
	 * @return The given str with all "." replaced with "_" and "_" with "__" 
	 */
	public static String convertToReportColumnString(String str) {
		String key = str.replaceAll("_", "__");
		key = key.replaceAll("\\.", "_");
		return key;
	}	
	
	protected FileFilter dirFileFilter = new FileFilter() {	
		public boolean accept(File pathname) {
			return pathname.isDirectory() && !(pathname.toString().endsWith("resource"));
		}	
	};

	private FilenameFilter scriptFileNameFilter = new FilenameFilter()
	{	
		public boolean accept(File dir, String name) 
		{				
			String fileExtension = IOUtil.getFileExtension(name);
			return "rptdesign".equals(fileExtension);
		}	
	};

	
	protected void createReportParameterAcquisitionSetup(File reportFile, Node reportNode, ReportLayout layout)
	throws ReportingInitialiserException
	{
		Node acquisitionNode = NLDOMUtil.findSingleNode(reportNode, "parameter-acquisition");
		if (acquisitionNode == null)
			return;

		// the setup already persistent
		ReportParameterAcquisitionSetup setup = ReportParameterAcquisitionSetup.getSetupForReportLayout(pm, (ReportRegistryItemID)JDOHelper.getObjectId(layout));
		
		Map<String, Object> id2BindingObject = new HashMap<String, Object>();
		
		Collection<Node> useCaseNodes = NLDOMUtil.findNodeList(acquisitionNode, "use-case");
		for (Node useCaseNode : useCaseNodes) {
			Node useCaseIDNode = useCaseNode.getAttributes().getNamedItem("id");
			if (useCaseIDNode == null)
				throw new ReportingInitialiserException("ReportParameterAcquisitionUseCase element <use-case> must define an id attribute. See file "+reportFile.toString());
			String useCaseID = useCaseIDNode.getNodeValue();
			
			boolean overwriteOnInit = false;			
			Node overwriteNode = reportNode.getAttributes().getNamedItem("overwriteOnInit");
			if (overwriteNode != null && !"".equals(overwriteNode.getTextContent()))
				overwriteOnInit = Boolean.parseBoolean(overwriteNode.getTextContent());
			
			ReportParameterAcquisitionUseCase useCase = getUseCase(setup, useCaseID);
			if (!overwriteOnInit && useCase != null)
				// use-case was already created sometime and should not be overwritten
				// continue with next use-case
				continue;
			
			if (useCase != null && overwriteOnInit) {
				// use-case should be overwritten
//				useCase = resetUseCaseAndAcquisitionSetup(setup, useCase);
				if (setup != null) {
					setup.removeUseCase(useCase);
					useCase = null;
				}
			}

			if (setup == null) {
				// if setup not created by now, create it
				setup = new ReportParameterAcquisitionSetup(organisationID, IDGenerator.nextID(ReportParameterAcquisitionSetup.class), layout);
				setup = (ReportParameterAcquisitionSetup) pm.makePersistent(setup);
			}
			
			// (re-)create the use case
			if (useCase == null)
				useCase = new ReportParameterAcquisitionUseCase(setup, useCaseID);
			createElementName(useCaseNode, "name", useCase.getName(), useCaseID);
			createElementName(useCaseNode, "description", useCase.getDescription(), useCaseID);

			ValueAcquisitionSetup acquisitionSetup = new ValueAcquisitionSetup(organisationID, IDGenerator.nextID(ValueAcquisitionSetup.class), setup, useCase);
			
			id2BindingObject.clear();

			// Read parameter list
			Collection<Node> parameters = NLDOMUtil.findNodeList(useCaseNode, "parameters/parameter");
			for (Node paramNode : parameters) {
				try {
					String id = NLDOMUtil.getNonEmptyAttributeValue(paramNode, "id");
					String name = NLDOMUtil.getNonEmptyAttributeValue(paramNode, "name");
					String type = NLDOMUtil.getNonEmptyAttributeValue(paramNode, "type");
					AcquisitionParameterConfig parameterConfig = new AcquisitionParameterConfig(acquisitionSetup, name, type);					
					setGraphicalData(paramNode, parameterConfig);
					id2BindingObject.put(id, parameterConfig);
					acquisitionSetup.getParameterConfigs().add(parameterConfig);
				} catch (IllegalArgumentException e) {
					throw new ReportingInitialiserException("Some attribute is missing for the parameters declaration in the parameter-acquisition in file "+reportFile, e);
				}
			}
			
			// Read value provider Config List
			Collection<Node> providerConfigs = NLDOMUtil.findNodeList(useCaseNode, "value-provider-configs/provider-config");
			for (Node providerNode : providerConfigs) {
				try {
					String id = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "id");
					String providerOrganisationID = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "organisationID");
					String providerCategoryID = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "categoryID");
					String valueProviderID = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "valueProviderID");
					String pageIndexStr = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "pageIndex");
					if (pageIndexStr == null)
						pageIndexStr = "0";
					String pageOrderStr = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "pageOrder");
					if (pageOrderStr == null)
						pageOrderStr = "0";
					int pageIndex = 0;
					try {
						pageIndex = Integer.parseInt(pageIndexStr);
					} catch (Exception e) {
						pageIndex = 0;
					}
					int pageOrder = 0;
					try {
						pageIndex = Integer.parseInt(pageIndexStr);
					} catch (Exception e) {
						pageIndex = 0;
					}

					ValueProviderConfig config = new ValueProviderConfig(acquisitionSetup, IDGenerator.nextID(ValueProviderConfig.class));
					ValueProviderID providerID = ValueProviderID.create(providerOrganisationID, providerCategoryID, valueProviderID);
					ValueProvider provider = null;
					try {
						provider = (ValueProvider) pm.getObjectById(providerID);
						provider.getCategory();
					} catch (JDOObjectNotFoundException e) {
						throw new ReportingInitialiserException("Referenced ValueProvider does not exist "+providerID+". See "+reportFile, e);
					}
					config.setValueProvider(provider);
					config.setPageIndex(pageIndex);					
					config.setPageOrder(pageOrder);
					
					boolean allowOutputNull = false;
					String allowNullValue = NLDOMUtil.getAttributeValue(providerNode, "allowNullOutputValue");
					if (allowNullValue != null) {
						try {
							allowOutputNull = Boolean.parseBoolean(allowNullValue);
						} catch (Exception e) {
							allowOutputNull = false;
						}
					}
					config.setAllowNullOutputValue(allowOutputNull);
					
					boolean showMessageInHeader = true;
					String showMessage = NLDOMUtil.getAttributeValue(providerNode, "showMessageInHeader");
					if (showMessage != null) {
						try {
							showMessageInHeader = Boolean.parseBoolean(showMessage);
						} catch (Exception e) {
							showMessageInHeader = true;
						}
					}
					config.setShowMessageInHeader(showMessageInHeader);
					
					createElementName(providerNode, "message", config.getMessage(), null);
					
					setGraphicalData(providerNode, config);
					
					id2BindingObject.put(id, config);
					acquisitionSetup.getValueProviderConfigs().add(config);					
				} catch (IllegalArgumentException e) {
					throw new ReportingInitialiserException("Some attribute is missing for the value-provider-configs declaration in the parameter-acquisition in file "+reportFile, e);
				}
			}
			
			// Read binding list
			Collection<Node> bindings = NLDOMUtil.findNodeList(useCaseNode, "value-consumer-bindings/value-consumer-binding");
			for (Node bindingNode : bindings) {
				try {
					Node providerNode = NLDOMUtil.findElementNode("binding-provider", bindingNode);
					if (providerNode == null)
						throw new ReportingInitialiserException("Element value-consumer-binding/binding has to define sub-element provider. See file "+reportFile);
					String providerID = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "id");
					Node parameterNode = NLDOMUtil.findElementNode("binding-parameter", bindingNode);
					if (parameterNode == null)
						throw new ReportingInitialiserException("Element value-consumer-binding/binding has to define sub-element parameter. See file "+reportFile);
					String parameterName = NLDOMUtil.getNonEmptyAttributeValue(parameterNode, "name");
					Node consumerNode = NLDOMUtil.findElementNode("binding-consumer", bindingNode);
					if (consumerNode == null)
						throw new ReportingInitialiserException("Element value-consumer-binding/binding has to define sub-element consumer. See file "+reportFile);
					String consumerID = NLDOMUtil.getNonEmptyAttributeValue(consumerNode, "id");

					ValueProviderConfig provider = null; 
					try {
						provider = (ValueProviderConfig) id2BindingObject.get(providerID);
						if (provider == null)
							throw new IllegalArgumentException("Could not resolve the provider with id=\""+providerID+"\".");
					} catch (Exception e) {
						throw new ReportingInitialiserException("The provider-id of a binding does not point to a ValueProviderConfig. File "+reportFile, e);
					}
					ValueConsumer consumer = null;
					try {
						consumer = (ValueConsumer) id2BindingObject.get(consumerID);
						if (consumer == null)
							throw new IllegalArgumentException("Could not resolve the consumer with id=\""+consumerID+"\".");
					} catch (Exception e) {
						throw new ReportingInitialiserException("The provider-id of a binding does not point to a ValueProviderConfig. File "+reportFile, e);
					}

					ValueConsumerBinding binding = new ValueConsumerBinding(organisationID, IDGenerator.nextID(ValueConsumerBinding.class), acquisitionSetup);
					binding.setProvider(provider);
					binding.setParameterID(parameterName);
					binding.setConsumer(consumer);
					acquisitionSetup.getValueConsumerBindings().add(binding);
				} catch (IllegalArgumentException e) {
					throw new ReportingInitialiserException("Some attribute is missing for the value-consumer-bindings declaration in the parameter-acquisition in file "+reportFile, e);
				}
			}

//			// TODO JPOX WORKAROUND : To directly put the new acquisitionSetup causes a duplicate key exception
//			Object o = setup.getValueAcquisitionSetups().get(useCase);
//			if (o != null) {
//				setup.getValueAcquisitionSetups().remove(useCase);
////				pm.deletePersistent(o);
//			}
//
//			pm.flush();

//			acquisitionSetup = pm.makePersistent(acquisitionSetup);
			// TODO end JPOX workaround

			// make the usecase setup persistent
			setup.addValueAcquisitionSetup(acquisitionSetup);


			// TODO JPOX WORKAROUND : this was not needed with JPOX 1.1 - but now we seem to need it
			pm.flush();


			// check if its the default use case
			boolean defUseCase = false;
			String defaultStr = NLDOMUtil.getAttributeValue(useCaseNode, "default");
			if (defaultStr != null || "".equals(defaultStr.trim())) {
				try {
					defUseCase = Boolean.parseBoolean(defaultStr);
				} catch (Exception e) {
					defUseCase = false;
				}
			}
			if (defUseCase)
				setup.setDefaultSetup(acquisitionSetup);
		}
	}
	
	private ReportParameterAcquisitionUseCase getUseCase(ReportParameterAcquisitionSetup setup, String useCaseID) {
		if (setup == null)
			return null;
		for (Map.Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> entry : setup.getValueAcquisitionSetups().entrySet()) {
			if (entry.getKey().getReportParameterAcquisitionUseCaseID().equals(useCaseID))
				return entry.getKey();
		}
		if (setup.getDefaultSetup() != null &&
			setup.getDefaultSetup().getUseCase() != null &&
			setup.getDefaultSetup().getUseCase().getReportParameterAcquisitionUseCaseID().equals(useCaseID)
		)
			return setup.getDefaultSetup().getUseCase();
		ReportParameterAcquisitionUseCaseID id = ReportParameterAcquisitionUseCaseID.create(setup.getOrganisationID(), setup.getReportParameterAcquisitionSetupID(), useCaseID);
		ReportParameterAcquisitionUseCase useCase = null;
		try {			
			useCase = (ReportParameterAcquisitionUseCase) pm.getObjectById(id);
			useCase.getReportParameterAcquisitionSetupID();
		} catch (JDOObjectNotFoundException e) {
			useCase = null;
		}
		return useCase;
	}
	
	private void setGraphicalData(Node elementNode, IGraphicalInfoProvider graphicalInfoProvider) {
		String xStr = NLDOMUtil.getAttributeValue(elementNode, "x");
		int x = 0;
		try {
			x = Integer.parseInt(xStr);
		} catch (Exception e) {
			x = 0;
		}
		graphicalInfoProvider.setX(x);
		
		String yStr = NLDOMUtil.getAttributeValue(elementNode, "y");
		int y = 0;
		try {
			y = Integer.parseInt(yStr);
		} catch (Exception e) {
			y = 0;
		}
		graphicalInfoProvider.setY(y);
	}
	
//	private ReportParameterAcquisitionUseCase resetUseCaseAndAcquisitionSetup(ReportParameterAcquisitionSetup setup, ReportParameterAcquisitionUseCase useCase) {
////		if (setup.getDefaultSetup() != null && setup.getDefaultSetup().getUseCase() != null && setup.getDefaultSetup().getUseCase().equals(useCase))
////			setup.setDefaultSetup(null);
//
//		// TODO JPOX WORKAROUND Deleting the setup via pm.deletePersistent(setup) fails => workaround
//		setup.setDefaultSetup(null);
////		setup.getValueAcquisitionSetups().clear();
//		for (Map.Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> entry : setup.getValueAcquisitionSetups().entrySet()) {
//			if (entry.getKey().getReportParameterAcquisitionUseCaseID().equals(useCase.getReportParameterAcquisitionUseCaseID())) {
//				for (ValueConsumerBinding binding : entry.getValue().getValueConsumerBindings()) {
//					pm.deletePersistent(binding);
//				}
//				pm.flush();
//				entry.getValue().getValueConsumerBindings().clear();
//				entry.getValue().getParameterConfigs().clear();
//				entry.getValue().getValueProviderConfigs().clear();
//				return entry.getKey();
//			}
//		}
//		pm.flush();
//		return useCase;
//	}
	
	/**
	 * Create the {@link ReportLayoutLocalisationData} objects for the given layout. It will search in a subfolder 'resource'
	 * for entries named like reportID_{locale}.properties
	 * 
	 * @param reportFile The report file currently processed.
	 * @param reportID The id of the {@link ReportLayout} currently processed. 
	 * @param reportNode The 'report' node of the content.xml document currenty processed
	 * @param layout The {@link ReportLayout} currently processed.
	 * @throws ReportingInitialiserException
	 */
	protected void createReportLocalisationBundle(File reportFile, final String reportID, Node reportNode, ReportLayout layout) // TODO shouldn't this method be named createReportLocalisationData ???
	throws ReportingInitialiserException
	{
		// initialise meta-data
		pm.getExtent(ReportLayoutLocalisationData.class);

		File resourceFolder = new File(reportFile.getParentFile(), "resource");
		File[] resourceFiles = resourceFolder.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().startsWith(reportID);
			}
		});
		if (resourceFiles == null)
			return;
		for (File resFile : resourceFiles) {
			String locale = ReportLayoutLocalisationData.extractLocale(resFile.getName());
			ReportLayoutLocalisationDataID localisationDataID = ReportLayoutLocalisationDataID.create(
					layout.getOrganisationID(), layout.getReportRegistryItemType(), layout.getReportRegistryItemID(), locale  
			);
			ReportLayoutLocalisationData localisationData = null;
			try {
				localisationData = (ReportLayoutLocalisationData) pm.getObjectById(localisationDataID);
				localisationData.getLocale();
			} catch (JDOObjectNotFoundException e) {
				localisationData = new ReportLayoutLocalisationData(layout, locale);
				localisationData = (ReportLayoutLocalisationData) pm.makePersistent(localisationData);
			}
			try {
				localisationData.loadFile(resFile);
			} catch (IOException e) {
				throw new ReportingInitialiserException("Could not load localisatino file "+resFile, e);
			}
		}
	}
}
