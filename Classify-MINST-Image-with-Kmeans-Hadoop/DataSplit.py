import os

# Merge the Data

Total = 0

with open("./minst-merge", "w") as merge:
    for fname in ['./minst-train', './minst-test']:
        with open(fname) as infile:
            for line in infile:
                merge.write(line)
                Total = Total + 1
print("Write " + str(Total) + " Line")

TotalChunks = 10;
TestRatio = 1/TotalChunks

for n in range(TotalChunks):

    print("Chunks " + str(n) + " for Test File")
    TrainRange_1 = range(0, int(round((Total * TestRatio * n))))
    TestRange = range(int(round(Total * TestRatio * n)), int(round((Total * TestRatio * (n+1)))))
    TrainRange_2 = range(int(round((Total * TestRatio * (n+1)))), Total)
    DataRange = [TrainRange_1, TestRange, TrainRange_2]
    TrainFile = 'minst-train-' + str(n) + "-" + str(TotalChunks)
    TestFile = 'minst-test-' + str(n) + "-" + str(TotalChunks)
    print(DataRange)

    with open("./minst-merge", "r") as merge:
        line = merge.readlines()
        os.mkdir("./" + TrainFile)
        with open("./" + TrainFile + "/" + TrainFile,"w") as Train:
            count = 0
            for i in TrainRange_1:
                Train.write(line[i])
                count = count + 1
            for i in TrainRange_2:
                Train.write(line[i])
                count = count + 1
            print("Write " + str(count) + " Line")
        os.mkdir("./" + TestFile)
        with open("./" + TestFile + "/" + TestFile, "w") as Test:
            count = 0
            for i in TestRange:
                Test.write(line[i])
                count = count + 1
            print("Write " + str(count) + " Line")



