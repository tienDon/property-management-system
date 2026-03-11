package com.pms.propertymanagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UploadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String hash;
    private String uploadTime;

    public UploadFile() {
    }

    public UploadFile( String fileName, String hash, String uploadTime) {
        this.fileName = fileName;
        this.hash = hash;
        this.uploadTime = uploadTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String updateTime) {
        this.uploadTime = updateTime;
    }

    @Override
    public String toString() {
        return "UploadFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", hash='" + hash + '\'' +
                ", updateTime='" + uploadTime + '\'' +
                '}';
    }
}
