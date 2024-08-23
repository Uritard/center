<template>
    <div class="page-title">
        <div class="page-logo">

        </div>
        <div class="page-text">设备健康度管理系统</div>
        <div class="page-option">
            <div style="display: inline-block;" v-show="isSalesman">
                <el-badge :value="alarmCount" :min="0" :max="99" class="item">
                    <i class="el-icon-bell alarm-img" @click="alarmBtnClick"></i>
                </el-badge>
            </div>

            <div v-popover:popover1 class="user-text" :title="user">{{user.length>0?user.substring(0,1):''}}</div>
        </div>
        <el-popover ref="popover1" width="150" height="190" trigger="click" v-model="closePop">
            <div class="pop-window">
                <div class="box-content">
                    <div @click="isSystem=true" :class="isSystem?'isActive':''">账号设置 ></div>
                    <div style="height: 1px;background: linear-gradient(90deg, rgba(12, 79, 86, 0), rgba(79, 195, 202, 0.99), rgba(12, 79, 86, 0));"></div>
                    <div @click="system_message()">系统设置</div>
                </div>
                <div class="box-content" v-if="isSystem" style="border-left: 2px solid #3AE4EF">
                    <div @click="show_quit_system=true">退出登录</div>
                    <div style="height: 1px;background: linear-gradient(90deg, rgba(12, 79, 86, 0), rgba(79, 195, 202, 0.99), rgba(12, 79, 86, 0));" ></div>
                    <div @click="btnCLick('changePassword')" >修改密码</div>
                </div>
            </div>



            <div class="pop-window" style="display: none">
                <div class="tabs-title">
                    <span :class="[isSystem?'':'isActive','title-text']" @click="isSystem=false">用 户</span>
                    <span :class="[isSystem?'isActive':'','title-text']" @click="isSystem=true">系 统</span>
                </div>
                <div v-if="!isSystem" style="margin-top: 16px;">
                    <div style="display: flex;justify-content:space-between;margin: 4px 6px;">
                        <span style="">用户：{{user}}</span>
<!--                        <span>角色：{{role}}</span>-->
                    </div>
                    <div style="text-align: left;margin: 4px 6px;">所属公司：{{company || ''}}</div>
                    <!--                    <div style="text-align: left;margin: 4px 6px;">所属部门：{{department}}</div>-->
<!--                    <div class="lineH"></div>-->
                    <div class="sys-item-low passwordImg" @click="btnCLick('changePassword')">修改密码</div>
                    <div class="lineH"></div>
                    <div class="sys-item-low exitSystemImg" @click="btnCLick('exitSystem')">退出系统</div>
                </div>
                <div v-else style="margin-top: 22px;margin-left: 32px;">
                    <!--<div class="sys-item systemUpdateImg" @click="btnCLick('systemUpdate')">系统升级</div>-->
                    <div class="sys-item userManualImg" @click="btnCLick('preview')">用户手册</div>
                    <div class="lineH"></div>
                    <div class="sys-item contactImg" @click="btnCLick('contact')">联系方式</div>
                    <div class="lineH"></div>
                    <div class="sys-item aboutUsImg" @click="btnCLick('aboutUs')">关于我们</div>
                </div>
            </div>
        </el-popover>
        <el-dialog :title="winTitle" :close-on-click-modal="false" :visible.sync="dialogVisible" width="46%" top="20vh">
            <!--系统升级  弹框-->
            <template v-if="isShowType=='first'">
                <el-row>
                    <el-col :span="11" style="text-align: center;padding: 20px 12px;">
                        <div class="system-text">变压器声纹识别系统</div>
                        <div class="system-update"></div>
                        <div class="system-logo"></div>
                    </el-col>
                    <el-col :span="2" style="text-align: center;">
                        <div class="v-line"></div>
                    </el-col>
                    <el-col :span="11" style="text-align: center;padding: 20px 12px;">
                        <br><br>
                        <el-button class="button-update" @click="systemUpdate">立即升级</el-button>
                        <p style="padding-top: 28px;color: #fff;line-height: 26px;">更新时间：{{updateTime}}</p>
                        <p style="padding-bottom: 28px;color: #fff;line-height: 26px;">更新版本：{{updateVersion}}</p>
                        <div style="padding: 4px 28px;text-align: left;color: #4EB6C1;">更新说明：</div>
                        <p v-for="(item,index) in infoList" :key="index"
                           style="padding-left: 28px;text-align: left;color: #4EB6C1;line-height: 22px;">
                            {{++index}}.{{item}}</p>
                    </el-col>
                </el-row>
            </template>
            <!--联系方式  弹框-->
            <template v-else-if="isShowType=='second'">
                <div class="about-us">
                    <el-row>
                        <el-col :span="12" style="padding: 4px 46px;">
                            <div class="ico-common ico-phone"></div>
                            <div class="ico-r-text" style="">电话<br><span>025-83168166</span></div>
                        </el-col>
                        <el-col :span="12">
                            <div class="ico-common ico-postcode"></div>
                            <div class="ico-r-text" style="">邮编<br><span>210012</span></div>
                        </el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="12" style="padding: 4px 46px;">
                            <div class="ico-common ico-email"></div>
                            <div class="ico-r-text" style="">邮箱<br><span>yijiahe@yijiahe.com</span></div>
                        </el-col>
                        <el-col :span="12">
                            <div class="ico-common ico-fax"></div>
                            <div class="ico-r-text" style="">传真<br><span>025-83168160</span></div>
                        </el-col>
                    </el-row>
                    <div style="padding: 4px 46px;">
                        <div class="ico-common ico-address"></div>
                        <div class="ico-r-text" style="">总部地址<br><span>南京市雨花台区大周路32号 软件谷科创城 B3栋</span></div>
                    </div>
                </div>
            </template>
            <!--关于我们  弹框-->
            <template v-else>
                <div class="about-us">
                    <p style="text-indent:2em;padding:6px 20px;line-height: 24px;">
                        亿嘉和科技股份有限公司（股票简称：亿嘉和，股票代码：603666）成立于1999年，是机器人领域
                        的高新技术企业，公司专注于电力、消防等行业特种机器人的研发、制造和推广应用。亿嘉和总部
                        位于中国南京，研发中心及分支机构遍布美国、香港、深圳、广东松山湖等地。</p>
                    <p style="text-indent:2em;padding:6px 20px;line-height: 24px;">
                        亿嘉和在坚持自主研发创新、提升产品质量的同时，结合客户的痛点与市场需求，从多维度完善技
                        术与产品，致力于为电力、消防、轨道交通等更多行业提供更为完备的智能化解决方案！公司将继
                        续秉承“应用智能科技，改善人类生活”的企业使命，向 “世界一流机器人公司”迈进 ！</p>
                </div>
            </template>
        </el-dialog>
        <el-dialog title="修改密码" :visible.sync="showChangePasswordWin" width="36%" top="30vh"
                   :close-on-click-modal="false">
            <div style="width:100%;height:88%;padding: 20px;">
                <el-form ref="passwordInfoForm" :model="passwordInfo" :rules="rules">
                    <el-form-item label="旧密码" label-width="25%" prop="oldPCode">
                        <el-input type="password" v-model="passwordInfo.oldPCode" autocomplete="off"
                                  style="width: 70%;"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码" label-width="25%" prop="newPCode">
                        <el-input type="password" v-model="passwordInfo.newPCode" autocomplete="off"
                                  style="width: 70%;"></el-input>
                    </el-form-item>
                    <el-form-item label="再次输入" label-width="25%" prop="againPCode">
                        <el-input type="password" v-model="passwordInfo.againPCode" autocomplete="off"
                                  style="width: 70%;"></el-input>
                    </el-form-item>
                </el-form>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click="submit">确 定</el-button>
                <el-button type="primary" @click="cancel">取 消</el-button>
            </div>
        </el-dialog>
        <el-dialog title="提示" :visible.sync="show_quit_system" width="36%" top="30vh"
                   :close-on-click-modal="false">
            <div  class="exit-title">
                <i class="el-icon-warning"></i>
                是否退出当前账号?
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click="btnCLick('exitSystem')">确定</el-button>
                <el-button type="primary" @click="exit">取消</el-button>
            </div>
        </el-dialog>
        <el-dialog title="系统设置" :visible.sync="show_system_install" width="18%" top="30vh"
                   :close-on-click-modal="false">
            <div style="width:100%;height:100%;padding: 20px;">
                <el-form ref="passwordInfoForm" :model="version_information">
                    <el-form-item label="软件版本" label-width="35%" >
                        <el-input type="text" v-model="version_information.software" autocomplete="off"
                                  style="width: 70%;" readonly="true"></el-input>
                    </el-form-item>
                    <el-form-item label="算法软件版本" label-width="35%" >
                        <el-input type="text" v-model="version_information.arithmetic" autocomplete="off"
                                  style="width: 70%;" readonly="true"></el-input>
                    </el-form-item>
                    <el-form-item label="操作手册" label-width="35%">
                        <el-button type="primary" style="width: 70%" @click="btnCLick('preview')">操作手册</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </el-dialog>

    </div>
</template>
<script>
    export default {
        name: 'headerComponent',
        data() {
            return {
                isSalesman: false,
                closePop: false,
                isSystem: false,
                user: '',
                role: '',
                company: '国家电网',
                department: '',
                winTitle: '',
                dialogVisible: false,
                isShowType: 'first',
                updateTime: '2020-07-06 12:12:12',
                updateVersion: 'v1.0.0_release',
                infoList: ['修复个别BUG', '声纹接入功能优化', '数据采集功能优化', '配置逻辑优化'],
                showChangePasswordWin: false,
                show_quit_system:false,// 退出登录弹窗控制
                show_system_install:false,// 系统设置弹窗控制
                passwordInfo: {
                    oldPCode: '',
                    newPCode: '',
                    againPCode: ''
                },
                rules: {
                    oldPCode: [{required: true, message: '请输入密码', trigger: 'blur'},
                        // {min: 8, max: 18, message: '长度在 8 到 18 个字符', trigger: 'blur'}
                        ],
                    newPCode: [{required: true, message: '请输入密码', trigger: 'blur'},
                        {min: 8, max: 16, message: '长度在 8 到 16 个字符', trigger: 'blur'},
                        {
                            validator: (rule, value, callback) => {
                                this.$RegExp.formItemValidate(value, 'PWD', callback);
                            }, trigger: 'blur'
                        }],
                    againPCode: [{required: true, message: '请输入密码', trigger: 'blur'},
                        {min: 8, max: 16, message: '长度在 8 到 16 个字符', trigger: 'blur'},
                        {
                            validator: (rule, value, callback) => {
                                this.$RegExp.formItemValidate(value, 'PWD', callback);
                            }, trigger: 'blur'
                        }]
                },
                alarmCount: '',
                version_information: {
                    software:'',
                    arithmetic:''
                },
                dialogVisibleUpdate: false,
                fileList:[], // 上传文件数组
            }
        },
        destroyed() {

        },
        mounted() {
            this.$nextTick(() => {
                this.user = sessionStorage.userName || '';
                // this.role = sessionStorage.roleName;
                // this.company = sessionStorage.companyName;
                // this.getAlarmCount();
            })
            this.$bus.$on('newAlarm', (obj) => {
                this.getAlarmCount()
            })
            this.$bus.$on('finishedOneAlarm', (obj) => {
                this.getAlarmCount()
            })
        },
        methods: {
            exit(){
              this.show_quit_system = false;
            },
            /**
             * 告警图标点击事件
             * */
            alarmBtnClick: function () {
                this.$router.push({path: '/scout/resultConfirm', query: {activeName: '告警确认', confirmType: '267'}});
                this.$utils.exitFullScreen();
                this.$bus.$emit('fullScreen', {fullscreen: false});
            },
            /**
             * 获取告警总数（未处理告警树）
             * */
            getAlarmCount() {
                this.alarmCount = ''
                this.$http.windGet('/apis/tWarnInfo/v1/warnCountsNonIdentify', {}).then((res) => {
                    this.alarmCount = res.data.count
                })
            },

            /**
             * 按钮点击事件处理
             * */
            btnCLick: function (type) {
                this.closePop = false
                switch (type) {
                    case 'changePassword':
                        this.showChangePasswordWin = true
                        break;
                    case 'exitSystem':
                        this.$http.post('/apis/sysUser/v1/logout').then((res) => {
                            sessionStorage.clear();
                            this.$router.push('/login')
                        })
                        break;
                    case 'systemUpdate':
                        this.winTitle = '系统升级'
                        this.dialogVisible = true;
                        this.isShowType = 'first'
                        break;
                    case 'contact':
                        this.winTitle = '联系方式'
                        this.dialogVisible = true;
                        this.isShowType = 'second'
                        break;
                    case 'aboutUs':
                        this.winTitle = '关于我们'
                        this.dialogVisible = true;
                        this.isShowType = 'three'
                        break;
                    case 'preview':
                        // 先下载
                        window.open(`${process.env.BASE_URL}/static/files/userManual.docx`)
                        break;
                    default:
                        break;
                }
            },
            /**
             *
             */
             system_message() {
                 this.show_system_install = true;
                 this.$http.get('/apis/sysUser/v1/getSystemSetting').then((res)=>{
                     if(res.data.code === 200) {
                         this.version_information.software = res.data.data.softwareVersion;
                         this.version_information.arithmetic = res.data.data.algorithmVersion;
                     }
                 })
            },
            /**
             * 修改密码
             */
            submit() {
                this.$refs.passwordInfoForm.validate((valid) => {
                    if (valid) {
                        if (this.passwordInfo.newPCode == this.passwordInfo.oldPCode) {
                            this.$message.error('新密码与旧密码一致，请重新输入！')
                            return
                        }
                        if (this.passwordInfo.newPCode != this.passwordInfo.againPCode) {
                            this.$message.error('新密码两次输入不一致！')
                            return
                        }
                        this.$http.put('/apis/sysUser/v1/changePassword', {
                            'oldPCode': this.passwordInfo.oldPCode,
                            'newPCode': this.passwordInfo.newPCode,
                            'againPCode': this.passwordInfo.againPCode
                        }).then((res) => {
                            // 请求成功之后，关闭弹框
                            this.$refs.passwordInfoForm.resetFields();
                            this.showChangePasswordWin = false;
                            this.$http.post('/apis/sysUser/v1/logout').then((res) => {
                                sessionStorage.clear();
                                this.$router.push('/login');
                            })
                        })
                    }
                })

            },

            /**
             * 取消
             */
            cancel() {
                // 清空数据
                this.$refs.passwordInfoForm.resetFields();
                this.showChangePasswordWin = false;
            },

            /**
             * 系统升级
             * */
            systemUpdate() {
                this.$message('功能实现中……')
            },
        },
        created() {
        }
    }
</script>
<style lang="less" scoped>
    .page-title {
        display: flex;
        width: 100%;
        height: 100%;
        background: url("~@/assets/header/header_bg.png") center top no-repeat;
        background-size: 100% 80%;

        .page-logo {
            flex: 1;
            background: url("~@/assets/header/logo_gw_text.png") 20px 26% no-repeat;
            height: 100%;
            display: flex;
            flex-direction: row;
        }

        .page-text {
            flex: 0 0 50%;
            padding-top: 6px;
            text-align: center;
            color: #fff;
            font: 30px 'SourceHanSansCN-Heavy';
            font-weight: bold;
            letter-spacing: 7px;
        }

        .page-option {
            flex: 1;
            padding: 8px 12px;
            text-align: right;

            .item {
                margin-top: 10px;
                margin-right: 40px;
            }

            .alarm-img {
                font-size: 32px;
                color: #fff;
                cursor: pointer;
            }

            .user-text {
                display: inline-block;
                width: 40px;
                height: 40px;
                border-radius: 50%;
                line-height: 40px;
                vertical-align: middle;
                text-align: center;
                color: #F0FFE7;
                font-size: 18px;
                font-weight: bold;
                border: 3px solid #54BABE;
            }

            .user-text:hover {
                cursor: pointer;
            }
        }

    }

    .pop-window {
        margin: 0;
        text-align: center;
        display: flex;
        justify-content: center;
        align-items: center;

        .box-content{
            width: 100%;
            height: 100%;
            cursor: pointer;
            .box-content>div{
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 14px;
                font-family: Source Han Sans CN;
                font-weight: 400;
                cursor: pointer;
                color: #FFFFFF;
            }
            .isActive {
                color: #71EFF5;
                /*border-bottom: 1px solid #6BEFF3;*/
            }
        }


        .tabs-title {
            width: 100%;
            text-align: center;
            border-bottom: 2px solid #1b7880;

            .title-text {
                display: inline-block;
                width: 42%;
                height: 22px;
                padding: 0 6px;
                margin: 0 2px;
                cursor: pointer;
            }

            .isActive {
                color: #71EFF5;
                border-bottom: 1px solid #6BEFF3;
            }

        }

        .lineH {
            width: 100%;
            height: 2px;
            background-color: #17686E;
        }

        .sys-item {
            margin: 8px 2px;
            font-family: SourceHanSansCN-Regular;
            font-size: 14px;
            color: #d89623;
        }

        .sys-item:hover {
            cursor: pointer;
        }

        .sys-item-low {
            margin: 4px 2px;
            font-family: SourceHanSansCN-Regular;
            font-size: 14px;
            color: #d89623;
        }

        .sys-item-low:hover {
            cursor: pointer;
        }

        .passwordImg {
            background: url("~@/assets/header/change_password.png") 28% center no-repeat;
        }

        .exitSystemImg {
            background: url("~@/assets/header/exit_system.png") 28% center no-repeat;
        }

        .systemUpdateImg {
            background: url("~@/assets/header/system_update.png") 24% center no-repeat;
        }
        .userManualImg {
            background: url("~@/assets/header/user_manual.png") 24% center no-repeat;
        }

        .contactImg {
            background: url("~@/assets/header/contact.png") 24% center no-repeat;
        }

        .aboutUsImg {
            background: url("~@/assets/header/about_us.png") 24% center no-repeat;
        }
    }

    .system-text {
        width: 100%;
        height: 60px;
        font-family: 'ReeJi-BigRuixain-BoldGBV1.0-Regular';
        font-size: 32px;
        font-weight: bold;
        letter-spacing: 2px;
        color: #ffffff;
        text-align: center;
    }

    .system-update {
        width: 100%;
        height: 240px;
        background: url("~@/assets/header/update_bg.png") center center no-repeat;
        background-size: 100% 100%;
    }

    .system-logo {
        width: 100%;
        height: 60px;
        background: url("~@/assets/header/logo_gw_text.png") center center no-repeat;
        background-size: 40% 66%;
    }

    .v-line {
        height: 340px;
        width: 1px;
        background-image: linear-gradient(0deg, #0c4f56 0%, #3ae4ef 50%, #0c4f56 100%,);
    }

    .button-update {
        width: 330px;
        height: 56px;
        background-color: #0c4f56;
        border-radius: 31px;
        border: solid 3px #3ae4ef;
        color: #fff;
        font-family: SourceHanSansCN-Regular;
        font-size: 18px;
    }

    .about-us {
        width: 100%;
        height: 100%;
        padding-bottom: 220px;
        background: #0C4F56 url("~@/assets/header/about_us_bg.png") center bottom no-repeat;
        background-size: 100% 60%;
        color: #fff;
        font-family: SourceHanSansCN-Bold;
        font-size: 20px;
        letter-spacing: 1px;

        .ico-common {
            display: inline-block;
            width: 60px;
            height: 60px;
            border-radius: 50%;
            border: 2px solid #0AAFB7;
        }

        .ico-r-text {
            display: inline-block;
            line-height: 30px;
            margin-left: 12px;
            vertical-align: top;

            span {
                font-size: 16px;
            }
        }

        .ico-phone {
            background: url("~@/assets/header/ico_phone.png") center center no-repeat;
        }

        .ico-postcode {
            background: url("~@/assets/header/ico_postcode.png") center center no-repeat;
        }

        .ico-email {
            background: url("~@/assets/header/ico_email.png") center center no-repeat;
        }

        .ico-fax {
            background: url("~@/assets/header/ico_fax.png") center center no-repeat;
        }

        .ico-address {
            background: url("~@/assets/header/ico_address.png") center center no-repeat;
        }
    }

    .exit-title {
        width: 100%;
        height: 88%;
        padding: 18px 100px;
        font-size: 20px;
        font-family: SourceHanSansCN-Medium, SourceHanSansCN;
        font-weight: 500;
        color: #FFFFFF;
    }
    .el-upload-dragger{
        background-color: unset;
    }
    /deep/ .el-dialog {
        height: auto;
        /*height: 320px;*/
    }

    /deep/ .el-dialog__body {
        padding: 42px 0;
        height: calc(100% - 12px);
    }
</style>
