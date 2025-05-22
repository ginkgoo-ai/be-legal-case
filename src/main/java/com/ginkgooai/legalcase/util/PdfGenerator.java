package com.ginkgooai.legalcase.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Utility class for generating PDF documents from questionnaire responses
 */
@Slf4j
public class PdfGenerator {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final float MARGIN = 50;

	private static final float FONT_SIZE = 12;

	private static final float LINE_HEIGHT = 20;

	/**
	 * Generate PDF from questionnaire responses
	 * @param questionnaireId Questionnaire ID
	 * @param responses Questionnaire response data
	 * @param title Questionnaire title
	 * @return PDF file byte array
	 */
	public static byte[] generateQuestionnairePdf(String questionnaireId, Map<String, Object> responses, String title) {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);

			float yPosition = page.getMediaBox().getHeight() - MARGIN;
			addContent(document, page, yPosition, questionnaireId, responses, title);

			// Convert PDF to byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			document.save(baos);
			return baos.toByteArray();

		}
		catch (IOException e) {
			log.error("Failed to generate PDF for questionnaire: {}", questionnaireId, e);
			throw new RuntimeException("PDF generation failed", e);
		}
	}

	/**
	 * Add content to the PDF document
	 * @param document PDF document
	 * @param page Initial page
	 * @param startY Initial Y position
	 * @param questionnaireId Questionnaire ID
	 * @param responses Questionnaire responses
	 * @param title Document title
	 * @throws IOException If there's an error writing to the PDF
	 */
	private static void addContent(PDDocument document, PDPage page, float startY, String questionnaireId,
			Map<String, Object> responses, String title) throws IOException {

		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		float yPosition = startY;

		try {
			// Add title
			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
			contentStream.newLineAtOffset(MARGIN, yPosition);
			contentStream.showText(title);
			contentStream.endText();
			yPosition -= LINE_HEIGHT * 2;

			// Add questionnaire ID and time
			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
			contentStream.newLineAtOffset(MARGIN, yPosition);
			contentStream.showText("Questionnaire ID: " + questionnaireId);
			contentStream.endText();
			yPosition -= LINE_HEIGHT;

			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
			contentStream.newLineAtOffset(MARGIN, yPosition);
			String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			contentStream.showText("Submission Time: " + currentDateTime);
			contentStream.endText();
			yPosition -= LINE_HEIGHT * 2;

			// Add response data
			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
			contentStream.newLineAtOffset(MARGIN, yPosition);
			contentStream.showText("Questionnaire Responses");
			contentStream.endText();
			yPosition -= LINE_HEIGHT * 1.5f;

			// Loop through response data and add to PDF
			for (Map.Entry<String, Object> entry : responses.entrySet()) {
				// Check if we need a new page
				if (yPosition < MARGIN + LINE_HEIGHT) {
					contentStream.close();
					page = new PDPage(PDRectangle.A4);
					document.addPage(page);
					contentStream = new PDPageContentStream(document, page);
					yPosition = page.getMediaBox().getHeight() - MARGIN;
				}

				// Question
				contentStream.beginText();
				contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
				contentStream.newLineAtOffset(MARGIN, yPosition);
				contentStream.showText(entry.getKey() + ":");
				contentStream.endText();
				yPosition -= LINE_HEIGHT;

				// Answer
				contentStream.beginText();
				contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
				contentStream.newLineAtOffset(MARGIN + 20, yPosition);
				String answer = entry.getValue() != null ? entry.getValue().toString() : "No response";
				// Handle long answers
				if (answer.length() > 80) {
					contentStream.showText(answer.substring(0, 77) + "...");
				}
				else {
					contentStream.showText(answer);
				}
				contentStream.endText();
				yPosition -= LINE_HEIGHT * 1.5f;
			}
		}
		finally {
			contentStream.close();
		}
	}

}