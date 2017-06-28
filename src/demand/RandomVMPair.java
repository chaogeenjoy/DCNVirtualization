package demand;

import java.util.ArrayList;
import java.util.Random;

import network.Node;
import network.NodePair;

/*
 * 该类中的方法主要是根据需求产生虚拟节点对的需求
 *             包含节点对的数量和节点对
 */
public class RandomVMPair {

	public void generateVMPair(ArrayList<Node> vmList){
		ArrayList<NodePair> vmPairList=new ArrayList<NodePair>();
		/*
		 * 首先产生节点对的数量，随机数
		 */
		int vmPairNum=new Random(1).nextInt((vmList.size()*(vmList.size()-1)/2)+1);//节点对的数量根据节点的数量随机产生
		
	}
}
