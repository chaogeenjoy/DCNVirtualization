package network;

import java.util.ArrayList;

import general.CommonObject;
import general.Constant;
 

public class Node extends CommonObject{
	
	private Layer associatedLayer = null; //the layer that the node is associated with
	private ArrayList<Node> neinodelist = null; //list of neighbor nodes	 
	private double cost_from_src = 10000000;//used search algorithm to get he cost from the src node
	private int hop_from_src = 100000000; //used in search algorithm to get the number of hops from the src node
	private int status = Constant.UNVISITED; //not visited yet
	private boolean arrange=false;
	private Node parentNode = null;//parent node of the current node
 
	/*
	 * 下面几个属性用来判别该节点是服务器还是交换机
	 */
	private int attribute;
	private double memory;
	private int cpu;
	private double disk;
	private Node PM;//VM所映射的PM
	private boolean uniCast=false;//
	private boolean successEmbed=false;//对于VM来说，这个标志用来表示当前的VM已经成功的映射
//	                                                            对于PM来说，这个标志用来表示在这个VDC分配中，PM已经分配给其他的VM了，不可以再次使用了，
//	                                                          用来放置不同的VM映射到同一个PM上
	public Node(String name,int index,String comments){
		super(name,index,comments);
		this.neinodelist=new ArrayList<Node>();
	}
	
	public Node(String name, int index, String comments, Layer associatedLayer,int attribute) {
		super(name, index, comments);
		this.associatedLayer = associatedLayer;
		this.neinodelist = new ArrayList<Node>();
		this.attribute=attribute;
	}
	
	
	public Node getParentNode() {
		return parentNode;
	}
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}
	public int getHop_from_src() {
		return hop_from_src;
	}
	public void setHop_from_src(int hop_from_src) {
		this.hop_from_src = hop_from_src;
	}
	public double getCost_from_src() {
		return cost_from_src;
	}
	public void setCost_from_src(double cost_from_src) {
		this.cost_from_src = cost_from_src;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Layer getAssociatedLayer() {
		return associatedLayer;
	}
	public void setAssociatedLayer(Layer associatedLayer) {
		this.associatedLayer = associatedLayer;
	}
	public ArrayList<Node> getNeinodelist() {
		return neinodelist;
	}
	public void setNeinodelist(ArrayList<Node> neinodelist) {
		this.neinodelist = neinodelist;
	}
	
	public void addNeiNode(Node node){
		this.neinodelist.add(node);
	}
	
	
	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	

	public double getMemory() {
		return memory;
	}


	public void setMemory(double memory) {
		this.memory = memory;
	}


	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public double getDisk() {
		return disk;
	}

	public void setDisk(double disk) {
		this.disk = disk;
	}
	

	public Node getPM() {
		return PM;
	}

	public void setPM(Node pM) {
		this.PM = pM;
	}
	
	
	

	public boolean isUniCast() {
		return uniCast;
	}

	public void setUniCast(boolean uniCast) {
		this.uniCast = uniCast;
	}

	public boolean isSuccessEmbed() {
		return successEmbed;
	}

	public void setSuccessEmbed(boolean successEmbed) {
		this.successEmbed = successEmbed;
	}

	/**
	 * remove neighbor node from the neinode list by index
	 */
	public void removeNeiNode(int index){
		for(int i=0;i<this.neinodelist.size();i++){
			if(this.neinodelist.get(i).getIndex() == index){
				this.neinodelist.remove(this.neinodelist.get(i));
				break;
			}
		}			
	}
	
	public void removeNeiNode(String name){
		for(int i=0;i<this.neinodelist.size();i++){
			if(this.neinodelist.get(i).getName().equals(name)){
				this.neinodelist.remove(this.neinodelist.get(i));
				break;
			}
		}			
	}
	/**
	 * remove neighbor node from the neinode list
	 */
	public void removeNeiNode(Node node){
		this.getNeinodelist().remove(node);
	}		   
	/**
	 * get degree of the node
	 */
	public int getDegree(){
		return this.neinodelist.size();
	}

	public boolean isArrange() {
		return arrange;
	}

	public void setArrange(boolean arrange) {
		this.arrange = arrange;
	}
	
 		
}



