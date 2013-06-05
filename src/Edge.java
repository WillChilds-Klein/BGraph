public class Edge<buyer, seller/*, Direction*/> {
	
    public static enum Direction {fromBuyer, fromSeller, none};
	
    private buyer b;
    private seller s;
    public Direction d;
    
    public Edge(buyer b, seller s, Direction d){
        this.b = b;
        this.s = s;
        this.d = d;
    }
    public Edge(buyer b, seller s){
        this.b = b;
        this.s = s;
        this.d = Edge.Direction.none;
    }
    public buyer getBuyer(){ return b; }
    public seller getSeller(){ return s; }
    public String toString(){ return "[" + b + "," + s + "," + d + "]"; }
}