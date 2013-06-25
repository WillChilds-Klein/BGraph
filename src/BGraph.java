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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.jnlp.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class BGraph extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private static ArrayList<Integer> buyers;
	private static ArrayList<Integer> sellers;
	private HashMap<Integer,HashMap<Integer,Integer>> edgeCapacity;
	private EdgeSet edges;
        private EdgeSet original;
	private Matrix matrix;
	private JFrame frame;
	EdgeSetQueue esq;
        int barb;  
        double phi;
        private Image BuyerImage, HouseImage;
        private Icon firstIcon, prevIcon, nextIcon, lastIcon;
        private JButton first, prev, next, last;
        private JPanel jp;
        
	public void paintComponent(Graphics g){
            
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
            System.out.println("!2!3! " + e.toString());
            float normThickness = (weight - matrix.getSM()) / (matrix.getLM() - matrix.getSM()) * normConstant;
            if(weight > 0 && normThickness < 1)
    		normThickness = 1;
            if(weight > 0) 
    		g2.setStroke(new BasicStroke(normThickness));
            // determine color based on directionality
            double x1 = 0, y1 = 0, x2 = 0, y2 = 0, theta = 0;
            Line2D.Double line = null;
            if(e.d == Edge.Direction.fromBuyer){
                g2.setPaint(Color.BLACK); // subject to change
                x2 = 100;
                y1 = ((Integer) e.getSeller())*65 + 50+25;
                x1 = 250;
                y2 = ((Integer) e.getBuyer())*65 + 50+25;
            }
            if(e.d == Edge.Direction.fromSeller){
                g2.setPaint(Color.CYAN.darker()); // subject to change
                x2 = 250;
                y1 = ((Integer) e.getBuyer())*65 + 50+25;
                x1 = 100;
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
                line = new Line2D.Double(x2,y2,x1,y1);
                theta = Math.atan2(y1-y2, x1-x2);
                drawArrow(g2, theta, x1, y1);
                g2.draw(line);
            }
                
    	}
        edges.resetIndex();
        
    	// Draw and fill in buyer nodes.
        ArrayList q = edges.getQ();
        //System.out.println("$$$" + q.toString());
        ArrayList p = edges.getP();
        String s;
    	for(int i = 0; i < buyers.size(); i++){
                s = "" + buyers.get(i);
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
                s = "" + sellers.get(i);
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
                s = "" + sellers.get(i);
    		g.setColor(Color.BLACK);
    		g.drawImage(HouseImage, 550, i*65+50, this);
                //g.fillOval(550, i*65 + 50, 50, 50);
    		g.setColor(Color.BLACK);
    		g.drawString(s, 550+65, i*65 + 50+25);
    	}
        
        e = null;
        //System.out.println(original.toString());
        Graphics2D g2 = (Graphics2D) g;
        while(original.hasNext()){
            e = original.getNext();
            //System.out.println(e.toString());
            //Graphics2D g2 = (Graphics2D) g;
            // normalize weights to make noticeable line thickness gradient
            float normConstant = 5; // empirically determined to provide good thickness gradient
            float weight = (float) matrix.get((Integer) e.getBuyer(), (Integer) e.getSeller());
            float normThickness = (weight - matrix.getSM()) / (matrix.getLM() - matrix.getSM()) * normConstant;
            if(normThickness < 1)
    		normThickness = 1;
            g2.setStroke(new BasicStroke(normThickness));
            double x1 = 400;
            double y1 = ((Integer) e.getBuyer())*65 + 50+25;
            double x2 = 550;
            double y2 = ((Integer) e.getSeller())*65 + 50+25;
            Line2D.Double line = new Line2D.Double(x1,y1,x2,y2);
            g2.draw(line);
        }
        original.resetIndex();
        
        
    }
        
    public void actionPerformed(ActionEvent e){
        this.removeAll();
        //edges = esq.getCurr();
        if(e.getActionCommand().equals("first")){
            while(esq.hasPrev()) // linear time, could be constant but meh...
                edges = esq.getPrev(); // not really worth it, ya know?
        }
        else if(e.getActionCommand().equals("prev")){
            if(esq.hasPrev())
                edges = esq.getPrev();
        }
        else if(e.getActionCommand().equals("next")){
            if(esq.hasNext())
                edges = esq.getNext();
        }
        else if(e.getActionCommand().equals("last")){
            while(esq.hasNext())
                edges = esq.getNext();
            System.out.println("you've pressed last!!");
        }
        else{
            // error
        }
        // enable/disable as appropriate
        if(!esq.hasPrev()){
            first.setEnabled(false);
            prev.setEnabled(false);
        }
        else{
            first.setEnabled(true);
            prev.setEnabled(true);
        }
        if(!esq.hasNext()){
            next.setEnabled(false);
            last.setEnabled(false);
        }
        else{
            next.setEnabled(true);
            last.setEnabled(true);
        }
        frame.repaint();
    }
    
    public void draw(){
        int height=0;
        if (buyers.size()>=sellers.size())
            height = buyers.size();
        else
            height = sellers.size();
        System.out.println("We are starting display");
        frame = new JFrame ();
        frame.setBackground(Color.WHITE); // make better white
        frame.setTitle ("Bipartite Graph");
        frame.setSize (800, Math.max(height*75, 500)); // enough space for buttons
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        // Add the canvas and show the JFrame
        //frame.add(this);
        frame.add(this);
        frame.setVisible(true);
        // set tooltips
        first.setToolTipText("See initial matching.");
        prev.setToolTipText("See matching in previous iteration.");
        next.setToolTipText("See matching in next iteration.");
        last.setToolTipText("See final matching.");
        // add buttons
        JPanel btnpnl = new JPanel();
        btnpnl.add(first);
        btnpnl.add(prev);
        btnpnl.add(next);
        btnpnl.add(last);
        first.setEnabled(false);
        prev.setEnabled(false);
        if(!esq.hasNext()){
            next.setEnabled(false);
            last.setEnabled(false);
        }
        else{
            next.setEnabled(true);
            last.setEnabled(true);
        }
        frame.add(btnpnl);
    }
    
	public BGraph(String algorithm){
	    buyers = new ArrayList<Integer>();
	    sellers = new ArrayList<Integer>();
//	    buyerNetwork = new HashMap<String, ArrayList<String>>();
//	    sellerNetwork = new HashMap<String, ArrayList<String>>();
	    edgeCapacity = new HashMap<Integer, HashMap<Integer, Integer>>();
	    edges = new EdgeSet();
	    //createNetworks();
            FileOpenService fos;
            FileContents fc = null;
            barb = 20;                   // barb length  
            phi = Math.PI/6;             // 30 degrees barb angle
            // initialize button icons
            ClassLoader cl;
            try{
                cl = this.getClass().getClassLoader();
                firstIcon = new ImageIcon(ImageIO.read(cl.getResourceAsStream("images/first.png")).getScaledInstance(15, 15, Image.SCALE_DEFAULT));
                prevIcon = new ImageIcon(ImageIO.read(cl.getResourceAsStream("images/prev.png")).getScaledInstance(15, 15, Image.SCALE_DEFAULT));
                nextIcon = new ImageIcon(ImageIO.read(cl.getResourceAsStream("images/next.png")).getScaledInstance(15, 15, Image.SCALE_DEFAULT));
                lastIcon = new ImageIcon(ImageIO.read(cl.getResourceAsStream("images/last.png")).getScaledInstance(15, 15, Image.SCALE_DEFAULT));
            }
            catch(IOException e){}
            // init button stuff
            // start codes for drawing buttons
            first = new JButton(firstIcon);
            first.setActionCommand("first");
            first.addActionListener(this);
            prev = new JButton(prevIcon);
            prev.setActionCommand("prev");
            prev.addActionListener(this);
            next = new JButton(nextIcon);
            next.setActionCommand("next");
            next.addActionListener(this);
            last = new JButton(lastIcon);
            last.setActionCommand("last");
            last.addActionListener(this);
            jp = new JPanel();
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
                cl = this.getClass().getClassLoader();
                BuyerImage = ImageIO.read(cl.getResourceAsStream("images/buyer.png")).getScaledInstance(60, 60, Image.SCALE_DEFAULT);
                HouseImage = ImageIO.read(cl.getResourceAsStream("images/house.png")).getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            }
            //catch(java.net.URISyntaxException uri){}
            catch(IOException e){}
            
            if(algorithm.equals("hungarian"))
	    	esq = (new AlgorithmEngine(matrix, buyers, sellers)).hungarian().getEdgeSetQueue();
            else if(algorithm.equals("hungarianGeneralization"))
                esq = (new AlgorithmEngine(matrix, buyers, sellers)).hungarianGeneralization().getEdgeSetQueue();
	    else if(algorithm.equals("complete"))
	    	esq = (new AlgorithmEngine(matrix, buyers, sellers)).complete();
	}
    
//    private void animate(){
//    	while(esq.hasNext()){
//    		edges = esq.getNext();
//		this.repaint();
//    		try{ Thread.sleep(4000); } catch(InterruptedException e){}
//    	}
//    			
//    }
	
	public void buildFromFile (InputStream is) throws java.io.FileNotFoundException{
		System.out.println("We are starting parsing");
		try{
			this.parseCSV(is);
		}
		catch(IOException e){
			// fail gracefully
			System.exit(1);
		}
		
		// store values of edge weights. hash by buyer, then seller to find edge.
		Integer tempBuyer, tempSeller;
		Integer tempWeight;
		for(int i = 0; i < matrix.getM(); i++){ // if matrix doesn't include id's, change i to 0
			tempBuyer = buyers.get(i); 
			for(int j = 0; j < matrix.getN(); j++){ // if matrix doesn't include id's, change i to 0
				tempSeller = sellers.get(j); 
				tempWeight = matrix.get(i, j);
				if(edgeCapacity.get(tempBuyer) == null) {
					edgeCapacity.put(tempBuyer, new HashMap<Integer, Integer>());
				}
				if(tempWeight.equals("-")) // no edge
					edgeCapacity.get(tempBuyer).put(tempSeller, 0); // max int
				else
					edgeCapacity.get(tempBuyer).put(tempSeller, tempWeight); // store weight as Integer
			}
		}
	}
	
	
    
    public static void main (String args[]){
    	/**/
    	BGraph bgraph = new BGraph("hungarian");
    	/**/
    	if(buyers.size() <= 10){
            bgraph.draw();
        }
    }

	public void parseCSV(InputStream is) throws IOException{
    	// NOTE: need to make this more robust. test more edge cases and fail gracefully
    	// NOTE: if this crashes on some kind of parsing error, the whole applet crashes...
    	// USAGE: for use with CSV files of letter-strings integers (not decimals). delimiter is any non-alpha-numeric character.
    	// EVENTUALLY: change this to parse doubles so we can have non-integer weights.
    	// '-' char denotes no edge between nodes. any other non-alphanumeric character will not be parsed.
    	
    	ArrayList<ArrayList<Integer>> arr = new ArrayList<ArrayList<Integer>>();
//    	if(!filepath.substring(filepath.length()-4, filepath.length()).equals(".csv")){ // check .csv file appendage
//    		System.err.println("Please build from a .csv file!");
//    		// fail gracefully?
//    	}
    		
    	BufferedReader input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    	String currLine;
    	ArrayList<Integer> temp;
    	while((currLine = input.readLine()) != null){
    		temp = new ArrayList<Integer>();
    		String [] vals = currLine.split("[^0-9]*[^0-9]"); // toggle for multiple delimiter chars case
    		for (String str : vals){
    			temp.add(Integer.parseInt(str));
    		}
    		arr.add(temp);
    	}
    	
    	/**print matrix for testing**/
    	for(ArrayList<Integer> r : arr){
    		for(Integer i : r)
    			System.out.print(i + ", ");
    		System.out.println();
    	}/**/
    	
    	// sellers will be populated by first row of adjacency matrix.
		for(int i = 0; i < arr.get(0).size(); i++){ // if matrix doesn't include id's, change i to 0
			this.sellers.add(new Integer(i));
		}

		// buyers will be populated by first column of adjacency matrix.
		for(int i = 0; i < arr.size(); i++){ // if matrix doesn't include id's, change i to 0
			this.buyers.add(new Integer(i));
		}
		
		matrix = new Matrix(arr.size(), arr.get(0).size());
		for(int i = 0; i < arr.size(); i++){
			for(int j = 0; j < arr.get(0).size(); j++){
                            
				this.matrix.put(i, j, arr.get(i).get(j));
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
	// http://stackoverflow.com/questions/4112701/drawing-a-line-with-arrow-in-jav
	
}
