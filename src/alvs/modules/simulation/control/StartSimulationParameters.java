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

import alvs.data.Parameter;
import alvs.data.ParameterType;
import alvs.data.impl.SimpleParameter;
import alvs.data.impl.SimpleParameterSet;
import alvs.modules.simulation.classifiersEnum;

public class StartSimulationParameters extends SimpleParameterSet {

        public static final Parameter iterations = new SimpleParameter(
                ParameterType.INTEGER, "Number of iterations",
                "Introduce the number of iterations", new Integer(1000));
        public static final Parameter worldSize = new SimpleParameter(
                ParameterType.INTEGER, "Size of the world",
                "Introduce the size of each side of the world", new Integer(100));
        public static final Parameter bugLimit = new SimpleParameter(
                ParameterType.INTEGER, "Max number of bugs",
                "Maximum number of bugs living in the world", new Integer(1500));
        public static final Parameter numberOfBugs = new SimpleParameter(
                ParameterType.INTEGER, "Number of copies of bugs",
                "Introduce the number of copies of variables", new Integer(3));
        public static final Parameter bugLife = new SimpleParameter(
                ParameterType.INTEGER, "Life of the Bugs",
                "Minimum number of cicles that a bug can live", new Integer(300));
        public static final Parameter classifier = new SimpleParameter(
                ParameterType.STRING, "Classifier",
                "Select the classifier", null, classifiersEnum.values());
        public static final Parameter stoppingCriteria = new SimpleParameter(
                ParameterType.INTEGER, "Stopping criteria (%)",
                "% of variables in living in the world", new Integer(30));
        public static final Parameter repProbability = new SimpleParameter(
                ParameterType.DOUBLE, "Reproduction probability",
                "Probability of reproduction when 2 bugs are compatible", new Double(0.5));
        public static final Parameter numberOfVariables = new SimpleParameter(
                ParameterType.INTEGER, "Number of variables",
                "Maximum number of Variables. Set \"-1\" if the selection has to be done automatically", new Integer(-1));

        public StartSimulationParameters() {
                super(new Parameter[]{iterations, worldSize, bugLimit, numberOfBugs, bugLife, classifier, stoppingCriteria, repProbability, numberOfVariables});
        }
}
