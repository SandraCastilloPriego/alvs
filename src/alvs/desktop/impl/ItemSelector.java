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

import alvs.data.BugDataset;

import alvs.data.impl.SimpleParameterSet;
import alvs.desktop.Desktop;
import alvs.main.ALVSCore;
import alvs.modules.file.saveOtherFile.SaveOtherFile;
import alvs.util.GUIUtils;
import alvs.util.components.DragOrderedJList;
import alvs.util.dialogs.ExitCode;
import alvs.util.dialogs.ParameterSetupDialog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class implements a selector of data sets
 *
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
public class ItemSelector extends JPanel implements ActionListener,
        MouseListener, ListSelectionListener {

        public static final String DATA_FILES_LABEL = "Dataset Files";
        private DragOrderedJList DatasetFiles;
        private List<BugDataset> DatasetFilesModel = new ArrayList<BugDataset>();
        private DefaultListModel DatasetNamesModel = new DefaultListModel();
        private JPopupMenu dataFilePopupMenu;
        private int copies = 0;
        private NameChangeParameter parameterName;

        /**
         * Constructor
         */
        public ItemSelector(Desktop desktop) {


                // Create panel for raw data objects
                JPanel rawDataPanel = new JPanel();
                JLabel rawDataTitle = new JLabel(DATA_FILES_LABEL);

                DatasetFiles = new DragOrderedJList(DatasetNamesModel);
                DatasetFiles.setCellRenderer(new ItemSelectorListRenderer());
                DatasetFiles.addMouseListener(this);
                DatasetFiles.addListSelectionListener(this);
                JScrollPane rawDataScroll = new JScrollPane(DatasetFiles);

                rawDataPanel.setLayout(new BorderLayout());
                rawDataPanel.add(rawDataTitle, BorderLayout.NORTH);
                rawDataPanel.add(rawDataScroll, BorderLayout.CENTER);
                rawDataPanel.setMinimumSize(new Dimension(150, 10));



                // Add panels to a split and put split on the main panel
                setPreferredSize(new Dimension(200, 10));
                setLayout(new BorderLayout());
                add(rawDataPanel, BorderLayout.CENTER);

                dataFilePopupMenu = new JPopupMenu();
                GUIUtils.addMenuItem(dataFilePopupMenu, "Change Name", this, "CHANGE_NAME");
                GUIUtils.addMenuItem(dataFilePopupMenu, "Show Dataset", this, "SHOW_DATASET");
                GUIUtils.addMenuItem(dataFilePopupMenu, "Add Comments", this, "ADD_COMMENT");
                GUIUtils.addMenuItem(dataFilePopupMenu, "Save Dataset in a File", this, "SAVE_DATASET");
                GUIUtils.addMenuItem(dataFilePopupMenu, "Remove", this, "REMOVE_FILE");

                this.parameterName = new NameChangeParameter();

        }

        void addSelectionListener(ListSelectionListener listener) {
                DatasetFiles.addListSelectionListener(listener);
        }

        public ExitCode setupParameters() {
                try {
                        ParameterSetupDialog nameDialog = new ParameterSetupDialog("Change Name", parameterName);
                        nameDialog.setVisible(true);
                        return nameDialog.getExitCode();
                } catch (Exception exception) {
                        return ExitCode.CANCEL;
                }
        }

        public void setupInfoDialog(BugDataset data) {
                try {
                        InfoDataIF dialog = new InfoDataIF();
                        dialog.setData(data);
                        ALVSCore.getDesktop().getDesktopPane().add(dialog);
                        ALVSCore.getDesktop().getDesktopPane().validate();
                        dialog.setVisible(true);
                } catch (Exception exception) {
                }
        }

        // Implementation of action listener interface
        public void actionPerformed(ActionEvent e) {
                Runtime.getRuntime().freeMemory();
                String command = e.getActionCommand();
                Boolean changeName = false;

                if (command.equals("CHANGE_NAME") || changeName) {
                        try {
                                BugDataset[] selectedFiles = this.getSelectedDatasets();
                                this.parameterName.setParameterValue(NameChangeParameter.name, selectedFiles[0].getDatasetName());
                                ExitCode code = this.setupParameters();
                                changeName = true;
                                if (code != ExitCode.OK) {
                                        return;
                                }


                                int index = DatasetNamesModel.indexOf(selectedFiles[0].getDatasetName());
                                selectedFiles[0].setDatasetName((String) parameterName.getParameterValue(NameChangeParameter.name));
                                DatasetNamesModel.setElementAt(selectedFiles[0].getDatasetName(), index);
                        } catch (Exception exception) {
                        }
                        changeName = false;
                }

                if (command.equals("ADD_COMMENT")) {
                        BugDataset[] selectedFiles = this.getSelectedDatasets();
                        try {
                                for (BugDataset data : selectedFiles) {
                                        this.setupInfoDialog(data);
                                }
                        } catch (Exception exception) {
                                return;
                        }
                }

                if (command.equals("REMOVE_FILE")) {
                        removeData();
                }

                if (command.equals("SHOW_DATASET")) {
                        showData();
                }

                if (command.equals("SAVE_DATASET")) {
                        BugDataset[] selectedFiles = getSelectedDatasets();
                        SaveOtherFile save = new SaveOtherFile(selectedFiles);
                        save.setParameters(((DesktopParameters) ALVSCore.getDesktop().getParameterSet()).getSaveOtherParameters());
                        save.initModule();
                        ((DesktopParameters) ALVSCore.getDesktop().getParameterSet()).setSaveOtherParameters((SimpleParameterSet) save.getParameterSet());

                }


        }

        private void showData() {
                BugDataset[] selectedFiles = getSelectedDatasets();
                for (BugDataset file : selectedFiles) {
                        if (file != null) {
                                GUIUtils.showNewTable(file, false);
                        }
                }
        }

        private void removeData() {
                BugDataset[] selectedFiles = getSelectedDatasets();

                for (BugDataset file : selectedFiles) {
                        if (file != null) {
                                DatasetFilesModel.remove(file);
                                DatasetNamesModel.removeElement(file.getDatasetName());
                        }
                }
        }

        public void removeData(BugDataset file) {
                if (file != null) {
                        DatasetFilesModel.remove(file);
                        DatasetNamesModel.removeElement(file.getDatasetName());
                }

        }

        /**
         * Returns selected raw data objects in an array
         */
        public BugDataset[] getSelectedDatasets() {

                Object o[] = DatasetFiles.getSelectedValues();

                BugDataset res[] = new BugDataset[o.length];

                for (int i = 0; i < o.length; i++) {
                        for (BugDataset dataset : DatasetFilesModel) {
                                if (dataset.getDatasetName().compareTo((String) o[i]) == 0) {
                                        res[i] = dataset;
                                }
                        }
                }

                return res;

        }

        public void mouseClicked(MouseEvent e) {

                if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1)) {
                        showData();
                }

        }

        public void mouseEntered(MouseEvent e) {
                // ignore
        }

        public void mouseExited(MouseEvent e) {
                // ignore
        }

        public void mousePressed(MouseEvent e) {

                if (e.isPopupTrigger()) {
                        if (e.getSource() == DatasetFiles) {
                                dataFilePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                }

        }

        public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                        if (e.getSource() == DatasetFiles) {
                                dataFilePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                }
        }

        public void valueChanged(ListSelectionEvent event) {

                Object src = event.getSource();

                // Update the highlighting of peak list list in case raw data list
                // selection has changed and vice versa.
                if (src == DatasetFiles) {
                        DatasetFiles.revalidate();
                }

        }

        public void addNewFile(BugDataset dataset) {
                for (int i = 0; i < DatasetNamesModel.getSize(); i++) {
                        if (dataset.getDatasetName().matches(DatasetNamesModel.getElementAt(i).toString())) {
                                dataset.setDatasetName(dataset.getDatasetName() + "_" + ++copies);
                        }
                }
                this.DatasetFilesModel.add(dataset);
                DatasetNamesModel.addElement(dataset.getDatasetName());
                this.DatasetFiles.revalidate();
        }
}
