package com.yjh.logs.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lichensi
 * @date 2020/12/10 15:28
 */
public class UserContext {

    private static final ThreadLocal<OperatorDto> USER_CONTEXT = new TransmittableThreadLocal();

    public static void init(Long userId, String userName) {
        init(OperatorDto.build(userId, userName));
    }

    public static void init(OperatorDto operatorDto) {
        USER_CONTEXT.set(operatorDto);
    }

    public static OperatorDto getCurrentUser() {
        return USER_CONTEXT.get();
    }

    public static void clear() {
        USER_CONTEXT.remove();
    }

    @Getter
    @Setter
    public static class OperatorDto{

        private Long userId;

        private String userName;

        public static OperatorDto build(Long userId, String userName){
            OperatorDto operatorDto = new OperatorDto();
            operatorDto.setUserId(userId);
            operatorDto.setUserName(userName);
            return operatorDto;
        }
    }
}
