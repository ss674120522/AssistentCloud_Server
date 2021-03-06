package com.kexie.acloud.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kexie.acloud.domain.Meeting;
import com.kexie.acloud.domain.Room;
import com.kexie.acloud.domain.User;
import com.kexie.acloud.exception.AuthorizedException;
import com.kexie.acloud.exception.FormException;
import com.kexie.acloud.service.IMeetingService;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.annotation.Resource;

/**
 * Created : wen
 * DateTime : 2017/5/11 20:43
 * Description :
 */
@RestController
@RequestMapping(value = "meeting", produces = {"application/json;charset=UTF-8"})
public class MeetingController {

    @Resource
    private IMeetingService mMeetingService;

    /**
     * 创建一个会议，创建成功返回会议房间
     *
     * @param meeting
     * @param form
     * @param userId
     * @return
     * @throws FormException
     * @throws AuthorizedException
     */
    @RequestMapping(method = RequestMethod.POST)
    public JSONObject createMeeting(@Validated @RequestBody Meeting meeting, BindingResult form,
                                    @RequestAttribute("userId") String userId) throws FormException, AuthorizedException {

        if (form.hasErrors()) throw new FormException(form);

        // 创建会议
        Room room = mMeetingService.createMeeting(meeting, new User(userId));

        // 构造json
        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        user.put("userId", room.getMaster().getUserId());
        user.put("logoUrl", room.getMaster().getLogoUrl());

        json.put("roomId", room.getRoomId());
        json.put("name", room.getName());
        json.put("master", user);
        json.put("type", room.getRoomType());
        return json;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Meeting> getMeeting(@RequestAttribute("userId") String userId) {
        List<Meeting> meetings = mMeetingService.getCurrentUserNotStartMeeting(userId);

        return meetings;
    }
}
