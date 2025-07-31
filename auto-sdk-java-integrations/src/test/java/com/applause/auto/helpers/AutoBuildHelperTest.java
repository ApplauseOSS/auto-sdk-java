/*
 *
 * Copyright © 2024 Applause App Quality, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.applause.auto.helpers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.util.applausepublicapi.ApplausePublicApi;
import com.applause.auto.util.applausepublicapi.api.BuildApi;
import com.applause.auto.util.applausepublicapi.dto.AttachmentWithHashesDto;
import com.applause.auto.util.applausepublicapi.dto.PageProductVersionDetailsDto;
import com.applause.auto.util.applausepublicapi.dto.ProductVersionDetailsDto;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.testng.Assert;
import org.testng.annotations.Test;
import retrofit2.Response;

public class AutoBuildHelperTest {

  @Test
  public void testGetAllBuilds() {
    // Mock the response from the public API client
    ApplauseEnvironmentConfigurationManager.INSTANCE.override(Map.of("productId", "1"));
    BuildApi mockBuildsApi = mock(BuildApi.class);
    ApplausePublicApi mockApiClient = mock(ApplausePublicApi.class);
    AttachmentWithHashesDto mockAttachment = mock(AttachmentWithHashesDto.class);
    PageProductVersionDetailsDto mockPage = mock(PageProductVersionDetailsDto.class);
    ProductVersionDetailsDto mockVersion = mock(ProductVersionDetailsDto.class);

    // Set up the mock responses
    when(mockAttachment.url()).thenReturn("http://mock.url");
    when(mockVersion.attachments()).thenReturn(List.of(mockAttachment));
    when(mockPage.content()).thenReturn(List.of(mockVersion));
    when(mockPage.totalPages()).thenReturn(1L);
    when(mockBuildsApi.getBuilds(anyLong(), anyLong(), anyLong(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(Response.success(mockPage)));
    when(mockApiClient.builds()).thenReturn(mockBuildsApi);

    // Inject the mock client into the AutoBuildHelper
    AutoBuildHelper.setPublicApiClient(mockApiClient);

    // Call the method under test
    List<ProductVersionDetailsDto> builds = AutoBuildHelper.getAllBuilds();

    // Verify the results
    Assert.assertNotNull(builds, "Builds list should not be null");
    Assert.assertFalse(builds.isEmpty(), "Builds list should not be empty");
    Assert.assertEquals(builds.size(), 1, "Builds list should contain one element");
    Assert.assertEquals(builds.get(0), mockVersion, "Builds list should contain the mock version");
  }

  @Test
  public void testGetAllBuildsWithMultiplePages() {
    // Mock the response from the public API client
    ApplauseEnvironmentConfigurationManager.INSTANCE.override(Map.of("productId", "1"));
    BuildApi mockBuildsApi = mock(BuildApi.class);
    ApplausePublicApi mockApiClient = mock(ApplausePublicApi.class);
    AttachmentWithHashesDto mockAttachment = mock(AttachmentWithHashesDto.class);
    PageProductVersionDetailsDto mockPage1 = mock(PageProductVersionDetailsDto.class);
    PageProductVersionDetailsDto mockPage2 = mock(PageProductVersionDetailsDto.class);
    PageProductVersionDetailsDto mockPage3 = mock(PageProductVersionDetailsDto.class);
    ProductVersionDetailsDto mockVersion1 = mock(ProductVersionDetailsDto.class);
    ProductVersionDetailsDto mockVersion2 = mock(ProductVersionDetailsDto.class);
    ProductVersionDetailsDto mockVersion3 = mock(ProductVersionDetailsDto.class);

    // Set up attachments for some of the versions
    when(mockAttachment.url()).thenReturn("http://mock.url");
    when(mockVersion1.attachments()).thenReturn(List.of(mockAttachment));
    when(mockVersion3.attachments()).thenReturn(List.of(mockAttachment));

    // Set up the mock responses for each page
    when(mockPage1.content()).thenReturn(List.of(mockVersion1));
    when(mockPage1.totalPages()).thenReturn(3L);
    when(mockPage2.content()).thenReturn(List.of(mockVersion2));
    when(mockPage2.totalPages()).thenReturn(3L);
    when(mockPage3.content()).thenReturn(List.of(mockVersion3));
    when(mockPage3.totalPages()).thenReturn(3L);

    // Mock the API client to return the correct page based on the page number
    when(mockBuildsApi.getBuilds(anyLong(), eq(1L), anyLong(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(Response.success(mockPage1)));
    when(mockBuildsApi.getBuilds(anyLong(), eq(2L), anyLong(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(Response.success(mockPage2)));
    when(mockBuildsApi.getBuilds(anyLong(), eq(3L), anyLong(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(Response.success(mockPage3)));

    when(mockApiClient.builds()).thenReturn(mockBuildsApi);

    // Inject the mock client into the AutoBuildHelper
    AutoBuildHelper.setPublicApiClient(mockApiClient);

    // Call the method under test
    List<ProductVersionDetailsDto> builds = AutoBuildHelper.getAllBuilds();

    // Verify the results
    Assert.assertNotNull(builds, "Builds list should not be null");
    Assert.assertFalse(builds.isEmpty(), "Builds list should not be empty");
    Assert.assertEquals(builds.size(), 3, "Builds list should contain three elements");
    Assert.assertEquals(
        builds.get(0), mockVersion1, "First element should be the first mock version");
    Assert.assertEquals(
        builds.get(1), mockVersion2, "Second element should be the second mock version");
    Assert.assertEquals(
        builds.get(2), mockVersion3, "Third element should be the third mock version");
  }
}
