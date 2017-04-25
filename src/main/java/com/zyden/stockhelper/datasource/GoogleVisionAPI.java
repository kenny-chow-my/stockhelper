package com.zyden.stockhelper.datasource;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by Kenny on 4/18/2017.
 */

@Component
public class GoogleVisionAPI implements IVisionAPI {

    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "Zyden-StockHelper/1.0";

    private static final int MAX_LABELS = 9;

    private Vision vision;

    private Log log =  org.apache.commons.logging.LogFactory.getLog(this.getClass());

    /**
     * Annotates an image using the Vision API.
     */
    @Override
    public Map<String, BigDecimal> getLabels(String path){
        Path imagePath = Paths.get(path);
        List<EntityAnnotation> labels = null;
        try {
            labels = labelImage(imagePath, MAX_LABELS);
        } catch (IOException e) {
            log.error("Unable to labelImage", e);
            throw new RuntimeException("Labeling failed");
        } catch (GeneralSecurityException e) {
            log.error("Unable to labelImage due to SecurityException", e);
            throw new RuntimeException("Labeling failed");
        }

        return mapLabels(labels);
    }

    @Override
    public Map<String, BigDecimal> getLabels(byte[] data){
        List<EntityAnnotation> labels = null;
        try {
            labels = labelImage(data, MAX_LABELS);
        } catch (IOException e) {
            log.error("Unable to labelImage", e);
            throw new RuntimeException("Labeling failed");
        } catch (GeneralSecurityException e) {
            log.error("Unable to labelImage due to SecurityException", e);
            throw new RuntimeException("Labeling failed");
        }

        return mapLabels(labels);
    }


    private Map<String, BigDecimal> mapLabels(List<EntityAnnotation> labels){
        Map<String, BigDecimal> labelMap = new Hashtable<>();
        for (EntityAnnotation label : labels) {
            BigDecimal score = new BigDecimal(label.getScore().toString());
            labelMap.put(label.getDescription(), score);
        }
        return labelMap;
    }



    /**
     * Connects to the Vision API using Application Default Credentials.
     */
    private Vision getVisionService() throws IOException, GeneralSecurityException {
        GoogleCredential credential =
                GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());

        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Gets up to {@code maxResults} labels for an image stored at {@code path}.
     */
    private List<EntityAnnotation> labelImage(Path path, int maxResults) throws IOException, GeneralSecurityException {
        byte[] data = Files.readAllBytes(path);
        return labelImage(data, maxResults);
    }

    private List<EntityAnnotation> labelImage(byte[] data, int maxResults) throws IOException, GeneralSecurityException {
        this.vision = getVisionService();
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(data))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("LABEL_DETECTION")
                                        .setMaxResults(maxResults)));
        Vision.Images.Annotate annotate =
                vision.images()
                        .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        annotate.setDisableGZipContent(true);

        BatchAnnotateImagesResponse batchResponse = annotate.execute();
        assert batchResponse.getResponses().size() == 1;
        AnnotateImageResponse response = batchResponse.getResponses().get(0);
        if (response.getLabelAnnotations() == null) {
            throw new IOException(
                    response.getError() != null
                            ? response.getError().getMessage()
                            : "Unknown error getting image annotations");
        }
        return response.getLabelAnnotations();
    }
}
