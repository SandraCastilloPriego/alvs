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
package alvs.desktop.impl;

import alvs.data.StorableParameterSet;
import alvs.data.impl.SimpleParameterSet;
import alvs.main.ALVSCore;
import alvs.modules.file.saveOtherFile.SaveOtherParameters;
import alvs.util.NumberFormatter;
import alvs.util.NumberFormatter.FormatterType;
import alvs.util.Range;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Hashtable;
import java.util.Iterator;



import java.util.Set;
import org.dom4j.Element;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
public class DesktopParameters implements StorableParameterSet,
        ComponentListener {

        public static final String FORMAT_ELEMENT_NAME = "format";
        public static final String FORMAT_TYPE_ATTRIBUTE_NAME = "type";       
        public static final String MAINWINDOW_ELEMENT_NAME = "mainwindow";
        public static final String X_ELEMENT_NAME = "x";
        public static final String Y_ELEMENT_NAME = "y";
        public static final String WIDTH_ELEMENT_NAME = "width";
        public static final String HEIGHT_ELEMENT_NAME = "height";
        public static final String LASTPATH_ELEMENT_NAME = "lastdirectory";
        public static final String LAST_PROJECT_PATH_ELEMENT_NAME = "lastProjectDirectory";
        public static final String LAST_SAVE_PATH_ELEMENT_NAME = "lastSaveDirectory";
        public static final String NORMALIZATION_PATH_ELEMENT_NAME = "normalizationDirectory";
        public static final int MAXIMIZED = -1;
        private int mainWindowX, mainWindowY, mainWindowWidth, mainWindowHeight;
        private String lastOpenPath = "";
        private String lastOpenProjectPath = "";
        private String lastSavePath = "";
        private SimpleParameterSet SaveOtherParameters;
        private SimpleParameterSet ConfigurationParameters;

        DesktopParameters() {
                SaveOtherParameters = new SaveOtherParameters();
                MainWindow mainWindow = (MainWindow) ALVSCore.getDesktop();
                mainWindow.addComponentListener(this);
        }
   

        public SimpleParameterSet getSaveOtherParameters() {
                return SaveOtherParameters;
        }

        public void setSaveOtherParameters(SimpleParameterSet SaveDatasetParameters) {
                this.SaveOtherParameters = SaveDatasetParameters;
        }

        public SimpleParameterSet getSaveConfigurationParameters() {
                return ConfigurationParameters;
        }

        public void setSaveConfigurationParameters(SimpleParameterSet ConfigurationParameters) {
                this.ConfigurationParameters = ConfigurationParameters;
        }
       
        /**
         * @return Returns the mainWindowHeight.
         */
        int getMainWindowHeight() {
                return mainWindowHeight;
        }

        /**
         * @param mainWindowHeight
         *            The mainWindowHeight to set.
         */
        void setMainWindowHeight(int mainWindowHeight) {
                this.mainWindowHeight = mainWindowHeight;
        }

        /**
         * @return Returns the mainWindowWidth.
         */
        int getMainWindowWidth() {
                return mainWindowWidth;
        }

        /**
         * @param mainWindowWidth
         *            The mainWindowWidth to set.
         */
        void setMainWindowWidth(int mainWindowWidth) {
                this.mainWindowWidth = mainWindowWidth;
        }

        /**
         * @return Returns the mainWindowX.
         */
        int getMainWindowX() {
                return mainWindowX;
        }

        /**
         * @param mainWindowX
         *            The mainWindowX to set.
         */
        void setMainWindowX(int mainWindowX) {
                this.mainWindowX = mainWindowX;
        }

        /**
         * @return Returns the mainWindowY.
         */
        int getMainWindowY() {
                return mainWindowY;
        }

        /**
         * @param mainWindowY
         *            The mainWindowY to set.
         */
        void setMainWindowY(int mainWindowY) {
                this.mainWindowY = mainWindowY;
        }

        /**
         * @return Returns the lastOpenPath.
         */
        public String getLastOpenPath() {
                return lastOpenPath;
        }

        /**
         * @param lastOpenProjectPath
         *            The lastOpenProjectPath to set.
         */
        public void setLastOpenProjectPath(String lastOpenPath) {
                this.lastOpenProjectPath = lastOpenPath;
        }

        /**
         * @return Returns the lastOpenPath.
         */
        public String getLastOpenProjectPath() {
                return lastOpenProjectPath;
        }

        /**
         * @param lastOpenPath
         *            The lastOpenPath to set.
         */
        public void setLastOpenPath(String lastOpenPath) {
                this.lastOpenPath = lastOpenPath;
        }

        /**
         * @return Returns the lastSavePath.
         */
        public String getLastSavePath() {
                return lastSavePath;
        }

       
      
        /**
         * @param lastSavePath
         *            The lastSavePath to set.
         */
        public void setLastSavePath(String lastSavePath) {
                this.lastSavePath = lastSavePath;
        }

        public void exportValuesToXML(Element element) {             

                Element mainWindowElement = element.addElement(MAINWINDOW_ELEMENT_NAME);
                mainWindowElement.addElement(X_ELEMENT_NAME).setText(
                        String.valueOf(mainWindowX));
                mainWindowElement.addElement(Y_ELEMENT_NAME).setText(
                        String.valueOf(mainWindowY));
                mainWindowElement.addElement(WIDTH_ELEMENT_NAME).setText(
                        String.valueOf(mainWindowWidth));
                mainWindowElement.addElement(HEIGHT_ELEMENT_NAME).setText(
                        String.valueOf(mainWindowHeight));

                element.addElement(LASTPATH_ELEMENT_NAME).setText(lastOpenPath);
                element.addElement(LAST_PROJECT_PATH_ELEMENT_NAME).setText(
                        lastOpenProjectPath);
                element.addElement(LAST_SAVE_PATH_ELEMENT_NAME).setText(
                        lastSavePath);           
              

                SaveOtherParameters.exportValuesToXML(element);

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

                lastOpenPath = element.elementText(LASTPATH_ELEMENT_NAME);
                lastOpenProjectPath = element.elementText(LAST_PROJECT_PATH_ELEMENT_NAME);
                lastSavePath = element.elementText(LAST_SAVE_PATH_ELEMENT_NAME);
                
                SaveOtherParameters.importValuesFromXML(element);

               
        }

        public DesktopParameters clone() {
                return new DesktopParameters();
        }

        /**
         * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
         */
        public void componentHidden(ComponentEvent arg0) {
        }

        /**
         * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
         */
        public void componentMoved(ComponentEvent arg0) {
                MainWindow mainWindow = (MainWindow) ALVSCore.getDesktop();
                Point location = mainWindow.getLocation();
                mainWindowX = location.x;
                mainWindowY = location.y;
        }

        /**
         * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
         */
        public void componentResized(ComponentEvent arg0) {
                MainWindow mainWindow = (MainWindow) ALVSCore.getDesktop();
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
        }

        /**
         * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
         */
        public void componentShown(ComponentEvent arg0) {
        }
}
