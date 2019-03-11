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

public class ParallelKmeans { 
            
    public static class KmeansMap extends Mapper<LongWritable, Text, IntWritable, Text> {
        
        TreeMap<Integer,FeatureWritable> CenteroidMap;

        public void setup(Context context) throws IOException, InterruptedException {    
                     
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
            
            double minDistance = 1000000000;
            StringTokenizer stk = new StringTokenizer(value.toString(),":");
            stk.nextToken();
            FeatureWritable Point = new FeatureWritable(stk.nextToken());       
            int ClusterID = 0;
            
            for(Integer d : CenteroidMap.keySet()){
                
                FeatureWritable Centeroid = CenteroidMap.get(d);
                
                double Distance = Point.ComputeDistance(Centeroid);
                
                if(Distance < minDistance){
                    minDistance = Distance;
                    ClusterID = d;
                }
                             
            }
            
            context.write(new IntWritable(ClusterID), new Text("1" + "," + Point.toString()));
            
        }
        
    }    

    public static class KmeansReduce extends Reducer<IntWritable, Text, IntWritable, Text> {
        
    	public void reduce(IntWritable key, Iterable<Text> value, Context context) throws IOException, InterruptedException {
            
            int Total = 0;
            FeatureWritable Mean = new FeatureWritable();
            FeatureWritable Point = new FeatureWritable();
        
            for(Text t : value){
                
                StringTokenizer stk = new StringTokenizer(t.toString(),",");
                if(stk.hasMoreElements())
                    Total = Total + Integer.parseInt(stk.nextToken());
                if(stk.hasMoreElements())
                    Point = new FeatureWritable(stk.nextToken());
                
                Mean.sum(Point);
                
            }
            
            Mean.divide(Total);
            context.write(key, new Text(":" + Mean.toString()));
            
        }
        
    }    
    
    public static void main(String[] args) throws Exception{
         
        String Centeroid;
        String TrainDir = args[0];
        String TestDir = args[1];
        String outDir = args[2];
        int Iteration = Integer.parseInt(args[3]);
        double CentralPercentage = Double.parseDouble(args[4]);
        int ClusterNumber = Integer.parseInt(args[5]);
        String tmpDir;
        
        Centeroid = ChooseInitial.ChooseInitialDriver(TrainDir, ClusterNumber);
        
        //Centeroid = "1 :2.6 1.4,2 :3.2 3";

        for(int i = 0; i < Iteration; i++){
            
            Configuration conf = new Configuration();
            conf.set("Centeroid", Centeroid);
            
            Job job1 = new Job(conf, "ParallelKmeans");
 
            job1.setJarByClass(ParallelKmeans.class);
            job1.setMapperClass(KmeansMap.class);
            job1.setReducerClass(KmeansReduce.class);

            job1.setMapOutputKeyClass(IntWritable.class);
            job1.setMapOutputValueClass(Text.class);
            job1.setOutputKeyClass(IntWritable.class);
            job1.setOutputValueClass(Text.class);

            tmpDir = TrainDir.replaceAll("\\/\\*", "") + "-tmp-" + String.valueOf(i);
            
            FileInputFormat.addInputPath(job1, new Path(TrainDir));
            FileOutputFormat.setOutputPath(job1, new Path(tmpDir));
            
            FileSystem fs = FileSystem.get(conf);
            long FileLength = fs.listStatus(new Path(TrainDir.replaceAll("\\*", "")))[0].getLen();
            FileInputFormat.setMaxInputSplitSize(job1,(FileLength/40));
            job1.setNumReduceTasks(20);

            job1.waitForCompletion(true);
            
            Centeroid = ReadCenteroid.ReadCenteroidDriver(tmpDir , tmpDir + "-Adjust");    
           
            fs.delete(new Path(tmpDir), true);
                      
        }
        
        tmpDir = TrainDir.replaceAll("\\/\\*", "") + "-label";
        String CenteroidLabel = CreateLabel.CreateLabelDriver(TrainDir, tmpDir, Centeroid, CentralPercentage);
        ClassifyData.ClassifyDataDriver(TestDir, outDir, Centeroid, CenteroidLabel);
        
    }
    
}
