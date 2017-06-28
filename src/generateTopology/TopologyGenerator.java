package generateTopology;

import java.io.IOException;

import general.file_out_put;

public class TopologyGenerator {
	//条件：ASNum必须小于或等于 ToRSNum
	public void generateVL2Topology(int serverNum, int ToRSNum, int ASNum, int intSNum) throws IOException{
		file_out_put file=new file_out_put(); 
		String filename="E:\\读论文\\DCN\\Topology\\P"+serverNum+"T"+ToRSNum+"A"+ASNum+".csv";
		file.filewrite(filename, "Node,Attribute,,,,\r\n");
		
		//PM
		for(int i=0;i<serverNum;i++){
			file.filewrite(filename, "N"+i+","+"PM,,,,\r\n");
		}
		 
		//ToR switch		
		for(int j=serverNum;j<serverNum+ToRSNum;j++){
			file.filewrite(filename, "N"+j+","+"ToR,,,,\r\n");
		}
		
		//AS
		int startAS=serverNum+ToRSNum;
		for(int k=startAS;k<startAS+ASNum;k++){
			file.filewrite(filename, "N"+k+","+"AS,,,,\r\n");
		}
		
		//Int switch
		int startInt=startAS+ASNum;
		for(int l=startInt;l<startInt+intSNum;l++){
			file.filewrite(filename, "N"+l+","+"Int,,,,\r\n");
		}
		
		/*
		 * Link
		 */
		
		//tire 1
		file.filewrite(filename, "Link,NodeA,NodeB,Length,Cost,tire\r\n");
		int d=serverNum/ToRSNum;//每个ToR链接多少个PM
		for(int i=serverNum;i<serverNum+ToRSNum;i++){
			
			for(int j=0+d*(i-serverNum);j<d+d*(i-serverNum);j++){
				file.filewrite(filename, "N"+j+"-"+"N"+i+","  +  "N"+j+","+"N"+i+","  +"2,2,1\r\n");
			}
		}
		
		
		//tire 2
		for(int i=0;i<2*ToRSNum;){
			int j=0;
			for(;(j<ASNum)&&(i<2*ToRSNum);i++,j++){
				int Ti=i/2+serverNum;
				int Aj=j+serverNum+ToRSNum;
				file.filewrite(filename, "N"+Ti+"-"+"N"+Aj+","  +  "N"+Ti+","+"N"+Aj+","  +"1,1,2\r\n");
			}
			for(j--;(j>=0)&&(i<ToRSNum);i++,j--){
				int Ti=i/2+serverNum;
				int Aj=j+serverNum+ToRSNum;
				file.filewrite(filename, "N"+Ti+"-"+"N"+Aj+","  +  "N"+Ti+","+"N"+Aj+","  +"1,1,2\r\n");
			}
			
		}
		
	/*	int i=0;
		while(i<ToRSNum){
			int j=0;
			for(;(j<ASNum)&&(i<ToRSNum);i++,j++){
				int Ti=i+serverNum;
				int Aj=j+serverNum+ToRSNum;
				file.filewrite(filename, "N"+Ti+"-"+"N"+Aj+","  +  "N"+Ti+","+"N"+Aj+","  +"1,1,2\r\n");
			}
			
			for(j--;(j>=0)&&(i<ToRSNum);i++,j--){
				int Ti=i+serverNum;
				int Aj=j+serverNum+ToRSNum;
				file.filewrite(filename, "N"+Ti+"-"+"N"+Aj+","  +  "N"+Ti+","+"N"+Aj+","  +"1,1,2\r\n");
			}
		}
		
		int k=ToRSNum-1;
		while(k>=0){
			int j=0;
			for(;(j<ASNum)&&(k>=0);k--,j++){
				int Tk=k+serverNum;
				int Aj=j+serverNum+ToRSNum;
				file.filewrite(filename, "N"+Tk+"-"+"N"+Aj+","  +  "N"+Tk+","+"N"+Aj+","  +"1,1,2\r\n");
			}
			
			for(j--;(j>=0)&&(k>=0);k--,j--){
				int Tk=k+serverNum;
				int Aj=j+serverNum+ToRSNum;
				file.filewrite(filename, "N"+Tk+"-"+"N"+Aj+","  +  "N"+Tk+","+"N"+Aj+","  +"1,1,2\r\n");
			}
		}
		*/
		
		//tier 3
		for(int m=0;m<intSNum;m++){
			for(int l=0;l<ASNum;l++){
				int Im=m+serverNum+ToRSNum+ASNum;
				int Al=l+serverNum+ToRSNum;
				file.filewrite(filename, "N"+Al+"-"+"N"+Im+","  +  "N"+Al+","+"N"+Im+","  +"1,1,3\r\n");
			}
		}
		
	}

}
