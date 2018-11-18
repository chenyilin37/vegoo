package vegoo.stockdata.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vegoo.stockcommon.utils.BaseJob;


public abstract class ExportDataJob extends BaseJob {
	private static final Logger logger = LoggerFactory.getLogger(ExportTdxDataJob.class);

	
	
	public static interface FileHandler{
		void execute(FileWriter fw)throws IOException;
	}
	
	public static void export2File(String path, String fileName, FileHandler hander)throws IOException {
		boolean successed = false;
		
		File file = getFile("."+fileName, path);
		if(file.exists()) {
			file.delete();
		}
		
		FileWriter fw = new FileWriter(file);
		try {
			hander.execute(fw);
			successed = true;
		} catch (IOException e) {
			logger.error("", e);
			throw e;
		} finally {
			fw.close();
		}
		
		if(successed) {
			File fileOk = getFile(fileName, path);
			if(fileOk.exists()) {
			  fileOk.delete();
			}
			file.renameTo(fileOk);
		}
	}
		
	public static File getFile(String filename, String relativePath) {
		String userhome = System.getProperty("user.home");
		
		File path = new File(userhome, relativePath);
		
		if(!path.exists()) {
			path.mkdirs();
		}
		
		String fileNameExt = String.format("%s.txt", filename);  // %s-%s.txt  dateTag, dataType
		
		return new File(path, fileNameExt);
	}
	
	
	private static void writeFile(List<String> items, File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		try {
			for (String s : items) {
				fw.write(s+"\n");
			}
		}finally {
		    fw.close();
		}
	}
	

}
