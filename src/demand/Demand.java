package demand;

import java.util.Random;

/*
 * ����ÿ��VM��cpu, 
 */
public class Demand {
	public static int CPUDemand(Random cpu) {
		return (int) (cpu.nextInt(8));// 0~7֮���CPU
	}

	public static double MemoryDemand(Random memory) {
		return memory.nextDouble() * 21 + 10;// 0~64GB֮��
	}

	public static double DiskDemand(Random disk) {
		return disk.nextDouble() * 100;// 0~100GB֮��
	}

	public static double generateTrafficDemand(Random demand) {

		return demand.nextDouble()*30+2;// 2~32Mbps
	}

	public static int VMNumDeman(Random vmNum) {
		return vmNum.nextInt(15) + 7;// 4��16
	}
}
