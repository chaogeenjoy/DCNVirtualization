package embedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.MulticastRequest;
import general.Constant;
import graphalgorithms.RouteSearching;
import graphalgorithms.SearchConstraint;
import network.Layer;
import network.Link;
import network.Node;
import subgraph.LinearRoute;

public class MulticastEmbedding2 {

	public boolean simpleMulticast(MulticastRequest request, Layer phyLayer){
		boolean flag=true;
		if(!this.enoughEdgeBandwidth(request, phyLayer)){
			flag=false;
		}else{
			ArrayList<Node> MCNodeList=new ArrayList<>();
			ArrayList<Node> virtualMCNodeList=new ArrayList<>();
			HashMap<String, Node> virtualMCNodeMap=new HashMap<>();
			ArrayList<Node> LeafVMList=new ArrayList<>();
			
			Iterator<String> itr0=request.getNodelist().keySet().iterator();
			while(itr0.hasNext()){
				Node leafVM=(Node)(request.getNodelist().get(itr0.next()));
				LeafVMList.add(leafVM);
			}
			
			Node rootVM=request.getRootVM();
			Node rootPM=rootVM.getPM();			
			Node firstLeafVM=LeafVMList.get(0);
			Node firstLeafPM=firstLeafVM.getPM();
			SearchConstraint constraint = new SearchConstraint();
			Iterator<String> itr2=phyLayer.getLinklist().keySet().iterator();
			while(itr2.hasNext()){
				Link link=(Link)(phyLayer.getLinklist().get(itr2.next()));
				if(link.getRemainingBandwidth()<request.getTrafficDemand()){
					constraint.getExcludedLinklist().add(link);
				}
			}
			
			
			LinearRoute newRoute=new LinearRoute("",0, null);
			RouteSearching rs=new RouteSearching();
			rs.Dijkstras(rootPM, firstLeafPM, phyLayer, newRoute, constraint);
			if(newRoute.getLinklist().size()==0){
				flag=false;
			}else{
				for(Link link:newRoute.getLinklist()){
					link.setRemainingBandwidth(link.getRemainingBandwidth()-request.getTrafficDemand());
				}
				
				//添加所经过的交换机节点
				for(Node node:newRoute.getNodelist()){
					if(node.getAttribute()!=Constant.SERVER){
						MCNodeList.add(node);
						Node newNode=new Node(node.getName(), node.getIndex(), null, request, Constant.SWITCH);
						virtualMCNodeList.add(newNode);
						virtualMCNodeMap.put(node.getName(), newNode);
					}
				}
				String name=rootVM.getName()+"-"+firstLeafVM.getName();
				Link newLink=new Link(name, request.getLinkNum(), null, request, rootVM, firstLeafVM, newRoute.getlength(), newRoute.getlength());
				request.addLink(newLink);
				newLink.setPhyLinkList(newRoute.getLinklist());
			}
			
			
			
			
			
			if(flag){
				LeafVMList.remove(0);
				for(int i=0;i<LeafVMList.size();i++){
					Node currentLeafVM=LeafVMList.get(i);
					Node currentLeafPM=currentLeafVM.getPM();
					double minCost=Double.MAX_VALUE;
					LinearRoute minRoute=null;
					Node tempMCNode=null;
					for(int j=0;j<MCNodeList.size();j++){
						Node MCNode=MCNodeList.get(i);						
						LinearRoute newRoute1=new LinearRoute("",(i+1), null);
						RouteSearching rs1=new RouteSearching();
						rs1.Dijkstras(MCNode, currentLeafPM, phyLayer, newRoute1, constraint);
						
						if(newRoute1.getlength()<minCost){
							minCost=newRoute1.getlength();
							minRoute=newRoute1;
							tempMCNode=MCNode;
						}
					}
					
					
					
					if(minRoute!=null){
						for(Link link:minRoute.getLinklist()){
							link.setRemainingBandwidth(link.getRemainingBandwidth()-request.getTrafficDemand());
						}
						
						for(Node node:minRoute.getNodelist()){
							if((node.getAttribute()!=Constant.SERVER)&&(!MCNodeList.contains(node))){
								MCNodeList.add(node);
								Node newNode=new Node(node.getName(), node.getIndex(), null, request, Constant.SWITCH);
								virtualMCNodeList.add(newNode);
								virtualMCNodeMap.put(node.getName(), newNode);
							}
						}
						
						String name=currentLeafVM.getName()+"-"+tempMCNode.getName();
						Node vtempMVNode=virtualMCNodeMap.get(tempMCNode.getName());
						Link tempLink=new Link(name, request.getLinkNum(), null, request, currentLeafVM, vtempMVNode, minCost, minCost);
						request.addLink(tempLink);
						tempLink.setPhyLinkList(minRoute.getLinklist());
					}
				}
			}
		}
		
		return flag;
	}
	
	
	
	
	
	
	
	public boolean enoughEdgeBandwidth(MulticastRequest request,Layer phyLayer){
		boolean flag=true;
		Node rootPM=request.getRootVM().getPM();
		Link rootLink=phyLayer.findLink(rootPM, rootPM.getNeinodelist().get(0));
		if(rootLink.getRemainingBandwidth()<request.getTrafficDemand()){
			flag=false;
		}else{
			Iterator<String> itr1=request.getNodelist().keySet().iterator();
			while(itr1.hasNext()){
				Node leafVM=request.getNodelist().get(itr1.next());
				Link leafLink=phyLayer.findLink(leafVM.getPM(), leafVM.getPM().getNeinodelist().get(0));
				if(leafLink.getRemainingBandwidth()<request.getTrafficDemand()){
					flag=false;
					break;
				}
			}
		}		
		return flag;
	}
	
}
