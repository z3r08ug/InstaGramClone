package com.example.cv0318.instagramclone.Models;

public class Photo
{
    private String caption;
    private String date_created;
    private String image_path;
    private String photo_id;
    private String user_id;
    private String tags;
    
    public Photo(String caption, String date_created, String image_path, String photo_id, String user_id, String tags)
    {
        this.caption = caption;
        this.date_created = date_created;
        this.image_path = image_path;
        this.photo_id = photo_id;
        this.user_id = user_id;
        this.tags = tags;
    }
    
    public Photo()
    {
    }
    
    public String getCaption()
    {
        return caption;
    }
    
    public void setCaption(String caption)
    {
        this.caption = caption;
    }
    
    public String getDateCreated()
    {
        return date_created;
    }
    
    public void setDateCreated(String date_created)
    {
        this.date_created = date_created;
    }
    
    public String getImagePath()
    {
        return image_path;
    }
    
    public void setImagePath(String image_path)
    {
        this.image_path = image_path;
    }
    
    public String getPhoto_id()
    {
        return photo_id;
    }
    
    public void setPhotoId(String photo_id)
    {
        this.photo_id = photo_id;
    }
    
    public String getUserId()
    {
        return user_id;
    }
    
    public void setUserId(String user_id)
    {
        this.user_id = user_id;
    }
    
    public String getTags()
    {
        return tags;
    }
    
    public void setTags(String tags)
    {
        this.tags = tags;
    }
    
    @Override
    public String toString()
    {
        return "Photo{" +
                "caption='" + caption + '\'' +
                ", date_created='" + date_created + '\'' +
                ", image_path='" + image_path + '\'' +
                ", photo_id='" + photo_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }
}
