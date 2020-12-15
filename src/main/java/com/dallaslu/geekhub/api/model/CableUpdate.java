package com.dallaslu.geekhub.api.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CableUpdate {
	private List<GeekHubPostItem> posts = new ArrayList<>();
	private List<GeekHubComment> comments = new ArrayList<>();
}
