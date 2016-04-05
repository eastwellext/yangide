/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.cisco.yangide.ext.model.editor.figures;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ITargetContext;
import org.eclipse.graphiti.internal.command.CommandContainer;
import org.eclipse.graphiti.internal.command.ICommand;
import org.eclipse.graphiti.internal.command.MoveShapeFeatureCommandWithContext;
import org.eclipse.graphiti.ui.internal.command.CreateModelObjectCommand;
import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;

/**
 * @author Konstantin Zaitsev
 * date: Aug 22, 2014
 */
@SuppressWarnings("restriction")
public class FeedbackEditPolicy extends GraphicalEditPolicy {

    private FeedbackFigure feedbackFigure;
    private EditPolicy editPolicy;

    /**
     * @param feedbackFigure
     * @param editPolicy
     */
    public FeedbackEditPolicy(FeedbackFigure feedbackFigure, EditPolicy editPolicy) {
        this.feedbackFigure = feedbackFigure;
        this.editPolicy = editPolicy;
    }

    @Override
    public EditPart getTargetEditPart(Request request) {
        return editPolicy.getTargetEditPart(request);
    }

    @Override
    public void eraseTargetFeedback(Request request) {
        editPolicy.eraseTargetFeedback(request);
        feedbackFigure.setVisible(false);
    }

    @Override
    public void showTargetFeedback(Request request) {
        if (request.getType().equals(RequestConstants.REQ_CREATE) || request.getType().equals(RequestConstants.REQ_ADD)
                || request.getType().equals(RequestConstants.REQ_MOVE_CHILDREN)
                || request.getType().equals(RequestConstants.REQ_MOVE)) {
            if (request.getType().equals(RequestConstants.REQ_MOVE)) {
                request.setType(RequestConstants.REQ_MOVE_CHILDREN);
            }
            Command command = getHost().getCommand(request);
            if (command.canExecute()) {
                int position = 0;
                IContext context = getCommandContext(command);
                EditPart container = getHost();
                if (context instanceof ITargetContext) {
                    while (!container.getModel().equals(((ITargetContext) context).getTargetContainer())) {
                        container = container.getParent();
                    }
                }

                if (null != context && null != context.getProperty("parent_position")) {
                    position = (int) context.getProperty("parent_position");
                }
                EditPolicy policy = container.getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
                if (policy instanceof FeedbackEditPolicy) {
                    ((FeedbackEditPolicy) policy).showFeedback(request, position);
                }
            } else {
                hideFeedback();
            }
        }

    }

    public void showFeedback(Request request, int position) {
        feedbackFigure.setPosition(position);
        feedbackFigure.setVisible(true);
        feedbackFigure.repaint();
        editPolicy.showTargetFeedback(request);
    }

    public void hideFeedback() {
        feedbackFigure.setVisible(false);
        feedbackFigure.repaint();
    }

    protected IContext getCommandContext(Command command) {
        if (command instanceof CreateModelObjectCommand) {
            return ((CreateModelObjectCommand) command).getContext();
        }
        if (command instanceof GefCommandWrapper) {
            return getCommandContext(((GefCommandWrapper) command).getCommand());
        }
        return null;
    }

    protected IContext getCommandContext(ICommand command) {
        if (command instanceof MoveShapeFeatureCommandWithContext) {
            return ((MoveShapeFeatureCommandWithContext) command).getContext();
        }
        if (command instanceof CommandContainer) {
            for (ICommand c : ((CommandContainer) command).getCommands()) {
                IContext result = getCommandContext(c);
                if (null != result) {
                    return result;
                }
            }
        }
        return null;
    }
}
