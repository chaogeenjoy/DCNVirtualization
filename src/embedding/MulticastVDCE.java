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

public class MulticastVDCE {
	/*
	 * 算法描述：
	 * 1.首先将root VM映射
	 * 2.在排除了 root VM所映射的物理PM以后，映射剩余的VM具体是：
	 * 3.按照VM的size大小进行排序，然后一个一个映射（size=三种资源按照一定的权重求和）
	 * 4.随机映射
	 */
	public boolean vmEmbed(MulticastRequest request,Layer phyLayer){

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
	
	
	
	
	public boolean vlEmbed(MulticastRequest request,Layer phyLayer){
		boolean flag=true;
		if(!this.enoughEdgeBandwidth(request, phyLayer)){
			System.out.println("物理层叶子链路带宽不够-_-");
			flag=false;
		}else{
//			System.out.println("物理层叶子链路带宽还是够的^-^"); 
			
			ArrayList<Node> mcNodeList=new ArrayList<Node>();//行使multicast功能的交换机节点
			HashMap<String, Node> vmcList=new HashMap<String,Node>();//虚拟的上述节点
			ArrayList<Node> vmcNodeList=new ArrayList<Node>();//这个也是和上面的一样的，只不过是list结构而不是map结构
			
			
			
			/*
			 * 找到和所映射的服务器相连接的交换机，添加到mcNode中去
			 * attention:这里认为服务器只和一个交换机相连
			 */
			Node rootPM=request.getRootVM().getPM();
			Node rootMC=rootPM.getNeinodelist().get(0);		
			
			if((rootMC!=null)&&(rootMC.getAttribute()!=Constant.SERVER)){
//				System.out.println("和root PM直接相连的交换机节点是："+rootMC.getName());
				if(!mcNodeList.contains(rootMC)){
					mcNodeList.add(rootMC);	
					Node vmcNode=new Node(rootMC.getName(), vmcList.size(),null , request, Constant.SWITCH);
					vmcList.put(vmcNode.getName(), vmcNode);
					vmcNodeList.add(vmcNode);
				}
			}	
			
			Iterator<String> itr_mc=request.getNodelist().keySet().iterator();
			while(itr_mc.hasNext()){
				Node vm=request.getNodelist().get(itr_mc.next());
				Node mcNode=vm.getPM().getNeinodelist().get(0);//找到映射的服务器\
//				System.out.println("multicast switch:"+mcNode.getName());
				if((mcNode!=null)&&(mcNode.getAttribute()!=Constant.SERVER)){
//					System.out.println("PM"+vm.getPM().getName()+"\t直接相连的交换机节点是：\t"+mcNode.getName());
					if(!mcNodeList.contains(mcNode)){
						mcNodeList.add(mcNode);
						Node vmcNode=new Node(mcNode.getName(), vmcList.size(),null , request, Constant.SWITCH);
						vmcList.put(vmcNode.getName(), vmcNode);
						vmcNodeList.add(vmcNode);
					}
				}					
			}
			/*
			for(Node vmNode:vmcNodeList){
				System.out.println("包含进来的virtual mc node:"+vmNode.getName());
			}*/
			
			/*Iterator<String> itv=vmcList.keySet().iterator();
			while(itv.hasNext()){
				Node vmc=vmcList.get(itv.next());
				System.out.println("真正的vmc node list:"+vmc.getName());
			}*/
			
			/**
			 * 根据mc节点的个数来考虑吧是否需要构造生成树
			 */
			
			if(vmcNodeList.size()>1){	
				
				/*
				 * 在物理拓扑中，用容量足够的剩余链路为mcNodeList中的节点
				 * 构建虚拟的全连接，虚拟连接的长度为hop，
				 * attention:这里认为服务器只喝一个交换机相连，因此不需要从拓扑中删除所有的服务器
				 *            因为他们在构建虚拓扑的时候不构成任何影响
				 */
//				System.out.println("\n--------------------\n");
				SearchConstraint constraint=new SearchConstraint();
				Iterator<String> itr_L=phyLayer.getLinklist().keySet().iterator();
				while(itr_L.hasNext()){
					Link link=phyLayer.getLinklist().get(itr_L.next());
					if(link.getRemainingBandwidth()<request.getTrafficDemand()){
//						System.out.println("被排除的链路是");
						constraint.getExcludedLinklist().add(link);				
					}
				}
				
				
				//构建多播节点的虚拟互联，当然这里如果某个节点的互联不存在也不要紧
				int k=0;
				for(int i=0;i<mcNodeList.size();i++){
					for(int j=i+1;j<mcNodeList.size();j++){
						Node srcNode=mcNodeList.get(i);
						Node desNode=mcNodeList.get(j);				
						Node vmcSrc=vmcList.get(srcNode.getName());//属于request layer的虚拟节点
						Node vmcDes=vmcList.get(desNode.getName());
//						System.out.println(srcNode.getName()+"-"+desNode.getName());
						LinearRoute newRoute= new LinearRoute("route"+(k++), k, null);
						RouteSearching rs=new RouteSearching();
						
						rs.Dijkstras(srcNode, desNode, phyLayer, newRoute, constraint);
						
						if(newRoute.getLinklist().size()==0){
//							System.out.println("在互联各个多播switch的时候，找不到从"+srcNode.getName()+"到"+desNode.getName()+"的链路");
						}else{
							
							Link newLink=new Link(vmcSrc.getName()+"-"+vmcDes.getName(), k, null, request, vmcSrc, vmcDes, newRoute.getlength(), newRoute.getlength());
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
					//构建最小生成树
					//注意，在最小生成树构建完成的时候，带宽资源就已经分配了
//					System.out.println("一共有\t"+request.getLinkNum()+"\t个虚拟多播节点，实际至少需要\t"+(vmcNodeList.size()-1)+"\t个");
					
					this.minSpanningTree(request, vmcNodeList);
					
					
					
					
					
					
					
					
					/*System.out.println("\n\n最小生成树算法结束产生的链路有：");
					Iterator<String> itrvm=request.getLinklist().keySet().iterator();
					while(itrvm.hasNext()){
						Link link=request.getLinklist().get(itrvm.next());
						System.out.println("生成树的链路有："+link.getName());
					}*/
					
					/*
					 * 链路映射
					 * 首先找到VM映射到的PM所在的ToR Switch
					 * 然后在虚拟拓扑中路由从当root switch到leaf switches
					 * 如果路由失败，需要在物理层的链路上进行路由
					 * OK
					 */
					Node srcNode=vmcList.get(rootPM.getNeinodelist().get(0).getName());
					
					Link links=phyLayer.findLink(rootPM, rootPM.getNeinodelist().get(0));
					links.setRemainingBandwidth(links.getRemainingBandwidth()-request.getTrafficDemand());
					Link newLinks=new Link(links.getName(), request.getLinkNum(), null, request, request.getRootVM(), srcNode, 1, 1);
					ArrayList<Link> phyLinkLists=new ArrayList<Link>();
					phyLinkLists.add(links);
					newLinks.setPhyLinkList(phyLinkLists);
					request.addLink(newLinks);
//					System.out.println("和root PM直接相连的链路为"+newLinks.getPhyLinkList().get(0).getName()+"剩余带宽为："+links.getRemainingBandwidth()+"\n\t"
//							+"相应的新建虚拟链路为"+newLinks.getName());
								
					
					
					
					
					
					
					
					
					
					
					
//					System.out.println("----------------------------");
//					System.out.println();
					Iterator<String> itleaf=request.getNodelist().keySet().iterator();
					int h=0;
					while(itleaf.hasNext()){
						
						Node leafVM=request.getNodelist().get(itleaf.next());
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
									
									Link newDirecLink=new Link(rootPM.getName()+"-"+leafVM.getName(), request.getLinkNum(), null, request, request.getRootVM(), leafVM, 
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
						
						
						
						
						
					
						
						
						
						
						
						
						
						
						
						
//						System.out.println("\n*********************************************\n");
					}
					/*
					Iterator<String> ir2=phyLayer.getNodelist().keySet().iterator();
					while(ir2.hasNext()){
						Node pm=(Node)(phyLayer.getNodelist().get(ir2.next()));
						if((pm.getName().equals("N42"))||(pm.getName().equals("N40"))){
							for(Node node:pm.getNeinodelist()){
								System.out.println(pm.getName()+"--"+node.getName());
							}
							System.out.println("--------");
						}
					}*/
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
//				System.out.println("root link"+rootLink.getName());
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
					
					Iterator<String> itrLeafVM=request.getNodelist().keySet().iterator();
					while(itrLeafVM.hasNext()){
						Node leafVM=request.getNodelist().get(itrLeafVM.next());
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
	
	/**
	 * 该方法用来判别从叶子节点到ToR的链路单款资源是否都满足request
	 *     1：先判断root edge
	 *     2: 再遍历叶子结点集合，进行判断，但凡有一个不满足，则整个不满足
	 * @param request
	 * @param phyLayer
	 */
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
	/*
	 * 最小生成树算法：
	 * 1.从vmcList中取出一个node，放到Tree中，
	 * 2，在request的链路列表中搜索距离tree最短的那个节点，然后将该列表和相应的边添加到Tree中
	 */
	public void minSpanningTree(MulticastRequest request, ArrayList<Node> vmcNodeList){
//		ArrayList<Link> treeLinkList=new ArrayList<Link>();
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
			//将不属于tree的链路删除
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
