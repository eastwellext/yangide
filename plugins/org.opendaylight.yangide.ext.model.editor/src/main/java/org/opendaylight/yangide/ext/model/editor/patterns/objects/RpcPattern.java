/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.ext.model.editor.patterns.objects;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import org.opendaylight.yangide.ext.model.Node;
import org.opendaylight.yangide.ext.model.Rpc;
import org.opendaylight.yangide.ext.model.RpcIO;
import org.opendaylight.yangide.ext.model.editor.util.YangDiagramImageProvider;
import org.opendaylight.yangide.ext.model.editor.util.YangModelUtil;
import org.opendaylight.yangide.ext.model.impl.ModelFactoryImpl;

public class RpcPattern extends DomainObjectPattern {

    @Override
    public String getCreateImageId() {
        return YangDiagramImageProvider.IMG_RPC_PROPOSAL;
    }

    @Override
    public String getCreateName() {
        return "rpc";
    }

    @Override
    public EClass getObjectEClass() {
        return YangModelUtil.MODEL_PACKAGE.getRpc();
    }

    @Override
    public PictogramElement add(IAddContext context) {
        ContainerShape cs = (ContainerShape) super.add(context);
        Rpc rpc = (Rpc) context.getNewObject();
        boolean input = false;
        boolean output = false;
        for (Node n : YangModelUtil.filter(rpc.getChildren(), YangModelUtil.MODEL_PACKAGE.getRpcIO())) {
            if (((RpcIO) n).isInput()) {
                input = true;
            } else {
                output = true;
            }
        }
        if (!input) {
            addIO(true, rpc, cs);
        }
        if (!output) {
            addIO(false, rpc, cs);
        }
        layoutPictogramElement(cs);
        return cs;
    }

    private void addIO(boolean isInput, Rpc rpc, ContainerShape cs) {
        RpcIO o = ModelFactoryImpl.eINSTANCE.createRpcIO();
        o.setInput(isInput);
        YangModelUtil.add(rpc, o, rpc.getChildren().size());
    }
}
