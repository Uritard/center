<template>
    <div>
        <el-table :data="tableData" class="hidden-tbody">
            <el-table-column align="center" type="index" label="序号" width="56"></el-table-column>
            <el-table-column  v-for="(item,index) in columnArr" :key="index"
                              align="center" :prop="item.prop" :label="item.label"
                              show-overflow-tooltip :width="item.type=='time' ? '140px':''"></el-table-column>
        </el-table>

        <vueSeamless :data="tableData" class="auto-scroll-table" :class-option="defaultOption">
            <el-table :data="tableData" class="hidden-thead">
                <el-table-column align="center" type="index" label="序号" width="56">
                    <template slot-scope="scope">
                        <span>{{ scope.$index + 1 }} </span>
                    </template>
                </el-table-column>
                <el-table-column  v-for="(item,index) in columnArr" :key="index"
                                  align="center" :prop="item.prop" :label="item.label"
                                  show-overflow-tooltip :width="item.type=='time' ? '140px':''"></el-table-column>
            </el-table>
        </vueSeamless>
    </div>
</template>

<script>
    import vueSeamless from 'vue-seamless-scroll'
    export default {
        name: 'TableRollingEffect',
        components: {vueSeamless},
        props: ['columnArr','tableData'],
        data() {
            return {

            }
        },
        // 监听属性 类似于data概念
        computed: {
            defaultOption () {
                return {
                    step: 0.2, // 数值越大速度滚动越快
                    //limitMoveNum: 20, // 开始无缝滚动的数据量 this.dataList.length
                    hoverStop: true, // 是否开启鼠标悬停stop
                    direction: 1, // 0向下 1向上 2向左 3向右
                    openWatch: true, // 开启数据实时监控刷新dom
                    singleHeight: 0, // 单步运动停止的高度(默认值0是无缝不停止的滚动) direction => 0/1
                    singleWidth: 0, // 单步运动停止的宽度(默认值0是无缝不停止的滚动) direction => 2/3
                    waitTime: 1000 // 单步运动停止的时间(默认值1000ms)
                }
            }
        },
        mounted() {

        },
        destroyed() {

        },
        methods: {}
    }
</script>

<style scoped lang="less">
    .hidden-tbody.el-table {
        height: 34px;
        box-sizing: border-box;
        tbody { //隐藏上面表格的tbody
            display: none;
            overflow: hidden;
        }
    }
    .auto-scroll-table {
        height: calc(100% - 34px);
        overflow: hidden;

    }
    .hidden-thead{
        /deep/ .el-table__header-wrapper {
            border-top: none; //防止边框重叠
            thead { //隐藏下面表格的thead
                display: none;
                overflow: hidden;
            }
        }
    }

</style>
