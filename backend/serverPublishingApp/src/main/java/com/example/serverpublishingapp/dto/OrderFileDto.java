package com.example.serverpublishingapp.dto;

public class OrderFileDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String downloadUrl;

    public OrderFileDto(Long id, String fileName, String fileType, String downloadUrl) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.downloadUrl = downloadUrl;
    }

    // getters

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
