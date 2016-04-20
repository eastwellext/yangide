/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.ext.model.editor.patterns.objects;

import org.eclipse.emf.ecore.EClass;

import org.opendaylight.yangide.ext.model.editor.util.YangDiagramImageProvider;
import org.opendaylight.yangide.ext.model.editor.util.YangModelUtil;

public class IdentityPattern extends DomainObjectPattern {

    @Override
    public EClass getObjectEClass() {
        return YangModelUtil.MODEL_PACKAGE.getIdentity();
    }

    @Override
    public String getCreateImageId() {
        return YangDiagramImageProvider.IMG_IDENTITY_PROPOSAL;
    }

    @Override
    public String getCreateName() {
        return "identity";
    }

}
