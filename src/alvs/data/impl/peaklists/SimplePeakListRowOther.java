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
package alvs.data.impl.peaklists;

import alvs.data.PeakListRow;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SCSANDRA
 */
public class SimplePeakListRowOther implements PeakListRow {

        private int ID;
        private int cluster;
        private boolean selection;
        private Hashtable<String, String> peaks;

        public SimplePeakListRowOther() {
                this.peaks = new Hashtable<String, String>();
                this.ID = -1;
        }

        @Override
        public PeakListRow clone() {
                PeakListRow peakListRow = new SimplePeakListRowOther();
                String str;
                Set<String> set = peaks.keySet();

                Iterator<String> itr = set.iterator();
                while (itr.hasNext()) {
                        str = itr.next();
                        peakListRow.setPeak(str, peaks.get(str));
                }
                return peakListRow;
        }

        public Double getPeak(String ExperimentName) {
                return Double.parseDouble(this.peaks.get(ExperimentName));
        }

        public void setPeak(String name, String value) {
                this.peaks.put(name, value);
        }

        public int getID() {
                return ID;
        }

        public void setID(int ID) {
                this.ID = ID;
        }

        public int getNumberPeaks() {
                return this.peaks.size();
        }

        public void removePeaks() {
                this.peaks = new Hashtable<String, String>();
        }

        public String[] getPeaks() {
                String[] aPeaks = new String[this.peaks.size()];
                String str;
                Set<String> set = peaks.keySet();
                int cont = 0;
                Iterator<String> itr = set.iterator();
                while (itr.hasNext()) {
                        str = itr.next();
                        aPeaks[cont++] = peaks.get(str);
                }
                return aPeaks;
        }

        public void setPeak(String name, Double value) {
        }

        public boolean isSelected() {
                return this.selection;
        }

        public void setSelectionMode(boolean selectionMode) {
                this.selection = selectionMode;
        }

        public void removeNoSamplePeaks(String[] group) {
                for (String name : group) {
                        if (this.peaks.containsKey(name)) {
                                this.peaks.remove(name);
                        }
                }
        }

        public Object getVar(String varName) {
                try {
                        Method m = this.getClass().getMethod(varName, new Class[]{});
                        return m.invoke(this);

                } catch (IllegalAccessException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
        }

        public void setVar(String varName, Object value) {
                try {
                        Class partypes[] = new Class[1];
                        if (value.getClass().toString().contains("Double")) {
                                partypes[0] = Double.TYPE;
                        } else if (value.getClass().toString().contains("Integer")) {
                                partypes[0] = Integer.TYPE;
                        } else if (value.getClass().toString().contains("String")) {
                                partypes[0] = String.class;
                        } else if (value.getClass().toString().contains("Boolean")) {
                                partypes[0] = Boolean.TYPE;
                        } else {
                                partypes[0] = Object.class;
                        }
                        Method m = this.getClass().getMethod(varName, partypes);
                        Object[] parameters = new Object[1];
                        parameters[0] = value;
                        m.invoke(this, parameters);

                } catch (IllegalAccessException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                        Logger.getLogger(SimplePeakListRowOther.class.getName()).log(Level.SEVERE, null, ex);
                }

        }

        public void setCluster(int cluster) {
                this.cluster = cluster;
        }

        public int getCluster() {
                return cluster;
        }
}
