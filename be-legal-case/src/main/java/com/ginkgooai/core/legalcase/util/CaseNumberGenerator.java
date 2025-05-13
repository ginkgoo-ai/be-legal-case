package com.ginkgooai.core.legalcase.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 案件编号生成器
 */
@Component
public class CaseNumberGenerator {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	private static final Random RANDOM = new Random();

	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * 生成唯一的案件编号 格式：GK-{日期}-{4位随机字母和数字} 例如：GK-20230510-A12B
	 * @return 案件编号
	 */
	public String generate() {
		LocalDateTime now = LocalDateTime.now();
		String dateStr = now.format(FORMATTER);

		// 生成4位随机字母和数字
		StringBuilder suffix = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			if (RANDOM.nextBoolean()) {
				// 添加随机字母
				suffix.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
			}
			else {
				// 添加随机数字
				suffix.append(RANDOM.nextInt(10));
			}
		}

		return String.format("GK-%s-%s", dateStr, suffix.toString());
	}

}