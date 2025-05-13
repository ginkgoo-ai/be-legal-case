package com.ginkgooai.core.legalcase.service;

import com.ginkgooai.core.legalcase.client.storage.StorageClient;
import com.ginkgooai.core.legalcase.client.storage.dto.StorageFileDTO;
import com.ginkgooai.core.legalcase.domain.CaseDocument;
import com.ginkgooai.core.legalcase.domain.CaseDocument.DocumentType;
import com.ginkgooai.core.legalcase.domain.LegalCase;
import com.ginkgooai.core.legalcase.dto.request.CreateCaseDocumentRequest;
import com.ginkgooai.core.legalcase.dto.response.CaseDocumentResponse;
import com.ginkgooai.core.legalcase.exception.ResourceNotFoundException;
import com.ginkgooai.core.legalcase.repository.CaseDocumentRepository;
import com.ginkgooai.core.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.core.legalcase.service.CaseDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseDocumentServiceImpl implements CaseDocumentService {

	private final CaseDocumentRepository caseDocumentRepository;

	private final LegalCaseRepository legalCaseRepository;

	private final StorageClient storageClient;

	private static final String DOCUMENT_FOLDER = "case-documents";

	@Override
	@Transactional
	public CaseDocumentResponse uploadDocument(UUID caseId, MultipartFile file, CreateCaseDocumentRequest request) {
		// 查找案件
		LegalCase legalCase = findLegalCaseById(caseId);

		try {
			// 上传文件到存储服务
			StorageFileDTO storageFile = storageClient.uploadFile(file, DOCUMENT_FOLDER);

			// 创建文档记录
			CaseDocument document = CaseDocument.builder()
				.title(request.getTitle())
				.description(request.getDescription())
				.filePath(storageFile.getUrl())
				.fileType(storageFile.getContentType())
				.fileSize(storageFile.getSize())
				.storageId(storageFile.getId())
				.legalCase(legalCase)
				.documentType(request.getDocumentType() != null ? DocumentType.valueOf(request.getDocumentType())
						: DocumentType.OTHER)
				.build();

			// 保存文档
			CaseDocument savedDocument = caseDocumentRepository.save(document);

			// 转换为响应对象
			return convertToResponse(savedDocument);
		}
		catch (Exception e) {
			log.error("上传文档失败", e);
			throw new RuntimeException("上传文档失败: " + e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CaseDocumentResponse> getDocumentsByCaseId(UUID caseId, Pageable pageable) {
		// 验证案件存在
		findLegalCaseById(caseId);

		// 获取案件文档
		Page<CaseDocument> documents = caseDocumentRepository.findByLegalCaseId(caseId, pageable);
		return documents.map(this::convertToResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public CaseDocumentResponse getDocument(UUID caseId, UUID documentId) {
		CaseDocument document = findDocumentByCaseIdAndDocumentId(caseId, documentId);
		return convertToResponse(document);
	}

	@Override
	@Transactional
	public CaseDocumentResponse updateDocument(UUID caseId, UUID documentId, CreateCaseDocumentRequest request) {
		CaseDocument document = findDocumentByCaseIdAndDocumentId(caseId, documentId);

		// 更新文档信息
		if (request.getTitle() != null) {
			document.setTitle(request.getTitle());
		}

		if (request.getDescription() != null) {
			document.setDescription(request.getDescription());
		}

		if (request.getDocumentType() != null) {
			try {
				document.setDocumentType(DocumentType.valueOf(request.getDocumentType()));
			}
			catch (IllegalArgumentException e) {
				log.warn("无效的文档类型: {}", request.getDocumentType());
				// 使用默认值
				document.setDocumentType(DocumentType.OTHER);
			}
		}

		CaseDocument updatedDocument = caseDocumentRepository.save(document);
		return convertToResponse(updatedDocument);
	}

	@Override
	@Transactional
	public void deleteDocument(UUID caseId, UUID documentId) {
		CaseDocument document = findDocumentByCaseIdAndDocumentId(caseId, documentId);

		// 删除存储中的文件
		try {
			if (document.getStorageId() != null) {
				storageClient.deleteFile(document.getStorageId());
			}
		}
		catch (Exception e) {
			log.error("删除存储文件失败: {}", document.getStorageId(), e);
			// 继续删除数据库记录
		}

		// 删除文档记录
		caseDocumentRepository.delete(document);
	}

	// 辅助方法

	private LegalCase findLegalCaseById(UUID caseId) {
		return legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new ResourceNotFoundException("案件不存在: " + caseId));
	}

	private CaseDocument findDocumentByCaseIdAndDocumentId(UUID caseId, UUID documentId) {
		return caseDocumentRepository.findByCaseIdAndDocumentId(caseId, documentId)
			.orElseThrow(() -> new ResourceNotFoundException("文档不存在: " + documentId));
	}

	private CaseDocumentResponse convertToResponse(CaseDocument document) {
		// 获取下载URL
		String downloadUrl = null;
		try {
			if (document.getStorageId() != null) {
				downloadUrl = storageClient.getDownloadUrl(document.getStorageId());
			}
		}
		catch (Exception e) {
			log.error("获取下载URL失败: {}", document.getStorageId(), e);
		}

		return CaseDocumentResponse.builder()
			.id(document.getId())
			.title(document.getTitle())
			.description(document.getDescription())
			.filePath(document.getFilePath())
			.fileType(document.getFileType())
			.fileSize(document.getFileSize())
			.storageId(document.getStorageId())
			.caseId(document.getLegalCase().getId())
			.documentType(document.getDocumentType())
			.downloadUrl(downloadUrl)
			.createdAt(document.getCreatedAt())
			.updatedAt(document.getUpdatedAt())
			.createdBy(document.getCreatedBy())
			.build();
	}

}