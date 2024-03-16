package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Component
@ConfigurationProperties(value = "ncloud")
@PropertySource("classpath:key.yml")
public class NCloudObjectStorage {

    private final String endPoint = "https://kr.object.ncloudstorage.com";
    private final String regionName = "kr-standard";
    private static String accessKey;
    private static String secretKey;


    public NCloudObjectStorage(@Value("${ACCESS_KEY}") String accessKey, @Value("${SECRET_KEY}") String secretKey) {
        NCloudObjectStorage.accessKey = accessKey;
        NCloudObjectStorage.secretKey = secretKey;
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

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(file.available());

        URL seeandyougoUrl = s3.getUrl("seeandyougo", "img-folder/" + one);
        System.out.format("Object %s has been created.\n", objectName);
        return seeandyougoUrl.toURI().toString();
    }
}

