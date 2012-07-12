/*
 * Copyright 2010 - 2012 
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

package alvs.desktop.impl.helpsystem;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/ *
 */
public class HelpImpl {

    private ALVSHelpSet hs;

    public HelpImpl() {
        try {

            // Construct help
            boolean test = false;

            File file = new File(System.getProperty("user.dir")
                    + File.separator + "ALVS.jar");

            if (!file.exists()) {
                file = new File(System.getProperty("user.dir") + File.separator
                        + "dist" + File.separator + "ALVS.jar");
                test = true;
            }

            if (!file.exists()) {
                return;
            }

            ALVSHelpMap helpMap = new ALVSHelpMap(test);

            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                String name = entry.getName();
                if ((name.endsWith("htm")) || (name.endsWith("html"))) {
                    helpMap.setTarget(name);
                }
            }

            helpMap.setTargetImage("topic.png");

            hs = new ALVSHelpSet();
            hs.setLocalMap(helpMap);

            ALVSTOCView myTOC = new ALVSTOCView(hs, "TOC",
                    "Table Of Contents", helpMap, file);

            hs.setTitle("ALVS");
            hs.addTOCView(myTOC);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ALVSHelpSet getHelpSet() {
        return hs;
    }

}
