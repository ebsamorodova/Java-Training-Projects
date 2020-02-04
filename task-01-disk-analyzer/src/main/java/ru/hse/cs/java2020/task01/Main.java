package ru.hse.cs.java2020.task01;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class Main {
    public static SortedSet<File> filesSet = new TreeSet<>(Comparator.comparingLong(File::length));
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        String dir_path = args[0];
        System.out.println("Path: " + dir_path);

        File dir = new File(dir_path);
        if (Files.isSymbolicLink(dir.toPath())) {
            System.out.println("Symbolic link: " + dir.getAbsolutePath() + ", size: " + dir.length());
            return;
        } else if (dir.isFile()) {
            System.out.println("File: " + dir.getAbsolutePath() + ", size: " + dir.length());
            return;
        }
        // assert(dir.isDirectory())

        long dirNameLen = getMaxDirNameLength(dir), size = getDirSize(dir), filesNumber = getFilesNumber(dir);
        System.out.println("Head folder: " + dir.getAbsolutePath() + ", " +  filesNumber + " items, size: " + size);
        ArrayList<FileInfo> fileInfos = getItemsSize(dir);
        Collections.sort(fileInfos, Collections.reverseOrder());
        int cnt = 0;
        for (FileInfo elem: fileInfos) {
            cnt++;
            System.out.printf("Item %3d: ", cnt);
            double part = elem.size / (1.0 * size) * 100;
            // assert(part <= 100)
            if (elem.file.isDirectory()) {
                String longName = String.format(String.format("%%%ds", dirNameLen), elem.file.getName());
                System.out.printf("folder %s, %9d items, %12d size, %.4f%% part\n",
                        longName, elem.itemsCnt, elem.size, part);
            } else {
                String longName = String.format(String.format("%%%ds", dirNameLen + 2), elem.file.getName());
                System.out.printf("file %s, %29d size, %.4f%% part\n", longName, elem.size, part);
            }
        }

        System.out.println("\nLargest files:");
        long fileNameLen = 0;
        for (File elem: filesSet) {
            fileNameLen = Math.max(fileNameLen, elem.getName().length());
        }

        for (File elem: filesSet) {
            String longName = String.format(String.format("%%%ds", fileNameLen), elem.getName());
            System.out.printf("%s, size: %9d, path: %s\n", longName, elem.length(), elem.getAbsolutePath());
        }

        long time = System.currentTimeMillis() - startTime;
        System.out.println("It took " + time + " milliseconds");
    }

    public static long getMaxDirNameLength(File file) {
        long maxLen = 0;
        for (File elem: file.listFiles()) {
            if (!Files.isSymbolicLink(elem.toPath()) && elem.isDirectory()) {
                maxLen = Math.max(maxLen, elem.getName().length());
            }
        }
        return maxLen;
    }

    public static long getDirSize(File dir) {
        long curSize = 0;
        for (File elem: dir.listFiles()) {
            if (!Files.isSymbolicLink(elem.toPath())) {
                curSize += elem.length();
                if (elem.isDirectory()) {
                    curSize += getDirSize(elem);
                }
            }
        }
        return curSize;
    }

    public static long getFilesNumber(File dir) {
        long curNumber = 0;
        for (File elem: dir.listFiles()) {
            if (!Files.isSymbolicLink(elem.toPath())) {
                curNumber += 1;
                if (elem.isDirectory()) {
                    curNumber += getFilesNumber(elem);
                } else if (elem.isFile()) {
                    filesSet.add(elem);
                    if (filesSet.size() > 5) {
                        filesSet.remove(filesSet.first());
                    }
                }
            }
        }
        return curNumber;
    }

    public static ArrayList<FileInfo> getItemsSize(File dir) {
        ArrayList<FileInfo> filesArray = new ArrayList<>();
        for (File elem: dir.listFiles()) {
            if (!Files.isSymbolicLink(elem.toPath())) {
                if (elem.isDirectory()) {
                    long size = getDirSize(elem), filesNumber = getFilesNumber(elem);
                    FileInfo curFile = new FileInfo(elem, filesNumber, size);
                    filesArray.add(curFile);
                } else if (elem.isFile()) {
                    FileInfo curFile = new FileInfo(elem, -1, elem.length());
                    filesArray.add(curFile);
                }
            }
        }
        return filesArray;
    }
}

class FileInfo implements Comparable<FileInfo> {
    public File file;
    public long itemsCnt, size;

    public FileInfo(File file, long itemsCnt, long size) {
        this.file = file;
        this.itemsCnt = itemsCnt;
        this.size = size;
    }

    @Override
    public int compareTo(FileInfo other) {
        return Long.compare(this.size, other.size);
    }
}