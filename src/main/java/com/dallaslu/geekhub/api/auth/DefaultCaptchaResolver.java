package com.dallaslu.geekhub.api.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.dallaslu.utils.captcha.CaptchaResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCaptchaResolver implements CaptchaResolver {

	@Override
	public void resolve(String title, String description, Function<String, Boolean> validatorCallback,
			Supplier<File> refreshCallback) {
		File captchaImage = refreshCallback.get();
		log.info("captcha image: " + captchaImage.getPath(),
				", please resolve the value to " + captchaImage.getName() + ".txt int the same folder.");
		String captcha = null;
		File captcharFolder = captchaImage.getParentFile();
		boolean applied = false;
		for (int i = 0; i < 60 * 10; i++) {
			try {
				Thread.sleep(1000);
				File captchaValue = new File(
						captcharFolder.getPath() + File.separator + captchaImage.getName() + ".txt");
				if (captchaValue.exists()) {
					captcha = new BufferedReader(new FileReader(captchaValue)).readLine();
					if (validatorCallback.apply(captcha)) {
						applied = true;
					}
					break;
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
				break;
			}
		}
		if (!applied) {
			resolve(title, description, validatorCallback, refreshCallback);
		}
	}
}
