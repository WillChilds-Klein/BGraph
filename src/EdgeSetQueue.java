import java.util.ArrayList;

public class EdgeSetQueue {
	
	private ArrayList<EdgeSet> queue;
	
	public EdgeSetQueue(){
		queue = new ArrayList<EdgeSet>();
	}
	
	public void push(EdgeSet edgeSet){
		queue.add(queue.size(), edgeSet);
	}
	
	public EdgeSet pop(){
		return queue.remove(0);
	}
	
	public EdgeSet peek(){
		return queue.get(0);
	}
        
        public ArrayList getFinalP(){
            return queue.get(queue.size()-1).getP();
        }
        
        public ArrayList getFinalQ(){
            return queue.get(queue.size()-1).getQ();
        }
	
	public void setTail(EdgeSet es){
		queue.set(queue.size()-1, es);
	}
	
	public boolean hasNext(){
		return queue.size() > 0;
	}
	
	public String toString(){
		String ret = "";
		for(EdgeSet e : queue)
			ret += e.toString() + '\n';
		return ret;
	}
}