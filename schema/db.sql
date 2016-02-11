CREATE DATABASE  IF NOT EXISTS `autoscheduler` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `autoscheduler`;
-- MySQL dump 10.13  Distrib 5.6.17, for Win32 (x86)
--
-- Host: 127.0.0.1    Database: autoscheduler
-- ------------------------------------------------------
-- Server version	5.6.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `appointment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `booking_request_id` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `start_time` double DEFAULT NULL,
  `end_time` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_booking_id_idx` (`booking_request_id`),
  CONSTRAINT `fk_booking_appointment` FOREIGN KEY (`booking_request_id`) REFERENCES `booking_request` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment`
--

LOCK TABLES `appointment` WRITE;
/*!40000 ALTER TABLE `appointment` DISABLE KEYS */;
/*!40000 ALTER TABLE `appointment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_request`
--

DROP TABLE IF EXISTS `booking_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `booking_request` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `service` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  `day` varchar(45) DEFAULT NULL,
  `time` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_service_idx` (`service`),
  KEY `fk_service1_idx` (`service`),
  CONSTRAINT `FK_Service_id` FOREIGN KEY (`service`) REFERENCES `service_configuration` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=8574 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_request`
--

LOCK TABLES `booking_request` WRITE;
/*!40000 ALTER TABLE `booking_request` DISABLE KEYS */;
/*!40000 ALTER TABLE `booking_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_configuration`
--

DROP TABLE IF EXISTS `service_configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_configuration` (
  `id` int(11) NOT NULL,
  `service` varchar(100) DEFAULT NULL,
  `historical_average` double DEFAULT NULL,
  `duration` double DEFAULT NULL,
  `current_week_avg` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_configuration`
--

LOCK TABLES `service_configuration` WRITE;
/*!40000 ALTER TABLE `service_configuration` DISABLE KEYS */;
INSERT INTO `service_configuration` VALUES (1,'Teeth Whitening - Basic',0.064150943,1.5,1),(2,'Dental Bridge',0.06490566,2,1),(3,'Cleaning - Adult',0.068679245,1,0.8359550561539705),(4,'Consultation Denture',0.065660377,4,1),(5,'Consultation Veneer',0.060377358,2.5,1),(6,'Comprehensive Oral Exam',0.068679245,1.5,1),(7,'Crown - Multiple Visits',0.054339622,0.5,0.8361111111042824),(8,'Consultation Implant',0.06490566,1.5,0.8265060239793294),(9,'Consultation Filling',0.06490566,2,1),(10,'Crown - Single Visit',0.067924528,2.5,1),(11,'Consultation Root Canal',0.065660377,3,0.8333333333333334),(12,'Consultation Tooth Extraction',0.074716981,3.5,0.8212765957370756),(13,'Wisdom Tooth Extraction',0.071698113,1,0.84),(14,'Cleaning - Child',0.068679245,1,0.8344827585750297),(15,'Consultation Toothache',0.074716981,1,1),(16,'Teeth Whitening - Custom',0,1,1);
/*!40000 ALTER TABLE `service_configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_daywise_probability`
--

DROP TABLE IF EXISTS `service_daywise_probability`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_daywise_probability` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `service_id` int(11) DEFAULT NULL,
  `day` varchar(45) DEFAULT NULL,
  `probability` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_day_service_id_idx` (`service_id`),
  CONSTRAINT `fk_day_service_id` FOREIGN KEY (`service_id`) REFERENCES `service_configuration` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_daywise_probability`
--

LOCK TABLES `service_daywise_probability` WRITE;
/*!40000 ALTER TABLE `service_daywise_probability` DISABLE KEYS */;
INSERT INTO `service_daywise_probability` VALUES (1,1,'MON',0),(2,2,'MON',0),(3,3,'MON',0),(4,4,'MON',0),(5,5,'MON',0),(6,6,'MON',0),(7,7,'MON',0),(8,8,'MON',0),(9,9,'MON',0),(10,10,'MON',0),(11,11,'MON',0),(12,12,'MON',0),(13,13,'MON',0),(14,14,'MON',0),(15,15,'MON',0),(16,16,'MON',0),(32,1,'TUE',0),(33,2,'TUE',0),(34,3,'TUE',0),(35,4,'TUE',0),(36,5,'TUE',0),(37,6,'TUE',0),(38,7,'TUE',0),(39,8,'TUE',0),(40,9,'TUE',0),(41,10,'TUE',0),(42,11,'TUE',0),(43,12,'TUE',0),(44,13,'TUE',0),(45,14,'TUE',0),(46,15,'TUE',0),(47,16,'TUE',0),(63,1,'WED',0),(64,2,'WED',0),(65,3,'WED',0),(66,4,'WED',0),(67,5,'WED',0),(68,6,'WED',0),(69,7,'WED',0),(70,8,'WED',0),(71,9,'WED',0),(72,10,'WED',0),(73,11,'WED',0),(74,12,'WED',0),(75,13,'WED',0),(76,14,'WED',0),(77,15,'WED',0),(78,16,'WED',0),(94,1,'THU',0),(95,2,'THU',0),(96,3,'THU',0),(97,4,'THU',0),(98,5,'THU',0),(99,6,'THU',0),(100,7,'THU',0),(101,8,'THU',0),(102,9,'THU',0),(103,10,'THU',0),(104,11,'THU',0),(105,12,'THU',0),(106,13,'THU',0),(107,14,'THU',0),(108,15,'THU',0),(109,16,'THU',0),(125,1,'FRI',0),(126,2,'FRI',0),(127,3,'FRI',0),(128,4,'FRI',0),(129,5,'FRI',0),(130,6,'FRI',0),(131,7,'FRI',0),(132,8,'FRI',0),(133,9,'FRI',0),(134,10,'FRI',0),(135,11,'FRI',0),(136,12,'FRI',0),(137,13,'FRI',0),(138,14,'FRI',0),(139,15,'FRI',0),(140,16,'FRI',0),(156,1,'SAT',0),(157,2,'SAT',0),(158,3,'SAT',0),(159,4,'SAT',0),(160,5,'SAT',0),(161,6,'SAT',0),(162,7,'SAT',0),(163,8,'SAT',0),(164,9,'SAT',0),(165,10,'SAT',0),(166,11,'SAT',0),(167,12,'SAT',0),(168,13,'SAT',0),(169,14,'SAT',0),(170,15,'SAT',0),(171,16,'SAT',0),(187,1,'SUN',0),(188,2,'SUN',0),(189,3,'SUN',0),(190,4,'SUN',0),(191,5,'SUN',0),(192,6,'SUN',0),(193,7,'SUN',0),(194,8,'SUN',0),(195,9,'SUN',0),(196,10,'SUN',0),(197,11,'SUN',0),(198,12,'SUN',0),(199,13,'SUN',0),(200,14,'SUN',0),(201,15,'SUN',0),(202,16,'SUN',0);
/*!40000 ALTER TABLE `service_daywise_probability` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-02-11 12:45:41
