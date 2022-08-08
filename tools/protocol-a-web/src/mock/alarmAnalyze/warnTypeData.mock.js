let warnTypeDataMock = {
    code: 200,
    message: 'success',
    data: [
        {
            content: '未知的声源',
            count: '20'
        },
        {
            content: '正常声音',
            count: '30'
        },
        {
            content: '疑似故障',
            count: '10'
        },
        {
            content: '故障声音（其他故障）',
            count: '67'
        },
        {
            content: '三相不平衡故障',
            count: '23'
        },
        {
            content: '相间短路故障',
            count: '45'
        },
        {
            content: '局部放电故障',
            count: '16'
        },
        {
            content: '空载（开路）故障',
            count: '50'
        }
    ]
};
export default {
    'get|/apis/tWarnInfo/v1/warnType': warnTypeDataMock
};
