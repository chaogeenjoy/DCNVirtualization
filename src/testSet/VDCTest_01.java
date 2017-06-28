package testSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import demand.VDCRequest;
import general.Constant;
import network.Layer;
import network.Node;
import network.NodePair;

public class VDCTest_01 {
	public static void main(String[] args) {
		Layer layer0=new Layer("PHYLayer", 0, null);
		layer0.readTopology("E:\\¶ÁÂÛÎÄ\\DCN\\Topology\\P9T3A2I2.csv");
		
		
		Random vmNum=new Random(1);
		Random cpu=new Random(2);
		Random memory=new Random(3) ;
		Random disk=new Random(4);
		Random demand=new Random(5);
		Random arrT=new Random(6);
		int VMNum=vmNum.nextInt(6)+4;
		double arriveTime=1/8*(-Math.log(arrT.nextDouble()));
		VDCRequest vdcRequest=new VDCRequest("VDC0", 0, null, Constant.ARRIVAL,arriveTime, arriveTime-Math.log(arrT.nextDouble()));
		vdcRequest.generateVM(vmNum, cpu, memory, disk);
		vdcRequest.generateNodepairs(demand);
		ArrayList<VDCRequest> requestList=new ArrayList<VDCRequest>();
		requestList.add(vdcRequest);
		
		Iterator<String> itr=vdcRequest.getNodelist().keySet().iterator();
		while(itr.hasNext()){
			Node vm=vdcRequest.getNodelist().get(itr.next());
			System.out.println(vm.getName());
		}
		int i=0;
		Iterator<String> itr1=vdcRequest.getNodepairlist().keySet().iterator();
		while(itr1.hasNext()){
			NodePair vmPair=vdcRequest.getNodepairlist().get(itr1.next());
			System.out.println(vmPair.getName());
			i++;
		}
		System.out.println(i);
		
		
		
		
		
		
		
	}
	
	
	public static void insertRequest(ArrayList<VDCRequest> requestList, VDCRequest request){
		if(requestList.size()==0){
			requestList.add(0, request);
		}else{
			double occurTime;
			if(request.getReqType()==Constant.ARRIVAL)
				occurTime=request.getArriveTime();
			else
				occurTime=request.getDepartTime();
			boolean inserted=false;
			for(int i=0;i<requestList.size();i++){
				VDCRequest currentRequest=requestList.get(i);
				double compareTime;
				if(currentRequest.getReqType()==Constant.ARRIVAL)
					compareTime=currentRequest.getArriveTime();
				else
					compareTime=currentRequest.getDepartTime();
				if(occurTime<compareTime){
					requestList.add(i, request);
					inserted=true;
					break;
				}
			}
			if(!inserted){
				requestList.add(request);
			}
		}
	}
	

}
