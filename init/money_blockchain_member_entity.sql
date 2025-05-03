-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: money_blockchain
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `member_entity`
--

DROP TABLE IF EXISTS `member_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_entity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `student_id` bigint DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_student` (`student_id`),
  CONSTRAINT `fk_student` FOREIGN KEY (`student_id`) REFERENCES `student_entity` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_entity`
--

LOCK TABLES `member_entity` WRITE;
/*!40000 ALTER TABLE `member_entity` DISABLE KEYS */;
INSERT INTO `member_entity` VALUES (7,'김정수','yj001shin@ka.com','010-2255-7148','$2a$10$jFo2jnRORReVcwJPUmMTf.HE3toMrY7i2888YzgzRsOnJ0qyG6HrW',201873977,'STUDENT',NULL,NULL),(8,'이수민','yj001shin@ka.com','010-9012-3897','$2a$10$LX9NYOti/gQdUMJBAunHxuu9x8P95HfUseM1HIUrvHYjwoEO9XC2.',201815341,'STUDENT',NULL,NULL),(9,'양현우','yj001shin@ka.com','010-3166-6072','$2a$10$eiUdQn6.OrvG9mSpDnL4/.m25KpyQU5Mh9LxcU4zYzRxA.M1R7kAq',201891408,'STUDENT',NULL,NULL),(10,'손영일','yj001shin@ka.com','010-9010-9874','$2a$10$WqiftRyABPXhJmXdB/zEqutsBhI.nucIMpne6/JYpO0pXmFPuG95.',201871712,'STUDENT','2025-04-15 02:40:20','2025-04-15 02:40:20'),(11,'김현주','zoqtmxhs@naver.com','010-4934-8664','$2a$10$swWcgFGE1u62Rl.o4szoh.3iy.sBkzsl.4JtAetFRUurEBEQWUlga',201829667,'STUDENT','2025-04-15 03:58:24','2025-04-15 03:58:24');
/*!40000 ALTER TABLE `member_entity` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-20  4:20:53
