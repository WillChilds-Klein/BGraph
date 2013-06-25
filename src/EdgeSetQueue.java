import java.util.ArrayList;

public class EdgeSetQueue {
	
	private ArrayList<EdgeSet> queue;
        private int i;
	
	public EdgeSetQueue(){
		queue = new ArrayList<EdgeSet>();
                i = -1;
	}
	
	public void push(EdgeSet edgeSet){
		queue.add(queue.size(), edgeSet);
	}
        
        public boolean hasNext(){
            return i < queue.size()-1;
        }
        
        public boolean hasPrev(){
            return i > 0;
        }
	
	public EdgeSet getNext(){
            i++;
            return queue.get(i);
        }
        
        public EdgeSet getPrev(){
            i--;
            return queue.get(i);
        }
        
        public EdgeSet getCurr(){
            return queue.get(i);
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
	
	public String toString(){
		String ret = "";
		for(EdgeSet e : queue)
			ret += e.toString() + '\n';
		return ret;
	}
}