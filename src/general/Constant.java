package general;

public class Constant {
	public final static int UNVISITED=0;
	public final static int VISITEDONCE=1;
	public final static int VISITEDTWICE=2;
	public final static int UNORDER=0;          //节点对还未按顺序排列
	public final static int ORDERED=1;          //节点对已经按顺序排列
	
	public final static int CPU_PER_PM=8;//CPU数目
	public final static double DISK_PER_PM=500;//500G硬盘
	public final static double MEMORY_PER_PM=64;//64GB的memory
	public final static double WC=10.0;//CPU权重
	public final static double WM=0.1;//Memory
	public final static double WD=0.01;//Disk
	public final static char ARRIVAL=0;
	public final static char DEPARTURE=1;
	public final static int SERVER=0;
	public final static int SWITCH=1;
	
	public static final double BANDWIDTH_TIER1 = 1024;//1Gb/s
	public static final double BANDWIDTH_TIER2 = 1024*10;//10Gb/s
	public static final double BANDWIDTH_TIER3 = 1024*10;//10Gb/s
	
	public static final int SIM_WAN = 10000;
}

