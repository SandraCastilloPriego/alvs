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
package alvs.modules.simulation;

import alvs.data.BugDataset;
import alvs.data.PeakListRow;
import java.util.List;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.ComplementNaiveBayes;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IB1;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.MultiScheme;
import weka.classifiers.meta.RandomCommittee;
import weka.classifiers.meta.RandomSubSpace;
import weka.classifiers.meta.Stacking;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.lmt.LogisticBase;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 *
 *
 * @author SCSANDRA
 */
public class TestBug {

        BugDataset training, validation;
        private Classifier classifier;
        double spec = 0, sen = 0, totalspec = 0, totalsen = 0;
        List<Integer> ids;
        classifiersEnum classifierType;
        Instances data;

        public TestBug(List<Integer> ids, classifiersEnum classifier, BugDataset training, BugDataset validation) {
                this.training = training;
                this.validation = validation;
                this.ids = ids;
                classifierType = classifier;
                this.classify();
        }

        public void addId(int id) {
                this.ids.add(id);
        }

        private void classify() {
                try {
                        data = this.getDataset(this.training);
                        classifier = this.getClassifier(classifierType);
                        classifier.buildClassifier(data);
                } catch (Exception ex) {
                }
        }

        public double[] prediction() {
                try {
                        double[] values = new double[6];
                        if (validation != null) {
                                Instances validationData = this.getDataset(this.validation);
                                Evaluation eval = new Evaluation(validationData);
                                eval.crossValidateModel(classifier, validationData, 10, new Random(1));
                                values[3] = eval.precision(1);
                                values[4] = eval.recall(1);

                                values[5] = eval.weightedAreaUnderROC();
                        }
                        spec = 0;
                        sen = 0;
                        totalspec = 0;
                        totalsen = 0;
                       
                        values[0] = spec / totalspec;
                        values[1] = sen / totalsen;

                        Evaluation eval = new Evaluation(data);
                        eval.crossValidateModel(classifier, data, 10, new Random(1));
                        values[0] = eval.precision(1);
                        values[1] = eval.recall(1);
                        values[2] = eval.weightedAreaUnderROC();
                        return values;

                } catch (Exception ex) {
                        ex.printStackTrace();
                        return null;
                }
        }

        private Instances getDataset(BugDataset dataset) {
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
                Instances train = new Instances("Dataset", attributes, 0);

                for (int i = 0; i < dataset.getNumberCols(); i++) {
                        double[] values = new double[train.numAttributes()];
                        String sampleName = dataset.getAllColumnNames().elementAt(i);
                        int cont = 0;
                        for (Integer id : ids) {
                                for (PeakListRow row : dataset.getRows()) {
                                        if (row.getID() == id) {
                                                values[cont++] = (Double) row.getPeak(sampleName);
                                        }
                                }
                        }
                        values[cont] = train.attribute(train.numAttributes() - 1).indexOfValue(dataset.getSampleType(sampleName));

                        Instance inst = new SparseInstance(1.0, values);
                        train.add(inst);
                }

                train.setClass(type);

                return train;

        }

        private Classifier getClassifier(classifiersEnum classifierType) {
                switch (classifierType) {
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
