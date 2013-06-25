/*
 * Matrix class is a simple representation of an m x n matrix
 * in which the matrix is represented as an ArrayList of ArrayLists
 * of Integers. Matrix is a basic wrapper for a 2D ArrayList
 */

import java.util.*;

public class Matrix {
	
	private ArrayList<ArrayList<Integer>> matrix;
	private int m;
	private int n;
	private float sm;
	private float lm;
	
	public Matrix(int m, int n){
		this.m = m;
		this.n = n;
		matrix = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> temp;
		for(int i = 0; i < m; i++){
			temp = new ArrayList<Integer>();
			for(int j = 0; j < n; j++){
				//System.out.println("i = " + i + ", j = " + j);
				temp.add(j, new Integer(0));
			}
			matrix.add(i, temp);
		}
	}
        
        public Matrix(double[][] arr){
            // make new constructor to make matrix of 2d array.
            matrix = new ArrayList<ArrayList<Integer>>();
            this.m = arr.length;
            this.n = arr.length;
            ArrayList<Integer> temp;
		for(int i = 0; i < m; i++){
			temp = new ArrayList<Integer>();
			for(int j = 0; j < n; j++){
				//System.out.println("i = " + i + ", j = " + j);
				temp.add(j, new Integer((int) arr[i][j]));
			}
			matrix.add(i, temp);
		}
        }
        
        public void squarify(){
            int size = Math.max(m, n);
            for(int i = 0; i < size; i++){
                if(i >= m){
                    matrix.add(i, new ArrayList(size));
                    for(int j = 0; j < size; j++){
                        matrix.get(i).add(j, 0);
                    }
                }
                else{
                    for(int j = 0; j < size; j++){
                        if(j >= n)
                            matrix.get(i).add(j, 0);
                    }
                }
            }
            m = size;
            n = size;
            System.out.println(this.toString());
        }
	
	public Integer get(int m, int n){
		return matrix.get(m).get(n);
	}
	
	public int getM(){
		return m;
	}
	
	public int getN(){
		return n;
	}
	
	// returns smallest member of the matrix
	public float getSM(){
		int smallest = Integer.MAX_VALUE;
		for(int i = 0; i < this.m; i++){
			for(int j = 0; j < this.n; j++){
				if(this.get(i,j) < smallest && this.get(i,j) > 0)
					smallest = this.get(i,j);
			}
		}
		return (float) smallest;
	}
	
	// returns largest member of the matrix
	public float getLM(){
		int largest = -1 * Integer.MAX_VALUE;
		for(int i = 0; i < this.m; i++){
			for(int j = 0; j < this.n; j++){
				if(this.get(i,j) > largest)
					largest = this.get(i,j);
			}
		}
		return (float) largest;
	}
	
	public void put(int m, int n, Integer val){
		matrix.get(m).set(n, val);
	}
	
	public Integer[][] toArray(){
		Integer[][] outer = new Integer[m][n];
		for(int i = 0; i < matrix.size(); i++){
			for(int j = 0; j < matrix.get(i).size(); j++){
				outer[i][j] = matrix.get(i).get(j);
			}
		}
		return outer;
	}
	
	public String toString(){
		String str = "";
		for(ArrayList<Integer> arr : matrix)
			str += arr.toString() + '\n';
		return str;
	}
}
