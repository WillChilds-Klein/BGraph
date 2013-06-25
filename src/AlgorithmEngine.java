import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import javax.jnlp.FileContents;
import javax.jnlp.FileSaveService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

public class AlgorithmEngine {
	
	Matrix matrix;
	ArrayList<Integer> buyers;
	ArrayList<Integer> sellers;
	
	public AlgorithmEngine(Matrix matrix, ArrayList<Integer> buyers, ArrayList<Integer> sellers){
		this.matrix = matrix;
		this.buyers = buyers;
		this.sellers = sellers;
	}
	
	public MEPack hungarian(){
            MEPack mep = new HungarianAlgorithm(matrix, buyers, sellers).go();
                
            FileSaveService fss;
            try {
                fss = (FileSaveService) ServiceManager.lookup("javax.jnlp.FileSaveService");
            }
            catch(UnavailableServiceException e) {
                fss = null;
                System.out.println("whoops!");
            }
            if(fss != null){
    		try{
                    String str = mep.getMatrix().toString();
                    str += "Final Q values: " + mep.getEdgeSetQueue().getFinalQ().toString() + "\n";
                    str += "Final P values: " + mep.getEdgeSetQueue().getFinalP().toString();
                    InputStream is = new ByteArrayInputStream(str.getBytes());
                    String[] appendage = {"csv", "txt"};
                    fss.saveFileDialog(null, appendage, is, "FinalMatching");
    		}
    		catch(Exception e){}
            }
                
		return mep;
	}
        
        public MEPack hungarianGeneralization(){
            MEPack mep = new HungarianGeneralization(matrix, buyers, sellers).go();
                
            FileSaveService fss;
            FileContents fc = null;
            fc = null;
            try {
                fss = (FileSaveService) ServiceManager.lookup("javax.jnlp.FileSaveService");
            }
            catch(UnavailableServiceException e) {
                fss = null;
                System.out.println("whoops!");
            }
            if(fss != null){
    		try{
                    String str = mep.getMatrix().toString();
                    //str += "Final Q values: " + mep.getEdgeSetQueue().getFinalQ().toString() + "\n";
                    //str += "Final P values: " + mep.getEdgeSetQueue().getFinalP().toString();
                    InputStream is = new ByteArrayInputStream(str.getBytes());
                    String[] appendage = {"csv", "txt"};
                    fc = fss.saveFileDialog(null, appendage, is, "FinalMatching");
    		}
    		catch(Exception e){}
            }
                
		return mep;
        }
	
	public EdgeSetQueue complete(){
		EdgeSetQueue esq = new EdgeSetQueue();
		EdgeSet temp = new EdgeSet(); // stores state of screen
		EdgeSet temp2; // push this onto esq, clones temp
		int i = 0,j = 0;
//		for(String b : buyers){
//			for(String s : sellers){
//				Edge p = new Edge(i,j);
//				temp.add(p);
//				temp2 = temp.clone();
//				esq.push(temp2);
//				temp.remove(p);
//				j++;
//			}
//			j = 0;
//			i++;
//		}
		System.out.println(esq.toString());
		esq.push(new EdgeSet());
		return esq;
	}
}
