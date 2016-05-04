/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.editor.editors.text;

import org.eclipse.jface.text.source.Annotation;

/**
 * @author Konstantin Zaitsev
 * date: Jul 10, 2014
 */
public class YangSyntaxAnnotation extends Annotation {
    public static final String TYPE = "org.opendaylight.yangide.core.syntax";

    public YangSyntaxAnnotation(String msg) {
        super(TYPE, false, msg);
    }
}
