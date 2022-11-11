package generator.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 标准化设备表
 * @TableName t_std_device
 */
@Data
public class TStdDevice implements Serializable {
    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 别名
     */
    private String aliasName;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 点号位置，inside-内部设备，outside-外部设备
     */
    private String positionType;

    /**
     * 模版ID
     */
    private Long modelId;

    /**
     * 路径
     */
    private String regionPath;

    /**
     * 上级区域id
     */
    private Long upRegionId;

    /**
     * 上级区域名称
     */
    private String upRegionName;

    /**
     * 部位类型
     */
    private Integer customType;

    /**
     * 设备状态(0：新建，1：在线，2：离线)
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 预置位id
     */
    private Long presetId;

    /**
     * 摄像机id
     */
    private Long cameraId;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 
     */
    private String realCode;

    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TStdDevice other = (TStdDevice) that;
        return (this.getDeviceId() == null ? other.getDeviceId() == null : this.getDeviceId().equals(other.getDeviceId()))
            && (this.getDeviceCode() == null ? other.getDeviceCode() == null : this.getDeviceCode().equals(other.getDeviceCode()))
            && (this.getDeviceName() == null ? other.getDeviceName() == null : this.getDeviceName().equals(other.getDeviceName()))
            && (this.getAliasName() == null ? other.getAliasName() == null : this.getAliasName().equals(other.getAliasName()))
            && (this.getDeviceType() == null ? other.getDeviceType() == null : this.getDeviceType().equals(other.getDeviceType()))
            && (this.getPositionType() == null ? other.getPositionType() == null : this.getPositionType().equals(other.getPositionType()))
            && (this.getModelId() == null ? other.getModelId() == null : this.getModelId().equals(other.getModelId()))
            && (this.getRegionPath() == null ? other.getRegionPath() == null : this.getRegionPath().equals(other.getRegionPath()))
            && (this.getUpRegionId() == null ? other.getUpRegionId() == null : this.getUpRegionId().equals(other.getUpRegionId()))
            && (this.getUpRegionName() == null ? other.getUpRegionName() == null : this.getUpRegionName().equals(other.getUpRegionName()))
            && (this.getCustomType() == null ? other.getCustomType() == null : this.getCustomType().equals(other.getCustomType()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getPresetId() == null ? other.getPresetId() == null : this.getPresetId().equals(other.getPresetId()))
            && (this.getCameraId() == null ? other.getCameraId() == null : this.getCameraId().equals(other.getCameraId()))
            && (this.getOriginId() == null ? other.getOriginId() == null : this.getOriginId().equals(other.getOriginId()))
            && (this.getEdgeCode() == null ? other.getEdgeCode() == null : this.getEdgeCode().equals(other.getEdgeCode()))
            && (this.getRealCode() == null ? other.getRealCode() == null : this.getRealCode().equals(other.getRealCode()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDeviceId() == null) ? 0 : getDeviceId().hashCode());
        result = prime * result + ((getDeviceCode() == null) ? 0 : getDeviceCode().hashCode());
        result = prime * result + ((getDeviceName() == null) ? 0 : getDeviceName().hashCode());
        result = prime * result + ((getAliasName() == null) ? 0 : getAliasName().hashCode());
        result = prime * result + ((getDeviceType() == null) ? 0 : getDeviceType().hashCode());
        result = prime * result + ((getPositionType() == null) ? 0 : getPositionType().hashCode());
        result = prime * result + ((getModelId() == null) ? 0 : getModelId().hashCode());
        result = prime * result + ((getRegionPath() == null) ? 0 : getRegionPath().hashCode());
        result = prime * result + ((getUpRegionId() == null) ? 0 : getUpRegionId().hashCode());
        result = prime * result + ((getUpRegionName() == null) ? 0 : getUpRegionName().hashCode());
        result = prime * result + ((getCustomType() == null) ? 0 : getCustomType().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getPresetId() == null) ? 0 : getPresetId().hashCode());
        result = prime * result + ((getCameraId() == null) ? 0 : getCameraId().hashCode());
        result = prime * result + ((getOriginId() == null) ? 0 : getOriginId().hashCode());
        result = prime * result + ((getEdgeCode() == null) ? 0 : getEdgeCode().hashCode());
        result = prime * result + ((getRealCode() == null) ? 0 : getRealCode().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", deviceId=").append(deviceId);
        sb.append(", deviceCode=").append(deviceCode);
        sb.append(", deviceName=").append(deviceName);
        sb.append(", aliasName=").append(aliasName);
        sb.append(", deviceType=").append(deviceType);
        sb.append(", positionType=").append(positionType);
        sb.append(", modelId=").append(modelId);
        sb.append(", regionPath=").append(regionPath);
        sb.append(", upRegionId=").append(upRegionId);
        sb.append(", upRegionName=").append(upRegionName);
        sb.append(", customType=").append(customType);
        sb.append(", status=").append(status);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", presetId=").append(presetId);
        sb.append(", cameraId=").append(cameraId);
        sb.append(", originId=").append(originId);
        sb.append(", edgeCode=").append(edgeCode);
        sb.append(", realCode=").append(realCode);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}