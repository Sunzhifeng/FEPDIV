package test;

import file.FileOperation;

public class Test {	
	public static final String PUBLIC="D:/fileVerify/public.txt";
	public static final String Path="D:/test/test-2.txt";
	public static void main(String[] args) throws Exception {
        int []damagedBlocks={1,20,30,40,50};
        FileOperation.genRandomFile(Path, 2);
		FileOperation.destoryBlocks(Path, 4, damagedBlocks);
		//FileOperation.repairBlocks(Path, 4, damagedBlocks);

	}
    
	
	
}