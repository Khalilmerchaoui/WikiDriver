package com.app.wikidriver.models;

public class Comment {

    private String commentId;
    private String userId;
    private String content;
    private long timeStamp;
    private String imgUrl;

    public Comment() {
    }

    public Comment(String commentId, String userId, String content, long timeStamp, String imgUrl) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.timeStamp = timeStamp;
        this.imgUrl = imgUrl;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
