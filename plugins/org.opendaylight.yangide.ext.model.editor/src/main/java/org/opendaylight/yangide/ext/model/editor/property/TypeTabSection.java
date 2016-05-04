/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.ext.model.editor.property;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import org.opendaylight.yangide.core.indexing.ElementIndexType;
import org.opendaylight.yangide.ext.model.Module;
import org.opendaylight.yangide.ext.model.TypedNode;
import org.opendaylight.yangide.ext.model.editor.dialog.YangElementListSelectionDialog;
import org.opendaylight.yangide.ext.model.editor.util.YangDiagramImageProvider;
import org.opendaylight.yangide.ext.model.editor.util.YangModelUtil;
import org.opendaylight.yangide.ext.model.editor.widget.DialogText;

public class TypeTabSection extends YangPropertySection implements ITabbedPropertyConstants {
    private DialogText type;

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);

        TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
        Composite composite = factory.createFlatFormComposite(parent);

        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
        CLabel valueLabel = factory.createCLabel(composite, "Type:");
        GridDataFactory.fillDefaults().hint(STANDARD_LABEL_WIDTH, SWT.DEFAULT).applyTo(valueLabel);
        type = new DialogText(composite, tabbedPropertySheetPage.getWidgetFactory()) {

            @Override
            protected Object openDialogBox(Text text) {
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                Module module = (Module) getDiagramTypeProvider().getFeatureProvider()
                        .getBusinessObjectForPictogramElement(getDiagram());
                YangElementListSelectionDialog dialog = new YangElementListSelectionDialog(shell, ElementIndexType.TYPE,
                        null, YangDiagramImageProvider.IMG_CUSTOM_TYPE_PROPOSAL, module, type.getText());
                if (IStatus.OK == dialog.open()) {
                    setType(dialog.getValue());
                }
                return null;
            }
        };
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(false, false).applyTo(type.getControl());
    }

    private void setType(String firstResult) {
        type.setText(firstResult);
    }

    @Override
    protected Binding createBinding(DataBindingContext bindingContext, EObject obj) {
        return bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observeDelayed(150, type.getTextControl()),
                EMFProperties.value(YangModelUtil.MODEL_PACKAGE.getNamedNode_Name())
                        .observe(((TypedNode) obj).getType()));
    }

    @Override
    protected boolean isApplied(Object bo) {
        return YangModelUtil.checkType(YangModelUtil.MODEL_PACKAGE.getTypedNode(), bo);
    }
}
