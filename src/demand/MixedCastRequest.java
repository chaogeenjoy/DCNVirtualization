package demand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import general.Constant;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;

public class MixedCastRequest extends Layer {
	private Node rootVM;
	private int leafNum;//多播的叶子数目
	private int uniNum;//单播的数目
	private double trafficDemand;//多播带宽请求
	private char reqType;
	private double arriveTime;
	private double departTime;
	private boolean flag;
	private HashMap<String, Node> leafVMList=new HashMap<String,Node>();
	private HashMap<String, Node> uniVMList=new HashMap<String,Node>();
	
	
	
	public MixedCastRequest(String name, int index, String comments, double trafficDemand, char reqType,
			double arriveTime, double departTime) {
		super(name, index, comments);
		this.trafficDemand = trafficDemand;
		this.reqType = reqType;
		this.arriveTime = arriveTime;
		this.departTime = departTime;
	}
	public Node getRootVM() {
		return rootVM;
	}
	public void setRootVM(Node rootVM) {
		this.rootVM = rootVM;
	}
	public int getLeafNum() {
		return leafNum;
	}
	public void setLeafNum(int leafNum) {
		this.leafNum = leafNum;
	}
	public int getUniNum() {
		return uniNum;
	}
	public void setUniNum(int uniNum) {
		this.uniNum = uniNum;
	}
	public double getTrafficDemand() {
		return trafficDemand;
	}
	public void setTrafficDemand(double trafficDemand) {
		this.trafficDemand = trafficDemand;
	}
	public char getReqType() {
		return reqType;
	}
	public void setReqType(char reqType) {
		this.reqType = reqType;
	}
	public double getArriveTime() {
		return arriveTime;
	}
	public void setArriveTime(double arriveTime) {
		this.arriveTime = arriveTime;
	}
	public double getDepartTime() {
		return departTime;
	}
	public void setDepartTime(double departTime) {
		this.departTime = departTime;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public HashMap<String, Node> getLeafVMList() {
		return leafVMList;
	}
	public void setLeafVMList(HashMap<String, Node> leafVMList) {
		this.leafVMList = leafVMList;
	}
	public HashMap<String, Node> getUniVMList() {
		return uniVMList;
	}
	public void setUniVMList(HashMap<String, Node> uniVMList) {
		this.uniVMList = uniVMList;
	}
	
	public void generateVM(Random leafNum,Random uniNum,Random cpu,Random memory,Random disk){
		Node rootVM=new Node("N0", 0, null, this, Constant.SERVER);
		rootVM.setCpu(Demand.CPUDemand(cpu));
		rootVM.setMemory(Demand.MemoryDemand(memory));
		rootVM.setDisk(Demand.DiskDemand(disk));
		this.setRootVM(rootVM);
		
		this.setLeafNum(Demand.VMNumDeman(leafNum));
		for(int i=1;i<this.getLeafNum()+1;i++){
			Node VM=new Node("N"+(this.getNodeNum()+1), this.getNodeNum()+1, null, this, Constant.SERVER);
			VM.setUniCast(false);
			VM.setCpu(Demand.CPUDemand(cpu));
			VM.setMemory(Demand.MemoryDemand(memory));
			VM.setDisk(Demand.DiskDemand(disk));
			this.getNodelist().put(VM.getName(), VM);
			this.getLeafVMList().put(VM.getName(), VM);
		}
		
		this.setUniNum(Demand.UNINumDeamnd(uniNum));
		for(int i=0;i<this.getUniNum();i++){
			Node VM=new Node("N"+(this.getNodeNum()+1), this.getNodeNum()+1, null, this, Constant.SERVER);
			VM.setUniCast(true);
			VM.setCpu(Demand.CPUDemand(cpu));
			VM.setMemory(Demand.MemoryDemand(memory));
			VM.setDisk(Demand.DiskDemand(disk));
			this.getNodelist().put(VM.getName(), VM);
			this.getUniVMList().put(VM.getName(),VM);
		}
		
	}
	
	public void generateVMPair(Random demand){
		HashMap<String, Node> map = this.getUniVMList();
		HashMap<String, Node> map2 = this.getUniVMList();
		Iterator<String> iter1 = map.keySet().iterator();
		
		while (iter1.hasNext()) {
		    Node node1 = (Node)(map.get(iter1.next()));
		    Iterator<String> iter2 = map2.keySet().iterator();
		    while(iter2.hasNext()){
		    	Node node2= (Node)(map2.get(iter2.next()));
				if (!node1.equals(node2)) {
					if (node1.getIndex() < node2.getIndex()) {
						String name = node1.getName() + "-" + node2.getName();
						int index = this.getNodepairlist().size();
						NodePair nodepair = new NodePair(name, index, "", this,
								node1, node2);
						nodepair.setTrafficdemand(Demand.generateTrafficDemand(demand));
						this.addNodepair(nodepair);
					}
		    	}
		    	
		    }		    
		} 		
	}
	
	public void copyRequest(MixedCastRequest request){
		this.setLeafNum(request.getLeafNum());
		this.setUniNum(request.getUniNum());
		this.setRootVM(request.getRootVM());
		this.setNodelist(request.getNodelist());
		this.setUniVMList(request.getUniVMList());
		this.setLeafVMList(request.getLeafVMList());
		this.setNodepairlist(request.getNodepairlist());
		this.setLinklist(request.getLinklist());
	}
	
	public void releaseVMResource(){
		Node rootPM=this.getRootVM().getPM();
		rootPM.setCpu(rootPM.getCpu()+this.getRootVM().getCpu());
		rootPM.setMemory(rootPM.getMemory()+this.getRootVM().getMemory());
		rootPM.setDisk(rootPM.getDisk()+this.getRootVM().getDisk());
		this.getRootVM().setPM(null);
		Iterator<String> it=this.getNodelist().keySet().iterator();
		while(it.hasNext()){
			Node vm=this.getNodelist().get(it.next());
			vm.getPM().setCpu(vm.getPM().getCpu()+vm.getCpu());
			vm.getPM().setMemory(vm.getPM().getMemory()+vm.getMemory());
			vm.getPM().setDisk(vm.getPM().getDisk()+vm.getDisk());
			vm.setPM(null);
		}
	}
	
	public void releaseLinkSource(){
		Iterator<String> itr=this.getLinklist().keySet().iterator();
		while(itr.hasNext()){
			Link link=(Link)(this.getLinklist().get(itr.next()));
			for(Link pLink:link.getPhyLinkList()){
				pLink.setRemainingBandwidth(pLink.getRemainingBandwidth()+this.getTrafficDemand());
			}
		}
	}
}
