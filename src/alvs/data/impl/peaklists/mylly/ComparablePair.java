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

/**
 * @author jmjarkko
 */
public class ComparablePair<F extends Comparable<? super F>,S extends Comparable<? super S>> extends Pair<F,S> implements Comparable<ComparablePair<F,S>>
{
	
	public ComparablePair(F first, S second)
	{
		super(first, second);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ComparablePair<F, S> o)
	{
		int comparison = 0;
		if (first == null)
		{
			if (o.first != null){comparison = -1;}
		}
		else {comparison = first.compareTo(o.first);}
		if (comparison == 0)
		{
			if (second == null)
			{
				if (o.second != null){comparison = -1;}
			}
			else{comparison = second.compareTo(o.second);}
		}
		return comparison;
	}
	
	/**
	 * Compares 1st second of this ComparablePair to second of the other
	 * ComparablePair and return the comparison. If these are the same, 
	 * then returns the comparison between firsts.
	 * @param o
	 * @return
	 */
	public int swapCompareTo(ComparablePair<F, S> o)
	{
		int comparison = 0;
		if (second == null)
		{
			if (o.getSecond() != null){comparison = -1;}
		}
		else{comparison = second.compareTo(o.getSecond());}
		if (comparison == 0)
		{
			if (first == null)
			{
				if (o.first != null){comparison = -1;}
			}
			else{comparison = first.compareTo(o.getFirst());}
		}
		return comparison;
	}
}
