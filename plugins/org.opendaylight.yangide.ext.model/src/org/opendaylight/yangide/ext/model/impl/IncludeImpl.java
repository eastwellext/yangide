/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.ext.model.impl;

import org.opendaylight.yangide.ext.model.Include;
import org.opendaylight.yangide.ext.model.ModelPackage;
import org.opendaylight.yangide.ext.model.Node;
import org.opendaylight.yangide.ext.model.Submodule;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Include</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.opendaylight.yangide.ext.model.impl.IncludeImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.opendaylight.yangide.ext.model.impl.IncludeImpl#getSubmodule <em>Submodule</em>}</li>
 * </ul>
 *
 * @generated
 */
public class IncludeImpl extends MinimalEObjectImpl.Container implements Include {
    /**
     * The cached value of the '{@link #getParent() <em>Parent</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getParent()
     * @generated
     * @ordered
     */
    protected Node parent;

    /**
     * The cached value of the '{@link #getSubmodule() <em>Submodule</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSubmodule()
     * @generated
     * @ordered
     */
    protected Submodule submodule;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected IncludeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ModelPackage.Literals.INCLUDE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Node getParent() {
        if (parent != null && parent.eIsProxy()) {
            InternalEObject oldParent = (InternalEObject)parent;
            parent = (Node)eResolveProxy(oldParent);
            if (parent != oldParent) {
                if (eNotificationRequired())
                    eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.INCLUDE__PARENT, oldParent, parent));
            }
        }
        return parent;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Node basicGetParent() {
        return parent;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setParent(Node newParent) {
        Node oldParent = parent;
        parent = newParent;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.INCLUDE__PARENT, oldParent, parent));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Submodule getSubmodule() {
        return submodule;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetSubmodule(Submodule newSubmodule, NotificationChain msgs) {
        Submodule oldSubmodule = submodule;
        submodule = newSubmodule;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.INCLUDE__SUBMODULE, oldSubmodule, newSubmodule);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSubmodule(Submodule newSubmodule) {
        if (newSubmodule != submodule) {
            NotificationChain msgs = null;
            if (submodule != null)
                msgs = ((InternalEObject)submodule).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.INCLUDE__SUBMODULE, null, msgs);
            if (newSubmodule != null)
                msgs = ((InternalEObject)newSubmodule).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.INCLUDE__SUBMODULE, null, msgs);
            msgs = basicSetSubmodule(newSubmodule, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.INCLUDE__SUBMODULE, newSubmodule, newSubmodule));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case ModelPackage.INCLUDE__SUBMODULE:
                return basicSetSubmodule(null, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ModelPackage.INCLUDE__PARENT:
                if (resolve) return getParent();
                return basicGetParent();
            case ModelPackage.INCLUDE__SUBMODULE:
                return getSubmodule();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case ModelPackage.INCLUDE__PARENT:
                setParent((Node)newValue);
                return;
            case ModelPackage.INCLUDE__SUBMODULE:
                setSubmodule((Submodule)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case ModelPackage.INCLUDE__PARENT:
                setParent((Node)null);
                return;
            case ModelPackage.INCLUDE__SUBMODULE:
                setSubmodule((Submodule)null);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case ModelPackage.INCLUDE__PARENT:
                return parent != null;
            case ModelPackage.INCLUDE__SUBMODULE:
                return submodule != null;
        }
        return super.eIsSet(featureID);
    }

} //IncludeImpl
