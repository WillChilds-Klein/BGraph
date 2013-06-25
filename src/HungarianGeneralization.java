
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
        
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author will
 */

public class HungarianGeneralization {
    
    private static double[][] array;
	
    public static EdgeSetQueue esq;
    public static EdgeSet currEdges;
    static double maxCost;
    static boolean cameFromStep6 = false;
    static double maxWeight;
    
    public HungarianGeneralization(Matrix matrix, ArrayList<Integer> buyers, ArrayList<Integer> sellers){
        // code for display
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
        
        // Real code
        ArrayList A = new ArrayList(sellers.size()),//new ArrayList(),
                B = new ArrayList(buyers.size());//new ArrayList(), 
        // convert buyers
        for(int i = 0; i < sellers.size(); i++){
            A.add(i, sellers.get(i));
        }
        // convert sellers
        for(int j = 0; j < buyers.size(); j++){
            B.add(j, buyers.get(j));
        }
        System.out.println("A = " + A.toString());
        System.out.println("B = " + B.toString());
        // convert matrix to double[][]
        double[][] m = new double[A.size()][B.size()];
        for(int i = 0; i < m.length; i++){
            for(int j = 0; j < m[0].length; j++){
                m[i][j] = matrix.get(i, j);
            }
        }
        
        // init the 2D array of pareto functions
        ArrayList<ArrayList<ParetoFunction>> pf = new ArrayList(A.size());
        for(int i = 0; i < A.size(); i++)
            pf.add(i, new ArrayList<ParetoFunction>(B.size()));

        for(int i = 0; i < buyers.size(); i++){
            for(int j = 0; j < sellers.size(); j++){
                pf.get(i).add(j, new ParetoFunction(){ // consider [j, i]?
                    public double v(double param, double weight){
                        // possibly vary per buyer...
                        return weight - param;
                        //return (-0.1*param) * param + 8; // dummy function
                    }
                    public double v(double param){
                        // possibly vary per buyer...
                        return 500 - param;
                        //return (-0.1*param) * param + 8; // dummy function
                    }
                });
            }
        }
        
        double[] Ra = new double[A.size()], // A's reserves
                Rb = new double[B.size()]; // B's reserves
        
        // init rA
        for(int i = 0; i < A.size(); i++){
            Ra[i] = 0;
        }
        // init rB
        for(int j = 0; j < B.size(); j++){
            Rb[j] = 0;
        }
        
        double[] eo = new double[A.size()];
        
        // begin iterations.
        hg_gen(A, B, Ra, Rb, pf, m, eo);
    }
    
    private void hg_gen(ArrayList<Integer> A, ArrayList<Integer> B, double[] Ra, double[] Rb, ArrayList<ArrayList<ParetoFunction>> pf, double[][] m, double[] eo){
        double[][] array = copyOf(m);	
        // 5.1 Initialization
        // 5.1.1
        double[] Oa = new double[A.size()], // A's offers
                Ob = new double[B.size()]; // B's offers
        
        // 1.) init O
        // build initial eq subgraph with edges that make Oi
        int maxInd = -1;
        int[][] eq = new int[array.length][array[0].length];
        // init Oa
        double max;
        for(int i = 0; i < A.size(); i++){
            max = 0;
            for(int j = 0; j < B.size(); j++){
                if(pf.get(i).get(j).v(Rb[j], m[i][j]) > max && m[i][j] != 0){
                    max = pf.get(i).get(j).v(Rb[j], m[i][j]);
                    maxInd = j;
                }
            }
            Oa[i] = Math.max(max, Ra[i]);
            eq[i][maxInd] = 2;
        }
        // init Ob
        for(int j = 0; j < B.size(); j++){
            Ob[j] = Rb[j];
        }
        System.out.println("pf is " + pf.size() + " elements long.");
        
        System.out.println("Oa = " + Arrays.toString(Oa));
        System.out.println("Ob = " + Arrays.toString(Ob));
        System.out.println("Ra = " + Arrays.toString(Ra));
        System.out.println("Rb = " + Arrays.toString(Rb));
        
        // 5.1.2
        // 1.) find initial matching <-- REVIEW THIS. CHECK Ri AND Oi constraints
        System.out.println("initialized...");
        print2DArray(eq);
        // find some maximum matching.
        while(!maxMatch(eq));
        System.out.println("initial maximum cardinality matching...");
        print2DArray(eq);
        // 2.)
        // eliminate alternating paths...
        while(!eliminateAltPath(eq, Oa, Ra));
        
        // 5.1.3
        // do stuff here for making vector I
        boolean isMatched;
        ArrayList<Integer> unmatchedA = new ArrayList();
        for(int i = 0; i < eq.length; i++){
            isMatched = false;
            for(int j = 0; j < eq[0].length; j++){
                if(eq[i][j] == 1){
                    isMatched = true;
                    break;
                }
            }
            if(!isMatched)
                unmatchedA.add(new Integer(i));
        }
        System.out.println("unmatchedA = " + unmatchedA.toString());
        // compare offers to reserve payoffs for unmatched A's, add to vector I
        // use individual steps of hg alg but not actual thing?
        ArrayList<Integer> I = new ArrayList();
        for(Integer i : unmatchedA){
            if(Oa[i] > Ra[i])
                I.add(new Integer(i));
        }
        System.out.println("I = " + I.toString());
        // initialization finished.
        
        // 5.2 Iterations
        // 5.2.1
        // 1.)
        ArrayList<Integer>[] AinT = new ArrayList[1];
        ArrayList<Integer>[] BinT = new ArrayList[1];
        ArrayList<Integer>[] BnotinT = new ArrayList[1];
        ArrayList<Integer>[] AnotinT = new ArrayList[1];
        int[][] c;
        int Astart;
        while(!I.isEmpty()){
            Astart = (Integer) I.get(0); // first node in I
            System.out.println("first element of I is: " + I.get(0));
            AinT[0] = new ArrayList();
            BinT[0] = new ArrayList();
            BnotinT[0] = new ArrayList();
            AnotinT[0] = new ArrayList();
            c = new int[m.length][m[0].length]; // init'd to all 0's
            // we create our own recursive traversal method and do all the 
            //operations within that method. computes A int T and B int T
            recursiveTraverse(eq, c, Astart, AinT, BinT);
            System.out.println("AinT = " + AinT[0].toString());
            System.out.println("BinT = " + BinT[0].toString());
            // comtpute B / T
            BnotinT[0] = new ArrayList();
            for(int i = 0; i < B.size(); i++){
                BnotinT[0].add(new Integer(i));
            }
            for(int i = 0; i < BinT[0].size(); i++){
                BnotinT[0].remove(BinT[0].get(i));
            }
            System.out.println("BnotinT = " + BnotinT[0].toString());
            // compute A / T
            AnotinT[0] = new ArrayList();
            for(int i = 0; i < A.size(); i++){
                AnotinT[0].add(new Integer(i));
            }
            for(int i = 0; i < AinT[0].size(); i++){
                AnotinT[0].remove(AinT[0].get(i));
            }
            System.out.println("AnotinT = " + AnotinT[0].toString());
            // find eo[i]'s
            for(Integer i : AinT[0]){
                max = Ra[(Integer) A.get(i)];
                for(Integer j : BnotinT[0]){
                    if(m[i][j] != 0 && pf.get(i).get(j).v(Ob[j], m[i][j]) > max)
                        max = pf.get(i).get(j).v(Ob[j], m[i][j]);
                }
                eo[(Integer) A.get(i)] = max; // maybe eo[i]?
            }
            System.out.println("eo = " + Arrays.toString(eo));
            // 2.)
            // compute SASTGOP s.t. Oa[i] >= eo[i] f.a. i in AinT w/ at least one Oa[i] == eo[i]
            // Next few lines of code are sim of SASTGOP
            for(Integer i : AinT[0]){
                Oa[i] = Oa[i] - 1;
            }
            for(Integer j : BinT[0]){
                Ob[j] = Ob[j] + 1;
            }
            // 3.) 
            // update EQ subgraph with new offer profile <-- union of two subg's
            // right now, this is just adding edges which have become splits of 
            // the offer profile.
            for(Integer i : AinT[0]){
                for(Integer j : BnotinT[0]){
                    if(array[i][j] > 0 && Oa[i] == pf.get(i).get(j).v(Ob[j], m[i][j]))
                        eq[i][j] = 2;
                }
            }
            for(Integer i : AnotinT[0]){
                for(Integer j : BinT[0]){
                    eq[i][j] = 0;
                }
            }
            
            // 5.2.2 Change the Matching
            // 1.)
            // taken care of by SASTGOP computation
            // 2.) check for augmenting paths and alternate them.
            System.out.println("pre-augmentation...");
            print2DArray(eq);
            while(buildAugPath(eq, new int[eq.length][eq[0].length], Astart, new EdgeSet())); // eliminate augmenting paths...
            System.out.println("augmented...");
            print2DArray(eq);
            if(Oa[Astart] > 0 || AisInMatching(Astart, eq))
                while(buildAltPath(eq, new int[eq.length][eq[0].length], Oa, Ra, Astart, new EdgeSet())); // eliminate alternating paths...
            System.out.println("alternating paths eliminated...");
            print2DArray(eq);
            
            // 5.2.3 Update Hungarian Forest
            if(Oa[I.get(0)] == Ra[I.get(0)] || AisInMatching(I.get(0), eq))
                I.remove(0);
            System.out.println("A's offers: " + Arrays.toString(Oa));
            System.out.println("B's offers: " + Arrays.toString(Ob));
        }
        
        
    }
    
    private boolean maxMatch(int[][] mask){
        //boolean max = true;
        for(int i = 0; i < mask.length; i++){
            if(buildAugPath(mask, new int[mask.length][mask[0].length], i, new EdgeSet())){
                //max = false;
                return false;
            }
        }
        //return max;
        return true;
    }
    
    private boolean buildAugPath(int[][] mask, int[][] c, int Astart, EdgeSet path){
        print2DArray(mask);
        for(int j = 0; j < mask[0].length; j++){
            if(c[Astart][j] == 0 && mask[Astart][j] == 2){
                c[Astart][j] = 1; // can't visit this twice.
                if(!BisInMatching(j, mask)){
                    path.add(new Edge(Astart, j, Edge.Direction.fromBuyer));
                    switchEdges(mask, path);
                    return true;
                }
                else{
                    for(int i = 0; i < mask.length; i++){
                        if(c[i][j] == 0 && mask[i][j] == 1){
                            c[i][j] = 1;
                            path.add(new Edge(Astart, j, Edge.Direction.fromBuyer));
                            path.add(new Edge(i, j, Edge.Direction.fromSeller));
                            System.out.println(path.toString());
                            return buildAugPath(mask, c, i, path);
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean eliminateAltPath(int[][] mask, double[] Oa, double[] Ra){
        for(int i = 0; i < mask.length; i++){
            if(Oa[i] > Ra[i] && buildAltPath(mask, new int[mask.length][mask[0].length], Oa, Ra, i, new EdgeSet())){
                return false;
            }
        }
        return true;
    }
    
    private boolean buildAltPath(int[][] mask, int[][] c, double[] Oa, double[] Ra, int Astart, EdgeSet path){
        if(!path.isEmpty() && Oa[Astart] == Ra[Astart] && AisInMatching(Astart, mask)/* check Oi == Ri and no more edges in matching on node.*/){
            switchEdges(mask, path);
            return true;
        }
        print2DArray(mask);
        for(int j = 0; j < mask[0].length; j++){
            if(c[Astart][j] == 0 && mask[Astart][j] == 2){
                c[Astart][j] = 1; // can't visit this twice.
                if(BisInMatching(j, mask)){
                    for(int i = 0; i < mask.length; i++){
                        if(c[i][j] == 0 && mask[i][j] == 1){
                            c[i][j] = 1;
                            path.add(new Edge(Astart, j, Edge.Direction.fromBuyer));
                            path.add(new Edge(i, j, Edge.Direction.fromSeller));
                            System.out.println(path.toString());
                            return buildAltPath(mask, c, Oa, Ra, i, path);
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void switchEdges(int[][] mask, EdgeSet path){
        esq.push(path);
        System.out.println("in switchEdges...");
        Edge curr;
        while(path.hasNext()){
            curr = path.getNext();
            //System.out.println(curr.toString());
            if(curr.d == Edge.Direction.fromBuyer)
                mask[(Integer) curr.getBuyer()][(Integer) curr.getSeller()] = 1;
            else if(curr.d == Edge.Direction.fromSeller)
                mask[(Integer) curr.getBuyer()][(Integer) curr.getSeller()] = 2;
            else{
                // error
            }
        }
        return;
    }
    
    private boolean AisInMatching(int row, int[][] mask){
        for(int j = 0; j < mask[row].length; j++){
            if(mask[row][j] == 1)
                return true;
        }
        return false;
    }
    
    private boolean BisInMatching(int col, int[][] mask){
        for(int i = 0; i < mask.length; i++){
            if(mask[i][col] == 1)
                return true;
        }
        return false;
    }
    
//    public void switchAltPaths(int[][] mask, int[] ac, int[] bc, int Astart){
//        for(int i = 0; i < mask[0].length; i++){
//            if(mask[Astart][i] == 2 && ac[Astart] == 0){
//                ac[Astart] = 1; // can't visit this twice.
//                mask[Astart][i] = 1;
//                for(int j = 0; j < mask.length; j++){
//                    if(mask[j][i] == 1 && bc[i] == 0 && j != Astart){
//                        bc[i] = 1;
//                        mask[j][i] = 2;
//                        switchAltPaths(mask, ac, bc, j);
//                    }
//                }
//            }
//        }
//        return;
//    }
    
//    public boolean isSwitchable(int[][] mask, double[] Oa, double[] Ra, int Astart){
//        if(AisInMatching(Astart, mask) && Oa[Astart] == Ra[Astart])
//            return true;
//        for(int i = 0; i < mask[0].length; i++){
//            if(mask[Astart][i] == 2){
//                for(int j = 0; j < mask.length; j++){
//                    if(mask[j][i] == 1)
//                        return isSwitchable(mask, Oa, Ra, j);
//                }
//            }
//        }
//        return false;
//    }
    
    public void recursiveTraverse(int[][] mask, int[][] c, int Astart, ArrayList[] AinT, ArrayList[] BinT){
        if(!AinT[0].contains(Astart)) // shouldn't need this conditional...
            AinT[0].add(new Integer(Astart));
        for(int j = 0; j < mask[0].length; j++){
            if(c[Astart][j] == 0 && mask[Astart][j] == 2){
                c[Astart][j] = 1;
                if(!BinT[0].contains(j))
                    BinT[0].add(new Integer(j));
                for(int i = 0; i < mask.length; i++){
                    if(c[i][j] == 0 && mask[i][j] == 1){
                        c[i][j] = 1;
                        recursiveTraverse(mask, c, i, AinT, BinT);
                    }
                }
            }
        }
        return;
    }
        
    public MEPack go(){
        // print this shtuff <-- assignment
	int[][] assignment = new int[array.length][2];
		
	System.out.println("\n(Printing out only 2 decimals)\n");
	System.out.println("The matrix is:");
	for (int i=0; i<array.length; i++){
            for (int j=0; j<array[i].length; j++){
                System.out.printf("%.2f\t", array[i][j]);
            }
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
						
	System.out.println("The winning assignment (sum) is:\n");
	double sum = 0;
	for (int i=0; i<assignment.length; i++){
            System.out.printf("array(%d,%d) = %.2f\n", (assignment[i][0]+1), (assignment[i][1]+1),
            array[assignment[i][0]][assignment[i][1]]);
            assignedWeights[assignment[i][0]][assignment[i][1]] = array[assignment[i][0]][assignment[i][1]];
            sum = sum + array[assignment[i][0]][assignment[i][1]];
	}
		
        System.out.printf("\nThe max is: %.2f\n", sum);
                
	return new MEPack(new Matrix(assignedWeights), this.esq);
    }
         
    public void print2DArray(int[][] array){
        for (int i=0; i<array.length; i++){
            for (int j=0; j<array[i].length; j++){
                System.out.print(array[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }
	
    public double[][] copyOf(double[][] original){
        double[][] copy = new double[original.length][original[0].length];
	for (int i=0; i<original.length; i++){
            //Need to do it this way, otherwise it copies only memory location
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
	}
	return copy;
    }
    
//    public void parsePPF(){
//        FileOpenService fos;
//        FileContents fc = null;
//        try {
//            fos = (FileOpenService) ServiceManager.lookup("javax.jnlp.FileOpenService");
//        }
//        catch(UnavailableServiceException e) {
//            fos = null;
//            System.err.println("whoops!");
//        }
//        if(fos != null){
//            try{
//                String[] appendage = {".csv"};
//                fc = fos.openFileDialog(null, appendage);
//            }
//            catch(Exception e){}
//        }
//        
//        InputStream is = fc.getInputStream();
//        BufferedReader br = new BufferedReader(is);
//        String line;
//        while((line = br.))
//    }
}