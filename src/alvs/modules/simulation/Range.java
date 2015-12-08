/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alvs.modules.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class Range {

        public List<Integer> range;

        public Range() {
                this.range = new ArrayList<>();
        }

        public void addValue(int value) {
                range.add(value);
        }
}
