package com.dallaslu.geekhub.api;

import lombok.Data;

@Data
public class CsrfData {
	private String csrfParam;
	private String csrfToken;
}
