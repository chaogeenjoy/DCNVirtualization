package embedding;

import java.util.ArrayList;
import java.util.Iterator;

import demand.MulticastRequest;
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
			ArrayList<Node> VMList=new ArrayList<>();
			
			Iterator<String> itr0=request.getNodelist().keySet().iterator();
			while(itr0.hasNext()){
				
			}
			Node rootVM=request.getRootVM();
			Node rootPM=rootVM.getPM();
			
			int i=0;
			Iterator<String> itr1=request.getNodelist().keySet().iterator();
			while(itr1.hasNext()){
				Node leafVM=(Node)(request.getNodelist().get(itr1.next()));
				Node leafPM=leafVM.getPM();
				
				SearchConstraint constraint = new SearchConstraint();
				Iterator<String> itr2=phyLayer.getLinklist().keySet().iterator();
				while(itr2.hasNext()){
					Link link=(Link)(phyLayer.getLinklist().get(itr2.next()));
					if(link.getRemainingBandwidth()<request.getTrafficDemand()){
						constraint.getExcludedLinklist().add(link);
					}
				}
				
				LinearRoute newRoute=new LinearRoute("", i, null);
				RouteSearching rs=new RouteSearching();
				rs.Dijkstras(rootPM, leafPM, phyLayer, newRoute, constraint);
				
				
				if(newRoute.getLinklist().size()==0){
					flag=false;
					break;
				}else{
					for(Link link:newRoute.getLinklist()){
						link.setRemainingBandwidth(link.getRemainingBandwidth()-request.getTrafficDemand());
					}
					
					String name=rootVM.getName()+"-"+leafVM.getName();
					Link newLink=new Link(name, request.getLinkNum(), null, request, rootVM, leafVM, newRoute.getlength(), newRoute.getlength());
					request.addLink(newLink);
					newLink.setPhyLinkList(newRoute.getLinklist());
				}
				
				i++;
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
