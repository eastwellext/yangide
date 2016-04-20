/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.editor.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for preference configuration blocks which can either be wrapped by a
 * {@link org.opendaylight.yangide.editor.preferences.AbstractConfigurationBlockPreferencePage} or be
 * included some preference page.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @author Alexey Kholupko
 */
public interface IPreferenceConfigurationBlock {

    /**
     * Creates the preference control.
     */
    Control createControl(Composite parent);

    /**
     * Called after creating the control. Implementations should load the preferences values and
     * update the controls accordingly.
     */
    void initialize();

    /**
     * Called when the <code>OK</code> button is pressed on the preference page. Implementations
     * should commit the configured preference settings into their form of preference storage.
     */
    void performOk();

    /**
     * Called when the <code>Defaults</code> button is pressed on the preference page.
     * Implementation should reset any preference settings to their default values and adjust the
     * controls accordingly.
     */
    void performDefaults();

    /**
     * Called when the preference page is being disposed. Implementations should free any resources
     * they are holding on to.
     */
    void dispose();
}
