package network;

import java.util.ArrayList;

import general.CommonObject;
import general.Constant;
import subgraph.LinearRoute;

public class Link extends CommonObject{
	
	private Layer associatedLayer = null; //the layer that the link belongs to
	private NodePair nodePair=null;//占用当前link的节点对
	private Node nodeA = null; //node A
	private Node nodeB = null; //node B
	private double length = 0; //physical distance of the link
	private double cost = 0;// the cost of the link
	private int status = Constant.UNVISITED;//the visited status	
	private double remainingBandwidth;
	private double bandwidth;
	private ArrayList<Link> phyLinkList=new ArrayList<>();
	
	private int tier;//该链路所处的级别
	public Layer getAssociatedLayer() {
		return associatedLayer;
	}
	public void setAssociatedLayer(Layer associatedLayer) {
		this.associatedLayer = associatedLayer;
	}
	
	public NodePair getNodePair() {
		return nodePair;
	}
	public void setNodePair(NodePair nodePair) {
		this.nodePair = nodePair;
	}
	public Node getNodeA() {
		return nodeA;
	}
	public void setNodeA(Node nodeA) {
		this.nodeA = nodeA;
	}
	public Node getNodeB() {
		return nodeB;
	}
	public void setNodeB(Node nodeB) {
		this.nodeB = nodeB;
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
	public double getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}
	
	
	public double getRemainingBandwidth() {
		return remainingBandwidth;
	}
	public void setRemainingBandwidth(double remainingBandwidth) {
		this.remainingBandwidth = remainingBandwidth;
	}
	
	
	public int getTier() {
		return tier;
	}
	public void setTier(int tier) {
		this.tier = tier;
	}
	
	
	public ArrayList<Link> getPhyLinkList() {
		return phyLinkList;
	}
	public void setPhyLinkList(ArrayList<Link> phyLinkList) {
		this.phyLinkList = phyLinkList;
	}
	public Link(String name, int index, String comments, Layer associatedLayer,
			Node nodeA, Node nodeB, double length, double cost) {
		super(name, index, comments);
		this.associatedLayer = associatedLayer;
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.length = length;
		this.cost = cost;	 	 
		status = Constant.UNVISITED;
	}

  
	
}

