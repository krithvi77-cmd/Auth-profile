-- MySQL dump 10.13  Distrib 8.0.45, for Linux (x86_64)
--
-- Host: localhost    Database: auth_profiles
-- ------------------------------------------------------
-- Server version	8.0.45-0ubuntu0.24.04.1

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
-- Table structure for table `connection_values`
--

DROP TABLE IF EXISTS `connection_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `connection_values` (
  `id` int NOT NULL AUTO_INCREMENT,
  `connection_id` int NOT NULL,
  `field_id` int NOT NULL,
  `value` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_connection_field` (`connection_id`,`field_id`),
  KEY `field_id` (`field_id`),
  CONSTRAINT `connection_values_ibfk_1` FOREIGN KEY (`connection_id`) REFERENCES `connections` (`id`) ON DELETE CASCADE,
  CONSTRAINT `connection_values_ibfk_2` FOREIGN KEY (`field_id`) REFERENCES `profile_fields` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `connection_values`
--

LOCK TABLES `connection_values` WRITE;
/*!40000 ALTER TABLE `connection_values` DISABLE KEYS */;
INSERT INTO `connection_values` VALUES (1,1,1,'john_doe','2026-04-17 06:54:02'),(2,1,2,'password123','2026-04-17 06:54:02'),(3,3,7,'my-secret-api-key-xyz','2026-04-17 06:54:05');
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
  `access_token` text,
  `refresh_token` text,
  `expires_at` datetime DEFAULT NULL,
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
INSERT INTO `connections` VALUES (1,1,201,'My Basic Connection','active',NULL,NULL,NULL,'2026-04-17 06:53:58'),(2,2,201,'My OAuth Connection','active','oauth-access-token-abc','oauth-refresh-token-xyz','2026-04-17 13:25:04','2026-04-17 06:53:58'),(3,3,201,'My API Key Connection','active',NULL,NULL,NULL,'2026-04-17 06:53:58'),(4,4,201,'Public API Connection','active',NULL,NULL,NULL,'2026-04-17 06:53:58');
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
  `label` varchar(100) NOT NULL,
  `key` varchar(100) NOT NULL,
  `field_type` enum('text','password','header','query') DEFAULT 'text',
  `is_required` tinyint(1) DEFAULT '0',
  `is_secret` tinyint(1) DEFAULT '0',
  `visible_to_member` tinyint(1) DEFAULT '1',
  `editable_by_member` tinyint(1) DEFAULT '1',
  `default_value` text,
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_profile_key` (`profile_id`,`key`),
  CONSTRAINT `profile_fields_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_fields`
--

LOCK TABLES `profile_fields` WRITE;
/*!40000 ALTER TABLE `profile_fields` DISABLE KEYS */;
INSERT INTO `profile_fields` VALUES (1,1,'Username','username','text',1,0,1,1,NULL,1,'2026-04-17 06:53:27'),(2,1,'Password','password','password',1,1,1,1,NULL,2,'2026-04-17 06:53:27'),(3,2,'Client ID','client_id','text',1,1,0,0,'oauth-client-id-123',1,'2026-04-17 06:53:39'),(4,2,'Client Secret','client_secret','password',1,1,0,0,'oauth-secret-xyz',2,'2026-04-17 06:53:39'),(5,2,'Auth URL','auth_url','text',1,0,0,0,'https://auth.example.com',3,'2026-04-17 06:53:39'),(6,2,'Token URL','token_url','text',1,0,0,0,'https://token.example.com',4,'2026-04-17 06:53:39'),(7,3,'API Key','api_key','text',1,1,1,1,NULL,1,'2026-04-17 06:53:46');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profiles`
--

LOCK TABLES `profiles` WRITE;
/*!40000 ALTER TABLE `profiles` DISABLE KEYS */;
INSERT INTO `profiles` VALUES (1,'Basic Auth Profile',1,1,101,1,'2026-04-17 06:53:19'),(2,'OAuth2 Profile',2,1,101,1,'2026-04-17 06:53:19'),(3,'API Key Profile',3,1,101,1,'2026-04-17 06:53:19'),(4,'No Auth Profile',5,1,101,1,'2026-04-17 06:53:19');
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

-- Dump completed on 2026-04-17 15:59:12
