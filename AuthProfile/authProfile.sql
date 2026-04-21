

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `connection_values`;
DROP TABLE IF EXISTS `connections`;
DROP TABLE IF EXISTS `profile_fields`;
DROP TABLE IF EXISTS `profiles`;

-- -----------------------------------------------------
-- profiles
-- -----------------------------------------------------
CREATE TABLE `profiles` (
  `id`         INT NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(100) NOT NULL,
  `auth_type`  INT NOT NULL,
  `version`    INT DEFAULT 1,
  `created_by` INT DEFAULT NULL,
  `is_active`  TINYINT(1) DEFAULT 1,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- profile_fields
-- -----------------------------------------------------
CREATE TABLE `profile_fields` (
  `id`             INT NOT NULL AUTO_INCREMENT,
  `profile_id`     INT NOT NULL,
  `key`            VARCHAR(100) NOT NULL,
  `label`          VARCHAR(100) DEFAULT NULL,
  `field_type`     ENUM('text','password') DEFAULT 'text',
  `default_value`  TEXT,
  `is_custom`      TINYINT(1) NOT NULL DEFAULT 0,
  `placement`      VARCHAR(20) DEFAULT NULL,
  `position`       INT DEFAULT 0,
  `created_at`     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_profile_key` (`profile_id`, `key`),
  CONSTRAINT `profile_fields_ibfk_1`
    FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- connections  (kept for future use)
-- -----------------------------------------------------
CREATE TABLE `connections` (
  `id`            INT NOT NULL AUTO_INCREMENT,
  `profile_id`    INT NOT NULL,
  `user_id`       INT NOT NULL,
  `name`          VARCHAR(100) NOT NULL,
  `status`        ENUM('active','inactive','failed') DEFAULT 'active',
  `access_token`  TEXT,
  `refresh_token` TEXT,
  `expires_at`    DATETIME DEFAULT NULL,
  `created_at`    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `profile_id` (`profile_id`),
  CONSTRAINT `connections_ibfk_1`
    FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `connection_values` (
  `id`            INT NOT NULL AUTO_INCREMENT,
  `connection_id` INT NOT NULL,
  `field_id`      INT NOT NULL,
  `value`         TEXT,
  `created_at`    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_connection_field` (`connection_id`,`field_id`),
  KEY `field_id` (`field_id`),
  CONSTRAINT `connection_values_ibfk_1`
    FOREIGN KEY (`connection_id`) REFERENCES `connections` (`id`) ON DELETE CASCADE,
  CONSTRAINT `connection_values_ibfk_2`
    FOREIGN KEY (`field_id`) REFERENCES `profile_fields` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- SEED DATA
-- =====================================================

INSERT INTO `profiles` (`id`,`name`,`auth_type`,`version`,`created_by`,`is_active`) VALUES
  (1,'Basic Auth Profile', 1, 1, 101, 1),
  (2,'OAuth2 Profile',     2, 1, 101, 1),
  (3,'API Key Profile',    3, 1, 101, 1),
  (4,'No Auth Profile',    5, 1, 101, 1);

-- Basic Auth  -> system fields, label NULL, is_custom 0
INSERT INTO `profile_fields`
  (`profile_id`,`key`,`label`,`field_type`,`default_value`,`is_custom`,`placement`,`position`)
VALUES
  (1,'username', NULL,'text',     'john_doe',    0, NULL, 1),
  (1,'password', NULL,'password', 'password123', 0, NULL, 2);

-- OAuth v2  -> system fields, label NULL, is_custom 0
INSERT INTO `profile_fields`
  (`profile_id`,`key`,`label`,`field_type`,`default_value`,`is_custom`,`placement`,`position`)
VALUES
  (2,'client_id',         NULL,'text',     'oauth-client-id-123',       0, NULL,     1),
  (2,'client_secret',     NULL,'password', 'oauth-secret-xyz',          0, NULL,     2),
  (2,'authorization_url', NULL,'text',     'https://auth.example.com',  0, NULL,     3),
  (2,'access_token_url',  NULL,'text',     'https://token.example.com', 0, NULL,     4),
  (2,'scopes',            NULL,'text',     '',                          0, NULL,     5),
  (2,'token_placement',   NULL,'text',     'header',                    0, 'header', 6);

-- API Key  -> ONE user-defined field, is_custom 1, label stored
INSERT INTO `profile_fields`
  (`profile_id`,`key`,`label`,`field_type`,`default_value`,`is_custom`,`placement`,`position`)
VALUES
  (3,'x-api-key', 'My API Key', 'password', 'my-secret-api-key-xyz', 1, 'header', 1);
