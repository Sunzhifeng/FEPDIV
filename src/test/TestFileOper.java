package test;

import java.io.IOException;
import java.util.Properties;

import file.FileOperation;
import tool.PropertiesUtil;

public class TestFileOper {

	public static void genRandomFile() throws NumberFormatException, IOException {
		Properties pro = PropertiesUtil.loadProperties();
		String filePath = pro.getProperty("filePath");
		String fileSize = pro.getProperty("fileSize");
		FileOperation.genRandomFile(filePath, Integer.valueOf(fileSize));
	}

	public static void handleBlocks(int[] errorBlockNums, boolean handleType) throws IOException {
		Properties pro = PropertiesUtil.loadProperties();
		String filePath = pro.getProperty("filePath");
		int blockSize = Integer.valueOf(pro.getProperty("blockSize"));
		if (handleType)
			FileOperation.destoryBlocks(filePath, blockSize, errorBlockNums);
		else
			FileOperation.repairBlocks(filePath, blockSize, errorBlockNums);
	}

	public static void main(String[] args) throws Exception {
		genRandomFile();
	}

}