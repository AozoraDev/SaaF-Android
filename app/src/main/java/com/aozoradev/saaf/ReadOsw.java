package com.aozoradev.saaf;

import java.util.ArrayList;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

public class ReadOsw {
  public static void load (String fileName, ArrayList<String> listItems) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipInputStream zis = new ZipInputStream(bis)) { 
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                listItems.add(ze.getName());
            }
        }
    }
}