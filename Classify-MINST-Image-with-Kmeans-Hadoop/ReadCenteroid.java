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

public class ReadCenteroid {

    public static class ReadCenteroidMap extends Mapper<LongWritable, Text, Text, Text> {          
        
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException { 
            
            context.write(value, new Text(""));
             
        }
        
    }    

    public static class ReadCenteroidReduce extends Reducer<Text, Text, Text, Text> {
        
    	public void reduce(Text key, Iterable<Text> value, Context context) throws IOException, InterruptedException {
            
            context.write(key ,new Text(""));
            
        }
        
    }
    
    public static String ReadCenteroidDriver(String inDir, String outDir) throws Exception{

        Configuration conf = new Configuration();

        Job job1 = new Job(conf, "ReadCenteroid");

        job1.setJarByClass(ReadCenteroid.class);
        job1.setMapperClass(ReadCenteroidMap.class);
        job1.setReducerClass(ReadCenteroidReduce.class);

        job1.setMapOutputKeyClass(Text.class);
        job1.setMapOutputValueClass(Text.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        
        FileInputFormat.addInputPath(job1, new Path(inDir));   
        FileOutputFormat.setOutputPath(job1, new Path(outDir));
        
        job1.setNumReduceTasks(1);

        job1.waitForCompletion(true);
        
        FileSystem fs = FileSystem.get(conf);
        InputStream in = fs.open(new Path(outDir + "/part-r-00000"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuffer sb = new StringBuffer();
        
        while((line = br.readLine()) != null){
           line = line.trim();
           sb.append(line).append(",");
        }        

        return sb.toString();

      }
    
}
