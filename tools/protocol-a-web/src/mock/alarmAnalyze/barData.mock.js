let deviceDataMock = {
    code: 200,
    message: 'success',
    data: [
        {
            deviceTypeName: '110kV变压器',
            count: '20'
        },
        {
            deviceTypeName: '10kV变压器',
            count: '30'
        },
        {
            deviceTypeName: '220kV变压器',
            count: '10'
        },
        {
            deviceTypeName: '50kV变压器',
            count: '100'
        }
    ]
};
export default {
    'get|/apis/tWarnInfo/v1/countAlarmByDevice': deviceDataMock
};
