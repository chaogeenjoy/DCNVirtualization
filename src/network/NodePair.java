package network;

import java.util.ArrayList;
import subgraph.LinearRoute;
import general.CommonObject;
import general.Constant;

public class NodePair extends CommonObject{
	
	private Layer associateLayer =null;//the layer that the node pair associated with
	private Node srcNode = null; //the source node
	private Node desNode = null;//Destination node
	private ArrayList<LinearRoute> linearroutelist = null; //list of linear route associated with the node pair
	private double trafficdemand=0; //traffic demand between node pair
	private boolean Arrange_status=false; //节点对还未按流量大小顺序排列
	
	public Layer getAssociateLayer() {
		return associateLayer;
	}
	public void setAssociateLayer(Layer associateLayer) {
		this.associateLayer = associateLayer;
	}
	public Node getSrcNode() {
		return srcNode;
	}
	public void setSrcNode(Node srcNode) {
		this.srcNode = srcNode;
	}
	public Node getDesNode() {
		return desNode;
	}
	public void setDesNode(Node desNode) {
		this.desNode = desNode;
	}		
	public ArrayList<LinearRoute> getLinearroutelist() {
		return linearroutelist;
	}
	public void setLinearroutelist(ArrayList<LinearRoute> linearroutelist) {
		this.linearroutelist = linearroutelist;
	}			 				
	public void setTrafficdemand(double trafficdemand) {
		this.trafficdemand = trafficdemand;
	}
	public double getTrafficdemand() {
		return trafficdemand;
	}	
	
	
	
	public boolean isArrange_status() {
		return Arrange_status;
	}
	public void setArrange_status(boolean arrange_status) {
		Arrange_status = arrange_status;
	}
	public NodePair(String name, int index, String comments,
			Layer associateLayer, Node srcNode, Node desNode) {
		super(name, index, comments);
		this.associateLayer = associateLayer;
		this.srcNode = srcNode;
		this.desNode = desNode;
		this.linearroutelist = new ArrayList<LinearRoute>();
		this.trafficdemand=0;
		this.Arrange_status=false;
	}
	
	/**
	 * add a route to the route list
	 */
	public void addRoute(LinearRoute route){
		this.linearroutelist.add(route);		
	}
	
	/**
	 * remove route from the list 
	 */
	public void removeRoute(LinearRoute route){
		this.linearroutelist.remove(route);
	}
	
	/**
	 * remove route from the list based on index
	 */
	public void removeRoute(int index){
		for(int i=0;i<this.linearroutelist.size();i++){
			if(this.linearroutelist.get(i).getIndex()==index){
				this.linearroutelist.remove(i);
				break;			
			}
		}
	}
	
	/**
	 * remove route from the list based on name
	 */
	public void removeRoute(String name){
		for(int i=0;i<this.linearroutelist.size();i++){
			if(this.linearroutelist.get(i).getName().equals(name)){
				this.linearroutelist.remove(i);
				break;			
			}
		}
	}
	


}

