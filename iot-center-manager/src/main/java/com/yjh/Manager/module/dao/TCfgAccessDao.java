package com.yjh.Manager.module.dao;

import java.util.List;
import java.util.Date;
import com.yjh.Manager.module.entity.TCfgAccess;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-17
 */
@Repository
public interface TCfgAccessDao {

    List<TCfgAccess> select(@Param(value = "accessId") Long accessId,
                                @Param(value = "name") String name,
                                @Param(value = "projectId") Long projectId,
                                @Param(value = "url") String url,
                                @Param(value = "port") String port,
                                @Param(value = "appId") String appId,
                                @Param(value = "secret") String secret,
                                @Param(value = "accessCode") String accessCode,
                                @Param(value = "userId") Long userId,
                                @Param(value = "userName") String userName,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "updateTime") Date updateTime,
                                @Param(value = "remark") String remark,
                                @Param(value = "state") String state);
}
