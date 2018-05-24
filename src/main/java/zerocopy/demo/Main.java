package zerocopy.demo;

import org.springframework.boot.SpringApplication;
import zerocopy.BlankFileBuilder;
import zerocopy.Files;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        switch (args[0]) {
            case "create":
                int count = Integer.parseInt(args[1]);
                String dir = args[2];
                int fileSizeInBytes = Integer.parseInt(args[3]);

                BlankFileBuilder.builder()
                        .directory(dir)
                        .fileCount(count)
                        .fileSizeInBytes(fileSizeInBytes)
                        .create();

                break;

            case "zeroCopy": {
                String sourceDir = args[1];
                String destDir = args.length > 2 ? args[2] : sourceDir + "-copy";

                Files.copyDir(new File(sourceDir), new File(destDir), Files.ZERO_FILE_COPIER);

                break;
            }

            case "nonZeroCopy": {
                String sourceDir = args[1];
                String destDir = args.length > 2 ? args[2] : sourceDir + "-copy";

                Files.copyDir(new File(sourceDir), new File(destDir), Files.OIO_FILE_COPIER);

                break;
            }

            case "web":
                SpringApplication.run(ZerocopyApplication.class, args);

                break;

            default:
                throw new IllegalArgumentException("Unrecognized command: " + args[0]);

        }

    }

}
