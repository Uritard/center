package generator.mapper;

import generator.domain.TStdDevice;

/**
* @author YIJIAHE
* @description 针对表【t_std_device(标准化设备表)】的数据库操作Mapper
* @createDate 2022-11-11 16:06:29
* @Entity generator.domain.TStdDevice
*/
public interface TStdDeviceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TStdDevice record);

    int insertSelective(TStdDevice record);

    TStdDevice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TStdDevice record);

    int updateByPrimaryKey(TStdDevice record);

}
