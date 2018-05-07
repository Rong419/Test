# Test
Relaxed clock model when genetic distances are held constantn
Data: dna sequence of Frog Human Seal
Tree(fixed): (Frog:10,(Human:1,Seal:1):9)
Clock Model: Uncorrelated Relaxed Clock Model
rates follow a LogNormal distribution: r~LogNormal(M=-3, S=0.25, MeanInRealSpace=false) 
Initialization:
T=10: time of the root
t=1: start from 1, ranges from 0 to T
d=[0.1 0.2 0.4 0.27]: genetic distance, remained unchanged during the process
r=[0.1 0.2 0.04 0.03]: initial rates on the branches
