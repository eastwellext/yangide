/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.editor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

import org.opendaylight.yangide.ui.preferences.IYangColorConstants;

/**
 * @author Alexey Kholupko
 */
public class YangCommentScanner extends AbstractYangScanner {

    private static String[] tokenProperties = { IYangColorConstants.YANG_COMMENT };

    /**
     * @param manager
     * @param store
     */
    public YangCommentScanner(IColorManager manager, IPreferenceStore store) {
        super(manager, store);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangide.editor.editors.AbstractYangScanner#getTokenProperties()
     */
    @Override
    protected String[] getTokenProperties() {
        return tokenProperties;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangide.editor.editors.AbstractYangScanner#createRules()
     */
    @Override
    protected List<IRule> createRules() {
        List<IRule> rules = new ArrayList<IRule>();

        IToken comment = getToken(IYangColorConstants.YANG_COMMENT);
        setDefaultReturnToken(comment);

        /*
         * @see org.opendaylight.yangide.editor.editors.YangStringScanner#createRules()
         */

        return rules;
    }
}
