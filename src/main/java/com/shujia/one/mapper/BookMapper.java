package com.shujia.one.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shujia.one.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}

