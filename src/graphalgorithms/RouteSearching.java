package graphalgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import general.Constant;
import network.Layer;
import network.Link;
import network.Node;
import subgraph.LinearRoute;

public class RouteSearching {
	public void Dijkstras(Node srcNode, Node destNode, Layer layer, LinearRoute newRoute, SearchConstraint constraint) {
		ArrayList<Node> visitedNodeList = new ArrayList<Node>();
		visitedNodeList.clear();

		// initialize all the node states
		HashMap<String, Node> map = layer.getNodelist();
		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			Node node = (Node) (map.get(iter.next()));
			node.setStatus(Constant.UNVISITED);
			node.setParentNode(null);
			node.setCost_from_src(10000000);
			node.setHop_from_src(10000000);
		}

		// Initialization
		Node currentNode = srcNode;
		currentNode.setCost_from_src(0);
		currentNode.setHop_from_src(0);
		currentNode.setStatus(Constant.VISITEDTWICE);

		if (constraint == null) {
			for (Node node : currentNode.getNeinodelist()) {

				if (node.getStatus() == Constant.UNVISITED) {

					Link link = layer.findLink(currentNode, node);
					if (link != null) {

						node.setCost_from_src(currentNode.getCost_from_src() + link.getCost());
						node.setHop_from_src(currentNode.getHop_from_src() + 1);
						node.setStatus(Constant.VISITEDONCE);
						node.setParentNode(currentNode);
						visitedNodeList.add(node);
					}
				}
			}
		} else {
			for (Node node : currentNode.getNeinodelist()) {
				if (!constraint.getExcludedNodelist().contains(node)) {
					
					
					if (node.getStatus() == Constant.UNVISITED) {
						
						Link link = layer.findLink(currentNode, node);
						if (!constraint.getExcludedLinklist().contains(link)) {
							node.setCost_from_src(currentNode.getCost_from_src() + link.getCost());
							node.setHop_from_src(currentNode.getHop_from_src() + 1);
							node.setStatus(Constant.VISITEDONCE);
							node.setParentNode(currentNode);
							visitedNodeList.add(node);
						}
					}
				}
			}

		}

		currentNode = this.getLowestCostNode(visitedNodeList);
		if (currentNode != null) {
			while (!currentNode.equals(destNode)) {
				currentNode.setStatus(Constant.VISITEDTWICE);
				visitedNodeList.remove(currentNode);

				if (constraint == null) {
					for (Node node : currentNode.getNeinodelist()) {
						if (node.getStatus() == Constant.UNVISITED) {
							Link link = layer.findLink(currentNode, node);
							if (link != null) {
								node.setCost_from_src(currentNode.getCost_from_src() + link.getCost());
								node.setHop_from_src(currentNode.getHop_from_src() + 1);
								node.setStatus(Constant.VISITEDONCE);
								node.setParentNode(currentNode);
								visitedNodeList.add(node);
							}
						} else if (node.getStatus() == Constant.VISITEDONCE) {
							Link link = layer.findLink(currentNode, node);
							if (link != null) {
								if (node.getCost_from_src() > currentNode.getCost_from_src() + link.getCost()) {
									// update the node status
									node.setCost_from_src(currentNode.getCost_from_src() + link.getCost());
									node.setParentNode(currentNode);
								}
							}
						}
					}
				} else {
					for (Node node : currentNode.getNeinodelist()) {
						if (!constraint.getExcludedNodelist().contains(node)) {
							if (node.getStatus() == Constant.UNVISITED) {
								Link link = layer.findLink(currentNode, node);
								if (!constraint.getExcludedLinklist().contains(link)) {
									node.setCost_from_src(currentNode.getCost_from_src() + link.getCost());
									node.setHop_from_src(currentNode.getHop_from_src() + 1);
									node.setStatus(Constant.VISITEDONCE);
									node.setParentNode(currentNode);
									visitedNodeList.add(node);
								}
							} else if (node.getStatus() == Constant.VISITEDONCE) {
								String name;
								if (currentNode.getIndex() < node.getIndex())
									name = currentNode.getName() + "-" + node.getName();
								else
									name = node.getName() + "-" + currentNode.getName();
								Link link = layer.getLinklist().get(name);
								if (!constraint.getExcludedLinklist().contains(link)) {
									if (node.getCost_from_src() > currentNode.getCost_from_src() + link.getCost()) {
										// update the node status
										node.setCost_from_src(currentNode.getCost_from_src() + link.getCost());
										node.setHop_from_src(currentNode.getHop_from_src() + 1);
										node.setParentNode(currentNode);
									}
								}

							}
						}

					}

				}

				currentNode = this.getLowestCostNode(visitedNodeList);
				if (currentNode == null)
					break;
			}
		}

		newRoute.getNodelist().clear();
		newRoute.getLinklist().clear();

		currentNode = destNode;
		if (destNode.getParentNode() != null) {
			newRoute.getNodelist().add(0, currentNode);

			while (currentNode != srcNode) {
				Link link = layer.findLink(currentNode, currentNode.getParentNode());
				newRoute.getLinklist().add(0, link);

				currentNode = currentNode.getParentNode();
				newRoute.getNodelist().add(0, currentNode);

			}
		}
	}

	private Node getLowestCostNode(ArrayList<Node> visitedNodeList) {

		Node currentnode = null;
		double current_cost_to_desc = 100000000;
		for (Node node : visitedNodeList) {
			if (node.getCost_from_src() < current_cost_to_desc) {
				currentnode = node;
				current_cost_to_desc = node.getCost_from_src();
			}
		}
		return currentnode;
	}

	/**
	 * find K-disjoint shortest path routes
	 */
	public int Kshortest(Node srcNode, Node destNode, Layer layer, int k, ArrayList<LinearRoute> routelist) {

		routelist.clear();
		SearchConstraint constraint = new SearchConstraint(1000000, 100000);

		int num_found = 0; // number of found route
		while (true) {
			LinearRoute newRoute = new LinearRoute("", 0, "");
			this.Dijkstras(srcNode, destNode, layer, newRoute, constraint);
			if (newRoute.getLinklist().size() > 0) {
				routelist.add(newRoute);
				constraint.getExcludedLinklist().addAll(newRoute.getLinklist());
				num_found++;
				if (num_found == k)
					break;
			} else
				break;
		}
		return num_found; // the number of found routes
	}

	/**
	 * find all routes between a pair of nodes
	 */
	public int findAllRoute(Node nodeA, Node nodeB, Layer layer, SearchConstraint constraint, int k,
			ArrayList<LinearRoute> routelist) {

		int hoplimit = 100000;
		if (constraint != null)
			hoplimit = constraint.getMax_hop();

		ArrayList<Message> messageList = new ArrayList<Message>(); 
		Message newMessage = new Message(nodeA);

		messageList.add(newMessage);

		while (!messageList.isEmpty()) {
			// get the header message
			Message currentmessage = messageList.remove(0);
			Node currentNode = currentmessage.getCurrentNode();

			if (currentNode == nodeB) {
				// find one route
				LinearRoute newroute = new LinearRoute("", 0, "");
				newroute.getNodelist().addAll(currentmessage.getVisitedNodelist());
				routelist.add(newroute);
				newroute.ConvertfromNodeListtoLinkList();
				if (routelist.size() == k)
					break;
			} else {
				if (currentmessage.getVisitedNodelist().size() - 1 < hoplimit)
					for (Node neinode : currentNode.getNeinodelist()) {
						if (!currentmessage.getVisitedNodelist().contains(neinode)) {

							newMessage = new Message(neinode);
							newMessage.getVisitedNodelist().addAll(0, currentmessage.getVisitedNodelist());
							messageList.add(newMessage);

						}
					}
			}

		}

		return routelist.size();

	}
}
