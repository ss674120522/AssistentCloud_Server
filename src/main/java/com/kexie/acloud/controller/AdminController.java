package com.kexie.acloud.controller;

import com.kexie.acloud.domain.School;
import com.kexie.acloud.service.ISchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by zojian on 2017/4/26.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ISchoolService schoolService;

    @RequestMapping(method = RequestMethod.GET)
    public String index(){
       return "admin-index";
    }

    @ResponseBody
    @RequestMapping(value = "/schools",method = RequestMethod.GET)
    public String getAllSchool(){
        return schoolService.getAllSchool().toString();
    }

    @ResponseBody
    @RequestMapping(value = "/schools/{id}",method = RequestMethod.GET)
    public String getSchoolById(@PathVariable int id){
        return schoolService.getSchoolById(id).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/schools",method = RequestMethod.POST)
    public String  addSchools(School school){
        System.out.println(school.toString());
        schoolService.addSchool(school);
        return "success!";
    }


}
