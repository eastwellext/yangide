/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.cisco.yangide.ext.model.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;

import com.cisco.yangide.core.YangCorePlugin;
import com.cisco.yangide.editor.YangEditorPlugin;
import com.cisco.yangide.editor.editors.IYangEditor;
import com.cisco.yangide.editor.editors.YangEditor;
import com.cisco.yangide.editor.editors.YangSourceViewer;
import com.cisco.yangide.ext.model.Module;
import com.cisco.yangide.ext.model.editor.editors.YangDiagramEditor;
import com.cisco.yangide.ext.model.editor.editors.YangDiagramEditorInput;
import com.cisco.yangide.ext.model.editor.sync.ModelSynchronizer;

/**
 * @author Konstantin Zaitsev
 * date: Aug 7, 2014
 */
public class YangMultiPageEditorPart extends MultiPageEditorPart implements IYangEditor {

    private YangEditor yangSourceEditor;
    private YangSourceViewer yangSourceViewer;
    private YangDiagramEditor yangDiagramEditor;
    private ModelSynchronizer modelSynchronizer;

    @Override
    protected void createPages() {
        yangSourceEditor = new YangEditor();
        yangDiagramEditor = new YangDiagramEditor(yangSourceEditor);
        modelSynchronizer = new ModelSynchronizer(yangSourceEditor, yangDiagramEditor);
        initSourcePage();
        initDiagramPage();
        modelSynchronizer.init();
        modelSynchronizer.enableNotification();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        yangSourceEditor.doSave(monitor);
    }

    @Override
    public void doSaveAs() {
        yangSourceEditor.doSaveAs();
    }

    @Override
    public boolean isSaveAsAllowed() {
        return yangSourceEditor.isSaveAsAllowed();
    }

    @Override
    public boolean isDirty() {
        return yangSourceEditor.isDirty();
    }

    @Override
    protected IEditorSite createSite(IEditorPart editor) {
        return new MultiPageEditorSite(this, editor) {
            @Override
            protected void handlePostSelectionChanged(SelectionChangedEvent event) {
                if ((event.getSelection() instanceof StructuredSelection && getActivePage() == 1)
                        || (getActivePage() == 0 && !(event.getSelection() instanceof StructuredSelection))) {
                    super.handlePostSelectionChanged(event);
                }
            }
        };
    }

    private void initDiagramPage() {
        try {
            Module diagModule = modelSynchronizer.getDiagramModule();
            YangDiagramEditorInput input = new YangDiagramEditorInput(URI.createURI("tmp:/local"), getFile(),
                    "com.cisco.yangide.ext.model.editor.editorDiagramTypeProvider", diagModule);
            addPage(1, yangDiagramEditor, input);
            setPageText(1, "Diagram");

            yangDiagramEditor.setSourceModelManager(modelSynchronizer.getSourceModelManager());
        } catch (PartInitException e) {
            YangEditorPlugin.log(e);
        }
    }

    private IFile getFile() {
        if (null != yangSourceEditor && null != yangSourceEditor.getEditorInput()) {
            if (yangSourceEditor.getEditorInput() instanceof IFileEditorInput) {
                IFileEditorInput fileEI = (IFileEditorInput) yangSourceEditor.getEditorInput();
                return fileEI.getFile();
            }
        }
        return null;
    }

    private void initSourcePage() {
        try {
            addPage(0, yangSourceEditor, getEditorInput());
            setPageText(0, "Source");
            yangSourceViewer = (YangSourceViewer) yangSourceEditor.getViewer();
        } catch (PartInitException e) {
            YangEditorPlugin.log(e);
        }
        setPartName(yangSourceEditor.getPartName());
    }

    @Override
    protected void pageChange(int newPageIndex) {
        if (newPageIndex == 1) {
            modelSynchronizer.syncWithSource();
            if (modelSynchronizer.isSourceInvalid()) {
                MessageDialog.openWarning(getSite().getShell(), "Yang source is invalid",
                        "Yang source has syntax error and diagram view cannot be synchronized correctly.\n"
                                + "Please correct syntax error first.");
            }
            yangSourceViewer.disableProjection();
            if (yangSourceViewer.getReconciler() != null) {
                yangSourceViewer.getReconciler().uninstall();
            }
            yangSourceViewer.disableTextListeners();
            try {
                getEditorSite().getPage().showView("org.eclipse.ui.views.PropertySheet");
            } catch (PartInitException e) {
                YangEditorPlugin.log(e);
            }
            yangDiagramEditor.startSourceSelectionUpdater();
        } else {
            yangDiagramEditor.stopSourceSelectionUpdater();
            IRegion highlightRange = yangSourceEditor.getHighlightRange();
            yangSourceViewer.enableTextListeners();
            yangSourceViewer.updateDocument();

            yangSourceViewer.enableProjection();
            if (yangSourceViewer.getReconciler() != null) {
                yangSourceViewer.getReconciler().install(yangSourceEditor.getViewer());
            }
            setSourceSelection(highlightRange);
        }
        super.pageChange(newPageIndex);
    }

    private void setSourceSelection(IRegion highlightRange) {
        if (highlightRange != null) {
            Point selectedRange = yangSourceViewer.getSelectedRange();
            if (selectedRange.x != highlightRange.getOffset() && selectedRange.y != highlightRange.getLength()) {
                yangSourceEditor.selectAndReveal(highlightRange.getOffset(), highlightRange.getLength());
            }
        }
    }

    /**
     * @return the yangSourceEditor
     */
    public YangEditor getYangSourceEditor() {
        return yangSourceEditor;
    }

    /**
     * @return the yangDiagramEditor
     */
    public YangDiagramEditor getYangDiagramEditor() {
        return yangDiagramEditor;
    }

    @Override
    public void dispose() {
        try {
            modelSynchronizer.dispose();
        } catch (Exception e) {
            YangCorePlugin.log(e);
        }
        super.dispose();
    }

    @Override
    public void selectAndReveal(int offset, int length) {
        yangSourceEditor.selectAndReveal(offset, length);
    }
}
