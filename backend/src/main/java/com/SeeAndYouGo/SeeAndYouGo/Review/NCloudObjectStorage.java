package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class NCloudObjectStorage {

    final String endPoint = "https://kr.object.ncloudstorage.com";
    final String regionName = "kr-standard";

    String accessKey;
    String secretKey;

    public NCloudObjectStorage() {
        this.accessKey = System.getProperty("access_key");
        this.secretKey = System.getProperty("secret_key");
    }

    public String imgUpload(InputStream file, String contentType) throws Exception {
        // S3 client
        // S3 client
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();

        String bucketName = "seeandyougo";

        // create folder
        String folderName = "img-folder/";

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName, new ByteArrayInputStream(new byte[0]), objectMetadata);

        s3.putObject(putObjectRequest);

        UUID one = UUID.randomUUID();
        // upload local file
        String objectName = one.toString();
        String filePath = "/tmp/sample.txt";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(file.available());


        PutObjectResult putObjectResult = s3.putObject(new PutObjectRequest(bucketName,folderName+one.toString(), file,metadata).withCannedAcl(CannedAccessControlList.PublicRead));

        URL seeandyougoUrl = s3.getUrl("seeandyougo", "img-folder/" + one.toString());
        System.out.format("Object %s has been created.\n", objectName);
        return seeandyougoUrl.toURI().toString();
    }
}

