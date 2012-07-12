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
package alvs.data.impl.datasets;

import alvs.data.impl.*;
import alvs.data.DatasetType;
import alvs.data.BugDataset;
import alvs.data.PeakListRow;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Basic data set implementation.
 *
 * @author SCSANDRA
 */
public class SimpleBasicDataset implements BugDataset {

    String datasetName;
    Vector<PeakListRow> peakList;
    Vector<String> columnNames;
    protected DatasetType type;
    String infoDataset = "";
    private Hashtable<String, SampleDescription> parameters;
    private Vector<String> parameterNames;
    private int ID;
    private int numberRows = 0;
    Hashtable<String, String> sampleType;

    /**
     *
     * @param datasetName Name of the data set
     */
    public SimpleBasicDataset(String datasetName) {
        this.datasetName = datasetName;
        this.peakList = new Vector<PeakListRow>();
        this.columnNames = new Vector<String>();
        this.parameters = new Hashtable<String, SampleDescription>();
        this.parameterNames = new Vector<String>();
        this.sampleType = new Hashtable<String, String>();        
    }

    /**
     * Returns true when the list of column names contain the parameter <b>columnName</b>.
     *
     * @param columnName Name of the column to be searched
     * @return true or false depending if the parameter <b>columnName</b> is in the name of any column
     */
    public boolean containtName(String columnName) {
        for (String name : this.columnNames) {
            if (name.compareTo(columnName) == 0) {
                return true;
            }
            if (name.matches(".*" + columnName + ".*")) {
                return true;
            }
            if (columnName.matches(".*" + name + ".*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true when any row of the data set contains the String <b>str</b> into a
     * column called "Name"
     *
     * @param str
     * @return true or false depending if the parameter <b>str</b> is in the column called "Name"
     */
    public boolean containRowName(String srt) {
        for (PeakListRow row : this.getRows()) {
            if (((String) row.getPeak("Name")).contains(srt) || srt.contains((CharSequence) row.getPeak("Name"))) {
                return true;
            }
        }
        return false;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void addParameterValue(String experimentName, String parameterName, String parameterValue) {
        if (parameters.containsKey(experimentName)) {
            SampleDescription p = parameters.get(experimentName);
            p.addParameter(parameterName, parameterValue);
        } else {
            SampleDescription p = new SampleDescription();
            p.addParameter(parameterName, parameterValue);
            parameters.put(experimentName, p);
        }
        if (!this.parameterNames.contains(parameterName)) {
            parameterNames.addElement(parameterName);
        }
    }

    public void deleteParameter(String parameterName) {
        for (String experimentName : columnNames) {
            if (parameters.containsKey(experimentName)) {
                SampleDescription p = parameters.get(experimentName);
                p.deleteParameter(parameterName);
            }
        }
        this.parameterNames.remove(parameterName);
    }

    public String getParametersValue(String experimentName, String parameterName) {
        if (parameters.containsKey(experimentName)) {
            SampleDescription p = parameters.get(experimentName);
            return p.getParameter(parameterName);
        } else {
            return null;
        }
    }

    public Vector<String> getParameterAvailableValues(String parameter) {
        Vector<String> availableParameterValues = new Vector<String>();
        for (String rawDataFile : this.getAllColumnNames()) {
            String paramValue = this.getParametersValue(rawDataFile, parameter);
            if (!availableParameterValues.contains(paramValue)) {
                availableParameterValues.add(paramValue);
            }
        }
        return availableParameterValues;
    }

    public Vector<String> getParametersName() {
        return parameterNames;
    }

    public String getDatasetName() {
        return this.datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void addRow(PeakListRow peakListRow) {
        this.peakList.addElement(peakListRow);
    }

    public void addColumnName(String nameExperiment) {
        this.columnNames.addElement(nameExperiment);
    }

    public PeakListRow getRow(int i) {
        return (PeakListRow) this.peakList.elementAt(i);
    }

    public Vector<PeakListRow> getRows() {
        return this.peakList;
    }

    public int getNumberRows() {
        return this.peakList.size();
    }

    public int getNumberRowsdb() {
        return this.numberRows;
    }

    public void setNumberRows(int numberRows) {
        this.numberRows = numberRows;
    }

    public int getNumberCols() {
        return this.columnNames.size();
    }

    public Vector<String> getAllColumnNames() {
        return this.columnNames;
    }

    public void setNameExperiments(Vector<String> experimentNames) {
        this.columnNames = experimentNames;
    }

    public DatasetType getType() {
        return type;
    }

    public void setType(DatasetType type) {
        this.type = type;
    }

    public void removeRow(PeakListRow row) {
        try {
            this.peakList.removeElement(row);

        } catch (Exception e) {
            System.out.println("No row found");
        }
    }

    public void addColumnName(String nameExperiment, int position) {
        this.columnNames.insertElementAt(datasetName, position);
    }

    public String getInfo() {
        return infoDataset;
    }

    public void setInfo(String info) {
        this.infoDataset = info;
    }

    @Override
    public SimpleBasicDataset clone() {
        SimpleBasicDataset newDataset = new SimpleBasicDataset(this.datasetName);
        for (String experimentName : this.columnNames) {
            newDataset.addColumnName(experimentName);
        }
        for (PeakListRow peakListRow : this.peakList) {
            newDataset.addRow(peakListRow.clone());
        }
        newDataset.setType(this.type);
        return newDataset;
    }

    public void setSampleType(String sampleName, String type) {            
        this.sampleType.put(sampleName, type);
    }

    public String getSampleType(String sampleName) {
        return sampleType.get(sampleName);
    }
}

       
