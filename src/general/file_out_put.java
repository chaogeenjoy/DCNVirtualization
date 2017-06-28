package general;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class file_out_put {

	public void filewrite(String write_name, String string) throws IOException {
		// TODO Auto-generated method stub
	 File f=new File(write_name);
	 FileWriter out=new FileWriter(f,true);
	 out.write(string);
	 out.close();
	}

}
