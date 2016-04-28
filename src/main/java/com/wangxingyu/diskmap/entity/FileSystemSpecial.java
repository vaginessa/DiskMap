/**
 * DiskUsage - displays sdcard usage on android.
 * Copyright (C) 2016 wangxingyu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.wangxingyu.diskmap.entity;


import com.wangxingyu.diskmap.AppFilter;

public class FileSystemSpecial extends FileSystemEntry {
  public AppFilter filter;

  public FileSystemSpecial(String name, long size, int blockSize) {
    super(null, name);
    initSizeInBytes(size, blockSize);
  }
  
  public FileSystemSpecial(String name, FileSystemEntry[] children, int blockSize) {
    super(null, name);
    this.setChildren(children, blockSize);
  }

  @Override
  public FileSystemEntry filter(CharSequence pattern, int blockSize) {
    return filterChildren(pattern, blockSize);
  }
  
  @Override
  public FileSystemEntry create() {
    // dummy values for size
    FileSystemSpecial copy = new FileSystemSpecial(this.name, 0, 512);
    copy.filter = filter;
    return copy;
  }
}
