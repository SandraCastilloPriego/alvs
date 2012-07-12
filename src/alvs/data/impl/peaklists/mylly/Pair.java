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
 * None of the methods are synchronized so use with that in mind.
 * 
 * @author jmjarkko
 *
 * @param <T> type of first part of pair
 * @param <Y> type of second part of pair
 */
public class Pair<T, Y>
{
	protected T first;
	protected Y second;
	
	public Pair(T first, Y second)
	{
		this.first = first;
		this.second = second;
	}
	
	public T getFirst(){return first;}
	public Y getSecond(){return second;}
	
	/**
	 * Sets first part of pair to newFirst and returns this pair.
	 * @param newFirst new first part.
	 * @return <code>this</code>
	 */
	public Pair<T,Y> setFirst(T newFirst)
	{
		first = newFirst;
		return this;
	}
	
	/**
	 * Sets second part of pair to newSecond and returns this pair.
	 * @param newSecond
	 * @return <code>this</code>
	 */
	public Pair<T,Y> setSecond(Y newSecond)
	{
		second = newSecond;
		return this;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('(').append((first == null) ? "null" : first.toString());
		sb.append(" , ").append((second == null) ? "null" : second.toString());
		sb.append(')');
		return sb.toString();
	}
	
	public int hashCode()
	{
		int hashcode = super.hashCode();
		hashcode += (getFirst() != null) ? getFirst().hashCode() : 0;
		hashcode += (getSecond() != null) ? getSecond().hashCode() : 0;
		return hashcode;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
	{
		if (o instanceof Pair)
		{
			Pair anotherPair = (Pair) o;
			Object a1 = getFirst();
			Object a2 = getSecond();
			
			Object b1 = anotherPair.getFirst();
			Object b2 = anotherPair.getSecond();
			
			boolean equal = true;
			equal = (a1 == b1 || (a1 != null && a1.equals(b1)));
			equal = (equal & (a2 == b2 || (a2 != null && a2.equals(b2))));
			return equal;
		}
		return false;
	}


}
