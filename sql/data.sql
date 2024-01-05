drop table if exists `tb_spectrum_data`;
CREATE TABLE `tb_spectrum_data`
(
    id                    bigint auto_increment not null comment '主键',
    `name`                varchar(128)  default ''  comment '标准品名称',
    origin                text comment '原始数据',
    smooth_one            text comment '一次平滑',
    background            text comment '背景荧光',
    corrected             text comment '校准基线',
    smooth_two            text comment '二次平滑',
    fix_peak              text comment '保留峰值',
    normalized            text comment '归一化',
    is_delete             tinyint(3) default 0 not null comment '是否删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '光谱数据';