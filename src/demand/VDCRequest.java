package demand;

import java.util.Iterator;
import java.util.Random;

import general.Constant;
import network.Layer;
import network.Link;
import network.Node;

/*
 * VDCRequest:
 *     虚拟机的数量，
 *     每个虚拟机的：
 *        1：cpu
 *        2:memory
 *        3:disk
 *        4:任意两个VM之间的带宽需求
 */
public class VDCRequest extends Layer {
	private int VMNum;
	private char reqType;
	private double arriveTime;
	private double departTime;
	private boolean flag=false;//表示该VDC embedding是否成功

	
	public VDCRequest(String name, int index, String comments, char reqType, double arriveTime,
			double departTime) {
		super(name, index, comments);
		this.setReqType(reqType);
		this.setArriveTime(arriveTime);
		this.setDepartTime(departTime);
	}
	
	
	
	public int getVMNum() {
		return VMNum;
	}


	public void setVMNum(int vMNum) {
		VMNum = vMNum;
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



	public void generateVM(Random vmNum,Random cpu,Random memory,Random disk){
		this.setVMNum(Demand.UNINumDeamnd(vmNum));
		for(int i=0;i<this.getVMNum();i++){
			Node VM=new Node("N"+i, i, null, this, Constant.SERVER);
			VM.setCpu(Demand.CPUDemand(cpu));
			VM.setMemory(Demand.MemoryDemand(memory));
			VM.setDisk(Demand.DiskDemand(disk));
			this.getNodelist().put(VM.getName(), VM);
		}
	}
	
	
	
	//该方法实现将已有的vdc request信息复制到当前的VDCRequest
	public void copyRequest(VDCRequest request){
		this.setVMNum(request.getVMNum());
		this.setNodelist(request.getNodelist());
		this.setNodepairlist(request.getNodepairlist());
		this.setLinklist(request.getLinklist());
	}
	
	
	/*
	 * 将本次request所占用的PM资源释放掉
	 */
	public void releaseVMResource(){
				
		Iterator<String> it=this.getNodelist().keySet().iterator();
		while(it.hasNext()){
			Node vm=this.getNodelist().get(it.next());
			vm.getPM().setCpu(vm.getPM().getCpu()+vm.getCpu());
			vm.getPM().setMemory(vm.getPM().getMemory()+vm.getMemory());
			vm.getPM().setDisk(vm.getPM().getDisk()+vm.getDisk());
		}
	}
	
	public void releaseLinkSource(){
		Iterator<String> itr=this.getLinklist().keySet().iterator();
		while(itr.hasNext()){
			Link link=(Link)(this.getLinklist().get(itr.next()));
			for(Link pLink:link.getPhyLinkList()){
				pLink.setRemainingBandwidth(pLink.getRemainingBandwidth()+link.getNodePair().getTrafficdemand());
			}
		}
	}
}
