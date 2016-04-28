package com.wangxingyu.diskmap.entity;

public class FileSystemFile extends FileSystemEntry {
  private FileSystemFile(FileSystemEntry parent, String name) {
    super(parent, name);
  }

  public static FileSystemEntry makeNode(
      FileSystemEntry parent, String name) {
    return new FileSystemFile(parent, name);
  }

  @Override
  public FileSystemEntry create() {
    return new FileSystemFile(null, this.name);
  }
}
