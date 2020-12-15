package com.dallaslu.utils.captcha;

import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CaptchaResolver {
	public void revole(String title, String description, Function<String, Boolean> validatorCallback,
			Supplier<File> refreshCallback);
}
