-- MySQL dump 10.13  Distrib 8.0.18, for Win64 (x86_64)
--
-- Host: localhost    Database: db_diplom
-- ------------------------------------------------------
-- Server version	8.0.18

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
-- Table structure for table `banlist`
--

DROP TABLE IF EXISTS `banlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banlist` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `reason` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `start` datetime NOT NULL,
  `end` datetime NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `bannedby` bigint(20) unsigned NOT NULL,
  `ipaddress` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `bannedby` (`bannedby`),
  CONSTRAINT `banlist_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `banlist_ibfk_2` FOREIGN KEY (`bannedby`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banlist`
--

LOCK TABLES `banlist` WRITE;
/*!40000 ALTER TABLE `banlist` DISABLE KEYS */;
INSERT INTO `banlist` VALUES (3,'Оскорбление администрации','2025-05-16 19:37:00','2025-06-19 19:35:00',12,1,NULL);
/*!40000 ALTER TABLE `banlist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat`
--

DROP TABLE IF EXISTS `chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'chat',
  `subjectid` int(10) unsigned DEFAULT NULL,
  `createdby` bigint(20) unsigned NOT NULL,
  `groupid` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `subjectid` (`subjectid`),
  KEY `createdby` (`createdby`),
  KEY `fk_chat_group_id` (`groupid`),
  CONSTRAINT `chat_ibfk_1` FOREIGN KEY (`subjectid`) REFERENCES `subject` (`id`),
  CONSTRAINT `chat_ibfk_2` FOREIGN KEY (`createdby`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_chat_group_id` FOREIGN KEY (`groupid`) REFERENCES `group` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat`
--

LOCK TABLES `chat` WRITE;
/*!40000 ALTER TABLE `chat` DISABLE KEYS */;
INSERT INTO `chat` VALUES (1,'test chat',NULL,1,NULL),(2,'5head 4aTuK ^_^',3,11,1),(4,'4aT rpy/7/7bl Ay6/7-21-2',2,11,2),(9,'HoBblu 4aT',5,1,2);
/*!40000 ALTER TABLE `chat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `text` varchar(10000) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `date` datetime NOT NULL,
  `chat_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `replyto` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `chat_id` (`chat_id`),
  KEY `user_id` (`user_id`),
  KEY `replyto` (`replyto`),
  CONSTRAINT `chat_message_ibfk_1` FOREIGN KEY (`chat_id`) REFERENCES `chat` (`id`),
  CONSTRAINT `chat_message_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `chat_message_ibfk_3` FOREIGN KEY (`replyto`) REFERENCES `chat_message` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
INSERT INTO `chat_message` VALUES (1,'test','2025-06-05 00:50:20',1,1,NULL),(3,'test4','2025-06-05 14:32:46',1,1,NULL),(4,'769690967','2025-06-05 15:12:08',1,1,NULL),(5,'rtf','2025-06-05 18:04:27',1,11,NULL),(6,'zzz','2025-06-05 18:04:46',1,11,NULL),(7,'777','2025-06-05 18:06:27',1,11,NULL),(8,'6666','2025-06-05 18:07:17',1,11,NULL),(9,'test msg','2025-06-05 18:13:08',1,11,NULL),(10,'test msg2','2025-06-05 18:13:45',1,11,NULL),(11,'test5','2025-06-05 18:23:24',1,11,NULL),(12,'=-=--=','2025-06-05 18:27:05',1,11,NULL),(13,'joepeach','2025-06-05 18:30:00',1,11,NULL),(14,'joepeach','2025-06-05 18:30:11',1,11,NULL),(15,'testetesetetest','2025-06-05 18:30:43',1,11,NULL),(16,'▲','2025-06-05 18:33:34',1,11,NULL),(17,'▲ ▲','2025-06-05 18:34:30',1,11,NULL),(18,'▲ ▲','2025-06-05 18:34:39',1,11,NULL),(19,'▲','2025-06-05 18:34:54',1,11,NULL),(20,'458468','2025-06-05 18:42:45',1,11,NULL),(21,'458468','2025-06-05 18:42:46',1,11,NULL),(22,'458468','2025-06-05 18:43:03',1,11,NULL),(23,'89','2025-06-05 19:41:34',1,11,NULL),(24,'rtf','2025-06-05 19:44:22',1,11,NULL),(25,'zzz','2025-06-05 19:44:37',1,1,NULL),(26,'o/','2025-06-05 23:52:13',2,1,NULL),(27,'o/','2025-06-05 23:52:42',2,11,NULL),(28,'u 4e?','2025-06-05 23:52:59',2,1,NULL),(29,'u Bce','2025-06-05 23:53:09',2,11,NULL),(30,'coo6IIIeHuR He /7oKa3blBa|-oTcR >_<','2025-06-05 23:54:30',2,1,NULL),(31,'BpoDe /7o4uHuJI','2025-06-06 00:42:14',2,1,NULL),(32,'Hu4e He /704uHuJI 6JIaT','2025-06-06 00:44:20',2,1,NULL),(33,'12','2025-06-06 00:44:26',2,11,NULL),(34,'test','2025-06-06 00:44:31',2,1,NULL),(35,'test message','2025-06-06 18:31:11',2,1,NULL),(37,'long message09328582957938275982375923572398','2025-06-07 17:55:16',2,1,NULL),(38,'3205898732956327532756832765238765732657832657832657236573265862387562765328765','2025-06-07 17:55:23',2,1,NULL),(39,'Алё','2025-06-07 23:55:44',2,11,NULL),(40,'o/','2025-06-08 20:01:52',4,11,NULL),(42,'test','2025-06-15 22:31:28',9,1,NULL),(43,'message 1','2025-06-15 22:31:32',9,1,NULL),(49,'gsdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdfdf','2025-06-15 23:09:58',9,11,NULL),(50,': ⠄⠄⠄⠄ ⠄⠄⠄⠄ ⠄⠄⠄⠄\n ⠄⠄⡔⠙⠢⡀⠄⠄⠄⢀⠼⠅⠈⢂⠄⠄⠄⠄\n ⠄⠄⡌⠄⢰⠉⢙⢗⣲⡖⡋⢐⡺⡄⠈⢆⠄⠄⠄\n ⠄⡜⠄⢀⠆⢠⣿⣿⣿⣿⢡⢣⢿⡱⡀⠈⠆⠄⠄\n ⠄⠧⠤⠂⠄⣼⢧⢻⣿⣿⣞⢸⣮⠳⣕⢤⡆⠄⠄\n ⢺⣿⣿⣶⣦⡇⡌⣰⣍⠚⢿⠄⢩⣧⠉⢷⡇⠄⠄\n ⠘⣿⣿⣯⡙⣧⢎⢨⣶⣶⣶⣶⢸⣼⡻⡎⡇⠄⠄\n ⠄⠘⣿⣿⣷⡀⠎⡮⡙⠶⠟⣫⣶⠛⠧⠁⠄⠄⠄\n ⠄⠄⠘⣿⣿⣿⣦⣤⡀⢿⣿⣿⣿⣄⠄⠄⠄⠄⠄\n ⠄⠄⠄⠈⢿⣿⣿⣿⣿⣷⣯⣿⣿⣷⣾⣿⣷⡄⠄\n ⠄⠄⠄⠄⠄⢻⠏⣼⣿⣿⣿⣿⡿⣿⣿⣏⢾⠇⠄\n ⠄⠄⠄⠄⠄⠈⡼⠿⠿⢿⣿⣦⡝⣿⣿⣿⠷⢀⠄\n ⠄⠄⠄⠄⠄⠄⡇⠄⠄⠄⠈⠻⠇⠿⠋⠄⠄⢘⡆\n ⠄⠄⠄⠄⠄⠄⠱⣀⠄⠄⠄⣀⢼⡀⠄⢀⣀⡜⠄\n ⠄⠄⠄⠄⠄⠄⠄⢸⣉⠉⠉⠄⢀⠈⠉⢏⠁⠄⠄\n ⠄⠄⠄⠄⠄⠄⡰⠃⠄⠄⠄⠄⢸⠄⠄⢸⣧⠄⠄\n ⠄⠄⠄⠄⠄⣼⣧⠄⠄⠄⠄⠄⣼⠄⠄⡘⣿⡆⠄\n ⠄⠄⠄⢀⣼⣿⡙⣷⡄⠄⠄⠄⠃⠄⢠⣿⢸⣿⡀\n ⠄⠄⢀⣾⣿⣿⣷⣝⠿⡀⠄⠄⠄⢀⡞⢍⣼⣿⠇\n ⠄⠄⣼⣿⣿⣿⣿⣿⣷⣄⠄⠄⠠⡊⠴⠋⠹⡜⠄\n ⠄⠄⣿⣿⣿⣿⣿⣿⣿⣿⡆⣤⣾⣿⣿⣧⠹⠄⠄\n ⠄⠄⢿⣿⣿⣿⣿⣿⣿⣿⢃⣿⣿⣿⣿⣿⡇⠄⠄\n ⠄⠄⠐⡏⠉⠉⠉⠉⠉⠄⢸⠛⠿⣿⣿⡟⠄⠄⠄\n ⠄⠄⠄⠹⡖⠒⠒⠒⠒⠊⢹⠒⠤⢤⡜⠁⠄⠄⠄\n ⠄⠄⠄⠄⠱⠄⠄⠄⠄⠄⢸⠄⠄⠄⡖⠄⠄⠄⠄','2025-06-15 23:23:27',9,11,NULL),(51,'test long text ','2025-06-15 23:25:14',9,1,NULL),(52,'1\nnformation system, navigation, HTML, CSS, JS, MySQL, Java, Spring Boot, Thymeleaf, Nginx, Janus Gateway\nThis final qualification work is dedicated to the research, design, and development of an online educational platform.\nThe thesis consists of an abstract, an introduction, three chapters, and a conclusion. The work includes 174 pages, 91 figures, 0 tables, 24 code listings, and 16 references.\nDevelopment objects: audio/video communication system, assignment system, journal system.\nThe study conducted in this work focuses on the subject of audio/video online communication.\nThe software developed and presented in this thesis is a fully functional information system\n','2025-06-15 23:28:11',9,1,NULL),(53,'1','2025-06-15 23:48:39',2,11,NULL),(54,'My}|{uKu, 3DapoBa','2025-06-16 18:34:01',2,1,NULL),(55,'Hi','2025-06-16 19:12:51',2,4,NULL),(56,'AdMuH - 4Mo','2025-06-16 19:36:34',9,12,NULL),(57,'JIaDHo, /7oxyu Ha ypoBeHb }|{u3Hu, /7oD phonk 3aTo HaBaJIuM /7o xoxJIaM\n⣿⣿⣿⣿⣻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿\n⣿⣿⣿⣵⣿⣿⣿⠿⡟⣛⣧⣿⣯⣿⣝⡻⢿⣿⣿⣿⣿⣿⣿⣿\n⣿⣿⣿⣿⣿⠋⠁⣴⣶⣿⣿⣿⣿⣿⣿⣿⣦⣍⢿⣿⣿⣿⣿⣿\n⣿⣿⣿⣿⢷⠄⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣏⢼⣿⣿⣿⣿\n⢹⣿⣿⢻⠎⠔⣛⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡏⣿⣿⣿⣿\n⢸⣿⣿⠇⡶⠄⣿⣿⠿⠟⡛⠛⠻⣿⡿⠿⠿⣿⣗⢣⣿⣿⣿⣿\n⠐⣿⣿⡿⣷⣾⣿⣿⣿⣾⣶⣶⣶⣿⣁⣔⣤⣀⣼⢲⣿⣿⣿⣿\n⠄⣿⣿⣿⣿⣾⣟⣿⣿⣿⣿⣿⣿⣿⡿⣿⣿⣿⢟⣾⣿⣿⣿⣿\n⠄⣟⣿⣿⣿⡷⣿⣿⣿⣿⣿⣮⣽⠛⢻⣽⣿⡇⣾⣿⣿⣿⣿⣿\n⠄⢻⣿⣿⣿⡷⠻⢻⡻⣯⣝⢿⣟⣛⣛⣛⠝⢻⣿⣿⣿⣿⣿⣿\n⠄⠸⣿⣿⡟⣹⣦⠄⠋⠻⢿⣶⣶⣶⡾⠃⡂⢾⣿⣿⣿⣿⣿⣿\n⠄⠄⠟⠋⠄⢻⣿⣧⣲⡀⡀⠄⠉⠱⣠⣾⡇⠄⠉⠛⢿⣿⣿⣿\n⠄⠄⠄⠄⠄⠈⣿⣿⣿⣷⣿⣿⢾⣾⣿⣿⣇⠄⠄⠄⠄⠄⠉⠉\n⠄⠄⠄⠄⠄⠄⠸⣿⣿⠟⠃⠄⠄⢈⣻⣿⣿⠄⠄⠄⠄⠄⠄⠄\n⠄⠄⠄⠄⠄⠄⠄⢿⣿⣾⣷⡄⠄⢾⣿⣿⣿⡄⠄⠄⠄⠄⠄⠄\n⠄⠄⠄⠄⠄⠄⠄⠸⣿⣿⣿⠃⠄⠈⢿⣿⣿⠄⠄⠄⠄⠄⠄⠄','2025-06-16 19:48:06',2,4,NULL),(58,'BoT cyka','2025-06-16 19:48:58',9,1,NULL),(59,'3a Bbl/7eHDpe}|{ HaKa}|{y','2025-06-16 19:49:25',9,1,NULL),(62,'test','2025-06-16 23:42:24',2,11,NULL),(63,'////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////','2025-06-16 23:42:35',2,11,NULL),(64,'<><><><<><<>','2025-06-16 23:42:46',2,11,NULL),(65,'Ale','2025-06-16 23:48:28',1,4,NULL),(66,'Ну','2025-06-17 01:04:09',1,4,NULL),(67,'4e TyT /7poucxoDuT\n\n??','2025-06-17 01:30:22',2,14,NULL);
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_user`
--

DROP TABLE IF EXISTS `chat_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `chat_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_user` (`chat_id`,`user_id`),
  KEY `idx_chat_id` (`chat_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_chat_user_chat` FOREIGN KEY (`chat_id`) REFERENCES `chat` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chat_user_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_user`
--

LOCK TABLES `chat_user` WRITE;
/*!40000 ALTER TABLE `chat_user` DISABLE KEYS */;
INSERT INTO `chat_user` VALUES (3,1,1),(1,1,4),(2,1,11),(132,2,1),(133,2,10),(6,2,11),(138,2,14),(13,4,5),(14,4,6),(12,4,11),(11,4,12),(86,9,1),(123,9,5),(122,9,6),(121,9,11),(124,9,12);
/*!40000 ALTER TABLE `chat_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chatfiles`
--

DROP TABLE IF EXISTS `chatfiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chatfiles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `path` varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `message_id` (`message_id`),
  CONSTRAINT `chatfiles_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `chat_message` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chatfiles`
--

LOCK TABLES `chatfiles` WRITE;
/*!40000 ALTER TABLE `chatfiles` DISABLE KEYS */;
/*!40000 ALTER TABLE `chatfiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `completedtasksfiles`
--

DROP TABLE IF EXISTS `completedtasksfiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `completedtasksfiles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `path` varchar(300) NOT NULL,
  `task_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_completedtasksfiles_tasks_completed1_idx` (`task_id`),
  CONSTRAINT `fk_completedtasksfiles_tasks_completed1` FOREIGN KEY (`task_id`) REFERENCES `tasks_completed` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `completedtasksfiles`
--

LOCK TABLES `completedtasksfiles` WRITE;
/*!40000 ALTER TABLE `completedtasksfiles` DISABLE KEYS */;
INSERT INTO `completedtasksfiles` VALUES (14,'src\\main\\files\\tasks_completed\\1\\10\\107screen.png',14),(15,'src\\main\\files\\tasks_completed\\1\\10\\108text.txt',14),(17,'src\\main\\files\\tasks_completed\\1\\4\\10019 Уфимцев Цифровизация Заявка.docx',13),(18,'src\\main\\files\\tasks_completed\\1\\4\\4_1bcklg.xlsx',13),(19,'src\\main\\files\\tasks_completed\\3\\4\\4_1_lab3_(2).ipynb',15),(20,'src\\main\\files\\tasks_completed\\8\\12\\12_1_diplom_edited1506.docx',18);
/*!40000 ALTER TABLE `completedtasksfiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conferences`
--

DROP TABLE IF EXISTS `conferences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conferences` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL DEFAULT 'videoconference',
  `datestart` datetime NOT NULL,
  `dateend` datetime DEFAULT NULL,
  `subject_id` int(10) unsigned NOT NULL,
  `createdby` bigint(20) unsigned NOT NULL,
  `repeatable` tinyint(3) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_conferences_subject1_idx` (`subject_id`),
  KEY `fk_conferences_user1_idx` (`createdby`),
  CONSTRAINT `fk_conferences_subject1` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `fk_conferences_user1` FOREIGN KEY (`createdby`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conferences`
--

LOCK TABLES `conferences` WRITE;
/*!40000 ALTER TABLE `conferences` DISABLE KEYS */;
INSERT INTO `conferences` VALUES (3,'test conf','2025-04-22 19:50:00','2025-08-10 08:12:11',2,1,0),(4,'about math','2025-04-22 21:30:00','2025-09-27 15:02:49',2,1,0),(8,'lold','2025-06-02 16:15:00',NULL,3,1,1);
/*!40000 ALTER TABLE `conferences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group`
--

DROP TABLE IF EXISTS `group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(70) NOT NULL,
  `private` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group`
--

LOCK TABLES `group` WRITE;
/*!40000 ALTER TABLE `group` DISABLE KEYS */;
INSERT INTO `group` VALUES (1,'aubp-21-1',0),(2,'aubp-21-2',0),(3,'inf-23',0),(4,'bio-20',0),(5,'infm-23',0),(6,'aubp-24',0),(7,'aubp-20-1',0),(8,'aubp-20-2',0);
/*!40000 ALTER TABLE `group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_has_conferences`
--

DROP TABLE IF EXISTS `group_has_conferences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_has_conferences` (
  `group_id` int(10) unsigned NOT NULL,
  `conferences_id` bigint(20) NOT NULL,
  PRIMARY KEY (`group_id`,`conferences_id`),
  KEY `fk_group_has_conferences_conferences1_idx` (`conferences_id`),
  KEY `fk_group_has_conferences_group1_idx` (`group_id`),
  CONSTRAINT `fk_group_has_conferences_conferences1` FOREIGN KEY (`conferences_id`) REFERENCES `conferences` (`id`),
  CONSTRAINT `fk_group_has_conferences_group1` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_has_conferences`
--

LOCK TABLES `group_has_conferences` WRITE;
/*!40000 ALTER TABLE `group_has_conferences` DISABLE KEYS */;
INSERT INTO `group_has_conferences` VALUES (1,3),(1,8),(2,3),(2,4),(2,8),(3,8);
/*!40000 ALTER TABLE `group_has_conferences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_has_user`
--

DROP TABLE IF EXISTS `group_has_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_has_user` (
  `group_id` int(10) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`group_id`,`user_id`),
  KEY `fk_group_has_user_user1_idx` (`user_id`),
  KEY `fk_group_has_user_group1_idx` (`group_id`),
  CONSTRAINT `fk_group_has_user_group1` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
  CONSTRAINT `fk_group_has_user_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_has_user`
--

LOCK TABLES `group_has_user` WRITE;
/*!40000 ALTER TABLE `group_has_user` DISABLE KEYS */;
INSERT INTO `group_has_user` VALUES (1,1),(1,4),(1,11),(1,14),(2,5),(2,6),(2,11),(2,12),(3,10);
/*!40000 ALTER TABLE `group_has_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `journal`
--

DROP TABLE IF EXISTS `journal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `journaluser_id` bigint(20) unsigned NOT NULL,
  `journalsubject_id` int(10) unsigned NOT NULL,
  `date` varchar(16) NOT NULL,
  `grade` tinyint(3) unsigned DEFAULT NULL,
  `waspresent` tinyint(1) DEFAULT NULL,
  `tasks_completed_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_journal_user1_idx` (`journaluser_id`),
  KEY `fk_journal_subject1_idx` (`journalsubject_id`),
  KEY `fk_journal_tasks_completed1_idx` (`tasks_completed_id`),
  CONSTRAINT `fk_journal_subject1` FOREIGN KEY (`journalsubject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `fk_journal_tasks_completed1` FOREIGN KEY (`tasks_completed_id`) REFERENCES `tasks_completed` (`id`),
  CONSTRAINT `fk_journal_user1` FOREIGN KEY (`journaluser_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `journal`
--

LOCK TABLES `journal` WRITE;
/*!40000 ALTER TABLE `journal` DISABLE KEYS */;
INSERT INTO `journal` VALUES (2,4,1,'2024-12-20',4,NULL,NULL),(4,4,2,'2024-12-22',NULL,0,NULL),(5,5,2,'2024-12-06',6,1,NULL),(6,6,3,'2024-12-22',NULL,0,NULL),(7,6,2,'2024-12-11',NULL,0,NULL),(8,4,3,'2024-11-15',NULL,1,NULL),(12,5,1,'2024-12-09',3,NULL,NULL),(13,10,1,'2024-12-09',1,NULL,NULL),(14,6,2,'2024-12-10',NULL,0,NULL),(17,4,3,'2025-01-11',4,NULL,NULL),(18,10,3,'2025-01-16',1,0,NULL),(25,4,2,'2025-04-10',5,NULL,13),(26,4,4,'2025-06-18',NULL,0,NULL),(27,5,4,'2025-06-19',8,0,NULL),(28,11,1,'2025-06-26',NULL,1,NULL),(29,11,2,'2025-06-27',NULL,0,NULL),(30,11,3,'2025-06-08',1,NULL,17),(31,12,1,'2025-06-19',NULL,1,NULL),(32,14,3,'2025-06-16',4,NULL,19),(33,14,2,'2025-06-05',0,1,NULL),(34,5,3,'2025-05-26',NULL,1,NULL),(35,6,3,'2025-05-26',NULL,1,NULL),(36,11,3,'2025-05-26',NULL,1,NULL);
/*!40000 ALTER TABLE `journal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_restore_mails`
--

DROP TABLE IF EXISTS `password_restore_mails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_restore_mails` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `datesend` varchar(20) NOT NULL,
  `uuid` varchar(300) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_password_restore_mails_user1_idx` (`user_id`),
  CONSTRAINT `fk_password_restore_mails_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_restore_mails`
--

LOCK TABLES `password_restore_mails` WRITE;
/*!40000 ALTER TABLE `password_restore_mails` DISABLE KEYS */;
INSERT INTO `password_restore_mails` VALUES (12,'2024-12-25 20:00:05','941d6450-4caa-4aa7-9e8a-0f8519b35700',4),(15,'2025-06-17 01:31:44','e469735d-2469-4c52-9d9a-97278a5bcb2b',14);
/*!40000 ALTER TABLE `password_restore_mails` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `power` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'ROLE_STUDENT',20),(2,'ROLE_TEACHER',60),(3,'ROLE_ADMIN',100),(6,'ROLE_GROUPLEADER',30),(7,'ROLE_MODERATOR',30);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_has_user`
--

DROP TABLE IF EXISTS `role_has_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_has_user` (
  `role_id` int(11) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`role_id`,`user_id`),
  KEY `fk_role_has_user_user1_idx` (`user_id`),
  KEY `fk_role_has_user_role1_idx` (`role_id`),
  CONSTRAINT `fk_role_has_user_role1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `fk_role_has_user_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_has_user`
--

LOCK TABLES `role_has_user` WRITE;
/*!40000 ALTER TABLE `role_has_user` DISABLE KEYS */;
INSERT INTO `role_has_user` VALUES (1,4),(1,5),(1,6),(1,10),(1,11),(1,12),(1,14),(2,1),(2,7),(2,8),(2,9),(3,1);
/*!40000 ALTER TABLE `role_has_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subject`
--

DROP TABLE IF EXISTS `subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subject`
--

LOCK TABLES `subject` WRITE;
/*!40000 ALTER TABLE `subject` DISABLE KEYS */;
INSERT INTO `subject` VALUES (1,'math'),(2,'psychology'),(3,'computer science'),(4,'testsubject'),(5,'none-named');
/*!40000 ALTER TABLE `subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `datestart` datetime NOT NULL,
  `dateend` datetime NOT NULL,
  `text` varchar(10000) NOT NULL,
  `createdby` bigint(20) unsigned NOT NULL,
  `tasksubject_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tasks_users1_idx` (`createdby`),
  KEY `fk_tasks_subject1_idx` (`tasksubject_id`),
  CONSTRAINT `fk_tasks_subject1` FOREIGN KEY (`tasksubject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `fk_tasks_users1` FOREIGN KEY (`createdby`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks`
--

LOCK TABLES `tasks` WRITE;
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
INSERT INTO `tasks` VALUES (1,'task nomer 1','2024-12-28 15:25:00','2024-12-30 14:59:00',' random text for first task. Need to check how big text displaying on page. Pe6RTa, DoMaIIIHee 3aDaHue, cJIyIIIaeM: He/7epeBoDuMaR urpa cJIoB',1,2),(2,'ycTaHoBuTb /7porpaMMy','2024-12-19 10:10:00','2024-12-30 18:00:00','install notepad++',1,1),(3,'Task3.ML','2025-04-16 21:30:00','2025-04-30 21:29:00','U will have to do retarded task about fucking machine learning and u won\'t get any tips. Enjoy :P',1,3),(8,'HoBoe 3aDaHue','2025-04-18 15:30:00','2025-05-10 20:29:00','Исправить в use case неправильное использование include\nИсправить в активностях недочёты (посмотреть где забыл соединить, посмотреть управление заданиями преподавателя)\n6blblblblblcTpa 6JIRTb!!!!!!!!!!',1,3),(10,'3aDaHue c /7oDBoxou','2025-06-16 22:08:00','2025-06-26 22:08:00','O/7ucaHuR He 6yDeT. /7o4eMy? /7oTomy',1,5);
/*!40000 ALTER TABLE `tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks_completed`
--

DROP TABLE IF EXISTS `tasks_completed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks_completed` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tasks_id` bigint(20) unsigned NOT NULL,
  `dateofsubmit` varchar(16) NOT NULL,
  `grade` smallint(6) DEFAULT NULL,
  `commentary` varchar(10000) DEFAULT NULL,
  `feedback` varchar(10000) DEFAULT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `dateofcheck` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tasks_completed_tasks1_idx` (`tasks_id`),
  KEY `fk_tasks_completed_user1_idx` (`user_id`),
  CONSTRAINT `fk_tasks_completed_tasks1` FOREIGN KEY (`tasks_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `fk_tasks_completed_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks_completed`
--

LOCK TABLES `tasks_completed` WRITE;
/*!40000 ALTER TABLE `tasks_completed` DISABLE KEYS */;
INSERT INTO `tasks_completed` VALUES (13,1,'2025-04-10 18:47',5,'something 59','sample text 23',4,'2025-04-10 19:18'),(14,1,'2025-04-15 23:57',2,'переделал, чё дальше?','переделывай',10,'04.04.2025 23:55'),(15,3,'2025-04-17 15:04',NULL,'Hy Tu/7a cDeJIaJI, u 4e?','',4,''),(16,1,'2025-05-21 19:51',NULL,'A /7o4eMy Hu4ero HeT??','',11,''),(17,8,'2025-06-7 23:56',1,'He xo4y He 6yDy!!!','HeT, 6yDeIIIb',11,'2025-06-8 00:27'),(18,8,'2025-06-16 19:45',NULL,'all done','',12,''),(19,3,'2025-06-16 22:00',4,'Исследование популярных образовательных онлайн-систем помогло выявить наиболее распространённые проблемы, с которыми сталкиваются как студенты, так и преподаватели:\n1.В системах могут отсутствовать необходимые модули, такие как модуль конференций, электронный журнал и т.д.\n2.Многие системы обладают избыточным функционалом, не используемым в образовательном процессе, что негативно сказывается на стабильности работы и дальнейшей поддержке. \n3.Далеко не на всех платформах имеются действительно полные инструменты для управления пользователями, например, не везде есть возможность управлять участниками видеоконференции.\n','JIaDHo, /7ouDeT',14,'2025-06-16 22:01');
/*!40000 ALTER TABLE `tasks_completed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks_has_group`
--

DROP TABLE IF EXISTS `tasks_has_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks_has_group` (
  `tasks_id` bigint(20) unsigned NOT NULL,
  `group_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`tasks_id`,`group_id`),
  KEY `fk_tasks_has_group_group1_idx` (`group_id`),
  KEY `fk_tasks_has_group_tasks1_idx` (`tasks_id`),
  CONSTRAINT `fk_tasks_has_group_group1` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
  CONSTRAINT `fk_tasks_has_group_tasks1` FOREIGN KEY (`tasks_id`) REFERENCES `tasks` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks_has_group`
--

LOCK TABLES `tasks_has_group` WRITE;
/*!40000 ALTER TABLE `tasks_has_group` DISABLE KEYS */;
INSERT INTO `tasks_has_group` VALUES (1,1),(3,1),(8,1),(10,1),(3,2),(8,2),(10,2),(1,3),(3,3);
/*!40000 ALTER TABLE `tasks_has_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasksfiles`
--

DROP TABLE IF EXISTS `tasksfiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasksfiles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) unsigned NOT NULL,
  `path` varchar(300) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tasksfiles_tasks1_idx` (`task_id`),
  CONSTRAINT `fk_tasksfiles_tasks1` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasksfiles`
--

LOCK TABLES `tasksfiles` WRITE;
/*!40000 ALTER TABLE `tasksfiles` DISABLE KEYS */;
INSERT INTO `tasksfiles` VALUES (1,3,'src\\main\\files\\tasks\\3\\1\\1_1_drivers_Manuals_S_smartbuy-sbt-dt9201a_instrukcia_233135_24072024.pdf'),(3,8,'src\\main\\files\\tasks\\8\\1\\1_2_9.png'),(4,8,'src\\main\\files\\tasks\\8\\1\\1_3_22_2__1.drawio'),(6,10,'src\\main\\files\\tasks\\10\\1\\1_1_diplom.txt');
/*!40000 ALTER TABLE `tasksfiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `login` varchar(100) NOT NULL,
  `password` varchar(200) NOT NULL,
  `firstname` varchar(150) NOT NULL,
  `lastname` varchar(150) NOT NULL,
  `surname` varchar(150) NOT NULL,
  `dateofbirth` varchar(10) NOT NULL,
  `email` varchar(100) NOT NULL,
  `qwestion` enum('PETNAME','MOMLNAME','RESERVEPW') NOT NULL,
  `qwestionanswer` varchar(200) NOT NULL,
  `studentcard` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admsrg','$argon2id$v=19$m=65536,t=3,p=2$9gE2AxOAnGr6ykKTO57Q5g$dQcp7wzCm/66R4OMrAsY0MoR3z6xoZVEzL86n4dsphE','sergey','kruglov','','2003-01-12','fake@mail.com','RESERVEPW','12344555','89'),(4,'admin','$argon2id$v=19$m=65536,t=3,p=2$DxAw0AoaoDEEk5bR2DSXOg$YuNukFu/Vd/XvjLaZEaNlg7G84kPkZhKSzOor7CY9xs','anton','chernov','sergeevich','2024-11-26','egor.ufimtsef@ya.ru','RESERVEPW','--','26'),(5,'student1','$argon2id$v=19$m=65536,t=3,p=2$n3AJnTZerj+fBrjwafdHYQ$qmW4cozdBB0cvgbqu+pJFV2meAnHKZO2/GSIQU3JkXE','vladimir','petrov','semenovich','2008-09-09','mail@mail.ru','MOMLNAME','79','3'),(6,'student2','$argon2id$v=19$m=65536,t=3,p=2$R6FQjxdFG2pSxcwYu63riA$DXQKEHYMAq7OBSYNp6o3OSCno68NAJ8R9hYMcSc0lYE','boris','chervanev','andreevich','2007-12-21','egor.ufimtsef@ya.ru','PETNAME','4535','11'),(7,'tchr1','$argon2id$v=19$m=65536,t=3,p=2$AotJU2lIuAjTNoQF3p8eQg$F0U84ESg7JbVxet+5qNlYEOCnVwiyF1uqiomIPg1YoU','leonid','lomov','','1991-06-06','mail@mail.ru','PETNAME','1111',NULL),(8,'tchr2','$argon2id$v=19$m=65536,t=3,p=2$VPT/gTs2r9QzPhJzpC8QFg$eHq1k2aHy/J5C3lKk8+jN/6aARN+kPNLxXmYZSHLCqs','aleksandr','pocahontov','dmitrovich','1969-05-04','testbox@mail.ru','RESERVEPW','111',NULL),(9,'tchr3','$argon2id$v=19$m=65536,t=3,p=2$DqrGZ5QrOplCBtI5NQxNNQ$BJnGqFrXW6Lxqg8AChDMBcqupZqI8PLo7dWo/p6urt8','ivan','erohin','','1998-02-10','mail@mail.ru','RESERVEPW','1111',NULL),(10,'stud3','$argon2id$v=19$m=65536,t=3,p=2$UgCPwYxqnhnJwFh44XEG2Q$PYBWoh9TYmZFBTzjsbydJHGjEMmAZGTIESG9u1cwEPY','vlad','suchev','denisovich','2005-11-10','egor.ufimtsef@ya.ru','MOMLNAME','435','9090'),(11,'admin1','$argon2id$v=19$m=65536,t=3,p=2$thrqlQz3uDQAu8+gNlElOw$2SHxvZ3U1A9FS1qRFxwJP6R+tCJFxbEw3O1CpJPS96I','maksim','sakharov','denisovich','2002-06-04','dezertir.106245@gmail.com','RESERVEPW','1111','9090'),(12,'user5','$argon2id$v=19$m=65536,t=3,p=2$3EMCuEEtNgI9nT9k/i7zSA$3gu47pyB9a54w/Ik7MrAOz43uUyCFNjWc5hdF8SJhtg','name','undefined','','2025-05-16','-','RESERVEPW','0000','98'),(14,'vov4ik','$argon2id$v=19$m=65536,t=3,p=2$9lQcQ9xhLOJ0ZS2RONkI9A$80lxNrbfiIS5w5oR4JEM14JwFMVh2rJFFPnIz2tDyxk','vova','vist','','2002-07-24','egor.ufimtseff@gmail.com','RESERVEPW','1111','60859');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userfiles`
--

DROP TABLE IF EXISTS `userfiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `userfiles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `path` varchar(300) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `type` enum('AVATAR','CSS','SETTINGS','OTHER') NOT NULL DEFAULT 'OTHER',
  PRIMARY KEY (`id`),
  KEY `fk_userfiles_user1_idx` (`user_id`),
  CONSTRAINT `fk_userfiles_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userfiles`
--

LOCK TABLES `userfiles` WRITE;
/*!40000 ALTER TABLE `userfiles` DISABLE KEYS */;
INSERT INTO `userfiles` VALUES (1,'src/main/files/user/profile/avatar_1.jpg',1,'AVATAR'),(4,'src/main/files/user/profile/avatar_10.jpg',10,'AVATAR'),(5,'src/main/files/user/profile/avatar_7.jpg',7,'AVATAR'),(8,'src/main/files/user/profile/avatar_4.jpg',4,'AVATAR'),(9,'src/main/files/user/profile/avatar_11.jpg',11,'AVATAR'),(12,'src/main/files/user/profile/avatar_14.jpg',14,'AVATAR');
/*!40000 ALTER TABLE `userfiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videocall_chat`
--

DROP TABLE IF EXISTS `videocall_chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `videocall_chat` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `videocalluser_id` bigint(20) unsigned NOT NULL,
  `message` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text',
  `date` datetime NOT NULL,
  `replyto` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `videocalluser_id` (`videocalluser_id`),
  KEY `fk_reply_to_user` (`replyto`),
  CONSTRAINT `fk_reply_to_user` FOREIGN KEY (`replyto`) REFERENCES `videocalls_has_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `videocall_chat_ibfk_1` FOREIGN KEY (`videocalluser_id`) REFERENCES `videocalls_has_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videocall_chat`
--

LOCK TABLES `videocall_chat` WRITE;
/*!40000 ALTER TABLE `videocall_chat` DISABLE KEYS */;
/*!40000 ALTER TABLE `videocall_chat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videocall_has_user_properties`
--

DROP TABLE IF EXISTS `videocall_has_user_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `videocall_has_user_properties` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `videocall_has_user_id` bigint(20) unsigned NOT NULL,
  `micromuted` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `cameramuted` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `demomuted` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `banned` tinyint(1) NOT NULL DEFAULT '0',
  `soundmuted` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `videocall_has_user_id` (`videocall_has_user_id`),
  CONSTRAINT `videocall_has_user_properties_ibfk_1` FOREIGN KEY (`videocall_has_user_id`) REFERENCES `videocalls_has_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videocall_has_user_properties`
--

LOCK TABLES `videocall_has_user_properties` WRITE;
/*!40000 ALTER TABLE `videocall_has_user_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `videocall_has_user_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videocalls`
--

DROP TABLE IF EXISTS `videocalls`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `videocalls` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conferences_id` bigint(20) NOT NULL,
  `room_id` bigint(20) unsigned DEFAULT NULL,
  `participants` smallint(5) unsigned NOT NULL DEFAULT '0',
  `session_id` bigint(20) unsigned DEFAULT NULL,
  `handle_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_videocalls_conferences1_idx` (`conferences_id`),
  CONSTRAINT `fk_videocalls_conferences1` FOREIGN KEY (`conferences_id`) REFERENCES `conferences` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=403 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videocalls`
--

LOCK TABLES `videocalls` WRITE;
/*!40000 ALTER TABLE `videocalls` DISABLE KEYS */;
/*!40000 ALTER TABLE `videocalls` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videocalls_has_user`
--

DROP TABLE IF EXISTS `videocalls_has_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `videocalls_has_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `videocalls_id` bigint(20) NOT NULL,
  `videocalluser_id` bigint(20) unsigned NOT NULL,
  `microstate` enum('ON','OFF','MUTED_BY_ADMIN') NOT NULL DEFAULT 'OFF',
  `camstate` enum('ON','OFF','MUTED_BY_ADMIN') NOT NULL DEFAULT 'OFF',
  `signalstate` tinyint(1) NOT NULL DEFAULT '0',
  `connected` tinyint(1) DEFAULT '0',
  `soundstate` enum('ON','OFF','MUTED_BY_ADMIN') NOT NULL DEFAULT 'ON',
  `demostate` enum('ON','OFF','MUTED_BY_ADMIN') NOT NULL DEFAULT 'OFF',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_call_user` (`videocalls_id`,`videocalluser_id`),
  UNIQUE KEY `uc_videocall_user` (`videocalls_id`,`videocalluser_id`),
  KEY `fk_videocalls_has_user_user1_idx` (`videocalluser_id`),
  KEY `fk_videocalls_has_user_videocalls1_idx` (`videocalls_id`),
  CONSTRAINT `fk_videocalls_has_user_user1` FOREIGN KEY (`videocalluser_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_videocalls_has_user_videocalls1` FOREIGN KEY (`videocalls_id`) REFERENCES `videocalls` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=667 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videocalls_has_user`
--

LOCK TABLES `videocalls_has_user` WRITE;
/*!40000 ALTER TABLE `videocalls_has_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `videocalls_has_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-17  1:49:08
