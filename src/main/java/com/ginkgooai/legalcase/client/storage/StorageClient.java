package com.ginkgooai.legalcase.client.storage;

import com.ginkgooai.core.common.config.FeignConfig;
import com.ginkgooai.legalcase.client.storage.dto.CloudFileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URL;
import java.util.List;

@FeignClient(name = "storage-service", url = "${core-storage-uri}", configuration = FeignConfig.class)
public interface StorageClient {

	@GetMapping("/v1/files")
	ResponseEntity<List<CloudFileResponse>> getFileDetails(@RequestParam List<String> fileIds);

	/**
	 * Generate a time-limited pre-signed URL for temporary access to private files
	 * @param fileId File ID (unique identifier)
	 * @return Pre-signed URL for file access
	 */
	@GetMapping("/v1/files/{fileId}/presigned-url")
	ResponseEntity<URL> generatePresignedUrl(@PathVariable("fileId") String fileId);

}
