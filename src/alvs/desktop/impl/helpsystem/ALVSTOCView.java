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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.jar.JarFile;

import javax.help.BadIDException;
import javax.help.HelpSet;
import javax.help.Map;
import javax.help.TOCItem;
import javax.help.TOCView;
import javax.help.TreeItem;
import javax.help.Map.ID;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 */
public class ALVSTOCView extends TOCView {

        private ALVSHelpMap hm;
        private HelpSet hs;
        private File file;

        public ALVSTOCView(HelpSet hs, String name, String label, ALVSHelpMap hm, File file) {
                super(hs, name, label, null);
                this.hm = hm;
                this.hs = hs;
                this.file = file;
        }

        /**
         * Public method that gets a DefaultMutableTreeNode representing the
         * information in this view instance.
         */
        @Override
        public DefaultMutableTreeNode getDataAsTree() {

                try {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode();

                        TreeSet<TOCItem> sortedItems = new TreeSet<TOCItem>(new TOCItemSorterByName());

                        List<String> list = Collections.list(hm.getAllIDs());
                        Collections.sort(list);
                        Iterator<String> e = list.iterator();

                        while (e.hasNext()) {
                                String target = (String) e.next();
                                if (target.contains(".png")) {
                                        continue;
                                }
                                sortedItems.add((TOCItem) createMyItem(target));
                                System.out.print(target + "\n");
                        }

                        Iterator<TOCItem> i = sortedItems.iterator();

                        while (i.hasNext()) {
                                TOCItem item = i.next();
                                DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(item);
                                node.add(newChild);
                        }

                        return node;

                } catch (Exception ex) {
                        throw new Error("Trouble creating TOC data progamatically; " + ex);
                }

        }

        /**
         * Create an TOCItem with the given data.
         *
         * @param tagName
         *            The TOC type to create. Valid types are "tocitem". Null or
         *            invalid types will throw an IllegalArgumentException
         * @param atts
         *            Attributes of the Item. Valid attributes are "target",
         *            "image", and "text". A null atts is valid and means no
         *            attributes
         * @param hs
         *            HelpSet this item was created under.
         * @param locale
         *            Locale of this item. A null locale is valid.
         * @returns A fully constructed TreeItem.
         * @throws IllegalArgumentExcetpion
         *             if tagname is null or invalid.
         */
        public TreeItem createMyItem(String target) {

                String line, title = "Test";
                try {
                        JarFile jarFile = new JarFile(file);
                        InputStream test = jarFile.getInputStream(jarFile.getEntry(target));
                        BufferedReader in = new BufferedReader(new InputStreamReader(test));

                        if (!in.ready()) {
                                throw new IOException();
                        }

                        while ((line = in.readLine()) != null) {
                                if (line.toLowerCase().contains("title")) {
                                        int beginIndex = line.toLowerCase().indexOf("title") + 6;
                                        int endIndex = line.toLowerCase().indexOf("</title>");
                                        title = line.substring(beginIndex, endIndex);
                                        break;
                                }
                        }

                        in.close();
                } catch (IOException e) {
                }

                Map.ID mapID = null;
                try {
                        mapID = ID.create(target, hs);
                } catch (BadIDException bex1) {
                }

                Map.ID imageMapID = null;
                String imageID = "topic.png";
                try {
                        imageMapID = ID.create(imageID, hs);
                } catch (BadIDException bex2) {
                }

                TOCItem item = new TOCItem(mapID, imageMapID, hs, Locale.getDefault());
                item.setName(title);
                item.setMergeType("javax.help.AppendMerge");
                item.setExpansionType(TreeItem.COLLAPSE);

                return item;
        }

        /**
         * Creates a default TOCItem.
         */
        public TreeItem createItem() {
                return new TOCItem();
        }
}
