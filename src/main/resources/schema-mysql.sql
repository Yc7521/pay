CREATE TABLE IF NOT EXISTS `user_info`
(
    `id`        bigint         NOT NULL AUTO_INCREMENT,
    `credible`  bit(1)         NOT NULL,
    `deleted`   bit(1) DEFAULT NULL,
    `money`     decimal(19, 4) NOT NULL,
    `nickname`  varchar(63)    NOT NULL,
    `user_type` varchar(255)   NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT,
    `password`     varchar(63) NOT NULL,
    `username`     varchar(31) NOT NULL,
    `user_info_id` bigint      NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_user_info_id` (`user_info_id`),
    UNIQUE KEY `UK_username` (`username`),
    KEY `idx_account_username` (`username`),
    CONSTRAINT `FK_user_info_id` FOREIGN KEY (`user_info_id`)
        REFERENCES `user_info` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
