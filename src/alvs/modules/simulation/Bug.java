/*
 * Copyright 2010 - 2012
 * This file is part of ALVS.
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
package alvs.modules.simulation;

import alvs.data.BugDataset;
import alvs.data.PeakListRow;
import alvs.util.Range;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author bicha
 */
public final class Bug {

        private List<PeakListRow> rowList;
        private int life = 300;
        private Classifier classifier;
        private classifiersEnum classifierType;
        private double total;
        double spec = 0, sen = 0, totaltposfneg = 0, tpos = 0, tneg = 0, fpos = 0, fneg = 0, precision = 0, recall = 0;
        private Random rand;
        private int MAXNUMBERGENES;
        Evaluation eval;
        boolean fixValue = false;
        Range range;
        int[] clusters;
        Instances training, test;
        double fValue = 0;

        public Bug(PeakListRow row, BugDataset dataset, int bugLife, int maxVariable, classifiersEnum classifier, Range range) {
                rand = new Random();
                this.range = range;
                this.rowList = new ArrayList<PeakListRow>();
                if (row != null) {
                        this.rowList.add(row);
                }
                this.MAXNUMBERGENES = maxVariable;
                if (classifier == classifiersEnum.Automatic_Selection) {
                        int n = rand.nextInt(classifiersEnum.values().length);
                        this.classifierType = classifiersEnum.values()[n];
                } else {
                        this.classifierType = classifier;
                }
                this.classify(range, dataset);
                this.life = bugLife;

                clusters = new int[rowList.size()];
                for (int i = 0; i < rowList.size(); i++) {
                        clusters[i] = rowList.get(i).getCluster();
                }
        }

        public void eval() {
                try {
                        eval = new Evaluation(training);
                } catch (Exception ex) {
                        this.life = 0;
                }
        }

        @Override
        public Bug clone() {
                Bug newBug = new Bug(null, null, this.life, this.MAXNUMBERGENES, this.classifierType, this.range);
                newBug.training = this.training;
                newBug.test = this.test;
                newBug.rowList = this.getRows();
                return newBug;
        }

        public double getAge() {
                return this.total;
        }

        public Bug(Bug father, Bug mother, BugDataset dataset, int bugLife, int maxVariable, Range range) {
                rand = new Random();
                this.rowList = new ArrayList<PeakListRow>();
                this.MAXNUMBERGENES = maxVariable;
                this.assingGenes(mother, false);
                this.assingGenes(father, false);
                this.range = range;

                this.orderPurgeGenes();

                if (rand.nextInt(1) == 0) {
                        this.classifierType = mother.getClassifierType();
                } else {
                        this.classifierType = father.getClassifierType();
                }
                this.classify(range, dataset);

                this.life = bugLife;

                clusters = new int[rowList.size()];
                for (int i = 0; i < rowList.size(); i++) {
                        clusters[i] = rowList.get(i).getCluster();
                }
                this.eval();
        }

        public void setMaxVariable(int maxVariable) {
                this.MAXNUMBERGENES = maxVariable;
        }

        private void assingGenes(Bug parent, boolean isFather) {
                for (PeakListRow row : parent.getRows()) {
                        if (!this.rowList.contains(row)) {
                                this.rowList.add(row);
                                if (isFather) {
                                        break;
                                }
                        }
                }
        }

        private void orderPurgeGenes() {
                int removeGenes = this.rowList.size() - this.MAXNUMBERGENES;
                if (removeGenes > 0) {
                        for (int i = 0; i < removeGenes; i++) {
                                int index = getRepeatIndex();
                                if (index == -1) {
                                        index = rand.nextInt(this.rowList.size() - 1);
                                }
                                this.rowList.remove(index);
                        }
                }
        }

        public int getRepeatIndex() {
                for (int i = 0; i < this.rowList.size(); i++) {
                        PeakListRow row = this.rowList.get(i);
                        for (PeakListRow r : this.rowList) {
                                if (row != r && row.getCluster() == r.getCluster()) {
                                        return i;
                                }
                        }
                }
                return -1;
        }

        public classifiersEnum getClassifierType() {
                return this.classifierType;
        }

        public double getFMeasure() {
                return fValue;
        }

        public List<PeakListRow> getRows() {
                return this.rowList;
        }

        public double getLife() {
                return life;
        }

        boolean isDead() {
                life--;
                if (this.rowList.isEmpty()) {
                        life = 0;
                }
                if (this.life < 1 || this.life == Double.NaN) {
                        return true;
                } else {
                        return false;
                }
        }

        public void classify(Range range, BugDataset dataset) {
                try {
                        getWekaDataset(range, dataset);
                        classifier = setClassifier();
                        if (classifier != null) {
                                classifier.buildClassifier(training);
                        }
                } catch (Exception ex) {
                        ex.printStackTrace();
                        //Logger.getLogger(Bug.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        public void eat() {
                Instance instance = this.test.instance(this.rand.nextInt(this.test.numInstances()));
                double prediction = isClassified(instance);
                double realValue = Double.parseDouble(instance.stringValue(instance.classAttribute()));

                if (prediction == 0.0 && realValue == 1.0) {
                        this.life += 0.5;
                        this.tneg++;
                        this.totaltposfneg++;
                } else if (prediction == 1.0 && realValue == 2.0) {
                        this.life += 0.5;
                        this.tpos++;
                        this.totaltposfneg++;
                } else if (prediction == 0.0 && realValue == 2.0) {
                        this.life -= 0.5;
                        this.fneg++;
                } else if (prediction == 1.0 && realValue == 1.0) {
                        this.life -= 0.5;
                        this.fpos++;
                }
                this.total++;
                if (this.tpos > 0) {
                        this.sen = this.tpos / this.totaltposfneg;
                        this.precision = this.tpos / (this.tpos + this.fpos);
                        this.recall = this.tpos / (this.tpos + this.fneg);
                }

                if (this.tneg > 0) {
                        this.spec = this.tneg / this.totaltposfneg;
                }
                this.fValue = 2 * ((precision * recall) / (precision + recall));
        }

        public void increaseEnergy() {
                this.life += 0.5;
        }

        public void kill() {
                this.life = -1;
        }

        public void addLife() {
                this.life++;
        }

        public double isClassified(Instance instance) {
                try {
                        double prediction = eval.evaluateModelOnceAndRecordPrediction(classifier, instance);
                        //  System.out.println(instance.stringValue(instance.classAttribute()) + " - " + prediction);
                        return prediction;
                } catch (Exception ex) {
                        ex.printStackTrace();
                        return -1;
                }
        }

        private Classifier setClassifier() {
                switch (this.classifierType) {
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
                                life = 0;
                                return null;
                }

        }

        private void getWekaDataset(Range range, BugDataset dataset) {
                try {

                        FastVector attributes = new FastVector();

                        for (int i = 0; i < rowList.size(); i++) {
                                Attribute weight = new Attribute("weight" + i);
                                attributes.addElement(weight);
                        }

                        FastVector labels = new FastVector();

                        labels.addElement("1");
                        labels.addElement("2");
                        Attribute type = new Attribute("class", labels);

                        attributes.addElement(type);

                        //Creates the dataset
                        this.training = new Instances("Dataset", attributes, 0);
                        this.test = new Instances("test Dataset", attributes, 0);

                        for (int i = 0; i < dataset.getNumberCols(); i++) {
                                if (!range.contains(i)) {
                                        String sampleName = dataset.getAllColumnNames().elementAt(i);

                                        double[] values = new double[training.numAttributes()];
                                        int cont = 0;
                                        for (PeakListRow row : rowList) {
                                                values[cont++] = (Double) row.getPeak(sampleName);
                                        }
                                        values[cont] = training.attribute(training.numAttributes() - 1).indexOfValue(dataset.getSampleType(sampleName));

                                        Instance inst = new SparseInstance(1.0, values);
                                        training.add(inst);
                                } else {
                                        String sampleName = dataset.getAllColumnNames().elementAt(i);

                                        double[] values = new double[test.numAttributes()];
                                        int cont = 0;
                                        for (PeakListRow row : rowList) {
                                                values[cont++] = (Double) row.getPeak(sampleName);
                                        }
                                        values[cont] = test.attribute(test.numAttributes() - 1).indexOfValue(dataset.getSampleType(sampleName));

                                        Instance inst = new SparseInstance(1.0, values);
                                        test.add(inst);

                                }
                        }

                        training.setClass(type);
                        test.setClass(type);

                } catch (Exception ex) {
                        Logger.getLogger(Bug.class.getName()).log(Level.SEVERE, null, ex);

                }

        }

        public double getTestError() {
                try {
                        Evaluation evalC = new Evaluation(training);
                        evalC.crossValidateModel(classifier, training, 10, new Random(1));
                        // evalC.evaluateModel(classifier, test);
                        double CVError = 1 - evalC.fMeasure(1);
                        return CVError;
                } catch (Exception ex) {
                        ex.printStackTrace();
                        return -1.0;
                }
        }

        void setNewRange(Range newRange, BugDataset dataset) {
                this.range = newRange;
                classify(newRange, dataset);
                this.eval();
        }

        public boolean isSameBug(Bug bug) {
                if (bug.getRows().size() != this.rowList.size()) {
                        return false;
                }
                for (PeakListRow val : bug.getRows()) {
                        if (!this.rowList.contains(val)) {
                                return false;
                        }
                }
                if (bug.getClassifierType().equals(this.classifierType)) {
                        return true;
                } else {
                        return false;
                }
        }

        public double getspecificity() {
                return this.spec;
        }

        public double getsensitivity() {
                return this.sen;
        }
}
