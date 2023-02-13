<template>
  <div class="bg">
    <div class="send">
      <div class="connect">
        <el-form :inline="true" style="padding: 10px">
          <el-form-item label="IP:">
            <el-input v-model="wsIp"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onConnect">连接</el-button>
<!--            <el-button type="primary" @click="onSend">发送报文</el-button>-->
          </el-form-item>
          <el-form-item label="客户端IP">
            <el-input v-model="clientIp"></el-input>
          </el-form-item>
          <el-form-item label="客户端端口">
            <el-input v-model="clientPort"></el-input>
          </el-form-item>
          <el-form-item >
            <el-button type="primary" @click="establishClient">创建客户端</el-button>
            <el-button type="primary" @click="destroyClient">销毁客户端</el-button>
            <el-button type="primary" @click="clientSend">客户端发送消息</el-button>
            <el-button type="primary" @click="severSend">服务端发送消息</el-button>
            <el-button type="primary" @click="resetData">重置发送报文</el-button>
            <el-button type="primary" @click="resetTextReception">重置接收报文</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-input
          type="textarea"
          :rows="14"
          placeholder="请输入报文"
          v-model="textarea">
      </el-input>
    </div>
    <div class="reception">
      <el-scrollbar style="width: 100%;">
      <div v-for="item in textReceptionList" class="text-reception" v-text="item.name"></div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script>
import websocket from "@/utils/websocket";

export default {
  name: "test",
  data() {
    return {
      wsIp:'ws://127.0.0.1:18088/demo/message',
      ws_platform: null,
      textarea:'<?xmlversion="1.0"encoding="UTF-8"?>\n' +
          '<PatrolDevice>\n' +
          '\t<SendCode>Client01</SendCode>\n' +
          '\t<ReceiveCode>Server01</ReceiveCode>\n' +
          '\t<Type>251</Type>\n' +
          '\t<Code>200</Code>\n' +
          '\t<Command></Command>\n' +
          '\t<Time>2022-07-22 14:36:36</Time>\n' +
          '\t<Items>\n' +
          '\t\t<Item/></Items>\t\n' +
          '</PatrolDevice >',
      textReceptionList:[],
      clientIp:'',
      clientPort:'',
    }
  },
  methods:{
    onConnect() {
      this.ws_platform = new websocket({
        wxurl: this.wsIp
      })
      this.ws_platform.onmessageWS((msgData) => {
        if (msgData == '连接成功') return
        let oo = JSON.parse(msgData);
        this.textReceptionList.push({name:oo.data.xml});
      })
    },
    onSend() {
    },
    /**
     * 创建客户端
     */
    establishClient() {
      const data = "?ip="+this.clientIp+"&port=" + this.clientPort
      this.$http.windPost(`/demo/demo-client/create${data}`).then(res => {
        this.$message.success('创建成功!!!');
      })
    },
    /**
     * 销毁客户端
     */
    destroyClient() {
      this.$http.windPost('/demo/demo-client/destroy').then(res => {
        this.$message.success('创建成功!!!');
      })
    },
    /**
     * 客户端发送消息
     */
    clientSend() { //?xml=${this.textarea}
      this.$http.windPost(`/demo/demo-client/out-msg`,{
        xml:this.textarea
      }).then(res => {
        this.$message.success('发送成功!!!')
      })
    },
    /**
     * 服务端发送消息
     */
    severSend() {
      this.$http.windPost(`/demo/demo-server/out-msg`,{
        xml: this.textarea
      }).then(res => {
        this.$message.success('发送成功!!!')
      })
    },
    /**
     * 重置
     */
    resetData() {
      this.textarea = '<?xmlversion="1.0"encoding="UTF-8"?>\n' +
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
    resetTextReception() {
      this.textReceptionList = [];
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
  height: 318px;
  border: 1px solid #0AAFB7;
  .connect {
    width: 100%;
    height: 50px;
  }
}
.reception {
  width: 100%;
  height: calc(100vh - 324px);
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