package com.ginkgooai.legalcase.util;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for working with MultipartFile objects
 */
public class MultipartFileUtils {

	/**
	 * Convert a byte array to MultipartFile
	 * @param bytes The file content as byte array
	 * @param fileName The original file name
	 * @param contentType The content type of the file
	 * @return A MultipartFile instance containing the provided content
	 */
	public static MultipartFile createMultipartFile(byte[] bytes, String fileName, String contentType) {
		return new MockMultipartFile("file", fileName, contentType, bytes);
	}

}