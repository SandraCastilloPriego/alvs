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
        Cell[][] cells;
        List<Bug> population;
        Random rand;
        int cellsPerSide;
        int numberOfCells;
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

        public World(BugDataset training, BugDataset validation, int cellsPerSide, Range range,
                int numberOfBugsCopies, int bugLife, JTextArea text,
                int bugsLimitNumber, int maxVariables, classifiersEnum classifier, double repProbability) {
                this.trainingDataset = training;
                this.validationDataset = validation;
                this.cellsPerSide = cellsPerSide;
                this.numberOfCells = cellsPerSide * cellsPerSide;
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
                                cells = new Cell[cellsPerSide][cellsPerSide];
                                for (int i = 0; i < cellsPerSide; i++) {
                                        cells[i] = new Cell[cellsPerSide];
                                        for (int j = 0; j < cells[i].length; j++) {
                                                cells[i][j] = new Cell(bugLife, maxVariables, repProbability);
                                                this.setSamplesInCell(training.getAllColumnNames(), cells[i][j], range);
                                        }
                                }


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

        private void setSamplesInCell(Vector<String> samplesNames, Cell cell, Range range) {
                int pos = range.getRandom();
                String name = samplesNames.elementAt(pos);
                cell.setParameters(name, range, trainingDataset.getSampleType(name));
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
                boolean isInside = true;
                int cont = 0;
                while (isInside) {
                        int X = this.rand.nextInt(this.cellsPerSide - 1);
                        int Y = this.rand.nextInt(this.cellsPerSide - 1);
                        Bug bug = new Bug(X, Y, cells[X][Y], row, trainingDataset, bugLife, maxVariables, classifier);
                        bug.eval();
                        cells[X][Y].addBug(bug);
                        this.population.add(bug);
                        isInside = false;
                        cont++;
                        if (cont > numberOfCells) {
                                break;
                        }
                }
        }

        public int getMaxVariables() {
                return this.maxVariables;
        }

        public void cicle() {
                movement();
                eat();

                for (Cell[] cellArray : cells) {
                        for (Cell cell : cellArray) {
                                if (this.changeNVariables) {
                                        cell.setMaxVariable(maxVariables);
                                }
                                List<Bug[]> childs = cell.reproduction();
                                if (childs != null) {
                                        for (Bug[] parents : childs) {                                              
                                                Bug child = new Bug(parents[0], parents[1], this.trainingDataset, this.bugLife, this.maxVariables);
                                                cell.addBug(child);
                                                this.population.add(child);
                                        }
                                }
                        }
                }

                death();

                if (population.size() > this.bugsLimitNumber) {
                        this.purgeBugs();
                }

                this.cicleNumber++;
                this.printCount++;

                this.changeNVariables = false;

        }

        private synchronized void movement() {
                for (Bug bug : population) {
                        try {
                                int direction = rand.nextInt(8);

                                int x = bug.getx();
                                int y = bug.gety();

                                switch (direction) {
                                        case 0:
                                                this.setBugPosition(bug, x + jump, y);
                                                break;
                                        case 1:
                                                this.setBugPosition(bug, x, y);
                                                break;
                                        case 2:
                                                this.setBugPosition(bug, x, y + jump);
                                                break;
                                        case 3:
                                                this.setBugPosition(bug, x, y - jump);
                                                break;
                                        case 4:
                                                this.setBugPosition(bug, x + jump, y + jump);
                                                break;
                                        case 5:
                                                this.setBugPosition(bug, x + jump, y - jump);
                                                break;
                                        case 6:
                                                this.setBugPosition(bug, x - jump, y + jump);
                                                break;
                                        case 7:
                                                this.setBugPosition(bug, x - jump, y - jump);
                                                break;
                                }
                        } catch (Exception e) {
                        }
                }
        }

        private void setBugPosition(Bug bug, int newx, int newy) {
                if (newx > this.cellsPerSide - 1) {
                        newx = 1;
                } else if (newx < 0) {
                        newx = this.cellsPerSide - 1;
                }
                if (newy > this.cellsPerSide - 1) {
                        newy = 1;
                } else if (newy < 0) {
                        newy = this.cellsPerSide - 1;
                }
                bug.getCell().removeBug(bug);
                bug.setPosition(newx, newy);
                cells[newx][newy].addBug(bug);
                bug.setCell(cells[newx][newy]);

        }

        public int getWorldSize() {
                return this.cellsPerSide;
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
                                        this.cells[bug.getx()][bug.gety()].removeBug(bug);
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
                for (Cell[] cellArray : cells) {
                        for (Cell cell : cellArray) {
                                cell.setRange(newRange, this.trainingDataset);
                        }
                }

                this.addMoreBugs();
        }
       
}
