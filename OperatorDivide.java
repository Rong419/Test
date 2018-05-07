package myoperator.operator1;

import beast.core.Description;
import beast.core.Input;
import beast.core.Operator;
import beast.core.parameter.RealParameter;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.evolution.tree.Tree;
import beast.math.distributions.LogNormalDistributionModel;
import beast.math.distributions.ParametricDistribution;
import beast.math.distributions.Uniform;
import beast.util.Randomizer;
import beast.evolution.tree.Node;
import org.apache.commons.math.MathException;


import java.util.List;

@Description("Divide a random branch into three segments, one of which uses a uniform operator and two of which uses a scale operator")
public class OperatorDivide extends Operator {
    public final Input<Tree> treeInput = new Input<>("tree", "the rooted time tree for the operator to work on");
    public final Input<Double> windowSizeInput =
            new Input<>("windowSize", "the size of the window both up and down when using uniform interval", Input.Validate.REQUIRED);
    public final Input<Double> scalerInput =
            new Input<>("scaler", "the size of the scale when in the small interval", Input.Validate.REQUIRED);
    public final Input<BranchRateModel.Base> branchRateModelInput = new Input<>("branchRateModel",
            "A model describing the rates on the branches of the beast.tree.");
    final public Input<RealParameter> rateInput = new Input<>("rates", "the rates associated with nodes in the tree for sampling of individual rates among branches.", Input.Validate.REQUIRED);
    //final public Input<ParametricDistribution> rateDistInput = new Input<>("distr", "the distribution governing the rates among branches. Must have mean of 1. The clock.rate parameter can be used to change the mean rate.", Input.Validate.REQUIRED);

    double windowSize;
    double scaler;
    Tree tree;
    RealParameter rates;
    double hastingsRatio;
    protected BranchRateModel.Base branchRateModel;


    @Override
    public void initAndValidate() {
        windowSize = windowSizeInput.get();
        scaler = scalerInput.get();
        tree = treeInput.get();
        branchRateModel = branchRateModelInput.get();
        rates = rateInput.get();
        //distribution = rateDistInput.get();
    }

    @Override
    public double proposal() {
        final Tree tree = treeInput.get(this);
        double r_i;
        double r_j;
        double r_k;
        Node node;
        double Pr_a ;
        double Pr_b ;
        double a;
        double x;
        double y;
        double t_x_ = 0.0; //initialize the new state, i.e. the proposed time of the node


        //Step 1: randomly select a time in internal node times, i.e. x
        //return the number of nodes in the tree
        final int nodeCount = tree.getNodeCount();
        do {
            final int nodeNr = nodeCount / 2 + 1 + Randomizer.nextInt(nodeCount / 2);
            //this is the node
            node = tree.getNode(nodeNr);
        } while (node.isRoot() || node.isLeaf());//exclude the root and leaf node


        //Step 2: makes changes on this node time, i.e. t(x)
        //(1): get the times of the corresponding ndoes
        double t_x = node.getHeight();
        double t_i = node.getParent().getHeight();
        //System.out.println("parental node time:"+t_i);
        double t_j = node.getChild(0).getHeight();
        //System.out.println("son node time:"+t_j);
        double t_k = node.getChild(1).getHeight();
        //System.out.println("daughter node time:"+t_k);
        //(2): deal with bound
        final double upper = t_i;
        final double lower = Math.max(t_j, t_k);
        //(3): make change on this node time
        // generate a random number a~U[-windowSize,+windowSize]
        //a = Randomizer.nextDouble() * 2 * windowSize - windowSize;
        a = Randomizer.uniform(-windowSize,windowSize);
        t_x_ = t_x + a;
        //(4): Deal with the case in which time exceeds the height range
        if (t_x_ <= lower || t_x_ >= upper) {
            return Double.NEGATIVE_INFINITY;
        }
        if (t_x_ == t_x) {
            return Double.NEGATIVE_INFINITY;
        }
        //(5): assign the new time to this node
        node.setHeight(t_x_);

        //Step 3: make changes on rates
        //(1): find the corresponding rates, the three branches linked to this node
        //branch i:the parental branch, branch j:the son branch, branch k:the daughter branch
        int nodeN01 = node.getNr();//node number of this node
        Node son = node.getChild(0);//get the left child of this node, i.e. son
        int nodeN02 = son.getNr();//node number of son
        Node daughter = node.getChild(1);//get the right child of this node, i.e. daughter
        int nodeN03 = daughter.getNr();//node number of daughter
        //get the rate on branches
        r_i = branchRateModel.getRateForBranch(node);//branch rate for this node
        r_j = branchRateModel.getRateForBranch(son);//branch rate for son
        r_k = branchRateModel.getRateForBranch(daughter);//branch rate for daughter
        //(2): change the rates, i.e. r'=r*(original branch length)/(new branch length)
        double r_i_ = r_i * (t_i - t_x) / (t_i - t_x_);
        double r_j_ = r_j * (t_x - t_j) / (t_x_ - t_j);
        double r_k_ = r_k * (t_x - t_k) / (t_x_ - t_k);
        //(3): save the new rates
        rates.setValue(nodeN01, r_i_);
        rates.setValue(nodeN02, r_j_);
        rates.setValue(nodeN03, r_k_);
        //Step 4: return Hastings ratio
            try {
                //pr_a: the probability of original state to new state;
                //pr_b:the probability of new state to original state
                x = Math.max(a, -windowSize);
                Pr_a= (x + windowSize) / (2*windowSize);
                y = Math.max(-a, -windowSize);
                Pr_b = (y + windowSize) / (2*windowSize);
                hastingsRatio=Pr_b/Pr_a;
            } catch (Exception e) {
                return Double.NEGATIVE_INFINITY;
            }
        return hastingsRatio;
        }
    }



