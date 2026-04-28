-- MySQL dump 10.13  Distrib 8.0.40, for Linux (x86_64)
--
-- Host: localhost    Database: auth_profiles
-- ------------------------------------------------------
-- Server version	8.0.40-0ubuntu0.20.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `connection_oauth_values`
--

DROP TABLE IF EXISTS `connection_oauth_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `connection_oauth_values` (
  `id` int NOT NULL AUTO_INCREMENT,
  `connection_id` int NOT NULL,
  `access_token` text,
  `refresh_token` text,
  `expires_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_connection_oauth_connection` (`connection_id`),
  KEY `fk_connection_oauth_connection` (`connection_id`),
  CONSTRAINT `fk_connection_oauth_connection` FOREIGN KEY (`connection_id`) REFERENCES `connections` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `connection_oauth_values`
--

LOCK TABLES `connection_oauth_values` WRITE;
/*!40000 ALTER TABLE `connection_oauth_values` DISABLE KEYS */;
/*!40000 ALTER TABLE `connection_oauth_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `connection_values`
--

DROP TABLE IF EXISTS `connection_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `connection_values` (
  `id` int NOT NULL AUTO_INCREMENT,
  `connection_id` int NOT NULL,
  `field_id` int NOT NULL,
  `key` varchar(80) DEFAULT NULL,
  `value` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_connection_field` (`connection_id`,`field_id`),
  KEY `field_id` (`field_id`),
  CONSTRAINT `connection_values_ibfk_1` FOREIGN KEY (`connection_id`) REFERENCES `connections` (`id`) ON DELETE CASCADE,
  CONSTRAINT `connection_values_ibfk_2` FOREIGN KEY (`field_id`) REFERENCES `profile_fields` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `connection_values`
--

LOCK TABLES `connection_values` WRITE;
/*!40000 ALTER TABLE `connection_values` DISABLE KEYS */;
INSERT INTO `connection_values` VALUES (1,1,13,NULL,'key','2026-04-21 11:50:36');
/*!40000 ALTER TABLE `connection_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `connections`
--

DROP TABLE IF EXISTS `connections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `connections` (
  `id` int NOT NULL AUTO_INCREMENT,
  `profile_id` int NOT NULL,
  `user_id` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `status` enum('active','inactive','failed') DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `profile_id` (`profile_id`),
  CONSTRAINT `connections_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `connections`
--

LOCK TABLES `connections` WRITE;
/*!40000 ALTER TABLE `connections` DISABLE KEYS */;
INSERT INTO `connections` VALUES (1,8,0,'api - connection','active','2026-04-21 11:50:36'),(2,7,0,'API-key2 -CONNECTION','active','2026-04-21 12:51:49'),(3,7,0,'API-key2 - connection2','active','2026-04-21 13:01:48'),(4,7,0,'API-key2 - connection','active','2026-04-21 13:04:35');
/*!40000 ALTER TABLE `connections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_fields`
--

DROP TABLE IF EXISTS `profile_fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profile_fields` (
  `id` int NOT NULL AUTO_INCREMENT,
  `profile_id` int NOT NULL,
  `key` varchar(100) NOT NULL,
  `label` varchar(100) DEFAULT NULL,
  `field_type` enum('text','password') DEFAULT 'text',
  `default_value` text,
  `is_custom` tinyint(1) NOT NULL DEFAULT '0',
  `placement` varchar(20) DEFAULT NULL,
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_profile_key` (`profile_id`,`key`),
  CONSTRAINT `profile_fields_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_fields`
--

LOCK TABLES `profile_fields` WRITE;
/*!40000 ALTER TABLE `profile_fields` DISABLE KEYS */;
INSERT INTO `profile_fields` VALUES (1,1,'username',NULL,'text','john_doe',0,NULL,1,'2026-04-20 08:29:42'),(2,1,'password',NULL,'password','password123',0,NULL,2,'2026-04-20 08:29:42'),(3,2,'client_id',NULL,'text','oauth-client-id-123',0,NULL,1,'2026-04-20 08:29:42'),(4,2,'client_secret',NULL,'password','oauth-secret-xyz',0,NULL,2,'2026-04-20 08:29:42'),(5,2,'authorization_url',NULL,'text','https://auth.example.com',0,NULL,3,'2026-04-20 08:29:42'),(6,2,'access_token_url',NULL,'text','https://token.example.com',0,NULL,4,'2026-04-20 08:29:42'),(7,2,'scopes',NULL,'text','',0,NULL,5,'2026-04-20 08:29:42'),(8,2,'token_placement',NULL,'text','header',0,'header',6,'2026-04-20 08:29:42'),(9,3,'x-api-key','My API Key','password','my-secret-api-key-xyz',1,'header',1,'2026-04-20 08:29:42'),(10,5,'my-key','Secret Token','password','abc123',1,'header',1,'2026-04-20 08:32:30'),(11,6,'api-key','API KEY','password','',1,'header',1,'2026-04-20 08:34:20'),(13,8,'api','api','text','',1,'header',1,'2026-04-21 09:48:42'),(14,9,'username',NULL,'text','u',0,NULL,1,'2026-04-21 09:49:05'),(15,9,'password',NULL,'password','p',0,NULL,2,'2026-04-21 09:49:05');
/*!40000 ALTER TABLE `profile_fields` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profiles`
--

DROP TABLE IF EXISTS `profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profiles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `auth_type` int NOT NULL,
  `version` int DEFAULT '1',
  `created_by` int DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profiles`
--

LOCK TABLES `profiles` WRITE;
/*!40000 ALTER TABLE `profiles` DISABLE KEYS */;
INSERT INTO `profiles` VALUES (1,'Basic Auth Profile',1,1,101,0,'2026-04-20 08:29:42'),(2,'OAuth2 Profile',2,1,101,1,'2026-04-20 08:29:42'),(3,'API Key Profile',3,1,101,1,'2026-04-20 08:29:42'),(4,'No Auth Profile',5,1,101,1,'2026-04-20 08:29:42'),(5,'RoundTripTest',3,1,NULL,0,'2026-04-20 08:32:30'),(6,'auth - API',3,1,NULL,0,'2026-04-20 08:34:20'),(7,'API-key2',1,4,NULL,1,'2026-04-20 09:05:43'),(8,'api',3,1,NULL,1,'2026-04-21 09:48:42'),(9,'TEST_DEBUG',1,1,NULL,1,'2026-04-21 09:49:05'),(10,'API-Key ',1,2,NULL,1,'2026-04-21 10:13:08');
/*!40000 ALTER TABLE `profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Migration for existing DBs: add OAuth table if schema was already created earlier
--
CREATE TABLE IF NOT EXISTS `connection_oauth_values` (
  `id` int NOT NULL AUTO_INCREMENT,
  `connection_id` int NOT NULL,
  `access_token` text,
  `refresh_token` text,
  `expires_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_connection_oauth_connection` (`connection_id`),
  KEY `fk_connection_oauth_connection` (`connection_id`),
  CONSTRAINT `fk_connection_oauth_connection_existing` FOREIGN KEY (`connection_id`) REFERENCES `connections` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Migration: ensure the (connection_id) UNIQUE KEY exists even on databases
-- created before that constraint was added. Without it, the upsert in
-- OAuthService.upsertOauth degrades to a plain INSERT and we accumulate one
-- row per reconnect. Idempotent: drop+add is wrapped so a missing key isn't
-- an error, and the recreate is a no-op when it's already in place.
-- Run this once on legacy DBs; safe to re-run.
--
-- Step A: dedupe any existing duplicates, keeping the most recent row.
DELETE c1 FROM `connection_oauth_values` c1
INNER JOIN `connection_oauth_values` c2
  ON c1.connection_id = c2.connection_id
 AND c1.id < c2.id;
-- Step B: add the unique key if absent.
SET @uk_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'connection_oauth_values'
    AND INDEX_NAME = 'uk_connection_oauth_connection'
);
SET @ddl := IF(@uk_exists = 0,
  'ALTER TABLE `connection_oauth_values` ADD UNIQUE KEY `uk_connection_oauth_connection` (`connection_id`)',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-22  6:20:38

-- ----------------------------------------------------------------------
-- OAuth: add redirect_uri field for profile_id=2 (must match exactly the
-- "Authorized redirect URI" registered in your OAuth provider console
-- e.g. https://api-console.zoho.com/  -> Edit client -> Authorized Redirect URIs
-- For local Tomcat development the typical value is:
--   http://localhost:8080/authPr/api/connection/callback
-- Zoho explicitly allows http://localhost (any port). For non-localhost
-- hosts Zoho requires HTTPS.
-- ----------------------------------------------------------------------
INSERT INTO `profile_fields`
  (`profile_id`,`key`,`label`,`field_type`,`default_value`,`is_custom`,`placement`,`position`)
VALUES
  (2,'redirect_uri','Redirect URI','text','http://localhost:8080/authPr/api/connection/callback',0,NULL,7)
ON DUPLICATE KEY UPDATE
  `default_value`=VALUES(`default_value`),
  `label`=VALUES(`label`);
