package com.yjh.platform.module.user.entity.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/3
 * @since [产品/模块版本] （可选）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonByIdReq {
    private Long id;

    public static CommonByIdReq of(Long id) {
        return new CommonByIdReq(id);
    }
}
