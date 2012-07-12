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
package alvs.data.impl.peaklists.mylly;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jmjarkko
 */
public class GCGCDatum implements Cloneable, Comparable<GCGCDatum>, Peak {

    public final static String UNKOWN_NAME = "Unknown";
    public final static double NO_QUANT_MASS = -1.0;
    private final static double DEFAULT_RI = 0.0;
    private static GCGCDatum GAP;
    private final static Pattern p = Pattern.compile("(Unknown\\s(\\d*))|Not Found");
    private double rt1;
    private double rt2;
    private double area;
    private double concentration;
    private double rti;
    private double quantMass;
    private int similarity;
    private int id;
    private boolean identified;
    private String name,  CAS;
    private Spectrum spectrum;
    private boolean useConc = false;
    private String columnName;

    public GCGCDatum() {
    }

    public GCGCDatum(GCGCDatum source) {
        this.rt1 = source.rt1;
        this.rt2 = source.rt2;
        this.area = source.area;
        this.concentration = source.concentration;
        this.CAS = source.CAS;
        this.name = source.name;
        this.quantMass = source.quantMass;
        this.similarity = source.similarity;
        this.rti = source.rti;
        this.id = source.id;
        this.identified = source.identified;
        this.spectrum = source.spectrum;
        this.useConc = source.useConc;
        this.columnName = source.columnName;
    }

    synchronized public static GCGCDatum getGAP() {
        if (GAP == null) {
            GAP = new GCGCDatum(0, 0.0, 0.0, 0.0, NO_QUANT_MASS, 0, 0.0, 0.0, new Boolean(false), " ", "GAP", "", null);
        }
        return GAP;
    }
    public boolean isGap() {
        return this == getGAP();
    }

    public GCGCDatum(int id, double rt1, double rt2, double retentionIndex,
            double quantMass, int similarity, double area,
            double concentration, boolean useConc,
            String CAS, String name, String columnName,
            List<? extends Pair<Integer, Integer>> peakList) {
        this.id = id;
        this.rt1 = rt1;
        this.rt2 = rt2;
        this.area = area;
        this.concentration = concentration;
        this.quantMass = quantMass;
        this.similarity = similarity;
        this.rti = retentionIndex;
        this.useConc = useConc;
        verifyValues();
        this.CAS = CAS;
        this.columnName = columnName;
        Matcher m = p.matcher(name);
        if (m.matches()) {
            this.name = UNKOWN_NAME;
            identified = false;
        } else {
            this.name = name;
            identified = true;
        }
        if (peakList != null) {
            this.spectrum = new Spectrum(peakList);
        } else {
            this.spectrum = Spectrum.getNullSpectrum();
        }
    }

    public GCGCDatum(int id, double rt1, double rt2, double quantMass,
            double area, double concentration, boolean useConc, int similarity, String CAS, String name, String columnName, List<? extends Pair<Integer, Integer>> spectrum) {
        this(id, rt1, rt2, DEFAULT_RI, quantMass, similarity, area, concentration, useConc, CAS, name, columnName, spectrum);
    }

    @Override
    public GCGCDatum clone() {
        GCGCDatum d = new GCGCDatum();
        d.rt1 = rt1;
        d.rt2 = rt2;
        d.rti = rti;
        d.quantMass = quantMass;
        d.similarity = similarity;
        d.area = area;
        d.concentration = concentration;
        d.CAS = CAS;
        d.name = name;
        d.id = id;
        d.spectrum = (spectrum == null) ? null : spectrum.clone();
        d.useConc = useConc;
        d.columnName = columnName;
        d.identified = identified;
        return d;
    }

    private void verifyValues() {
        StringBuilder sb = null;
        boolean failureFound = false;
        double[] fieldValues = {rt1, rt2, area, rti, similarity};
        String[] fieldNames = {"rt1", "rt2", "area", "rti", "similarity"};
        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i] < 0) {
                if (!failureFound) {
                    sb = new StringBuilder("Following values were invalid:");
                    failureFound = !failureFound;
                }
                sb.append(' ').append(fieldNames[i]).append(" was ").append(fieldValues[i]);
                sb.append(" (should be > 0)");
            }
        }
        if (failureFound) {
            throw new IllegalArgumentException(sb.toString());
        }
    }

    public double getRT1() {
        return rt1;
    }

    public void setRT1(double newRT1) {
        this.rt1 = newRT1;
    }

    public double getRT2() {
        return rt2;
    }

    public void setRT2(double newRT2) {
        this.rt2 = newRT2;
    }

    public double getRTI() {
        return rti;
    }

    public void setRTI(double newRTI) {
        this.rti = newRTI;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double newArea) {
        this.area = newArea;
    }

    public double getConcentration() {
        return concentration;
    }

    public void setConcentration(double newConcentration) {
        this.concentration = newConcentration;
    }

    public boolean useConcentration() {
        return useConc;
    }

    public void setUseConcentration(boolean newUseConcentration) {
        useConc = newUseConcentration;
    }

    public int getSimilarity() {
        return similarity;
    }

    public void setSimilarity(int newSimilarity) {
        this.similarity = newSimilarity;
    }

    public boolean isIdentified() {
        return identified;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getCAS() {
        return CAS;
    }

    public void setCAS(String newCAS) {
        this.CAS = newCAS;
    }

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public boolean hasQuantMass() {
        return (quantMass != NO_QUANT_MASS);
    }

    public double getQuantMass() {
        return quantMass;
    }

    public int hashCode() {
        long mod = (long) Integer.MAX_VALUE - (long) Integer.MIN_VALUE;
        int hashcode = (int) (id % mod);
        return hashcode;
    }

    public boolean matches(GCGCDatum other) {
        boolean matches = identified && other.identified;
        if (matches) {
            matches = name.equals(other.name);
        }
        return matches;
    }

    public String toFileString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(rt1).append('\t');
        sb.append(rt2).append('\t');
        if (useConc) {
            sb.append(concentration);
        } else {
            sb.append(area);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(" RT(").append(rt1).append(',').append(rt2).append(')');
        if (quantMass >= 0) {
            sb.append(" Quant mass ").append(quantMass);
        }
        sb.append(" RTI ").append(rti);


        return sb.toString();
    }

    /**
     * Objects are equal if their identifier is equal.
     */
    public boolean equals(Object obj) {
        if (obj instanceof GCGCDatum) {
            return (((GCGCDatum) obj).id == id);
        }
        return false;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(GCGCDatum o) {
        int comparison = 0;
        if (rt1 < o.rt1) {
            comparison = -1;
        } else if (rt1 > o.rt1) {
            comparison = 1;
        }
        if (comparison == 0) {
            if (rt2 < o.rt2) {
                comparison = -1;
            } else if (rt2 > o.rt2) {
                comparison = 1;
            }
        }
        if (comparison == 0) {
            if (rti < o.rti) {
                comparison = -1;
            } else if (rti > o.rti) {
                comparison = 1;
            }
        }
        if (comparison == 0) {
            if (area < o.area) {
                comparison = -1;
            } else if (area > o.area) {
                comparison = 1;
            }
        }
        if (comparison == 0) {
            if (concentration < o.concentration) {
                comparison = -1;
            } else if (concentration > o.concentration) {
                comparison = 1;
            }
        }
        if (comparison == 0) {
            if (similarity < o.similarity) {
                comparison = -1;
            } else if (similarity > o.similarity) {
                comparison = 1;
            }
        }
        if (comparison == 0) {
            if (!identified && o.identified) {
                comparison = -1;
            } else if (identified && !o.identified) {
                comparison = 1;
            }
        }
        if (comparison == 0) {
            comparison = name.compareTo(o.name);
        }
        if (comparison == 0) {
            comparison = CAS.compareTo(o.CAS);
        }
        return comparison;
    }

    /* (non-Javadoc)
     * @see gcgcaligner.Peak#matchesWithName(gcgcaligner.Peak)
     */
    public boolean matchesWithName(Peak p) {
        if (UNKOWN_NAME.equals(name)) {
            return false;
        }
        for (String peakName : p.names()) {
            if (name.equals(peakName)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see gcgcaligner.Peak#names()
     */
    public List<String> names() {
        ArrayList<String> names = new ArrayList<String>();
        names.add(name);
        return names;
    }

    public int getId() {
        return id;
    }

    public GCGCDatum combineWith(GCGCDatum other) {
        if (other == null) {
            return this;
        }

        double newRT1 = (rt1 + other.rt1) / 2;
        double newRT2 = (rt2 + other.rt2) / 2;
        double newRTI = (rti + other.rti) / 2;
        double newArea = area + other.area;
        double newConcentration = concentration + other.concentration;
        boolean newUseConc = useConc;
        double newQuantMass = quantMass; //Quantified masses should be same!
        int newSimilarity = Math.max(similarity, other.similarity);
        boolean newIdentified = identified || other.identified;
        String newCAS = CAS;
        String newName = name;
        String newColumnName = columnName;
        Spectrum newSpectrum;
        if (spectrum == null) {
            newSpectrum = other.spectrum == null ? null : other.spectrum.clone();
        } else {
            newSpectrum = spectrum.combineWith(other.spectrum);
        }
        GCGCDatum newDatum = new GCGCDatum(id, newRT1, newRT2, newRTI,
                newQuantMass, newSimilarity, newArea, newConcentration, newUseConc, newCAS, newName, newColumnName, null);
        newDatum.spectrum = newSpectrum;
        newDatum.identified = newIdentified;
        if (useConc) {
            newDatum.useConc = true;
        }
        return newDatum;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
