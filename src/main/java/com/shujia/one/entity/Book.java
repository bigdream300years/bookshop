package com.shujia.one.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("book")
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    /**
     * 书本图片
     */
    @TableField("picture")
    private String picture;

    /**
     * 书本名称
     */
    @TableField("name")
    private String name;

    /**
     * 书本介绍
     */
    @TableField("introduce")
    private String introduce;

    /**
     * 出版社
     */
    @TableField("publish")
    private String publish;

    /**
     * 作者
     */
    @TableField("auth")
    private String auth;

    /**
     * 价格
     */
    @TableField("price")
    private Double price;

    /**
     * 删除标志
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @TableField("gmt_create")
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date gmtCreate;

    /**
     * 修改时间
     */
    @TableField("gmt_modified")
    private Date gmtModified;
}

