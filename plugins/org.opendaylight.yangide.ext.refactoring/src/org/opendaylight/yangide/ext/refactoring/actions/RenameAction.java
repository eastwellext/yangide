/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.ext.refactoring.actions;

import static org.opendaylight.yangide.ext.refactoring.actions.RenameSupport.isDirectRename;
import static org.opendaylight.yangide.ext.refactoring.actions.RenameSupport.isIndirectRename;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchSite;

import org.opendaylight.yangide.core.YangModelException;
import org.opendaylight.yangide.core.dom.ASTNamedNode;
import org.opendaylight.yangide.core.dom.ASTNode;
import org.opendaylight.yangide.core.indexing.ElementIndexInfo;
import org.opendaylight.yangide.editor.editors.YangEditor;
import org.opendaylight.yangide.ext.refactoring.RefactorUtil;
import org.opendaylight.yangide.ext.refactoring.ui.RenameLinkedMode;

/**
 * @author Konstantin Zaitsev
 * date: Jul 29, 2014
 */
public class RenameAction extends SelectionDispatchAction {

    private YangEditor editor;
    private ASTNode node;

    public RenameAction(IWorkbenchSite site) {
        super(site);
        this.node = null;
        setText("Rename...");
        setToolTipText("Rename the selected element");
        setDescription("Rename the selected element");
    }

    public RenameAction(YangEditor editor) {
        this(editor.getSite());
        this.editor = editor;
    }

    @Override
    public void selectionChanged(ITextSelection selection) {

        try {
            if (editor.getModule() != null) {
                this.node = editor.getModule().getNodeAtPosition(selection.getOffset());
            } else {
                this.node = null;
            }
        } catch (YangModelException e) {
            this.node = null;
        }

        boolean enabled = false;
        if (this.node != null) {
            if (this.node instanceof ASTNamedNode) {
                ASTNamedNode nnode = (ASTNamedNode) node;
                if (nnode.getNameStartPosition() <= selection.getOffset()
                        && (nnode.getNameStartPosition() + nnode.getNameLength()) >= selection.getOffset()) {
                    enabled = isDirectRename(nnode) || isIndirectRename(nnode);
                }
            }
        }
        setEnabled(enabled);
    }

    @Override
    public void run(ITextSelection selection) {
        if (node != null && (isDirectRename(node) || isIndirectRename(node))) {
            RenameLinkedMode activeLinkedMode = RenameLinkedMode.getActiveLinkedMode();
            if (activeLinkedMode != null) {
                if (activeLinkedMode.isCaretInLinkedPosition()) {
                    activeLinkedMode.startFullDialog();
                    return;
                } else {
                    activeLinkedMode.cancel();
                }
            }

            ASTNode originalNode = null;
            IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
            if (isIndirectRename(node)) {
                ElementIndexInfo info = RefactorUtil.getByReference(file.getProject(), node);
                if (info != null) {
                    if (info.getEntry() != null && !info.getEntry().isEmpty()) {
                        MessageDialog.openInformation(getShell(), "Rename",
                                "Operation unavailable on the current selection.\n"
                                        + "The original element is located in JAR file and cannot be renamed.");
                        return;
                    }
                    originalNode = RefactorUtil.resolveIndexInfo(info);
                }
                if (originalNode == null) {
                    MessageDialog.openInformation(getShell(), "Rename",
                            "Operation unavailable on the current selection.\n"
                                    + "Cannot find the original element for the reference.");
                    return;
                }
                file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(info.getPath()));
            } else {
                originalNode = node;
            }
            new RenameLinkedMode((ASTNamedNode) originalNode, file, (ASTNamedNode) node, editor).start();
        } else {
            MessageDialog.openInformation(getShell(), "Rename", "Operation unavailable on the current selection.\n"
                    + "Select a grouping name, module name, type name or identify name.");
        }
    }
}
