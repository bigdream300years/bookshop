package com.shujia.one.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shujia.one.entity.Book;

public interface BookService {

    /**
     * 书城条件分页查询
     * @param page
     * @param wrapper
     * @return
     */
    Page<Book> selectPage(Page<Book> page, QueryWrapper<Book> wrapper);

    /**
     * 增加一本书的信息
     * @param book
     * @return
     */
    int addBookInfo(Book book);

    /**
     * 根据id获取书本信息
     * @param id
     * @return
     */
    Book getOneBook(Integer id);

    /**
     * 删除一本书
     * @param book
     * @return
     */
    int deleteOneBook(Book book);

    /**
     * 修改一本书的信息
     * @param book
     * @return
     */
    int updOneBook(Book book);
}

