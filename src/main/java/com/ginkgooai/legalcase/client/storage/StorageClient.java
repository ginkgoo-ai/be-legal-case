package com.ginkgooai.legalcase.client.storage;

import com.ginkgooai.legalcase.client.storage.dto.CloudFileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;

@FeignClient(name = "storage-service", url = "${core-storage-uri}")
// @FeignClient(name = "storage-service", url = "${core-storage-uri}", configuration =
// FeignConfig.class)
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

	/**
	 * Upload file to storage service
	 * @param file Multipart file to upload
	 * @return Cloud file response with storage ID
	 */
	@PostMapping(value = "/v1/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<CloudFileResponse> uploadFile(@RequestPart("file") MultipartFile file);

}
