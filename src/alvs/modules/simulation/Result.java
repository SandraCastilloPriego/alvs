package alvs.modules.simulation;

import java.util.ArrayList;
import java.util.List;

public class Result {

        public String Classifier;
        public List<String> values;
        public double tsensitivity, tspecificity, vsensitivity, vspecificity;
        public double fScore, aucV;
        public int count = 1;

        public Result() {
                this.values = new ArrayList<String>();
        }

        public void addValue(String value) {
                this.values.add(value);
        }

        public List<String> getValues() {
                return this.values;
        }

        public boolean isIt(List<String> values2, String classifier) {
                if (values2.size() != this.values.size()) {
                        return false;
                }
                for (String val : values2) {
                        if (!this.values.contains(val)) {
                                return false;
                        }
                }
                if (classifier.equals(Classifier)) {
                        return true;
                } else {
                        return false;
                }
        }
        
        public int getCount(){
                return this.count;
        }

        public void count() {
                count++;
        }

        @Override
        public String toString() {
                 String text = Classifier + " Training - Specificity: " + tspecificity + " - Sensitivity: " + tsensitivity + " - F-score: " + fScore + " - Count: " + count + "\n";
                 text += "Validation - " + vspecificity + " - " + vsensitivity + " - " + aucV + " \n";

                for (String val : values) {
                        text += val + " - ";
                }
                text += "\n ----------------------------------------------------------\n";
                return text;
        }
}
