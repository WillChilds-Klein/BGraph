/*
 * Created on Apr 25, 2005
 * 
 * Munkres-Kuhn (Hungarian) Algorithm Clean Version: 0.11
 * 
 * Konstantinos A. Nedas                     
 * Department of Spatial Information Science & Engineering
 * University of Maine, Orono, ME 04469-5711, USA
 * kostas@spatial.maine.edu
 * http://www.spatial.maine.edu/~kostas       
 *
 * This Java class implements the Hungarian algorithm [a.k.a Munkres' algorithm,
 * a.k.a. Kuhn algorithm, a.k.a. Assignment problem, a.k.a. Marriage problem,
 * a.k.a. Maximum Weighted Maximum Cardinality Bipartite Matching].
 *
 * [It can be used as a method call from within any main (or other function).]
 * It takes 2 arguments:
 * a. A 2-D array (could be rectangular or square).
 * b. A string ("min" or "max") specifying whether you want the min or max assignment.
 * [It returns an assignment matrix[array.length][2] that contains the row and col of
 * the elements (in the original inputted array) that make up the optimum assignment.]
 *  
 * [This version contains only scarce comments. If you want to understand the 
 * inner workings of the algorithm, get the tutorial version of the algorithm
 * from the same website you got this one (http://www.spatial.maine.edu/~kostas/dev/soft/munkres.htm)]
 * 
 * Any comments, corrections, or additions would be much appreciated. 
 * Credit due to professor Bob Pilgrim for providing an online copy of the
 * pseudocode for this algorithm (http://216.249.163.93/bob.pilgrim/445/munkres.html)
 * 
 * Feel free to redistribute this source code, as long as this header--with
 * the exception of sections in brackets--remains as part of the file.
 * 
 * Requirements: JDK 1.5.0_01 or better.
 * [Created in Eclipse 3.1M6 (www.eclipse.org).]
 * 
 */

import static java.lang.Math.floor;
import static java.lang.Math.round;

import java.util.ArrayList;

public class HungarianAlgorithm{
	
	private static double[][] array;
	
	public static EdgeSetQueue esq;
	public static EdgeSet currEdges;
	static double maxCost;
	static boolean cameFromStep6 = false;
	static double[] originalRowLeast; // keeps track of what everything is subtracted by in step one
	static double maxWeight;
	static ArrayList<Float> q;
	static ArrayList<Float> p;
	
	public HungarianAlgorithm(Matrix matrix, ArrayList<Integer> buyers, ArrayList<Integer> sellers){
		matrix.squarify();
            
                Integer[][] arr = matrix.toArray();
		
		array = new double[arr.length][arr[0].length];
		for(int i = 0; i < arr.length; i++)
			for(int j = 0; j < arr[i].length; j++)
				array[i][j] = (double) arr[i][j];
		this.esq = new EdgeSetQueue();
		/**/
		for(int i = 0; i < 1; i++){ //introduce short delay ~2s, allows display to draw. invoke in main...
			esq.push(new EdgeSet());
		}
		/**/
		
		this.currEdges = new EdgeSet();
		
	}

	//
//	//********************************//
//	//METHODS FOR CONSOLE INPUT-OUTPUT//
//	//********************************//
//	
//	public static int readInput(String prompt)	//Reads input,returns double.
//	{
//		Scanner in = new Scanner(System.in);
//		System.out.print(prompt);
//		int input = in.nextInt();
//		return input;
//	}
	
	public static void printTime(double time)	//Formats time output.
	{
		String timeElapsed = "";
		int days = (int)floor(time)/(24 * 3600);
		int hours = (int)floor(time%(24*3600))/(3600);
		int minutes = (int)floor((time%3600)/60);
		int seconds = (int)round(time%60);
		
		if (days > 0)
			timeElapsed = Integer.toString(days) + "d:";
		if (hours > 0)
			timeElapsed = timeElapsed + Integer.toString(hours) + "h:";
		if (minutes > 0)
			timeElapsed = timeElapsed + Integer.toString(minutes) + "m:";
		
		timeElapsed = timeElapsed + Integer.toString(seconds) + "s";
		System.out.print("\nTotal time required: " + timeElapsed + "\n\n");
	}
	
	//*******************************************//
	//METHODS THAT PERFORM ARRAY-PROCESSING TASKS//
	//*******************************************//
	
//	public static void generateRandomArray	//Generates random 2-D array.
//	(double[][] array, String randomMethod)	
//	{
//		Random generator = new Random();
//		for (int i=0; i<array.length; i++)
//		{
//			for (int j=0; j<array[i].length; j++)
//			{
//				if (randomMethod.equals("random"))
//					{array[i][j] = generator.nextDouble();}
//				if (randomMethod.equals("gaussian"))
//				{
//						array[i][j] = generator.nextGaussian()/4;		//range length to 1.
//						if (array[i][j] > 0.5) {array[i][j] = 0.5;}		//eliminate outliers.
//						if (array[i][j] < -0.5) {array[i][j] = -0.5;}	//eliminate outliers.
//						array[i][j] = array[i][j] + 0.5;				//make elements positive.
//				}
//			}
//		}			
//	}
	public static double findLargest		//Finds the largest element in a positive array.
	(double[][] array)
	//works for arrays where all values are >= 0.
	{
		double largest = 0;
		for (int i=0; i<array.length; i++)
		{
			for (int j=0; j<array[i].length; j++)
			{
				if (array[i][j] > largest)
				{
					largest = array[i][j];
				}
			}
		}
			
		return largest;
	}
//	public static double[][] transpose		//Transposes a double[][] array.
//	(double[][] array)	
//	{
//		double[][] transposedArray = new double[array[0].length][array.length];
//		for (int i=0; i<transposedArray.length; i++)
//		{
//			for (int j=0; j<transposedArray[i].length; j++)
//			{transposedArray[i][j] = array[j][i];}
//		}
//		return transposedArray;
//	}
	public static double[][] copyOf			//Copies all elements of an array to a new array.
	(double[][] original)	
	{
		double[][] copy = new double[original.length][original[0].length];
		for (int i=0; i<original.length; i++)
		{
			//Need to do it this way, otherwise it copies only memory location
			System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
		}
		
		return copy;
	}
	
	//**********************************//
	//METHODS OF THE HUNGARIAN ALGORITHM//
	//**********************************//
	
	public static int[][] hgAlgorithm (double[][] array, String sumType)
	{
		double[][] cost = copyOf(array);	//Create the cost matrix
		originalRowLeast = new double[cost.length];
                
                //esq.push(new EdgeSet()); // slight delay.
                //esq.push(new EdgeSet());
		
		q = new ArrayList<Float>(array.length);
		p = new ArrayList<Float>(array[0].length);
		for(int i = 0; i < array.length; i++)
			q.add(new Float(-1));
		for(int i = 0; i < array[0].length; i++)
			p.add(new Float(-1));
		
		/**/
		if (sumType.equalsIgnoreCase("max"))	//Then array is weight array. Must change to cost.
		{
			maxWeight = findLargest(cost);
			for (int i=0; i<cost.length; i++)		//Generate cost by subtracting.
			{
				for (int j=0; j<cost[i].length; j++)
				{
					cost [i][j] = (maxWeight - cost [i][j]);
				}
			}
		}
		/**/
		maxCost = findLargest(cost);		//Find largest cost matrix element (needed for step 6).
		
		int[][] mask = new int[cost.length][cost[0].length];	//The mask array.
		int[] rowCover = new int[cost.length];					//The row covering vector.
		int[] colCover = new int[cost[0].length];				//The column covering vector.
		int[] zero_RC = new int[2];								//Position of last zero from Step 4.
		int step = 1;											
		boolean done = false;
		while (done == false)	//main execution loop
		{ 
			switch (step)
		    {
				case 1:
					step = hg_step1(step, cost);     
		    	    break;
		    	case 2:
		    	    step = hg_step2(step, cost, mask, rowCover, colCover);
					break;
		    	case 3:
		    	    step = hg_step3(step, mask, colCover);
					break;
		    	case 4:
		    	    step = hg_step4(step, cost, mask, rowCover, colCover, zero_RC);
					break;
		    	case 5:
					step = hg_step5(step, mask, rowCover, colCover, zero_RC);
					break;
		    	case 6:
		    	   	step = hg_step6(step, cost, rowCover, colCover, maxCost, mask);
					break;
		  	    case 7:
		    	    done=true;
		    	    break;
		    }
		}//end while
		
		// final result of only edges in matching
		EdgeSet es = new EdgeSet();
		
		int[][] assignment = new int[array.length][2];	//Create the returned array.
		for (int i=0; i<mask.length; i++)
		{
			for (int j=0; j<mask[i].length; j++)
			{
				if (mask[i][j] == 1)
				{
					assignment[i][0] = i;
					assignment[i][1] = j;
					
					es.add(new Edge(i,j, Edge.Direction.fromSeller));
				}
			}
		}

		es.add((ArrayList<Float>) q.clone(), (ArrayList<Float>) p.clone());
		
		esq.push(es);
		
		//If you want to return the min or max sum, in your own main method
		//instead of the assignment array, then use the following code:
		/*
		double sum = 0; 
		for (int i=0; i<assignment.length; i++)
		{
			sum = sum + array[assignment[i][0]][assignment[i][1]];
		}
		return sum;
		*/
		//Of course you must also change the header of the method to:
		//public static double hgAlgorithm (double[][] array, String sumType)
		
		return assignment;
	}
	public static int hg_step1(int step, double[][] cost)
	{
                System.out.println("STEP 1");
		//What STEP 1 does:
		//For each row of the cost matrix, find the smallest element
		//and subtract it from from every other element in its row. 
	    
	   	double minval;
	   	
		for (int i=0; i<cost.length; i++)	
	   	{									
	   	    minval=cost[i][0];
	   	    for (int j=0; j<cost[i].length; j++)	//1st inner loop finds min val in row.
	   	    {
	   	        if (minval>cost[i][j])
	   	        {
	   	            minval=cost[i][j];
	   	        }
			}
			for (int j=0; j<cost[i].length; j++)	//2nd inner loop subtracts it.
	   	    {
	   	        cost[i][j]=cost[i][j]-minval;
	   	        
	   	    }
			
			originalRowLeast[i] = minval;
		}
	   			    
		step=2;
		return step;
	}
	public static int hg_step2(int step, double[][] cost, int[][] mask, int[]rowCover, int[] colCover)
	{
		//What STEP 2 does:
		//Marks uncovered zeros as starred and covers their row and column.
		
		// initial matches in EQ subgraph
            
            System.out.println("STEP 2");
		
		EdgeSet es = new EdgeSet();
		
		for (int i=0; i<cost.length; i++)
	    {
	        for (int j=0; j<cost[i].length; j++)
	        {
	            if ((cost[i][j]==0) && (colCover[j]==0) && (rowCover[i]==0))
	            {
	                mask[i][j]=1;
					colCover[j]=1;
	                rowCover[i]=1;
	                es.add(new Edge(i,j, Edge.Direction.fromSeller));
				}
	            else if(cost[i][j] == 0){
	               es.add(new Edge(i,j, Edge.Direction.fromBuyer));
	   	        }
	        }
	    }
		
		float minval;
		int row = 0;
		// initialize q's to all 0's
		// need to get array of lowest values per row of ORIGINAL cost matrix. subtract cost from original largest weight
		for(int i = 0; i < cost.length; i++){
                    System.out.println("cost.length: " + cost.length);
                    System.out.println("cost[0].length: " + cost[0].length);
                    System.out.println("q.size(): " + q.size());
                    System.out.println("p.size(): " + p.size());
			minval = (float) Integer.MAX_VALUE;
			for(int j = 0; j < cost[0].length; j++){
				if(cost[i][j] < minval){
					minval = (float) cost[i][j];
					row = i;
				}
			}
			q.set(i, new Float(maxWeight - originalRowLeast[row] - minval));
		}
		// initialize p's to all 0's
		for(int i = 0; i < cost[0].length; i++){
			p.set(i, new Float(0));
		}
		
		es.add((ArrayList<Float>) q.clone(), (ArrayList<Float>) p.clone());
		esq.push(es); // uncomment for non-incremental display
		
		clearCovers(rowCover, colCover);	//Reset cover vectors.
			    
		step=3;
		return step;
	}
	public static int hg_step3(int step, int[][] mask, int[] colCover)
	{
            System.out.println("STEP 3");
		//What STEP 3 does:
		//Cover columns of starred zeros. Check if all columns are covered.
		
		for (int i=0; i<mask.length; i++)	//Cover columns of starred zeros.
	    {
	        for (int j=0; j<mask[i].length; j++)
	        {
	            if (mask[i][j] == 1)
	            {
	                colCover[j]=1;
				}
	        }
	    }
	    
		int count=0;						
		for (int j=0; j<colCover.length; j++)	//Check if all columns are covered.
	    {
	        count=count+colCover[j];
	    }
		
		if (count>=mask.length)	//Should be cost.length but ok, because mask has same dimensions.	
	    {
			step=7;
		}
	    else
		{
			step=4;
		}
	    	
		return step;
	}
	public static int hg_step4(int step, double[][] cost, int[][] mask, int[] rowCover, int[] colCover, int[] zero_RC)
	{
            System.out.println("STEP 4");
		//What STEP 4 does:
		//Find an uncovered zero in cost and prime it (if none go to step 6). Check for star in same row:
		//if yes, cover the row and uncover the star's column. Repeat until no uncovered zeros are left
		//and go to step 6. If not, save location of primed zero and go to step 5.
		
//		EdgeSet es = new EdgeSet();
//		currEdges = new EdgeSet();
		
		/*if(true){/*/if(cameFromStep6 == true){
			// adds n^2 cost, so only practical for small values of n.
			EdgeSet es = new EdgeSet();
			
			for(int i = 0; i < cost.length; i++){
				for(int j = 0; j < cost[0].length; j++){
					if(cost[i][j] == 0){
						if(mask[i][j] == 1){
							es.add(new Edge(i, j, Edge.Direction.fromSeller)); // might be j,i...
						}
						else {//if(mask[i][j] != 0 && cost[i][j] == 0){
							es.add(new Edge(i, j, Edge.Direction.fromBuyer)); // might be j,i...
						}
					}
				}
			}

			//es = currEdges.clone(); // uncomment for non-incremental display
			es.add((ArrayList<Float>) q.clone(), (ArrayList<Float>) p.clone());
			esq.push(es); // uncomment for non-incremental display
			cameFromStep6 = false;
		}
		
		int[] row_col = new int[2];	//Holds row and col of uncovered zero.
		boolean done = false;
		while (done == false)
		{
			row_col = findUncoveredZero(row_col, cost, rowCover, colCover);
			if (row_col[0] == -1)
			{
				done = true;
				step = 6;
			}
			else
			{
				mask[row_col[0]][row_col[1]] = 2;	//Prime the found uncovered zero.
				
//				currEdges.add(new Edge(row_col[0], row_col[1], Edge.Direction.fromBuyer));
//                es = currEdges.clone(); // uncomment for incremental display
//				esq.push(es); // uncomment for incremental display
				
				boolean starInRow = false;
				for (int j=0; j<mask[row_col[0]].length; j++)
				{
					if (mask[row_col[0]][j]==1)		//If there is a star in the same row...
					{
						starInRow = true;
						row_col[1] = j;		//remember its column.
					}
				}
							
				if (starInRow==true)	
				{
					rowCover[row_col[0]] = 1;	//Cover the star's row.
					colCover[row_col[1]] = 0;	//Uncover its column.
				}
				else
				{
					zero_RC[0] = row_col[0];	//Save row of primed zero.
					zero_RC[1] = row_col[1];	//Save column of primed zero.
					done = true;
					step = 5;
				}
			}
		}
		
		return step;
	}
	public static int[] findUncoveredZero	//Aux 1 for hg_step4.
	(int[] row_col, double[][] cost, int[] rowCover, int[] colCover)
	{
		row_col[0] = -1;	//Just a check value. Not a real index.
		row_col[1] = 0;
		
		int i = 0; boolean done = false;
		while (done == false)
		{
			int j = 0;
			while (j < cost[i].length)
			{
				if (cost[i][j]==0 && rowCover[i]==0 && colCover[j]==0)
				{
					row_col[0] = i;
					row_col[1] = j;
					done = true;
				}
				j = j+1;
			}//end inner while
			i=i+1;
			if (i >= cost.length)
			{
				done = true;
			}
		}//end outer while
		
		return row_col;
	}
	public static int hg_step5(int step, int[][] mask, int[] rowCover, int[] colCover, int[] zero_RC)
	{
            System.out.println("STEP 5");
		//What STEP 5 does:	
		//Construct series of alternating primes and stars. Start with prime from step 4.
		//Take star in the same column. Next take prime in the same row as the star. Finish
		//at a prime with no star in its column. Unstar all stars and star the primes of the
		//series. Erase any other primes. Reset covers. Go to step 3.
		
		int count = 0;												//Counts rows of the path matrix.
		int[][] path = new int[(mask[0].length*mask.length)][2];	//Path matrix (stores row and col).
		for(int i = 0; i < path.length; i++){ // for path thing
			path[i][0] = -1;
		}
		path[count][0] = zero_RC[0];								//Row of last prime.
		path[count][1] = zero_RC[1];								//Column of last prime.
		
		boolean done = false;
		while (done == false)
		{ 
			int r = findStarInCol(mask, path[count][1]);
			if (r>=0)
			{
				count = count+1;
				path[count][0] = r;					//Row of starred zero.
				path[count][1] = path[count-1][1];	//Column of starred zero.
			}
			else
			{
				done = true;
			}
			
			if (done == false)
			{
				int c = findPrimeInRow(mask, path[count][0]);
				count = count+1;
				path[count][0] = path [count-1][0];	//Row of primed zero.
				path[count][1] = c;					//Col of primed zero.
			}
		}//end while
		
		print2DArray(path);
		
		// go through path and display.
//		EdgeSet es = new EdgeSet();
//		for(int i = 0; i < path.length && path[i][0] > -1; i++){
//			Edge e;
//			if(i % 2 == 0) // even number in aug path, not in matching
//				e = new Edge(path[i][0], path[i][1], Edge.Direction.fromSeller);
//			else // odd number in aug path, in matching
//				e = new Edge(path[i][0], path[i][1], Edge.Direction.fromBuyer);
//			currEdges.add(e);
//			es = currEdges.clone();
//			esq.push(es); // uncomment for incremental path display.
//		}
//		esq.push(es); // uncomment for non-incremental path display
		
		convertPath(mask, path, count);
		clearCovers(rowCover, colCover);
		erasePrimes(mask);
		
		step = 3;
		
		//cameFromStep6 = true;
		
		return step;
		
	}
	public static int findStarInCol			//Aux 1 for hg_step5.
	(int[][] mask, int col)
	{
		int r=-1;	//Again this is a check value.
		for (int i=0; i<mask.length; i++)
		{
			if (mask[i][col]==1)
			{
				r = i;
			}
		}
				
		return r;
	}
	public static int findPrimeInRow		//Aux 2 for hg_step5.
	(int[][] mask, int row)
	{
		int c = -1;
		for (int j=0; j<mask[row].length; j++)
		{
			if (mask[row][j]==2)
			{
				c = j;
			}
		}
		
		return c;
	}
	public static void convertPath			//Aux 3 for hg_step5.
	(int[][] mask, int[][] path, int count)
	{
		for (int i=0; i<=count; i++)
		{
			if (mask[(path[i][0])][(path[i][1])]==1)
			{
				mask[(path[i][0])][(path[i][1])] = 0;
			}
			else
			{
				mask[(path[i][0])][(path[i][1])] = 1;
			}
		}
	}
	public static void erasePrimes			//Aux 4 for hg_step5.
	(int[][] mask)
	{
		for (int i=0; i<mask.length; i++)
		{
			for (int j=0; j<mask[i].length; j++)
			{
				if (mask[i][j]==2)
				{
					mask[i][j] = 0;
				}
			}
		}
	}
	public static void clearCovers			//Aux 5 for hg_step5 (and not only).
	(int[] rowCover, int[] colCover)
	{
		for (int i=0; i<rowCover.length; i++)
		{
			rowCover[i] = 0;
		}
		for (int j=0; j<colCover.length; j++)
		{
			colCover[j] = 0;
		}
	}
	public static int hg_step6(int step, double[][] cost, int[] rowCover, int[] colCover, double maxCost, int[][] mask)
	{
            System.out.println("STEP 6");
		//What STEP 6 does:
		//Find smallest uncovered value in cost: a. Add it to every element of covered rows
		//b. Subtract it from every element of uncovered columns. Go to step 4.
		
		double minval = findSmallest(cost, rowCover, colCover, maxCost);
		
		//double maxval = findLargest(cost, rowCover, colCover, maxCost);
		double delta = minval;//maxCost - maxval;
		
		//System.out.println("Weight delta = " + delta + "     " + maxWeight);
		
		for (int i=0; i<rowCover.length; i++)
		{
			for (int j=0; j<colCover.length; j++)
			{
				if (rowCover[i]==1)
				{
					cost[i][j] = cost[i][j] + minval;
				}
				if (colCover[j]==0)
				{
					cost[i][j] = cost[i][j] - minval;
				}
			}
		}
		
		EdgeSet es = new EdgeSet();
		
		Float rowSmallest;
		Float val = new Float(-1);
		Float colSmallest;
		
		for(int i = 0; i < cost[0].length; i++){
//			colSmallest = new Float(Integer.MAX_VALUE);
//			for(int j = 0; j < cost.length; j++){
//				if(cost[j][i] < colSmallest && colCover[j] == 1){
//					colSmallest = new Float(cost[j][i]);
//					val = (float) ((float) (maxWeight - cost[j][i]) - originalRowLeast[i]);
//				}
//			}
			if(hasStarredAndPrimedinCol(mask, cost, i))
				p.set(i, new Float(p.get(i) + delta));
			//p.set(i, val);
		}
		
		// find q values by iterating through each row
		for(int i = 0; i < cost.length; i++){
//			rowSmallest = new Float(0/*Integer.MAX_VALUE*/);
//			for(int j = 0; j < cost[0].length; j++){
//				if(cost[i][j] >= rowSmallest && rowCover[i] == 0){
//					rowSmallest = new Float(cost[i][j]);
//					val = (float) (maxWeight - cost[i][j] - originalRowLeast[i]);
//				}
//			}
			val = new Float(0/*Integer.MAX_VALUE*/);
			for(int j = 0; j < cost[0].length; j++){
				if(array[i][j] - p.get(j) > val){
					val = (float) array[i][j] - p.get(j);
				}
			}
//			if(rowCover[i] == 0)
//				q.set(i, new Float(q.get(i) - delta));
			q.set(i, val);
		}
		
		// add to edgeset
		es.add(q, p);
		
		step = 4;
		cameFromStep6 = true;
		return step;
	}
	public static double findSmallest		//Aux 1 for hg_step6.
	(double[][] cost, int[] rowCover, int[] colCover, double maxCost)
	{
		double minval = maxCost;				//There cannot be a larger cost than this.
		for (int i=0; i<cost.length; i++)		//Now find the smallest uncovered value.
		{
			for (int j=0; j<cost[i].length; j++)
			{
				if (rowCover[i]==0 && colCover[j]==0 && (minval > cost[i][j]))
				{
					minval = cost[i][j];
				}
			}
		}
		
		return minval;
	}
	
	public static double findLargest		//Aux 2 for hg_step6. ADDED BY WCK
	(double[][] cost, int[] rowCover, int[] colCover, double maxCost)
	{
		// used to find weight delta, not cost delta.
		
		double maxval = 0;	//There cannot be a smaller cost than this.
		
		//Now find the smallest uncovered value.
		for (int i=0; i<cost.length; i++)
		{
			for (int j=0; j<cost[i].length; j++)
			{
				if (rowCover[i]==0 && colCover[j]==0 && (maxval < cost[i][j]))
				{
					maxval = cost[i][j];
				}
			}
		}
		
		return maxval;
	}
	
	//***********//
	//MAIN METHOD//
	//***********//
	
	public MEPack go(){
		// print this shtuff <-- assignment
		String sumType = "max";	
		int[][] assignment = new int[array.length][2];
		assignment = hgAlgorithm(array, sumType);
		
		System.out.println("\n(Printing out only 2 decimals)\n");
		System.out.println("The matrix is:");
		for (int i=0; i<array.length; i++)
		{
			for (int j=0; j<array[i].length; j++)
				{System.out.printf("%.2f\t", array[i][j]);}
			System.out.println();
		}
		System.out.println();
                
                // init final weight matrix to 0's => no edges
                double[][] assignedWeights = new double[array.length][array[0].length];	
                for(int i = 0; i < assignment.length; i++){
                    for(int j = 0; j < assignment[0].length; j++){
                        assignedWeights[i][j] = 0;
                    }
                }
						
		System.out.println("The winning assignment (" + sumType + " sum) is:\n");
		double sum = 0;
		for (int i=0; i<assignment.length; i++){
			System.out.printf("array(%d,%d) = %.2f\n", (assignment[i][0]+1), (assignment[i][1]+1),
					array[assignment[i][0]][assignment[i][1]]);
                        assignedWeights[assignment[i][0]][assignment[i][1]] = array[assignment[i][0]][assignment[i][1]];
			sum = sum + array[assignment[i][0]][assignment[i][1]];
		}
		
		System.out.printf("\nThe %s is: %.2f\n", sumType, sum);
                
		return new MEPack(new Matrix(assignedWeights), this.esq);
	}
	
//	public static void main(String[] args) 
//	{
//		//Below enter "max" or "min" to find maximum sum or minimum sum assignment.
//		String sumType = "max";		
//				
//		// Hard-coded example.
////		double[][] array =
////		{
////				{1, 2, 3},
////				{2, 4, 6},
////				{3, 6, 9}
////		};
//		
//		//<UNCOMMENT> BELOW AND COMMENT BLOCK ABOVE TO USE A RANDOMLY GENERATED MATRIX
////		int numOfRows = readInput("How many rows for the matrix? ");
////		int numOfCols = readInput("How many columns for the matrix? ");
////		double[][] array = new double[numOfRows][numOfCols];
////		generateRandomArray(array, "random");	//All elements within [0,1].
//		//</UNCOMMENT>
//		
//		if (array.length > array[0].length)
//		{
//			System.out.println("Array transposed (because rows>columns).\n");	//Cols must be >= Rows.
//			array = transpose(array);
//		}
//				
//		//<COMMENT> TO AVOID PRINTING THE MATRIX FOR WHICH THE ASSIGNMENT IS CALCULATED
//		System.out.println("\n(Printing out only 2 decimals)\n");
//		System.out.println("The matrix is:");
//		for (int i=0; i<array.length; i++)
//		{
//			for (int j=0; j<array[i].length; j++)
//				{System.out.printf("%.2f\t", array[i][j]);}
//			System.out.println();
//		}
//		System.out.println();
//		//</COMMENT>*/
//		
//		double startTime = System.nanoTime();	
//		int[][] assignment = new int[array.length][2];
//		assignment = hgAlgorithm(array, sumType);	//Call Hungarian algorithm.
//		double endTime = System.nanoTime();
//						
//		System.out.println("The winning assignment (" + sumType + " sum) is:\n");	
//		double sum = 0;
//		for (int i=0; i<assignment.length; i++)
//		{
//			//<COMMENT> to avoid printing the elements that make up the assignment
//			System.out.printf("array(%d,%d) = %.2f\n", (assignment[i][0]+1), (assignment[i][1]+1),
//					array[assignment[i][0]][assignment[i][1]]);
//			sum = sum + array[assignment[i][0]][assignment[i][1]];
//			//</COMMENT>
//		}
//		
//		System.out.printf("\nThe %s is: %.2f\n", sumType, sum);
//		printTime((endTime - startTime)/1000000000.0);
//		
//	}
	public static void print2DArray 	//Prints 2-D double array.
	(int[][] array)
	{
		System.out.println();
		for (int i=0; i<array.length; i++)
		{
			for (int j=0; j<array[i].length; j++)
				{System.out.print(array[i][j]);}
			System.out.println();
		}
	}
	
	private static boolean hasStarredAndPrimedinCol(int [][] mask, double[][] cost, int col){
		boolean starFlag = false;
		boolean primeFlag = false;
		
		for(int i = 0; i < mask.length; i++){
			if(mask[i][col] == 1)
				starFlag = true;
			else if(mask[i][col] == 0 && cost[i][col] == 0)
				primeFlag = true;
		}
		
		return starFlag && primeFlag;
	}
}
