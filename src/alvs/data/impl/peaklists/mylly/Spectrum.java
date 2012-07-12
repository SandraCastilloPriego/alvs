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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jmjarkko
 */
public class Spectrum implements Cloneable {

        private final static Comparator<Pair<Integer, Integer>> reverseIntensityComp = getRevIntensityComparator();
        private final static Comparator<Pair<Integer, Integer>> intensityComp = getIntensityComparator();

        public static enum SortingMode {

                REVERSEMASS, REVERSEINTENSITY, INTENSITY
        }
        final private static Spectrum nullSpectrum = new Spectrum();
        //First comes the fragment mass, then intensity
        protected int _intensities[];
        protected int _masses[];
        protected SortingMode _sortMode;

        protected Spectrum() {
                _intensities = new int[0];
                _masses = new int[0];
        }

        public Spectrum(String spectrum) {
                int[][] data = this.get_spectrum(spectrum);
                _intensities = new int[data.length];
                _masses = new int[data.length];


                for (int i = 0; i < data.length; i++) {
                        _intensities[i] = data[i][1];
                        _masses[i] = data[i][0];
                }

        }

        public int[][] get_spectrum(String spectrum) {

                if (spectrum == null) {
                        return null;
                }
                int[][] num = new int[spectrum.split(",").length][];

                for (int i = 0; i < num.length; i++) {
                        num[i] = new int[2];
                }
                Pattern sp = Pattern.compile("\\d\\d?\\d?\\d?");
                Matcher matcher = sp.matcher(spectrum);
                int i = 0;
                while (matcher.find()) {
                        num[i][0] = Integer.parseInt(spectrum.substring(matcher.start(), matcher.end()));
                        matcher.find();
                        num[i][1] = Integer.parseInt(spectrum.substring(matcher.start(), matcher.end()));
                        i++;
                }

                return num;
        }

        /**
         *
         * @param peaks first one of each pair should contain the mass and second the intensity
         */
        public Spectrum(List<? extends Pair<Integer, Integer>> peaks) {

                Pair<int[], int[]> temp = parseMassesAndIntensities(peaks);
                _masses = temp.getFirst();
                _intensities = temp.getSecond();
                this._sortMode = SortingMode.REVERSEINTENSITY;
        }

        protected <T extends Pair<Integer, Integer>> Pair<int[], int[]> parseMassesAndIntensities(List<T> peaks) {
                if (peaks.size() == 0) {
                        throw new IllegalArgumentException("Peaklist was of length 0");
                }
                ArrayList<Pair<Integer, Integer>> tempPeaks = new ArrayList<Pair<Integer, Integer>>(peaks);
                java.util.Collections.sort(tempPeaks, reverseIntensityComp);
                int zeroIntensityCount = 0;
                for (Pair<Integer, Integer> pair : tempPeaks) {
                        if (pair.getSecond() == 0) {
                                zeroIntensityCount++;
                        } else {
                                break;
                        }
                }
                int[] intensities = new int[tempPeaks.size() - zeroIntensityCount];
                int[] masses = new int[tempPeaks.size() - zeroIntensityCount];
                for (int i = zeroIntensityCount, j = 0; i < tempPeaks.size(); i++, j++) {
                        masses[j] = tempPeaks.get(i).getFirst();
                        intensities[j] = tempPeaks.get(i).getSecond();
                }

                return new Pair<int[], int[]>(masses, intensities);
        }

        public void sort(SortingMode mode) {
                if (mode != _sortMode) {
                        _sortMode = mode;
                        actualSort(_sortMode);
                }

        }

        protected void actualSort(SortingMode mode) {
                List<ComparablePair<Integer, Integer>> tempPeaks = getPeakList();
                if (mode == SortingMode.REVERSEMASS) {
                        java.util.Collections.sort(tempPeaks, java.util.Collections.reverseOrder());
                } else if (mode == SortingMode.REVERSEINTENSITY) {
                        java.util.Collections.sort(tempPeaks, reverseIntensityComp);
                } else if (mode == SortingMode.INTENSITY) {
                        java.util.Collections.sort(tempPeaks, intensityComp);
                }
                for (int i = 0; i < tempPeaks.size(); i++) {
                        _masses[i] = tempPeaks.get(i).getFirst();
                        _intensities[i] = tempPeaks.get(i).getSecond();
                }
        }

        public SortingMode getSortingMode() {
                return _sortMode;
        }

        protected int[] peakMasses() {
                return _masses;
        }

        protected int[] peakIntensities() {
                return _intensities;
        }

        public int[] getMasses() {
                return peakMasses();
        }

        public int[] getIntensities() {
                return peakIntensities();
        }

        @Override
        public Spectrum clone() {
                Spectrum s = new Spectrum();
                s._intensities = getIntensities().clone();
                s._masses = getMasses().clone();
                return s;
        }

        @Override
        public String toString() {
                List<? extends Pair<Integer, Integer>> peaks = getPeakList();
                if (peaks != null) {
                        java.util.Collections.sort(peaks, intensityComp);
                        int[] peakMasses = new int[peaks.size()];
                        int[] peakIntensities = new int[peaks.size()];

                        for (int i = 0; i < peaks.size(); i++) {
                                peakMasses[i] = peaks.get(i).getFirst();
                                peakIntensities[i] = peaks.get(i).getSecond();
                        }


                        if (peakIntensities == null || peakIntensities.length == 0) {
                                return "EMPTY";
                        } else {

                                StringBuilder sb = new StringBuilder();
                                sb.append("[ ");
                                for (int i = 0; i < peakMasses.length; i++) {
                                        sb.append(peakMasses[i]).append(":").append(peakIntensities[i]);
                                        if (i != peakMasses.length - 1) {
                                                sb.append(" , ");
                                        }
                                }
                                sb.append(" ]");
                                return sb.toString();
                        }
                }
                return "";
        }

        public int length() {
                return peakIntensities().length;
        }

        public boolean isNull() {
                return this == getNullSpectrum();
        }

        public List<ComparablePair<Integer, Integer>> getPeakList() {
                try {
                        int thisMasses[] = peakMasses();
                        int thisIntensities[] = peakIntensities();
                        ArrayList<ComparablePair<Integer, Integer>> returned = new ArrayList<ComparablePair<Integer, Integer>>(_masses.length);
                        for (int i = 0; i < _masses.length; i++) {
                                ComparablePair<Integer, Integer> p = new ComparablePair<Integer, Integer>(thisMasses[i], thisIntensities[i]);
                                returned.add(p);
                        }
                        return returned;
                } catch (Exception e) {
                        return null;
                }
        }

        public static Spectrum getNullSpectrum() {
                return nullSpectrum;
        }

        public Spectrum combineWith(Spectrum other) {
                if (other.isNull()) {
                        return clone();
                }
                LinkedList<Pair<Integer, Integer>> combinedPeakList = new LinkedList<Pair<Integer, Integer>>();
                int mass1 = _masses[0];
                int mass2 = other._masses[0];
                int i = 0;
                int j = 0;
                int len1 = _masses.length;
                int len2 = other._masses.length;
                while (true) {
                        while ((mass1 > mass2 || j == len2) && i < len1) {
                                int int1 = _intensities[i++];
                                if (i < len1) {
                                        mass1 = _masses[i];
                                }
                                combinedPeakList.add(new ComparablePair<Integer, Integer>(mass1, int1));
                        }
                        while ((mass2 > mass1 || i == len1) && j < len2) {
                                int int2 = other._intensities[j++];
                                if (j < len2) {
                                        mass2 = other._masses[j];
                                }
                                combinedPeakList.add(new ComparablePair<Integer, Integer>(mass2, int2));
                        }
                        while (mass1 == mass2 && i < len1 && j < len2) {
                                int int1 = _intensities[i++];
                                int int2 = other._intensities[j++];
                                if (i < len1) {
                                        mass1 = _masses[i];
                                }
                                if (j < len2) {
                                        mass2 = other._masses[j];
                                }
                                combinedPeakList.add(new ComparablePair<Integer, Integer>(mass1, (int1 + int2) / 2));
                        }
                        if (i == len1 && j == len2) {
                                break;
                        }
                }
                return new Spectrum(combinedPeakList);
        }

        private static <T extends Pair<Integer, Integer>> Comparator<T> getIntensityComparator() {
                return new Comparator<T>() {

                        public int compare(T o1, T o2) {
                                int comparison = o2.getSecond() - o1.getSecond();
                                if (comparison == 0) {
                                        comparison = o1.getFirst() - o2.getFirst();
                                }
                                return comparison;
                        }
                };
        }

        private static <T extends Pair<Integer, Integer>> Comparator<T> getRevIntensityComparator() {
                return new Comparator<T>() {

                        public int compare(T o1, T o2) {
                                int comparison = o1.getSecond() - o2.getSecond();
                                if (comparison == 0) {
                                        comparison = o1.getFirst() - o2.getFirst();
                                }
                                return comparison;
                        }
                };
        }
}
