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
package alvs.modules.simulation.control;

import alvs.data.BugDataset;
import alvs.data.ParameterSet;
import alvs.data.impl.SimpleParameterSet;
import alvs.desktop.Desktop;
import alvs.desktop.ALVSMenu;
import alvs.main.ALVSCore;
import alvs.main.ALVSModule;
import alvs.taskcontrol.Task;
import alvs.taskcontrol.TaskStatus;

import alvs.taskcontrol.TaskListener;
import alvs.util.dialogs.ExitCode;
import alvs.util.dialogs.ParameterSetupDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

/**
 *
 * @author scsandra
 */
public class StartSimulation implements ALVSModule, TaskListener, ActionListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Desktop desktop;
    private SimpleParameterSet parameters;

    public void initModule() {
        this.desktop = ALVSCore.getDesktop();
        desktop.addMenuItem(ALVSMenu.CONTROL, "Start simulation..",
                "Start simulatione", KeyEvent.VK_L, this, null, null);
        parameters = new StartSimulationParameters();
    }

    public void taskStarted(Task task) {
        logger.info("Running Start simulation");
    }

    public void taskFinished(Task task) {
        if (task.getStatus() == TaskStatus.FINISHED) {
            logger.info("Finished simulation" + ((StartSimulationTask) task).getTaskDescription());
        }

        if (task.getStatus() == TaskStatus.ERROR) {

            String msg = "Error while Start simulation .. " + ((StartSimulationTask) task).getErrorMessage();
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
        try {
            ParameterSetupDialog dialog = new ParameterSetupDialog("Simulation parameters", parameters);
            dialog.setVisible(true);
            return dialog.getExitCode();
        } catch (Exception exception) {
            return ExitCode.CANCEL;
        }
    }

    public ParameterSet getParameterSet() {
        return parameters;
    }

    public void setParameters(ParameterSet parameterValues) {
        parameters = (SimpleParameterSet) parameterValues;
    }

    @Override
    public String toString() {
        return "Start simulation";
    }

    public Task[] runModule() {
        BugDataset[] datasets = ALVSCore.getDesktop().getSelectedDataFiles();

        // prepare a new group of tasks        

        StartSimulationTask task = new StartSimulationTask(datasets, parameters);
        task.run();


        return null;


    }
}
