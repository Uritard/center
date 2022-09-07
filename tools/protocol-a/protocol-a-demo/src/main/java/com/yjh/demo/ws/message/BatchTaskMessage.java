package com.yjh.demo.ws.message;

import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.Msg;
import lombok.Data;

/**
 * @ClassName: BatchTaskMessage
 * @Description:
 * @author: yanhao
 * @date: 2022/9/7
 */
@Data
public class BatchTaskMessage extends BaseMessage implements Msg.Outbound {

    private int index;

    private String msg;

}
