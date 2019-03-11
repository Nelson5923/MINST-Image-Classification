package ParallelKmeans;
import java.util.*;

public class FeatureWritable {

    String Label = "";
    public ArrayList<Double> FeatureVector;
    
    public FeatureWritable() {
        this.Label = "";
        this.FeatureVector = new ArrayList<Double>();
    }
    
    public FeatureWritable(String s) {
        this.Label = "";
        this.FeatureVector = new ArrayList<Double>();
        StringTokenizer stk = new StringTokenizer(s);
        while(stk.hasMoreElements()) {
            FeatureVector.add(Double.parseDouble(stk.nextToken()));
        }   
    }
    
    public FeatureWritable(String Label, String s) {
        this.Label = Label;
        this.FeatureVector = new ArrayList<Double>();
        StringTokenizer stk = new StringTokenizer(s);
        while(stk.hasMoreElements()) {
            FeatureVector.add(Double.parseDouble(stk.nextToken()));
        }   
    }

    public void sum(FeatureWritable l1){
        
        if(FeatureVector.isEmpty()){
           for(int i = 0; i < l1.size(); i++)
                FeatureVector.add(l1.get(i));
        }else if(FeatureVector.size() == l1.size()){     
            for(int i = 0; i < l1.size(); i++)
                FeatureVector.set(i, FeatureVector.get(i) + l1.get(i));
        }
   
    }
    
    public double get(int i) {
        return FeatureVector.get(i);
    }
    
    public String getLabel(){
        return Label;
    }
    
    public int size(){
        return FeatureVector.size();
    }
    
    public double ComputeDistance(FeatureWritable l1){
        
        if(FeatureVector.size() != l1.size())
            return -1;
        
        double distance = 0;
        
        for(int i = 0; i < FeatureVector.size(); i++)
            distance += Math.pow((FeatureVector.get(i) - l1.get(i)), 2);
        
        return distance;
        
    }
    
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        
        for(Double d : FeatureVector)
            sb.append(String.valueOf(d)).append(" ");
        
        return sb.toString().trim();
        
    }
    
    public void divide(double Total){
        
        for(int i = 0; i < FeatureVector.size(); i++)
            FeatureVector.set(i, FeatureVector.get(i)/Total);    
        
    }
    
}
