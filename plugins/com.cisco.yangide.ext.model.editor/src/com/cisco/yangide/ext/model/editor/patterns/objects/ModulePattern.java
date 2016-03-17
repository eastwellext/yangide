/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.cisco.yangide.ext.model.editor.patterns.objects;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import com.cisco.yangide.ext.model.editor.util.YangDiagramImageProvider;
import com.cisco.yangide.ext.model.editor.util.YangModelUtil;
import com.cisco.yangide.ext.model.impl.ModelFactoryImpl;

public class ModulePattern extends DomainObjectPattern {

    @Override
    public String getCreateName() {
        return "module";
    }

    @Override
    protected EObject createEObject() {
        return ModelFactoryImpl.eINSTANCE.createModule();
    }

    @Override
    public String getCreateImageId() {
        return YangDiagramImageProvider.IMG_MODULE_PROPOSAL;
    }

    @Override
    public EClass getObjectEClass() {
        return YangModelUtil.MODEL_PACKAGE.getModule();
    }

}
