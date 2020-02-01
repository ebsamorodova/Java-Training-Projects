package ru.hse.cs.java2020.task01;
import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

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
        // Здесь уже точно папка (должно быть)

        long dirNameLen = getMaxDirNameLength(dir), size = getDirSize(dir), filesNumber = getFilesNumber(dir);
        String nameFormat = String.format("%%%ds", dirNameLen);
        System.out.println("Head folder: " + dir.getAbsolutePath() + ", " +  filesNumber + " items, size: " + size);
        getItemsSize(dir, size, nameFormat);

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

    public static void getItemsSize(File dir, long dirSize, String nameFormat) {
        int foldersCnt = 0;
        for (File elem: dir.listFiles()) {
            if (!Files.isSymbolicLink(elem.toPath()) && elem.isDirectory()) {
                foldersCnt++;
                long size = getDirSize(elem), filesNumber = getFilesNumber(elem);
                System.out.printf("Folder %3d: %s, %6d items, size %10d (%.4f%%)\n",
                        foldersCnt, String.format(nameFormat, elem.getName()), filesNumber, size, size / (1.0 * dirSize) * 100);
            }
        }
    }
}
