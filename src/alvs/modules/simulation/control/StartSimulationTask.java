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
import alvs.data.DatasetType;
import alvs.data.PeakListRow;
import alvs.data.impl.SimpleParameterSet;
import alvs.desktop.impl.DesktopParameters;
import alvs.main.ALVSCore;
import alvs.modules.configuration.ConfigurationParameters;
import alvs.modules.simulation.Bug;
import alvs.modules.simulation.Result;
import alvs.modules.simulation.World;
import alvs.modules.simulation.classifiersEnum;
import alvs.taskcontrol.TaskStatus;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 *
 * @author scsandra
 */
public class StartSimulationTask implements ActionListener, InternalFrameListener{

        private BugDataset training, validation;
        private TaskStatus status = TaskStatus.WAITING;
        private String errorMessage;
        private sinkThread thread;
        private final int bugLife;
        private final int iterations;
        private int maxBugs = 1000;
        private final classifiersEnum[] classifiers;
        private JTextArea textArea;
        private final List<Result> results;
        private boolean showResults;
        private final int dataPartition;
        PolynomialFitter fitter;
        int[] counter;
        boolean stoppingCriteria;
        JButton stopButton;
        JInternalFrame frame2;

        public StartSimulationTask(BugDataset[] datasets, SimpleParameterSet parameters) {
                this.stoppingCriteria = true;
                for (BugDataset dataset : datasets) {
                        if (dataset.getType() == DatasetType.TRAINING) {
                                training = dataset;
                        } else if (dataset.getType() == DatasetType.VALIDATION) {
                                validation = dataset;
                        }
                }
                this.bugLife = (Integer) parameters.getParameterValue(StartSimulationParameters.bugLife);
                this.iterations = (Integer) parameters.getParameterValue(StartSimulationParameters.iterations);
                this.maxBugs = (Integer) parameters.getParameterValue(StartSimulationParameters.bugLimit);
                this.dataPartition = (Integer) parameters.getParameterValue(StartSimulationParameters.dataPartition);
                Object[] selectedClassifier = (Object[]) parameters.getParameterValue(StartSimulationParameters.classifier);

                this.classifiers = new classifiersEnum[selectedClassifier.length];
                for (int i = 0; i < selectedClassifier.length; i++) {
                        this.classifiers[i] = (classifiersEnum) selectedClassifier[i];
                }

                DesktopParameters desktopParameters = (DesktopParameters) ALVSCore.getDesktop().getParameterSet();
                ConfigurationParameters configuration = (ConfigurationParameters) desktopParameters.getSaveConfigurationParameters();
                this.showResults = (Boolean) configuration.getParameterValue(ConfigurationParameters.showResults);

                this.results = new ArrayList<Result>();

                this.fitter = new PolynomialFitter(3, new LevenbergMarquardtOptimizer());
        }

        public String getTaskDescription() {
                return "Start simulation... ";
        }

        public double getFinishedPercentage() {
                return 0.0f;
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
                        status = TaskStatus.PROCESSING;

                        frame2 = new JInternalFrame("Results", true, true, true, true);
                        frame2.setSize(new Dimension(800, 700));
                        frame2.addInternalFrameListener(this);

                        this.stopButton = new JButton("Stop");
                        this.stopButton.addActionListener(this);
                        this.stopButton.setVisible(true);
                        textArea = new JTextArea("");
                        textArea.setSize(new Dimension(700, 700));
                        JPanel mainPanel = new JPanel(new BorderLayout());
                        mainPanel.add(this.stopButton, BorderLayout.NORTH);
                        mainPanel.add(textArea, BorderLayout.CENTER);

                        JScrollPane panel = new JScrollPane(mainPanel);

                        frame2.add(panel);

                        ALVSCore.getDesktop().addInternalFrame(frame2);

                        this.startCicle();

                        status = TaskStatus.FINISHED;
                } catch (Exception e) {
                        status = TaskStatus.ERROR;
                }
        }

        private void startCicle() {
                DesktopParameters desktopParameters = (DesktopParameters) ALVSCore.getDesktop().getParameterSet();
                ConfigurationParameters configuration = (ConfigurationParameters) desktopParameters.getSaveConfigurationParameters();
                int range = this.training.getNumberCols() / this.dataPartition;
                this.showResults = (Boolean) configuration.getParameterValue(ConfigurationParameters.showResults);
                World world = new World(training, validation, range, this.bugLife, textArea, this.maxBugs, this.classifiers);
                thread = new sinkThread(world);
                thread.start();
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
                this.stoppingCriteria = false;
                this.stopButton.setVisible(false);
        }

        
        @Override
        public void internalFrameClosing(InternalFrameEvent ife) {
                this.stoppingCriteria = false;
        }

        @Override
        public void internalFrameClosed(InternalFrameEvent ife) {
                this.stoppingCriteria = false;
        }

       
        @Override
        public void internalFrameDeactivated(InternalFrameEvent ife) {
                this.stoppingCriteria = false;
        }

        @Override
        public void internalFrameOpened(InternalFrameEvent ife) {
                
        }

        @Override
        public void internalFrameIconified(InternalFrameEvent ife) {
                
        }

        @Override
        public void internalFrameDeiconified(InternalFrameEvent ife) {
                
        }

        @Override
        public void internalFrameActivated(InternalFrameEvent ife) {
                
        }

        public class sinkThread extends Thread {

                World world;

                public sinkThread(World world) {
                        this.world = world;
                }

                @Override
                public void run() {
                        while (stoppingCriteria) {
                                for (int i = 0; i < iterations; i++) {
                                        world.cicle();
                                }

                                if (showResults) {
                                        printResult(world.getBugs());
                                }

                                int range = training.getNumberCols() / dataPartition;
                                world.restart(range);
                        }
                }
        }

        public void printResult(List<Bug> bugs) {

                Comparator<Result> c = new Comparator<Result>() {
                        public int compare(Result o1, Result o2) {
                                return Double.compare(o1.fScore, o2.fScore);
                        }
                };

                Comparator<Result> c2 = new Comparator<Result>() {
                        public int compare(Result o1, Result o2) {
                                return Double.compare(o1.getValues().size(), o2.getValues().size());
                        }
                };

                Comparator<Bug> c3 = new Comparator<Bug>() {
                        public int compare(Bug o1, Bug o2) {
                                return Double.compare(o1.getFMeasure(), o2.getFMeasure());
                        }
                };
                int count = 0;
                try {
                        Collections.sort(bugs, c3);
                        Collections.reverse(bugs);

                } catch (Exception e) {
                        e.printStackTrace();
                }
                for (Bug bug : bugs) {
                        if (bug.getFMeasure() > 0.01 && count < 300 && bug.getRows().size() > 1) {
                                Result result = new Result();
                                result.Classifier = bug.getClassifierType().name();
                                for (PeakListRow row : bug.getRows()) {
                                        result.addValue(String.valueOf(row.getID()));
                                }

                                Instances data = bug.getWekaDatasetNoRange(this.training);
                               
                                Evaluation eval = bug.evaluation(data);
                                double tpos = eval.numTruePositives(1);
                                double tneg = eval.numTrueNegatives(1);
                                double fpos = eval.numFalsePositives(1);
                                double fneg = eval.numFalseNegatives(1);
                                result.tspecificity = tneg / (tneg + fpos);
                                result.tsensitivity = tpos / (tpos + fneg);
                                result.fScore = eval.fMeasure(1);
                                if (this.validation != null) {
                                        data = bug.getWekaDatasetNoRange(this.validation);
                                        eval = bug.evaluation(data);
                                        tpos = eval.numTruePositives(1);
                                        tneg = eval.numTrueNegatives(1);
                                        fpos = eval.numFalsePositives(1);
                                        fneg = eval.numFalseNegatives(1);
                                        result.vspecificity = tneg / (tneg + fpos);
                                        result.vsensitivity = tpos / (tpos + fneg);
                                        result.aucV = eval.areaUnderROC(1);
                                }
                                boolean isIt = false;
                                for (Result r : this.results) {
                                        if (r.isIt(result.getValues(), result.Classifier)) {
                                                r.count();
                                                isIt = true;
                                        }
                                }
                                if (!isIt) {
                                        this.results.add(result);
                                }
                        }
                        count++;

                }

                try {
                        Collections.sort(results, c2);
                        Collections.reverse(results);
                        Collections.sort(results, c);
                        Collections.reverse(results);
                } catch (Exception e) {
                        e.printStackTrace();
                }

                int contbug = 0;
                String result = "";

                for (Result r : results) {
                        result += r.toString();
                        contbug++;
                        if (contbug > 500) {
                                break;
                        }
                }
                this.textArea.setText(result);

        }

}
