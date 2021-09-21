package lamborghini.wallpapers.CarWallpapers.CarSounds.models;

import java.io.Serializable;

public class Video implements Serializable {

    public int id;

    public String cat_id = "";
    public String category_name = "";

    public String vid = "";
    public String video_title = "";
    public String video_url = "";
    public String video_id = "";
    public String video_thumbnail = "";
    public String video_type = "";

    public Video() {
    }

    public Video(String vid) {
        this.vid = vid;
    }

    public Video(String category_name, String vid, String video_title, String video_url, String video_id, String video_thumbnail, String video_type) {
        this.category_name = category_name;
        this.vid = vid;
        this.video_title = video_title;
        this.video_url = video_url;
        this.video_id = video_id;
        this.video_thumbnail = video_thumbnail;
        this.video_type = video_type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }

    public String getVideo_type() {
        return video_type;
    }

    public void setVideo_type(String video_type) {
        this.video_type = video_type;
    }

}
