package ParallelKmeans;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import java.lang.Math;

public class CreateLabel {
    
    public static class  CreateLabelMap extends Mapper<LongWritable, Text, IntWritable, Text> {

        TreeMap<Integer,FeatureWritable> CenteroidMap;

        public void setup(Context context) throws IOException, InterruptedException {
            
            /* Input: List of [ClusterID]: [N Dimensional Vector] */
                     
            Configuration conf = context.getConfiguration();
            StringTokenizer stk = new StringTokenizer(conf.get("Centeroid"),",");
            CenteroidMap = new TreeMap<Integer,FeatureWritable>();
            int ClusterID = 0;
            
            while(stk.hasMoreElements()) {    
                StringTokenizer stk2 = new StringTokenizer(stk.nextToken(),":");
                if(stk2.hasMoreElements())
                    ClusterID = Integer.parseInt(stk2.nextToken().trim());
                if(stk2.hasMoreElements())              
                    CenteroidMap.put(ClusterID, new FeatureWritable(stk2.nextToken()));
            }
            
        } 
          
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            
            /* Input: [Label]: [N-Dimensional Vector] */
            
            double minDistance = 1000000000;
            double Distance = 1000000000;
            String ImageLabel = "";
            int ClosestCluster = 0;
            FeatureWritable Feature = null;
            
            StringTokenizer stk = new StringTokenizer(value.toString(),":");
            
            if(stk.hasMoreElements())
                ImageLabel = stk.nextToken();
            if(stk.hasMoreElements())
                Feature = new FeatureWritable(ImageLabel,stk.nextToken());
            
            /* Find the Cloest Cluster */
            
            for(Integer d : CenteroidMap.keySet()){
                
                Distance = Feature.ComputeDistance(CenteroidMap.get(d));
                if(Distance < minDistance){
                    minDistance = Distance;
                    ClosestCluster = d; 
                }
                
            }
            
            context.write(new IntWritable(ClosestCluster), new Text(minDistance + " " + Feature.getLabel()));
             
        }
        
    }
    
    public static class  CreateLabelReduce extends Reducer<IntWritable, Text, IntWritable, Text> { 
        
        double CentralPercentage;

        public void setup(Context context) throws IOException, InterruptedException {
                     
            Configuration conf = context.getConfiguration();
            CentralPercentage = Double.parseDouble(conf.get("CentralPercentage"));
            
        }
        
    	public void reduce(IntWritable key, Iterable<Text> value, Context context) throws IOException, InterruptedException {

            ArrayList<String> DataPoint = new ArrayList<String>();
            
            for(Text s : value)
                DataPoint.add(s.toString());
            
            /* Sort the DataPoint by Distance */
            
            Collections.sort(DataPoint, new Comparator() {

                public int compare(Object s1, Object s2) {

                    StringTokenizer stk = new StringTokenizer((String)s1);
                    StringTokenizer stk2 = new StringTokenizer((String)s2);
                    Double x1 = Double.parseDouble(stk.nextToken());
                    Double x2 = Double.parseDouble(stk2.nextToken());
                    
                    return x1.compareTo(x2); 
                    
            }});
            
            HashMap<String, Integer> LabelCounter = new HashMap<String, Integer>();
            
            for(int i = 0; i < (int)(DataPoint.size() * CentralPercentage); i++){
                
                StringTokenizer stk = new StringTokenizer(DataPoint.get(i));
                String Label = "";
                
                if(stk.hasMoreElements())
                    stk.nextToken();
                if(stk.hasMoreElements())
                    Label = stk.nextToken();
                
                Integer Count = LabelCounter.get(Label);
                   
                if(Count == null)
                    LabelCounter.put(Label,1);
                else{
                    Count = Count + 1;
                    LabelCounter.put(Label,Count);
                }
                
            }
            
            String majorLabel = "";
            int majorLabelCount = -1;
            
            for(String s : LabelCounter.keySet()){
                if(LabelCounter.get(s) > majorLabelCount){
                    majorLabel = s;
                    majorLabelCount = LabelCounter.get(s);
                }
            }
            
            context.write(key, new Text(majorLabel));
            
        }
        
    }    
    
    public static String CreateLabelDriver(String inDir, String outDir, String FinalCenteroid, double CentralPercentage) throws Exception{
        
            Configuration conf = new Configuration();
            conf.set("Centeroid", FinalCenteroid);
            conf.set("CentralPercentage", String.valueOf(CentralPercentage));
            
            Job job1 = new Job(conf, "CreateLabel");
 
            job1.setJarByClass(ParallelKmeans.class);
            job1.setMapperClass(CreateLabelMap.class);
            job1.setReducerClass(CreateLabelReduce.class);

            job1.setMapOutputKeyClass(IntWritable.class);
            job1.setMapOutputValueClass(Text.class);
            job1.setOutputKeyClass(IntWritable.class);
            job1.setOutputValueClass(Text.class);

            
            FileInputFormat.addInputPath(job1, new Path(inDir));
            FileOutputFormat.setOutputPath(job1, new Path(outDir));
            
            FileSystem fs = FileSystem.get(conf);
            long FileLength = fs.listStatus(new Path(inDir.replaceAll("\\*", "")))[0].getLen();
            FileInputFormat.setMaxInputSplitSize(job1,(FileLength/20));
            job1.setNumReduceTasks(1);
            
            job1.waitForCompletion(true);
            
            InputStream in = fs.open(new Path(outDir + "/part-r-00000"));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuffer sb = new StringBuffer();
            while((line = br.readLine()) != null){
               line = line.trim();
               sb.append(line).append(",");
            } 
            
            //fs.delete(new Path(outDir), true);
            
            return sb.toString();
            
    }
    
}
