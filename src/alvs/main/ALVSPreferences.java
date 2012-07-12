/*
 * Copyright 2010 - 2012 VTT Biotechnology
 * This file is part of ALVS.
 *
 * ALVS is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * ALVS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ALVS; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package alvs.main;

import alvs.data.StorableParameterSet;
import alvs.desktop.impl.MainWindow;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;


import org.dom4j.Element;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
public class ALVSPreferences implements StorableParameterSet {

    public static final String FORMAT_ELEMENT_NAME = "format";
    public static final String FORMAT_TYPE_ATTRIBUTE_NAME = "type";   
    public static final String FORMAT_TYPE_ATTRIBUTE_INT = "Intensity";
    public static final String MAINWINDOW_ELEMENT_NAME = "mainwindow";
    public static final String X_ELEMENT_NAME = "x";
    public static final String Y_ELEMENT_NAME = "y";
    public static final String WIDTH_ELEMENT_NAME = "width";
    public static final String HEIGHT_ELEMENT_NAME = "height";
    public static final String THREADS_ELEMENT_NAME = "threads";
    public static final int MAXIMIZED = -1;  
    private int mainWindowX, mainWindowY, mainWindowWidth, mainWindowHeight;
    private boolean autoNumberOfThreads = true;
    private int manualNumberOfThreads = 2;

    public ALVSPreferences() {
    }
  


    public void exportValuesToXML(Element element) {

        MainWindow mainWindow = (MainWindow) ALVSCore.getDesktop();
        Point location = mainWindow.getLocation();
        mainWindowX = location.x;
        mainWindowY = location.y;
        int state = mainWindow.getExtendedState();
        Dimension size = mainWindow.getSize();
        if ((state & Frame.MAXIMIZED_HORIZ) != 0) {
            mainWindowWidth = MAXIMIZED;
        } else {
            mainWindowWidth = size.width;
        }
        if ((state & Frame.MAXIMIZED_VERT) != 0) {
            mainWindowHeight = MAXIMIZED;
        } else {
            mainWindowHeight = size.height;
        }       

        Element mainWindowElement = element.addElement(MAINWINDOW_ELEMENT_NAME);
        mainWindowElement.addElement(X_ELEMENT_NAME).setText(
                String.valueOf(mainWindowX));
        mainWindowElement.addElement(Y_ELEMENT_NAME).setText(
                String.valueOf(mainWindowY));
        mainWindowElement.addElement(WIDTH_ELEMENT_NAME).setText(
                String.valueOf(mainWindowWidth));
        mainWindowElement.addElement(HEIGHT_ELEMENT_NAME).setText(
                String.valueOf(mainWindowHeight));

        Element threadsElement = element.addElement(THREADS_ELEMENT_NAME);
        if (autoNumberOfThreads) {
            threadsElement.addAttribute("auto", "true");
        }
        threadsElement.setText(String.valueOf(manualNumberOfThreads));        

    }


    public void importValuesFromXML(Element element) {        
        Element mainWindowElement = element.element(MAINWINDOW_ELEMENT_NAME);
        if (mainWindowElement != null) {
            mainWindowX = Integer.parseInt(mainWindowElement.elementText(X_ELEMENT_NAME));
            mainWindowY = Integer.parseInt(mainWindowElement.elementText(Y_ELEMENT_NAME));
            mainWindowWidth = Integer.parseInt(mainWindowElement.elementText(WIDTH_ELEMENT_NAME));
            mainWindowHeight = Integer.parseInt(mainWindowElement.elementText(HEIGHT_ELEMENT_NAME));
        }

        MainWindow mainWindow = (MainWindow) ALVSCore.getDesktop();
        if (mainWindowX > 0) {
            mainWindow.setLocation(mainWindowX, mainWindowY);
        }

        if ((mainWindowWidth > 0) || (mainWindowHeight > 0)) {
            mainWindow.setSize(mainWindowWidth, mainWindowHeight);
        }

        int newState = Frame.NORMAL;
        if (mainWindowWidth == MAXIMIZED) {
            newState |= Frame.MAXIMIZED_HORIZ;
        }

        if (mainWindowHeight == MAXIMIZED) {
            newState |= Frame.MAXIMIZED_VERT;
        }

        mainWindow.setExtendedState(newState);

        Element threadsElement = element.element(THREADS_ELEMENT_NAME);
        if (threadsElement != null) {
            autoNumberOfThreads = (threadsElement.attributeValue("auto") != null);
            manualNumberOfThreads = Integer.parseInt(threadsElement.getText());
        }      

    }

    public ALVSPreferences clone() {
        return new ALVSPreferences();
    }

    public boolean isAutoNumberOfThreads() {
        return autoNumberOfThreads;
    }

    public void setAutoNumberOfThreads(boolean autoNumberOfThreads) {
        this.autoNumberOfThreads = autoNumberOfThreads;
    }

    public int getManualNumberOfThreads() {
        return manualNumberOfThreads;
    }

    public void setManualNumberOfThreads(int manualNumberOfThreads) {
        this.manualNumberOfThreads = manualNumberOfThreads;
    }
   }