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
package alvs.modules.file.openBasicFiles;


import alvs.data.BugDataset;
import alvs.data.impl.datasets.SimpleBasicDataset;
import alvs.data.parser.Parser;
import alvs.data.parser.impl.BasicFilesParserCSV;
import alvs.desktop.Desktop;
import alvs.taskcontrol.Task;
import alvs.taskcontrol.TaskStatus;

/**
 *
 * @author scsandra
 */
public class OpenBasicFileTask implements Task {

        private String fileDir;
        private TaskStatus status = TaskStatus.WAITING;
        private String errorMessage;
        private Desktop desktop;
        private Parser parser;

        public OpenBasicFileTask(String fileDir, Desktop desktop) {
                if (fileDir != null) {
                        this.fileDir = fileDir;
                }
                this.desktop = desktop;
        }

        public String getTaskDescription() {
                return "Opening File... ";
        }

        public double getFinishedPercentage() {
                if (parser != null) {
                        return parser.getProgress();
                } else {
                        return 0.0f;
                }
        }

        public TaskStatus getStatus() {
                return status;
        }

        public String getErrorMessage() {
                return errorMessage;
        }

        public void cancel() {
                status = TaskStatus.CANCELED;
        }

        public void run() {
                try {
                        this.openFile();
                } catch (Exception e) {
                        status = TaskStatus.ERROR;
                        errorMessage = e.toString();
                        return;
                }
        }

        public void openFile() {
                status = TaskStatus.PROCESSING;
                try {
                        if (status == TaskStatus.PROCESSING) {
                                parser = new BasicFilesParserCSV(fileDir);
                                parser.fillData();
                                BugDataset dataset = (SimpleBasicDataset) parser.getDataset();
                                desktop.AddNewFile(dataset);
                        }
                } catch (Exception ex) {
                }

                status = TaskStatus.FINISHED;
        }
}
