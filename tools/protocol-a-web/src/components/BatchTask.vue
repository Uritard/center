<template>
  <div class="bg">
    <div class="send">
      <el-form :inline="true" style="margin-top: 10px">
        <el-form-item label="WebSocket 地址:">
          <el-input v-model="wsIp"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onConnect">连接</el-button>
        </el-form-item>
      </el-form>

      <el-form :inline="true">
        <el-form-item label="IP">
          <el-input v-model="ip"></el-input>
        </el-form-item>
        <el-form-item label="端口">
          <el-input v-model="port"></el-input>
        </el-form-item>
        <el-form-item label="sendCode(多个逗号隔开)">
          <el-input type="textarea" autosize v-model="sendCode"></el-input>
        </el-form-item>
        <el-form-item label="receviceCode">
          <el-input v-model="receiveCode"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="createClient">批量创建客户端</el-button>
          <el-button type="primary" @click="destroyClient">销毁客户端</el-button>
          <el-button type="primary" @click="batchSendTest">批量发送消息</el-button>
          <el-button type="primary" @click="resetData">重置发送报文</el-button>
          <el-button type="primary" @click="resetResult">重置接收报文</el-button>
          <el-button type="primary" @click="setTaskResult">设置任务响应报文</el-button>
        </el-form-item>
      </el-form>
      <el-form>
        <el-form-item label="请求报文">
          <el-input type="textarea" autosize :rows="10" placeholder="请输入请求报文" v-model="xml">
          </el-input>
        </el-form-item>

        <el-form-item label="返回任务响应报文">
          <el-input type="textarea" autosize :rows="10" placeholder="请输入任务响应报文" v-model="taskXml">
          </el-input>
        </el-form-item>
      </el-form>
    </div>
    <div class="reception">
      <el-scrollbar style="width: 1920px;height: 600px;">

        <ul v-for="(item,index) in resultList" :key="index">
          <li style=" font-size: 16px ;color: #3ae4ef;margin: 10px">SendCode: {{ item.sendCode }}</li>
          <div v-for="response in item.responseList" class="text-reception" v-text="response"></div>
        </ul>
      </el-scrollbar>
    </div>

  </div>
</template>

<script>
import websocket from "@/utils/websocket";

export default {
  name: "BatchTask",
  data() {
    return {
      wsIp: 'ws://192.168.20.71:18088/demo/message',
      ip: "192.168.33.19",
      port: 10011,
      xml: '<?xmlversion="1.0"encoding="UTF-8"?>\n' +
          '<PatrolDevice>\n' +
          '\t<SendCode>Client01</SendCode>\n' +
          '\t<ReceiveCode>Server01</ReceiveCode>\n' +
          '\t<Type>251</Type>\n' +
          '\t<Command>2</Command>\n' +
          '\t<Time>2022-07-22 14:36:36</Time>\n' +
          '\t<Items>\n' +
          '\t\t<Item/></Items>\t\n' +
          '</PatrolDevice >',
      textReceptionList: [],
      sendCode: "Client5001,Client1002",
      receiveCode: "server01",
      resultList: [],
      taskXml: "<PatrolHost>\n" +
          "\t<SendCode>Client01</SendCode>\n" +
          "\t<ReceiveCode>Server01</ReceiveCode>\n" +
          "\t<Type>61</Type>\n" +
          "\t<Code>123</Code>\n" +
          "\t<Command/>\n" +
          "\t<Time>2022-09-06 20:41:50</Time>\n" +
          "\t<Items>\n" +
          "\t\t<Item robot_code=\"317\" task_name=\"ydfrobot20220906203905\" origin_file_path=\"\" file_path=\"\" value_type=\"0\" patroldevice_code=\"317\" device_id=\"102434\" patroldevice_name=\"0317-0901\" task_code=\"f8dbe83f4f014066b32cd82a470e0356\" valid=\"1\" device_name=\"红外精测B相\" unit=\"℃\" recognition_type=\"4\" origin_file_result_path=\"\" file_type=\"1\" data_type=\"0x02\" value_unit=\"30.88℃\" rectangle=\"\" material_id=\"123\" time=\"2022-09-06 20:38:59\" task_patrolled_id=\"f8dbe83f4f014066b32cd82a470e0356_20220906203905\" value=\"30.88\"/>\n" +
          "\t</Items>\n" +
          "</PatrolHost>",
    }
  },
  methods: {
    onConnect() {
      this.ws_platform = new websocket({
        wxurl: this.wsIp
      })
      this.ws_platform.onmessageWS((msgData) => {
        if (msgData === '连接成功') {
          this.$message.success('连接成功!!!');
          return;
        } else {
          let res = JSON.parse(msgData);
          this.resultList[res.index].responseList.push(res.msg);
        }

      })
    },
    createClient() {
      this.resultList = [];
      for (let str of this.sendCode.split(",")) {
        this.resultList.push({sendCode: str, responseList: []});
      }

      this.$http.windPost(`/demo/demo-client-task/create`, {
        ip: this.ip,
        port: this.port,
        sendCodeList: this.sendCode.split(","),
        receiveCode: this.receiveCode
      }).then((res) => {
        this.$message.success(res.result)
      })
    },
    destroyClient() {
      this.$http.windGet('/demo/demo-client-task/destroy').then(res => {
        this.$message.success(res.result);
        this.resultList = [];

      })
    },

    batchSendTest() {
      this.$http.windPost(`/demo/demo-client-task/batchsend`, {
        xml: this.xml,
      }).then(res => {
        this.$message.success(res.result)
      })
    },
    resetData() {
      this.xml = '<?xmlversion="1.0"encoding="UTF-8"?>\n' +
          '<PatrolDevice>\n' +
          '\t<SendCode>Client01</SendCode>\n' +
          '\t<ReceiveCode>Server01</ReceiveCode>\n' +
          '\t<Type>251</Type>\n' +
          '\t<Command>2</Command>\n' +
          '\t<Time>2022-07-22 14:36:36</Time>\n' +
          '\t<Items>\n' +
          '\t\t<Item/></Items>\t\n' +
          '</PatrolDevice >';
    },
    resetResult() {
      for (let resultListElement of this.resultList) {
        resultListElement.responseList = [];
      }
    },
    setTaskResult() {
      this.$http.windPost(`/demo/demo-client-task/settaskresult`, {
        xml: this.taskXml,
      }).then(res => {
        this.$message.success(res.result);
      })

    }


  }
}
</script>

<style scoped lang="less">
.bg {
  width: 100%;
  height: 100%;
  background-color: #166167;
}

.send {
  width: 100%;
  height: 50%;
  border: 1px solid #0AAFB7;

  .connect {
    width: 100%;
    height: 50px;
  }
}

.reception {
  width: 100%;
  height: 50%;

  .text-reception {
    width: 100%;
    background-color: #FFFFFF;
    padding: 8px 0 7px;
    border-bottom: 4px double #d1d1d1;
    color: black;
    font-size: 16px;

  }
}
</style>