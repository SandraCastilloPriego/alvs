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
package alvs.util.components;

import alvs.data.BugDataset;
import alvs.data.PeakListRow;
import alvs.data.datamodels.OtherDataModel;
import alvs.data.DatasetType;
import alvs.data.impl.datasets.SimpleBasicDataset;
import alvs.data.impl.peaklists.SimplePeakListRowOther;
import alvs.util.Tables.DataTableModel;

/**
 *
 * @author scsandra
 */
public class FileUtils {

    public static PeakListRow getPeakListRow(DatasetType type) {
        switch (type) {           
            case TRAINING:
            case VALIDATION:
                return new SimplePeakListRowOther();
        }
        return null;
    }

    public static BugDataset getDataset(BugDataset dataset, String Name) {
        BugDataset newDataset = null;
        switch (dataset.getType()) {           
            case TRAINING:
            case VALIDATION:
                newDataset = new SimpleBasicDataset(Name + dataset.getDatasetName());
                break;
        }
        newDataset.setType(dataset.getType());
        return newDataset;
    }

    public static DataTableModel getTableModel(BugDataset dataset) {
        DataTableModel model = null;
        switch (dataset.getType()) {           
            case TRAINING:
            case VALIDATION:
                model = new OtherDataModel(dataset);
                break;
        }
        return model;
    }
}
