package zerocopy;

import java.io.*;
import java.nio.channels.FileChannel;

public class Files {

    public static final int BUFFER_SIZE_IN_BYTES = 4096;


    public static FileCopier OIO_FILE_COPIER = (File sourceFile, File destFile, byte[] buffer) -> {
        try {nonZeroCopyFile(sourceFile, new FileOutputStream(destFile), buffer);}
        catch (IOException e) {throw new RuntimeException(e);}
    };

    public static FileCopier ZERO_FILE_COPIER = (sourceFile, destFile, buffer) -> {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {

            FileChannel sourceChannel = fis.getChannel();
            FileChannel destChannel = fos.getChannel();

            sourceChannel.transferTo(0, sourceFile.length(), destChannel);

        } catch (IOException e) {
            throw new RuntimeException(e);}
    };

    public static void copyDir(File sourceDir, File destDir, FileCopier fileCopier) {

        destDir.mkdirs();

        byte[] buffer = new byte[BUFFER_SIZE_IN_BYTES];

        for (File source : sourceDir.listFiles(f -> f.isFile())) {
            File dest = new File(destDir, source.getName());

            fileCopier.copy(source, dest, buffer);
        }
    }

    public static void nonZeroCopyFile(File sourceFile, OutputStream output, byte[] buffer) {
        try (BufferedInputStream bis =
                     new BufferedInputStream(new FileInputStream(sourceFile), BUFFER_SIZE_IN_BYTES);
             BufferedOutputStream bos =
                     new BufferedOutputStream((output))) {

            for (int bytesRead = bis.read(buffer); bytesRead != -1; bytesRead = bis.read(buffer)) {
                bos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {throw new RuntimeException((e));}
    }


    ///////////////////////////////////////////////////////////////////////////


    @FunctionalInterface
    interface FileCopier {
        void copy(File sourceFile, File destFile, byte[] buffer);
    }

}
