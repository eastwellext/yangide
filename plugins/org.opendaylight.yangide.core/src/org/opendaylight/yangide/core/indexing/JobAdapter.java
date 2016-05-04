/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.core.indexing;


/**
 * @author Konstantin Zaitsev
 * date: Aug 4, 2014
 */
public abstract class JobAdapter implements IJob {

    @Override
    public boolean belongsTo(String jobFamily) {
        return false;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void ensureReadyToRun() {
    }

    @Override
    public String getJobFamily() {
        return "yang_generic";
    }
}
