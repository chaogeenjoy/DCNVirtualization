package mixedEmbedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.MixedCastRequest;
import general.Constant;
import graphalgorithms.RouteSearching;
import graphalgorithms.SearchConstraint;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;

public class MixedEmbedding {

	public boolean vmEmbed(MixedCastRequest request,Layer phyLayer){

		ArrayList<Node> PMList=new ArrayList<Node>();
		ArrayList<Node> VMList=new ArrayList<Node>();
		
		Iterator<String> itr=phyLayer.getNodelist().keySet().iterator();
		while(itr.hasNext()){
			Node node=(Node)(phyLayer.getNodelist().get(itr.next()));
			if(node.getAttribute()==Constant.SERVER){
				PMList.add(node);
			}
		}
		
		
		//对VM降序排列,按照server内部资源的加权求和的大小进行降序排列
		//size=vm.getCpu()*Constant.WC+vm.getMemory()*Constant.WM+vm.getDisk()*Constant.WD;
        for(int j=0;j<request.getNodelist().size();j++){			
			double minSize=Double.MAX_VALUE;
			Node tempVM=null;
			Iterator<String> it0=request.getNodelist().keySet().iterator();
			while(it0.hasNext()){
				Node vm=request.getNodelist().get(it0.next());
				if(!vm.isArrange()){
					double size=vm.getCpu()*Constant.WC+vm.getMemory()*Constant.WM+vm.getDisk()*Constant.WD;
					if(size<=minSize){
						minSize=size;
						tempVM=vm;
					}
				}		
				
			}
			VMList.add(0,tempVM);
			tempVM.setArrange(true);
		}
		
        
		boolean flag=true;//标志当前的VDC是否映射成功
        
		//搜索PM，进行映射root PM
		Node root=request.getRootVM();
		for(int i=0;i<PMList.size();i++){
			Node pm=PMList.get(i);
			if((pm.getCpu()>=root.getCpu())&&(pm.getMemory()>root.getMemory()&&(pm.getDisk()>root.getDisk()))){
				pm.setCpu(pm.getCpu()-root.getCpu());
				pm.setMemory(pm.getMemory()-root.getMemory());
				pm.setDisk(pm.getDisk()-root.getDisk());
				root.setPM(pm);
				root.setSuccessEmbed(true);//用作释放资源的时候用
				break;
			}
		}
		
		
		
		
		if(!root.isSuccessEmbed()){
			flag=false;
		}else{
			//只有当root节点映射成功以后才可以对其他节点映射
			PMList.remove(root.getPM());//将root所在的PM从PMlist中去掉
			Iterator<Node> it1=VMList.iterator();
			while(it1.hasNext()){
				Node vm=(Node)  it1.next();
				int i=0;
				for(;i<PMList.size();i++){
					Node pm=PMList.get(i);
					if(pm.isSuccessEmbed())
						continue;
					
					if((pm.getCpu()>=vm.getCpu())&&(pm.getMemory()>vm.getMemory()&&(pm.getDisk()>vm.getDisk()))){//
						pm.setCpu(pm.getCpu()-vm.getCpu());
						pm.setMemory(pm.getMemory()-vm.getMemory());
						pm.setDisk(pm.getDisk()-vm.getDisk());
						pm.setSuccessEmbed(true);//这个标志用来表示当前的PM已经分配给同一VDC中的其他VM了，要注意及时释放掉
						vm.setPM(pm);
						vm.setSuccessEmbed(true);					
						break;
					}
				}
				
				if(i>=PMList.size()){//virtual machine embedding failed
					flag=false;
					break;
				}
			}
			
			//及时的释放资源
			for(Node pm:PMList){
				pm.setSuccessEmbed(false);
			}
			
			if(!flag){//when vm embedding failed , the resource allocated for vm should be released
				Node rootPM=root.getPM();
				rootPM.setCpu(rootPM.getCpu()+root.getCpu());
				rootPM.setMemory(rootPM.getMemory()+root.getMemory());
				rootPM.setDisk(rootPM.getDisk()+root.getDisk());
				root.setPM(null);
				root.setSuccessEmbed(false);
				
				Iterator<Node> it2=VMList.iterator();
				while(it2.hasNext()){
					Node vm=(Node) it2.next();
					if(vm.isSuccessEmbed()){
						Node pm=vm.getPM();
						pm.setCpu(pm.getCpu()+vm.getCpu());
						pm.setMemory(pm.getMemory()+vm.getMemory());
						pm.setDisk(pm.getDisk()+vm.getDisk());
						vm.setPM(null);
						vm.setSuccessEmbed(false);
					}
				}
			}			
			
		}		
		return flag;

	}
	
	public boolean mixVLEmbedding(){
		boolean flag=true;
		
		
		
		return flag;
	}
	
	public boolean multicastEmbedding(MixedCastRequest request, Layer phyLayer){
		boolean flag=true;
		if(!this.enoughEdgeBandwidth(request, phyLayer)){
//			System.out.println("物理层叶子链路带宽不够-_-");
			flag=false;
		}else{
//			System.out.println("物理层叶子链路带宽还是够的^-^"); 
			
			ArrayList<Node> mcNodeList=new ArrayList<Node>();//行使multicast功能的交换机节点
			HashMap<String, Node> vmcList=new HashMap<String,Node>();//虚拟的上述节点
			ArrayList<Node> vmcNodeList=new ArrayList<Node>();//这个也是和上面的一样的，只不过是list结构而不是map结构
			Node rootPM=request.getRootVM().getPM();
			Node rootMC=rootPM.getNeinodelist().get(0);		
			if((rootMC!=null)&&(rootMC.getAttribute()!=Constant.SERVER)){
//				System.out.println("和root PM直接相连的交换机节点是："+rootMC.getName());
				if(!mcNodeList.contains(rootMC)){
					mcNodeList.add(rootMC);	
					Node vmcNode=new Node(rootMC.getName(), rootMC.getIndex(),null , request, Constant.SWITCH);
					vmcList.put(vmcNode.getName(), vmcNode);
					vmcNodeList.add(vmcNode);
				}
			}
			
			Iterator<String> itr_mc=request.getLeafVMList().keySet().iterator();
			while(itr_mc.hasNext()){
				Node vm=request.getLeafVMList().get(itr_mc.next());
				Node mcNode=vm.getPM().getNeinodelist().get(0);//找到映射的服务器
				
				if((mcNode!=null)&&(mcNode.getAttribute()!=Constant.SERVER)){
//					System.out.println("-------------");
//					System.out.println("1.PM:"+vm.getPM().getName()+"\t直接相连的交换机节点是：\t"+mcNode.getName());
					if(!mcNodeList.contains(mcNode)){
//						System.out.println("2.PM:"+vm.getPM().getName()+"\t直接相连的交换机节点是：\t"+mcNode.getName());
						mcNodeList.add(mcNode);
						Node vmcNode=new Node(mcNode.getName(), mcNode.getIndex(),null , request, Constant.SWITCH);
						vmcList.put(vmcNode.getName(), vmcNode);
						vmcNodeList.add(vmcNode);
					}
				}					
			}
			/*
			for(Node mc:mcNodeList){
				System.out.println(mc.getName());
			}*/
			
			if(vmcNodeList.size()>1){
				SearchConstraint constraint=new SearchConstraint();
				Iterator<String> itr_L=phyLayer.getLinklist().keySet().iterator();
				while(itr_L.hasNext()){
					Link link=phyLayer.getLinklist().get(itr_L.next());
					if(link.getRemainingBandwidth()<request.getTrafficDemand()){
						constraint.getExcludedLinklist().add(link);				
					}
				}
				
				int k=0;
				for(int i=0;i<mcNodeList.size();i++){
					for(int j=i+1;j<mcNodeList.size();j++){
						Node srcNode=mcNodeList.get(i);
						Node desNode=mcNodeList.get(j);				
						Node vmcSrc=vmcList.get(srcNode.getName());//属于request layer的虚拟节点
						Node vmcDes=vmcList.get(desNode.getName());
						LinearRoute newRoute= new LinearRoute("route"+(k++), k, null);
						RouteSearching rs=new RouteSearching();					
						rs.Dijkstras(srcNode, desNode, phyLayer, newRoute, constraint);
					
						if(newRoute.getLinklist().size()==0){
//							System.out.println("在互联各个多播switch的时候，找不到从"+srcNode.getName()+"到"+desNode.getName()+"的链路");
						}else{
							String name;
							if(vmcSrc.getIndex()<vmcDes.getIndex()){
								 name=vmcSrc.getName()+"-"+vmcDes.getName();
							}else{
								 name=vmcDes.getName()+"-"+vmcSrc.getName();
							}
							Link newLink=new Link(name, k, null, request, vmcSrc, vmcDes, newRoute.getlength(), newRoute.getlength());
							newLink.setPhyLinkList(newRoute.getLinklist());
							request.addLink(newLink);
//							System.out.println("新建的全链接链路为："+newLink.getName());
//							System.out.println("所经过的物理链路为：");
//							newRoute.OutputRoute_node(newRoute);
						}
					}
				}
				
				
				
				if(request.getLinklist().size()<vmcNodeList.size()-1){
//					System.out.println("一共有\t"+request.getLinkNum()+"\t个虚拟多播节点，实际至少需要\t"+(vmcNodeList.size()-1)+"\t个");
					request.getLinklist().clear();
					flag=false;
				}else{
//					System.out.println("一共有\t"+request.getLinkNum()+"\t个虚拟多播节点，实际至少需要\t"+(vmcNodeList.size()-1)+"\t个");
					
					this.minSpanningTree(request, vmcNodeList);
					
					/*
					Iterator<String> itst=request.getLinklist().keySet().iterator();
					while(itst.hasNext()){
						Link link=(Link)(request.getLinklist().get(itst.next()));
						System.out.println("MST Link:"+link.getName());
					}
					*/
					
					
					
                    Node srcNode=vmcList.get(rootPM.getNeinodelist().get(0).getName());
					
					Link links=phyLayer.findLink(rootPM, rootPM.getNeinodelist().get(0));
					links.setRemainingBandwidth(links.getRemainingBandwidth()-request.getTrafficDemand());
					Link newLinks=new Link(links.getName(), request.getLinkNum(), null, request, request.getRootVM(), srcNode, 1, 1);
					ArrayList<Link> phyLinkLists=new ArrayList<Link>();
					phyLinkLists.add(links);
					newLinks.setPhyLinkList(phyLinkLists);
					request.addLink(newLinks);
					
					
					Iterator<String> itleaf=request.getLeafVMList().keySet().iterator();
					int h=0;
					while(itleaf.hasNext()){
						
						Node leafVM=request.getLeafVMList().get(itleaf.next());
//						System.out.print("当前叶子节点的虚拟机为："+leafVM.getName());
						Node physicalMCSwitch=leafVM.getPM().getNeinodelist().get(0);//和PM直接相连的是multicast switch
						Node destNode=vmcList.get(physicalMCSwitch.getName());
//						System.out.println("相连的交换机："+physicalMCSwitch.getName()+"虚拟"+destNode.getName());

						LinearRoute newRoute=new LinearRoute("multicast route"+h, h, null);
						RouteSearching rs=new RouteSearching();
						rs.Dijkstras(srcNode, destNode, request, newRoute, null);
					
						
						
						/*
						 * 1.如果成功，则只需要分配从ToR到 PM的带宽
						 * 2.如果失败，则
						 *     a)判断源节点和目的节点是否在一个ToR下，如果是，则短路由，否，则
						 *     b)尝试直接路由，如果失败，则阻塞
						 */
						if(newRoute.getLinklist().size()!=0){
							Link linkd=phyLayer.findLink(leafVM.getPM(), physicalMCSwitch);							
							linkd.setRemainingBandwidth(linkd.getRemainingBandwidth()-request.getTrafficDemand());	
							Link newLinkd=new Link(linkd.getName(), request.getLinkNum(), null, request, leafVM, destNode, 1, 1);
							ArrayList<Link> phyLinkListd=new ArrayList<Link>();
							phyLinkListd.add(linkd);
							newLinkd.setPhyLinkList(phyLinkListd);
							request.addLink(newLinkd);
//							System.out.println("\t和leaf PM直接相连的链路为"+newLinkd.getPhyLinkList().get(0).getName()+"剩余带宽为："+linkd.getRemainingBandwidth()+"\n\t"
//									+"相应的新建虚拟链路为"+newLinkd.getName());
						}else{
							if(physicalMCSwitch.equals(rootPM.getNeinodelist().get(0))){
								Link linkd=phyLayer.findLink(leafVM.getPM(), physicalMCSwitch);	
								linkd.setRemainingBandwidth(linkd.getRemainingBandwidth()-request.getTrafficDemand());
								Link newLinkd=new Link(linkd.getName(), request.getLinkNum(), null, request, leafVM, destNode, 1, 1);
								ArrayList<Link> phyLinkListd=new ArrayList<Link>();
								phyLinkListd.add(linkd);
								newLinkd.setPhyLinkList(phyLinkListd);
								request.addLink(newLinkd);
//								System.out.println("\t当前leaf VM和 root在同一个Switch之下，所以直接相连，分配带宽");
							}else{
								//直接路由
								LinearRoute directRoute=new LinearRoute("directRoute"+h, h, null);
								RouteSearching directSearch=new RouteSearching();
								
								directSearch.Dijkstras(rootPM, leafVM.getPM(), phyLayer, directRoute, constraint);
								
								if(directRoute.getLinklist().size()!=0){
									//直接路由成功
									for(Link directLink:directRoute.getLinklist()){
										directLink.setRemainingBandwidth(directLink.getRemainingBandwidth()-request.getTrafficDemand());
									}
									
									
									Link newDirecLink=new Link(request.getRootVM().getName()+"-"+leafVM.getName(), request.getLinkNum(), null, request, request.getRootVM(), leafVM, 
											directRoute.getlength(), directRoute.getlength());
									newDirecLink.setPhyLinkList(directRoute.getLinklist());
									request.addLink(newDirecLink);
									
								}else{
									//直接路由失败
									//需要撤销已经分配的资源								
									flag=false;
									/*Iterator<String> itrb=request.getLinklist().keySet().iterator();
									while(itrb.hasNext()){
										Link link=request.getLinklist().get(itrb.next());
										for(Link linkp:link.getPhyLinkList()){
											linkp.setRemainingBandwidth(linkp.getRemainingBandwidth()+request.getTrafficDemand());
										}
									}*/
									break;
								}
							}
							
							
							/*
							 * 直接路由结束
							 */
						}
					
					
					}
				}
			}else{
				/*
				 * 如果只有一个mc节点，也就是说所有的节点都分配到同一个ToR下了，那么做法反而简单了
				 * 只需要利用该ToR通信即可
				 * #########################
				 * ########ATTATION#########
				 * ######在这种情况下########
				 * #request是没有虚拟链路的#
				 * #可用于资源释放的时候的判别
				 * ########################			
				 */			
				
				Link rootLink=phyLayer.findLink(rootPM, rootPM.getNeinodelist().get(0));
				if(rootLink.getRemainingBandwidth()<request.getTrafficDemand()){
					flag=false;
				}else{
					rootLink.setRemainingBandwidth(rootLink.getRemainingBandwidth()-request.getTrafficDemand());
					Node vRootMC=vmcList.get(rootPM.getNeinodelist().get(0).getName());
					Link rootNewLink=new Link(rootLink.getName(), request.getLinkNum(), null, request, request.getRootVM(), vRootMC, 1, 1);
					ArrayList<Link> phyLinkListr=new ArrayList<Link>();
					phyLinkListr.add(rootLink);
					rootNewLink.setPhyLinkList(phyLinkListr);
					request.addLink(rootNewLink);
					
					Iterator<String> itrLeafVM=request.getLeafVMList().keySet().iterator();
					while(itrLeafVM.hasNext()){
						Node leafVM=request.getLeafVMList().get(itrLeafVM.next());
						Link leafLink=phyLayer.findLink(leafVM.getPM(), leafVM.getPM().getNeinodelist().get(0));
						if(leafLink.getRemainingBandwidth()<request.getTrafficDemand()){
							flag=false;
							break;
						}else{
							leafLink.setRemainingBandwidth(leafLink.getRemainingBandwidth()-request.getTrafficDemand());
						
							Node vLeafMC=vmcList.get(leafVM.getPM().getNeinodelist().get(0).getName());
							Link leafNewLink=new Link(leafLink.getName(), request.getLinkNum(), null, request, leafVM, vLeafMC, 1, 1);
							ArrayList<Link> phyLinkListl=new ArrayList<Link>();
							phyLinkListl.add(leafLink);
							leafNewLink.setPhyLinkList(phyLinkListl);
							request.addLink(leafNewLink);
					    }
					}
				}
				
			}
			
			
		}
		
		
		
		return flag;
	}
	
	public boolean unicastEmbedding(MixedCastRequest request,Layer phyLayer){
		boolean flag=true;
		ArrayList<NodePair> vmPairList=new ArrayList<NodePair>();
		
		Iterator<String> itr1=request.getNodepairlist().keySet().iterator();
		while(itr1.hasNext()){
			NodePair nodePair=(NodePair)(request.getNodepairlist().get(itr1.next()));
			System.out.println("Node Pair:"+nodePair.getName()+": "+nodePair.getTrafficdemand());
		}
		
		
		for(int i=0;i<request.getNodepairlist().size();i++){
			double maxTraffic=Double.MAX_VALUE;
			NodePair tempVMPair=null;
			Iterator<String> it=request.getNodepairlist().keySet().iterator();
			while(it.hasNext()){
				NodePair vmPair=(NodePair)(request.getNodepairlist().get(it.next()));
				if(vmPair.isArrange_status()==false){
					if(maxTraffic>vmPair.getTrafficdemand()){
						maxTraffic=vmPair.getTrafficdemand();
						tempVMPair=vmPair;
					}
				}		
			}
			
			vmPairList.add(0,tempVMPair);
			tempVMPair.setArrange_status(true);
		}
		
		for(int i=0;i<vmPairList.size();i++){
			NodePair currentVMPair=vmPairList.get(i);
			Node srcNode=currentVMPair.getSrcNode().getPM();
			Node desNode=currentVMPair.getDesNode().getPM();
		
			SearchConstraint constraint=new SearchConstraint();
			Iterator<String> it=phyLayer.getLinklist().keySet().iterator();
			while(it.hasNext()){
				Link link=(Link)(phyLayer.getLinklist().get(it.next()));
				if(link.getRemainingBandwidth()<currentVMPair.getTrafficdemand()){
					constraint.getExcludedLinklist().add(link);
				}
			}
			LinearRoute newRoute=new LinearRoute("route"+i, i, null);
			RouteSearching rs=new RouteSearching();
			rs.Dijkstras(srcNode, desNode, phyLayer, newRoute, constraint);
			
			if(newRoute.getLinklist().size()==0){	
				flag=false;
				break;
			}else{
				for(Link link:newRoute.getLinklist()){
					link.setRemainingBandwidth(link.getRemainingBandwidth()-currentVMPair.getTrafficdemand());
				}
				
				String name;
				if(srcNode.getIndex()<desNode.getIndex()){
					name=srcNode.getName()+"-"+desNode.getName();
				}else{
					name=desNode.getName()+"-"+srcNode.getName();
				}
				Link newLink=new Link(name, request.getLinklist().size(), null, request, currentVMPair.getSrcNode(), currentVMPair.getDesNode(), newRoute.getlength(), newRoute.getlength());
				request.addLink(newLink);
				newLink.setPhyLinkList(newRoute.getLinklist());
				newLink.setNodePair(currentVMPair);
			}
		
		
		}
		
		return flag;
	}
	
	
	public boolean enoughEdgeBandwidth(MixedCastRequest request,Layer phyLayer){
		boolean flag=true;
		Node rootPM=request.getRootVM().getPM();
		Link rootLink=phyLayer.findLink(rootPM, rootPM.getNeinodelist().get(0));
		if(rootLink.getRemainingBandwidth()<request.getTrafficDemand()){
			flag=false;
		}else{
			Iterator<String> itr1=request.getLeafVMList().keySet().iterator();
			while(itr1.hasNext()){
				Node leafVM=request.getLeafVMList().get(itr1.next());
				Link leafLink=phyLayer.findLink(leafVM.getPM(), leafVM.getPM().getNeinodelist().get(0));
				if(leafLink.getRemainingBandwidth()<request.getTrafficDemand()){
					flag=false;
					break;
				}
			}
		}		
		return flag;
	}
	
	public void minSpanningTree(MixedCastRequest request, ArrayList<Node> vmcNodeList){
		HashMap<String, Link> treeLinkList=new HashMap<String,Link>();
		ArrayList<Node> treeNodeList=new ArrayList<Node>();
		/*
		 * 遍历treeNode的每个节点，
		 * 遍历这些节点的相邻的节点
		 * 找到这些节点和相邻节点的链路
		 * 从其中找到长度最小的
		 * 然后将该相邻节点加到treeNode，
		 * 将链路加到treeLink
		 * 注意限制：所有的相邻节点不能是treeNode的节点
		 */
		
		treeNodeList.add(vmcNodeList.get(0));
		while(treeNodeList.size()!=vmcNodeList.size()){
			double tempLength=Double.MAX_VALUE;
			Link tempLink=null;
			Node tempNode=null;
			for(Node node:treeNodeList){
				for(Node neiNode:node.getNeinodelist()){
					if(!treeNodeList.contains(neiNode)){//必须保证当前要找的节点不能使treeNode节点
						Link link=request.findLink(node, neiNode);
						if(link!=null){
							if(link.getLength()<tempLength){
								tempLength=link.getLength();
								tempLink=link;
								tempNode=neiNode;
							} 
						}
					}									
				}
			}
			if((tempLink!=null)&&(tempNode!=null)){
				treeNodeList.add(tempNode);
				treeLinkList.put(tempLink.getName(), tempLink);
			}
		}
		
		
		//有两种情况
		//1，只有1个节点，那就是只有树根，链路为空
		//2，找到树了
		if(treeLinkList.size()==0){
			request.setLinklist(null);
		}else{
			ArrayList<Link> tempDelList=new ArrayList<Link>();
			Iterator<String> itr=request.getLinklist().keySet().iterator();
			while(itr.hasNext()){
				Link link=request.getLinklist().get(itr.next());
				if(!treeLinkList.containsKey(link.getName())){
					tempDelList.add(link);					
				}
			}
			
			for(Link link:tempDelList){
				request.removeLink(link.getName());
			}
			
		}
		
		
		
		//分配链路资源：
		//对每个tree link的物理链路，带宽应为一个单位的B
		Iterator<String> itr_treelink=treeLinkList.keySet().iterator();
		while(itr_treelink.hasNext()){
			Link link=treeLinkList.get(itr_treelink.next());
			for(Link link1:link.getPhyLinkList()){
				link1.setRemainingBandwidth(link1.getRemainingBandwidth()-request.getTrafficDemand());
			}
		}
	}
}
