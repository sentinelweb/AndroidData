package co.uk.sentinelweb.androidgetdata.util;

import java.io.File;

import android.os.Environment;

public class DiskUtil {
	
	//private static StatFs _sf = null;
	private static long MB = 1024*1024;
//	public static StatFs getSf(File f) {
//		if (f.exists()) {
//			if (_sf==null) {
//				try {
//					_sf=new StatFs(f.getAbsolutePath());
//				} catch (Exception e) {
//					
//				}
//			}
//			return _sf;
//		}
//		return null;
//	}
	



	
	public static File getDefaultDir() {
		//return android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+Globals.APP_DIR_NAME;
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
	}
	
	public static Boolean sdWriteState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		   return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    return false;
		} else {
		    return null;
		}
	}
	
	public static boolean sdMounted() {
		String sdcardState = android.os.Environment.getExternalStorageState(); 
		if (sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED)){ 
		    return true;
        } 
        return false; 
	}
	    	 
//	public static Long getFreeSpace(File appDir) {// in MegaBytes
//		if (!sdMounted()) {return null;}
//		try {
//			getSf(appDir);
//			_sf.restat(appDir.getAbsolutePath());
//			long freeSpc = _sf.getAvailableBlocks()/1024*_sf.getBlockSize()/1024;
//			return freeSpc;
//		} catch (Exception e) {
//			return null;
//		}
//	}
	
	public static String getSizeStr(long spaceLongMb) {
		String spaceStr = spaceLongMb+"Mb";
		if (spaceLongMb>1024) {
			spaceStr = spaceLongMb/1024+"Gb";
		}
		return spaceStr;
	}
	

}
