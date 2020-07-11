// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import com.google.gson.Gson;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;



/**
 * Handles image uploads
 */
@WebServlet("/blobstore-upload")
public class BlobstoreUploadServlet extends HttpServlet {
  private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  private static final String sampleJson = "[{\"description_\": \"Cat\", \"score_\": 0.99284726}, {\"description_\": \"Small to medium-sized cats\", \"score_\": 0.9689183}, {\"description_\": \"Whiskers\", \"score_\": 0.9066983}]";
  private static final String CONTENT_TYPE_TEXT = "text/html";
  private static final String CONTENT_TYPE_JSON = "application/json";
  private static final String UPLOAD_URL = "/blobstore-upload";
  private static final String NO_BLOB_KEY = "null";

  private static class BlobResponse {
    private final String imageKey;
    private final String imageLabels;

    public BlobResponse(String imageKey, String imageLabels) {
      this.imageKey = imageKey;
      this.imageLabels = imageLabels;
    }
  }

  /** Returns url image should be uploaded to */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String uploadUrl = blobstoreService.createUploadUrl(UPLOAD_URL);
    response.setContentType(CONTENT_TYPE_TEXT);
    response.getWriter().println(uploadUrl);
    System.out.println(uploadUrl);
  }

  /** Returns blobKey of stored image */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType(CONTENT_TYPE_JSON);

    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a blobKey (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      response.getWriter().println(NO_BLOB_KEY);
      return;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a blobKey (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      response.getWriter().println(NO_BLOB_KEY);
      return;
    }

    Gson gson = new Gson();
    String imageLabelsJson;
    // Get the labels of the image that the user uploaded.
    byte[] blobBytes = getBlobBytes(blobKey);
    try {
      List<EntityAnnotation> imageLabels = getImageLabels(blobBytes);
      imageLabelsJson = gson.toJson(imageLabels);
    } catch(Exception e) {
      imageLabelsJson = sampleJson;
    }

    System.out.println(imageLabelsJson);

    BlobResponse blobResponse = new BlobResponse(blobKey.getKeyString(), imageLabelsJson);

    System.out.println(gson.toJson(blobResponse));

    response.getWriter().println(gson.toJson(blobResponse));
  }

   /**
   * Get the binary data stored at the blobKey parameter.
   */
  private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
    long currentByteIndex = 0;
    boolean continueReading = true;
    while (continueReading) {
      byte[] b =
          blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(b);
      if (b.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }

  /**
   * Uses the Google Cloud Vision API to generate a list of labels that apply to the image
   */
  private List<EntityAnnotation> getImageLabels(byte[] imgBytes) throws IOException {
    ByteString byteString = ByteString.copyFrom(imgBytes);
    Image image = Image.newBuilder().setContent(byteString).build();

    Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
    List<AnnotateImageRequest> requests = new ArrayList<>();
    requests.add(request);

    ImageAnnotatorClient client = ImageAnnotatorClient.create();
    BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
    client.close();
    List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
    AnnotateImageResponse imageResponse = imageResponses.get(0);

    if (imageResponse.hasError()) {
      System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
      return null;
    }

    return imageResponse.getLabelAnnotationsList();
  }
}
