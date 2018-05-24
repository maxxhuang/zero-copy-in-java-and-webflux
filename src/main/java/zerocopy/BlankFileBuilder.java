package zerocopy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BlankFileBuilder {

    public static BlankFileBuilder builder() {
        return new BlankFileBuilder();
    }


    private int fileCount =  5;

    private int fileSizeInBytes = 1024;

    private String fileBaseName = "file";

    private File directory = new File(".");


    private BlankFileBuilder() {}

    public BlankFileBuilder fileCount(int fileCount) {
        this.fileCount = fileCount;
        return this;
    }

    public BlankFileBuilder fileSizeInBytes(int fileSizeInBytes) {
        this.fileSizeInBytes = fileSizeInBytes;
        return this;
    }

    public BlankFileBuilder directory(File directory) {
        this.directory = directory;
        return this;
    }

    public BlankFileBuilder directory(String dirPath) {
        return directory(new File(dirPath));
    }

    public void create() {
        directory.mkdirs();

        for (int i = 1; i <= this.fileCount; ++i) {
            createFile(new File(this.directory, this.fileBaseName + i), this.fileSizeInBytes);
        }
    }

    private static void createFile(File file, int size) {
        try (FileOutputStream f = new FileOutputStream(file)) {
            f.write(new byte[size]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
