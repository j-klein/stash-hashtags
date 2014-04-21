//
// Copyright 2014 Jason Klein
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//
// Class to extract hashtags from project descriptions and display projects indexed
// by hashtag.
//

package com.jklein.stash.hashtags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.project.ProjectService;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;
import com.google.common.collect.ImmutableMap;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashtagServlet extends HttpServlet{
  
  public class ProjectData implements Comparable<ProjectData> {
    private String name;
    private String url;
    
    public ProjectData(String name, String key) {
      this.name = name;
      this.url = "/stash/projects/" + key;
    }
    
    public String getName() {
      return this.name;
    }
    
    public String getUrl() {
      return this.url;
    }

    @Override
    public int compareTo(ProjectData o) {
      return this.name.compareTo(o.getName());
    }
  }
  
  private static final Pattern hashtagRegexPattern = Pattern.compile("#(\\w+)\\b");
  private static final Logger log = LoggerFactory.getLogger(HashtagServlet.class);
  private static final String moduleKey = "com.jklein.stash.stash-hashtags:hashtag-soy";
  private static final int pageSize = 10;
  private static final String templateName = "hashtag.ui.hashtagPanel";
  private final LoginUriProvider loginUriProvider;
  private final ProjectService projectService;
  private final SoyTemplateRenderer soyTemplateRenderer;
  private final StashAuthenticationContext stashAuthenticationContext;

  public HashtagServlet(LoginUriProvider loginUriProvider, ProjectService projectService, 
                           SoyTemplateRenderer soyTemplateRenderer, 
                           StashAuthenticationContext stashAuthenticationContext) {
    this.loginUriProvider = loginUriProvider;
    this.projectService = projectService;
    this.soyTemplateRenderer = soyTemplateRenderer;
    this.stashAuthenticationContext = stashAuthenticationContext;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    log.debug(String.format("Received get request for path '%s'.", req.getPathInfo()));
    
    // Ensure request is from an authenticated user. If not, redirect through a login page.
    if(!this.stashAuthenticationContext.isAuthenticated())
      resp.sendRedirect(this.loginUriProvider.getLoginUri(this.getUriWithQuery(req)).toASCIIString());
    
    HashMap<String, SortedSet<ProjectData>> hashtagProjectsMap = new HashMap<String, SortedSet<ProjectData>>();
    
    Matcher matcher;
    String currHashtag;
    ProjectData currProjectData;

    // Iterate through projects. For each project, parse description to extract hashtags. Build up a map of hashtags
    // to a sorted (for usability when displaying) set (to ignore duplicate tags) of projects with a given tag. 
    PageRequest proj_req = new PageRequestImpl(0,  pageSize);
    Page<? extends Project> currPage = this.projectService.findAll(proj_req);
    while(true) {
      for(Project currProj : currPage.getValues()) {
        log.debug(String.format("Found project %s with description %s.", currProj.getName(), 
            currProj.getDescription()));
        
        // Projects allow an empty description which is returned here as null. Nothing to match in this case.
        if(currProj.getDescription() == null)
          continue;
          
        // Create ProjectData instance to pass project name and link.
        currProjectData = new ProjectData(currProj.getName(), currProj.getKey());
        
        // Extract hashtags from project description via regex. Add current ProjectData instance to set for each 
        // relevant hashtag.
        matcher = hashtagRegexPattern.matcher(currProj.getDescription());
        while(matcher.find()) {
          currHashtag = matcher.group(1).toLowerCase();
          if(!hashtagProjectsMap.containsKey(currHashtag))
            hashtagProjectsMap.put(currHashtag, new TreeSet<ProjectData>());
          hashtagProjectsMap.get(currHashtag).add(currProjectData);
        }
      }
            
      if(currPage.getIsLastPage())
        break;
      currPage = this.projectService.findAll(currPage.getNextPageRequest());
    }
            
    this.renderGet(resp, ImmutableMap.<String, Object>of("hashtagProjectsMap", hashtagProjectsMap));
  }
  
  private URI getUriWithQuery(HttpServletRequest req) {
    StringBuilder uri = new StringBuilder(req.getRequestURL());
    if(req.getQueryString() != null)
      uri.append("?").append(req.getQueryString());

    return URI.create(uri.toString());
  }

  private void renderGet(HttpServletResponse resp, ImmutableMap<String, Object> data) 
      throws IOException, ServletException {
    resp.setContentType("text/html;charset=UTF-8");    
    try {
      soyTemplateRenderer.render(resp.getWriter(), moduleKey, templateName, data);
    }
    catch (SoyException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      throw new ServletException(e);
    }
  }
}
