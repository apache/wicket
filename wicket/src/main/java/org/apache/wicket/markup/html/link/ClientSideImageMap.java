/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.link;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.IClusterable;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A client-side image map implementation which allows you to "attach" the map to any existing {@link Image} component.
 *
 * @since 1.5
 */
public class ClientSideImageMap extends Panel implements IMarkupResourceStreamProvider, IMarkupCacheKeyProvider
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static final long serialVersionUID = 1L;
    private static final String CIRCLE = "circle";
    private static final String POLYGON = "polygon";
    private static final String RECTANGLE = "rect";
    private final List<Area> areas = new LinkedList<Area>();

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    /**
     * Constructs a client-side image map which is "attached" to the given {@link Image} component.
     * @param id the component id
     * @param image the image component
     */
    public ClientSideImageMap(String id, Image image)
    {
        super(id);
        setOutputMarkupId(true);
        add(new AttributeModifier("name", true, new PropertyModel<String>(this, "markupId")));
        image.add(new AttributeModifier("usemap", true, new UsemapModel()));
    }

//**********************************************************************************************************************
// IMarkupCacheKeyProvider Implementation
//**********************************************************************************************************************

    public String getCacheKey(MarkupContainer markupContainer, Class<?> aClass)
    {
        // don't cache the evaluated template
        return null;
    }

//**********************************************************************************************************************
// IMarkupResourceStreamProvider Implementation
//**********************************************************************************************************************

    public IResourceStream getMarkupResourceStream(MarkupContainer markupContainer, Class<?> c)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("\n<wicket:panel>\n");
        for (Area area : areas)
        {
            builder.append(area.toString()).append("\n");
        }
        builder.append("</wicket:panel>");
        return new StringResourceStream(builder.toString());
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    private String circleCoordinates(int x, int y, int radius)
    {
        return x + "," + y + "," + radius;
    }

    private String polygonCoordinates(int... coordinates)
    {
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < coordinates.length; i++)
        {
            buffer.append(coordinates[i]);

            if (i < (coordinates.length - 1))
            {
                buffer.append(',');
            }
        }
        return buffer.toString();
    }

    private String rectangleCoordinates(int x1, int y1, int x2, int y2)
    {
        return x1 + "," + y1 + "," + x2 + "," + y2;
    }

    private String shapeCoordinates(Shape shape)
    {
        final StringBuilder sb = new StringBuilder();
        final PathIterator pi = shape.getPathIterator(null, 1.0);
        final float[] coords = new float[6];
        final float[] lastMove = new float[2];
        while (!pi.isDone())
        {
            switch (pi.currentSegment(coords))
            {
                case PathIterator.SEG_MOVETO:
                    if (sb.length() != 0)
                    {
                        sb.append(",");
                    }
                    sb.append(Math.round(coords[0]));
                    sb.append(",");
                    sb.append(Math.round(coords[1]));
                    lastMove[0] = coords[0];
                    lastMove[1] = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    if (sb.length() != 0)
                    {
                        sb.append(",");
                    }
                    sb.append(Math.round(coords[0]));
                    sb.append(",");
                    sb.append(Math.round(coords[1]));
                    break;
                case PathIterator.SEG_CLOSE:
                    if (sb.length() != 0)
                    {
                        sb.append(",");
                    }
                    sb.append(Math.round(lastMove[0]));
                    sb.append(",");
                    sb.append(Math.round(lastMove[1]));
                    break;
            }
            pi.next();
        }
        return sb.toString();
    }

    /**
     * Adds a circle-shaped area centered at (x,y) with radius r.
     *
     * @param link   the link
     * @param x      x coordinate of the center of the circle
     * @param y      y coordinate of center
     * @param radius the radius
     * @return this
     */
    public ClientSideImageMap addCircleArea(AbstractLink link, int x, int y, int radius)
    {
        add(link);
        areas.add(new Area(link, circleCoordinates(x, y, radius), CIRCLE));
        return this;
    }

    /**
     * Adds a polygon-shaped area defined by coordinates.
     *
     * @param link        the link
     * @param coordinates the coordinates for the polygon
     * @return This
     */
    public ClientSideImageMap addPolygonArea(AbstractLink link, int... coordinates)
    {
        add(link);
        areas.add(new Area(link, polygonCoordinates(coordinates), POLYGON));
        return this;
    }

    /**
     * Adds a rectangular-shaped area.
     *
     * @param link the link
     * @param x1   top left x
     * @param y1   top left y
     * @param x2   bottom right x
     * @param y2   bottom right y
     * @return this
     */
    public ClientSideImageMap addRectangleArea(AbstractLink link, int x1, int y1, int x2, int y2)
    {
        add(link);
        areas.add(new Area(link, rectangleCoordinates(x1, y1, x2, y2), RECTANGLE));
        return this;
    }

    /**
     * Adds an area defined by a shape object.
     *
     * @param link  the link
     * @param shape the shape
     * @return this
     */
    public ClientSideImageMap addShapeArea(AbstractLink link, Shape shape)
    {
        add(link);
        areas.add(new Area(link, shapeCoordinates(shape), POLYGON));
        return this;
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    /**
     * Encapsulates the concept of an <area> within a <map>.
     */
    private static class Area implements IClusterable
    {
        private static final long serialVersionUID = 1L;

        private final AbstractLink link;
        private final String coordinates;
        private final String type;

        protected Area(final AbstractLink link, final String coordinates, final String type)
        {
            this.link = link;
            this.coordinates = coordinates;
            this.type = type;
        }

        @Override
        public String toString()
        {
            return "<area shape=\"" + type + "\"" + " coords=\""
                    + coordinates + "\"" +
                    " wicket:id=\"" + link.getId() + "\" />";
        }
    }

    private class UsemapModel extends Model<String>
    {
        private static final long serialVersionUID = 1L;

        public String getObject()
        {
            return "#" + getMarkupId();
        }
    }
}
