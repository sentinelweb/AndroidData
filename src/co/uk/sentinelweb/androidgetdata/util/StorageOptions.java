package co.uk.sentinelweb.androidgetdata.util;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import co.uk.sentinelweb.androidgetdata.Globals;


/**
 * SOURCE:
 * http://sapienmobile.com/?p=204
 *
 * @author robert
 */
public class StorageOptions {
    private static final String EXTERNALSTORAGE_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static ArrayList<String> mMounts = new ArrayList<String>();
    private static ArrayList<String> mVold = new ArrayList<String>();

    //public static String[] labels;
    //public static String[] paths;
    //public static int count = 0;
    public static ArrayList<Volume> _volumes;

    public static class Volume {
        public boolean main = false;
        private File path;
        private File writePath;
        public String label;
        public long availableMB = -1;
        public long freeMB = -1;
        public long mpusedBytes = -1;
        public long mpAllocMb = -1;
        private StatFs _sf;

        public File getReadPath() {
            return path;
        }

        public File getWritePath() {
            if (writePath != null) {
                return writePath;
            } else {
                return path;
            }
        }

        public Volume(String read, String write) {
            path = new File(read);
            if (write != null) {
                writePath = new File(write);
            }
            label = getLabel(path.getAbsolutePath(), _volumes.size());
            updateSpace();
        }

        public void updateSpace() {
            try {
                if (_sf == null) {
                    _sf = new StatFs(path.getAbsolutePath());
                } else {
                    _sf.restat(path.getAbsolutePath());
                }
                if (Build.VERSION.SDK_INT < 18) {
                    freeMB = ((long) _sf.getAvailableBlocks() * (long) _sf.getBlockSize()) / 1024l / 1024l;
                    availableMB = ((long) _sf.getBlockCount() * (long) _sf.getBlockSize()) / 1024l / 1024l;
                } else {
                    freeMB = (_sf.getAvailableBlocksLong() * _sf.getBlockSizeLong()) / 1024l / 1024l;
                    availableMB = (_sf.getBlockCountLong() * _sf.getBlockSizeLong()) / 1024l / 1024l;
                }
//				freeMB = (_sf.getAvailableBlocksLong()*_sf.getBlockSizeLong())/1024/1024;
//				availableMB = (_sf.getBlockCountLong()*_sf.getBlockSizeLong())/1024/1024;
            } catch (IllegalArgumentException e) {
                Log.d("StorageOptions", "Invalid path for storage:" + path.getAbsolutePath());
            }
        }
    }

    public static String[] getReadPaths(Context c) {
        if (_volumes.size() == 0) {
            determineStorageOptions(c);
        }
        String[] paths = new String[_volumes.size()];
        for (int i = 0; i < _volumes.size(); i++) {
            paths[i] = _volumes.get(i).getReadPath().getAbsolutePath();
        }
        return paths;
    }

    public static String[] getWritePaths(Context c) {
        if (_volumes.size() == 0) {
            determineStorageOptions(c);
        }
        String[] paths = new String[_volumes.size()];
        for (int i = 0; i < _volumes.size(); i++) {
            paths[i] = _volumes.get(i).getWritePath().getAbsolutePath();
        }
        return paths;
    }

    public static void determineStorageOptions(Context c) {
        long str = SystemClock.uptimeMillis();
        if (_volumes == null) {
            _volumes = new ArrayList<StorageOptions.Volume>();
        }
        _volumes.clear();
        if (Build.VERSION.SDK_INT < 19) {
            readMountsFile();
            readVoldFile();
            compareMountsWithVold();
            testAndCleanMountsList();
            setProperties(c);
        } else {
            readExternalFilesDirs(c);
        }
        Log.d(Globals.LOG_TAG, "determineStorageOptions: time:" + (SystemClock.uptimeMillis() - str) + "ms vols:" + (_volumes != null ? Integer.toString(_volumes.size()) : "null"));
    }

    private static void readExternalFilesDirs(Context c) {
        final List<File> paths;

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
            paths = Arrays.asList(c.getExternalFilesDirs(null));
        } else {
            paths = new ArrayList<File>();
            paths.add(c.getExternalFilesDir(null));
        }
        for (int i = 0; i < paths.size(); i++) {
            File path = paths.get(i);
            if (path != null) {
                Log.d(Globals.LOG_TAG, "StorageOptions.readExternalFilesDirs: idx=" + i + " path=" + path.getAbsolutePath() + " r:" + path.canRead() + " w:" + path.canWrite() + " wt:" + testWrite(path));
                File extDir = path;
                File testDir = extDir;
                while (!"Android".equals(testDir.getName())) {
                    testDir = testDir.getParentFile();
                }
                testDir = testDir.getParentFile();// will be root of the volume
                Volume v = null;
                Log.d(Globals.LOG_TAG, "StorageOptions.testRoot: idx=" + i + " path=" + testDir + " r:" + testDir.canRead() + " w:" + testDir.canWrite() + " wt:" + testWrite(testDir));

                if (testDir.canRead() && testWrite(testDir)) {
                    v = new Volume(testDir.getAbsolutePath(), null);
                } else if (testDir.canRead() && !testWrite(testDir)) {
                    v = new Volume(testDir.getAbsolutePath(), path.getAbsolutePath());
                } else {
                    v = new Volume(path.getAbsolutePath(), null);
                }
                v.main = i == 0;
                addVolume(c, v);
            }

        }
    }

    private static boolean testWrite(File testDir) {
        //return testDir.canWrite();
        try {
            String testDirName = Long.toString(System.currentTimeMillis());
            File testWrite = new File(testDir, testDirName);
            testWrite.mkdir();
            if (testWrite.exists()) {
                testWrite.delete();
                return true;
            }
        } catch (Throwable e) {

        }
        return false;
    }

    private static void readMountsFile() {
    /*
     * Scan the /proc/mounts file and look for lines like this:
     * /dev/block/vold/179:1 /mnt/sdcard vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000,gid=1015,fmask=0602,dmask=0602,allow_utime=0020,codepage=cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
     * 
     * When one is found, split it into its elements
     * and then pull out the path to the that mount point
     * and add it to the arraylist
     */

        // some mount files don't list the default
        // path first, so we add it here to
        // ensure that it is first in our list
        mMounts.add(EXTERNALSTORAGE_SDCARD);

        try {
            Scanner scanner = new Scanner(new File("/proc/mounts"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("/dev/block/vold/")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[1];

                    // don't add the default mount path
                    // it's already in the list.
                    if (!element.equals(EXTERNALSTORAGE_SDCARD)) {
                        mMounts.add(element);
                    }
                }
            }
        } catch (Exception e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void readVoldFile() {
    /*
     * Scan the /system/etc/vold.fstab file and look for lines like this:
     * dev_mount sdcard /mnt/sdcard 1 /devices/platform/s3c-sdhci.0/mmc_host/mmc0
     * 
     * When one is found, split it into its elements
     * and then pull out the path to the that mount point
     * and add it to the arraylist
     */

        // some devices are missing the vold file entirely
        // so we add a path here to make sure the list always
        // includes the path to the first sdcard, whether real
        // or emulated.
        mVold.add(EXTERNALSTORAGE_SDCARD);

        try {
            Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("dev_mount")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[2];

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }

                    // don't add the default vold path
                    // it's already in the list.
                    if (!element.equals(EXTERNALSTORAGE_SDCARD)) {
                        mVold.add(element);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private static void compareMountsWithVold() {
    /*
     * Sometimes the two lists of mount points will be different.
     * We only want those mount points that are in both list.
     * 
     * Compare the two lists together and remove items that are not in both lists.
     */

        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);
            if (!mVold.contains(mount)) {
                mMounts.remove(i--);
            }
        }

        // don't need this anymore, clear the vold list to reduce memory
        // use and to prepare it for the next time it's needed.
        mVold.clear();
    }

    private static void testAndCleanMountsList() {
    /*
     * Now that we have a cleaned list of mount paths
     * Test each one to make sure it's a valid and
     * available path. If it is not, remove it from
     * the list. 
     */

        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);
            File root = new File(mount);
            if (!root.exists() || !root.isDirectory() || !testWrite(root)) {
                mMounts.remove(i--);
            }
        }
    }

    private static void setProperties(Context c) {

        _volumes.clear();
        for (int i = 0; i < mMounts.size(); i++) {
            Volume v = new Volume(mMounts.get(i), mMounts.get(i));
            if (i == 0) {
                v.main = true;
            }
            addVolume(c, v);
        }
        mMounts.clear();
    }

    private static void addVolume(Context c, Volume v) {

        _volumes.add(v);
    }

    private static String getLabel(String path, int i) {
        if (i == 0) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return "Auto";
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                if (Environment.isExternalStorageRemovable()) {
                    return "External SD Card 1";
                    // j = 1;
                } else {
                    return "Internal Storage";
                }
            } else {
                if (!Environment.isExternalStorageRemovable() || Environment.isExternalStorageEmulated()) {
                    return "Internal Storage";
                } else {
                    return "External SD Card 1";
                    // j = 1;
                }
            }
        }
        return "External SD Card " + (i + 1);
    }
}
