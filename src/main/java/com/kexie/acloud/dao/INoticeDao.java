package com.kexie.acloud.dao;

import com.kexie.acloud.domain.Notice;
import com.kexie.acloud.exception.NoticeException;

import java.util.List;
import java.util.Set;

/**
 * Created by zojian on 2017/5/8.
 */
public interface INoticeDao {

    /**
     * 发布公告
     * @param notice 公告对象
     * @return
     */
    boolean addNotice(Notice notice,String userId);

    /**
     * 更新公告
     * @param notice
     * @param notice_id
     * @return
     */
    boolean updateNotice(int notice_id, Notice notice, String user_id);

    /**
     * 删除公告
     * @param notice_id
     * @param user_id
     * @return
     */
    boolean deleteNotice(int notice_id, String user_id);

    /**
     * 根据用户ID分页获取所以用户可见的公告列表
     * @param user_id 用户ID
     * @param page 页数
     * @return
     */
    List<Notice> getNoticesByUserId(String user_id, int page,int pageSize);

    /**
     * 根据发布者ID分页获取由用户发布的公告
     * @param publisher_id
     * @param page
     * @param pageSize
     * @return
     */
    List<Notice> getNoticesByPublisherId(String publisher_id,int page, int pageSize);

    /**
     * 根据用户ID和社团ID分页获取公告列表
     * @param user_id
     * @param society_id
     * @param page
     * @param pageSize
     * @return
     */
    List<Notice> getNoticesByUserIdAndSocietyId(String user_id, int society_id, int page,int pageSize);

    /**
     * 根据公告ID获取公告详细信息
     * @param notice_id 公告ID
     * @return
     */
    Notice getNoticeByNoticeId(int notice_id,String user_id,String identifier) throws NoticeException;

    /**
     * 获取公告的浏览者
     * @param notice_id
     * @return
     */
    Set<String> getNoticeVisitorByNoticeId(int notice_id,String user_id) throws NoticeException;

    /**
     * 判断user_id是否有权查看公告
     * @param notice_id
     * @param user_id
     * @return
     */
    boolean getPermission(int notice_id,String user_id);

}
