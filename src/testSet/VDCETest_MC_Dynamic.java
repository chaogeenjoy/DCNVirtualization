package testSet;

import java.util.ArrayList;
import java.util.Random;

import demand.Demand;
import demand.MulticastRequest;
import embedding.MulticastVDCE;
import general.Constant;
import network.Layer;

public class VDCETest_MC_Dynamic {
	public static void main(String[] args) {
		double[] erlang={0.2,0.4,0.6,0.8,1.0};
		
		long begin=System.currentTimeMillis();
		for(int i=0;i<erlang.length;i++){
			double er=erlang[i];
			System.out.println("------------erlang load = "+er+"------------------");
			Layer phyLayer=new Layer("PHYLayer", 0, null);
			phyLayer.readTopology("E:\\读论文\\DCN\\Topology\\P40T4A4.csv");
			
			Random leafNum=new Random(1);
			Random cpu=new Random(2);
			Random memory=new Random(3);
			Random disk=new Random(4);
			Random demand=new Random(5);
			Random arrT=new Random(6);
			double arriveTime=-1.0/er*(Math.log(arrT.nextDouble()));
			MulticastRequest mcRequest=new MulticastRequest("request0", 0, null, Demand.generateTrafficDemand(demand), Constant.ARRIVAL, arriveTime, arriveTime-Math.log(arrT.nextDouble()));
			mcRequest.generateVM(leafNum, cpu, memory, disk);
			ArrayList<MulticastRequest> requestList=new ArrayList<MulticastRequest>();
			requestList.add(mcRequest);
			
			
			
			
			
			
			
			
			
			int arrivalNUM=0;
			int acceptNUM=0;
			int acceptVMNum=0;
			while((arrivalNUM<Constant.SIM_WAN*100)&&(!requestList.isEmpty())){
				MulticastRequest currentRequest=requestList.get(0);
				if(currentRequest.getReqType()==Constant.ARRIVAL){
//					System.out.println("到达资源");
					arrivalNUM++;
					if(arrivalNUM%(Constant.SIM_WAN*10)==0){
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
					MulticastRequest departRequest=new MulticastRequest(currentRequest.getName(),currentRequest.getIndex(), null, currentRequest.getTrafficDemand(), Constant.DEPARTURE,
							currentRequest.getArriveTime(), currentRequest.getDepartTime());
					departRequest.copyRequest(currentRequest);
					insertRequest(requestList, departRequest);
					
					
					MulticastVDCE mcVDCE=new MulticastVDCE();
					if(mcVDCE.vmEmbed(currentRequest, phyLayer)){
					
						acceptVMNum++;
						/*Iterator<String> ir=currentRequest.getNodelist().keySet().iterator();
						while(ir.hasNext()){
							Node vm=(Node)(currentRequest.getNodelist().get(ir.next()));
							System.out.println("VM:"+vm.getName()+"\t----PM:"+vm.getPM().getName());
						}*/
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
					
						
						if(mcVDCE.vlEmbed(currentRequest, phyLayer)){
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
					
					MulticastRequest newRequest=new MulticastRequest("request"+arrivalNUM, arrivalNUM, null, Demand.generateTrafficDemand(demand), Constant.ARRIVAL,
							newArrivalTime, newDepartTime);
					newRequest.generateVM(leafNum, cpu, memory, disk);
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
	
	
	
	
	
	
	
	
	
	
	public static void insertRequest(ArrayList<MulticastRequest> requestList, MulticastRequest request){
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
				MulticastRequest currentRequest=requestList.get(i);
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
