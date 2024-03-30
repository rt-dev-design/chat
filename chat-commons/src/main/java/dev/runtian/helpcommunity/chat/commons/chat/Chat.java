package dev.runtian.helpcommunity.chat.commons.chat;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 对话表
 * @TableName chat
 */
@TableName(value ="chat")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Chat implements Serializable {
    /**
     * 主键，标识符
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 最近一条消息的时间
     */
    private Date lastMessageTime;

    /**
     * 用户x
     */
    private Long userxId;

    /**
     * 用户y
     */
    private Long useryId;

    /**
     * 用户x最后活跃时间
     */
    private Date usexLastPresentTime;

    /**
     * 用户y最后出现时间
     */
    private Date useryLastPresentTime;

    @TableField(exist = false)
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
        Chat other = (Chat) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
            && (this.getLastMessageTime() == null ? other.getLastMessageTime() == null : this.getLastMessageTime().equals(other.getLastMessageTime()))
            && (this.getUserxId() == null ? other.getUserxId() == null : this.getUserxId().equals(other.getUserxId()))
            && (this.getUseryId() == null ? other.getUseryId() == null : this.getUseryId().equals(other.getUseryId()))
            && (this.getUsexLastPresentTime() == null ? other.getUsexLastPresentTime() == null : this.getUsexLastPresentTime().equals(other.getUsexLastPresentTime()))
            && (this.getUseryLastPresentTime() == null ? other.getUseryLastPresentTime() == null : this.getUseryLastPresentTime().equals(other.getUseryLastPresentTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getLastMessageTime() == null) ? 0 : getLastMessageTime().hashCode());
        result = prime * result + ((getUserxId() == null) ? 0 : getUserxId().hashCode());
        result = prime * result + ((getUseryId() == null) ? 0 : getUseryId().hashCode());
        result = prime * result + ((getUsexLastPresentTime() == null) ? 0 : getUsexLastPresentTime().hashCode());
        result = prime * result + ((getUseryLastPresentTime() == null) ? 0 : getUseryLastPresentTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", lastMessageTime=").append(lastMessageTime);
        sb.append(", userxId=").append(userxId);
        sb.append(", useryId=").append(useryId);
        sb.append(", usexLastPresentTime=").append(usexLastPresentTime);
        sb.append(", useryLastPresentTime=").append(useryLastPresentTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}