package cn.xjtu.iotlab.controller;


import cn.xjtu.iotlab.service.FilesManagerService;
import cn.xjtu.iotlab.service.impl.FilesManagerServiceImpl;
import cn.xjtu.iotlab.utils.ReadFiles;
import cn.xjtu.iotlab.utils.encdec.FileSearch;
import cn.xjtu.iotlab.vo.BFFile;
import cn.xjtu.iotlab.vo.Files;
import com.alibaba.fastjson.JSONObject;
import org.apache.tomcat.jni.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sun.nio.cs.ext.SJIS;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@RestController
@Controller
@RequestMapping("/fileManager")
public class FileManagerController {
//    int rand=(int)(Math.random()*10000);
    int rand = 10086;
    Set<Integer> idSet;
    int id = 0;
    @Autowired
    FilesManagerServiceImpl filesManagerService;

    @RequestMapping(value = "/getFileList", method = RequestMethod.POST)
    @ResponseBody
    public Object getFileList(HttpServletRequest req, HttpSession session){
        JSONObject jsonObject = new JSONObject();
        String userName = req.getParameter("userName");
//        System.out.println(userName);
        jsonObject.put("Message", "操作成功");
        jsonObject.put("StatusCode",200);
        jsonObject.put("CallbackType", null);
        jsonObject.put("Data", filesManagerService.getRootFile(userName));

        return jsonObject;
    }

    @RequestMapping(value = "/getId" , method = RequestMethod.GET)
    @ResponseBody
    public Object getId(){
        System.out.println("123");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Data", filesManagerService.getAllId());
        return jsonObject;
    }

    @RequestMapping(value = "/getAllFoldersApi")
    public Object getAllFoldersApi(HttpServletRequest req, HttpSession session){
        String userName = req.getParameter("userName");
//        System.out.println(userName);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Message", "操作成功");
        jsonObject.put("StatusCode",200);
        jsonObject.put("CallbackType", null);
        jsonObject.put("Data", filesManagerService.getAllFileList(userName));

        return jsonObject;
    }

    @RequestMapping(value = "/getFilesByNameApi", method = RequestMethod.POST)
    @ResponseBody
    public Object getFilesByNameApi(HttpServletRequest req, HttpSession session){
        List<Files> targetFiles = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        String BFFileName = req.getParameter("fileName");
        String fileName = req.getParameter("fileName");
        int type = Integer.parseInt(req.getParameter("type"));
        String CreateUsername = req.getParameter("userName");

        jsonObject.put("Message", "操作成功");
        jsonObject.put("StatusCode",200);
        jsonObject.put("CallbackType", null);

        if(fileName.equals("")){
            jsonObject.put("Data", filesManagerService.getAllId());
            return jsonObject;
        }

        fileName = "%" + fileName + "%";
        //BF搜索
        FileSearch fileSearch = new FileSearch(CreateUsername);
        List<String> searchKwBF = fileSearch.searchKeywordSplit(BFFileName,rand);//将搜索关键字划分为BF值
        //明文局部搜索
        if(type == 1){
            targetFiles = filesManagerService.getFilesByNameLocal(fileName, CreateUsername);
            System.out.println(targetFiles.size());
        }
        //明文全局搜索
        else if(type == 2){
            targetFiles = filesManagerService.getFilesByNameGlobal(fileName);
            System.out.println(targetFiles.size());
        }
        //BF局部搜索
        else if(type == 3){
            Set<Integer> ids = new HashSet<>();
            for(String searchBF:searchKwBF){
                List<Files> temp = filesManagerService.getFilesByBFLocal(searchBF, CreateUsername);
                for(Files f:temp){
                    if(!ids.contains(f.getId())){
                        targetFiles.add(f);
                        ids.add(f.getId());
                    }
                }
            }
        }
        //BF全局搜索
        else{
            Set<Integer> ids = new HashSet<>();
            for(String searchBF:searchKwBF){
                List<Files> temp = filesManagerService.getFilesByBFGlobal(searchBF);
                for(Files f:temp){
                    if(!ids.contains(f.getId())){
                        targetFiles.add(f);
                        ids.add(f.getId());
                    }
                }
            }
        }

        jsonObject.put("Data", targetFiles);
        return jsonObject;
    }

    @RequestMapping(value = "/saveBFValue", method = RequestMethod.POST)
    public void saveBFValue(HttpServletRequest req, HttpSession session){
//        String fileName = req.getParameter("fileName");
//        String userName = req.getParameter("userName");
        String fileName = "西安新一线城市.txt";
        String userName = "admin";
        FileSearch fileSearch = new FileSearch(userName);
        System.out.println(fileName);
        System.out.println(userName);
        fileSearch.random = Integer.toString(rand);
        List<String> BFValues = fileSearch.keywordSplitByFileName(fileName,rand);
        int id = filesManagerService.getIdByName(fileName,userName);
        for(String bfValue: BFValues){
            filesManagerService.insertFilesBF(new BFFile(id, fileName, bfValue));
            System.out.println(bfValue);
        }
    }





}
