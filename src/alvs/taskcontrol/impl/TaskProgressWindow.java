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
package alvs.taskcontrol.impl;

import alvs.main.ALVSCore;
import alvs.taskcontrol.Task;
import alvs.taskcontrol.TaskPriority;
import alvs.taskcontrol.TaskStatus;
import alvs.util.GUIUtils;
import alvs.util.components.ComponentCellRenderer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 * This class represents a window with a table of running tasks
 */
public class TaskProgressWindow extends JInternalFrame implements
        ActionListener {

        private JTable taskTable;
        private JPopupMenu popupMenu;
        private JMenu priorityMenu;
        private JMenuItem cancelTaskMenuItem, highPriorityMenuItem, normalPriorityMenuItem;

        /**
         * Constructor
         */
        public TaskProgressWindow() {

                super("Tasks in progress...", true, true, true, true);
                try {
                        // We don't want this window to be closed until all tasks are finished
                        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

                        TaskControllerImpl taskController = (TaskControllerImpl) ALVSCore.getTaskController();

                        taskTable = new JTable(taskController.getTaskQueue());
                        taskTable.setCellSelectionEnabled(false);
                        taskTable.setColumnSelectionAllowed(false);
                        taskTable.setRowSelectionAllowed(true);
                        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        taskTable.setDefaultRenderer(JComponent.class,
                                new ComponentCellRenderer());

                        JScrollPane jJobScroll = new JScrollPane(taskTable);
                        add(jJobScroll, BorderLayout.CENTER);

                        // Create popup menu and items
                        popupMenu = new JPopupMenu();

                        priorityMenu = new JMenu("Set priority...");
                        highPriorityMenuItem = GUIUtils.addMenuItem(priorityMenu, "High", this);
                        normalPriorityMenuItem = GUIUtils.addMenuItem(priorityMenu, "Normal",
                                this);
                        popupMenu.add(priorityMenu);

                        cancelTaskMenuItem = GUIUtils.addMenuItem(popupMenu, "Cancel task",
                                this);

                        // Addd popup menu to the task table
                        taskTable.setComponentPopupMenu(popupMenu);

                        // Set the width for first column (task description)
                        taskTable.getColumnModel().getColumn(0).setPreferredWidth(350);

                        pack();

                        // Set position and size
                        setBounds(20, 20, 600, 150);
                } catch (Exception e) {
                        e.printStackTrace();
                }

        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent event) {
                try {

                        TaskControllerImpl taskController = (TaskControllerImpl) ALVSCore.getTaskController();

                        WrappedTask currentQueue[] = taskController.getTaskQueue().getQueueSnapshot();

                        int selectedRow = taskTable.getSelectedRow();

                        if (selectedRow >= currentQueue.length) {
                                return;
                        }

                        Task selectedTask = currentQueue[selectedRow].getActualTask();

                        Object src = event.getSource();

                        if (src == cancelTaskMenuItem) {
                                TaskStatus status = selectedTask.getStatus();
                                if ((status == TaskStatus.WAITING) || (status == TaskStatus.PROCESSING)) {
                                        selectedTask.cancel();
                                }
                        }

                        if (src == highPriorityMenuItem) {
                                taskController.setTaskPriority(selectedTask, TaskPriority.HIGH);
                        }

                        if (src == normalPriorityMenuItem) {
                                taskController.setTaskPriority(selectedTask, TaskPriority.NORMAL);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
