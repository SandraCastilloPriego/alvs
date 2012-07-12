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

import java.util.List;

/**
 * @author jmjarkko
 */
public class GCGCDatumWithConcentration extends GCGCDatum {

	private double conc;

	public GCGCDatumWithConcentration(
			int id,
			double rt1,
			double rt2,
			double retentionIndex,
			double quantMass,
			int similarity,
			double area,
			String CAS,
			String name,
			boolean useConc,
			String columnName,
			List<? extends Pair<Integer, Integer>> peakList,
			double concentration) {
		super(id, rt1, rt2, retentionIndex, quantMass, similarity, area, concentration, useConc, CAS, name, columnName, peakList);
		conc = concentration;
	}

	public GCGCDatumWithConcentration(GCGCDatumWithConcentration source) {
		super(source);
		this.conc = source.conc;
	}

	public double getConc() {
		return conc;
	}

	public GCGCDatumWithConcentration clone() {
		return new GCGCDatumWithConcentration(this);
	}

	public String toFileString() {
		return getArea() + "\t" + conc;
	}
}
