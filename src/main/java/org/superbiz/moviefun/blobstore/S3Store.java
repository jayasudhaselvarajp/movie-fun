package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements  BlobStore{

    private AmazonS3Client amazonS3Client;
    private String phtoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.amazonS3Client = s3Client;
        this.phtoStorageBucket = photoStorageBucket;
        if(!amazonS3Client.doesBucketExist(photoStorageBucket))
            amazonS3Client.createBucket(photoStorageBucket);
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        objectMetadata.setContentLength(blob.getInputStream().available());
        amazonS3Client.putObject(phtoStorageBucket, blob.getName(), blob.getInputStream(), objectMetadata);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        System.out.println("s3");
        if(amazonS3Client.doesObjectExist(phtoStorageBucket,name)){
            S3Object s3Object = amazonS3Client.getObject(phtoStorageBucket,name);

            Blob blob=new Blob(s3Object.getKey(),s3Object.getObjectContent(),s3Object.getObjectMetadata().getContentType());
            return Optional.of(blob);
        }else{
            return Optional.empty();
        }

    }

    @Override
    public void deleteAll() {

    }
}
