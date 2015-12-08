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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
    private classifiersEnum[] classifiers;
    private final classifiersEnum classifierType;
    double spec = 0, sen = 0, totaltpostneg = 0, tpos = 0, tneg = 0, fpos = 0, fneg = 0, precision = 0, recall = 0;
    private final Random rand;
    private int MAXNUMBERGENES;
    Evaluation eval;
    Set<Integer> range;
    int[] clusters;
    Instances training, test;
    double fValue = 0;
    double ridge = 0;
    BugDataset dataset;
    int count = 1;

    public Bug(PeakListRow row, BugDataset dataset, int bugLife, int maxVariable, classifiersEnum[] classifiers, Set<Integer> range) {
        rand = new Random();
        this.range = range;
        this.rowList = new ArrayList<>();

        if (row != null) {
            this.rowList.add(row);
        }
        this.MAXNUMBERGENES = maxVariable;
        this.dataset = dataset;

        this.classifiers = classifiers;
        int n = rand.nextInt(classifiers.length);
        this.classifierType = classifiers[n];
        /*   if (this.classifierType == classifiersEnum.) {
         this.ridge = (double) (this.rand.nextInt(200) + 1) / 10;
         }*/

        this.classify(dataset);
        evaluation();
        this.life = bugLife;
        clusters = new int[rowList.size()];
        for (int i = 0; i < rowList.size(); i++) {
            clusters[i] = rowList.get(i).getCluster();
        }
    }

    @Override
    public Bug clone() {
        Bug newBug = new Bug(null, null, this.life, this.MAXNUMBERGENES, this.classifiers, this.range);
        newBug.training = this.training;
        newBug.test = this.test;
        newBug.rowList = this.getRows();
        return newBug;
    }

    public Bug(Bug father, Bug mother, BugDataset dataset, int bugLife, int maxVariable, Set<Integer> range) {
        this.dataset = dataset;
        this.life = bugLife;
        this.MAXNUMBERGENES = maxVariable;
        this.range = range;
        this.rowList = new ArrayList<>();

        rand = new Random();
        if (rand.nextInt(1) == 0) {
            this.classifierType = mother.getClassifierType();
        } else {
            this.classifierType = father.getClassifierType();
        }

        classifier = setClassifier();
        if (father.getRows().isEmpty() || mother.getRows().isEmpty()) {
            this.life = 0;
        } else {
            this.rowList = selectGenes(father.getRows(), mother.getRows());
            if (this.rowList.isEmpty()) {
                this.life = 0;

            } else {
                this.classify(dataset);
                this.life = bugLife;
                this.evaluation();
            }
        }

    }

    private boolean containsGene(List<PeakListRow> list, PeakListRow gene) {
        for (PeakListRow originalGene : list) {
            if (originalGene.getID() == gene.getID()) {
                return true;
            }
        }
        return false;
    }

    private List<PeakListRow> selectGenes(List<PeakListRow> father, List<PeakListRow> mother) {
        List<PeakListRow> finalGenes = new ArrayList<>();
        List<PeakListRow> tempGenes = new ArrayList<>();
        Instances data;

        double correctFinal = 0;

        // Add all the father genes to the finalGenes and tempGenes lists
        for (PeakListRow gene : father) {
            finalGenes.add(gene);
            tempGenes.add(gene);
        }

        // Add all the mother genes to the finalGenes and tempGenes lists trying not to duplicate them
        for (PeakListRow gene : mother) {
            if (!this.containsGene(finalGenes, gene)) {
                finalGenes.add(gene);
                tempGenes.add(gene);
                //        System.out.print(gene.getID() + ", ");
            }
        }

        // Create a dataset with all the genes
        data = this.getWekaDataset(this.dataset, finalGenes);
        //System.out.println(data.numInstances());
        correctFinal = this.getMeasure(data);
       // System.out.println("Genes: "+finalGenes.size()+" Value:"+correctFinal);

        // For each gene in the temporary list
        for (PeakListRow tempGene : tempGenes) {
            // remove gene from the final gene list
            finalGenes.remove(tempGene);
            // create a new dataset
            if (finalGenes.size() > 0) {
                data = this.getWekaDataset(this.dataset, finalGenes);

                double correct = this.getMeasure(data);
                // if the f-value is better or equal than the previous one update it. 
                // If not, put the gene again to the finalGenes list.
                if (correct >= correctFinal) {
                    correctFinal = correct;
                } else {
                    //            System.out.println("added back");
                    finalGenes.add(tempGene);
                }
            }
        }

        return finalGenes;
    }

    private double getMeasure(Instances data) {
        if (classifier != null && data != null) {
            try {
                ThresholdSelector TSclassifier = new ThresholdSelector();
                TSclassifier.setMeasure(new SelectedTag("recall", ThresholdSelector.TAGS_MEASURE));
                TSclassifier.setClassifier(classifier);
                
                TSclassifier.buildClassifier(data);
               // classifier.buildClassifier(data);
                eval = new Evaluation(data);
                eval.evaluateModel(TSclassifier, data);
               //  System.out.println("ROC: " + eval.areaUnderROC(1));

                // return eval.fMeasure(1);
                return eval.areaUnderROC(1);
            } catch (Exception ex) {
                Logger.getLogger(Bug.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return 0.0;
    }

    public Instances getWekaDataset(BugDataset dataset, List<PeakListRow> rowList) {
        try {

            FastVector attributes = new FastVector();
            if (rowList.isEmpty()) {
                return null;
            }
            for (int i = 0; i < rowList.size(); i++) {
                Attribute weight = new Attribute("weight" + i);
                attributes.addElement(weight);
            }

            FastVector labels = new FastVector();

            labels.addElement("1");
            labels.addElement("2");
            Attribute type = new Attribute("class", labels);

            attributes.addElement(type);

            Instances data = new Instances("Data", attributes, 0);

            for (Integer i : range) {
                String sampleName = dataset.getAllColumnNames().elementAt(i);

                double[] values = new double[data.numAttributes()];
                int cont = 0;
                for (PeakListRow row : rowList) {
                    values[cont++] = (Double) row.getPeak(sampleName);
                }
                values[cont] = data.attribute(data.numAttributes() - 1).indexOfValue(dataset.getSampleType(sampleName));

                Instance inst = new SparseInstance(1.0, values);
                data.add(inst);
            }

            data.setClass(type);
            return data;
        } catch (Exception ex) {
            Logger.getLogger(Bug.class.getName()).log(Level.SEVERE, null, ex);

        }
        return null;

    }

    public Instances getWekaDatasetNoRange(BugDataset dataset) {
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

            Instances data = new Instances("Data", attributes, 0);

            for (int i = 0; i < dataset.getNumberCols(); i++) {

                String sampleName = dataset.getAllColumnNames().elementAt(i);

                double[] values = new double[data.numAttributes()];
                int cont = 0;
                for (PeakListRow row : rowList) {
                    values[cont++] = (Double) row.getPeak(sampleName);
                }
                values[cont] = data.attribute(data.numAttributes() - 1).indexOfValue(dataset.getSampleType(sampleName));

                Instance inst = new SparseInstance(1.0, values);
                data.add(inst);
            }

            data.setClass(type);
            return data;
        } catch (Exception ex) {
            Logger.getLogger(Bug.class.getName()).log(Level.SEVERE, null, ex);

        }
        return null;

    }

    public void evaluation() {
        try {
            
            
            ThresholdSelector TSclassifier = new ThresholdSelector();
            TSclassifier.setClassifier(classifier);
            TSclassifier.setMeasure(new SelectedTag("recall", ThresholdSelector.TAGS_MEASURE));
            TSclassifier.buildClassifier(training);
            eval = new Evaluation(training);
            eval.evaluateModel(TSclassifier, training);

            // this.fValue = eval.fMeasure(1);
            this.fValue = eval.areaUnderROC(1);
            this.tpos = eval.numTruePositives(1);
            this.tneg = eval.numTrueNegatives(1);
            this.fpos = eval.numFalsePositives(1);
            this.fneg = eval.numFalseNegatives(1);
            this.sen = this.tpos / (this.tpos + this.fneg);
            this.spec = this.tneg / (this.tneg + this.fpos);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.life = 0;
        }
    }

    public Evaluation evaluation(Instances dataset) {
        try {
            eval = new Evaluation(dataset);
            eval.evaluateModel(classifier, dataset);
            return eval;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void setMaxVariable(int maxVariable) {
        this.MAXNUMBERGENES = maxVariable;
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
        if (this.rowList.size() > 1) {
            life = life - (this.rowList.size() / 2);
        }
        if (this.rowList.isEmpty()) {
            life = 0;
        }
        if (this.life < 1 || this.life == Double.NaN) {
            return true;
        } else {
            return false;
        }
    }

    public void classify(BugDataset dataset) {
        try {
            this.training = getWekaDataset(dataset, this.rowList);
            classifier = setClassifier();
            if (classifier != null && this.training != null) {
                classifier.buildClassifier(training);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void kill() {
        this.life = -1;
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
                RandomForest classif = new RandomForest();
                classif.setNumFeatures(this.rowList.size());
                return classif;
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

    void setNewRange(Set<Integer> newRange, BugDataset dataset) {
        this.range = newRange;
        classify(dataset);
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

    public double getPrecision() {
        return this.precision;
    }

    public double geRecall() {
        return this.recall;
    }

    @Override
    public String toString() {
        String rows = "";
        for (PeakListRow row : this.rowList) {
            rows += row.getID();
            rows += ",";
        }
        return rows;
    }

}
