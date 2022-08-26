package com.shumiproject.saaf.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;

import android.os.Environment;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

public class OSW {
    private static class IDX {
        int compressedSize;
        int length;
        String name;
    }
    
    public static void replace (String path, String filename) throws IOException {
        File audio = new File(path);
        
        try (ZipFile osw = new ZipFile(RadioList.stationPath)) {
            osw.addFile(audio, parameter(filename));
            
            // Update ZipResourceFile
            RadioList.osw = new ZipResourceFile(RadioList.stationPath);
        }
    }
    
    public static void extract (RadioList radioList) throws IOException {
        String externalPath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(externalPath + "/SaaFAndroid/" + RadioList.stationCode);
        String filename = radioList.getFilename();
        if (!file.exists()) {
            file.mkdirs();
        }
        
        try (ZipFile osw = new ZipFile(RadioList.stationPath)) {
            osw.extractFile(filename, file.getAbsolutePath());
        }
    }
    
    // By LSDsl
    public static void createIDX (String path) throws IOException {
        java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(path);
        ArrayList<Object> arrayList = new ArrayList<Object>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
    
        long length = 0;
        while (entries.hasMoreElements()) {
            long compressedSize;
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            length += (long) (zipEntry.getName().length() + 30);
            if (zipEntry.isDirectory()) {
                compressedSize = 0;
            } else {
                compressedSize = zipEntry.getCompressedSize();
                IDX idx = new IDX();
                idx.name = zipEntry.getName();
                idx.compressedSize = (int) compressedSize;
                idx.length = (int) length;
                arrayList.add(idx);
            }
            length += compressedSize;
        }
    
        FileOutputStream fileOutputStream = new FileOutputStream(path + ".idx");
  
        ByteBuffer allocate = ByteBuffer.allocate(4);
        ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
        allocate = allocate.order(byteOrder);
        allocate.putInt(arrayList.size());
        fileOutputStream.write(allocate.array());
        allocate = ByteBuffer.allocate(10).order(byteOrder);
        Iterator<Object> iterator = arrayList.iterator();
    
        while (iterator.hasNext()) {
            IDX idx = (IDX) iterator.next();
            allocate.rewind();
            allocate.putInt(idx.length);
            allocate.putInt(idx.compressedSize);
            allocate.putShort((short) idx.name.length());
            
            fileOutputStream.write(allocate.array());
            byte[] obj = new byte[idx.name.length()];
            byte[] bytes = idx.name.getBytes("ISO-8859-1");
            System.arraycopy(bytes, 0, obj, 0, bytes.length);
            fileOutputStream.write(obj);
        }
        
        fileOutputStream.close();
        zipFile.close();
    }
    
    public static ZipParameters parameter (String filename) {
        ZipParameters parameter = new ZipParameters();
        parameter.setCompressionLevel(CompressionLevel.NO_COMPRESSION);
        parameter.setCompressionMethod(CompressionMethod.STORE);
        parameter.setFileNameInZip(filename);
        parameter.setUnixMode(true);
        
        return parameter;
    }
}