let treeDataMock = {
    code: 200,
    message: 'success',
    data: [
        {
            'label': '220KV变电站',
            'upId': null,
            'upName': null,
            'infoType': 'station',
            'children': [
                {
                    'label': '110kV变压器',
                    'upId': 700001,
                    'upName': '变电总公司',
                    'infoType': 'device_type',
                    'children': [
                    ],
                    'id': 700002
                },
                {
                    'label': '50kV变压器',
                    'upId': 700001,
                    'upName': '变电总公司',
                    'infoType': 'device_type',
                    'children': null,
                    'id': 700024
                },
                {
                    'label': '500kV变压器',
                    'upId': 700001,
                    'upName': '变电总公司',
                    'infoType': 'device_type',
                    'children': null,
                    'id': 700041
                },
                {
                    'label': '10kV变压器',
                    'upId': 700001,
                    'upName': '变电总公司',
                    'infoType': 'device_type',
                    'children': null,
                    'id': 700042
                }
            ],
            'id': 700001
        }
    ]
};
export default {
    'get|/apis/tStdMeteModel/v1/selectDeviceTypeModelTree': treeDataMock
};
