<template>
  <div class="bg">
    <div class="send">
      <el-form :inline="true" style="padding: 10px; text-align: left">
        <el-form-item>
          <el-button type="primary" size="small" @click="onConnect">WebSocket重连</el-button>
          <span style="margin-left: 20px; color: #FFFFFF; font-size: 16px">websocket IP: {{ this.wsIp }}</span>
          <span style="color: #FFFFFF; font-size: 16px; margin-left: 20px;">{{ this.isClient ? '客户端' : '服务端' }} IP: {{
              this.ip
            }}</span>
          <span style="color: #FFFFFF; font-size: 16px; margin-left: 20px;">{{ this.isClient ? '客户端' : '服务端' }} 端口: {{
              this.port
            }}</span>
          <span style="color: #FFFFFF; font-size: 16px; margin-left: 20px;">sendCode: {{ this.sendCode }}</span>
          <span style="color: #FFFFFF; font-size: 16px; margin-left: 20px;">receiveCode: {{ this.receiveCode }}</span>
        </el-form-item>
        <br/>
        <el-form-item class="top-option" label="协议类型">
          <el-select v-model="stationType" placeholder="请选择" @change="selectStationType">
            <el-option v-for="(item, index) in stationTypeList"
                       :key="index" :label="item.label"
                       :value="item.label">
            </el-option>
          </el-select>
          <el-select v-model="platformType" placeholder="请选择" @change="getProtocolTextTree">
            <el-option v-for="(item, index) in platformTypeList"
                       :key="index" :label="item.label"
                       :value="item.label">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="establishClient">创建客户端</el-button>
          <el-button size="small" @click="destroyClient">销毁客户端</el-button>
          <el-button size="small" type="primary" @click="establishServer">创建服务端</el-button>
          <el-button size="small" @click="destroyServer">销毁服务端</el-button>
          <el-button size="small" @click="resetData">重置发送报文</el-button>
          <el-button size="small" @click="resetTextReception">重置接收报文</el-button>
          <el-button size="small" type="primary" @click="gotoBatchTask">并发测试</el-button>
        </el-form-item>
        <el-form-item>
          <span style="color: #FFFFFF; font-size: 16px;">是否自动回复：</span>
          <el-switch
              style="margin-left: 12px;"
              v-model="autoReply"
              active-text="开启"
              inactive-text="关闭"
              inactive-color="#FFFFFF"
              @change="autoReplyHandle">
          </el-switch>
        </el-form-item>
        <br/>
        <el-form-item>
          <el-button size="small" type="primary" @click="startAutoTest">自动检测</el-button>
          <el-button size="small" type="primary" @click="clientSend">客户端发送消息</el-button>
          <el-button size="small" type="primary" @click="severSend">服务端发送消息</el-button>
          <label style="color: #FFFFFF; margin:0 15px">MsgId:</label>
          <el-input style="display: inline-block; width: 200px" v-model="msgId"></el-input>
        </el-form-item>
        <br/>
      </el-form>
    </div>
    <div style="display: flex">
      <div style="display: inline-block; width: 20%;">
        <div style="height: 350px; width: 100%;">
          <el-tree ref="messageTree" :data="treeData" :style="{ maxHeight: '350px', overflow: 'auto' }"
                   @node-click="handleNodeClick">
          </el-tree>
        </div>
      </div>
      <div style="display: inline-block; width: 79%;">
        <div style="height: 350px; width: 100%">
          <el-input
              type="textarea"
              :rows="16"
              placeholder="请输入报文"
              v-model="textarea">
          </el-input>
        </div>
      </div>
    </div>

    <div class="reception">
      <el-scrollbar style="width: 100%;height: 400px;">
        <div v-for="(item, index) in textReceptionList" :key="index" class="text-reception">
          <div v-text="item.name"></div>
          <div v-html="item.tips"></div>
        </div>
      </el-scrollbar>
    </div>

    <el-dialog :title="operationName" :visible="visibleFlag">
      <el-form v-model="submitForm" :inline="true">
        <el-form-item label="ip" v-if="operationName === '创建客户端'">
          <el-input v-model="submitForm.ip"></el-input>
        </el-form-item>
        <el-form-item label="端口">
          <el-input v-model="submitForm.port" style="width:80px"></el-input>
        </el-form-item>
        <el-form-item label="sendCode">
          <el-input v-model="submitForm.sendCode" style="width:80px"></el-input>
        </el-form-item>
        <el-form-item label="receiveCode" v-if="operationName === '创建客户端'">
          <el-input v-model="submitForm.receiveCode" style="width:80px"></el-input>
        </el-form-item>
      </el-form>
      <el-footer>
        <el-button type="primary" @click="create(operationName)">创建</el-button>
        <el-button @click="cancel">取消</el-button>
      </el-footer>
    </el-dialog>
  </div>
</template>
<script>
import websocket from "@/utils/websocket";

export default {
  name: 'toolIndex',
  data() {
    return {
      operationName: '创建客户端',
      isClient: true,
      wsIp: 'ws://' + window.location.host + '/demo/message',
      ws_platform: null,
      textarea: '<?xmlversion="1.0"encoding="UTF-8"?>\n' +
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
      textReceptionList: [],
      visibleFlag: false,
      submitForm: {
        ip: '',
        port: '',
        sendCode: '',
        receiveCode: ''
      },
      ip: '',
      port: '',
      sendCode: '',
      receiveCode: '',
      stationType: '',
      platformType: '',
      stationTypeList: [],
      platformTypeList: [],
      messageTreeData: [],
      protocolTreeData: [],
      treeData: [],
      serverFlag: '',
      autoReply: true,
      msgId: '',
    }
  },
  mounted() {
    this.onConnect()
    this.getProtocolTree()
  },
  methods: {
    onConnect() {
      this.ws_platform = new websocket({
        wxurl: this.wsIp
      })
      this.ws_platform.onmessageWS((msgData) => {
        if (msgData == '连接成功') return
        let oo = JSON.parse(msgData);
        this.textReceptionList.unshift({name: oo.data.xml, tips: oo.data.tips});
      })
    },
    autoReplyHandle(val) {
      this.autoReply = val
      this.$http.windPost(`/demo/automation/autoReply?autoReply=` + this.autoReply).then(res => {
        if (res.code == 200) {
          this.$message.success('切换成功!!!');
        }
      })
    },
    getProtocolTree() {
      this.$http.windGet(`/demo/automation/protocolTree`).then(res => {
        this.protocolTreeData = res.data
        this.stationTypeList = this.protocolTreeData.map(item => ({label: item.label}));
      })
    },
    selectStationType(label) {
      // 当第一级分类变化时，根据选中的第一级分类筛选出第二级分类的选项
      const selectedFirst = this.protocolTreeData.find(item => item.label === label);
      if (selectedFirst && selectedFirst.children) {
        this.platformTypeList = selectedFirst.children.map(child => ({label: child.label}));
      } else {
        // 如果没有子分类，则清空第二级选项
        this.platformTypeList = [];
      }
    },
    getProtocolTextTree() {
      this.$http.windGet(`/demo/automation/protocolTree?station=${this.stationType}&platform=${this.platformType}`).then(res => {
        this.treeData = []
        this.treeData = this.addParentRefs(res.data)
      })
    },
    addParentRefs(treeData, parent = null) {
      if (treeData.length ==0) {
        return []
      }
      return treeData.map((node) => {
        const newNode = {...node, parent};
        if (node.children && Array.isArray(node.children)) {
          newNode.children = this.addParentRefs(node.children, node.label); // 使用当前节点ID作为子节点的parentId
        }
        return newNode;
      });
    },
    handleNodeClick(node) {
      if (this.serverFlag === '') {
        this.$message.error("请先创建客户端或服务端！")
        return
      }
      const param = "?isServer=" + this.serverFlag +
          "&message=" + node.label +
          "&platform=" + this.platformType +
          "&protocol=" + node.parent +
          "&station=" + this.stationType
          this.$http.windGet(`/demo/automation/messageContent${param}`).then(res => {
            this.textarea = res.data
          })
    },
    create() {
      const data = "?ip=" + this.submitForm.ip +
          "&port=" + this.submitForm.port +
          "&sendCode=" + this.submitForm.sendCode +
          "&receiveCode=" + this.submitForm.receiveCode +
          "&station=" + this.stationType +
          "&platform=" + this.platformType
      let info = JSON.parse(JSON.stringify(this.submitForm))
      if (this.operationName == '创建客户端') {
        this.$http.windPost(`/demo/demo-client/create${data}`).then(res => {
          if (res.code == 200) {
            this.$message.success('创建成功!!!');
            this.ip = info.ip
            this.port = info.port
            this.sendCode = info.sendCode
            this.receiveCode = info.receiveCode
            this.serverFlag = 0
            localStorage.setItem('ip', this.ip)
            localStorage.setItem('port', this.port)
            localStorage.setItem('client-sendCode', this.sendCode)
            localStorage.setItem('client-receiveCode', this.receiveCode)
          }
        })
      } else {
        this.$http.windPost(`/demo/demo-server/create${data}`).then(res => {
          if (res.code == 200) {
            this.$message.success('创建成功!!!');
            this.ip = info.ip
            this.port = info.port
            this.sendCode = info.sendCode
            this.receiveCode = info.receiveCode
            this.serverFlag = 1
            localStorage.setItem('server-sendCode', this.sendCode)
            localStorage.setItem('server-receiveCode', this.receiveCode)
          }
        })
      }
      this.cancel()
    },
    cancel() {
      this.visibleFlag = false
    },
    establishClient() {
      this.operationName = '创建客户端'
      this.visibleFlag = true
      this.submitForm = {
        ip: localStorage.getItem('ip'),
        port: localStorage.getItem('port'),
        sendCode: localStorage.getItem('client-sendCode'),
        receiveCode: localStorage.getItem('client-receiveCode')
      }
    },
    destroyClient() {
      this.$http.windPost('/demo/demo-client/destroy').then(res => {
        if (res.code) {
          this.isClient = true
          this.serverFlag = ''
          this.ip = ''
          this.port = ''
          this.sendCode = ''
          this.receiveCode = ''
          this.$message.success('销毁成功!!!')
        }
      })
    },
    establishServer() {
      this.operationName = '创建服务端'
      this.visibleFlag = true
      this.submitForm = {
        ip: '',
        port: '',
        sendCode: localStorage.getItem('server-sendCode'),
        receiveCode: localStorage.getItem('server-receiveCode')
      }
    },
    destroyServer() {
      this.$http.windPost('/demo/demo-server/destroy').then(res => {
        if (res.code == 200) {
          this.isClient = true
          this.serverFlag = ''
          this.ip = ''
          this.port = ''
          this.sendCode = ''
          this.receiveCode = ''
          this.$message.success('销毁成功!!!');
        }
      })
    },
    startAutoTest() {
      if (this.serverFlag === '') {
        this.$message.error("请先创建客户端")
        return
      }
      this.$http.windPost('/demo/automation/startAutotest?server=' + this.serverFlag, {}).then(res => {
        if (res.code == 200) {
          this.$message.success('发送成功!!!');
        }
      })
    },
    clientSend() {
      if (this.serverFlag === '' || this.serverFlag === 1) {
        this.$message.error("请先创建客户端")
        return
      }
      this.$http.windPost(`/demo/demo-client/out-msg`, {
        xml: this.textarea,
        msgId: this.msgId
      }).then(res => {
        if (res.code == 200)
          this.$message.success('发送成功!!!')
      })
    },
    severSend() {
      if (this.serverFlag === '' || this.serverFlag === 0) {
        this.$message.error("请先创建服务端")
        return
      }
      this.$http.windPost(`/demo/demo-server/out-msg`, {
        xml: this.textarea,
        msgId: this.msgId
      }).then(res => {
        if (res.code == 200)
          this.$message.success('发送成功!!!')
      })
    },
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
    },
    gotoBatchTask() {
    },
  }
}
</script>

<style scoped>
.top-option ::v-deep .el-form-item__label {
  color: #FFFFFF !important;
  font-size: 16px;
}

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
  margin-top: 5px;

  .text-reception {
    white-space: pre-wrap;
    text-align: left;
    width: 100%;
    height: auto;
    background-color: #FFFFFF;
    padding: 8px 0 7px;
    border-bottom: 4px double #d1d1d1;
    color: black;
    font-size: 16px;
  }
}

::v-deep .el-switch__label {
  color: #FFFFFF;
}

::v-deep .el-form-item {
  margin-bottom: 5px;
}
</style>
