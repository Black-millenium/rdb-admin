/*
 * SortArrowIcon.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import workbench.gui.renderer.ColorUtils;
import workbench.resource.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * @author Thomas Kellerer
 */
public class SortArrowIcon
    implements Icon {
  private static final HashMap<Integer, SortArrowIcon> sharedUpArrows = new HashMap<Integer, SortArrowIcon>();
  private static final HashMap<Integer, SortArrowIcon> sharedDownArrows = new HashMap<Integer, SortArrowIcon>();
  private final Direction direction;
  private final int width;
  private final int height;
  private final int blendValue;
  private SortArrowIcon(Direction dir, int size) {
    direction = dir;
    width = (int) (size * 1.1);
    height = size;
    blendValue = Settings.getInstance().getIntProperty("workbench.gui.sorticon.blend", 128);
  }

  public static synchronized SortArrowIcon getIcon(Direction dir, int size) {
    HashMap<Integer, SortArrowIcon> cache = (dir == Direction.UP ? sharedUpArrows : sharedDownArrows);

    Integer key = Integer.valueOf(size);
    SortArrowIcon icon = cache.get(key);
    if (icon == null) {
      icon = new SortArrowIcon(dir, size);
      cache.put(key, icon);
    }
    return icon;
  }

  @Override
  public int getIconWidth() {
    return width;
  }

  @Override
  public int getIconHeight() {
    return height;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Color bg = c.getBackground();
    Color fg = c.getForeground();

    Color arrowColor = ColorUtils.blend(bg, Color.BLACK, blendValue);
    int w = width;
    int h = height;
    int top = y + h;
    int bottom = y;

    int[] xPoints = new int[]{x, x + w / 2, x + w}; // left, middle, right
    int[] yPoints;

    if (direction == Direction.UP) {
      yPoints = new int[]{top - 1, bottom - 1, top - 1};
    } else {
      yPoints = new int[]{bottom, top, bottom};
    }

    g.setColor(arrowColor);
    g.fillPolygon(xPoints, yPoints, 3);
    g.setColor(fg);
  }

  public enum Direction {
    UP,
    DOWN;
  }
}

