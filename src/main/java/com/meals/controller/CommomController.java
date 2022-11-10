package com.meals.controller;

import com.meals.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommomController {

    @Value("${meal.path}")//从属性配置文件获取
    private String path;

    /**
     * 文件上传下载
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){//这里的file必须和前端传过来的name属性的名字相同
        //获取上传图片的原始文件名
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));//从最后一个.开始截取

        //使用UUID生成新的文件名，防止名字重复上传的文件被覆盖
        String fileName = UUID.randomUUID().toString()+substring;
        //创建一个目录对象
        File f = new File(path);
        //判断目录是否存在，不存在创建
        if (!f.exists()){
            f.mkdirs();
        }
        try {
            //上传文件
            file.transferTo(new File(path+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
        //这里是因为新增菜品时，是先传过来文件名字的，然后进行上传，最后保存到时候是要保存到数据库中的，数据库就必须要知道这个名字，数据库存的是名字
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(@RequestParam String name, HttpServletResponse response){

        try {
            //输入流，通过输入流读取文件内容(前端发来请求，后端通过这个方法的输入流从硬盘中读取数据，然后返回给浏览器 )
            //上面的上传图片：上传的图片同时会自动调用下载请求,上传后返回文件名称，图片显示的过程通过此方法完成
            FileInputStream fileInputStream = new FileInputStream(new File(path+name));
            //输出流，通过输出流将文件写回（返回）到浏览器，在浏览器展实图片（浏览器将返回的数据通过输出流返回在浏览器，浏览器上面就能显示图片了）
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("imag/jpeg");//固定写法，告诉前端返回的是图片

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){//等于-1代表读取完毕
                //读取文件，将读取的数据存放到byte数组中
                outputStream.write(bytes,0,len);//从bytes数组中，从下标为0开始，写入总长为len的数据
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
