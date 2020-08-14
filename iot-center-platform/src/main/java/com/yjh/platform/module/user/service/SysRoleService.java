package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.*;
import com.yjh.platform.module.user.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author tt
* @since 2020-07-23
*/
@Service
public class SysRoleService{

    @Autowired
    private SysRoleDao sysRoleDao;
    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;
    @Autowired
    private SysRoleCameraDao sysRoleCameraDao;
    @Autowired
    private SysRoleRegionDao sysRoleRegionDao;
    @Autowired
    private SysRoleDeviceDao sysRoleDeviceDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRole sysRole) {
        return this.sysRoleDao.insert(sysRole);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long roleId) {
        return this.sysRoleDao.deleteByPrimaryId(roleId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysRole sysRole) {
        return this.sysRoleDao.update(sysRole);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public SysRole selectByPrimaryId(Long roleId) {
        return this.sysRoleDao.selectByPrimaryId(roleId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRole> select(Long roleId, String roleName, Date createTime, Long creatorId, Integer sysState) {
        return this.sysRoleDao.select(roleId, roleName, createTime, creatorId, sysState);
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRole> selectByPage(SysRole sysRole) {
        return this.sysRoleDao.selectByPage(sysRole);
    }

    @Logs(title = "根据角色ID查询关联的菜单信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectRelationMenu(Long roleId) { return this.sysRoleDao.selectRelationMenu(roleId); }

    @Logs(title = "根据角色ID查询关联的区域信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectRelationRegion(Long roleId) {
        return this.sysRoleDao.selectRelationRegion(roleId);
    }

    @Logs(title = "根据角色ID查询关联的相机信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectRelationCamera(Long roleId) {
        return this.sysRoleDao.selectRelationCamera(roleId);
    }

    @Logs(title = "根据角色ID查询关联的设备信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectRelationDevice(Long roleId) {
        return this.sysRoleDao.selectRelationDevice(roleId);
    }

    @Logs(title = "根据角色ID查询关联的权限信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectRelationAuthor(Long roleId) {
        List<String> list = new ArrayList<>();
        List<AreaInfo> areaInfoList = this.sysRoleDao.selectRelationAuthor(roleId);
        for (AreaInfo areaInfo:areaInfoList) {
            switch (areaInfo.getInfoType()) {
                case "region":
                    list.add(String.valueOf(areaInfo.getId()));
                    break;
                case "camera":
                    list.add(String.valueOf(areaInfo.getId()));
                    break;
                case "device":
                    list.add(String.valueOf(areaInfo.getId()));
                    break;
                default:
                    System.out.println("无此权限参数类型！");
                    break;
            }
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    @Logs(title = "根据角色ID查询关联的权限树信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectRelationAuthorTree(Long roleId) {
        List<String> list = new ArrayList<>();
        List<AreaInfo> areaInfoList = this.sysRoleDao.selectRelationAuthor(roleId);
        List<AreaInfo> DevTreeByRole = new ArrayList<>();
        for (AreaInfo areaInfo:areaInfoList) {
            if (areaInfo.getUpId() == null || areaInfo.getUpId() == 0) {
                DevTreeByRole.add(areaInfo);
            }
        }
        diGuiDevTree(DevTreeByRole, areaInfoList);
        for (AreaInfo areaInfo:DevTreeByRole) {
            switch (areaInfo.getInfoType()) {
                case "region":
                    if (areaInfo.getChildren()!=null) {
                        List<AreaInfo> areaInfoListChild = areaInfo.getChildren();
                        diGuiAuthorList(areaInfoListChild, list);
                    } else {list.add(String.valueOf(areaInfo.getId()));}
                    break;
                case "camera":
                    list.add(String.valueOf(areaInfo.getId()));
                    break;
                case "device":
                    list.add(String.valueOf(areaInfo.getId()));
                    break;
                default:
                    System.out.println("无此权限参数类型！");
                    break;
            }
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    private void diGuiDevTree(List<AreaInfo> DevTreeByRole, List<AreaInfo> areaInfoList) {
        for(AreaInfo areaInfo:DevTreeByRole){
            List<AreaInfo> childrenList = new ArrayList<>();
            for (AreaInfo areaInfoTem:areaInfoList) {
                if (Objects.equals(areaInfo.getId(), areaInfoTem.getUpId())) {
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGuiDevTree(childrenList, areaInfoList);
            }
        }
    }

    private void diGuiAuthorList(List<AreaInfo> areaInfoList, List<String> list) {
        for (AreaInfo areaInfo:areaInfoList) {
            switch (areaInfo.getInfoType()) {
                case "region":
                    if (areaInfo.getChildren()!=null) {
                        List<AreaInfo> areaInfoListChil = areaInfo.getChildren();
                        diGuiAuthorList(areaInfoListChil, list);
                    } else {list.add(String.valueOf(areaInfo.getId()));}
                    break;
                case "camera":
                    list.add(String.valueOf(areaInfo.getId()));
                    break;
                case "device":
                    list.add(String.valueOf(areaInfo.getId()));
                    break;
                default:
                    break;
            }
        }
    }

    @Logs(title = "修改角色区域设备权限", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int updateRoleDeviceRight(Map<String, Object> req) {
        Long roleId = Long.valueOf(String.valueOf(req.get("roleId")));
        sysRoleRegionDao.deleteByPrimaryId(roleId);
        sysRoleDeviceDao.deleteByPrimaryId(roleId);
        sysRoleCameraDao.deleteByPrimaryId(roleId);
        //递归获取摄像机，设备，区域list,分别插入
        List<SysRoleRegion> roleRegionList = new ArrayList<>();
        List<SysRoleCamera> roleCameraList = new ArrayList<>();
        List<SysRoleDevice> roleDeviceList = new ArrayList<>();
        if (req.get("checked")!=null) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) req.get("checked");
            for (Map<String, Object> mapObject:list) {
                switch (String.valueOf(mapObject.get("infoType"))) {
                    case "region":
                        SysRoleRegion sysRoleRegion = new SysRoleRegion();
                        long idRegion = Long.valueOf(String.valueOf(mapObject.get("id")));
                        sysRoleRegion.setRegionId(idRegion);
                        sysRoleRegion.setRoleId(roleId);
                        sysRoleRegion.setIsChecked("checked");
                        roleRegionList.add(sysRoleRegion);
                        break;
                    case "camera":
                        SysRoleCamera sysRoleCamera = new SysRoleCamera();
                        long idCamera = Long.valueOf(String.valueOf(mapObject.get("id")));
                        sysRoleCamera.setCameraId(idCamera);
                        sysRoleCamera.setRoleId(roleId);
                        sysRoleCamera.setIsChecked("checked");
                        roleCameraList.add(sysRoleCamera);
                        break;
                    case "device":
                        SysRoleDevice sysRoleDevice = new SysRoleDevice();
                        long idDevice = Long.valueOf(String.valueOf(mapObject.get("id")));
                        sysRoleDevice.setDeviceId(idDevice);
                        sysRoleDevice.setRoleId(roleId);
                        sysRoleDevice.setIsChecked("checked");
                        roleDeviceList.add(sysRoleDevice);
                        break;
                    default:
                        throw new BusinessException("类型输入有误！");
                }
            }
        }
        if (req.get("halfChecked")!=null) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) req.get("halfChecked");
            for (Map<String, Object> mapArea:list) {
                switch (String.valueOf(mapArea.get("infoType"))) {
                    case "region":
                        SysRoleRegion sysRoleRegion = new SysRoleRegion();
                        long idRegion = Long.valueOf(String.valueOf(mapArea.get("id")));
                        sysRoleRegion.setRegionId(idRegion);
                        sysRoleRegion.setRoleId(roleId);
                        sysRoleRegion.setIsChecked("halfChecked");
                        roleRegionList.add(sysRoleRegion);
                        break;
                    case "camera":
                        SysRoleCamera sysRoleCamera = new SysRoleCamera();
                        long idCamera = Long.valueOf(String.valueOf(mapArea.get("id")));
                        sysRoleCamera.setCameraId(idCamera);
                        sysRoleCamera.setRoleId(roleId);
                        sysRoleCamera.setIsChecked("halfChecked");
                        roleCameraList.add(sysRoleCamera);
                        break;
                    case "device":
                        SysRoleDevice sysRoleDevice = new SysRoleDevice();
                        long idDevice = Long.valueOf(String.valueOf(mapArea.get("id")));
                        sysRoleDevice.setDeviceId(idDevice);
                        sysRoleDevice.setRoleId(roleId);
                        sysRoleDevice.setIsChecked("halfChecked");
                        roleDeviceList.add(sysRoleDevice);
                        break;
                    default:
                        throw new BusinessException("类型输入有误！");
                }
            }
        }
        List<SysRoleDevice> roleDeviceListNoDup = roleDeviceList.stream().distinct().collect(Collectors.toList());
        if (roleCameraList.size()>0) {this.sysRoleCameraDao.batchInsert(roleCameraList);}
        if (roleDeviceListNoDup.size()>0) {this.sysRoleDeviceDao.batchInsert(roleDeviceListNoDup);}
        if (roleRegionList.size()>0) {this.sysRoleRegionDao.batchInsert(roleRegionList);}
        return 0;
    }

    @Logs(title = "修改角色菜单权限", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int updateRoleMenuRight(Map<String, Object> req) {
        Long roleId = Long.valueOf(String.valueOf(req.get("roleId")));
        sysRoleMenuDao.deleteByRoleId(roleId);
        List<String> listString = (List<String>) req.get("checked");
        List<SysRoleMenu> sysRoleMenuList = new ArrayList<>();
        for (String menuCode:listString) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuCode(menuCode);
            sysRoleMenu.setRoleId(roleId);
            sysRoleMenuList.add(sysRoleMenu);
        }
        return this.sysRoleMenuDao.batchInsert(sysRoleMenuList);
    }

//    @Logs(title = "根据角色ID查询关联的菜单信息", code = "module")
//    @Transactional(rollbackFor = Exception.class)
//    public List<String> selectRelationMenu(Long roleId) {
//        List<String> list = new ArrayList<>();
//        List<AreaInfo> areaInfoList = this.sysRoleDao.selectRelationMenu(roleId);
//        List<AreaInfo> MenuTreeByRole = new ArrayList<>();
//        for (AreaInfo areaInfo:areaInfoList) {
//            if (Objects.equals(areaInfo.getUpId(), null) || Objects.equals(areaInfo.getUpId(), "")) {
//                MenuTreeByRole.add(areaInfo);
//            }
//        }
//        diGuiMenuTree(MenuTreeByRole, areaInfoList);
//        System.out.println("MenuTreeByRole: "+MenuTreeByRole);
//        for (AreaInfo areaInfo:MenuTreeByRole) {
//            if (areaInfo.getChildren()!=null) {
//                List<AreaInfo> areaInfoListChild = areaInfo.getChildren();
//                System.out.println("查找子集");
//                diGuiMenuList(areaInfoListChild, list);
//            } else {list.add(areaInfo.getId());}
//        }
//        return list.stream().distinct().collect(Collectors.toList());
//    }
//
//    private void diGuiMenuTree(List<AreaInfo> MenuTreeByRole, List<AreaInfo> areaInfoList) {
//        for(AreaInfo areaInfo:MenuTreeByRole){
//            List<AreaInfo> childrenList = new ArrayList<>();
//            for (AreaInfo areaInfoTem:areaInfoList) {
//                if (!Objects.equals(areaInfoTem.getUpId(), null) && Objects.equals(areaInfo.getId(), areaInfoTem.getUpId())) {
//                    childrenList.add(areaInfoTem);
//                }
//            }
//            if (childrenList.size()>0 ) {
//                areaInfo.setChildren(childrenList);
//                diGuiMenuTree(childrenList, areaInfoList);
//            }
//        }
//    }
//
//    private void diGuiMenuList(List<AreaInfo> MenuTreeByRole, List<String> list) {
//        for (AreaInfo areaInfo:MenuTreeByRole) {
//            if (areaInfo.getChildren()!=null) {
//                List<AreaInfo> areaInfoListChild = areaInfo.getChildren();
//                diGuiMenuList(areaInfoListChild, list);
//            } else {list.add(areaInfo.getId());}
//        }
//    }

}

