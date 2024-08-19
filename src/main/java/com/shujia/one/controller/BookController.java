package com.shujia.one.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shujia.one.entity.Book;
import com.shujia.one.entity.RestBean;
import com.shujia.one.service.BookService;
import com.shujia.one.utils.Code;
import com.shujia.one.utils.TimeUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;

@RestController
@RequestMapping("book")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 时间格式化
     */
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/");

    /**
     * 图片保存路径
     */
    @Value("${file-save-path}")
    private String fileSavePath;

    /**
     * 图片上传
     *
     * @param file
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public RestBean<String> uploadPicture(@RequestParam("file") MultipartFile file, HttpServletRequest request) {

        String directory = simpleDateFormat.format(TimeUtil.getTime());//格式化图片上传时间设置保存路径


        /**
         * 文件保存目录 E:/images/2020/03/15/
         * 如果目录不存在，则创建
         */
        File dir = new File(fileSavePath + directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        System.out.println("图片上传，保存的位置:" + fileSavePath + directory);

        /**
         * 给文件重新设置一个名字`
         * 后缀
         */
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;

        //4.创建这个新文件
        File newFile = new File(fileSavePath + directory + newFileName);
        //5.复制操作
        try {
            file.transferTo(newFile);
            //协议 :// ip地址 ：端口号 / 文件目录(/images/2020/03/15/xxx.jpg)
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/images/" + directory + newFileName;
            System.out.println("图片上传，访问URL：" + url);
            return  RestBean.success( "上传成功", url);
        } catch (IOException e) {
            return RestBean.fail(Code.WORK_ERR, "IO异常");
        }
    }


    /**
     * 书城条件分页查询
     * @param current 当前页数
     * @param pageSize 一页大小
     * @param book
     * @return
     * @RequestBody把前端传来json格式值自动转为相应request实体
     */
    @PostMapping("/list/{current}/{pageSize}")
    public RestBean<Page<Book>> selectPage(@PathVariable("current") long current,@PathVariable("pageSize") long pageSize,
                        @RequestBody Book book){

        //mybatis-plus分页
        Page<Book> page = new Page<>(current, pageSize);
        //条件查询构造器，mapper继承了basemapper方法用QueryWrapper类进行条件查询
        QueryWrapper<Book> wrapper = new QueryWrapper<>();
        //指定查询的条件，等价于select * from book where name like #{name} and is_deleted=0 order by gmt_modified desc
        String name = book.getName();
        if (!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }
        wrapper.eq("is_deleted","0");//条件值等于
        wrapper.orderByDesc("gmt_modified");//降序排序
        //结果页
        Page<Book> result = bookService.selectPage(page, wrapper);
        if (StringUtils.isEmpty(String.valueOf(result.getRecords()))){
            return  RestBean.fail(Code.WORK_ERR,"查询为空");
        }
        return  RestBean.success("操作成功",result);
    }

    /**
     * 书城新增一本书
     * @param book
     * @return
     */
    @PostMapping("/add-one-book")
    public RestBean<String> addBookInfo(@RequestBody Book book){
        int flag = bookService.addBookInfo(book);
        if (flag != 1){
            return  RestBean.fail(Code.WORK_ERR,"新增书本信息失败！");
        }else {
            return RestBean.success("新增书本信息成功！");
        }
    }

    /**
     * 根据id获取书本信息
     * @param id
     * @return
     */
    @GetMapping("/get-one-book/{id}")
    public RestBean<Book> getOneBook(@PathVariable("id") Integer id){
        Book result = bookService.getOneBook(id);
        if (!Strings.isNotEmpty(result.getName())){
            return  RestBean.fail(Code.WORK_ERR,"根据id获取书本信息失败！");
        }
        return RestBean.success("获取书本信息成功",result);
    }

    /**
     * 修改一本书的信息
     * @param book
     * @return
     */
    @PostMapping("/upd-one-book")
    public RestBean<String> updOneBook(@RequestBody Book book){
        int flag = bookService.updOneBook(book);
        if (flag != 1){
            return  RestBean.fail(Code.WORK_ERR,"修改书本信息失败！");
        }else {
            return RestBean.success("修改书本信息成功！");
        }
    }

    /**
     * 删除一本书
     * @param book
     * @return
     */
    @PostMapping("/delete-one-book")
    public RestBean<String> deleteOneBook(@RequestBody Book book){
        int flag = bookService.deleteOneBook(book);
        if (flag != 1){
            return  RestBean.fail(Code.WORK_ERR,"删除书本信息失败！");
        }else {
            return  RestBean.success("删除书本信息成功！");
        }
    }
}

