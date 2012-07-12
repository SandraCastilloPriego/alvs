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


/**
 * How to have all pre- and post-processing filters in a nice way
 * usable for selected files (pre part..) and alignment?
 * @author jmjarkko
 *
 */
public class InputSet
{
	private List<GCGCData> data;
	
	public InputSet()
	{
		this(new ArrayList<GCGCData>());
	}
	
	public InputSet(List<GCGCData> input)
	{
		if (input == null)
		{
			throw new IllegalArgumentException("input cannot be null");
		}
		data = new ArrayList<GCGCData>(input);
	}
	
	/**
	 * Returns an unmodifiable list of the data.
	 * @return
	 */
	public List<GCGCData> getData()
	{
		return java.util.Collections.unmodifiableList(data);
	}
	
	public int size(){return data.size();}
	
}
