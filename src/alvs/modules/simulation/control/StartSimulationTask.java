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
import alvs.util.Range;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import java.util.List;
import java.util.Random;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

/**
 *
 * @author scsandra
 */
public class StartSimulationTask {

        private BugDataset training, validation;
        private TaskStatus status = TaskStatus.WAITING;
        private String errorMessage;
        private sinkThread thread;
        private JInternalFrame frame;
        private int numberOfBugsCopies, bugLife, iterations, maxBugs = 1000;
        private classifiersEnum classifier = classifiersEnum.Automatic_Selection;
        private JTextArea textArea;
        private List<Range> ranges;
        private Random rand;
        private int totalIDs, stoppingCriteria, stopCounting = 0;
        private double minCountId = 100;
        private List<Result> results;
        private boolean showResults, showCanvas;
        private int userDefinedMaxNVariables;
        private double repProbability;
        PolynomialFitter fitter;
        int[] counter;

        public StartSimulationTask(BugDataset[] datasets, SimpleParameterSet parameters) {
                for (BugDataset dataset : datasets) {
                        if (dataset.getType() == DatasetType.TRAINING) {
                                training = dataset;
                                this.totalIDs = training.getNumberRows();
                        } else if (dataset.getType() == DatasetType.VALIDATION) {
                                validation = dataset;
                        }
                }
                this.numberOfBugsCopies = (Integer) parameters.getParameterValue(StartSimulationParameters.numberOfBugs);
                this.bugLife = (Integer) parameters.getParameterValue(StartSimulationParameters.bugLife);
                this.iterations = (Integer) parameters.getParameterValue(StartSimulationParameters.iterations);
                this.maxBugs = (Integer) parameters.getParameterValue(StartSimulationParameters.bugLimit);
                this.stoppingCriteria = (Integer) parameters.getParameterValue(StartSimulationParameters.stoppingCriteria);
                this.repProbability = (Double) parameters.getParameterValue(StartSimulationParameters.repProbability);
                this.classifier = (classifiersEnum) parameters.getParameterValue(StartSimulationParameters.classifier);
                this.userDefinedMaxNVariables = (Integer) parameters.getParameterValue(StartSimulationParameters.numberOfVariables);


                DesktopParameters desktopParameters = (DesktopParameters) ALVSCore.getDesktop().getParameterSet();
                ConfigurationParameters configuration = (ConfigurationParameters) desktopParameters.getSaveConfigurationParameters();
                this.showResults = (Boolean) configuration.getParameterValue(ConfigurationParameters.showResults);


                this.ranges = new ArrayList<Range>();
                this.results = new ArrayList<Result>();
                this.rand = new Random();

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

                        JInternalFrame frame2 = new JInternalFrame("Results", true, true, true, true);
                        frame2.setSize(new Dimension(700, 700));

                        textArea = new JTextArea("");
                        textArea.setSize(new Dimension(700, 700));
                        JScrollPane panel = new JScrollPane(textArea);
                        frame2.add(panel);
                        ALVSCore.getDesktop().addInternalFrame(frame2);

                        // Creates the ranges of samples that will be used in the world
                        createRanges();

                        // Selects one of the ranges randomly
                        int index = rand.nextInt(ranges.size() - 1);
                        Range range = ranges.get(index);

                        // Starts the simulation (first cicle with the selected range of samples)
                        if (this.userDefinedMaxNVariables == -1) {
                                counter = new int[10];
                                for (int i = 2; i < 10; i++) {
                                        counter[i] = 0;
                                        this.startCicleVariableNumberSelection(range, i);
                                }
                        } else {
                                this.startCicle(range, this.userDefinedMaxNVariables);
                        }

                        status = TaskStatus.FINISHED;
                } catch (Exception e) {
                        e.printStackTrace();
                        status = TaskStatus.ERROR;
                }
        }

        private void startCicle(Range range, int maxVariables) {
                DesktopParameters desktopParameters = (DesktopParameters) ALVSCore.getDesktop().getParameterSet();
                ConfigurationParameters configuration = (ConfigurationParameters) desktopParameters.getSaveConfigurationParameters();

                this.showResults = (Boolean) configuration.getParameterValue(ConfigurationParameters.showResults);
                World world = new World(training, validation, range, this.numberOfBugsCopies, this.bugLife, textArea, this.maxBugs, maxVariables, this.classifier, this.repProbability);
                thread = new sinkThread(world);
                thread.start();
        }

        private void createRanges() {
                int cont = 0;
                int unit = training.getNumberCols() / 10;
                while (cont < 11) {
                        this.ranges.add(new Range(unit * cont, (unit * cont) + unit));
                        cont++;
                }

        }

        private int countIDs(List<Bug> bugs) {
                int count = 0;
                List<Integer> alreadyCount = new ArrayList<Integer>();
                for (Bug bug : bugs) {
                        for (PeakListRow row : bug.getRows()) {
                                if (!alreadyCount.contains(row.getID())) {
                                        alreadyCount.add(row.getID());
                                        count++;
                                }

                        }
                }
                double result = ((double) ((double) count / (double) this.totalIDs)) * 100;
                System.out.println("Count : " + count + "/" + this.totalIDs + " result: " + result + "%");
                return count;
        }

        public class sinkThread extends Thread {

                World world;

                public sinkThread(World world) {
                        this.world = world;
                }

                @Override
                public void run() {
                        double result = Double.MAX_VALUE;
                        while (result > stoppingCriteria) {
                                for (int i = 0; i < iterations; i++) {
                                        world.cicle();
                                }

                                int index = rand.nextInt(ranges.size() - 1);
                                Range range = ranges.get(index);
                                if (showResults) {
                                        printResult(world.getBugs(), range);
                                }

                                // Counting the number of variables in the world
                                int count = countIDs(world.getBugs());

                                result = ((double) ((double) count / (double) training.getNumberRows())) * 100;

                                if (result < minCountId) {
                                        minCountId = result;
                                        stopCounting = 0;
                                } else {
                                        stopCounting++;
                                }


                                // Checking the stopping criteria
                                if (result <= stoppingCriteria) {
                                        printResult(world.getBugs(), range);
                                } else {
                                        world.restart(range);
                                }
                        }
                }
        }

        public void printResult(List<Bug> bugs, Range range) {

                Comparator<Result> c = new Comparator<Result>() {

                        public int compare(Result o1, Result o2) {
                                if (o1.count < o2.count) {
                                        return 1;
                                } else {
                                        return -1;
                                }
                        }
                };

                for (Bug bug : bugs) {
                        if (bug.getFMeasure() > 0.4) {
                                Result result = new Result();
                                result.Classifier = bug.getClassifierType().name();
                                List<Integer> ids = new ArrayList<Integer>();
                                for (PeakListRow row : bug.getRows()) {
                                        result.addValue(String.valueOf(row.getID()));
                                        ids.add(row.getID());
                                }

                              //  TestBug testing = new TestBug(ids, bug.getClassifierType(), training, validation);
                               // double[] values = testing.prediction();
                                result.tspecificity = bug.getspecificity();
                                result.tsensitivity = bug.getsensitivity();
                                result.fScore = bug.getFMeasure();
                                result.vspecificity = 0;
                                result.vsensitivity = 0;
                                result.aucV = 0;
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

                }


                Collections.sort(results, c);


                int contbug = 0;
                String result = range.toString() + " \n";

                for (Result r : results) {
                        result += r.toString();
                        contbug++;
                        if (contbug > 500) {
                                break;
                        }
                }

                this.textArea.setText(result);

        }

        public class sinkThreadVariableNumberSelection extends Thread {

                int nVar;
                World world;

                public sinkThreadVariableNumberSelection(int nVariables, World world) {
                        this.nVar = nVariables;
                        this.world = world;
                }

                @Override
                public void run() {
                        for (int iteration = 0; iteration < 20; iteration++) {
                                for (int i = 0; i < iterations; i++) {
                                        this.world.cicle();
                                }
                                System.out.println("World " + nVar);
                                int index = rand.nextInt(ranges.size() - 1);
                                Range range = ranges.get(index);

                                double[] points = this.world.getNewPoints(nVar);
                                for (int i = 0; i < points.length; i++) {
                                        fitter.addObservedPoint(1, nVar, points[i]);
                                }
                                world.restart(range);
                                counter[nVar]++;
                        }
                        CheckErrorCurve();
                }
        }

        private void startCicleVariableNumberSelection(Range range, int numberOfVariables) {
                World iworld = new World(training, validation, range, this.numberOfBugsCopies, this.bugLife, null, this.maxBugs, numberOfVariables, this.classifier, this.repProbability);
                sinkThreadVariableNumberSelection thread = new sinkThreadVariableNumberSelection(numberOfVariables, iworld);
                thread.start();
        }

        public void CheckErrorCurve() {
                boolean allOver = true;

                for (int i = 2; i < 10; i++) {
                        if (counter[i] < 20) {
                                allOver = false;
                        }
                }

                if (allOver) {
                        try {
                                PolynomialFunction function = fitter.fit();
                                for (int i = 2; i < 10; i++) {
                                        System.out.println(i + " - " + function.value(i));
                                }
                                UnivariateRealFunction derivative = function.derivative();
                                int var = 2;
                                for (int i = 2; i < 10; i++) {
                                        try {
                                                double value = derivative.value(i);
                                                if (value > 0) {
                                                        var = i - 1;
                                                        break;
                                                }
                                                System.out.println(value);
                                        } catch (FunctionEvaluationException ex) {
                                                Logger.getLogger(StartSimulationTask.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                }
                                int index = rand.nextInt(ranges.size() - 1);
                                Range range = ranges.get(index);
                                System.out.println("The choosen value is " + var);
                                this.startCicle(range, var);
                        } catch (OptimizationException ex) {
                                Logger.getLogger(StartSimulationTask.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
        }
}
