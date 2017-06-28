package demand;

import java.util.Iterator;
import java.util.Random;

import general.Constant;
import network.Layer;
import network.Link;
import network.Node;

public class MulticastRequest extends Layer{
	private Node rootVM;
	private int leafNum;
	private double trafficDemand;
	private char reqType;
	private double arriveTime;
	private double departTime;
	private boolean flag;
	
	
	
	public MulticastRequest(String name, int index, String comments, double trafficDemand, char reqType,
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

	public void generateVM(Random leafNum,Random cpu,Random memory,Random disk){
		Node rootVM=new Node("N0", 0, null, this, Constant.SERVER);
		rootVM.setCpu(Demand.CPUDemand(cpu));
		rootVM.setMemory(Demand.MemoryDemand(memory));
		rootVM.setDisk(Demand.DiskDemand(disk));
		this.setRootVM(rootVM);
		this.setLeafNum(Demand.VMNumDeman(leafNum));
		for(int i=1;i<this.getLeafNum()+1;i++){
			Node VM=new Node("N"+i, i, null, this, Constant.SERVER);
			VM.setCpu(Demand.CPUDemand(cpu));
			VM.setMemory(Demand.MemoryDemand(memory));
			VM.setDisk(Demand.DiskDemand(disk));
			this.getNodelist().put(VM.getName(), VM);
		}
	}
	
	
	//该方法实现将已有的vdc request信息复制到当前的VDCRequest
		public void copyRequest(MulticastRequest request){
			this.setRootVM(request.getRootVM());
			this.setLeafNum(request.getLeafNum());
			this.setNodelist(request.getNodelist());
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
