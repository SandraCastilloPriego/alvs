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
    classifiersEnum[] classifiers;
    boolean firstCycle = true;
    Set<Integer> range;

    public World(BugDataset training, BugDataset validation, int range, int bugLife, JTextArea text,
        int bugsLimitNumber, classifiersEnum[] classifiers) {
        this.trainingDataset = training;
        this.validationDataset = validation;
        this.population = new ArrayList<>();
        this.rand = new Random();
        this.bugLife = bugLife;
        this.text = text;
        this.bugsLimitNumber = bugsLimitNumber;
        this.classifiers = classifiers;
        if (range == this.trainingDataset.getNumberCols()) {
            this.range = this.getSamples();
        } else {
            this.range = this.getRandomSample(range);
        }
        this.results = new ArrayList<>();

        if (training != null) {
            try {
                for (PeakListRow row : training.getRows()) {
                    this.addBug(row);
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

    public Set<Integer> getRandomSample(int range) {
        Random rng = new Random();
        Set<Integer> generated = new LinkedHashSet<>();
        while (generated.size() < range) {
            Integer next = rng.nextInt(this.trainingDataset.getNumberCols() - 1) + 1;
            // As we're adding to a set, this will automatically do a containment check
            generated.add(next);
        }
        return generated;
    }

    public List<Bug> getBugs() {
        return this.population;
    }

    private void addBug(PeakListRow row) {
        Bug bug = new Bug(row, trainingDataset, bugLife, maxVariables, classifiers, this.range);
        this.population.add(bug);
    }

    public int getMaxVariables() {
        return this.maxVariables;
    }

    public synchronized void cicle() {
        death();
        Comparator<Bug> c = new Comparator<Bug>() {
            public int compare(Bug o1, Bug o2) {
                return Double.compare(o1.getFMeasure(), o2.getFMeasure());
            }
        };

        Collections.sort(this.population, c);
        if (population.size() > this.bugsLimitNumber) {
            this.purgeBugs();
        }
        Collections.reverse(this.population);

        reproduce(this.population);

        this.cicleNumber++;
        this.printCount++;

        this.changeNVariables = false;

    }

    public void purgeBugs() {
        try {
            if (this.population.size() > this.bugsLimitNumber) {
                for (int i = 0; i < this.bugsLimitNumber - this.population.size(); i++) {
                    Bug b = population.get(i);
                    if (b.getRows().size() > 1) {
                        b.kill();
                    }
                }
            }
        } catch (Exception e) {
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

    public void restart(int newRange) {
        if (newRange == this.trainingDataset.getNumberCols()) {
            this.range = this.getSamples();
        } else {
            this.range = this.getRandomSample(newRange);
        }
        for (Bug b : this.population) {
            b.setNewRange(range,
                this.trainingDataset);
        }
    }

    public void reproduce(List<Bug> bugsInside) {
        List<Reproduction> allThreads = new ArrayList<>();
        try {
            for (int j = 0; j < 50; j++) {
                Reproduction thread = new Reproduction(bugsInside, this.population, rand, this.trainingDataset, this.bugLife, this.maxVariables, this.range);
                allThreads.add(thread);
                thread.run();
            }

            while (!allThreads.isEmpty()) {
                Iterator<Reproduction> ite = allThreads.iterator();
                while (ite.hasNext()) {
                    Reproduction thread = ite.next();
                    if (!thread.isAlive()) {
                        ite.remove();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Something failed during reproduction");
            e.printStackTrace();
        }
     //   Bug mother = bugsInside.get(this.rand.nextInt(bugsInside.size()));
        // Bug father = bugsInside.get(this.rand.nextInt(bugsInside.size()));
        //System.out.println("Reproducing: Mother= " + mother.toString()+ " Father= "+ father.toString());
        //  Thread reproduce = new Thread(new Runnable() {
        //    public void run() {
        //      if (!mother.isSameBug(father)) {
        //        population.add(new Bug(father, mother, trainingDataset, bugLife, maxVariables, range));
        //  }
        //  }
        //});
        //reproduce.start();
    }

    private Set<Integer> getSamples() {
        Set<Integer> sampleSet = new LinkedHashSet<Integer>();
        for (int i = 0; i < this.trainingDataset.getNumberCols(); i++) {
            sampleSet.add(i);
        }
        return sampleSet;
    }
}
