/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.cisco.yangide.ext.model.editor.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MultilineTextDialog extends Dialog {
    private Composite composite;
    private String originalValue;
    private String value;
    private String title;
    private Text textControl;

    public MultilineTextDialog(Shell parentShell, final String originalValue, String title) {
        super(parentShell);
        setShellStyle(SWT.RESIZE | SWT.TOOL | SWT.TITLE);

        this.originalValue = originalValue;
        this.title = title;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        GridLayoutFactory.swtDefaults().applyTo(parent);

        composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().applyTo(composite);

        Label label = new Label(composite, SWT.WRAP);
        label.setText("Specify " + title + " value");

        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        textControl = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER_SOLID);

        textControl.setText(originalValue);
        GridDataFactory.fillDefaults().grab(true, true).hint(300, 200).applyTo(textControl);

        return parent;
    }

    @Override
    protected void okPressed() {
        // store the value from the spinners so it can be set in the text control
        value = textControl.getText();
        super.okPressed();
    }

    public String getValue() {
        return value;
    }

}
