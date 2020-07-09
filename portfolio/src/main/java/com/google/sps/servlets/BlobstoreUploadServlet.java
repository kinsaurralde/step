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
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles image uploads
 */
@WebServlet("/blobstore-upload")
public class BlobstoreUploadServlet extends HttpServlet {
  private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  private static final String CONTENT_TYPE_TEXT = "text/html";
  private static final String UPLOAD_URL = "/blobstore";

  /** Returns url image should be uploaded to */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String uploadUrl = blobstoreService.createUploadUrl(UPLOAD_URL);
    response.setContentType(CONTENT_TYPE_TEXT);
    response.getWriter().println(uploadUrl);
    System.out.println(uploadUrl);
  }

  /** Returns url where image is now stored */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the URL of the image that the user uploaded to Blobstore.
    //String imageUrl = getUploadedFileUrl(request, "image");
    response.setContentType(CONTENT_TYPE_TEXT);

    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    response.setContentType("text/html");

    // User submitted form without selecting a file, so we can't get a blobKey (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      response.getWriter().println("null");
      return;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a blobKey (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      response.getWriter().println("null");
      return;
    }

    response.getWriter().println(blobKey.getKeyString());
  }
}
