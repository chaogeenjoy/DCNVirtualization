package testSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import demand.Demand;
import demand.MixedCastRequest;
import general.Constant;
import mixedEmbedding.MixedEmbedding;
import network.Layer;
import network.Node;

public class VDCTest_MixedCast {
	public static void main(String[] args) {
		double[] erlang={0.2,0.4,0.6,0.8,1.0};
		
		long begin=System.currentTimeMillis();
		for(int i=0;i<erlang.length;i++){
			double er=erlang[i];
			System.out.println("------------erlang load = "+er+"------------------");
			Layer phyLayer=new Layer("PHYLayer", 0, null);
			phyLayer.readTopology("E:\\读论文\\DCN\\Topology\\P40T4A4.csv");
			
			Random leafNum=new Random(1);
			Random uniNum=new Random(7);
			Random cpu=new Random(2);
			Random memory=new Random(3);
			Random disk=new Random(4);
			Random demand=new Random(5);
			Random arrT=new Random(6);
			double arriveTime=-1.0/er*(Math.log(arrT.nextDouble()));
			MixedCastRequest mcRequest=new MixedCastRequest("request0", 0, null, Demand.generateTrafficDemand(demand), Constant.ARRIVAL, arriveTime, arriveTime-Math.log(arrT.nextDouble()));
			mcRequest.generateVM(leafNum, uniNum,cpu, memory, disk);
			mcRequest.generateVMPair(demand);
			ArrayList<MixedCastRequest> requestList=new ArrayList<MixedCastRequest>();
			requestList.add(mcRequest);
			
			
			
			
			
			
			
			
			
			int arrivalNUM=0;
			int acceptNUM=0;
			int acceptVMNum=0;
			while((arrivalNUM<Constant.SIM_WAN/1000)&&(!requestList.isEmpty())){
				MixedCastRequest currentRequest=requestList.get(0);
				if(currentRequest.getReqType()==Constant.ARRIVAL){
//					System.out.println("到达资源");
					arrivalNUM++;
					if(arrivalNUM%(Constant.SIM_WAN/10000)==0){
//						System.out.println();
						System.out.println("Arrival NO.:"+arrivalNUM+"\taccept num="+acceptNUM);//currentRequest.getName()+":Arrival  VM number:\t"+currentRequest.getVMNum()
					}
					/*if((arrivalNUM%29==0)||(arrivalNUM%26==0)||(arrivalNUM%27==0)||(arrivalNUM%28==0)){
						Iterator<String> it1=phyLayer.getLinklist().keySet().iterator();
						while(it1.hasNext()){
							Link pm=phyLayer.getLinklist().get(it1.next());
							System.out.println("PLink: "+pm.getName()+"\t"+pm.getRemainingBandwidth());
						}
						System.out.println("-----------------------------");
						Iterator<String> it2=currentRequest.getNodepairlist().keySet().iterator();
						while(it2.hasNext()){
							NodePair vmPair=currentRequest.getNodepairlist().get(it2.next());
							System.out.println("VPair:"+vmPair.getName()+"\t"+vmPair.getTrafficdemand());
						}
					}
					*/
					MixedCastRequest departRequest=new MixedCastRequest(currentRequest.getName(),currentRequest.getIndex(), null, currentRequest.getTrafficDemand(), Constant.DEPARTURE,
							currentRequest.getArriveTime(), currentRequest.getDepartTime());
					departRequest.copyRequest(currentRequest);
					insertRequest(requestList, departRequest);
					
					
					MixedEmbedding mcVDCE=new MixedEmbedding();
					if(mcVDCE.vmEmbed(currentRequest, phyLayer)){
					
						acceptVMNum++;
						
					
						
						if(mcVDCE.unicastEmbedding(currentRequest, phyLayer)){
							/*Iterator<String> ir2=phyLayer.getNodelist().keySet().iterator();
							while(ir2.hasNext()){
								Node pm=(Node)(phyLayer.getNodelist().get(ir2.next()));
								if((pm.getName().equals("N42"))||(pm.getName().equals("N40"))){
									for(Node node:pm.getNeinodelist()){
										System.out.println(pm.getName()+"--"+node.getName());
									}
									System.out.println("--------");
								}
							}*/
							acceptNUM++;
							currentRequest.setFlag(true);
							departRequest.setFlag(true);
							departRequest.copyRequest(currentRequest);
						}else{
							currentRequest.releaseVMResource();
							currentRequest.releaseLinkSource();
						}
					}
					
					double newArrivalTime=currentRequest.getArriveTime()-1.0/er*Math.log(arrT.nextDouble());
					double newDepartTime=newArrivalTime-Math.log(arrT.nextDouble());
					
					MixedCastRequest newRequest=new MixedCastRequest("request"+arrivalNUM, arrivalNUM, null, Demand.generateTrafficDemand(demand), Constant.ARRIVAL,
							newArrivalTime, newDepartTime);
					newRequest.generateVM(leafNum,uniNum, cpu, memory, disk);
					newRequest.generateVMPair(demand);
					requestList.remove(0);
					insertRequest(requestList, newRequest);
				}else{					
					if(currentRequest.isFlag()){
//						System.out.println("释放资源");
						currentRequest.releaseVMResource();	
						currentRequest.releaseLinkSource();
					}
					requestList.remove(0);
				}
			}
			
			System.out.println("accept vm num="+acceptVMNum);
			System.out.println("total accept num="+acceptNUM);
			System.out.println("accept ratio:"+((double)acceptNUM/arrivalNUM));
			System.out.println("\n\n");
		}
			
		System.out.println("\nsimulation time:"+(System.currentTimeMillis()-begin)+" (ms)");
	}
	
	
	
	
	
	
	
	
	
	
	public static void insertRequest(ArrayList<MixedCastRequest> requestList, MixedCastRequest request){
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
				MixedCastRequest currentRequest=requestList.get(i);
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
