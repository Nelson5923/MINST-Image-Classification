# Initial Matrix

A = rbind(c(0,0,6,3,2,0,9)
, c(3,9,2,8,7,0,4)
, c(3,9,2,6,1,4,5)
, c(3,8,2,6,8,0,7)
, c(7,9,9,5,4,8,9)
, c(0,4,7,4,3,4,3))

# SVD

s <- La.svd(A)
U <- s$u
D <- diag(s$d)
VT <- s$vt

# Dimension Reduction

R <- U[,1:2] %*% D[1:2,1:2] %*% VT[1:2,]
s <- La.svd(R)
U <- s$u[,1:2]
D <- diag(s$d)[1:2,1:2]
VT <- s$vt[1:2,]