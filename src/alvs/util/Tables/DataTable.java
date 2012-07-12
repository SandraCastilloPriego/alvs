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
package alvs.util.Tables;

import alvs.data.DatasetType;
import javax.swing.JTable;

/**
 *
 * @author scsandra
 */
public interface DataTable {

        /**
         * Changes the model of the table.
         *
         * @param model
         */
        public void createTable(DataTableModel model);

        /**
         * Sets the properties of the table: selection mode, tooltips, actions with keys..
         *
         */
        public void setTableProperties();

        /**
         * Returns the table.
         *
         * @return Table
         */
        public JTable getTable();

        /**
         * Formating of the numbers in the table depening on the data set type.
         *
         * @param type Type of dataset @see ALVS.data.DatasetType
         */
        public void formatNumbers(DatasetType type);

        /**
         * Formating of the numbers in certaing column
         *
         * @param column Column where the numbers will be formated
         */
        public void formatNumbers(int column);
}
