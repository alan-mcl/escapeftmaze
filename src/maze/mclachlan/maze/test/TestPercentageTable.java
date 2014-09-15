package mclachlan.maze.test;

import java.util.*;
import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class TestPercentageTable
{
	public static void main(String[] args) throws Exception
	{
		List<String> strings = Arrays.asList("a", "b", "c", "d", "e", "f");
		List<Double> weights = Arrays.asList(1D,1D,1D,1D,1D,.5);

		PercentageTable perc = new PercentageTable(strings, weights);

		System.out.println("perc = [" + perc + "]");
		
	}
}
