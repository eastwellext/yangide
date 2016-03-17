/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.cisco.yangide.core.dom;

/**
 * @author Konstantin Zaitsev
 * date: Jun 26, 2014
 */
public abstract class ASTNamedNode extends ASTNode {

    private String name;

    private int nameStartPosition = -1;
    private int nameLength = 0;

    public ASTNamedNode(ASTNode parent) {
        this(parent, null);
    }

    public ASTNamedNode(ASTNode parent, String name) {
        super(parent);
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the nameStartPosition
     */
    public int getNameStartPosition() {
        return nameStartPosition;
    }

    /**
     * @param nameStartPosition the nameStartPosition to set
     */
    public void setNameStartPosition(int nameStartPosition) {
        this.nameStartPosition = nameStartPosition;
    }

    /**
     * @return the nameLength
     */
    public int getNameLength() {
        return nameLength;
    }

    /**
     * @param nameLength the nameLength to set
     */
    public void setNameLength(int nameLength) {
        this.nameLength = nameLength;
    }
}
