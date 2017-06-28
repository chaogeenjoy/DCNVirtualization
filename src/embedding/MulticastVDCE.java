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
	 * �㷨������
	 * 1.���Ƚ�root VMӳ��
	 * 2.���ų��� root VM��ӳ�������PM�Ժ�ӳ��ʣ���VM�����ǣ�
	 * 3.����VM��size��С��������Ȼ��һ��һ��ӳ�䣨size=������Դ����һ����Ȩ����ͣ�
	 * 4.���ӳ��
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
		
		
		//��VM��������,����server�ڲ���Դ�ļ�Ȩ��͵Ĵ�С���н�������
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
		
        
		boolean flag=true;//��־��ǰ��VDC�Ƿ�ӳ��ɹ�
        
		//����PM������ӳ��root PM
		Node root=request.getRootVM();
		for(int i=0;i<PMList.size();i++){
			Node pm=PMList.get(i);
			if((pm.getCpu()>=root.getCpu())&&(pm.getMemory()>root.getMemory()&&(pm.getDisk()>root.getDisk()))){
				pm.setCpu(pm.getCpu()-root.getCpu());
				pm.setMemory(pm.getMemory()-root.getMemory());
				pm.setDisk(pm.getDisk()-root.getDisk());
				root.setPM(pm);
				root.setSuccessEmbed(true);//�����ͷ���Դ��ʱ����
				break;
			}
		}
		
		
		
		
		if(!root.isSuccessEmbed()){
			flag=false;
		}else{
			//ֻ�е�root�ڵ�ӳ��ɹ��Ժ�ſ��Զ������ڵ�ӳ��
			PMList.remove(root.getPM());//��root���ڵ�PM��PMlist��ȥ��
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
						pm.setSuccessEmbed(true);//�����־������ʾ��ǰ��PM�Ѿ������ͬһVDC�е�����VM�ˣ�Ҫע�⼰ʱ�ͷŵ�
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
			
			//��ʱ���ͷ���Դ
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
			System.out.println("�����Ҷ����·������-_-");
			flag=false;
		}else{
//			System.out.println("�����Ҷ����·�����ǹ���^-^"); 
			
			ArrayList<Node> mcNodeList=new ArrayList<Node>();//��ʹmulticast���ܵĽ������ڵ�
			HashMap<String, Node> vmcList=new HashMap<String,Node>();//����������ڵ�
			ArrayList<Node> vmcNodeList=new ArrayList<Node>();//���Ҳ�Ǻ������һ���ģ�ֻ������list�ṹ������map�ṹ
			
			
			
			/*
			 * �ҵ�����ӳ��ķ����������ӵĽ���������ӵ�mcNode��ȥ
			 * attention:������Ϊ������ֻ��һ������������
			 */
			Node rootPM=request.getRootVM().getPM();
			Node rootMC=rootPM.getNeinodelist().get(0);		
			
			if((rootMC!=null)&&(rootMC.getAttribute()!=Constant.SERVER)){
//				System.out.println("��root PMֱ�������Ľ������ڵ��ǣ�"+rootMC.getName());
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
				Node mcNode=vm.getPM().getNeinodelist().get(0);//�ҵ�ӳ��ķ�����\
//				System.out.println("multicast switch:"+mcNode.getName());
				if((mcNode!=null)&&(mcNode.getAttribute()!=Constant.SERVER)){
//					System.out.println("PM"+vm.getPM().getName()+"\tֱ�������Ľ������ڵ��ǣ�\t"+mcNode.getName());
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
				System.out.println("����������virtual mc node:"+vmNode.getName());
			}*/
			
			/*Iterator<String> itv=vmcList.keySet().iterator();
			while(itv.hasNext()){
				Node vmc=vmcList.get(itv.next());
				System.out.println("������vmc node list:"+vmc.getName());
			}*/
			
			/**
			 * ����mc�ڵ�ĸ��������ǰ��Ƿ���Ҫ����������
			 */
			
			if(vmcNodeList.size()>1){	
				
				/*
				 * �����������У��������㹻��ʣ����·ΪmcNodeList�еĽڵ�
				 * ���������ȫ���ӣ��������ӵĳ���Ϊhop��
				 * attention:������Ϊ������ֻ��һ����������������˲���Ҫ��������ɾ�����еķ�����
				 *            ��Ϊ�����ڹ��������˵�ʱ�򲻹����κ�Ӱ��
				 */
//				System.out.println("\n--------------------\n");
				SearchConstraint constraint=new SearchConstraint();
				Iterator<String> itr_L=phyLayer.getLinklist().keySet().iterator();
				while(itr_L.hasNext()){
					Link link=phyLayer.getLinklist().get(itr_L.next());
					if(link.getRemainingBandwidth()<request.getTrafficDemand()){
//						System.out.println("���ų�����·��");
						constraint.getExcludedLinklist().add(link);				
					}
				}
				
				
				//�����ಥ�ڵ�����⻥������Ȼ�������ĳ���ڵ�Ļ���������Ҳ��Ҫ��
				int k=0;
				for(int i=0;i<mcNodeList.size();i++){
					for(int j=i+1;j<mcNodeList.size();j++){
						Node srcNode=mcNodeList.get(i);
						Node desNode=mcNodeList.get(j);				
						Node vmcSrc=vmcList.get(srcNode.getName());//����request layer������ڵ�
						Node vmcDes=vmcList.get(desNode.getName());
//						System.out.println(srcNode.getName()+"-"+desNode.getName());
						LinearRoute newRoute= new LinearRoute("route"+(k++), k, null);
						RouteSearching rs=new RouteSearching();
						
						rs.Dijkstras(srcNode, desNode, phyLayer, newRoute, constraint);
						
						if(newRoute.getLinklist().size()==0){
//							System.out.println("�ڻ��������ಥswitch��ʱ���Ҳ�����"+srcNode.getName()+"��"+desNode.getName()+"����·");
						}else{
							
							Link newLink=new Link(vmcSrc.getName()+"-"+vmcDes.getName(), k, null, request, vmcSrc, vmcDes, newRoute.getlength(), newRoute.getlength());
							newLink.setPhyLinkList(newRoute.getLinklist());
							request.addLink(newLink);
//							System.out.println("�½���ȫ������·Ϊ��"+newLink.getName());
//							System.out.println("��������������·Ϊ��");
//							newRoute.OutputRoute_node(newRoute);
						}							
					}
				}
				
				
				
				
				if(request.getLinklist().size()<vmcNodeList.size()-1){
//					System.out.println("һ����\t"+request.getLinkNum()+"\t������ಥ�ڵ㣬ʵ��������Ҫ\t"+(vmcNodeList.size()-1)+"\t��");
					request.getLinklist().clear();
					flag=false;
				}else{
					//������С������
					//ע�⣬����С������������ɵ�ʱ�򣬴�����Դ���Ѿ�������
//					System.out.println("һ����\t"+request.getLinkNum()+"\t������ಥ�ڵ㣬ʵ��������Ҫ\t"+(vmcNodeList.size()-1)+"\t��");
					
					this.minSpanningTree(request, vmcNodeList);
					
					
					
					
					
					
					
					
					/*System.out.println("\n\n��С�������㷨������������·�У�");
					Iterator<String> itrvm=request.getLinklist().keySet().iterator();
					while(itrvm.hasNext()){
						Link link=request.getLinklist().get(itrvm.next());
						System.out.println("����������·�У�"+link.getName());
					}*/
					
					/*
					 * ��·ӳ��
					 * �����ҵ�VMӳ�䵽��PM���ڵ�ToR Switch
					 * Ȼ��������������·�ɴӵ�root switch��leaf switches
					 * ���·��ʧ�ܣ���Ҫ����������·�Ͻ���·��
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
//					System.out.println("��root PMֱ����������·Ϊ"+newLinks.getPhyLinkList().get(0).getName()+"ʣ�����Ϊ��"+links.getRemainingBandwidth()+"\n\t"
//							+"��Ӧ���½�������·Ϊ"+newLinks.getName());
								
					
					
					
					
					
					
					
					
					
					
					
//					System.out.println("----------------------------");
//					System.out.println();
					Iterator<String> itleaf=request.getNodelist().keySet().iterator();
					int h=0;
					while(itleaf.hasNext()){
						
						Node leafVM=request.getNodelist().get(itleaf.next());
//						System.out.print("��ǰҶ�ӽڵ�������Ϊ��"+leafVM.getName());
						Node physicalMCSwitch=leafVM.getPM().getNeinodelist().get(0);//��PMֱ����������multicast switch
						Node destNode=vmcList.get(physicalMCSwitch.getName());
//						System.out.println("�����Ľ�������"+physicalMCSwitch.getName()+"����"+destNode.getName());

						LinearRoute newRoute=new LinearRoute("multicast route"+h, h, null);
						RouteSearching rs=new RouteSearching();
						rs.Dijkstras(srcNode, destNode, request, newRoute, null);
						
						
						
						
						
						
						
						
						
						
						/*
						 * 1.����ɹ�����ֻ��Ҫ�����ToR�� PM�Ĵ���
						 * 2.���ʧ�ܣ���
						 *     a)�ж�Դ�ڵ��Ŀ�Ľڵ��Ƿ���һ��ToR�£�����ǣ����·�ɣ�����
						 *     b)����ֱ��·�ɣ����ʧ�ܣ�������
						 */
						if(newRoute.getLinklist().size()!=0){
							Link linkd=phyLayer.findLink(leafVM.getPM(), physicalMCSwitch);							
							linkd.setRemainingBandwidth(linkd.getRemainingBandwidth()-request.getTrafficDemand());	
							Link newLinkd=new Link(linkd.getName(), request.getLinkNum(), null, request, leafVM, destNode, 1, 1);
							ArrayList<Link> phyLinkListd=new ArrayList<Link>();
							phyLinkListd.add(linkd);
							newLinkd.setPhyLinkList(phyLinkListd);
							request.addLink(newLinkd);
//							System.out.println("\t��leaf PMֱ����������·Ϊ"+newLinkd.getPhyLinkList().get(0).getName()+"ʣ�����Ϊ��"+linkd.getRemainingBandwidth()+"\n\t"
//									+"��Ӧ���½�������·Ϊ"+newLinkd.getName());
						}else{
							
							
							if(physicalMCSwitch.equals(rootPM.getNeinodelist().get(0))){
								Link linkd=phyLayer.findLink(leafVM.getPM(), physicalMCSwitch);	
								linkd.setRemainingBandwidth(linkd.getRemainingBandwidth()-request.getTrafficDemand());
								Link newLinkd=new Link(linkd.getName(), request.getLinkNum(), null, request, leafVM, destNode, 1, 1);
								ArrayList<Link> phyLinkListd=new ArrayList<Link>();
								phyLinkListd.add(linkd);
								newLinkd.setPhyLinkList(phyLinkListd);
								request.addLink(newLinkd);
//								System.out.println("\t��ǰleaf VM�� root��ͬһ��Switch֮�£�����ֱ���������������");
							}else{
								//ֱ��·��
								LinearRoute directRoute=new LinearRoute("directRoute"+h, h, null);
								RouteSearching directSearch=new RouteSearching();
								
								directSearch.Dijkstras(rootPM, leafVM.getPM(), phyLayer, directRoute, constraint);
								
								if(directRoute.getLinklist().size()!=0){
									//ֱ��·�ɳɹ�
									for(Link directLink:directRoute.getLinklist()){
										directLink.setRemainingBandwidth(directLink.getRemainingBandwidth()-request.getTrafficDemand());
									}
									
									Link newDirecLink=new Link(rootPM.getName()+"-"+leafVM.getName(), request.getLinkNum(), null, request, request.getRootVM(), leafVM, 
											directRoute.getlength(), directRoute.getlength());
									newDirecLink.setPhyLinkList(directRoute.getLinklist());
									request.addLink(newDirecLink);
									
								}else{
									//ֱ��·��ʧ��
									//��Ҫ�����Ѿ��������Դ								
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
							 * ֱ��·�ɽ���
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
				 * ���ֻ��һ��mc�ڵ㣬Ҳ����˵���еĽڵ㶼���䵽ͬһ��ToR���ˣ���ô������������
				 * ֻ��Ҫ���ø�ToRͨ�ż���
				 * #########################
				 * ########ATTATION#########
				 * ######�����������########
				 * #request��û��������·��#
				 * #��������Դ�ͷŵ�ʱ����б�
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
	 * �÷��������б��Ҷ�ӽڵ㵽ToR����·������Դ�Ƿ�����request
	 *     1�����ж�root edge
	 *     2: �ٱ���Ҷ�ӽ�㼯�ϣ������жϣ�������һ�������㣬������������
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
	 * ��С�������㷨��
	 * 1.��vmcList��ȡ��һ��node���ŵ�Tree�У�
	 * 2����request����·�б�����������tree��̵��Ǹ��ڵ㣬Ȼ�󽫸��б����Ӧ�ı���ӵ�Tree��
	 */
	public void minSpanningTree(MulticastRequest request, ArrayList<Node> vmcNodeList){
//		ArrayList<Link> treeLinkList=new ArrayList<Link>();
		HashMap<String, Link> treeLinkList=new HashMap<String,Link>();
		ArrayList<Node> treeNodeList=new ArrayList<Node>();
		/*
		 * ����treeNode��ÿ���ڵ㣬
		 * ������Щ�ڵ�����ڵĽڵ�
		 * �ҵ���Щ�ڵ�����ڽڵ����·
		 * �������ҵ�������С��
		 * Ȼ�󽫸����ڽڵ�ӵ�treeNode��
		 * ����·�ӵ�treeLink
		 * ע�����ƣ����е����ڽڵ㲻����treeNode�Ľڵ�
		 */
		
		treeNodeList.add(vmcNodeList.get(0));
		while(treeNodeList.size()!=vmcNodeList.size()){
			double tempLength=Double.MAX_VALUE;
			Link tempLink=null;
			Node tempNode=null;
			for(Node node:treeNodeList){
				for(Node neiNode:node.getNeinodelist()){
					if(!treeNodeList.contains(neiNode)){//���뱣֤��ǰҪ�ҵĽڵ㲻��ʹtreeNode�ڵ�
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
		
		
		//���������
		//1��ֻ��1���ڵ㣬�Ǿ���ֻ����������·Ϊ��
		//2���ҵ�����
		if(treeLinkList.size()==0){
			request.setLinklist(null);
		}else{
			//��������tree����·ɾ��
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
		
		
		
		//������·��Դ��
		//��ÿ��tree link��������·������ӦΪһ����λ��B
		Iterator<String> itr_treelink=treeLinkList.keySet().iterator();
		while(itr_treelink.hasNext()){
			Link link=treeLinkList.get(itr_treelink.next());
			for(Link link1:link.getPhyLinkList()){
				link1.setRemainingBandwidth(link1.getRemainingBandwidth()-request.getTrafficDemand());
			}
		}
	}
	
	
}
