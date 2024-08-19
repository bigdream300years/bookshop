package com.shujia.one.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shujia.one.entity.Book;
import com.shujia.one.mapper.BookMapper;
import com.shujia.one.service.BookService;
import com.shujia.one.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    /**
     * 书城条件分页查询
     *
     * @param page
     * @param wrapper
     * @return
     */
    @Override
    public Page<Book> selectPage(Page<Book> page, QueryWrapper<Book> wrapper) {
        return bookMapper.selectPage(page,wrapper);//调用继承的色了Page方法，由写好的类进行查询
    }

    /**
     * 增加一本书的信息
     *
     * @param book
     * @return
     */
    @Override
    public int addBookInfo(Book book) {
        Book entity = new Book();
        entity.setId(book.getId());
        entity.setPicture(book.getPicture());
        entity.setName(book.getName());
        entity.setIntroduce(book.getIntroduce());
        entity.setPublish(book.getPublish());
        entity.setAuth(book.getAuth());
        entity.setPrice(book.getPrice());
        entity.setIsDeleted(0);
        entity.setGmtCreate(TimeUtil.getTime());
        entity.setGmtModified(TimeUtil.getTime());
        return bookMapper.insert(entity);
    }

    /**
     * 根据id获取书本信息
     *
     * @param id
     * @return
     */
    @Override
    public Book getOneBook(Integer id) {
        Book book = bookMapper.selectById(id);
        return book;
    }

    /**
     * 删除一本书
     *
     * @param book
     * @return
     */
    @Override
    public int deleteOneBook(Book book) {
        Book entity = new Book();
        entity.setId(book.getId());
        entity.setIsDeleted(1);
        entity.setGmtModified(TimeUtil.getTime());
        return bookMapper.updateById(entity);
    }

    /**
     * 修改一本书的信息
     *
     * @param book
     * @return
     */
    @Override
    public int updOneBook(Book book) {
        Book entity = new Book();
        entity.setId(book.getId());
        entity.setPicture(book.getPicture());
        entity.setName(book.getName());
        entity.setIntroduce(book.getIntroduce());
        entity.setPublish(book.getPublish());
        entity.setAuth(book.getAuth());
        entity.setPrice(book.getPrice());
        entity.setGmtModified(TimeUtil.getTime());
        return bookMapper.updateById(entity);
    }
}

