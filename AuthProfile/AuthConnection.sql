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
  `access_token` text,
  `refresh_token` text,
  `expires_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `connection_oauth_values`
--

LOCK TABLES `connection_oauth_values` WRITE;
/*!40000 ALTER TABLE `connection_oauth_values` DISABLE KEYS */;
INSERT INTO `connection_oauth_values` VALUES (40,'1000.58772d0478cbc21bdfbabed9c6deb25f.6af3064cfb680e575418eb7accd75959','1000.e9046d49ce53c02e9fc4a881f4b22fca.e51475a04fcdfea8c75ac040647262b1','2026-05-07 15:33:00','2026-05-06 12:02:42'),(49,'1000.3e46fcf688e2b1b6f3a86bf0401cc44d.e38cb44fc0b608bdd4039ca83b00366f','1000.eb333373e4d6ddb35f960253cb3b7e5f.fb67e1f9e3d3cb02cb376f2977296e9c','2026-05-07 15:44:14','2026-05-07 09:14:03');
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
  `field_id` int NOT NULL,
  `key` varchar(80) DEFAULT NULL,
  `value` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `field_id` (`field_id`),
  CONSTRAINT `connection_values_ibfk_2` FOREIGN KEY (`field_id`) REFERENCES `profile_fields` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `connection_values`
--

LOCK TABLES `connection_values` WRITE;
/*!40000 ALTER TABLE `connection_values` DISABLE KEYS */;
INSERT INTO `connection_values` VALUES (20,80,'username','{\"username\":\"Krithvi\",\"password\":\"Shai\"}','2026-05-07 06:01:53');
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
  `value_type` enum('VALUES','OAUTH') NOT NULL,
  `value_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `profile_id` (`profile_id`),
  KEY `idx_value_id` (`value_id`),
  KEY `idx_value_ref` (`value_type`,`value_id`),
  CONSTRAINT `connections_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `connections`
--

LOCK TABLES `connections` WRITE;
/*!40000 ALTER TABLE `connections` DISABLE KEYS */;
INSERT INTO `connections` VALUES (73,12,0,'Zoho Flow Oauth_v2 - connection_clone','active','OAUTH',40,'2026-05-06 12:02:56'),(75,23,0,'Basic auth - connection','active','VALUES',20,'2026-05-07 06:01:53'),(84,25,0,'CRM -v2 - connection','active','OAUTH',49,'2026-05-07 09:14:03');
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
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_fields`
--

LOCK TABLES `profile_fields` WRITE;
/*!40000 ALTER TABLE `profile_fields` DISABLE KEYS */;
INSERT INTO `profile_fields` VALUES (1,1,'username',NULL,'text','john_doe',0,NULL,1,'2026-04-20 08:29:42'),(2,1,'password',NULL,'password','password123',0,NULL,2,'2026-04-20 08:29:42'),(3,2,'client_id',NULL,'text','oauth-client-id-123',0,NULL,1,'2026-04-20 08:29:42'),(4,2,'client_secret',NULL,'password','oauth-secret-xyz',0,NULL,2,'2026-04-20 08:29:42'),(5,2,'authorization_url',NULL,'text','https://auth.example.com',0,NULL,3,'2026-04-20 08:29:42'),(6,2,'access_token_url',NULL,'text','https://token.example.com',0,NULL,4,'2026-04-20 08:29:42'),(7,2,'scopes',NULL,'text','',0,NULL,5,'2026-04-20 08:29:42'),(8,2,'token_placement',NULL,'text','header',0,'header',6,'2026-04-20 08:29:42'),(9,3,'x-api-key','My API Key','password','my-secret-api-key-xyz',1,'header',1,'2026-04-20 08:29:42'),(10,5,'my-key','Secret Token','password','abc123',1,'header',1,'2026-04-20 08:32:30'),(11,6,'api-key','API KEY','password','',1,'header',1,'2026-04-20 08:34:20'),(13,8,'api','api','text','',1,'header',1,'2026-04-21 09:48:42'),(14,9,'username',NULL,'text','u',0,NULL,1,'2026-04-21 09:49:05'),(15,9,'password',NULL,'password','p',0,NULL,2,'2026-04-21 09:49:05'),(19,11,'client_id',NULL,'text','1000.EGYDPPRCNUJO5AM33LD8X73HTO0VDK',0,NULL,1,'2026-04-22 07:04:45'),(20,11,'client_secret',NULL,'password','3d5d9e491a9f4d001d037dc9a1b8282da810e6c68e',0,NULL,2,'2026-04-22 07:04:45'),(21,11,'authorization_url',NULL,'text','https://accounts.zoho.in /oauth/v2/auth',0,NULL,3,'2026-04-22 07:04:45'),(22,11,'access_token_url',NULL,'text','https://accounts.zoho.in/oauth/v2/token',0,NULL,4,'2026-04-22 07:04:45'),(23,11,'scopes',NULL,'text','Site24x7.Admin.All',0,NULL,5,'2026-04-22 07:04:45'),(24,11,'token_placement',NULL,'text','query',0,'query',6,'2026-04-22 07:04:45'),(31,14,'client_id',NULL,'text','1000.RS15ALN2ZS8GYRT70R7MH90DK69EEG',0,NULL,1,'2026-04-27 06:46:11'),(32,14,'client_secret',NULL,'password','4ebdeee3f4e382cb79101e8be16662809a322f985d',0,NULL,2,'2026-04-27 06:46:11'),(33,14,'authorization_url',NULL,'text','https://accounts.zoho.in/oauth/v2/auth',0,NULL,3,'2026-04-27 06:46:11'),(34,14,'access_token_url',NULL,'text','https://accounts.zoho.in/oauth/v2/token',0,NULL,4,'2026-04-27 06:46:11'),(35,14,'scopes',NULL,'text','Site24x7.Admin.All',0,NULL,5,'2026-04-27 06:46:11'),(36,14,'token_placement',NULL,'text','query',0,'query',6,'2026-04-27 06:46:11'),(37,16,'client_id',NULL,'text','1000.S0LR0WFTYI32WN7BJK46N5OLV1IJVF',0,NULL,1,'2026-04-27 07:09:34'),(38,16,'client_secret',NULL,'password','5f9bd6fb3f2c1ed973dfeba40e46693c5590aafc04',0,NULL,2,'2026-04-27 07:09:34'),(39,16,'authorization_url',NULL,'text','https://accounts.zoho.in/oauth/v2/auth',0,NULL,3,'2026-04-27 07:09:34'),(40,16,'access_token_url',NULL,'text','https://accounts.zoho.in/oauth/v2/token',0,NULL,4,'2026-04-27 07:09:34'),(41,16,'scopes',NULL,'text','ZohoCRM.modules.CREATE',0,NULL,5,'2026-04-27 07:09:34'),(42,16,'token_placement',NULL,'text','query',0,'query',6,'2026-04-27 07:09:34'),(43,17,'client_id',NULL,'text','1000.RWWSP678Z535BVT32I5BWL68AIIICJ',0,NULL,1,'2026-04-27 07:16:17'),(44,17,'client_secret',NULL,'password','d9c501dbad7c15636fc7d58e01f1eba2fa7fec9308',0,NULL,2,'2026-04-27 07:16:17'),(45,17,'authorization_url',NULL,'text','https://accounts.zoho.in/oauth/v2/auth',0,NULL,3,'2026-04-27 07:16:17'),(46,17,'access_token_url',NULL,'text','https://accounts.zoho.in/oauth/v2/token',0,NULL,4,'2026-04-27 07:16:17'),(47,17,'scopes',NULL,'text','ZohoCRM.modules.READ',0,NULL,5,'2026-04-27 07:16:17'),(48,17,'token_placement',NULL,'text','query',0,'query',6,'2026-04-27 07:16:17'),(67,18,'client_id',NULL,'text','Ov23liAfLRzKVuhrXayV',0,NULL,1,'2026-04-27 09:52:14'),(68,18,'client_secret',NULL,'password','1989b1300ed2215ba1374f68451e5a77db9871df',0,NULL,2,'2026-04-27 09:52:14'),(69,18,'authorization_url',NULL,'text','https://github.com/login/oauth/authorize',0,NULL,3,'2026-04-27 09:52:14'),(70,18,'access_token_url',NULL,'text','https://github.com/login/oauth/access_token',0,NULL,4,'2026-04-27 09:52:14'),(71,18,'scopes',NULL,'text','read:user user:email',0,NULL,5,'2026-04-27 09:52:14'),(72,18,'token_placement',NULL,'text','query',0,'query',6,'2026-04-27 09:52:14'),(73,20,'x-api-key','API KEY ','password','',1,'header',1,'2026-04-27 10:27:08'),(74,12,'client_id',NULL,'text','1000.RWWSP678Z535BVT32I5BWL68AIIICJ',0,NULL,1,'2026-04-27 10:39:04'),(75,12,'client_secret',NULL,'password','d9c501dbad7c15636fc7d58e01f1eba2fa7fec9308',0,NULL,2,'2026-04-27 10:39:04'),(76,12,'authorization_url',NULL,'text','https://accounts.zoho.in/oauth/v2/auth',0,NULL,3,'2026-04-27 10:39:04'),(77,12,'access_token_url',NULL,'text','https://accounts.zoho.in/oauth/v2/token',0,NULL,4,'2026-04-27 10:39:04'),(78,12,'scopes',NULL,'text','Site24x7.Admin.All',0,NULL,5,'2026-04-27 10:39:04'),(79,12,'token_placement',NULL,'text','query',0,'query',6,'2026-04-27 10:39:04'),(80,23,'username',NULL,'text','',0,NULL,1,'2026-04-27 12:36:45'),(81,23,'password',NULL,'password','',0,NULL,2,'2026-04-27 12:36:45'),(82,24,'client_id',NULL,'text','1000.S0LR0WFTYI32WN7BJK46N5OLV1IJVF',0,NULL,1,'2026-04-28 10:30:24'),(83,24,'client_secret',NULL,'password','5f9bd6fb3f2c1ed973dfeba40e46693c5590aafc04',0,NULL,2,'2026-04-28 10:30:24'),(84,24,'authorization_url',NULL,'text','https://accounts.zoho.in/oauth/v2/auth',0,NULL,3,'2026-04-28 10:30:24'),(85,24,'access_token_url',NULL,'text','https://accounts.zoho.in/oauth/v2/token',0,NULL,4,'2026-04-28 10:30:24'),(86,24,'scopes',NULL,'text','ZohoCRM.settings.ALL',0,NULL,5,'2026-04-28 10:30:24'),(87,24,'token_placement',NULL,'text','header',0,'header',6,'2026-04-28 10:30:24'),(88,25,'client_id',NULL,'text','1000.S0LR0WFTYI32WN7BJK46N5OLV1IJVF',0,NULL,1,'2026-05-07 09:08:42'),(89,25,'client_secret',NULL,'password','5f9bd6fb3f2c1ed973dfeba40e46693c5590aafc04',0,NULL,2,'2026-05-07 09:08:42'),(90,25,'authorization_url',NULL,'text','https://accounts.zoho.in/oauth/v2/auth',0,NULL,3,'2026-05-07 09:08:42'),(91,25,'access_token_url',NULL,'text','https://accounts.zoho.in/oauth/v2/token',0,NULL,4,'2026-05-07 09:08:42'),(92,25,'scopes',NULL,'text','ZohoCRM.users.ALL',0,NULL,5,'2026-05-07 09:08:42'),(93,25,'token_placement',NULL,'text','header',0,'header',6,'2026-05-07 09:08:42');
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
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profiles`
--

LOCK TABLES `profiles` WRITE;
/*!40000 ALTER TABLE `profiles` DISABLE KEYS */;
INSERT INTO `profiles` VALUES (1,'Basic Auth Profile',1,1,101,0,'2026-04-20 08:29:42'),(2,'OAuth2 Profile',2,1,101,0,'2026-04-20 08:29:42'),(3,'API Key Profile',3,1,101,0,'2026-04-20 08:29:42'),(4,'No Auth Profile',5,1,101,0,'2026-04-20 08:29:42'),(5,'RoundTripTest',3,1,NULL,0,'2026-04-20 08:32:30'),(6,'auth - API',3,1,NULL,0,'2026-04-20 08:34:20'),(7,'API-key2',1,4,NULL,0,'2026-04-20 09:05:43'),(8,'api',3,1,NULL,0,'2026-04-21 09:48:42'),(9,'TEST_DEBUG',1,1,NULL,0,'2026-04-21 09:49:05'),(10,'API-Key ',1,2,NULL,0,'2026-04-21 10:13:08'),(11,'Zoho Outh',2,1,NULL,0,'2026-04-22 07:04:45'),(12,'Zoho Flow Oauth_v2',2,2,NULL,1,'2026-04-27 06:20:12'),(13,'Api console -Oauth v2',1,1,NULL,1,'2026-04-27 06:45:08'),(14,'Oauth v2',2,1,NULL,1,'2026-04-27 06:46:11'),(16,'Oauth V2 flow',2,1,NULL,1,'2026-04-27 07:09:34'),(17,'Gmail Oauth v2',2,1,NULL,1,'2026-04-27 07:16:17'),(18,'git Hub v2',2,4,NULL,1,'2026-04-27 07:34:33'),(20,'API Auth',3,1,NULL,1,'2026-04-27 10:27:08'),(21,'a',1,1,NULL,1,'2026-04-27 10:53:40'),(23,'Basic auth',1,2,NULL,1,'2026-04-27 12:36:45'),(24,'Zoho crm',2,3,NULL,1,'2026-04-28 10:30:24'),(25,'CRM -v2',2,4,NULL,1,'2026-05-07 09:08:42');
/*!40000 ALTER TABLE `profiles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-07 17:13:36
