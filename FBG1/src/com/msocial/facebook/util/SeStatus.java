package com.msocial.facebook.util;

import java.util.Date;

public class SeStatus implements java.io.Serializable{
    public Date createdAt;
    public long id;
    public String text;
    public boolean isFavorited;
    public boolean ismytweets;
    public boolean selected;
    public SeSimplyUser user;

}