package com.example.serverpublishingapp.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ReuploadFilesRequest {
    private String comment;
    private List<MultipartFile> files;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}