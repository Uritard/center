let tableDataMock = {
    code: 200,
    message: 'success',
    data: [
        {
            content: '正常',
            count: '20'
        },
        {
            content: '异常',
            count: '30'
        }
    ]

};
let countTransformerByMonitor = {
    code: 200,
    message: 'success',
    data: {
            normal: 6,
            abnormal: 4
        }
}
export default {
    'get|/apis/homePage/v1/chartData': tableDataMock,
    'get|/apis/tStdDevice/v1/countTransformerByMonitor': countTransformerByMonitor
}
