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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author jmjarkko
 * This class displays bad design: Superclass is immutable yet this one isn't.
 * FIXME Fix class hierarchy.
 */
public class ConsensusSpectrum extends Spectrum implements Cloneable
{
	
	private final static ConsensusSpectrum nullSpectrum = new ConsensusSpectrum(Spectrum.getNullSpectrum());
	
	private Map<Integer, Long> _spectrum;
	private boolean _cacheValid;
	private int _count;
	
	public ConsensusSpectrum(List<Pair<Integer, Integer>> base)
	{
		super(base);
		_spectrum = new HashMap<Integer, Long>();
		for (Pair<Integer, Integer> pair : base)
		{
			_spectrum.put(pair.getFirst(), pair.getSecond().longValue());
		}
		_count = 1;
	}
	
	public ConsensusSpectrum()
	{
		this._spectrum = new HashMap<Integer, Long>();
	}
	
	public ConsensusSpectrum(Spectrum s)
	{
		_spectrum = new HashMap<Integer, Long>();
		for (int i = 0; i < s.peakIntensities().length; i++)
		{
			_spectrum.put(s.peakMasses()[i], new Long(s.peakIntensities()[i]));
		}
		_count = 1;
	}
	
	private ConsensusSpectrum(ConsensusSpectrum s)
	{
		_spectrum = new HashMap<Integer, Long>();
		for(Entry<Integer, Long> e : s._spectrum.entrySet())
		{
			_spectrum.put(e.getKey(), e.getValue());
		}
		if (s._cacheValid)
		{
			_intensities = s.peakIntensities().clone();
			_masses = s.peakMasses().clone();
			_cacheValid = true;
		}
		_count = s._count;
	}
	
	protected int[] peakIntensities()
	{
		if(!_cacheValid){updateCache();}
		return _intensities;
	}

	protected int[] peakMasses()
	{
		if (!_cacheValid){updateCache();}
		return _masses;
	}

	public ConsensusSpectrum clone()
	{
		return new ConsensusSpectrum(this);
	}
	
	public ConsensusSpectrum addPeak(GCGCDatum peak)
	{
		Spectrum sp = peak.getSpectrum();
		if (!sp.isNull())
		{
			int otherIntensities[] = sp.peakIntensities();
			int otherMasses[] = sp.peakMasses();

			for (int i = 0; i < sp.length(); i++)
			{
				Long intensitySum = _spectrum.get(otherMasses[i]);
				if (intensitySum == null){intensitySum = 0L;}
				intensitySum += otherIntensities[i];
				_spectrum.put(otherMasses[i], intensitySum);
			}
			_count++;
			_cacheValid = false;
		}
		return this;
	}
	
	public boolean isNull()
	{
		return (this == getNullSpectrum() || super.isNull());
	}
	
	public static ConsensusSpectrum getNullSpectrum()
	{
		return nullSpectrum;
	}
	
	public int length(){return _spectrum.size();}
	
	protected void updateCache()
	{
		List<Pair<Integer, Integer>> tempList = new ArrayList<Pair<Integer, Integer>>(_spectrum.size());
		for(Entry<Integer, Long> e : _spectrum.entrySet())
		{
			int scaledVal = (int) (e.getValue().longValue() / _count);
			tempList.add(new Pair<Integer, Integer>(e.getKey(), scaledVal));
		}
//		sort(super._sortMode);
//		Pair<int[], int[]> temp = parseMassesAndIntensities(tempList);
//		_masses = temp.getFirst();
//		_intensities = temp.getSecond();
		Spectrum tempSpectrum = new Spectrum(tempList);
		tempSpectrum.sort(super._sortMode);
		_masses = tempSpectrum.getMasses();
		_intensities = tempSpectrum.getIntensities();
//		for (int i = 0; i < _intensities.length; i++)
//		{
//			_intensities[i] /= _count;
//		}
		
		_cacheValid = true;
	}
}
