package com.inner.model;

import org.springframework.web.multipart.MultipartFile;

public class Empleador {

	private String DestinationLanguage;

	private MultipartFile Image;

	public Empleador() {
	}

	public Empleador(String DestinationLanguage, MultipartFile Image) {
		this.DestinationLanguage = DestinationLanguage;
		this.Image = Image;

	}

	public String getDestinationLanguage() {
		return DestinationLanguage;
	}

	public void setDestinationLanguage(String destinationLanguage) {
		DestinationLanguage = destinationLanguage;
	}

	public MultipartFile getImage() {
		return Image;
	}

	public void setImage(MultipartFile image) {
		Image = image;
	}

}
