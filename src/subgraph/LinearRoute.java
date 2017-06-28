package subgraph;

import java.io.IOException;
import java.util.ArrayList;

 
import general.file_out_put;
import network.Link;
import network.Node;


public class LinearRoute extends Subgraph {
    
  	
	public LinearRoute(String name, int index, String comments) {
		super(name, index, comments);
		// TODO Auto-generated constructor stub
	       
	}
	
	public int Equal_link(LinearRoute newroute,Link link){
		     ArrayList<Link>linklist=newroute.getLinklist();
		     for(Link link1:linklist){
		    	 if(link.getName().endsWith(link1.getName()))return 1;
		     }
		     return 0;
	}
	public void OutputRoute_link(LinearRoute newroute){
		     ArrayList<Link>linklist=newroute.getLinklist();
		     for(Link link:linklist){
		    	 if(link.getNodeA().getIndex()<link.getNodeB().getIndex()){
		    	 System.out.println(link.getNodeA().getName()+"---"+link.getNodeB().getName());
		    	 }
		     }
	}
	public void OutputRoute_node(LinearRoute newroute,String write_name) throws IOException{
		file_out_put file=new file_out_put();
		if(newroute.getNodelist().size()==0){
			System.out.println("no path to the desnode");
			file.filewrite(write_name,"no path to the desnode");
		}
		else{ 
		      for(Node node:newroute.getNodelist()){		    	       
			           System.out.println(node.getName());			        
			           file.filewrite(write_name,node.getName());			          			           
		      }		     
		}		
	}
	
	public void OutputRoute_node(LinearRoute newroute){
		if(newroute.getNodelist().size()==0){
			System.out.println("no path to the desnode");
		}
		else{			  
		      for(Node node:newroute.getNodelist()){		    	   
		    	       System.out.print(node.getName()+"-");
		      }	      
		      System.out.println();
		}
	}

}

	
 
