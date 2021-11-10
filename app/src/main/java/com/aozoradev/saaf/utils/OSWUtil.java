package com.aozoradev.saaf.utils;

import com.aozoradev.saaf.variables.Constant;
import com.aozoradev.saaf.variables.Static;
import com.aozoradev.saaf.Radio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.os.Environment;
import android.content.Context;
import android.widget.Toast;

public class OSWUtil {
  private static class idx {
    int zeCompressedSize;
    int zeLength;
    String zeName;
    private idx() { /* Empty lol */ }
  }
  
  public static void extract (Context context, Radio radio) throws IOException {
    File file = new File(Environment.getExternalStorageDirectory().getPath() + "/SaaFAndroid/" + Static.station);
    if (!file.exists()) {
      file.mkdirs();
    }
    String fn = radio.getFileName();
    
    try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + "/" + fn);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    InputStream is = Static.zipFile.getInputStream(fn)) {
      byte[] bytesIn = new byte[Constant.BUFFER_SIZE];
      int read = 0;
      while ((read = is.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
      }
      Toast.makeText(context, fn + " has been extracted to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }
  }
  
  public static void createIDX(String path) throws IOException {
    ZipFile zipFile = new ZipFile(path);
    ArrayList<Object> arrayList = new ArrayList<Object>();
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    
    long zeLength = 0;
    while (entries.hasMoreElements()) {
      long zes;
      ZipEntry zipEntry = (ZipEntry) entries.nextElement();
      zeLength += (long) (zipEntry.getName().length() + 30);
      if (zipEntry.isDirectory()) {
        zes = 0;
      } else {
        zes = zipEntry.getCompressedSize();
        idx idxVar = new idx();
        idxVar.zeName = zipEntry.getName();
        idxVar.zeCompressedSize = (int) zes;
        idxVar.zeLength = (int) zeLength;
        arrayList.add(idxVar);
      }
      zeLength += zes;
    }
    
    FileOutputStream fileOutputStream = new FileOutputStream(path + ".idx");
  
    ByteBuffer allocate = ByteBuffer.allocate(4);
    ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    allocate = allocate.order(byteOrder);
    allocate.putInt(arrayList.size());
    fileOutputStream.write(allocate.array());
    allocate = ByteBuffer.allocate(10).order(byteOrder);
    Iterator<Object> it = arrayList.iterator();
    
    while (it.hasNext()) {
      idx _idxVar = (idx) it.next();
      allocate.rewind();
      allocate.putInt(_idxVar.zeLength);
      allocate.putInt(_idxVar.zeCompressedSize);
      allocate.putShort((short) _idxVar.zeName.length());
      
      fileOutputStream.write(allocate.array());
      byte[] obj = new byte[_idxVar.zeName.length()];
      byte[] bytes = _idxVar.zeName.getBytes("ISO-8859-1");
      System.arraycopy(bytes, 0, obj, 0, bytes.length);
      fileOutputStream.write(obj);
    }
    fileOutputStream.close();
    zipFile.close();
  }
}