package app.m26.wikidriver.models;

import java.util.List;

public class Publication {

    private String publicationId;
    private String userId;
    private long timeStamp;
    private int numberOfComments, numberOfLikes, numberOfDislikes;
    private List<String> likeList, dislikeList;
    private List<Comment> commentList;
    private String content;
    private int type;
    private String city;
    private String country;
    private List<String> imgUrlList;
    private String videoUrl;

    public Publication(String publicationId, String userId, long timeStamp, int numberOfComments, int numberOfLikes, int numberOfDislikes, List<String> likeList, List<String> dislikeList, List<Comment> commentList, String content, int type, String city, String country, List<String> imgUrlList, String videoUrl) {
        this.publicationId = publicationId;
        this.userId = userId;
        this.timeStamp = timeStamp;
        this.numberOfComments = numberOfComments;
        this.numberOfLikes = numberOfLikes;
        this.numberOfDislikes = numberOfDislikes;
        this.likeList = likeList;
        this.dislikeList = dislikeList;
        this.commentList = commentList;
        this.content = content;
        this.type = type;
        this.city = city;
        this.country = country;
        this.imgUrlList = imgUrlList;
        this.videoUrl = videoUrl;
    }

    public Publication() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public List<String> getLikeList() {
        return likeList;
    }

    public void setLikeList(List<String> likeList) {
        this.likeList = likeList;
    }

    public List<String> getDislikeList() {
        return dislikeList;
    }

    public void setDislikeList(List<String> dislikeList) {
        this.dislikeList = dislikeList;
    }

    public int getNumberOfLikes() {
        return numberOfLikes;
    }

    public void setNumberOfLikes(int numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }

    public int getNumberOfDislikes() {
        return numberOfDislikes;
    }

    public void setNumberOfDislikes(int numberOfDislikes) {
        this.numberOfDislikes = numberOfDislikes;
    }

    public List<String> getImgUrlList() {
        return imgUrlList;
    }

    public void setImgUrlList(List<String> imgUrlList) {
        this.imgUrlList = imgUrlList;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
