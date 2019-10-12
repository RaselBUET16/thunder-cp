package com.sharif.thunder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class DataSource {
  protected final HashMap<String, String[]> data = new HashMap<>();
  // final protected ArrayList<String[]> data = new ArrayList<>();
  protected String filename = "discordbot.null";
  protected int size;
  protected Function<String[], String> generateKey;

  ExecutorService filewriting = Executors.newSingleThreadExecutor();
  boolean writeScheduled = false;

  protected DataSource() {}

  public int getSize() {
    return size;
  }

  public String[] get(String key) {
    synchronized (data) {
      return data.get(key) == null ? null : data.get(key).clone();
    }
  }

  public void set(String[] item) {
    synchronized (data) {
      data.put(generateKey.apply(item), item);
      setToWrite();
    }
  }

  public String[] remove(String key) {
    synchronized (data) {
      String[] ret = data.remove(key);
      if (ret != null) setToWrite();
      return ret;
    }
  }

  public void setToWrite() {
    if (!writeScheduled) {
      writeScheduled = true;
      filewriting.submit(
          new Thread() {
            @Override
            public void run() {
              try {
                Thread.sleep(30000);
              } catch (InterruptedException ex) {
              }
              write();
              writeScheduled = false;
            }
          });
    }
  }

  public boolean read() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(filename));
    } catch (FileNotFoundException e) {
      System.err.println("WARNING - " + filename + " not found : " + e.toString());
    }
    ArrayList<String[]> newData = new ArrayList<>();
    if (reader != null) {
      try {
        String str;
        do {
          str = reader.readLine();
          if (str != null && !str.trim().isEmpty()) {
            String[] stra = Arrays.copyOf(str.split((char) 31 + ""), size);
            for (int i = 0; i < stra.length; i++)
              stra[i] = stra[i] == null ? "" : stra[i].replaceAll((char) 30 + "", "\n");
            newData.add(stra);
          }
        } while (str != null);
        reader.close();
        synchronized (data) {
          data.clear();
          newData.stream()
              .forEach(
                  (item) -> {
                    data.put(generateKey.apply(item), item);
                  });
        }
        return true;
      } catch (IOException e) {
        System.err.println("ERROR - Could not read from " + filename + " : " + e.toString());
      }
    }
    return false;
  }

  private boolean write() {
    HashMap<String, String[]> copy;
    synchronized (data) {
      copy = new HashMap<>(data);
    }
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
      for (String[] s : copy.values()) {
        String str = s[0];
        for (int i = 1; i < s.length; i++) {
          str += (char) 31;
          if (s[i] != null)
            str +=
                s[i].replace((char) 30 + "", "")
                    .replace((char) 31 + "", "")
                    .replace("\n", (char) 30 + "");
        }
        writer.write(str);
        writer.newLine();
      }
      writer.flush();
    } catch (IOException e) {
      System.err.println("Error writing to " + filename + " : " + e.toString());
      return false;
    }

    try {
      Files.copy(
          new File(filename).toPath(),
          new File("DataCopies" + File.separatorChar + filename).toPath(),
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      System.err.println("Error making backup of " + filename + " : " + e.toString());
    }

    return true;
  }

  public void shutdown() {
    filewriting.shutdown();
  }
}
