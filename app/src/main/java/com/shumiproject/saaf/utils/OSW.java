package com.shumiproject.saaf.utils;

import java.io.IOException;
import java.io.FileOutputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OSW {
    private static class IDX {
        int compressedSize;
        int length;
        String name;
    }

    public static void createIDX (String path) throws IOException {
        ZipFile zipFile = new ZipFile(path);
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
}