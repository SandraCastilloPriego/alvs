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
import java.util.*;
import javax.swing.JTextArea;

/**
 *
 * @author bicha
 */
public class World {

        BugDataset trainingDataset, validationDataset;
        List<Bug> population;
        Random rand;
        int jump = 1;
        int cicleNumber = 0;
        int bugLife;
        int maxVariables = 1;
        boolean changeNVariables = false;
        JTextArea text;
        List<Result> results;
        int printCount = 0;
        int bugsLimitNumber;
        classifiersEnum classifier;
        boolean firstCycle = true;
        int numberOfBugsCopies;
        Range range;

        public World(BugDataset training, BugDataset validation, Range range,
                int numberOfBugsCopies, int bugLife, JTextArea text,
                int bugsLimitNumber, int maxVariables, classifiersEnum classifier) {
                this.trainingDataset = training;
                this.validationDataset = validation;
                this.population = new ArrayList<Bug>();
                this.rand = new Random();
                this.bugLife = bugLife;
                this.maxVariables = maxVariables;
                this.text = text;
                this.bugsLimitNumber = bugsLimitNumber;
                this.classifier = classifier;
                this.numberOfBugsCopies = numberOfBugsCopies;
                this.range = range;
                this.results = new ArrayList<Result>();

                if (training != null) {
                        try {
                                for (int i = 0; i < numberOfBugsCopies; i++) {
                                        for (PeakListRow row : training.getRows()) {
                                                this.addBug(row);
                                        }
                                }
                        } catch (Exception e) {
                                System.out.println("variable X has been reporting memory problems");
                                this.population = new ArrayList<Bug>();
                        }

                }
                for (PeakListRow row : training.getRows()) {
                        this.addBug(row);
                }
        }

        public synchronized void addMoreBugs() {
                for (PeakListRow row : trainingDataset.getRows()) {
                        this.addBug(row);
                }
        }

        public List<Bug> getBugs() {
                return this.population;
        }

        private void addBug(PeakListRow row) {
                Bug bug = new Bug(row, trainingDataset, bugLife, maxVariables, classifier, this.range);
                bug.evaluation();
                this.population.add(bug);
        }

        public int getMaxVariables() {
                return this.maxVariables;
        }

        public void cicle() {
                death();

                if (population.size() > this.bugsLimitNumber) {
                        this.purgeBugs();
                }
                eat();
                for (int j = 0; j < this.population.size(); j++) {
                        List<Bug> bugsInside = new ArrayList<Bug>();
                        bugsInside.add(this.population.get(j));
                        for (int i = 0; i < 2; i++) {
                                int index = this.rand.nextInt(this.population.size());
                                if (index > 0) {
                                        bugsInside.add(this.population.get(index));
                                }
                        }
                        reproduction(bugsInside);
                }

                this.cicleNumber++;
                this.printCount++;

                this.changeNVariables = false;

        }

        private synchronized void eat() {
                for (Bug bug : population) {
                        try {
                                bug.eat();
                        } catch (NullPointerException e) {
                                e.printStackTrace();
                        }
                }
        }

        public void purgeBugs() {
                Comparator<Bug> c = new Comparator<Bug>() {

                        public int compare(Bug o1, Bug o2) {
                                if (o1.getFMeasure() < o2.getFMeasure()) {
                                        return 1;
                                } else {
                                        return -1;
                                }
                        }
                };

                Collections.sort(population, c);
                for (int i = this.bugsLimitNumber; i < this.population.size(); i++) {
                        population.get(i).kill();
                }
        }

        private synchronized void death() {
                List<Bug> deadBugs = new ArrayList<Bug>();
                for (Bug bug : population) {
                        try {
                                if (bug.isDead()) {
                                        deadBugs.add(bug);
                                } else {
                                        if (this.changeNVariables) {
                                                bug.setMaxVariable(maxVariables);
                                        }
                                }
                        } catch (Exception e) {
                        }
                }
                for (Bug bug : deadBugs) {
                        this.population.remove(bug);
                }
        }

        public List<Result> getResult() {
                return this.results;
        }

        public double[] getNewPoints(int nPoints) {
                int counter = 0;
                double[] points = new double[nPoints + 1];
                for (int i = 0; i < this.population.size(); i++) {
                        Bug bug = this.population.get(i);
                        if ((bug.getRows().size() == this.maxVariables)) {
                                points[counter++] = bug.getTestError();
                        }
                        if (counter == nPoints) {
                                break;
                        }
                }
                return points;
        }

        public void restart(Range newRange) {
                this.range = newRange;
                for (Bug b : this.population) {
                        b.setNewRange(newRange,
                                this.trainingDataset);
                }

                this.addMoreBugs();
        }

        private void reproduction(List<Bug> bugsInside) {
                try {
                        Comparator<Bug> c = new Comparator<Bug>() {

                                public int compare(Bug o1, Bug o2) {
                                        if (o1.getFMeasure() < o2.getFMeasure()) {
                                                return 1;
                                        } else {
                                                return -1;
                                        }
                                }
                        };
                        if (bugsInside.size() > 1) {
                                Collections.sort(bugsInside, c);
                                Bug mother = bugsInside.get(0);
                                for (Bug father : bugsInside) {
                                        if (!mother.isSameBug(father)) {
                                                if (father.getAge() > (this.bugLife / 3) && mother.getAge() > (this.bugLife / 3) 
                                                        && father.predict() && mother.predict()) {
                                                        population.add(new Bug(father, mother, this.trainingDataset, this.bugLife, this.maxVariables, this.range));
                                                }
                                        }
                                }
                        }

                } catch (Exception e) {
                        System.out.println("Something failed during reproduction");
                }
        }
       
}
