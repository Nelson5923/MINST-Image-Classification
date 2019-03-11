hadoop fs -copyFromLocal ./km-train /user/1155079291/km-train
hadoop fs -copyFromLocal ./km-test /user/1155079291/km-test

hadoop jar [.jar file] ParallelKmeans [TrainDir] [TestDir] [outDir] [iteration] [CentralPercentage] [ClusterNumber] [Split]

cd ./ParallelKmeans
hadoop com.sun.tools.javac.Main *.java
cd ../
jar cf km.jar ParallelKmeans/*.class

hadoop fs -rm -r /user/1155079291/minst-train-8-10-tmp*
hadoop fs -rm -r /user/1155079291/minst-train-8-10-initial*
hadoop fs -rm -r /user/1155079291/minst-train-8-10-label*
hadoop fs -rm -r /user/1155079291/minst-out-8-10

hadoop jar km.jar ParallelKmeans/ParallelKmeans \
/user/1155079291/minst-train-9-10/* \
/user/1155079291/minst-test-9-10/* \
/user/1155079291/minst-out-9-10 \
13 \
1 \
41