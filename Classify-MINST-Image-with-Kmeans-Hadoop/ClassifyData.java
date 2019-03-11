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

public class ClassifyData {
    
   public static class  ClassifyDataMap extends Mapper<LongWritable, Text, IntWritable, Text> {

        TreeMap<Integer,FeatureWritable> CenteroidMap;

        public void setup(Context context) throws IOException, InterruptedException {
            
            /* Input: List of [ClusterID]: [N-Dimensional Vector] */
                     
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
            
            context.write(new IntWritable(ClosestCluster), new Text(Feature.getLabel()));
             
        }
        
    }
    
    public static class  ClassifyDataReduce extends Reducer<IntWritable, Text, IntWritable, Text> {
        
        /* Input: [ClusterID] [Label] */
        
        TreeMap<Integer,String> CenteroidLabel;

        public void setup(Context context) throws IOException, InterruptedException {
                     
            Configuration conf = context.getConfiguration();
            StringTokenizer stk = new StringTokenizer(conf.get("Label"),",");
            CenteroidLabel = new TreeMap<Integer,String>();
            int ClusterID = 0;
            
            while(stk.hasMoreElements()) {
                StringTokenizer stk2 = new StringTokenizer(stk.nextToken());
                if(stk2.hasMoreElements())
                    ClusterID = Integer.parseInt(stk2.nextToken());
                if(stk2.hasMoreElements())              
                    CenteroidLabel.put(ClusterID, stk2.nextToken());
            }
            
        }
        
    	public void reduce(IntWritable key, Iterable<Text> value, Context context) throws IOException, InterruptedException {
            
            int TotalImage = 0;
            int TotalCorrectImage = 0;
            double Accuracy = 0;
            String ClusterLabel = CenteroidLabel.get(key.get());
            
            for(Text s : value){
                
                TotalImage = TotalImage + 1;
                if(ClusterLabel.equals(s.toString()))
                    TotalCorrectImage = TotalCorrectImage + 1;
                
            }
            
            Accuracy = (double) TotalCorrectImage / (double)TotalImage;
            
            context.write(key, new Text(TotalImage + " " + ClusterLabel + " " + 
                    TotalCorrectImage + " " + String.valueOf(Accuracy)));
            
        }
        
    }    
    
    public static void  ClassifyDataDriver(String inDir, String outDir, String FinalCenteroid, String CenteroidLabel) throws Exception{
        
            Configuration conf = new Configuration();
            
            conf.set("Centeroid", FinalCenteroid);
            conf.set("Label", CenteroidLabel);
            
            Job job1 = new Job(conf, "ClassifyData");
 
            job1.setJarByClass(ParallelKmeans.class);
            job1.setMapperClass(ClassifyDataMap.class);
            job1.setReducerClass(ClassifyDataReduce.class);

            job1.setMapOutputKeyClass(IntWritable.class);
            job1.setMapOutputValueClass(Text.class);
            job1.setOutputKeyClass(IntWritable.class);
            job1.setOutputValueClass(Text.class);
            
            FileInputFormat.addInputPath(job1, new Path(inDir));
            FileOutputFormat.setOutputPath(job1, new Path(outDir));
            
            FileSystem fs = FileSystem.get(conf);
            long FileLength = fs.listStatus(new Path(inDir.replaceAll("\\*", "")))[0].getLen();
            FileInputFormat.setMaxInputSplitSize(job1,(FileLength/20));

            job1.waitForCompletion(true);
            
            InputStream in = fs.open(new Path(outDir + "/part-r-00000"));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuffer sb = new StringBuffer();
            StringTokenizer stk; 
            
            sb.append("ClsuterID" + " " + "TotalImage" + " " + "ClusterLabel" + " " + "TotalCorrectImage" + " " + "Accuracy");
            System.out.println(sb.toString());
            
            int TotalImage = 0;
            int TotalCorrectImage = 0;
            double Accuracy = 0;
            
            while((line = br.readLine()) != null){
                
               line = line.trim();
               System.out.println(line);
               
               stk = new StringTokenizer(line);
               stk.nextToken();
               TotalImage = Integer.parseInt(stk.nextToken()) + TotalImage;
               stk.nextToken();
               TotalCorrectImage = Integer.parseInt(stk.nextToken()) + TotalCorrectImage;
               
            }
            
            Accuracy =  (double)TotalCorrectImage / (double)TotalImage;  
            System.out.println("TotalSet" + " " + TotalImage  + " " + "N/A" + " " + TotalCorrectImage + " " + Accuracy);
            
    }
    
}
