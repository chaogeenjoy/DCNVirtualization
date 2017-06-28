package embedding;

import java.util.ArrayList;
import java.util.Iterator;

import demand.VDCRequest;
import general.Constant;
import graphalgorithms.RouteSearching;
import graphalgorithms.SearchConstraint;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;
/*
 * input：VDC request：一组虚拟机，每个虚拟机对cpu、memory、disk的数量，以及虚拟机对之间的带宽需求
 *      对于每个VM请求：
 *                  查找PM是否剩余足够，如果足够找到剩余的最少的那个，然后embedding
 */
public class VDCEmbedding {
	/*
	 * 算法：
	 *    step1：取出第一个VM
	 *    step2：取出第一个PM
	 *    step3：判断该PM是否可用于VM，如果可用，执行step4，否则转入step5。
	 *    step4：将该VM临时映射到PM，占用PM资源，跳到下一个VM，转入step3。如果没有下一个VM了，则跳到step6
	 *    step5：选择下一个PM，转入step3，如果遍历了所有的PM了，当前VM映射失败，整个VDC映射失败。
	 *    step6：VDC映射成功
	 */
	public boolean vmEmbedding(VDCRequest request,Layer phyLayer){
        ArrayList<Node> PMList=new ArrayList<Node>();
		
		Iterator<String> itr=phyLayer.getNodelist().keySet().iterator();
		while(itr.hasNext()){
			Node node=(Node)(phyLayer.getNodelist().get(itr.next()));
			if(node.getAttribute()==Constant.SERVER){
				PMList.add(node);
			}
		}
		
		
		boolean flag=true;
		Iterator<String> it1=request.getNodelist().keySet().iterator();
		while(it1.hasNext()){
			Node vm=request.getNodelist().get(it1.next());
			int i=0;
			for(;i<PMList.size();i++){
				Node pm=PMList.get(i);
				if((pm.getCpu()>=vm.getCpu())&&(pm.getMemory()>vm.getMemory()&&(pm.getDisk()>vm.getDisk()))){//
					pm.setCpu(pm.getCpu()-vm.getCpu());
					pm.setMemory(pm.getMemory()-vm.getMemory());
					pm.setDisk(pm.getDisk()-vm.getDisk());
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
		
				
		
		if(!flag){//when vm embedding failed , the resource allocated for vm should be released
			Iterator<String> it2=request.getNodelist().keySet().iterator();
			while(it2.hasNext()){
				Node vm=request.getNodelist().get(it2.next());
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
		
		return flag;
		
	}
	
	
	
	
	
	
	
	/*
	 * 和vmEbedding相比，这个的做法是将vm按照cpu的数量递减的形式进行排序，然后再分配
	 */
	public boolean vmEmbedding_sort(VDCRequest request,Layer phyLayer){
        ArrayList<Node> PMList=new ArrayList<Node>();
        ArrayList<Node> VMList=new ArrayList<Node>();
		
		Iterator<String> itr=phyLayer.getNodelist().keySet().iterator();
		while(itr.hasNext()){
			Node node=(Node)(phyLayer.getNodelist().get(itr.next()));
			if(node.getAttribute()==Constant.SERVER){
				PMList.add(node);
			}
		}
		
		

		for(int j=0;j<request.getNodelist().size();j++){
			
			int minCPU=Integer.MAX_VALUE;
			Node tempVM=null;
			Iterator<String> it0=request.getNodelist().keySet().iterator();
			while(it0.hasNext()){
				Node vm=request.getNodelist().get(it0.next());
				if(!vm.isArrange()){
					if(vm.getCpu()<=minCPU){
						minCPU=vm.getCpu();
						tempVM=vm;
					}
				}		
				
			}
			VMList.add(0,tempVM);
			tempVM.setArrange(true);
		}

		
		boolean flag=true;
		Iterator<Node> it1=VMList.iterator();
		while(it1.hasNext()){
			Node vm=(Node)  it1.next();
			int i=0;
			for(;i<PMList.size();i++){
				Node pm=PMList.get(i);
				if((pm.getCpu()>=vm.getCpu())&&(pm.getMemory()>vm.getMemory()&&(pm.getDisk()>vm.getDisk()))){//
					pm.setCpu(pm.getCpu()-vm.getCpu());
					pm.setMemory(pm.getMemory()-vm.getMemory());
					pm.setDisk(pm.getDisk()-vm.getDisk());
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
		
		
		if(!flag){//when vm embedding failed , the resource allocated for vm should be released
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
		
		return flag;
		
	}
	
	
	
	/*
	 * 这里和上面的映射程序相比，除了对VM进行排序以外，并且不同的VM不可以映射 到同一个PM
	 */
	
	public boolean vmEmbeddingsortVM_NonSharePM(VDCRequest request,Layer phyLayer){
        ArrayList<Node> PMList=new ArrayList<Node>();
        ArrayList<Node> VMList=new ArrayList<Node>();
		
        //从节点列表中找到PM放到PMList中
		Iterator<String> itr=phyLayer.getNodelist().keySet().iterator();
		while(itr.hasNext()){
			Node node=(Node)(phyLayer.getNodelist().get(itr.next()));
			if(node.getAttribute()==Constant.SERVER){
				PMList.add(node);
			}
		}

		//对vm按照CPU数量进行排序，放在VMList中
		for(int j=0;j<request.getNodelist().size();j++){
			
			int minCPU=Integer.MAX_VALUE;
			Node tempVM=null;
			Iterator<String> it0=request.getNodelist().keySet().iterator();
			while(it0.hasNext()){
				Node vm=request.getNodelist().get(it0.next());
				if(!vm.isArrange()){
					if(vm.getCpu()<=minCPU){
						minCPU=vm.getCpu();
						tempVM=vm;
					}
				}		
				
			}
			VMList.add(0,tempVM);
			tempVM.setArrange(true);
		}

		
		
		//判断是否可以映射
		boolean flag=true;
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
		
		//及时释放掉标志，放置其他VDC使用时候产生问题
		for(Node pm:PMList){
			pm.setSuccessEmbed(false);
		}
		
		if(!flag){//when vm embedding failed , the resource allocated for vm should be released
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
		
		return flag;
		
	}
	
	//这里给出的request应当是VM映射好的
	public boolean vlEmbedding(VDCRequest request,Layer phyLayer){
		boolean flag=true;//表示virtual link是否映射成功
		ArrayList<NodePair> vmPairList=new ArrayList<NodePair>();


		//按照节点对需求大小对node pair 进行排序
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
		
		//对每一个virtual link 对其进行映射
		for(int i=0;i<vmPairList.size();i++){
			NodePair currentVMPair=vmPairList.get(i);
			Node srcNode=currentVMPair.getSrcNode().getPM();//corresponding PM source node of virtual node pair
			Node desNode=currentVMPair.getDesNode().getPM();
			
//			System.out.println("srcNode:"+srcNode.getName()+"\t destination:"+desNode.getName());
			SearchConstraint constraint=new SearchConstraint();
			Iterator<String> it=phyLayer.getLinklist().keySet().iterator();
			while(it.hasNext()){
				Link link=(Link)(phyLayer.getLinklist().get(it.next()));
//				System.out.println("链路的剩余带宽："+link.getName()+"\t"+link.getRemainingBandwidth());
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
				
				Link newLink=new Link(srcNode.getName()+"-"+desNode.getName(), request.getLinklist().size(), null, request, currentVMPair.getSrcNode(), currentVMPair.getDesNode(), newRoute.getlength(), newRoute.getlength());
				request.addLink(newLink);
				newLink.setPhyLinkList(newRoute.getLinklist());
				newLink.setNodePair(currentVMPair);
			}
		}
		
		
		//如果Virtual Link映射失败，那么，将已经给分配给virtual link的链路资源释放掉		
		/*if(!flag){
			request.releaseLinkSource();
		}
         */
		
		return flag;
	}
}
 