from mlxtend.data import loadlocal_mnist
import matplotlib.pyplot as plt
import numpy as np

LABEL_FILE = "emnist-letters-train-labels-idx1-ubyte";
IMAGE_FILE = "emnist-letters-train-images-idx3-ubyte";

X, y = loadlocal_mnist(images_path=IMAGE_FILE,labels_path=LABEL_FILE)

with open("minst-train", "w") as minst:
    for s in range(X.shape[0]):
        FeatureLabel = y[s].tolist()
        FeatureList = X[s].tolist()
        minst.write(str(FeatureLabel) + ": " + ' '.join(str(d) for d in FeatureList) + "\n")
print("Write " + str(X.shape[0]) + " Line")

LABEL_FILE = "emnist-letters-test-labels-idx1-ubyte";
IMAGE_FILE = "emnist-letters-test-images-idx3-ubyte";

X, y = loadlocal_mnist(images_path=IMAGE_FILE,labels_path=LABEL_FILE)

with open("minst-test", "w") as minst:
    for s in range(X.shape[0]):
        FeatureLabel = y[s].tolist()
        FeatureList = X[s].tolist()
        minst.write(str(FeatureLabel) + ": " + ' '.join(str(d) for d in FeatureList) + "\n")
print("Write " + str(X.shape[0]) + " Line")

'''

def plot_digit(X, y, idx):
    img = X[idx].reshape(28,28)
    plt.imshow(img, cmap='Greys',  interpolation='nearest')
    plt.title('true label: %s' % chr(96 + y[idx]))
    plt.show()

'''

'''
with open("Centeroid.txt", "w") as Centeroid:
    Sample = np.random.choice(X.shape[0], size=1)[0]
    RandomList = X[Sample].tolist()
    Centeroid.write(' '.join(str(d) for d in RandomList) + "\n")
t = np.array([[1,2,3],[4,9,2]])
y = np.mean(t, axis=0).tolist()
print(y)
Sample = np.random.choice(X.shape[0], size=1)[0]
'''

'''    
with open("Centeroid.txt", "w") as Centeroid:
        i = 1
        k = 2 # WwUuOoPpKkCcVvXxZzSsIi
        while(1):
                Sample = np.random.choice(X.shape[0], size=1)[0]
                if y[Sample] == i:
                        plot_digit(X, y, Sample)
                        RandomList = X[Sample].tolist()
                        Centeroid.write(' '.join(str(d) for d in RandomList) + "\n")
                        i = i + 1
                if i == k:
                        break
'''


'''
print('Dimensions: %s x %s' % (X.shape[0], X.shape[1]))
print(X[0])
FeatureList = X[0].tolist()
print("[" + ' '.join(str(d) for d in FeatureList) + "]")

print('Digits:  0 1 2 3 4 5 6 7 8 9')
print('labels: %s' % np.unique(y))
print('Class distribution: %s' % np.bincount(y))
print(np.bincount(y).size)
print(X[0].size)

RandomList = np.random.randint(0,256,X.shape[1]).tolist()
print(' '.join(str(d) for d in RandomList) + "\n")
print(len(RandomList))
'''

'''k = 26
with open("Centeroid.txt", "w") as Centeroid:
        for s in range(k):
                RandomList = np.random.randint(0, 256, X.shape[1]).tolist()
                Centeroid.write(' '.join(str(d) for d in RandomList) + "\n")

'''

'''
print(np.random.choice(X.shape[0], size=1, replace=False)[0])
print(X[np.random.choice(X.shape[0], size=2, replace=False),:])
dist = np.linalg.norm(X[0]-X[3])
print(X[0])

mu1 = X[np.random.choice(X.shape[0], size=1, replace=False)[0]]
Center = mu1
D2 = np.array([min([np.linalg.norm(x-c)**2 for c in Center]) for x in X])

M = np.append(a, mu1)
'''