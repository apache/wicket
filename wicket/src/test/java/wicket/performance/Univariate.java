/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.performance;

/**
 * @(#)Univariate.java * * Copyright (c) 2000 by Sundar Dorai-Raj
 * @author Sundar Dorai-Raj Email: sdoraira@vt.edu
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version, provided that any use properly credits the author. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details at
 * http://www.gnu.org * *
 */
public class Univariate
{
	private double[] x, sortx;
	private final double[] summary = new double[6];
	private boolean isSorted = false;
	private int n;
	private double mean, variance, stdev;
	
	/** */
	public double[] five = new double[5];

	/**
	 * Construct.
	 * 
	 * @param data
	 */
	public Univariate(final double[] data)
	{
		x = data.clone();
		n = x.length;
		createSummaryStats();
	}

	/**
	 * 
	 */
	private void createSummaryStats()
	{
		int i;
		mean = 0;
		for (i = 0; i < n; i++)
		{
			mean += x[i];
		}
		mean /= n;
		variance = variance();
		stdev = stdev();

		double sumxx = 0;
		variance = 0;
		for (i = 0; i < n; i++)
		{
			sumxx += x[i] * x[i];
		}
		if (n > 1)
		{
			variance = (sumxx - n * mean * mean) / (n - 1);
		}
		stdev = Math.sqrt(variance);
	}

	/**
	 * 
	 * @return summary
	 */
	public double[] summary()
	{
		summary[0] = n;
		summary[1] = mean;
		summary[2] = variance;
		summary[3] = stdev;
		summary[4] = Math.sqrt(variance / n);
		summary[5] = mean / summary[4];
		return (summary);
	}

	/**
	 * 
	 * @return mean
	 */
	public double mean()
	{
		return (mean);
	}

	/**
	 * 
	 * @return variance
	 */
	public double variance()
	{
		return (variance);
	}

	/**
	 * 
	 * @return stdev
	 */
	public double stdev()
	{
		return (stdev);
	}

	/**
	 * 
	 * @return SE
	 */
	public double SE()
	{
		return (Math.sqrt(variance / n));
	}

	/**
	 * 
	 * @return max
	 */
	public double max()
	{
		if (!isSorted)
		{
			sortx = sort();
		}
		return (sortx[n - 1]);
	}

	/**
	 * 
	 * @return min
	 */
	public double min()
	{
		if (!isSorted)
		{
			sortx = sort();
		}
		return (sortx[0]);
	}

	/**
	 * 
	 * @return median
	 */
	public double median()
	{
		return (quant(0.50));
	}

	/**
	 * 
	 * @param q
	 * @return quant
	 */
	public double quant(final double q)
	{
		if (!isSorted)
		{
			sortx = sort();
		}
		if (q > 1 || q < 0)
		{
			return (0);
		}
		else
		{
			final double index = (n + 1) * q;
			if (index - (int)index == 0)
			{
				return sortx[(int)index - 1];
			}
			else
			{
				return q * sortx[(int)Math.floor(index) - 1] + (1 - q)
						* sortx[(int)Math.ceil(index) - 1];
			}
		}
	}

	/**
	 * 
	 * @return sort
	 */
	public double[] sort()
	{
		sortx = x.clone();
		int incr = (int)(n * .5);
		while (incr >= 1)
		{
			for (int i = incr; i < n; i++)
			{
				final double temp = sortx[i];
				int j = i;
				while (j >= incr && temp < sortx[j - incr])
				{
					sortx[j] = sortx[j - incr];
					j -= incr;
				}
				sortx[j] = temp;
			}
			incr /= 2;
		}
		isSorted = true;
		return (sortx);
	}

	/**
	 * 
	 * @return data
	 */
	public double[] getData()
	{
		return (x);
	}

	/**
	 * 
	 * @return size
	 */
	public int size()
	{
		return (n);
	}

	/** 
	 * 
	 * @param index
	 * @return data
	 */
	public double elementAt(final int index)
	{
		double element = 0;
		try
		{
			element = x[index];
		}
		catch (final ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Index " + index + " does not exist in data.");
		}
		return (element);
	}

	/**
	 * 
	 * @param indices
	 * @return subset
	 */
	public double[] subset(final int[] indices)
	{
		final int k = indices.length;
		int i = 0;
		final double elements[] = new double[k];
		try
		{
			for (i = 0; i < k; i++)
			{
				elements[i] = x[k];
			}
		}
		catch (final ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Index " + i + " does not exist in data.");
		}
		return (elements);
	}

	/**
	 * 
	 * @param t
	 * @return compare
	 */
	public int compare(final double t)
	{
		int index = n - 1;
		int i;
		boolean found = false;
		for (i = 0; i < n && !found; i++)
		{
			if (sortx[i] > t)
			{
				index = i;
				found = true;
			}
		}
		return (index);
	}

	/**
	 * 
	 * @param t1
	 * @param t2
	 * @return between
	 */
	public int[] between(final double t1, final double t2)
	{
		final int[] indices = new int[2];
		indices[0] = compare(t1);
		indices[1] = compare(t2);
		return (indices);
	}

	/**
	 * 
	 * @param element
	 * @return index
	 */
	public int indexOf(final double element)
	{
		int index = -1;
		for (int i = 0; i < n; i++)
		{
			if (Math.abs(x[i] - element) < 1e-6)
			{
				index = i;
			}
		}
		return (index);
	}
}
