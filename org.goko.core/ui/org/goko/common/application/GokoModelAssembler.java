package org.goko.common.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.ExtensionsSort;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.goko.core.config.GokoPreference;

/**
 *
 */
public class GokoModelAssembler {
	@Inject
	private Logger logger;

	@Inject
	private MApplication application;

	@Inject
	private IEclipseContext context;

	@Inject
	private IExtensionRegistry registry;

	@Inject
	@Preference(nodePath = GokoPreference.NODE_ID, value = GokoPreference.KEY_TARGET_BOARD)
	private String targetBoard;
		
	final private static String extensionPointID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$
	
	final private static String gokoExtensionPointID = "org.goko.core.ui.e4.model"; //$NON-NLS-1$

	//	private static final String ALWAYS = "always"; //$NON-NLS-1$
	private static final String INITIAL = "initial"; //$NON-NLS-1$
	private static final String NOTEXISTS = "notexists"; //$NON-NLS-1$
	private static final String TARGETBOARD = "targetBoard"; //$NON-NLS-1$
	/**
	 * Process the model
	 */
	public void processModel(boolean initial) {
		List<IExtension> lstExtensions = new ArrayList<>();
		
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointID);		
		lstExtensions.addAll(Arrays.asList(extPoint.getExtensions()));
		
		IExtensionPoint gokoExtPoint = registry.getExtensionPoint(gokoExtensionPointID);
		lstExtensions.addAll(Arrays.asList(gokoExtPoint.getExtensions()));
		
		IExtension[] extensions = new ExtensionsSort().sort(lstExtensions.toArray(new IExtension[]{}));
		
		List<MApplicationElement> imports = new ArrayList<MApplicationElement>();
		List<MApplicationElement> addedElements = new ArrayList<MApplicationElement>();

		// run processors which are marked to run before fragments
		runProcessors(extensions, initial, false);
		processFragments(extensions, imports, addedElements, initial);
		// run processors which are marked to run after fragments
		runProcessors(extensions, initial, true);

		resolveImports(imports, addedElements);
	}

	/**
	 * @param extensions
	 * @param imports
	 * @param addedElements
	 */
	private void processFragments(IExtension[] extensions, List<MApplicationElement> imports,
			List<MApplicationElement> addedElements, boolean initial) {

		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if ("fragment".equals(ce.getName())) { //$NON-NLS-1$
					if (initial || !INITIAL.equals(ce.getAttribute("apply"))) { //$NON-NLS-1$		
						// Make sure we match target board if needed
						if(matchTargetBoard(ce)){
							processFragment(ce, imports, addedElements, initial);
						}
					}
				}
			}
		}
	}

	/**
	 * @param ce
	 * @return
	 */
	private boolean matchTargetBoard(IConfigurationElement ce) {
		List<String> lst = Arrays.asList(ce.getAttributeNames());
		
		if(lst.contains( TARGETBOARD )) { 
			String ceTargetBoard = ce.getAttribute(TARGETBOARD);
			if(StringUtils.defaultString(targetBoard).equals( ceTargetBoard )){
				return true;
			}
			logger.info("Skipping "+ce.getAttribute("uri")+" because it does not match current target board ["+targetBoard+"]");
			// We expect another target board
			return false;
		}
		return true;
	}

	private void processFragment(IConfigurationElement ce, List<MApplicationElement> imports,
			List<MApplicationElement> addedElements, boolean initial) {
		E4XMIResource applicationResource = (E4XMIResource) ((EObject) application).eResource();
		ResourceSet resourceSet = applicationResource.getResourceSet();
		IContributor contributor = ce.getContributor();
		String attrURI = ce.getAttribute("uri"); //$NON-NLS-1$
		String bundleName = contributor.getName();
		if (attrURI == null) {
			logger.warn("Unable to find location for the model extension \"{0}\"", bundleName); //$NON-NLS-1$
			return;
		}

		URI uri;
		try {
			// check if the attrURI is already a platform URI
			if (URIHelper.isPlatformURI(attrURI)) {
				uri = URI.createURI(attrURI);
			} else {
				String path = bundleName + '/' + attrURI;
				uri = URI.createPlatformPluginURI(path, false);
			}
		} catch (RuntimeException e) {
			logger.warn(e, "Invalid location \"" + attrURI + "\" of model extension \"" + bundleName + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}

		String contributorURI = URIHelper.constructPlatformURI(contributor);
		Resource resource;
		try {
			resource = resourceSet.getResource(uri, true);
		} catch (RuntimeException e) {
			logger.warn(e, "Unable to read model extension from \"" + uri.toString() +"\" of \"" + bundleName + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}

		EList<?> contents = resource.getContents();
		if (contents.isEmpty()) {
			return;
		}

		Object extensionRoot = contents.get(0);

		if (!(extensionRoot instanceof MModelFragments)) {
			logger.warn("Unable to create model extension \"{0}\"", bundleName); //$NON-NLS-1$
			return;
		}
		boolean checkExist = !initial && NOTEXISTS.equals(ce.getAttribute("apply")); //$NON-NLS-1$

		MModelFragments fragmentsContainer = (MModelFragments) extensionRoot;
		List<MModelFragment> fragments = fragmentsContainer.getFragments();
		boolean evalImports = false;
		for (MModelFragment fragment : fragments) {
			List<MApplicationElement> elements = fragment.getElements();
			if (elements.size() == 0) {
				continue;
			}

			for (MApplicationElement el : elements) {
				EObject o = (EObject) el;

				E4XMIResource r = (E4XMIResource) o.eResource();

				if (checkExist && applicationResource.getIDToEObjectMap().containsKey(r.getID(o))) {
					continue;
				}

				applicationResource.setID(o, r.getID(o));

				if (contributorURI != null)
					el.setContributorURI(contributorURI);

				// Remember IDs of subitems
				TreeIterator<EObject> treeIt = EcoreUtil.getAllContents(o, true);
				while (treeIt.hasNext()) {
					EObject eObj = treeIt.next();
					r = (E4XMIResource) eObj.eResource();
					if (contributorURI != null && (eObj instanceof MApplicationElement))
						((MApplicationElement) eObj).setContributorURI(contributorURI);
					applicationResource.setID(eObj, r.getInternalId(eObj));
				}
			}

			List<MApplicationElement> merged = fragment.merge(application);

			if (merged.size() > 0) {
				evalImports = true;
				addedElements.addAll(merged);
			}
		}

		if (evalImports) {
			List<MApplicationElement> localImports = fragmentsContainer.getImports();
			if (localImports != null) {
				imports.addAll(localImports);
			}
		}
	}

	/**
	 * @param extensions
	 * @param afterFragments
	 */
	private void runProcessors(IExtension[] extensions, boolean initial, boolean afterFragments) {
		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				boolean parseBoolean = Boolean.parseBoolean(ce.getAttribute("beforefragment")); //$NON-NLS-1$
				if ("processor".equals(ce.getName()) && afterFragments != parseBoolean) { //$NON-NLS-1$
					if (initial || !INITIAL.equals(ce.getAttribute("apply"))) { //$NON-NLS-1$
						runProcessor(ce);
					}
				}
			}
		}
	}

	private void runProcessor(IConfigurationElement ce) {
		IEclipseContext localContext = EclipseContextFactory.create();
		IContributionFactory factory = context.get(IContributionFactory.class);

		for (IConfigurationElement ceEl : ce.getChildren("element")) { //$NON-NLS-1$
			String id = ceEl.getAttribute("id"); //$NON-NLS-1$

			if (id == null) {
				logger.warn("No element id given"); //$NON-NLS-1$
				continue;
			}

			String key = ceEl.getAttribute("contextKey"); //$NON-NLS-1$
			if (key == null) {
				key = id;
			}

			MApplicationElement el = ModelUtils.findElementById(application, id);
			if (el == null) {
				logger.warn("Could not find element with id '" + id + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			localContext.set(key, el);
		}

		try {
			Object o = factory
					.create("bundleclass://" + ce.getContributor().getName() + "/" + ce.getAttribute("class"), //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
							context, localContext);
			if (o == null) {
				logger.warn("Unable to create processor " + ce.getAttribute("class") + " from " + ce.getContributor().getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				ContextInjectionFactory.invoke(o, Execute.class, context, localContext);
			}
		} catch (Exception e) {
			logger.warn(e, "Could not run processor"); //$NON-NLS-1$
		}
	}

	private void resolveImports(List<MApplicationElement> imports,
			List<MApplicationElement> addedElements) {
		if (imports.isEmpty())
			return;
		// now that we have all components loaded, resolve imports
		Map<MApplicationElement, MApplicationElement> importMaps = new HashMap<MApplicationElement, MApplicationElement>();
		for (MApplicationElement importedElement : imports) {
			MApplicationElement realElement = ModelUtils.findElementById(application,
					importedElement.getElementId());
			if (realElement == null) {
				logger.warn("Could not resolve an import element for '" + realElement + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			importMaps.put(importedElement, realElement);
		}

		TreeIterator<EObject> it = EcoreUtil.getAllContents(addedElements);
		List<Runnable> commands = new ArrayList<Runnable>();

		// TODO Probably use EcoreUtil.UsageCrossReferencer
		while (it.hasNext()) {
			EObject o = it.next();

			EContentsEList.FeatureIterator<EObject> featureIterator = (EContentsEList.FeatureIterator<EObject>) o
					.eCrossReferences().iterator();
			while (featureIterator.hasNext()) {
				EObject importObject = featureIterator.next();
				if (importObject.eContainmentFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
					EStructuralFeature feature = featureIterator.feature();

					MApplicationElement el = importMaps.get(importObject);
					if (el == null) {
						logger.warn("Could not resolve import for " + el); //$NON-NLS-1$
					}

					final EObject interalTarget = o;
					final EStructuralFeature internalFeature = feature;
					final MApplicationElement internalElment = el;
					final EObject internalImportObject = importObject;

					commands.add(new Runnable() {

						@Override
						public void run() {
							if (internalFeature.isMany()) {
								logger.error("Replacing"); //$NON-NLS-1$
								@SuppressWarnings("unchecked")
								List<Object> l = (List<Object>) interalTarget.eGet(internalFeature);
								int index = l.indexOf(internalImportObject);
								if (index >= 0) {
									l.set(index, internalElment);
								}
							} else {
								interalTarget.eSet(internalFeature, internalElment);
							}
						}
					});
				}
			}
		}

		for (Runnable cmd : commands) {
			cmd.run();
		}
	}
}
