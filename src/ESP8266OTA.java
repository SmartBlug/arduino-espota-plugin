/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

package com.esp8266.espOTA;

import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import processing.app.PreferencesData;
import processing.app.Editor;
import processing.app.Base;
import processing.app.BaseNoGui;
import processing.app.Platform;
import processing.app.Sketch;
import processing.app.tools.Tool;
import processing.app.helpers.ProcessUtils;
import processing.app.debug.TargetPlatform;

import org.apache.commons.codec.digest.DigestUtils;
import processing.app.helpers.FileUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Tools menu entry.
 */
public class ESP8266OTA implements Tool {
  Editor editor;
  String dstIP,dstPass,srcPort;

  public void init(Editor editor) {
    this.editor = editor;
  }

  public String getMenuTitle() {
    return "ESP8266 Remote OTA Upload";
  }

  private int listenOnProcess(String[] arguments){
    try {
      final Process p = ProcessUtils.exec(arguments);
      Thread thread = new Thread() {
        public void run() {
          try {
            InputStreamReader reader = new InputStreamReader(p.getInputStream());
            int c;
            while ((c = reader.read()) != -1) System.out.print((char) c);
            reader.close();
            
            reader = new InputStreamReader(p.getErrorStream());
            while ((c = reader.read()) != -1) System.err.print((char) c);
            reader.close();
            System.out.println("Done !");
          } catch (Exception e){}
        }
      };
      thread.start();
      int res = p.waitFor();
      thread.join();
      return res;
    } catch (Exception e) { return -1; }
  }

  private void sysExec(final String[] arguments){
    Thread thread = new Thread() {
      public void run() {
        try {
          if (listenOnProcess(arguments) != 0) editor.statusError("Upload failed!");
                                          else editor.statusNotice("Image Uploaded");
        } catch (Exception e) { editor.statusError("Upload failed!"); }
      }
    };
    thread.start();
  }

  private String getBuildFolderPath(Sketch s) {
    // first of all try the getBuildPath() function introduced with IDE 1.6.12
    // see commit arduino/Arduino#fd1541eb47d589f9b9ea7e558018a8cf49bb6d03
    try {
      String buildpath = s.getBuildPath().getAbsolutePath();
      return buildpath;
    }
    catch (IOException er) { editor.statusError(er); }
    catch (Exception er) {
      try {
        File buildFolder = FileUtils.createTempFolder("build", DigestUtils.md5Hex(s.getMainFilePath()) + ".tmp");
        return buildFolder.getAbsolutePath();
      }
      catch (IOException e) { editor.statusError(e); }
      catch (Exception e) {
        // Arduino 1.6.5 doesn't have FileUtils.createTempFolder
        // String buildPath = BaseNoGui.getBuildFolder().getAbsolutePath();
        java.lang.reflect.Method method;
        try {
          method = BaseNoGui.class.getMethod("getBuildFolder");
          File f = (File) method.invoke(null);
          return f.getAbsolutePath();
        } catch (SecurityException ex) { editor.statusError(ex); } 
          catch (IllegalAccessException ex) { editor.statusError(ex); } 
          catch (InvocationTargetException ex) { editor.statusError(ex); } 
          catch (NoSuchMethodException ex) { editor.statusError(ex); }
      }
    }
    return "";
  }

  private void loadParams(String path) {
    File src = new File(path);
    if (src.exists()) {
      try (Stream<String> lines = Files.lines(Paths.get(path))) {
        lines.forEach( line -> {
          String[] parts = line.split("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
          if (parts.length>2) {
            if (parts[0].equals("#define")) {
              switch (parts[1]) {
                case "OTA_IP": this.dstIP = parts[2]; break;
                case "OTA_Pass": this.dstPass = parts[2].replace("\"", ""); break;
                case "OTA_Port": this.srcPort = parts[2]; break;
              }
            }
          }
        });
      } catch (IOException e) { e.printStackTrace(); }
    }
  }

  public void run() {
    String pythonCmd;
    if(PreferencesData.get("runtime.os").contentEquals("windows")) pythonCmd = "python.exe";
                                                              else pythonCmd = "python";

    TargetPlatform platform = BaseNoGui.getTargetPlatform();
    File espota = new File(platform.getFolder()+"/tools","espota.py");
    String sketchName = editor.getSketch().getName();
    String imagePath = getBuildFolderPath(editor.getSketch()) + "/" + sketchName + ".ino.bin";
    
    this.dstIP = "";
    this.dstPass = "";
    this.srcPort = "5353";

    loadParams(editor.getSketch().getMainFilePath());

    File f = new File(imagePath);
    if(f.exists() && !f.isDirectory()) { 
      if (!this.dstIP.equals("")) {
        editor.statusNotice("Sending "+sketchName+".ino.bin to "+this.dstIP+"...");
        if (!this.dstPass.equals("")) sysExec(new String[]{pythonCmd, espota.getAbsolutePath(), "-P", this.srcPort, "-a", this.dstPass , "-i", this.dstIP, "-f", imagePath});
                                 else sysExec(new String[]{pythonCmd, espota.getAbsolutePath(), "-P", this.srcPort, "-i", this.dstIP, "-f", imagePath});
      }
      else editor.statusError("You need to specify IP in your sketch like his : #define OTA_IP 1.2.3.4");
    }
    else editor.statusError("You need to compile first");
  }
}
