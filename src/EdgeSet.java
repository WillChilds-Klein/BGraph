import java.util.*;

public class EdgeSet extends Object{
	
	private ArrayList<Edge> edgeset;
	private ArrayList<Float> q;
	private ArrayList<Float> p;
        private int index;

	public EdgeSet(){
		this.edgeset = new ArrayList<Edge>();
		this.q = new ArrayList<Float>();
		this.p = new ArrayList<Float>();
                index = 0;
		// make update methods.
	}
	
	public void add(Edge e){ // change Direction
		int flag = 0;
		for(int i = 0; i < edgeset.size(); i++){
			Edge currEdge = edgeset.get(i);
			if(e.getBuyer() == currEdge.getBuyer() && e.getSeller() == currEdge.getSeller()){
				edgeset.remove(i); // got it.
				break;
			}
		}
		
		edgeset.add(e);
	}
	
	public void add(ArrayList<Float> q, ArrayList<Float> p){	
		this.q = (ArrayList<Float>) q.clone();
		this.p = (ArrayList<Float>) p.clone();
	}
	
	public void remove(Edge p){
		edgeset.remove(p);
	}
        
        public void resetIndex(){
            index = 0;
        }
	
	public boolean hasNext(){
		return index < edgeset.size();
	}
	
	public Edge getNext(){
            if(edgeset.size() > 0){
                Edge temp = edgeset.get(index);
                index++;
		return temp;
            }
            else
                return null;
	}
        
        public ArrayList getQ(){
            return this.q;
        }
        
        public ArrayList getP(){
            return this.p;
        }
	
	public EdgeSet clone(){
		EdgeSet ret = new EdgeSet();
		for(Edge p : edgeset)
			ret.add(p);
		return ret;
	}
	
	public String toString(){
		String ret = "";
		for(Edge p : edgeset){
			ret += p.toString() + ",";
		}
		ret += "q = {";
		for(Float f : this.q){
			ret += f.toString() + ",";
		}
		ret += "}";
		ret += " p = {";
		for(Float f : this.p){
			ret += f.toString() + ",";
		}
		ret += "}";
		return ret;
	}
}
