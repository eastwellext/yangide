/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.cisco.yangide.ext.model.editor.patterns.objects;

import org.eclipse.emf.ecore.EClass;

import com.cisco.yangide.ext.model.ModelPackage;
import com.cisco.yangide.ext.model.editor.util.YangDiagramImageProvider;

public class LeafPattern extends DomainObjectPattern {

    @Override
    public String getCreateName() {
        return "leaf";
    }

    @Override
    public String getCreateImageId() {
        return YangDiagramImageProvider.IMG_LEAF_PROPOSAL;
    }

    @Override
    public EClass getObjectEClass() {
        return ModelPackage.eINSTANCE.getLeaf();
    }

}
