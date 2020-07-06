// Copyright 2019 Google LLC
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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  UserService userService = UserServiceFactory.getUserService();

  private static class Comment {
    String text;
    String name;
    String email;
    long timestamp;

    public Comment(Entity entity) {
      this.timestamp = (long) entity.getProperty("timestamp");
      this.name = (String) entity.getProperty("name");
      this.text = (String) entity.getProperty("text");
      this.email = (String) entity.getProperty("email");
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numComments = Integer.parseInt(getParameter(request, "num-comments", "5"));
    int page = Integer.parseInt(getParameter(request, "page", "1"));
    String sort = getParameter(request, "sort", "time-newest");
    SortDirection sortDirection = SortDirection.DESCENDING;
    String sortProperty = "timestamp";
    if (sort.equals("time-oldest")) {
      sortDirection = SortDirection.ASCENDING;
    } else if (sort.equals("name-normal")) {
      sortDirection = SortDirection.ASCENDING;
      sortProperty = "name";
    }
    Query query = new Query("Comment").addSort(sortProperty, sortDirection);
    List<Entity> results = datastore.prepare(query).asList(
        FetchOptions.Builder.withLimit(numComments).offset(numComments * (page - 1)));
    ArrayList<Comment> comments = new ArrayList<Comment>();
    for (Entity entity : results) {
      comments.add(new Comment(entity));
    }
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long timestamp = System.currentTimeMillis();
    String text = getParameter(request, "comment-text", "");
    String name = getParameter(request, "comment-name", "");
    if (text.length() > 0 && userService.isUserLoggedIn()) {
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("text", text);
      commentEntity.setProperty("name", name);
      commentEntity.setProperty("timestamp", timestamp);
      commentEntity.setProperty("email", userService.getCurrentUser().getEmail());
      datastore.put(commentEntity);
    }
    response.sendRedirect("/#comments");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
