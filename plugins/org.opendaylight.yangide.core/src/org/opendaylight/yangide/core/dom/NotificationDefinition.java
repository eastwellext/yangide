/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.core.dom;

/**
 * @author Konstantin Zaitsev
 * date: Jul 23, 2014
 */
public class NotificationDefinition extends ASTCompositeNode {

    public NotificationDefinition(ASTNode parent) {
        super(parent);
    }

    @Override
    public String getNodeName() {
        return "notification";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.preVisit(this);
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getChildren());
        }
    }
}
