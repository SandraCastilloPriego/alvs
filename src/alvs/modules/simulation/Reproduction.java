/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alvs.modules.simulation;

import alvs.data.BugDataset;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author scsandra
 */
public class Reproduction extends Thread {

    private final List<Bug> bugsInside;
    List<Bug> population;
    private final Random rand;
    BugDataset trainingDataset;
    int bugLife;
    int maxVariables;
    Set<Integer> range;

    public Reproduction(List<Bug> bugsInside, List<Bug> population, Random rand, BugDataset trainingDataset, int bugLife, int maxVariables, Set<Integer> range) {
        this.bugsInside = bugsInside;
        this.population = population;
        this.rand = rand;
        this.bugLife = bugLife;
        this.maxVariables = maxVariables;
        this.range = range;
        this.trainingDataset = trainingDataset;
    }

    public void run() {
        Bug mother = bugsInside.get(this.rand.nextInt(bugsInside.size()));
        Bug father = bugsInside.get(this.rand.nextInt(bugsInside.size()));
        if (!mother.isSameBug(father)) {
            population.add(new Bug(father, mother, trainingDataset, bugLife, maxVariables, range));
        }
    }
}
