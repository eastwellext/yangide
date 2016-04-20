/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.editor.editors.text.hover;

import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;

import org.opendaylight.yangide.core.YangCorePlugin;
import org.opendaylight.yangide.core.YangModelException;
import org.opendaylight.yangide.core.dom.ASTNode;
import org.opendaylight.yangide.core.dom.Module;
import org.opendaylight.yangide.editor.editors.YangEditor;
import org.opendaylight.yangide.editor.editors.text.YangMarkerAnnotation;
import org.opendaylight.yangide.editor.editors.text.YangSyntaxAnnotation;
import org.opendaylight.yangide.editor.editors.text.help.HelpCompositionUtils;

/**
 * @author Konstantin Zaitsev
 * date: Jul 23, 2014
 */
public class YangTextHover extends DefaultTextHover implements ITextHoverExtension {

    private IInformationControlCreator hoverControlCreator;
    private IInformationControlCreator presenterCtrlCreator;
    private IEditorPart editor;

    public YangTextHover(ISourceViewer sourceViewer) {
        super(sourceViewer);
    }

    /**
     * @param editor the editor to set
     */
    public void setEditor(IEditorPart editor) {
        this.editor = editor;
    }

    /**
     * @return the editor
     */
    public IEditorPart getEditor() {
        return editor;
    }

    @Override
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        @SuppressWarnings("deprecation")
        String info = super.getHoverInfo(textViewer, hoverRegion);
        if (info != null) {
            return HelpCompositionUtils.wrapHtmlText(info, null);
        }

        String hoverInfo = null;
        YangEditor editor = (YangEditor) getEditor();
        if (editor != null) {
            try {
                Module module = editor.getModule();
                if (module != null) {
                    ASTNode node = module.getNodeAtPosition(hoverRegion.getOffset());
                    hoverInfo = HelpCompositionUtils.getNodeHelp(node);
                }
            } catch (YangModelException e) {
                YangCorePlugin.log(e);
            }
        }
        return hoverInfo;
    }

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        if (presenterCtrlCreator == null) {
            presenterCtrlCreator = new PresenterControlCreator();
        }
        if (hoverControlCreator == null) {
            hoverControlCreator = new HoverControlCreator(presenterCtrlCreator);
        }
        return hoverControlCreator;
    }

    @Override
    protected boolean isIncluded(Annotation annotation) {
        return YangSyntaxAnnotation.TYPE.equals(annotation.getType())
                || YangMarkerAnnotation.TYPE.equals(annotation.getType());
    }
}
