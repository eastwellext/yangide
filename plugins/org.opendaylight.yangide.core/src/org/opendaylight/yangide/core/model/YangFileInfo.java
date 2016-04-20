/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.core.model;

import org.opendaylight.yangide.core.OpenableElementInfo;
import org.opendaylight.yangide.core.dom.Module;

/**
 * @author Konstantin Zaitsev
 * date: Jun 26, 2014
 */
public class YangFileInfo extends OpenableElementInfo {
    private Module module;

    /**
     * @return the module
     */
    public Module getModule() {
        return module;
    }

    /**
     * @param module the module to set
     */
    public void setModule(Module module) {
        this.module = module;
    }
}
