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
package alvs.modules.simulation.test;

import alvs.data.BugDataset;
import alvs.data.PeakListRow;
import alvs.data.impl.SimpleParameterSet;
import alvs.modules.simulation.classifiersEnum;
import alvs.taskcontrol.Task;
import alvs.taskcontrol.TaskStatus;
import java.util.ArrayList;
import java.util.List;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IB1;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.*;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.*;
import weka.classifiers.trees.lmt.LogisticBase;
import weka.core.*;

/**
 *
 * @author scsandra
 */
public class TestTask implements Task {

    BugDataset training, validation;
    private Classifier classifier;
    private classifiersEnum classifierName;
    double spec = 0, sen = 0, totalspec = 0, totalsen = 0;
    List<Integer> ids;
    Instances trainingData, validationData;
    double ridge = 0;
    private TaskStatus status = TaskStatus.WAITING;
    private String errorMessage;

    public TestTask(BugDataset training, BugDataset validation, SimpleParameterSet parameters) {
        this.training = training;
        if (validation == null) {
            this.validation = training;
        } else {
            this.validation = validation;
        }

        classifierName = (classifiersEnum) parameters.getParameterValue(TestParameters.classifier);
        String id = (String) parameters.getParameterValue(TestParameters.ids);
        String[] idsS = id.split(",");
        ids = new ArrayList<Integer>();
        for (String idS : idsS) {
            ids.add(Integer.valueOf(idS));
        }

    }

    public String getTaskDescription() {
        return "Test concrete rows... ";
    }

    public double getFinishedPercentage() {
        return 0.0;
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
            this.classify();
            this.printPrediction();
            status = TaskStatus.FINISHED;
        } catch (Exception e) {
            status = TaskStatus.ERROR;
        }
    }

    public double prediction() {
        try {
            Evaluation eval = new Evaluation(trainingData);
            eval.evaluateModel(classifier, trainingData);
            for (int i = 0; i < trainingData.numInstances(); i++) {
                System.out.println(trainingData.instance(i).classValue() + " - " + eval.evaluateModelOnce(classifier, trainingData.instance(i)));
            }

            return eval.errorRate();

        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    public void printPrediction() {
        try {
                     
            validationData = this.getDataset(validation);
            
            ThresholdSelector TSclassifier = new ThresholdSelector();
            TSclassifier.setClassifier(classifier);
            TSclassifier.setMeasure(new SelectedTag("FMEASURE", ThresholdSelector.TAGS_MEASURE));
            TSclassifier.buildClassifier(validationData);  
            Evaluation eval = new Evaluation(validationData);
            eval.evaluateModel(TSclassifier, validationData);
            for (int i = 0; i < validationData.numInstances(); i++) {
                System.out.println(validationData.instance(i).classValue() + " - " + eval.evaluateModelOnce(classifier, validationData.instance(i)));
            }

            System.out.println(eval.toSummaryString());
            System.out.println(classifier.toString());
            double tpos = eval.numTruePositives(1);
            double tneg = eval.numTrueNegatives(1);
            double fpos = eval.numFalsePositives(1);
            double fneg = eval.numFalseNegatives(1);
            this.sen = tpos / (tpos + fneg);
            this.spec = tneg / (tneg + fpos);
            System.out.println("AUC: " + eval.areaUnderROC(1));
            System.out.println("Spec:" + spec + "Sen: " + sen);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void classify() {
        try {
            trainingData = this.getDataset(this.training);
            classifier = this.setClassifier();
            classifier.buildClassifier(trainingData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Instances getDataset(BugDataset dataset) {
        try {

            FastVector attributes = new FastVector();

            for (int i = 0; i < ids.size(); i++) {
                Attribute weight = new Attribute("weight" + i);
                attributes.addElement(weight);
            }

            FastVector labels = new FastVector();

            labels.addElement("1");
            labels.addElement("2");
            Attribute type = new Attribute("class", labels);

            attributes.addElement(type);

            //Creates the dataset
            Instances data = new Instances("Dataset", attributes, 0);

            for (int i = 0; i < dataset.getNumberCols(); i++) {

                String sampleName = dataset.getAllColumnNames().elementAt(i);

                double[] values = new double[data.numAttributes()];
                int cont = 0;
                for (Integer id : ids) {
                    for (PeakListRow row : dataset.getRows()) {
                        if (row.getID() == id) {
                            values[cont++] = (Double) row.getPeak(sampleName);
                        }
                    }
                }
                values[cont] = data.attribute(data.numAttributes() - 1).indexOfValue(dataset.getSampleType(sampleName));

                Instance inst = new SparseInstance(1.0, values);
                data.add(inst);

            }

            data.setClass(type);

            return data;
        } catch (Exception ex) {
            return null;
        }

    }

    private Classifier setClassifier() {
        switch (this.classifierName) {
            case Logistic:
                return new Logistic();
            case LogisticBase:
                return new LogisticBase();
            case LogitBoost:
                return new LogitBoost();
            case NaiveBayesMultinomialUpdateable:
                return new NaiveBayesMultinomialUpdateable();
            case NaiveBayesUpdateable:
                return new NaiveBayesUpdateable();
            case RandomForest:
                return new RandomForest();
            case RandomCommittee:
                return new RandomCommittee();
            case RandomTree:
                return new RandomTree();
            case ZeroR:
                return new ZeroR();
            case Stacking:
                return new Stacking();
            case AdaBoostM1:
                return new AdaBoostM1();
            case Bagging:
                return new Bagging();
            case ComplementNaiveBayes:
                return new ComplementNaiveBayes();
            case IB1:
                return new IB1();
            case J48:
                return new J48();
            case KStar:
                return new KStar();
            case LMT:
                return new LMT();
            case MultiScheme:
                return new MultiScheme();
            case NaiveBayes:
                return new NaiveBayes();
            case NaiveBayesMultinomial:
                return new NaiveBayesMultinomial();
            case OneR:
                return new OneR();
            case PART:
                return new PART();
            case RandomSubSpace:
                return new RandomSubSpace();
            case REPTree:
                return new REPTree();
            case SimpleLogistic:
                return new SimpleLogistic();
            case SMO:
                return new SMO();
            default:
                return null;
        }

    }
}
