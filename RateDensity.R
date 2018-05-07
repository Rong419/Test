d1 <- 0.1; d2 <- 0.2; d3 <- 0.4; d4 <- 0.27
T <- 10;

r1 <- function (t) { d1 / t;}
r2 <- function (t) {d2 / t;}
r3 <- 0.04
r4 <- function (t) { d4 / (T-t);}

curve(r1,from=0,to=T, col="red")
curve(r2,from=0,to=T, add=TRUE, col="green")
curve(r4,from=0,to=T, add=TRUE, col="blue")

M = -3;
S = 0.25;

logRateDensity <- function (t) {
logd <- log(dlnorm(r1(t),meanlog=M, sdlog=S)) + 
       log(dlnorm(r2(t),meanlog=M, sdlog=S)) + 
       log(dlnorm(r3,meanlog=M, sdlog=S))+log(dlnorm(r4(t),meanlog=M, sdlog=S)) ;
}

densityFromRates <- function (t) {
0.01*exp(logRateDensity(t)); 
}
 
l <- read.table(file="dna.log", sep="\t", header=T)
hist(l$mrcatime.ingroup.)
curve(densityFromRates,from=0,to=T, col="red",add=TRUE)

