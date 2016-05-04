/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.opendaylight.yangide.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;

import org.opendaylight.yangide.core.CoreUtil;
import org.opendaylight.yangide.core.IOpenable;
import org.opendaylight.yangide.core.OpenableElementInfo;
import org.opendaylight.yangide.core.YangModelException;
import org.opendaylight.yangide.core.buffer.BufferChangedEvent;
import org.opendaylight.yangide.core.buffer.BufferManager;
import org.opendaylight.yangide.core.buffer.IBuffer;
import org.opendaylight.yangide.core.buffer.IBufferChangedListener;
import org.opendaylight.yangide.core.buffer.NullBuffer;

/**
 * @author Konstantin Zaitsev
 * date: Jun 24, 2014
 */
public abstract class YangElement implements IOpenable, IBufferChangedListener {
    public static final IOpenable[] NO_ELEMENTS = new IOpenable[0];

    private final IOpenable parent;

    public YangElement(IOpenable parent) {
        this.parent = parent;
    }

    @Override
    public void bufferChanged(BufferChangedEvent event) {
        if (event.getBuffer().isClosed()) {
            YangModelManager.getYangModelManager().getElementsOutOfSynchWithBuffers().remove(this);
            getBufferManager().removeBuffer(event.getBuffer());
        } else {
            YangModelManager.getYangModelManager().getElementsOutOfSynchWithBuffers().add(this);
        }
    }

    @Override
    public IBuffer getBuffer() throws YangModelException {
        if (hasBuffer()) {
            // ensure element is open
            Object info = getElementInfo(null);
            IBuffer buffer = getBufferManager().getBuffer(this);
            if (buffer == null) {
                // try to (re)open a buffer
                buffer = openBuffer(null, info);
            }
            if (buffer instanceof NullBuffer) {
                return null;
            }
            return buffer;
        } else {
            return null;
        }
    }

    public boolean canBeRemovedFromCache() {
        try {
            return !hasUnsavedChanges();
        } catch (YangModelException e) {
            return false;
        }
    }

    @Override
    public boolean exists() {
        try {
            getElementInfo(null);
            return true;
        } catch (YangModelException e) {
            // element doesn't exist: return false
        }
        return false;
    }

    @Override
    public String toStringWithAncestors() {
        StringBuffer sb = new StringBuffer(getName());
        IOpenable p = getParent();
        if (p != null) {
            sb.append("[").append(p.toStringWithAncestors()).append("]");
        }
        return sb.toString();
    }

    @Override
    public IOpenable getParent() {
        return this.parent;
    }

    @Override
    public IPath getPath() {
        return null;
    }

    @Override
    public IResource getResource() {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * @param buffer
     * @return
     */
    public boolean canBufferBeRemovedFromCache(IBuffer buffer) {
        return !buffer.hasUnsavedChanges();
    }
    
    @Override
    public void close() throws YangModelException {
        if (hasBuffer()) {
            IBuffer buffer = getBufferManager().getBuffer(this);
            if (buffer != null) {
                buffer.close();
                buffer.removeBufferChangedListener(this);
            }
        }
        YangModelManager.getYangModelManager().removeInfoAndChildren(this);

    }

    public boolean hasUnsavedChanges() throws YangModelException {
        return false;
    }
    
    @Override
    public boolean isOpen() {
        return YangModelManager.getYangModelManager().getInfo(this) != null;
    }

    @Override
    public void open(IProgressMonitor progress) throws YangModelException {
        getElementInfo(progress);
    }

    public Object getElementInfo(IProgressMonitor monitor) throws YangModelException {
        YangModelManager manager = YangModelManager.getYangModelManager();
        Object info = manager.getInfo(this);
        if (info != null) {
            return info;
        }
        return openWhenClosed(createElementInfo(), monitor);
    }

    public IOpenable[] getChildren() throws YangModelException {
        Object elementInfo = getElementInfo(null);
        if (elementInfo instanceof OpenableElementInfo) {
            return ((OpenableElementInfo) elementInfo).getChildren();
        } else {
            return NO_ELEMENTS;
        }
    }

    @Override
    public String getName() {
        return getPath().toString();
    }

    @Override
    public int hashCode() {
        if (this.parent == null) {
            return super.hashCode();
        }
        return CoreUtil.combineHashCodes(getName().hashCode(), this.parent.hashCode());
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        // Yang model parent is null
        if (this.parent == null) {
            return super.equals(o);
        }

        // assume instanceof check is done in subclass
        YangElement other = (YangElement) o;
        return getName().equals(other.getName()) && this.parent.equals(other.parent);
    }

    protected void generateInfos(OpenableElementInfo info, HashMap<IOpenable, OpenableElementInfo> newElements,
            IProgressMonitor monitor) throws YangModelException {

        // open its ancestors if needed
        openAncestors(newElements, monitor);

        // validate existence
        IResource underlResource = getResource();
        IStatus status = validateExistence(underlResource);
        if (!status.isOK()) {
            if (status.getException() != null) {
                throw new YangModelException(status.getException(), status.getCode());
            } else {
                throw new YangModelException(status.getMessage());
            }
        }

        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        // puts the info before building the structure so that questions to the handle behave as if
        // the element existed
        // (case of compilation units becoming working copies)
        newElements.put(this, info);

        // build the structure of the openable (this will open the buffer if needed)
        try {
            OpenableElementInfo openableElementInfo = info;
            boolean isStructureKnown = buildStructure(openableElementInfo, monitor, newElements, underlResource);
            openableElementInfo.setIsStructureKnown(isStructureKnown);
        } catch (YangModelException e) {
            newElements.remove(this);
            throw e;
        }

        // remove out of sync buffer for this element
        YangModelManager.getYangModelManager().getElementsOutOfSynchWithBuffers().remove(this);
    }

    protected void openAncestors(HashMap<IOpenable, OpenableElementInfo> newElements, IProgressMonitor monitor)
            throws YangModelException {
        YangElement openableParent = (YangElement) getParent();
        if (openableParent != null && !openableParent.isOpen()) {
            openableParent.generateInfos(openableParent.createElementInfo(), newElements, monitor);
        }
    }

    /**
     * Opens a buffer on the contents of this element, and returns the buffer, or returns
     * <code>null</code> if opening fails. By default, do nothing - subclasses that have buffers
     * must override as required.
     */
    protected IBuffer openBuffer(IProgressMonitor pm, Object info) throws YangModelException {
        return null;
    }

    protected OpenableElementInfo createElementInfo() {
        return new OpenableElementInfo();
    }

    protected boolean hasBuffer() {
        return false;
    }

    /*
     * Opens an <code>Openable</code> that is known to be closed (no check for
     * <code>isOpen()</code>). Returns the created element info.
     */
    protected OpenableElementInfo openWhenClosed(OpenableElementInfo info, IProgressMonitor monitor)
            throws YangModelException {
        YangModelManager manager = YangModelManager.getYangModelManager();
        HashMap<IOpenable, OpenableElementInfo> newElements = new HashMap<IOpenable, OpenableElementInfo>();
        generateInfos(info, newElements, monitor);
        if (info == null) {
            info = newElements.get(this);
        }
        manager.putInfos(this, newElements);
        return info;
    }

    protected BufferManager getBufferManager() {
        return BufferManager.getDefaultBufferManager();
    }

    /**
     * Builds this element's structure and properties in the given info object, based on this
     * element's current contents (reuse buffer contents if this element has an open buffer, or
     * resource contents if this element does not have an open buffer). Children are placed in the
     * given newElements table (note, this element has already been placed in the newElements
     * table). Returns true if successful, or false if an error is encountered while determining the
     * structure of this element.
     */
    protected abstract boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm,
            Map<IOpenable, OpenableElementInfo> newElements, IResource underlyingResource) throws YangModelException;

    /*
     * Validates the existence of this openable. Returns a non ok status if it doesn't exist.
     */
    abstract protected IStatus validateExistence(IResource underlyingResource);

    /**
     * @return element type.
     */
    public abstract YangElementType getElementType();
}
