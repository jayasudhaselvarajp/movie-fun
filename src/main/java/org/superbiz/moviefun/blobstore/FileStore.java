package org.superbiz.moviefun.blobstore;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Optional;

import static java.lang.String.format;

public class FileStore implements BlobStore {

    private AmazonS3Client s3Client;
    private String bucket;

    public FileStore(AmazonS3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }
//    @Override
//    public void put(Blob blob) throws IOException {
//        File targetFile = new File((blob.getName()));
//        targetFile.delete();
//        targetFile.getParentFile().mkdirs();
//        targetFile.createNewFile();
//
//        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
//            outputStream.write(IOUtils.toByteArray(blob.getInputStream()));
//
//        }
////
//
//    }
//
    @Override
    public void put(Blob blob) throws IOException {
    File newFile = new File(blob.name);
    System.out.println("put method");
    //newFile.delete();
    //newFile.getParentFile().mkdirs();
    //newFile.createNewFile();

    try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
    outputStream.write(IOUtils.toByteArray(blob.inputStream));
    }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File file = new File(name);
        Blob blob = null;
        try {
             blob = new Blob(name, new FileInputStream(file), new Tika().detect(name));
        }catch(FileNotFoundException ex) {
            blob = getDefaultCover();
            ex.printStackTrace();
            //System.out.print(ex.toString());
        }
        return Optional.of(blob);
    }
    private Blob getDefaultCover() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream("default-cover.jpg");


        return new Blob("default-cover", input, MimeTypeUtils.IMAGE_JPEG_VALUE);
    }

//    @Override
//    public Optional<Blob> get(String name) throws IOException {
//        //String coverFileName = format("covers/%d", albumId);
//        //return new File(coverFileName);
//        S3Object object = s3Client.getObject(bucket, name);
//        if (object != null) {
//            Blob blob = new Blob(name, object.getObjectContent(), null);
//           return  Optional.of(blob);
//        }
//        return Optional.empty();
//    }

    @Override
    public void deleteAll() {
        // ...
    }
}
