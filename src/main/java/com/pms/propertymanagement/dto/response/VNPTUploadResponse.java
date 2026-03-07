package com.pms.propertymanagement.dto.response;

public class VNPTUploadResponse {
    private String message;
   private VNPTObject object;
   public static  class VNPTObject {
       private String fileName;
       private String title;
       private String description;
       private String hash;
       private String fileType;
       private String uploadedDate;
       private String storageType;
       private String tokenId;

       public String getFileName() {
           return fileName;
       }

       public void setFileName(String fileName) {
           this.fileName = fileName;
       }

       public String getTitle() {
           return title;
       }

       public void setTitle(String title) {
           this.title = title;
       }

       public String getDescription() {
           return description;
       }

       public void setDescription(String description) {
           this.description = description;
       }

       public String getHash() {
           return hash;
       }

       public void setHash(String hash) {
           this.hash = hash;
       }

       public String getFileType() {
           return fileType;
       }

       public void setFileType(String fileType) {
           this.fileType = fileType;
       }

       public String getUploadedDate() {
           return uploadedDate;
       }

       public void setUploadedDate(String uploadedDate) {
           this.uploadedDate = uploadedDate;
       }

       public String getStorageType() {
           return storageType;
       }

       public void setStorageType(String storageType) {
           this.storageType = storageType;
       }

       public String getTokenId() {
           return tokenId;
       }

       public void setTokenId(String tokenId) {
           this.tokenId = tokenId;
       }
   }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VNPTObject getObject() {
        return object;
    }

    public void setObject(VNPTObject object) {
        this.object = object;
    }
}
