package generateTopology;

import java.io.IOException;

public class GenerateTP {

	public static void main(String[] args) throws IOException {
		TopologyGenerator tg=new TopologyGenerator();
		tg.generateVL2Topology(40, 4, 4, 4);
	}
}
