<template>
  <div class="bg">
    <div class="send">
      <el-form :inline="true" style="padding: 10px">
        <el-form-item label="IP">
          <el-input v-model="ip"></el-input>
        </el-form-item>
        <el-form-item label="FTP端口">
          <el-input v-model="ftpPort"></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="username"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password"></el-input>
        </el-form-item>
        <el-form-item label="本地文件路径">
          <el-input v-model="filePath"></el-input>
        </el-form-item>
        <el-form-item label="远程文件名">
          <el-input v-model="remoteFilename"></el-input>
        </el-form-item>
        <el-form-item label="key_Pw">
          <el-input v-model="keyPw"></el-input>
        </el-form-item>
      </el-form>
      <el-form :inline="true" style="padding: 10px">
        <el-form-item label="发送端口">
          <el-input v-model="socketPort"></el-input>
        </el-form-item>
        <el-form-item label="并发线程数">
          <el-input v-model="threadCount"></el-input>
        </el-form-item>
        <el-form-item label="发送总请求数">
          <el-input v-model="taskCount"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="batchSendTest">批量发送消息</el-button>
          <el-button type="primary" @click="resetData">重置发送报文</el-button>
          <el-button type="primary" @click="queryResult">查看结果</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-input type="textarea" :rows="20" placeholder="请输入报文" v-model="xml">
    </el-input>
    <el-form :inline="true" style="padding: 10px">
      <el-form-item label="总任务数">
        <el-input v-model="totalCount" readonly></el-input>
      </el-form-item>
      <el-form-item label="已完成任务数">
        <el-input v-model="finishCount" readonly></el-input>
      </el-form-item>
      <el-form-item label="发送消息SendCode与接收消息ReceiveCode不等数量">
        <el-input v-model="errorCount" readonly></el-input>
      </el-form-item>
    </el-form>

    <div class="reception">
      <el-scrollbar style="width: 1920px;height: 500px;">
        <div v-for="item in textReceptionList" class="text-reception" v-text="item.name"></div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script>
export default {
  name: "BatchSend",
  data() {
    return {
      ip: "172.24.39.9",
      ftpPort: 10012,
      username: "Yzzx220",
      password: "Yzzx@220901",
      filePath: "d://1.jpg",
      remoteFilename: "",
      keyPw: "1",
      socketPort: 10011,
      threadCount: 1,
      taskCount: 2,
      xml: '<?xmlversion="1.0"encoding="UTF-8"?>\n' +
          '<PatrolDevice>\n' +
          '\t<SendCode>Edge01</SendCode>\n' +
          '\t<ReceiveCode>Region01</ReceiveCode>\n' +
          '\t<Type>251</Type>\n' +
          '\t<Code>200</Code>\n' +
          '\t<Command></Command>\n' +
          '\t<Time>2022-07-22 14:36:36</Time>\n' +
          '\t<Items>\n' +
          '\t\t<Item/></Items>\t\n' +
          '</PatrolDevice >',
      textReceptionList: [],
      errorCount: "",
      finishCount: "",
      totalCount: ""
    }
  },
  mounted: function () {
    this.resetRemoteFileName();
  },
  methods: {
    batchSendTest() {
      this.$http.windPost(`/demo/demo-client-batch/batchsendtest`, {
        xml: this.xml,
        ip: this.ip,
        ftpPort: this.ftpPort,
        username: this.username,
        password: this.password,
        filePath: this.filePath,
        remoteFilename: this.remoteFilename,
        keyPw: this.keyPw,
        socketPort: this.socketPort,
        threadCount: this.threadCount,
        taskCount: this.taskCount
      }).then(res => {
      })
    },
    queryResult() {
      this.$http.windGet(`/demo/demo-client-batch/queryResult`, {}).then((res) => {
        console.log(res)
        this.errorCount = res.errorCount;
        this.totalCount = res.totalCount;
        this.finishCount = res.finishCount;
      })
    },
    resetData() {
      this.xml = '<?xmlversion="1.0"encoding="UTF-8"?>\n' +
          '<PatrolDevice>\n' +
          '\t<SendCode>Client01</SendCode>\n' +
          '\t<ReceiveCode>Server01</ReceiveCode>\n' +
          '\t<Type>251</Type>\n' +
          '\t<Code>200</Code>\n' +
          '\t<Command></Command>\n' +
          '\t<Time>2022-07-22 14:36:36</Time>\n' +
          '\t<Items>\n' +
          '\t\t<Item/></Items>\t\n' +
          '</PatrolDevice >';
    },
    resetRemoteFileName() {
      this.remoteFilename = getTimeStr() + "/1.jpg";
    }
  }
}

function getTimeStr() {
  let date = new Date();
  return date.getFullYear().toString() + formate(date.getMonth() + 1) + formate(date.getDate()) + formate(date.getHours()) + formate(date.getMinutes()) + formate(date.getSeconds());
}

function formate(n) {
  return n < 10 ? '0' + n : n;
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
    height: 50px;
    background-color: #FFFFFF;
    padding: 8px 0 7px;
    border-bottom: 4px double #d1d1d1;
    color: black;
    font-size: 16px;
  }
}
</style>