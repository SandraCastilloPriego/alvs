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

import alvs.taskcontrol.Task;
import alvs.taskcontrol.TaskPriority;
import alvs.taskcontrol.TaskStatus;
import alvs.util.components.LabeledProgressBar;
import java.util.Hashtable;

import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 * 
 * Task queue
 */
class TaskQueue extends AbstractTableModel {

        private Logger logger = Logger.getLogger(this.getClass().getName());
        private static final int DEFAULT_CAPACITY = 64;
        /**
         * This array stores the actual tasks
         */
        private WrappedTask[] queue;
        /**
         * Current size of the queue
         */
        private int size;
        private Hashtable<Integer, LabeledProgressBar> progressBars;

        TaskQueue() {
                size = 0;
                queue = new WrappedTask[DEFAULT_CAPACITY];
                progressBars = new Hashtable<Integer, LabeledProgressBar>();
        }

        synchronized void addWrappedTask(WrappedTask task) {

                logger.finest("Adding task \"" + task + "\" to the task controller queue");

                // If the queue is full, make a bigger queue
                if (size == queue.length) {
                        WrappedTask[] newQueue = new WrappedTask[queue.length * 2];
                        System.arraycopy(queue, 0, newQueue, 0, size);
                        queue = newQueue;
                }

                queue[size] = task;
                size++;

                // Call fireTableDataChanged because we have a new row and order of rows
                // may have changed
                fireTableDataChanged();

        }

        synchronized void clear() {
                size = 0;
                queue = new WrappedTask[DEFAULT_CAPACITY];
                fireTableDataChanged();
        }

        /**
         * Refresh the queue (reorder the tasks according to priority) and send a
         * signal to Tasks in progress window to redraw updated data, such as task
         * status and finished percentages.
         */
        synchronized void refresh() {

                // We must not call fireTableDataChanged, because that would clear the
                // selection in the task window
                fireTableRowsUpdated(0, size - 1);

        }

        synchronized boolean isEmpty() {
                return size == 0;
        }

        synchronized boolean allTasksFinished() {
                for (int i = 0; i < size; i++) {
                        TaskStatus status = queue[i].getActualTask().getStatus();
                        if ((status == TaskStatus.PROCESSING) || (status == TaskStatus.WAITING)) {
                                return false;
                        }
                }
                return true;
        }

        synchronized WrappedTask[] getQueueSnapshot() {
                WrappedTask[] snapshot = new WrappedTask[size];
                System.arraycopy(queue, 0, snapshot, 0, size);
                return snapshot;
        }

        /* TableModel implementation */
        private static final String columns[] = {"Item", "Priority", "Status",
                "% done"};

        /**
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public synchronized int getRowCount() {
                return size;
        }

        /**
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
                return columns.length;
        }

        public String getColumnName(int column) {
                return columns[column];
        }

        /**
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public synchronized Object getValueAt(int row, int column) {

                if (row < size) {

                        WrappedTask wrappedTask = queue[row];
                        Task actualTask = wrappedTask.getActualTask();

                        switch (column) {
                                case 0:
                                        return actualTask.getTaskDescription();
                                case 1:
                                        return wrappedTask.getPriority();
                                case 2:
                                        return actualTask.getStatus();
                                case 3:
                                        double finishedPercentage = actualTask.getFinishedPercentage();
                                        LabeledProgressBar progressBar = progressBars.get(row);
                                        if (progressBar == null) {
                                                progressBar = new LabeledProgressBar(finishedPercentage);
                                                progressBars.put(row, progressBar);
                                        } else {
                                                progressBar.setValue(finishedPercentage);
                                        }
                                        return progressBar;
                        }
                }

                return null;

        }

        /**
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int column) {
                switch (column) {
                        case 0:
                                return String.class;
                        case 1:
                                return TaskPriority.class;
                        case 2:
                                return TaskStatus.class;
                        case 3:
                                return LabeledProgressBar.class;
                }
                return null;

        }
}
