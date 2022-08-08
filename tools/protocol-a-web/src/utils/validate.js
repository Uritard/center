/**
 * Form 正则验证规则集合
 * @param {*} str 需要验证的字符串
 * @param {*} type 验证规则类型
 */
export const formItemValidate = (str, type, callBackFun) => {
    let bl = false;
    let errorMsg = '';
    switch (type) {
        case 'LONGITUDE': // 经度 范围：-180.00~180.00;
            // bl = /^[\-\+]?(0?\d{1,2}\.\d{1,5}|1[0-7]?\d{1}\.\d{1,5}|180\.0{1,5})$/.test(str);
            bl = /^[\-\+]?(([0-9]|[1-9][0-9]|1[0-7][0-9])\.([0-9]{4})|180([.][0]{4}))$/.test(str);
            errorMsg = '请输入正确的经度'
            break
        case 'LATITUDE': // 纬度 -90.00~90.00
            // bl = /^[\-\+]?([0-8]?\d{1}\.\d{1,5}|90\.0{1,5})$/.test(str);
            bl = /^-?((0|[1-8]?[0-9]?)([.][0-9]{4})|90([.][0]{4}))$/.test(str);
            errorMsg = '请输入正确的纬度'
            break
        case 'PHONE': // 手机号码
            bl = /^1[3|4|5|6|7|8|9][0-9]{9}$/.test(str);
            errorMsg = '请输入正确的手机号'
            break
        case 'TEL': // 座机
            bl = /^(0\d{2,3}-\d{7,8})(-\d{1,4})?$/.test(str);
            errorMsg = '请输入正确的座机号码'
            break
        case 'CARD': // 身份证
            bl = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(str);
            errorMsg = '请输入正确的身份证号'
            break
        case 'POSTAL': // 邮政编码
            bl = /[1-9]\d{5}(?!\d)/.test(str);
            errorMsg = '请输入正确的邮政编码'
            break
        case 'QQ': // QQ号
            bl = /^[1-9][0-9]{4,9}$/.test(str);
            errorMsg = '请输入正确的QQ号'
            break
        case 'EMAIL': // 邮箱
            bl = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((.[a-zA-Z0-9_-]{2,3}){1,2})$/.test(str);
            errorMsg = '请输入正确的邮箱'
            break
        case 'MONEY': // 金额(小数点2位)
            bl = /^\d*(?:\.\d{0,2})?$/.test(str);
            errorMsg = '请输入正确的金额'
            break
        case 'URL': // 网址
            bl = /(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&:/~\+#]*[\w\-\@?^=%&/~\+#])?/.test(str);
            errorMsg = '请输入正确的网址'
            break
        case 'IP': // IP
            bl = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/.test(str);
            errorMsg = '请输入正确的IP'
            break
        case 'PORT': // PORT端口
            bl = /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/.test(str);
            errorMsg = '请输入正确的端口'
            break
        case 'DATE': // 日期时间
            bl = /^(\d{4})\-(\d{2})\-(\d{2}) (\d{2})(?:\:\d{2}|:(\d{2}):(\d{2}))$/.test(str) || /^(\d{4})\-(\d{2})\-(\d{2})$/.test(str);
            errorMsg = '请输入正确的日期'
            break
        case 'NUMBER': // 第一位不可以为0的数字，单个数字可以为0  /(^[1-9]([0-9]*)$|^[0-9]$)/
            bl = /(^[1-9]([0-9]*)$|^[0-9]$)/.test(str);
            errorMsg = '请输入正确的数字'
            break
        case 'NUMBER2': // 第一位可以0的数字
            bl = /^[0-9]*$/.test(str);
            errorMsg = '请输入正确的数字'
            break
        case 'INTEGER': // 数字
            bl = /^[1-9]\d*$/.test(str);
            errorMsg = '请输入非零正整数'
            break
        case 'DECIMAL': // 带小数的数字
            bl = /^[+-]?(0|([1-9]\d*))(\.\d+)?$/g.test(str);
            errorMsg = '请输入正确的数字'
            break
        case 'EN': // 英文
            bl = /^[a-zA-Z]+$/.test(str);
            errorMsg = '请输入英文'
            break
        case 'CHINESE': // 中文
            bl = /^[\u4E00-\u9FA5]+$/.test(str);
            errorMsg = '请输入中文'
            break
        case 'LOWER': // 小写
            bl = /^[a-z]+$/.test(str);
            errorMsg = '请输入小写字母'
            break
        case 'UPPER': // 大写
            bl = /^[A-Z]+$/.test(str);
            errorMsg = '请输入大写字母'
            break
        case 'HTML': // HTML标记
            bl = /<("[^"]*"|'[^']*'|[^'">])*>/.test(str);
            errorMsg = '请输入不要输入html标记'
            break
        case 'NAME': // 名称格式
            let regEn = /[ `~!@#$%^&*_+<>?:"{},.\/;'[\]]/im, regCn = /[ ·！#￥——：；“”‘、，|《。》？、【】[\]]/im;
            bl = !(regEn.test(str) || regCn.test(str));
            errorMsg = '请不要输入特殊字符'
            break
        case 'NAME-SIMPLE': // 数字、字母组成的字符
            let regEnS = /[ `~!@$%^&*_+<>?:"{},.\/;'[\]]/im, regCnS = /[ ·！￥——：；“”‘、，|《。》？、【】[\]]/im;
            bl = !(regEnS.test(str) || regCnS.test(str));
            errorMsg = '请不要输入除#以外的特殊字符'
            break
        case 'PWD': // 密码强度正则，最少6位，包括至少1个大写字母，1个小写字母，1个数字，1个特殊字符
            bl = /^.*(?=.{5,})(?=.*\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*? ]).*$/.test(str);
            errorMsg = '请输入正确的格式(大写、小写、数字、个别特殊字符)'
            break
        case 'PWD-SIMPLE': // 密码(以字母开头，长度在6~18之间，只能包含字母、数字和下划线)
            bl = /^[a-zA-Z]\w{5,17}$/.test(str);
            errorMsg = '请输入正确的格式'
            break
        case 'PWD-STRONG': // 强密码(必须包含大小写字母和数字的组合，不能使用特殊字符，长度在8-10之间)
            bl = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,10}$/.test(str);
            errorMsg = '请输入正确格式的密码'
            break
        case 'CODE': // 数字、字母或下划线组成的字符
            bl = /^[A-Za-z0-9_\-]+$/.test(str);
            errorMsg = '数字、字母或划线组成的字符'
            break
        case 'CODE-SIMPLE': // 数字、字母组成的字符
            bl = /^[A-Za-z0-9\#]+$/.test(str);
            errorMsg = '数字、字母和#组成的字符'
            break
        case 'CODE-SIMPLE2': // 只包含数字和字母组成的字符 不能纯是数字或纯是字母
            bl = /^(?!(\d{2,}|[a-zA-Z]{2,})$)[0-9a-zA-Z]{2,}$/.test(str);
            errorMsg = '包含数字和字母的字符'
            break
        case 'REMARK': //
            let regEn1 = /[`~@$%^&*+<>"{}\/'[\]]/im, regCn1 = /[·￥“”‘|《》【】[\]]/im;
            bl = !(regEn1.test(str) || regCn1.test(str));
            errorMsg = '请不要输入特殊字符'
            break
        case 'CHANNEL-FORM': // 数字和英文逗号
            // bl = /^[1-9]|[,]*$/.test(str);
            let regEn11 = /^[0-9]+$/, regCn11 = /[,]/im;
            bl = (regEn11.test(str) || regCn11.test(str));
            errorMsg = '请输入数字和英文逗号'
            break
    }
    if (str != '' && str != null && !bl) {
        callBackFun(new Error(errorMsg));
    } else {
        callBackFun();
    }
}

/**
 * 严格的身份证校验
 * @param {*} sId
 * */
export const isCardID = (sId) => {
    if (!/(^\d{15}$)|(^\d{17}(\d|X|x)$)/.test(sId)) {
        console.log('你输入的身份证长度或格式错误')
        return false
    }
    // 身份证城市
    var aCity = {
        11: '北京',
        12: '天津',
        13: '河北',
        14: '山西',
        15: '内蒙古',
        21: '辽宁',
        22: '吉林',
        23: '黑龙江',
        31: '上海',
        32: '江苏',
        33: '浙江',
        34: '安徽',
        35: '福建',
        36: '江西',
        37: '山东',
        41: '河南',
        42: '湖北',
        43: '湖南',
        44: '广东',
        45: '广西',
        46: '海南',
        50: '重庆',
        51: '四川',
        52: '贵州',
        53: '云南',
        54: '西藏',
        61: '陕西',
        62: '甘肃',
        63: '青海',
        64: '宁夏',
        65: '新疆',
        71: '台湾',
        81: '香港',
        82: '澳门',
        91: '国外'
    };
    if (!aCity[parseInt(sId.substr(0, 2))]) {
        console.log('你的身份证地区非法')
        return false
    }

    // 出生日期验证
    var sBirthday = (sId.substr(6, 4) + '-' + Number(sId.substr(10, 2)) + '-' + Number(sId.substr(12, 2))).replace(/-/g, '/');
    var d = new Date(sBirthday)
    if (sBirthday != (d.getFullYear() + '/' + (d.getMonth() + 1) + '/' + d.getDate())) {
        console.log('身份证上的出生日期非法')
        return false
    }

    // 身份证号码校验
    var sum = 0;
    var weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
    var codes = '10X98765432'
    for (var i = 0; i < sId.length - 1; i++) {
        sum += sId[i] * weights[i];
    }
    var last = codes[sum % 11]; // 计算出来的最后一位身份证号码
    if (sId[sId.length - 1] != last) {
        console.log('你输入的身份证号非法')
        console.log(sum)
        console.log(last)
        console.log(sum % 11)
        return false
    }

    return true
}
