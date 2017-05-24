package com.kexie.acloud.dao;

import com.kexie.acloud.domain.Notice;
import com.kexie.acloud.domain.User;
import com.kexie.acloud.websocket.TextMessageHandler;
import com.sun.org.apache.regexp.internal.RE;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by zojian on 2017/5/8.
 */
@Repository
@Transactional
public class NoticeDao implements INoticeDao {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    TextMessageHandler textMessageHandler;

    @Autowired
    SessionFactory sessionFactory;

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public boolean addNotice(Notice notice) {
        try {
            getCurrentSession().save(notice);
            //获取去掉@的userId
            List<String> executorList = new ArrayList<>();
            for (User u : notice.getExecutors()) {
                // executorList.add(FormatName.withOutEmailSuffix(u.getUserId()));
                executorList.add(u.getUserId());
            }
            //向每个公告可见者发送新公告消息
            TextMessage message = new TextMessage("你有一条新公告");
            for (String username : executorList) {
                textMessageHandler.sendMessageToUser(username, message);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateNotice(int notice_id, Notice notice, String user_id) {

        Notice oldNotice = getCurrentSession().get(Notice.class, notice_id);
        if (!notice.getPublisher().getUserId().equals(oldNotice.getPublisher().getUserId())
                || !notice.getPublisher().getUserId().equals(user_id)) {
            //发布者信息不能修改
            return false;
        }
        try {
            notice.setId(oldNotice.getId());
            notice.setTime(new Date());
            getCurrentSession().evict(oldNotice);
            getCurrentSession().update(notice);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteNotice(int notice_id, String user_id) {
        try {
            Notice notice = getNoticeByNoticeId(notice_id, user_id);
            if (notice.getPublisher().getUserId().equals(user_id)) {
                notice.setStatus((short) 1);
                getCurrentSession().update(notice);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Notice> getNoticesByUserId(String user_id, int page, int pageSize) {
        String hql = "FROM Notice WHERE (publisher_id = ? AND notice_status = 0)OR ? in elements(executors) ORDER BY time DESC";
        User user = new User();
        user.setUserId(user_id);
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter(0, user_id);
        query.setParameter(1, user);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);
        return query.list();
    }

    @Override
    public List<Notice> getNoticesByUserIdAndSocietyId(String user_id, int society_id, int page, int pageSize) {
        String hql = "FROM Notice WHERE (publisher_id = ? AND society_id = ? AND notice_status = 0) OR ? in elements(executors) ORDER BY time DESC";
        User user = new User();
        user.setUserId(user_id);
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter(0, user_id);
        query.setParameter(1, society_id);
        query.setParameter(2, user);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);
        return query.list();
    }

    @Override
    public List<Notice> getNoticesByPublisherId(String publisher_id, int page, int pageSize) {
        String hql = "FROM Notice WHERE publisher_id = ? AND notice_status = 0 ORDER BY time DESC";
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter(0, publisher_id);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);
        return query.list();
    }

    @Override
    public Notice getNoticeByNoticeId(int notice_id, String user_id) {
        String notice_visitor = "notice:visitor:" + notice_id;
        Notice notice = getCurrentSession().get(Notice.class, notice_id);
        if(notice.getVisitor_status()==0){
            // 未被所有人查看
            if(!user_id.equals(notice.getPublisher().getUserId())) {
                // 不记录发布者的查看记录
                redisTemplate.boundSetOps(notice_visitor).add(user_id);
            }
            if(redisTemplate.boundSetOps(notice_visitor).size()==notice.getExecutors().size()){
                // 公告可见者都查看了公告,将状态持久化到MySQL
                notice.setVisitor_status((short) 1);
                updateNotice(notice.getId(),notice,notice.getPublisher().getUserId());
                // 设置键过期时间（5天）
                redisTemplate.boundSetOps(notice_visitor).expire(20,TimeUnit.SECONDS);
            }
        }
        return notice;
    }

    @Override
    public Set<String> getNoticeVisitorByNoticeId(int notice_id) {
        String notice_visitor = "notice:visitor:" + notice_id;
        if(redisTemplate.boundSetOps(notice_visitor).members().size()==0){
            // 缓存未命中,将MySQL中数据载入redis
            Notice notice = getCurrentSession().get(Notice.class,notice_id);
            if(notice.getVisitor_status()==1) {
                for (User user : notice.getExecutors()) {
                    redisTemplate.boundSetOps(notice_visitor).add(user.getUserId());
                }
            }
        }
        return redisTemplate.boundSetOps(notice_visitor).members();
    }

    @Override
    public boolean getPermission(int notice_id, String user_id) {
        String hql = "from notice_user_permission where notice_id=? and user_id=?";
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter(0, notice_id);
        query.setParameter(1,user_id);

        // 判断是否是该公告的参与者或发布者
        if(query.list().size()>0 ||
                getCurrentSession().get(Notice.class,notice_id).getPublisher().getUserId().equals(user_id)) {
            return true;
        }
        return false;
    }
}
