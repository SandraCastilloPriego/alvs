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
package alvs.data.impl;

import alvs.data.Parameter;
import alvs.data.ParameterType;
import alvs.data.StorableParameterSet;
import alvs.util.Range;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;



import org.dom4j.Element;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 * 
 * Simple storage for the parameters and their values. Typical module will
 * inherit this class and define the parameters for the constructor.
 */
public class SimpleParameterSet implements StorableParameterSet {

        public static final String PARAMETER_ELEMENT_NAME = "parameter";
        public static final String PARAMETER_NAME_ATTRIBUTE = "name";
        public static final String PARAMETER_TYPE_ATTRIBUTE = "type";
        // Parameters
        private Parameter parameters[];
        // Parameter -> value
        private Hashtable<Parameter, Object> values;
        // Multiple selection parameter -> multiple values (array of possible
        // values)
        private Hashtable<Parameter, Object[]> multipleSelectionValues;

        /**
         * This constructor is only used for cloning
         */
        private SimpleParameterSet() {
                this(new Parameter[0]);
        }

        /**
         * Checks if project contains current value for some of the parameters, and
         * initializes this object using those values if present.
         *
         */
        public SimpleParameterSet(Parameter[] initParameters) {
                this.parameters = initParameters;
                values = new Hashtable<Parameter, Object>();
                multipleSelectionValues = new Hashtable<Parameter, Object[]>();
        }

        public Parameter[] getParameters() {
                return parameters;
        }

        public Parameter getParameter(String name) {
                for (Parameter p : parameters) {
                        if (p.getName().equals(name)) {
                                return p;
                        }
                }
                return null;
        }

        public Object getParameterValue(Parameter parameter) {
                Object value = values.get(parameter);
                if (value == null) {
                        value = parameter.getDefaultValue();
                }
                if (value == null) {
                        if (parameter.getType() == ParameterType.MULTIPLE_SELECTION) {
                                return new Object[0];
                        }
                }
                return value;
        }

        public void setMultipleSelection(Parameter parameter,
                Object[] selectionArray) {
                assert parameter.getType() == ParameterType.MULTIPLE_SELECTION;
                if (selectionArray == null) {
                        selectionArray = new Object[0];
                }
                multipleSelectionValues.put(parameter, selectionArray);
        }

        public Object[] getMultipleSelection(Parameter parameter) {
                return multipleSelectionValues.get(parameter);
        }

        public void setParameterValue(Parameter parameter, Object value)
                throws IllegalArgumentException {

                Object possibleValues[] = parameter.getPossibleValues();

                if (value instanceof String[]) {
                        parameter.setPossibleValues((String[]) value);
                        return;
                }

                if ((possibleValues != null) && (parameter.getType() != ParameterType.MULTIPLE_SELECTION) && (parameter.getType() != ParameterType.ORDERED_LIST)) {
                        for (Object possibleValue : possibleValues) {
                                // We compare String version of the values, in case some values
                                // were specified as Enum constants
                                if (possibleValue.toString().equals(value.toString())) {
                                        values.put(parameter, possibleValue);
                                        return;
                                }
                        }
                        // Value not found
                        throw (new IllegalArgumentException("Invalid value " + value + " for parameter " + parameter));

                }

                switch (parameter.getType()) {
                        case BOOLEAN:
                                if (!(value instanceof Boolean)) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }
                                break;
                        case INTEGER:
                                if (!(value instanceof Integer)) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }
                                int intValue = (Integer) value;
                                Integer minIValue = (Integer) parameter.getMinimumValue();
                                if ((minIValue != null) && (intValue < minIValue)) {
                                        throw (new IllegalArgumentException(
                                                "Minimum value of parameter " + parameter + " is " + minIValue));
                                }
                                Integer maxIValue = (Integer) parameter.getMaximumValue();
                                if ((maxIValue != null) && (intValue > maxIValue)) {
                                        throw (new IllegalArgumentException(
                                                "Maximum value of parameter " + parameter + "  is " + maxIValue));
                                }

                                break;

                        case DOUBLE:
                                if (!(value instanceof Double)) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }
                                double doubleValue = (Double) value;
                                Double minFValue = (Double) parameter.getMinimumValue();
                                if ((minFValue != null) && (doubleValue < minFValue)) {
                                        throw (new IllegalArgumentException(
                                                "Minimum value of parameter " + parameter + "  is " + minFValue));
                                }
                                Double maxFValue = (Double) parameter.getMaximumValue();
                                if ((maxFValue != null) && (doubleValue > maxFValue)) {
                                        throw (new IllegalArgumentException(
                                                "Maximum value of parameter " + parameter + "  is " + maxFValue));
                                }
                                break;

                        case RANGE:
                                if (!(value instanceof Range)) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }
                                Range rangeValue = (Range) value;
                                Double minRValue = (Double) parameter.getMinimumValue();
                                if ((minRValue != null) && (rangeValue.getMin() < minRValue)) {
                                        throw (new IllegalArgumentException(
                                                "Minimum value of parameter " + parameter + "  is " + minRValue));
                                }
                                Double maxRValue = (Double) parameter.getMaximumValue();
                                if ((maxRValue != null) && (rangeValue.getMax() > maxRValue)) {
                                        throw (new IllegalArgumentException(
                                                "Maximum value of parameter " + parameter + "  is " + maxRValue));
                                }
                                break;

                        case STRING:
                                if (!(value instanceof String)) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }

                                break;
                        case TEXTAREA:
                                if (!(value instanceof String)) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }

                                break;

                        case MULTIPLE_SELECTION:
                                if (!value.getClass().isArray()) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }
                                Object valueArray[] = (Object[]) value;
                                if (parameter.getMinimumValue() != null) {
                                        int min = (Integer) parameter.getMinimumValue();
                                        if (valueArray.length < min) {
                                                throw (new IllegalArgumentException(
                                                        "Please select minimum " + min + " values for parameter " + parameter));
                                        }
                                }
                                if (parameter.getMaximumValue() != null) {
                                        int max = (Integer) parameter.getMaximumValue();
                                        if (valueArray.length > max) {
                                                throw (new IllegalArgumentException(
                                                        "Please select maximum " + max + " values for parameter " + parameter));
                                        }
                                }
                                break;

                        case FILE_NAME:
                                if (!(value instanceof String)) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }
                                break;

                        case ORDERED_LIST:
                                if (!value.getClass().isArray()) {
                                        throw (new IllegalArgumentException("Value type mismatch"));
                                }
                                break;

                }

                values.put(parameter, value);
        }

        public void exportValuesToXML(Element element) {

                for (Parameter p : parameters) {

                        Element newElement = element.addElement(PARAMETER_ELEMENT_NAME);

                        newElement.addAttribute(PARAMETER_NAME_ATTRIBUTE, p.getName());
                        newElement.addAttribute(PARAMETER_TYPE_ATTRIBUTE,
                                p.getType().toString());

                        if ((p.getType() == ParameterType.MULTIPLE_SELECTION) || (p.getType() == ParameterType.ORDERED_LIST)) {
                                Object[] parameterValues = (Object[]) getParameterValue(p);
                                if (parameterValues != null) {
                                        String valueAsString = "";
                                        for (int i = 0; i < parameterValues.length; i++) {
                                                if (i == parameterValues.length - 1) {
                                                        valueAsString += String.valueOf(parameterValues[i]);
                                                } else {
                                                        valueAsString += String.valueOf(parameterValues[i]) + ",";
                                                }
                                        }
                                        newElement.addText(valueAsString);
                                }
                        } else {
                                Object value = getParameterValue(p);
                                if (value != null) {
                                        String valueAsString;
                                        if (value instanceof Range) {
                                                Range rangeValue = (Range) value;
                                                valueAsString = String.valueOf(rangeValue.getMin()) + "-" + String.valueOf(rangeValue.getMax());
                                        } else {
                                                valueAsString = value.toString();
                                        }
                                        newElement.addText(valueAsString);

                                }
                        }

                }

        }

        public void importValuesFromXML(Element element) {

                Iterator paramIter = element.elementIterator(PARAMETER_ELEMENT_NAME);

                while (paramIter.hasNext()) {
                        Element paramElem = (Element) paramIter.next();

                        Parameter param = getParameter(paramElem.attributeValue(PARAMETER_NAME_ATTRIBUTE));

                        if (param == null) {
                                continue;
                        }

                        ParameterType paramType = ParameterType.valueOf(paramElem.attributeValue(PARAMETER_TYPE_ATTRIBUTE));
                        String valueText = paramElem.getText();

                        if ((valueText == null) || (valueText.length() == 0)) {
                                continue;
                        }

                        Object value = null;
                        switch (paramType) {
                                case BOOLEAN:
                                        value = Boolean.parseBoolean(valueText);
                                        break;
                                case INTEGER:
                                        value = Integer.parseInt(valueText);
                                        break;
                                case DOUBLE:
                                        value = Double.parseDouble(valueText);
                                        break;
                                case RANGE:
                                        String values[] = valueText.split("-");
                                        double min = Double.parseDouble(values[0]);
                                        double max = Double.parseDouble(values[1]);
                                        value = new Range(min, max);
                                        break;
                                case STRING:
                                        value = valueText;
                                        break;
                                case TEXTAREA:
                                        value = valueText;
                                        break;
                                case MULTIPLE_SELECTION:
                                        String stringMultipleValues[] = valueText.split(",");
                                        Object possibleMultipleValues[] = param.getPossibleValues();
                                        if (possibleMultipleValues == null) {
                                                continue;
                                        }
                                        Vector<Object> multipleValues = new Vector<Object>();

                                        for (int i = 0; i < stringMultipleValues.length; i++) {
                                                for (int j = 0; j < possibleMultipleValues.length; j++) {
                                                        if (stringMultipleValues[i].equals(String.valueOf(possibleMultipleValues[j]))) {
                                                                multipleValues.add(possibleMultipleValues[j]);
                                                        }
                                                }
                                        }
                                        value = multipleValues.toArray();

                                        break;
                                case FILE_NAME:
                                        value = valueText;
                                        break;
                                case ORDERED_LIST:
                                        String stringValues[] = valueText.split(",");
                                        Object possibleValues[] = param.getPossibleValues();
                                        Object orderedValues[] = new Object[stringValues.length];
                                        for (int i = 0; i < stringValues.length; i++) {
                                                for (int j = 0; j < possibleValues.length; j++) {
                                                        if (stringValues[i].equals(String.valueOf(possibleValues[j]))) {
                                                                orderedValues[i] = possibleValues[j];
                                                        }
                                                }
                                        }
                                        value = orderedValues;
                                        break;
                        }

                        try {
                                setParameterValue(param, value);
                        } catch (IllegalArgumentException e) {
                                // ignore
                        }

                }

        }

        public SimpleParameterSet clone() {

                try {
                        // do not make a new instance of SimpleParameterSet, but instead
                        // clone the runtime class of this instance - runtime type may be
                        // inherited class

                        SimpleParameterSet newSet = this.getClass().newInstance();
                        newSet.parameters = this.parameters;

                        for (Parameter p : parameters) {
                                Object v = values.get(p);
                                if (v != null) {
                                        newSet.setParameterValue(p, v);
                                }
                        }
                        return newSet;
                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }

        }

        /**
         * Represent method's parameters and their values in human-readable format
         */
        @Override
        public String toString() {
                StringBuffer s = new StringBuffer();
                for (Parameter p : getParameters()) {
                        s = s.append(p.getName() + ": " + values.get(p) + ", ");
                }
                return s.toString();
        }
}
