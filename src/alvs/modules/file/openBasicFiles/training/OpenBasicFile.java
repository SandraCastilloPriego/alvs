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
package alvs.modules.file.openBasicFiles.training;

import alvs.data.ParameterSet;
import alvs.desktop.Desktop;
import alvs.desktop.ALVSMenu;
import alvs.desktop.impl.DesktopParameters;
import alvs.main.ALVSCore;
import alvs.main.ALVSModule;
import alvs.taskcontrol.Task;
import alvs.taskcontrol.TaskListener;
import alvs.taskcontrol.TaskStatus;
import alvs.util.dialogs.ExitCode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Logger;

/**
 *
 * @author scsandra
 */
public class OpenBasicFile implements ALVSModule, TaskListener, ActionListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Desktop desktop;
    private String FilePath;

    public void initModule() {
        this.desktop = ALVSCore.getDesktop();
        desktop.addMenuItem(ALVSMenu.FILE, "Open training File..",
                "Opens training of file", KeyEvent.VK_O, this, null, null);
    }

    public void taskStarted(Task task) {
        logger.info("Running training Files");
    }

    public void taskFinished(Task task) {
        if (task.getStatus() == TaskStatus.FINISHED) {
            logger.info("Finished training Files on " + ((OpenBasicFileTask) task).getTaskDescription());
        }

        if (task.getStatus() == TaskStatus.ERROR) {

            String msg = "Error while training Files on .. " + ((OpenBasicFileTask) task).getErrorMessage();
            logger.severe(msg);
            desktop.displayErrorMessage(msg);

        }
    }

    public void actionPerformed(ActionEvent e) {
        ExitCode exitCode = setupParameters();
        if (exitCode != ExitCode.OK) {
            return;
        }

        runModule();
    }

    public ExitCode setupParameters() {
        DesktopParameters deskParameters = (DesktopParameters) ALVSCore.getDesktop().getParameterSet();
        String lastPath = deskParameters.getLastOpenProjectPath();
        if (lastPath == null) {
            lastPath = "";
        }
        File lastFilePath = new File(lastPath);
        DatasetOpenDialog dialog = new DatasetOpenDialog(lastFilePath);
        dialog.setVisible(true);
        try {
            this.FilePath = dialog.getCurrentDirectory();
        } catch (Exception e) {
        }
        return dialog.getExitCode();
    }

    public ParameterSet getParameterSet() {
        return null;
    }

    public void setParameters(ParameterSet parameterValues) {
    }

    public String toString() {
        return "training Files";
    }

    public Task[] runModule() {

        // prepare a new group of tasks
        if (FilePath != null) {
            Task tasks[] = new OpenBasicFileTask[1];
            tasks[0] = new OpenBasicFileTask(FilePath, desktop);

            ALVSCore.getTaskController().addTasks(tasks);

            return tasks;
        } else {
            return null;
        }

    }
}
