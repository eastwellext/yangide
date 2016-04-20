/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.ui.nls;

import org.eclipse.osgi.util.NLS;

/**
 * @author Konstantin Zaitsev
 * date: Jul 3, 2014
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.opendaylight.yangide.ui.nls.messages"; //$NON-NLS-1$
    public static String pref_Base_cleanCodeGen;
    public static String pref_Base_enableTracing;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
