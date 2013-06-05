/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Feng
 */
//package edu.harvard.eecs.airg.coloredtrails.shared.types;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.jnlp.*;

import javax.swing.JFrame;


public class BGraph extends Canvas implements Serializable{
	private static final long serialVersionUID = 1L;
	private static ArrayList<String> buyers;
	private static ArrayList<String> sellers;
	private HashMap<String,ArrayList<String>> buyerNetwork;
	private HashMap<String,ArrayList<String>> sellerNetwork;
	private HashMap<String,HashMap<String,Integer>> edgeCapacity;
	private EdgeSet edges;
        private EdgeSet original;
	private Matrix matrix;
	private JFrame frame;
	EdgeSetQueue esq;
        int barb;  
        double phi;
        private Image BuyerImage;
        private Image HouseImage;

	public void paint(Graphics g){
    	// Set locations of buyer nodes.
//    	HashMap<String, Integer> bLoc = new HashMap<String, Integer>();
//    	for(int i=0;i<buyers.size();i++){
//    		bLoc.put(buyers.get(i), new Integer(65*i+50));
//    	}
//    	// Set locations of seller nodes.
//    	HashMap<String, Integer> sLoc= new HashMap<String, Integer>();
//    	for(int i=0; i < sellers.size(); i++){
//    		sLoc.put(sellers.get(i), new Integer(65*i+50));
//    	}
    	
            
            
        System.out.println("---!!!_--" + edges.toString());
    	// Draw edges. Pass in ArrayList of Buyer/Seller Pairs.
        //Graphics2D g2 = (Graphics2D) g;
    	Edge e;
    	while(edges.hasNext()){
            e = edges.getNext();
            Graphics2D g2 = (Graphics2D) g;
            // normalize weights to make noticeable line thickness gradient on scale from 1-15
            float normConstant = 5; // empirically determined to provide good gradient
            float weight = (float) matrix.get((Integer) e.getBuyer(), (Integer) e.getSeller());
            //System.out.println(p.toString() + "    " + weight);
            float normThickness = (weight - matrix.getSM()) / (matrix.getLM() - matrix.getSM()) * normConstant;
            //System.out.println("normThickness = " + normThickness);
            if(weight > 0 && normThickness < 1)
    		normThickness = 1;
            if(weight > 0) 
    		g2.setStroke(new BasicStroke(normThickness));
            // determine color based on directionality
            double x1 = 0, y1 = 0, x2 = 0, y2 = 0, theta = 0;
            Line2D.Double line = null;
            if(e.d == Edge.Direction.fromBuyer){
                g2.setPaint(Color.BLACK); // subject to change
                x1 = 100;
                y1 = ((Integer) e.getSeller())*65 + 50+25;
                x2 = 250;
                y2 = ((Integer) e.getBuyer())*65 + 50+25;
            }
            if(e.d == Edge.Direction.fromSeller){
                g2.setPaint(Color.CYAN.darker()); // subject to change
                x1 = 250;
                y1 = ((Integer) e.getBuyer())*65 + 50+25;
                x2 = 100;
                y2 = ((Integer) e.getSeller())*65 + 50+25;
            }
            if(e.d == Edge.Direction.none){
                g2.setPaint(Color.BLACK);
                x1 = 0;
                y1 = 0;
                x2 = 0;
                y2 = 0;
            }
            
            if(normThickness >= 1){ // if edge actually exists...double check this. could be source for error
            // in the case that lowest value is normalized improperly...
                line = new Line2D.Double(x1,y1,x2,y2);
                theta = Math.atan2(y2-y1, x2-x1);
                drawArrow(g2, theta, x2, y2);
                g2.draw(line);
            }
                
    	}
        
    	// Draw and fill in buyer nodes.
        ArrayList q = edges.getQ();
        //System.out.println("$$$" + q.toString());
        ArrayList p = edges.getP();
        String s;
    	for(int i = 0; i < buyers.size(); i++){
                s = buyers.get(i);
    		g.setColor(Color.BLACK);
    		//g.fillOval(50, i*65 + 50, 50, 50);
                g.drawImage(BuyerImage, 50, i*65+50, this);
    		g.setColor(Color.WHITE);
                if(q.size() > 0)
                    g.drawString("" + q.get(i), 50+10, i*65 + 50+50); // buyer q
                g.setColor(Color.BLACK);
    		g.drawString(s, 30, i*65 + 50+25);
    	}
    	// Draw and fill in seller nodes.
    	for(int i = 0; i < sellers.size(); i++){
                s = sellers.get(i);
    		g.setColor(Color.BLACK);
    		g.drawImage(HouseImage, 245, i*65+55, this);
                //g.fillOval(250, i*65 + 50, 50, 50);
                g.setColor(Color.WHITE);
                if(p.size() > 0)
    		 g.drawString("" + p.get(i), 250, i*65 + 50+50);
    		g.setColor(Color.BLACK);
    		g.drawString(s, 250+70, i*65 + 50+25);
    	}
        
        // Start code for static graph
        for(int i = 0; i < buyers.size(); i++){
    		g.setColor(Color.BLACK);
                g.drawImage(BuyerImage, 350, i*65+50, this);
    		//g.fillOval(350, i*65 + 50, 50, 50);
    	}
    	// Draw and fill in seller nodes.
    	for(int i = 0; i < sellers.size(); i++){
                s = sellers.get(i);
    		g.setColor(Color.BLACK);
    		g.drawImage(HouseImage, 550, i*65+50, this);
                //g.fillOval(550, i*65 + 50, 50, 50);
    		g.setColor(Color.BLACK);
    		g.drawString(s, 550+65, i*65 + 50+25);
    	}
        
        e = null;
        System.out.println(original.toString());
        Graphics2D g2 = (Graphics2D) g;
        while(original.hasNext()){
            e = original.getNext();
            System.out.println(e.toString());
            //Graphics2D g2 = (Graphics2D) g;
            // normalize weights to make noticeable line thickness gradient
            float normConstant = 5; // empirically determined to provide good thickness gradient
            float weight = (float) matrix.get(Integer.parseInt((String) e.getBuyer())-1, Integer.parseInt((String) e.getSeller())-1);
            float normThickness = (weight - matrix.getSM()) / (matrix.getLM() - matrix.getSM()) * normConstant;
            if(normThickness < 1)
    		normThickness = 1;
            g2.setStroke(new BasicStroke(normThickness));
            double x1 = 400;
            double y1 = (Integer.parseInt((String) e.getBuyer()) - 1)*65 + 50+25;
            double x2 = 550;
            double y2 = (Integer.parseInt((String) e.getSeller()) - 1)*65 + 50+25;
            Line2D.Double line = new Line2D.Double(x1,y1,x2,y2);
            g2.draw(line);
        }
        original.resetIndex();
        
    }
    
    public void draw(){
        int height=0;
        if (buyerNetwork.size()>=sellerNetwork.size())
            height = buyerNetwork.size();
        else
            height = sellerNetwork.size();
        System.out.println("We are starting display");
        frame = new JFrame ();
        frame.setBackground(Color.WHITE); // make better white
        frame.setTitle ("Bipartite Graph");
        frame.setSize (800, Math.max(height*75, 500)); // enough space for buttons
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        // Add the canvas and show the JFrame
        frame.add(this);
        frame.setVisible(true);
    }
    
	public BGraph(String algorithm){
	    buyers = new ArrayList<String>();
	    sellers = new ArrayList<String>();
	    buyerNetwork = new HashMap<String, ArrayList<String>>();
	    sellerNetwork = new HashMap<String, ArrayList<String>>();
	    edgeCapacity = new HashMap<String, HashMap<String, Integer>>();
	    edges = new EdgeSet();
	    createNetworks();
            FileOpenService fos;
            FileContents fc = null;
            barb = 20;                   // barb length  
            phi = Math.PI/6;             // 30 degrees barb angle
            try {
                fos = (FileOpenService) ServiceManager.lookup("javax.jnlp.FileOpenService");
            }
            catch(UnavailableServiceException e) {
                fos = null;
                System.out.println("whoops!");
            }
            if(fos != null){
    		try{
                    String[] appendage = {".csv"};
                    fc = fos.openFileDialog(null, appendage);
    		}
    		catch(Exception e){}
            }
            try{
                if(fc != null) //System.out.println("yay!"); else System.out.println(":(");
                    buildFromFile(fc.getInputStream());
            }
            catch(IOException e){
                e.printStackTrace();
            }
            try{
                BuyerImage = ImageIO.read(new File("buyer.png")).getScaledInstance(60, 60, Image.SCALE_DEFAULT);
                HouseImage = ImageIO.read(new File("house.png")).getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            }
            catch(IOException e){}
            
            if(algorithm.equals("hungarian"))
	    	esq = (new AlgorithmEngine(matrix, buyers, sellers)).hungarian().getEdgeSetQueue();
	    else if(algorithm.equals("complete"))
	    	esq = (new AlgorithmEngine(matrix, buyers, sellers)).complete();
	}
    
    private void animate(){
    	while(esq.hasNext()){
    		edges = esq.pop();
		this.repaint();
    		try{ Thread.sleep(4000); } catch(InterruptedException e){}
    	}
    			
    }
	
	public void buildFromFile (InputStream is) throws java.io.FileNotFoundException{
		System.out.println("We are starting parsing");
		try{
			this.parseCSV(is);
		}
		catch(IOException e){
			// fail gracefully
			System.exit(1);
		}
		
		// populate buyerNetwork. Initialize with complete graph.
		// key is id (represented as String) of buyer node, value is id (represented as String)
		// of corresponding seller node.
		System.out.println("We are parsing the Buyer Network.");
		ArrayList<String> temp;
		for(String b : buyers){
			temp = new ArrayList<String>();
			for(String s: sellers){
				temp.add(s); // making complete graph
			}
			buyerNetwork.put(b,temp);
		}
		
		// populate sellerNetwork. Initialize with complete graph.
		// key is id (represented as String) of seller node, value is id (represented as String)
		// of corresponding buyer node.
		System.out.println("We are parsing the Seller Network.");
		for(String b : buyers){
			temp = new ArrayList<String>();
			for(String s : sellers){
				temp.add(s); // making complete graph
			}
			sellerNetwork.put(b,temp);
		}
		System.out.println("We have finished parsing.");
		
		// store values of edge weights. hash by buyer, then seller to find edge.
		String tempBuyer, tempSeller;
		Integer tempWeight;
		for(int i = 0; i < matrix.getM(); i++){ // if matrix doesn't include id's, change i to 0
			tempBuyer = buyers.get(i); 
			for(int j = 0; j < matrix.getN(); j++){ // if matrix doesn't include id's, change i to 0
				tempSeller = sellers.get(j); 
				tempWeight = matrix.get(j, i);
				if(edgeCapacity.get(tempBuyer) == null) {
					edgeCapacity.put(tempBuyer,new HashMap<String, Integer>());
				}
				if(tempWeight.equals("-")) // no edge
					edgeCapacity.get(tempBuyer).put(tempSeller, 0); // max int
				else
					edgeCapacity.get(tempBuyer).put(tempSeller, tempWeight); // store weight as Integer
			}
		}
	}
	
	public void writeToFile(String f) throws java.io.FileNotFoundException{
	    // write to file. write another csv back out with optimal edges.
	}
	
	public void setBuyer(ArrayList<String> b){
	    buyers = b;
	}
	
	public void setSeller(ArrayList<String> s){
	    sellers = s;
	}
	
	public HashMap<String, ArrayList<String>> getBuyerNetwork(){
	    return buyerNetwork;
	}
	
	public HashMap<String, ArrayList<String>> getSellerNetwork(){
	    return sellerNetwork;
	}
	
    public ArrayList<String> getBuyers(){
	    return buyers;
    }
	
    public ArrayList<String> getSellers(){
	    return sellers;
	}
     
    public Integer getEdgeCapacity(String perGameId, String opponentId){
    	Integer weight = Integer.MAX_VALUE; //infinity
    	if(edgeCapacity.containsKey(perGameId) && edgeCapacity.get(perGameId).containsKey(opponentId)){
    		weight = edgeCapacity.get(perGameId).get(opponentId);
    	}
    	 
    	return weight;
    }

	public void createNetworks(){
	    int nb = buyers.size ();
	    int ns = sellers.size ();
	    
	    // change this bit to scale for more nodes...whats it do?
	    if(nb > 8)
	    	nb = 8;
	    if(ns > 8)
	    	ns = 8;
	    
	    // populate networks
	    for(int i = 0 ; i < buyers.size () ; i++){
	    	buyerNetwork.put (buyers.get (i), new ArrayList<String>());
	    }
	    for(int i = 0 ; i < sellers.size () ; i++){
	    	sellerNetwork.put (sellers.get (i), new ArrayList<String>());
	    }
	}
	
    public ArrayList<String> getNeighbors(String id){
        if (buyerNetwork.keySet().contains(id))
            return (ArrayList<String>)buyerNetwork.get(id);
        else if (sellerNetwork.keySet().contains(id))
            return (ArrayList<String>)sellerNetwork.get(id);
        
        return new ArrayList<String>();
    }
    public String toString(){
        return buyerNetwork.toString();
        
    }
    
    public static void main (String args[]){
    	/**/
    	BGraph bgraph = new BGraph("hungarian");
    	/** /
    	BGraph bgraph = new BGraph("test.csv", "complete");
    	/**/
        //try{ Thread.sleep(2000); } catch(InterruptedException e){}
    	if(buyers.size() <= 10){
            bgraph.draw();
//    	long time = System.currentTimeMillis();
//    	while(System.currentTimeMillis() < time+200){}
        //try{ Thread.sleep(2000); } catch(InterruptedException e){}
            bgraph.animate();
        }
    }

	public void parseCSV(InputStream is) throws IOException{
    	// NOTE: need to make this more robust. test more edge cases and fail gracefully
    	// NOTE: if this crashes on some kind of parsing error, the whole applet crashes...
    	// USAGE: for use with CSV files of letter-strings integers (not decimals). delimiter is any non-alpha-numeric character.
    	// EVENTUALLY: change this to parse doubles so we can have non-integer weights.
    	// '-' char denotes no edge between nodes. any other non-alphanumeric character will not be parsed.
    	
    	ArrayList<ArrayList<String>> arr = new ArrayList<ArrayList<String>>();
//    	if(!filepath.substring(filepath.length()-4, filepath.length()).equals(".csv")){ // check .csv file appendage
//    		System.err.println("Please build from a .csv file!");
//    		// fail gracefully?
//    	}
    		
    	BufferedReader input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    	String currLine;
    	ArrayList<String> temp;
    	while((currLine = input.readLine()) != null){
    		temp = new ArrayList<String>();
    		String [] vals = currLine.split("[^0-9]*[^0-9]"); // toggle for multiple delimiter chars case
    		for (String str : vals){
    			temp.add(str);
    		}
    		arr.add(temp);
    	}
    	
    	/**print matrix for testing**/
    	for(ArrayList<String> r : arr){
    		for(String i : r)
    			System.out.print(i + ", ");
    		System.out.println();
    	}/**/
    	
    	// sellers will be populated by first row of adjacency matrix.
		for(int i = 0; i < arr.get(0).size(); i++){ // if matrix doesn't include id's, change i to 0
			this.sellers.add("" + (i+1));
		}

		// buyers will be populated by first column of adjacency matrix.
		for(int i = 0; i < arr.size(); i++){ // if matrix doesn't include id's, change i to 0
			this.buyers.add("" + (i+1));
		}
		
		matrix = new Matrix(buyers.size(), sellers.size());
		//System.out.println("m = " + matrix.matrix.size() + ", n = " + matrix.matrix.get(0).size());
		for(int i = 0; i < arr.get(0).size(); i++){
			for(int j = 0; j < arr.size(); j++){
				this.matrix.put(i, j, Integer.parseInt(arr.get(i).get(j)));
				//System.out.println(Integer.parseInt(arr.get(i).get(j)));
			}
		}
                
         
            // populate static graph
            original = new EdgeSet();
            //Edge e;
            for(int i = 0; i < buyers.size(); i++){
                for(int j = 0; j < sellers.size(); j++){
                    if(matrix.get(i,j) != 0)
                        original.add(new Edge(buyers.get(i), sellers.get(j), Edge.Direction.none));
                }
            }
            System.out.println("!!!" + original.toString());
    		
    	input.close();
    }
        
private void drawArrow(Graphics2D g2, double theta, double x0, double y0)  {  
    double x = x0 - barb * Math.cos(theta + phi);  
    double y = y0 - barb * Math.sin(theta + phi);  
    g2.draw(new Line2D.Double(x0, y0, x, y));  
    x = x0 - barb * Math.cos(theta - phi);  
    y = y0 - barb * Math.sin(theta - phi);  
    g2.draw(new Line2D.Double(x0, y0, x, y));  
}
	// http://stackoverflow.com/questions/4112701/drawing-a-line-with-arrow-in-java
	
//	private final int ARR_SIZE = 4;
//
//    void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
//        Graphics2D g = (Graphics2D) g1.create();
//
//        double dx = x2 - x1, dy = y2 - y1;
//        double angle = Math.atan2(dy, dx);
//        int len = (int) Math.sqrt(dx*dx + dy*dy);
//        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
//        at.concatenate(AffineTransform.getRotateInstance(angle));
//        g.transform(at);
//
//        // Draw horizontal arrow starting in (0, 0)
//        g.drawLine(0, 0, len, 0);
//        g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
//                      new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
//    }
	
}
