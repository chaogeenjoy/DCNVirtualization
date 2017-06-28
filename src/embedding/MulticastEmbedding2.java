package embedding;

import java.util.ArrayList;
import java.util.Iterator;

import demand.MulticastRequest;
import general.Constant;
import network.Layer;
import network.Node;

public class MulticastEmbedding2 {

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
	
	
	
}
