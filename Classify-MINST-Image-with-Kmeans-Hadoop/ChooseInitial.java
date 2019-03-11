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

public class ChooseInitial {
    
    public static class ChooseInitialMap extends Mapper<LongWritable, Text, IntWritable, Text> {

        ArrayList<FeatureWritable> Centeroid;
        
        public void setup(Context context) throws IOException, InterruptedException {
            
            /* Input: List of [N-Dimensional Vector] */
            
            Configuration conf = context.getConfiguration();
            StringTokenizer stk = new StringTokenizer(conf.get("Centeroid"),",");
            Centeroid = new ArrayList<FeatureWritable>();
            while(stk.hasMoreElements())
                Centeroid.add(new FeatureWritable(stk.nextToken())); 
            
        }         
          
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            
            /* Input: [N-Dimensional Vector] */
            
            double minDistance = 1000000000;
            double Distance = 1000000000;
            
            StringTokenizer stk = new StringTokenizer(value.toString(),":");
            stk.nextToken();
            FeatureWritable Point = new FeatureWritable(stk.nextToken());  
            
            for(FeatureWritable fw : Centeroid){
                
                Distance = Point.ComputeDistance(fw);
                if(Distance < minDistance)
                    minDistance = Distance;
                
            }
            
            /* Output the minimum distance from data point to centeroid */
            
            context.write(new IntWritable(1), new Text(Point.toString() + "," + minDistance));
             
        }
        
    }
    
    public static class ChooseInitialReduce extends Reducer<IntWritable, Text, Text, Text> {
        
        ArrayList<FeatureWritable> Centeroid;
        
        public void setup(Context context) throws IOException, InterruptedException {
            
            /* Input: List of [N-Dimensional Vector] */
            
            Configuration conf = context.getConfiguration();
            StringTokenizer stk = new StringTokenizer(conf.get("Centeroid"),",");
            Centeroid = new ArrayList<FeatureWritable>();
            while(stk.hasMoreElements())
                Centeroid.add(new FeatureWritable(stk.nextToken())); 
            
        } 
        
    	public void reduce(IntWritable key, Iterable<Text> value, Context context) throws IOException, InterruptedException {

            FeatureWritable Point = null;
            double Distance = 1000000000;
            FeatureWritable maxDistanceFeature = null;
            double maxDistance = -1;
            
            /* Choose the new centeroid with maximum separation to old centeroid from the sample  */
            
            for(Text s : value){
                
                StringTokenizer stk = new StringTokenizer(s.toString(),",");
                if(stk.hasMoreElements())
                    Point = new FeatureWritable(stk.nextToken());
                if(stk.hasMoreElements())
                    Distance = Double.parseDouble(stk.nextToken());
                if(Distance > maxDistance){
                    maxDistanceFeature = Point;
                    maxDistance = Distance;
                }
                
            }
            
            Centeroid.add(maxDistanceFeature);
            
            /* Output the Centeroid List */
            
            for(FeatureWritable fw : Centeroid)
                context.write(new Text(fw.toString()), new Text(""));
            
        }
        
    }    
    
    public static String ChooseInitialDriver(String inDir, int k) throws Exception{
        
            Configuration conf = new Configuration();
            
            FileSystem fs = FileSystem.get(conf);
            String inName = fs.listStatus(new Path(inDir.replaceAll("\\*", "")))[0].getPath().toString();
            InputStream in = fs.open(new Path(inName));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            FeatureWritable Mean = new FeatureWritable();
            StringTokenizer stk;
            int Total = 0;
            while((line = br.readLine()) != null){
                stk = new StringTokenizer(line, ":");
                stk.nextToken();
                FeatureWritable Record = new FeatureWritable(stk.nextToken());
                Mean.sum(Record);
                Total = Total + 1;
            }
            Mean.divide(Total);
            System.out.println(Mean.toString());
            conf.set("Centeroid", Mean.toString());
            
            for(int i = 0; i < k - 1; i++){
                
                Job job1 = new Job(conf, "ChooseInitial");

                job1.setJarByClass(ParallelKmeans.class);
                job1.setMapperClass(ChooseInitialMap.class);
                job1.setReducerClass(ChooseInitialReduce.class);

                job1.setMapOutputKeyClass(IntWritable.class);
                job1.setMapOutputValueClass(Text.class);
                job1.setOutputKeyClass(Text.class);
                job1.setOutputValueClass(Text.class);

                String OutPath = inDir.replaceAll("\\/\\*", "") + "-initial-" + String.valueOf(i);

                FileInputFormat.addInputPath(job1, new Path(inDir));
                FileOutputFormat.setOutputPath(job1, new Path(OutPath));
                
                fs = FileSystem.get(conf);
                long FileLength = fs.listStatus(new Path(inDir.replaceAll("\\*", "")))[0].getLen();
                FileInputFormat.setMaxInputSplitSize(job1,(FileLength/20));
                
                job1.setNumReduceTasks(1);
                job1.waitForCompletion(true);
                
                in = fs.open(new Path(OutPath + "/part-r-00000"));
                br = new BufferedReader(new InputStreamReader(in));
                line = "";
                StringBuffer sb = new StringBuffer();
                while((line = br.readLine()) != null){
                   line = line.trim();
                   sb.append(line).append(",");
                }                
                
                conf.set("Centeroid", sb.toString());
                //fs.delete(new Path(OutPath), true);
                
            }
            
            /* Gathering the Output and Append the Cluster Index */
            
            stk = new StringTokenizer(conf.get("Centeroid"),",");
            StringBuffer sb = new StringBuffer();
            int i = 1;
            while(stk.hasMoreElements()){
                sb.append(String.valueOf(i)).append(": ").append(stk.nextToken()).append(",");
                i++;
            }
            
            return sb.toString();
            
    }
    
}
