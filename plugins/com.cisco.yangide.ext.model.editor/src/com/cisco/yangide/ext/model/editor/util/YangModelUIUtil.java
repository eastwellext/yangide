/*******************************************************************************
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.cisco.yangide.ext.model.editor.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.LayoutContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import com.cisco.yangide.ext.model.ContainingNode;
import com.cisco.yangide.ext.model.NamedNode;
import com.cisco.yangide.ext.model.TypedNode;
import com.cisco.yangide.ext.model.Typeref;

public class YangModelUIUtil {

    private YangModelUIUtil() {
        super();
    }

    public static final int DEFAULT_WIDTH = 225;
    public static final int DEFAULT_TEXT_HEIGHT = 18;
    public static final int DEFAULT_V_ALIGN = 5;
    public static final int DEFAULT_H_ALIGN = 2;
    public static final int DEFAULT_OBJECT_NUMBER_IND = 2;

    public static final int DEFAULT_BOX_ANCHOR_RADIUS = 2;

    public static final int DEFAULT_COMPOSITE_HEIGHT = 30;

    private static final ShapeVerticalComparator COMPARATOR = new ShapeVerticalComparator();

    private static Map<EAttribute, String> attributeShapes = new HashMap<EAttribute, String>();

    static {
        attributeShapes.put(YangModelUtil.MODEL_PACKAGE.getNamedNode_Name(), PropertyUtil.OBJECT_HEADER_TEXT_SHAPE_KEY);
        attributeShapes.put(YangModelUtil.MODEL_PACKAGE.getUses_QName(), PropertyUtil.OBJECT_HEADER_TEXT_SHAPE_KEY);
    }

    private static class ShapeVerticalComparator implements Comparator<Shape> {

        @Override
        public int compare(Shape o1, Shape o2) {
            if (null == o1 && null == o2) {
                return 0;
            }
            if (null != o1 && null == o2) {
                return 1;
            }
            if (null == o1 && null != o2) {
                return -1;
            }
            if (PropertyUtil.isBusinessObjectShape(o1) && PropertyUtil.isBusinessObjectShape(o2)) {
                return o1.getGraphicsAlgorithm().getY() > o2.getGraphicsAlgorithm().getY() ? 1
                        : o1.getGraphicsAlgorithm().getY() == o2.getGraphicsAlgorithm().getY() ? 0 : -1;
            }
            return o1.getGraphicsAlgorithm().getY()
                    + o1.getGraphicsAlgorithm().getHeight() > o2.getGraphicsAlgorithm().getY()
                            + o2.getGraphicsAlgorithm().getHeight()
                                    ? 1
                                    : o1.getGraphicsAlgorithm().getY()
                                            + o1.getGraphicsAlgorithm().getHeight() == o2.getGraphicsAlgorithm().getY()
                                                    + o2.getGraphicsAlgorithm().getHeight() ? 0 : -1;
        }

    }

    public static void sortPictogramElements(EList<Shape> elements) {
        ECollections.sort(elements, COMPARATOR);
    }

    public static void sortPictogramElements(List<Shape> elements) {
        Collections.sort(elements, COMPARATOR);
    }

    public static int getPositionInParent(ContainerShape parent, Shape child, IFeatureProvider fp) {
        EList<Shape> elements = parent.getChildren();
        sortPictogramElements(elements);
        int pos = 0;
        for (Shape shape : elements) {
            if (child == shape) {
                return pos;
            }
            if (null != fp.getBusinessObjectForPictogramElement(shape)
                    && YangModelUtil.checkType(YangModelUtil.MODEL_PACKAGE.getContainingNode(),
                            fp.getBusinessObjectForPictogramElement(parent))
                    && ((ContainingNode) fp.getBusinessObjectForPictogramElement(parent)).getChildren()
                            .contains(fp.getBusinessObjectForPictogramElement(shape))) {
                pos++;
            }
        }
        return pos;
    }

    public static int getPositionInParent(ContainerShape parent, int y, IFeatureProvider fp) {
        List<Shape> elements = filterBusinessObjectShapes(parent.getChildren());
        sortPictogramElements(elements);
        int pos = 0;
        for (Shape shape : elements) {
            if (null != fp.getBusinessObjectForPictogramElement(shape)
                    && YangModelUtil.checkType(YangModelUtil.MODEL_PACKAGE.getContainingNode(),
                            fp.getBusinessObjectForPictogramElement(parent))
                    && ((ContainingNode) fp.getBusinessObjectForPictogramElement(parent)).getChildren()
                            .contains(fp.getBusinessObjectForPictogramElement(shape))) {
                if (y < shape.getGraphicsAlgorithm().getY()) {
                    return pos;
                }
                pos++;
            }
        }
        return pos;
    }

    public static void layoutPictogramElement(PictogramElement diagram, IFeatureProvider fp) {
        LayoutContext lc = new LayoutContext(diagram);

        fp.layoutIfPossible(lc);
    }

    public static Anchor getChopboxAnchor(AnchorContainer ac) {
        if (null != ac) {
            return Graphiti.getPeService().getChopboxAnchor(ac);
        }
        return null;
    }

    public static Anchor getBoxRelativeAnchor(AnchorContainer ac) {
        if (null != ac) {
            for (Anchor a : ac.getAnchors()) {
                if (a instanceof BoxRelativeAnchor) {
                    return a;
                }
            }
        }
        return null;
    }

    public static List<Shape> filterBusinessObjectShapes(List<Shape> shapes) {
        List<Shape> result = new ArrayList<Shape>();
        for (Shape shape : shapes) {
            if (PropertyUtil.isBusinessObjectShape(shape)) {
                result.add(shape);
            }
        }
        return result;
    }

    public static Shape getBusinessObjectPropShape(ContainerShape parent, String prop) {
        for (Shape shape : parent.getChildren()) {
            if (PropertyUtil.isObjectShapeProp(shape, prop)) {
                return shape;
            }
        }
        return null;
    }

    public static PictogramElement getBusinessObjectShape(IFeatureProvider fp, EObject obj) {
        PictogramElement[] pe = fp.getAllPictogramElementsForBusinessObject(obj);
        for (PictogramElement shape : pe) {
            if (shape instanceof Diagram || PropertyUtil.isBusinessObjectShape(shape)) {
                return shape;
            }
        }
        return null;
    }

    public static PictogramElement getBusinessObjectPropShape(IFeatureProvider fp, EObject obj, String prop) {
        PictogramElement[] pe = fp.getAllPictogramElementsForBusinessObject(obj);
        for (PictogramElement shape : pe) {
            if (PropertyUtil.isObjectShapeProp(shape, prop)) {
                return shape;
            }
        }
        return null;
    }

    public static PictogramElement getBusinessObjectPropShape(IFeatureProvider fp, EObject obj, EAttribute attr) {
        if (attributeShapes.containsKey(attr)) {
            return getBusinessObjectPropShape(fp, obj, attributeShapes.get(attr));
        }
        return null;
    }

    public static Polyline getPolyline(ContainerShape cs) {
        for (Shape shape : cs.getChildren()) {
            if (shape.getGraphicsAlgorithm() instanceof Polyline) {
                return (Polyline) shape.getGraphicsAlgorithm();
            }
        }
        return null;

    }

    public static GraphicsAlgorithm getObjectPropGA(ContainerShape cs, String prop) {
        Shape shape = getBusinessObjectPropShape(cs, prop);
        if (null != shape) {
            return shape.getGraphicsAlgorithm();
        }
        return null;

    }

    public static void deletePictogramElement(IFeatureProvider fp, PictogramElement pe) {

        RemoveContext removeContext = new RemoveContext(pe);
        IRemoveFeature f = fp.getRemoveFeature(removeContext);
        f.remove(removeContext);
    }

    public static void updatePictogramElement(IFeatureProvider fp, PictogramElement pe) {
        UpdateContext context = new UpdateContext(pe);
        fp.updateIfPossible(context);
    }

    public static boolean updateConnections(EObject obj, IFeatureProvider fp) {
        if (YangModelUtil.hasReference(obj)) {
            EObject ref = YangModelUtil.getReferencedObject(fp, obj);
            PictogramElement startPE = YangModelUIUtil.getBusinessObjectShape(fp, obj);
            Anchor start = YangModelUIUtil.getBoxRelativeAnchor((AnchorContainer) startPE);
            Connection con = null;
            if (null != start && null != start.getOutgoingConnections() && !start.getOutgoingConnections().isEmpty()) {
                con = start.getOutgoingConnections().get(0);
            }
            if (null == ref) {
                if (null != con) {
                    deletePictogramElement(fp, con);
                    // RemoveContext removeContext = new RemoveContext(con);
                    // fp.getRemoveFeature(removeContext).remove(removeContext);
                    return true;
                }
            } else {
                PictogramElement finishPE = YangModelUIUtil.getBusinessObjectShape(fp, ref);
                if (null != finishPE) {
                    Anchor finish = YangModelUIUtil.getChopboxAnchor((AnchorContainer) finishPE);
                    if (null != con && con.getEnd().equals(finish)) {
                        return false;
                    }
                    if (null != con) {
                        con.setEnd(finish);
                    } else {
                        YangModelUIUtil.drawConnection(obj, start, finish, fp);
                    }
                    LayoutUtil.layoutDiagramConnections(fp);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean updateConnections(EClass type, IFeatureProvider fp) {
        Object module = fp.getBusinessObjectForPictogramElement(fp.getDiagramTypeProvider().getDiagram());
        if (null != module && module instanceof EObject) {
            List<EObject> objects = YangModelUtil.filter(YangModelUtil.getAllBusinessObjects((EObject) module, null),
                    type);
            for (EObject obj : objects) {
                updateConnections(obj, fp);
            }
        }
        return false;
    }

    public static PictogramElement drawObject(EObject obj, ContainerShape cs, IFeatureProvider fp, int x, int y) {
        AddContext ac = new AddContext();
        ac.setTargetContainer(cs);
        ac.setLocation(x, y);
        ac.setNewObject(obj);
        return fp.addIfPossible(ac);
    }

    public static Connection drawConnection(EObject obj, Anchor start, Anchor finish, IFeatureProvider fp) {
        AddConnectionContext ac = new AddConnectionContext(start, finish);
        ac.setNewObject(obj);
        return (Connection) fp.addIfPossible(ac);
    }

    public static Connection drawPictogramConnectionElement(IAddConnectionContext context, IFeatureProvider fp) {
        return drawPictogramConnectionElement(context, fp, Strings.EMPTY_STRING);
    }

    public static Connection drawPictogramConnectionElement(IAddConnectionContext context, IFeatureProvider fp,
            String title) {
        EObject addedEReference = (EObject) context.getNewObject();
        IPeCreateService peCreateService = Graphiti.getPeCreateService();

        Connection connection = peCreateService.createFreeFormConnection(fp.getDiagramTypeProvider().getDiagram());
        connection.setStart(context.getSourceAnchor());
        connection.setEnd(context.getTargetAnchor());

        IGaService gaService = Graphiti.getGaService();
        Polyline polyline = gaService.createPlainPolyline(connection);
        polyline.setStyle(StyleUtil.getStyleForDomainObject(fp.getDiagramTypeProvider().getDiagram()));

        // add dynamic text decorator for the reference name

        ConnectionDecorator textDecorator = peCreateService.createConnectionDecorator(connection, false, 0.5, true);
        Text text = gaService.createPlainText(textDecorator);
        text.setStyle(StyleUtil.getStyleForTextDecorator(fp.getDiagramTypeProvider().getDiagram()));
        gaService.setLocation(text, 10, 0);

        // set reference name in the text decorator
        text.setValue(title);
        // add static graphical decorators (composition and inheritance)
        if (YangModelUtil.checkType(YangModelUtil.MODEL_PACKAGE.getIdentity(), addedEReference)) {
            createInheritanceConnectionArrow(connection, fp);
        } else {
            createConnectionArrow(connection, fp);
        }

        return connection;
    }

    public static Polyline createConnectionArrow(Connection connection, IFeatureProvider fp) {
        ConnectionDecorator cd = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 1.0, true);
        Polyline polyline = Graphiti.getGaCreateService().createPlainPolyline(cd, new int[] { -10, 5, 0, 0, -10, -5 });
        polyline.setStyle(StyleUtil.getStyleForDomainObject(fp.getDiagramTypeProvider().getDiagram()));
        return polyline;
    }

    public static Polyline createInheritanceConnectionArrow(Connection connection, IFeatureProvider fp) {
        ConnectionDecorator cd = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 1.0, true);
        Polyline polyline = Graphiti.getGaCreateService().createPlainPolygon(cd, new int[] { -10, 5, 0, 0, -10, -5 });
        polyline.setStyle(StyleUtil.getStyleForDomainObject(fp.getDiagramTypeProvider().getDiagram()));
        return polyline;
    }

    public static void drawBoxRelativeAnchor(ContainerShape containerShape, IAddContext context, IFeatureProvider fp) {
        final BoxRelativeAnchor boxAnchor = Graphiti.getPeCreateService().createBoxRelativeAnchor(containerShape);
        boxAnchor.setRelativeWidth(1.0);
        boxAnchor.setRelativeHeight(0.5);

        boxAnchor.setReferencedGraphicsAlgorithm(containerShape.getGraphicsAlgorithm());
        final Ellipse ellipse = Graphiti.getGaService().createPlainEllipse(boxAnchor);

        Graphiti.getGaService().setLocationAndSize(ellipse, -2 * DEFAULT_BOX_ANCHOR_RADIUS,
                -2 * DEFAULT_BOX_ANCHOR_RADIUS, 2 * DEFAULT_BOX_ANCHOR_RADIUS, 2 * DEFAULT_BOX_ANCHOR_RADIUS);
        ellipse.setStyle(StyleUtil.getStyleForDomainObject(fp.getDiagramTypeProvider().getDiagram()));
    }

    public static String getTypeText(TypedNode node) {
        if (null != node) {
            return getTypeText(node.getType());
        }
        return null;
    }

    public static String getTypeText(Typeref ref) {
        if (null != ref) {
            return " : " + ref.getName();
        }
        return null;
    }

    public static void drawPictogramElementHeader(ContainerShape containerShape, IAddContext context,
            IFeatureProvider fp, String imageId, String title, int width, int height) {
        final Shape imageShape = Graphiti.getPeCreateService().createShape(containerShape, false);
        // create and set text graphics algorithm
        final Image image = Graphiti.getGaService().createImage(imageShape, imageId);
        image.setHeight(height);
        image.setWidth(height);
        image.setStretchH(true);
        image.setStretchH(true);
        image.setProportional(true);
        Graphiti.getGaService().setLocationAndSize(image, DEFAULT_V_ALIGN, 0, height, height);
        PropertyUtil.setObjectShapeProp(imageShape, PropertyUtil.OBJECT_IMAGE_SHAPE_KEY, true);
        final Shape textShape = Graphiti.getPeCreateService().createShape(containerShape, false);
        Text text;
        if (YangModelUtil.checkType(YangModelUtil.MODEL_PACKAGE.getNamedNode(), context.getNewObject())) {
            if (null == ((NamedNode) context.getNewObject()).getName()) {
                ((NamedNode) context.getNewObject()).setName("name");
            }
            text = Graphiti.getGaService().createPlainText(textShape, ((NamedNode) context.getNewObject()).getName());
            fp.link(textShape,
                    new Object[] { context.getNewObject(), YangModelUtil.MODEL_PACKAGE.getNamedNode_Name() });
            PropertyUtil.setObjectShapeProp(textShape, PropertyUtil.EDITABLE_SHAPE, true);
        } else {
            String qName = YangModelUtil.getQNamePresentation((EObject) context.getNewObject());
            if (null == qName) {
                text = Graphiti.getGaService().createPlainText(textShape, title);
            } else {
                text = Graphiti.getGaService().createPlainText(textShape, qName);
                fp.link(textShape, context.getNewObject());
            }

        }
        text.setStyle(StyleUtil.getStyleForDomainObjectText(fp.getDiagramTypeProvider().getDiagram()));
        PropertyUtil.setObjectShapeProp(textShape, PropertyUtil.OBJECT_HEADER_TEXT_SHAPE_KEY, true);
        Graphiti.getGaService().setLocationAndSize(text, height + DEFAULT_V_ALIGN, 0, width, height);

        if (YangModelUtil.checkType(YangModelUtil.MODEL_PACKAGE.getTypedNode(), context.getNewObject())) {
            Shape typeShape = Graphiti.getPeCreateService().createShape(containerShape, false);
            Text type = Graphiti.getGaService().createPlainText(typeShape,
                    getTypeText((TypedNode) context.getNewObject()));
            fp.link(typeShape, context.getNewObject());
            type.setStyle(StyleUtil.getStyleForDomainObjectTypeText(fp.getDiagramTypeProvider().getDiagram()));
            PropertyUtil.setObjectShapeProp(typeShape, PropertyUtil.BUSINESS_OBJECT_TYPE_SHAPE_KEY, true);
            Graphiti.getGaService().setLocationAndSize(type, height + DEFAULT_V_ALIGN, 0, Math.max(0, width), height);
        }

    }

    public static void drawPictogramElementPositionInParent(ContainerShape shape, IFeatureProvider fp) {
        Integer pos = YangModelUtil.getPositionInParent(fp.getBusinessObjectForPictogramElement(shape.getContainer()),
                fp.getBusinessObjectForPictogramElement(shape));
        if (0 <= pos) {
            pos++;
            final Shape textShape = Graphiti.getPeCreateService().createShape(shape, false);
            Text text = Graphiti.getGaService().createPlainText(textShape, pos.toString());
            text.setStyle(StyleUtil.getStyleForDomainObjectNumberText(fp.getDiagramTypeProvider().getDiagram()));
            IDimension dim = GraphitiUi.getUiLayoutService().calculateTextSize(text.getValue(),
                    text.getStyle().getFont());
            Graphiti.getGaService().setLocationAndSize(text,
                    shape.getGraphicsAlgorithm().getWidth() - DEFAULT_OBJECT_NUMBER_IND, DEFAULT_OBJECT_NUMBER_IND,
                    dim.getWidth(), dim.getHeight());
            PropertyUtil.setObjectShapeProp(textShape, PropertyUtil.OBJECT_NUMBER_SHAPE_KEY, true);
        }
    }

    public static ContainerShape drawPictogramElement(IAddContext context, IFeatureProvider fp, String imageId,
            String title) {
        ContainerShape result = null;
        if (YangModelUtil.checkType(YangModelUtil.MODEL_PACKAGE.getContainingNode(), context.getNewObject())) {
            result = drawCompositePictogramElement(context, fp, imageId, title);
        } else {
            result = drawAttributePictogramElement(context, fp, imageId, title);
        }
        fp.link(result, context.getNewObject());
        ChopboxAnchor anchor = Graphiti.getPeCreateService().createChopboxAnchor(result);
        anchor.setReferencedGraphicsAlgorithm(result.getGraphicsAlgorithm());
        // create BoxRelativeAnchor for references
        if (context.getNewObject() instanceof EObject && YangModelUtil.hasReference((EObject) context.getNewObject())) {
            drawBoxRelativeAnchor(result, context, fp);
        }
        PropertyUtil.setObjectShapeProp(result, PropertyUtil.BUSINESS_OBJECT_SHAPE_KEY, true);

        // call the layout feature
        if (context.getTargetContainer() instanceof Diagram) {
            drawPictogramElementPositionInParent(result, fp);
        }

        // add feedback element
        final Shape feedbackShape = Graphiti.getPeCreateService().createShape(result, false);
        Graphiti.getGaService().createPlatformGraphicsAlgorithm(feedbackShape, "feedback");

        ContainerShape layoutShape = result;
        while (!(layoutShape.getContainer() instanceof Diagram)) {
            layoutShape = layoutShape.getContainer();
        }
        layoutPictogramElement(layoutShape, fp);
        return result;
    }

    public static ContainerShape drawCompositePictogramElement(IAddContext context, IFeatureProvider fp, String imageId,
            String title) {

        ContainerShape targetShape = context.getTargetContainer();
        IPeCreateService peCreateService = Graphiti.getPeCreateService();
        IGaService gaService = Graphiti.getGaService();

        ContainerShape containerShape = peCreateService.createContainerShape(targetShape, true);

        final int width = context.getWidth() <= 0 ? DEFAULT_WIDTH : context.getWidth();
        final int height = context.getHeight() <= 0 ? DEFAULT_COMPOSITE_HEIGHT : context.getHeight();

        RoundedRectangle roundedRectangle = gaService.createPlainRoundedRectangle(containerShape, 5, 5);
        roundedRectangle.setStyle(StyleUtil.getStyleForDomainObject(fp.getDiagramTypeProvider().getDiagram()));
        gaService.setLocationAndSize(roundedRectangle, context.getX(), context.getY(), width, height);

        // create shape for line
        final Shape shape = peCreateService.createShape(containerShape, false);

        // create and set graphics algorithm
        final Polyline polyline = gaService.createPlainPolyline(shape,
                new int[] { 0, DEFAULT_TEXT_HEIGHT, width, DEFAULT_TEXT_HEIGHT });
        polyline.setStyle(StyleUtil.getStyleForDomainObject(fp.getDiagramTypeProvider().getDiagram()));

        drawPictogramElementHeader(containerShape, context, fp, imageId, title, width, DEFAULT_TEXT_HEIGHT);
        return containerShape;

    }

    public static ContainerShape drawAttributePictogramElement(IAddContext context, IFeatureProvider fp, String imageId,
            String title) {
        ContainerShape targetShape = context.getTargetContainer();
        IPeCreateService peCreateService = Graphiti.getPeCreateService();
        IGaService gaService = Graphiti.getGaService();

        ContainerShape containerShape = peCreateService.createContainerShape(targetShape, true);

        final int width = context.getWidth() <= 0 ? DEFAULT_WIDTH : context.getWidth();
        final int height = context.getHeight() <= 0 ? DEFAULT_TEXT_HEIGHT : context.getHeight();

        GraphicsAlgorithm rectangle;
        if (context.getTargetContainer() instanceof Diagram) {
            rectangle = gaService.createPlainRoundedRectangle(containerShape, 5, 5);
        } else {
            rectangle = gaService.createInvisibleRectangle(containerShape);
        }
        rectangle.setStyle(StyleUtil.getStyleForDomainObject(fp.getDiagramTypeProvider().getDiagram()));
        gaService.setLocationAndSize(rectangle, context.getX(), context.getY(), width, height);
        drawPictogramElementHeader(containerShape, context, fp, imageId, title, width, DEFAULT_TEXT_HEIGHT);
        return containerShape;

    }
}
