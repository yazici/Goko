package org.goko.tools.macro.part.management;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;
import org.goko.common.GkUiComponent;
import org.goko.common.preferences.fieldeditor.ui.UiBooleanFieldEditor;
import org.goko.common.preferences.fieldeditor.ui.UiColorFieldEditor;
import org.goko.common.preferences.fieldeditor.ui.UiStringFieldEditor;
import org.goko.core.common.exception.GkException;
import org.goko.core.log.GkLog;
import org.goko.tools.macro.bean.GCodeMacro;
import org.goko.tools.macro.service.IGCodeMacroService;

/**
 * Part for macro management
 * 
 * @author Psyko
 * @date 16 oct. 2016
 */
public class MacroManagementPart extends GkUiComponent<MacroManagementController, MacroManagementModel> {
	private static final GkLog LOG = GkLog.getLogger(MacroManagementPart.class);
	/** The macro */
	@Inject
	IGCodeMacroService macroService;

	/**
	 * Constructor
	 */
	@Inject
	public MacroManagementPart(IEclipseContext context) {
		super(context, new MacroManagementController());
	}

	@PostConstruct
	public void postConstruct(Composite parent) throws GkException {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		composite.setLayout(gl_composite);

		Composite composite_2 = new Composite(composite, SWT.NONE);
		GridLayout gl_composite_2 = new GridLayout(1, false);
		gl_composite_2.marginTop = 5;
		gl_composite_2.marginWidth = 0;
		gl_composite_2.marginHeight = 0;
		composite_2.setLayout(gl_composite_2);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		Group composite_1 = new Group(composite, SWT.NONE);
		composite_1.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		composite_1.setText("Macro");
		composite_1.setLayout(new GridLayout(1, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		UiStringFieldEditor macroNameFieldEditor = new UiStringFieldEditor(composite_1, SWT.FILL);
		((GridData) macroNameFieldEditor.getControl().getLayoutData()).horizontalAlignment = SWT.FILL;
		macroNameFieldEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		macroNameFieldEditor.setLabel("Name");
		macroNameFieldEditor.setPropertyName(MacroManagementModel.MACRO_NAME);

		Button editContentButton = new Button(composite_1, SWT.NONE);
		editContentButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				try {
					getController().openMacroInEditor();
				} catch (GkException exception) {
					LOG.error(exception);
				}
			}
		});
		editContentButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		editContentButton.setText("Open in GCode editor");

		UiBooleanFieldEditor macroRequestConfirm = new UiBooleanFieldEditor(composite_1, SWT.NONE);
		macroRequestConfirm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		macroRequestConfirm.setLabel("Request confirm before execution");
		macroRequestConfirm.setPropertyName(MacroManagementModel.REQUEST_CONFIRM);
		getController().addFieldEditor(macroRequestConfirm);
		new Label(composite_1, SWT.NONE);

		UiBooleanFieldEditor macroDisplayButton = new UiBooleanFieldEditor(composite_1, SWT.NONE);
		macroDisplayButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		macroDisplayButton.setLabel("Display in macro panel");
		macroDisplayButton.setPropertyName(MacroManagementModel.DISPLAY_BUTTON);
		getController().addFieldEditor(macroDisplayButton);

		UiBooleanFieldEditor overrideButtonColorEditor = new UiBooleanFieldEditor(composite_1, SWT.NONE);
		overrideButtonColorEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		overrideButtonColorEditor.setLabel("Override button color");
		overrideButtonColorEditor.setPropertyName(MacroManagementModel.OVERRIDE_BUTTON_COLOR);
		getController().addFieldEditor(overrideButtonColorEditor);

		UiColorFieldEditor macroButtonColor = new UiColorFieldEditor(composite_1, SWT.NONE);
		macroButtonColor.setLabel("Button color");
		macroButtonColor.setPropertyName(MacroManagementModel.BUTTON_COLOR);
		getController().addFieldEditor(macroButtonColor);

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		getController().addFieldEditor(macroNameFieldEditor);

		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));

		Composite composite_4 = new Composite(composite_1, SWT.NONE);
		GridLayout gl_composite_4 = new GridLayout(3, false);
		gl_composite_4.verticalSpacing = 0;
		gl_composite_4.marginHeight = 0;
		composite_4.setLayout(gl_composite_4);
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		Label lblNewLabel_1 = new Label(composite_4, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		Button applyButton = new Button(composite_4, SWT.NONE);
		applyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				try {
					getController().applyChangesToGCodeMacro();
				} catch (GkException exception) {
					LOG.error(exception);
				}
			}
		});
		GridData gd_applyButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_applyButton.widthHint = 70;
		applyButton.setLayoutData(gd_applyButton);
		applyButton.setText("Apply");

		Button cancelButton = new Button(composite_4, SWT.NONE);
		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				getController().cancelChangesToGCodeMacro();
			}
		});
		GridData gd_cancelButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_cancelButton.widthHint = 70;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");

		getController().addEnableBinding(applyButton, MacroManagementModel.DIRTY);
		getController().addEnableBinding(cancelButton, MacroManagementModel.DIRTY);

		Composite composite_6 = new Composite(composite_2, SWT.NONE);
		GridLayout gl_composite_6 = new GridLayout(2, false);
		gl_composite_6.marginWidth = 0;
		gl_composite_6.marginHeight = 0;
		composite_6.setLayout(gl_composite_6);

		Button btnNew = new Button(composite_6, SWT.NONE);
		btnNew.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				try {
					getController().createNewMacro();
					getController().enableEditionMode();
				} catch (GkException e1) {
					LOG.error(e1);
				}
			}
		});
		btnNew.setText("New");
		btnNew.setImage(ResourceManager.getPluginImage("org.goko.tools.macro", "resources/icons/new-macro.png"));

		Button btnEdit = new Button(composite_6, SWT.NONE);
		btnEdit.setText("Edit");
		btnEdit.setImage(ResourceManager.getPluginImage("org.goko.tools.macro", "resources/icons/script-edit.png"));
		btnEdit.addMouseListener(new MouseAdapter() {
			/** (inheritDoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseUp(MouseEvent e) {				
				getController().enableEditionMode();
			}
		});
		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		GridLayout gl_composite_3 = new GridLayout(1, false);
		gl_composite_3.marginHeight = 0;
		gl_composite_3.marginWidth = 0;
		composite_3.setLayout(gl_composite_3);
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		ListViewer listViewer = new ListViewer(composite_3, SWT.BORDER | SWT.V_SCROLL);
		List list = listViewer.getList();
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		listViewer.setLabelProvider(new LabelProvider() {
			/**
			 * (inheritDoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				return ((GCodeMacro) element).getCode();
			}
		});

		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					getController().selectMacro((GCodeMacro) selection.getFirstElement());
				}
			}
		});
		listViewer.setContentProvider(contentProvider);

		Composite composite_5 = new Composite(composite_2, SWT.NONE);
		GridLayout gl_composite_5 = new GridLayout(1, false);
		gl_composite_5.marginWidth = 0;
		gl_composite_5.marginHeight = 0;
		composite_5.setLayout(gl_composite_5);
		composite_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		composite_5.setBounds(0, 0, 64, 64);

		Button btnDelete = new Button(composite_5, SWT.NONE);
		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnDelete.setText("Delete");
		btnDelete.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				boolean confirm = MessageDialog.openConfirm(parent.getShell(), "Confirm",
						"Are you sure you want to delete the selected macro ?");
				if (confirm) {
					try {
						getController().deleteSelectedMacro();
					} catch (GkException e1) {
						LOG.error(e1);
					}
				}
			}
		});
		btnDelete.setImage(ResourceManager.getPluginImage("org.goko.tools.macro", "resources/icons/delete-macro.png"));

		getController().addItemsBinding(listViewer, MacroManagementModel.AVAILABLE_MACRO);
		getController().addSelectionBinding(listViewer, MacroManagementModel.SELECTED_MACRO);
		getController().addEnableBinding(macroButtonColor, MacroManagementModel.OVERRIDE_BUTTON_COLOR);
		
		getController().addEnableReverseBinding(btnEdit, MacroManagementModel.EDITION_MODE);
		getController().addEnableReverseBinding(btnNew, MacroManagementModel.EDITION_MODE);
		getController().addEnableReverseBinding(btnDelete, MacroManagementModel.EDITION_MODE);
		getController().addEnableReverseBinding(listViewer.getList(), MacroManagementModel.EDITION_MODE);
		
		getController().addEnableBinding(macroNameFieldEditor, MacroManagementModel.EDITION_MODE);
		//getController().addEnableBinding(editContentButton, MacroManagementModel.EDITION_MODE);
		getController().addEnableBinding(macroDisplayButton, MacroManagementModel.EDITION_MODE);
		getController().addEnableBinding(macroRequestConfirm, MacroManagementModel.EDITION_MODE);
		getController().addEnableBinding(overrideButtonColorEditor, MacroManagementModel.EDITION_MODE);
	}
}