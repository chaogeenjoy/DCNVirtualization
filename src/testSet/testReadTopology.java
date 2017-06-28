package testSet;

import graphalgorithms.RouteSearching;
import network.Layer;
import network.Node;
import subgraph.LinearRoute;

public class testReadTopology {

	public static void main(String[] args) {
		Layer layer0=new Layer("PHYLayer", 0, null);
		layer0.readTopology("E:\\╤абшнд\\DCN\\Topology\\P9T3A2I2.csv");
		
		Node srcNode=layer0.getNodelist().get("N0");
		Node destNode=layer0.getNodelist().get("N9");
		RouteSearching rs=new RouteSearching();
		LinearRoute newRoute=new LinearRoute("", 0, null);
		
		
		rs.Dijkstras(srcNode, destNode, layer0, newRoute, null);
		
		newRoute.OutputRoute_node(newRoute);
		/*;*/
		/*
		Iterator<String> itr=layer0.getNodelist().keySet().iterator();
		while(itr.hasNext()){
			Node node=layer0.getNodelist().get(itr.next());
			System.out.println(node.getName()+"--"+node.getAttribute());
		}
		
		System.out.println("---------------------------");
		Iterator<String> itr1=layer0.getLinklist().keySet().iterator();
		while(itr1.hasNext()){
			Link link=layer0.getLinklist().get(itr1.next());
			System.out.println(link.getName()+"--"+link.getTier()+"--"+link.getRemainingBandwidth());
		}*/
		
	}
}
