package com.aozoradev.saaf;

import java.util.ArrayList;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

public class ReadOsw {
  public static void load (String fileName, ArrayList<String> listItems) throws IOException {
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    ZipInputStream zis = null;
    
    try {
      fis = new FileInputStream(fileName);
      bis = new BufferedInputStream(fis);
      zis = new ZipInputStream(bis); 
      ZipEntry ze;
      int index = 0;
      while ((ze = zis.getNextEntry()) != null) {
          index = ++index;
          listItems.add(ze.getName() + index);
      }
    } finally {
      if (zis != null) {
        zis.close();
      }
      if (bis != null) {
        bis.close();
      }
      if (fis != null) {
        fis.close();
      }
    }
  }
}