/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author will
 */
public class MEPack {
    
    Matrix matrix;
    EdgeSetQueue esq;
    
    public MEPack(Matrix matrix, EdgeSetQueue esq){
        this.matrix = matrix;
        this.esq = esq;
    }
    
    public Matrix getMatrix(){
        return this.matrix;
    }
    
    public EdgeSetQueue getEdgeSetQueue(){
        return this.esq;
    }
    
}
