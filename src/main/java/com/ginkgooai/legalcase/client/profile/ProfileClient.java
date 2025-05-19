package com.ginkgooai.legalcase.client.profile;

import com.ginkgooai.legalcase.client.profile.dto.ProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// @FeignClient(name = "profile-service", url = "${app.services.profile.url}")
// public interface ProfileClient {
//
// @GetMapping("/api/v1/profiles/user/{userId}")
// ProfileDTO getProfileByUserId(@PathVariable("userId") String userId);
//
// @GetMapping("/api/v1/profiles/{profileId}")
// ProfileDTO getProfileById(@PathVariable("profileId") String profileId);
//
// }