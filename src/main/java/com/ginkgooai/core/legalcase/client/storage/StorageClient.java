package com.ginkgooai.core.legalcase.client.storage;

import com.ginkgooai.core.legalcase.client.storage.dto.StorageFileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "storage-service", url = "${app.services.storage.url}")
public interface StorageClient {

	@PostMapping(value = "/api/v1/storage/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	StorageFileDTO uploadFile(@RequestPart("file") MultipartFile file, @RequestParam("folder") String folder);

	@GetMapping("/api/v1/storage/download/{fileId}")
	byte[] downloadFile(@PathVariable("fileId") String fileId);

	@DeleteMapping("/api/v1/storage/files/{fileId}")
	void deleteFile(@PathVariable("fileId") String fileId);

	@GetMapping("/api/v1/storage/files/{fileId}/url")
	String getDownloadUrl(@PathVariable("fileId") String fileId);

}