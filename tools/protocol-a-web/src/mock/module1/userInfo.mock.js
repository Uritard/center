let userInfoMock = {
    code: 200,
    message: 'success',
    data: {
        sysUserLogin: {
            userId: '110',
            userName: 'admin',
            trueName: '变变',
            roleName: '管理员',
        }
    }
};
export default {
    'post|/apis/sysUser/v1/login': userInfoMock
}
