package com.ph7.foodscan.services;

import android.content.Context;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.ph7.foodscan.utils.Validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadavg on 01/08/2016.
 */
public class ScanStorageService {

    private final static String SCAN_DIR = "scans";
    private File baseDir;

    private List<String> fileNames = new ArrayList<>();
    private List<ScioReading> scioReadings = new ArrayList<>();

    public void deleteScan(int position) {
        new File(baseDir, fileNames.get(position)).delete();
        scioReadings.remove(position);
        fileNames.remove(position);
    }

    public String  initScansStorage(final Context context) {
        String scanDir = Validation.generateRandomString(10);
        baseDir = new File(context.getCacheDir() + File.separator + scanDir);
        baseDir.mkdirs();
        return baseDir.getPath() ;
    }

    public void deleteScanStorage(String scanDirPath)
    {
        File scanDir = new File(scanDirPath);
        if (scanDir.isDirectory()) {
            String[] children = scanDir.list();
            for (int i = 0; i < children.length; i++) {
                new File(scanDir, children[i]).delete();
            }
        }
        scanDir.delete() ;
    }

    public void saveScanToStorage(final List<ScioReading> scioReadings)
    {
        for (ScioReading scioReading : scioReadings) {
            saveScanToStorage(scioReading) ;
        }
    }

    public void saveScanToStorage(final ScioReading scioReading) {
        try {
            File file = new File(baseDir, String.valueOf(System.currentTimeMillis())) ;
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(scioReading);
            os.close();
            fos.close();
            //String Name = file.getName() ;
            os.toString() ;
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
        }
    }

    public void loadScansFromStorage(String scanDirPath) {
        File scanDir =  new File(scanDirPath);
        if(scanDir.exists() && scanDir.isDirectory())
        {
            File[] filelist = scanDir.listFiles();
            String[] theNamesOfFiles = new String[filelist.length];
            scioReadings.clear();
            fileNames.clear();
            for (int i = 0; i < theNamesOfFiles.length; i++) {
                try {
                    FileInputStream fis = new FileInputStream(new File(scanDir, filelist[i].getName()));
                    ObjectInputStream is = new ObjectInputStream(fis);
                    ScioReading reading = (ScioReading) is.readObject();
                    is.close();
                    fis.close();

                    fileNames.add(filelist[i].getName());
                    scioReadings.add(reading);
                }
                catch (FileNotFoundException e) {
                }
                catch (OptionalDataException e) {
                }
                catch (StreamCorruptedException e) {
                }
                catch (IOException e) {
                }
                catch (ClassNotFoundException e) {
                }

            }
        }
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public List<ScioReading> getScioReadings() {
        return scioReadings;
    }
}
